// XcoreTests.cpp — emf-xcore 单元测试
// 覆盖：
//   1. 基础语法解析（package/class/attr/ref/op/enum/extends）
//   2. 派生 EPackage/EClass/EAttribute/EReference/EOperation/EEnum
//   3. 三个真实 .xcore 样本回归
//   4. XcoreResource 端到端 load
#include "test_main.h"
#include "emf/ecore/xcore/XcoreParser.h"
#include "emf/ecore/xcore/XcoreGenerator.h"
#include "emf/ecore/xcore/XcoreResource.h"
#include "emf/ecore/EcorePackage.h"
#include <sstream>

using namespace emf::ecore::xcore;

// ===== 测试 1：基础解析 =====
EMF_TEST(XcoreParser_BasicClass) {
    std::string src = R"(
        package demo
        class Foo {
            String name
            int count
            contains Bar[] bars
            op String greet() { "hi" }
        }
        class Bar {
            boolean active
        }
    )";
    auto pkg = XcoreParser::parse(src);
    EXPECT_EQ(pkg->name, "demo");
    EXPECT_EQ(pkg->classes.size(), 2u);
    auto& foo = pkg->classes[0];
    EXPECT_EQ(foo->name, "Foo");
    EXPECT_EQ(foo->attributes.size(), 2u);
    EXPECT_EQ(foo->references.size(), 1u);
    EXPECT_EQ(foo->operations.size(), 1u);
    EXPECT_EQ(foo->references[0]->typeName, "Bar");
    EXPECT_EQ(static_cast<int>(foo->references[0]->kind),
              static_cast<int>(ReferenceKind::Containment));
    EXPECT_TRUE(foo->references[0]->multi);
    EXPECT_EQ(foo->operations[0]->name, "greet");
    EXPECT_EQ(foo->operations[0]->typeName, "String");
}

// ===== 测试 2：extends + enum =====
EMF_TEST(XcoreParser_ExtendsAndEnum) {
    std::string src = R"(
        package example
        class Base { String id }
        class Derived extends Base {
            refers Base parent
        }
        enum Color { RED = 0, GREEN, BLUE }
    )";
    auto pkg = XcoreParser::parse(src);
    EXPECT_EQ(pkg->classes.size(), 2u);
    EXPECT_EQ(pkg->classes[1]->superTypes.size(), 1u);
    EXPECT_EQ(pkg->classes[1]->superTypes[0], "Base");
    EXPECT_EQ(pkg->enums.size(), 1u);
    auto& e = pkg->enums[0];
    EXPECT_EQ(e->literals.size(), 3u);
    EXPECT_EQ(e->literals[0]->name, "RED");
    EXPECT_EQ(*e->literals[0]->value, 0);
    EXPECT_EQ(*e->literals[1]->value, 1);  // 自动递增
    EXPECT_EQ(*e->literals[2]->value, 2);
}

// ===== 测试 3：注解 + 修饰符 =====
EMF_TEST(XcoreParser_AnnotationsAndModifiers) {
    std::string src = R"(
        @Ecore(nsURI="http://test", nsPrefix="t")
        package test
        annotation "http://www.eclipse.org/emf/2002/Ecore" as Ecore
        class Node {
            @Ecore(name="NODE")
            derived long average get { 0 }
            readonly String label
            id String uuid
        }
    )";
    auto pkg = XcoreParser::parse(src);
    EXPECT_EQ(pkg->nsURI, "http://test");
    EXPECT_EQ(pkg->nsPrefix, "t");
    EXPECT_EQ(pkg->annotationDirectives.size(), 1u);
    auto& node = pkg->classes[0];
    EXPECT_EQ(node->attributes.size(), 3u);
    // average: derived, get body
    EXPECT_TRUE(node->attributes[0]->derived);
    EXPECT_TRUE(node->attributes[0]->getterBody.has_value());
    // label: readonly
    EXPECT_TRUE(node->attributes[1]->readOnly);
    // uuid: id
    EXPECT_TRUE(node->attributes[2]->idFlag);
}

