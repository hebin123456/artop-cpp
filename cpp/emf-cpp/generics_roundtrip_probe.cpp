// GenericsGoCrazy roundtrip probe: load .ecore → save → diff vs original
// 用于字节级验证 C++ EMF 输出与 Java EMF 一致性
#include "emf/xmi/XMIResource.h"
#include "emf/xmi/XMIResourceFactory.h"
#include "emf/common/EPackageRegistry.h"
#include "emf/ecore/EcorePackage.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/common/URI.h"

#include <cstdio>
#include <fstream>
#include <sstream>
#include <string>
#include <vector>

using namespace emf;
using emf::ecore::EPackage;
using emf::ecore::EClass;
using emf::ecore::EClassifier;
using emf::ecore::EcoreFactory;
using emf::ecore::EcorePackage;

static std::string readAll(const std::string& path) {
    std::ifstream f(path, std::ios::binary);
    std::stringstream ss; ss << f.rdbuf();
    return ss.str();
}

static int failures = 0;
#define CHECK(cond, msg) do { if (cond) { std::printf("[OK] %s\n", msg); } else { std::printf("[FAIL] %s\n", msg); ++failures; } } while(0)

int main(int argc, char** argv) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    emf::xmi::XMIResourceFactory::registerDefaults();

    std::string ecorePath = (argc >= 2) ? argv[1]
        : "/workspace/libs/opensource/emf/tests/org.eclipse.emf.test.examples/data/htmlExporter/GenericsGoCrazy.ecore";
    std::string original = readAll(ecorePath);
    CHECK(!original.empty(), "读取 GenericsGoCrazy.ecore 成功");
    if (original.empty()) return 1;

    // (1) 加载 .ecore
    auto res = emf::xmi::XMIResourceFactory::createResourceFor(common::URI::createFileURI(ecorePath));
    res->loadFromString(original);
    CHECK(!res->getContents().empty(), "加载得到 contents 非空");
    auto* pkg = dynamic_cast<EPackage*>(res->getContents().front());
    CHECK(pkg != nullptr, "根对象是 EPackage");
    if (!pkg) return 1;
    std::printf("  pkg name=%s nsURI=%s classifiers=%zu\n",
                pkg->getName().c_str(), pkg->getNsURI().c_str(), pkg->getEClassifiers().size());

    // (2) saveToString —— 重新序列化
    std::printf("[DBG] res->getEncoding() = '%s'\n", res->getEncoding().c_str());
    std::printf("[DBG] res->getXmiVersion() = '%s'\n", res->getXmiVersion().c_str());
    std::string roundtrip = res->saveToString();
    CHECK(!roundtrip.empty(), "saveToString 非空");

    // (3) 写到文件用于人工对比
    {
        std::ofstream f("/workspace/cpp/emf-cpp/GenericsGoCrazy.roundtrip.ecore", std::ios::binary);
        f << roundtrip;
    }
    {
        std::ofstream f("/workspace/cpp/emf-cpp/GenericsGoCrazy.original.ecore", std::ios::binary);
        f << original;
    }

    // (4) 字节级对比
    if (roundtrip == original) {
        std::printf("[OK] 字节级完全一致：roundtrip == original (size=%zu)\n", original.size());
    } else {
        std::printf("[FAIL] 字节级不一致：roundtrip.size=%zu original.size=%zu\n",
                    roundtrip.size(), original.size());
        ++failures;
        // 找到第一个不同的字节
        size_t n = std::min(roundtrip.size(), original.size());
        size_t i = 0;
        while (i < n && roundtrip[i] == original[i]) ++i;
        size_t line = 1, col = 1;
        for (size_t j = 0; j < i; ++j) {
            if (original[j] == '\n') { ++line; col = 1; } else ++col;
        }
        std::printf("  第一个差异位置：byte=%zu line=%zu col=%zu\n", i, line, col);
        // 打印差异上下文
        size_t ctxStart = (i >= 80) ? i - 80 : 0;
        size_t ctxEnd = std::min(i + 120, n);
        std::printf("  --- 原文上下文 (line %zu) ---\n", line);
        std::string origCtx = original.substr(ctxStart, ctxEnd - ctxStart);
        std::printf("  %.200s\n", origCtx.c_str());
        if (origCtx.size() > 200) std::printf("  ...[truncated]\n");
        std::printf("  --- roundtrip 上下文 (line %zu) ---\n", line);
        std::string rtCtx = roundtrip.substr(ctxStart, ctxEnd - ctxStart);
        std::printf("  %.200s\n", rtCtx.c_str());
        if (rtCtx.size() > 200) std::printf("  ...[truncated]\n");
    }

    std::printf("\n=== 探针结束: %d 个失败 ===\n", failures);
    return failures == 0 ? 0 : 1;
}
