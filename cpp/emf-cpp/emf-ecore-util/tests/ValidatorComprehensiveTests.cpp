// ValidatorComprehensiveTests.cpp
// EObjectValidator + EcoreValidator 全量测试
#include "test_main.h"
#include "emf/ecore/util/EObjectValidator.h"
#include "emf/ecore/util/EcoreValidator.h"
#include "emf/ecore/EcorePackage.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/common/Diagnostic.h"
#include "emf/common/EList.h"
#include <vector>
#include <any>

using namespace emf;
using namespace emf::ecore;
using namespace emf::ecore::util;
using emf::common::EObject;
using emf::common::Diagnostic;
using emf::common::BasicDiagnostic;
using emf::common::DiagnosticChain;

// ===== EObjectValidator tests =====

EMF_TEST(EObjectValidator_validate_EveryDefaultConstraint_nullSafe) {
    bool r = EObjectValidator::validate_EveryDefaultConstraint(nullptr, nullptr, nullptr);
    EXPECT_TRUE(r);
}

EMF_TEST(EObjectValidator_validate_NoCircularContainment_positive) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* c = EcoreFactory::instance().createEClass();
    c->setName("Foo");
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r = EObjectValidator::validate_NoCircularContainment(c, &chain, &ctx);
    EXPECT_TRUE(r);
    EXPECT_TRUE(chain.empty());
}

EMF_TEST(EObjectValidator_validate_NoCircularContainment_detectCycle) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* c = EcoreFactory::instance().createEClass();
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    // 强制设置 root == c 来模拟循环
    ctx[EObjectValidator::ROOT_OBJECT] = std::any(static_cast<emf::common::EObject*>(c));
    bool r = EObjectValidator::validate_NoCircularContainment(c, &chain, &ctx);
    EXPECT_FALSE(r);
    EXPECT_FALSE(chain.empty());
}

EMF_TEST(EObjectValidator_validate_EveryBidirectionalReferenceIsPaired_empty) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* c = EcoreFactory::instance().createEClass();
    c->setName("Foo");
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r = EObjectValidator::validate_EveryBidirectionalReferenceIsPaired(c, &chain, &ctx);
    EXPECT_TRUE(r);
}

EMF_TEST(EObjectValidator_validate_EveryMultiplicityConforms_basic) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* c = EcoreFactory::instance().createEClass();
    c->setName("Foo");
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r = EObjectValidator::validate_EveryMultiplicityConforms(c, &chain, &ctx);
    EXPECT_TRUE(r);
}

EMF_TEST(EObjectValidator_validate_EveryProxyResolves_noXRef) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* c = EcoreFactory::instance().createEClass();
    c->setName("Foo");
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r = EObjectValidator::validate_EveryProxyResolves(c, &chain, &ctx);
    EXPECT_TRUE(r);
}

EMF_TEST(EObjectValidator_validate_EveryReferenceIsContained_basic) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* c = EcoreFactory::instance().createEClass();
    c->setName("Foo");
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r = EObjectValidator::validate_EveryReferenceIsContained(c, &chain, &ctx);
    EXPECT_TRUE(r);
}

EMF_TEST(EObjectValidator_validate_EveryDataValueConforms_empty) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* c = EcoreFactory::instance().createEClass();
    c->setName("Foo");
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r = EObjectValidator::validate_EveryDataValueConforms(c, &chain, &ctx);
    EXPECT_TRUE(r);
}

EMF_TEST(EObjectValidator_validate_UniqueID_positive) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* c = EcoreFactory::instance().createEClass();
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r = EObjectValidator::validate_UniqueID(c, &chain, &ctx);
    EXPECT_TRUE(r);
}

EMF_TEST(EObjectValidator_validate_EveryKeyUnique_empty) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* c = EcoreFactory::instance().createEClass();
    c->setName("Foo");
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r = EObjectValidator::validate_EveryKeyUnique(c, &chain, &ctx);
    EXPECT_TRUE(r);
}

EMF_TEST(EObjectValidator_validate_EveryMapEntryUnique_empty) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* c = EcoreFactory::instance().createEClass();
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r = EObjectValidator::validate_EveryMapEntryUnique(c, &chain, &ctx);
    EXPECT_TRUE(r);
}

// ===== EcoreValidator DIAGNOSTIC_CODE tests =====

EMF_TEST(EcoreValidatorCodes_constants) {
    EXPECT_EQ(EcoreValidatorCodes::AT_MOST_ONE_ID, 1);
    EXPECT_EQ(EcoreValidatorCodes::INTERFACE_IS_ABSTRACT, 25);
    EXPECT_EQ(EcoreValidatorCodes::NO_CIRCULAR_SUPER_TYPES, 26);
    EXPECT_EQ(EcoreValidatorCodes::UNIQUE_FEATURE_NAMES, 32);
    EXPECT_EQ(EcoreValidatorCodes::VALID_TYPE, 40);
    EXPECT_EQ(EcoreValidatorCodes::WELL_FORMED_NAME, 44);
    EXPECT_EQ(EcoreValidatorCodes::CONSISTENT_CONTAINER, 51);
}

