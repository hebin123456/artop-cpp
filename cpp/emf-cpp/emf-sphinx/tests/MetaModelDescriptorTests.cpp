// MetaModelDescriptor 测试
// 对齐 Java: org.eclipse.sphinx.emf.metamodel.AbstractMetaModelDescriptor
//          + org.eclipse.sphinx.emf.metamodel.MetaModelDescriptorRegistry
//          + org.eclipse.sphinx.emf.metamodel.MetaModelVersionData
#include "test_main.h"
#include "emf/sphinx/metamodel/AbstractMetaModelDescriptor.h"
#include "emf/sphinx/metamodel/MetaModelDescriptorRegistry.h"
#include "emf/sphinx/metamodel/MetaModelVersionData.h"
#include "emf/sphinx/metamodel/IMetaModelDescriptor.h"
#include "emf/common/Resource.h"
#include "emf/common/EObject.h"
#include "emf/common/EPackage.h"
#include "emf/ecore/EcoreImpls.h"

using emf::sphinx::metamodel::AbstractMetaModelDescriptor;
using emf::sphinx::metamodel::IMetaModelDescriptor;
using emf::sphinx::metamodel::MetaModelDescriptorRegistry;
using emf::sphinx::metamodel::MetaModelVersionData;
using emf::ecore::EPackageImpl;
using emf::common::Resource;
using emf::common::URI;
using emf::common::EObject;

// 测试 1：三参构造 / namespace 拼接
EMF_TEST(MetaModelDescriptor_Basic3Arg) {
    AbstractMetaModelDescriptor d("urn:my.mm", "urn:my.mm", "MyMetaModel");
    EXPECT_EQ(d.getIdentifier(), std::string("urn:my.mm"));
    EXPECT_EQ(d.getName(), std::string("MyMetaModel"));
    EXPECT_EQ(d.getNamespace(), std::string("urn:my.mm"));
}

// 测试 2：四参构造（多 EPackage）
EMF_TEST(MetaModelDescriptor_MultiEPackage) {
    AbstractMetaModelDescriptor d("urn:my.mm", "urn:my.mm", "v[0-9]+", "MyMetaModel");
    EXPECT_EQ(d.getEPackageNsURIPattern(), std::string("urn:my.mm/v[0-9]+"));
}

// 测试 3：带 version 构造
EMF_TEST(MetaModelDescriptor_WithVersion) {
    MetaModelVersionData vd("v1", "v1", "Version1");
    AbstractMetaModelDescriptor d("urn:my.mm", "urn:my.mm", vd);
    EXPECT_EQ(d.getNamespace(), std::string("urn:my.mm/v1"));
    EXPECT_EQ(d.getName(), std::string("Version1"));
}

// 测试 4：matchesNamespace
EMF_TEST(MetaModelDescriptor_MatchesNamespace) {
    AbstractMetaModelDescriptor d("urn:my.mm", "urn:my.mm", "MyMetaModel");
    EXPECT_TRUE(d.matchesNamespace("urn:my.mm"));
    EXPECT_FALSE(d.matchesNamespace("urn:other.mm"));
}

// 测试 5：matchesEPackageNsURIPattern
EMF_TEST(MetaModelDescriptor_MatchesEPackagePattern) {
    AbstractMetaModelDescriptor d("urn:my.mm", "urn:my.mm", "v[0-9]+", "MyMetaModel");
    EXPECT_TRUE(d.matchesEPackageNsURIPattern("urn:my.mm/v1"));
    EXPECT_TRUE(d.matchesEPackageNsURIPattern("urn:my.mm/v42"));
    EXPECT_FALSE(d.matchesEPackageNsURIPattern("urn:my.mm/stable"));
    EXPECT_FALSE(d.matchesEPackageNsURIPattern("urn:other.mm/v1"));
}

// 测试 6：equals 按 identifier
EMF_TEST(MetaModelDescriptor_Equals) {
    AbstractMetaModelDescriptor a("urn:my.mm", "urn:my.mm", "A");
    AbstractMetaModelDescriptor b("urn:my.mm", "urn:other.mm", "B");  // 不同 namespace 但同 identifier
    EXPECT_TRUE(a.equals(&b));
    AbstractMetaModelDescriptor c("urn:other", "urn:other", "C");
    EXPECT_FALSE(a.equals(&c));
    EXPECT_FALSE(a.equals(nullptr));
}

// 测试 7：hashCode
EMF_TEST(MetaModelDescriptor_HashCode) {
    AbstractMetaModelDescriptor a("urn:my.mm", "urn:my.mm", "A");
    AbstractMetaModelDescriptor b("urn:my.mm", "urn:other", "B");
    EXPECT_EQ(a.hashCode(), b.hashCode());  // 同 identifier 同 hash
}

// 测试 8：ordinal
EMF_TEST(MetaModelDescriptor_Ordinal) {
    MetaModelVersionData vd("v1", "v1", "v1", 5);
    AbstractMetaModelDescriptor d("urn:my.mm", "urn:my.mm", vd);
    EXPECT_EQ(d.getOrdinal(), 5);
}

// 测试 9：compatible descriptors
EMF_TEST(MetaModelDescriptor_Compatible) {
    AbstractMetaModelDescriptor d("urn:my.mm", "urn:my.mm", "X");
    AbstractMetaModelDescriptor c1("urn:my.mm/v1", "urn:my.mm", "v1", "v1");
    AbstractMetaModelDescriptor c2("urn:my.mm/v2", "urn:my.mm", "v2", "v2");
    d.addCompatibleResourceVersionDescriptor(&c1);
    d.addCompatibleResourceVersionDescriptor(&c2);
    EXPECT_EQ((int)d.getCompatibleResourceVersionDescriptors().size(), 2);
}

