#!/usr/bin/env python3
"""
Generate a single .cpp file providing out-of-class definitions for all
`static const int X = N;` members declared in generated autosar448 headers.

Simplified line-based parser: tracks namespace and class scope via braces.
"""
import os
import re
import sys

ROOT = "/workspace/.build_cache/autosar448_combined"
OUT = "/workspace/.build_cache/autosar448_static_defs.cpp"

RE_STATIC_CONST_INT = re.compile(
    r"^\s*static\s+const\s+int\s+([A-Z_][A-Z0-9_]*)\s*=\s*-?\d+\s*;"
)
# Match nested namespace: namespace A::B::C {  OR  namespace A {
RE_NAMESPACE = re.compile(r"^\s*namespace\s+([A-Za-z_][A-Za-z0-9_:]*(?:::[A-Za-z_][A-Za-z0-9_]*)*)\s*\{?")
RE_NAMESPACE_ANON = re.compile(r"^\s*namespace\s*\{")
RE_CLASS = re.compile(r"^\s*(?:class|struct)\s+([A-Za-z_][A-Za-z0-9_]*)")

def strip_line_comment(line):
    # Remove // comments not inside strings
    out = []
    in_str = False
    i = 0
    while i < len(line):
        c = line[i]
        if c == '"' and (i == 0 or line[i-1] != '\\'):
            in_str = not in_str
        if not in_str and c == '/' and i+1 < len(line) and line[i+1] == '/':
            break
        out.append(c)
        i += 1
    return ''.join(out)

def scan_header(path):
    results = []
    ns_stack = []     # list of (name, brace_depth_at_open)
    class_stack = []  # list of (name, brace_depth_at_open)
    brace_depth = 0
    try:
        with open(path, "r", encoding="utf-8", errors="replace") as f:
            lines = f.readlines()
    except Exception:
        return results

    for raw_line in lines:
        line = strip_line_comment(raw_line).rstrip('\n')
        # Remove block comments crudely (single-line /* */ only)
        line = re.sub(r'/\*.*?\*/', '', line)

        # Detect namespace declarations BEFORE counting braces
        m_ns = RE_NAMESPACE.match(line)
        m_ns_anon = RE_NAMESPACE_ANON.match(line)
        if m_ns:
            ns_name = m_ns.group(1)
            # The '{' may be on this line or next; we record the brace_depth BEFORE opening
            ns_stack.append((ns_name, brace_depth))
        elif m_ns_anon:
            ns_stack.append(("", brace_depth))

        # Detect class/struct declarations
        m_cls = RE_CLASS.match(line)
        if m_cls:
            cls_name = m_cls.group(1)
            # Only track if there's a '{' on this line (definition, not forward decl)
            if '{' in line:
                class_stack.append((cls_name, brace_depth))

        # Count braces (respecting strings already stripped)
        for c in line:
            if c == '{':
                brace_depth += 1
            elif c == '}':
                brace_depth -= 1
                # Pop scope if we just closed a namespace or class block
                if class_stack and class_stack[-1][1] == brace_depth:
                    class_stack.pop()
                elif ns_stack and ns_stack[-1][1] == brace_depth:
                    ns_stack.pop()

        # Check for static const int declaration
        m_sci = RE_STATIC_CONST_INT.match(line)
        if m_sci and class_stack:
            const_name = m_sci.group(1)
            full_ns = "::".join([x[0] for x in ns_stack if x[0]])
            full_cls = "::".join([x[0] for x in class_stack])
            if full_ns:
                fq = f"{full_ns}::{full_cls}::{const_name}"
            else:
                fq = f"{full_cls}::{const_name}"
            results.append(fq)
    return results


def main():
    all_defs = []
    headers = []
    for dirpath, dirnames, filenames in os.walk(ROOT):
        for fn in filenames:
            if fn.endswith(".h"):
                headers.append(os.path.join(dirpath, fn))
    headers.sort()
    print(f"Scanning {len(headers)} headers...", file=sys.stderr)
    for h in headers:
        defs = scan_header(h)
        if defs:
            all_defs.extend(defs)
    print(f"Found {len(all_defs)} static const int definitions", file=sys.stderr)

    # Build per-header definition lists to avoid including all headers in one file
    # (single file with 4213 includes is too slow to compile)
    # Instead, emit one .cpp per header, then compile all and archive.
    # For simplicity, emit a few grouped .cpp files.
    # Actually, let's try the single-file approach first with -O0.

    with open(OUT, "w") as f:
        f.write("// Auto-generated: out-of-class definitions for static const int members\n")
        # Don't include all headers - instead use forward declarations
        # Actually we need the class definitions. Let's include all headers.
        for h in headers:
            rel = os.path.relpath(h, ROOT)
            f.write(f'#include "{rel}"\n')
        f.write("\n")
        seen = set()
        for fq in all_defs:
            if fq in seen:
                continue
            seen.add(fq)
            f.write(f"const int {fq};\n")
    print(f"Wrote {OUT} ({len(seen)} unique definitions)", file=sys.stderr)


if __name__ == "__main__":
    main()
