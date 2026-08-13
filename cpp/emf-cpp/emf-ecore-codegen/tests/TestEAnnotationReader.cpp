// 测试 EAnnotationReader：加载 dummy.ecore，读取 ARPackage 的 eAnnotation 信息
// 验证模型驱动：所有建模信息都从 ecore 的 eAnnotation 读取
#include "emf/ecore/EcorePackage.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/common/Resource.h"
#include "emf/common/URI.h"
#include "emf/xmi/XMIResource.h"
#include "emf/xmi/XMIResourceSet.h"
#include "emf/xmi/XMIResourceFactory.h"
#include "emf/ecore/codegen/EAnnotationReader.h"

#include <cstdio>
#include <fstream>
#include <filesystem>
#include <functional>

using namespace emf::ecore::codegen;

// 递归查找指定名称的 EClass
emf::ecore::EClass* findClass(emf::ecore::EPackage* pkg, const std::string& name) {
    for (auto* c : pkg->getEClassifiers()) {
        if (auto* cls = dynamic_cast<emf::ecore::EClass*>(c)) {
            if (cls->getName() == name) return cls;
        }
    }
    for (auto* sp : pkg->getESubpackages()) {
        if (auto* found = findClass(sp, name)) return found;
    }
    return nullptr;
}

// 递归查找指定名称的 EPackage
emf::ecore::EPackage* findPackage(emf::ecore::EPackage* pkg, const std::string& name) {
    if (pkg->getName() == name) return pkg;
    for (auto* sp : pkg->getESubpackages()) {
        if (auto* found = findPackage(sp, name)) return found;
    }
    return nullptr;
}

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

    std::printf("\n========== EAnnotationReader 测试 ==========\n");
    std::printf("文件: %s\n", path.c_str());
    std::printf("contents=%zu\n\n", res->getContents().size());

    if (res->getContents().empty()) {
        std::printf("ERROR: no contents\n");
        return 1;
    }

    auto* root = res->getContents()[0];
    auto* rootPkg = dynamic_cast<emf::ecore::EPackage*>(root);
    if (!rootPkg) {
        std::printf("ERROR: root is not EPackage\n");
        return 1;
    }

    // ===== 测试 1: 读取根包的元数据 =====
    std::printf("===== 测试 1: 根包元数据 =====\n");
    auto pkgMeta = EAnnotationReader::readPackageMeta(rootPkg);
    std::printf("  nsPrefix = %s\n", pkgMeta.nsPrefix.c_str());
    std::printf("  nsUri    = %s\n", pkgMeta.nsUri.c_str());
    std::printf("  qualified = %s\n", pkgMeta.isQualified ? "true" : "false");

    // 检查根包是否有 eAnnotation
    auto* ann = rootPkg->getEAnnotation("TaggedValues");
    std::printf("  has TaggedValues annotation: %s\n", ann ? "YES" : "NO");
    if (ann) {
        std::printf("  TaggedValues details:\n");
        for (auto& [k, v] : ann->getDetailsMap()) {
            std::printf("    %s = %s\n", k.c_str(), v.c_str());
        }
    }

    // ===== 测试 2: 读取 ARPackage 的元数据 =====
    std::printf("\n===== 测试 2: ARPackage 元数据 =====\n");
    auto* arpkg = findClass(rootPkg, "ARPackage");
    if (!arpkg) {
        std::printf("  ARPackage not found\n");
    } else {
        auto meta = EAnnotationReader::readClassMeta(arpkg);
        std::printf("  xmlName        = %s\n", meta.xmlName.c_str());
        std::printf("  xmlNamePlural  = %s\n", meta.xmlNamePlural.c_str());
        std::printf("  contentKind    = %s\n", meta.contentKind.c_str());
        std::printf("  namespace      = %s\n", meta.namespace_.c_str());
        std::printf("  nsPrefix       = %s\n", meta.nsPrefix.c_str());
        std::printf("  nsUri          = %s\n", meta.nsUri.c_str());
        std::printf("  isGlobalElement = %s\n", meta.isGlobalElement ? "true" : "false");
        std::printf("  isExtensionPoint = %s\n", meta.isExtensionPoint ? "true" : "false");
        std::printf("  isOrdered      = %s\n", meta.isOrdered ? "true" : "false");
        std::printf("  stereotype     = %s\n", meta.stereotype.c_str());

        // 读取 ARPackage 的 feature 元数据
        std::printf("\n  --- ARPackage features ---\n");
        for (auto* sf : arpkg->getEStructuralFeatures()) {
            auto fmeta = EAnnotationReader::readFeatureMeta(sf);
            std::printf("  feature: %s\n", sf->getName().c_str());
            std::printf("    xmlName         = %s\n", fmeta.xmlName.c_str());
            std::printf("    xmlNamePlural   = %s\n", fmeta.xmlNamePlural.c_str());
            std::printf("    featureKind     = %s\n", fmeta.featureKind.c_str());
            std::printf("    namespace       = %s\n", fmeta.namespace_.c_str());
            std::printf("    sequenceOffset  = %d\n", fmeta.sequenceOffset);
            std::printf("    isRoleElement   = %s\n", fmeta.isRoleElement ? "true" : "false");
            std::printf("    isRoleWrapper   = %s\n", fmeta.isRoleWrapperElement ? "true" : "false");
            std::printf("    isTypeElement   = %s\n", fmeta.isTypeElement ? "true" : "false");
            std::printf("    isTypeWrapper   = %s\n", fmeta.isTypeWrapperElement ? "true" : "false");
            const char* ruleStr = "UNKNOWN";
            switch (fmeta.aprxmlRule) {
                case AprxmlRule::APRXML0012: ruleStr = "APRXML0012"; break;
                case AprxmlRule::APRXML0013: ruleStr = "APRXML0013"; break;
                case AprxmlRule::APRXML0015: ruleStr = "APRXML0015"; break;
                case AprxmlRule::APRXML0016: ruleStr = "APRXML0016"; break;
                default: break;
            }
            std::printf("    aprxmlRule      = %s\n", ruleStr);
        }

        // 读取继承关系的 sequenceOffset
        std::printf("\n  --- ARPackage 继承关系 ---\n");
        for (auto* super : arpkg->getESuperTypes()) {
            if (!super) continue;
            int offset = EAnnotationReader::getGeneralizationSequenceOffset(arpkg, super->getName());
            std::printf("  super: %s, sequenceOffset = %d\n", super->getName().c_str(), offset);
        }
    }

    // ===== 测试 3: 读取 AUTOSAR 根类的元数据 =====
    std::printf("\n===== 测试 3: AUTOSAR 根类元数据 =====\n");
    auto* autosar = findClass(rootPkg, "AUTOSAR");
    if (!autosar) {
        std::printf("  AUTOSAR not found\n");
    } else {
        auto meta = EAnnotationReader::readClassMeta(autosar);
        std::printf("  xmlName        = %s\n", meta.xmlName.c_str());
        std::printf("  xmlNamePlural  = %s\n", meta.xmlNamePlural.c_str());
        std::printf("  contentKind    = %s\n", meta.contentKind.c_str());
        std::printf("  isGlobalElement = %s\n", meta.isGlobalElement ? "true" : "false");

        std::printf("\n  --- AUTOSAR features ---\n");
        for (auto* sf : autosar->getEStructuralFeatures()) {
            auto fmeta = EAnnotationReader::readFeatureMeta(sf);
            std::printf("  feature: %s, xmlName=%s, sequenceOffset=%d, roleWrapper=%s, typeElement=%s\n",
                sf->getName().c_str(), fmeta.xmlName.c_str(), fmeta.sequenceOffset,
                fmeta.isRoleWrapperElement ? "true" : "false",
                fmeta.isTypeElement ? "true" : "false");
        }
    }

    std::printf("\n========== 测试完成 ==========\n");
    return 0;
}
