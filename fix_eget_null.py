#!/usr/bin/env python3
"""Patch generated eGet(int) multi-ref: add null check before returning internal EList pointer.

Bug: when a multi-ref field (e.g. arPackages_) is never initialized (nullptr),
the eGet returns std::any(nullptr-as-EList<EObject*>*). Consumers call p->size()
which dereferences null → segfault.

Fix: return empty std::any() when field is null. Consumers check has_value() first.

Pattern (2 lines):
        return std::any(static_cast<emf::common::EList<emf::common::EObject*>*>(
            static_cast<void*>(FIELDNAME)));

Replace with (1 line):
        return FIELDNAME ? std::any(static_cast<emf::common::EList<emf::common::EObject*>*>(static_cast<void*>(FIELDNAME))) : std::any();
"""
import os, re, sys

ROOT = "/workspace/.build_cache/autosar448_combined"
PATTERN = re.compile(
    r'return std::any\(static_cast<emf::common::EList<emf::common::EObject\*>\*>\(\s*\n\s*static_cast<void\*>\((\w+)\)\)\);'
)
REPL = lambda m: (
    'return %s ? std::any(static_cast<emf::common::EList<emf::common::EObject*>*>'
    '(static_cast<void*>(%s))) : std::any();'
) % (m.group(1), m.group(1))

patched_files = 0
patched_sites = 0

for dirpath, _, filenames in os.walk(ROOT):
    for fn in filenames:
        if not fn.endswith('.cpp'):
            continue
        path = os.path.join(dirpath, fn)
        with open(path, 'r', errors='replace') as f:
            content = f.read()
        new_content, n = PATTERN.subn(REPL, content)
        if n > 0:
            with open(path, 'w') as f:
                f.write(new_content)
            patched_files += 1
            patched_sites += n

print(f"Patched {patched_files} files, {patched_sites} sites")
