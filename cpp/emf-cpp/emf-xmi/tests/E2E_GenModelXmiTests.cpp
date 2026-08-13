// E2E_GenModelXmiTests.cpp —— 端到端：Load .ecore -> instantiate -> save .xmi -> reload -> verify
// 对齐 Java: org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl 端到端加载/保存流程
//
// 覆盖：
//   - 从 .ecore 元模型文档加载 EPackage（含 EClass / EAttribute / EReference）
//   - 用 EFactory::create 动态实例化用户模型对象（DynamicEObject 回退）
//   - 通过反射 eSet 设置属性、push_back 添加 containment 子对象
//   - saveToString 序列化为 XMI 实例文档（<prefix:RootClass ...>）
//   - 重新加载序列化结果，验证结构等价（名称/属性值/containment 子对象数量）
#include "test_main.h"
#include "emf/xmi/XMIResource.h"
#include "emf/xmi/XMIResourceFactory.h"
#include "emf/ecore/EcorePackage.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/ecore/DynamicEObject.h"
#include "emf/common/EPackageRegistry.h"
#include "emf/common/EList.h"

#include <any>
#include <string>
#include <vector>

using emf::xmi::XMIResource;
using emf::xmi::XMIResourceFactory;
using emf::ecore::EcoreFactory;
using emf::ecore::EcorePackage;
using emf::ecore::EPackage;
using emf::ecore::EClass;
using emf::ecore::EAttribute;
using emf::ecore::EReference;
using emf::ecore::EStructuralFeature;
using emf::ecore::EFactory;

namespace {

void initEnv() {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    XMIResourceFactory::registerDefaults();
}

// 完整的 library 元模型：Library / Book / Writer
const char* kLibraryEcore =
    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
    "<ecore:EPackage xmi:version=\"2.0\"\n"
    "    xmlns:xmi=\"http://www.omg.org/XMI\"\n"
    "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
    "    xmlns:ecore=\"http://www.eclipse.org/emf/2002/Ecore\"\n"
    "    name=\"library\" nsURI=\"http://example.com/e2e/genmodel/library\" nsPrefix=\"library\">\n"
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
    "        eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EInt\"/>\n"
    "  </eClassifiers>\n"
    "  <eClassifiers xsi:type=\"ecore:EClass\" name=\"Writer\">\n"
    "    <eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"name\"\n"
    "        eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString\"/>\n"
    "  </eClassifiers>\n"
    "</ecore:EPackage>\n";

EPackage* loadPkg(XMIResource& res, const std::string& xml) {
    res.loadFromString(xml);
    EXPECT_TRUE(!res.getContents().empty());
    return dynamic_cast<EPackage*>(res.getContents().front());
}

}  // namespace

// =====================================================================
// 1) 端到端：加载 .ecore 元模型 -> 验证 EPackage 结构
// =====================================================================
EMF_TEST(E2E_GenModel_LoadEcore_VerifyStructure) {
    initEnv();
    XMIResource res;
    auto* pkg = loadPkg(res, kLibraryEcore);
    EXPECT_NOT_NULL(pkg);
    EXPECT_EQ(pkg->getName(), std::string("library"));
    EXPECT_EQ(pkg->getNsURI(), std::string("http://example.com/e2e/genmodel/library"));
    EXPECT_EQ(pkg->getNsPrefix(), std::string("library"));
    // 3 个 classifier: Library / Book / Writer
    EXPECT_EQ(pkg->getEClassifiers().size(), 3u);
    EXPECT_NOT_NULL(pkg->getEClassifier("Library"));
    EXPECT_NOT_NULL(pkg->getEClassifier("Book"));
    EXPECT_NOT_NULL(pkg->getEClassifier("Writer"));
}

// =====================================================================
// 2) 端到端：加载 .ecore -> 实例化 Library -> 设置 name 属性
// =====================================================================
EMF_TEST(E2E_GenModel_InstantiateLibrary_SetName) {
    initEnv();
    XMIResource res;
    auto* pkg = loadPkg(res, kLibraryEcore);
    auto* libCls = dynamic_cast<EClass*>(pkg->getEClassifier("Library"));
    auto* factory = pkg->getEFactoryInstance();
    auto* lib = factory->create(libCls);
    EXPECT_NOT_NULL(lib);
    auto* nameFeat = libCls->getEStructuralFeature("name");
    EXPECT_NOT_NULL(nameFeat);
    lib->eSet(nameFeat, std::any(std::string("City Library")));
    EXPECT_EQ(std::any_cast<std::string>(lib->eGet(nameFeat)),
              std::string("City Library"));
}

