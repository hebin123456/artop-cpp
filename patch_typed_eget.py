#!/usr/bin/env python3
# patch_typed_eget.py — 批量 patch 生成 .cpp/.h：新增 4 个类型化 eGet override（方案 B 子集）
#
# 目标：为单值 attribute（string/bool/int64 等）和单值 reference 生成类型化取值方法，
# 避免 saver 走 eGet(int)+std::any 装箱。新增 4 个 override：
#   bool eGetString(int featureID, std::string& out) const
#   bool eGetInt64(int featureID, int64_t& out) const
#   bool eGetBool(int featureID, bool& out) const
#   bool eGetEObject(int featureID, emf::common::EObject*& out) const
# 多值/不匹配返回 false，调用方 fallback 到 eGet(featureID)+std::any。
#
# 匹配 eGet(int) 中的两种单值 case：
#   单值 attribute:  case XXX__: return std::any{fieldName_};
#   单值 reference:  case XXX__: return std::any(static_cast<emf::common::EObject*>(fieldName_));
# 多值 case（含 EObjectRefView / std::vector）跳过。
#
# 属性 cppType 从 eSet(int) 的 setX(std::any_cast<TYPE>(value)) 提取。
import os, re, sys

COMBINED = "/workspace/.build_cache/autosar448_combined"

# 单值 attribute: case XXX__: return std::any{fieldName_};
RE_SINGLE_ATTR = re.compile(
    r'case (\w+): return std::any\{(\w+)\};'
)
# 单值 reference: case XXX__: return std::any(static_cast<emf::common::EObject*>(fieldName_));
RE_SINGLE_REF = re.compile(
    r'case (\w+): return std::any\(static_cast<emf::common::EObject\*>\((\w+)\)\);'
)
# eSet 单值 attribute 类型: case XXX__: setY(std::any_cast<TYPE>(value)); break;
RE_SET_ATTR_TYPE = re.compile(
    r'case (\w+): set\w+\(std::any_cast<([^>]+)>\(value\)\); break;'
)
# eGet(int) 整个函数（用于定位插入点）
RE_EGET_INT = re.compile(
    r'(std::any (\w+)::eGet\(int featureID\) const \{.*?\n\})',
    re.DOTALL
)

INT_CPP_TYPES = {
    'int64_t', 'int32_t', 'int16_t', 'int8_t',
    'uint64_t', 'uint32_t', 'uint16_t', 'uint8_t',
    'long', 'int',
}

def patch_cpp(content):
    """在 eGet(int) 之后插入 4 个类型化 override。返回 (new_content, patched?)"""
    m = RE_EGET_INT.search(content)
    if not m:
        return content, False
    cls_name = m.group(2)
    eget_body = m.group(1)
    insert_pos = m.end()

    # 收集单值 attribute: {caseConst: fieldName}
    single_attrs = {}
    for cm in RE_SINGLE_ATTR.finditer(eget_body):
        single_attrs[cm.group(1)] = cm.group(2)

    # 收集单值 reference: {caseConst: fieldName}
    single_refs = {}
    for cm in RE_SINGLE_REF.finditer(eget_body):
        single_refs[cm.group(1)] = cm.group(2)

    if not single_attrs and not single_refs:
        return content, False  # 无单值字段，无需 patch

    # 从 eSet(int) 提取 attribute cppType
    attr_types = {}  # caseConst -> cppType
    for cm in RE_SET_ATTR_TYPE.finditer(content):
        case_const = cm.group(1)
        cpp_type = cm.group(2).strip()
        if case_const in single_attrs:
            attr_types[case_const] = cpp_type

    # 按类型分组
    string_cases = []
    bool_cases = []
    int64_cases = []
    for case_const, field in single_attrs.items():
        cpp_type = attr_types.get(case_const, "std::string")  # 默认 string（enum 等）
        if cpp_type == "std::string":
            string_cases.append((case_const, field))
        elif cpp_type == "bool":
            bool_cases.append((case_const, field))
        elif cpp_type in INT_CPP_TYPES:
            int64_cases.append((case_const, field))
        # double/float/其他类型不处理，fallback 到 eGet

    ref_cases = [(c, f) for c, f in single_refs.items()]

    # 生成 4 个 override
    blocks = []

    def gen_override(kind_tag, out_type, cases, transform=""):
        if not cases:
            return None
        lines = []
        lines.append(f"bool {cls_name}::eGet{kind_tag}(int featureID, {out_type} out) const {{")
        lines.append("    switch (featureID) {")
        for case_const, field in cases:
            if transform:
                lines.append(f"        case {case_const}: out = {transform}({field}); return true;")
            else:
                lines.append(f"        case {case_const}: out = {field}; return true;")
        lines.append("        default: return false;")
        lines.append("    }")
        lines.append("}")
        return "\n".join(lines)

    b = gen_override("String", "std::string&", string_cases)
    if b: blocks.append(b)
    b = gen_override("Bool", "bool&", bool_cases)
    if b: blocks.append(b)
    b = gen_override("Int64", "int64_t&", int64_cases, "static_cast<int64_t>")
    if b: blocks.append(b)

    # eGetEObject
    if ref_cases:
        lines = []
        lines.append(f"bool {cls_name}::eGetEObject(int featureID, emf::common::EObject*& out) const {{")
        lines.append("    switch (featureID) {")
        for case_const, field in ref_cases:
            lines.append(f"        case {case_const}: out = static_cast<emf::common::EObject*>({field}); return true;")
        lines.append("        default: return false;")
        lines.append("    }")
        lines.append("}")
        blocks.append("\n".join(lines))

    if not blocks:
        return content, False

    insertion = "\n" + "\n\n".join(blocks) + "\n\n"
    new_content = content[:insert_pos] + insertion + content[insert_pos:]
    return new_content, True


