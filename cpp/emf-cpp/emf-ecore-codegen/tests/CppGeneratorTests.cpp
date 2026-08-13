// CppGeneratorTests.cpp —— 端到端测试
#include "test_main.h"
#include "test_helpers.h"
#include "emf/ecore/codegen/CppGenerator.h"
#include "emf/ecore/codegen/PackageEmitter.h"
#include "emf/ecore/codegen/SwitchEmitter.h"
#include "emf/ecore/EcorePackage.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/common/EPackage.h"
#include "emf/common/Resource.h"
#include "emf/xmi/XMIResource.h"
#include "emf/xmi/XMIResourceFactory.h"
#include "emf/common/URI.h"

#include <cstdlib>
#include <filesystem>
#include <fstream>
#include <sstream>
#include <string>

using namespace emf;
using namespace emf::ecore;
using namespace emf::ecore::codegen;

namespace {

std::string commonLib() { return std::string(EMF_BUILD_DIR) + "/emf-common/libemf_common.a"; }
std::string ecoreLib() { return std::string(EMF_BUILD_DIR) + "/emf-ecore/libemf_ecore.a"; }
std::string ecoreUtilLib() { return std::string(EMF_BUILD_DIR) + "/emf-ecore-util/libemf_ecore_util.a"; }
std::string xmiLib() { return std::string(EMF_BUILD_DIR) + "/emf-xmi/libemf_xmi.a"; }

}  // namespace

// ===== 1. 端到端：加载 .ecore → 生成 C++ 落盘 =====
EMF_TEST(CppGenerator_EndToEnd_GenerateFromSample) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    emf::xmi::XMIResourceFactory::registerDefaults();

    std::string ecorePath = std::string(EMFCPP_SOURCE_DIR) + "/emf-ecore-codegen/tests/samples/library.ecore";
    std::string outDir = makeTestDir("gen-out/library");

    GenConfig cfg;
    cfg.inputEcorePath = ecorePath;
    cfg.outputDirectory = outDir;
    cfg.baseNamespace = "emf";
    CppGenerator gen(cfg);
    gen.generateFromFile();

    // 落盘文件检查（单类单继承方案：每个 EClass 只有一个 .h/.cpp，无 Impl 后缀）
    std::vector<std::string> expected = {
        "library/LibraryPackage.h", "library/LibraryPackage.cpp",
        "library/LibraryFactory.h", "library/LibraryFactory.cpp",
        "library/Library.h", "library/Library.cpp",
        "library/Book.h", "library/Book.cpp",
        "library/Writer.h", "library/Writer.cpp",
        "library/LibrarySwitch.h", "library/LibrarySwitch.cpp",
        "library/LibraryAdapterFactory.h", "library/LibraryAdapterFactory.cpp",
        "library/LibraryValidator.h", "library/LibraryValidator.cpp",
    };
    for (auto& rel : expected) {
        if (!std::filesystem::exists(outDir + "/" + rel)) {
            throw std::runtime_error("missing generated file: " + rel);
        }
    }
}

// ===== 2. PackageEmitter 内容关键字 =====
EMF_TEST(CppGenerator_Package_ContentHasNsUri) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* pkg = EcoreFactory::instance().createEPackage();
    pkg->setName("my");
    pkg->setNsURI("http://x/y/1.0");
    pkg->setNsPrefix("my");
    PackageEmitter em(pkg, "emf");
    auto s = em.emitSource();
    EXPECT_TRUE(s.find("\"http://x/y/1.0\"") != std::string::npos);
    EXPECT_TRUE(s.find("MyPackage::initialize()") != std::string::npos);
    delete pkg;
}

