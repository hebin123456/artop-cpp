// EMF XSD: XSDPackage 元模型声明
// 对齐 Java: org.eclipse.xsd.XSDPackage
#pragma once

#include "emf/ecore/EcorePackage.h"
#include <string>
#include <vector>

namespace emf::xsd {

// 前向声明
class XSDFactory;

// ===== XSDPackage 单例 =====
class XSDPackage {
public:
    // 初始化（首次访问时自动调用）
    static void initialize();

    // 单例：首次访问时自动 init
    static XSDPackage& instance();

    // 命名空间常量
    static constexpr const char* eNAME = "xsd";
    static constexpr const char* eNS_URI = "http://www.w3.org/2001/XMLSchema";
    static constexpr const char* eNS_PREFIX = "xsd";

    // 获取元模型
    emf::ecore::EPackage* getEPackage() const { return xsdPackage_; }
    emf::ecore::EFactory* getEFactory() const;

    // EClass 访问
    emf::ecore::EClass* getEClass_XSDSchema() const { return xsdSchema_; }
    emf::ecore::EClass* getEClass_XSDComplexTypeDefinition() const { return xsdComplexTypeDefinition_; }
    emf::ecore::EClass* getEClass_XSDSimpleTypeDefinition() const { return xsdSimpleTypeDefinition_; }
    emf::ecore::EClass* getEClass_XSDElementDeclaration() const { return xsdElementDeclaration_; }
    emf::ecore::EClass* getEClass_XSDAttributeDeclaration() const { return xsdAttributeDeclaration_; }
    emf::ecore::EClass* getEClass_XSDAttributeGroupDefinition() const { return xsdAttributeGroupDefinition_; }
    emf::ecore::EClass* getEClass_XSDModelGroup() const { return xsdModelGroup_; }
    emf::ecore::EClass* getEClass_XSDWildcard() const { return xsdWildcard_; }
    emf::ecore::EClass* getEClass_XSDTypeDefinition() const { return xsdTypeDefinition_; }
    emf::ecore::EClass* getEClass_XSDParticle() const { return xsdParticle_; }
    emf::ecore::EClass* getEClass_XSDAnnotation() const { return xsdAnnotation_; }
    emf::ecore::EClass* getEClass_XSDImport() const { return xsdImport_; }
    emf::ecore::EClass* getEClass_XSDInclude() const { return xsdInclude_; }
    // ===== Facet EClass (19 个) =====
    emf::ecore::EClass* getEClass_XSDFacet() const { return xsdFacet_; }
    emf::ecore::EClass* getEClass_XSDConstrainingFacet() const { return xsdConstrainingFacet_; }
    emf::ecore::EClass* getEClass_XSDFixedFacet() const { return xsdFixedFacet_; }
    emf::ecore::EClass* getEClass_XSDRepeatableFacet() const { return xsdRepeatableFacet_; }
    emf::ecore::EClass* getEClass_XSDFundamentalFacet() const { return xsdFundamentalFacet_; }
    emf::ecore::EClass* getEClass_XSDOrderedFacet() const { return xsdOrderedFacet_; }
    emf::ecore::EClass* getEClass_XSDBoundedFacet() const { return xsdBoundedFacet_; }
    emf::ecore::EClass* getEClass_XSDCardinalityFacet() const { return xsdCardinalityFacet_; }
    emf::ecore::EClass* getEClass_XSDNumericFacet() const { return xsdNumericFacet_; }
    emf::ecore::EClass* getEClass_XSDLengthFacet() const { return xsdLengthFacet_; }
    emf::ecore::EClass* getEClass_XSDMinLengthFacet() const { return xsdMinLengthFacet_; }
    emf::ecore::EClass* getEClass_XSDMaxLengthFacet() const { return xsdMaxLengthFacet_; }
    emf::ecore::EClass* getEClass_XSDPatternFacet() const { return xsdPatternFacet_; }
    emf::ecore::EClass* getEClass_XSDEnumerationFacet() const { return xsdEnumerationFacet_; }
    emf::ecore::EClass* getEClass_XSDWhiteSpaceFacet() const { return xsdWhiteSpaceFacet_; }
    emf::ecore::EClass* getEClass_XSDTotalDigitsFacet() const { return xsdTotalDigitsFacet_; }
    emf::ecore::EClass* getEClass_XSDFractionDigitsFacet() const { return xsdFractionDigitsFacet_; }
    emf::ecore::EClass* getEClass_XSDMinFacet() const { return xsdMinFacet_; }
    emf::ecore::EClass* getEClass_XSDMaxFacet() const { return xsdMaxFacet_; }
    emf::ecore::EClass* getEClass_XSDMinInclusiveFacet() const { return xsdMinInclusiveFacet_; }
    emf::ecore::EClass* getEClass_XSDMaxInclusiveFacet() const { return xsdMaxInclusiveFacet_; }
    emf::ecore::EClass* getEClass_XSDMinExclusiveFacet() const { return xsdMinExclusiveFacet_; }
    emf::ecore::EClass* getEClass_XSDMaxExclusiveFacet() const { return xsdMaxExclusiveFacet_; }

