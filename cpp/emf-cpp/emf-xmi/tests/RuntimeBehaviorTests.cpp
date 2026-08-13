// RuntimeBehaviorTests.cpp —— 动态 EObject 运行时反射行为测试
// 对齐 Java: org.eclipse.emf.ecore.impl.DynamicEObjectImpl 的反射语义
//
// 覆盖：
//   - 动态 EObject eGet/eSet（单值 EAttribute）
//   - eIsSet / eUnset 行为
//   - containment EReference 子对象自动设置 eContainer / eContainingFeature
//   - 多值 EReference 的 list 访问
//   - eClass() 反射返回正确 EClass
//   - eContents() 收集 containment 子对象
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
using emf::ecore::DynamicEObject;

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
    "    <eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"pages\"\n"
    "        eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EInt\"/>\n"
    "  </eClassifiers>\n"
    "</ecore:EPackage>\n";

EPackage* loadPkg(XMIResource& res, const std::string& xml) {
    res.loadFromString(xml);
    EXPECT_TRUE(!res.getContents().empty());
    return dynamic_cast<EPackage*>(res.getContents().front());
}

}  // namespace

// =====================================================================
// 1) 动态 EObject：eClass() 返回正确 EClass
// =====================================================================
EMF_TEST(Runtime_DynamicEObject_eClass_ReturnsCorrectClass) {
    initEnv();
    XMIResource res;
    auto* pkg = loadPkg(res, kLibraryEcore);
    auto* libCls = dynamic_cast<EClass*>(pkg->getEClassifier("Library"));
    auto* factory = pkg->getEFactoryInstance();
    auto* lib = factory->create(libCls);
    EXPECT_NOT_NULL(lib);
    EXPECT_EQ(lib->eClass(), libCls);
    EXPECT_EQ(lib->eClass()->getName(), std::string("Library"));
}

// =====================================================================
// 2) 动态 EObject：单值 EAttribute eSet/eGet
// =====================================================================
EMF_TEST(Runtime_DynamicEObject_eSet_eGet_SingleAttribute) {
    initEnv();
    XMIResource res;
    auto* pkg = loadPkg(res, kLibraryEcore);
    auto* libCls = dynamic_cast<EClass*>(pkg->getEClassifier("Library"));
    auto* factory = pkg->getEFactoryInstance();
    auto* lib = factory->create(libCls);
    auto* nameFeat = libCls->getEStructuralFeature("name");
    EXPECT_NOT_NULL(nameFeat);
    lib->eSet(nameFeat, std::any(std::string("Central Library")));
    auto v = lib->eGet(nameFeat);
    EXPECT_EQ(std::any_cast<std::string>(v), std::string("Central Library"));
}

// =====================================================================
// 3) 动态 EObject：eSet 覆盖旧值
// =====================================================================
EMF_TEST(Runtime_DynamicEObject_eSet_OverwritesValue) {
    initEnv();
    XMIResource res;
    auto* pkg = loadPkg(res, kLibraryEcore);
    auto* bookCls = dynamic_cast<EClass*>(pkg->getEClassifier("Book"));
    auto* factory = pkg->getEFactoryInstance();
    auto* book = factory->create(bookCls);
    auto* titleFeat = bookCls->getEStructuralFeature("title");
    book->eSet(titleFeat, std::any(std::string("First")));
    book->eSet(titleFeat, std::any(std::string("Second")));
    EXPECT_EQ(std::any_cast<std::string>(book->eGet(titleFeat)),
              std::string("Second"));
}

// =====================================================================
// 4) 动态 EObject：eIsSet 在 eSet 前后变化
// =====================================================================
EMF_TEST(Runtime_DynamicEObject_eIsSet_BeforeAndAfterSet) {
    initEnv();
    XMIResource res;
    auto* pkg = loadPkg(res, kLibraryEcore);
    auto* bookCls = dynamic_cast<EClass*>(pkg->getEClassifier("Book"));
    auto* factory = pkg->getEFactoryInstance();
    auto* book = factory->create(bookCls);
    auto* titleFeat = bookCls->getEStructuralFeature("title");
    // 初始未设置：eIsSet 应为 false
    bool before = book->eIsSet(titleFeat);
    EXPECT_FALSE(before);
    book->eSet(titleFeat, std::any(std::string("A Title")));
    EXPECT_TRUE(book->eIsSet(titleFeat));
}

