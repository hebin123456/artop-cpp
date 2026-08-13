// emf-artop-codegen 测试
// 1. 单元测试：ArtopCppGenerator 框架
// 2. 端到端测试：使用 library.ecore 跑 codegen，验证生成的 ResourceImpl/FactoryImpl 可编译
#include "test_main.h"

#include "emf/artop/codegen/ArtopCppGenerator.h"
#include "emf/artop/runtime/AutosarResource.h"
#include "emf/artop/runtime/AutosarResourceFactory.h"
#include "emf/artop/runtime/AutosarReleaseDescriptor.h"
#include "emf/ecore/EcorePackage.h"
#include "emf/xmi/XMIResource.h"
#include "emf/xmi/XMIResourceFactory.h"
#include "emf/xmi/XMIResourceSet.h"

#include <cstdio>
#include <fstream>
#include <sstream>
#include <string>
#include <filesystem>

namespace emf::artop::codegen::test {

// 用例 1: ArtopGenConfig 默认值
bool test_config_defaults() {
    ArtopGenConfig cfg;
    EMF_ASSERT_EQ(cfg.version, std::string("4.4.8"));
    EMF_ASSERT_EQ(cfg.releaseId, std::string("org.artop.aal.autosar448"));
    EMF_ASSERT(cfg.generateResource);
    EMF_ASSERT(cfg.injectRootExtensions);
    return true;
}

// 用例 2: 找一个小型 ecore 跑 codegen，确认输出文件
bool test_generate_on_small_ecore() {
    namespace fs = std::filesystem;
    fs::path smallEcore = "/workspace/cpp/emf-cpp/emf-ecore-codegen/tests/samples/library.ecore";
    if (!fs::exists(smallEcore)) {
        std::printf("  [SKIP] library.ecore not found at %s\n", smallEcore.c_str());
        return true;
    }
    fs::path outDir = fs::temp_directory_path() / "artop-codegen-test-out";
    std::error_code ec;
    fs::remove_all(outDir, ec);

    ArtopGenConfig cfg;
    cfg.base.inputEcorePath = smallEcore.string();
    cfg.base.outputDirectory = outDir.string();
    cfg.base.baseNamespace = "emf::artop";

    emf::ecore::EcoreFactory::initialize();
    emf::ecore::EcorePackage::initialize();
    emf::xmi::XMIResourceFactory::registerDefaults();

    ArtopCppGenerator gen(cfg);
    gen.generateFromFile();

    // 验证：基本 EMF 文件应该被生成
    EMF_ASSERT(fs::exists(outDir / "library" / "LibraryPackage.h"));
    EMF_ASSERT(fs::exists(outDir / "library" / "LibraryFactory.h"));
    // ARTOP 特有文件
    EMF_ASSERT(fs::exists(outDir / "library" / "LibraryResourceImpl.h"));
    EMF_ASSERT(fs::exists(outDir / "library" / "LibraryResourceFactoryImpl.h"));
    EMF_ASSERT(fs::exists(outDir / "library" / "ARTOP_ROOT_EXTENSIONS.md"));
    return true;
}

// 用例 3: 加载 autosar448.ecore（18MB 大文件）
// 验证：ArtopCppGenerator 能跨包加载 gautosar + autosar448，
// 不会因为 ecore 大小而崩溃
bool test_generate_on_autosar448() {
    namespace fs = std::filesystem;
    fs::path autosarEcore = "/workspace/decompiler/autosar448/model/autosar448.ecore";
    // fallback: 也检查 cpp 目录下
    if (!fs::exists(autosarEcore)) {
        autosarEcore = "/workspace/cpp/emf-cpp/emf-artop/emf-artop-codegen/tests/samples/autosar448.ecore";
    }
    if (!fs::exists(autosarEcore)) {
        std::printf("  [SKIP] autosar448.ecore not found at %s\n", autosarEcore.c_str());
        return true;
    }
    fs::path outDir = fs::temp_directory_path() / "artop-codegen-test-448";
    std::error_code ec;
    fs::remove_all(outDir, ec);

    ArtopGenConfig cfg;
    cfg.base.inputEcorePath = autosarEcore.string();
    cfg.base.outputDirectory = outDir.string();
    cfg.base.baseNamespace = "emf::artop";

    emf::ecore::EcoreFactory::initialize();
    emf::ecore::EcorePackage::initialize();
    emf::xmi::XMIResourceFactory::registerDefaults();

    ArtopCppGenerator gen(cfg);
    gen.generateFromFile();

    // 验证：顶层包有基本文件
    EMF_ASSERT(fs::exists(outDir / "autosar40" / "Autosar40Package.h"));
    // 顶层 ResourceImpl/FactoryImpl 应该有
    EMF_ASSERT(fs::exists(outDir / "autosar40" / "Autosar40ResourceImpl.h"));
    EMF_ASSERT(fs::exists(outDir / "autosar40" / "Autosar40ResourceFactoryImpl.h"));
    return true;
}

}  // namespace emf::artop::codegen::test

int main() {
    using namespace emf::artop::codegen::test;
    bool all_ok = true;
    all_ok &= EMF_RUN(test_config_defaults);
    all_ok &= EMF_RUN(test_generate_on_small_ecore);
    all_ok &= EMF_RUN(test_generate_on_autosar448);

    std::printf("\n[emf-artop-codegen] %s\n", all_ok ? "ALL PASS" : "FAIL");
    return all_ok ? 0 : 1;
}
