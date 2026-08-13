// XMILoaderTests.cpp —— XMILoader 反序列化逻辑单测
// 对齐 Java: org.eclipse.emf.ecore.xmi.impl.XMLLoadImpl + SAXXMIHandler
//
// 覆盖 XMILoader.cpp 中的：
//   - buildEPackage（name/nsURI/nsPrefix/eClassifiers/eSubpackages）
//   - buildEClass（name/abstract/interface/eSuperTypes/instanceClassName）
//   - buildEStructuralFeature（EAttribute/EReference + bounds + eType）
//   - buildEDataType / buildEEnum（含 eLiterals）
//   - xmi:id 注册（res.setID）
//   - eType href 解析（#//Book 同包引用 / ecore:EDataType ...#//EString 内建类型）
#include "test_main.h"
#include "emf/xmi/XMIResource.h"
#include "emf/xmi/XMIResourceFactory.h"
#include "emf/xmi/XMIHelper.h"
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
using emf::ecore::EDataType;
using emf::ecore::EEnum;
using emf::ecore::EEnumLiteral;
using emf::ecore::EClassifier;
using emf::ecore::EStructuralFeature;

namespace {

// 简单 EPackage：1 个 EClass，1 个 EAttribute(name->EString)
const char* kSimpleEcore =
    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
    "<ecore:EPackage xmi:version=\"2.0\"\n"
    "    xmlns:xmi=\"http://www.omg.org/XMI\"\n"
    "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
    "    xmlns:ecore=\"http://www.eclipse.org/emf/2002/Ecore\"\n"
    "    name=\"simple\" nsURI=\"http://example.com/simple\" nsPrefix=\"sim\">\n"
    "  <eClassifiers xsi:type=\"ecore:EClass\" name=\"Foo\">\n"
    "    <eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"label\"\n"
    "        eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString\"/>\n"
    "  </eClassifiers>\n"
    "</ecore:EPackage>\n";

void initEnv() {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    XMIResourceFactory::registerDefaults();
}

EPackage* loadEcore(XMIResource& res, const std::string& xml) {
    res.loadFromString(xml);
    EXPECT_TRUE(!res.getContents().empty());
    return dynamic_cast<EPackage*>(res.getContents().front());
}

}  // namespace

// =====================================================================
// 1) 加载简单 .ecore：验证 EPackage name/nsURI/nsPrefix
// =====================================================================
EMF_TEST(XMILoader_LoadSimpleEPackage_Metadata) {
    initEnv();
    XMIResource res;
    auto* pkg = loadEcore(res, kSimpleEcore);
    EXPECT_NOT_NULL(pkg);
    EXPECT_EQ(pkg->getName(), std::string("simple"));
    EXPECT_EQ(pkg->getNsURI(), std::string("http://example.com/simple"));
    EXPECT_EQ(pkg->getNsPrefix(), std::string("sim"));
    EXPECT_EQ(pkg->getEClassifiers().size(), 1u);
}

// =====================================================================
// 2) 加载简单 .ecore：验证 EClass + EAttribute
// =====================================================================
EMF_TEST(XMILoader_LoadSimpleEPackage_EClassAndEAttribute) {
    initEnv();
    XMIResource res;
    auto* pkg = loadEcore(res, kSimpleEcore);
    EXPECT_NOT_NULL(pkg);
    auto* cls = dynamic_cast<EClass*>(pkg->getEClassifier("Foo"));
    EXPECT_NOT_NULL(cls);
    EXPECT_EQ(cls->getName(), std::string("Foo"));
    EXPECT_EQ(cls->getEStructuralFeatures().size(), 1u);
    auto* sf = cls->getEStructuralFeature("label");
    EXPECT_NOT_NULL(sf);
    auto* attr = dynamic_cast<EAttribute*>(sf);
    EXPECT_NOT_NULL(attr);
    EXPECT_EQ(attr->getName(), std::string("label"));
}

// =====================================================================
// 3) eType href 解析：ecore:EDataType ...#//EString -> EcorePackage 内建类型
// =====================================================================
EMF_TEST(XMILoader_ETypeResolution_EcoreBuiltinEString) {
    initEnv();
    XMIResource res;
    auto* pkg = loadEcore(res, kSimpleEcore);
    EXPECT_NOT_NULL(pkg);
    auto* cls = dynamic_cast<EClass*>(pkg->getEClassifier("Foo"));
    EXPECT_NOT_NULL(cls);
    auto* attr = dynamic_cast<EAttribute*>(cls->getEStructuralFeature("label"));
    EXPECT_NOT_NULL(attr);
    auto* dt = attr->getEAttributeType();
    EXPECT_NOT_NULL(dt);
    EXPECT_EQ(dt->getName(), std::string("EString"));
}