EMF_TEST(EObjectValidatorCodes_constants) {
    EXPECT_EQ(EObjectValidatorCodes::EOBJECT__NO_CIRCULAR_CONTAINMENT, 15);
    EXPECT_EQ(EObjectValidatorCodes::EOBJECT__UNIQUE_ID, 12);
    EXPECT_EQ(EObjectValidatorCodes::EOBJECT__EVERY_BIDIRECTIONAL_REFERENCE_IS_PAIRED, 16);
}

// ===== EClass tests =====

EMF_TEST(EcoreValidator_validateEClass_positive) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* c = EcoreFactory::instance().createEClass();
    c->setName("Foo");
    c->setAbstract(false);
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r = EcoreValidator::validateEClass(c, &chain, &ctx);
    EXPECT_TRUE(r);
}

EMF_TEST(EcoreValidator_validateEClass_AtMostOneID_positive_oneId) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* c = EcoreFactory::instance().createEClass();
    c->setName("Foo");
    auto* a = EcoreFactory::instance().createEAttribute();
    a->setName("id");
    a->setID(true);
    c->addEStructuralFeature(a);
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r = EcoreValidator::validateEClass_AtMostOneID(c, &chain, &ctx);
    EXPECT_TRUE(r);
}

EMF_TEST(EcoreValidator_validateEClass_AtMostOneID_negative_twoIds) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* c = EcoreFactory::instance().createEClass();
    c->setName("Foo");
    auto* a1 = EcoreFactory::instance().createEAttribute();
    a1->setName("id1");
    a1->setID(true);
    c->addEStructuralFeature(a1);
    auto* a2 = EcoreFactory::instance().createEAttribute();
    a2->setName("id2");
    a2->setID(true);
    c->addEStructuralFeature(a2);
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r = EcoreValidator::validateEClass_AtMostOneID(c, &chain, &ctx);
    EXPECT_FALSE(r);
    EXPECT_FALSE(chain.empty());
}

EMF_TEST(EcoreValidator_validateEClass_InterfaceIsAbstract_positive) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* c = EcoreFactory::instance().createEClass();
    c->setName("IFace");
    c->setInterface(true);
    c->setAbstract(true);
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r = EcoreValidator::validateEClass_InterfaceIsAbstract(c, &chain, &ctx);
    EXPECT_TRUE(r);
}

EMF_TEST(EcoreValidator_validateEClass_InterfaceIsAbstract_negative) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* c = EcoreFactory::instance().createEClass();
    c->setName("IFace");
    c->setInterface(true);
    c->setAbstract(false);
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r = EcoreValidator::validateEClass_InterfaceIsAbstract(c, &chain, &ctx);
    EXPECT_FALSE(r);
    EXPECT_FALSE(chain.empty());
}

EMF_TEST(EcoreValidator_validateEClass_UniqueFeatureNames_positive) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* c = EcoreFactory::instance().createEClass();
    c->setName("Foo");
    auto* a = EcoreFactory::instance().createEAttribute();
    a->setName("name");
    c->addEStructuralFeature(a);
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r = EcoreValidator::validateEClass_UniqueFeatureNames(c, &chain, &ctx);
    EXPECT_TRUE(r);
}

EMF_TEST(EcoreValidator_validateEClass_UniqueFeatureNames_negative) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* c = EcoreFactory::instance().createEClass();
    c->setName("Foo");
    auto* a1 = EcoreFactory::instance().createEAttribute();
    a1->setName("dup");
    c->addEStructuralFeature(a1);
    auto* a2 = EcoreFactory::instance().createEAttribute();
    a2->setName("dup");
    c->addEStructuralFeature(a2);
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r = EcoreValidator::validateEClass_UniqueFeatureNames(c, &chain, &ctx);
    EXPECT_FALSE(r);
}

EMF_TEST(EcoreValidator_validateEClass_NoCircularSuperTypes_positive) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* c = EcoreFactory::instance().createEClass();
    c->setName("A");
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r = EcoreValidator::validateEClass_NoCircularSuperTypes(c, &chain, &ctx);
    EXPECT_TRUE(r);
}

EMF_TEST(EcoreValidator_validateEClass_ConsistentSuperTypes_positive) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* c = EcoreFactory::instance().createEClass();
    c->setName("Foo");
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r = EcoreValidator::validateEClass_ConsistentSuperTypes(c, &chain, &ctx);
    EXPECT_TRUE(r);
}

EMF_TEST(EcoreValidator_validateEClass_DisjointFeatureAndOperationSignatures_positive) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* c = EcoreFactory::instance().createEClass();
    c->setName("Foo");
    auto* a = EcoreFactory::instance().createEAttribute();
    a->setName("name");
    c->addEStructuralFeature(a);
    auto* op = EcoreFactory::instance().createEOperation();
    op->setName("op");
    c->addEOperation(op);
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r = EcoreValidator::validateEClass_DisjointFeatureAndOperationSignatures(c, &chain, &ctx);
    EXPECT_TRUE(r);
}

