// resourceset_multi_file_test.cpp
// 实证：普通 XMI ResourceSet 多文件 demand-load + arxml AutosarResourceSet 多文件
//
// 场景 A（普通 XMI）：
//   a.xmi 含 EClass "A"，b.xmi 含 EClass "B"，B.eSuperTypes href="a.xmi#//A"
//   XMIResourceSet 加载 b.xmi → 自动 demand-load a.xmi → B.eSuperTypes 解析为 A（非 proxy）
//
// 场景 B（arxml）：
//   AutosarResourceSet 加载两个真实 arxml 文件
//   验证：createResource 返回 AutosarXMLResource + load 后 AutosarLibraryIndex 有内容 + demand-load
#include "emf/ecore/EcorePackage.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/common/EPackageRegistry.h"
#include "emf/xmi/XMIResource.h"
#include "emf/xmi/XMIResourceFactory.h"
#include "emf/xmi/XMIResourceSet.h"
#include "emf/xmi/XMIOptions.h"
#include "emf/artop/runtime/AutosarResource.h"
#include "emf/artop/runtime/AutosarResourceSet.h"
#include "emf/artop/runtime/AutosarLibraryIndex.h"

#include "autosar40/Autosar40Package.h"
#include "autosar40/Autosar40ResourceImpl.h"
#include "autosar40/autosartoplevelstructure/AutosartoplevelstructurePackage.h"
#include "init_all_packages.h"

#include <cstdio>
#include <fstream>
#include <iostream>
#include <sstream>
#include <string>

static std::string loadFile(const std::string& path) {
    std::ifstream ifs(path);
    if (!ifs.is_open()) { std::cerr << "cannot open " << path << "\n"; std::exit(2); }
    std::stringstream ss; ss << ifs.rdbuf(); return ss.str();
}

static void writeFile(const std::string& path, const std::string& content) {
    std::ofstream ofs(path);
    ofs << content;
}

