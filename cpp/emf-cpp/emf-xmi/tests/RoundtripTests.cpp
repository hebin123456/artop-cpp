// RoundtripTests.cpp —— load -> save -> reload 等价性测试
// 对齐 Java: org.eclipse.emf.ecore.xmi.impl.XMLSaveImpl/XMLLoadImpl 的双向一致性
//
// 验证：
//   - 加载 .ecore -> save -> reload 后 EPackage 结构（name/nsURI/classifiers）保持一致
//   - 加载实例 -> save -> reload 后 EObject 树保持一致
//   - name/type/features 在 roundtrip 后不丢失
#include "test_main.h"
#include "emf/xmi/XMIResource.h"
#include "emf/xmi/XMIResourceFactory.h"
#include "emf/ecore/EcorePackage.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/common/EPackageRegistry.h"

#include <string>

using emf::xmi::XMIResource;
using emf::xmi::XMIResourceFactory;
using emf::ecore::EcoreFactory;
using emf::ecore::EcorePackage;
using emf::ecore::EPackage;
using emf::ecore::EClass;
using emf::ecore::EAttribute;
using emf::ecore::EReference;
using emf::ecore::EEnum;

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
    "        eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EInt\"\n"
    "        defaultValueLiteral=\"0\"/>\n"
    "  </eClassifiers>\n"
    "</ecore:EPackage>\n";

const char* kEnumEcore =
    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
    "<ecore:EPackage xmi:version=\"2.0\"\n"
    "    xmlns:xmi=\"http://www.omg.org/XMI\"\n"
    "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
    "    xmlns:ecore=\"http://www.eclipse.org/emf/2002/Ecore\"\n"
    "    name=\"enums\" nsURI=\"http://example.com/enums/1.0\" nsPrefix=\"en\">\n"
    "  <eClassifiers xsi:type=\"ecore:EEnum\" name=\"Color\">\n"
    "    <eLiterals name=\"Red\" value=\"0\" literal=\"RED\"/>\n"
    "    <eLiterals name=\"Green\" value=\"1\" literal=\"GREEN\"/>\n"
    "  </eClassifiers>\n"
    "</ecore:EPackage>\n";

EPackage* loadPkg(XMIResource& res, const std::string& xml) {
    res.loadFromString(xml);
    EXPECT_TRUE(!res.getContents().empty());
    return dynamic_cast<EPackage*>(res.getContents().front());
}

}  // namespace

// =====================================================================
// 1) Roundtrip .ecore：name/nsURI/nsPrefix 保持一致
// =====================================================================
EMF_TEST(Roundtrip_Ecore_PackageMetadataPreserved) {
    initEnv();
    XMIResource res1;
    auto* pkg1 = loadPkg(res1, kLibraryEcore);
    EXPECT_NOT_NULL(pkg1);
    std::string saved = res1.saveToString();

    XMIResource res2;
    auto* pkg2 = loadPkg(res2, saved);
    EXPECT_NOT_NULL(pkg2);
    EXPECT_EQ(pkg2->getName(), pkg1->getName());
    EXPECT_EQ(pkg2->getNsURI(), pkg1->getNsURI());
    EXPECT_EQ(pkg2->getNsPrefix(), pkg1->getNsPrefix());
}

// =====================================================================
// 2) Roundtrip .ecore：classifier 数量与名字保持一致
// =====================================================================
EMF_TEST(Roundtrip_Ecore_ClassifiersPreserved) {
    initEnv();
    XMIResource res1;
    auto* pkg1 = loadPkg(res1, kLibraryEcore);
    std::string saved = res1.saveToString();

    XMIResource res2;
    auto* pkg2 = loadPkg(res2, saved);
    EXPECT_EQ(pkg2->getEClassifiers().size(), pkg1->getEClassifiers().size());
    EXPECT_NOT_NULL(pkg2->getEClassifier("Library"));
    EXPECT_NOT_NULL(pkg2->getEClassifier("Book"));
}

