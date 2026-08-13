// XMISaverTests.cpp —— XMISaver 序列化逻辑单测
// 对齐 Java: org.eclipse.emf.ecore.xmi.impl.XMLSaveImpl + XMIHelperImpl
//
// 覆盖 XMISaver.cpp 中的：
//   - EcoreSaver：保存 EPackage -> <ecore:EPackage> 文档
//     （name/nsURI/nsPrefix、eClassifiers、eStructuralFeatures、eType href、eLiterals）
//   - InstanceSaver：保存实例 -> <xmi:XMI> 或单根实例文档
//   - XMIOptions：encoding / xmiVersion / xmlDeclaration / indent
//   - 空 contents 输出 <xmi:XMI/>
#include "test_main.h"
#include "emf/xmi/XMIResource.h"
#include "emf/xmi/XMIResourceFactory.h"
#include "emf/xmi/XMIOptions.h"
#include "emf/xmi/XMIHelper.h"
#include "emf/ecore/EcorePackage.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/ecore/util/ExtendedMetaData.h"
#include "emf/common/EPackageRegistry.h"

#include <string>

using emf::xmi::XMIResource;
using emf::xmi::XMIResourceFactory;
using emf::xmi::XMIOptions;
using emf::ecore::EcoreFactory;
using emf::ecore::EcorePackage;
using emf::ecore::EPackage;
using emf::ecore::EClass;
using emf::ecore::EAttribute;
using emf::ecore::EReference;
using emf::ecore::EEnum;
using emf::ecore::EDataType;

namespace {

void initEnv() {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    XMIResourceFactory::registerDefaults();
}

// 构造一个简单 EPackage：含 1 个 EClass(Foo) + 1 个 EAttribute(label->EString)
EPackage* buildSimplePackage() {
    auto* pkg = EcoreFactory::instance().createEPackage();
    pkg->setName("simple");
    pkg->setNsURI("http://example.com/simple");
    pkg->setNsPrefix("sim");
    auto* cls = EcoreFactory::instance().createEClass();
    cls->setName("Foo");
    auto* attr = EcoreFactory::instance().createEAttribute();
    attr->setName("label");
    attr->setEAttributeType(EcorePackage::instance().getEDataType_EString());
    cls->addEStructuralFeature(attr);
    pkg->addEClassifier(cls);
    return pkg;
}

}  // namespace

// =====================================================================
// 1) 空 resource：saveToString 输出 <xmi:XMI/> 空文档
// =====================================================================
EMF_TEST(XMISaver_EmptyResource_OutputsEmptyXmi) {
    initEnv();
    XMIResource res;
    std::string out = res.saveToString();
    EXPECT_TRUE(out.find("<?xml") != std::string::npos);
    EXPECT_TRUE(out.find("<xmi:XMI") != std::string::npos);
    EXPECT_TRUE(out.find("/>") != std::string::npos);
}

