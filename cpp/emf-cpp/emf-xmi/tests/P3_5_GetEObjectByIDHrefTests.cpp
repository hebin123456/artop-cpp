// P3_5_GetEObjectByIDHrefTests.cpp —— ID / href / position path 查找测试
// 对齐 Java: org.eclipse.emf.ecore.xmi.impl.XMLHelperImpl.getID/getHREF
//           + XMIResource.getEObject(String) + resolvePositionPath
//
// 覆盖：
//   - XMIResource.setID / getID / getEObjectByID 的双向映射
//   - xmi:id 在加载时自动注册，可通过 getEObjectByID 查到
//   - getEObject(fragment) 多种形态：
//       "?id"   -> 按 xmi:id 查
//       "Name"  -> 按 classifier 名 / 根对象 eClass 名查
//       "//Name"-> 同上（去前导 /）
//   - resolvePositionPath：@feat.index 形式的位置路径
#include "test_main.h"
#include "emf/xmi/XMIResource.h"
#include "emf/xmi/XMIResourceFactory.h"
#include "emf/ecore/EcorePackage.h"
#include "emf/ecore/EcoreImpls.h"
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

namespace {

void initEnv() {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    XMIResourceFactory::registerDefaults();
}

const char* kLibraryEcore =
    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
    "<ecore:EPackage xmi:version=\"2.0\"\n"
    "    xmlns:xmi=\"http://www.omg.org/XMI\"\n"
    "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
    "    xmlns:ecore=\"http://www.eclipse.org/emf/2002/Ecore\"\n"
    "    name=\"library\" nsURI=\"http://example.com/library/1.0\" nsPrefix=\"library\">\n"
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
    "</ecore:EPackage>\n";

EPackage* loadPkg(XMIResource& res, const std::string& xml) {
    res.loadFromString(xml);
    EXPECT_TRUE(!res.getContents().empty());
    return dynamic_cast<EPackage*>(res.getContents().front());
}

}  // namespace

// =====================================================================
// 1) setID / getID 双向映射
// =====================================================================
EMF_TEST(GetEObjectByID_SetGetID_Roundtrip) {
    initEnv();
    XMIResource res;
    auto* pkg = EcoreFactory::instance().createEPackage();
    res.addToContents(pkg);
    res.setID(pkg, "pkg1");
    EXPECT_EQ(res.getID(pkg), std::string("pkg1"));
    EXPECT_EQ(res.getEObjectByID("pkg1"),
              static_cast<emf::common::EObject*>(pkg));
}

// =====================================================================
// 2) setID 覆盖：新 id 替换旧 id，旧 id 不再可查
// =====================================================================
EMF_TEST(GetEObjectByID_SetID_OverwritesOld) {
    initEnv();
    XMIResource res;
    auto* pkg = EcoreFactory::instance().createEPackage();
    res.addToContents(pkg);
    res.setID(pkg, "old");
    EXPECT_EQ(res.getID(pkg), std::string("old"));
    res.setID(pkg, "new");
    EXPECT_EQ(res.getID(pkg), std::string("new"));
    EXPECT_NULL(res.getEObjectByID("old"));
    EXPECT_EQ(res.getEObjectByID("new"),
              static_cast<emf::common::EObject*>(pkg));
}

// =====================================================================
// 3) getEObjectByID 未知 id 返回 nullptr
// =====================================================================
EMF_TEST(GetEObjectByID_UnknownID_ReturnsNull) {
    initEnv();
    XMIResource res;
    EXPECT_NULL(res.getEObjectByID("does-not-exist"));
}

// =====================================================================
// 4) getID 未注册对象返回空串
// =====================================================================
EMF_TEST(GetEObjectByID_GetID_UnregisteredObject_EmptyString) {
    initEnv();
    XMIResource res;
    auto* pkg = EcoreFactory::instance().createEPackage();
    EXPECT_EQ(res.getID(pkg), std::string(""));
}

// =====================================================================
// 5) xmi:id 加载时自动注册：getEObjectByID 查到 classifier
// =====================================================================
EMF_TEST(GetEObjectByID_XmiIdRegisteredOnLoad) {
    initEnv();
    const char* xml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        "<ecore:EPackage xmi:version=\"2.0\"\n"
        "    xmlns:xmi=\"http://www.omg.org/XMI\"\n"
        "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
        "    xmlns:ecore=\"http://www.eclipse.org/emf/2002/Ecore\"\n"
        "    name=\"l\" nsURI=\"http://example.com/l\" nsPrefix=\"l\">\n"
        "  <eClassifiers xsi:type=\"ecore:EClass\" name=\"A\" xmi:id=\"_idA\"/>\n"
        "  <eClassifiers xsi:type=\"ecore:EClass\" name=\"B\" xmi:id=\"_idB\"/>\n"
        "</ecore:EPackage>\n";
    XMIResource res;
    auto* pkg = loadPkg(res, xml);
    auto* a = dynamic_cast<EClass*>(pkg->getEClassifier("A"));
    auto* b = dynamic_cast<EClass*>(pkg->getEClassifier("B"));
    EXPECT_EQ(res.getID(a), std::string("_idA"));
    EXPECT_EQ(res.getID(b), std::string("_idB"));
    EXPECT_EQ(res.getEObjectByID("_idA"), static_cast<emf::common::EObject*>(a));
    EXPECT_EQ(res.getEObjectByID("_idB"), static_cast<emf::common::EObject*>(b));
}

