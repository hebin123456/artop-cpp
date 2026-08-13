// probe_autosar448.cpp —— 探查 autosar448.ecore 的根 EPackage 结构
#include <cstdio>
#include <fstream>
#include "emf/ecore/EcorePackage.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/common/Resource.h"
#include "emf/common/URI.h"
#include "emf/xmi/XMIResource.h"
#include "emf/xmi/XMIResourceSet.h"
#include "emf/xmi/XMIResourceFactory.h"
#include <filesystem>

int main(int argc, char** argv) {
    std::string path = (argc > 1) ? argv[1]
        : "/workspace/decompiler/autosar448/model/autosar448.ecore";
    emf::ecore::EcoreFactory::initialize();
    emf::ecore::EcorePackage::initialize();
    emf::xmi::XMIResourceFactory::registerDefaults();

    emf::xmi::XMIResourceSet rs;
    namespace fs = std::filesystem;
    fs::path inputPath(path);
    if (fs::is_regular_file(inputPath)) {
        fs::path dir = inputPath.parent_path();
        for (auto& e : fs::directory_iterator(dir)) {
            if (!e.is_regular_file() || e.path().extension() != ".ecore") continue;
            if (e.path().filename() == inputPath.filename()) continue;
            auto* sib = dynamic_cast<emf::xmi::XMIResource*>(
                rs.createResource(emf::common::URI::createFileURI(e.path().string())));
            if (sib) { std::ifstream sfs(e.path().string()); if (sfs.is_open()) sib->load(sfs); }
        }
    }
    auto* res = dynamic_cast<emf::xmi::XMIResource*>(
        rs.createResource(emf::common::URI::createFileURI(path)));
    std::ifstream ifs(path);
    res->load(ifs);

    std::printf("contents=%zu\n", res->getContents().size());
    for (size_t i = 0; i < res->getContents().size(); ++i) {
        auto* root = res->getContents()[i];
        auto* pkg = dynamic_cast<emf::ecore::EPackage*>(root);
        if (!pkg) { std::printf("[%zu] not EPackage, type=%s\n", i, typeid(*root).name()); continue; }
        std::printf("[%zu] EPackage name=%s nsURI=%s classifiers=%zu subpackages=%zu\n",
            i, pkg->getName().c_str(), pkg->getNsURI().c_str(),
            pkg->getEClassifiers().size(), pkg->getESubpackages().size());

        // 递归打印前 5 层 subpackage
        std::function<void(emf::ecore::EPackage*, int)> walk = [&](emf::ecore::EPackage* p, int depth) {
            if (depth > 5) return;
            for (auto* sp : p->getESubpackages()) {
                if (!sp) continue;
                std::printf("%*s- subpkg name=%s nsURI=%s classifiers=%zu subpackages=%zu\n",
                    depth*2, "", sp->getName().c_str(), sp->getNsURI().c_str(),
                    sp->getEClassifiers().size(), sp->getESubpackages().size());
                walk(sp, depth + 1);
            }
        };
        walk(pkg, 1);

        // 统计所有 EPackage 总数（递归）
        std::function<int(emf::ecore::EPackage*)> count = [&](emf::ecore::EPackage* p) -> int {
            int n = 1;
            for (auto* sp : p->getESubpackages()) if (sp) n += count(sp);
            return n;
        };
        std::printf("TOTAL packages (recursive) = %d\n", count(pkg));

        // 统计所有 EClass 总数（递归）
        std::function<int(emf::ecore::EPackage*)> countCls = [&](emf::ecore::EPackage* p) -> int {
            int n = (int)p->getEClassifiers().size();
            for (auto* sp : p->getESubpackages()) if (sp) n += countCls(sp);
            return n;
        };
        std::printf("TOTAL classifiers (recursive) = %d\n", countCls(pkg));
    }
    return 0;
}