// =====================================================================
// 5) 动态 EObject：eUnset 后 eIsSet 为 false
// =====================================================================
EMF_TEST(Runtime_DynamicEObject_eUnset_ClearsIsSet) {
    initEnv();
    XMIResource res;
    auto* pkg = loadPkg(res, kLibraryEcore);
    auto* bookCls = dynamic_cast<EClass*>(pkg->getEClassifier("Book"));
    auto* factory = pkg->getEFactoryInstance();
    auto* book = factory->create(bookCls);
    auto* titleFeat = bookCls->getEStructuralFeature("title");
    book->eSet(titleFeat, std::any(std::string("Title")));
    EXPECT_TRUE(book->eIsSet(titleFeat));
    book->eUnset(titleFeat);
    EXPECT_FALSE(book->eIsSet(titleFeat));
}

// =====================================================================
// 6) 多值 containment EReference：eGet 返回 list 指针，push_back 生效
// =====================================================================
EMF_TEST(Runtime_MultiValueContainment_ListAccess) {
    initEnv();
    XMIResource res;
    auto* pkg = loadPkg(res, kLibraryEcore);
    auto* libCls = dynamic_cast<EClass*>(pkg->getEClassifier("Library"));
    auto* bookCls = dynamic_cast<EClass*>(pkg->getEClassifier("Book"));
    auto* factory = pkg->getEFactoryInstance();
    auto* lib = factory->create(libCls);
    auto* booksFeat = libCls->getEStructuralFeature("books");
    auto v = lib->eGet(booksFeat);
    auto* listPtr = std::any_cast<emf::common::EList<emf::common::EObject*>*>(v);
    EXPECT_NOT_NULL(listPtr);
    EXPECT_EQ(listPtr->size(), 0u);
    auto* b0 = factory->create(bookCls);
    auto* b1 = factory->create(bookCls);
    listPtr->add(b0);
    listPtr->add(b1);
    EXPECT_EQ(listPtr->size(), 2u);
    EXPECT_EQ((*listPtr)[0], b0);
    EXPECT_EQ((*listPtr)[1], b1);
}

// =====================================================================
// 7) containment 子对象：eContainer 自动指向父对象
// =====================================================================
EMF_TEST(Runtime_Containment_eContainer_AutoSet) {
    initEnv();
    XMIResource res;
    auto* pkg = loadPkg(res, kLibraryEcore);
    auto* libCls = dynamic_cast<EClass*>(pkg->getEClassifier("Library"));
    auto* bookCls = dynamic_cast<EClass*>(pkg->getEClassifier("Book"));
    auto* factory = pkg->getEFactoryInstance();
    auto* lib = factory->create(libCls);
    auto* booksFeat = libCls->getEStructuralFeature("books");
    auto* book = factory->create(bookCls);
    emf_test::addToContainment(lib, booksFeat, book);
    // containment add 后 book.eContainer() 应为 lib
    EXPECT_EQ(book->eContainer(), static_cast<emf::common::EObject*>(lib));
}

// =====================================================================
// 8) containment 子对象：eContainingFeature 自动指向 books feature
// =====================================================================
EMF_TEST(Runtime_Containment_eContainingFeature_AutoSet) {
    initEnv();
    XMIResource res;
    auto* pkg = loadPkg(res, kLibraryEcore);
    auto* libCls = dynamic_cast<EClass*>(pkg->getEClassifier("Library"));
    auto* bookCls = dynamic_cast<EClass*>(pkg->getEClassifier("Book"));
    auto* factory = pkg->getEFactoryInstance();
    auto* lib = factory->create(libCls);
    auto* booksFeat = libCls->getEStructuralFeature("books");
    auto* book = factory->create(bookCls);
    emf_test::addToContainment(lib, booksFeat, book);
    EXPECT_EQ(book->eContainingFeature(), booksFeat);
    EXPECT_EQ(book->eContainmentFeature(), booksFeat);
}

