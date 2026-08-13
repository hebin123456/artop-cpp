#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Compare two ecore files structurally."""
import sys
import os
import xml.etree.ElementTree as ET
from collections import Counter, defaultdict

ECORE_NS = "http://www.eclipse.org/emf/2002/Ecore"
XSI_NS = "http://www.w3.org/2001/XMLSchema-instance"
ET.register_namespace("ecore", ECORE_NS)
ET.register_namespace("xsi", XSI_NS)


def qn(tag):
    return f"{{{ECORE_NS}}}{tag}"


def local_name(elem):
    t = elem.tag
    if isinstance(t, str) and "}" in t:
        return t.split("}", 1)[1]
    return t


def find_children(elem, name):
    """Find direct children by local name (namespace-agnostic)."""
    return [c for c in elem if local_name(c) == name]


def findall_local(elem, name):
    """Find all descendants by local name (namespace-agnostic)."""
    return [c for c in elem.iter() if local_name(c) == name]


def iter_all(elem):
    yield elem
    for child in elem:
        yield from iter_all(child)


def analyze(path):
    tree = ET.parse(path)
    root = tree.getroot()

    info = {
        "path": path,
        "root_tag": root.tag,
        "root_name": root.get("name"),
        "root_nsURI": root.get("nsURI"),
        "root_nsPrefix": root.get("nsPrefix"),
        "n_epackage_total": 0,
        "n_eclass": 0,
        "n_eenum": 0,
        "n_edatatype": 0,
        "n_ereference": 0,
        "n_eattribute": 0,
        "n_eoperation": 0,
        "n_eenumliteral": 0,
        "n_eannotation": 0,
        "n_eclas_no_name": 0,
        "n_eref_etype_null": 0,
        "n_eattr_eattrtype_null": 0,
        "n_eclassifiers_no_name": 0,
        "esupertype_refs": [],
        "esupertype_cross_pkg": [],
        "etype_refs": [],
        "eattrtype_refs": [],
        "eclassifier_names_per_pkg": defaultdict(list),
        "duplicate_class_names_global": Counter(),
        "eclassifier_names_global": Counter(),
        "all_classifier_names": [],
        "all_subpkg_names": [],
        "eclassifiers_count_per_pkg": Counter(),
        "top_level_subpkg_names": [],
        "model_plugin_version": None,
    }

    for sp in find_children(root, "eSubpackages"):
        info["top_level_subpkg_names"].append(sp.get("name"))

    all_elems = list(iter_all(root))
    # Packages: root EPackage + all eSubpackages elements (which are EPackage-typed)
    epackages = [e for e in all_elems if local_name(e) in ("EPackage", "eSubpackages")]
    info["n_epackage_total"] = len(epackages)

    # Walk every element once and classify by (local_name, xsi:type).
    # In ecore XML serialization:
    #   - classifiers are <eClassifiers xsi:type="ecore:EClass|EEnum|EDataType">
    #   - structural features are <eStructuralFeatures xsi:type="ecore:EReference|EAttribute">
    #   - <eOperations>, <eLiterals>, <eAnnotations>, <eParameters> are containment elements
    for elem in all_elems:
        ln = local_name(elem)
        xsi = elem.get(f"{{{XSI_NS}}}type", "")
        if ln == "eSubpackages":
            info["all_subpkg_names"].append(elem.get("name"))
        elif ln == "eClassifiers":
            name = elem.get("name")
            info["all_classifier_names"].append(name)
            info["eclassifier_names_global"][name] += 1
            if name is None:
                info["n_eclassifiers_no_name"] += 1
            if xsi == "ecore:EClass":
                info["n_eclass"] += 1
                if name is None:
                    info["n_eclas_no_name"] += 1
                est = elem.get("eSuperTypes")
                if est:
                    for ref in est.split():
                        info["esupertype_refs"].append(ref)
                        if ref.startswith("../") or ref.startswith("http") or ref.startswith("platform:/"):
                            info["esupertype_cross_pkg"].append(ref)
            elif xsi == "ecore:EEnum":
                info["n_eenum"] += 1
            elif xsi == "ecore:EDataType":
                info["n_edatatype"] += 1
        elif ln == "eStructuralFeatures":
            if xsi == "ecore:EReference":
                info["n_ereference"] += 1
                et = elem.get("eType")
                info["etype_refs"].append(et)
                if et is None or et == "":
                    info["n_eref_etype_null"] += 1
            elif xsi == "ecore:EAttribute":
                info["n_eattribute"] += 1
                eat = elem.get("eAttributeType")
                info["eattrtype_refs"].append(eat)
                if eat is None or eat == "":
                    info["n_eattr_eattrtype_null"] += 1
        elif ln == "eOperations":
            info["n_eoperation"] += 1
        elif ln == "eLiterals":
            info["n_eenumliteral"] += 1
        elif ln == "eAnnotations":
            info["n_eannotation"] += 1

    for ann in find_children(root, "eAnnotations"):
        if ann.get("source") == "http://www.eclipse.org/emf/2002/GenModel":
            for d in find_children(ann, "details"):
                if d.get("key") == "modelPluginVersion":
                    info["model_plugin_version"] = d.get("value")

    for name, cnt in info["eclassifier_names_global"].items():
        if cnt > 1:
            info["duplicate_class_names_global"][name] = cnt

    return info