int main() {
    int failures = 0;

    // ====================================================================
    // 场景 A：普通 XMI ResourceSet 多文件 demand-load
    // ====================================================================
    std::cout << "===== Scenario A: XMI ResourceSet multi-file demand-load =====\n";
    {
        emf::ecore::EcoreFactory::initialize();
        emf::ecore::EcorePackage::initialize();
        emf::xmi::XMIResourceFactory::registerDefaults();

        // 构造 a.xmi：EPackage 含 EClass "A"（标准 ecore XMI 形式）
        std::string a_xmi =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            "<ecore:EPackage xmi:version=\"2.0\" xmlns:xmi=\"http://www.omg.org/XMI\" xmlns:ecore=\"http://www.eclipse.org/emf/2002/Ecore\" name=\"pkgA\" nsURI=\"http://example.com/pkgA\" nsPrefix=\"pkgA\">\n"
            "  <eClassifiers xsi:type=\"ecore:EClass\" name=\"A\"/>\n"
            "</ecore:EPackage>\n";
        writeFile("/tmp/a.xmi", a_xmi);

        // 构造 b.xmi：EPackage 含 EClass "B"，eSuperTypes 跨文档引用 a.xmi#//A
        // Java EMF XMI 标准序列化形式：multi-valued EReference 用属性（空格分隔多个值）
        std::string b_xmi =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            "<ecore:EPackage xmi:version=\"2.0\" xmlns:xmi=\"http://www.omg.org/XMI\" xmlns:ecore=\"http://www.eclipse.org/emf/2002/Ecore\" name=\"pkgB\" nsURI=\"http://example.com/pkgB\" nsPrefix=\"pkgB\">\n"
            "  <eClassifiers xsi:type=\"ecore:EClass\" name=\"B\" eSuperTypes=\"a.xmi#//A\"/>\n"
            "</ecore:EPackage>\n";
        writeFile("/tmp/b.xmi", b_xmi);

        emf::xmi::XMIResourceSet rs;
        // 加载 b.xmi（demand-load 会自动加载 a.xmi）
        auto uriB = emf::common::URI::createFileURI("/tmp/b.xmi");
        auto* resB = rs.getResource(uriB, true);
        if (!resB) {
            std::cout << "  FAIL: getResource(b.xmi) returned null\n";
            ++failures;
        } else {
            std::cout << "  [A1] loaded b.xmi, resources in set = " << rs.getResources().size() << "\n";
            if (rs.getResources().size() < 2) {
                std::cout << "  FAIL: expected >=2 resources (b + demand-loaded a), got " << rs.getResources().size() << "\n";
                ++failures;
            } else {
                std::cout << "  [A2] OK: demand-load auto-loaded a.xmi\n";
            }
            // 检查 B.eSuperTypes 是否解析为 A（非 proxy）
            // resB contents[0] 是 EPackage "pkgB"，需导航到 EClass "B"
            auto* pkgB = dynamic_cast<emf::ecore::EPackage*>(resB->getContents().empty() ? nullptr : resB->getContents()[0]);
            emf::ecore::EClass* bCls = nullptr;
            if (pkgB) {
                for (auto* c : pkgB->getEClassifiers()) {
                    if (c && c->getName() == "B") { bCls = dynamic_cast<emf::ecore::EClass*>(c); break; }
                }
            }
            if (!bCls) {
                std::cout << "  FAIL: EClass \"B\" not found in b.xmi\n";
                ++failures;
            } else {
                auto& sups = bCls->getESuperTypes();
                if (sups.empty()) {
                    std::cout << "  FAIL: B.eSuperTypes is empty\n";
                    ++failures;
                } else {
                    auto* sup = sups[0];
                    if (sup->eIsProxy()) {
                        std::cout << "  FAIL: B.eSuperTypes[0] is still proxy (not resolved)\n";
                        ++failures;
                    } else {
                        std::cout << "  [A3] OK: B.eSuperTypes[0] resolved (non-proxy)\n";
                        if (sup->getName() == "A") {
                            std::cout << "  [A4] OK: resolved target is EClass \"A\"\n";
                        } else {
                            std::cout << "  FAIL: resolved target name = " << sup->getName() << ", expected A\n";
                            ++failures;
                        }
                    }
                }
            }
        }
    }

    // ====================================================================
    // 场景 B：arxml AutosarResourceSet 多文件
    // ====================================================================
    std::cout << "\n===== Scenario B: arxml AutosarResourceSet multi-file =====\n";
    {
        // 初始化 autosar40
        emf::ecore::EcoreFactory::initialize();
        emf::ecore::EcorePackage::initialize();
        auto* pkg = emf::artop::autosar40::Autosar40Package::instance();
        emf::artop::autosar40::autosartoplevelstructure::AutosartoplevelstructurePackage::initialize();
        emf::artop::autosar40::initializeAllPackages();
        {
            auto* rootPkg = dynamic_cast<emf::ecore::EPackageImpl*>(pkg);
            if (rootPkg) {
                for (auto* regPkg : emf::common::EPackageRegistry::instance().values()) {
                    if (regPkg == pkg) continue;
                    auto* ePkg = dynamic_cast<emf::ecore::EPackage*>(regPkg);
                    if (!ePkg) continue;
                    const std::string& uri = ePkg->getNsURI();
                    if (uri.rfind("http://autosar.org/schema/r4.0", 0) != 0) continue;
                    rootPkg->addESubpackage(ePkg);
                }
            }
        }
        emf::common::EPackageRegistry::instance().put(pkg->getNsURI(), pkg);
        emf::common::EPackageRegistry::instance().put("http://autosar.org/schema/r4.0", pkg);

        // 清空 AutosarLibraryIndex（隔离测试）
        emf::artop::runtime::AutosarLibraryIndex::instance().clear();

        emf::artop::runtime::AutosarResourceSet rs;
        // 注入 Autosar40ResourceImpl creator（对齐 codegen 专用 resource）
        rs.setResourceCreator([](const emf::common::URI& uri) {
            return std::make_unique<emf::artop::autosar40::Autosar40ResourceImpl>(uri);
        });

        // 选两个真实 arxml 文件
        std::string f1 = "/workspace/java/demo/output_test/AISpecificationBaseTypesStandard.arxml";
        std::string f2 = "/workspace/java/demo/output_test/AISpecificationCompuMethodBlueprint.arxml";

        // getResource(uri, true) demand-load + 自动 indexLibrary
        auto* res1 = rs.getResource(emf::common::URI::createFileURI(f1), true);
        auto* res2 = rs.getResource(emf::common::URI::createFileURI(f2), true);

        if (!res1 || !res2) {
            std::cout << "  FAIL: getResource returned null\n";
            ++failures;
        } else {
            std::cout << "  [B1] loaded 2 arxml files, resources in set = " << rs.getResources().size() << "\n";

            // 验证 createResource 返回 autosar resource
            auto* ar1 = dynamic_cast<emf::artop::runtime::AutosarResource*>(res1);
            if (ar1) {
                std::cout << "  [B2] OK: res1 is AutosarResource\n";
            } else {
                std::cout << "  FAIL: res1 is not AutosarResource\n";
                ++failures;
            }

            // 验证 load 后 AutosarLibraryIndex 有内容（自动 indexLibrary）
            size_t idxSize = emf::artop::runtime::AutosarLibraryIndex::instance().size();
            std::cout << "  [B3] AutosarLibraryIndex size after load = " << idxSize << "\n";
            if (idxSize == 0) {
                std::cout << "  FAIL: AutosarLibraryIndex empty (indexLibrary not auto-called)\n";
                ++failures;
            } else {
                std::cout << "  OK: auto indexLibrary worked\n";
            }

            // 验证 resource 有 ResourceSet
            if (res1->getResourceSet() == &rs) {
                std::cout << "  [B4] OK: res1.getResourceSet() == rs\n";
            } else {
                std::cout << "  FAIL: res1.getResourceSet() mismatch\n";
                ++failures;
            }

            // 验证两次 getResource 同一 URI 返回同一 resource（不重复加载）
            auto* res1Again = rs.getResource(emf::common::URI::createFileURI(f1), true);
            if (res1Again == res1) {
                std::cout << "  [B5] OK: getResource(idempotent) returns same resource\n";
            } else {
                std::cout << "  FAIL: getResource returned different resource for same URI\n";
                ++failures;
            }
        }
    }

    std::cout << "\n=== RESULT: " << (failures == 0 ? "ALL OK" : "HAS FAILURES") << " ===\n";
    return failures == 0 ? 0 : 99;
}