// =====================================================================
// 9) eContents()：收集 containment 子对象
// =====================================================================
EMF_TEST(Runtime_eContents_CollectsContainmentChildren) {
    initEnv();
    XMIResource res;
    auto* pkg = loadPkg(res, kLibraryEcore);
    auto* libCls = dynamic_cast<EClass*>(pkg->getEClassifier("Library"));
    auto* bookCls = dynamic_cast<EClass*>(pkg->getEClassifier("Book"));
    auto* factory = pkg->getEFactoryInstance();
    auto* lib = factory->create(libCls);
    auto* booksFeat = libCls->getEStructuralFeature("books");
    auto* b0 = factory->create(bookCls);
    auto* b1 = factory->create(bookCls);
    emf_test::addToContainment(lib, booksFeat, b0);
    emf_test::addToContainment(lib, booksFeat, b1);
    const auto& contents = lib->eContents();
    EXPECT_EQ(contents.size(), 2u);
}

// =====================================================================
// 10) 动态 EObject：eGet 未设置的单值属性返回默认（不抛异常）
// =====================================================================
EMF_TEST(Runtime_DynamicEObject_eGet_UnsetSingle_NoThrow) {
    initEnv();
    XMIResource res;
    auto* pkg = loadPkg(res, kLibraryEcore);
    auto* bookCls = dynamic_cast<EClass*>(pkg->getEClassifier("Book"));
    auto* factory = pkg->getEFactoryInstance();
    auto* book = factory->create(bookCls);
    auto* titleFeat = bookCls->getEStructuralFeature("title");
    // 未 eSet 直接 eGet：不应抛异常
    EXPECT_FALSE(book->eIsSet(titleFeat));
    auto v = book->eGet(titleFeat);
    // 返回值可能为空 any 或默认值，仅验证不抛异常
    (void)v;
}

// =====================================================================
// 11) eResource：未加入 resource 时沿 eContainer 链查找
// =====================================================================
EMF_TEST(Runtime_eResource_NullWhenNotInResource) {
    initEnv();
    XMIResource res;
    auto* pkg = loadPkg(res, kLibraryEcore);
    auto* bookCls = dynamic_cast<EClass*>(pkg->getEClassifier("Book"));
    auto* factory = pkg->getEFactoryInstance();
    auto* book = factory->create(bookCls);
    // book 未加入任何 resource，eResource 应为 nullptr
    EXPECT_NULL(book->eResource());
}

// =====================================================================
// 12) DynamicEObject 是 DynamicEObject 类型（动态实例化回退）
// =====================================================================
EMF_TEST(Runtime_DynamicEObject_TypeConfirmed) {
    initEnv();
    XMIResource res;
    auto* pkg = loadPkg(res, kLibraryEcore);
    auto* libCls = dynamic_cast<EClass*>(pkg->getEClassifier("Library"));
    auto* factory = pkg->getEFactoryInstance();
    auto* lib = factory->create(libCls);
    // 用户模型类（Library/Book）无生成静态类 -> 回退到 DynamicEObject
    auto* dyn = dynamic_cast<DynamicEObject*>(lib);
    EXPECT_NOT_NULL(dyn);
}