EMF_TEST(EcoreValidator_validateEClass_DisjointFeatureAndOperationSignatures_negative) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* c = EcoreFactory::instance().createEClass();
    c->setName("Foo");
    auto* a = EcoreFactory::instance().createEAttribute();
    a->setName("doIt");
    c->addEStructuralFeature(a);
    auto* op = EcoreFactory::instance().createEOperation();
    op->setName("doIt");
    c->addEOperation(op);
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r = EcoreValidator::validateEClass_DisjointFeatureAndOperationSignatures(c, &chain, &ctx);
    EXPECT_FALSE(r);
}

EMF_TEST(EcoreValidator_validateEClass_UniqueOperationSignatures_positive) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* c = EcoreFactory::instance().createEClass();
    c->setName("Foo");
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r = EcoreValidator::validateEClass_UniqueOperationSignatures(c, &chain, &ctx);
    EXPECT_TRUE(r);
}

// ===== EAttribute tests =====

EMF_TEST(EcoreValidator_validateEAttribute_ConsistentTransient_positive) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* a = EcoreFactory::instance().createEAttribute();
    a->setName("a");
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r = EcoreValidator::validateEAttribute_ConsistentTransient(a, &chain, &ctx);
    EXPECT_TRUE(r);
}

EMF_TEST(EcoreValidator_validateEAttribute_ConsistentTransient_negative) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* c = EcoreFactory::instance().createEClass();
    c->setName("Foo");
    auto* a = EcoreFactory::instance().createEAttribute();
    a->setName("a");
    a->setTransient(true);
    a->setEContainingClass(c);
    c->addEStructuralFeature(a);
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r = EcoreValidator::validateEAttribute_ConsistentTransient(a, &chain, &ctx);
    EXPECT_FALSE(r);
}

EMF_TEST(EcoreValidator_validateEAttribute_top_positive) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* a = EcoreFactory::instance().createEAttribute();
    a->setName("a");
    a->setEAttributeType(EcorePackage::instance().getEDataType_EInt());
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r = EcoreValidator::validateEAttribute(a, &chain, &ctx);
    EXPECT_TRUE(r);
}

// ===== EAnnotation tests =====

EMF_TEST(EcoreValidator_validateEAnnotation_WellFormed_positive) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* ann = EcoreFactory::instance().createEAnnotation();
    ann->setSource("http://foo");
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r = EcoreValidator::validateEAnnotation_WellFormed(ann, &chain, &ctx);
    EXPECT_TRUE(r);
}

EMF_TEST(EcoreValidator_validateEAnnotation_WellFormed_negative_emptySource) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* ann = EcoreFactory::instance().createEAnnotation();
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r = EcoreValidator::validateEAnnotation_WellFormed(ann, &chain, &ctx);
    EXPECT_FALSE(r);
}

EMF_TEST(EcoreValidator_validateEAnnotation_WellFormedSourceURI_positive) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* ann = EcoreFactory::instance().createEAnnotation();
    ann->setSource("http://foo/bar");
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r = EcoreValidator::validateEAnnotation_WellFormedSourceURI(ann, &chain, &ctx);
    EXPECT_TRUE(r);
}

EMF_TEST(EcoreValidator_validateEAnnotation_WellFormedSourceURI_negative) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* ann = EcoreFactory::instance().createEAnnotation();
    ann->setSource("not-a-uri");
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r = EcoreValidator::validateEAnnotation_WellFormedSourceURI(ann, &chain, &ctx);
    EXPECT_FALSE(r);
}

// ===== EClassifier tests =====

EMF_TEST(EcoreValidator_validateEClassifier_WellFormedInstanceTypeName_positive) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* c = EcoreFactory::instance().createEClass();
    c->setName("Foo");
    c->setInstanceClassName("java.lang.String");
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r = EcoreValidator::validateEClassifier_WellFormedInstanceTypeName(c, &chain, &ctx);
    EXPECT_TRUE(r);
}

EMF_TEST(EcoreValidator_validateEClassifier_WellFormedInstanceTypeName_negative) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* c = EcoreFactory::instance().createEClass();
    c->setName("Foo");
    c->setInstanceClassName(".bad.");
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r = EcoreValidator::validateEClassifier_WellFormedInstanceTypeName(c, &chain, &ctx);
    EXPECT_FALSE(r);
}

EMF_TEST(EcoreValidator_validateEClassifier_UniqueTypeParameterNames_positive) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* c = EcoreFactory::instance().createEClass();
    c->setName("Foo");
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r = EcoreValidator::validateEClassifier_UniqueTypeParameterNames(c, &chain, &ctx);
    EXPECT_TRUE(r);
}

// ===== EEnum tests =====

EMF_TEST(EcoreValidator_validateEEnum_UniqueEnumeratorNames_positive) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* e = EcoreFactory::instance().createEEnum();
    e->setName("Color");
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r = EcoreValidator::validateEEnum_UniqueEnumeratorNames(e, &chain, &ctx);
    EXPECT_TRUE(r);
}

