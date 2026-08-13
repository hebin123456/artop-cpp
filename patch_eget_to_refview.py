#!/usr/bin/env python3
# patch_eget_to_refview.py — 批量 patch 生成 .cpp：多值 reference eGet 从 vector 拷贝改为 EObjectRefView 零拷贝
# 匹配模式（revert_eget_to_vector.py 生成的代码）：
#   case XXX__: {
#   std::vector<emf::common::EObject*> __v;
#   if (fieldName) {
#       for (size_t i = 0; i < fieldName->size(); ++i) {
#           __v.push_back(static_cast<emf::common::EObject*>(fieldName->get(i)));
#       }
#   }
#   return std::any(std::move(__v));
#   }
# 替换为：
#   case XXX__: {
#   if (!fieldName) return std::any();
#   auto& __v = fieldName->data();
#   return std::any(emf::common::EObjectRefView(
#       reinterpret_cast<emf::common::EObject* const*>(__v.data()), __v.size()));
#   }
import os, re, sys

COMBINED = "/workspace/.build_cache/autosar448_combined"

# 匹配多值 reference eGet 的 vector 拷贝块
# 捕获 fieldName
PATTERN = re.compile(
    r'std::vector<emf::common::EObject\*> __v;\n'
    r'\s*if \((\w+)\) \{\n'
    r'\s*for \(size_t i = 0; i < \1->size\(\); \+\+i\) \{\n'
    r'\s*__v\.push_back\(static_cast<emf::common::EObject\*>\(\1->get\(i\)\)\);\n'
    r'\s*\}\n'
    r'\s*\}\n'
    r'\s*return std::any\(std::move\(__v\)\);'
)

def repl(m):
    fld = m.group(1)
    return (
        'if (!' + fld + ') return std::any();\n'
        '        auto& __v = ' + fld + '->data();\n'
        '        return std::any(emf::common::EObjectRefView(\n'
        '            reinterpret_cast<const emf::common::EObjectImpl* const*>(__v.data()), __v.size()));'
    )

def main():
    patched_files = 0
    patched_sites = 0
    for root, dirs, files in os.walk(COMBINED):
        for fn in files:
            if not fn.endswith('.cpp'):
                continue
            path = os.path.join(root, fn)
            try:
                with open(path, 'r', encoding='utf-8') as f:
                    content = f.read()
            except:
                continue
            if 'std::vector<emf::common::EObject*> __v;' not in content:
                continue
            new_content, n = PATTERN.subn(repl, content)
            if n > 0:
                with open(path, 'w', encoding='utf-8') as f:
                    f.write(new_content)
                patched_files += 1
                patched_sites += n
    print(f"patched {patched_files} files, {patched_sites} sites")

if __name__ == '__main__':
    main()
