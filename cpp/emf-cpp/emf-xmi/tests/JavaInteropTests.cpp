// 测试用 Java EMF 生成的 .ecore/.xmi 文件做互读
// 对齐 Java: org.eclipse.emf.test.tools/data/ant.expected/...
#include "test_main.h"
#include "emf/xmi/XMIResource.h"
#include "emf/xmi/XMIResourceFactory.h"
#include "emf/common/EPackageRegistry.h"
#include "emf/ecore/EcorePackage.h"
#include <cstring>
#include <fstream>
#include <sstream>
#include <iostream>

namespace {
constexpr const char* kJavaEcorePath = "/workspace/emf-cpp-demo/build/java_ref/library.ecore";
constexpr const char* kJavaXmiPath   = "/workspace/emf-cpp-demo/build/java_ref/library.xmi";

std::string readAll(const char* path) {
    std::ifstream f(path);
    std::stringstream ss; ss << f.rdbuf();
    return ss.str();
}
}  // namespace

// 直接做 Java-style 加载：Java 的 library.ecore 是独立 EPackage document
// 因此根元素就是 <ecore:EPackage>，不需要外部包注册。
EMF_TEST(JavaInterop_LoadJavaLibraryEcore) {
    using namespace emf;
    std::string src = readAll(kJavaEcorePath);
    if (src.empty()) {
        std::fprintf(stderr, "skip: %s not found\n", kJavaEcorePath);
        return;  // skip
    }
    auto res = xmi::XMIResourceFactory::createResourceFor(common::URI::createFileURI(kJavaEcorePath));
    try {
        res->loadFromString(src);
    } catch (const std::exception& e) {
        std::fprintf(stderr, "load exception: %s\n", e.what());
        throw;
    }
    std::fprintf(stderr, "LoadJavaLibraryEcore: contents=%zu\n", res->getContents().size());
    EXPECT_NOT_NULL(res->getContents().front());
    // 期望根对象是 EPackage
    auto* pkg = dynamic_cast<ecore::EPackage*>(res->getContents().front());
    EXPECT_NOT_NULL(pkg);
    std::fprintf(stderr, "LoadJavaLibraryEcore: pkg=%p name=%s nsURI=%s classifiers=%zu\n",
                (void*)pkg, pkg->getName().c_str(), pkg->getNsURI().c_str(), pkg->getEClassifiers().size());
    for (auto* c : pkg->getEClassifiers()) {
        std::fprintf(stderr, "  classifier: %p %s\n", (void*)c, c ? c->getName().c_str() : "<null>");
    }
    EXPECT_EQ(pkg->getName(), std::string("library"));
    EXPECT_EQ(pkg->getNsURI(), std::string("http://example.com/emfdemo/library"));
    EXPECT_EQ(pkg->getNsPrefix(), std::string("library"));
    EXPECT_TRUE(pkg->getEClassifiers().size() >= 4u);
}

EMF_TEST(JavaInterop_SaveBackJavaLibraryEcore) {
    using namespace emf;
    std::string src = readAll(kJavaEcorePath);
    if (src.empty()) return;
    auto res = xmi::XMIResourceFactory::createResourceFor(common::URI::createFileURI(kJavaEcorePath));
    res->loadFromString(src);
    std::string out = res->saveToString();
    std::fprintf(stderr, "SaveBackJavaLibraryEcore output:\n%s\n", out.c_str());
    EXPECT_TRUE(out.find("nsURI=\"http://example.com/emfdemo/library\"") != std::string::npos);
    EXPECT_TRUE(out.find("name=\"library\"") != std::string::npos);
    EXPECT_TRUE(out.find("nsPrefix=\"library\"") != std::string::npos);
    EXPECT_TRUE(out.find("BookCategory") != std::string::npos);
    EXPECT_TRUE(out.find("Library") != std::string::npos);
    EXPECT_TRUE(out.find("xsi:type=\"ecore:EClass\"") != std::string::npos);
    EXPECT_TRUE(out.find("xsi:type=\"ecore:EEnum\"") != std::string::npos);
    EXPECT_TRUE(out.find("xsi:type=\"ecore:EDataType\"") != std::string::npos);
    // 只允许 1 个 ecore:EPackage 包裹
    auto pos = out.find("<ecore:EPackage");
    if (pos != std::string::npos) {
        auto pos2 = out.find("<ecore:EPackage", pos + 1);
        EXPECT_TRUE(pos2 == std::string::npos);
    }
}