// =====================================================================
// 3) 端到端：加载 .ecore -> 实例化 Library + Books -> saveToString 产生 XMI
// =====================================================================
EMF_TEST(E2E_GenModel_InstantiateAndSave_ProducesXmi) {
    initEnv();
    XMIResource res;
    auto* pkg = loadPkg(res, kLibraryEcore);
    auto* libCls = dynamic_cast<EClass*>(pkg->getEClassifier("Library"));
    auto* bookCls = dynamic_cast<EClass*>(pkg->getEClassifier("Book"));
    auto* factory = pkg->getEFactoryInstance();

    auto* lib = factory->create(libCls);
    lib->eSet(libCls->getEStructuralFeature("name"),
              std::any(std::string("Central Library")));

    auto* b0 = factory->create(bookCls);
    b0->eSet(bookCls->getEStructuralFeature("title"),
             std::any(std::string("Book A")));
    b0->eSet(bookCls->getEStructuralFeature("pages"),
             std::any(std::string("100")));

    auto* b1 = factory->create(bookCls);
    b1->eSet(bookCls->getEStructuralFeature("title"),
             std::any(std::string("Book B")));
    b1->eSet(bookCls->getEStructuralFeature("pages"),
             std::any(std::string("200")));

    auto* booksFeat = libCls->getEStructuralFeature("books");
    emf_test::addToContainment(lib, booksFeat, b0);
    emf_test::addToContainment(lib, booksFeat, b1);

    res.addToContents(lib);
    std::string out = res.saveToString();
    // 验证 XMI 输出包含关键元素和属性
    EXPECT_TRUE(out.find("library:Library") != std::string::npos);
    EXPECT_TRUE(out.find("name=\"Central Library\"") != std::string::npos);
    EXPECT_TRUE(out.find("title=\"Book A\"") != std::string::npos);
    EXPECT_TRUE(out.find("title=\"Book B\"") != std::string::npos);
    EXPECT_TRUE(out.find("pages=\"100\"") != std::string::npos);
    EXPECT_TRUE(out.find("pages=\"200\"") != std::string::npos);
}

// =====================================================================
// 4) 端到端：save .xmi -> reload -> 验证 Library name 保持
// =====================================================================
EMF_TEST(E2E_GenModel_SaveAndReload_LibraryNamePreserved) {
    initEnv();
    XMIResource res;
    auto* pkg = loadPkg(res, kLibraryEcore);
    // EPackage 已自动注册到 EPackageRegistry；从资源 contents 移除，避免与实例一起序列化
    res.getContents().clear();
    auto* libCls = dynamic_cast<EClass*>(pkg->getEClassifier("Library"));
    auto* factory = pkg->getEFactoryInstance();

    auto* lib = factory->create(libCls);
    lib->eSet(libCls->getEStructuralFeature("name"),
              std::any(std::string("Reload Test Library")));
    res.addToContents(lib);

    std::string xmi = res.saveToString();
    // 重新加载（pkg 已注册到 EPackageRegistry，实例文档可解析）
    XMIResource res2;
    res2.loadFromString(xmi);
    EXPECT_EQ(res2.getContents().size(), 1u);
    auto* loadedLib = res2.getContents().front();
    EXPECT_NOT_NULL(loadedLib);
    EXPECT_EQ(loadedLib->eClass()->getName(), std::string("Library"));
    auto nameV = loadedLib->eGet(libCls->getEStructuralFeature("name"));
    EXPECT_EQ(std::any_cast<std::string>(nameV),
              std::string("Reload Test Library"));
}