    // 获取所有 XSD EClass
    const std::vector<emf::ecore::EClass*>& getClassifiers() const { return classifiers_; }

private:
    XSDPackage() = default;
    void init();

    emf::ecore::EPackage* xsdPackage_ = nullptr;
    emf::ecore::EClass* xsdSchema_ = nullptr;
    emf::ecore::EClass* xsdComplexTypeDefinition_ = nullptr;
    emf::ecore::EClass* xsdSimpleTypeDefinition_ = nullptr;
    emf::ecore::EClass* xsdElementDeclaration_ = nullptr;
    emf::ecore::EClass* xsdAttributeDeclaration_ = nullptr;
    emf::ecore::EClass* xsdAttributeGroupDefinition_ = nullptr;
    emf::ecore::EClass* xsdModelGroup_ = nullptr;
    emf::ecore::EClass* xsdWildcard_ = nullptr;
    emf::ecore::EClass* xsdTypeDefinition_ = nullptr;
    emf::ecore::EClass* xsdParticle_ = nullptr;
    emf::ecore::EClass* xsdAnnotation_ = nullptr;
    emf::ecore::EClass* xsdImport_ = nullptr;
    emf::ecore::EClass* xsdInclude_ = nullptr;
    // ===== Facet EClass 字段（19 个）=====
    emf::ecore::EClass* xsdFacet_ = nullptr;
    emf::ecore::EClass* xsdConstrainingFacet_ = nullptr;
    emf::ecore::EClass* xsdFixedFacet_ = nullptr;
    emf::ecore::EClass* xsdRepeatableFacet_ = nullptr;
    emf::ecore::EClass* xsdFundamentalFacet_ = nullptr;
    emf::ecore::EClass* xsdOrderedFacet_ = nullptr;
    emf::ecore::EClass* xsdBoundedFacet_ = nullptr;
    emf::ecore::EClass* xsdCardinalityFacet_ = nullptr;
    emf::ecore::EClass* xsdNumericFacet_ = nullptr;
    emf::ecore::EClass* xsdLengthFacet_ = nullptr;
    emf::ecore::EClass* xsdMinLengthFacet_ = nullptr;
    emf::ecore::EClass* xsdMaxLengthFacet_ = nullptr;
    emf::ecore::EClass* xsdPatternFacet_ = nullptr;
    emf::ecore::EClass* xsdEnumerationFacet_ = nullptr;
    emf::ecore::EClass* xsdWhiteSpaceFacet_ = nullptr;
    emf::ecore::EClass* xsdTotalDigitsFacet_ = nullptr;
    emf::ecore::EClass* xsdFractionDigitsFacet_ = nullptr;
    emf::ecore::EClass* xsdMinFacet_ = nullptr;
    emf::ecore::EClass* xsdMaxFacet_ = nullptr;
    emf::ecore::EClass* xsdMinInclusiveFacet_ = nullptr;
    emf::ecore::EClass* xsdMaxInclusiveFacet_ = nullptr;
    emf::ecore::EClass* xsdMinExclusiveFacet_ = nullptr;
    emf::ecore::EClass* xsdMaxExclusiveFacet_ = nullptr;
    std::vector<emf::ecore::EClass*> classifiers_;
};

}  // namespace emf::xsd