// =====================================================================
// 13) P0-2: XMILoader 解析 EOperation / EParameter / EAnnotation
//     对齐 Java SAXXMIHandler：加载 .ecore 时完整解析元模型元素，不丢失 EOperation 等。
// =====================================================================
EMF_TEST(Runtime_LoadEcore_EOperationAndAnnotation_Preserved) {
    initEnv();
    const char* kEcoreWithOpAndAnn =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        "<ecore:EPackage xmi:version=\"2.0\"\n"
        "    xmlns:xmi=\"http://www.omg.org/XMI\"\n"
        "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
        "    xmlns:ecore=\"http://www.eclipse.org/emf/2002/Ecore\"\n"
        "    name=\"srv\" nsURI=\"http://example.com/srv/1.0\" nsPrefix=\"srv\">\n"
        "  <eClassifiers xsi:type=\"ecore:EClass\" name=\"Service\">\n"
        "    <eAnnotations source=\"http://example.com/doc\">\n"
        "      <details key=\"author\" value=\"emf\"/>\n"
        "      <details key=\"version\" value=\"2\"/>\n"
        "    </eAnnotations>\n"
        "    <eOperations name=\"invoke\" lowerBound=\"0\" upperBound=\"1\"\n"
        "        eType=\"#//Result\">\n"
        "      <eParameters name=\"request\" eType=\"#//Request\"/>\n"
        "    </eOperations>\n"
        "    <eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"id\"\n"
        "        eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString\"/>\n"
        "  </eClassifiers>\n"
        "  <eClassifiers xsi:type=\"ecore:EClass\" name=\"Result\"/>\n"
        "  <eClassifiers xsi:type=\"ecore:EClass\" name=\"Request\"/>\n"
        "</ecore:EPackage>\n";
    XMIResource res;
    auto* pkg = loadPkg(res, kEcoreWithOpAndAnn);
    EXPECT_NOT_NULL(pkg);

    auto* svc = dynamic_cast<EClass*>(pkg->getEClassifier("Service"));
    EXPECT_NOT_NULL(svc);

    // EOperation 解析
    const auto& ops = svc->getEAllOperations();
    EXPECT_EQ(ops.size(), 1u);
    if (!ops.empty()) {
        auto* op = ops[0];
        EXPECT_EQ(op->getName(), std::string("invoke"));
        // eType 延迟解析后应指向 Result
        auto* retType = op->getEType();
        EXPECT_NOT_NULL(retType);
        if (retType) EXPECT_EQ(retType->getName(), std::string("Result"));
        // EParameter 解析
        const auto& params = op->getEParameters();
        EXPECT_EQ(params.size(), 1u);
        if (!params.empty()) {
            auto* p = params[0];
            EXPECT_EQ(p->getName(), std::string("request"));
            auto* pType = p->getEType();
            EXPECT_NOT_NULL(pType);
            if (pType) EXPECT_EQ(pType->getName(), std::string("Request"));
        }
    }

    // EAnnotation 解析（EModelElement.getEAnnotations）
    const auto& anns = svc->getEAnnotations();
    EXPECT_EQ(anns.size(), 1u);
    if (!anns.empty()) {
        auto* ann = anns[0];
        EXPECT_EQ(ann->getSource(), std::string("http://example.com/doc"));
        EXPECT_EQ(ann->getDetail("author"), std::string("emf"));
        EXPECT_EQ(ann->getDetail("version"), std::string("2"));
    }

    // EStructuralFeature 仍正常（不因新 builder 退化）
    auto* idSf = svc->getEStructuralFeature("id");
    EXPECT_NOT_NULL(idSf);
}

// =====================================================================
// 14) P0-2: XMILoader 解析 ETypeParameter / EGenericType (eGenericSuperTypes)
//     对齐 Java：泛型类型参数 + 泛型超类型（走 eSuperTypes 派生路径）。
// =====================================================================
EMF_TEST(Runtime_LoadEcore_TypeParameterAndGenericSuper_Preserved) {
    initEnv();
    const char* kEcoreGeneric =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        "<ecore:EPackage xmi:version=\"2.0\"\n"
        "    xmlns:xmi=\"http://www.omg.org/XMI\"\n"
        "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
        "    xmlns:ecore=\"http://www.eclipse.org/emf/2002/Ecore\"\n"
        "    name=\"gen\" nsURI=\"http://example.com/gen/1.0\" nsPrefix=\"gen\">\n"
        "  <eClassifiers xsi:type=\"ecore:EClass\" name=\"Base\"/>\n"
        "  <eClassifiers xsi:type=\"ecore:EClass\" name=\"Container\">\n"
        "    <eTypeParameters name=\"T\"/>\n"
        "    <eGenericSuperTypes eClassifier=\"#//Base\"/>\n"
        "  </eClassifiers>\n"
        "</ecore:EPackage>\n";
    XMIResource res;
    auto* pkg = loadPkg(res, kEcoreGeneric);
    EXPECT_NOT_NULL(pkg);

    auto* cont = dynamic_cast<EClass*>(pkg->getEClassifier("Container"));
    EXPECT_NOT_NULL(cont);
    if (cont) {
        // ETypeParameter 解析（EClassifier.getETypeParameters）
        auto& tps = cont->getETypeParameters();
        EXPECT_EQ(tps.size(), 1u);
        if (!tps.empty()) EXPECT_EQ(tps[0]->getName(), std::string("T"));

        // eGenericSuperTypes -> eSuperTypes 派生路径
        const auto& supers = cont->getESuperTypes();
        EXPECT_EQ(supers.size(), 1u);
        if (!supers.empty()) {
            EXPECT_EQ(supers[0]->getName(), std::string("Base"));
        }
    }
}
