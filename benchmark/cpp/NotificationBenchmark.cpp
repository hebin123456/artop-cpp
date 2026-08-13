// C++ emf-artop Notification Benchmark
// 测量通知机制性能：EContentAdapter 挂载、eNotify 分发、NotificationChain 合并。
// 对齐 Java EMF eNotify/EContentAdapter 性能特性。
//
// 用法：notification_benchmark [input.arxml] [iterations]
// 不写输出文件（纯内存测量，避免磁盘累积）。
#include "emf/artop/runtime/AutosarResource.h"
#include "emf/artop/runtime/AutosarResourceFactory.h"
#include "emf/artop/runtime/AutosarReleaseDescriptor.h"
#include "emf/xmi/XMIResource.h"
#include "emf/xmi/XMIOptions.h"
#include "emf/common/EObject.h"
#include "emf/common/ENotifier.h"
#include "emf/common/EPackageRegistry.h"
#include "emf/common/Notification.h"

#include <chrono>
#include <cstdio>
#include <cstdlib>
#include <fstream>
#include <iostream>
#include <memory>
#include <string>
#include <vector>

#include "autosar40/autosartoplevelstructure/AutosartoplevelstructurePackage.h"

// 收集 EObject 树所有节点（DFS）
static void collectAll(emf::common::EObject* obj, std::vector<emf::common::EObject*>& out) {
    if (!obj) return;
    out.push_back(obj);
    auto contents = obj->eContents();
    for (auto* child : contents) {
        collectAll(child, out);
    }
}

// 测试用 adapter：计数收到的通知
class CountingAdapter : public emf::common::EAdapter {
public:
    long long count = 0;
    void notifyChanged(const emf::common::Notification& n) override {
        (void)n;
        ++count;
    }
};

int main(int argc, char** argv) {
    std::string inputFile = (argc > 1) ? argv[1]
        : "/workspace/java/demo/output/ECUConfigurationParameters.arxml";
    int iterations = (argc > 2) ? std::atoi(argv[2]) : 3;

    extern void init_all_autosar40_packages();
    init_all_autosar40_packages();
    emf::common::EPackageRegistry::instance().put(
        "http://autosar.org/schema/r4.0",
        emf::artop::autosar40::autosartoplevelstructure::AutosartoplevelstructurePackage::eINSTANCE);

    std::cout << "=== C++ Notification Benchmark ===" << std::endl;
    std::cout << "File: " << inputFile << std::endl;
    std::cout << "Iterations: " << iterations << std::endl;
    std::cout << std::endl;

    double tLoad = 0, tCollect = 0, tAttach = 0, tNotify = 0, tChain = 0;
    long long objCount = 0;

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

        // 2. 收集所有 EObject
        auto colStart = std::chrono::high_resolution_clock::now();
        std::vector<emf::common::EObject*> all;
        for (auto* root : resource->getContents()) {
            collectAll(root, all);
        }
        auto colEnd = std::chrono::high_resolution_clock::now();
        double colMs = std::chrono::duration<double, std::milli>(colEnd - colStart).count();
        objCount = static_cast<long long>(all.size());

        // 3. EContentAdapter 挂载（递归 attach 所有 containment 子对象）
        auto attachStart = std::chrono::high_resolution_clock::now();
        auto* contentAdapter = new emf::common::EContentAdapter();
        for (auto* root : resource->getContents()) {
            root->eAdapters().push_back(contentAdapter);
        }
        auto attachEnd = std::chrono::high_resolution_clock::now();
        double attachMs = std::chrono::duration<double, std::milli>(attachEnd - attachStart).count();

        // 4. eNotify 分发：对每个 EObject 发一条 SET 通知，测 eNotify 耗时
        //    （用 CountingAdapter 避免递归 eContents 的干扰）
        CountingAdapter counter;
        for (auto* obj : all) {
            obj->eAdapters().push_back(&counter);
        }
        auto notifyStart = std::chrono::high_resolution_clock::now();
        for (auto* obj : all) {
            emf::common::Notification n(
                emf::common::Notification::EventType::SET, obj, nullptr, -1,
                std::any(), std::any());
            obj->eNotify(n);
        }
        auto notifyEnd = std::chrono::high_resolution_clock::now();
        double notifyMs = std::chrono::duration<double, std::milli>(notifyEnd - notifyStart).count();

        // 5. NotificationChain 批量构造 + dispatch
        auto chainStart = std::chrono::high_resolution_clock::now();
        emf::common::NotificationChain chain;
        for (auto* obj : all) {
            chain.add(emf::common::Notification(
                emf::common::Notification::EventType::SET, obj, nullptr, -1,
                std::any(), std::any()));
        }
        chain.dispatch();
        auto chainEnd = std::chrono::high_resolution_clock::now();
        double chainMs = std::chrono::duration<double, std::milli>(chainEnd - chainStart).count();

        // 清理 adapter（避免重复挂载影响下一轮）
        for (auto* obj : all) {
            auto& ads = obj->eAdapters();
            ads.erase(std::remove(ads.begin(), ads.end(), &counter), ads.end());
        }
        for (auto* root : resource->getContents()) {
            auto& ads = root->eAdapters();
            ads.erase(std::remove(ads.begin(), ads.end(), contentAdapter), ads.end());
        }
        delete contentAdapter;

        std::printf("Iter %d: load=%.0f ms, collect=%.0f ms, attach=%.0f ms, "
                    "eNotify=%.0f ms (%lld objs, %lld notified), chain=%.0f ms\n",
                    i + 1, loadMs, colMs, attachMs, notifyMs, objCount, counter.count, chainMs);

        if (i > 0) {  // 排除 warmup
            tLoad += loadMs; tCollect += colMs; tAttach += attachMs;
            tNotify += notifyMs; tChain += chainMs;
        }
    }

    int n = iterations > 1 ? iterations - 1 : 1;
    std::cout << std::endl << "=== Summary (excl. warmup) ===" << std::endl;
    std::printf("Objects: %lld\n", objCount);
    std::printf("Avg load:          %.0f ms\n", tLoad / n);
    std::printf("Avg collect:       %.0f ms\n", tCollect / n);
    std::printf("Avg EContentAdapter attach: %.0f ms\n", tAttach / n);
    std::printf("Avg eNotify x N:   %.0f ms (%.2f us/notify)\n",
                tNotify / n, tNotify / n * 1000.0 / objCount);
    std::printf("Avg NotificationChain: %.0f ms\n", tChain / n);
    std::cout << "=== DONE ===" << std::endl;
    return 0;
}
