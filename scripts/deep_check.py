#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Deeper targeted checks on dummy.ecore vs official."""
import xml.etree.ElementTree as ET
from collections import Counter

ECORE_NS = "http://www.eclipse.org/emf/2002/Ecore"
XSI_NS = "http://www.w3.org/2001/XMLSchema-instance"
XSI_TYPE = f"{{{XSI_NS}}}type"


def ln(e):
    t = e.tag
    return t.split("}", 1)[1] if isinstance(t, str) and "}" in t else t


def load(path):
    return ET.parse(path).getroot()


def attrs_of(elem):
    return dict(elem.attrib)


def main():
    dummy = load("/workspace/decompiler/autosar448/model/dummy.ecore")
    official = load("/tmp/official_autosar448.ecore")

    print("=" * 80)
    print("深度检查")
    print("=" * 80)

    # ---- A. dummy 顶层 7 个子包 ----
    print("\n[A] dummy.ecore 顶层 7 个子包:")
    for sp in dummy:
        if ln(sp) == "eSubpackages":
            print(f"    - {sp.get('name')}  nsURI={sp.get('nsURI')}")

    print("\n    official 顶层 13 个子包:")
    for sp in official:
        if ln(sp) == "eSubpackages":
            print(f"    - {sp.get('name')}")

    # ---- B. EAttribute: eAttributeType vs eType 用法对比 ----
    print("\n[B] EAttribute 属性用法对比 (eAttributeType vs eType):")
    for label, root in [("dummy", dummy), ("official", official)]:
        n_attr = 0
        n_with_eattrtype = 0
        n_with_etype = 0
        n_both_null = 0
        n_both_set = 0
        for e in root.iter():
            if ln(e) == "eStructuralFeatures" and e.get(XSI_TYPE) == "ecore:EAttribute":
                n_attr += 1
                eat = e.get("eAttributeType")
                et = e.get("eType")
                if eat: n_with_eattrtype += 1
                if et: n_with_etype += 1
                if not eat and not et: n_both_null += 1
                if eat and et: n_both_set += 1
        print(f"  {label:<10}: EAttribute 总数={n_attr}, 有 eAttributeType={n_with_eattrtype}, 有 eType={n_with_etype}, 两者都空={n_both_null}, 两者都有={n_both_set}")

    # ---- C. EReference: eType 用法对比 ----
    print("\n[C] EReference eType 用法对比:")
    for label, root in [("dummy", dummy), ("official", official)]:
        n_ref = 0
        n_with_etype = 0
        for e in root.iter():
            if ln(e) == "eStructuralFeatures" and e.get(XSI_TYPE) == "ecore:EReference":
                n_ref += 1
                if e.get("eType"): n_with_etype += 1
        print(f"  {label:<10}: EReference 总数={n_ref}, 有 eType={n_with_etype}, eType 为空={n_ref - n_with_etype}")

    # ---- D. dummy EAttribute eAttributeType 为空 - 看是否用了 eType ----
    print("\n[D] dummy 中 eAttributeType 为空的 EAttribute 是否用了 eType? 样例:")
    cnt = 0
    for e in dummy.iter():
        if ln(e) == "eStructuralFeatures" and e.get(XSI_TYPE) == "ecore:EAttribute":
            if not e.get("eAttributeType"):
                cnt += 1
                if cnt <= 10:
                    nm = e.get("name", "?")
                    et = e.get("eType", "<无>")
                    print(f"    - name={nm!r}  eType={et!r}  所有属性={dict(e.attrib)}")
    print(f"  共 {cnt} 个")

    # ---- E. dummy EReference eType 引用解析 ----
    print("\n[E] dummy EReference eType 引用解析:")
    # 收集 dummy 所有 classifier 名 (按 fragment path)
    parent_map = {c: p for p in dummy.iter() for c in p}
    name_to_paths = {}
    path_set = set()
    def chain(elem):
        c = []
        cur = elem
        while cur is not None and cur is not dummy:
            cur = parent_map.get(cur)
            if cur is not None and ln(cur) in ("eSubpackages", "EPackage"):
                c.append(cur.get("name"))
            if cur is dummy:
                break
        c.reverse()
        return c
    for e in dummy.iter():
        if ln(e) == "eClassifiers":
            nm = e.get("name")
            if nm:
                ch = chain(e)
                frag = "//" + "/".join(ch + [nm])
                path_set.add(frag)
                path_set.add("//" + nm)
                name_to_paths.setdefault(nm, []).append(frag)
    # 也加上 gautosar 的目标名 (从跨包引用中提取)
    gautosar_targets = set()
    for e in dummy.iter():
        if ln(e) == "eClassifiers" and e.get(XSI_TYPE) == "ecore:EClass":
            est = e.get("eSuperTypes")
            if est:
                for ref in est.split():
                    if "gautosar" in ref.lower() and "#" in ref:
                        gautosar_targets.add(ref.split("#", 1)[1])
    # 检查 EReference eType
    unresolved_local = 0
    cross_pkg = 0
    samples_unresolved = []
    samples_cross = []
    for e in dummy.iter():
        if ln(e) == "eStructuralFeatures" and e.get(XSI_TYPE) == "ecore:EReference":
            et = e.get("eType")
            if not et:
                continue
            if et.startswith("#//"):
                # local
                if et[1:] not in path_set and et[2:] not in path_set:
                    leaf = et.rsplit("/", 1)[-1]
                    if leaf not in name_to_paths:
                        unresolved_local += 1
                        if len(samples_unresolved) < 15:
                            samples_unresolved.append((e.get("name"), et))
            elif et.startswith("../") or et.startswith("http") or et.startswith("platform:/"):
                cross_pkg += 1
                if len(samples_cross) < 10:
                    samples_cross.append((e.get("name"), et))
    print(f"  本地 #// eType 无法解析: {unresolved_local}")
    for nm, ref in samples_unresolved:
        print(f"    - {nm} -> {ref}")
    print(f"  跨包 eType 引用: {cross_pkg}")
    for nm, ref in samples_cross:
        print(f"    - {nm} -> {ref}")

    # ---- F. gautosar 跨包引用路径检查 ----
    print("\n[F] dummy 中跨包引用 gautosar 的路径:")
    gautosar_paths = Counter()
    for e in dummy.iter():
        if ln(e) == "eClassifiers" and e.get(XSI_TYPE) == "ecore:EClass":
            est = e.get("eSuperTypes")
            if est:
                for ref in est.split():
                    if "gautosar" in ref.lower() and "#" in ref:
                        path_part = ref.split("#", 1)[0]
                        gautosar_paths[path_part] += 1
    for p, c in gautosar_paths.most_common():
        print(f"  {c:>4}  {p}")
    # 检查实际文件是否存在
    import os
    print("\n  实际 gautosar.ecore 文件位置检查:")
    candidates = [
        "/workspace/opensourse/artop-4.19.0/core-4.19.0/aal/plugins/org.artop.aal.autosar448/model/gautosar.ecore",
        "/tmp/gautosar.ecore",
        "/workspace/tool/plugins/org.artop.aal.gautosar_4.13.0.201912171516.jar",
    ]
    for c in candidates:
        print(f"    {'存在' if os.path.exists(c) else '不存在':<6} {c}")

    # ---- G. official 是否有跨包引用 gautosar ----
    print("\n[G] official 中跨包引用 gautosar 的数量:")
    n = 0
    samples = []
    for e in official.iter():
        if ln(e) == "eClassifiers" and e.get(XSI_TYPE) == "ecore:EClass":
            est = e.get("eSuperTypes")
            if est:
                for ref in est.split():
                    if "gautosar" in ref.lower():
                        n += 1
                        if len(samples) < 5:
                            samples.append(ref)
    print(f"  official 跨包引用 gautosar 数量: {n}")
    for s in samples:
        print(f"    - {s}")

    # ---- H. dummy 独有的 50 个 classifier 名 - 是否是 *Object 模式 ----
    print("\n[H] dummy 独有 classifier 名特征分析:")
    d_names = set()
    o_names = set()
    for e in dummy.iter():
        if ln(e) == "eClassifiers" and e.get("name"):
            d_names.add(e.get("name"))
    for e in official.iter():
        if ln(e) == "eClassifiers" and e.get("name"):
            o_names.add(e.get("name"))
    d_only = sorted(d_names - o_names)
    suffixes = Counter()
    for n in d_only:
        if n.endswith("Object"): suffixes["*Object"] += 1
        elif n.endswith("EnumObject"): suffixes["*EnumObject"] += 1
        else: suffixes["other"] += 1
    print(f"  dummy 独有名总数: {len(d_only)}")
    print(f"  后缀分类: {dict(suffixes)}")
    print(f"  非 *Object 的独有名: {[n for n in d_only if not n.endswith('Object')]}")

    # ---- I. dummy 各顶层子包的 classifier 数 ----
    print("\n[I] dummy 各顶层子包的 eClassifier 数:")
    for sp in dummy:
        if ln(sp) == "eSubpackages":
            # 递归统计该子树下所有 eClassifiers
            def count_classifiers(elem):
                cnt = 0
                for e in elem.iter():
                    if ln(e) == "eClassifiers":
                        cnt += 1
                return cnt
            n = count_classifiers(sp)
            # 统计该子树下 EPackage 数
            n_pkg = sum(1 for e in sp.iter() if ln(e) == "eSubpackages") + 1
            print(f"  {sp.get('name'):<30} classifiers={n:<5} 子包数={n_pkg}")

    # ---- J. official 对应的同样 7 个子包的 classifier 数 ----
    print("\n[J] official 对应 7 个子包的 eClassifier 数:")
    dummy_top_names = [sp.get("name") for sp in dummy if ln(sp) == "eSubpackages"]
    for sp in official:
        if ln(sp) == "eSubpackages" and sp.get("name") in dummy_top_names:
            def count_classifiers(elem):
                return sum(1 for e in elem.iter() if ln(e) == "eClassifiers")
            n = count_classifiers(sp)
            n_pkg = sum(1 for e in sp.iter() if ln(e) == "eSubpackages") + 1
            print(f"  {sp.get('name'):<30} classifiers={n:<5} 子包数={n_pkg}")


if __name__ == "__main__":
    main()
