// ExtendedMetaData.h
// 对齐 Java org.eclipse.emf.ecore.util.ExtendedMetaData
#pragma once

#include "emf/ecore/EcorePackage.h"  // 拉入 EClass/EPackage/EStructuralFeature 等完整定义

#include <string>

namespace emf::ecore::util {

// （类型从 emf/ecore/EcorePackage.h 拉入）

class ExtendedMetaData {
public:
    virtual ~ExtendedMetaData() = default;

    // ===== 常量（来自 Java ExtendedMetaData） =====
    static constexpr const char* ANNOTATION_URI =
        "http:///org/eclipse/emf/ecore/util/ExtendedMetaData";
    static constexpr const char* XMLNS_URI  = "http://www.w3.org/2000/xmlns/";
    static constexpr const char* XML_URI    = "http://www.w3.org/XML/1998/namespace";
    static constexpr const char* XSI_URI    = "http://www.w3.org/2001/XMLSchema-instance";
    static constexpr const char* XML_SCHEMA_URI = "http://www.w3.org/2001/XMLSchema";

    // FeatureKind: 标识结构特征在 XML schema 中的角色
    enum FeatureKind {
        ELEMENT              = 0,
        ATTRIBUTE            = 1,
        ELEMENT_ATTRIBUTE    = 2,
        ELEMENT_UNSPECIFIED  = 3,
        ATTRIBUTE_UNSPECIFIED= 4,
        GROUP_ELEMENT        = 5,
        SIMPLE_EXTENSION     = 6,
        INVALID              = -1
    };

    // ContentKind: XML 复杂类型的内容模型
    enum ContentKind {
        EMPTY          = 0,
        SIMPLE         = 1,
        MIXED          = 2,
        ELEMENT_ONLY   = 3
    };

    // ProcessingKind: wildcard 的处理规则
    enum ProcessingKind {
        UNSPECIFIED_PROCESSING = 0,
        LAX      = 1,
        SKIP     = 2,
        STRICT   = 3
    };

    // WhiteSpace facet
    enum WhiteSpace {
        PRESERVE_WS = 0,
        REPLACE_WS  = 1,
        COLLAPSE_WS = 2
    };

    // ===== Package 操作 =====
    virtual std::string getNamespace(EPackage* ePackage) const = 0;
    virtual bool isQualified(EPackage* ePackage) const = 0;
    virtual void setQualified(EPackage* ePackage, bool isQualified) = 0;
    virtual EPackage* getPackage(const std::string& namespace_) const = 0;
    virtual void putPackage(const std::string& namespace_, EPackage* ePackage) = 0;
    virtual EClass* getDocumentRoot(EPackage* ePackage) const = 0;
    virtual void setDocumentRoot(EClass* eClass) = 0;
    virtual bool isDocumentRoot(EClass* eClass) const = 0;

    // ===== Classifier 操作 =====
    virtual std::string getName(EClassifier* eClassifier) const = 0;
    virtual void setName(EClassifier* eClassifier, const std::string& name) = 0;
    virtual std::string getNamespace(EClassifier* eClassifier) const = 0;
    virtual EClassifier* getType(EPackage* ePackage, const std::string& name) const = 0;
    virtual EDataType* getBaseType(EDataType* eDataType) const = 0;
    virtual void setBaseType(EDataType* eDataType, EDataType* baseType) = 0;

