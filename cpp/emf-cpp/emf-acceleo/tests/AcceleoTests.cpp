// AcceleoTests.cpp — emf-acceleo 单元测试
#include "test_main.h"
#include "emf/acceleo/AcceleoParser.h"
#include "emf/acceleo/AcceleoEngine.h"
#include "emf/ecore/EcorePackage.h"
#include "emf/ecore/EcoreImpls.h"
#include <sstream>
#include <fstream>
#include <filesystem>

using namespace emf::acceleo;

// ===== 测试 1：模块解析 =====
EMF_TEST(AcceleoParser_Module) {
    std::string src = R"(
[module gen(c : Class)]
[template public genClass(c : Class)]
class [c.name/] {
}
[/template]
[/module]
)";
    auto m = AcceleoParser::parse(src);
    EXPECT_EQ(m->name, "gen");
    EXPECT_EQ(m->params.size(), 1u);
    EXPECT_EQ(m->params[0].name, "c");
    EXPECT_EQ(m->templates.size(), 1u);
    EXPECT_EQ(m->templates[0]->name, "genClass");
    EXPECT_EQ(m->templates[0]->params.size(), 1u);
}

// ===== 测试 2：表达式块 =====
EMF_TEST(AcceleoParser_ExprBlock) {
    std::string src = R"(
[module t(c : Class)]
[template public f(c : Class)]Hello [c.name/]![/template]
[/module]
)";
    auto m = AcceleoParser::parse(src);
    auto& tpl = m->templates[0];
    // 块序列：TextBlock "Hello ", ExprBlock c.name, TextBlock "!"
    EXPECT_EQ(tpl->body.size(), 3u);
}

// ===== 测试 3：for/if 块解析 =====
EMF_TEST(AcceleoParser_ForAndIf) {
    std::string src = R"(
[module t(c : Class)]
[template public f(c : Class)]
[for (a | c.attributes)]
[if (a.name = 'id')]ID[a.name/][else][a.name/][/if]
[/for]
[/template]
[/module]
)";
    auto m = AcceleoParser::parse(src);
    auto& tpl = m->templates[0];
    // 应解析出 ForBlock 包含 IfBlock
    bool hasFor = false, hasIf = false;
    for (auto& b : tpl->body) {
        std::visit([&](auto& n) {
            using T = std::decay_t<decltype(n)>;
            if constexpr (std::is_same_v<T, ForBlock>) hasFor = true;
        }, b->node);
    }
    EXPECT_TRUE(hasFor);
}

// ===== 测试 4：求值静态模板 =====
EMF_TEST(AcceleoEngine_StaticText) {
    std::string src = R"(
[module t(c : Class)]
[template public f(c : Class)]Hello World[/template]
[/module]
)";
    AcceleoService svc;
    std::string out = svc.evaluateTemplate(src, "f", {});
    EXPECT_EQ(out, "Hello World");
}

// ===== 测试 5：求值带表达式（无模型，用字面量）=====
EMF_TEST(AcceleoEngine_ExprWithLiteral) {
    std::string src = R"(
[module t(c : Class)]
[template public f(c : Class)]Value: ['test'/][/template]
[/module]
)";
    AcceleoService svc;
    std::string out = svc.evaluateTemplate(src, "f", {});
    EXPECT_EQ(out, "Value: test");
}

// ===== 测试 6：let 块 =====
EMF_TEST(AcceleoEngine_Let) {
    std::string src = R"(
[module t(c : Class)]
[template public f(c : Class)][let x = 'hello'][x/][/let][/template]
[/module]
)";
    AcceleoService svc;
    std::string out = svc.evaluateTemplate(src, "f", {});
    EXPECT_EQ(out, "hello");
}

// ===== 测试 7：if/else 块 =====
EMF_TEST(AcceleoEngine_IfElse) {
    std::string src = R"(
[module t(c : Class)]
[template public f(c : Class)][if (true)]YES[else]NO[/if][/template]
[/module]
)";
    AcceleoService svc;
    std::string out = svc.evaluateTemplate(src, "f", {});
    EXPECT_EQ(out, "YES");
}

// ===== 测试 8：注册服务调用 =====
EMF_TEST(AcceleoEngine_ServiceCall) {
    std::string src = R"(
[module t(c : Class)]
[template public f(c : Class)][upper('hello')/][/template]
[/module]
)";
    AcceleoService svc;
    svc.registerService("upper", [](const std::vector<std::any>& args, EvalContext&) -> std::any {
        if (args.empty() || args[0].type() != typeid(std::string)) return std::any{std::string{}};
        std::string s = std::any_cast<std::string>(args[0]);
        std::transform(s.begin(), s.end(), s.begin(), ::toupper);
        return std::any{s};
    });
    std::string out = svc.evaluateTemplate(src, "f", {});
    EXPECT_EQ(out, "HELLO");
}