EMF_TEST(EcoreValidator_validateEEnum_UniqueEnumeratorLiterals_positive) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* e = EcoreFactory::instance().createEEnum();
    e->setName("Color");
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r = EcoreValidator::validateEEnum_UniqueEnumeratorLiterals(e, &chain, &ctx);
    EXPECT_TRUE(r);
}

EMF_TEST(EcoreValidator_validateEEnum_top_positive) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* e = EcoreFactory::instance().createEEnum();
    e->setName("Color");
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r = EcoreValidator::validateEEnum(e, &chain, &ctx);
    EXPECT_TRUE(r);
}

// ===== EOperation tests =====

EMF_TEST(EcoreValidator_validateEOperation_UniqueParameterNames_positive) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* op = EcoreFactory::instance().createEOperation();
    op->setName("op");
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r = EcoreValidator::validateEOperation_UniqueParameterNames(op, &chain, &ctx);
    EXPECT_TRUE(r);
}

EMF_TEST(EcoreValidator_validateEOperation_UniqueTypeParameterNames_positive) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* op = EcoreFactory::instance().createEOperation();
    op->setName("op");
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r = EcoreValidator::validateEOperation_UniqueTypeParameterNames(op, &chain, &ctx);
    EXPECT_TRUE(r);
}

EMF_TEST(EcoreValidator_validateEOperation_NoRepeatingVoid_positive) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* op = EcoreFactory::instance().createEOperation();
    op->setName("op");
    op->setEType(EcorePackage::instance().getEDataType_EInt());
    op->setUpperBound(1);
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r = EcoreValidator::validateEOperation_NoRepeatingVoid(op, &chain, &ctx);
    EXPECT_TRUE(r);
}

EMF_TEST(EcoreValidator_validateEOperation_top_positive) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* op = EcoreFactory::instance().createEOperation();
    op->setName("op");
    op->setEType(EcorePackage::instance().getEDataType_EInt());
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r = EcoreValidator::validateEOperation(op, &chain, &ctx);
    EXPECT_TRUE(r);
}

// ===== EPackage tests =====

EMF_TEST(EcoreValidator_validateEPackage_WellFormedNsURI_positive) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* p = EcoreFactory::instance().createEPackage();
    p->setName("P");
    p->setNsURI("http://foo/bar");
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r = EcoreValidator::validateEPackage_WellFormedNsURI(p, &chain, &ctx);
    EXPECT_TRUE(r);
}

EMF_TEST(EcoreValidator_validateEPackage_WellFormedNsURI_negative) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* p = EcoreFactory::instance().createEPackage();
    p->setName("P");
    p->setNsURI("bad");
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r = EcoreValidator::validateEPackage_WellFormedNsURI(p, &chain, &ctx);
    EXPECT_FALSE(r);
}

EMF_TEST(EcoreValidator_validateEPackage_WellFormedNsPrefix_positive) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* p = EcoreFactory::instance().createEPackage();
    p->setName("P");
    p->setNsPrefix("foo");
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r = EcoreValidator::validateEPackage_WellFormedNsPrefix(p, &chain, &ctx);
    EXPECT_TRUE(r);
}

EMF_TEST(EcoreValidator_validateEPackage_WellFormedNsPrefix_negative) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* p = EcoreFactory::instance().createEPackage();
    p->setName("P");
    p->setNsPrefix("123-bad");
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r = EcoreValidator::validateEPackage_WellFormedNsPrefix(p, &chain, &ctx);
    EXPECT_FALSE(r);
}

EMF_TEST(EcoreValidator_validateEPackage_UniqueSubpackageNames_positive) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* p = EcoreFactory::instance().createEPackage();
    p->setName("P");
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r = EcoreValidator::validateEPackage_UniqueSubpackageNames(p, &chain, &ctx);
    EXPECT_TRUE(r);
}

EMF_TEST(EcoreValidator_validateEPackage_UniqueClassifierNames_positive) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* p = EcoreFactory::instance().createEPackage();
    p->setName("P");
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r = EcoreValidator::validateEPackage_UniqueClassifierNames(p, &chain, &ctx);
    EXPECT_TRUE(r);
}

EMF_TEST(EcoreValidator_validateEPackage_UniqueNsURIs_positive) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* p = EcoreFactory::instance().createEPackage();
    p->setName("P");
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r = EcoreValidator::validateEPackage_UniqueNsURIs(p, &chain, &ctx);
    EXPECT_TRUE(r);
}

EMF_TEST(EcoreValidator_validateEPackage_top_positive) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* p = EcoreFactory::instance().createEPackage();
    p->setName("P");
    p->setNsURI("http://x/y");
    p->setNsPrefix("y");
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r = EcoreValidator::validateEPackage(p, &chain, &ctx);
    EXPECT_TRUE(r);
}

