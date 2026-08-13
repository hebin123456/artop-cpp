// emf-artop-runtime 测试主入口
// 对齐 Java: 单元测试 + 集成测试
#include "test_main.h"

#include "emf/artop/runtime/AutosarMetaModelVersionData.h"
#include "emf/artop/runtime/AutosarReleaseDescriptor.h"
#include "emf/artop/runtime/AutosarResource.h"
#include "emf/artop/runtime/AutosarResourceFactory.h"
#include "emf/artop/runtime/IdentifiableUtil.h"

#include "emf/ecore/EcorePackage.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/common/EPackageRegistry.h"
#include "emf/xmi/XMIResource.h"
#include "emf/xmi/XMIResourceSet.h"
#include "emf/xmi/XMIResourceFactory.h"
#include "emf/common/EList.h"

#include <cstdio>
#include <string>
#include <memory>
#include <sstream>

namespace emf::artop::runtime::test {

// 用例 1: AutosarMetaModelVersionData 基础构造
bool test_version_data_basic() {
    AutosarMetaModelVersionData v(4, 4, 8);
    EMF_ASSERT_EQ(v.getMajor(), 4);
    EMF_ASSERT_EQ(v.getMinor(), 4);
    EMF_ASSERT_EQ(v.getRevision(), 8);
    EMF_ASSERT(v.isNewVersion());
    EMF_ASSERT(!v.isNewVersion() == false);
    EMF_ASSERT_EQ(v.toString(), std::string("4.4.8"));
    return true;
}

// 用例 2: 从字符串解析
bool test_version_data_parse() {
    auto v = AutosarMetaModelVersionData::createFromCanonicalVersionNumberString("4.4.8");
    EMF_ASSERT_EQ(v.getMajor(), 4);
    EMF_ASSERT_EQ(v.getMinor(), 4);
    EMF_ASSERT_EQ(v.getRevision(), 8);

    auto v2 = AutosarMetaModelVersionData::createFromCanonicalVersionNumberString("4.2.1");
    EMF_ASSERT_EQ(v2.getMajor(), 4);
    EMF_ASSERT(!v2.isNewVersion());

    // 部分版本
    auto v3 = AutosarMetaModelVersionData::createFromCanonicalVersionNumberString("4.0");
    EMF_ASSERT_EQ(v3.getMajor(), 4);
    EMF_ASSERT_EQ(v3.getMinor(), 0);
    EMF_ASSERT_EQ(v3.getRevision(), 0);
    return true;
}

// 用例 3: canonical number 编/解码
bool test_version_canonical() {
    AutosarMetaModelVersionData v(4, 4, 8);
    int cn = v.getCanonicalVersionNumber();
    EMF_ASSERT(cn != 0);
    return true;
}

// 用例 4: schema version number string
bool test_schema_version_string() {
    AutosarMetaModelVersionData v448(4, 4, 8);
    auto s_new = v448.getSchemaVersionNumberString("-");
    EMF_ASSERT_EQ(s_new, std::string("4-4-8"));

    AutosarMetaModelVersionData v421(4, 2, 1);
    auto s_old = v421.getSchemaVersionNumberString("");
    EMF_ASSERT_EQ(s_old, std::string("00042"));
    return true;
}

// 用例 5: AutosarReleaseDescriptor 基础属性
bool test_release_descriptor_basic() {
    auto desc = std::make_shared<AutosarReleaseDescriptor>(
        "org.artop.aal.autosar448", AutosarMetaModelVersionData(4, 4, 8));
    EMF_ASSERT_EQ(desc->getId(), std::string("org.artop.aal.autosar448"));
    EMF_ASSERT_EQ(desc->getAutosarVersionData().getMajor(), 4);
    EMF_ASSERT_EQ(desc->getBaseNamespace(), std::string("http://autosar.org/schema/r4.0"));
    return true;
}

// 用例 6: schema location 拼接
bool test_release_schema_location() {
    auto desc = std::make_shared<AutosarReleaseDescriptor>(
        "org.artop.aal.autosar448", AutosarMetaModelVersionData(4, 4, 8));
    auto sl = desc->getSchemaLocation();
    // "http://autosar.org/schema/r4.0 AUTOSAR_4-4-8.xsd"
    EMF_ASSERT(sl.find("http://autosar.org/schema/r4.0") != std::string::npos);
    EMF_ASSERT(sl.find("AUTOSAR_4-4-8.xsd") != std::string::npos);

    auto desc40 = std::make_shared<AutosarReleaseDescriptor>(
        "org.artop.aal.autosar40", AutosarMetaModelVersionData(4, 2, 1));
    auto sl40 = desc40->getSchemaLocation();
    EMF_ASSERT(sl40.find("AUTOSAR_00042.xsd") != std::string::npos);
    return true;
}

// 用例 7: matchesSchemaLocation
bool test_release_matches() {
    auto desc = std::make_shared<AutosarReleaseDescriptor>(
        "org.artop.aal.autosar448", AutosarMetaModelVersionData(4, 4, 8));
    EMF_ASSERT(desc->matchesSchemaLocation("http://autosar.org/schema/r4.0 AUTOSAR_4-4-8.xsd"));
    EMF_ASSERT(desc->matchesSchemaLocation("http://autosar.org/schema/r4.0"));
    EMF_ASSERT(!desc->matchesSchemaLocation("http://example.com/other"));
    return true;
}

// 用例 8: descriptor 兼容性比较
bool test_release_compare() {
    auto d448 = std::make_shared<AutosarReleaseDescriptor>(
        "a", AutosarMetaModelVersionData(4, 4, 8));
    auto d447 = std::make_shared<AutosarReleaseDescriptor>(
        "b", AutosarMetaModelVersionData(4, 4, 7));
    EMF_ASSERT(d447->compareTo(*d448) < 0);
    EMF_ASSERT(d448->compareTo(*d447) > 0);
    return true;
}

// 用例 9: AutosarResource 基础
bool test_resource_basic() {
    auto desc = std::make_shared<AutosarReleaseDescriptor>(
        "x", AutosarMetaModelVersionData(4, 4, 8));
    AutosarResource res(emf::common::URI("file:///tmp/test.arxml"), desc);
    EMF_ASSERT_EQ(res.getAutosarRelease().get(), desc.get());
    EMF_ASSERT_EQ(res.getURI().toString(), std::string("file:///tmp/test.arxml"));
    EMF_ASSERT(res.getSchemaLocation().empty());
    res.setSchemaLocation("http://autosar.org/schema/r4.0 AUTOSAR_4-4-8.xsd");
    EMF_ASSERT(!res.getSchemaLocation().empty());
    return true;
}

// 用例 10: AutosarXMLResource createXMLLoad/createXMLSave
bool test_xml_resource_factory_methods() {
    AutosarXMLResource res(emf::common::URI("file:///tmp/test.arxml"));
    auto load = res.createXMLLoad();
    auto save = res.createXMLSave();
    auto helper = res.createXMLHelper();
    EMF_ASSERT(load != nullptr);
    EMF_ASSERT(save != nullptr);
    EMF_ASSERT(helper != nullptr);
    return true;
}

// 用例 11: AutosarResourceFactory 注入 creator
bool test_resource_factory_creator() {
    auto desc = std::make_shared<AutosarReleaseDescriptor>(
        "x", AutosarMetaModelVersionData(4, 4, 8));

    class TestFactory : public AutosarXMLResourceFactory {
    public:
        using AutosarXMLResourceFactory::AutosarXMLResourceFactory;
        std::unique_ptr<emf::common::Resource> createResource(const emf::common::URI& uri) override {
            return std::make_unique<AutosarResource>(uri, autosarRelease_);
        }
    };
    TestFactory factory(desc);
    factory.initSchemaLocationBaseURIs();
    auto res = factory.createResource(emf::common::URI("file:///tmp/t.arxml"));
    EMF_ASSERT(res != nullptr);
    EMF_ASSERT_EQ(res->getURI().toString(), std::string("file:///tmp/t.arxml"));
    return true;
}

// 用例 12: initResource 注入 schema location
bool test_init_resource_sets_xsi() {
    auto desc = std::make_shared<AutosarReleaseDescriptor>(
        "x", AutosarMetaModelVersionData(4, 4, 8));
    emf::xmi::XMIResource res(emf::common::URI("file:///tmp/t.arxml"));
    class TestFactory : public AutosarXMLResourceFactory {
    public:
        using AutosarXMLResourceFactory::AutosarXMLResourceFactory;
        std::unique_ptr<emf::common::Resource> createResource(const emf::common::URI& uri) override {
            return std::make_unique<emf::xmi::XMIResource>(uri);
        }
    };
    TestFactory factory(desc);
    factory.initResource(&res);
    EMF_ASSERT(res.getXSISchemaLocation().find("AUTOSAR_4-4-8.xsd") != std::string::npos);
    return true;
}

// 用例 13: schema location catalog
bool test_schema_catalog() {
    auto desc = std::make_shared<AutosarReleaseDescriptor>(
        "x", AutosarMetaModelVersionData(4, 4, 8));
    class TestFactory : public AutosarXMLResourceFactory {
    public:
        using AutosarXMLResourceFactory::AutosarXMLResourceFactory;
        std::unique_ptr<emf::common::Resource> createResource(const emf::common::URI& uri) override {
            return std::make_unique<emf::xmi::XMIResource>(uri);
        }
    };
    TestFactory factory(desc);
    auto cat = factory.createSchemaLocationCatalog();
    EMF_ASSERT(!cat.empty());
    EMF_ASSERT(cat.find("http://autosar.org/schema/r4.0") != cat.end());
    return true;
}

// 用例 14: IdentifiableUtil 在无对象时安全
bool test_identifiable_util_null() {
    std::string s = IdentifiableUtil::getShortName(nullptr);
    EMF_ASSERT(s.empty());
    EMF_ASSERT(!IdentifiableUtil::hasShortName(nullptr));
    IdentifiableUtil::setShortName(nullptr, "x");  // 应当不崩
    return true;
}

// 用例 15: descriptor INSTANCE 兜底
bool test_descriptor_instance() {
    auto& inst = AutosarReleaseDescriptor::getInstance();
    EMF_ASSERT_EQ(inst.getId(), std::string("org.artop.aal.autosar.release"));
    return true;
}

// 用例 16: 元模型注册 —— 加载内置 autosar40.ecore
bool test_metamodel_registration() {
    auto* pkg = AutosarResourceFactory::registerDefaultAutosar40Metamodel();
    EMF_ASSERT(pkg != nullptr);
    EMF_ASSERT_EQ(pkg->getName(), std::string("autosar40"));
    EMF_ASSERT_EQ(pkg->getNsURI(), std::string("http://autosar.org/schema/r4.0"));
    // 幂等：再调用一次返回同一对象
    auto* pkg2 = AutosarResourceFactory::registerDefaultAutosar40Metamodel();
    EMF_ASSERT_EQ(pkg2, pkg);
    // EPackageRegistry 已注册
    EMF_ASSERT(emf::common::EPackageRegistry::instance().containsKey("http://autosar.org/schema/r4.0"));
    // 验证 AUTOSAR EClass 已加载
    auto* autosarCls = pkg->getEClassifier("AUTOSAR");
    EMF_ASSERT(autosarCls != nullptr);
    return true;
}

// 用例 17: arxml 实例化为 EObject —— <AUTOSAR> 根元素
bool test_arxml_instantiation_root() {
    AutosarResourceFactory::registerDefaultAutosar40Metamodel();
    const char* arxml =
        "<AUTOSAR xmlns=\"http://autosar.org/schema/r4.0\">"
        "  <SHORT-NAME>rootAutosar</SHORT-NAME>"
        "</AUTOSAR>";
    emf::xmi::XMIResourceSet rs;
    auto* res = rs.createResource(emf::common::URI("file:///tmp/test.arxml"));
    auto* xres = dynamic_cast<emf::xmi::XMIResource*>(res);
    EMF_ASSERT(xres != nullptr);
    std::istringstream iss(arxml);
    xres->load(iss);
    EMF_ASSERT(!xres->getContents().empty());
    auto* root = xres->getContents()[0];
    EMF_ASSERT(root != nullptr);
    auto* cls = root->eClass();
    EMF_ASSERT(cls != nullptr);
    EMF_ASSERT_EQ(cls->getName(), std::string("AUTOSAR"));
    return true;
}

// 用例 18: arxml containment 递归 —— AR-PACKAGE 子结构
bool test_arxml_containment_arpackage() {
    AutosarResourceFactory::registerDefaultAutosar40Metamodel();
    const char* arxml =
        "<AUTOSAR xmlns=\"http://autosar.org/schema/r4.0\">"
        "  <AR-PACKAGE>"
        "    <SHORT-NAME>pkg1</SHORT-NAME>"
        "  </AR-PACKAGE>"
        "  <AR-PACKAGE>"
        "    <SHORT-NAME>pkg2</SHORT-NAME>"
        "    <AR-PACKAGES>"
        "      <AR-PACKAGE><SHORT-NAME>sub1</SHORT-NAME></AR-PACKAGE>"
        "    </AR-PACKAGES>"
        "  </AR-PACKAGE>"
        "</AUTOSAR>";
    emf::xmi::XMIResourceSet rs;
    auto* res = rs.createResource(emf::common::URI("file:///tmp/test2.arxml"));
    auto* xres = dynamic_cast<emf::xmi::XMIResource*>(res);
    std::istringstream iss(arxml);
    xres->load(iss);
    EMF_ASSERT(!xres->getContents().empty());
    auto* root = xres->getContents()[0];
    auto* cls = root->eClass();
    // AR-PACKAGE 是多值 containment，应有 2 个
    auto* sf = cls->getEStructuralFeature("AR-PACKAGE");
    EMF_ASSERT(sf != nullptr);
    auto v = root->eGet(sf);
    // DynamicEObject 多值 EReference 返回 EList<EObject*>*（featureID 存储方案）；
    // 适配两种返回类型以保持向后兼容。
    size_t listSize = 0;
    emf::common::EObject* pkg1 = nullptr;
    if (auto* elist = std::any_cast<emf::common::EList<emf::common::EObject*>*>(&v)) {
        EMF_ASSERT(*elist != nullptr);
        listSize = (*elist)->size();
        if (listSize >= 1) pkg1 = (*elist)->get(0);
    } else if (auto* listPtr = std::any_cast<std::vector<emf::common::EObject*>*>(&v)) {
        EMF_ASSERT(listPtr != nullptr && *listPtr != nullptr);
        listSize = (*listPtr)->size();
        if (listSize >= 1) pkg1 = (*listPtr)->at(0);
    } else {
        EMF_ASSERT(false && "unexpected eGet return type for AR-PACKAGE");
    }
    EMF_ASSERT_EQ(listSize, (size_t)2);
    // 第一个包 SHORT-NAME = pkg1
    EMF_ASSERT(pkg1 != nullptr);
    auto* sn = pkg1->eClass()->getEStructuralFeature("SHORT-NAME");
    auto snv = pkg1->eGet(sn);
    auto* snVal = std::any_cast<std::string>(&snv);
    EMF_ASSERT(snVal != nullptr);
    EMF_ASSERT_EQ(*snVal, std::string("pkg1"));
    return true;
}

}  // namespace emf::artop::runtime::test