// =====================================================================
// 6) getEObject("?id") 形式：按 xmi:id 查找
// =====================================================================
EMF_TEST(GetEObject_QuestionMarkId_Lookup) {
    initEnv();
    const char* xml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        "<ecore:EPackage xmi:version=\"2.0\"\n"
        "    xmlns:xmi=\"http://www.omg.org/XMI\"\n"
        "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
        "    xmlns:ecore=\"http://www.eclipse.org/emf/2002/Ecore\"\n"
        "    name=\"l\" nsURI=\"http://example.com/l\" nsPrefix=\"l\">\n"
        "  <eClassifiers xsi:type=\"ecore:EClass\" name=\"A\" xmi:id=\"_idA\"/>\n"
        "</ecore:EPackage>\n";
    XMIResource res;
    auto* pkg = loadPkg(res, xml);
    auto* a = dynamic_cast<EClass*>(pkg->getEClassifier("A"));
    EXPECT_EQ(res.getEObject("?_idA"), static_cast<emf::common::EObject*>(a));
}

// =====================================================================
// 7) getEObject("ClassName")：按 classifier 名查找
// =====================================================================
EMF_TEST(GetEObject_ByClassName_Lookup) {
    initEnv();
    XMIResource res;
    auto* pkg = loadPkg(res, kLibraryEcore);
    auto* libCls = dynamic_cast<EClass*>(pkg->getEClassifier("Library"));
    EXPECT_EQ(res.getEObject("Library"),
              static_cast<emf::common::EObject*>(libCls));
    auto* bookCls = dynamic_cast<EClass*>(pkg->getEClassifier("Book"));
    EXPECT_EQ(res.getEObject("Book"),
              static_cast<emf::common::EObject*>(bookCls));
}

// =====================================================================
// 8) getEObject("//ClassName")：去前导 / 后按名查找
// =====================================================================
EMF_TEST(GetEObject_DoubleSlashClassName_Lookup) {
    initEnv();
    XMIResource res;
    auto* pkg = loadPkg(res, kLibraryEcore);
    auto* libCls = dynamic_cast<EClass*>(pkg->getEClassifier("Library"));
    EXPECT_EQ(res.getEObject("//Library"),
              static_cast<emf::common::EObject*>(libCls));
}

// =====================================================================
// 9) getEObject 空串返回 nullptr
// =====================================================================
EMF_TEST(GetEObject_EmptyFragment_ReturnsNull) {
    initEnv();
    XMIResource res;
    EXPECT_NULL(res.getEObject(""));
}

// =====================================================================
// 10) getEObject 未知名字返回 nullptr
// =====================================================================
EMF_TEST(GetEObject_UnknownName_ReturnsNull) {
    initEnv();
    XMIResource res;
    loadPkg(res, kLibraryEcore);
    EXPECT_NULL(res.getEObject("NoSuchClass"));
}

// =====================================================================
// 11) resolvePositionPath：@books.0 取第一个 containment 子对象
// =====================================================================
EMF_TEST(GetEObjectByID_ResolvePositionPath_FirstChild) {
    initEnv();
    XMIResource res;
    auto* pkg = loadPkg(res, kLibraryEcore);
    // EPackage 已自动注册到 EPackageRegistry；从资源 contents 移除，避免与实例一起序列化
    res.getContents().clear();
    // 用 factory 构造 Library + 2 个 Book
    auto* libCls = dynamic_cast<EClass*>(pkg->getEClassifier("Library"));
    auto* bookCls = dynamic_cast<EClass*>(pkg->getEClassifier("Book"));
    auto* factory = pkg->getEFactoryInstance();
    auto* lib = factory->create(libCls);
    auto* b0 = factory->create(bookCls);
    auto* b1 = factory->create(bookCls);
    auto* booksFeat = libCls->getEStructuralFeature("books");
    // 多值 containment：eGet 返回内部 list 指针，push_back 即生效
    emf_test::addToContainment(lib, booksFeat, b0);
    emf_test::addToContainment(lib, booksFeat, b1);
    res.addToContents(lib);

    // resolvePositionPath("@books.0") -> b0
    auto* got0 = res.resolvePositionPath("@books.0");
    EXPECT_EQ(got0, b0);
    // resolvePositionPath("@books.1") -> b1
    auto* got1 = res.resolvePositionPath("@books.1");
    EXPECT_EQ(got1, b1);
}

// =====================================================================
// 12) resolvePositionPath：classifier 名字直接查找（EPackage contents）
// =====================================================================
EMF_TEST(GetEObjectByID_ResolvePositionPath_ClassifierName) {
    initEnv();
    XMIResource res;
    auto* pkg = loadPkg(res, kLibraryEcore);
    auto* libCls = dynamic_cast<EClass*>(pkg->getEClassifier("Library"));
    // path 不以 @ 开头：在 EPackage classifiers 中按名查
    EXPECT_EQ(res.resolvePositionPath("Library"),
              static_cast<emf::common::EObject*>(libCls));
    EXPECT_EQ(res.resolvePositionPath("Book"),
              static_cast<emf::common::EObject*>(pkg->getEClassifier("Book")));
}

// =====================================================================
// 13) idToEObjectMap getter：包含所有已注册 id
// =====================================================================
EMF_TEST(GetEObjectByID_IDMapContainsAllRegistered) {
    initEnv();
    XMIResource res;
    auto* a = EcoreFactory::instance().createEClass();
    auto* b = EcoreFactory::instance().createEClass();
    res.setID(a, "a");
    res.setID(b, "b");
    const auto& map = res.getIDToEObjectMap();
    EXPECT_EQ(map.size(), 2u);
    EXPECT_EQ(map.at("a"), static_cast<emf::common::EObject*>(a));
    EXPECT_EQ(map.at("b"), static_cast<emf::common::EObject*>(b));
}
