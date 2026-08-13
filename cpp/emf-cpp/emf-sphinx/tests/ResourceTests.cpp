// Resource 测试
// 对齐 Java: org.eclipse.sphinx.emf.resource.SchemaLocationURIHandler
//          + org.eclipse.sphinx.emf.resource.ExtendedBasicExtendedMetaData
//          + org.eclipse.sphinx.emf.resource.ModelConverterRegistry
#include "test_main.h"
#include "emf/sphinx/resource/SchemaLocationURIHandler.h"
#include "emf/sphinx/resource/ExtendedBasicExtendedMetaData.h"
#include "emf/sphinx/resource/ModelConverterRegistry.h"
#include "emf/sphinx/resource/IModelConverter.h"
#include "emf/sphinx/metamodel/IMetaModelDescriptor.h"
#include "emf/xmi/XMIResource.h"

using emf::sphinx::resource::SchemaLocationURIHandler;
using emf::sphinx::resource::ExtendedBasicExtendedMetaData;
using emf::sphinx::resource::ModelConverterRegistry;
using emf::sphinx::resource::IModelConverter;
using emf::sphinx::metamodel::IMetaModelDescriptor;
using emf::xmi::XMIResource;

// 简单的两个 IMetaModelDescriptor 实现
class SrcDesc : public IMetaModelDescriptor {
public:
    std::string getIdentifier() const override { return "src"; }
    emf::common::URI getNamespaceURI() const override { return emf::common::URI("src"); }
    std::string getNamespace() const override { return "src"; }
    std::string getName() const override { return "Src"; }
    IMetaModelDescriptor* getBaseDescriptor() const override { return nullptr; }
    std::string getCustomURIScheme() const override { return ""; }
    int getOrdinal() const override { return 0; }
    std::string getEPackageNsURIPattern() const override { return "src"; }
    bool matchesNamespace(const std::string& ns) const override { return ns == "src"; }
    bool matchesEPackageNsURIPattern(const std::string& ns) const override { return ns == "src"; }
    bool equals(const IMetaModelDescriptor* o) const override { return o && o->getIdentifier() == "src"; }
    std::vector<IMetaModelDescriptor*> getCompatibleResourceVersionDescriptors() const override { return {}; }
};

class TgtDesc : public IMetaModelDescriptor {
public:
    std::string getIdentifier() const override { return "tgt"; }
    emf::common::URI getNamespaceURI() const override { return emf::common::URI("tgt"); }
    std::string getNamespace() const override { return "tgt"; }
    std::string getName() const override { return "Tgt"; }
    IMetaModelDescriptor* getBaseDescriptor() const override { return nullptr; }
    std::string getCustomURIScheme() const override { return ""; }
    int getOrdinal() const override { return 0; }
    std::string getEPackageNsURIPattern() const override { return "tgt"; }
    bool matchesNamespace(const std::string& ns) const override { return ns == "tgt"; }
    bool matchesEPackageNsURIPattern(const std::string& ns) const override { return ns == "tgt"; }
    bool equals(const IMetaModelDescriptor* o) const override { return o && o->getIdentifier() == "tgt"; }
    std::vector<IMetaModelDescriptor*> getCompatibleResourceVersionDescriptors() const override { return {}; }
};

// 一个 mock 的 IModelConverter，用于测试
class MockModelConverter : public IModelConverter {
public:
    MockModelConverter(const std::string& id, IMetaModelDescriptor* src, IMetaModelDescriptor* tgt)
        : id_(id), src_(src), tgt_(tgt) {}
    std::string getId() const override { return id_; }
    IMetaModelDescriptor* getSourceMetaModelDescriptor() const override { return src_; }
    IMetaModelDescriptor* getTargetMetaModelDescriptor() const override { return tgt_; }
    emf::common::Resource* convert(emf::common::Resource* source, const std::string&) override {
        return source;  // 简化：直接返回
    }
private:
    std::string id_;
    IMetaModelDescriptor* src_;
    IMetaModelDescriptor* tgt_;
};

// 测试 1：parseSchemaLocation 解析成对 ns + uri
EMF_TEST(SchemaLocationURIHandler_ParseSimple) {
    SchemaLocationURIHandler h;
    auto m = h.parseSchemaLocation("ns1 uri1 ns2 uri2");
    EXPECT_EQ((int)m.size(), 2);
    EXPECT_EQ(m["ns1"], std::string("uri1"));
    EXPECT_EQ(m["ns2"], std::string("uri2"));
}

// 测试 2：parseSchemaLocation 空字符串
EMF_TEST(SchemaLocationURIHandler_ParseEmpty) {
    SchemaLocationURIHandler h;
    auto m = h.parseSchemaLocation("");
    EXPECT_EQ((int)m.size(), 0);
}

