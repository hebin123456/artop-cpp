// EObjectValidator 测试
#include "test_main.h"
#include "emf/ecore/util/EcoreUtil.h"
#include "emf/ecore/util/EObjectValidator.h"
#include "emf/ecore/EcorePackage.h"
#include "emf/common/Diagnostic.h"

using namespace emf;
using namespace emf::ecore;
using namespace emf::ecore::util;
using emf::common::Diagnostic;

EMF_TEST(EObjectValidator_EmptyPackage_HasErrors) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* p = new EPackageImpl();
    auto diags = EObjectValidator::validateEPackage(p);
    EXPECT_TRUE(!diags.empty());
}

EMF_TEST(EObjectValidator_ClassWithoutName) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* c = EcoreFactory::instance().createEClass();
    auto diags = EObjectValidator::validateEClass(c);
    EXPECT_TRUE(!diags.empty());
}

EMF_TEST(EObjectValidator_AttributeWithoutType) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* a = EcoreFactory::instance().createEAttribute();
    a->setName("foo");
    auto diags = EObjectValidator::validateEAttribute(a);
    // 对齐 Java：EObjectValidator 不直接 validate meta EAttribute。
    // EAttribute 的 eType 约束由 EcoreValidator.validateEClass 触发。
    EXPECT_TRUE(diags.empty());
}

EMF_TEST(EObjectValidator_ReferenceWithoutType) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* r = EcoreFactory::instance().createEReference();
    r->setName("foo");
    auto diags = EObjectValidator::validateEReference(r);
    // 对齐 Java：EObjectValidator 不直接 validate meta EReference。
    // EReference 的 eType 约束由 EcoreValidator.validateEClass 触发。
    EXPECT_TRUE(diags.empty());
}

EMF_TEST(EObjectValidator_ValidPackage) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* p = new EPackageImpl();
    p->setName("ok");
    p->setNsURI("http://x");
    p->setNsPrefix("x");
    auto* c = EcoreFactory::instance().createEClass();
    c->setName("Foo");
    p->addEClassifier(c);
    auto diags = EObjectValidator::validateEPackage(p);
    bool foundError = false;
    for (auto& d : diags) {
        if (d.severity() == Diagnostic::Severity::ERROR && d.message().find("name") != std::string::npos) {
            foundError = true;
        }
    }
    EXPECT_FALSE(foundError);
}