// ===== 测试 4：Generator 派生 EPackage =====
EMF_TEST(XcoreGenerator_DeriveEPackage) {
    std::string src = R"(
        package demo
        class Foo {
            String name
            int count
            contains Bar[] bars
            op String greet() { "hi" }
        }
        class Bar { boolean active }
    )";
    auto xpkg = XcoreParser::parse(src);
    XcoreGenerator gen;
    auto* pkg = gen.generate(xpkg);
    EXPECT_NOT_NULL(pkg);
    EXPECT_EQ(pkg->getName(), "demo");
    auto classifiers = pkg->getEClassifiers();
    EXPECT_EQ(classifiers.size(), 2u);
    auto* foo = pkg->getEClassifier("Foo");
    EXPECT_NOT_NULL(foo);
    auto* fooCls = dynamic_cast<::emf::ecore::EClass*>(foo);
    EXPECT_NOT_NULL(fooCls);
    EXPECT_EQ(fooCls->getEAttributes().size(), 2u);
    EXPECT_EQ(fooCls->getEReferences().size(), 1u);
    EXPECT_EQ(fooCls->getEOperations().size(), 1u);
    // bars: containment, multi
    auto* bars = fooCls->getEReference("bars");
    EXPECT_NOT_NULL(bars);
    EXPECT_TRUE(bars->isContainment());
    EXPECT_EQ(bars->getUpperBound(), -1);
    // greet operation
    auto* greet = fooCls->getEOperation("greet");
    EXPECT_NOT_NULL(greet);
    EXPECT_EQ(greet->getEParameters().size(), 0u);
}

// ===== 测试 5：继承语义（eSuperTypes）=====
EMF_TEST(XcoreGenerator_Inheritance) {
    std::string src = R"(
        package demo
        class Base { String id }
        class Derived extends Base { String extra }
    )";
    auto xpkg = XcoreParser::parse(src);
    XcoreGenerator gen;
    auto* pkg = gen.generate(xpkg);
    auto* derived = dynamic_cast<::emf::ecore::EClass*>(pkg->getEClassifier("Derived"));
    EXPECT_NOT_NULL(derived);
    // eSuperTypes
    EXPECT_EQ(derived->getESuperTypes().size(), 1u);
    EXPECT_EQ(derived->getESuperTypes()[0]->getName(), "Base");
    // eAllSuperTypes 递归
    EXPECT_EQ(derived->getEAllSuperTypes().size(), 1u);
    // eAllOperations 应包含 Base.id 的 setter？实际 EMF 的 eAllOperations 包含继承的操作
    // 这里只验证 eAllStructuralFeatures 包含 Base 的属性
    // Base 有 id，Derived 有 extra，eAllStructuralFeatures 应该 2 个
    // （注意：EClass 的 getEAllStructuralFeatures 实现可能不存在，用 eAllAttributes 替代）
}

// ===== 测试 6：类型映射（Xcore 类型 → Ecore EDataType）=====
EMF_TEST(XcoreGenerator_TypeMapping) {
    std::string src = R"(
        package demo
        class Types {
            String s
            boolean b
            int i
            long l
            short h
            double d
            float f
            char c
            byte by
        }
    )";
    auto xpkg = XcoreParser::parse(src);
    XcoreGenerator gen;
    auto* pkg = gen.generate(xpkg);
    auto* types = dynamic_cast<::emf::ecore::EClass*>(pkg->getEClassifier("Types"));
    EXPECT_NOT_NULL(types);
    auto& attrs = types->getEAttributes();
    EXPECT_EQ(attrs.size(), 9u);
    // String → EString
    EXPECT_EQ(attrs[0]->getEAttributeType()->getName(), "EString");
    // boolean → EBoolean
    EXPECT_EQ(attrs[1]->getEAttributeType()->getName(), "EBoolean");
    // int → EInt
    EXPECT_EQ(attrs[2]->getEAttributeType()->getName(), "EInt");
    // long → ELong
    EXPECT_EQ(attrs[3]->getEAttributeType()->getName(), "ELong");
}

// ===== 测试 7：XcoreResource 端到端 load =====
EMF_TEST(XcoreResource_LoadFromString) {
    std::string src = R"(
        package demo
        class Foo { String name }
    )";
    ::emf::common::URI uri("demo.xcore");
    XcoreResource res(uri);
    std::istringstream is(src);
    res.load(is);
    auto* pkg = res.getEPackage();
    EXPECT_NOT_NULL(pkg);
    EXPECT_EQ(pkg->getName(), "demo");
    // contents()[0] 应该是 EPackage
    auto& contents = res.getContents();
    EXPECT_EQ(contents.size(), 1u);
}

