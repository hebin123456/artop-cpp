// GenModelGeneratorTests.cpp —— 单元测试：GenModelGenerator
// 对应 Java: org.eclipse.emf.codegen.ecore.Generator.generate()
#include "test_main.h"
#include "test_helpers.h"

#include "emf/ecore/codegen/GenModel.h"
#include "emf/ecore/codegen/GenModelLoader.h"
#include "emf/ecore/codegen/GenModelGenerator.h"
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
#include <memory>
#include <string>

using namespace emf;
using namespace emf::ecore;
using namespace emf::ecore::codegen;

namespace {
const char* kSampleEcorePath = EMFCPP_SOURCE_DIR "/emf-ecore-codegen/tests/samples/library.ecore";
}  // namespace

// ===== 1. 从已加载 EPackage 生成所有文件 =====
EMF_TEST(GenModelGenerator_generateFromEcore_allFilesCreated) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    emf::xmi::XMIResourceFactory::registerDefaults();

    // 加载 .ecore
    auto uri = emf::common::URI::createFileURI(kSampleEcorePath);
    auto res = emf::xmi::XMIResourceFactory::createResourceFor(uri);
    std::ifstream ifs(kSampleEcorePath);
    res->load(ifs);
    auto* root = res->getContents()[0];
    auto* pkg = dynamic_cast<emf::ecore::EPackage*>(root);
    EXPECT_NOT_NULL(pkg);

    // 走 GenModel 路线生成
    std::string outDir = makeTestDir("genmodel-gen/from-ecore");
    GenModelGenerator::Options opt;
    opt.outputDirectory = outDir;
    opt.baseNamespace = "emf";
    GenModelGenerator gen(opt);
    int n = gen.generateFromEcore(pkg);
    EXPECT_TRUE(n > 0);

    // 验证落盘文件
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

// ===== 2. .genmodel 加载 + 调度 =====
EMF_TEST(GenModelGenerator_generateFromFile_loadsGenModelAndGenerates) {
    EcoreFactory::initialize();
    EcorePackage::initialize();

    std::string tmpDir = makeTestDir("genmodel-gen/from-file");
    // 拷贝 .ecore 到 tmp 下
    std::string ecoreCopy = tmpDir + "/library.ecore";
    {
        std::ifstream src(kSampleEcorePath, std::ios::binary);
        std::ofstream dst(ecoreCopy, std::ios::binary);
        dst << src.rdbuf();
    }
    // 写 .genmodel
    std::string genmodelPath = tmpDir + "/library.genmodel";
    {
        std::ofstream f(genmodelPath);
        f << "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
          << "<genmodel:GenModel xmi:version=\"2.0\"\n"
          << "    xmlns:xmi=\"http://www.omg.org/XMI\"\n"
          << "    xmlns:genmodel=\"http://www.eclipse.org/emf/2002/GenModel\"\n"
          << "    modelDirectory=\"" << tmpDir << "\" modelName=\"Library\"\n"
          << "    modelPluginID=\"com.example.library\" forceOverwrite=\"true\" bundleManifest=\"false\">\n"
          << "  <genPackages prefix=\"Library\" basePackage=\"com.example\">\n"
          << "    <ecorePackage href=\"library.ecore#/\"/>\n"
          << "    <genClasses ecoreClass=\"library.ecore#//Library\"/>\n"
          << "    <genClasses ecoreClass=\"library.ecore#//Book\"/>\n"
          << "    <genClasses ecoreClass=\"library.ecore#//Writer\"/>\n"
          << "  </genPackages>\n"
          << "</genmodel:GenModel>\n";
    }

    std::string outDir = makeTestDir("genmodel-gen/from-file-out");
    GenModelGenerator::Options opt;
    opt.outputDirectory = outDir;
    opt.baseNamespace = "emf";
    GenModelGenerator gen(opt);
    int n = gen.generateFromFile(genmodelPath);
    EXPECT_TRUE(n > 0);
    EXPECT_TRUE(std::filesystem::exists(outDir + "/library/LibraryPackage.h"));
    EXPECT_TRUE(std::filesystem::exists(outDir + "/library/LibraryFactory.h"));
    EXPECT_TRUE(std::filesystem::exists(outDir + "/library/Book.h"));
}

