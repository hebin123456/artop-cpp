// E2E_GenModelXmiTypedMultiFileTests.cpp —— 多文件 typed XMI 实例加载测试
// 对齐 Java: org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl 多文件实例加载
//
// 场景：
//   - 用 Java EMF 产生的 library.xmi / authors.xmi / publishers.xmi 实例文件
//   - 这些文件共享同一个 nsURI="http://example.com/emfdemo/library" 的元模型
//   - 先加载元模型（内联 ecore），再用元模型解析实例文档
//
// 覆盖：
//   - 加载内联 ecore（nsURI 匹配 xmi 文件）并注册到 EPackageRegistry
//   - 加载 library.xmi（单根 <library:Library> 实例文档）
//   - 加载 authors.xmi（<xmi:XMI> 包裹多根 <library:Author> 实例文档）
//   - 加载 publishers.xmi（<xmi:XMI> 包裹多根 <library:Publisher> 实例文档）
//   - 验证 Library 根对象的 name 属性正确加载
//   - 验证 Library 的 containment 子对象（books/authors/publishers）加载
//   - 验证多根 xmi:XMI 文档的 contents 数量
//   - 验证 Author 实例的 name/email 属性正确加载
//   - 验证所有 xmi 文件使用同一注册的 EPackage
//   - 验证加载多个 xmi 文件互不干扰
#include "test_main.h"
#include "emf/xmi/XMIResource.h"
#include "emf/xmi/XMIResourceFactory.h"
#include "emf/ecore/EcorePackage.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/common/EPackageRegistry.h"
#include "emf/common/EList.h"

#include <any>
#include <fstream>
#include <sstream>
#include <string>
#include <vector>

using emf::xmi::XMIResource;
using emf::xmi::XMIResourceFactory;
using emf::ecore::EcoreFactory;
using emf::ecore::EcorePackage;
using emf::ecore::EPackage;
using emf::ecore::EClass;
using emf::ecore::EReference;
using emf::ecore::EStructuralFeature;

namespace {

void initEnv() {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    XMIResourceFactory::registerDefaults();
}

// 内联 ecore：匹配 samples/multi-xmi-java/ 下 xmi 文件的 nsURI
// nsURI = "http://example.com/emfdemo/library"
// 包含 Library / Book / Author / Publisher / Magazine / Address 类
const char* kEmfDemoLibraryEcore =
    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
    "<ecore:EPackage xmi:version=\"2.0\"\n"
    "    xmlns:xmi=\"http://www.omg.org/XMI\"\n"
    "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
    "    xmlns:ecore=\"http://www.eclipse.org/emf/2002/Ecore\"\n"
    "    name=\"library\" nsURI=\"http://example.com/emfdemo/library\" nsPrefix=\"library\">\n"
    "  <eClassifiers xsi:type=\"ecore:EClass\" name=\"Library\">\n"
    "    <eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"name\"\n"
    "        eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString\"/>\n"
    "    <eStructuralFeatures xsi:type=\"ecore:EReference\" name=\"books\" upperBound=\"-1\"\n"
    "        eType=\"#//Book\" containment=\"true\"/>\n"
    "    <eStructuralFeatures xsi:type=\"ecore:EReference\" name=\"magazines\" upperBound=\"-1\"\n"
    "        eType=\"#//Magazine\" containment=\"true\"/>\n"
    "    <eStructuralFeatures xsi:type=\"ecore:EReference\" name=\"authors\" upperBound=\"-1\"\n"
    "        eType=\"#//Author\" containment=\"true\"/>\n"
    "    <eStructuralFeatures xsi:type=\"ecore:EReference\" name=\"publishers\" upperBound=\"-1\"\n"
    "        eType=\"#//Publisher\" containment=\"true\"/>\n"
    "  </eClassifiers>\n"
    "  <eClassifiers xsi:type=\"ecore:EClass\" name=\"Book\">\n"
    "    <eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"title\"\n"
    "        eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString\"/>\n"
    "    <eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"pages\"\n"
    "        eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EInt\"/>\n"
    "  </eClassifiers>\n"
    "  <eClassifiers xsi:type=\"ecore:EClass\" name=\"Magazine\">\n"
    "    <eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"title\"\n"
    "        eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString\"/>\n"
    "    <eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"issueNumber\"\n"
    "        eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EInt\"/>\n"
    "  </eClassifiers>\n"
    "  <eClassifiers xsi:type=\"ecore:EClass\" name=\"Author\">\n"
    "    <eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"name\"\n"
    "        eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString\"/>\n"
    "    <eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"email\"\n"
    "        eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString\"/>\n"
    "    <eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"birthYear\"\n"
    "        eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EInt\"/>\n"
    "  </eClassifiers>\n"
    "  <eClassifiers xsi:type=\"ecore:EClass\" name=\"Publisher\">\n"
    "    <eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"name\"\n"
    "        eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString\"/>\n"
    "    <eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"email\"\n"
    "        eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString\"/>\n"
    "    <eStructuralFeatures xsi:type=\"ecore:EReference\" name=\"address\"\n"
    "        eType=\"#//Address\" containment=\"true\"/>\n"
    "  </eClassifiers>\n"
    "  <eClassifiers xsi:type=\"ecore:EClass\" name=\"Address\">\n"
    "    <eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"street\"\n"
    "        eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString\"/>\n"
    "    <eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"city\"\n"
    "        eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString\"/>\n"
    "    <eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"country\"\n"
    "        eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString\"/>\n"
    "    <eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"zipCode\"\n"
    "        eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString\"/>\n"
    "  </eClassifiers>\n"
    "</ecore:EPackage>\n";

const char* kLibraryXmiPath = "emf-xmi/tests/samples/multi-xmi-java/library.xmi";
const char* kAuthorsXmiPath = "emf-xmi/tests/samples/multi-xmi-java/authors.xmi";
const char* kPublishersXmiPath = "emf-xmi/tests/samples/multi-xmi-java/publishers.xmi";

std::string readAll(const std::string& path) {
    std::ifstream f(path);
    if (!f) return "";
    std::stringstream ss; ss << f.rdbuf();
    return ss.str();
}

// 加载元模型并返回 EPackage（已注册到 EPackageRegistry）
EPackage* loadMetaModel() {
    XMIResource res;
    res.loadFromString(kEmfDemoLibraryEcore);
    return dynamic_cast<EPackage*>(res.getContents().front());
}

}  // namespace

