// GenModel.cpp —— GenModel 数据结构的 helper 方法实现
// 对齐 Java: org.eclipse.emf.codegen.ecore.genmodel.*.getXxxName() 等
#include "emf/ecore/codegen/GenModel.h"
#include "emf/ecore/codegen/StringUtils.h"
#include <algorithm>
#include <cctype>
#include <unordered_map>
#include <unordered_set>

namespace emf::ecore::codegen {

// ===== GenModel helpers =====
std::string GenModel::getEffectiveBasePackage() const {
    if (!basePackage.empty()) return basePackage;
    return modelPluginID;  // fallback
}

const GenPackage* GenModel::findGenPackageByNSURI(const std::string& nsURI) const {
    for (const auto& gp : genPackages) {
        if (gp->ecorePackage && gp->ecorePackage->getNsURI() == nsURI) return gp.get();
    }
    return nullptr;
}

// ===== GenPackage helpers =====
// C++ 等价于 Java GenPackage.getPackageInterfaceName(): <Prefix>Package
std::string GenPackage::getPackageInterfaceName() const {
    return prefix + "Package";
}
std::string GenPackage::getFactoryInterfaceName() const {
    return prefix + "Factory";
}
std::string GenPackage::getSwitchInterfaceName() const {
    return prefix + "Switch";
}
std::string GenPackage::getAdapterFactoryClassName() const {
    return prefix + "AdapterFactory";
}
std::string GenPackage::getValidatorClassName() const {
    return prefix + "Validator";
}

// C++ 命名空间路径：basePackage::prefix
std::string GenPackage::getInterfacePackageName() const {
    if (basePackage.empty()) return prefix;
    return basePackage + "::" + prefix;
}
std::string GenPackage::getClassPackageName() const {
    if (basePackage.empty()) return prefix + "::impl";
    return basePackage + "::" + prefix + "::impl";
}
std::string GenPackage::getQualifiedPackageInterfaceName() const {
    if (basePackage.empty()) return prefix + "::" + prefix + "Package";
    return basePackage + "::" + prefix + "::" + prefix + "Package";
}

std::vector<std::shared_ptr<GenClassifier>> GenPackage::getGenClassifiers() const {
    std::vector<std::shared_ptr<GenClassifier>> out;
    for (auto& c : genClasses)   out.push_back(std::static_pointer_cast<GenClassifier>(c));
    for (auto& e : genEnums)     out.push_back(std::static_pointer_cast<GenClassifier>(e));
    for (auto& d : genDataTypes) out.push_back(std::static_pointer_cast<GenClassifier>(d));
    return out;
}

// ===== GenClass helpers =====
std::string GenClass::getClassName() const        { return name; }
std::string GenClass::getInterfaceName() const   { return name; }
std::string GenClass::getImplClassName() const   { return name + "Impl"; }

bool GenClass::isAbstract() const {
    if (!ecoreClass) return false;
    return ecoreClass->isAbstract();
}
bool GenClass::isInterface() const {
    if (!ecoreClass) return false;
    return ecoreClass->isInterface();
}
bool GenClass::isMapEntry() const {
    // 对齐 Java EClassImpl.isMapEntry(): 检查 instanceClassName == "java.util.Map$Entry"
    // C++ 端 EClass 没有 isMapEntry() 方法（无需支持 EMap 反射），这里复刻语义。
    if (!ecoreClass) return false;
    const auto& icn = ecoreClass->getInstanceClassName();
    return icn == "java.util.Map$Entry";
}

std::shared_ptr<GenClass> GenClass::getBaseClass() const {
    if (!ecoreClass) return nullptr;
    for (auto* s : ecoreClass->getESuperTypes()) {
        if (!genPackage) break;
        for (auto& gc : genPackage->genClasses) {
            if (gc->ecoreClass.get() == s) return gc;
        }
    }
    return nullptr;
}

// ===== GenFeature helpers =====
std::string GenFeature::getFeatureName() const { return name; }
std::string GenFeature::getAccessorName() const { return capitalizeFirst(name); }
std::string GenFeature::getGetterName() const { return "get" + capitalizeFirst(name); }
std::string GenFeature::getSetterName() const { return "set" + capitalizeFirst(name); }
std::string GenFeature::getUncapSafeName() const {
    std::string s = name;
    if (!s.empty()) s[0] = static_cast<char>(std::tolower(static_cast<unsigned char>(s[0])));
    return s;
}

std::string GenFeature::getFeatureID() const {
    if (!genClass || !genClass->genPackage) return "0";
    int idx = 0;
    for (auto& otherGc : genClass->genPackage->genClasses) {
        for (auto& gf : otherGc->genFeatures) {
            if (gf.get() == this) return std::to_string(idx);
            ++idx;
        }
    }
    return "0";
}

std::string GenFeature::getQualifiedFeatureAccessor() const {
    if (!genClass || !genClass->genPackage) return "0";
    std::string p = genClass->genPackage->prefix;
    std::string upper;
    for (char c : p) upper += static_cast<char>(std::toupper(static_cast<unsigned char>(c)));
    return upper + "__" + toUpper(name);
}

std::string GenFeature::getCppType() const {
    if (attribute) {
        // 简化：标量类型
        if (type == "EString") return many ? "std::vector<std::string>" : "std::string";
        if (type == "EBoolean") return "bool";
        if (type == "EInt") return "int";
        if (type == "ELong") return "long long";
        if (type == "EDouble") return "double";
        if (type == "EFloat") return "float";
        return type;
    }
    // reference
    std::string tn = type;
    if (tn.empty()) tn = "emf::common::EObject";
    return many ? ("std::vector<" + tn + "*>") : (tn + "*");
}

std::string GenFeature::getCppFieldType() const {
    return getCppType();
}

std::string GenFeature::getCppDefaultValue() const {
    if (attribute) {
        if (defaultValueLiteral.empty()) {
            if (type == "EString") return "std::string()";
            if (type == "EBoolean") return "false";
            return "0";
        }
        return defaultValueLiteral;
    }
    return "nullptr";
}

// ===== GenEnum helpers =====
std::string GenEnum::getEnumName() const { return name; }

// ===== GenDataType helpers =====
std::string GenDataType::getDataTypeName() const { return name; }

}  // namespace emf::ecore::codegen
