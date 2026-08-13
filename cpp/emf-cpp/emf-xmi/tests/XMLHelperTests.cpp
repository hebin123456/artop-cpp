// XMLHelperTests.cpp —— XMIHelper / XMLHelper 工具函数单测
// 对齐 Java: org.eclipse.emf.ecore.xmi.impl.XMIHelperImpl / XMLHelperImpl（工具子集）
//
// 覆盖 emf/xmi/XMIHelper.h 中的 inline 工具函数：
//   - splitQName / splitHref / stripFragmentSlash
//   - escapeXmlAttr / escapeXmlText
//   - kEcoreNsURI / kXmiNsURI / kXmiNsURI2 / kXsiNsURI 常量
// 以及 emf/xmi/XMLHelper.h 的 XMLHelperImpl 命名空间上下文 / 编码映射。
#include "test_main.h"
#include "emf/xmi/XMIHelper.h"
#include "emf/xmi/XMLHelper.h"
#include "emf/ecore/EcorePackage.h"
#include "emf/ecore/EcoreImpls.h"

#include <string>

using emf::xmi::splitQName;
using emf::xmi::splitHref;
using emf::xmi::stripFragmentSlash;
using emf::xmi::escapeXmlAttr;
using emf::xmi::escapeXmlText;
using emf::xmi::kEcoreNsURI;
using emf::xmi::kXmiNsURI;
using emf::xmi::kXmiNsURI2;
using emf::xmi::kXsiNsURI;
using emf::xmi::XMLHelper;
using emf::xmi::XMLHelperImpl;

// =====================================================================
// 1) splitQName：把 "prefix:local" 切成 {prefix, local}
// =====================================================================
EMF_TEST(XMIHelper_splitQName_WithPrefix) {
    auto p = splitQName("ecore:EClass");
    EXPECT_EQ(p.first, std::string("ecore"));
    EXPECT_EQ(p.second, std::string("EClass"));
}

EMF_TEST(XMIHelper_splitQName_NoColon) {
    auto p = splitQName("EPackage");
    EXPECT_EQ(p.first, std::string(""));
    EXPECT_EQ(p.second, std::string("EPackage"));
}

EMF_TEST(XMIHelper_splitQName_EmptyString) {
    auto p = splitQName("");
    EXPECT_EQ(p.first, std::string(""));
    EXPECT_EQ(p.second, std::string(""));
}

// =====================================================================
// 2) splitHref：解析 href 字符串
// =====================================================================
EMF_TEST(XMIHelper_splitHref_PathAndFragment) {
    auto r = splitHref("library.ecore#//Library");
    EXPECT_EQ(r.path, std::string("library.ecore"));
    EXPECT_EQ(r.fragment, std::string("//Library"));
}

EMF_TEST(XMIHelper_splitHref_OnlyFragment) {
    auto r = splitHref("#//Book");
    EXPECT_EQ(r.path, std::string(""));
    EXPECT_EQ(r.fragment, std::string("//Book"));
}

EMF_TEST(XMIHelper_splitHref_OnlyPath) {
    auto r = splitHref("other.ecore");
    EXPECT_EQ(r.path, std::string("other.ecore"));
    EXPECT_EQ(r.fragment, std::string(""));
}

EMF_TEST(XMIHelper_splitHref_EcoreEDataTypeForm) {
    // "ecore:EDataType http://.../Ecore#//EString" 先取空格后部分
    auto r = splitHref("ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString");
    EXPECT_EQ(r.path, std::string("http://www.eclipse.org/emf/2002/Ecore"));
    EXPECT_EQ(r.fragment, std::string("//EString"));
}

EMF_TEST(XMIHelper_splitHref_EcoreEClassForm) {
    auto r = splitHref("ecore:EClass library.ecore#//Book");
    EXPECT_EQ(r.path, std::string("library.ecore"));
    EXPECT_EQ(r.fragment, std::string("//Book"));
}

// =====================================================================
// 3) stripFragmentSlash：去掉 fragment 前导 "/"
// =====================================================================
EMF_TEST(XMIHelper_stripFragmentSlash_DoubleSlash) {
    EXPECT_EQ(stripFragmentSlash("//EString"), std::string("EString"));
}

EMF_TEST(XMIHelper_stripFragmentSlash_SingleSlash) {
    EXPECT_EQ(stripFragmentSlash("/Library"), std::string("Library"));
}

EMF_TEST(XMIHelper_stripFragmentSlash_NoSlash) {
    EXPECT_EQ(stripFragmentSlash("Book"), std::string("Book"));
}

EMF_TEST(XMIHelper_stripFragmentSlash_PathStyle) {
    // "//Container/feature.name" -> "Container/feature.name"
    EXPECT_EQ(stripFragmentSlash("//Container/feature.name"),
              std::string("Container/feature.name"));
}

EMF_TEST(XMIHelper_stripFragmentSlash_Empty) {
    EXPECT_EQ(stripFragmentSlash(""), std::string(""));
}