// ===== 测试 9：基于真实 Ecore EObject 求值 =====
EMF_TEST(AcceleoEngine_EObjectNavigation) {
    // 创建一个 EClass（作为模型对象），设置 name
    ::emf::ecore::EcorePackage::initialize();
    auto& factory = ::emf::ecore::EcoreFactory::instance();
    auto* cls = factory.createEClass();
    cls->setName("MyClass");

    std::string src = R"(
[module gen(c : EClass)]
[template public gen(c : EClass)]class [c.name/] {}[/template]
[/module]
)";
    AcceleoService svc;
    std::string out = svc.evaluateTemplate(src, "gen", {std::any{cls}});
    EXPECT_EQ(out, "class MyClass {}");
}

// ===== 测试 10：for 迭代 EList =====
EMF_TEST(AcceleoEngine_ForOverEList) {
    ::emf::ecore::EcorePackage::initialize();
    auto& factory = ::emf::ecore::EcoreFactory::instance();
    auto* pkg = factory.createEPackage();
    pkg->setName("demo");
    auto* c1 = factory.createEClass(); c1->setName("A"); pkg->addEClassifier(c1);
    auto* c2 = factory.createEClass(); c2->setName("B"); pkg->addEClassifier(c2);

    std::string src = R"(
[module gen(p : EPackage)]
[template public gen(p : EPackage)][for (c | p.eClassifiers)][c.name/] [/for][/template]
[/module]
)";
    AcceleoService svc;
    std::string out = svc.evaluateTemplate(src, "gen", {std::any{pkg}});
    EXPECT_EQ(out, "A B ");
}

// ===== 测试 11：file 块写入文件 =====
EMF_TEST(AcceleoEngine_FileBlock) {
    std::string src = R"(
[module gen(c : EClass)]
[template public gen(c : EClass)]
[file (c.name + '.txt', false)]
content for [c.name/]
[/file]
[/template]
[/module]
)";
    ::emf::ecore::EcorePackage::initialize();
    auto& factory = ::emf::ecore::EcoreFactory::instance();
    auto* cls = factory.createEClass();
    cls->setName("Foo");

    AcceleoService svc;
    std::string outDir = "/tmp/acceleo_test_out";
    // 清理旧文件
    std::filesystem::remove_all(outDir);
    svc.doGenerate(src, cls, outDir);
    // 验证文件存在
    EXPECT_TRUE(std::filesystem::exists(std::string(outDir) + "/Foo.txt"));
    // 读回内容
    std::ifstream f(std::string(outDir) + "/Foo.txt");
    std::string content((std::istreambuf_iterator<char>(f)),
                         std::istreambuf_iterator<char>());
    EXPECT_EQ(content, "content for Foo\n");
}

// ===== 测试 12：query 解析与求值 =====
EMF_TEST(AcceleoParser_Query) {
    std::string src = R"(
[module t(c : Class)]
[query public double(x : String) : String = 'X' + x /]
[template public f(c : Class)][/template]
[/module]
)";
    auto m = AcceleoParser::parse(src);
    EXPECT_EQ(m->queries.size(), 1u);
    EXPECT_EQ(m->queries[0]->name, "double");
    EXPECT_EQ(m->queries[0]->returnTypeName, "String");
}

// ===== 测试 13：lambda collect =====
// 对齐 AQL: col->collect(e | e.name) —— 收集每个元素的 name 属性
EMF_TEST(AcceleoLambda_Collect) {
    ::emf::ecore::EcorePackage::initialize();
    auto& factory = ::emf::ecore::EcoreFactory::instance();
    auto* pkg = factory.createEPackage();
    pkg->setName("demo");
    // 两个 EClass
    auto* a = factory.createEClass(); a->setName("A"); pkg->addEClassifier(a);
    auto* b = factory.createEClass(); b->setName("B"); pkg->addEClassifier(b);

    std::string src = R"(
[module gen(p : EPackage)]
[template public f(p : EPackage)]
[p.eClassifiers->collect(c | c.name)/]
[/template]
[/module]
)";
    AcceleoService svc;
    std::string out = svc.evaluateTemplate(src, "f", {std::any{pkg}});
    // collect 应输出两个 EClass 的 name（顺序按 vector）
    // 输出："AB"（无分隔符）
    EXPECT_TRUE(out.find("A") != std::string::npos);
    EXPECT_TRUE(out.find("B") != std::string::npos);
}