def fmt(n):
    return f"{n:,}"


def categorize(refs):
    c = Counter()
    for r in refs:
        if r.startswith("#//"):
            c["local(#//)"] += 1
        elif r.startswith("../../"):
            c["cross(../../)"] += 1
        elif r.startswith("../"):
            c["cross(../)"] += 1
        elif r.startswith("http"):
            c["http-uri"] += 1
        elif r.startswith("platform:/"):
            c["platform-resource"] += 1
        else:
            c["other"] += 1
    return c


def main():
    dummy_path = "/workspace/decompiler/autosar448/model/dummy.ecore"
    official_path = "/tmp/official_autosar448.ecore"

    print("=" * 80)
    print("Ecore 结构对比报告")
    print("=" * 80)

    print("\n[1] 解析中...")
    d = analyze(dummy_path)
    o = analyze(official_path)
    print(f"  dummy  : {d['path']}")
    print(f"  official: {o['path']}")

    print("\n[2] 根包基本信息")
    print(f"{'项目':<30}{'dummy':<35}{'official':<35}")
    print("-" * 100)
    print(f"{'根 name':<30}{str(d['root_name']):<35}{str(o['root_name']):<35}")
    print(f"{'根 nsURI':<30}{str(d['root_nsURI']):<35}{str(o['root_nsURI']):<35}")
    print(f"{'根 nsPrefix':<30}{str(d['root_nsPrefix']):<35}{str(o['root_nsPrefix']):<35}")
    print(f"{'modelPluginVersion':<30}{str(d['model_plugin_version']):<35}{str(o['model_plugin_version']):<35}")

    print("\n[3] 子包数量与名称")
    print(f"  dummy   EPackage 总数: {d['n_epackage_total']}")
    print(f"  official EPackage 总数: {o['n_epackage_total']}")
    print(f"  dummy   顶层子包数: {len(d['top_level_subpkg_names'])}")
    print(f"  official 顶层子包数: {len(o['top_level_subpkg_names'])}")
    d_top = set(d['top_level_subpkg_names'])
    o_top = set(o['top_level_subpkg_names'])
    print(f"  dummy 独有顶层子包: {sorted(d_top - o_top)}")
    print(f"  official 独有顶层子包: {sorted(o_top - d_top)}")
    print(f"  顶层子包顺序是否一致: {d['top_level_subpkg_names'] == o['top_level_subpkg_names']}")

    d_all = set(d['all_subpkg_names'])
    o_all = set(o['all_subpkg_names'])
    print(f"  dummy 全部子包名集合大小: {len(d_all)}")
    print(f"  official 全部子包名集合大小: {len(o_all)}")
    d_only = sorted(d_all - o_all)
    o_only = sorted(o_all - d_all)
    print(f"  dummy 独有子包名({len(d_only)}): {d_only[:30]}")
    print(f"  official 独有子包名({len(o_only)}): {o_only[:30]}")

    print("\n[4] EClassifier 数量对比")
    print(f"{'类型':<25}{'dummy':<20}{'official':<20}{'差异':<20}")
    print("-" * 85)
    for label, key in [
        ("EClass", "n_eclass"),
        ("EEnum", "n_eenum"),
        ("EDataType", "n_edatatype"),
        ("EEnumLiteral", "n_eenumliteral"),
        ("EReference", "n_ereference"),
        ("EAttribute", "n_eattribute"),
        ("EOperation", "n_eoperation"),
        ("EAnnotation", "n_eannotation"),
    ]:
        dv = d[key]
        ov = o[key]
        diff = dv - ov
        print(f"{label:<25}{fmt(dv):<20}{fmt(ov):<20}{fmt(diff):<20}")

    print("\n[5] EClassifier 名称集合差异（去重后）")
    d_names = set(d['all_classifier_names'])
    o_names = set(o['all_classifier_names'])
    print(f"  dummy   唯一 classifier 名: {len(d_names)}")
    print(f"  official 唯一 classifier 名: {len(o_names)}")
    print(f"  dummy 独有 classifier 名数量: {len(d_names - o_names)}")
    print(f"  official 独有 classifier 名数量: {len(o_names - d_names)}")
    d_only = sorted(d_names - o_names)
    o_only = sorted(o_names - d_names)
    print(f"  dummy 独有（前30）: {d_only[:30]}")
    print(f"  official 独有（前30）: {o_only[:30]}")

    print("\n[6] 重复 classifier 名（全局）")
    print(f"  dummy   重复名数量: {len(d['duplicate_class_names_global'])}")
    print(f"  official 重复名数量: {len(o['duplicate_class_names_global'])}")
    if d['duplicate_class_names_global']:
        print(f"  dummy 重复名样例（前20）: {d['duplicate_class_names_global'].most_common(20)}")
    if o['duplicate_class_names_global']:
        print(f"  official 重复名样例（前20）: {o['duplicate_class_names_global'].most_common(20)}")

    print("\n[7] eSuperTypes 引用统计")
    print(f"  dummy   eSuperTypes 引用总数: {len(d['esupertype_refs'])}")
    print(f"  official eSuperTypes 引用总数: {len(o['esupertype_refs'])}")
    print(f"  dummy   跨包引用数: {len(d['esupertype_cross_pkg'])}")
    print(f"  official 跨包引用数: {len(o['esupertype_cross_pkg'])}")
    print(f"  dummy   分类: {dict(categorize(d['esupertype_refs']))}")
    print(f"  official 分类: {dict(categorize(o['esupertype_refs']))}")

    print("\n[8] dummy.ecore 健康检查")
    print(f"  EClassifier 缺少 name: {d['n_eclassifiers_no_name']}")
    print(f"  EClass 缺少 name: {d['n_eclas_no_name']}")
    print(f"  EReference eType 为 null/空: {d['n_eref_etype_null']}")
    print(f"  EAttribute eAttributeType 为 null/空: {d['n_eattr_eattrtype_null']}")

    print("\n  [8.1] dummy.ecore 中 eType 为 null 的 EReference 样例（前20）:")
    cnt = 0
    tree = ET.parse(dummy_path)
    for ref in tree.iter():
        if local_name(ref) == "eStructuralFeatures" and ref.get(f"{{{XSI_NS}}}type") == "ecore:EReference":
            et = ref.get("eType")
            if et is None or et == "":
                cnt += 1
                if cnt <= 20:
                    nm = ref.get("name", "?")
                    print(f"    - EReference name={nm!r} (eType 缺失)")
    print(f"  共计 {cnt} 个 EReference eType 为空")

    print("\n  [8.2] dummy.ecore 中 eAttributeType 为 null 的 EAttribute 样例（前20）:")
    cnt = 0
    for ref in tree.iter():
        if local_name(ref) == "eStructuralFeatures" and ref.get(f"{{{XSI_NS}}}type") == "ecore:EAttribute":
            eat = ref.get("eAttributeType")
            if eat is None or eat == "":
                cnt += 1
                if cnt <= 20:
                    nm = ref.get("name", "?")
                    print(f"    - EAttribute name={nm!r} (eAttributeType 缺失)")
    print(f"  共计 {cnt} 个 EAttribute eAttributeType 为空")

    print("\n  [8.3] dummy.ecore 中 EClassifier 缺少 name 的样例（前20）:")
    cnt = 0
    for ref in tree.iter():
        if local_name(ref) == "eClassifiers":
            nm = ref.get("name")
            if nm is None or nm == "":
                cnt += 1
                xsi = ref.get(f"{{{XSI_NS}}}type", "?")
                if cnt <= 20:
                    print(f"    - eClassifiers xsi:type={xsi!r} name 缺失")
    print(f"  共计 {cnt} 个 eClassifiers 缺少 name")

    print("\n[9] eSuperTypes 引用解析检查（dummy.ecore）")
    d_root = ET.parse(dummy_path).getroot()
    parent_map = {c: p for p in d_root.iter() for c in p}

    def get_pkg_chain(elem):
        chain = []
        cur = elem
        while cur is not None and cur is not d_root:
            cur = parent_map.get(cur)
            if cur is not None and local_name(cur) == "EPackage":
                chain.append(cur.get("name"))
            if cur is d_root:
                break
        chain.reverse()
        return chain

    name_to_paths = defaultdict(list)
    path_to_name = {}
    for clf in d_root.iter():
        if local_name(clf) == "eClassifiers":
            xsi = clf.get(f"{{{XSI_NS}}}type", "")
            if xsi == "ecore:EClass":
                nm = clf.get("name")
                chain = get_pkg_chain(clf)
                frag = "//" + "/".join(chain + [nm]) if chain else "//" + nm
                name_to_paths[nm].append(frag)
                path_to_name[frag] = nm
                path_to_name["//" + nm] = nm

    unresolved_local = 0
    cross_pkg_gautosar = 0
    cross_pkg_other = 0
    samples_unresolved = []
    samples_gautosar = []
    for clf in d_root.iter():
        if local_name(clf) == "eClassifiers":
            est = clf.get("eSuperTypes")
            if est:
                for ref in est.split():
                    if ref.startswith("#//"):
                        if ref[1:] not in path_to_name and ref[2:] not in path_to_name:
                            leaf = ref.rsplit("/", 1)[-1]
                            if leaf not in name_to_paths:
                                unresolved_local += 1
                                if len(samples_unresolved) < 20:
                                    samples_unresolved.append((clf.get("name"), ref))
                    elif ref.startswith("../../") or ref.startswith("../"):
                        if "gautosar" in ref.lower():
                            cross_pkg_gautosar += 1
                            if len(samples_gautosar) < 10:
                                samples_gautosar.append((clf.get("name"), ref))
                        else:
                            cross_pkg_other += 1
    print(f"  本地 #// 引用无法解析: {unresolved_local}")
    if samples_unresolved:
        print(f"  样例（前20）:")
        for cls, ref in samples_unresolved:
            print(f"    - {cls} -> {ref}")
    print(f"  跨包引用指向 gautosar: {cross_pkg_gautosar}")
    print(f"  跨包引用指向其他: {cross_pkg_other}")
    if samples_gautosar:
        print(f"  gautosar 引用样例（前10）:")
        for cls, ref in samples_gautosar:
            print(f"    - {cls} -> {ref}")

    print("\n[10] eSuperTypes 跨包引用前缀分布（dummy）")
    cross_prefixes = Counter()
    for ref in d['esupertype_cross_pkg']:
        cross_prefixes[ref[:30]] += 1
    for p, c in cross_prefixes.most_common(15):
        print(f"  {c:>6}  {p}...")

    print("\n[11] 文件大小对比")
    ds = os.path.getsize(dummy_path)
    os_ = os.path.getsize(official_path)
    print(f"  dummy  : {fmt(ds)} bytes ({ds/1024/1024:.2f} MB)")
    print(f"  official: {fmt(os_)} bytes ({os_/1024/1024:.2f} MB)")
    print(f"  比例: {ds/os_:.2%}")

    print("\n[12] 结论摘要")
    issues = []
    if d['root_name'] != o['root_name']:
        issues.append(f"根包名不一致: dummy={d['root_name']} vs official={o['root_name']}")
    if d['root_nsURI'] != o['root_nsURI']:
        issues.append(f"nsURI 不一致: dummy={d['root_nsURI']} vs official={o['root_nsURI']}")
    if d['n_epackage_total'] != o['n_epackage_total']:
        issues.append(f"EPackage 总数不一致: dummy={d['n_epackage_total']} vs official={o['n_epackage_total']}")
    if d['n_eclass'] != o['n_eclass']:
        issues.append(f"EClass 数量不一致: dummy={d['n_eclass']} vs official={o['n_eclass']}")
    if d['n_eenum'] != o['n_eenum']:
        issues.append(f"EEnum 数量不一致: dummy={d['n_eenum']} vs official={o['n_eenum']}")
    if d['n_edatatype'] != o['n_edatatype']:
        issues.append(f"EDataType 数量不一致: dummy={d['n_edatatype']} vs official={o['n_edatatype']}")
    if d['n_ereference'] != o['n_ereference']:
        issues.append(f"EReference 数量不一致: dummy={d['n_ereference']} vs official={o['n_ereference']}")
    if d['n_eattribute'] != o['n_eattribute']:
        issues.append(f"EAttribute 数量不一致: dummy={d['n_eattribute']} vs official={o['n_eattribute']}")
    if d['n_eref_etype_null'] > 0:
        issues.append(f"dummy.ecore 有 {d['n_eref_etype_null']} 个 EReference 的 eType 为空")
    if d['n_eattr_eattrtype_null'] > 0:
        issues.append(f"dummy.ecore 有 {d['n_eattr_eattrtype_null']} 个 EAttribute 的 eAttributeType 为空")
    if d['n_eclassifiers_no_name'] > 0:
        issues.append(f"dummy.ecore 有 {d['n_eclassifiers_no_name']} 个 EClassifier 缺少 name")
    if unresolved_local > 0:
        issues.append(f"dummy.ecore 有 {unresolved_local} 个本地 eSuperTypes 引用无法解析")
    if d_names != o_names:
        issues.append(f"classifier 名称集合不一致: dummy 独有 {len(d_names - o_names)} 个, official 独有 {len(o_names - d_names)} 个")
    if d['model_plugin_version'] != o['model_plugin_version']:
        issues.append(f"modelPluginVersion 不一致: dummy={d['model_plugin_version']} vs official={o['model_plugin_version']}")

    if not issues:
        print("  ✓ 未发现明显结构差异")
    else:
        print(f"  发现 {len(issues)} 项差异/问题:")
        for i, iss in enumerate(issues, 1):
            print(f"  {i}. {iss}")


if __name__ == "__main__":
    main()