// =====================================================================
// 4) escapeXmlAttr：attribute value 转义（对齐 Java XMLSaveImpl.Escape.convert）
//    规则：& < " 转义；\n \r \t 转义为 &#xA;/&#xD;/&#x9;；
//    > ' 不转义（Java 属性值不转义这两个）；非 ASCII 转义为 &#xNNNN;
// =====================================================================
EMF_TEST(XMIHelper_escapeXmlAttr_BasicChars) {
    EXPECT_EQ(escapeXmlAttr("a&b"), std::string("a&amp;b"));
    EXPECT_EQ(escapeXmlAttr("a<b"), std::string("a&lt;b"));
    EXPECT_EQ(escapeXmlAttr("a\"b"), std::string("a&quot;b"));
    // Java 属性值不转义 > 和 '
    EXPECT_EQ(escapeXmlAttr("a>b"), std::string("a>b"));
    EXPECT_EQ(escapeXmlAttr("a'b"), std::string("a'b"));
}

EMF_TEST(XMIHelper_escapeXmlAttr_ControlChars) {
    // \n \r \t 转义为 &#xA;/&#xD;/&#x9;（对齐 Java convert()）
    EXPECT_EQ(escapeXmlAttr("a\nb"), std::string("a&#xA;b"));
    EXPECT_EQ(escapeXmlAttr("a\rb"), std::string("a&#xD;b"));
    EXPECT_EQ(escapeXmlAttr("a\tb"), std::string("a&#x9;b"));
}

EMF_TEST(XMIHelper_escapeXmlAttr_NoSpecialChars) {
    EXPECT_EQ(escapeXmlAttr("hello world 123"), std::string("hello world 123"));
}

EMF_TEST(XMIHelper_escapeXmlAttr_AllSpecials) {
    // 对齐 Java：> ' 不转义
    EXPECT_EQ(escapeXmlAttr("<&>\"'"),
              std::string("&lt;&amp;>&quot;'"));
}

EMF_TEST(XMIHelper_escapeXmlAttr_NonAscii) {
    // 非 ASCII（ASCII 编码，mappableLimit=0x7F）转义为 &#xNNNN;
    // "中" = U+4E2D, UTF-8 = E4 B8 AD
    EXPECT_EQ(escapeXmlAttr(std::string("a\xE4\xB8\xAD" "b")), std::string("a&#x4e2d;b"));
    // "é" = U+00E9, UTF-8 = C3 A9
    EXPECT_EQ(escapeXmlAttr(std::string("a\xC3\xA9" "b")), std::string("a&#xe9;b"));
}

EMF_TEST(XMIHelper_escapeXmlAttr_Utf8Passthrough) {
    // encoding=UTF-8 时（mappableLimit=0x10FFFF），非 ASCII 原样输出
    EXPECT_EQ(escapeXmlAttr(std::string("a\xE4\xB8\xAD" "b"), 0x10FFFF),
              std::string("a\xE4\xB8\xAD" "b"));
}

EMF_TEST(XMIHelper_escapeXmlAttr_Empty) {
    EXPECT_EQ(escapeXmlAttr(""), std::string(""));
}

// =====================================================================
// 5) escapeXmlText：text content 转义（对齐 Java convertText）
//    规则：& < " \r 转义；> ' \n \t 不转义
// =====================================================================
EMF_TEST(XMIHelper_escapeXmlText_BasicChars) {
    EXPECT_EQ(escapeXmlText("a&b"), std::string("a&amp;b"));
    EXPECT_EQ(escapeXmlText("a<b"), std::string("a&lt;b"));
    // Java convertText 转义 "
    EXPECT_EQ(escapeXmlText("a\"b"), std::string("a&quot;b"));
    // Java convertText 不转义 > 和 '
    EXPECT_EQ(escapeXmlText("a>b"), std::string("a>b"));
    EXPECT_EQ(escapeXmlText("a'b"), std::string("a'b"));
}

EMF_TEST(XMIHelper_escapeXmlText_KeepsNewlineTab) {
    // Java convertText 保留 \n \t
    EXPECT_EQ(escapeXmlText("a\nb"), std::string("a\nb"));
    EXPECT_EQ(escapeXmlText("a\tb"), std::string("a\tb"));
    // \r 转义
    EXPECT_EQ(escapeXmlText("a\rb"), std::string("a&#xD;b"));
}

EMF_TEST(XMIHelper_escapeXmlText_NoSpecialChars) {
    EXPECT_EQ(escapeXmlText("plain text 42"), std::string("plain text 42"));
}

// =====================================================================
// 6) 命名空间 URI 常量
// =====================================================================
EMF_TEST(XMIHelper_NamespaceConstants) {
    EXPECT_EQ(std::string(kEcoreNsURI),
              std::string("http://www.eclipse.org/emf/2002/Ecore"));
    EXPECT_EQ(std::string(kXmiNsURI),
              std::string("http://www.omg.org/XMI"));
    EXPECT_EQ(std::string(kXmiNsURI2),
              std::string("http://schema.omg.org/spec/XMI/2.0"));
    EXPECT_EQ(std::string(kXsiNsURI),
              std::string("http://www.w3.org/2001/XMLSchema-instance"));
}

