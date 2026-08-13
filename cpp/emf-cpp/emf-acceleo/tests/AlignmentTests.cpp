// AlignmentTests.cpp — emf-acceleo + emf-xcore + emf-ecore 行为对齐集成测试
//
// 目标：验证三模块组合的端到端行为对齐 Java EMF/Acceleo。
// 流程（与 Java 等价）：
//   1. emf-xcore：解析 .xcore → 派生 EPackage（对齐 Java XcoreResource.load）
//   2. emf-ecore：基于派生 EPackage 的 EFactory 创建 EObject 实例
//                （对齐 Java EFactory.create）
//   3. emf-acceleo：以 EObject 实例为模型，求值 .mtl 模板生成代码
//                （对齐 Java AcceleoService.doGenerate）
//
// 验证点：所有中间产物与 Java 行为等价：
//   - Xcore 解析出的 EPackage.name / EClass.name / EAttribute.name 与源一致
//   - 派生 EFactory 能 create EClass 实例（动态模型，对齐 Java dynamic EObject）
//   - eGet / eSet 反射行为对齐 Java EObjectImpl
//   - Acceleo [for (c | p.eClassifiers)][c.name/][/for] 输出对齐 Java MTL
#include "test_main.h"
#include "emf/acceleo/AcceleoParser.h"
#include "emf/acceleo/AcceleoEngine.h"
#include "emf/ecore/xcore/XcoreParser.h"
#include "emf/ecore/xcore/XcoreGenerator.h"
#include "emf/ecore/xcore/XcoreResource.h"
#include "emf/ecore/EcorePackage.h"
#include "emf/ecore/EcoreImpls.h"
#include <sstream>
#include <fstream>
#include <filesystem>

using namespace emf::acceleo;
using namespace emf::ecore::xcore;

// ===== 对齐测试 1：xcore 派生 EPackage 元数据与 Java 一致 =====
// Java 等价：
//   XcoreResource res = new XcoreResourceImpl(uri);
//   res.load(Collections.emptyMap());
//   EPackage pkg = res.getContents().get(0);  // 派生 EPackage
//   assertEquals("demo", pkg.getName());
//   assertEquals("Foo", pkg.getEClassifiers().get(0).getName());
EMF_TEST(Alignment_XcoreDerivesEPackage_LikeJava) {
    std::string xcoreSrc = R"(
        package demo
        class Foo {
            String name
            int count
        }
        class Bar {
            boolean active
        }
    )";
    auto xpkg = XcoreParser::parse(xcoreSrc);
    XcoreGenerator gen;
    auto* pkg = gen.generate(xpkg);

    EXPECT_NOT_NULL(pkg);
    EXPECT_EQ(pkg->getName(), "demo");
    // 对齐 Java：getEClassifiers() 按声明顺序返回
    EXPECT_EQ(pkg->getEClassifiers().size(), 2u);
    EXPECT_EQ(pkg->getEClassifiers()[0]->getName(), "Foo");
    EXPECT_EQ(pkg->getEClassifiers()[1]->getName(), "Bar");

    // 反射取 name（对齐 Java eGet）
    auto* fooCls = pkg->getEClassifiers()[0];
    auto* nameFeat = fooCls->eClass()->getEStructuralFeature("name");
    EXPECT_NOT_NULL(nameFeat);
    auto nameVal = fooCls->eGet(nameFeat);
    EXPECT_EQ(std::any_cast<std::string>(nameVal), "Foo");
}

// ===== 对齐测试 2：xcore 派生 EClass 的 features 与 Java 一致 =====
// Java 等价：
//   EClass fooCls = (EClass) pkg.getEClassifier("Foo");
//   EAttribute nameAttr = (EAttribute) fooCls.getEStructuralFeature("name");
//   assertEquals("name", nameAttr.getName());
//   assertEquals(EcorePackage.eINSTANCE.getEString(), nameAttr.getEAttributeType());
EMF_TEST(Alignment_XcoreDerivedEClassFeatures_LikeJava) {
    std::string xcoreSrc = R"(
        package demo
        class Foo {
            String name
            int count
        }
    )";
    auto xpkg = XcoreParser::parse(xcoreSrc);
    XcoreGenerator gen;
    auto* pkg = gen.generate(xpkg);
    auto* fooCls = dynamic_cast<::emf::ecore::EClass*>(pkg->getEClassifier("Foo"));
    EXPECT_NOT_NULL(fooCls);

    // 对齐 Java：getEStructuralFeatures() 返回所有声明的 features
    EXPECT_EQ(fooCls->getEStructuralFeatures().size(), 2u);
    auto* nameFeat = fooCls->getEStructuralFeature("name");
    EXPECT_NOT_NULL(nameFeat);
    EXPECT_EQ(nameFeat->getName(), "name");

    // 类型解析：String → EString（对齐 Java XcoreTypeParameterResolver）
    auto* nameAttr = dynamic_cast<::emf::ecore::EAttribute*>(nameFeat);
    EXPECT_NOT_NULL(nameAttr);
    EXPECT_NOT_NULL(nameAttr->getEAttributeType());
    EXPECT_EQ(nameAttr->getEAttributeType()->getName(), "EString");

    auto* countFeat = fooCls->getEStructuralFeature("count");
    auto* countAttr = dynamic_cast<::emf::ecore::EAttribute*>(countFeat);
    EXPECT_EQ(countAttr->getEAttributeType()->getName(), "EInt");
}

