// EMF C++ vs Java 性能对比 benchmark
// 加载 + 保存 ecore 文件，测量耗时，输出 CSV 格式
// 对齐 Java EmfBenchmark: load → saveToString，多次迭代取平均
#include "emf/xmi/XMIResource.h"
#include "emf/xmi/XMIResourceFactory.h"
#include "emf/common/EPackageRegistry.h"
#include "emf/ecore/EcorePackage.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/common/URI.h"

#include <chrono>
#include <cstdio>
#include <cstdlib>
#include <fstream>
#include <sstream>
#include <string>

using namespace emf;

static std::string readAll(const std::string& path) {
    std::ifstream f(path, std::ios::binary);
    std::stringstream ss; ss << f.rdbuf();
    return ss.str();
}

int main(int argc, char** argv) {
    if (argc < 3) {
        std::fprintf(stderr, "Usage: %s <ecoreFile> <iterations>\n", argv[0]);
        return 1;
    }
    std::string ecorePath = argv[1];
    int iters = std::atoi(argv[2]);
    if (iters <= 0) iters = 10;

    ecore::EcoreFactory::initialize();
    ecore::EcorePackage::initialize();
    xmi::XMIResourceFactory::registerDefaults();

    // 读取文件字节
    std::string original = readAll(ecorePath);
    long loadBytes = (long)original.size();

    // 预热：首次加载（含初始化，不计入计时）
    long saveBytes = 0;
    {
        auto res = xmi::XMIResourceFactory::createResourceFor(common::URI::createFileURI(ecorePath));
        res->loadFromString(original);
        std::string saved = res->saveToString();
        saveBytes = (long)saved.size();
        std::fprintf(stderr, "[warmup] loaded %ld bytes, saved %ld bytes\n", loadBytes, saveBytes);
    }

    // 计时迭代
    double totalLoadMs = 0;
    double totalSaveMs = 0;
    for (int i = 0; i < iters; ++i) {
        auto res = xmi::XMIResourceFactory::createResourceFor(common::URI::createFileURI(ecorePath));

        auto t0 = std::chrono::high_resolution_clock::now();
        res->loadFromString(original);
        auto t1 = std::chrono::high_resolution_clock::now();
        std::string saved = res->saveToString();
        auto t2 = std::chrono::high_resolution_clock::now();

        totalLoadMs += std::chrono::duration<double, std::milli>(t1 - t0).count();
        totalSaveMs += std::chrono::duration<double, std::milli>(t2 - t1).count();
    }

    double avgLoadMs = totalLoadMs / iters;
    double avgSaveMs = totalSaveMs / iters;
    // CSV: lang,file,iters,loadBytes,saveBytes,avgLoadMs,avgSaveMs
    // 文件名提取 basename
    std::string base = ecorePath;
    auto slash = base.find_last_of('/');
    if (slash != std::string::npos) base = base.substr(slash + 1);
    std::printf("cpp,%s,%d,%ld,%ld,%.3f,%.3f\n", base.c_str(), iters, loadBytes, saveBytes, avgLoadMs, avgSaveMs);
    return 0;
}