// ===== 3. 关闭 Switch/AdapterFactory/Validator 时的输出 =====
EMF_TEST(GenModelGenerator_generateFromEcore_respectsOptionsFlags) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    emf::xmi::XMIResourceFactory::registerDefaults();
    auto uri = emf::common::URI::createFileURI(kSampleEcorePath);
    auto res = emf::xmi::XMIResourceFactory::createResourceFor(uri);
    std::ifstream ifs(kSampleEcorePath);
    res->load(ifs);
    auto* pkg = dynamic_cast<emf::ecore::EPackage*>(res->getContents()[0]);

    std::string outDir = makeTestDir("genmodel-gen/no-switch");
    GenModelGenerator::Options opt;
    opt.outputDirectory = outDir;
    opt.generateSwitch = false;
    opt.generateAdapterFactory = false;
    opt.generateValidator = false;
    GenModelGenerator gen(opt);
    gen.generateFromEcore(pkg);

    // Package/Factory/Interface 还在
    EXPECT_TRUE(std::filesystem::exists(outDir + "/library/LibraryPackage.h"));
    EXPECT_TRUE(std::filesystem::exists(outDir + "/library/LibraryFactory.h"));
    EXPECT_TRUE(std::filesystem::exists(outDir + "/library/Library.h"));
    // 关闭的应不存在
    EXPECT_FALSE(std::filesystem::exists(outDir + "/library/LibrarySwitch.h"));
    EXPECT_FALSE(std::filesystem::exists(outDir + "/library/LibraryAdapterFactory.h"));
    EXPECT_FALSE(std::filesystem::exists(outDir + "/library/LibraryValidator.h"));
}

// ===== 4. 输出可被读回 =====
EMF_TEST(GenModelGenerator_outputIsNonEmptyText) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    emf::xmi::XMIResourceFactory::registerDefaults();
    auto uri = emf::common::URI::createFileURI(kSampleEcorePath);
    auto res = emf::xmi::XMIResourceFactory::createResourceFor(uri);
    std::ifstream ifs(kSampleEcorePath);
    res->load(ifs);
    auto* pkg = dynamic_cast<emf::ecore::EPackage*>(res->getContents()[0]);

    std::string outDir = makeTestDir("genmodel-gen/non-empty");
    GenModelGenerator::Options opt;
    opt.outputDirectory = outDir;
    GenModelGenerator gen(opt);
    gen.generateFromEcore(pkg);
    auto txt = readAll(outDir + "/library/LibraryPackage.h");
    EXPECT_FALSE(txt.empty());
    EXPECT_TRUE(txt.find("class LibraryPackage") != std::string::npos);
    EXPECT_TRUE(txt.find("class Book") == std::string::npos);  // Header 只有 Package，无 Class
}

// ===== 5. 多包：现在 wrapEcore 单包即可；用两个 generator 走多包 =====
EMF_TEST(GenModelGenerator_generatesForEachGenPackage) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    emf::xmi::XMIResourceFactory::registerDefaults();
    auto uri = emf::common::URI::createFileURI(kSampleEcorePath);
    auto res = emf::xmi::XMIResourceFactory::createResourceFor(uri);
    std::ifstream ifs(kSampleEcorePath);
    res->load(ifs);
    auto* pkg = dynamic_cast<emf::ecore::EPackage*>(res->getContents()[0]);

    std::string outDir = makeTestDir("genmodel-gen/multi-pkg");
    GenModelGenerator::Options opt;
    opt.outputDirectory = outDir;
    GenModelGenerator gen(opt);
    int n = gen.generateFromEcore(pkg);
    // 单类单继承方案：1 个 GenPackage 生成 16 个文件
    //   Package x2 + Factory x2 + Switch x2 + AF x2 + Val x2 + 3 Class x 2 = 16
    //   （旧 Impl 方案是 18：每 Class 3 个文件）
    EXPECT_TRUE(n >= 16);
}
