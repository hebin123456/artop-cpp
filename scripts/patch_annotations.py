#!/usr/bin/env python3
"""
patch_annotations.py — 从 ecore 提取完整 annotation details，修补生成的 .cpp 文件。

背景：旧版 codegen 过滤了 annotation details（只保留 5 个 TaggedValues + 2 个 ExtendedMetaData），
且 xml.name 常被错误设为复数。当前 codegen 源码的 emitAnnotations 已正确，但重新生成会 OOM
（18MB ecore 加载需要 4GB+，cgroup 限制 4GB）。

本脚本绕过 codegen，直接：
1. 用 Python xml.etree 解析 ecore（内存高效）
2. 为每个 EClass / EStructuralFeature 收集所有 eAnnotations 及其 details
3. 在生成的 .cpp 文件中找到对应的 annotation 块并替换为完整版本

对齐 Java: ARTOP 无自定义 ExtendedMetaData，feature→XML 名映射完全依赖 EAnnotation。
"""
import os
import re
import sys
import xml.etree.ElementTree as ET

# ecore XML 命名空间
NS = {
    'ecore': 'http://www.eclipse.org/emf/2002/Ecore',
    'xmi': 'http://www.omg.org/XMI',
    'xsi': 'http://www.w3.org/2001/XMLSchema-instance',
}

# 注册命名空间，避免输出时加前缀
for prefix, uri in NS.items():
    ET.register_namespace(prefix, uri)


def strip_ns(tag):
    """去掉命名空间前缀：{uri}local → local"""
    if tag and tag[0] == '{':
        return tag.split('}', 1)[1]
    return tag


def escape_cpp_str(s):
    """C++ 字符串字面量转义"""
    if s is None:
        return ""
    r = []
    for c in s:
        if c == '"' or c == '\\':
            r.append('\\')
            r.append(c)
        elif c == '\n':
            r.append('\\n')
        elif c == '\r':
            r.append('\\r')
        elif c == '\t':
            r.append('\\t')
        else:
            r.append(c)
    return ''.join(r)


def collect_annotations(elem):
    """收集 XML 元素的所有 eAnnotations 子元素，返回 [(source, [(key, value), ...]), ...]"""
    anns = []
    for child in elem:
        if strip_ns(child.tag) != 'eAnnotations':
            continue
        source = child.get('source', '')
        details = []
        for d in child:
            if strip_ns(d.tag) != 'details':
                continue
            k = d.get('key', '')
            v = d.get('value', '')
            details.append((k, v))
        anns.append((source, details))
    return anns


def gen_annotation_cpp(anns, member_expr, indent="        "):
    """生成 C++ annotation 重建代码块，对齐 PackageEmitter::emitAnnotations"""
    lines = []
    for source, details in anns:
        lines.append(indent + "{")
        lines.append(indent + "    auto* __ann = emf::ecore::EcoreFactory::instance().createEAnnotation();")
        lines.append(indent + '    __ann->setSource("' + escape_cpp_str(source) + '");')
        for k, v in details:
            lines.append(indent + '    __ann->setDetail("' + escape_cpp_str(k) + '", "' +
                         escape_cpp_str(v) + '");')
        lines.append(indent + "    " + member_expr + "->addEAnnotation(__ann);")
        lines.append(indent + "}")
    return '\n'.join(lines)


def parse_ecore(ecore_path):
    """
    解析 ecore，返回两个 map：
      classes: {class_name: [(source, [(key, value), ...]), ...]}
      features: {(class_name, feature_name): [(source, [(key, value), ...]), ...]}

    注意：class_name 在 ecore 内可能重复（不同包），但生成的 .cpp 文件按包分文件，
    所以同包内 class_name 唯一。跨包重复由文件隔离处理。
    """
    classes = {}
    features = {}

    # 用 iterparse 流式解析，减少内存
    # 但 ecore 结构需要知道 parent EClass，所以用递归遍历
    tree = ET.parse(ecore_path)
    root = tree.getroot()

    def visit_package(pkg_elem):
        """递归遍历 EPackage，收集 EClass 和 EStructuralFeature 的 annotations"""
        for child in pkg_elem:
            local = strip_ns(child.tag)
            if local == 'eClassifiers':
                xsi_type = child.get('{http://www.w3.org/2001/XMLSchema-instance}type', '')
                cls_name = child.get('name', '')
                if 'EClass' in xsi_type:
                    anns = collect_annotations(child)
                    if anns:
                        classes[cls_name] = anns
                    # 遍历 eStructuralFeatures
                    for sf in child:
                        if strip_ns(sf.tag) != 'eStructuralFeatures':
                            continue
                        sf_name = sf.get('name', '')
                        sf_anns = collect_annotations(sf)
                        if sf_anns:
                            features[(cls_name, sf_name)] = sf_anns
                elif 'EEnum' in xsi_type or 'EDataType' in xsi_type:
                    anns = collect_annotations(child)
                    if anns:
                        classes[cls_name] = anns
            elif local == 'eSubpackages':
                visit_package(child)

    visit_package(root)
    return classes, features


