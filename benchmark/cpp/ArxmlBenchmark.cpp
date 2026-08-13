// C++ arxml roundtrip benchmark —— 对齐 Java ArxmlBenchmark
// 测量 load（反序列化）和 save（序列化）的耗时。
#include "emf/artop/runtime/AutosarResource.h"
#include "emf/artop/runtime/AutosarResourceFactory.h"
#include "emf/artop/runtime/AutosarReleaseDescriptor.h"
#include "emf/xmi/XMIResource.h"
#include "emf/xmi/XMIOptions.h"
#include "emf/common/EObject.h"
#include "emf/common/EPackageRegistry.h"

#include <chrono>
#include <cstdio>
#include <cstdlib>
#include <fstream>
#include <iostream>
#include <memory>
#include <string>

// 引入所有生成的 Package 以触发注册
#include "autosar40/autosartoplevelstructure/AutosartoplevelstructurePackage.h"

// 自动清理：程序退出时删除输出文件，避免 96MB arxml 累积塞满磁盘。
// 输出文件只在最后一轮写出一次（供验证 save 正确性），中间轮不落盘。
// 设置环境变量 ARXML_BENCHMARK_KEEP=1 可跳过清理（用于互操作测试抓取输出）。
// 注：atexit 不接受捕获变量的 lambda，用全局变量 + 无捕获函数。
static std::string g_cleanupFile;
static void cleanupOutputFile() {
    if (!g_cleanupFile.empty()) {
        std::remove(g_cleanupFile.c_str());
    }
}

int main(int argc, char** argv) {
    std::string inputFile = (argc > 1) ? argv[1]
        : "/workspace/benchmark/data/large_100m.arxml";
    std::string outputFile = (argc > 2) ? argv[2]
        : "/workspace/benchmark/data/large_100m_out_cpp.arxml";
    int iterations = (argc > 3) ? std::atoi(argv[3]) : 3;

    // 初始化所有生成的 autosar40 子包（420+ 个），确保所有 EClass 注册到 EPackageRegistry。
    // 生成的 AutosartoplevelstructurePackage::initialize() 只初始化 6 个子包，不完整。
    extern void init_all_autosar40_packages();
    init_all_autosar40_packages();
    // 把根包注册到 nsURI="http://autosar.org/schema/r4.0"（Loader 查找此 nsURI）。
    emf::common::EPackageRegistry::instance().put(
        "http://autosar.org/schema/r4.0",
        emf::artop::autosar40::autosartoplevelstructure::AutosartoplevelstructurePackage::eINSTANCE);

    // 获取文件大小
    std::ifstream sizeIfs(inputFile, std::ios::binary | std::ios::ate);
    double fileSize = static_cast<double>(sizeIfs.tellg());
    sizeIfs.close();

    std::cout << "=== C++ emf-artop Arxml Benchmark ===" << std::endl;
    std::cout << "File: " << inputFile << std::endl;
    std::cout << "Size: " << fileSize / 1024.0 / 1024.0 << " MB ("
              << static_cast<long long>(fileSize) << " bytes)" << std::endl;
    std::cout << "Iterations: " << iterations << std::endl;
    std::cout << std::endl;

    double loadTimes[16], saveTimes[16];

    // 自动清理：程序退出时删除输出文件，避免 96MB arxml 累积塞满磁盘。
    // 输出文件只在最后一轮写出一次（供验证 save 正确性），中间轮不落盘。
    // 设置环境变量 ARXML_BENCHMARK_KEEP=1 可跳过清理（用于互操作测试抓取输出）。
    std::string cleanupFile = outputFile;
    bool keepOutput = (std::getenv("ARXML_BENCHMARK_KEEP") != nullptr);
    if (!keepOutput) {
        g_cleanupFile = cleanupFile;
        std::atexit(cleanupOutputFile);
    }

    for (int i = 0; i < iterations; ++i) {
        // TEMP: 循环开头 RSS（上一轮 resource 已析构）
        auto readRss = []() -> long {
            std::ifstream f("/proc/self/status");
            std::string line;
            while (std::getline(f, line)) {
                if (line.rfind("VmRSS:", 0) == 0) return std::atol(line.c_str() + 6) * 1024;
            }
            return -1;
        };
        long rssBefore = readRss();

        // ===== Load =====
        auto loadStart = std::chrono::high_resolution_clock::now();
        emf::common::URI uri("file:" + inputFile);
        auto resource = std::make_shared<emf::artop::runtime::AutosarXMLResource>(uri);
        resource->setSchemaLocation("http://autosar.org/schema/r4.0 AUTOSAR_00048.xsd");

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
        long rssAfterLoad = readRss();
        std::fprintf(stderr, "[phase] load done: %zu ms, roots=%zu, rssAfterLoad=%.1f MB\n",
                    (size_t)loadMs, rootCount, rssAfterLoad / 1048576.0);

        // ARXML_LOAD_ONLY：只测 load（排查 OOM 在 load 还是 save）
        if (std::getenv("ARXML_LOAD_ONLY")) {
            long rssFinal = readRss();
            std::printf("Iter %d: load=%.0f ms | roots=%zu | rssBefore=%.1f MB rssAfterLoad=%.1f MB rssFinal=%.1f MB\n",
                        i + 1, loadMs, rootCount,
                        rssBefore / 1048576.0, rssAfterLoad / 1048576.0, rssFinal / 1048576.0);
            continue;
        }

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

        // 仅最后一轮落盘（供验证 save 正确性），中间轮不写文件避免磁盘累积
        size_t outBytes = oss.str().size();
        if (i == iterations - 1) {
            std::string output = oss.str();
            std::ofstream ofs(outputFile, std::ios::binary);
            ofs << output;
            outBytes = output.size();
        }

        // TEMP: 每轮 RSS 采样（验证多轮内存回收）
        long rssAfter = readRss();

        std::printf("Iter %d: load=%.0f ms, save=%.0f ms, total=%.0f ms | roots=%zu, out=%zu bytes | rssBefore=%.1f MB rssAfter=%.1f MB\n",
                    i + 1, loadMs, saveMs, loadMs + saveMs, rootCount, outBytes, rssBefore / 1048576.0, rssAfter / 1048576.0);
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
