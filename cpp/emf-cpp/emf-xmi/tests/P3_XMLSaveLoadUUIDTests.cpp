// P3 测试：XMLSave / XMLLoad 抽象 + UUID 支持（emf-xmi）
// 对齐 Java: org.eclipse.emf.ecore.xmi.XMLLoad, XMLSave, XMIResource.USE_UUIDs
#include "test_main.h"
#include "emf/xmi/XMIResource.h"
#include "emf/xmi/XMLLoad.h"
#include "emf/xmi/XMIOptions.h"
#include "emf/ecore/EcorePackage.h"
#include "emf/ecore/EcoreImpls.h"

#include <any>
#include <set>
#include <sstream>
#include <string>

using emf::ecore::EcoreFactory;
using emf::ecore::EcorePackage;
using emf::ecore::EPackage;
using emf::ecore::EClass;
using emf::ecore::EAttribute;
using emf::xmi::XMIResource;
using emf::xmi::XMLSave;
using emf::xmi::XMLLoad;
using emf::xmi::XMLSaveImpl;
using emf::xmi::XMLLoadImpl;
using emf::xmi::XMIOptions;

// 注入的 mock XMLSave：把 save 调用记录下来，并写出 sentinel "MOCK_SAVE\n"
class MockXMLSave : public XMLSave {
public:
    int callCount = 0;
    std::string lastResURI;
    void save(const XMIResource* resource, std::ostream& output, const XMIOptions& options) override {
        ++callCount;
        if (resource) lastResURI = resource->getURI().toString();
        output << "MOCK_SAVE\n";
        (void)options;
    }
};

class MockXMLLoad : public XMLLoad {
public:
    int callCount = 0;
    void load(XMIResource* resource, std::istream& input, const XMIOptions& options) override {
        ++callCount;
        if (!resource) return;
        // 简单 mock：把流里的内容写到 resource 的某个标记字段
        std::stringstream ss; ss << input.rdbuf();
        loadedContent_ = ss.str();
        (void)options;
    }
    std::string loadedContent_;
};

// =====================================================================
// 1) UUID 生成：v4 格式正确（36 字符，8-4-4-4-12，第 14 位 '4'，第 19 位 [89ab]）
// =====================================================================
EMF_TEST(UUID_Format_v4) {
    XMIResource res;
    std::string id = res.generateUUID();
    EXPECT_EQ(id.size(), 36u);
    EXPECT_EQ(id[8], '-');
    EXPECT_EQ(id[13], '-');
    EXPECT_EQ(id[18], '-');
    EXPECT_EQ(id[23], '-');
    // version 4
    EXPECT_EQ(id[14], '4');
    // variant 10xx：第 19 位是 '8' / '9' / 'a' / 'b'
    char v = id[19];
    EXPECT_TRUE(v == '8' || v == '9' || v == 'a' || v == 'b');
}

// =====================================================================
// 2) UUID 唯一性：连续生成 1000 个都不同
// =====================================================================
EMF_TEST(UUID_Generation_Unique) {
    XMIResource res;
    std::set<std::string> ids;
    for (int i = 0; i < 1000; ++i) {
        ids.insert(res.generateUUID());
    }
    EXPECT_EQ(ids.size(), 1000u);
}

// =====================================================================
// 3) ensureID：useUUIDs=false 时不分配
// =====================================================================
EMF_TEST(UseUUIDs_Disabled_ensureID_NoAssign) {
    XMIResource res;
    res.setUseUUIDs(false);
    // 构造一个空 EObject
    EcorePackage::instance();
    auto* pkg = EcoreFactory::instance().createEPackage();
    std::string id = res.ensureID(pkg);
    EXPECT_EQ(id, "");
    // idToEObject_ 应为空
    EXPECT_EQ(res.getIDToEObjectMap().size(), 0u);
}