def find_and_replace_annotation_blocks(content, classes_map, features_map):
    """
    在生成的 .cpp 文件内容中，找到每个 feature/class 的 annotation 块并替换。

    策略：
    1. 找到 `MEMBER->setName("name");` 行，记录 MEMBER 和 name
    2. 找到紧随其后的 `{ ... }` annotation 块（以 `auto* __tv` 或 `auto* __ann` 开头）
    3. 根据 MEMBER 名推断是 EClass 还是 EStructuralFeature
       - EClass: MEMBER 形如 `XxxClass_class_`
       - EStructuralFeature: MEMBER 形如 `XxxClass_featureName_ref_` 或 `_attr_`
    4. 从 map 中查找正确的 annotations，替换块
    """
    lines = content.split('\n')
    output = []
    i = 0
    replaced_count = 0

    # 正则：匹配 MEMBER->setName("name");
    re_setname = re.compile(r'^\s*(\w+)->setName\("([^"]+)"\);')

    # 正则：匹配 annotation 块开始 {
    re_block_start = re.compile(r'^\s*\{$')

    while i < len(lines):
        line = lines[i]
        m = re_setname.match(line)
        if m:
            member = m.group(1)
            name = m.group(2)
            output.append(line)
            i += 1

            # 跳过 setup 行（setLowerBound, setUpperBound, setContainment, setEReferenceType, setEAttributeType, setID 等）
            # 直到找到 annotation 块开始 {
            block_start_idx = None
            while i < len(lines):
                l = lines[i]
                # 跳过空行和 setup 行
                stripped = l.strip()
                if stripped == '' or stripped.startswith('//'):
                    output.append(l)
                    i += 1
                    continue
                if re_block_start.match(l):
                    block_start_idx = i
                    break
                # 如果遇到非 setup 行（如 addEStructuralFeature），说明没有 annotation 块
                if 'addEStructuralFeature' in stripped or 'addEClassifier' in stripped:
                    output.append(l)
                    i += 1
                    break
                # 其他 setup 行
                output.append(l)
                i += 1

            if block_start_idx is not None:
                # 找到 annotation 块结束 }
                block_end_idx = block_start_idx + 1
                depth = 1
                while block_end_idx < len(lines) and depth > 0:
                    for ch in lines[block_end_idx]:
                        if ch == '{':
                            depth += 1
                        elif ch == '}':
                            depth -= 1
                    if depth == 0:
                        break
                    block_end_idx += 1

                if block_end_idx < len(lines):
                    # 提取块内容，判断是否是 annotation 块
                    block_content = '\n'.join(lines[block_start_idx:block_end_idx + 1])
                    if '__tv' in block_content or '__ann' in block_content or '__em' in block_content:
                        # 这是 annotation 块，需要替换
                        # 推断 owner 类型
                        anns = None
                        if member.endswith('_class_'):
                            # EClass: member = "Compu_class_", name = "Compu"
                            anns = classes_map.get(name)
                        elif member.endswith('_ref_') or member.endswith('_attr_'):
                            # EStructuralFeature: member = "CompuScales_compuScales_ref_"
                            # 需要从 member 名中提取 class name
                            # 格式: <ClassName>_<featureName>_{ref_,attr_}
                            base = member[:-5]  # 去掉 _ref_ 或 _attr_
                            # ClassName 是第一个 _ 之前的部分？不对，class name 可能包含 _
                            # 更好的方法：feature name = name 参数，class name = base 去掉 feature name
                            if base.endswith('_' + name):
                                cls_name = base[:-(len(name) + 1)]
                                anns = features_map.get((cls_name, name))
                            else:
                                # 尝试其他方式：遍历所有 classes 找匹配
                                for cls_name in classes_map:
                                    if base.startswith(cls_name + '_'):
                                        anns = features_map.get((cls_name, name))
                                        if anns:
                                            break
                        elif member.endswith('_enum_') or member.endswith('_dt_'):
                            # EEnum / EDataType
                            anns = classes_map.get(name)

                        if anns:
                            # 生成替换内容
                            indent = '        '
                            new_block = gen_annotation_cpp(anns, member, indent)
                            output.append(new_block)
                            replaced_count += 1
                        else:
                            # 没找到匹配，保留原块
                            for l in lines[block_start_idx:block_end_idx + 1]:
                                output.append(l)
                        i = block_end_idx + 1
                    else:
                        # 不是 annotation 块，保留
                        for l in lines[block_start_idx:block_end_idx + 1]:
                            output.append(l)
                        i = block_end_idx + 1
                else:
                    # 块没有结束，保留剩余
                    for l in lines[block_start_idx:]:
                        output.append(l)
                    i = len(lines)
        else:
            output.append(line)
            i += 1

    return '\n'.join(output), replaced_count


def main():
    ecore_path = sys.argv[1] if len(sys.argv) > 1 else '/workspace/models/autosar448/autosar448.ecore'
    gen_dir = sys.argv[2] if len(sys.argv) > 2 else '/workspace/.build_cache/autosar448_combined'

    print(f"[patch] ecore: {ecore_path}")
    print(f"[patch] gen_dir: {gen_dir}")

    # 1. 解析 ecore
    print("[patch] parsing ecore...")
    classes_map, features_map = parse_ecore(ecore_path)
    print(f"[patch] classes with annotations: {len(classes_map)}")
    print(f"[patch] features with annotations: {len(features_map)}")

    # 2. 找到所有生成的 Package.cpp 文件
    pkg_files = []
    for root, dirs, files in os.walk(gen_dir):
        for f in files:
            if f.endswith('Package.cpp'):
                pkg_files.append(os.path.join(root, f))
    print(f"[patch] found {len(pkg_files)} Package.cpp files")

    # 3. 修补每个文件
    total_replaced = 0
    patched_files = 0
    for fpath in sorted(pkg_files):
        with open(fpath, 'r', encoding='utf-8', errors='replace') as f:
            content = f.read()
        new_content, count = find_and_replace_annotation_blocks(content, classes_map, features_map)
        if count > 0:
            with open(fpath, 'w', encoding='utf-8') as f:
                f.write(new_content)
            total_replaced += count
            patched_files += 1
            if patched_files <= 5 or count > 10:
                print(f"  [patch] {os.path.relpath(fpath, gen_dir)}: {count} blocks replaced")

    print(f"[patch] done: {total_replaced} annotation blocks replaced in {patched_files} files")


if __name__ == '__main__':
    main()
