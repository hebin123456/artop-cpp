#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""提取官方 ecore 中 ApiPrincipleEnum / ModeActivationKind 两个 EEnum 的完整片段"""
from lxml import etree

OFFICIAL = "/tmp/official_autosar448.ecore"
XSI_NS = "http://www.w3.org/2001/XMLSchema-instance"

tree = etree.parse(OFFICIAL)
root = tree.getroot()

targets = {"ApiPrincipleEnum", "ModeActivationKind"}


def local(tag):
    if "}" in tag:
        return tag.split("}", 1)[1]
    return tag


def walk(elem, path):
    name = elem.get("name")
    cur = path + (name,) if name else path
    if local(elem.tag) in ("eSubpackages", "EPackage"):
        for child in elem:
            if local(child.tag) == "eSubpackages":
                yield from walk(child, cur)
            elif local(child.tag) == "eClassifiers":
                cn = child.get("name")
                if cn in targets:
                    yield (cur, child)


print("搜索官方 ecore 中的 ApiPrincipleEnum / ModeActivationKind ...")
found = list(walk(root, ()))
print("共找到 %d 个目标枚举\n" % len(found))
for path, elem in found:
    t = elem.get("{%s}type" % XSI_NS)
    print("=" * 70)
    print("名称: %s  xsi:type=%s  包路径: %s" % (elem.get("name"), t, "/".join(path)))
    print("=" * 70)
    print(etree.tostring(elem, encoding="unicode").strip())
    print()

# 同时检查 dummy.ecore 中这两个引用的原始 eType 字符串
print("\n" + "=" * 70)
print("dummy.ecore 中对这两个枚举的引用上下文")
print("=" * 70)
DUMMY = "/workspace/decompiler/autosar448/model/dummy.ecore"
dtree = etree.parse(DUMMY)
droot = dtree.getroot()
for elem in droot.iter("eStructuralFeatures"):
    t = elem.get("{%s}type" % XSI_NS)
    if t and t.endswith("EAttribute"):
        etype = elem.get("eType", "")
        if "ApiPrinciple" in etype or "ModeActivation" in etype:
            parent = elem.getparent()
            pn = parent.get("name") if parent is not None else "?"
            print("  %s.%s" % (pn, elem.get("name")))
            print("    eType = %s" % etype)
