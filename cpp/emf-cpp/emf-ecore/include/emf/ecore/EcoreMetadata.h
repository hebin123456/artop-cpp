// EMF Ecore: 元数据/反射工具
#pragma once

#include "EcorePackage.h"
#include "emf/common/EList.h"
#include <any>
#include <string>

namespace emf::ecore {

// 命名空间间指针转换（emf::common 的前向声明 EClass/EStructuralFeature
// 实际指向 emf::ecore 的实例）
inline EClass* asEClass(emf::common::EObject* obj) {
    return reinterpret_cast<EClass*>(obj);
}
inline const EClass* asEClass(const emf::common::EObject* obj) {
    return reinterpret_cast<const EClass*>(obj);
}
inline EClass* asEClass(emf::ecore::EClass* c) {
    return reinterpret_cast<EClass*>(c);
}
inline const EClass* asEClass(const emf::ecore::EClass* c) {
    return reinterpret_cast<const EClass*>(c);
}
inline EStructuralFeature* asEStructuralFeature(emf::ecore::EStructuralFeature* f) {
    return reinterpret_cast<EStructuralFeature*>(f);
}
inline const EStructuralFeature* asEStructuralFeature(const emf::ecore::EStructuralFeature* f) {
    return reinterpret_cast<const EStructuralFeature*>(f);
}
inline EClassifier* asEClassifier(emf::ecore::EClassifier* c) {
    return reinterpret_cast<EClassifier*>(c);
}
inline const EClassifier* asEClassifier(const emf::ecore::EClassifier* c) {
    return reinterpret_cast<const EClassifier*>(c);
}
inline EDataType* asEDataType(emf::ecore::EDataType* d) {
    return reinterpret_cast<EDataType*>(d);
}
inline const EDataType* asEDataType(const emf::ecore::EDataType* d) {
    return reinterpret_cast<const EDataType*>(d);
}

// DataTypeUtil: 按 EDataType 名称处理基本类型的字符串转换
class DataTypeUtil {
public:
    static std::any fromString(const std::string& dtName, const std::string& lit);
    static std::any defaultValue(const std::string& dtName);
    static std::string toString(const std::string& dtName, const std::any& v);
    static std::string toString(const std::string& dtName, const std::any& v, bool* ok);
    static const std::string& nameOf(EDataType* dt);

    // EClassifier->默认实例值
    static std::any defaultValueForClassifier(EClassifier* cls);
    // 将一个 any 转换为符合特定 EDataType 的 any（用于动态对象赋值）
    static std::any coerce(const std::any& v, const std::string& dtName);
};

// EClassifier::isInstance() 辅助
class InstanceCheck {
public:
    static bool isInstance(EClassifier* cls, emf::common::EObject* obj);
};

// EDataType 工厂（基本类型）
EDataType* createBuiltinDataType(const std::string& name);

// 收集 EClass 自身及所有 super types 的 EStructuralFeature（按 super type 链深度优先去重）。
// Java 行为：EClass.getEAllStructuralFeatures() 包含 inherited features。
// 返回的 vector 可能包含重复的 feature（如果 super type 链上有同名 feature），但每个
// 不同的 feature ID 出现一次（featureID==-1 的未注册 feature 始终保留）。
// 调用者负责使用后释放返回的 vector。
std::vector<EStructuralFeature*> collectAllStructuralFeatures(EClass* cls);

// 取 EClass 对应某 EClassifier 的默认 EPackage：
//   - 如果 cls 属于 ecore meta-model，返回 EcorePackage 的 ePackage
//   - 否则返回 cls->getEPackage()
EPackage* packageOfClassifier(EClassifier* cls);

// 取得 EClassifier 的 qname（"prefix:Name"）。无 prefix 时回退到 "ecore"（Ecore 自身）。
// 用于 XMI 中 xsi:type、element qname 等场景。
std::string qNameOfClassifier(EClassifier* cls);

// Java Double.toString / Float.toString 的 C++ 等价实现。
// 用于 EDouble/EFloat 的 XMI 序列化，保证与 Java EMF 字节级一致。
// 规则：最短往返十进制；NaN→"NaN"；±Inf→"Infinity"/"-Infinity"；
// ±0.0 保留符号；1e-3 <= |d| < 1e7 用普通格式，否则科学计数法（大写 E，指数无前导零/+）；
// 整数倍数追加 ".0"。
std::string formatJavaDouble(double d);
std::string formatJavaFloat(float f);

// Java Double.valueOf / Float.valueOf 的 C++ 等价实现（解析端）。
// 显式处理 "NaN"/"Infinity"/"-Infinity"，其余回退 strtod/strtof。
double parseJavaDouble(const std::string& s);
float parseJavaFloat(const std::string& s);

}  // namespace emf::ecore