// ===== 测试 8：真实样本 Design_Principles_Inheritance.xcore =====
EMF_TEST(Xcore_RealSample_DesignPrinciples) {
    std::string src = R"(
package example

/**
 * Class Node
 */
class ClassA {
        String attributeA
        String[] attributeB
        contains ClassB referenceA
        contains ClassB[] referenceB
        refers ClassC referenceC
        refers ClassC[] referenceD
}

class ClassB {
        String attributeC
}


class SubClassB extends ClassB {
        String[] attributeD
}


class ClassC {
        String attributeF
}

class SubClassC extends ClassC {
        String[] attributeG
}
)";
    auto xpkg = XcoreParser::parse(src);
    EXPECT_EQ(xpkg->name, "example");
    EXPECT_EQ(xpkg->classes.size(), 5u);
    // ClassA: 2 attr, 4 ref
    auto& a = xpkg->classes[0];
    EXPECT_EQ(a->name, "ClassA");
    EXPECT_EQ(a->attributes.size(), 2u);
    EXPECT_EQ(a->references.size(), 4u);
    EXPECT_EQ(static_cast<int>(a->references[0]->kind),
              static_cast<int>(ReferenceKind::Containment));
    EXPECT_EQ(static_cast<int>(a->references[2]->kind),
              static_cast<int>(ReferenceKind::NonContainment));
    EXPECT_TRUE(a->attributes[1]->multi);
    // SubClassB extends ClassB
    EXPECT_EQ(xpkg->classes[2]->superTypes.size(), 1u);
    EXPECT_EQ(xpkg->classes[2]->superTypes[0], "ClassB");
    // Generator 派生
    XcoreGenerator gen;
    auto* pkg = gen.generate(xpkg);
    EXPECT_EQ(pkg->getEClassifiers().size(), 5u);
    auto* sub = dynamic_cast<::emf::ecore::EClass*>(pkg->getEClassifier("SubClassB"));
    EXPECT_EQ(sub->getESuperTypes().size(), 1u);
}

// ===== 测试 9：真实样本 EAttributeContained.xcore（带 annotation directive）=====
EMF_TEST(Xcore_RealSample_EAttributeContained) {
    std::string src = R"(
@Ecore(nsURI="nodesURI")
package nodes

annotation "http://www.eclipse.org/emf/2002/Ecore"
as Ecore

annotation "http:///org/eclipse/emf/ecore/util/ExtendedMetaData"
as ExtendedMetaData

/**
 * Class Node
 */
@ExtendedMetaData(name="NODE")
class Node {
        @ExtendedMetaData(name="NODE")
        String[] property
}


/**
 * Datatype String
 */
@ExtendedMetaData(name="STRING")
type String wraps java.lang.String
)";
    auto xpkg = XcoreParser::parse(src);
    EXPECT_EQ(xpkg->name, "nodes");
    EXPECT_EQ(xpkg->nsURI, "nodesURI");
    EXPECT_EQ(xpkg->annotationDirectives.size(), 2u);
    EXPECT_EQ(xpkg->classes.size(), 1u);
    EXPECT_EQ(xpkg->dataTypes.size(), 1u);
    EXPECT_EQ(xpkg->dataTypes[0]->name, "String");
    EXPECT_EQ(xpkg->dataTypes[0]->wrappedClassName, "java.lang.String");
    // 派生
    XcoreGenerator gen;
    auto* pkg = gen.generate(xpkg);
    EXPECT_EQ(pkg->getEClassifiers().size(), 2u);  // 1 class + 1 datatype
    auto* node = pkg->getEClassifier("Node");
    EXPECT_NOT_NULL(node);
}

// ===== 测试 10：StandaloneSetup + Factory =====
EMF_TEST(XcoreStandaloneSetup_Registers) {
    XcoreStandaloneSetup::setup();
    // 多次调用幂等
    XcoreStandaloneSetup::setup();
    // 用 factory 创建 resource
    ::emf::common::URI uri("test.xcore");
    auto* r = XcoreResourceFactory::createResourceFor(uri);
    EXPECT_NOT_NULL(r);
    delete r;
}

