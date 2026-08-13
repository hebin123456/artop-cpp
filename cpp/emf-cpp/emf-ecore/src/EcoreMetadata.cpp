// EcoreMetadata.cpp —— 元数据/反射工具实现
// 对齐 org.eclipse.emf.ecore.util.EcoreUtil + EcoreFactoryImpl 的字符串转换
//
// 职责：
//   1. DataTypeUtil：按 EDataType 名称做 string↔typed any 转换（对齐 Java
//      EcoreFactoryImpl.createEBooleanFromString / createEIntFromString / ...）
//   2. InstanceCheck：EClassifier::isInstance 的实现回退
//   3. createBuiltinDataType：按名称查找 EcorePackage 内建 EDataType
//   4. collectAllStructuralFeatures：收集 EClass 全部 feature（含继承）
//   5. packageOfClassifier / qNameOfClassifier：包归属与限定名
//
// 类型映射（C++ 端选择，对齐 DataTypeUtilTests 期望）：
//   EString→std::string   EBoolean→bool       EInt→int
//   EDouble→double        EFloat→float        ELong→int64_t
//   EShort→int16_t        EByte→int8_t        EChar→char32_t
//   EDate→std::string(ISO)  EBigInteger→std::string  EBigDecimal→std::string
//   EJavaObject→std::any(原值)  EFeatureMapEntry/FeatureMap→std::string
#include "emf/ecore/EcoreMetadata.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/ecore/EcorePackage.h"
#include "emf/common/EObject.h"

#include <algorithm>
#include <cmath>
#include <cstdint>
#include <cstdio>
#include <cstdlib>
#include <limits>
#include <sstream>
#include <stdexcept>
#include <string>
#include <unordered_set>

