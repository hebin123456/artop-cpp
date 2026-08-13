// roundtrip_test.cpp —— arxml round-trip 测试
// 加载 arxml → 保存 → 对比 Java 输出
#include "emf/ecore/EcorePackage.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/common/EPackageRegistry.h"
#include "emf/common/EPackage.h"
#include "emf/xmi/XMIResource.h"
#include "emf/xmi/XMIOptions.h"
#include "emf/artop/runtime/AutosarResource.h"
#include "emf/artop/runtime/AutosarResourceFactory.h"
#include "emf/artop/runtime/AutosarXMLLoader.h"
#include "emf/artop/runtime/AutosarXMLSaver.h"

#include <cstdio>
#include <fstream>
#include <functional>
#include <iostream>
#include <sstream>
#include <string>

// 生成的静态包
#include "autosar40/Autosar40Package.h"
#include "autosar40/Autosar40Factory.h"
#include "autosar40/Autosar40ResourceImpl.h"
// atls 子包：initialize() 会级联触发所有依赖子包（对齐 Java AutosartoplevelstructurePackageImpl.init）
#include "autosar40/autosartoplevelstructure/AutosartoplevelstructurePackage.h"
// master init：一次性初始化全部 424 个 autosar40 子包（对齐 Java EPackage.Registry 全集）
// Autosar40Package::initialize() 不级联子包，AutosartoplevelstructurePackage 也只覆盖部分依赖，
// 因此需显式调用全部子包的 initialize()，否则 findEClassByXmlNameRecursive 找不到
// KeywordSet/LifeCycleInfoSet 等位于其他子包的 EClass。
#include "init_all_packages.h"