// =====================================================================
// 5) 端到端：save .xmi -> reload -> 验证 containment 子对象保持
// =====================================================================
EMF_TEST(E2E_GenModel_SaveAndReload_ContainmentChildrenPreserved) {
    initEnv();
    XMIResource res;
    auto* pkg = loadPkg(res, kLibraryEcore);
    // EPackage 已自动注册到 EPackageRegistry；从资源 contents 移除，避免与实例一起序列化
    res.getContents().clear();
    auto* libCls = dynamic_cast<EClass*>(pkg->getEClassifier("Library"));
    auto* bookCls = dynamic_cast<EClass*>(pkg->getEClassifier("Book"));
    auto* factory = pkg->getEFactoryInstance();

    auto* lib = factory->create(libCls);
    lib->eSet(libCls->getEStructuralFeature("name"),
              std::any(std::string("Lib With Books")));
    auto* b0 = factory->create(bookCls);
    b0->eSet(bookCls->getEStructuralFeature("title"),
             std::any(std::string("T1")));
    auto* b1 = factory->create(bookCls);
    b1->eSet(bookCls->getEStructuralFeature("title"),
             std::any(std::string("T2")));
    auto* booksFeat = libCls->getEStructuralFeature("books");
    emf_test::addToContainment(lib, booksFeat, b0);
    emf_test::addToContainment(lib, booksFeat, b1);
    res.addToContents(lib);

    std::string xmi = res.saveToString();
    XMIResource res2;
    res2.loadFromString(xmi);
    auto* loadedLib = res2.getContents().front();
    EXPECT_NOT_NULL(loadedLib);
    // 重新加载后 books containment 列表应有 2 个子对象
    auto loadedBooksV = loadedLib->eGet(booksFeat);
    auto* loadedList = std::any_cast<emf::common::EList<emf::common::EObject*>*>(
        loadedBooksV);
    EXPECT_NOT_NULL(loadedList);
    EXPECT_EQ(loadedList->size(), 2u);
}

// =====================================================================
// 6) 端到端：save .xmi -> reload -> 验证 Book 属性值保持
// =====================================================================
EMF_TEST(E2E_GenModel_SaveAndReload_BookAttributesPreserved) {
    initEnv();
    XMIResource res;
    auto* pkg = loadPkg(res, kLibraryEcore);
    // EPackage 已自动注册到 EPackageRegistry；从资源 contents 移除，避免与实例一起序列化
    res.getContents().clear();
    auto* libCls = dynamic_cast<EClass*>(pkg->getEClassifier("Library"));
    auto* bookCls = dynamic_cast<EClass*>(pkg->getEClassifier("Book"));
    auto* factory = pkg->getEFactoryInstance();

    auto* lib = factory->create(libCls);
    lib->eSet(libCls->getEStructuralFeature("name"),
              std::any(std::string("L")));
    auto* book = factory->create(bookCls);
    book->eSet(bookCls->getEStructuralFeature("title"),
               std::any(std::string("Deep Book")));
    book->eSet(bookCls->getEStructuralFeature("pages"),
               std::any(std::string("42")));
    emf_test::addToContainment(lib, libCls->getEStructuralFeature("books"), book);
    res.addToContents(lib);

    std::string xmi = res.saveToString();
    XMIResource res2;
    res2.loadFromString(xmi);
    auto* loadedLib = res2.getContents().front();
    auto* loadedList = std::any_cast<emf::common::EList<emf::common::EObject*>*>(
        loadedLib->eGet(libCls->getEStructuralFeature("books")));
    EXPECT_EQ(loadedList->size(), 1u);
    auto* loadedBook = (*loadedList)[0];
    EXPECT_EQ(loadedBook->eClass()->getName(), std::string("Book"));
    auto titleV = loadedBook->eGet(bookCls->getEStructuralFeature("title"));
    EXPECT_EQ(std::any_cast<std::string>(titleV), std::string("Deep Book"));
    auto pagesV = loadedBook->eGet(bookCls->getEStructuralFeature("pages"));
    // pages 是 EInt，加载器按 int 反序列化（对齐 Java EInt → Integer）
    EXPECT_EQ(std::any_cast<int>(pagesV), 42);
}

