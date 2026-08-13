// BasicExtendedMetaData.h
// 对齐 Java org.eclipse.emf.ecore.util.BasicExtendedMetaData
// 实现：用 EAnnotation（source = ANNOTATION_URI）+ EMap<String,String> details 存储元数据
#pragma once

#include "emf/ecore/util/ExtendedMetaData.h"
#include "emf/common/util/BasicEMap.h"

#include <memory>
#include <string>
#include <unordered_map>

namespace emf::ecore::util {

class BasicExtendedMetaData : public ExtendedMetaData {
public:
    BasicExtendedMetaData();
    explicit BasicExtendedMetaData(EPackage* registry);
    ~BasicExtendedMetaData() override = default;

    // ===== Package =====
    std::string getNamespace(EPackage* ePackage) const override;
    bool isQualified(EPackage* ePackage) const override;
    void setQualified(EPackage* ePackage, bool isQualified) override;
    EPackage* getPackage(const std::string& namespace_) const override;
    void putPackage(const std::string& namespace_, EPackage* ePackage) override;
    EClass* getDocumentRoot(EPackage* ePackage) const override;
    void setDocumentRoot(EClass* eClass) override;
    bool isDocumentRoot(EClass* eClass) const override;

    // ===== Classifier =====
    std::string getName(EClassifier* eClassifier) const override;
    void setName(EClassifier* eClassifier, const std::string& name) override;
    std::string getNamespace(EClassifier* eClassifier) const override;
    EClassifier* getType(EPackage* ePackage, const std::string& name) const override;
    EDataType* getBaseType(EDataType* eDataType) const override;
    void setBaseType(EDataType* eDataType, EDataType* baseType) override;

    // ===== StructuralFeature =====
    std::string getName(EStructuralFeature* eStructuralFeature) const override;
    void setName(EStructuralFeature* eStructuralFeature, const std::string& name) override;
    std::string getNamespace(EStructuralFeature* eStructuralFeature) const override;
    std::string basicGetNamespace(EStructuralFeature* eStructuralFeature) const override;
    void setNamespace(EStructuralFeature* eStructuralFeature, const std::string& ns) override;
    FeatureKind getFeatureKind(EStructuralFeature* eStructuralFeature) const override;
    void setFeatureKind(EStructuralFeature* eStructuralFeature, FeatureKind kind) override;
    ContentKind getContentKind(EClass* eClass) const override;
    void setContentKind(EClass* eClass, ContentKind kind) override;
    EDataType* getItemType(EDataType* eDataType) const override;
    void setItemType(EDataType* eDataType, EDataType* itemType) override;
    std::string getMemberTypes(EDataType* eDataType) const override;
    void setMemberTypes(EDataType* eDataType, const std::string& members) override;
    std::string getWildcards(EStructuralFeature* eStructuralFeature) const override;
    void setWildcards(EStructuralFeature* eStructuralFeature, const std::string& wildcards) override;
    ProcessingKind getProcessingKind(EStructuralFeature* eStructuralFeature) const override;
    void setProcessingKind(EStructuralFeature* eStructuralFeature, ProcessingKind kind) override;
    EClass* getAffiliation(EClass* eClass, EStructuralFeature* eGroup) const override;
    void setAffiliation(EClass* eClass, EStructuralFeature* eGroup) override;
    EStructuralFeature* getGroup(EStructuralFeature* eStructuralFeature) const override;
    void setGroup(EStructuralFeature* eStructuralFeature, EStructuralFeature* group) override;

    // ===== Facet =====
    WhiteSpace getWhiteSpaceFacet(EDataType* eDataType) const override;
    void setWhiteSpaceFacet(EDataType* eDataType, WhiteSpace ws) override;
    std::string getEnumerationFacet(EDataType* eDataType) const override;
    void setEnumerationFacet(EDataType* eDataType, const std::string& enum_) override;
    std::string getPatternFacet(EDataType* eDataType) const override;
    void setPatternFacet(EDataType* eDataType, const std::string& pattern) override;
    int getTotalDigitsFacet(EDataType* eDataType) const override;
    void setTotalDigitsFacet(EDataType* eDataType, int n) override;
    int getFractionDigitsFacet(EDataType* eDataType) const override;
    void setFractionDigitsFacet(EDataType* eDataType, int n) override;
    int getLengthFacet(EDataType* eDataType) const override;
    void setLengthFacet(EDataType* eDataType, int n) override;
    int getMinLengthFacet(EDataType* eDataType) const override;
    void setMinLengthFacet(EDataType* eDataType, int n) override;
    int getMaxLengthFacet(EDataType* eDataType) const override;
    void setMaxLengthFacet(EDataType* eDataType, int n) override;
    int getMinExclusiveFacet(EDataType* eDataType) const override;
    void setMinExclusiveFacet(EDataType* eDataType, int n) override;
    int getMaxExclusiveFacet(EDataType* eDataType) const override;
    void setMaxExclusiveFacet(EDataType* eDataType, int n) override;
    int getMinInclusiveFacet(EDataType* eDataType) const override;
    void setMinInclusiveFacet(EDataType* eDataType, int n) override;
    int getMaxInclusiveFacet(EDataType* eDataType) const override;
    void setMaxInclusiveFacet(EDataType* eDataType, int n) override;

    // ===== XML 相关便捷查询 =====
    EReference* getXMLNSPrefixMapFeature(EClass* eClass) const override;
    EReference* getXSISchemaLocationMapFeature(EClass* eClass) const override;

    // ===== 内部：annotation / 缓存 =====
    EAnnotation* getAnnotation(EModelElement* eModelElement, bool create) const override;
    EAnnotation* getAnnotation(emf::common::EObject* eObject, bool create) const override;
    ExtendedMetaData* getExtendedMetaData(EModelElement* eModelElement) const override;
    ExtendedMetaData* getExtendedMetaData(EPackage* ePackage) const override;
    ExtendedMetaData* getExtendedMetaData(EClassifier* eClassifier) const override;
    ExtendedMetaData* getExtendedMetaData(EStructuralFeature* eStructuralFeature) const override;

    // 直接访问 details map（用于 EAnnotation details）
    std::string getDetailValue(EModelElement* elem, const std::string& key, const std::string& def = "") const;
    void setDetailValue(EModelElement* elem, const std::string& key, const std::string& value);
    void removeDetailValue(EModelElement* elem, const std::string& key);

    // 命名空间缓存
    std::string getPackageNamespace(EStructuralFeature* eStructuralFeature) const;

    // Demand-create package registry
    void* getDemandRegistry() const { return demandRegistry_; }

protected:
    // 内部：每个 EPackage 关联一个子 ExtendedMetaData（cache 自己的元数据）
    struct EPackageExtendedMetaData {
        std::unordered_map<std::string, std::string> details;  // 缓存 version/details
        bool qualified = true;
        std::string name;
        std::string namespace_;
    };

    EPackageExtendedMetaData* getOrCreateMeta(EPackage* ePackage) const;

    // 内部: 解析整数字段
    int parseInt(const std::string& s, int default_ = 0) const;
    std::string toString(int v) const;

    // package registry（naive std::unordered_map）
    std::unordered_map<std::string, EPackage*> packageRegistry_;
    // 缓存每个 package 的 ExtendedMetaData
    mutable std::unordered_map<EPackage*, std::shared_ptr<EPackageExtendedMetaData>> packageMeta_;
    void* demandRegistry_ = nullptr;
};

}  // namespace emf::ecore::util