// =====================================================================
// 1) 加载内联 ecore -> EPackage 注册到 Registry（nsURI 匹配 xmi 文件）
// =====================================================================
EMF_TEST(E2E_TypedMultiFile_MetaModelRegisteredToRegistry) {
    initEnv();
    auto* pkg = loadMetaModel();
    EXPECT_NOT_NULL(pkg);
    EXPECT_EQ(pkg->getNsURI(), std::string("http://example.com/emfdemo/library"));
    auto* regPkg = emf::common::EPackageRegistry::instance().get(
        "http://example.com/emfdemo/library");
    EXPECT_NOT_NULL(regPkg);
    EXPECT_EQ(regPkg, pkg);
}

// =====================================================================
// 2) 加载 library.xmi -> 单根 Library 实例加载
// =====================================================================
EMF_TEST(E2E_TypedMultiFile_LoadLibraryXmi_SingleRoot) {
    initEnv();
    loadMetaModel();
    std::string src = readAll(kLibraryXmiPath);
    if (src.empty()) {
        std::fprintf(stderr, "skip: %s not found\n", kLibraryXmiPath);
        return;
    }
    XMIResource res;
    res.loadFromString(src);
    EXPECT_EQ(res.getContents().size(), 1u);
    auto* root = res.getContents().front();
    EXPECT_NOT_NULL(root);
    EXPECT_EQ(root->eClass()->getName(), std::string("Library"));
}

// =====================================================================
// 3) library.xmi -> Library name 属性正确加载
// =====================================================================
EMF_TEST(E2E_TypedMultiFile_LibraryName_LoadedCorrectly) {
    initEnv();
    auto* pkg = loadMetaModel();
    std::string src = readAll(kLibraryXmiPath);
    if (src.empty()) return;
    XMIResource res;
    res.loadFromString(src);
    auto* root = res.getContents().front();
    auto* libCls = dynamic_cast<EClass*>(pkg->getEClassifier("Library"));
    auto* nameFeat = libCls->getEStructuralFeature("name");
    auto v = root->eGet(nameFeat);
    EXPECT_EQ(std::any_cast<std::string>(v),
              std::string("City Central Library"));
}

// =====================================================================
// 4) library.xmi -> containment 子对象（books）加载
// =====================================================================
EMF_TEST(E2E_TypedMultiFile_LibraryBooks_ContainmentLoaded) {
    initEnv();
    auto* pkg = loadMetaModel();
    auto* libCls = dynamic_cast<EClass*>(pkg->getEClassifier("Library"));
    std::string src = readAll(kLibraryXmiPath);
    if (src.empty()) return;
    XMIResource res;
    res.loadFromString(src);
    auto* root = res.getContents().front();
    auto* booksFeat = libCls->getEStructuralFeature("books");
    auto* listPtr = std::any_cast<emf::common::EList<emf::common::EObject*>*>(
        root->eGet(booksFeat));
    EXPECT_NOT_NULL(listPtr);
    // library.xmi 有 2 个 books
    EXPECT_EQ(listPtr->size(), 2u);
    // 验证第一个 book 的 title
    auto* bookCls = dynamic_cast<EClass*>(pkg->getEClassifier("Book"));
    auto* titleFeat = bookCls->getEStructuralFeature("title");
    auto titleV = (*listPtr)[0]->eGet(titleFeat);
    EXPECT_EQ(std::any_cast<std::string>(titleV),
              std::string("The Pragmatic Programmer"));
}

