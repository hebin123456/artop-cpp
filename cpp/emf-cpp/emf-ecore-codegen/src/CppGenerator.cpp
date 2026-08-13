// CppGenerator.cpp —— 顶层调度器（直接从 EPackage 生成）
// 对齐 Java: org.eclipse.emf.codegen.ecore.Generator（无 .genmodel 的简化路径）
//
// 流程：
//   1. generateFromFile(): 用 emf::xmi 加载 .ecore → EPackage
//   2. generateFromPackage(pkg, parentPath): 递归每个 EPackage，
//      用 PackageEmitter/FactoryEmitter/EClassEmitter/SwitchEmitter/
//      AdapterFactoryEmitter/ValidatorEmitter 生成文件，落盘到
//      outputDirectory/<parentPath/pkgName>/。
#include "emf/ecore/codegen/CppGenerator.h"
#include "emf/ecore/codegen/PackageEmitter.h"
#include "emf/ecore/codegen/FactoryEmitter.h"
#include "emf/ecore/codegen/EClassEmitter.h"
#include "emf/ecore/codegen/SwitchEmitter.h"
#include "emf/ecore/codegen/AdapterFactoryEmitter.h"
#include "emf/ecore/codegen/ValidatorEmitter.h"
#include "emf/ecore/EcorePackage.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/common/URI.h"
#include "emf/xmi/XMIResource.h"
#include "emf/xmi/XMIResourceFactory.h"

#include <filesystem>
#include <fstream>
#include <functional>
#include <iostream>
#include <stdexcept>
#include <system_error>

namespace emf::ecore::codegen {

CppGenerator::CppGenerator(GenConfig config) : config_(std::move(config)) {}
CppGenerator::~CppGenerator() = default;

void CppGenerator::generateFromFile() {
    emf::ecore::EcoreFactory::initialize();
    emf::ecore::EcorePackage::initialize();
    emf::xmi::XMIResourceFactory::registerDefaults();

    auto uri = emf::common::URI::createFileURI(config_.inputEcorePath);
    auto res = emf::xmi::XMIResourceFactory::createResourceFor(uri);
    if (!res) {
        throw std::runtime_error("CppGenerator: cannot create resource for " + config_.inputEcorePath);
    }
    std::ifstream ifs(config_.inputEcorePath);
    if (!ifs.is_open()) {
        throw std::runtime_error("CppGenerator: cannot open " + config_.inputEcorePath);
    }
    res->load(ifs);
    if (res->getContents().empty()) {
        throw std::runtime_error("CppGenerator: empty resource " + config_.inputEcorePath);
    }
    package_ = dynamic_cast<emf::ecore::EPackage*>(res->getContents()[0]);
    if (!package_) {
        throw std::runtime_error("CppGenerator: root is not EPackage in " + config_.inputEcorePath);
    }
    generateFromPackage(package_, std::string{});
}

void CppGenerator::generateFromPackage(emf::ecore::EPackage* package) {
    generateFromPackage(package, std::string{});
}

void CppGenerator::generateFromPackage(emf::ecore::EPackage* package, const std::string& parentPath) {
    if (!package) return;
    std::string pkgName = package->getName();
    if (pkgName.empty()) pkgName = "unnamed";
    // 子目录 = parentPath/pkgName（同时用作 namespace 父路径，"/" 分隔）
    std::string subdir = parentPath.empty() ? pkgName : (parentPath + "/" + pkgName);
    ensureOutputDir(subdir);

    auto write = [&](const std::string& fileName, const std::string& content) {
        writeFile(subdir + "/" + fileName, content);
    };

    // Package
    {
        PackageEmitter em(package, config_.baseNamespace, parentPath);
        write(em.className() + ".h", em.emitHeader());
        write(em.className() + ".cpp", em.emitSource());
    }
    // Factory
    {
        FactoryEmitter em(package, config_.baseNamespace, parentPath);
        write(em.className() + ".h", em.emitHeader());
        write(em.className() + ".cpp", em.emitSource());
    }
    // 每个 EClass
    for (auto* c : package->getEClassifiers()) {
        auto* cls = dynamic_cast<emf::ecore::EClass*>(c);
        if (!cls) continue;
        EClassEmitter em(cls, config_.baseNamespace, parentPath);
        write(em.headerName(), em.emitHeader());
        write(em.sourceName(), em.emitSource());
    }
    // Switch
    if (config_.generateSwitch) {
        SwitchEmitter em(package, config_.baseNamespace, parentPath);
        write(em.className() + ".h", em.emitHeader());
        write(em.className() + ".cpp", em.emitSource());
    }
    // AdapterFactory
    if (config_.generateAdapterFactory) {
        AdapterFactoryEmitter em(package, config_.baseNamespace, parentPath);
        write(em.className() + ".h", em.emitHeader());
        write(em.className() + ".cpp", em.emitSource());
    }
    // Validator
    if (config_.generateValidator) {
        ValidatorEmitter em(package, config_.baseNamespace, parentPath);
        write(em.className() + ".h", em.emitHeader());
        write(em.className() + ".cpp", em.emitSource());
    }
    // 递归子包
    for (auto* sp : package->getESubPackages()) {
        generateFromPackage(sp, subdir);
    }
}

void CppGenerator::ensureOutputDir(const std::string& subdir) const {
    std::filesystem::path p = std::filesystem::path(config_.outputDirectory) / subdir;
    std::error_code ec;
    std::filesystem::create_directories(p, ec);
    // 静默忽略 ec（目录已存在等）
}

void CppGenerator::writeFile(const std::string& relativePath, const std::string& content) const {
    std::filesystem::path p = std::filesystem::path(config_.outputDirectory) / relativePath;
    std::ofstream f(p);
    if (!f.is_open()) {
        throw std::runtime_error("CppGenerator: cannot write " + p.string());
    }
    f << content;
}

}  // namespace emf::ecore::codegen