// =====================================================================
// 3) Roundtrip .ecore：EClass features name/type 保持一致
// =====================================================================
EMF_TEST(Roundtrip_Ecore_FeaturesAndTypesPreserved) {
    initEnv();
    XMIResource res1;
    loadPkg(res1, kLibraryEcore);
    std::string saved = res1.saveToString();

    XMIResource res2;
    auto* pkg2 = loadPkg(res2, saved);
    auto* libCls = dynamic_cast<EClass*>(pkg2->getEClassifier("Library"));
    EXPECT_NOT_NULL(libCls);
    EXPECT_EQ(libCls->getEStructuralFeatures().size(), 2u);
    auto* nameAttr = dynamic_cast<EAttribute*>(libCls->getEStructuralFeature("name"));
    EXPECT_NOT_NULL(nameAttr);
    EXPECT_NOT_NULL(nameAttr->getEAttributeType());
    EXPECT_EQ(nameAttr->getEAttributeType()->getName(), std::string("EString"));
    auto* booksRef = dynamic_cast<EReference*>(libCls->getEStructuralFeature("books"));
    EXPECT_NOT_NULL(booksRef);
    EXPECT_EQ(booksRef->getUpperBound(), -1);
    EXPECT_TRUE(booksRef->isContainment());
    EXPECT_EQ(booksRef->getEReferenceType()->getName(), std::string("Book"));
}

// =====================================================================
// 4) Roundtrip .ecore：defaultValueLiteral 保持一致
// =====================================================================
EMF_TEST(Roundtrip_Ecore_DefaultValueLiteralPreserved) {
    initEnv();
    XMIResource res1;
    loadPkg(res1, kLibraryEcore);
    std::string saved = res1.saveToString();

    XMIResource res2;
    auto* pkg2 = loadPkg(res2, saved);
    auto* bookCls = dynamic_cast<EClass*>(pkg2->getEClassifier("Book"));
    EXPECT_NOT_NULL(bookCls);
    auto* pages = dynamic_cast<EAttribute*>(bookCls->getEStructuralFeature("pages"));
    EXPECT_NOT_NULL(pages);
    EXPECT_EQ(pages->getDefaultValueLiteral(), std::string("0"));
    EXPECT_EQ(pages->getEAttributeType()->getName(), std::string("EInt"));
}

// =====================================================================
// 5) Roundtrip .ecore：两次 save 输出幂等（save1 == save2）
// =====================================================================
EMF_TEST(Roundtrip_Ecore_SaveIsIdempotent) {
    initEnv();
    XMIResource res1;
    loadPkg(res1, kLibraryEcore);
    std::string saved1 = res1.saveToString();

    XMIResource res2;
    res2.loadFromString(saved1);
    std::string saved2 = res2.saveToString();

    EXPECT_EQ(saved1, saved2);
}

// =====================================================================
// 6) Roundtrip EEnum：literals name/value/literal 保持一致
// =====================================================================
EMF_TEST(Roundtrip_EEnum_LiteralsPreserved) {
    initEnv();
    XMIResource res1;
    auto* pkg1 = loadPkg(res1, kEnumEcore);
    std::string saved = res1.saveToString();

    XMIResource res2;
    auto* pkg2 = loadPkg(res2, saved);
    auto* en = dynamic_cast<EEnum*>(pkg2->getEClassifier("Color"));
    EXPECT_NOT_NULL(en);
    EXPECT_EQ(en->getELiterals().size(), 2u);
    EXPECT_EQ(en->getELiterals()[0]->getName(), std::string("Red"));
    EXPECT_EQ(en->getELiterals()[0]->getValue(), 0);
    EXPECT_EQ(en->getELiterals()[0]->getLiteral(), std::string("RED"));
    EXPECT_EQ(en->getELiterals()[1]->getName(), std::string("Green"));
    EXPECT_EQ(en->getELiterals()[1]->getValue(), 1);
}

