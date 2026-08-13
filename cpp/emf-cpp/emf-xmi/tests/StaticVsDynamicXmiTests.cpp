// StaticVsDynamicXmiTests.cpp —— 静态(codegen) vs 动态(DynamicEObject) XMI 产出一致性
// 对齐 Java: 同一 .ecore 元模型，两种实例化方式产出的 XMI 应语义等价
//
// 流程：
//   1. 加载 library.ecore 得到 EPackage
//   2. 动态方式：factory->create + eSet 构造对象 → saveToString 得 dynamicXmi
//   3. 静态方式：codegen 生成 C++ 静态类 → driver 用 typed API 构造相同对象 → saveToString 得 staticXmi
//   4. 对比两者：XML 规范化后关键字段（name/title/pages/containment 结构）一致
//   5. 重新加载 dynamicXmi，验证字段保持（roundtrip）
#include "test_main.h"
#include "emf/xmi/XMIResource.h"
#include "emf/xmi/XMIResourceFactory.h"
#include "emf/ecore/EcorePackage.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/ecore/DynamicEObject.h"
#include "emf/common/EPackageRegistry.h"
#include "emf/common/EList.h"
#include "emf/ecore/codegen/CppGenerator.h"

#include <any>
#include <cstdlib>
#include <fstream>
#include <sstream>
#include <string>
#include <filesystem>

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
using emf::ecore::codegen::CppGenerator, emf::ecore::codegen::GenConfig;
using emf::common::EObject;

namespace {

void initEnv() {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    XMIResourceFactory::registerDefaults();
}

// library.ecore 路径（codegen 样本，含 Library/Book/Writer）
const char* kLibraryEcoreRel = "/emf-ecore-codegen/tests/samples/library.ecore";

// 动态构造：Library(name) -> Book(title,pages) + Book(title)
// 返回 saveToString 的 XMI
std::string buildDynamicXmi(XMIResource& res) {
    auto* pkg = dynamic_cast<EPackage*>(res.getContents().front());
    res.getContents().clear();  // 移除 EPackage，只保留实例
    auto* libCls = dynamic_cast<EClass*>(pkg->getEClassifier("Library"));
    auto* bookCls = dynamic_cast<EClass*>(pkg->getEClassifier("Book"));
    auto* factory = pkg->getEFactoryInstance();

    auto* lib = factory->create(libCls);
    lib->eSet(libCls->getEStructuralFeature("name"), std::any(std::string("Test Library")));

    auto* b0 = factory->create(bookCls);
    b0->eSet(bookCls->getEStructuralFeature("title"), std::any(std::string("Book One")));
    b0->eSet(bookCls->getEStructuralFeature("pages"), std::any(100));

    auto* b1 = factory->create(bookCls);
    b1->eSet(bookCls->getEStructuralFeature("title"), std::any(std::string("Book Two")));
    b1->eSet(bookCls->getEStructuralFeature("pages"), std::any(200));

    auto* booksFeat = libCls->getEStructuralFeature("books");
    emf_test::addToContainment(lib, booksFeat, b0);
    emf_test::addToContainment(lib, booksFeat, b1);

    res.addToContents(lib);
    return res.saveToString();
}

// XML 规范化：去除首尾空白、压缩连续空白，便于语义对比
std::string normalizeXml(const std::string& s) {
    std::string out;
    out.reserve(s.size());
    bool inWhitespace = false;
    bool startedContent = false;
    for (char c : s) {
        if (c == '\n' || c == '\r' || c == '\t' || c == ' ') {
            if (startedContent) inWhitespace = true;
        } else {
            if (inWhitespace) { out += ' '; inWhitespace = false; }
            out += c;
            startedContent = true;
        }
    }
    return out;
}

}  // namespace

