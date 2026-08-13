// E2E_GenModelXmiEquivalentReplacementTests.cpp —— C++/Java 等价替换测试
// 对齐 Java: org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl 双向互操作
//
// 目标：验证 C++ XMI 实现可以读取 Java EMF 产生的 .ecore 文件，
//       且 C++ 重新保存后的输出在结构上与 Java 格式等价（可被 Java 读回）。
//
// 覆盖：
//   - 读取 Java 风格的 .ecore 文件（samples/multi/library.ecore），验证 C++ 能解析
//   - C++ saveToString 输出包含 Java 格式的关键元素（xsi:type, ecore:EPackage 等）
//   - load -> save -> reload 往返等价（结构保持）
//   - C++ 保存输出只含一个 ecore:EPackage 根（无多余包裹）
//   - EClass / EAttribute / EReference / EEnum 的 xsi:type 正确保留
//   - 元模型属性（name, nsURI, nsPrefix, eType, containment 等）保留
#include "test_main.h"
#include "emf/xmi/XMIResource.h"
#include "emf/xmi/XMIResourceFactory.h"
#include "emf/ecore/EcorePackage.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/common/EPackageRegistry.h"

#include <fstream>
#include <sstream>
#include <string>

using emf::xmi::XMIResource;
using emf::xmi::XMIResourceFactory;
using emf::ecore::EcoreFactory;
using emf::ecore::EcorePackage;
using emf::ecore::EPackage;
using emf::ecore::EClass;
using emf::ecore::EAttribute;
using emf::ecore::EReference;
using emf::ecore::EStructuralFeature;

namespace {

void initEnv() {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    XMIResourceFactory::registerDefaults();
}

std::string readAll(const std::string& path) {
    std::ifstream f(path);
    if (!f) return "";
    std::stringstream ss; ss << f.rdbuf();
    return ss.str();
}

const char* kSamplePath = "emf-xmi/tests/samples/multi/library.ecore";

// Java 风格 ecore（内联副本，避免文件缺失时跳过）
const char* kJavaStyleEcore =
    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
    "<ecore:EPackage xmi:version=\"2.0\"\n"
    "    xmlns:xmi=\"http://www.omg.org/XMI\"\n"
    "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
    "    xmlns:ecore=\"http://www.eclipse.org/emf/2002/Ecore\"\n"
    "    name=\"library\" nsURI=\"http://example.com/e2e/equiv/library\" nsPrefix=\"library\">\n"
    "  <eClassifiers xsi:type=\"ecore:EClass\" name=\"Library\">\n"
    "    <eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"name\"\n"
    "        eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString\"/>\n"
    "    <eStructuralFeatures xsi:type=\"ecore:EReference\" name=\"books\" upperBound=\"-1\"\n"
    "        eType=\"#//Book\" containment=\"true\"/>\n"
    "  </eClassifiers>\n"
    "  <eClassifiers xsi:type=\"ecore:EClass\" name=\"Book\">\n"
    "    <eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"title\"\n"
    "        eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString\"/>\n"
    "    <eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"pages\"\n"
    "        eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EInt\" defaultValueLiteral=\"0\"/>\n"
    "  </eClassifiers>\n"
    "</ecore:EPackage>\n";

EPackage* loadPkg(XMIResource& res, const std::string& xml) {
    res.loadFromString(xml);
    EXPECT_TRUE(!res.getContents().empty());
    return dynamic_cast<EPackage*>(res.getContents().front());
}

}  // namespace

// =====================================================================
// 1) 读取 Java 风格 .ecore -> C++ 能正确解析 EPackage 结构
// =====================================================================
EMF_TEST(E2E_Equiv_LoadJavaStyleEcore_StructureParsed) {
    initEnv();
    XMIResource res;
    auto* pkg = loadPkg(res, kJavaStyleEcore);
    EXPECT_NOT_NULL(pkg);
    EXPECT_EQ(pkg->getName(), std::string("library"));
    EXPECT_EQ(pkg->getNsURI(), std::string("http://example.com/e2e/equiv/library"));
    EXPECT_EQ(pkg->getNsPrefix(), std::string("library"));
    EXPECT_EQ(pkg->getEClassifiers().size(), 2u);
}

// =====================================================================
// 2) C++ saveToString 输出包含 Java 格式关键元素
// =====================================================================
EMF_TEST(E2E_Equiv_CppSaveContainsJavaFormatElements) {
    initEnv();
    XMIResource res;
    loadPkg(res, kJavaStyleEcore);
    std::string out = res.saveToString();
    // Java 格式的关键元素
    EXPECT_TRUE(out.find("<ecore:EPackage") != std::string::npos);
    EXPECT_TRUE(out.find("xmlns:ecore=") != std::string::npos);
    EXPECT_TRUE(out.find("xmlns:xmi=") != std::string::npos);
    EXPECT_TRUE(out.find("xmlns:xsi=") != std::string::npos);
    EXPECT_TRUE(out.find("xsi:type=\"ecore:EClass\"") != std::string::npos);
    EXPECT_TRUE(out.find("xsi:type=\"ecore:EAttribute\"") != std::string::npos);
    EXPECT_TRUE(out.find("xsi:type=\"ecore:EReference\"") != std::string::npos);
}

// =====================================================================
// 3) C++ save 输出只含一个 ecore:EPackage 根（无多余包裹）
// =====================================================================
EMF_TEST(E2E_Equiv_CppSave_SingleEcoreRoot) {
    initEnv();
    XMIResource res;
    loadPkg(res, kJavaStyleEcore);
    std::string out = res.saveToString();
    auto pos = out.find("<ecore:EPackage");
    EXPECT_TRUE(pos != std::string::npos);
    auto pos2 = out.find("<ecore:EPackage", pos + 1);
    EXPECT_TRUE(pos2 == std::string::npos);
}

