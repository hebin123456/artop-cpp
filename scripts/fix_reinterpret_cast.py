#!/usr/bin/env python3
"""批量修复生成的 .cpp 文件中的 reinterpret_cast 指针偏移问题。

问题：旧版代码生成器生成的 eSet 中，reinterpret_cast<TYPE*>(X) 没有先
dynamic_cast<EObjectImpl*> 调整虚基类偏移，导致指针指向错误子对象。
（所有生成类直接继承 EObjectImpl，但 EObject 是虚基类，reinterpret_cast 不调整偏移）

修复：将 reinterpret_cast<TYPE*>(X) 替换为
reinterpret_cast<TYPE*>(dynamic_cast<emf::common::EObjectImpl*>(X))

支持的 X 模式：
  - v[i]            (vector<any> 分支)
  - __o             (reference single EObject* 分支)
  - __e             (EList 迭代器分支)
  - __lst->get(i)   (EList get 分支)

注意：只修复尚未包含 dynamic_cast<EObjectImpl*> 的行。
"""
import os
import re

# 匹配 reinterpret_cast<TYPE>(X) 其中 TYPE 不含 '>'（避免嵌套模板误匹配），
# X 是 v[i] / __o / __e / __lst->get(i) 之一
PATTERN = re.compile(
    r'reinterpret_cast<([^<>]+)>\((v\[\w+\]|__o|__e|__lst->get\(\w+\))\)'
)

def fix_file(path):
    try:
        with open(path, 'r', encoding='utf-8', errors='replace') as f:
            content = f.read()
    except Exception:
        return 0

    count = 0
    def replacer(m):
        nonlocal count
        full = m.group(0)
        # 已修复则跳过
        if 'dynamic_cast<emf::common::EObjectImpl*>' in full:
            return full
        type_str = m.group(1)
        var = m.group(2)
        count += 1
        return f'reinterpret_cast<{type_str}>(dynamic_cast<emf::common::EObjectImpl*>({var}))'

    new_content = PATTERN.sub(replacer, content)

    if count > 0:
        with open(path, 'w', encoding='utf-8') as f:
            f.write(new_content)

    return count

def main():
    base = '/workspace/.build_cache/autosar448_combined'
    total_files = 0
    total_fixes = 0

    for root, dirs, files in os.walk(base):
        for fname in files:
            if not fname.endswith('.cpp'):
                continue
            fpath = os.path.join(root, fname)
            n = fix_file(fpath)
            if n > 0:
                total_files += 1
                total_fixes += n

    print(f'Fixed {total_fixes} reinterpret_cast in {total_files} files')

if __name__ == '__main__':
    main()
