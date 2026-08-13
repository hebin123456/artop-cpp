// C++ emf-artop Validation Benchmark
// 测量模型校验性能：batch 全树校验 + LiveValidator attach（对齐 Java EMF Validation）。
//
// 用法：validation_benchmark [input.arxml] [iterations]
// 不写输出文件（纯内存测量，避免磁盘累积）。
#include "emf/artop/runtime/AutosarResource.h"
#include "emf/artop/runtime/AutosarResourceFactory.h"
#include "emf/artop/runtime/AutosarReleaseDescriptor.h"
#include "emf/xmi/XMIResource.h"
#include "emf/xmi/XMIOptions.h"
#include "emf/common/EObject.h"
#include "emf/common/EPackageRegistry.h"
#include "emf/validation/Diagnostician.h"
#include "emf/validation/LiveValidator.h"
#include "emf/validation/EValidator.h"
#include "emf/validation/ValidationService.h"
#include "emf/validation/AutosarConstraints.h"

#include <chrono>
#include <cstdio>
#include <cstdlib>
#include <fstream>
#include <iostream>
#include <memory>
#include <string>

#include "autosar40/autosartoplevelstructure/AutosartoplevelstructurePackage.h"

int main(int argc, char** argv) {
    std::string inputFile = (argc > 1) ? argv[1]
        : "/workspace/java/demo/output/ECUConfigurationParameters.arxml";
    int iterations = (argc > 2) ? std::atoi(argv[2]) : 3;

    extern void init_all_autosar40_packages();
    init_all_autosar40_packages();
    emf::common::EPackageRegistry::instance().put(
        "http://autosar.org/schema/r4.0",
        emf::artop::autosar40::autosartoplevelstructure::AutosartoplevelstructurePackage::eINSTANCE);

    std::cout << "=== C++ Validation Benchmark ===" << std::endl;
    std::cout << "File: " << inputFile << std::endl;
    std::cout << "Iterations: " << iterations << std::endl;
    std::cout << std::endl;

    double tLoad = 0, tBatch = 0, tLiveAttach = 0, tLiveValidate = 0;
    long long diagCount = 0;

    for (int i = 0; i < iterations; ++i) {
        // 1. Load
        auto loadStart = std::chrono::high_resolution_clock::now();
        emf::common::URI uri("file:" + inputFile);
        auto resource = std::make_shared<emf::artop::runtime::AutosarXMLResource>(uri);
        resource->setSchemaLocation("http://autosar.org/schema/r4.0 AUTOSAR_00048.xsd");
        emf::xmi::XMIOptions opts;
        std::ifstream ifs(inputFile, std::ios::binary);
        resource->load(ifs, opts);
        auto loadEnd = std::chrono::high_resolution_clock::now();
        double loadMs = std::chrono::duration<double, std::milli>(loadEnd - loadStart).count();

        emf::common::EObject* root = resource->getContents().empty()
            ? nullptr : resource->getContents()[0];

        // 2. Batch validate（全树 DFS，对齐 Java IBatchValidator.validate）
        //    Diagnostician：走 EValidator::Registry + ecore generated validator
        auto batchStart = std::chrono::high_resolution_clock::now();
        auto diags = emf::validation::Diagnostician::validate(root,
            emf::validation::ConstraintMode::BATCH);
        auto batchEnd = std::chrono::high_resolution_clock::now();
        double batchMs = std::chrono::duration<double, std::milli>(batchEnd - batchStart).count();
        diagCount = static_cast<long long>(diags.size());

        // 2b. ValidationService（含 ECUC 约束 + clientContext EClass 过滤，对齐 artop）
        emf::validation::ValidationService vs;
        auto vsStart = std::chrono::high_resolution_clock::now();
        auto vsDiags = vs.validateAll(root);
        auto vsEnd = std::chrono::high_resolution_clock::now();
        double vsMs = std::chrono::duration<double, std::milli>(vsEnd - vsStart).count();

        // 3. LiveValidator attach（递归挂载到 containment 树）
        emf::validation::EValidator validator;
        auto* live = new emf::validation::ValidationLiveAdapter(validator);
        auto attachStart = std::chrono::high_resolution_clock::now();
        live->attach(root);
        auto attachEnd = std::chrono::high_resolution_clock::now();
        double attachMs = std::chrono::duration<double, std::milli>(attachEnd - attachStart).count();

        // 4. LiveValidator 单对象校验（模拟修改后增量触发）
        auto lvStart = std::chrono::high_resolution_clock::now();
        live->validateNow(root);
        auto lvEnd = std::chrono::high_resolution_clock::now();
        double lvMs = std::chrono::duration<double, std::milli>(lvEnd - lvStart).count();

        // 清理
        auto& ads = root->eAdapters();
        ads.erase(std::remove(ads.begin(), ads.end(), live), ads.end());
        delete live;

        std::printf("Iter %d: load=%.0f ms, batch=%.0f ms (%lld diags), vs=%lld diags %.0f ms, "
                    "liveAttach=%.0f ms, liveValidate=%.0f ms\n",
                    i + 1, loadMs, batchMs, diagCount,
                    static_cast<long long>(vsDiags.size()), vsMs, attachMs, lvMs);

        if (i > 0) {
            tLoad += loadMs; tBatch += batchMs;
            tLiveAttach += attachMs; tLiveValidate += lvMs;
        }
    }

    int n = iterations > 1 ? iterations - 1 : 1;
    std::cout << std::endl << "=== Summary (excl. warmup) ===" << std::endl;
    std::printf("Avg load:         %.0f ms\n", tLoad / n);
    std::printf("Avg batch validate: %.0f ms (%lld diagnostics)\n", tBatch / n, diagCount);
    std::printf("Avg live attach:  %.0f ms\n", tLiveAttach / n);
    std::printf("Avg live validateNow: %.0f ms\n", tLiveValidate / n);
    std::cout << "=== DONE ===" << std::endl;
    return 0;
}