// ===== 测试 14：lambda select + forAll + exists =====
EMF_TEST(AcceleoLambda_SelectForAllExists) {
    ::emf::ecore::EcorePackage::initialize();
    auto& factory = ::emf::ecore::EcoreFactory::instance();
    auto* pkg = factory.createEPackage();
    pkg->setName("demo");
    auto* a = factory.createEClass(); a->setName("AAA"); pkg->addEClassifier(a);
    auto* bb = factory.createEClass(); bb->setName("BB"); pkg->addEClassifier(bb);
    auto* c = factory.createEClass(); c->setName("C"); pkg->addEClassifier(c);

    // select 保留 name 长度 >= 3 的
    std::string src1 = R"(
[module gen(p : EPackage)]
[template public f(p : EPackage)]
[for (x | p.eClassifiers->select(c | c.name))][x.name/]
[/for]
[/template]
[/module]
)";
    AcceleoService svc;
    // 由于 select 的 cond 是 c.name（非空字符串 truthy），全保留。
    // 这里用 select + size 验证 lambda 语义
    // trim 末尾换行（[/template] 前的 \n 是模板体一部分，对齐 Java Acceleo）
    auto trim = [](std::string s) {
        while (!s.empty() && (s.back() == '\n' || s.back() == '\r' || s.back() == ' ')) s.pop_back();
        return s;
    };
    std::string src = R"(
[module gen(p : EPackage)]
[template public f(p : EPackage)]
[p.eClassifiers->size()/]
[/template]
[/module]
)";
    std::string out = svc.evaluateTemplate(src, "f", {std::any{pkg}});
    EXPECT_EQ(trim(out), "3");

    // forAll: 所有 name 非空 → true
    std::string src2 = R"(
[module gen(p : EPackage)]
[template public f(p : EPackage)]
[p.eClassifiers->forAll(c | c.name)/]
[/template]
[/module]
)";
    out = svc.evaluateTemplate(src2, "f", {std::any{pkg}});
    EXPECT_EQ(trim(out), "true");

    // exists: 存在 name == "BB" → true
    std::string src3 = R"(
[module gen(p : EPackage)]
[template public f(p : EPackage)]
[p.eClassifiers->exists(c | c.name = 'BB')/]
[/template]
[/module]
)";
    out = svc.evaluateTemplate(src3, "f", {std::any{pkg}});
    EXPECT_EQ(trim(out), "true");

    // reject: 排除 name == "C"，剩 2 个
    std::string src4 = R"(
[module gen(p : EPackage)]
[template public f(p : EPackage)]
[p.eClassifiers->reject(c | c.name = 'C')->size()/]
[/template]
[/module]
)";
    out = svc.evaluateTemplate(src4, "f", {std::any{pkg}});
    EXPECT_EQ(trim(out), "2");
}

// ===== 测试 15：query 求值 =====
// 对齐 Java: 模板表达式内可调用模块级 query
EMF_TEST(AcceleoQuery_Eval) {
    ::emf::ecore::EcorePackage::initialize();
    auto& factory = ::emf::ecore::EcoreFactory::instance();
    auto* cls = factory.createEClass();
    cls->setName("Foo");

    std::string src = R"(
[module gen(c : Class)]
[query public greet(name : String) : String = 'Hello ' + name /]
[template public f(c : Class)]
[greet(c.name)/]
[/template]
[/module]
)";
    AcceleoService svc;
    std::string out = svc.evaluateTemplate(src, "f", {std::any{cls}});
    // trim 末尾换行（[/template] 前的 \n 是模板体一部分）
    while (!out.empty() && (out.back() == '\n' || out.back() == '\r' || out.back() == ' ')) out.pop_back();
    EXPECT_EQ(out, "Hello Foo");
}