    // ===== StructuralFeature 操作 =====
    virtual std::string getName(EStructuralFeature* eStructuralFeature) const = 0;
    virtual void setName(EStructuralFeature* eStructuralFeature, const std::string& name) = 0;
    virtual std::string getNamespace(EStructuralFeature* eStructuralFeature) const = 0;
    virtual std::string basicGetNamespace(EStructuralFeature* eStructuralFeature) const = 0;
    virtual void setNamespace(EStructuralFeature* eStructuralFeature, const std::string& ns) = 0;
    virtual FeatureKind getFeatureKind(EStructuralFeature* eStructuralFeature) const = 0;
    virtual void setFeatureKind(EStructuralFeature* eStructuralFeature, FeatureKind kind) = 0;
    virtual ContentKind getContentKind(EClass* eClass) const = 0;
    virtual void setContentKind(EClass* eClass, ContentKind kind) = 0;
    virtual EDataType* getItemType(EDataType* eDataType) const = 0;
    virtual void setItemType(EDataType* eDataType, EDataType* itemType) = 0;
    virtual std::string getMemberTypes(EDataType* eDataType) const = 0;
    virtual void setMemberTypes(EDataType* eDataType, const std::string& members) = 0;
    virtual std::string getWildcards(EStructuralFeature* eStructuralFeature) const = 0;
    virtual void setWildcards(EStructuralFeature* eStructuralFeature, const std::string& wildcards) = 0;
    virtual ProcessingKind getProcessingKind(EStructuralFeature* eStructuralFeature) const = 0;
    virtual void setProcessingKind(EStructuralFeature* eStructuralFeature, ProcessingKind kind) = 0;
    virtual EClass* getAffiliation(EClass* eClass, EStructuralFeature* eGroup) const = 0;
    virtual void setAffiliation(EClass* eClass, EStructuralFeature* eGroup) = 0;
    virtual EStructuralFeature* getGroup(EStructuralFeature* eStructuralFeature) const = 0;
    virtual void setGroup(EStructuralFeature* eStructuralFeature, EStructuralFeature* group) = 0;

    // ===== Facet 操作（XSD 简单类型约束） =====
    virtual WhiteSpace getWhiteSpaceFacet(EDataType* eDataType) const = 0;
    virtual void setWhiteSpaceFacet(EDataType* eDataType, WhiteSpace ws) = 0;
    virtual std::string getEnumerationFacet(EDataType* eDataType) const = 0;
    virtual void setEnumerationFacet(EDataType* eDataType, const std::string& enum_) = 0;
    virtual std::string getPatternFacet(EDataType* eDataType) const = 0;
    virtual void setPatternFacet(EDataType* eDataType, const std::string& pattern) = 0;
    virtual int getTotalDigitsFacet(EDataType* eDataType) const = 0;
    virtual void setTotalDigitsFacet(EDataType* eDataType, int n) = 0;
    virtual int getFractionDigitsFacet(EDataType* eDataType) const = 0;
    virtual void setFractionDigitsFacet(EDataType* eDataType, int n) = 0;
    virtual int getLengthFacet(EDataType* eDataType) const = 0;
    virtual void setLengthFacet(EDataType* eDataType, int n) = 0;
    virtual int getMinLengthFacet(EDataType* eDataType) const = 0;
    virtual void setMinLengthFacet(EDataType* eDataType, int n) = 0;
    virtual int getMaxLengthFacet(EDataType* eDataType) const = 0;
    virtual void setMaxLengthFacet(EDataType* eDataType, int n) = 0;
    virtual int getMinExclusiveFacet(EDataType* eDataType) const = 0;
    virtual void setMinExclusiveFacet(EDataType* eDataType, int n) = 0;
    virtual int getMaxExclusiveFacet(EDataType* eDataType) const = 0;
    virtual void setMaxExclusiveFacet(EDataType* eDataType, int n) = 0;
    virtual int getMinInclusiveFacet(EDataType* eDataType) const = 0;
    virtual void setMinInclusiveFacet(EDataType* eDataType, int n) = 0;
    virtual int getMaxInclusiveFacet(EDataType* eDataType) const = 0;
    virtual void setMaxInclusiveFacet(EDataType* eDataType, int n) = 0;

    // ===== XML 相关便捷查询 =====
    virtual EReference* getXMLNSPrefixMapFeature(EClass* eClass) const = 0;
    virtual EReference* getXSISchemaLocationMapFeature(EClass* eClass) const = 0;

    // ===== 内部：annotation 查找（Java 内部行为） =====
    virtual EAnnotation* getAnnotation(EModelElement* eModelElement, bool create) const = 0;
    // 通用 EObject 重载（EPackage 不在 EModelElement 继承链上）
    virtual EAnnotation* getAnnotation(emf::common::EObject* eObject, bool create) const = 0;
    virtual ExtendedMetaData* getExtendedMetaData(EModelElement* eModelElement) const = 0;
    virtual ExtendedMetaData* getExtendedMetaData(EPackage* ePackage) const = 0;
    virtual ExtendedMetaData* getExtendedMetaData(EClassifier* eClassifier) const = 0;
    virtual ExtendedMetaData* getExtendedMetaData(EStructuralFeature* eStructuralFeature) const = 0;
};

}  // namespace emf::ecore::util