int main(int argc, char** argv) {
    if (argc < 2) {
        std::cerr << "Usage: " << argv[0] << " <input.arxml> [output.arxml] [java_output.arxml]" << std::endl;
        return 1;
    }
    std::string inputPath = argv[1];
    std::string outputPath = (argc >= 3) ? argv[2] : "/tmp/roundtrip_out.arxml";

    // 初始化 EMF 核心
    emf::ecore::EcoreFactory::initialize();
    emf::ecore::EcorePackage::initialize();

    // 初始化 autosar40 静态包
    auto* pkg = emf::artop::autosar40::Autosar40Package::instance();
    if (!pkg) {
        std::cerr << "ERROR: Failed to initialize Autosar40Package" << std::endl;
        return 2;
    }
    // 显式初始化 atls 子包：对齐 Java AutosartoplevelstructurePackageImpl.init()，
    // 该方法会级联触发所有依赖子包（arobject/admindata/arpackage/blockelements/specialdata/ginfrastructure...）
    // 的 initialize()，从而构建完整的元模型 EClass 树。
    emf::artop::autosar40::autosartoplevelstructure::AutosartoplevelstructurePackage::initialize();
    // 一次性初始化全部 424 个 autosar40 子包（含 KeywordSet/LifeCycleInfoSet 等）
    emf::artop::autosar40::initializeAllPackages();
    // 扁平化：把 registry 中所有 autosar40 子包加为 Autosar40Package 的直接子包。
    // 对齐 Java ExtendedMetaData.getType 的全局查找行为——Java 通过 EPackage.Registry 遍历
    // 所有注册的包查找 EClass。C++ 端 registry 中每个子包都独立注册了自己的 nsURI，
    // 但 Autosar40Package（根包）的 eSubpackages 为空，导致 findEClassByXmlNameRecursive
    // 无法递归到子包。这里把所有 nsURI 以 "http://autosar.org/schema/r4.0" 开头的包
    // 加为根包的子包，使 loader 的递归查找能覆盖全部元模型。
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
    std::cerr << "INFO: Autosar40Package initialized, nsURI=" << pkg->getNsURI()
              << ", classifiers=" << pkg->getEClassifiers().size()
              << ", subpackages=" << pkg->getESubpackages().size() << std::endl;
    // 递归统计所有子包的 classifiers
    {
        std::function<size_t(emf::ecore::EPackage*)> countAll;
        countAll = [&](emf::ecore::EPackage* p) -> size_t {
            size_t n = p->getEClassifiers().size();
            for (auto* sp : p->getESubpackages()) n += countAll(sp);
            return n;
        };
        std::cerr << "INFO: Total classifiers (recursive): " << countAll(pkg) << std::endl;
    }

    // 注册到全局 registry
    emf::common::EPackageRegistry::instance().put(pkg->getNsURI(), pkg);
    // arxml 文件用 http://autosar.org/schema/r4.0 namespace,但 ecore 根包 nsURI
    // 是 http://autosar.org/schema/r4.0/autosar40。Java ARTOP 通过 AutosarReleaseDescriptor
    // 映射,这里简化为额外注册 arxml namespace。
    emf::common::EPackageRegistry::instance().put("http://autosar.org/schema/r4.0", pkg);

    // 创建 Resource
    auto uri = emf::common::URI::createFileURI(inputPath);
    auto resource = std::make_unique<emf::artop::autosar40::Autosar40ResourceImpl>(uri);

    // 加载
    std::ifstream ifs(inputPath);
    if (!ifs.is_open()) {
        std::cerr << "ERROR: Cannot open " << inputPath << std::endl;
        return 3;
    }
    std::stringstream ss;
    ss << ifs.rdbuf();
    std::string content = ss.str();

    emf::xmi::XMIOptions opts;
    opts.recordUnknownFeature = true;
    try {
        resource->loadFromString(content, opts);
    } catch (const std::exception& e) {
        std::cerr << "ERROR: loadFromString failed: " << e.what() << std::endl;
        return 4;
    }

    std::cerr << "INFO: Loaded, contents size=" << resource->getContents().size() << std::endl;
    for (auto* obj : resource->getContents()) {
        if (obj && obj->eClass()) {
            std::cerr << "  root: " << obj->eClass()->getName() << std::endl;
        }
    }

    // 保存
    std::string out;
    try {
        out = resource->saveToString(opts);
    } catch (const std::exception& e) {
        std::cerr << "ERROR: saveToString failed: " << e.what() << std::endl;
        return 5;
    }

    std::ofstream ofs(outputPath);
    if (!ofs.is_open()) {
        std::cerr << "ERROR: Cannot open " << outputPath << " for write" << std::endl;
        return 6;
    }
    ofs << out;
    ofs.close();

    std::cerr << "INFO: Saved to " << outputPath << ", size=" << out.size() << std::endl;

    // 简单对比:如果提供了 Java 输出路径
    if (argc >= 4) {
        std::string javaPath = argv[3];
        std::ifstream jfs(javaPath);
        if (jfs.is_open()) {
            std::stringstream jss;
            jss << jfs.rdbuf();
            std::string javaContent = jss.str();
            if (out == javaContent) {
                std::cout << "MATCH: C++ output identical to Java output" << std::endl;
                return 0;
            } else {
                std::cout << "DIFF: C++ output differs from Java output" << std::endl;
                std::cout << "  C++ size: " << out.size() << ", Java size: " << javaContent.size() << std::endl;
                size_t minLen = std::min(out.size(), javaContent.size());
                for (size_t i = 0; i < minLen; ++i) {
                    if (out[i] != javaContent[i]) {
                        size_t start = (i >= 40) ? i - 40 : 0;
                        size_t end = std::min(minLen, i + 60);
                        std::cout << "  First diff at pos " << i << std::endl;
                        std::cout << "  C++:  ..." << out.substr(start, end - start) << "..." << std::endl;
                        std::cout << "  Java: ..." << javaContent.substr(start, end - start) << "..." << std::endl;
                        break;
                    }
                }
                return 7;
            }
        }
    }

    return 0;
}
