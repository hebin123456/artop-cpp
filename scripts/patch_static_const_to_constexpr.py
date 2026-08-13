#!/usr/bin/env python3
"""
Patch generated autosar448 headers:
  static const int X = N;   ->   static constexpr int X = N;

This aligns already-generated headers with the fixed codegen template
(EClassEmitter.cpp:455 now emits `static constexpr int`). C++17 makes
`static constexpr` implicitly inline, avoiding the out-of-class definition
requirement that caused linker errors for odr-used constants.

Only the feature ID constants (uppercase names) are patched; other
`static const int` instances (if any) are left untouched.
"""
import os
import re
import sys

ROOT = "/workspace/.build_cache/autosar448_combined"
RE = re.compile(r"(^[ \t]*static\s+)const(\s+int\s+[A-Z_][A-Z0-9_]*\s*=\s*-?\d+\s*;)",
                re.MULTILINE)

def patch_file(path):
    try:
        with open(path, "r", encoding="utf-8", errors="replace") as f:
            content = f.read()
    except Exception:
        return 0
    new_content, n = RE.subn(r"\1constexpr\2", content)
    if n > 0:
        with open(path, "w", encoding="utf-8") as f:
            f.write(new_content)
    return n

def main():
    total = 0
    files = 0
    for dirpath, dirnames, filenames in os.walk(ROOT):
        for fn in filenames:
            if fn.endswith(".h"):
                p = os.path.join(dirpath, fn)
                n = patch_file(p)
                if n > 0:
                    total += n
                    files += 1
    print(f"Patched {total} static const int -> constexpr in {files} files", file=sys.stderr)

if __name__ == "__main__":
    main()
