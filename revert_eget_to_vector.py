#!/usr/bin/env python3
# revert_eget_to_vector.py — 批量回退 multi-ref eGet 从内部 EList 指针到 vector by value
# 验证 eGet 内部指针跨类型转换是否是崩溃根因
import re, sys, glob

# Pattern: return fieldName ? std::any(static_cast<EList<EObject*>*>(static_cast<void*>(fieldName))) : std::any();
PATTERN = re.compile(
    r'return (\w+) \? std::any\(static_cast<emf::common::EList<emf::common::EObject\*>\*>\(static_cast<void\*>\(\1\)\)\) : std::any\(\);'
)

def repl(m):
    fld = m.group(1)
    return (
        '{\n'
        '        std::vector<emf::common::EObject*> __v;\n'
        '        if (' + fld + ') {\n'
        '            for (size_t i = 0; i < ' + fld + '->size(); ++i) {\n'
        '                __v.push_back(static_cast<emf::common::EObject*>(' + fld + '->get(i)));\n'
        '            }\n'
        '        }\n'
        '        return std::any(std::move(__v));\n'
        '    }'
    )

files = glob.glob('/workspace/.build_cache/autosar448_combined/**/*.cpp', recursive=True)
patched = 0
sites = 0
for f in files:
    with open(f, 'r', encoding='utf-8', errors='replace') as fh:
        content = fh.read()
    new, n = PATTERN.subn(repl, content)
    if n > 0:
        with open(f, 'w', encoding='utf-8') as fh:
            fh.write(new)
        patched += 1
        sites += n
print(f'patched {patched} files, {sites} sites')
