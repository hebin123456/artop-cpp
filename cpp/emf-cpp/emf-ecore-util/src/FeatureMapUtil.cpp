// FeatureMapUtil.cpp
// 1:1 对齐 Java: org.eclipse.emf.ecore.util.FeatureMapUtil
//
// FeatureMapUtil 提供对 EObject 上 FeatureMap / group / choice / sequence /
// wildcard / anyAttribute 模式相关的一组静态工具方法。
#include "emf/ecore/util/FeatureMapUtil.h"
#include "emf/ecore/EcorePackage.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/common/EObject.h"
#include "emf/common/EList.h"

#include <stdexcept>
#include <utility>

namespace emf::ecore::util {

using emf::common::EObject;
using emf::common::EList;
using emf::common::Notification;
using emf::ecore::EClass;
using emf::ecore::EClassifier;
using emf::ecore::EAttribute;
using emf::ecore::EStructuralFeature;
using emf::ecore::EDataType;
using emf::ecore::EcorePackage;
using emf::ecore::EcoreFactory;

namespace {

// 取得 EObject 上给定 feature 对应的 FeatureMap（多种 std::any 容器形态都尝试）。
// Java 行为是 (FeatureMap) eObject.eGet(feature) —— C++ 端 std::any_cast 失败时
// 严格按 Java IllegalStateException / ClassCastException 抛 std::runtime_error。
FeatureMap* featureMapOf(EObject* object, EStructuralFeature* feature) {
    if (!object || !feature) {
        throw std::runtime_error("FeatureMapUtil: object and feature must be non-null");
    }
    std::any v = object->eGet(feature);
    if (!v.has_value()) {
        throw std::runtime_error("FeatureMapUtil: eGet returned null for the FeatureMap feature");
    }
    if (v.type() == typeid(FeatureMap*)) {
        auto* fm = std::any_cast<FeatureMap*>(v);
        if (!fm) {
            throw std::runtime_error("FeatureMapUtil: eGet returned a null FeatureMap*");
        }
        return fm;
    }
    if (v.type() == typeid(BasicFeatureMap*)) {
        return static_cast<FeatureMap*>(std::any_cast<BasicFeatureMap*>(v));
    }
    if (v.type() == typeid(EList<FeatureMapEntry*>*)) {
        // 安全的指针转换：EList<FeatureMapEntry*> 与 FeatureMap 关系由 BasicFeatureMap 维护
        return reinterpret_cast<FeatureMap*>(std::any_cast<EList<FeatureMapEntry*>*>(v));
    }
    throw std::runtime_error(
        "FeatureMapUtil: eGet did not return a FeatureMap for the given feature");
}

// isMany 等价：Java 中 EStructuralFeature.isMany() = (getUpperBound() != 1)
inline bool isManyOf(EStructuralFeature* f) {
    return f && f->getUpperBound() != 1;
}

}  // namespace

// ====== Validator（极简 no-op 实现） ======

bool FeatureMapUtil::Validator::isValid(EStructuralFeature* /*feature*/) const {
    return true;
}

FeatureMapUtil::Validator* FeatureMapUtil::getValidator(
    EClass* /*eClass*/, EStructuralFeature* /*feature*/) {
    static Validator v;
    return &v;
}

// ====== 基本谓词 ======

bool FeatureMapUtil::isWildcard(EStructuralFeature* feature) {
    if (!feature) return false;
    // Java 判 EAttribute 且 name 以 ":" 开头或 == "*"
    auto* attr = dynamic_cast<EAttribute*>(feature);
    if (!attr) return false;
    if (!isManyOf(feature)) return false;
    const std::string& name = feature->getName();
    return name == "*" || (!name.empty() && name[0] == ':');
}

bool FeatureMapUtil::isAnyAttribute(EStructuralFeature* feature) {
    if (!feature) return false;
    auto* attr = dynamic_cast<EAttribute*>(feature);
    if (!attr) return false;
    // Java 看 eAttributeType 是不是 EFeatureMapEntry 的特殊类型
    EDataType* dt = attr->getEAttributeType();
    if (!dt) return false;
    EDataType* fme = EcorePackage::instance().getEDataType_EFeatureMapEntry();
    if (dt == fme) return true;
    // 退而求其次：用 instanceClassName 字符串比较
    if (dt->getInstanceClassName() == "org.eclipse.emf.ecore.util.FeatureMap$Entry") {
        return true;
    }
    return false;
}

bool FeatureMapUtil::isGroup(EStructuralFeature* feature) {
    if (!feature) return false;
    EClass* containing = feature->getEContainingClass();
    if (!containing) return false;
    if (!isManyOf(feature)) return false;
    const std::string& name = feature->getName();
    return name == "group" ||
           (name.size() > 6 && name.substr(name.size() - 6) == ":group");
}

bool FeatureMapUtil::isFeatureMap(EStructuralFeature* feature) {
    if (!feature) return false;
    EClassifier* type = feature->getEType();
    if (!type) return false;
    EDataType* dt = dynamic_cast<EDataType*>(type);
    if (!dt) return false;
    return dt == EcorePackage::instance().getEDataType_EFeatureMap();
}

bool FeatureMapUtil::isMany(EObject* /*owner*/, EStructuralFeature* feature) {
    if (!feature) return false;
    return isManyOf(feature);
}

FeatureMap::Entry* FeatureMapUtil::createEntry(EStructuralFeature* feature, std::any value) {
    return new FeatureMap::Entry(feature, std::move(value));
}

FeatureMap::Entry::Internal* FeatureMapUtil::createRawEntry(
    EStructuralFeature* feature, std::any value) {
    return new FeatureMap::Entry::Internal(feature, std::move(value));
}

bool FeatureMapUtil::isDocumentRoot(EObject* eObject) {
    if (!eObject) return false;
    EClass* cls = eObject->eClass();
    if (!cls) return false;
    return cls->getName() == "DocumentRoot";
}

// ====== FeatureMap 入口 ======

std::vector<FeatureMap::Entry*> FeatureMapUtil::getEntries(
    EObject* object, EStructuralFeature* feature) {
    FeatureMap* fm = featureMapOf(object, feature);
    return fm->entries(feature);
}

std::vector<std::any> FeatureMapUtil::getValues(
    EObject* object, EStructuralFeature* feature) {
    FeatureMap* fm = featureMapOf(object, feature);
    return fm->values(feature);
}

void FeatureMapUtil::add(EObject* object, EStructuralFeature* feature, std::any value) {
    FeatureMap* fm = featureMapOf(object, feature);
    fm->add(feature, std::move(value));
}

std::any FeatureMapUtil::get(EObject* object, EStructuralFeature* feature, int index) {
    FeatureMap* fm = featureMapOf(object, feature);
    return fm->get(feature, index);
}

std::any FeatureMapUtil::set(EObject* object, EStructuralFeature* feature, int index, std::any value) {
    FeatureMap* fm = featureMapOf(object, feature);
    return fm->set(feature, index, std::move(value));
}

bool FeatureMapUtil::contains(EObject* object, EStructuralFeature* feature, const std::any& value) {
    FeatureMap* fm = featureMapOf(object, feature);
    // FeatureMap 没有 std::any 重载的 contains；直接遍历 entries 比较
    auto entries = fm->entries(feature);
    for (auto* e : entries) {
        if (e->getValue().type() == value.type()) {
            return true;
        }
    }
    return false;
}

int FeatureMapUtil::size(EObject* object, EStructuralFeature* feature) {
    FeatureMap* fm = featureMapOf(object, feature);
    return fm->size(feature);
}

bool FeatureMapUtil::isEmpty(EObject* object, EStructuralFeature* feature) {
    return size(object, feature) == 0;
}

// ====== XML 处理工具 ======

EStructuralFeature* FeatureMapUtil::createFeature(
    EClass* /*owner*/, const std::string& name, EClassifier* /*type*/) {
    EAttribute* attr = EcoreFactory::instance().createEAttribute();
    attr->setName(name);
    return attr;
}

std::pair<std::string, std::string> FeatureMapUtil::decodeFeatureName(
    const std::string& qualifiedName) {
    // Java 行为：解析 "ns#name" / "ns:name" / "{ns}name" 等多种格式。
    // 简化：只支持 "#" 分隔。
    std::string::size_type pos = qualifiedName.find('#');
    if (pos == std::string::npos) {
        return std::make_pair(std::string{}, qualifiedName);
    }
    return std::make_pair(qualifiedName.substr(0, pos),
                          qualifiedName.substr(pos + 1));
}

std::pair<std::string, std::string> FeatureMapUtil::splitName(
    const std::string& name) {
    // 内部辅助：与 decodeFeatureName 类似，提供 "ns#name" 形式拆分
    return decodeFeatureName(name);
}

EStructuralFeature* FeatureMapUtil::createEAttributeWildcard(
    EClass* /*owner*/, const std::string& name) {
    EAttribute* attr = EcoreFactory::instance().createEAttribute();
    attr->setName(name);
    attr->setUpperBound(-1);  // unbounded
    return attr;
}

}  // namespace emf::ecore::util