// =====================================================================
// 4) eType href 解析：#//Book 同包 EClass 引用
// =====================================================================
EMF_TEST(XMILoader_ETypeResolution_SamePackageReference) {
    initEnv();
    const char* xml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        "<ecore:EPackage xmi:version=\"2.0\"\n"
        "    xmlns:xmi=\"http://www.omg.org/XMI\"\n"
        "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
        "    xmlns:ecore=\"http://www.eclipse.org/emf/2002/Ecore\"\n"
        "    name=\"lib\" nsURI=\"http://example.com/lib\" nsPrefix=\"lib\">\n"
        "  <eClassifiers xsi:type=\"ecore:EClass\" name=\"Library\">\n"
        "    <eStructuralFeatures xsi:type=\"ecore:EReference\" name=\"books\"\n"
        "        upperBound=\"-1\" eType=\"#//Book\" containment=\"true\"/>\n"
        "  </eClassifiers>\n"
        "  <eClassifiers xsi:type=\"ecore:EClass\" name=\"Book\">\n"
        "    <eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"title\"\n"
        "        eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString\"/>\n"
        "  </eClassifiers>\n"
        "</ecore:EPackage>\n";
    XMIResource res;
    auto* pkg = loadEcore(res, xml);
    EXPECT_NOT_NULL(pkg);
    auto* libCls = dynamic_cast<EClass*>(pkg->getEClassifier("Library"));
    EXPECT_NOT_NULL(libCls);
    auto* booksRef = dynamic_cast<EReference*>(libCls->getEStructuralFeature("books"));
    EXPECT_NOT_NULL(booksRef);
    EXPECT_EQ(booksRef->getUpperBound(), -1);
    EXPECT_TRUE(booksRef->isContainment());
    // eType 应解析为同包 Book
    auto* target = booksRef->getEReferenceType();
    EXPECT_NOT_NULL(target);
    EXPECT_EQ(target->getName(), std::string("Book"));
}

// =====================================================================
// 5) 加载 EEnum：验证 eLiterals（name/value/literal）
// =====================================================================
EMF_TEST(XMILoader_LoadEEnum_WithLiterals) {
    initEnv();
    const char* xml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        "<ecore:EPackage xmi:version=\"2.0\"\n"
        "    xmlns:xmi=\"http://www.omg.org/XMI\"\n"
        "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
        "    xmlns:ecore=\"http://www.eclipse.org/emf/2002/Ecore\"\n"
        "    name=\"enums\" nsURI=\"http://example.com/enums\" nsPrefix=\"en\">\n"
        "  <eClassifiers xsi:type=\"ecore:EEnum\" name=\"Color\">\n"
        "    <eLiterals name=\"Red\" value=\"0\" literal=\"RED\"/>\n"
        "    <eLiterals name=\"Green\" value=\"1\" literal=\"GREEN\"/>\n"
        "    <eLiterals name=\"Blue\" value=\"2\" literal=\"BLUE\"/>\n"
        "  </eClassifiers>\n"
        "</ecore:EPackage>\n";
    XMIResource res;
    auto* pkg = loadEcore(res, xml);
    EXPECT_NOT_NULL(pkg);
    auto* en = dynamic_cast<EEnum*>(pkg->getEClassifier("Color"));
    EXPECT_NOT_NULL(en);
    EXPECT_EQ(en->getELiterals().size(), 3u);
    EXPECT_EQ(en->getELiterals()[0]->getName(), std::string("Red"));
    EXPECT_EQ(en->getELiterals()[0]->getValue(), 0);
    EXPECT_EQ(en->getELiterals()[0]->getLiteral(), std::string("RED"));
    EXPECT_EQ(en->getELiterals()[2]->getValue(), 2);
    EXPECT_EQ(en->getELiteral("Green")->getLiteral(), std::string("GREEN"));
    EXPECT_EQ(en->getELiteralByValue(1)->getName(), std::string("Green"));
}

