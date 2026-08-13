// emf-artop-codegen 主入口
// 用法：emf-artop-codegen <ecore-or-genmodel> <out-dir> [--version=X.Y.Z] [--release-id=...]
#include <cstdio>
#include <cstdlib>
#include <filesystem>
#include <iostream>
#include <memory>
#include <string>

#include "emf/ecore/codegen/CppGenerator.h"
#include "emf/ecore/codegen/GenModelLoader.h"
#include "emf/artop/codegen/ArtopCppGenerator.h"

#include "emf/ecore/EcorePackage.h"
#include "emf/xmi/XMIResource.h"
#include "emf/xmi/XMIResourceSet.h"
#include "emf/xmi/XMIResourceFactory.h"

namespace fs = std::filesystem;

namespace {

void printUsage(const char* prog) {
    std::fprintf(stderr,
                 "emf-artop-codegen v0.0.0\n"
                 "usage: %s <ecore> <out-dir> [options]\n"
                 "options:\n"
                 "  --version=X.Y.Z          AUTOSAR version (default 4.4.8)\n"
                 "  --release-id=ID          Release id (default org.artop.aal.autosar448)\n"
                 "  --namespace=URI          Base namespace (default http://autosar.org/schema/r4.0)\n"
                 "  --no-resource            Skip Resource/Factory generation\n"
                 "  --no-extensions          Skip root extensions injection\n",
                 prog);
}

bool endsWith(const std::string& s, const std::string& suf) {
    return s.size() >= suf.size() && s.compare(s.size() - suf.size(), suf.size(), suf) == 0;
}

}  // namespace

int main(int argc, char** argv) {
    if (argc < 3) {
        printUsage(argv[0]);
        return 1;
    }
    std::string inputPath = argv[1];
    std::string outDir = argv[2];

    emf::artop::codegen::ArtopGenConfig cfg;
    cfg.base.inputEcorePath = inputPath;
    cfg.base.outputDirectory = outDir;
    cfg.base.baseNamespace = "emf::artop";

    for (int i = 3; i < argc; ++i) {
        std::string a = argv[i];
        if (a.rfind("--version=", 0) == 0) cfg.version = a.substr(10);
        else if (a.rfind("--release-id=", 0) == 0) cfg.releaseId = a.substr(13);
        else if (a.rfind("--namespace=", 0) == 0) cfg.baseNamespaceUri = a.substr(12);
        else if (a == "--no-resource") cfg.generateResource = false;
        else if (a == "--no-extensions") cfg.injectRootExtensions = false;
        else {
            std::fprintf(stderr, "unknown option: %s\n", a.c_str());
            return 1;
        }
    }
    cfg.schemaLocation = cfg.baseNamespaceUri + " AUTOSAR_4-4-8.xsd";

    // 初始化 EMF 基础
    emf::ecore::EcoreFactory::initialize();
    emf::ecore::EcorePackage::initialize();
    emf::xmi::XMIResourceFactory::registerDefaults();

    // 文件存在性检查
    if (!fs::is_regular_file(inputPath)) {
        std::fprintf(stderr, "[emf-artop-codegen] no such file: %s\n", inputPath.c_str());
        return 1;
    }

    std::printf("[emf-artop-codegen] input : %s\n", inputPath.c_str());
    std::printf("[emf-artop-codegen] output: %s\n", outDir.c_str());
    std::printf("[emf-artop-codegen] ver   : %s\n", cfg.version.c_str());

    emf::artop::codegen::ArtopCppGenerator gen(cfg);
    if (endsWith(inputPath, ".genmodel")) {
        // 加载 .genmodel 然后调底层 generator
        auto gm = emf::ecore::codegen::GenModelLoader::load(inputPath);
        if (!gm) {
            std::fprintf(stderr, "[emf-artop-codegen] failed to load genmodel\n");
            return 1;
        }
        // 取每个 genPackage 的 ecorePackage 生成
        for (auto& gp : gm->genPackages) {
            if (gp && gp->ecorePackage) {
                gen.generateFromPackage(gp->ecorePackage.get());
            }
        }
    } else {
        // .ecore 文件路径
        gen.generateFromFile();
    }

    std::printf("[emf-artop-codegen] done.\n");
    return 0;
}
