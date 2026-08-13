// E2E_MultiFileEcoreTests.cpp —— 多文件 .ecore 加载与跨文件引用解析
// 对齐 Java: org.eclipse.emf.ecore.xmi.impl.XMLHandler 跨资源 eType/eSuperTypes 解析
//
// 覆盖：
//   - 加载 base .ecore（Library/Book/Writer）并注册到 EPackageRegistry
//   - 加载 ext .ecore（AnnotatedLibrary 继承 Library，引用 Book）
//   - 跨包 eSuperTypes 解析：AnnotatedLibrary.eSuperTypes -> base#//Library
//   - 跨包 eType 解析：ext 包的 EReference.eType -> base#//Book
//   - 读取 samples/multi/library.ecore 和 library_ext.ecore 文件
//   - 两个 EPackage 同时存在于 Registry
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
using emf::ecore::EReference;
using emf::ecore::EStructuralFeature;

namespace {

void initEnv() {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    XMIResourceFactory::registerDefaults();
}

// base 包：定义 Library / Book / Writer
const char* kBaseEcore =
    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
    "<ecore:EPackage xmi:version=\"2.0\"\n"
    "    xmlns:xmi=\"http://www.omg.org/XMI\"\n"
    "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
    "    xmlns:ecore=\"http://www.eclipse.org/emf/2002/Ecore\"\n"
    "    name=\"base\" nsURI=\"http://example.com/e2e/multi/base\" nsPrefix=\"base\">\n"
    "  <eClassifiers xsi:type=\"ecore:EClass\" name=\"Library\">\n"
    "    <eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"name\"\n"
    "        eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString\"/>\n"
    "    <eStructuralFeatures xsi:type=\"ecore:EReference\" name=\"books\" upperBound=\"-1\"\n"
    "        eType=\"#//Book\" containment=\"true\"/>\n"
    "  </eClassifiers>\n"
    "  <eClassifiers xsi:type=\"ecore:EClass\" name=\"Book\">\n"
    "    <eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"title\"\n"
    "        eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString\"/>\n"
    "  </eClassifiers>\n"
    "  <eClassifiers xsi:type=\"ecore:EClass\" name=\"Writer\">\n"
    "    <eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"name\"\n"
    "        eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString\"/>\n"
    "  </eClassifiers>\n"
    "</ecore:EPackage>\n";

// ext 包：AnnotatedLibrary 继承 base#//Library，highlighted 引用 base#//Book
const char* kExtEcore =
    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
    "<ecore:EPackage xmi:version=\"2.0\"\n"
    "    xmlns:xmi=\"http://www.omg.org/XMI\"\n"
    "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
    "    xmlns:ecore=\"http://www.eclipse.org/emf/2002/Ecore\"\n"
    "    name=\"ext\" nsURI=\"http://example.com/e2e/multi/ext\" nsPrefix=\"ext\">\n"
    "  <eClassifiers xsi:type=\"ecore:EClass\" name=\"AnnotatedLibrary\"\n"
    "      eSuperTypes=\"http://example.com/e2e/multi/base#//Library\">\n"
    "    <eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"note\"\n"
    "        eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString\"/>\n"
    "    <eStructuralFeatures xsi:type=\"ecore:EReference\" name=\"highlighted\" upperBound=\"-1\"\n"
    "        eType=\"ecore:EClass http://example.com/e2e/multi/base#//Book\" containment=\"true\"/>\n"
    "  </eClassifiers>\n"
    "  <eClassifiers xsi:type=\"ecore:EClass\" name=\"BookCollection\">\n"
    "    <eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"label\"\n"
    "        eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString\"/>\n"
    "    <eStructuralFeatures xsi:type=\"ecore:EReference\" name=\"books\" upperBound=\"-1\"\n"
    "        eType=\"ecore:EClass http://example.com/e2e/multi/base#//Book\" containment=\"true\"/>\n"
    "  </eClassifiers>\n"
    "</ecore:EPackage>\n";

EPackage* loadPkg(XMIResource& res, const std::string& xml) {
    res.loadFromString(xml);
    EXPECT_TRUE(!res.getContents().empty());
    return dynamic_cast<EPackage*>(res.getContents().front());
}

std::string readAll(const std::string& path) {
    std::ifstream f(path);
    if (!f) return "";
    std::stringstream ss; ss << f.rdbuf();
    return ss.str();
}

}  // namespace

// =====================================================================
// 1) 加载 base .ecore -> 注册到 EPackageRegistry
// =====================================================================
EMF_TEST(E2E_MultiFileEcore_BasePackageLoadedAndRegistered) {
    initEnv();
    XMIResource res;
    auto* pkg = loadPkg(res, kBaseEcore);
    EXPECT_NOT_NULL(pkg);
    EXPECT_EQ(pkg->getName(), std::string("base"));
    EXPECT_EQ(pkg->getNsURI(), std::string("http://example.com/e2e/multi/base"));
    // Registry 中应能按 nsURI 查到
    auto* regPkg = emf::common::EPackageRegistry::instance().get(
        "http://example.com/e2e/multi/base");
    EXPECT_NOT_NULL(regPkg);
}

