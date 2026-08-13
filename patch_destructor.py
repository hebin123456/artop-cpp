#!/usr/bin/env python3
# patch_destructor.py — 批量 patch 生成 .h/.cpp：析构从 =default 改为 delete EList* 成员
# 修复：codegen 类的 EList<T*>* 成员构造时 new，=default 析构不 delete 导致泄漏。
# .h: ~ClassName() override = default;  →  ~ClassName() override;
# .cpp: 在 eStaticClass 前插入析构实现，delete 所有 EList* 成员
import os, re, sys

COMBINED = "/workspace/.build_cache/autosar448_combined"

# 匹配 .h 中的析构声明
RE_DTOR = re.compile(r'~(\w+)\(\) override = default;')
# 匹配 EList 成员声明：emf::common::EList<...>* fieldName_ = ...
RE_ELIST_MEMBER = re.compile(r'emf::common::EList<.*>\*\s+(\w+)_\s*=')

patched = 0
skipped = 0
for root, dirs, files in os.walk(COMBINED):
    for fn in files:
        if not fn.endswith('.h'):
            continue
        hpath = os.path.join(root, fn)
        try:
            with open(hpath, 'r', encoding='utf-8') as f:
                hcontent = f.read()
        except:
            continue
        m = RE_DTOR.search(hcontent)
        if not m:
            continue
        clsName = m.group(1)
        # 收集 EList 成员
        members = RE_ELIST_MEMBER.findall(hcontent)
        # 替换 .h 析构声明
        new_h = RE_DTOR.sub('~' + clsName + '() override;', hcontent, count=1)
        # 找对应 .cpp
        cpppath = hpath[:-2] + '.cpp'
        if not os.path.exists(cpppath):
            # 回退 .h 改动（无 .cpp 无法加实现）
            skipped += 1
            continue
        try:
            with open(cpppath, 'r', encoding='utf-8') as f:
                cppcontent = f.read()
        except:
            skipped += 1
            continue
        # 检查是否已有析构实现（幂等）
        dtor_sig = clsName + '::~' + clsName + '() {'
        if dtor_sig in cppcontent:
            # 已有，只更新 .h
            with open(hpath, 'w', encoding='utf-8') as f:
                f.write(new_h)
            patched += 1
            continue
        # 构造析构实现
        lines = [clsName + '::~' + clsName + '() {']
        if members:
            for mem in members:
                lines.append('    delete ' + mem + '_;')
        else:
            lines.append('    (void)0;')
        lines.append('}')
        lines.append('')
        dtor_block = '\n'.join(lines)
        # 在 eStaticClass 前插入
        estatic = 'emf::ecore::EClass* ' + clsName + '::eStaticClass() {'
        if estatic not in cppcontent:
            # 备用：在 eClass() const 前
            eclass = 'emf::ecore::EClass* ' + clsName + '::eClass() const {'
            if eclass in cppcontent:
                cppcontent = cppcontent.replace(eclass, dtor_block + eclass, 1)
            else:
                skipped += 1
                continue
        else:
            cppcontent = cppcontent.replace(estatic, dtor_block + estatic, 1)
        with open(hpath, 'w', encoding='utf-8') as f:
            f.write(new_h)
        with open(cpppath, 'w', encoding='utf-8') as f:
            f.write(cppcontent)
        patched += 1

print(f"patched {patched} classes, skipped {skipped}")