EMF_TEST(EcoreValidator_validateEPackage_top_negative_badNsURI) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* p = EcoreFactory::instance().createEPackage();
    p->setName("P");
    p->setNsURI("bad");
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r = EcoreValidator::validateEPackage(p, &chain, &ctx);
    EXPECT_FALSE(r);
}

// ===== EReference tests =====

EMF_TEST(EcoreValidator_validateEReference_ConsistentOpposite_positive_noOpp) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* r = EcoreFactory::instance().createEReference();
    r->setName("ref");
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r1 = EcoreValidator::validateEReference_ConsistentOpposite(r, &chain, &ctx);
    EXPECT_TRUE(r1);
}

EMF_TEST(EcoreValidator_validateEReference_ConsistentOpposite_negative_mismatch) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* r1 = EcoreFactory::instance().createEReference();
    r1->setName("a");
    auto* r2 = EcoreFactory::instance().createEReference();
    r2->setName("b");
    r1->setEOpposite(r2);
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r = EcoreValidator::validateEReference_ConsistentOpposite(r1, &chain, &ctx);
    EXPECT_FALSE(r);
}

EMF_TEST(EcoreValidator_validateEReference_ConsistentOpposite_negative_bothContainment) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* r1 = EcoreFactory::instance().createEReference();
    r1->setName("a");
    r1->setContainment(true);
    auto* r2 = EcoreFactory::instance().createEReference();
    r2->setName("b");
    r2->setContainment(true);
    r1->setEOpposite(r2);
    r2->setEOpposite(r1);
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r = EcoreValidator::validateEReference_ConsistentOpposite(r1, &chain, &ctx);
    EXPECT_FALSE(r);
}

EMF_TEST(EcoreValidator_validateEReference_SingleContainer_positive) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* r = EcoreFactory::instance().createEReference();
    r->setName("ref");
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r1 = EcoreValidator::validateEReference_SingleContainer(r, &chain, &ctx);
    EXPECT_TRUE(r1);
}

EMF_TEST(EcoreValidator_validateEReference_ConsistentKeys_positive_nonContain) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* r = EcoreFactory::instance().createEReference();
    r->setName("ref");
    r->setContainment(false);
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r1 = EcoreValidator::validateEReference_ConsistentKeys(r, &chain, &ctx);
    EXPECT_TRUE(r1);
}

EMF_TEST(EcoreValidator_validateEReference_ConsistentUnique_positive) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* r = EcoreFactory::instance().createEReference();
    r->setName("ref");
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r1 = EcoreValidator::validateEReference_ConsistentUnique(r, &chain, &ctx);
    EXPECT_TRUE(r1);
}

EMF_TEST(EcoreValidator_validateEReference_ConsistentContainer_positive) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* r = EcoreFactory::instance().createEReference();
    r->setName("ref");
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r1 = EcoreValidator::validateEReference_ConsistentContainer(r, &chain, &ctx);
    EXPECT_TRUE(r1);
}

EMF_TEST(EcoreValidator_validateEReference_top_positive) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* r = EcoreFactory::instance().createEReference();
    r->setName("ref");
    r->setEType(EcorePackage::instance().getEClass_EClass());
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r1 = EcoreValidator::validateEReference(r, &chain, &ctx);
    EXPECT_TRUE(r1);
}

// ===== EStructuralFeature tests =====

EMF_TEST(EcoreValidator_validateEStructuralFeature_ValidDefaultValueLiteral_positive) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* a = EcoreFactory::instance().createEAttribute();
    a->setName("a");
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r = EcoreValidator::validateEStructuralFeature_ValidDefaultValueLiteral(a, &chain, &ctx);
    EXPECT_TRUE(r);
}

EMF_TEST(EcoreValidator_validateEStructuralFeature_ValidDefaultValueLiteral_negative_null) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* a = EcoreFactory::instance().createEAttribute();
    a->setName("a");
    a->setLowerBound(1);
    a->setDefaultValueLiteral("null");
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r = EcoreValidator::validateEStructuralFeature_ValidDefaultValueLiteral(a, &chain, &ctx);
    EXPECT_FALSE(r);
}

// ===== ETypedElement tests =====

EMF_TEST(EcoreValidator_validateETypedElement_ValidLowerBound_positive) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* a = EcoreFactory::instance().createEAttribute();
    a->setName("a");
    a->setLowerBound(0);
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r = EcoreValidator::validateETypedElement_ValidLowerBound(a, &chain, &ctx);
    EXPECT_TRUE(r);
}

EMF_TEST(EcoreValidator_validateETypedElement_ValidLowerBound_negative) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* a = EcoreFactory::instance().createEAttribute();
    a->setName("a");
    a->setLowerBound(-1);
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r = EcoreValidator::validateETypedElement_ValidLowerBound(a, &chain, &ctx);
    EXPECT_FALSE(r);
}

EMF_TEST(EcoreValidator_validateETypedElement_ValidUpperBound_positive) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* a = EcoreFactory::instance().createEAttribute();
    a->setName("a");
    a->setUpperBound(1);
    a->setLowerBound(0);
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r = EcoreValidator::validateETypedElement_ValidUpperBound(a, &chain, &ctx);
    EXPECT_TRUE(r);
}

