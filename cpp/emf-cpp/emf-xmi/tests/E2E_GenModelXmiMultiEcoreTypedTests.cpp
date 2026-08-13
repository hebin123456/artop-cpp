// E2E_GenModelXmiMultiEcoreTypedTests.cpp —— 多 ecore 文件 typed 跨包引用测试
// 对齐 Java: org.eclipse.emf.ecore.EClass 跨包继承 + getEAllStructuralFeatures 反射
//
// 覆盖：
//   - 加载 base + ext 两个 ecore 包，验证 AnnotatedLibrary 继承 base#//Library
//   - AnnotatedLibrary 实例化（DynamicEObject 回退）
//   - 继承的 feature 可通过 getEAllStructuralFeatures 访问（name, books 来自 Library）
//   - 自有 feature 可访问（note, highlighted）
//   - 设置继承属性 name（通过反射 eSet）
//   - 向继承的 books containment 列表添加 Book 子对象
//   - 向自有的 highlighted containment 列表添加 Book 子对象
//   - 跨包 containment 的 eType 正确解析（highlighted -> base#//Book）
//   - save AnnotatedLibrary 实例为 XMI
//   - eAllContainments 包含继承和自有的 containment reference
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
using emf::ecore::EFactory;

namespace {

void initEnv() {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    XMIResourceFactory::registerDefaults();
}

// base 包：Library (name, books) + Book (title)
const char* kBaseEcore =
    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
    "<ecore:EPackage xmi:version=\"2.0\"\n"
    "    xmlns:xmi=\"http://www.omg.org/XMI\"\n"
    "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
    "    xmlns:ecore=\"http://www.eclipse.org/emf/2002/Ecore\"\n"
    "    name=\"base\" nsURI=\"http://example.com/e2e/typed/base\" nsPrefix=\"base\">\n"
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

// ext 包：AnnotatedLibrary 继承 base#//Library，含 note 属性 + highlighted containment
const char* kExtEcore =
    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
    "<ecore:EPackage xmi:version=\"2.0\"\n"
    "    xmlns:xmi=\"http://www.omg.org/XMI\"\n"
    "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
    "    xmlns:ecore=\"http://www.eclipse.org/emf/2002/Ecore\"\n"
    "    name=\"ext\" nsURI=\"http://example.com/e2e/typed/ext\" nsPrefix=\"ext\">\n"
    "  <eClassifiers xsi:type=\"ecore:EClass\" name=\"AnnotatedLibrary\"\n"
    "      eSuperTypes=\"http://example.com/e2e/typed/base#//Library\">\n"
    "    <eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"note\"\n"
    "        eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString\"/>\n"
    "    <eStructuralFeatures xsi:type=\"ecore:EReference\" name=\"highlighted\" upperBound=\"-1\"\n"
    "        eType=\"ecore:EClass http://example.com/e2e/typed/base#//Book\" containment=\"true\"/>\n"
    "  </eClassifiers>\n"
    "</ecore:EPackage>\n";

struct LoadedModel {
    EPackage* basePkg;
    EPackage* extPkg;
    EClass* libCls;
    EClass* bookCls;
    EClass* annLibCls;
};

LoadedModel loadModel() {
    LoadedModel m{};
    {
        XMIResource res;
        res.loadFromString(kBaseEcore);
        m.basePkg = dynamic_cast<EPackage*>(res.getContents().front());
    }
    {
        XMIResource res;
        res.loadFromString(kExtEcore);
        m.extPkg = dynamic_cast<EPackage*>(res.getContents().front());
    }
    m.libCls = dynamic_cast<EClass*>(m.basePkg->getEClassifier("Library"));
    m.bookCls = dynamic_cast<EClass*>(m.basePkg->getEClassifier("Book"));
    m.annLibCls = dynamic_cast<EClass*>(m.extPkg->getEClassifier("AnnotatedLibrary"));
    return m;
}

}  // namespace

// =====================================================================
// 1) 加载 base + ext -> AnnotatedLibrary 继承 Library
// =====================================================================
EMF_TEST(E2E_TypedMultiEcore_AnnotatedLibraryInheritsLibrary) {
    initEnv();
    auto m = loadModel();
    EXPECT_NOT_NULL(m.annLibCls);
    auto& supers = m.annLibCls->getESuperTypes();
    EXPECT_EQ(supers.size(), 1u);
    EXPECT_EQ(supers[0], m.libCls);
}

