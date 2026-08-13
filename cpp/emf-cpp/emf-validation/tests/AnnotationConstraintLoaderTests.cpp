// AnnotationConstraintLoader 单元测试
// V2/V3 回归：从 EClass 的 OCL/Constraints annotation 加载约束并求值
#include "test_main.h"
#include "emf/validation/AnnotationConstraintLoader.h"
#include "emf/validation/EValidator.h"
#include "emf/ecore/EcorePackage.h"
#include "emf/ecore/EcoreImpls.h"

using emf::ecore::EcoreFactory;
using emf::ecore::EcorePackage;
using emf::ecore::EClass;
using emf::ecore::EAttribute;
using emf::ecore::EAnnotation;
using emf::validation::EValidator;
using emf::validation::AnnotationConstraintLoader;
using emf::validation::Severity;

namespace {

// 构造一个 EClass，加 OCL annotation：name != ''（name 非空约束）
EClass* makeClassWithOclNameConstraint() {
    auto* cls = EcoreFactory::instance().createEClass();
    cls->setName("Foo");
    // 加 OCL annotation
    auto* ann = EcoreFactory::instance().createEAnnotation();
    ann->setSource(AnnotationConstraintLoader::OCL_SOURCE);
    ann->setDetail("name_nonempty", "name != ''");
    cls->addEAnnotation(ann);
    return cls;
}

}  // namespace

// V2: OCL annotation 加载——约束数正确
EMF_TEST(AnnotationLoader_LoadOcl_RegistersConstraints) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* cls = makeClassWithOclNameConstraint();
    EValidator v;
    int n = AnnotationConstraintLoader::loadOclConstraints(v, cls);
    EXPECT_EQ(n, 1);
    EXPECT_NOT_NULL(v.getConstraint(std::string(AnnotationConstraintLoader::OCL_SOURCE) + "#name_nonempty"));
}

// V2: OCL 约束求值——name 非空时通过
EMF_TEST(AnnotationLoader_OclConstraint_NameNonEmpty_Passes) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* cls = makeClassWithOclNameConstraint();
    cls->setName("valid");  // name 非空
    EValidator v;
    AnnotationConstraintLoader::loadOclConstraints(v, cls);
    auto diags = v.validate(cls);
    // name 非空，约束通过，无 diagnostic
    EXPECT_EQ(diags.size(), 0u);
}

// V2: OCL 约束求值——name 为空时报 ERROR
EMF_TEST(AnnotationLoader_OclConstraint_NameEmpty_Fails) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* cls = makeClassWithOclNameConstraint();
    cls->setName("");  // name 为空，违反约束
    EValidator v;
    AnnotationConstraintLoader::loadOclConstraints(v, cls);
    auto diags = v.validate(cls);
    EXPECT_EQ(diags.size(), 1u);
    EXPECT_TRUE(diags[0].severity() == emf::common::Diagnostic::Severity::ERROR);
}

// V3: Named-constraints annotation + OCL annotation 配合
EMF_TEST(AnnotationLoader_NamedConstraints_LoadsMatchingOcl) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* cls = EcoreFactory::instance().createEClass();
    cls->setName("Foo");
    // OCL annotation 提供两个 invariant 表达式
    auto* oclAnn = EcoreFactory::instance().createEAnnotation();
    oclAnn->setSource(AnnotationConstraintLoader::OCL_SOURCE);
    oclAnn->setDetail("inv1", "name != ''");
    oclAnn->setDetail("inv2", "name != null");
    cls->addEAnnotation(oclAnn);
    // Constraints annotation 声明只执行 inv1（不执行 inv2）
    auto* consAnn = EcoreFactory::instance().createEAnnotation();
    consAnn->setSource(AnnotationConstraintLoader::CONSTRAINTS_SOURCE);
    consAnn->setDetail("Foo", "inv1");
    cls->addEAnnotation(consAnn);

    EValidator v;
    int n = AnnotationConstraintLoader::loadNamedConstraints(v, cls);
    EXPECT_EQ(n, 1);  // 只加载 inv1
    EXPECT_NOT_NULL(v.getConstraint(std::string(AnnotationConstraintLoader::CONSTRAINTS_SOURCE) + "#inv1"));
    EXPECT_NULL(v.getConstraint(std::string(AnnotationConstraintLoader::CONSTRAINTS_SOURCE) + "#inv2"));
}

// V2: 无 annotation 时加载 0 个约束
EMF_TEST(AnnotationLoader_NoAnnotation_LoadsZero) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* cls = EcoreFactory::instance().createEClass();
    cls->setName("Foo");
    EValidator v;
    int n = AnnotationConstraintLoader::loadAll(v, cls);
    EXPECT_EQ(n, 0);
}