int main() {
    using namespace emf::artop::runtime::test;
    bool all_ok = true;
    all_ok &= EMF_RUN(test_version_data_basic);
    all_ok &= EMF_RUN(test_version_data_parse);
    all_ok &= EMF_RUN(test_version_canonical);
    all_ok &= EMF_RUN(test_schema_version_string);
    all_ok &= EMF_RUN(test_release_descriptor_basic);
    all_ok &= EMF_RUN(test_release_schema_location);
    all_ok &= EMF_RUN(test_release_matches);
    all_ok &= EMF_RUN(test_release_compare);
    all_ok &= EMF_RUN(test_resource_basic);
    all_ok &= EMF_RUN(test_xml_resource_factory_methods);
    all_ok &= EMF_RUN(test_resource_factory_creator);
    all_ok &= EMF_RUN(test_init_resource_sets_xsi);
    all_ok &= EMF_RUN(test_schema_catalog);
    all_ok &= EMF_RUN(test_identifiable_util_null);
    all_ok &= EMF_RUN(test_descriptor_instance);
    all_ok &= EMF_RUN(test_metamodel_registration);
    all_ok &= EMF_RUN(test_arxml_instantiation_root);
    all_ok &= EMF_RUN(test_arxml_containment_arpackage);

    std::printf("\n[emf-artop-runtime] %s\n", all_ok ? "ALL PASS" : "FAIL");
    return all_ok ? 0 : 1;
}