# .h patch：在 eContents() override 声明前插入 4 个 override 声明
HEADER_DECL = (
    "    bool eGetString(int featureID, std::string& out) const override;\n"
    "    bool eGetInt64(int featureID, int64_t& out) const override;\n"
    "    bool eGetBool(int featureID, bool& out) const override;\n"
    "    bool eGetEObject(int featureID, emf::common::EObject*& out) const override;\n"
)
# 匹配 eContents 声明行（在它前面插入）
RE_HEADER_INSERT = re.compile(
    r'(std::vector<emf::common::EObject\*> eContents\(\) const override;)'
)

def patch_header(content):
    if "eGetString(int featureID" in content:
        return content, False  # 已 patch
    m = RE_HEADER_INSERT.search(content)
    if not m:
        return content, False
    # 缩进取匹配行行首到匹配起点的空白（生成代码用 8 空格）
    line_start = content.rfind('\n', 0, m.start()) + 1
    indent = content[line_start:m.start()]
    decl = indent + HEADER_DECL.replace("\n", "\n" + indent).rstrip("\n") + "\n"
    # 插入到匹配行之前（保留匹配行原样，含其缩进）
    new_content = content[:line_start] + decl + content[line_start:]
    return new_content, True


def main():
    cpp_patched = 0
    h_patched = 0
    cpp_skipped = 0
    for root, dirs, files in os.walk(COMBINED):
        for fn in files:
            path = os.path.join(root, fn)
            if fn.endswith('.cpp'):
                try:
                    with open(path, 'r', encoding='utf-8') as f:
                        content = f.read()
                except:
                    continue
                if "eGetString(int featureID" in content:
                    cpp_skipped += 1
                    continue
                if 'eGet(int featureID) const' not in content:
                    continue
                new_content, patched = patch_cpp(content)
                if patched:
                    with open(path, 'w', encoding='utf-8') as f:
                        f.write(new_content)
                    cpp_patched += 1
            elif fn.endswith('.h'):
                try:
                    with open(path, 'r', encoding='utf-8') as f:
                        content = f.read()
                except:
                    continue
                if "eGetString(int featureID" in content:
                    continue
                if 'eContents() const override' not in content:
                    continue
                new_content, patched = patch_header(content)
                if patched:
                    with open(path, 'w', encoding='utf-8') as f:
                        f.write(new_content)
                    h_patched += 1
    print(f"patched {cpp_patched} .cpp files, {h_patched} .h files, {cpp_skipped} .cpp already patched")

if __name__ == '__main__':
    main()
