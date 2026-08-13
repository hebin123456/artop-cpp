// 检查 ecore 文件加载后 EClass 是否有 eAnnotation
#include "emf/ecore/EcorePackage.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/common/Resource.h"
#include "emf/common/URI.h"
#include "emf/xmi/XMIResource.h"
#include "emf/xmi/XMIResourceSet.h"
#include "emf/xmi/XMIResourceFactory.h"

#include <cstdio>
#include <fstream>
#include <filesystem>

int main(int argc, char** argv) {
    std::string path = (argc > 1) ? argv[1]
        : "/workspace/decompiler/autosar448/model/dummy.ecore";

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
    if (res->getContents().empty()) return 1;

    auto* root = res->getContents()[0];
    auto* rootPkg = dynamic_cast<emf::ecore::EPackage*>(root);
    if (!rootPkg) { std::printf("root not EPackage\n"); return 1; }

    // 检查根包的 eAnnotation
    std::printf("\n===== 根包 eAnnotations =====\n");
    auto anns = rootPkg->getEAnnotations();
    std::printf("根包 eAnnotations 数量: %zu\n", anns.size());
    for (auto* ann : anns) {
        if (!ann) continue;
        std::printf("  source: %s\n", ann->getSource().c_str());
        for (auto& [k, v] : ann->getDetailsMap()) {
            std::printf("    %s = %s\n", k.c_str(), v.c_str());
        }
    }

    // 检查根包的 EClass 的 eAnnotation
    std::printf("\n===== 根包 EClassifiers =====\n");
    for (auto* c : rootPkg->getEClassifiers()) {
        if (auto* cls = dynamic_cast<emf::ecore::EClass*>(c)) {
            auto cans = cls->getEAnnotations();
            std::printf("EClass %s: eAnnotations=%zu\n", cls->getName().c_str(), cans.size());
            for (auto* ann : cans) {
                if (!ann) continue;
                std::printf("  source: %s, details=%zu\n", ann->getSource().c_str(), ann->getDetailsMap().size());
            }
        }
    }

    // 递归查找 ARPackage
    std::function<emf::ecore::EClass*(emf::ecore::EPackage*, const std::string&)> findClass =
        [&](emf::ecore::EPackage* pkg, const std::string& name) -> emf::ecore::EClass* {
        for (auto* c : pkg->getEClassifiers()) {
            if (auto* cls = dynamic_cast<emf::ecore::EClass*>(c)) {
                if (cls->getName() == name) return cls;
            }
        }
        for (auto* sp : pkg->getESubpackages()) {
            if (auto* found = findClass(sp, name)) return found;
        }
        return nullptr;
    };

    auto* arpkg = findClass(rootPkg, "ARPackage");
    if (arpkg) {
        std::printf("\n===== ARPackage eAnnotations =====\n");
        auto aans = arpkg->getEAnnotations();
        std::printf("ARPackage eAnnotations 数量: %zu\n", aans.size());
        for (auto* ann : aans) {
            if (!ann) continue;
            std::printf("  source: %s\n", ann->getSource().c_str());
            for (auto& [k, v] : ann->getDetailsMap()) {
                std::printf("    %s = %s\n", k.c_str(), v.c_str());
            }
        }

        // 检查 ARPackage 的 features 的 eAnnotation
        std::printf("\n===== ARPackage features eAnnotations =====\n");
        for (auto* sf : arpkg->getEStructuralFeatures()) {
            auto sfans = sf->getEAnnotations();
            std::printf("feature %s: eAnnotations=%zu\n", sf->getName().c_str(), sfans.size());
            for (auto* ann : sfans) {
                if (!ann) continue;
                std::printf("  source: %s\n", ann->getSource().c_str());
                for (auto& [k, v] : ann->getDetailsMap()) {
                    std::printf("    %s = %s\n", k.c_str(), v.c_str());
                }
            }
        }
    } else {
        std::printf("ARPackage not found\n");
    }

    return 0;
}