// ===== 3. 端到端：编译 + 链接 + 跑生成代码 =====
EMF_TEST(CppGenerator_EndToEnd_CompileAndRunGeneratedCode) {
    std::string ecorePath = std::string(EMFCPP_SOURCE_DIR) + "/emf-ecore-codegen/tests/samples/library.ecore";
    std::string outDir = makeTestDir("gen-out/library");
    std::string runDir = makeTestDir("run-out");

    // 1) 生成代码
    {
        GenConfig cfg;
        cfg.inputEcorePath = ecorePath;
        cfg.outputDirectory = outDir;
        cfg.baseNamespace = "emf";
        CppGenerator gen(cfg);
        gen.generateFromFile();
    }

    // 2) 写 driver.cpp
    std::string driverPath = outDir + "/library/driver.cpp";
    {
        std::ofstream f(driverPath);
        f << "#include \"Library.h\"\n"
          << "#include \"Book.h\"\n"
          << "#include \"Writer.h\"\n"
          << "#include \"LibraryPackage.h\"\n"
          << "#include \"LibraryFactory.h\"\n"
          << "#include <iostream>\n"
          << "int main() {\n"
          << "  emf::library::LibraryPackage::initialize();\n"
          << "  auto* lib = dynamic_cast<emf::library::Library*>(\n"
          << "      emf::library::LibraryFactory::eINSTANCE->createLibrary());\n"
          << "  lib->setName(\"My Library\");\n"
          << "  auto* b1 = dynamic_cast<emf::library::Book*>(\n"
          << "      emf::library::LibraryFactory::eINSTANCE->createBook());\n"
          << "  b1->setTitle(\"C++ Templates\");\n"
          << "  b1->setPages(800);\n"
          << "  auto* b2 = dynamic_cast<emf::library::Book*>(\n"
          << "      emf::library::LibraryFactory::eINSTANCE->createBook());\n"
          << "  b2->setTitle(\"Modern C++ Design\");\n"
          << "  lib->getBooks().add(b1);\n"
          << "  lib->getBooks().add(b2);\n"
          << "  std::cout << \"lib=\" << lib->name() << \" books=\" << lib->getBooks().size()\n"
          << "            << \" first=\" << lib->getBooks().get(0)->title()\n"
          << "            << \" pages=\" << lib->getBooks().get(0)->pages() << std::endl;\n"
          << "  return 0;\n"
          << "}\n";
    }

    // 3) 编译
    std::string cmd =
        "bash -c 'set -e; "
        "INC1=\"" + std::string(EMFCPP_SOURCE_DIR) + "/emf-common/include\"; "
        "INC2=\"" + std::string(EMFCPP_SOURCE_DIR) + "/emf-ecore/include\"; "
        "INC3=\"" + std::string(EMFCPP_SOURCE_DIR) + "/emf-xmi/include\"; "
        "INC4=\"" + std::string(EMFCPP_SOURCE_DIR) + "/emf-ecore-util/include\"; "
        "INC5=\"" + std::string(EMFCPP_SOURCE_DIR) + "/emf-validation/include\"; "
        "g++ -std=c++17 "
        "-I \"$INC1\" -I \"$INC2\" -I \"$INC3\" -I \"$INC4\" -I \"$INC5\" "
        "-I \"" + outDir + "/library\" "
        + outDir + "/library/LibraryPackage.cpp "
        + outDir + "/library/LibraryFactory.cpp "
        + outDir + "/library/Library.cpp "
        + outDir + "/library/Book.cpp "
        + outDir + "/library/Writer.cpp "
        + outDir + "/library/LibraryValidator.cpp "
        + outDir + "/library/LibraryAdapterFactory.cpp "
        + outDir + "/library/driver.cpp "
        + xmiLib() + " " + ecoreUtilLib() + " " + ecoreLib() + " " + commonLib() + " "
        + std::string(EMF_BUILD_DIR) + "/emf-validation/libemf_validation.a "
        "-o " + runDir + "/library_driver 2>&1'";
    int rc = std::system(cmd.c_str());
    if (rc != 0) throw std::runtime_error("compilation failed (rc=" + std::to_string(rc) + ")");

    // 4) 运行
    int runrc = std::system((runDir + "/library_driver > " + runDir + "/stdout.txt").c_str());
    if (runrc != 0) throw std::runtime_error("driver run failed (rc=" + std::to_string(runrc) + ")");
    std::ifstream sout(runDir + "/stdout.txt");
    std::stringstream ss; ss << sout.rdbuf();
    std::string out = ss.str();
    EXPECT_TRUE(out.find("lib=My Library") != std::string::npos);
    EXPECT_TRUE(out.find("books=2") != std::string::npos);
    EXPECT_TRUE(out.find("first=C++ Templates") != std::string::npos);
    EXPECT_TRUE(out.find("pages=800") != std::string::npos);
    if (out.find("lib=My Library") == std::string::npos ||
        out.find("books=2") == std::string::npos ||
        out.find("first=C++ Templates") == std::string::npos ||
        out.find("pages=800") == std::string::npos) {
        throw std::runtime_error("driver output mismatch:\n" + out);
    }
}

// ===== 4. Switch 模板 =====
EMF_TEST(SwitchEmitter_Instantiate) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* pkg = EcoreFactory::instance().createEPackage();
    pkg->setName("my");
    pkg->setNsURI("http://x/y/1.0");
    pkg->setNsPrefix("my");
    auto* c = EcoreFactory::instance().createEClass();
    c->setName("X");
    pkg->addEClassifier(c);
    SwitchEmitter em(pkg, "emf");
    auto h = em.emitHeader();
    EXPECT_TRUE(h.find("class MySwitch") != std::string::npos);
    EXPECT_TRUE(h.find("caseX(X*") != std::string::npos);
}