// =====================================================================
// 5) library.xmi -> authors containment 子对象加载
// =====================================================================
EMF_TEST(E2E_TypedMultiFile_LibraryAuthors_ContainmentLoaded) {
    initEnv();
    auto* pkg = loadMetaModel();
    auto* libCls = dynamic_cast<EClass*>(pkg->getEClassifier("Library"));
    std::string src = readAll(kLibraryXmiPath);
    if (src.empty()) return;
    XMIResource res;
    res.loadFromString(src);
    auto* root = res.getContents().front();
    auto* authorsFeat = libCls->getEStructuralFeature("authors");
    auto* listPtr = std::any_cast<emf::common::EList<emf::common::EObject*>*>(
        root->eGet(authorsFeat));
    EXPECT_NOT_NULL(listPtr);
    // library.xmi 有 3 个 authors
    EXPECT_EQ(listPtr->size(), 3u);
    // 验证第一个 author 的 name
    auto* authorCls = dynamic_cast<EClass*>(pkg->getEClassifier("Author"));
    auto* nameFeat = authorCls->getEStructuralFeature("name");
    auto nameV = (*listPtr)[0]->eGet(nameFeat);
    EXPECT_EQ(std::any_cast<std::string>(nameV),
              std::string("Ada Lovelace"));
}

// =====================================================================
// 6) library.xmi -> publishers containment 子对象加载（含嵌套 address）
// =====================================================================
EMF_TEST(E2E_TypedMultiFile_LibraryPublishers_ContainmentLoaded) {
    initEnv();
    auto* pkg = loadMetaModel();
    auto* libCls = dynamic_cast<EClass*>(pkg->getEClassifier("Library"));
    std::string src = readAll(kLibraryXmiPath);
    if (src.empty()) return;
    XMIResource res;
    res.loadFromString(src);
    auto* root = res.getContents().front();
    auto* pubsFeat = libCls->getEStructuralFeature("publishers");
    auto* listPtr = std::any_cast<emf::common::EList<emf::common::EObject*>*>(
        root->eGet(pubsFeat));
    EXPECT_NOT_NULL(listPtr);
    // library.xmi 有 2 个 publishers
    EXPECT_EQ(listPtr->size(), 2u);
    // 验证第一个 publisher 的 name
    auto* pubCls = dynamic_cast<EClass*>(pkg->getEClassifier("Publisher"));
    auto* nameFeat = pubCls->getEStructuralFeature("name");
    auto nameV = (*listPtr)[0]->eGet(nameFeat);
    EXPECT_EQ(std::any_cast<std::string>(nameV),
              std::string("O'Reilly Media"));
}

// =====================================================================
// 7) 加载 authors.xmi -> <xmi:XMI> 多根文档，3 个 Author 根
// =====================================================================
EMF_TEST(E2E_TypedMultiFile_LoadAuthorsXmi_MultiRoot) {
    initEnv();
    loadMetaModel();
    std::string src = readAll(kAuthorsXmiPath);
    if (src.empty()) {
        std::fprintf(stderr, "skip: %s not found\n", kAuthorsXmiPath);
        return;
    }
    XMIResource res;
    res.loadFromString(src);
    // authors.xmi 用 <xmi:XMI> 包裹 3 个 <library:Author>
    EXPECT_EQ(res.getContents().size(), 3u);
    for (auto* obj : res.getContents()) {
        EXPECT_EQ(obj->eClass()->getName(), std::string("Author"));
    }
}

// =====================================================================
// 8) authors.xmi -> Author name/email 属性正确加载
// =====================================================================
EMF_TEST(E2E_TypedMultiFile_AuthorsAttributes_LoadedCorrectly) {
    initEnv();
    auto* pkg = loadMetaModel();
    auto* authorCls = dynamic_cast<EClass*>(pkg->getEClassifier("Author"));
    std::string src = readAll(kAuthorsXmiPath);
    if (src.empty()) return;
    XMIResource res;
    res.loadFromString(src);
    auto& contents = res.getContents();
    EXPECT_EQ(contents.size(), 3u);
    // 验证第一个 author
    auto* nameFeat = authorCls->getEStructuralFeature("name");
    auto* emailFeat = authorCls->getEStructuralFeature("email");
    auto nameV = contents[0]->eGet(nameFeat);
    EXPECT_EQ(std::any_cast<std::string>(nameV), std::string("Ada Lovelace"));
    auto emailV = contents[0]->eGet(emailFeat);
    EXPECT_EQ(std::any_cast<std::string>(emailV), std::string("ada@example.com"));
}

