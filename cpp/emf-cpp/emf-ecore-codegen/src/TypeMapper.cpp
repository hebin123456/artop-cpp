// TypeMapper.cpp —— EDataType → C++ 类型映射（模型驱动）
// 对齐 Java EMF codegen：EAttribute 的运行时类型由 EDataType.instanceClassName 决定。
// 所有 EDataType（内建 Ecore + 用户自定义）统一走 instanceClassName 映射，无特判。
#include "emf/ecore/codegen/TypeMapper.h"

#include <unordered_map>

namespace emf::ecore::codegen {

namespace {
// Java instanceClassName → C++ 类型（固定宽度，跨平台一致）
// 对齐 Java EcorePackageImpl 的 instanceClassName，统一映射到 C++ 等价物：
//   java.lang.String   -> std::string
//   java.lang.Boolean  -> bool
//   java.lang.Integer  -> int32_t
//   java.lang.Long     -> int64_t
//   java.lang.Short    -> int16_t
//   java.lang.Byte     -> int8_t
//   java.lang.Character-> char
//   java.lang.Double   -> double
//   java.lang.Float    -> float
//   java.math.BigInteger / 其他复杂类型 -> std::string（无原生等价物）
//   空（EEnum 或未指定 instanceClassName）-> std::string（枚举存字面量名）
const std::unordered_map<std::string, std::string>& instanceClassMap() {
    static const std::unordered_map<std::string, std::string> m = {
        {"java.lang.String",                    "std::string"},
        {"java.lang.Boolean",                   "bool"},
        {"boolean",                             "bool"},
        {"java.lang.Integer",                   "int32_t"},
        {"int",                                 "int32_t"},
        {"java.lang.Long",                      "int64_t"},
        {"long",                                "int64_t"},
        {"java.lang.Short",                     "int16_t"},
        {"short",                               "int16_t"},
        {"java.lang.Byte",                      "int8_t"},
        {"byte",                                "int8_t"},
        {"java.lang.Character",                 "char"},
        {"char",                                "char"},
        {"java.lang.Double",                    "double"},
        {"double",                              "double"},
        {"java.lang.Float",                     "float"},
        {"float",                               "float"},
        {"java.math.BigInteger",                "std::string"},
        {"java.math.BigDecimal",                "std::string"},
        {"javax.xml.datatype.XMLGregorianCalendar", "std::string"},
        {"java.util.Date",                      "std::string"},
        {"org.eclipse.emf.ecore.util.FeatureMap", "std::any"},
        {"org.eclipse.emf.common.util.Enumerator", "std::string"},  // EEnum 默认
    };
    return m;
}

// 内建 Ecore EDataType 名 → C++ 类型（仅 cppType(旧路径) 用）
const std::unordered_map<std::string, std::string>& ecoreBuiltinMap() {
    static const std::unordered_map<std::string, std::string> m = {
        {"EString",          "std::string"},
        {"EBoolean",         "bool"},
        {"EBooleanObject",   "bool"},
        {"EInt",             "int32_t"},
        {"EIntegerObject",   "int32_t"},
        {"ELong",            "int64_t"},
        {"ELongObject",      "int64_t"},
        {"EShort",           "int16_t"},
        {"EShortObject",     "int16_t"},
        {"EByte",            "int8_t"},
        {"EByteObject",      "int8_t"},
        {"EChar",            "char"},
        {"ECharacterObject", "char"},
        {"EFloat",           "float"},
        {"EFloatObject",     "float"},
        {"EDouble",          "double"},
        {"EDoubleObject",    "double"},
        {"EDate",            "std::string"},
        {"EBigInteger",      "std::string"},
        {"EBigDecimal",      "std::string"},
        {"EJavaObject",      "std::any"},
        {"EFeatureMapEntry", "std::any"},
        {"EFeatureMap",      "std::any"},
    };
    return m;
}

// 按 C++ 类型决定默认值字面量表达式
std::string defaultLitByCppType(const std::string& cppT, const std::string& literal) {
    if (literal.empty()) return "";
    if (cppT == "std::string") return "std::string(\"" + literal + "\")";
    if (cppT == "bool") return (literal == "true" || literal == "1") ? "true" : "false";
    return literal;  // 数值类型直接返回字面量
}
}  // namespace

std::string TypeMapper::cppTypeFromInstanceClass(const std::string& instanceClassName) {
    if (instanceClassName.empty()) return "std::string";  // EEnum / 未指定
    const auto& m = instanceClassMap();
    auto it = m.find(instanceClassName);
    if (it != m.end()) return it->second;
    // 未知 instanceClassName：默认 std::string（复杂 Java 类无原生 C++ 等价物）
    return "std::string";
}

std::string TypeMapper::cppType(const std::string& eDataTypeName) {
    // 旧路径：内建 Ecore EDataType 名映射。模型驱动新路径请用 cppTypeFromInstanceClass。
    const auto& m = ecoreBuiltinMap();
    auto it = m.find(eDataTypeName);
    if (it != m.end()) return it->second;
    return eDataTypeName;
}

std::string TypeMapper::includeFor(const std::string& cppType) {
    if (cppType == "std::string") return "<string>";
    if (cppType == "std::any")    return "<any>";
    if (cppType == "bool")        return "";
    if (cppType == "int8_t"  || cppType == "int16_t" ||
        cppType == "int32_t" || cppType == "int64_t" ||
        cppType == "uint8_t" || cppType == "uint16_t" ||
        cppType == "uint32_t"|| cppType == "uint64_t") {
        return "<cstdint>";
    }
    if (cppType == "float" || cppType == "double" || cppType == "char") return "";
    return "";
}

std::string TypeMapper::defaultValueLiteral(const std::string& eDataTypeName,
                                            const std::string& literal) {
    // 旧签名：eDataTypeName 是 EDataType 名（如 EString）。
    return defaultLitByCppType(cppType(eDataTypeName), literal);
}

std::string TypeMapper::defaultValueLiteralByCppType(const std::string& cppType,
                                                     const std::string& literal) {
    return defaultLitByCppType(cppType, literal);
}

}  // namespace emf::ecore::codegen