// 测试 10：setVersionData 同步 namespace
EMF_TEST(MetaModelDescriptor_SetVersionData) {
    AbstractMetaModelDescriptor d("urn:my.mm", "urn:my.mm", "X");
    EXPECT_EQ(d.getNamespace(), std::string("urn:my.mm"));
    MetaModelVersionData vd("v2", "v2", "v2");
    d.setVersionData(vd);
    EXPECT_EQ(d.getNamespace(), std::string("urn:my.mm/v2"));
    EXPECT_EQ(d.getName(), std::string("v2"));
}

// 测试 11：MetaModelVersionData 字段
EMF_TEST(MetaModelVersionData_Fields) {
    MetaModelVersionData vd("post", "v[0-9]+", "Ver1");
    EXPECT_EQ(vd.getNsPostfix(), std::string("post"));
    EXPECT_EQ(vd.getEPackageNsURIPostfixPattern(), std::string("v[0-9]+"));
    EXPECT_EQ(vd.getName(), std::string("Ver1"));
    EXPECT_EQ(vd.getOrdinal(), -1);  // 默认
}

// 测试 12：MetaModelVersionData equals
EMF_TEST(MetaModelVersionData_Equals) {
    MetaModelVersionData a("v1", "v1", "v1");
    MetaModelVersionData b("v1", "v1", "v1");
    MetaModelVersionData c("v2", "v2", "v2");
    EXPECT_TRUE(a.equals(b));
    EXPECT_FALSE(a.equals(c));
}

// 测试 13：Registry 注册/查找
EMF_TEST(MetaModelDescriptorRegistry_RegisterAndLookup) {
    auto& reg = MetaModelDescriptorRegistry::instance();
    reg.clear();
    auto* d = new AbstractMetaModelDescriptor("urn:my.mm", "urn:my.mm", "MyMetaModel");
    reg.registerDescriptor(d);
    EXPECT_TRUE(reg.getDescriptor("urn:my.mm") == d);
    EXPECT_TRUE(reg.getDescriptor(URI("urn:my.mm")) == d);
    reg.unregisterDescriptor(d);
    EXPECT_NULL(reg.getDescriptor("urn:my.mm"));
    delete d;
}

// 测试 14：Registry 通过 EObject 查找
EMF_TEST(MetaModelDescriptorRegistry_LookupByObject) {
    auto& reg = MetaModelDescriptorRegistry::instance();
    reg.clear();
    auto* d = new AbstractMetaModelDescriptor("urn:my.mm", "urn:my.mm", "MyMetaModel");
    reg.registerDescriptor(d);
    // 通过 null EObject 应该返回 null
    EXPECT_NULL(reg.getDescriptor(static_cast<EObject*>(nullptr)));
    EXPECT_NULL(reg.getDescriptor(static_cast<emf::common::Resource*>(nullptr)));
    // 找 EObject 但 eClass==null 的对象：返回 null
    // （没法直接构造 eClass==null 的 EObject，故只检查 null 入参）
    reg.unregisterDescriptor(d);
    delete d;
}

// 测试 15：Registry 重复注册不增加
EMF_TEST(MetaModelDescriptorRegistry_NoDuplicate) {
    auto& reg = MetaModelDescriptorRegistry::instance();
    reg.clear();
    auto* d = new AbstractMetaModelDescriptor("urn:my.mm", "urn:my.mm", "X");
    reg.registerDescriptor(d);
    reg.registerDescriptor(d);
    EXPECT_EQ((int)reg.getAll().size(), 1);
    reg.unregisterDescriptor(d);
    delete d;
}

// 测试 16：Registry 多个 descriptor
EMF_TEST(MetaModelDescriptorRegistry_Multiple) {
    auto& reg = MetaModelDescriptorRegistry::instance();
    reg.clear();
    auto* d1 = new AbstractMetaModelDescriptor("urn:a", "urn:a", "A");
    auto* d2 = new AbstractMetaModelDescriptor("urn:b", "urn:b", "B");
    reg.registerDescriptor(d1);
    reg.registerDescriptor(d2);
    EXPECT_EQ((int)reg.getAll().size(), 2);
    EXPECT_EQ((int)reg.keys().size(), 2);
    reg.clear();
    delete d1;
    delete d2;
}

// 测试 17：Target / Old descriptor 注册
EMF_TEST(MetaModelDescriptorRegistry_TargetAndOld) {
    auto& reg = MetaModelDescriptorRegistry::instance();
    reg.clear();
    auto* t = new AbstractMetaModelDescriptor("urn:target", "urn:target", "T");
    auto* o = new AbstractMetaModelDescriptor("urn:old", "urn:old", "O");
    reg.registerDescriptor(t);
    reg.registerDescriptor(o);
    EXPECT_TRUE(reg.getTargetDescriptor(URI("urn:target")) == t);
    EXPECT_TRUE(reg.getOldDescriptor(URI("urn:old")) == o);
    reg.clear();
    delete t;
    delete o;
}

// 测试 18：Registry getDescriptor(Resource) 通过 contents 第一个对象
EMF_TEST(MetaModelDescriptorRegistry_LookupByResource) {
    auto& reg = MetaModelDescriptorRegistry::instance();
    reg.clear();
    auto* d = new AbstractMetaModelDescriptor("urn:my.mm", "urn:my.mm", "X");
    reg.registerDescriptor(d);
    Resource r(URI("file:///tmp/x.xmi"));
    EXPECT_NULL(reg.getDescriptor(&r));  // empty contents
    reg.clear();
    delete d;
}
