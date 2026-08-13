#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
分析 autosar448.ecore / gautosar.ecore / dummy.ecore 中基础数据类型的定义与引用。
"""
import sys
import re
from collections import Counter, defaultdict
from lxml import etree

OFFICIAL = "/tmp/official_autosar448.ecore"
GAUTOSAR = "/tmp/gautosar.ecore"
DUMMY = "/workspace/decompiler/autosar448/model/dummy.ecore"

ECORE_NS = "http://www.eclipse.org/emf/2002/Ecore"
XSI_NS = "http://www.w3.org/2001/XMLSchema-instance"


def local(tag):
    if "}" in tag:
        return tag.split("}", 1)[1]
    return tag


def is_edatatype(elem):
    t = elem.get("{%s}type" % XSI_NS)
    return t == "ecore:EDataType" or (t and t.endswith("EDataType"))


def is_eattribute(elem):
    t = elem.get("{%s}type" % XSI_NS)
    return t == "ecore:EAttribute" or (t and t.endswith("EAttribute"))


def parse_etype(etype):
    """
    解析 eType 属性值，去掉前缀的 "ecore:EDataType " / "ecore:EClass " 等 XMI 类型标记。
    返回纯 URI 路径。
    例如: "ecore:EDataType ../../../xxx/autosar448.ecore#//.../Boolean" -> "../../../xxx/autosar448.ecore#//.../Boolean"
    """
    if not etype:
        return ""
    # 形如 "ecore:XXX  path" 的，去掉前缀
    if " " in etype and etype.startswith("ecore:"):
        return etype.split(" ", 1)[1].strip()
    return etype.strip()


def classify_etype(etype):
    """
    返回 (category, type_name, raw)
    category in {"local", "gautosar", "autosar448", "other"}
    """
    raw = etype
    etype = parse_etype(etype)
    if not etype:
        return ("none", "", raw)
    last = etype.split("/")[-1]
    if etype.startswith("#//"):
        return ("local", last, raw)
    if "gautosar.ecore" in etype:
        return ("gautosar", last, raw)
    if "autosar448.ecore" in etype:
        return ("autosar448", last, raw)
    return ("other", last, raw)


# ============================================================
# 任务 1: 官方 ecore 中 primitivetypes 包下所有 EDataType
# ============================================================
def task1_official_primitivetypes():
    print("=" * 80)
    print("任务 1: 官方 autosar448.ecore 中 primitivetypes 包下的 EDataType")
    print("=" * 80)

    print("[INFO] 正在解析 official_autosar448.ecore (17.83MB) ...")
    tree = etree.parse(OFFICIAL)
    root = tree.getroot()

    def walk_pkg(elem, path):
        results = []
        name = elem.get("name")
        cur_path = path + (name,) if name else path
        if local(elem.tag) in ("eSubpackages", "EPackage"):
            results.append((cur_path, elem))
            for child in elem:
                if local(child.tag) == "eSubpackages":
                    results.extend(walk_pkg(child, cur_path))
        return results

    all_pkgs = walk_pkg(root, ())
    prim_pkgs = [(p, e) for p, e in all_pkgs if p and p[-1] == "primitivetypes"]
    print("[INFO] 找到 %d 个名为 primitivetypes 的包:" % len(prim_pkgs))
    for p, e in prim_pkgs:
        print("  路径: /".join(p))

    print()
    print("---- primitivetypes 包下所有 EDataType 完整片段 ----")
    found = []
    for path, pkg_elem in prim_pkgs:
        print()
        print("### 包路径: %s" % "/".join(path))
        for child in pkg_elem:
            if local(child.tag) == "eClassifiers" and is_edatatype(child):
                name = child.get("name")
                snippet = etree.tostring(child, encoding="unicode").strip()
                print()
                print("--- EDataType: %s ---" % name)
                print(snippet)
                found.append((path, name, child))

    print()
    print("[汇总] primitivetypes 包下 EDataType 名称列表:")
    for path, name, _ in found:
        print("  - %s  (包: %s)" % (name, "/".join(path)))
    return found


# ============================================================
# 任务 2: gautosar.ecore 中所有 EDataType
# ============================================================
def task2_gautosar_edatatypes():
    print()
    print("=" * 80)
    print("任务 2: gautosar.ecore 中所有 EDataType")
    print("=" * 80)
    tree = etree.parse(GAUTOSAR)
    root = tree.getroot()

    def walk_pkg(elem, path):
        results = []
        name = elem.get("name")
        cur_path = path + (name,) if name else path
        if local(elem.tag) in ("eSubpackages", "EPackage"):
            results.append((cur_path, elem))
            for child in elem:
                if local(child.tag) == "eSubpackages":
                    results.extend(walk_pkg(child, cur_path))
        return results

    all_pkgs = walk_pkg(root, ())

    print("%-30s %-40s %s" % ("name", "instanceClassName", "包路径"))
    print("-" * 100)
    found = []
    for path, pkg_elem in all_pkgs:
        for child in pkg_elem:
            if local(child.tag) == "eClassifiers" and is_edatatype(child):
                name = child.get("name")
                icn = child.get("instanceClassName", "")
                print("%-30s %-40s %s" % (name, icn, "/".join(path)))
                found.append((path, name, icn, child))

    print()
    print("[汇总] gautosar.ecore 共 %d 个 EDataType:" % len(found))
    for path, name, icn, _ in found:
        print("  - %-25s instanceClassName=%-40s 包=%s" % (name, icn, "/".join(path)))

    print()
    print("[关键类型检查] gautosar 中是否定义了 dummy 需要的基础类型:")
    target_names = {"Boolean", "String", "VerbatimString", "Integer",
                    "PositiveInteger", "NegativeInteger", "Float", "Double",
                    "TimeValue", "Identifier", "NameToken", "DateTime",
                    "Numerical", "UnlimitedNatural", "BaseTypeEncodingString"}
    gautosar_names = {n for _, n, _, _ in found}
    for t in sorted(target_names):
        mark = "YES" if t in gautosar_names else "NO"
        print("  %-25s %s" % (t, mark))
    return found


# ============================================================
# 任务 3: 官方 ecore 中 EAttribute eType 引用模式统计
# ============================================================
def task3_official_eattr_references():
    print()
    print("=" * 80)
    print("任务 3: 官方 autosar448.ecore 中 EAttribute eType 引用模式统计")
    print("=" * 80)
    print("[INFO] 流式解析 official_autosar448.ecore ...")

    local_cnt = 0
    gautosar_cnt = 0
    autosar448_cnt = 0
    other_cnt = 0
    local_types = Counter()
    gautosar_types = Counter()
    autosar448_types = Counter()
    other_types = Counter()
    total_eattr = 0

    # eStructuralFeatures 在无命名空间下，用 iterparse 全量再过滤
    context = etree.iterparse(OFFICIAL, events=("end",), tag="eStructuralFeatures")
    for event, elem in context:
        if not is_eattribute(elem):
            elem.clear()
            continue
        total_eattr += 1
        raw = elem.get("eType", "")
        cat, tname, _ = classify_etype(raw)
        if cat == "local":
            local_cnt += 1
            local_types[tname] += 1
        elif cat == "gautosar":
            gautosar_cnt += 1
            gautosar_types[tname] += 1
        elif cat == "autosar448":
            autosar448_cnt += 1
            autosar448_types[tname] += 1
        else:
            other_cnt += 1
            other_types[parse_etype(raw) or "(none)"] += 1
        elem.clear()
        while elem.getprevious() is not None:
            del elem.getparent()[0]

    print()
    print("[EAttribute 总数]: %d" % total_eattr)
    print("[引用本地 #// 路径]: %d" % local_cnt)
    print("[引用 gautosar.ecore#//]: %d" % gautosar_cnt)
    print("[引用外部 autosar448.ecore#//]: %d" % autosar448_cnt)
    print("[引用其他外部路径]: %d" % other_cnt)

    print()
    print("[本地 #// 引用最多的类型 Top 25]:")
    for name, c in local_types.most_common(25):
        print("  %-40s %d" % (name, c))

    print()
    print("[gautosar.ecore 引用的类型]:")
    for name, c in gautosar_types.most_common():
        print("  %-40s %d" % (name, c))

    print()
    print("[autosar448.ecore 引用的类型]:")
    for name, c in autosar448_types.most_common():
        print("  %-40s %d" % (name, c))

    print()
    print("[其他外部引用的类型 Top 20]:")
    for name, c in other_types.most_common(20):
        print("  %-60s %d" % (name, c))

    print()
    print("[关键类型 Boolean/String/VerbatimString 等引用情况]:")
    print("%-20s %-10s %-10s %-12s" % ("类型", "本地", "gautosar", "autosar448"))
    for key in ["Boolean", "String", "VerbatimString", "Integer",
                "PositiveInteger", "NegativeInteger", "Float", "Double",
                "TimeValue", "Identifier", "NameToken", "Numerical",
                "DateTime", "Limit", "Ref"]:
        l = local_types.get(key, 0)
        g = gautosar_types.get(key, 0)
        a = autosar448_types.get(key, 0)
        print("  %-20s %-10d %-10d %-12d" % (key, l, g, a))


# ============================================================
# 任务 4: dummy.ecore 中引用外部 autosar448.ecore 的 EAttribute
# ============================================================
def task4_dummy_external_refs():
    print()
    print("=" * 80)
    print("任务 4: dummy.ecore 中引用外部 autosar448.ecore 的 EAttribute")
    print("=" * 80)
    print("[INFO] 解析 dummy.ecore ...")
    tree = etree.parse(DUMMY)
    root = tree.getroot()

    external_types = Counter()
    external_paths = Counter()  # 完整路径（去掉前缀）
    external_attrs = []
    total_eattr = 0
    local_cnt = 0
    gautosar_cnt = 0
    autosar448_cnt = 0
    other_cnt = 0

    # root.iter 也支持无命名空间 tag
    for elem in root.iter("eStructuralFeatures"):
        if not is_eattribute(elem):
            continue
        total_eattr += 1
        raw = elem.get("eType", "")
        cat, tname, _ = classify_etype(raw)
        clean = parse_etype(raw)
        if cat == "local":
            local_cnt += 1
        elif cat == "gautosar":
            gautosar_cnt += 1
        elif cat == "autosar448":
            autosar448_cnt += 1
            external_types[tname] += 1
            external_paths[clean] += 1
            parent = elem.getparent()
            parent_name = parent.get("name") if parent is not None else "?"
            attr_name = elem.get("name", "?")
            external_attrs.append((parent_name, attr_name, clean, tname))
        else:
            other_cnt += 1

    print()
    print("[dummy.ecore EAttribute 总数]: %d" % total_eattr)
    print("[引用本地 #//]: %d" % local_cnt)
    print("[引用 gautosar.ecore#//]: %d" % gautosar_cnt)
    print("[引用外部 autosar448.ecore#//]: %d" % autosar448_cnt)
    print("[其他]: %d" % other_cnt)

    print()
    print("[引用外部 autosar448.ecore 的具体类型路径（去重后，按出现次数）]:")
    for path, c in external_paths.most_common():
        last = path.split("/")[-1]
        print("  (x%d)  类型名=%-25s  路径=%s" % (c, last, path))

    print()
    print("[去重后的基础类型名集合] (共 %d 种):" % len(external_types))
    for t in sorted(external_types):
        print("  - %-25s (引用 %d 次)" % (t, external_types[t]))

    print()
    print("[前 40 个引用外部 autosar448.ecore 的 EAttribute 示例]:")
    for parent_name, attr_name, path, tname in external_attrs[:40]:
        print("  %-35s . %-25s -> %s" % (parent_name, attr_name, tname))

    return external_types, external_paths, external_attrs


# ============================================================
# 任务 5: 综合建议
# ============================================================
def task5_recommendation(official_found, gautosar_found, external_types):
    print()
    print("=" * 80)
    print("任务 5: dummy.ecore 自洽方案建议")
    print("=" * 80)

    official_names = {n for _, n, _ in official_found}
    gautosar_names = {n for _, n, _, _ in gautosar_found}
    dummy_needed = set(external_types.keys())

    print()
    print("[dummy.ecore 外部引用的基础类型] vs [gautosar 是否有] vs [官方 primitivetypes 是否有]:")
    print("%-30s %-15s %-15s %s" % ("类型名", "gautosar有", "官方prim有", "引用次数"))
    print("-" * 75)
    for t in sorted(dummy_needed):
        g = "YES" if t in gautosar_names else "NO"
        o = "YES" if t in official_names else "NO"
        c = external_types[t]
        print("%-30s %-15s %-15s %d" % (t, g, o, c))

    print()
    print("[分析]:")
    only_in_official = dummy_needed - gautosar_names
    in_both = dummy_needed & gautosar_names
    in_neither = dummy_needed - gautosar_names - official_names
    print("  - dummy 需要且 gautosar 已定义的类型 (%d): %s" %
          (len(in_both), sorted(in_both)))
    print("  - dummy 需要但 gautosar 没有、需在官方 primitivetypes 找的类型 (%d): %s" %
          (len(only_in_official), sorted(only_in_official)))
    if in_neither:
        print("  - dummy 需要但 gautosar 和官方 primitivetypes 都没有的类型 (%d): %s" %
              (len(in_neither), sorted(in_neither)))

    print()
    print("[建议方案]:")
    print("  对于 gautosar 已有的类型 -> 改为引用 gautosar.ecore#//ggenericstructure/gprimitivetypes/<Name>")
    print("  对于 gautosar 没有但官方 primitivetypes 有的类型 -> 在 dummy.ecore 内部新建")
    print("    genericstructure/generaltemplateclasses/primitivetypes 子包，复制官方 EDataType 定义")


def main():
    task1 = task1_official_primitivetypes()
    task2 = task2_gautosar_edatatypes()
    task3_official_eattr_references()
    external_types, external_paths, external_attrs = task4_dummy_external_refs()
    task5_recommendation(task1, task2, external_types)


if __name__ == "__main__":
    main()