// ===== 对齐测试 3：动态 EObject 创建 + eSet/eGet 反射（对齐 Java EFactory.create）=====
// Java 等价：
//   EFactory factory = pkg.getEFactoryInstance();
//   EObject foo = factory.create(fooCls);
//   foo.eSet(nameAttr, "hello");
//   assertEquals("hello", foo.eGet(nameAttr));
EMF_TEST(Alignment_DynamicEObject_eSet_eGet_LikeJava) {
    std::string xcoreSrc = R"(
        package demo
        class Foo {
            String name
            int count
        }
    )";
    auto xpkg = XcoreParser::parse(xcoreSrc);
    XcoreGenerator gen;
    auto* pkg = gen.generate(xpkg);
    auto* fooCls = dynamic_cast<::emf::ecore::EClass*>(pkg->getEClassifier("Foo"));
    auto* factory = pkg->getEFactoryInstance();
    EXPECT_NOT_NULL(factory);

    // 动态创建（对齐 Java EFactory.create(EClass)）
    auto* foo = factory->create(fooCls);
    EXPECT_NOT_NULL(foo);

    // 反射 eSet/eGet（对齐 Java EObjectImpl.eSet/eGet）
    auto* nameFeat = fooCls->getEStructuralFeature("name");
    foo->eSet(nameFeat, std::any{std::string{"hello"}});
    auto v = foo->eGet(nameFeat);
    EXPECT_EQ(std::any_cast<std::string>(v), "hello");
}

// ===== 对齐测试 4：Acceleo MTL 对派生 EPackage 求值（核心对齐点）=====
// Java 等价：
//   AcceleoService svc = new AcceleoService(genModule);
//   String out = svc.generate(template, pkg);
//   out 应为 "Foo Bar "
// 这是 xcore + ecore + acceleo 三模块组合的核心验证：
//   - xcore 派生 EPackage 的 eClassifiers 多值 feature
//   - ecore eGet 返回 std::vector<EClassifier*>
//   - acceleo ForBlock 通过 asEObjectVector 正确迭代
EMF_TEST(Alignment_AcceleoGeneratesFromXcorePackage_LikeJava) {
    std::string xcoreSrc = R"(
        package demo
        class Foo { String name }
        class Bar { String name }
    )";
    auto xpkg = XcoreParser::parse(xcoreSrc);
    XcoreGenerator gen;
    auto* pkg = gen.generate(xpkg);

    // MTL 模板：[for (c | p.eClassifiers)][c.name/] [/for]
    std::string mtl = R"(
[module gen(p : EPackage)]
[template public gen(p : EPackage)][for (c | p.eClassifiers)][c.name/] [/for][/template]
[/module]
)";
    AcceleoService svc;
    std::string out = svc.evaluateTemplate(mtl, "gen", {std::any{pkg}});
    EXPECT_EQ(out, "Foo Bar ");
}

// ===== 对齐测试 5：Acceleo 生成 C++ 类骨架（端到端代码生成）=====
// Java Acceleo 的典型用途：从 EPackage 生成代码骨架。
// 验证 [for]/[if]/[let]/表达式拼接组合工作正常。
EMF_TEST(Alignment_AcceleoGeneratesCppClassSkeleton_LikeJava) {
    std::string xcoreSrc = R"(
        package demo
        class Foo {
            String name
            int count
        }
    )";
    auto xpkg = XcoreParser::parse(xcoreSrc);
    XcoreGenerator gen;
    auto* pkg = gen.generate(xpkg);
    auto* fooCls = dynamic_cast<::emf::ecore::EClass*>(pkg->getEClassifier("Foo"));

    std::string mtl = R"(