// =====================================================================
// 9) 加载 publishers.xmi -> <xmi:XMI> 多根文档，2 个 Publisher 根
// =====================================================================
EMF_TEST(E2E_TypedMultiFile_LoadPublishersXmi_MultiRoot) {
    initEnv();
    loadMetaModel();
    std::string src = readAll(kPublishersXmiPath);
    if (src.empty()) {
        std::fprintf(stderr, "skip: %s not found\n", kPublishersXmiPath);
        return;
    }
    XMIResource res;
    res.loadFromString(src);
    EXPECT_EQ(res.getContents().size(), 2u);
    for (auto* obj : res.getContents()) {
        EXPECT_EQ(obj->eClass()->getName(), std::string("Publisher"));
    }
}

// =====================================================================
// 10) publishers.xmi -> Publisher name 属性 + 嵌套 address 子对象加载
// =====================================================================
EMF_TEST(E2E_TypedMultiFile_PublisherAddress_NestedContainment) {
    initEnv();
    auto* pkg = loadMetaModel();
    auto* pubCls = dynamic_cast<EClass*>(pkg->getEClassifier("Publisher"));
    std::string src = readAll(kPublishersXmiPath);
    if (src.empty()) return;
    XMIResource res;
    res.loadFromString(src);
    auto& contents = res.getContents();
    EXPECT_EQ(contents.size(), 2u);
    auto* nameFeat = pubCls->getEStructuralFeature("name");
    auto nameV = contents[0]->eGet(nameFeat);
    EXPECT_EQ(std::any_cast<std::string>(nameV), std::string("O'Reilly Media"));
    // address containment 子对象
    auto* addressFeat = pubCls->getEStructuralFeature("address");
    auto addrV = contents[0]->eGet(addressFeat);
    auto* addr = std::any_cast<emf::common::EObject*>(addrV);
    EXPECT_NOT_NULL(addr);
    EXPECT_EQ(addr->eClass()->getName(), std::string("Address"));
    // address 的 city 属性
    auto* addressCls = dynamic_cast<EClass*>(pkg->getEClassifier("Address"));
    auto* cityFeat = addressCls->getEStructuralFeature("city");
    auto cityV = addr->eGet(cityFeat);
    EXPECT_EQ(std::any_cast<std::string>(cityV), std::string("Sebastopol"));
}

// =====================================================================
// 11) 多文件加载互不干扰：library.xmi + authors.xmi 各自独立加载
// =====================================================================
EMF_TEST(E2E_TypedMultiFile_MultipleFiles_NoInterference) {
    initEnv();
    loadMetaModel();
    std::string libSrc = readAll(kLibraryXmiPath);
    std::string authSrc = readAll(kAuthorsXmiPath);
    if (libSrc.empty() || authSrc.empty()) return;

    XMIResource libRes;
    libRes.loadFromString(libSrc);
    EXPECT_EQ(libRes.getContents().size(), 1u);
    EXPECT_EQ(libRes.getContents().front()->eClass()->getName(),
              std::string("Library"));

    XMIResource authRes;
    authRes.loadFromString(authSrc);
    EXPECT_EQ(authRes.getContents().size(), 3u);
    for (auto* obj : authRes.getContents()) {
        EXPECT_EQ(obj->eClass()->getName(), std::string("Author"));
    }
}

// =====================================================================
// 12) 所有 xmi 文件使用同一注册的 EPackage
// =====================================================================
EMF_TEST(E2E_TypedMultiFile_AllFilesUseSameRegisteredPackage) {
    initEnv();
    auto* pkg = loadMetaModel();
    std::string libSrc = readAll(kLibraryXmiPath);
    std::string authSrc = readAll(kAuthorsXmiPath);
    std::string pubSrc = readAll(kPublishersXmiPath);
    if (libSrc.empty() || authSrc.empty() || pubSrc.empty()) return;

    XMIResource libRes, authRes, pubRes;
    libRes.loadFromString(libSrc);
    authRes.loadFromString(authSrc);
    pubRes.loadFromString(pubSrc);

    // 所有加载的实例的 EClass 所属 EPackage 应为同一注册的 pkg
    EXPECT_EQ(libRes.getContents().front()->eClass()->getEPackage(), pkg);
    EXPECT_EQ(authRes.getContents().front()->eClass()->getEPackage(), pkg);
    EXPECT_EQ(pubRes.getContents().front()->eClass()->getEPackage(), pkg);
}