// =====================================================================
// 7) 端到端：空 Library（无子对象）save -> reload 保持
// =====================================================================
EMF_TEST(E2E_GenModel_EmptyLibrary_SaveAndReload) {
    initEnv();
    XMIResource res;
    auto* pkg = loadPkg(res, kLibraryEcore);
    // EPackage 已自动注册到 EPackageRegistry；从资源 contents 移除，避免与实例一起序列化
    res.getContents().clear();
    auto* libCls = dynamic_cast<EClass*>(pkg->getEClassifier("Library"));
    auto* factory = pkg->getEFactoryInstance();

    auto* lib = factory->create(libCls);
    lib->eSet(libCls->getEStructuralFeature("name"),
              std::any(std::string("Empty")));
    res.addToContents(lib);

    std::string xmi = res.saveToString();
    XMIResource res2;
    res2.loadFromString(xmi);
    auto* loadedLib = res2.getContents().front();
    EXPECT_EQ(loadedLib->eClass()->getName(), std::string("Library"));
    auto* loadedList = std::any_cast<emf::common::EList<emf::common::EObject*>*>(
        loadedLib->eGet(libCls->getEStructuralFeature("books")));
    EXPECT_EQ(loadedList->size(), 0u);
}

// =====================================================================
// 8) 端到端：EPackage 自动注册到 EPackageRegistry（供实例加载用）
// =====================================================================
EMF_TEST(E2E_GenModel_PackageAutoRegisteredToRegistry) {
    initEnv();
    XMIResource res;
    loadPkg(res, kLibraryEcore);
    // 加载 .ecore 后，EPackage 应自动注册到 EPackageRegistry
    auto* regPkg = emf::common::EPackageRegistry::instance().get(
        "http://example.com/e2e/genmodel/library");
    EXPECT_NOT_NULL(regPkg);
    auto* ePkg = dynamic_cast<EPackage*>(regPkg);
    EXPECT_NOT_NULL(ePkg);
    EXPECT_EQ(ePkg->getName(), std::string("library"));
}

// =====================================================================
// 9) 端到端：多根实例文档（<xmi:XMI> 包裹）save 验证
// =====================================================================
EMF_TEST(E2E_GenModel_MultiRootSave_WrappedInXmiXmi) {
    initEnv();
    XMIResource res;
    auto* pkg = loadPkg(res, kLibraryEcore);
    auto* writerCls = dynamic_cast<EClass*>(pkg->getEClassifier("Writer"));
    auto* factory = pkg->getEFactoryInstance();

    auto* w0 = factory->create(writerCls);
    w0->eSet(writerCls->getEStructuralFeature("name"),
             std::any(std::string("Author A")));
    auto* w1 = factory->create(writerCls);
    w1->eSet(writerCls->getEStructuralFeature("name"),
             std::any(std::string("Author B")));
    res.addToContents(w0);
    res.addToContents(w1);

    std::string out = res.saveToString();
    // 多根时应使用 <xmi:XMI> 包裹
    EXPECT_TRUE(out.find("<xmi:XMI") != std::string::npos);
    EXPECT_TRUE(out.find("</xmi:XMI>") != std::string::npos);
    EXPECT_TRUE(out.find("name=\"Author A\"") != std::string::npos);
    EXPECT_TRUE(out.find("name=\"Author B\"") != std::string::npos);
}

// =====================================================================
// 10) 端到端：save -> reload -> save 幂等性（两次 save 输出一致）
// =====================================================================
EMF_TEST(E2E_GenModel_SaveReloadSave_Idempotent) {
    initEnv();
    XMIResource res;
    auto* pkg = loadPkg(res, kLibraryEcore);
    auto* libCls = dynamic_cast<EClass*>(pkg->getEClassifier("Library"));
    auto* bookCls = dynamic_cast<EClass*>(pkg->getEClassifier("Book"));
    auto* factory = pkg->getEFactoryInstance();

    auto* lib = factory->create(libCls);
    lib->eSet(libCls->getEStructuralFeature("name"),
              std::any(std::string("Idempotent Lib")));
    auto* book = factory->create(bookCls);
    book->eSet(bookCls->getEStructuralFeature("title"),
               std::any(std::string("IB")));
    emf_test::addToContainment(lib, libCls->getEStructuralFeature("books"), book);
    res.addToContents(lib);

    std::string xmi1 = res.saveToString();
    XMIResource res2;
    res2.loadFromString(xmi1);
    std::string xmi2 = res2.saveToString();
    // 两次 save 输出应一致（幂等）
    EXPECT_EQ(xmi1, xmi2);
}