// =====================================================================
// 2) AnnotatedLibrary 自有 feature：note, highlighted
// =====================================================================
EMF_TEST(E2E_TypedMultiEcore_OwnFeaturesAccessible) {
    initEnv();
    auto m = loadModel();
    auto& ownFeats = m.annLibCls->getEStructuralFeatures();
    EXPECT_EQ(ownFeats.size(), 2u);
    EXPECT_NOT_NULL(m.annLibCls->getEStructuralFeature("note"));
    EXPECT_NOT_NULL(m.annLibCls->getEStructuralFeature("highlighted"));
}

// =====================================================================
// 3) AnnotatedLibrary 通过 getEAllStructuralFeatures 访问继承的 feature
// =====================================================================
EMF_TEST(E2E_TypedMultiEcore_InheritedFeaturesInAllFeatures) {
    initEnv();
    auto m = loadModel();
    auto& allFeats = m.annLibCls->getEAllStructuralFeatures();
    // 应包含继承的 name, books + 自有的 note, highlighted
    EXPECT_TRUE(allFeats.size() >= 4u);
    EXPECT_NOT_NULL(m.annLibCls->getEStructuralFeature("name"));
    EXPECT_NOT_NULL(m.annLibCls->getEStructuralFeature("books"));
    EXPECT_NOT_NULL(m.annLibCls->getEStructuralFeature("note"));
    EXPECT_NOT_NULL(m.annLibCls->getEStructuralFeature("highlighted"));
}

// =====================================================================
// 4) AnnotatedLibrary 实例化 -> eClass() 返回 AnnotatedLibrary
// =====================================================================
EMF_TEST(E2E_TypedMultiEcore_InstantiateAnnotatedLibrary) {
    initEnv();
    auto m = loadModel();
    auto* factory = m.extPkg->getEFactoryInstance();
    auto* obj = factory->create(m.annLibCls);
    EXPECT_NOT_NULL(obj);
    EXPECT_EQ(obj->eClass(), m.annLibCls);
    EXPECT_EQ(obj->eClass()->getName(), std::string("AnnotatedLibrary"));
}

// =====================================================================
// 5) 设置继承属性 name（通过反射 eSet）
// =====================================================================
EMF_TEST(E2E_TypedMultiEcore_SetInheritedAttribute) {
    initEnv();
    auto m = loadModel();
    auto* factory = m.extPkg->getEFactoryInstance();
    auto* obj = factory->create(m.annLibCls);
    auto* nameFeat = m.annLibCls->getEStructuralFeature("name");
    EXPECT_NOT_NULL(nameFeat);
    obj->eSet(nameFeat, std::any(std::string("Typed Library")));
    auto v = obj->eGet(nameFeat);
    EXPECT_EQ(std::any_cast<std::string>(v), std::string("Typed Library"));
}

// =====================================================================
// 6) 设置自有属性 note
// =====================================================================
EMF_TEST(E2E_TypedMultiEcore_SetOwnAttribute) {
    initEnv();
    auto m = loadModel();
    auto* factory = m.extPkg->getEFactoryInstance();
    auto* obj = factory->create(m.annLibCls);
    auto* noteFeat = m.annLibCls->getEStructuralFeature("note");
    EXPECT_NOT_NULL(noteFeat);
    obj->eSet(noteFeat, std::any(std::string("A note")));
    auto v = obj->eGet(noteFeat);
    EXPECT_EQ(std::any_cast<std::string>(v), std::string("A note"));
}

// =====================================================================
// 7) 向继承的 books containment 列表添加 Book（base 包的 Book）
// =====================================================================
EMF_TEST(E2E_TypedMultiEcore_AddToInheritedContainment) {
    initEnv();
    auto m = loadModel();
    auto* extFactory = m.extPkg->getEFactoryInstance();
    auto* baseFactory = m.basePkg->getEFactoryInstance();
    auto* annLib = extFactory->create(m.annLibCls);
    auto* book = baseFactory->create(m.bookCls);
    book->eSet(m.bookCls->getEStructuralFeature("title"),
               std::any(std::string("Inherited Book")));
    auto* booksFeat = m.annLibCls->getEStructuralFeature("books");
    auto* listPtr = std::any_cast<emf::common::EList<emf::common::EObject*>*>(
        annLib->eGet(booksFeat));
    EXPECT_NOT_NULL(listPtr);
    listPtr->add(book);
    EXPECT_EQ(listPtr->size(), 1u);
    EXPECT_EQ((*listPtr)[0], book);
}

