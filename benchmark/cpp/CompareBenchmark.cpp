// C++ emf-artop Compare Benchmark
// 测量模型比较性能：match + diff（对齐 Java EMF Compare DefaultMatchEngine/DefaultDiffEngine）。
//
// 用法：compare_benchmark [input.arxml] [iterations]
// 加载 arxml 两次（left/right），比较 identical 模型测 match 性能基线。
// 不写输出文件（纯内存测量，避免磁盘累积）。
#include "emf/artop/runtime/AutosarResource.h"
#include "emf/artop/runtime/AutosarResourceFactory.h"
#include "emf/artop/runtime/AutosarReleaseDescriptor.h"
#include "emf/artop/runtime/IdentifiableUtil.h"  // artop IdentifierProvider
#include "emf/xmi/XMIResource.h"
#include "emf/xmi/XMIOptions.h"
#include "emf/common/EObject.h"
#include "emf/common/EPackageRegistry.h"
#include "emf/compare/Comparison.h"

#include <chrono>
#include <cstdio>
#include <cstdlib>
#include <fstream>
#include <iostream>
#include <memory>
#include <string>

#include "autosar40/autosartoplevelstructure/AutosartoplevelstructurePackage.h"

static std::shared_ptr<emf::artop::runtime::AutosarXMLResource> loadResource(const std::string& path) {
    emf::common::URI uri("file:" + path);
    auto res = std::make_shared<emf::artop::runtime::AutosarXMLResource>(uri);
    res->setSchemaLocation("http://autosar.org/schema/r4.0 AUTOSAR_00048.xsd");
    emf::xmi::XMIOptions opts;
    std::ifstream ifs(path, std::ios::binary);
    res->load(ifs, opts);
    return res;
}

int main(int argc, char** argv) {
    std::string inputFile = (argc > 1) ? argv[1]
        : "/workspace/java/demo/output/ECUConfigurationParameters.arxml";
    int iterations = (argc > 2) ? std::atoi(argv[2]) : 3;

    extern void init_all_autosar40_packages();
    init_all_autosar40_packages();
    emf::common::EPackageRegistry::instance().put(
        "http://autosar.org/schema/r4.0",
        emf::artop::autosar40::autosartoplevelstructure::AutosartoplevelstructurePackage::eINSTANCE);

    std::cout << "=== C++ Compare Benchmark ===" << std::endl;
    std::cout << "File: " << inputFile << std::endl;
    std::cout << "Iterations: " << iterations << std::endl;
    std::cout << std::endl;

    double tLoad = 0, tCompare = 0;
    long long matchCount = 0, diffCount = 0;

    for (int i = 0; i < iterations; ++i) {
        // 1. Load left + right（identical 模型）
        auto loadStart = std::chrono::high_resolution_clock::now();
        auto left = loadResource(inputFile);
        auto right = loadResource(inputFile);
        auto loadEnd = std::chrono::high_resolution_clock::now();
        double loadMs = std::chrono::duration<double, std::milli>(loadEnd - loadStart).count();

        // 2. Compare（match + diff）
        //    比较两个 Resource 的根 EObject
        //    注入 artop IdentifierProvider（shortName/uuid 作 ID），对齐 artop IdentifiableUtil，
        //    避免对无 isID 标记的 AUTOSAR 对象走 proximity（大文件性能关键）
        auto cmpStart = std::chrono::high_resolution_clock::now();
        emf::common::EObject* leftRoot = left->getContents().empty() ? nullptr : left->getContents()[0];
        emf::common::EObject* rightRoot = right->getContents().empty() ? nullptr : right->getContents()[0];
        emf::compare::Comparison comp = emf::compare::compare(
            leftRoot, rightRoot, emf::artop::runtime::IdentifiableUtil::asIdentifierProvider());
        auto cmpEnd = std::chrono::high_resolution_clock::now();
        double cmpMs = std::chrono::duration<double, std::milli>(cmpEnd - cmpStart).count();

        matchCount = static_cast<long long>(comp.getMatches().size());
        diffCount = 0;
        for (auto& m : comp.getMatches()) {
            diffCount += static_cast<long long>(m.getDiffs().size());
        }

        std::printf("Iter %d: load=%.0f ms (2x), compare=%.0f ms | matches=%lld, diffs=%lld\n",
                    i + 1, loadMs, cmpMs, matchCount, diffCount);

        if (i > 0) {
            tLoad += loadMs;
            tCompare += cmpMs;
        }
    }

    int n = iterations > 1 ? iterations - 1 : 1;
    std::cout << std::endl << "=== Summary (excl. warmup) ===" << std::endl;
    std::printf("Avg load (2x):  %.0f ms\n", tLoad / n);
    std::printf("Avg compare:    %.0f ms\n", tCompare / n);
    std::printf("Matches: %lld, Diffs: %lld (identical model)\n", matchCount, diffCount);
    std::cout << "=== DONE ===" << std::endl;
    return 0;
}