// =====================================================================
// 6) 加载 EDataType：验证 name + instanceClassName
// =====================================================================
EMF_TEST(XMILoader_LoadEDataType) {
    initEnv();
    const char* xml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        "<ecore:EPackage xmi:version=\"2.0\"\n"
        "    xmlns:xmi=\"http://www.omg.org/XMI\"\n"
        "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
        "    xmlns:ecore=\"http://www.eclipse.org/emf/2002/Ecore\"\n"
        "    name=\"dt\" nsURI=\"http://example.com/dt\" nsPrefix=\"dt\">\n"
        "  <eClassifiers xsi:type=\"ecore:EDataType\" name=\"Money\"\n"
        "      instanceClassName=\"java.math.BigDecimal\"/>\n"
        "</ecore:EPackage>\n";
    XMIResource res;
    auto* pkg = loadEcore(res, xml);
    EXPECT_NOT_NULL(pkg);
    auto* dt = dynamic_cast<EDataType*>(pkg->getEClassifier("Money"));
    EXPECT_NOT_NULL(dt);
    EXPECT_EQ(dt->getName(), std::string("Money"));
    EXPECT_EQ(dt->getInstanceClassName(), std::string("java.math.BigDecimal"));
}

// =====================================================================
// 7) xmi:id 注册：加载后通过 getEObjectByID 查到对象
// =====================================================================
EMF_TEST(XMILoader_XmiIdRegistration) {
    initEnv();
    const char* xml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        "<ecore:EPackage xmi:version=\"2.0\"\n"
        "    xmlns:xmi=\"http://www.omg.org/XMI\"\n"
        "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
        "    xmlns:ecore=\"http://www.eclipse.org/emf/2002/Ecore\"\n"
        "    name=\"idtest\" nsURI=\"http://example.com/idtest\" nsPrefix=\"it\">\n"
        "  <eClassifiers xsi:type=\"ecore:EClass\" name=\"A\" xmi:id=\"_clsA\">\n"
        "    <eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"n\"\n"
        "        eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString\"/>\n"
        "  </eClassifiers>\n"
        "</ecore:EPackage>\n";
    XMIResource res;
    auto* pkg = loadEcore(res, xml);
    EXPECT_NOT_NULL(pkg);
    auto* clsA = dynamic_cast<EClass*>(pkg->getEClassifier("A"));
    EXPECT_NOT_NULL(clsA);
    // xmi:id="_clsA" 应注册到 resource
    auto* byId = res.getEObjectByID("_clsA");
    EXPECT_EQ(byId, static_cast<emf::common::EObject*>(clsA));
    EXPECT_EQ(res.getID(clsA), std::string("_clsA"));
}

// =====================================================================
// 8) EClass 属性：abstract / interface / instanceClassName
// =====================================================================
EMF_TEST(XMILoader_EClass_AbstractAndInterface) {
    initEnv();
    const char* xml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        "<ecore:EPackage xmi:version=\"2.0\"\n"
        "    xmlns:xmi=\"http://www.omg.org/XMI\"\n"
        "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
        "    xmlns:ecore=\"http://www.eclipse.org/emf/2002/Ecore\"\n"
        "    name=\"abs\" nsURI=\"http://example.com/abs\" nsPrefix=\"ab\">\n"
        "  <eClassifiers xsi:type=\"ecore:EClass\" name=\"Base\"\n"
        "      abstract=\"true\" interface=\"true\" instanceClassName=\"java.util.Collection\"/>\n"
        "</ecore:EPackage>\n";
    XMIResource res;
    auto* pkg = loadEcore(res, xml);
    EXPECT_NOT_NULL(pkg);
    auto* cls = dynamic_cast<EClass*>(pkg->getEClassifier("Base"));
    EXPECT_NOT_NULL(cls);
    EXPECT_TRUE(cls->isAbstract());
    EXPECT_TRUE(cls->isInterface());
    EXPECT_EQ(cls->getInstanceClassName(), std::string("java.util.Collection"));
}

// =====================================================================
// 9) EAttribute bounds + defaultValueLiteral + iD
// =====================================================================
EMF_TEST(XMILoader_EAttribute_BoundsDefaultID) {
    initEnv();
    const char* xml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        "<ecore:EPackage xmi:version=\"2.0\"\n"
        "    xmlns:xmi=\"http://www.omg.org/XMI\"\n"
        "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
        "    xmlns:ecore=\"http://www.eclipse.org/emf/2002/Ecore\"\n"
        "    name=\"b\" nsURI=\"http://example.com/b\" nsPrefix=\"b\">\n"
        "  <eClassifiers xsi:type=\"ecore:EClass\" name=\"C\">\n"
        "    <eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"id\"\n"
        "        iD=\"true\" eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString\"/>\n"
        "    <eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"count\"\n"
        "        lowerBound=\"1\" upperBound=\"5\" defaultValueLiteral=\"0\"\n"
        "        eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EInt\"/>\n"
        "  </eClassifiers>\n"
        "</ecore:EPackage>\n";
    XMIResource res;
    auto* pkg = loadEcore(res, xml);
    EXPECT_NOT_NULL(pkg);
    auto* cls = dynamic_cast<EClass*>(pkg->getEClassifier("C"));
    EXPECT_NOT_NULL(cls);
    auto* idAttr = dynamic_cast<EAttribute*>(cls->getEStructuralFeature("id"));
    EXPECT_NOT_NULL(idAttr);
    EXPECT_TRUE(idAttr->isID());
    auto* countAttr = dynamic_cast<EAttribute*>(cls->getEStructuralFeature("count"));
    EXPECT_NOT_NULL(countAttr);
    EXPECT_EQ(countAttr->getLowerBound(), 1);
    EXPECT_EQ(countAttr->getUpperBound(), 5);
    EXPECT_EQ(countAttr->getDefaultValueLiteral(), std::string("0"));
}