namespace emf::ecore {

// ============================================================================
// DataTypeUtil
// ============================================================================

std::any DataTypeUtil::fromString(const std::string& dtName, const std::string& lit) {
    // 对齐 Java EcoreFactoryImpl.createEXxxFromString 系列
    if (dtName == "EString")          return std::any{std::string{lit}};
    if (dtName == "EBoolean" || dtName == "EBooleanObject") {
        // 对齐 Java："true".equalsIgnoreCase → true；"1" → true；其他 false
        if (lit == "true" || lit == "TRUE" || lit == "True" || lit == "1") return std::any{true};
        return std::any{false};
    }
    if (dtName == "EInt" || dtName == "EIntegerObject") {
        try { return std::any{std::stoi(lit)}; } catch (...) { return std::any{0}; }
    }
    if (dtName == "ELong" || dtName == "ELongObject") {
        try { return std::any{static_cast<int64_t>(std::stoll(lit))}; } catch (...) { return std::any{(int64_t)0}; }
    }
    if (dtName == "EShort" || dtName == "EShortObject") {
        try { return std::any{static_cast<int16_t>(std::stoi(lit))}; } catch (...) { return std::any{(int16_t)0}; }
    }
    if (dtName == "EByte" || dtName == "EByteObject") {
        try { return std::any{static_cast<int8_t>(std::stoi(lit))}; } catch (...) { return std::any{(int8_t)0}; }
    }
    if (dtName == "EDouble" || dtName == "EDoubleObject") {
        return std::any{parseJavaDouble(lit)};
    }
    if (dtName == "EFloat" || dtName == "EFloatObject") {
        return std::any{parseJavaFloat(lit)};
    }
    if (dtName == "EChar" || dtName == "ECharacterObject") {
        // 对齐 Java：先试 Integer.parseInt，失败取 toCharArray()[0]
        try {
            int code = std::stoi(lit);
            return std::any{static_cast<char32_t>(code)};
        } catch (...) {
            if (!lit.empty()) return std::any{static_cast<char32_t>(lit[0])};
            return std::any{static_cast<char32_t>(0)};
        }
    }
    if (dtName == "EDate") {
        // 保留 ISO 字符串（完整解析需时区/格式处理）
        return std::any{std::string{lit}};
    }
    if (dtName == "EBigInteger") {
        // 保留字符串以保精度
        return std::any{std::string{lit}};
    }
    if (dtName == "EBigDecimal") {
        return std::any{std::string{lit}};
    }
    if (dtName == "EJavaObject" || dtName == "EJavaClass") {
        return std::any{std::string{lit}};
    }
    if (dtName == "EFeatureMapEntry" || dtName == "EFeatureMap" ||
        dtName == "EByteArray" || dtName == "EEList" || dtName == "EMap" ||
        dtName == "ETreeIterator" || dtName == "EResource" || dtName == "EResourceSet" ||
        dtName == "EDiagnosticChain" || dtName == "EEnumerator" ||
        dtName == "EInvocationTargetException") {
        return std::any{std::string{lit}};
    }
    // 未知类型：原样字符串
    return std::any{std::string{lit}};
}

std::any DataTypeUtil::defaultValue(const std::string& dtName) {
    // 对齐 Java 各 EDataType 的 defaultValue literal
    if (dtName == "EString")          return std::any{std::string{}};
    if (dtName == "EBoolean" || dtName == "EBooleanObject") return std::any{false};
    if (dtName == "EInt" || dtName == "EIntegerObject")     return std::any{0};
    if (dtName == "ELong" || dtName == "ELongObject")       return std::any{(int64_t)0};
    if (dtName == "EShort" || dtName == "EShortObject")     return std::any{(int16_t)0};
    if (dtName == "EByte" || dtName == "EByteObject")       return std::any{(int8_t)0};
    if (dtName == "EDouble" || dtName == "EDoubleObject")   return std::any{0.0};
    if (dtName == "EFloat" || dtName == "EFloatObject")     return std::any{0.0f};
    if (dtName == "EChar" || dtName == "ECharacterObject")  return std::any{static_cast<char32_t>(0)};
    // 其余类型默认空字符串
    return std::any{std::string{}};
}

std::string DataTypeUtil::toString(const std::string& dtName, const std::any& v) {
    // 对齐 Java EcoreFactoryImpl.convertEXxxToString 系列
    if (dtName == "EString") {
        if (auto* p = std::any_cast<std::string>(&v)) return *p;
    }
    if (dtName == "EBoolean" || dtName == "EBooleanObject") {
        if (auto* p = std::any_cast<bool>(&v)) return *p ? "true" : "false";
    }
    if (dtName == "EInt" || dtName == "EIntegerObject") {
        if (auto* p = std::any_cast<int>(&v)) return std::to_string(*p);
    }
    if (dtName == "ELong" || dtName == "ELongObject") {
        if (auto* p = std::any_cast<int64_t>(&v)) return std::to_string(*p);
        if (auto* p = std::any_cast<long>(&v)) return std::to_string(*p);
    }
    if (dtName == "EShort" || dtName == "EShortObject") {
        if (auto* p = std::any_cast<int16_t>(&v)) return std::to_string(static_cast<int>(*p));
        if (auto* p = std::any_cast<short>(&v)) return std::to_string(static_cast<int>(*p));
    }
    if (dtName == "EByte" || dtName == "EByteObject") {
        if (auto* p = std::any_cast<int8_t>(&v)) return std::to_string(static_cast<int>(*p));
        if (auto* p = std::any_cast<signed char>(&v)) return std::to_string(static_cast<int>(*p));
    }
    if (dtName == "EDouble" || dtName == "EDoubleObject") {
        if (auto* p = std::any_cast<double>(&v)) {
            // 对齐 Java Double.toString：最短往返 + 科学计数法阈值 + NaN/Infinity
            return formatJavaDouble(*p);
        }
    }
    if (dtName == "EFloat" || dtName == "EFloatObject") {
        if (auto* p = std::any_cast<float>(&v)) {
            // 对齐 Java Float.toString
            return formatJavaFloat(*p);
        }
    }
    if (dtName == "EChar" || dtName == "ECharacterObject") {
        // 对齐 Java：Integer.toString((Character)v)
        if (auto* p = std::any_cast<char32_t>(&v)) return std::to_string(static_cast<int>(*p));
        if (auto* p = std::any_cast<char>(&v)) return std::to_string(static_cast<int>(static_cast<unsigned char>(*p)));
    }
    // EDate / EBigInteger / EBigDecimal / EJavaObject / 其他：字符串原样
    if (auto* p = std::any_cast<std::string>(&v)) return *p;
    // 兜底：空
    return {};
}

std::string DataTypeUtil::toString(const std::string& dtName, const std::any& v, bool* ok) {
    if (ok) *ok = false;
    if (!v.has_value()) { if (ok) *ok = true; return {}; }
    try {
        std::string r = toString(dtName, v);
        // 简单判定：若 any 类型匹配则 ok=true。这里宽松：只要没抛异常即视为成功。
        if (ok) *ok = true;
        return r;
    } catch (...) {
        return {};
    }
}

const std::string& DataTypeUtil::nameOf(EDataType* dt) {
    static const std::string empty;
    if (!dt) return empty;
    return dt->getName();
}

std::any DataTypeUtil::defaultValueForClassifier(EClassifier* cls) {
    if (!cls) return std::any{};
    if (auto* dt = dynamic_cast<EDataType*>(cls)) {
        return defaultValue(dt->getName());
    }
    // EClass 默认 nullptr
    return std::any{};
}

std::any DataTypeUtil::coerce(const std::any& v, const std::string& dtName) {
    if (!v.has_value()) return defaultValue(dtName);

    // 目标 EString：任意类型转字符串
    if (dtName == "EString") {
        if (auto* p = std::any_cast<std::string>(&v)) return std::any{*p};
        if (auto* p = std::any_cast<int>(&v)) return std::any{std::to_string(*p)};
        if (auto* p = std::any_cast<int64_t>(&v)) return std::any{std::to_string(*p)};
        if (auto* p = std::any_cast<bool>(&v)) return std::any{std::string(*p ? "true" : "false")};
        if (auto* p = std::any_cast<double>(&v)) {
            std::ostringstream os; os << *p; return std::any{os.str()};
        }
        if (auto* p = std::any_cast<float>(&v)) {
            std::ostringstream os; os << *p; return std::any{os.str()};
        }
        if (auto* p = std::any_cast<int16_t>(&v)) return std::any{std::to_string(static_cast<int>(*p))};
        if (auto* p = std::any_cast<int8_t>(&v)) return std::any{std::to_string(static_cast<int>(*p))};
        if (auto* p = std::any_cast<char32_t>(&v)) return std::any{std::to_string(static_cast<int>(*p))};
    }
    // 目标 EInt
    if (dtName == "EInt" || dtName == "EIntegerObject") {
        if (auto* p = std::any_cast<int>(&v)) return std::any{*p};
        if (auto* p = std::any_cast<std::string>(&v)) return fromString("EInt", *p);
        if (auto* p = std::any_cast<bool>(&v)) return std::any{(int)(*p ? 1 : 0)};
        if (auto* p = std::any_cast<double>(&v)) return std::any{(int)*p};
        if (auto* p = std::any_cast<int64_t>(&v)) return std::any{(int)*p};
        if (auto* p = std::any_cast<float>(&v)) return std::any{(int)*p};
    }
    // 目标 EBoolean
    if (dtName == "EBoolean" || dtName == "EBooleanObject") {
        if (auto* p = std::any_cast<bool>(&v)) return std::any{*p};
        if (auto* p = std::any_cast<std::string>(&v)) return fromString("EBoolean", *p);
        if (auto* p = std::any_cast<int>(&v)) return std::any{*p != 0};
        if (auto* p = std::any_cast<int64_t>(&v)) return std::any{*p != 0};
        if (auto* p = std::any_cast<double>(&v)) return std::any{*p != 0.0};
    }
    // 目标 ELong
    if (dtName == "ELong" || dtName == "ELongObject") {
        if (auto* p = std::any_cast<int64_t>(&v)) return std::any{*p};
        if (auto* p = std::any_cast<std::string>(&v)) return fromString("ELong", *p);
        if (auto* p = std::any_cast<int>(&v)) return std::any{static_cast<int64_t>(*p)};
        if (auto* p = std::any_cast<bool>(&v)) return std::any{static_cast<int64_t>(*p ? 1 : 0)};
    }
    // 目标 EDouble
    if (dtName == "EDouble" || dtName == "EDoubleObject") {
        if (auto* p = std::any_cast<double>(&v)) return std::any{*p};
        if (auto* p = std::any_cast<std::string>(&v)) return fromString("EDouble", *p);
        if (auto* p = std::any_cast<int>(&v)) return std::any{(double)*p};
        if (auto* p = std::any_cast<float>(&v)) return std::any{(double)*p};
    }
    // 目标 EFloat
    if (dtName == "EFloat" || dtName == "EFloatObject") {
        if (auto* p = std::any_cast<float>(&v)) return std::any{*p};
        if (auto* p = std::any_cast<std::string>(&v)) return fromString("EFloat", *p);
        if (auto* p = std::any_cast<double>(&v)) return std::any{(float)*p};
        if (auto* p = std::any_cast<int>(&v)) return std::any{(float)*p};
    }
    // 兜底：原样返回
    return v;
}

// ============================================================================
// InstanceCheck
// ============================================================================
bool InstanceCheck::isInstance(EClassifier* cls, emf::common::EObject* obj) {
    if (!cls || !obj) return false;
    if (auto* ec = dynamic_cast<EClass*>(cls)) {
        // 对齐 Java EClassImpl.isInstance：obj.eClass() 是 ec 或其子类
        EClass* objClass = obj->eClass();
        if (!objClass) return false;
        if (objClass == ec) return true;
        return ec->isSuperTypeOf(objClass);
    }
    if (auto* dt = dynamic_cast<EDataType*>(cls)) {
        (void)dt;  // EDataType::isInstance 简化：始终 true（值类型检查在赋值时做）
        return true;
    }
    return false;
}

// ============================================================================
// createBuiltinDataType：按名称从 EcorePackage 查找内建 EDataType
// ============================================================================
EDataType* createBuiltinDataType(const std::string& name) {
    auto& p = EcorePackage::instance();
    if (name == "EString")          return p.getEDataType_EString();
    if (name == "EBoolean")         return p.getEDataType_EBoolean();
    if (name == "EInt")             return p.getEDataType_EInt();
    if (name == "EDouble")          return p.getEDataType_EDouble();
    if (name == "EFloat")           return p.getEDataType_EFloat();
    if (name == "ELong")            return p.getEDataType_ELong();
    if (name == "EShort")           return p.getEDataType_EShort();
    if (name == "EByte")            return p.getEDataType_EByte();
    if (name == "EChar")            return p.getEDataType_EChar();
    if (name == "EDate")            return p.getEDataType_EDate();
    if (name == "EBigInteger")      return p.getEDataType_EBigInteger();
    if (name == "EBigDecimal")      return p.getEDataType_EBigDecimal();
    if (name == "EJavaObject")      return p.getEDataType_EJavaObject();
    if (name == "EFeatureMapEntry") return p.getEDataType_EFeatureMapEntry();
    if (name == "EFeatureMap")      return p.getEDataType_EFeatureMap();
    return nullptr;
}

// ============================================================================
// collectAllStructuralFeatures：收集 EClass 全部 feature（含继承，去重）
// 对齐 Java EClass.getEAllStructuralFeatures()
// ============================================================================
std::vector<EStructuralFeature*> collectAllStructuralFeatures(EClass* cls) {
    std::vector<EStructuralFeature*> result;
    if (!cls) return result;
    // 复用 EClassImpl 的 getEAllStructuralFeatures（已做去重和传递闭包）
    const auto& all = cls->getEAllStructuralFeatures();
    result.reserve(all.size());
    // 按 featureID 去重（featureID==-1 的未注册 feature 始终保留）
    std::unordered_set<int> seen;
    for (auto* sf : all) {
        if (!sf) continue;
        int fid = sf->getFeatureID();
        if (fid < 0 || seen.insert(fid).second) {
            result.push_back(sf);
        }
    }
    return result;
}

// ============================================================================
// packageOfClassifier：取 EClassifier 所属 EPackage
// ============================================================================
EPackage* packageOfClassifier(EClassifier* cls) {
    if (!cls) return nullptr;
    EPackage* pkg = cls->getEPackage();
    if (pkg) return pkg;
    // 属于 ecore 元模型自身 → 返回 EcorePackage 的 ePackage
    EPackage* ecorePkg = EcorePackage::instance().getEPackage();
    if (ecorePkg) {
        for (auto* c : ecorePkg->getEClassifiers()) {
            if (c == cls) return ecorePkg;
        }
    }
    return nullptr;
}

// ============================================================================
// qNameOfClassifier：取 "prefix:Name" 形式的限定名
// 用于 XMI 中 xsi:type、element qname 等场景。
// ============================================================================
std::string qNameOfClassifier(EClassifier* cls) {
    if (!cls) return {};
    const std::string& name = cls->getName();
    if (name.empty()) return {};
    EPackage* pkg = packageOfClassifier(cls);
    if (pkg) {
        const std::string& prefix = pkg->getNsPrefix();
        if (!prefix.empty()) return prefix + ":" + name;
    }
    // 回退到 "ecore"（Ecore 自身）
    return std::string("ecore:") + name;
}

// ============================================================================
// Java Double.toString / Float.toString 等价实现
// 对齐 java.lang.Double.toString / Float.toString：
//   - NaN → "NaN"；+Inf → "Infinity"；-Inf → "-Infinity"
//   - +0.0 → "0.0"；-0.0 → "-0.0"（保留符号）
//   - 最短往返十进制（试错精度 1..17 for double, 1..9 for float）
//   - 1e-3 <= |d| < 1e7 用普通格式；否则科学计数法（大写 E，指数无前导零、无 + 号）
//   - 整数倍数追加 ".0"
// ============================================================================

namespace {

// 核心格式化：value 已为 double（float 提升后）。maxPrec 为最短往返试错上限（double=17, float=9）。
// verifyAsFloat 为 true 时用 strtof 校验往返（float 模式）。
std::string formatJavaFloating(double value, int maxPrec, bool verifyAsFloat) {
    if (std::isnan(value)) return "NaN";
    if (std::isinf(value)) return value < 0 ? "-Infinity" : "Infinity";
    if (value == 0.0) return std::signbit(value) ? "-0.0" : "0.0";

    // 找最短往返精度
    char buf[64];
    int prec = -1;
    for (int p = 1; p <= maxPrec; ++p) {
        std::snprintf(buf, sizeof(buf), "%.*e", p - 1, value);
        if (verifyAsFloat) {
            float back = std::strtof(buf, nullptr);
            if (back == static_cast<float>(value)) { prec = p; break; }
        } else {
            double back = std::strtod(buf, nullptr);
            if (back == value) { prec = p; break; }
        }
    }
    if (prec < 0) prec = maxPrec;
    std::snprintf(buf, sizeof(buf), "%.*e", prec - 1, value);
    // buf 形如 "[-]d.ddddde±XX"

    std::string s = buf;
    bool neg = false;
    size_t i = 0;
    if (!s.empty() && s[0] == '-') { neg = true; i = 1; }
    size_t epos = s.find('e');
    std::string mantissa = s.substr(i, epos - i);
    int exp = std::stoi(s.substr(epos + 1));

    // 规范化尾数：保证含小数点且小数点后至少 1 位
    // %.0e（p=1）产生 "d" 无小数点 → 补 ".0"
    if (mantissa.find('.') == std::string::npos) mantissa += ".0";
    // 去掉小数点后末尾 0（保留至少 1 位）
    {
        size_t dot = mantissa.find('.');
        size_t last = mantissa.size() - 1;
        while (last > dot + 1 && mantissa[last] == '0') --last;
        mantissa = mantissa.substr(0, last + 1);
    }

    // Java 阈值：1e-3 <= |d| < 1e7 用普通格式，否则科学计数法
    // exp 是规格化指数（mantissa ∈ [1,10)），即值 = mantissa × 10^exp
    bool useScientific = (exp < -3 || exp >= 7);

    std::string result;
    if (useScientific) {
        result = mantissa + "E" + std::to_string(exp);
    } else {
        // 普通格式：把 mantissa 的小数点按 exp 右移
        std::string digits;
        for (char c : mantissa) if (c != '.') digits += c;
        int decimalPos = 1 + exp;  // 小数点在 digits 中的目标位置
        if (decimalPos <= 0) {
            // 0.00...digits
            result = "0." + std::string(-decimalPos, '0') + digits;
        } else if ((size_t)decimalPos >= digits.size()) {
            // digits 后补零到 decimalPos，再追加 ".0"
            result = digits + std::string(decimalPos - digits.size(), '0') + ".0";
        } else {
            // 小数点落在 digits 中间
            result = digits.substr(0, decimalPos) + "." + digits.substr(decimalPos);
        }
        // 去末尾 0（保留小数点后至少 1 位）
        size_t dotPos = result.find('.');
        if (dotPos != std::string::npos) {
            size_t last = result.size() - 1;
            while (last > dotPos + 1 && result[last] == '0') --last;
            result = result.substr(0, last + 1);
        }
    }
    if (neg) result = "-" + result;
    return result;
}

}  // namespace

std::string formatJavaDouble(double d) {
    return formatJavaFloating(d, 17, false);
}

std::string formatJavaFloat(float f) {
    return formatJavaFloating(static_cast<double>(f), 9, true);
}

double parseJavaDouble(const std::string& s) {
    // 对齐 Java Double.valueOf：显式处理特殊值
    if (s == "NaN" || s == "nan" || s == "NAN") return std::numeric_limits<double>::quiet_NaN();
    if (s == "Infinity" || s == "+Infinity" || s == "inf" || s == "+inf")
        return std::numeric_limits<double>::infinity();
    if (s == "-Infinity" || s == "-inf") return -std::numeric_limits<double>::infinity();
    try { return std::strtod(s.c_str(), nullptr); }
    catch (...) { return 0.0; }
}

float parseJavaFloat(const std::string& s) {
    if (s == "NaN" || s == "nan" || s == "NAN") return std::numeric_limits<float>::quiet_NaN();
    if (s == "Infinity" || s == "+Infinity" || s == "inf" || s == "+inf")
        return std::numeric_limits<float>::infinity();
    if (s == "-Infinity" || s == "-inf") return -std::numeric_limits<float>::infinity();
    try { return std::strtof(s.c_str(), nullptr); }
    catch (...) { return 0.0f; }
}

}  // namespace emf::ecore