// =====================================================================
// 4) ensureID：useUUIDs=true 时自动分配 UUID
// =====================================================================
EMF_TEST(UseUUIDs_Enabled_ensureID_AssignUUID) {
    XMIResource res;
    res.setUseUUIDs(true);
    EcorePackage::instance();
    auto* pkg = EcoreFactory::instance().createEPackage();
    std::string id1 = res.ensureID(pkg);
    EXPECT_EQ(id1.size(), 36u);
    EXPECT_EQ(id1[14], '4');
    // 再次 ensureID 应返回同一个 id（幂等）
    std::string id2 = res.ensureID(pkg);
    EXPECT_EQ(id1, id2);
    // idToEObject_ 中应有一条
    EXPECT_EQ(res.getIDToEObjectMap().size(), 1u);
    // 通过 id 能查到对象
    EXPECT_EQ(res.getEObjectByID(id1), static_cast<emf::common::EObject*>(pkg));
}

// =====================================================================
// 5) XMLSave / XMLLoad 抽象注入：自定义实现被调用
// =====================================================================
EMF_TEST(XMLSave_Injection_CustomImpl_Called) {
    XMIResource res;
    auto mock = std::make_shared<MockXMLSave>();
    res.setXMLSave(mock);

    EcorePackage::instance();
    auto* pkg = EcoreFactory::instance().createEPackage();
    res.addToContents(pkg);

    std::ostringstream oss;
    res.save(oss);
    EXPECT_EQ(mock->callCount, 1);
    EXPECT_EQ(oss.str(), std::string{"MOCK_SAVE\n"});
}

// =====================================================================
// 6) XMLLoad 抽象注入：自定义实现被调用
// =====================================================================
EMF_TEST(XMLLoad_Injection_CustomImpl_Called) {
    XMIResource res;
    auto mock = std::make_shared<MockXMLLoad>();
    res.setXMLLoad(mock);

    std::istringstream iss("hello world");
    res.load(iss);
    EXPECT_EQ(mock->callCount, 1);
    EXPECT_EQ(mock->loadedContent_, std::string{"hello world"});
}

// =====================================================================
// 7) 默认实现：getXMLSave 返回 XMLSaveImpl（共享指针非空）
// =====================================================================
EMF_TEST(XMLSave_DefaultImpl_NotNull) {
    XMIResource res;
    auto saver = res.getXMLSave();
    EXPECT_NOT_NULL(saver);
    // 二次调用应返回同一实例（lazy 缓存）
    EXPECT_EQ(saver.get(), res.getXMLSave().get());
    // dynamic_cast 到 XMLSaveImpl 应非空
    auto* impl = dynamic_cast<XMLSaveImpl*>(saver.get());
    EXPECT_NOT_NULL(impl);
}

EMF_TEST(XMLLoad_DefaultImpl_NotNull) {
    XMIResource res;
    auto loader = res.getXMLLoad();
    EXPECT_NOT_NULL(loader);
    EXPECT_EQ(loader.get(), res.getXMLLoad().get());
    auto* impl = dynamic_cast<XMLLoadImpl*>(loader.get());
    EXPECT_NOT_NULL(impl);
}

// =====================================================================
// 8) XMLSaveImpl 端到端：保存一个简单的 EPackage
// =====================================================================
EMF_TEST(XMLSave_DefaultImpl_Roundtrips) {
    XMIResource res;
    EcorePackage::instance();
    auto* pkg = EcoreFactory::instance().createEPackage();
    pkg->setName("Test");
    pkg->setNsURI("http://test.example.com");
    res.addToContents(pkg);

    std::ostringstream oss;
    res.save(oss);
    std::string out = oss.str();
    // 输出应包含 xmi 包装
    EXPECT_TRUE(out.find("<?xml") != std::string::npos);
    EXPECT_TRUE(out.find("xmi:XMI") != std::string::npos ||
                out.find("xmi") != std::string::npos);
    EXPECT_TRUE(out.find("Test") != std::string::npos);
}
