// GenModelGenerator.cpp —— GenModel 路线的顶层调度器
// 对齐 Java: org.eclipse.emf.codegen.ecore.Generator.generate()
//
// 设计：复用旧 emitter（PackageEmitter/FactoryEmitter/EClassEmitter/...）作为
// "模板子例程"，保证 GenModel 路线与 CppGenerator 直接路线输出一致。
//   1. generate(genModel): 遍历每个 GenPackage → generateForPackage
//   2. generateFromFile(path): GenModelLoader::load → generate
//   3. generateFromEcore(pkg): GenModelLoader::wrapEcore → generate
#include "emf/ecore/codegen/GenModelGenerator.h"
#include "emf/ecore/codegen/GenModelLoader.h"
#include "emf/ecore/codegen/PackageEmitter.h"
#include "emf/ecore/codegen/FactoryEmitter.h"
#include "emf/ecore/codegen/EClassEmitter.h"
#include "emf/ecore/codegen/SwitchEmitter.h"
#include "emf/ecore/codegen/AdapterFactoryEmitter.h"
#include "emf/ecore/codegen/ValidatorEmitter.h"
#include "emf/ecore/EcorePackage.h"

#include <filesystem>
#include <fstream>
#include <stdexcept>
#include <system_error>

namespace emf::ecore::codegen {

GenModelGenerator::GenModelGenerator(Options opt) : options_(std::move(opt)) {}
GenModelGenerator::~GenModelGenerator() = default;

int GenModelGenerator::generate(std::shared_ptr<GenModel> genModel) {
    if (!genModel) return -1;
    emf::ecore::EcoreFactory::initialize();
    emf::ecore::EcorePackage::initialize();
    int count = 0;
    for (auto& gp : genModel->genPackages) {
        if (!gp || !gp->ecorePackage) continue;
        count += generateForPackage(gp.get(), genModel);
    }
    return count;
}

int GenModelGenerator::generateFromFile(const std::string& genModelPath) {
    auto gm = GenModelLoader::load(genModelPath);
    if (!gm) return -1;
    return generate(gm);
}

int GenModelGenerator::generateFromEcore(emf::ecore::EPackage* ecorePkg) {
    if (!ecorePkg) return -1;
    auto gm = GenModelLoader::wrapEcore(ecorePkg, options_.baseNamespace);
    if (!gm) return -1;
    return generate(gm);
}

// GenPackage.basePackage（Java 风格 "." 分隔）→ C++ 命名空间（"::" 分隔）
static std::string basePackageToNs(const std::string& bp) {
    std::string r;
    for (char c : bp) {
        if (c == '.') r += "::";
        else r += c;
    }
    return r;
}

int GenModelGenerator::generateForPackage(GenPackage* gp, std::shared_ptr<GenModel> /*gm*/) {
    auto* pkg = gp->ecorePackage.get();
    if (!pkg) return 0;
    std::string pkgName = pkg->getName();
    if (pkgName.empty()) pkgName = "unnamed";

    // base namespace：优先 GenPackage.basePackage，否则 options_.baseNamespace
    std::string baseNs = options_.baseNamespace;
    if (!gp->basePackage.empty()) {
        baseNs = basePackageToNs(gp->basePackage);
    }

    std::string subdir = pkgName;
    mkdirsFor(subdir);
    int count = 0;
    auto write = [&](const std::string& fileName, const std::string& content) {
        writeFile(subdir + "/" + fileName, content);
        ++count;
    };

    // Package
    {
        PackageEmitter em(pkg, baseNs);
        write(em.className() + ".h", em.emitHeader());
        write(em.className() + ".cpp", em.emitSource());
    }
    // Factory
    {
        FactoryEmitter em(pkg, baseNs);
        write(em.className() + ".h", em.emitHeader());
        write(em.className() + ".cpp", em.emitSource());
    }
    // 每个 EClass
    if (options_.generateInterfaces) {
        for (auto* c : pkg->getEClassifiers()) {
            auto* cls = dynamic_cast<emf::ecore::EClass*>(c);
            if (!cls) continue;
            EClassEmitter em(cls, baseNs);
            write(em.headerName(), em.emitHeader());
            write(em.sourceName(), em.emitSource());
        }
    }
    // Switch
    if (options_.generateSwitch) {
        SwitchEmitter em(pkg, baseNs);
        write(em.className() + ".h", em.emitHeader());
        write(em.className() + ".cpp", em.emitSource());
    }
    // AdapterFactory
    if (options_.generateAdapterFactory) {
        AdapterFactoryEmitter em(pkg, baseNs);
        write(em.className() + ".h", em.emitHeader());
        write(em.className() + ".cpp", em.emitSource());
    }
    // Validator
    if (options_.generateValidator) {
        ValidatorEmitter em(pkg, baseNs);
        write(em.className() + ".h", em.emitHeader());
        write(em.className() + ".cpp", em.emitSource());
    }
    // 递归嵌套 GenPackage
    for (auto& ngp : gp->nestedGenPackages) {
        if (ngp) count += generateForPackage(ngp.get(), nullptr);
    }
    return count;
}

void GenModelGenerator::mkdirsFor(const std::string& fullPath) {
    std::filesystem::path p = std::filesystem::path(options_.outputDirectory) / fullPath;
    std::error_code ec;
    std::filesystem::create_directories(p, ec);
}

void GenModelGenerator::writeFile(const std::string& relativePath, const std::string& content) {
    std::filesystem::path p = std::filesystem::path(options_.outputDirectory) / relativePath;
    std::ofstream f(p);
    if (!f.is_open()) {
        throw std::runtime_error("GenModelGenerator: cannot write " + p.string());
    }
    f << content;
}

}  // namespace emf::ecore::codegen