// =====================================================================
// 7) Roundtrip：xmi:id 保持一致（reload 后仍可按 id 查到对象）
// =====================================================================
EMF_TEST(Roundtrip_XmiIdPreserved) {
    initEnv();
    const char* xml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        "<ecore:EPackage xmi:version=\"2.0\"\n"
        "    xmlns:xmi=\"http://www.omg.org/XMI\"\n"
        "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
        "    xmlns:ecore=\"http://www.eclipse.org/emf/2002/Ecore\"\n"
        "    name=\"idt\" nsURI=\"http://example.com/idt\" nsPrefix=\"idt\">\n"
        "  <eClassifiers xsi:type=\"ecore:EClass\" name=\"A\" xmi:id=\"_A\">\n"
        "    <eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"n\"\n"
        "        eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString\"/>\n"
        "  </eClassifiers>\n"
        "</ecore:EPackage>\n";
    XMIResource res1;
    loadPkg(res1, xml);
    std::string saved = res1.saveToString();
    // saver 应输出 xmi:id="_A"
    EXPECT_TRUE(saved.find("xmi:id=\"_A\"") != std::string::npos);

    XMIResource res2;
    auto* pkg2 = loadPkg(res2, saved);
    auto* clsA = dynamic_cast<EClass*>(pkg2->getEClassifier("A"));
    EXPECT_NOT_NULL(clsA);
    EXPECT_EQ(res2.getID(clsA), std::string("_A"));
    EXPECT_EQ(res2.getEObjectByID("_A"),
              static_cast<emf::common::EObject*>(clsA));
}

// =====================================================================
// 8) Roundtrip：containment EReference 在两次 save 后结构一致
// =====================================================================
EMF_TEST(Roundtrip_ContainmentReference_Stable) {
    initEnv();
    XMIResource res1;
    loadPkg(res1, kLibraryEcore);
    std::string s1 = res1.saveToString();

    XMIResource res2;
    res2.loadFromString(s1);
    std::string s2 = res2.saveToString();

    EXPECT_EQ(s1, s2);
    // 两轮后仍能找到 books 引用且 containment=true
    auto* pkg2 = dynamic_cast<EPackage*>(res2.getContents().front());
    EXPECT_NOT_NULL(pkg2);
    auto* lib = dynamic_cast<EClass*>(pkg2->getEClassifier("Library"));
    auto* books = dynamic_cast<EReference*>(lib->getEStructuralFeature("books"));
    EXPECT_TRUE(books->isContainment());
}

// =====================================================================
// 9) Roundtrip：EClass abstract 标志保持一致
// =====================================================================
EMF_TEST(Roundtrip_EClass_AbstractFlagPreserved) {
    initEnv();
    const char* xml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        "<ecore:EPackage xmi:version=\"2.0\"\n"
        "    xmlns:xmi=\"http://www.omg.org/XMI\"\n"
        "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
        "    xmlns:ecore=\"http://www.eclipse.org/emf/2002/Ecore\"\n"
        "    name=\"ab\" nsURI=\"http://example.com/ab\" nsPrefix=\"ab\">\n"
        "  <eClassifiers xsi:type=\"ecore:EClass\" name=\"Base\" abstract=\"true\"/>\n"
        "  <eClassifiers xsi:type=\"ecore:EClass\" name=\"Derived\" eSuperTypes=\"#//Base\"/>\n"
        "</ecore:EPackage>\n";
    XMIResource res1;
    loadPkg(res1, xml);
    std::string saved = res1.saveToString();
    EXPECT_TRUE(saved.find("abstract=\"true\"") != std::string::npos);

    XMIResource res2;
    auto* pkg2 = loadPkg(res2, saved);
    auto* base = dynamic_cast<EClass*>(pkg2->getEClassifier("Base"));
    EXPECT_TRUE(base->isAbstract());
    auto* derived = dynamic_cast<EClass*>(pkg2->getEClassifier("Derived"));
    EXPECT_EQ(derived->getESuperTypes().size(), 1u);
    EXPECT_EQ(derived->getESuperTypes()[0]->getName(), std::string("Base"));
}

// =====================================================================
// 10) Roundtrip：空 EPackage save -> reload -> save 幂等
// =====================================================================
EMF_TEST(Roundtrip_EmptyPackage_Idempotent) {
    initEnv();
    const char* xml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        "<ecore:EPackage xmi:version=\"2.0\"\n"
        "    xmlns:xmi=\"http://www.omg.org/XMI\"\n"
        "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
        "    xmlns:ecore=\"http://www.eclipse.org/emf/2002/Ecore\"\n"
        "    name=\"empty\" nsURI=\"http://example.com/empty\" nsPrefix=\"em\"/>\n";
    XMIResource res1;
    loadPkg(res1, xml);
    std::string s1 = res1.saveToString();
    XMIResource res2;
    res2.loadFromString(s1);
    std::string s2 = res2.saveToString();
    EXPECT_EQ(s1, s2);
    EXPECT_TRUE(s1.find("name=\"empty\"") != std::string::npos);
}