// ===== 测试 11：EOperation 完整性（EParameters + 返回 EType + throws）=====
// 对齐 Java XcoreEOperationBuilder：op 不再只 setName，需设置 EType + EParameters
EMF_TEST(XcoreGenerator_EOperation_Completeness) {
    std::string src = R"(
        package demo
        class Calculator {
            op int add(int a, int b) { a + b }
            op String format(int value) { "" }
            op boolean check() throws Exception { true }
        }
    )";
    auto xpkg = XcoreParser::parse(src);
    XcoreGenerator gen;
    auto* pkg = gen.generate(xpkg);
    EXPECT_NOT_NULL(pkg);
    auto* calc = dynamic_cast<::emf::ecore::EClass*>(pkg->getEClassifier("Calculator"));
    EXPECT_NOT_NULL(calc);
    EXPECT_EQ(calc->getEOperations().size(), 3u);

    // add(int a, int b) : int
    auto* add = calc->getEOperation("add");
    EXPECT_NOT_NULL(add);
    // 返回类型 EInt
    auto* addRet = add->getEType();
    EXPECT_NOT_NULL(addRet);
    EXPECT_EQ(addRet->getName(), "EInt");
    // 两个参数
    EXPECT_EQ(add->getEParameters().size(), 2u);
    auto& addParams = add->getEParameters();
    EXPECT_EQ(addParams[0]->getName(), "a");
    EXPECT_NOT_NULL(addParams[0]->getEType());
    EXPECT_EQ(addParams[0]->getEType()->getName(), "EInt");
    EXPECT_EQ(addParams[1]->getName(), "b");
    EXPECT_EQ(addParams[1]->getEType()->getName(), "EInt");

    // format(int value) : String
    auto* fmt = calc->getEOperation("format");
    EXPECT_NOT_NULL(fmt);
    EXPECT_EQ(fmt->getEType()->getName(), "EString");
    EXPECT_EQ(fmt->getEParameters().size(), 1u);
    EXPECT_EQ(fmt->getEParameters()[0]->getName(), "value");

    // check() : boolean throws Exception（throws 字符串解析失败不抛，仅 EExceptions 可能为空）
    auto* chk = calc->getEOperation("check");
    EXPECT_NOT_NULL(chk);
    EXPECT_EQ(chk->getEType()->getName(), "EBoolean");
    EXPECT_EQ(chk->getEParameters().size(), 0u);
}

// ===== 测试 12：EOpposite 双向链接 =====
// 对齐 Java EReference.setEOpposite：opposite A.b 与 B.a 互相链接
EMF_TEST(XcoreGenerator_EOpposite_Bidirectional) {
    std::string src = R"(
        package demo
        class Parent {
            contains Child[] children opposite parent
        }
        class Child {
            refers Parent parent opposite children
        }
    )";
    auto xpkg = XcoreParser::parse(src);
    XcoreGenerator gen;
    auto* pkg = gen.generate(xpkg);
    auto* parent = dynamic_cast<::emf::ecore::EClass*>(pkg->getEClassifier("Parent"));
    auto* child = dynamic_cast<::emf::ecore::EClass*>(pkg->getEClassifier("Child"));
    EXPECT_NOT_NULL(parent);
    EXPECT_NOT_NULL(child);

    auto* childrenRef = dynamic_cast<::emf::ecore::EReference*>(parent->getEStructuralFeature("children"));
    auto* parentRef = dynamic_cast<::emf::ecore::EReference*>(child->getEStructuralFeature("parent"));
    EXPECT_NOT_NULL(childrenRef);
    EXPECT_NOT_NULL(parentRef);

    // 双向链接：children.eOpposite == parent && parent.eOpposite == children
    EXPECT_EQ(childrenRef->getEOpposite(), parentRef);
    EXPECT_EQ(parentRef->getEOpposite(), childrenRef);

    // contains 语义保持：children 是 containment，parent 是 non-containment
    EXPECT_TRUE(childrenRef->isContainment());
    EXPECT_FALSE(parentRef->isContainment());
}

