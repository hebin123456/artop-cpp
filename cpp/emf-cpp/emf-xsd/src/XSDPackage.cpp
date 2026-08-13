// EMF XSD: XSDPackage 元模型初始化
// 对齐 Java: org.eclipse.xsd.util.XSDUtil / XSDPackage
#include "emf/xsd/XSDPackage.h"
#include "emf/xsd/XSDFactory.h"
#include "emf/ecore/EcorePackage.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/ecore/EcoreMetadata.h"
#include "emf/common/EPackageRegistry.h"

#include <string>

namespace emf::xsd {

// ===== XSDPackage =====

XSDPackage& XSDPackage::instance() {
    static XSDPackage* p = []() {
        static XSDPackage inst;
        inst.init();
        return &inst;
    }();
    return *p;
}

void XSDPackage::initialize() {
    instance();
}

emf::ecore::EFactory* XSDPackage::getEFactory() const {
    if (!xsdPackage_) return nullptr;
    return xsdPackage_->getEFactoryInstance();
}

namespace {

// 辅助：构造 EAttribute，设定 name / featureID / type
emf::ecore::EAttribute* makeAttribute(emf::ecore::EClass* owner, emf::ecore::EDataType* type,
                                       const std::string& name, int featureID) {
    auto& efac = emf::ecore::EcoreFactory::instance();
    auto* a = efac.createEAttribute();
    a->setName(name);
    a->setEAttributeType(type);
    a->setLowerBound(0);
    a->setUpperBound(1);
    a->setFeatureID(featureID);
    a->setEContainingClass(owner);
    return a;
}

// 辅助：构造 EReference
emf::ecore::EReference* makeReference(emf::ecore::EClass* owner, emf::ecore::EClass* type,
                                       const std::string& name, bool containment,
                                       int lowerBound, int upperBound, int featureID) {
    auto& efac = emf::ecore::EcoreFactory::instance();
    auto* r = efac.createEReference();
    r->setName(name);
    r->setEReferenceType(type);
    r->setContainment(containment);
    r->setLowerBound(lowerBound);
    r->setUpperBound(upperBound);
    r->setFeatureID(featureID);
    r->setEContainingClass(owner);
    return r;
}

}  // namespace

void XSDPackage::init() {
    // 确保 EcorePackage 已初始化
    emf::ecore::EcorePackage::instance();

    auto& efac = emf::ecore::EcoreFactory::instance();
    auto& ecorePkg = *emf::ecore::EcorePackage::instance().getEPackage();
    auto* eString = emf::ecore::EcorePackage::instance().getEDataType_EString();
    auto* eBoolean = emf::ecore::EcorePackage::instance().getEDataType_EBoolean();

    // 1. 创建 XSD EPackage
    xsdPackage_ = efac.createEPackage();
    xsdPackage_->setName(eNAME);
    xsdPackage_->setNsURI(eNS_URI);
    xsdPackage_->setNsPrefix(eNS_PREFIX);

    // 2. 创建所有 XSD EClass（先全部创建，再添加 features，避免前向引用问题）
    xsdSchema_                   = efac.createEClass(); xsdSchema_->setName("XSDSchema");
    xsdComplexTypeDefinition_    = efac.createEClass(); xsdComplexTypeDefinition_->setName("XSDComplexTypeDefinition");
    xsdSimpleTypeDefinition_     = efac.createEClass(); xsdSimpleTypeDefinition_->setName("XSDSimpleTypeDefinition");
    xsdElementDeclaration_       = efac.createEClass(); xsdElementDeclaration_->setName("XSDElementDeclaration");
    xsdAttributeDeclaration_     = efac.createEClass(); xsdAttributeDeclaration_->setName("XSDAttributeDeclaration");
    xsdAttributeGroupDefinition_ = efac.createEClass(); xsdAttributeGroupDefinition_->setName("XSDAttributeGroupDefinition");
    xsdModelGroup_               = efac.createEClass(); xsdModelGroup_->setName("XSDModelGroup");
    xsdWildcard_                 = efac.createEClass(); xsdWildcard_->setName("XSDWildcard");
    xsdTypeDefinition_           = efac.createEClass(); xsdTypeDefinition_->setName("XSDTypeDefinition");
    xsdParticle_                 = efac.createEClass(); xsdParticle_->setName("XSDParticle");
    xsdAnnotation_               = efac.createEClass(); xsdAnnotation_->setName("XSDAnnotation");
    xsdImport_                   = efac.createEClass(); xsdImport_->setName("XSDImport");
    xsdInclude_                  = efac.createEClass(); xsdInclude_->setName("XSDInclude");

    // ===== Facet EClass（19+ 个，abstract 用 setAbstract）=====
    xsdFacet_                     = efac.createEClass(); xsdFacet_->setName("XSDFacet");
    xsdConstrainingFacet_         = efac.createEClass(); xsdConstrainingFacet_->setName("XSDConstrainingFacet");
    xsdFixedFacet_                = efac.createEClass(); xsdFixedFacet_->setName("XSDFixedFacet");
    xsdRepeatableFacet_           = efac.createEClass(); xsdRepeatableFacet_->setName("XSDRepeatableFacet");
    xsdFundamentalFacet_          = efac.createEClass(); xsdFundamentalFacet_->setName("XSDFundamentalFacet");
    xsdOrderedFacet_              = efac.createEClass(); xsdOrderedFacet_->setName("XSDOrderedFacet");
    xsdBoundedFacet_              = efac.createEClass(); xsdBoundedFacet_->setName("XSDBoundedFacet");
    xsdCardinalityFacet_          = efac.createEClass(); xsdCardinalityFacet_->setName("XSDCardinalityFacet");
    xsdNumericFacet_              = efac.createEClass(); xsdNumericFacet_->setName("XSDNumericFacet");
    xsdLengthFacet_               = efac.createEClass(); xsdLengthFacet_->setName("XSDLengthFacet");
    xsdMinLengthFacet_            = efac.createEClass(); xsdMinLengthFacet_->setName("XSDMinLengthFacet");
    xsdMaxLengthFacet_            = efac.createEClass(); xsdMaxLengthFacet_->setName("XSDMaxLengthFacet");
    xsdPatternFacet_              = efac.createEClass(); xsdPatternFacet_->setName("XSDPatternFacet");
    xsdEnumerationFacet_          = efac.createEClass(); xsdEnumerationFacet_->setName("XSDEnumerationFacet");
    xsdWhiteSpaceFacet_           = efac.createEClass(); xsdWhiteSpaceFacet_->setName("XSDWhiteSpaceFacet");
    xsdTotalDigitsFacet_          = efac.createEClass(); xsdTotalDigitsFacet_->setName("XSDTotalDigitsFacet");
    xsdFractionDigitsFacet_       = efac.createEClass(); xsdFractionDigitsFacet_->setName("XSDFractionDigitsFacet");
    xsdMinFacet_                  = efac.createEClass(); xsdMinFacet_->setName("XSDMinFacet");
    xsdMaxFacet_                  = efac.createEClass(); xsdMaxFacet_->setName("XSDMaxFacet");
    xsdMinInclusiveFacet_         = efac.createEClass(); xsdMinInclusiveFacet_->setName("XSDMinInclusiveFacet");
    xsdMaxInclusiveFacet_         = efac.createEClass(); xsdMaxInclusiveFacet_->setName("XSDMaxInclusiveFacet");
    xsdMinExclusiveFacet_         = efac.createEClass(); xsdMinExclusiveFacet_->setName("XSDMinExclusiveFacet");
    xsdMaxExclusiveFacet_         = efac.createEClass(); xsdMaxExclusiveFacet_->setName("XSDMaxExclusiveFacet");

    // abstract 标记
    xsdFacet_->setAbstract(true);
    xsdConstrainingFacet_->setAbstract(true);
    xsdFixedFacet_->setAbstract(true);
    xsdRepeatableFacet_->setAbstract(true);
    xsdFundamentalFacet_->setAbstract(true);
    xsdMinFacet_->setAbstract(true);
    xsdMaxFacet_->setAbstract(true);

    // 3. 为每个 XSD EClass 添加基本 features

    // 3.1 XSDSchema
    {
        // targetNamespace: EString
        auto* a = makeAttribute(xsdSchema_, eString, "targetNamespace", 0);
        xsdSchema_->addEStructuralFeature(a);

        // elementDeclarations: reference to XSDElementDeclaration, containment, many
        auto* r = makeReference(xsdSchema_, xsdElementDeclaration_, "elementDeclarations",
                                /*containment=*/true, 0, -1, 1);
        xsdSchema_->addEStructuralFeature(r);

        // typeDefinitions: reference to XSDTypeDefinition, containment, many
        r = makeReference(xsdSchema_, xsdTypeDefinition_, "typeDefinitions",
                          /*containment=*/true, 0, -1, 2);
        xsdSchema_->addEStructuralFeature(r);

        // attributeDeclarations: many
        r = makeReference(xsdSchema_, xsdAttributeDeclaration_, "attributeDeclarations",
                          /*containment=*/true, 0, -1, 3);
        xsdSchema_->addEStructuralFeature(r);

        // attributeGroupDefinitions: many
        r = makeReference(xsdSchema_, xsdAttributeGroupDefinition_, "attributeGroupDefinitions",
                          /*containment=*/true, 0, -1, 4);
        xsdSchema_->addEStructuralFeature(r);

        // modelGroupDefinitions: many
        r = makeReference(xsdSchema_, xsdModelGroup_, "modelGroupDefinitions",
                          /*containment=*/true, 0, -1, 5);
        xsdSchema_->addEStructuralFeature(r);

        // imports: many
        r = makeReference(xsdSchema_, xsdImport_, "imports",
                          /*containment=*/true, 0, -1, 6);
        xsdSchema_->addEStructuralFeature(r);

        // includes: many
        r = makeReference(xsdSchema_, xsdInclude_, "includes",
                          /*containment=*/true, 0, -1, 7);
        xsdSchema_->addEStructuralFeature(r);

        // annotations: many
        r = makeReference(xsdSchema_, xsdAnnotation_, "annotations",
                          /*containment=*/true, 0, -1, 8);
        xsdSchema_->addEStructuralFeature(r);

        // qNamePrefixToNamespaceMap: EString -> EString（用 EString 简化表示）
        a = makeAttribute(xsdSchema_, eString, "qNamePrefixToNamespaceMap", 9);
        xsdSchema_->addEStructuralFeature(a);
    }

    // 3.2 XSDTypeDefinition (abstract base)
    {
        // name: EString
        auto* a = makeAttribute(xsdTypeDefinition_, eString, "name", 0);
        xsdTypeDefinition_->addEStructuralFeature(a);

        // baseTypeDefinition: reference to XSDTypeDefinition
        auto* r = makeReference(xsdTypeDefinition_, xsdTypeDefinition_, "baseTypeDefinition",
                                false, 0, 1, 1);
        xsdTypeDefinition_->addEStructuralFeature(r);

        // abstract
        a = makeAttribute(xsdTypeDefinition_, eBoolean, "abstract", 2);
        xsdTypeDefinition_->addEStructuralFeature(a);

        // lexicalValue
        a = makeAttribute(xsdTypeDefinition_, eString, "lexicalValue", 3);
        xsdTypeDefinition_->addEStructuralFeature(a);

        // qName
        a = makeAttribute(xsdTypeDefinition_, eString, "qName", 4);
        xsdTypeDefinition_->addEStructuralFeature(a);

        xsdTypeDefinition_->setAbstract(true);
    }

    // 3.3 XSDComplexTypeDefinition
    {
        // name: EString
        auto* a = makeAttribute(xsdComplexTypeDefinition_, eString, "name", 0);
        xsdComplexTypeDefinition_->addEStructuralFeature(a);

        // content: XSDModelGroup
        auto* r = makeReference(xsdComplexTypeDefinition_, xsdModelGroup_, "content",
                                true, 0, 1, 1);
        xsdComplexTypeDefinition_->addEStructuralFeature(r);

        // attributeDeclarations: many XSDAttributeDeclaration
        r = makeReference(xsdComplexTypeDefinition_, xsdAttributeDeclaration_, "attributeDeclarations",
                          true, 0, -1, 2);
        xsdComplexTypeDefinition_->addEStructuralFeature(r);

        // attributeUses
        r = makeReference(xsdComplexTypeDefinition_, xsdAttributeDeclaration_, "attributeUses",
                          true, 0, -1, 3);
        xsdComplexTypeDefinition_->addEStructuralFeature(r);

        // attributeWildcard
        r = makeReference(xsdComplexTypeDefinition_, xsdWildcard_, "attributeWildcard",
                          true, 0, 1, 4);
        xsdComplexTypeDefinition_->addEStructuralFeature(r);

        // particle
        r = makeReference(xsdComplexTypeDefinition_, xsdModelGroup_, "particle",
                          false, 0, 1, 5);
        xsdComplexTypeDefinition_->addEStructuralFeature(r);

        // abstract
        a = makeAttribute(xsdComplexTypeDefinition_, eBoolean, "abstract", 6);
        xsdComplexTypeDefinition_->addEStructuralFeature(a);

        // mixed
        a = makeAttribute(xsdComplexTypeDefinition_, eBoolean, "mixed", 7);
        xsdComplexTypeDefinition_->addEStructuralFeature(a);

        // contentType
        a = makeAttribute(xsdComplexTypeDefinition_, eString, "contentType", 8);
        xsdComplexTypeDefinition_->addEStructuralFeature(a);
    }

    // 3.4 XSDSimpleTypeDefinition
    {
        auto* a = makeAttribute(xsdSimpleTypeDefinition_, eString, "name", 0);
        xsdSimpleTypeDefinition_->addEStructuralFeature(a);

        // baseTypeDefinition
        auto* r = makeReference(xsdSimpleTypeDefinition_, xsdTypeDefinition_, "baseTypeDefinition",
                                false, 0, 1, 1);
        xsdSimpleTypeDefinition_->addEStructuralFeature(r);

        // lexicalPattern
        a = makeAttribute(xsdSimpleTypeDefinition_, eString, "lexicalPattern", 2);
        xsdSimpleTypeDefinition_->addEStructuralFeature(a);

        // variety
        a = makeAttribute(xsdSimpleTypeDefinition_, eString, "variety", 3);
        xsdSimpleTypeDefinition_->addEStructuralFeature(a);

        // memberTypeDefinitions
        r = makeReference(xsdSimpleTypeDefinition_, xsdTypeDefinition_, "memberTypeDefinitions",
                          true, 0, -1, 4);
        xsdSimpleTypeDefinition_->addEStructuralFeature(r);

        // itemTypeDefinition
        r = makeReference(xsdSimpleTypeDefinition_, xsdTypeDefinition_, "itemTypeDefinition",
                          true, 0, 1, 5);
        xsdSimpleTypeDefinition_->addEStructuralFeature(r);

        // primitiveTypeDefinition
        r = makeReference(xsdSimpleTypeDefinition_, xsdTypeDefinition_, "primitiveTypeDefinition",
                          true, 0, 1, 6);
        xsdSimpleTypeDefinition_->addEStructuralFeature(r);
    }

    // 3.5 XSDElementDeclaration
    {
        auto* a = makeAttribute(xsdElementDeclaration_, eString, "name", 0);
        xsdElementDeclaration_->addEStructuralFeature(a);

        // typeDefinition
        auto* r = makeReference(xsdElementDeclaration_, xsdTypeDefinition_, "typeDefinition",
                                false, 0, 1, 1);
        xsdElementDeclaration_->addEStructuralFeature(r);

        // substitutionGroupAffiliation
        r = makeReference(xsdElementDeclaration_, xsdElementDeclaration_, "substitutionGroupAffiliation",
                          false, 0, 1, 2);
        xsdElementDeclaration_->addEStructuralFeature(r);

        // targetNamespace
        a = makeAttribute(xsdElementDeclaration_, eString, "targetNamespace", 3);
        xsdElementDeclaration_->addEStructuralFeature(a);

        // qName
        a = makeAttribute(xsdElementDeclaration_, eString, "qName", 4);
        xsdElementDeclaration_->addEStructuralFeature(a);

        // scope
        a = makeAttribute(xsdElementDeclaration_, eString, "scope", 5);
        xsdElementDeclaration_->addEStructuralFeature(a);
    }

    // 3.6 XSDAttributeDeclaration
    {
        auto* a = makeAttribute(xsdAttributeDeclaration_, eString, "name", 0);
        xsdAttributeDeclaration_->addEStructuralFeature(a);

        // typeDefinition
        auto* r = makeReference(xsdAttributeDeclaration_, xsdTypeDefinition_, "typeDefinition",
                                false, 0, 1, 1);
        xsdAttributeDeclaration_->addEStructuralFeature(r);

        // scope
        a = makeAttribute(xsdAttributeDeclaration_, eString, "scope", 2);
        xsdAttributeDeclaration_->addEStructuralFeature(a);
    }

    // 3.7 XSDAttributeGroupDefinition
    {
        auto* a = makeAttribute(xsdAttributeGroupDefinition_, eString, "name", 0);
        xsdAttributeGroupDefinition_->addEStructuralFeature(a);

        // attributeUses: many XSDAttributeDeclaration
        auto* r = makeReference(xsdAttributeGroupDefinition_, xsdAttributeDeclaration_, "attributeUses",
                                true, 0, -1, 1);
        xsdAttributeGroupDefinition_->addEStructuralFeature(r);

        // attributeWildcard
        r = makeReference(xsdAttributeGroupDefinition_, xsdWildcard_, "attributeWildcard",
                          true, 0, 1, 2);
        xsdAttributeGroupDefinition_->addEStructuralFeature(r);
    }

    // 3.8 XSDModelGroup
    {
        // compositor: EString ("all"|"sequence"|"choice")
        auto* a = makeAttribute(xsdModelGroup_, eString, "compositor", 0);
        xsdModelGroup_->addEStructuralFeature(a);

        // particles: many XSDParticle
        auto* r = makeReference(xsdModelGroup_, xsdParticle_, "particles",
                                true, 0, -1, 1);
        xsdModelGroup_->addEStructuralFeature(r);
    }

    // 3.9 XSDWildcard
    {
        auto* a = makeAttribute(xsdWildcard_, eString, "namespaceConstraint", 0);
        xsdWildcard_->addEStructuralFeature(a);

        // processContents
        a = makeAttribute(xsdWildcard_, eString, "processContents", 1);
        xsdWildcard_->addEStructuralFeature(a);
    }

    // 3.10 XSDParticle
    {
        // minOccurs
        auto* a = makeAttribute(xsdParticle_, eString, "minOccurs", 0);
        xsdParticle_->addEStructuralFeature(a);

        // maxOccurs
        a = makeAttribute(xsdParticle_, eString, "maxOccurs", 1);
        xsdParticle_->addEStructuralFeature(a);

        // content: XSDModelGroup | XSDElementDeclaration | XSDWildcard (用 EObject 简化)
        auto* r = makeReference(xsdParticle_, nullptr, "content",
                                true, 0, 1, 2);
        xsdParticle_->addEStructuralFeature(r);
    }

    // 3.11 XSDAnnotation
    {
        auto* a = makeAttribute(xsdAnnotation_, eString, "userInformation", 0);
        xsdAnnotation_->addEStructuralFeature(a);

        // applicationInformation
        auto* r = makeReference(xsdAnnotation_, xsdAnnotation_, "applicationInformation",
                                true, 0, -1, 1);
        xsdAnnotation_->addEStructuralFeature(r);
    }

    // 3.12 XSDImport
    {
        // namespaceLocation
        auto* a = makeAttribute(xsdImport_, eString, "namespace", 0);
        xsdImport_->addEStructuralFeature(a);

        // schemaLocation
        a = makeAttribute(xsdImport_, eString, "schemaLocation", 1);
        xsdImport_->addEStructuralFeature(a);

        // resolvedSchema
        auto* r = makeReference(xsdImport_, xsdSchema_, "resolvedSchema",
                                false, 0, 1, 2);
        xsdImport_->addEStructuralFeature(r);
    }

    // 3.13 XSDInclude
    {
        auto* a = makeAttribute(xsdInclude_, eString, "schemaLocation", 0);
        xsdInclude_->addEStructuralFeature(a);

        // resolvedSchema
        auto* r = makeReference(xsdInclude_, xsdSchema_, "resolvedSchema",
                                false, 0, 1, 1);
        xsdInclude_->addEStructuralFeature(r);
    }

    // ===== 3.14 Facet EClass features =====

    // 3.14.1 XSDFacet (abstract)
    {
        // lexicalValue: EString
        auto* a = makeAttribute(xsdFacet_, eString, "lexicalValue", 0);
        xsdFacet_->addEStructuralFeature(a);

        // fixed: EBoolean (volatile)
        a = makeAttribute(xsdFacet_, eBoolean, "fixed", 1);
        xsdFacet_->addEStructuralFeature(a);

        // annotation: containment reference
        auto* r = makeReference(xsdFacet_, xsdAnnotation_, "annotation",
                                /*containment=*/true, 0, 1, 2);
        xsdFacet_->addEStructuralFeature(r);
    }

    // 3.14.2 XSDConstrainingFacet (abstract) - 继承自 XSDFacet，无新 feature

    // 3.14.3 XSDFixedFacet (abstract)
    {
        // value: EInt
        auto* a = makeAttribute(xsdFixedFacet_, eString, "value", 0);
        xsdFixedFacet_->addEStructuralFeature(a);

        // fixed: EBoolean (volatile)
        a = makeAttribute(xsdFixedFacet_, eBoolean, "fixed", 1);
        xsdFixedFacet_->addEStructuralFeature(a);
    }

    // 3.14.4 XSDRepeatableFacet (abstract)
    {
        // annotations: many XSDAnnotation
        auto* r = makeReference(xsdRepeatableFacet_, xsdAnnotation_, "annotations",
                                /*containment=*/false, 0, -1, 0);
        xsdRepeatableFacet_->addEStructuralFeature(r);
    }

    // 3.14.5 XSDFundamentalFacet (abstract) - 继承自 XSDFacet，无新 feature

    // 3.14.6 XSDOrderedFacet
    {
        auto* a = makeAttribute(xsdOrderedFacet_, eString, "value", 0);
        xsdOrderedFacet_->addEStructuralFeature(a);
    }

    // 3.14.7 XSDBoundedFacet
    {
        auto* a = makeAttribute(xsdBoundedFacet_, eBoolean, "value", 0);
        xsdBoundedFacet_->addEStructuralFeature(a);
    }

    // 3.14.8 XSDCardinalityFacet
    {
        auto* a = makeAttribute(xsdCardinalityFacet_, eString, "value", 0);
        xsdCardinalityFacet_->addEStructuralFeature(a);
    }

    // 3.14.9 XSDNumericFacet
    {
        auto* a = makeAttribute(xsdNumericFacet_, eBoolean, "value", 0);
        xsdNumericFacet_->addEStructuralFeature(a);
    }

    // 3.14.10 XSDLengthFacet
    {
        auto* a = makeAttribute(xsdLengthFacet_, eString, "value", 0);
        xsdLengthFacet_->addEStructuralFeature(a);

        a = makeAttribute(xsdLengthFacet_, eBoolean, "fixed", 1);
        xsdLengthFacet_->addEStructuralFeature(a);
    }

    // 3.14.11 XSDMinLengthFacet
    {
        auto* a = makeAttribute(xsdMinLengthFacet_, eString, "value", 0);
        xsdMinLengthFacet_->addEStructuralFeature(a);

        a = makeAttribute(xsdMinLengthFacet_, eBoolean, "fixed", 1);
        xsdMinLengthFacet_->addEStructuralFeature(a);
    }

    // 3.14.12 XSDMaxLengthFacet
    {
        auto* a = makeAttribute(xsdMaxLengthFacet_, eString, "value", 0);
        xsdMaxLengthFacet_->addEStructuralFeature(a);

        a = makeAttribute(xsdMaxLengthFacet_, eBoolean, "fixed", 1);
        xsdMaxLengthFacet_->addEStructuralFeature(a);
    }

    // 3.14.13 XSDPatternFacet
    {
        auto* a = makeAttribute(xsdPatternFacet_, eString, "value", 0);
        xsdPatternFacet_->addEStructuralFeature(a);
    }

    // 3.14.14 XSDEnumerationFacet
    {
        auto* a = makeAttribute(xsdEnumerationFacet_, eString, "value", 0);
        xsdEnumerationFacet_->addEStructuralFeature(a);
    }

    // 3.14.15 XSDWhiteSpaceFacet
    {
        auto* a = makeAttribute(xsdWhiteSpaceFacet_, eString, "value", 0);
        xsdWhiteSpaceFacet_->addEStructuralFeature(a);

        a = makeAttribute(xsdWhiteSpaceFacet_, eBoolean, "fixed", 1);
        xsdWhiteSpaceFacet_->addEStructuralFeature(a);
    }

    // 3.14.16 XSDTotalDigitsFacet
    {
        auto* a = makeAttribute(xsdTotalDigitsFacet_, eString, "value", 0);
        xsdTotalDigitsFacet_->addEStructuralFeature(a);

        a = makeAttribute(xsdTotalDigitsFacet_, eBoolean, "fixed", 1);
        xsdTotalDigitsFacet_->addEStructuralFeature(a);
    }

    // 3.14.17 XSDFractionDigitsFacet
    {
        auto* a = makeAttribute(xsdFractionDigitsFacet_, eString, "value", 0);
        xsdFractionDigitsFacet_->addEStructuralFeature(a);

        a = makeAttribute(xsdFractionDigitsFacet_, eBoolean, "fixed", 1);
        xsdFractionDigitsFacet_->addEStructuralFeature(a);
    }

    // 3.14.18 XSDMinFacet (abstract)
    {
        auto* a = makeAttribute(xsdMinFacet_, eString, "value", 0);
        xsdMinFacet_->addEStructuralFeature(a);

        a = makeAttribute(xsdMinFacet_, eBoolean, "fixed", 1);
        xsdMinFacet_->addEStructuralFeature(a);

        a = makeAttribute(xsdMinFacet_, eBoolean, "inclusive", 2);
        xsdMinFacet_->addEStructuralFeature(a);

        a = makeAttribute(xsdMinFacet_, eBoolean, "exclusive", 3);
        xsdMinFacet_->addEStructuralFeature(a);
    }

    // 3.14.19 XSDMaxFacet (abstract)
    {
        auto* a = makeAttribute(xsdMaxFacet_, eString, "value", 0);
        xsdMaxFacet_->addEStructuralFeature(a);

        a = makeAttribute(xsdMaxFacet_, eBoolean, "fixed", 1);
        xsdMaxFacet_->addEStructuralFeature(a);

        a = makeAttribute(xsdMaxFacet_, eBoolean, "inclusive", 2);
        xsdMaxFacet_->addEStructuralFeature(a);

        a = makeAttribute(xsdMaxFacet_, eBoolean, "exclusive", 3);
        xsdMaxFacet_->addEStructuralFeature(a);
    }

    // 3.14.20 XSDMinInclusiveFacet
    {
        auto* a = makeAttribute(xsdMinInclusiveFacet_, eString, "value", 0);
        xsdMinInclusiveFacet_->addEStructuralFeature(a);

        a = makeAttribute(xsdMinInclusiveFacet_, eBoolean, "fixed", 1);
        xsdMinInclusiveFacet_->addEStructuralFeature(a);
    }

    // 3.14.21 XSDMaxInclusiveFacet
    {
        auto* a = makeAttribute(xsdMaxInclusiveFacet_, eString, "value", 0);
        xsdMaxInclusiveFacet_->addEStructuralFeature(a);

        a = makeAttribute(xsdMaxInclusiveFacet_, eBoolean, "fixed", 1);
        xsdMaxInclusiveFacet_->addEStructuralFeature(a);
    }

    // 3.14.22 XSDMinExclusiveFacet
    {
        auto* a = makeAttribute(xsdMinExclusiveFacet_, eString, "value", 0);
        xsdMinExclusiveFacet_->addEStructuralFeature(a);

        a = makeAttribute(xsdMinExclusiveFacet_, eBoolean, "fixed", 1);
        xsdMinExclusiveFacet_->addEStructuralFeature(a);
    }

    // 3.14.23 XSDMaxExclusiveFacet
    {
        auto* a = makeAttribute(xsdMaxExclusiveFacet_, eString, "value", 0);
        xsdMaxExclusiveFacet_->addEStructuralFeature(a);

        a = makeAttribute(xsdMaxExclusiveFacet_, eBoolean, "fixed", 1);
        xsdMaxExclusiveFacet_->addEStructuralFeature(a);
    }

    // 4. 设置 superTypes（XSD 类型全部继承 EObject）
    // 简化：所有 XSD 概念均以 EObject 为父类
    auto* eObjectClass = emf::ecore::EcorePackage::instance().getEClass_EObject();
    xsdSchema_->addESuperType(eObjectClass);
    xsdComplexTypeDefinition_->addESuperType(xsdTypeDefinition_);
    xsdSimpleTypeDefinition_->addESuperType(xsdTypeDefinition_);
    xsdElementDeclaration_->addESuperType(eObjectClass);
    xsdAttributeDeclaration_->addESuperType(eObjectClass);
    xsdAttributeGroupDefinition_->addESuperType(eObjectClass);
    xsdModelGroup_->addESuperType(eObjectClass);
    xsdWildcard_->addESuperType(eObjectClass);
    xsdTypeDefinition_->addESuperType(eObjectClass);
    xsdParticle_->addESuperType(eObjectClass);
    xsdAnnotation_->addESuperType(eObjectClass);
    xsdImport_->addESuperType(eObjectClass);
    xsdInclude_->addESuperType(eObjectClass);

    // ===== Facet 继承关系（对齐 Java）=====
    // 抽象根
    xsdFacet_->addESuperType(eObjectClass);
    xsdConstrainingFacet_->addESuperType(xsdFacet_);
    xsdFixedFacet_->addESuperType(xsdConstrainingFacet_);
    xsdRepeatableFacet_->addESuperType(xsdConstrainingFacet_);
    xsdFundamentalFacet_->addESuperType(xsdFacet_);
    // Fundamental 四种
    xsdOrderedFacet_->addESuperType(xsdFundamentalFacet_);
    xsdBoundedFacet_->addESuperType(xsdFundamentalFacet_);
    xsdCardinalityFacet_->addESuperType(xsdFundamentalFacet_);
    xsdNumericFacet_->addESuperType(xsdFundamentalFacet_);
    // Fixed 子类
    xsdLengthFacet_->addESuperType(xsdFixedFacet_);
    xsdMinLengthFacet_->addESuperType(xsdFixedFacet_);
    xsdMaxLengthFacet_->addESuperType(xsdFixedFacet_);
    xsdWhiteSpaceFacet_->addESuperType(xsdFixedFacet_);
    xsdTotalDigitsFacet_->addESuperType(xsdFixedFacet_);
    xsdFractionDigitsFacet_->addESuperType(xsdFixedFacet_);
    // Repeatable 子类
    xsdPatternFacet_->addESuperType(xsdRepeatableFacet_);
    xsdEnumerationFacet_->addESuperType(xsdRepeatableFacet_);
    // Min/Max（抽象）
    xsdMinFacet_->addESuperType(xsdFixedFacet_);
    xsdMaxFacet_->addESuperType(xsdFixedFacet_);
    // Min/Max 子类
    xsdMinInclusiveFacet_->addESuperType(xsdMinFacet_);
    xsdMaxInclusiveFacet_->addESuperType(xsdMaxFacet_);
    xsdMinExclusiveFacet_->addESuperType(xsdMinFacet_);
    xsdMaxExclusiveFacet_->addESuperType(xsdMaxFacet_);

    // 5. 把所有 classifiers 加到 package
    xsdPackage_->addEClassifier(xsdSchema_);
    xsdPackage_->addEClassifier(xsdComplexTypeDefinition_);
    xsdPackage_->addEClassifier(xsdSimpleTypeDefinition_);
    xsdPackage_->addEClassifier(xsdElementDeclaration_);
    xsdPackage_->addEClassifier(xsdAttributeDeclaration_);
    xsdPackage_->addEClassifier(xsdAttributeGroupDefinition_);
    xsdPackage_->addEClassifier(xsdModelGroup_);
    xsdPackage_->addEClassifier(xsdWildcard_);
    xsdPackage_->addEClassifier(xsdTypeDefinition_);
    xsdPackage_->addEClassifier(xsdParticle_);
    xsdPackage_->addEClassifier(xsdAnnotation_);
    xsdPackage_->addEClassifier(xsdImport_);
    xsdPackage_->addEClassifier(xsdInclude_);
    // Facet classifiers
    xsdPackage_->addEClassifier(xsdFacet_);
    xsdPackage_->addEClassifier(xsdConstrainingFacet_);
    xsdPackage_->addEClassifier(xsdFixedFacet_);
    xsdPackage_->addEClassifier(xsdRepeatableFacet_);
    xsdPackage_->addEClassifier(xsdFundamentalFacet_);
    xsdPackage_->addEClassifier(xsdOrderedFacet_);
    xsdPackage_->addEClassifier(xsdBoundedFacet_);
    xsdPackage_->addEClassifier(xsdCardinalityFacet_);
    xsdPackage_->addEClassifier(xsdNumericFacet_);
    xsdPackage_->addEClassifier(xsdLengthFacet_);
    xsdPackage_->addEClassifier(xsdMinLengthFacet_);
    xsdPackage_->addEClassifier(xsdMaxLengthFacet_);
    xsdPackage_->addEClassifier(xsdPatternFacet_);
    xsdPackage_->addEClassifier(xsdEnumerationFacet_);
    xsdPackage_->addEClassifier(xsdWhiteSpaceFacet_);
    xsdPackage_->addEClassifier(xsdTotalDigitsFacet_);
    xsdPackage_->addEClassifier(xsdFractionDigitsFacet_);
    xsdPackage_->addEClassifier(xsdMinFacet_);
    xsdPackage_->addEClassifier(xsdMaxFacet_);
    xsdPackage_->addEClassifier(xsdMinInclusiveFacet_);
    xsdPackage_->addEClassifier(xsdMaxInclusiveFacet_);
    xsdPackage_->addEClassifier(xsdMinExclusiveFacet_);
    xsdPackage_->addEClassifier(xsdMaxExclusiveFacet_);

    // 5b. 同时维护 XSDPackage 自有的 classifiers_ 列表
    classifiers_ = {
        xsdSchema_,
        xsdComplexTypeDefinition_,
        xsdSimpleTypeDefinition_,
        xsdElementDeclaration_,
        xsdAttributeDeclaration_,
        xsdAttributeGroupDefinition_,
        xsdModelGroup_,
        xsdWildcard_,
        xsdTypeDefinition_,
        xsdParticle_,
        xsdAnnotation_,
        xsdImport_,
        xsdInclude_,
        // Facet
        xsdFacet_,
        xsdConstrainingFacet_,
        xsdFixedFacet_,
        xsdRepeatableFacet_,
        xsdFundamentalFacet_,
        xsdOrderedFacet_,
        xsdBoundedFacet_,
        xsdCardinalityFacet_,
        xsdNumericFacet_,
        xsdLengthFacet_,
        xsdMinLengthFacet_,
        xsdMaxLengthFacet_,
        xsdPatternFacet_,
        xsdEnumerationFacet_,
        xsdWhiteSpaceFacet_,
        xsdTotalDigitsFacet_,
        xsdFractionDigitsFacet_,
        xsdMinFacet_,
        xsdMaxFacet_,
        xsdMinInclusiveFacet_,
        xsdMaxInclusiveFacet_,
        xsdMinExclusiveFacet_,
        xsdMaxExclusiveFacet_
    };

    // 6. 设置 factory instance
    emf::ecore::EFactory* factory = new emf::ecore::EFactoryImpl();
    if (auto* fi = dynamic_cast<emf::ecore::EFactoryImpl*>(factory)) {
        fi->setEPackage(xsdPackage_);
    }
    xsdPackage_->setEFactoryInstance(factory);

    // 7. 注册到全局 EPackageRegistry
    emf::common::EPackageRegistry::instance().put(eNS_URI, xsdPackage_);
    emf::common::EPackageRegistry::instance().put(std::string(eNAME), xsdPackage_);
    (void)ecorePkg;
    (void)eBoolean;
}

}  // namespace emf::xsd