EMF_TEST(EcoreValidator_validateETypedElement_ValidUpperBound_negative) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* a = EcoreFactory::instance().createEAttribute();
    a->setName("a");
    a->setUpperBound(1);
    a->setLowerBound(2);
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r = EcoreValidator::validateETypedElement_ValidUpperBound(a, &chain, &ctx);
    EXPECT_FALSE(r);
}

EMF_TEST(EcoreValidator_validateETypedElement_ConsistentBounds_positive) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* a = EcoreFactory::instance().createEAttribute();
    a->setName("a");
    a->setLowerBound(0);
    a->setUpperBound(2);
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r = EcoreValidator::validateETypedElement_ConsistentBounds(a, &chain, &ctx);
    EXPECT_TRUE(r);
}

EMF_TEST(EcoreValidator_validateETypedElement_ConsistentBounds_negative) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* a = EcoreFactory::instance().createEAttribute();
    a->setName("a");
    a->setLowerBound(5);
    a->setUpperBound(2);
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r = EcoreValidator::validateETypedElement_ConsistentBounds(a, &chain, &ctx);
    EXPECT_FALSE(r);
}

EMF_TEST(EcoreValidator_validateETypedElement_ValidType_positive) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* a = EcoreFactory::instance().createEAttribute();
    a->setName("a");
    a->setEType(EcorePackage::instance().getEDataType_EInt());
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r = EcoreValidator::validateETypedElement_ValidType(a, &chain, &ctx);
    EXPECT_TRUE(r);
}

EMF_TEST(EcoreValidator_validateETypedElement_ValidType_negative_null) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* a = EcoreFactory::instance().createEAttribute();
    a->setName("a");
    a->setEType(nullptr);
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r = EcoreValidator::validateETypedElement_ValidType(a, &chain, &ctx);
    EXPECT_FALSE(r);
}

// ===== ENamedElement tests =====

EMF_TEST(EcoreValidator_validateENamedElement_WellFormedName_positive) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* c = EcoreFactory::instance().createEClass();
    c->setName("Foo");
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r = EcoreValidator::validateENamedElement_WellFormedName(c, &chain, &ctx);
    EXPECT_TRUE(r);
}

EMF_TEST(EcoreValidator_validateENamedElement_WellFormedName_negative) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* c = EcoreFactory::instance().createEClass();
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r = EcoreValidator::validateENamedElement_WellFormedName(c, &chain, &ctx);
    EXPECT_FALSE(r);
}

EMF_TEST(EcoreValidator_validateENamedElement_WellFormedName_strict_negative) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* c = EcoreFactory::instance().createEClass();
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    ctx[EcoreValidator::STRICT_NAMED_ELEMENT_NAMES] = std::any(true);
    bool r = EcoreValidator::validateENamedElement_WellFormedName(c, &chain, &ctx);
    EXPECT_FALSE(r);
    if (!chain.empty()) {
        EXPECT_TRUE(chain.get()[0]->severity() == Diagnostic::Severity::ERROR);
    }
}

// ===== EDataType stubs =====

EMF_TEST(EcoreValidator_EDataTypeStubs_positive) {
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    EXPECT_TRUE(EcoreValidator::validateEBoolean(true, &chain, &ctx));
    EXPECT_TRUE(EcoreValidator::validateEInt(42, &chain, &ctx));
    EXPECT_TRUE(EcoreValidator::validateEInt(-1, &chain, &ctx));
    EXPECT_TRUE(EcoreValidator::validateEString("hello", &chain, &ctx));
    EXPECT_TRUE(EcoreValidator::validateEDouble(3.14, &chain, &ctx));
    EXPECT_TRUE(EcoreValidator::validateEFloat(1.0f, &chain, &ctx));
    EXPECT_TRUE(EcoreValidator::validateELong(12345678LL, &chain, &ctx));
    EXPECT_TRUE(EcoreValidator::validateEShort(short(7), &chain, &ctx));
    EXPECT_TRUE(EcoreValidator::validateEBooleanObject(std::any(true), &chain, &ctx));
    EXPECT_TRUE(EcoreValidator::validateEIntegerObject(std::any(42), &chain, &ctx));
}

// ===== EGenericType tests =====

EMF_TEST(EcoreValidator_validateEGenericType_ConsistentType_positive) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* gt = EcoreFactory::instance().createEGenericType();
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r = EcoreValidator::validateEGenericType_ConsistentType(gt, &chain, &ctx);
    EXPECT_TRUE(r);
}

EMF_TEST(EcoreValidator_validateEGenericType_ConsistentBounds_positive) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* gt = EcoreFactory::instance().createEGenericType();
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r = EcoreValidator::validateEGenericType_ConsistentBounds(gt, &chain, &ctx);
    EXPECT_TRUE(r);
}

EMF_TEST(EcoreValidator_validateEGenericType_ConsistentArguments_positive) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* gt = EcoreFactory::instance().createEGenericType();
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r = EcoreValidator::validateEGenericType_ConsistentArguments(gt, &chain, &ctx);
    EXPECT_TRUE(r);
}

