#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""检查 dummy.ecore 现有的本地类型定义和引用"""
from lxml import etree
from collections import Counter

DUMMY = "/workspace/decompiler/autosar448/model/dummy.ecore"
XSI_NS = "http://www.w3.org/2001/XMLSchema-instance"


def local(tag):
    if "}" in tag:
        return tag.split("}", 1)[1]
    return tag


tree = etree.parse(DUMMY)
root = tree.getroot()

# 1. 统计 dummy.ecore 中所有 eClassifiers 的类型
print("=" * 70)
print("dummy.ecore 中所有 eClassifiers 分类统计")
print("=" * 70)
classifier_types = Counter()
all_classifiers = []
for elem in root.iter("eClassifiers"):
    t = elem.get("{%s}type" % XSI_NS) or "EClass(default)"
    name = elem.get("name", "?")
    classifier_types[t] += 1
    all_classifiers.append((t, name))
for t, c in classifier_types.most_common():
    print("  %-30s %d" % (t, c))

# 2. 是否有 EDataType / EEnum
print()
print("=" * 70)
print("dummy.ecore 中的 EDataType / EEnum 定义（如有）")
print("=" * 70)
edt = [(t, n) for t, n in all_classifiers if "EDataType" in t]
eenum = [(t, n) for t, n in all_classifiers if "EEnum" in t]
print("EDataType 数量: %d  -> %s" % (len(edt), [n for _, n in edt]))
print("EEnum 数量: %d  -> %s" % (len(eenum), [n for _, n in eenum]))

# 3. dummy.ecore 的包结构（顶层 eSubpackages）
print()
print("=" * 70)
print("dummy.ecore 顶层 eSubpackages 结构")
print("=" * 70)
for child in root:
    if local(child.tag) == "eSubpackages":
        print("  - %s" % child.get("name"))
        for sub in child:
            if local(sub.tag) == "eSubpackages":
                print("      - %s" % sub.get("name"))

# 4. 25 个本地引用指向哪些类型
print()
print("=" * 70)
print("dummy.ecore 中 EAttribute 的本地 #// 引用（去重）")
print("=" * 70)
local_refs = Counter()
other_ref = []
for elem in root.iter("eStructuralFeatures"):
    t = elem.get("{%s}type" % XSI_NS)
    if t and t.endswith("EAttribute"):
        etype = elem.get("eType", "")
        # 去掉 ecore:XXX 前缀
        if " " in etype and etype.startswith("ecore:"):
            etype = etype.split(" ", 1)[1].strip()
        if etype.startswith("#//"):
            local_refs[etype] += 1
        elif not etype.startswith("#//") and "autosar448.ecore" not in etype and "gautosar.ecore" not in etype:
            other_ref.append(etype)
for r, c in local_refs.most_common():
    print("  (x%d)  %s" % (c, r))
print()
print("其他引用 (%d):" % len(other_ref))
for r in other_ref:
    print("  %s" % r)
