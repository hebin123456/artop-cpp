#!/usr/bin/env python3
"""Generate a master init file that initializes ALL autosar40 packages."""
import os
import re
import sys

ROOT = "/workspace/.build_cache/autosar448_combined"
OUT_CPP = "/workspace/.build_cache/autosar448_combined/init_all_packages.cpp"
OUT_H = "/workspace/.build_cache/autosar448_combined/init_all_packages.h"


def find_package_files():
    result = []
    for dirpath, dirnames, filenames in os.walk(ROOT):
        for fn in filenames:
            if fn.endswith("Package.h"):
                result.append(os.path.join(dirpath, fn))
    return sorted(result)


def extract_namespace_and_class(header_path):
    """Extract the namespace immediately enclosing the class declaration.

    The class name is derived from the file basename (e.g. KeywordPackage.h ->
    KeywordPackage). This avoids matching EClass names that happen to end with
    "Package" (e.g. ARPackage, GARPackage).
    """
    class_name = os.path.basename(header_path)[:-2]  # strip ".h"
    if not class_name.endswith("Package"):
        return None, None
    try:
        with open(header_path, "r", encoding="utf-8", errors="replace") as f:
            lines = f.readlines()
    except Exception:
        return None, None

    # Find the line with "class <class_name>"
    class_line_idx = -1
    for i, line in enumerate(lines):
        if re.search(r"\bclass\s+" + re.escape(class_name) + r"\b", line):
            class_line_idx = i
            break
    if class_line_idx < 0:
        return None, None

    # Ensure it's an EPackage, not an EClass that happens to share the suffix.
    # Package headers declare "class XxxPackage : public emf::ecore::EPackageImpl".
    # EClass headers like ARPackage.h declare "class ARPackage : public ...EObjectImpl".
    # Check the class declaration line and the next few lines for EPackageImpl.
    decl_block = "".join(lines[class_line_idx:class_line_idx + 5])
    if "EPackageImpl" not in decl_block:
        return None, None

    # Walk backwards to find the immediately enclosing namespace.
    for i in range(class_line_idx, -1, -1):
        line = lines[i]
        m = re.search(r"^\s*namespace\s+([A-Za-z_][A-Za-z0-9_:]*)\s*\{", line)
        if m:
            return m.group(1), class_name

    return None, class_name


def main():
    files = find_package_files()
    print(f"Found {len(files)} Package.h files", file=sys.stderr)

    entries = []
    skipped = []
    for f in files:
        ns, class_name = extract_namespace_and_class(f)
        if not ns or not class_name:
            skipped.append(f)
            continue
        include_path = os.path.relpath(f, ROOT)
        entries.append((include_path, ns, class_name))

    print(f"Extracted {len(entries)} package entries, skipped {len(skipped)}", file=sys.stderr)
    if skipped:
        print("First 5 skipped:", file=sys.stderr)
        for s in skipped[:5]:
            print(f"  {s}", file=sys.stderr)

    # Generate header
    with open(OUT_H, "w") as out:
        out.write("#pragma once\n")
        out.write("// Auto-generated: initializes all autosar40 packages\n")
        out.write("namespace emf { namespace artop { namespace autosar40 {\n")
        out.write("    void initializeAllPackages();\n")
        out.write("} } }\n")

    # Generate cpp
    with open(OUT_CPP, "w") as out:
        out.write("// Auto-generated: initializes all autosar40 packages\n")
        out.write('#include "init_all_packages.h"\n')
        seen_includes = set()
        for inc, ns, cls in entries:
            if inc in seen_includes:
                continue
            seen_includes.add(inc)
            out.write(f'#include "{inc}"\n')
        out.write("\n")
        out.write("namespace emf { namespace artop { namespace autosar40 {\n")
        out.write("    void initializeAllPackages() {\n")
        for inc, ns, cls in entries:
            out.write(f"        ::{ns}::{cls}::initialize();\n")
        out.write("    }\n")
        out.write("} } }\n")

    print(f"Generated {OUT_CPP} and {OUT_H}", file=sys.stderr)


if __name__ == "__main__":
    main()