// =====================================================================
// 10) EReference：containment / resolveProxies / upperBound
// =====================================================================
EMF_TEST(XMILoader_EReference_ContainmentAndResolveProxies) {
    initEnv();
    const char* xml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        "<ecore:EPackage xmi:version=\"2.0\"\n"
        "    xmlns:xmi=\"http://www.omg.org/XMI\"\n"
        "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
        "    xmlns:ecore=\"http://www.eclipse.org/emf/2002/Ecore\"\n"
        "    name=\"r\" nsURI=\"http://example.com/r\" nsPrefix=\"r\">\n"
        "  <eClassifiers xsi:type=\"ecore:EClass\" name=\"Parent\">\n"
        "    <eStructuralFeatures xsi:type=\"ecore:EReference\" name=\"kids\"\n"
        "        upperBound=\"-1\" eType=\"#//Child\" containment=\"true\" resolveProxies=\"false\"/>\n"
        "  </eClassifiers>\n"
        "  <eClassifiers xsi:type=\"ecore:EClass\" name=\"Child\"/>\n"
        "</ecore:EPackage>\n";
    XMIResource res;
    auto* pkg = loadEcore(res, xml);
    EXPECT_NOT_NULL(pkg);
    auto* parent = dynamic_cast<EClass*>(pkg->getEClassifier("Parent"));
    EXPECT_NOT_NULL(parent);
    auto* kids = dynamic_cast<EReference*>(parent->getEStructuralFeature("kids"));
    EXPECT_NOT_NULL(kids);
    EXPECT_TRUE(kids->isContainment());
    EXPECT_FALSE(kids->isResolveProxies());
    EXPECT_EQ(kids->getUpperBound(), -1);
    EXPECT_EQ(kids->getEReferenceType()->getName(), std::string("Child"));
}

// =====================================================================
// 11) 加载后 EPackage 自动注册到 EPackageRegistry（按 nsURI）
// =====================================================================
EMF_TEST(XMILoader_AutoRegisterToPackageRegistry) {
    initEnv();
    XMIResource res;
    auto* pkg = loadEcore(res, kSimpleEcore);
    EXPECT_NOT_NULL(pkg);
    auto* reg = emf::common::EPackageRegistry::instance().get(pkg->getNsURI());
    EXPECT_EQ(dynamic_cast<EPackage*>(reg), pkg);
}

// =====================================================================
// 12) eSuperTypes 同包继承解析
// =====================================================================
EMF_TEST(XMILoader_ESuperTypes_SamePackage) {
    initEnv();
    const char* xml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        "<ecore:EPackage xmi:version=\"2.0\"\n"
        "    xmlns:xmi=\"http://www.omg.org/XMI\"\n"
        "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
        "    xmlns:ecore=\"http://www.eclipse.org/emf/2002/Ecore\"\n"
        "    name=\"inh\" nsURI=\"http://example.com/inh\" nsPrefix=\"inh\">\n"
        "  <eClassifiers xsi:type=\"ecore:EClass\" name=\"Animal\"/>\n"
        "  <eClassifiers xsi:type=\"ecore:EClass\" name=\"Dog\" eSuperTypes=\"#//Animal\"/>\n"
        "</ecore:EPackage>\n";
    XMIResource res;
    auto* pkg = loadEcore(res, xml);
    EXPECT_NOT_NULL(pkg);
    auto* dog = dynamic_cast<EClass*>(pkg->getEClassifier("Dog"));
    EXPECT_NOT_NULL(dog);
    EXPECT_EQ(dog->getESuperTypes().size(), 1u);
    EXPECT_EQ(dog->getESuperTypes()[0]->getName(), std::string("Animal"));
    // getEAllSuperTypes 应包含 Animal
    EXPECT_EQ(dog->getEAllSuperTypes().size(), 1u);
}