// 测试 3：parseSchemaLocation 单个 ns
EMF_TEST(SchemaLocationURIHandler_ParseSingle) {
    SchemaLocationURIHandler h;
    auto m = h.parseSchemaLocation("ns1 uri1");
    EXPECT_EQ((int)m.size(), 1);
    EXPECT_EQ(m["ns1"], std::string("uri1"));
}

// 测试 4：parseSchemaLocation 奇数个 token 视为空
EMF_TEST(SchemaLocationURIHandler_ParseOddTokens) {
    SchemaLocationURIHandler h;
    auto m = h.parseSchemaLocation("ns1 uri1 ns2");
    EXPECT_EQ((int)m.size(), 0);
}

// 测试 5：parseSchemaLocation 多个空白
EMF_TEST(SchemaLocationURIHandler_ParseMultipleSpaces) {
    SchemaLocationURIHandler h;
    auto m = h.parseSchemaLocation("  ns1   uri1   ns2   uri2  ");
    EXPECT_EQ((int)m.size(), 2);
    EXPECT_EQ(m["ns1"], std::string("uri1"));
    EXPECT_EQ(m["ns2"], std::string("uri2"));
}

// 测试 6：getSchemaLocation(null) 返回空字符串
EMF_TEST(SchemaLocationURIHandler_GetNull) {
    SchemaLocationURIHandler h;
    EXPECT_EQ(h.getSchemaLocation(static_cast<const XMIResource*>(nullptr)), std::string(""));
}

// 测试 7：getSchemaLocation 从 XMIResource 取
EMF_TEST(SchemaLocationURIHandler_GetFromResource) {
    SchemaLocationURIHandler h;
    XMIResource r;
    r.setXSISchemaLocation("ns1 uri1");
    EXPECT_EQ(h.getSchemaLocation(&r), std::string("ns1 uri1"));
}

// 测试 8：ExtendedBasicExtendedMetaData.getCacheKey
EMF_TEST(ExtendedBasicExtendedMetaData_CacheKey) {
    auto& m = ExtendedBasicExtendedMetaData::instance();
    EXPECT_EQ(m.getCacheKey("ns", "loc"), std::string("ns|loc"));
}

// 测试 9：ExtendedBasicExtendedMetaData.getCacheKey 空 ns
EMF_TEST(ExtendedBasicExtendedMetaData_CacheKeyEmpty) {
    auto& m = ExtendedBasicExtendedMetaData::instance();
    EXPECT_EQ(m.getCacheKey("", "loc"), std::string("loc"));
}

// 测试 10：ModelConverterRegistry 注册
EMF_TEST(ModelConverterRegistry_AddAndFind) {
    auto& reg = ModelConverterRegistry::instance();
    auto* src = new SrcDesc();
    auto* tgt = new TgtDesc();
    auto* c = new MockModelConverter("c1", src, tgt);
    reg.addConverter(c);
    EXPECT_EQ((int)reg.getAllConverters().size(), 1);
    reg.removeConverter(c);
    delete c;
    delete src;
    delete tgt;
}

// 测试 11：ModelConverterRegistry 重复注册不增加
EMF_TEST(ModelConverterRegistry_NoDuplicate) {
    auto& reg = ModelConverterRegistry::instance();
    auto* src = new SrcDesc();
    auto* tgt = new TgtDesc();
    auto* c = new MockModelConverter("c1", src, tgt);
    reg.addConverter(c);
    reg.addConverter(c);
    EXPECT_EQ((int)reg.getAllConverters().size(), 1);
    reg.removeConverter(c);
    delete c;
    delete src;
    delete tgt;
}

// 测试 12：ModelConverterRegistry removeConverter null
EMF_TEST(ModelConverterRegistry_RemoveNull) {
    auto& reg = ModelConverterRegistry::instance();
    // 不会崩溃
    reg.removeConverter(nullptr);
}

// 测试 13：ModelConverterRegistry findConverter 匹配 src/tgt
EMF_TEST(ModelConverterRegistry_FindByMetamodels) {
    auto& reg = ModelConverterRegistry::instance();
    auto* src = new SrcDesc();
    auto* tgt = new TgtDesc();
    auto* c = new MockModelConverter("c1", src, tgt);
    reg.addConverter(c);
    EXPECT_TRUE(reg.findConverter("src", "tgt") == c);
    EXPECT_NULL(reg.findConverter("tgt", "src"));  // 方向不同
    EXPECT_NULL(reg.findConverter("xxx", "yyy"));
    reg.removeConverter(c);
    delete c;
    delete src;
    delete tgt;
}
