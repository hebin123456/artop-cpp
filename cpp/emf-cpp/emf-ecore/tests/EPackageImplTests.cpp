// EPackageImpl 单元测试
#include "test_main.h"
#include "emf/ecore/EcorePackage.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/common/EPackageRegistry.h"

using emf::ecore::EcoreFactory;
using emf::ecore::EcorePackage;
using emf::ecore::EPackage;
using emf::ecore::EClass;
using emf::common::EPackageRegistry;

EMF_TEST(EPackageImpl_CreateAndGetClassifier) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* p = EcoreFactory::instance().createEPackage();
    p->setName("MyPkg");
    p->setNsURI("http://example.com/MyPkg");
    p->setNsPrefix("mypkg");

    auto* c1 = EcoreFactory::instance().createEClass();
    c1->setName("Alpha");
    auto* c2 = EcoreFactory::instance().createEClass();
    c2->setName("Beta");

    p->addEClassifier(c1);
    p->addEClassifier(c2);

    EXPECT_EQ(p->getEClassifiers().size(), (size_t)2);
    EXPECT_EQ(p->getEClassifier("Alpha"), static_cast<emf::ecore::EClassifier*>(c1));
    EXPECT_EQ(p->getEClassifier("Beta"), static_cast<emf::ecore::EClassifier*>(c2));
    EXPECT_NULL(p->getEClassifier("NotThere"));
}

EMF_TEST(EPackageImpl_RegisteredInGlobalRegistry) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* p = EcoreFactory::instance().createEPackage();
    p->setName("TestPkg");
    p->setNsURI("http://example.com/TestPkg-zzz");
    p->setNsPrefix("tp");
    EPackageRegistry::instance().put("http://example.com/TestPkg-zzz", p);
    auto* got = EPackageRegistry::instance().get("http://example.com/TestPkg-zzz");
    EXPECT_EQ(got, p);
    EPackageRegistry::instance().remove("http://example.com/TestPkg-zzz");
}

EMF_TEST(EPackageImpl_Accessors) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* p = EcoreFactory::instance().createEPackage();
    p->setName("X");
    p->setNsURI("u");
    p->setNsPrefix("x");
    EXPECT_EQ(p->getName(), std::string("X"));
    EXPECT_EQ(p->getNsURI(), std::string("u"));
    EXPECT_EQ(p->getNsPrefix(), std::string("x"));
}