// =====================================================================
// 4) load -> save -> reload 往返等价：EPackage name/nsURI/nsPrefix 保持
// =====================================================================
EMF_TEST(E2E_Equiv_Roundtrip_PackageMetadataPreserved) {
    initEnv();
    XMIResource res;
    auto* pkg = loadPkg(res, kJavaStyleEcore);
    std::string saved = res.saveToString();

    XMIResource res2;
    auto* pkg2 = loadPkg(res2, saved);
    EXPECT_EQ(pkg2->getName(), pkg->getName());
    EXPECT_EQ(pkg2->getNsURI(), pkg->getNsURI());
    EXPECT_EQ(pkg2->getNsPrefix(), pkg->getNsPrefix());
}

// =====================================================================
// 5) load -> save -> reload 往返等价：classifier 数量和名字保持
// =====================================================================
EMF_TEST(E2E_Equiv_Roundtrip_ClassifiersPreserved) {
    initEnv();
    XMIResource res;
    auto* pkg = loadPkg(res, kJavaStyleEcore);
    std::string saved = res.saveToString();

    XMIResource res2;
    auto* pkg2 = loadPkg(res2, saved);
    EXPECT_EQ(pkg2->getEClassifiers().size(), pkg->getEClassifiers().size());
    EXPECT_NOT_NULL(pkg2->getEClassifier("Library"));
    EXPECT_NOT_NULL(pkg2->getEClassifier("Book"));
}

// =====================================================================
// 6) load -> save -> reload 往返等价：EAttribute eType 保持
// =====================================================================
EMF_TEST(E2E_Equiv_Roundtrip_AttributeETypePreserved) {
    initEnv();
    XMIResource res;
    loadPkg(res, kJavaStyleEcore);
    std::string saved = res.saveToString();

    XMIResource res2;
    auto* pkg2 = loadPkg(res2, saved);
    auto* libCls = dynamic_cast<EClass*>(pkg2->getEClassifier("Library"));
    auto* nameFeat = libCls->getEStructuralFeature("name");
    auto* attr = dynamic_cast<EAttribute*>(nameFeat);
    EXPECT_NOT_NULL(attr);
    EXPECT_NOT_NULL(attr->getEAttributeType());
    EXPECT_EQ(attr->getEAttributeType()->getName(), std::string("EString"));
}

// =====================================================================
// 7) load -> save -> reload 往返等价：EReference containment/eType 保持
// =====================================================================
EMF_TEST(E2E_Equiv_Roundtrip_ReferenceContainmentPreserved) {
    initEnv();
    XMIResource res;
    loadPkg(res, kJavaStyleEcore);
    std::string saved = res.saveToString();

    XMIResource res2;
    auto* pkg2 = loadPkg(res2, saved);
    auto* libCls = dynamic_cast<EClass*>(pkg2->getEClassifier("Library"));
    auto* booksFeat = dynamic_cast<EReference*>(
        libCls->getEStructuralFeature("books"));
    EXPECT_NOT_NULL(booksFeat);
    EXPECT_TRUE(booksFeat->isContainment());
    auto* bookCls = dynamic_cast<EClass*>(pkg2->getEClassifier("Book"));
    EXPECT_EQ(booksFeat->getEReferenceType(), bookCls);
}

// =====================================================================
// 8) load -> save -> reload 往返等价：defaultValueLiteral 保持
// =====================================================================
EMF_TEST(E2E_Equiv_Roundtrip_DefaultValueLiteralPreserved) {
    initEnv();
    XMIResource res;
    loadPkg(res, kJavaStyleEcore);
    std::string saved = res.saveToString();

    XMIResource res2;
    auto* pkg2 = loadPkg(res2, saved);
    auto* bookCls = dynamic_cast<EClass*>(pkg2->getEClassifier("Book"));
    auto* pagesFeat = dynamic_cast<EAttribute*>(
        bookCls->getEStructuralFeature("pages"));
    EXPECT_NOT_NULL(pagesFeat);
    EXPECT_EQ(pagesFeat->getDefaultValueLiteral(), std::string("0"));
}

// =====================================================================
// 9) save 幂等性：两次 save 输出完全一致
// =====================================================================
EMF_TEST(E2E_Equiv_SaveIdempotent) {
    initEnv();
    XMIResource res;
    loadPkg(res, kJavaStyleEcore);
    std::string out1 = res.saveToString();
    std::string out2 = res.saveToString();
    EXPECT_EQ(out1, out2);
}

// =====================================================================
// 10) 读取实际 sample 文件（Java 产出）-> C++ 解析 -> save 回写验证
// =====================================================================
EMF_TEST(E2E_Equiv_LoadSampleFile_SaveBack) {
    initEnv();
    std::string src = readAll(kSamplePath);
    if (src.empty()) {
        std::fprintf(stderr, "skip: %s not found\n", kSamplePath);
        return;
    }
    XMIResource res;
    auto* pkg = loadPkg(res, src);
    EXPECT_NOT_NULL(pkg);
    EXPECT_EQ(pkg->getName(), std::string("library"));
    EXPECT_EQ(pkg->getNsURI(), std::string("http://example.com/e2e/library"));

    std::string out = res.saveToString();
    EXPECT_TRUE(out.find("name=\"library\"") != std::string::npos);
    EXPECT_TRUE(out.find("nsURI=\"http://example.com/e2e/library\"") != std::string::npos);
    EXPECT_TRUE(out.find("xsi:type=\"ecore:EClass\"") != std::string::npos);
    EXPECT_TRUE(out.find("containment=\"true\"") != std::string::npos);
}
