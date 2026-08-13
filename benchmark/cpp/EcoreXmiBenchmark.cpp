// C++ 纯 ecore XMI 序列化/反序列化基准测试 —— 对齐 Java EcoreXmiBenchmark
// 使用 EMF 原始 Ecore 元模型（http://www.eclipse.org/emf/2002/Ecore），
// 不涉及 artop 扩展。测量 load（反序列化）和 save（序列化）耗时。
#include "emf/xmi/XMIResource.h"
#include "emf/xmi/XMIOptions.h"
#include "emf/common/EObject.h"
#include "emf/common/EPackageRegistry.h"
#include "emf/ecore/EcorePackage.h"

#include <chrono>
#include <cstdio>
#include <cstdlib>
#include <fstream>
#include <iostream>
#include <string>

// 自动清理输出文件，避免磁盘累积
static std::string g_cleanupFile;
static void cleanupOutputFile() {
    if (!g_cleanupFile.empty()) std::remove(g_cleanupFile.c_str());
}

int main(int argc, char** argv) {
    std::string inputFile = (argc > 1) ? argv[1]
        : "/workspace/benchmark/data/xmi/ecore_5m.xmi";
    std::string outputFile = (argc > 2) ? argv[2]
        : "/tmp/bench_ecore_out_cpp.xmi";
    int iterations = (argc > 3) ? std::atoi(argv[3]) : 3;

    // 初始化 Ecore 元模型包（纯原始 ecore，非 artop）
    emf::ecore::EcorePackage::initialize();
    emf::common::EPackageRegistry::instance().put(
        "http://www.eclipse.org/emf/2002/Ecore",
        emf::ecore::EcorePackage::instance().getEPackage());

    // 文件大小
    std::ifstream sizeIfs(inputFile, std::ios::binary | std::ios::ate);
    double fileSize = static_cast<double>(sizeIfs.tellg());
    sizeIfs.close();

    std::cout << "=== C++ EMF Ecore XMI Benchmark ===" << std::endl;
    std::cout << "File: " << inputFile << std::endl;
    std::cout << "Size: " << fileSize / 1024.0 / 1024.0 << " MB ("
              << static_cast<long long>(fileSize) << " bytes)" << std::endl;
    std::cout << "Iterations: " << iterations << std::endl;
    std::cout << std::endl;

    double loadTimes[16], saveTimes[16];

    bool keepOutput = (std::getenv("XMI_BENCHMARK_KEEP") != nullptr);
    if (!keepOutput) {
        g_cleanupFile = outputFile;
        std::atexit(cleanupOutputFile);
    }

    for (int i = 0; i < iterations; ++i) {
        // ===== Load =====
        auto loadStart = std::chrono::high_resolution_clock::now();
        emf::common::URI uri("file:" + inputFile);
        auto resource = std::make_shared<emf::xmi::XMIResource>(uri);
        emf::xmi::XMIOptions opts;
        std::ifstream ifs(inputFile, std::ios::binary);
        try {
            resource->load(ifs, opts);
        } catch (const std::exception& e) {
            std::cerr << "LOAD ERROR: " << e.what() << std::endl;
            return 1;
        }
        auto loadEnd = std::chrono::high_resolution_clock::now();
        double loadMs = std::chrono::duration<double, std::milli>(loadEnd - loadStart).count();
        loadTimes[i] = loadMs;

        size_t rootCount = resource->getContents().size();

        // ===== Save =====
        auto saveStart = std::chrono::high_resolution_clock::now();
        std::ostringstream oss;
        try {
            resource->save(oss, opts);
        } catch (const std::exception& e) {
            std::cerr << "SAVE ERROR: " << e.what() << std::endl;
            return 1;
        }
        auto saveEnd = std::chrono::high_resolution_clock::now();
        double saveMs = std::chrono::duration<double, std::milli>(saveEnd - saveStart).count();
        saveTimes[i] = saveMs;

        size_t outBytes = oss.str().size();
        if (i == iterations - 1) {
            std::string output = oss.str();
            std::ofstream ofs(outputFile, std::ios::binary);
            ofs << output;
            outBytes = output.size();
        }

        std::printf("Iter %d: load=%.0f ms, save=%.0f ms, total=%.0f ms | roots=%zu, out=%zu bytes\n",
                    i + 1, loadMs, saveMs, loadMs + saveMs, rootCount, outBytes);
    }

    // 汇总（去掉第一轮 warmup）
    double avgLoad, avgSave;
    if (iterations > 1) {
        double sumLoad = 0, sumSave = 0;
        for (int i = 1; i < iterations; ++i) {
            sumLoad += loadTimes[i];
            sumSave += saveTimes[i];
        }
        avgLoad = sumLoad / (iterations - 1);
        avgSave = sumSave / (iterations - 1);
    } else {
        avgLoad = loadTimes[0];
        avgSave = saveTimes[0];
    }

    std::cout << std::endl;
    std::cout << "=== Summary (excl. warmup) ===" << std::endl;
    std::printf("Avg load: %.0f ms (%.1f MB/s)\n", avgLoad, fileSize / 1024.0 / avgLoad);
    std::printf("Avg save: %.0f ms (%.1f MB/s)\n", avgSave, fileSize / 1024.0 / avgSave);
    std::printf("Avg total: %.0f ms\n", avgLoad + avgSave);
    std::cout << "=== DONE ===" << std::endl;
    return 0;
}
