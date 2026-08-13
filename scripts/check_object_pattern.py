#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""查看 dummy.ecore 中一个 *Object EDataType 包装器和一个 EEnum 的结构作为模板"""
from lxml import etree

DUMMY = "/workspace/decompiler/autosar448/model/dummy.ecore"
XSI_NS = "http://www.w3.org/2001/XMLSchema-instance"

tree = etree.parse(DUMMY)
root = tree.getroot()

# 找 BswInterruptCategoryObject (EDataType) 和 BswInterruptCategory (EEnum)
targets = {"BswInterruptCategoryObject", "BswInterruptCategory",
           "ApiPrincipleEnumObject", "ModeActivationKindObject"}

def local(tag):
    if "}" in tag:
        return tag.split("}", 1)[1]
    return tag

# 记录包路径
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

print("dummy.ecore 中现有 *Object 包装器与 EEnum 模板示例:")
for path, elem in walk(root, ()):
    print()
    print("=" * 70)
    print("名称: %s  包路径: %s" % (elem.get("name"), "/".join(path)))
    print("=" * 70)
    print(etree.tostring(elem, encoding="unicode").strip())

# 检查 dummy.ecore 是否已有 commonstructure 包
print()
print("=" * 70)
print("dummy.ecore 是否已有 commonstructure / genericstructure 包?")
print("=" * 70)
for child in root.iter("eSubpackages"):
    n = child.get("name", "")
    if n in ("commonstructure", "genericstructure", "generaltemplateclasses", "primitivetypes",
             "internalbehavior", "modedeclaration"):
        print("  已存在: %s" % n)
print("(若上面无输出，说明这些包都不存在)")