// =====================================================================
// 8) 向自有的 highlighted containment 列表添加 Book
// =====================================================================
EMF_TEST(E2E_TypedMultiEcore_AddToOwnContainment) {
    initEnv();
    auto m = loadModel();
    auto* extFactory = m.extPkg->getEFactoryInstance();
    auto* baseFactory = m.basePkg->getEFactoryInstance();
    auto* annLib = extFactory->create(m.annLibCls);
    auto* book = baseFactory->create(m.bookCls);
    auto* highlightedFeat = m.annLibCls->getEStructuralFeature("highlighted");
    auto* listPtr = std::any_cast<emf::common::EList<emf::common::EObject*>*>(
        annLib->eGet(highlightedFeat));
    EXPECT_NOT_NULL(listPtr);
    listPtr->add(book);
    EXPECT_EQ(listPtr->size(), 1u);
}

// =====================================================================
// 9) 跨包 containment 的 eType 解析：highlighted -> base#//Book
// =====================================================================
EMF_TEST(E2E_TypedMultiEcore_CrossPackageContainmentEType) {
    initEnv();
    auto m = loadModel();
    auto* highlightedFeat = m.annLibCls->getEStructuralFeature("highlighted");
    auto* ref = dynamic_cast<EReference*>(highlightedFeat);
    EXPECT_NOT_NULL(ref);
    EXPECT_TRUE(ref->isContainment());
    EXPECT_EQ(ref->getEReferenceType(), m.bookCls);
}

// =====================================================================
// 10) eAllContainments 包含继承和自有的 containment reference
// =====================================================================
EMF_TEST(E2E_TypedMultiEcore_AllContainmentsIncludeInherited) {
    initEnv();
    auto m = loadModel();
    auto& allContainments = m.annLibCls->getEAllContainments();
    // 应包含 books (继承) 和 highlighted (自有)
    EXPECT_TRUE(allContainments.size() >= 2u);
    bool hasBooks = false;
    bool hasHighlighted = false;
    for (auto* ref : allContainments) {
        if (ref->getName() == "books") hasBooks = true;
        if (ref->getName() == "highlighted") hasHighlighted = true;
    }
    EXPECT_TRUE(hasBooks);
    EXPECT_TRUE(hasHighlighted);
}

// =====================================================================
// 11) save AnnotatedLibrary 实例为 XMI
// =====================================================================
EMF_TEST(E2E_TypedMultiEcore_SaveAnnotatedLibraryInstance) {
    initEnv();
    auto m = loadModel();
    auto* extFactory = m.extPkg->getEFactoryInstance();
    auto* annLib = extFactory->create(m.annLibCls);
    annLib->eSet(m.annLibCls->getEStructuralFeature("name"),
                 std::any(std::string("Saved AnnLib")));
    annLib->eSet(m.annLibCls->getEStructuralFeature("note"),
                 std::any(std::string("Note text")));

    XMIResource res;
    res.addToContents(annLib);
    std::string out = res.saveToString();
    EXPECT_TRUE(out.find("ext:AnnotatedLibrary") != std::string::npos);
    EXPECT_TRUE(out.find("name=\"Saved AnnLib\"") != std::string::npos);
    EXPECT_TRUE(out.find("note=\"Note text\"") != std::string::npos);
}

// =====================================================================
// 12) 两个 EPackage 的 EFactoryInstance 独立
// =====================================================================
EMF_TEST(E2E_TypedMultiEcore_FactoriesIndependent) {
    initEnv();
    auto m = loadModel();
    auto* baseFactory = m.basePkg->getEFactoryInstance();
    auto* extFactory = m.extPkg->getEFactoryInstance();
    EXPECT_NOT_NULL(baseFactory);
    EXPECT_NOT_NULL(extFactory);
    EXPECT_NE(static_cast<void*>(baseFactory), static_cast<void*>(extFactory));
    // base factory 创建 Book，ext factory 创建 AnnotatedLibrary
    auto* book = baseFactory->create(m.bookCls);
    auto* annLib = extFactory->create(m.annLibCls);
    EXPECT_EQ(book->eClass(), m.bookCls);
    EXPECT_EQ(annLib->eClass(), m.annLibCls);
}