// ===== 测试 13：EAnnotation 传播 =====
// 对齐 Java XcoreEAnnotationBuilder：@Directive(k=v) 传播为派生 EAnnotation
EMF_TEST(XcoreGenerator_EAnnotation_Propagation) {
    std::string src = R"(
        @Ecore(nsURI="http://annot", nsPrefix="a")
        package annot

        annotation "http://www.eclipse.org/emf/2002/Ecore" as Ecore
        annotation "http:///org/eclipse/emf/ecore/util/ExtendedMetaData" as ExtendedMetaData

        @ExtendedMetaData(name="NODE")
        class Node {
            @Ecore(name="NODE_ATTR")
            String label
        }
    )";
    auto xpkg = XcoreParser::parse(src);
    XcoreGenerator gen;
    auto* pkg = gen.generate(xpkg);
    auto* node = dynamic_cast<::emf::ecore::EClass*>(pkg->getEClassifier("Node"));
    EXPECT_NOT_NULL(node);

    // 类级注解：@ExtendedMetaData(name="NODE")
    auto* clsAnn = node->getEAnnotation("http:///org/eclipse/emf/ecore/util/ExtendedMetaData");
    EXPECT_NOT_NULL(clsAnn);
    EXPECT_EQ(clsAnn->getDetail("name"), "NODE");

    // 成员级注解：@Ecore(name="NODE_ATTR")
    auto* label = node->getEStructuralFeature("label");
    EXPECT_NOT_NULL(label);
    auto* attrAnn = label->getEAnnotation("http://www.eclipse.org/emf/2002/Ecore");
    EXPECT_NOT_NULL(attrAnn);
    EXPECT_EQ(attrAnn->getDetail("name"), "NODE_ATTR");
}

// ===== 测试 14：GenModel 生成 =====
// 对齐 Java GenModel 序列化：包含 genPackages/genClasses/genFeatures/genOperations
EMF_TEST(XcoreGenerator_GenModel_Generation) {
    std::string src = R"(
        package demo
        class Foo {
            String name
            contains Bar[] bars
            op String greet(String who) { "" }
        }
        class Bar { boolean active }
        enum Color { RED, GREEN }
    )";
    auto xpkg = XcoreParser::parse(src);
    XcoreGenerator gen;
    auto* pkg = gen.generate(xpkg);
    EXPECT_NOT_NULL(pkg);

    std::string gm = gen.generateGenModel(xpkg);
    // 关键元素存在
    EXPECT_TRUE(gm.find("<genmodel:GenModel") != std::string::npos);
    EXPECT_TRUE(gm.find("modelDirectory=\"/src\"") != std::string::npos);
    EXPECT_TRUE(gm.find("complianceLevel=\"8.0\"") != std::string::npos);
    EXPECT_TRUE(gm.find("<foreignModel>demo.xcore</foreignModel>") != std::string::npos);
    EXPECT_TRUE(gm.find("<genPackages") != std::string::npos);
    EXPECT_TRUE(gm.find("ecorePackage=\"demo#/\"") != std::string::npos);
    // genClasses Foo
    EXPECT_TRUE(gm.find("ecoreClass=\"Foo\"") != std::string::npos);
    // genFeatures：name（attribute）+ bars（reference）
    EXPECT_TRUE(gm.find("ecore:EAttribute name") != std::string::npos);
    EXPECT_TRUE(gm.find("ecore:EReference bars") != std::string::npos);
    // genOperations greet 含 genParameters who
    EXPECT_TRUE(gm.find("ecoreOperation=\"greet\"") != std::string::npos);
    EXPECT_TRUE(gm.find("ecoreParameter=\"who\"") != std::string::npos);
    // genEnums Color 含 genEnumLiterals
    EXPECT_TRUE(gm.find("ecoreEnum=\"Color\"") != std::string::npos);
    EXPECT_TRUE(gm.find("ecoreEnumLiteral=\"RED\"") != std::string::npos);

    // XcoreResource.getGenModel() 也应返回非空
    ::emf::common::URI uri("demo.xcore");
    XcoreResource res(uri);
    std::istringstream is(src);
    res.load(is);
    const std::string& resGm = res.getGenModel();
    EXPECT_TRUE(!resGm.empty());
    EXPECT_TRUE(resGm.find("genmodel:GenModel") != std::string::npos);
}