// =====================================================================
// 7) XMLHelperImpl：命名空间上下文 push/pop/addPrefix/getURI
// =====================================================================
EMF_TEST(XMLHelperImpl_NamespaceContext_AddAndGet) {
    emf::ecore::EcorePackage::initialize();
    XMLHelperImpl h;
    h.pushContext();
    h.addPrefix("ecore", kEcoreNsURI);
    h.addPrefix("xmi", kXmiNsURI);
    EXPECT_EQ(h.getURI("ecore"), std::string(kEcoreNsURI));
    EXPECT_EQ(h.getURI("xmi"), std::string(kXmiNsURI));
    EXPECT_EQ(h.getPrefix(kEcoreNsURI), std::string("ecore"));
    EXPECT_EQ(h.getPrefix(kXmiNsURI), std::string("xmi"));
    EXPECT_EQ(h.getNamespaceURI("ecore"), std::string(kEcoreNsURI));
    h.popContext();
}

EMF_TEST(XMLHelperImpl_NamespaceContext_PopClears) {
    emf::ecore::EcorePackage::initialize();
    XMLHelperImpl h;
    h.pushContext();
    h.addPrefix("ec", kEcoreNsURI);
    EXPECT_EQ(h.getURI("ec"), std::string(kEcoreNsURI));
    h.popContext();
    // pop 后该前缀应不再可见
    EXPECT_EQ(h.getURI("ec"), std::string(""));
}

EMF_TEST(XMLHelperImpl_NamespaceContext_NestedContexts) {
    emf::ecore::EcorePackage::initialize();
    XMLHelperImpl h;
    h.pushContext();
    h.addPrefix("a", "urn:a");
    EXPECT_EQ(h.getURI("a"), std::string("urn:a"));
    h.pushContext();
    h.addPrefix("b", "urn:b");
    EXPECT_EQ(h.getURI("a"), std::string("urn:a"));
    EXPECT_EQ(h.getURI("b"), std::string("urn:b"));
    h.popContext();
    // 内层 pop 后 b 不可见，a 仍可见
    EXPECT_EQ(h.getURI("b"), std::string(""));
    EXPECT_EQ(h.getURI("a"), std::string("urn:a"));
    h.popContext();
}

// =====================================================================
// 8) XMLHelperImpl：编码映射 getXMLEncoding / getJavaEncoding
// =====================================================================
EMF_TEST(XMLHelperImpl_EncodingMapping) {
    emf::ecore::EcorePackage::initialize();
    XMLHelperImpl h;
    // ASCII / UTF-8 互相映射不抛异常并返回非空字符串
    std::string xmlEnc = h.getXMLEncoding("ASCII");
    EXPECT_FALSE(xmlEnc.empty());
    std::string javaEnc = h.getJavaEncoding("UTF-8");
    EXPECT_FALSE(javaEnc.empty());
}

// =====================================================================
// 9) XMLHelperImpl：setResource / getResource
// =====================================================================
EMF_TEST(XMLHelperImpl_ResourceSetterGetter) {
    emf::ecore::EcorePackage::initialize();
    XMLHelperImpl h;
    EXPECT_NULL(h.getResource());
    // 用 nullptr 测试 set/get 不抛异常（仅验证接口契约）
    h.setResource(nullptr);
    EXPECT_NULL(h.getResource());
}

// =====================================================================
// 10) XMLHelperImpl：setNoNamespacePackage / getNoNamespacePackage
// =====================================================================
EMF_TEST(XMLHelperImpl_NoNamespacePackage) {
    emf::ecore::EcorePackage::initialize();
    XMLHelperImpl h;
    EXPECT_NULL(h.getNoNamespacePackage());
    auto* pkg = emf::ecore::EcoreFactory::instance().createEPackage();
    h.setNoNamespacePackage(pkg);
    EXPECT_EQ(h.getNoNamespacePackage(),
              static_cast<emf::ecore::EPackage*>(pkg));
}

// =====================================================================
// 11) XMLHelper feature kind 常量（对齐 Java XMLHelper.DATATYPE_SINGLE 等）
// =====================================================================
EMF_TEST(XMLHelper_FeatureKindConstants) {
    EXPECT_EQ(XMLHelper::DATATYPE_SINGLE, 1);
    EXPECT_EQ(XMLHelper::DATATYPE_IS_MANY, 2);
    EXPECT_EQ(XMLHelper::IS_MANY_ADD, 3);
    EXPECT_EQ(XMLHelper::IS_MANY_MOVE, 4);
    EXPECT_EQ(XMLHelper::OTHER, 5);
}

// =====================================================================
// 12) XMLHelperImpl：base URI 设置（href 解析相关）
// =====================================================================
EMF_TEST(XMLHelperImpl_BaseURI) {
    emf::ecore::EcorePackage::initialize();
    XMLHelperImpl h;
    auto u = emf::common::URI::createFileURI("/tmp/library.ecore");
    h.setBaseURI(u);
    EXPECT_EQ(h.getBaseURI().toFilePath(), std::string("/tmp/library.ecore"));
}