// ===== 测试 1：动态方式产出 XMI 含正确结构 =====
EMF_TEST(StaticDynamic_DynamicXmi_HasExpectedFields) {
    initEnv();
    XMIResource res;
    res.loadFromString(std::string("<?xml version=\"1.0\"?>\n<ecore:EPackage xmlns:ecore=\"http://www.eclipse.org/emf/2002/Ecore\" xmlns:xmi=\"http://www.omg.org/XMI\" xmi:version=\"2.0\" name=\"library\" nsURI=\"http://example.com/library/1.0\" nsPrefix=\"library\"><eClassifiers xsi:type=\"ecore:EClass\" name=\"Library\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"name\" eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString\"/><eStructuralFeatures xsi:type=\"ecore:EReference\" name=\"books\" upperBound=\"-1\" eType=\"#//Book\" containment=\"true\"/></eClassifiers><eClassifiers xsi:type=\"ecore:EClass\" name=\"Book\"><eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"title\" eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString\"/><eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"pages\" eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EInt\"/></eClassifiers></ecore:EPackage>"));
    std::string xmi = buildDynamicXmi(res);
    EXPECT_TRUE(xmi.find("library:Library") != std::string::npos);
    EXPECT_TRUE(xmi.find("name=\"Test Library\"") != std::string::npos);
    EXPECT_TRUE(xmi.find("title=\"Book One\"") != std::string::npos);
    EXPECT_TRUE(xmi.find("pages=\"100\"") != std::string::npos);
    EXPECT_TRUE(xmi.find("title=\"Book Two\"") != std::string::npos);
    EXPECT_TRUE(xmi.find("pages=\"200\"") != std::string::npos);
}

// ===== 测试 2：动态 XMI roundtrip —— reload 后字段保持 =====
EMF_TEST(StaticDynamic_DynamicXmi_RoundtripPreservesFields) {
    initEnv();
    XMIResource res;
    res.loadFromString(std::string("<?xml version=\"1.0\"?>\n<ecore:EPackage xmlns:ecore=\"http://www.eclipse.org/emf/2002/Ecore\" xmlns:xmi=\"http://www.omg.org/XMI\" xmi:version=\"2.0\" name=\"library\" nsURI=\"http://example.com/library/1.0\" nsPrefix=\"library\"><eClassifiers xsi:type=\"ecore:EClass\" name=\"Library\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"name\" eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString\"/><eStructuralFeatures xsi:type=\"ecore:EReference\" name=\"books\" upperBound=\"-1\" eType=\"#//Book\" containment=\"true\"/></eClassifiers><eClassifiers xsi:type=\"ecore:EClass\" name=\"Book\"><eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"title\" eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString\"/><eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"pages\" eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EInt\"/></eClassifiers></ecore:EPackage>"));
    std::string xmi = buildDynamicXmi(res);

    XMIResource res2;
    res2.loadFromString(xmi);
    EXPECT_TRUE(!res2.getContents().empty());
    auto* loadedLib = res2.getContents().front();
    EXPECT_NOT_NULL(loadedLib);
    auto* loadedCls = loadedLib->eClass();
    EXPECT_EQ(loadedCls->getName(), std::string("Library"));
    auto nameV = loadedLib->eGet(loadedCls->getEStructuralFeature("name"));
    EXPECT_EQ(std::any_cast<std::string>(nameV), std::string("Test Library"));
    auto booksV = loadedLib->eGet(loadedCls->getEStructuralFeature("books"));
    auto* booksList = std::any_cast<emf::common::EList<EObject*>*>(booksV);
    EXPECT_EQ(booksList->size(), (size_t)2);
    auto* b0 = booksList->get(0);
    auto* bookCls = b0->eClass();
    EXPECT_EQ(std::any_cast<std::string>(b0->eGet(bookCls->getEStructuralFeature("title"))), std::string("Book One"));
    EXPECT_EQ(std::any_cast<int>(b0->eGet(bookCls->getEStructuralFeature("pages"))), 100);
}