// =====================================================================
// 2) 加载 ext .ecore（依赖 base 已注册）-> 验证 ext 包结构
// =====================================================================
EMF_TEST(E2E_MultiFileEcore_ExtPackageLoadedAfterBase) {
    initEnv();
    XMIResource baseRes;
    loadPkg(baseRes, kBaseEcore);
    XMIResource extRes;
    auto* extPkg = loadPkg(extRes, kExtEcore);
    EXPECT_NOT_NULL(extPkg);
    EXPECT_EQ(extPkg->getName(), std::string("ext"));
    EXPECT_EQ(extPkg->getEClassifiers().size(), 2u);
    EXPECT_NOT_NULL(extPkg->getEClassifier("AnnotatedLibrary"));
    EXPECT_NOT_NULL(extPkg->getEClassifier("BookCollection"));
}

// =====================================================================
// 3) 跨包 eSuperTypes 解析：AnnotatedLibrary 继承 base#//Library
// =====================================================================
EMF_TEST(E2E_MultiFileEcore_CrossPackageSuperTypesResolved) {
    initEnv();
    XMIResource baseRes;
    auto* basePkg = loadPkg(baseRes, kBaseEcore);
    XMIResource extRes;
    auto* extPkg = loadPkg(extRes, kExtEcore);

    auto* annLibCls = dynamic_cast<EClass*>(
        extPkg->getEClassifier("AnnotatedLibrary"));
    EXPECT_NOT_NULL(annLibCls);
    // eSuperTypes 应解析到 base 包的 Library
    auto& supers = annLibCls->getESuperTypes();
    EXPECT_EQ(supers.size(), 1u);
    auto* baseLibCls = dynamic_cast<EClass*>(
        basePkg->getEClassifier("Library"));
    EXPECT_EQ(supers[0], baseLibCls);
}

// =====================================================================
// 4) 跨包 eType 解析：AnnotatedLibrary.highlighted -> base#//Book
// =====================================================================
EMF_TEST(E2E_MultiFileEcore_CrossPackageETypeResolved) {
    initEnv();
    XMIResource baseRes;
    auto* basePkg = loadPkg(baseRes, kBaseEcore);
    XMIResource extRes;
    auto* extPkg = loadPkg(extRes, kExtEcore);

    auto* annLibCls = dynamic_cast<EClass*>(
        extPkg->getEClassifier("AnnotatedLibrary"));
    auto* highlightedFeat = annLibCls->getEStructuralFeature("highlighted");
    EXPECT_NOT_NULL(highlightedFeat);
    auto* ref = dynamic_cast<EReference*>(highlightedFeat);
    EXPECT_NOT_NULL(ref);
    // eType 应解析到 base 包的 Book
    auto* bookCls = ref->getEReferenceType();
    auto* baseBookCls = dynamic_cast<EClass*>(
        basePkg->getEClassifier("Book"));
    EXPECT_EQ(bookCls, baseBookCls);
}

// =====================================================================
// 5) 跨包 eType 解析：BookCollection.books -> base#//Book
// =====================================================================
EMF_TEST(E2E_MultiFileEcore_BookCollectionBooksETypeResolved) {
    initEnv();
    XMIResource baseRes;
    auto* basePkg = loadPkg(baseRes, kBaseEcore);
    XMIResource extRes;
    auto* extPkg = loadPkg(extRes, kExtEcore);

    auto* bcCls = dynamic_cast<EClass*>(
        extPkg->getEClassifier("BookCollection"));
    auto* booksFeat = bcCls->getEStructuralFeature("books");
    auto* ref = dynamic_cast<EReference*>(booksFeat);
    EXPECT_NOT_NULL(ref);
    auto* bookCls = ref->getEReferenceType();
    auto* baseBookCls = dynamic_cast<EClass*>(
        basePkg->getEClassifier("Book"));
    EXPECT_EQ(bookCls, baseBookCls);
}

// =====================================================================
// 6) 两个 EPackage 同时存在于 Registry
// =====================================================================
EMF_TEST(E2E_MultiFileEcore_BothPackagesInRegistry) {
    initEnv();
    XMIResource baseRes;
    loadPkg(baseRes, kBaseEcore);
    XMIResource extRes;
    loadPkg(extRes, kExtEcore);

    EXPECT_TRUE(emf::common::EPackageRegistry::instance().containsKey(
        "http://example.com/e2e/multi/base"));
    EXPECT_TRUE(emf::common::EPackageRegistry::instance().containsKey(
        "http://example.com/e2e/multi/ext"));
}

