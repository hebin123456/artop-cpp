// BasicExtendedMetaData.cpp
// 对齐 Java org.eclipse.emf.ecore.util.BasicExtendedMetaData
#include "emf/ecore/util/BasicExtendedMetaData.h"
#include "emf/ecore/EcorePackage.h"
#include "emf/ecore/EcoreImpls.h"

#include <cstdlib>
#include <sstream>
#include <stdexcept>

namespace emf::ecore::util {

// 把 emf::ecore 类型引入 util 命名空间（避免每次写 emf::ecore::）
using ::emf::ecore::EAnnotation;
using ::emf::ecore::EAttribute;
using ::emf::ecore::EClass;
using ::emf::ecore::EClassifier;
using ::emf::ecore::EDataType;
using ::emf::ecore::EModelElement;
using ::emf::ecore::EPackage;
using ::emf::ecore::EReference;
using ::emf::ecore::EStructuralFeature;

BasicExtendedMetaData::BasicExtendedMetaData() = default;

BasicExtendedMetaData::BasicExtendedMetaData(EPackage* registry) {
    // 简化：把传入的 package 的子包注册到 packageRegistry_
    if (registry) {
        packageRegistry_[registry->getNsURI()] = registry;
    }
}

// ===== 内部 =====
BasicExtendedMetaData::EPackageExtendedMetaData*
BasicExtendedMetaData::getOrCreateMeta(EPackage* /*ePackage*/) const {
    // 暂未实现 per-package sub-metadata 的延迟创建，命中 packageMeta_。
    return nullptr;
}

EAnnotation* BasicExtendedMetaData::getAnnotation(EModelElement* eModelElement, bool create) const {
    if (!eModelElement) return nullptr;
    auto annotations = eModelElement->getEAnnotations();
    for (EAnnotation* a : annotations) {
        if (a && a->getSource() == ANNOTATION_URI) return a;
    }
    (void)create;
    return nullptr;
}

EAnnotation* BasicExtendedMetaData::getAnnotation(emf::common::EObject* eObject, bool create) const {
    // EPackage/EDataType/etc. 不在 EModelElement 继承链上；
    // 若它实际上是 EModelElement，dynamic_cast 找到对应 annotation。
    if (!eObject) return nullptr;
    if (auto* me = dynamic_cast<EModelElement*>(eObject)) {
        return getAnnotation(me, create);
    }
    (void)create;
    return nullptr;
}

ExtendedMetaData* BasicExtendedMetaData::getExtendedMetaData(EModelElement* eModelElement) const {
    return const_cast<BasicExtendedMetaData*>(this);
}

ExtendedMetaData* BasicExtendedMetaData::getExtendedMetaData(EPackage* ePackage) const {
    return const_cast<BasicExtendedMetaData*>(this);
}

ExtendedMetaData* BasicExtendedMetaData::getExtendedMetaData(EClassifier* eClassifier) const {
    return const_cast<BasicExtendedMetaData*>(this);
}

ExtendedMetaData* BasicExtendedMetaData::getExtendedMetaData(EStructuralFeature* eStructuralFeature) const {
    return const_cast<BasicExtendedMetaData*>(this);
}

std::string BasicExtendedMetaData::getDetailValue(EModelElement* elem, const std::string& key, const std::string& def) const {
    EAnnotation* a = getAnnotation(elem, false);
    if (!a) return def;
    std::string v = a->getDetail(key);
    return v.empty() ? def : v;
}

void BasicExtendedMetaData::setDetailValue(EModelElement* elem, const std::string& key, const std::string& value) {
    EAnnotation* a = getAnnotation(elem, true);
    if (a) a->setDetail(key, value);
}

void BasicExtendedMetaData::removeDetailValue(EModelElement* elem, const std::string& key) {
    EAnnotation* a = getAnnotation(elem, false);
    if (a) {
        // 把空值当作 remove —— setDetail("", "") 即可表示删除（简化语义）
        a->setDetail(key, "");
    }
}

int BasicExtendedMetaData::parseInt(const std::string& s, int default_) const {
    if (s.empty()) return default_;
    try { return std::stoi(s); } catch (...) { return default_; }
}

std::string BasicExtendedMetaData::toString(int v) const {
    return std::to_string(v);
}

std::string BasicExtendedMetaData::getPackageNamespace(EStructuralFeature* eStructuralFeature) const {
    EClass* eContainingClass = eStructuralFeature ? eStructuralFeature->getEContainingClass() : nullptr;
    if (eContainingClass) {
        EPackage* ePackage = eContainingClass->getEPackage();
        if (ePackage) return getNamespace(ePackage);
    }
    return "";
}

// ===== Package =====
std::string BasicExtendedMetaData::getNamespace(EPackage* ePackage) const {
    if (!ePackage) return "";
    if (isQualified(ePackage)) return ePackage->getNsURI();
    return "";
}

bool BasicExtendedMetaData::isQualified(EPackage* ePackage) const {
    EAnnotation* a = getAnnotation(ePackage, false);
    if (!a) return true;  // 默认 qualified
    std::string v = a->getDetail("qualified");
    return v != "false";
}

void BasicExtendedMetaData::setQualified(EPackage* ePackage, bool isQualified) {
    EAnnotation* a = getAnnotation(ePackage, true);
    if (!a) return;
    if (!isQualified) {
        a->setDetail("qualified", "false");
    } else {
        a->setDetail("qualified", "");  // remove
    }
}

EPackage* BasicExtendedMetaData::getPackage(const std::string& namespace_) const {
    auto it = packageRegistry_.find(namespace_);
    return it != packageRegistry_.end() ? it->second : nullptr;
}

void BasicExtendedMetaData::putPackage(const std::string& namespace_, EPackage* ePackage) {
    packageRegistry_[namespace_] = ePackage;
}

EClass* BasicExtendedMetaData::getDocumentRoot(EPackage* ePackage) const {
    return dynamic_cast<EClass*>(getType(ePackage, ""));
}

void BasicExtendedMetaData::setDocumentRoot(EClass* eClass) {
    if (!eClass) return;
    setName(eClass, "");
    setContentKind(eClass, MIXED);
}

bool BasicExtendedMetaData::isDocumentRoot(EClass* eClass) const {
    return getName(eClass) == "";
}

// ===== Classifier =====
std::string BasicExtendedMetaData::getName(EClassifier* eClassifier) const {
    if (!eClassifier) return "";
    EAnnotation* a = getAnnotation(eClassifier, false);
    if (a) {
        std::string n = a->getDetail("name");
        if (!n.empty()) return n;
    }
    return eClassifier->getName();
}

void BasicExtendedMetaData::setName(EClassifier* eClassifier, const std::string& name) {
    if (!eClassifier) return;
    std::string currentName = eClassifier->getName();
    if (currentName == name) {
        // 移除 name detail
        EAnnotation* a = getAnnotation(eClassifier, false);
        if (a) a->setDetail("name", "");
    } else {
        EAnnotation* a = getAnnotation(eClassifier, true);
        if (a) a->setDetail("name", name);
    }
}

std::string BasicExtendedMetaData::getNamespace(EClassifier* eClassifier) const {
    EPackage* pkg = eClassifier ? eClassifier->getEPackage() : nullptr;
    return pkg ? getNamespace(pkg) : std::string{};
}

EClassifier* BasicExtendedMetaData::getType(EPackage* ePackage, const std::string& name) const {
    if (!ePackage) return nullptr;
    for (auto* c : ePackage->getEClassifiers()) {
        if (c && c->getName() == name) return c;
    }
    return nullptr;
}

EDataType* BasicExtendedMetaData::getBaseType(EDataType* eDataType) const {
    if (!eDataType) return nullptr;
    EClassifier* c = getType(eDataType->getEPackage(), getDetailValue(eDataType, "baseType"));
    return dynamic_cast<EDataType*>(c);
}

void BasicExtendedMetaData::setBaseType(EDataType* eDataType, EDataType* baseType) {
    if (!eDataType) return;
    setDetailValue(eDataType, "baseType", baseType ? baseType->getName() : std::string{});
}

// ===== StructuralFeature =====
std::string BasicExtendedMetaData::getName(EStructuralFeature* eStructuralFeature) const {
    if (!eStructuralFeature) return "";
    EAnnotation* a = getAnnotation(eStructuralFeature, false);
    if (a) {
        std::string n = a->getDetail("name");
        if (!n.empty()) return n;
    }
    return eStructuralFeature->getName();
}

void BasicExtendedMetaData::setName(EStructuralFeature* eStructuralFeature, const std::string& name) {
    if (!eStructuralFeature) return;
    EAnnotation* a = getAnnotation(eStructuralFeature, true);
    if (a) a->setDetail("name", name);
}

std::string BasicExtendedMetaData::getNamespace(EStructuralFeature* eStructuralFeature) const {
    return basicGetNamespace(eStructuralFeature);
}

std::string BasicExtendedMetaData::basicGetNamespace(EStructuralFeature* eStructuralFeature) const {
    EAnnotation* a = getAnnotation(eStructuralFeature, false);
    if (!a) return "";
    std::string result = a->getDetail("namespace");
    if (result == "##targetNamespace") {
        return getPackageNamespace(eStructuralFeature);
    }
    return result;
}

void BasicExtendedMetaData::setNamespace(EStructuralFeature* eStructuralFeature, const std::string& ns) {
    if (!eStructuralFeature) return;
    std::string packageNamespace = getPackageNamespace(eStructuralFeature);
    std::string converted = ns;
    if (ns.empty() ? packageNamespace.empty() : (ns == packageNamespace)) {
        converted = "##targetNamespace";
    }
    EAnnotation* a = getAnnotation(eStructuralFeature, true);
    if (a) a->setDetail("namespace", converted);
}

ExtendedMetaData::FeatureKind BasicExtendedMetaData::getFeatureKind(EStructuralFeature* eStructuralFeature) const {
    std::string v = getDetailValue(eStructuralFeature, "kind");
    if (v == "element") return ELEMENT;
    if (v == "attribute") return ATTRIBUTE;
    if (v == "elementAttribute") return ELEMENT_ATTRIBUTE;
    if (v == "elementUnspecified") return ELEMENT_UNSPECIFIED;
    if (v == "attributeUnspecified") return ATTRIBUTE_UNSPECIFIED;
    if (v == "group") return GROUP_ELEMENT;
    if (v == "simpleExtension") return SIMPLE_EXTENSION;
    return ELEMENT;  // 默认
}

void BasicExtendedMetaData::setFeatureKind(EStructuralFeature* eStructuralFeature, FeatureKind kind) {
    static const char* names[] = {
        "element", "attribute", "elementAttribute",
        "elementUnspecified", "attributeUnspecified",
        "group", "simpleExtension"
    };
    int k = kind;
    if (k < 0 || k > 6) return;
    setDetailValue(eStructuralFeature, "kind", names[k]);
}

ExtendedMetaData::ContentKind BasicExtendedMetaData::getContentKind(EClass* eClass) const {
    std::string v = getDetailValue(eClass, "contentKind");
    if (v == "empty") return EMPTY;
    if (v == "simple") return SIMPLE;
    if (v == "mixed") return MIXED;
    if (v == "elementOnly") return ELEMENT_ONLY;
    return EMPTY;  // 默认
}

void BasicExtendedMetaData::setContentKind(EClass* eClass, ContentKind kind) {
    static const char* names[] = {"empty", "simple", "mixed", "elementOnly"};
    int k = kind;
    if (k < 0 || k > 3) return;
    setDetailValue(eClass, "contentKind", names[k]);
}

EDataType* BasicExtendedMetaData::getItemType(EDataType* eDataType) const {
    EClassifier* c = getType(eDataType ? eDataType->getEPackage() : nullptr,
                              getDetailValue(eDataType, "itemType"));
    return dynamic_cast<EDataType*>(c);
}

void BasicExtendedMetaData::setItemType(EDataType* eDataType, EDataType* itemType) {
    setDetailValue(eDataType, "itemType", itemType ? itemType->getName() : std::string{});
}

std::string BasicExtendedMetaData::getMemberTypes(EDataType* eDataType) const {
    return getDetailValue(eDataType, "memberTypes");
}

void BasicExtendedMetaData::setMemberTypes(EDataType* eDataType, const std::string& members) {
    setDetailValue(eDataType, "memberTypes", members);
}

std::string BasicExtendedMetaData::getWildcards(EStructuralFeature* eStructuralFeature) const {
    return getDetailValue(eStructuralFeature, "wildcards");
}

void BasicExtendedMetaData::setWildcards(EStructuralFeature* eStructuralFeature, const std::string& wildcards) {
    setDetailValue(eStructuralFeature, "wildcards", wildcards);
}

ExtendedMetaData::ProcessingKind BasicExtendedMetaData::getProcessingKind(EStructuralFeature* eStructuralFeature) const {
    std::string v = getDetailValue(eStructuralFeature, "processing");
    if (v == "lax") return LAX;
    if (v == "skip") return SKIP;
    if (v == "strict") return STRICT;
    return UNSPECIFIED_PROCESSING;
}

void BasicExtendedMetaData::setProcessingKind(EStructuralFeature* eStructuralFeature, ProcessingKind kind) {
    static const char* names[] = {"unspecified", "lax", "skip", "strict"};
    int k = kind;
    if (k < 0 || k > 3) return;
    setDetailValue(eStructuralFeature, "processing", names[k]);
}

EClass* BasicExtendedMetaData::getAffiliation(EClass* eClass, EStructuralFeature* eGroup) const {
    (void)eClass; (void)eGroup;
    return nullptr;  // 简化：完整实现需遍历 group 引用
}

void BasicExtendedMetaData::setAffiliation(EClass* eClass, EStructuralFeature* eGroup) {
    (void)eClass; (void)eGroup;
}

EStructuralFeature* BasicExtendedMetaData::getGroup(EStructuralFeature* eStructuralFeature) const {
    return nullptr;  // 简化
}

void BasicExtendedMetaData::setGroup(EStructuralFeature* eStructuralFeature, EStructuralFeature* group) {
    (void)eStructuralFeature; (void)group;
}

// ===== Facet =====
ExtendedMetaData::WhiteSpace BasicExtendedMetaData::getWhiteSpaceFacet(EDataType* eDataType) const {
    std::string v = getDetailValue(eDataType, "whiteSpace");
    if (v == "preserve") return PRESERVE_WS;
    if (v == "replace") return REPLACE_WS;
    if (v == "collapse") return COLLAPSE_WS;
    return COLLAPSE_WS;  // 默认
}

void BasicExtendedMetaData::setWhiteSpaceFacet(EDataType* eDataType, WhiteSpace ws) {
    static const char* names[] = {"preserve", "replace", "collapse"};
    int k = ws;
    if (k < 0 || k > 2) return;
    setDetailValue(eDataType, "whiteSpace", names[k]);
}

std::string BasicExtendedMetaData::getEnumerationFacet(EDataType* eDataType) const {
    return getDetailValue(eDataType, "enumeration");
}

void BasicExtendedMetaData::setEnumerationFacet(EDataType* eDataType, const std::string& enum_) {
    setDetailValue(eDataType, "enumeration", enum_);
}

std::string BasicExtendedMetaData::getPatternFacet(EDataType* eDataType) const {
    return getDetailValue(eDataType, "pattern");
}

void BasicExtendedMetaData::setPatternFacet(EDataType* eDataType, const std::string& pattern) {
    setDetailValue(eDataType, "pattern", pattern);
}

int BasicExtendedMetaData::getTotalDigitsFacet(EDataType* eDataType) const {
    return parseInt(getDetailValue(eDataType, "totalDigits"));
}

void BasicExtendedMetaData::setTotalDigitsFacet(EDataType* eDataType, int n) {
    setDetailValue(eDataType, "totalDigits", toString(n));
}

int BasicExtendedMetaData::getFractionDigitsFacet(EDataType* eDataType) const {
    return parseInt(getDetailValue(eDataType, "fractionDigits"));
}

void BasicExtendedMetaData::setFractionDigitsFacet(EDataType* eDataType, int n) {
    setDetailValue(eDataType, "fractionDigits", toString(n));
}

int BasicExtendedMetaData::getLengthFacet(EDataType* eDataType) const {
    return parseInt(getDetailValue(eDataType, "length"));
}

void BasicExtendedMetaData::setLengthFacet(EDataType* eDataType, int n) {
    setDetailValue(eDataType, "length", toString(n));
}

int BasicExtendedMetaData::getMinLengthFacet(EDataType* eDataType) const {
    return parseInt(getDetailValue(eDataType, "minLength"));
}

void BasicExtendedMetaData::setMinLengthFacet(EDataType* eDataType, int n) {
    setDetailValue(eDataType, "minLength", toString(n));
}

int BasicExtendedMetaData::getMaxLengthFacet(EDataType* eDataType) const {
    return parseInt(getDetailValue(eDataType, "maxLength"));
}

void BasicExtendedMetaData::setMaxLengthFacet(EDataType* eDataType, int n) {
    setDetailValue(eDataType, "maxLength", toString(n));
}

int BasicExtendedMetaData::getMinExclusiveFacet(EDataType* eDataType) const {
    return parseInt(getDetailValue(eDataType, "minExclusive"));
}

void BasicExtendedMetaData::setMinExclusiveFacet(EDataType* eDataType, int n) {
    setDetailValue(eDataType, "minExclusive", toString(n));
}

int BasicExtendedMetaData::getMaxExclusiveFacet(EDataType* eDataType) const {
    return parseInt(getDetailValue(eDataType, "maxExclusive"));
}

void BasicExtendedMetaData::setMaxExclusiveFacet(EDataType* eDataType, int n) {
    setDetailValue(eDataType, "maxExclusive", toString(n));
}

int BasicExtendedMetaData::getMinInclusiveFacet(EDataType* eDataType) const {
    return parseInt(getDetailValue(eDataType, "minInclusive"));
}

void BasicExtendedMetaData::setMinInclusiveFacet(EDataType* eDataType, int n) {
    setDetailValue(eDataType, "minInclusive", toString(n));
}

int BasicExtendedMetaData::getMaxInclusiveFacet(EDataType* eDataType) const {
    return parseInt(getDetailValue(eDataType, "maxInclusive"));
}

void BasicExtendedMetaData::setMaxInclusiveFacet(EDataType* eDataType, int n) {
    setDetailValue(eDataType, "maxInclusive", toString(n));
}

// ===== XML 相关便捷查询 =====
EReference* BasicExtendedMetaData::getXMLNSPrefixMapFeature(EClass* eClass) const {
    if (!eClass || getContentKind(eClass) != MIXED) return nullptr;
    for (EReference* ref : eClass->getEAllReferences()) {
        if (getName(ref) == "xmlns:prefix") return ref;
    }
    return nullptr;
}

EReference* BasicExtendedMetaData::getXSISchemaLocationMapFeature(EClass* eClass) const {
    if (!eClass || getContentKind(eClass) != MIXED) return nullptr;
    for (EReference* ref : eClass->getEAllReferences()) {
        if (getName(ref) == "xsi:schemaLocation") return ref;
    }
    return nullptr;
}

}  // namespace emf::ecore::util