// ===== 测试 3：静态(codegen)方式产出 XMI，与动态方式语义等价 =====
EMF_TEST(StaticDynamic_StaticXmi_EqualsDynamicXmi) {
    initEnv();
    std::string ecorePath = std::string(EMFCPP_SOURCE_DIR) + kLibraryEcoreRel;
    std::string outDir = std::string(EMF_CODEGEN_TEST_OUTPUT_DIR) + "/static_vs_dynamic/gen";
    std::string runDir = std::string(EMF_CODEGEN_TEST_OUTPUT_DIR) + "/static_vs_dynamic/run";
    std::filesystem::create_directories(outDir);
    std::filesystem::create_directories(runDir);

    // 1. codegen 生成静态类
    {
        GenConfig cfg;
        cfg.inputEcorePath = ecorePath;
        cfg.outputDirectory = outDir;
        cfg.baseNamespace = "emf";
        CppGenerator gen(cfg);
        gen.generateFromFile();
    }

    // 2. 动态方式产出 XMI
    std::string dynamicXmi;
    {
        XMIResource res;
        res.loadFromString(std::string("<?xml version=\"1.0\"?>\n<ecore:EPackage xmlns:ecore=\"http://www.eclipse.org/emf/2002/Ecore\" xmlns:xmi=\"http://www.omg.org/XMI\" xmi:version=\"2.0\" name=\"library\" nsURI=\"http://example.com/library/1.0\" nsPrefix=\"library\"><eClassifiers xsi:type=\"ecore:EClass\" name=\"Library\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"name\" eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString\"/><eStructuralFeatures xsi:type=\"ecore:EReference\" name=\"books\" upperBound=\"-1\" eType=\"#//Book\" containment=\"true\"/></eClassifiers><eClassifiers xsi:type=\"ecore:EClass\" name=\"Book\"><eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"title\" eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString\"/><eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"pages\" eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EInt\"/></eClassifiers></ecore:EPackage>"));
        dynamicXmi = buildDynamicXmi(res);
    }

    // 3. 写 driver：用静态 typed API 构造相同对象，saveToString 输出到 stdout
    std::string driverPath = outDir + "/library/driver.cpp";
    {
        std::ofstream f(driverPath);
        f << "#include \"Library.h\"\n"
          << "#include \"Book.h\"\n"
          << "#include \"Writer.h\"\n"
          << "#include \"LibraryPackage.h\"\n"
          << "#include \"LibraryFactory.h\"\n"
          << "#include \"emf/xmi/XMIResource.h\"\n"
          << "#include \"emf/xmi/XMIResourceFactory.h\"\n"
          << "#include \"emf/ecore/EcorePackage.h\"\n"
          << "#include <iostream>\n"
          << "int main() {\n"
          << "  emf::ecore::EcoreFactory::initialize();\n"
          << "  emf::ecore::EcorePackage::initialize();\n"
          << "  emf::xmi::XMIResourceFactory::registerDefaults();\n"
          << "  emf::library::LibraryPackage::initialize();\n"
          << "  auto* lib = dynamic_cast<emf::library::Library*>(\n"
          << "      emf::library::LibraryFactory::eINSTANCE->createLibrary());\n"
          << "  lib->setName(\"Test Library\");\n"
          << "  auto* b0 = dynamic_cast<emf::library::Book*>(\n"
          << "      emf::library::LibraryFactory::eINSTANCE->createBook());\n"
          << "  b0->setTitle(\"Book One\");\n"
          << "  b0->setPages(100);\n"
          << "  auto* b1 = dynamic_cast<emf::library::Book*>(\n"
          << "      emf::library::LibraryFactory::eINSTANCE->createBook());\n"
          << "  b1->setTitle(\"Book Two\");\n"
          << "  b1->setPages(200);\n"
          << "  lib->getBooks().add(b0);\n"
          << "  lib->getBooks().add(b1);\n"
          << "  emf::xmi::XMIResource res;\n"
          << "  res.addToContents(lib);\n"
          << "  std::cout << res.saveToString();\n"
          << "  return 0;\n"
          << "}\n";
    }

    // 4. 编译 driver
    // 注意：主构建用 -fsanitize=address 编译静态库，driver 必须带上同样的 flag，
    // 否则链接带 ASAN 的静态库时会出现 undefined reference to `__asan_report_*`。
    // EMF_TEST_CXX_FLAGS 由 CMake 从 CMAKE_CXX_FLAGS 注入，确保与主构建一致。
    std::string srcDir = std::string(EMFCPP_SOURCE_DIR);
    std::string buildDir = std::string(EMF_BUILD_DIR);
    std::string extraFlags = EMF_TEST_CXX_FLAGS;
    std::string cmd =
        "bash -c 'set -e; "
        "g++ -std=c++17 " + extraFlags + " "
        "-I \"" + srcDir + "/emf-common/include\" "
        "-I \"" + srcDir + "/emf-ecore/include\" "
        "-I \"" + srcDir + "/emf-xmi/include\" "
        "-I \"" + srcDir + "/emf-ecore-util/include\" "
        "-I \"" + srcDir + "/emf-validation/include\" "
        "-I \"" + outDir + "/library\" "
        + outDir + "/library/LibraryPackage.cpp "
        + outDir + "/library/LibraryFactory.cpp "
        + outDir + "/library/Library.cpp "
        + outDir + "/library/Book.cpp "
        + outDir + "/library/Writer.cpp "
        + outDir + "/library/LibraryValidator.cpp "
        + outDir + "/library/LibraryAdapterFactory.cpp "
        + outDir + "/library/driver.cpp "
        + buildDir + "/emf-xmi/libemf_xmi.a "
        + buildDir + "/emf-validation/libemf_validation.a "
        + buildDir + "/emf-ecore-util/libemf_ecore_util.a "
        + buildDir + "/emf-ecore/libemf_ecore.a "
        + buildDir + "/emf-common/libemf_common.a "
        + extraFlags + " "
        "-o " + runDir + "/static_driver 2>&1'";
    int rc = std::system(cmd.c_str());
    if (rc != 0) {
        throw std::runtime_error("static driver compilation failed (rc=" + std::to_string(rc) + ")");
    }

    // 5. 运行 driver 获取静态 XMI
    int runrc = std::system((runDir + "/static_driver > " + runDir + "/static_xmi.txt").c_str());
    if (runrc != 0) {
        throw std::runtime_error("static driver run failed (rc=" + std::to_string(runrc) + ")");
    }
    std::ifstream sout(runDir + "/static_xmi.txt");
    std::stringstream ss; ss << sout.rdbuf();
    std::string staticXmi = ss.str();

    // 6. 对比：规范化的 XML 应语义等价（关键字段一致）
    std::string normDyn = normalizeXml(dynamicXmi);
    std::string normSta = normalizeXml(staticXmi);

    // 关键字段断言（不要求字节级一致，因 xmi:id/命名空间声明顺序可能不同）
    EXPECT_TRUE(staticXmi.find("library:Library") != std::string::npos);
    EXPECT_TRUE(staticXmi.find("name=\"Test Library\"") != std::string::npos);
    EXPECT_TRUE(staticXmi.find("title=\"Book One\"") != std::string::npos);
    EXPECT_TRUE(staticXmi.find("pages=\"100\"") != std::string::npos);
    EXPECT_TRUE(staticXmi.find("title=\"Book Two\"") != std::string::npos);
    EXPECT_TRUE(staticXmi.find("pages=\"200\"") != std::string::npos);

    // 验证两种方式 books containment 子对象数量一致（都是 2 个 <library:Book>）
    auto countOccurrences = [](const std::string& s, const std::string& sub) {
        size_t count = 0, pos = 0;
        while ((pos = s.find(sub, pos)) != std::string::npos) { ++count; pos += sub.size(); }
        return count;
    };
    EXPECT_EQ(countOccurrences(staticXmi, "library:Book"), countOccurrences(dynamicXmi, "library:Book"));
}