// =====================================================================
// 7) 读取 samples/multi/library.ecore 文件 -> 加载验证
// =====================================================================
EMF_TEST(E2E_MultiFileEcore_LoadSampleLibraryEcore) {
    initEnv();
    std::string src = readAll("emf-xmi/tests/samples/multi/library.ecore");
    if (src.empty()) {
        std::fprintf(stderr, "skip: samples/multi/library.ecore not found\n");
        return;
    }
    XMIResource res;
    auto* pkg = loadPkg(res, src);
    EXPECT_NOT_NULL(pkg);
    EXPECT_EQ(pkg->getName(), std::string("library"));
    EXPECT_EQ(pkg->getNsURI(), std::string("http://example.com/e2e/library"));
    EXPECT_EQ(pkg->getEClassifiers().size(), 3u);
    EXPECT_NOT_NULL(pkg->getEClassifier("Library"));
    EXPECT_NOT_NULL(pkg->getEClassifier("Book"));
    EXPECT_NOT_NULL(pkg->getEClassifier("Writer"));
}

// =====================================================================
// 8) 读取 samples/multi/library_ext.ecore 文件 -> 加载验证
// =====================================================================
EMF_TEST(E2E_MultiFileEcore_LoadSampleLibraryExtEcore) {
    initEnv();
    // 先加载 base library.ecore 并额外注册到 filename key
    std::string baseSrc = readAll("emf-xmi/tests/samples/multi/library.ecore");
    if (baseSrc.empty()) {
        std::fprintf(stderr, "skip: samples/multi/library.ecore not found\n");
        return;
    }
    XMIResource baseRes;
    auto* basePkg = loadPkg(baseRes, baseSrc);
    // 额外注册到 "library.ecore" key（sample 文件用 filename 作 href 前缀）
    emf::common::EPackageRegistry::instance().put("library.ecore", basePkg);

    std::string extSrc = readAll("emf-xmi/tests/samples/multi/library_ext.ecore");
    if (extSrc.empty()) {
        std::fprintf(stderr, "skip: samples/multi/library_ext.ecore not found\n");
        return;
    }
    XMIResource extRes;
    auto* extPkg = loadPkg(extRes, extSrc);
    EXPECT_NOT_NULL(extPkg);
    EXPECT_EQ(extPkg->getName(), std::string("libraryExt"));
    EXPECT_EQ(extPkg->getEClassifiers().size(), 2u);
    EXPECT_NOT_NULL(extPkg->getEClassifier("AnnotatedLibrary"));
    EXPECT_NOT_NULL(extPkg->getEClassifier("BookCollection"));
}

// =====================================================================
// 9) 跨文件 eSuperTypes 解析（sample 文件）：AnnotatedLibrary 继承 Library
// =====================================================================
EMF_TEST(E2E_MultiFileEcore_SampleCrossFileSuperTypesResolved) {
    initEnv();
    std::string baseSrc = readAll("emf-xmi/tests/samples/multi/library.ecore");
    if (baseSrc.empty()) return;
    XMIResource baseRes;
    auto* basePkg = loadPkg(baseRes, baseSrc);
    emf::common::EPackageRegistry::instance().put("library.ecore", basePkg);

    std::string extSrc = readAll("emf-xmi/tests/samples/multi/library_ext.ecore");
    if (extSrc.empty()) return;
    XMIResource extRes;
    auto* extPkg = loadPkg(extRes, extSrc);

    auto* annLibCls = dynamic_cast<EClass*>(
        extPkg->getEClassifier("AnnotatedLibrary"));
    EXPECT_NOT_NULL(annLibCls);
    auto& supers = annLibCls->getESuperTypes();
    EXPECT_EQ(supers.size(), 1u);
    auto* baseLibCls = dynamic_cast<EClass*>(
        basePkg->getEClassifier("Library"));
    EXPECT_EQ(supers[0], baseLibCls);
}

// =====================================================================
// 10) 跨文件 eType 解析（sample 文件）：BookCollection.books -> Book
// =====================================================================
EMF_TEST(E2E_MultiFileEcore_SampleCrossFileETypeResolved) {
    initEnv();
    std::string baseSrc = readAll("emf-xmi/tests/samples/multi/library.ecore");
    if (baseSrc.empty()) return;
    XMIResource baseRes;
    auto* basePkg = loadPkg(baseRes, baseSrc);
    emf::common::EPackageRegistry::instance().put("library.ecore", basePkg);

    std::string extSrc = readAll("emf-xmi/tests/samples/multi/library_ext.ecore");
    if (extSrc.empty()) return;
    XMIResource extRes;
    auto* extPkg = loadPkg(extRes, extSrc);

    auto* bcCls = dynamic_cast<EClass*>(
        extPkg->getEClassifier("BookCollection"));
    auto* booksFeat = bcCls->getEStructuralFeature("books");
    auto* ref = dynamic_cast<EReference*>(booksFeat);
    EXPECT_NOT_NULL(ref);
    auto* bookCls = ref->getEReferenceType();
    auto* baseBookCls = dynamic_cast<EClass*>(
        basePkg->getEClassifier("Book"));
    EXPECT_EQ(bookCls, baseBookCls);
}
