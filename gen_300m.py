#!/usr/bin/env python3
# gen_300m.py — 基于 large_96m.arxml 生成 ~300MB 测试文件
# 策略：解析根 <AUTOSAR> 的子 AR-PACKAGE，复制 3 组，每组 shortName 加前缀避免冲突
# 输出到 /tmp/large_300m.arxml，测试后由调用方删除
import sys
import re

SRC = "/workspace/benchmark/data/large_96m.arxml"
DST = "/tmp/large_300m.arxml"

def main():
    with open(SRC, "r", encoding="utf-8") as f:
        content = f.read()

    # 找到根 <AUTOSAR ...> 开始标签 和 </AUTOSAR> 结束标签
    # 结构：<?xml?>\n<AUTOSAR xmlns=...>\n  <AR-PACKAGES>\n    <AR-PACKAGE>...\n  </AR-PACKAGES>\n</AUTOSAR>\n
    # 我们提取 <AR-PACKAGES>...</AR-PACKAGES> 块，复制 3 份，每份内的 SHORT-NAME 加前缀

    # 找根标签
    root_open_match = re.search(r'(<AUTOSAR\b[^>]*>)', content)
    if not root_open_match:
        print("ERROR: no <AUTOSAR> root", file=sys.stderr)
        sys.exit(1)
    root_open = root_open_match.group(1)
    root_open_end = root_open_match.end()

    # 找 </AUTOSAR>
    root_close_match = re.search(r'</AUTOSAR>\s*$', content[root_open_end:])
    if not root_close_match:
        print("ERROR: no </AUTOSAR>", file=sys.stderr)
        sys.exit(1)

    # 提取根标签之间的内容（含 XML 声明前的部分）
    xml_decl = content[:root_open_match.start()]
    inner = content[root_open_end:root_open_end + root_close_match.start()]

    # 找 <AR-PACKAGES> 块（顶层）
    pkgs_match = re.search(r'(\s*)(<AR-PACKAGES>)(.*?)(</AR-PACKAGES>)(\s*)', inner, re.DOTALL)
    if not pkgs_match:
        print("ERROR: no <AR-PACKAGES>", file=sys.stderr)
        sys.exit(1)

    pkgs_block = pkgs_match.group(2) + pkgs_match.group(3) + pkgs_match.group(4)
    # 前/后缀（注释等）
    inner_prefix = inner[:pkgs_match.start()]
    inner_suffix = inner[pkgs_match.end():]

    # 生成 3 份，每份的 SHORT-NAME 加前缀 c1_/c2_/c3_
    # 但只改顶层 AR-PACKAGE 的 SHORT-NAME（避免改子元素的引用路径）
    # 实际上引用路径以 /AUTOSAR/... 开头，改顶层 shortName 会让所有引用失效
    # 更安全：不改 shortName，而是包在不同的 <AR-PACKAGES> 但同名——但 EMF 会合并同名 package
    # 最简单可行：直接复制 3 份整个 inner（含 AR-PACKAGES），shortName 冲突在 load 时
    #   只会产生 duplicate path，但 save 仍能工作（round-trip 不依赖路径唯一）
    # 实测目标：测 load+save 速度和内存，不要求引用解析正确

    # 方案：3 份 inner 直接拼接，顶层加注释分隔
    parts = []
    for i in range(1, 4):
        parts.append(f"\n<!-- ===== COPY {i} ===== -->\n")
        parts.append(inner.strip())
    new_inner = "\n".join(parts) + "\n"

    # 写出
    with open(DST, "w", encoding="utf-8") as f:
        f.write(xml_decl)
        f.write(root_open)
        f.write(new_inner)
        f.write("</AUTOSAR>\n")

    import os
    size = os.path.getsize(DST)
    print(f"Generated: {DST}")
    print(f"Size: {size} bytes ({size/1024/1024:.1f} MB)")

if __name__ == "__main__":
    main()