[module gen(c : EClass)]
[template public gen(c : EClass)]
class [c.name/] {
public:
[for (a | c.eStructuralFeatures)]    [a.eAttributeType.name/] [a.name/];
[/for]};
[/template]
[/module]
)";
    AcceleoService svc;
    std::string out = svc.evaluateTemplate(mtl, "gen", {std::any{fooCls}});
    // 期望生成：
    //   class Foo {
    //   public:
    //       EString name;
    //       EInt count;
    //   };
    // 注意 skipLineAfterTag 已消费开标签后的换行
    std::string expected =
        "class Foo {\n"
        "public:\n"
        "    EString name;\n"
        "    EInt count;\n"
        "};\n";
    EXPECT_EQ(out, expected);
}

// ===== 对齐测试 6：[file] 块从 xcore 派生 EPackage 写文件 =====
// Java 等价：AcceleoService.doGenerate(model, module, outputFolder)
EMF_TEST(Alignment_AcceleoWritesFile_FromXcorePackage_LikeJava) {
    std::string xcoreSrc = R"(
        package demo
        class Foo { String name }
    )";
    auto xpkg = XcoreParser::parse(xcoreSrc);
    XcoreGenerator gen;
    auto* pkg = gen.generate(xpkg);
    auto* fooCls = dynamic_cast<::emf::ecore::EClass*>(pkg->getEClassifier("Foo"));

    std::string mtl = R"(
[module gen(c : EClass)]
[template public gen(c : EClass)]
[file (c.name + '.hpp', false)]
class [c.name/] {};
[/file]
[/template]
[/module]
)";
    AcceleoService svc;
    std::string outDir = "/tmp/acceleo_align_out";
    std::filesystem::remove_all(outDir);
    svc.doGenerate(mtl, fooCls, outDir);
    EXPECT_TRUE(std::filesystem::exists(outDir + "/Foo.hpp"));
    std::ifstream f(outDir + "/Foo.hpp");
    std::string content((std::istreambuf_iterator<char>(f)),
                         std::istreambuf_iterator<char>());
    EXPECT_EQ(content, "class Foo {};\n");
}

// ===== 对齐测试 7：XcoreResource 端到端 load（对齐 Java XcoreResource.load）=====
EMF_TEST(Alignment_XcoreResource_LoadsAndDerivesEPackage_LikeJava) {
    XcoreStandaloneSetup::setup();
    std::string xcoreSrc = R"(
        package demo
        class Foo { String name }
    )";
    ::emf::common::URI uri("test.xcore");
    XcoreResourceFactory f;
    auto* res = f.createResource(uri);
    std::istringstream iss(xcoreSrc);
    res->load(iss);
    auto* pkg = res->getEPackage();
    EXPECT_NOT_NULL(pkg);
    EXPECT_EQ(pkg->getName(), "demo");
    EXPECT_EQ(pkg->getEClassifiers().size(), 1u);
    EXPECT_EQ(pkg->getEClassifiers()[0]->getName(), "Foo");
    delete res;
}

// ===== 对齐测试 8：if + for + 表达式组合（对齐 Java Acceleo 复杂模板）=====
// Java 等价：MTL 模板中 [if (cond)]...[else]...[/if] 与 [for] 嵌套
EMF_TEST(Alignment_AcceleoComplexTemplate_IfForNested_LikeJava) {
    std::string xcoreSrc = R"(
        package demo
        class Foo {
            String name
            int count
        }
        class Bar {
            boolean active
        }
    )";
    auto xpkg = XcoreParser::parse(xcoreSrc);
    XcoreGenerator gen;
    auto* pkg = gen.generate(xpkg);

    // 模板：枚举每个 classifier，类有 1 个 feature 输出 "single"，
    //       否则输出 "multi: N"
    std::string mtl = R"(
[module gen(p : EPackage)]
[template public gen(p : EPackage)][for (c | p.eClassifiers)][if (c.eStructuralFeatures->size() = 1)][c.name/]: single
[else][c.name/]: multi: [c.eStructuralFeatures->size()/]
[/if][/for][/template]
[/module]
)";
    AcceleoService svc;
    std::string out = svc.evaluateTemplate(mtl, "gen", {std::any{pkg}});
    // Foo 有 2 个 features → "Foo: multi: 2"
    // Bar 有 1 个 feature → "Bar: single"
    std::string expected = "Foo: multi: 2\nBar: single\n";
    EXPECT_EQ(out, expected);
}