// =====================================================================
// 2) 保存 EPackage：验证 <ecore:EPackage> 结构与属性
// =====================================================================
EMF_TEST(XMISaver_SaveEPackage_Structure) {
    initEnv();
    XMIResource res;
    auto* pkg = buildSimplePackage();
    res.addToContents(pkg);
    std::string out = res.saveToString();
    // XML 声明（对齐 Java EcoreResourceFactoryImpl 默认 UTF-8）
    EXPECT_TRUE(out.find("<?xml version=\"1.0\" encoding=\"UTF-8\"?>") != std::string::npos);
    // ecore:EPackage 根元素 + 命名空间声明
    EXPECT_TRUE(out.find("<ecore:EPackage") != std::string::npos);
    EXPECT_TRUE(out.find("xmlns:xmi=\"http://www.omg.org/XMI\"") != std::string::npos);
    EXPECT_TRUE(out.find("xmlns:ecore=\"http://www.eclipse.org/emf/2002/Ecore\"") != std::string::npos);
    EXPECT_TRUE(out.find("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"") != std::string::npos);
    // 属性
    EXPECT_TRUE(out.find("name=\"simple\"") != std::string::npos);
    EXPECT_TRUE(out.find("nsURI=\"http://example.com/simple\"") != std::string::npos);
    EXPECT_TRUE(out.find("nsPrefix=\"sim\"") != std::string::npos);
    EXPECT_TRUE(out.find("xmi:version=\"2.0\"") != std::string::npos);
}

// =====================================================================
// 3) 保存 EPackage：验证 eClassifiers + xsi:type="ecore:EClass"
// =====================================================================
EMF_TEST(XMISaver_SaveEPackage_EClassOutput) {
    initEnv();
    XMIResource res;
    auto* pkg = buildSimplePackage();
    res.addToContents(pkg);
    std::string out = res.saveToString();
    EXPECT_TRUE(out.find("<eClassifiers") != std::string::npos);
    EXPECT_TRUE(out.find("xsi:type=\"ecore:EClass\"") != std::string::npos);
    EXPECT_TRUE(out.find("name=\"Foo\"") != std::string::npos);
    // EAttribute 子元素
    EXPECT_TRUE(out.find("<eStructuralFeatures") != std::string::npos);
    EXPECT_TRUE(out.find("xsi:type=\"ecore:EAttribute\"") != std::string::npos);
    EXPECT_TRUE(out.find("name=\"label\"") != std::string::npos);
}

// =====================================================================
// 4) 保存 EAttribute：eType 输出为 ecore 内建类型 href 形式
// =====================================================================
EMF_TEST(XMISaver_SaveEAttribute_ETypeEcoreBuiltin) {
    initEnv();
    XMIResource res;
    auto* pkg = buildSimplePackage();
    res.addToContents(pkg);
    std::string out = res.saveToString();
    // eType 应输出为 "ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString"
    EXPECT_TRUE(out.find("eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString\"")
                != std::string::npos);
}

// =====================================================================
// 5) 保存 EReference：同包 eType 输出为 "#//Book" 形式 + containment
// =====================================================================
EMF_TEST(XMISaver_SaveEReference_SamePackageEType) {
    initEnv();
    auto* pkg = EcoreFactory::instance().createEPackage();
    pkg->setName("lib");
    pkg->setNsURI("http://example.com/lib");
    pkg->setNsPrefix("lib");
    auto* libCls = EcoreFactory::instance().createEClass();
    libCls->setName("Library");
    auto* bookCls = EcoreFactory::instance().createEClass();
    bookCls->setName("Book");
    auto* booksRef = EcoreFactory::instance().createEReference();
    booksRef->setName("books");
    booksRef->setUpperBound(-1);
    booksRef->setContainment(true);
    booksRef->setEReferenceType(bookCls);
    libCls->addEStructuralFeature(booksRef);
    pkg->addEClassifier(libCls);
    pkg->addEClassifier(bookCls);

    XMIResource res;
    res.addToContents(pkg);
    std::string out = res.saveToString();
    EXPECT_TRUE(out.find("xsi:type=\"ecore:EReference\"") != std::string::npos);
    EXPECT_TRUE(out.find("name=\"books\"") != std::string::npos);
    EXPECT_TRUE(out.find("upperBound=\"-1\"") != std::string::npos);
    EXPECT_TRUE(out.find("containment=\"true\"") != std::string::npos);
    EXPECT_TRUE(out.find("eType=\"#//Book\"") != std::string::npos);
}

// =====================================================================
// 6) 保存 EEnum：验证 eLiterals 输出
// =====================================================================
EMF_TEST(XMISaver_SaveEEnum_Literals) {
    initEnv();
    auto* pkg = EcoreFactory::instance().createEPackage();
    pkg->setName("enums");
    pkg->setNsURI("http://example.com/enums");
    pkg->setNsPrefix("en");
    auto* en = EcoreFactory::instance().createEEnum();
    en->setName("Color");
    // 用 EEnumImpl::addELiteral 添加字面量
    auto* enImpl = dynamic_cast<emf::ecore::EEnumImpl*>(en);
    {
        auto* lit = EcoreFactory::instance().createEEnumLiteral();
        lit->setName("Red");
        lit->setValue(0);
        lit->setLiteral("RED");
        enImpl->addELiteral(lit);
    }
    {
        auto* lit = EcoreFactory::instance().createEEnumLiteral();
        lit->setName("Green");
        lit->setValue(1);
        lit->setLiteral("GREEN");
        enImpl->addELiteral(lit);
    }
    pkg->addEClassifier(en);

    XMIResource res;
    res.addToContents(pkg);
    std::string out = res.saveToString();
    EXPECT_TRUE(out.find("xsi:type=\"ecore:EEnum\"") != std::string::npos);
    EXPECT_TRUE(out.find("name=\"Color\"") != std::string::npos);
    EXPECT_TRUE(out.find("<eLiterals") != std::string::npos);
    EXPECT_TRUE(out.find("name=\"Red\"") != std::string::npos);
    // value=索引位置时不输出（对齐 Java EEnumLiteralSerializer：自动递增值省略）
    // Red(0)=索引0, Green(1)=索引1 → 均不输出 value
    EXPECT_TRUE(out.find("value=\"0\"") == std::string::npos);
    EXPECT_TRUE(out.find("value=\"1\"") == std::string::npos);
    EXPECT_TRUE(out.find("literal=\"GREEN\"") != std::string::npos);
}

// =====================================================================
// 7) XMIOptions：encoding 自定义
// =====================================================================
EMF_TEST(XMISaver_Options_CustomEncoding) {
    initEnv();
    XMIResource res;
    auto* pkg = buildSimplePackage();
    res.addToContents(pkg);
    XMIOptions opts;
    opts.encoding = "ASCII";
    std::string out = res.saveToString(opts);
    EXPECT_TRUE(out.find("<?xml version=\"1.0\" encoding=\"ASCII\"?>") != std::string::npos);
}

// =====================================================================
// 8) XMIOptions：xmlDeclaration=false 不输出 <?xml?>
// =====================================================================
EMF_TEST(XMISaver_Options_NoXmlDeclaration) {
    initEnv();
    XMIResource res;
    auto* pkg = buildSimplePackage();
    res.addToContents(pkg);
    XMIOptions opts;
    opts.xmlDeclaration = false;
    std::string out = res.saveToString(opts);
    EXPECT_TRUE(out.find("<?xml") == std::string::npos);
}

// =====================================================================
// 9) XMIOptions：xmiVersion 自定义
// =====================================================================
EMF_TEST(XMISaver_Options_CustomXmiVersion) {
    initEnv();
    XMIResource res;
    auto* pkg = buildSimplePackage();
    res.addToContents(pkg);
    XMIOptions opts;
    opts.xmiVersion = "2.1";
    std::string out = res.saveToString(opts);
    EXPECT_TRUE(out.find("xmi:version=\"2.1\"") != std::string::npos);
}

// =====================================================================
// 10) 保存 EClass：abstract=true 输出 abstract 属性
// =====================================================================
EMF_TEST(XMISaver_SaveEClass_AbstractFlag) {
    initEnv();
    auto* pkg = EcoreFactory::instance().createEPackage();
    pkg->setName("a");
    pkg->setNsURI("http://example.com/a");
    pkg->setNsPrefix("a");
    auto* cls = EcoreFactory::instance().createEClass();
    cls->setName("Base");
    cls->setAbstract(true);
    pkg->addEClassifier(cls);
    XMIResource res;
    res.addToContents(pkg);
    std::string out = res.saveToString();
    EXPECT_TRUE(out.find("abstract=\"true\"") != std::string::npos);
}

// =====================================================================
// 11) 保存 EAttribute：iD=true 输出 iD 属性
// =====================================================================
EMF_TEST(XMISaver_SaveEAttribute_IDFlag) {
    initEnv();
    auto* pkg = EcoreFactory::instance().createEPackage();
    pkg->setName("idpkg");
    pkg->setNsURI("http://example.com/idpkg");
    pkg->setNsPrefix("idpkg");
    auto* cls = EcoreFactory::instance().createEClass();
    cls->setName("WithID");
    auto* attr = EcoreFactory::instance().createEAttribute();
    attr->setName("uid");
    attr->setID(true);
    attr->setEAttributeType(EcorePackage::instance().getEDataType_EString());
    cls->addEStructuralFeature(attr);
    pkg->addEClassifier(cls);
    XMIResource res;
    res.addToContents(pkg);
    std::string out = res.saveToString();
    EXPECT_TRUE(out.find("iD=\"true\"") != std::string::npos);
}

// =====================================================================
// 12) 保存 EReference：resolveProxies=false 输出 resolveProxies 属性
// =====================================================================
EMF_TEST(XMISaver_SaveEReference_ResolveProxiesFalse) {
    initEnv();
    auto* pkg = EcoreFactory::instance().createEPackage();
    pkg->setName("rp");
    pkg->setNsURI("http://example.com/rp");
    pkg->setNsPrefix("rp");
    auto* parent = EcoreFactory::instance().createEClass();
    parent->setName("Parent");
    auto* child = EcoreFactory::instance().createEClass();
    child->setName("Child");
    auto* ref = EcoreFactory::instance().createEReference();
    ref->setName("kids");
    ref->setUpperBound(-1);
    ref->setContainment(true);
    ref->setResolveProxies(false);
    ref->setEReferenceType(child);
    parent->addEStructuralFeature(ref);
    pkg->addEClassifier(parent);
    pkg->addEClassifier(child);
    XMIResource res;
    res.addToContents(pkg);
    std::string out = res.saveToString();
    EXPECT_TRUE(out.find("resolveProxies=\"false\"") != std::string::npos);
}

// =====================================================================
// 13) ExtendedMetaData kind=element：EAttribute 输出为子元素 + roundtrip
// 对齐 Java XMLSaveImpl：getFeatureKind(feature)==ELEMENT 时输出为子元素
// =====================================================================
EMF_TEST(XMISaver_ExtendedMetaData_ElementKind_EAttributeAsElement) {
    initEnv();
    // 构造 EPackage + EClass + EAttribute（带 ExtendedMetaData kind=element 注解）
    auto* pkg = EcoreFactory::instance().createEPackage();
    pkg->setName("extmd");
    pkg->setNsURI("http://example.com/extmd");
    pkg->setNsPrefix("extmd");
    auto* cls = EcoreFactory::instance().createEClass();
    cls->setName("Item");
    auto* attr = EcoreFactory::instance().createEAttribute();
    attr->setName("description");
    attr->setEAttributeType(EcorePackage::instance().getEDataType_EString());
    // 添加 ExtendedMetaData kind=element 注解
    auto* ann = EcoreFactory::instance().createEAnnotation();
    ann->setSource(emf::ecore::util::ExtendedMetaData::ANNOTATION_URI);
    ann->setDetail("kind", "element");
    auto* attrImpl = dynamic_cast<emf::ecore::EModelElementImpl*>(attr);
    EXPECT_NOT_NULL(attrImpl);
    attrImpl->addEAnnotation(ann);
    cls->addEStructuralFeature(attr);
    pkg->addEClassifier(cls);
    // 注册到 EPackageRegistry（实例反序列化需要按 nsURI 查找）
    emf::common::EPackageRegistry::instance().put(pkg->getNsURI(), pkg);

    // 创建实例并设置 description 值
    auto* factory = pkg->getEFactoryInstance();
    EXPECT_NOT_NULL(factory);
    auto* obj = factory->create(cls);
    EXPECT_NOT_NULL(obj);
    obj->eSet(attr, std::any(std::string("hello world")));

    // 序列化
    XMIResource res;
    res.getContents().push_back(obj);
    std::string out = res.saveToString();

    // 验证：description 输出为子元素而非属性
    EXPECT_TRUE(out.find("<description>hello world</description>") != std::string::npos);
    // 不应作为属性输出
    EXPECT_TRUE(out.find("description=\"") == std::string::npos);

    // 反序列化验证 roundtrip
    XMIResource res2;
    res2.loadFromString(out);
    auto& contents = res2.getContents();
    EXPECT_EQ(contents.size(), (size_t)1);
    auto* loadedObj = contents[0];
    EXPECT_NOT_NULL(loadedObj);
    auto* loadedCls = loadedObj->eClass();
    auto* loadedAttr = loadedCls->getEStructuralFeature("description");
    EXPECT_NOT_NULL(loadedAttr);
    std::any v = loadedObj->eGet(loadedAttr);
    std::string valStr;
    if (v.type() == typeid(std::string)) {
        valStr = std::any_cast<std::string>(v);
    }
    EXPECT_EQ(valStr, std::string("hello world"));
}

// =====================================================================
// 14) useEncodedAttributeStyle=true：强制 EAttribute 输出为属性（覆盖 kind=element）
// 对齐 Java XMLResource.OPTION_USE_ENCODED_ATTRIBUTE_STYLE
// =====================================================================
EMF_TEST(XMISaver_UseEncodedAttributeStyle_ForcesAttributeOutput) {
    initEnv();
    auto* pkg = EcoreFactory::instance().createEPackage();
    pkg->setName("encstyle");
    pkg->setNsURI("http://example.com/encstyle");
    pkg->setNsPrefix("enc");
    auto* cls = EcoreFactory::instance().createEClass();
    cls->setName("Doc");
    auto* attr = EcoreFactory::instance().createEAttribute();
    attr->setName("body");
    attr->setEAttributeType(EcorePackage::instance().getEDataType_EString());
    // 添加 kind=element 注解（正常情况下应输出为子元素）
    auto* ann = EcoreFactory::instance().createEAnnotation();
    ann->setSource(emf::ecore::util::ExtendedMetaData::ANNOTATION_URI);
    ann->setDetail("kind", "element");
    dynamic_cast<emf::ecore::EModelElementImpl*>(attr)->addEAnnotation(ann);
    cls->addEStructuralFeature(attr);
    pkg->addEClassifier(cls);
    emf::common::EPackageRegistry::instance().put(pkg->getNsURI(), pkg);

    auto* factory = pkg->getEFactoryInstance();
    auto* obj = factory->create(cls);
    obj->eSet(attr, std::any(std::string("data")));

    // useEncodedAttributeStyle=true：强制 attribute 风格
    XMIOptions opts;
    opts.useEncodedAttributeStyle = true;
    XMIResource res;
    res.getContents().push_back(obj);
    std::string out = res.saveToString(opts);

    // 验证：body 输出为属性（覆盖 kind=element 注解）
    EXPECT_TRUE(out.find("body=\"data\"") != std::string::npos);
    // 不应输出为子元素
    EXPECT_TRUE(out.find("<body>") == std::string::npos);
}