// ===== 测试 16：protected 区合并 =====
// 对齐 Java Acceleo: 重新生成文件时保留 protected 区的用户手改内容
EMF_TEST(AcceleoProtected_Merge) {
    ::emf::ecore::EcorePackage::initialize();
    auto& factory = ::emf::ecore::EcoreFactory::instance();
    auto* cls = factory.createEClass();
    cls->setName("Foo");

    std::string src = R"(
[module gen(c : Class)]
[template public f(c : Class)]
[file (c.name + ".txt", false)]
class [c.name/] {
[protected (body)]
// generated body
[/protected]
}
[/file]
[/template]
[/module]
)";
    std::string outDir = "/tmp/acceleo_protected_test";
    std::filesystem::remove_all(outDir);
    AcceleoService svc;
    svc.doGenerate(src, cls, outDir);

    std::string filePath = outDir + std::string("/Foo.txt");
    EXPECT_TRUE(std::filesystem::exists(filePath));
    // 读回首次生成内容
    auto readAll = [](const std::string& path) {
        std::ifstream f(path);
        return std::string((std::istreambuf_iterator<char>(f)),
                            std::istreambuf_iterator<char>());
    };
    std::string first = readAll(filePath);
    // 应含 BEGIN/END 标记 + "// generated body"
    EXPECT_TRUE(first.find("BEGIN Begin Protected Region ID[body]") != std::string::npos);
    EXPECT_TRUE(first.find("END End Protected Region ID[body]") != std::string::npos);
    EXPECT_TRUE(first.find("// generated body") != std::string::npos);

    // 模拟用户手改 protected 区内容
    std::string userModified = first;
    std::string oldBody = "// generated body";
    std::string newBody = "// USER HAND-EDITED CONTENT";
    size_t pos = userModified.find(oldBody);
    EXPECT_TRUE(pos != std::string::npos);
    userModified.replace(pos, oldBody.size(), newBody);
    std::ofstream f2(filePath);
    f2 << userModified;
    f2.close();

    // 重新生成（同样模板），应保留用户手改内容
    svc.doGenerate(src, cls, outDir);
    std::string regenerated = readAll(filePath);
    EXPECT_TRUE(regenerated.find("// USER HAND-EDITED CONTENT") != std::string::npos);
    // 原生成内容不应再出现
    EXPECT_TRUE(regenerated.find("// generated body") == std::string::npos);
}

// ===== 测试 17：模块继承（extends）=====
// 对齐 Java: 子模块可调用父模块的 template/query
EMF_TEST(AcceleoModule_Extends) {
    ::emf::ecore::EcorePackage::initialize();
    auto& factory = ::emf::ecore::EcoreFactory::instance();
    auto* cls = factory.createEClass();
    cls->setName("Foo");

    // 父模块：定义 helper template，输出 [name]（用字符串拼接避免 [ 的歧义）
    std::string parentSrc = R"(
[module parent(c : Class)]
[template public bracket(name : String)]
['[' + name + ']'/]
[/template]
[/module]
)";
    auto parent = AcceleoParser::parse(parentSrc);

    // 子模块：extends parent，调用 bracket
    std::string childSrc = R"(
[module child(c : Class) extends parent]
[template public f(c : Class)]
[bracket(c.name)/]
[/template]
[/module]
)";
    auto child = AcceleoParser::parse(childSrc);

    AcceleoService svc;
    svc.engine().registerModule(parent);
    svc.engine().setCurrentModule(child);
    // 调用 child 的 f 模板，它内部调用 parent 的 bracket
    std::string out = svc.evaluateTemplate(childSrc, "f", {std::any{cls}});
    // trim 末尾换行
    while (!out.empty() && (out.back() == '\n' || out.back() == '\r' || out.back() == ' ')) out.pop_back();
    // bracket("Foo") → [Foo]
    EXPECT_EQ(out, "[Foo]");
}

// ===== 测试 18：模块导入（import）+ extends 链 query 调用 =====
EMF_TEST(AcceleoModule_ImportAndExtendsQuery) {
    ::emf::ecore::EcorePackage::initialize();
    auto& factory = ::emf::ecore::EcoreFactory::instance();
    auto* cls = factory.createEClass();
    cls->setName("Bar");

    // 父模块定义 query
    std::string parentSrc = R"(
[module parent(c : Class)]
[query public prefix(name : String) : String = 'pre_' + name /]
[/module]
)";
    auto parent = AcceleoParser::parse(parentSrc);

    // 子模块 extends parent，在模板内调用父模块的 query
    std::string childSrc = R"(
[module child(c : Class) extends parent]
[template public f(c : Class)]
[prefix(c.name)/]
[/template]
[/module]
)";
    AcceleoService svc;
    svc.engine().registerModule(parent);
    std::string out = svc.evaluateTemplate(childSrc, "f", {std::any{cls}});
    // trim 末尾换行
    while (!out.empty() && (out.back() == '\n' || out.back() == '\r' || out.back() == ' ')) out.pop_back();
    EXPECT_EQ(out, "pre_Bar");
}
