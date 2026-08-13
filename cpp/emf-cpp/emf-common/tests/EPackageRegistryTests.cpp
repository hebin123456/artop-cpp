// EPackageRegistry 单元测试
#include "test_main.h"
#include "emf/common/EPackageRegistry.h"
#include "emf/ecore/EcorePackage.h"
#include "emf/ecore/EcoreImpls.h"

using emf::common::EPackageRegistry;
using emf::ecore::EcoreFactory;
using emf::ecore::EcorePackage;
using emf::ecore::EPackage;
using emf::ecore::EClass;

static EPackage* toEPkg(emf::common::EPackage* p) { return dynamic_cast<EPackage*>(p); }

EMF_TEST(EPackageRegistry_PutGetByNsURI) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto& reg = EPackageRegistry::instance();
    // EcorePackage 已注册到 nsURI
    EPackage* p = toEPkg(reg.get("http://www.eclipse.org/emf/2002/Ecore"));
    EXPECT_NOT_NULL(p);
    EXPECT_EQ(p->getNsURI(), std::string("http://www.eclipse.org/emf/2002/Ecore"));
}

EMF_TEST(EPackageRegistry_GetByName) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto& reg = EPackageRegistry::instance();
    EPackage* p = toEPkg(reg.get("ecore"));
    EXPECT_NOT_NULL(p);
    EXPECT_EQ(p->getName(), std::string("ecore"));
}

EMF_TEST(EPackageRegistry_GetByPrefix) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto& reg = EPackageRegistry::instance();
    EPackage* p = toEPkg(reg.get("ecore"));  // prefix 与 name 相同
    EXPECT_NOT_NULL(p);
    EXPECT_EQ(p->getNsPrefix(), std::string("ecore"));
}

EMF_TEST(EPackageRegistry_ContainsKey) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto& reg = EPackageRegistry::instance();
    EXPECT_TRUE(reg.containsKey("ecore"));
    EXPECT_TRUE(reg.containsKey("http://www.eclipse.org/emf/2002/Ecore"));
    EXPECT_FALSE(reg.containsKey("non-existent-nsuri-12345"));
}

EMF_TEST(EPackageRegistry_PutCustom) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto& reg = EPackageRegistry::instance();
    auto* p = EcoreFactory::instance().createEPackage();
    p->setName("test-pkg");
    p->setNsURI("http://example.com/test-pkg");
    p->setNsPrefix("tp");
    reg.put("http://example.com/test-pkg", p);
    EXPECT_TRUE(reg.containsKey("http://example.com/test-pkg"));
    EPackage* p2 = toEPkg(reg.get("http://example.com/test-pkg"));
    EXPECT_EQ(p, p2);
    // 清理
    reg.remove("http://example.com/test-pkg");
}

EMF_TEST(EPackageRegistry_KeysIncludesEcore) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto& reg = EPackageRegistry::instance();
    auto keys = reg.keys();
    bool hasEcore = false;
    for (auto& k : keys) {
        if (k == "http://www.eclipse.org/emf/2002/Ecore") { hasEcore = true; break; }
    }
    EXPECT_TRUE(hasEcore);
}

EMF_TEST(EPackageRegistry_NotFoundReturnsNull) {
    auto& reg = EPackageRegistry::instance();
    EXPECT_NULL(reg.get("definitely-not-a-real-key-xyz"));
}