// ===== Entry validate(int, ...) =====

EMF_TEST(EcoreValidator_validate_classifierID_stub) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* c = EcoreFactory::instance().createEClass();
    c->setName("Foo");
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r = EcoreValidator::validate(0, c, &chain, &ctx);
    EXPECT_TRUE(r);
}

// V1 回归：validateEObject 分派到具体约束（EClass_NoCircularSuperTypes 检测环）
EMF_TEST(EcoreValidator_validateEObject_dispatchesToEClassConstraint) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* a = EcoreFactory::instance().createEClass();
    a->setName("A");
    auto* b = EcoreFactory::instance().createEClass();
    b->setName("B");
    // 构造循环继承 A -> B -> A
    a->addESuperType(b);
    b->addESuperType(a);
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    // V1 修复前：validateEObject 只调 validateEModelElement 桩，返回 true 不报环
    // V1 修复后：分派到 validateEClass -> validateEClass_NoCircularSuperTypes，报 ERROR
    bool r = EcoreValidator::validateEObject(a, &chain, &ctx);
    EXPECT_FALSE(r);  // 检测到环，返回 false
    EXPECT_FALSE(chain.empty());  // chain 有诊断
}

// V1 回归：validate(classifierID, ...) 委托 validateEObject 分派
EMF_TEST(EcoreValidator_validate_classifierID_dispatches) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* a = EcoreFactory::instance().createEClass();
    a->setName("A");
    auto* b = EcoreFactory::instance().createEClass();
    b->setName("B");
    a->addESuperType(b);
    b->addESuperType(a);
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r = EcoreValidator::validate(0, a, &chain, &ctx);
    EXPECT_FALSE(r);  // 分派到 EClass 约束检测环
}

// V1 回归：validateEObject 分派到 EPackage 约束（WellFormedNsURI）
EMF_TEST(EcoreValidator_validateEObject_dispatchesToEPackageConstraint) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* p = EcoreFactory::instance().createEPackage();
    p->setName("p");
    p->setNsURI("invalid uri with spaces");  // 不规范 URI
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r = EcoreValidator::validateEObject(p, &chain, &ctx);
    EXPECT_FALSE(r);  // WellFormedNsURI 报 ERROR
}

// ===== Misc top-level dispatchers =====

EMF_TEST(EcoreValidator_validateETypeParameter_positive) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* tp = EcoreFactory::instance().createETypeParameter();
    tp->setName("T");
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r = EcoreValidator::validateETypeParameter(tp, &chain, &ctx);
    EXPECT_TRUE(r);
}

EMF_TEST(EcoreValidator_validateEFactory_positive) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* f = EcoreFactory::instance().createEFactory();
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r = EcoreValidator::validateEFactory(f, &chain, &ctx);
    EXPECT_TRUE(r);
}

EMF_TEST(EcoreValidator_validateEObject_positive) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* c = EcoreFactory::instance().createEClass();
    c->setName("Foo");
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r = EcoreValidator::validateEObject(c, &chain, &ctx);
    EXPECT_TRUE(r);
}

EMF_TEST(EcoreValidator_validateEModelElement_positive) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* c = EcoreFactory::instance().createEClass();
    c->setName("Foo");
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r = EcoreValidator::validateEModelElement(c, &chain, &ctx);
    EXPECT_TRUE(r);
}

EMF_TEST(EcoreValidator_validateENamedElement_positive) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* c = EcoreFactory::instance().createEClass();
    c->setName("Foo");
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r = EcoreValidator::validateENamedElement(c, &chain, &ctx);
    EXPECT_TRUE(r);
}

EMF_TEST(EcoreValidator_validateEClassifier_positive) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* c = EcoreFactory::instance().createEClass();
    c->setName("Foo");
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r = EcoreValidator::validateEClassifier(c, &chain, &ctx);
    EXPECT_TRUE(r);
}

EMF_TEST(EcoreValidator_validateEDataType_positive) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* dt = EcoreFactory::instance().createEDataType();
    dt->setName("Foo");
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r = EcoreValidator::validateEDataType(dt, &chain, &ctx);
    EXPECT_TRUE(r);
}

EMF_TEST(EcoreValidator_validateEEnumLiteral_positive) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* e = EcoreFactory::instance().createEEnum();
    e->setName("E");
    auto* lit = EcoreFactory::instance().createEEnumLiteral();
    lit->setName("FOO");
    lit->setLiteral("FOO");
    lit->setValue(0);
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r = EcoreValidator::validateEEnumLiteral(lit, &chain, &ctx);
    EXPECT_TRUE(r);
}

EMF_TEST(EcoreValidator_validateEParameter_positive) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* p = EcoreFactory::instance().createEParameter();
    p->setName("x");
    p->setEType(EcorePackage::instance().getEDataType_EInt());
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r = EcoreValidator::validateEParameter(p, &chain, &ctx);
    EXPECT_TRUE(r);
}

