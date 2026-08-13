// EcorePackage 单元测试
#include "test_main.h"
#include "emf/ecore/EcorePackage.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/common/EPackageRegistry.h"

using emf::ecore::EcoreFactory;
using emf::ecore::EcorePackage;
using emf::common::EPackageRegistry;

EMF_TEST(EcorePackage_Initialize) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    // 不应抛异常
    auto& pkg = EcorePackage::instance();
    EXPECT_NOT_NULL(pkg.getEPackage());
    EXPECT_NOT_NULL(pkg.getEFactory());
}

EMF_TEST(EcorePackage_MetaEClassesNonNull) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto& p = EcorePackage::instance();
    EXPECT_NOT_NULL(p.getEClass_EClass());
    EXPECT_NOT_NULL(p.getEClass_EAttribute());
    EXPECT_NOT_NULL(p.getEClass_EReference());
    EXPECT_NOT_NULL(p.getEClass_EPackage());
    EXPECT_NOT_NULL(p.getEClass_EEnum());
    EXPECT_NOT_NULL(p.getEClass_EDataType());
    EXPECT_EQ(p.getEClass_EClass()->getName(), std::string("EClass"));
    EXPECT_EQ(p.getEClass_EAttribute()->getName(), std::string("EAttribute"));
    EXPECT_EQ(p.getEClass_EReference()->getName(), std::string("EReference"));
}

EMF_TEST(EcorePackage_BuiltInDataTypesNonNull) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto& p = EcorePackage::instance();
    EXPECT_NOT_NULL(p.getEDataType_EString());
    EXPECT_NOT_NULL(p.getEDataType_EBoolean());
    EXPECT_NOT_NULL(p.getEDataType_EInt());
    EXPECT_EQ(p.getEDataType_EString()->getName(), std::string("EString"));
    EXPECT_EQ(p.getEDataType_EInt()->getName(), std::string("EInt"));
    EXPECT_EQ(p.getEDataType_EBoolean()->getName(), std::string("EBoolean"));
}

EMF_TEST(EcorePackage_RegisteredToGlobalRegistry) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* p = EPackageRegistry::instance().get("http://www.eclipse.org/emf/2002/Ecore");
    EXPECT_NOT_NULL(p);
    EXPECT_EQ(p, EcorePackage::instance().getEPackage());
}

EMF_TEST(EcorePackage_NamespaceConstants) {
    EXPECT_EQ(std::string(EcorePackage::eNS_URI),
              std::string("http://www.eclipse.org/emf/2002/Ecore"));
    EXPECT_EQ(std::string(EcorePackage::eNS_PREFIX), std::string("ecore"));
    EXPECT_EQ(std::string(EcorePackage::eNAME), std::string("ecore"));
}

EMF_TEST(EcorePackage_FeatureIDConstants) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto& p = EcorePackage::instance();
    // 方案 3 使用全局 FeatureID 命名空间（与 Java EMF 内部 per-class 编号不同）
    EXPECT_EQ(p.getFeatureID_EClass_eSuperTypes(),
              static_cast<int>(::emf::common::FeatureID::ECLASS_ESUPERTYPES));
    EXPECT_EQ(p.getFeatureID_EClass_eStructuralFeatures(),
              static_cast<int>(::emf::common::FeatureID::ECLASS_ESTRUCTURALFEATURES));
    EXPECT_EQ(p.getFeatureID_EPackage_eClassifiers(),
              static_cast<int>(::emf::common::FeatureID::EPACKAGE_ECLASSIFIERS));
    EXPECT_EQ(p.getFeatureID_EPackage_eNsURI(),
              static_cast<int>(::emf::common::FeatureID::EPACKAGE_ENSURI));
}