EMF_TEST(EcoreValidator_validateEGenericType_top_positive) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* gt = EcoreFactory::instance().createEGenericType();
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r = EcoreValidator::validateEGenericType(gt, &chain, &ctx);
    EXPECT_TRUE(r);
}

EMF_TEST(EcoreValidator_validateEStringToStringMapEntry_positive) {
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    std::pair<std::string, std::string> entry{"k", "v"};
    bool r = EcoreValidator::validateEStringToStringMapEntry(&entry, &chain, &ctx);
    EXPECT_TRUE(r);
}

EMF_TEST(EcoreValidator_validateEStructuralFeature_positive) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* a = EcoreFactory::instance().createEAttribute();
    a->setName("a");
    a->setEType(EcorePackage::instance().getEDataType_EInt());
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r = EcoreValidator::validateEStructuralFeature(a, &chain, &ctx);
    EXPECT_TRUE(r);
}

EMF_TEST(EcoreValidator_validateETypedElement_positive) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* a = EcoreFactory::instance().createEAttribute();
    a->setName("a");
    a->setEType(EcorePackage::instance().getEDataType_EInt());
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r = EcoreValidator::validateETypedElement(a, &chain, &ctx);
    EXPECT_TRUE(r);
}

EMF_TEST(EcoreValidator_validateEAnnotation_top_positive) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* ann = EcoreFactory::instance().createEAnnotation();
    ann->setSource("http://foo");
    DiagnosticChain chain;
    std::unordered_map<std::string, std::any> ctx;
    bool r = EcoreValidator::validateEAnnotation(ann, &chain, &ctx);
    EXPECT_TRUE(r);
}

EMF_TEST(EcoreValidator_oldAPI_validate) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* p = EcoreFactory::instance().createEPackage();
    p->setName("P");
    p->setNsURI("http://x/y");
    p->setNsPrefix("y");
    auto diags = EcoreValidator::validate(p);
    EXPECT_TRUE(diags.empty());
}

EMF_TEST(EcoreValidator_oldAPI_validateEClass) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* c = EcoreFactory::instance().createEClass();
    c->setName("Foo");
    auto diags = EcoreValidator::validateEClass(c);
    EXPECT_TRUE(diags.empty());
}

// ===== Diagnostic tool tests =====

EMF_TEST(BasicDiagnostic_createAndAdd) {
    auto d = std::make_shared<BasicDiagnostic>(
        Diagnostic::Severity::ERROR, "test", 42, "boom");
    EXPECT_TRUE(d->severity() == Diagnostic::Severity::ERROR);
    EXPECT_EQ(d->code(), 42);
    EXPECT_EQ(d->message(), "boom");

    auto child = std::make_shared<BasicDiagnostic>(
        Diagnostic::Severity::WARNING, "child", 7, "child msg");
    d->add(child);
    EXPECT_EQ(d->children().size(), 1u);
}

EMF_TEST(BasicDiagnostic_severityUpgrade) {
    auto d = std::make_shared<BasicDiagnostic>(
        Diagnostic::Severity::OK, "t", 0, "ok");
    auto e = std::make_shared<BasicDiagnostic>(
        Diagnostic::Severity::ERROR, "t", 1, "err");
    d->add(e);
    EXPECT_TRUE(d->severity() == Diagnostic::Severity::ERROR);
}

EMF_TEST(DiagnosticChain_addAndSize) {
    DiagnosticChain chain;
    EXPECT_TRUE(chain.empty());
    chain.add(std::make_shared<BasicDiagnostic>(Diagnostic::Severity::INFO, "t", 0, "i"));
    EXPECT_FALSE(chain.empty());
    EXPECT_EQ(chain.size(), 1u);
    chain.clear();
    EXPECT_TRUE(chain.empty());
}

EMF_TEST(EcoreValidator_isWellFormedURI) {
    EXPECT_TRUE(EcoreValidator::isWellFormedURI("http://x"));
    EXPECT_TRUE(EcoreValidator::isWellFormedURI("https://x/y"));
    EXPECT_TRUE(EcoreValidator::isWellFormedURI("file:/x/y"));
    EXPECT_FALSE(EcoreValidator::isWellFormedURI(""));
    EXPECT_FALSE(EcoreValidator::isWellFormedURI("not-a-uri"));
}

EMF_TEST(EcoreValidator_isWellFormedJavaIdentifier) {
    EXPECT_TRUE(EcoreValidator::isWellFormedJavaIdentifier("Foo"));
    EXPECT_TRUE(EcoreValidator::isWellFormedJavaIdentifier("_x"));
    EXPECT_TRUE(EcoreValidator::isWellFormedJavaIdentifier("$y"));
    EXPECT_FALSE(EcoreValidator::isWellFormedJavaIdentifier(""));
    EXPECT_FALSE(EcoreValidator::isWellFormedJavaIdentifier("1foo"));
    EXPECT_FALSE(EcoreValidator::isWellFormedJavaIdentifier("foo-bar"));
}
