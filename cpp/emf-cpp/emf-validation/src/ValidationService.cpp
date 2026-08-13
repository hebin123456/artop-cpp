// ValidationService.cpp
#include "emf/validation/ValidationService.h"
#include "emf/validation/AutosarConstraints.h"  // validateUuidUniqueness（模型级约束）

#include <algorithm>
#include <thread>
#include <vector>

namespace emf::validation {

ValidationService::ValidationService()
    : validator_(std::make_unique<EValidator>()) {
    // 默认注册内置约束
    validator_->registerDefaultConstraints();
    // 注册 artop ECUC 专用约束（对齐 org.artop.aal.autosar40.constraints.ecuc）
    // 约束通过 evaluator 内部 classNameContains 按 EClass 名过滤（clientContext enablement 等价），
    // 只对匹配的 Ecuc* 类对象执行，避免全树扫描开销。
    registerEcucConstraints(*validator_);
}

ValidationService::~ValidationService() = default;

void ValidationService::setValidator(std::unique_ptr<EValidator> v) {
    validator_ = std::move(v);
}

std::vector<emf::common::Diagnostic> ValidationService::validate(emf::common::EObject* target) {
    std::vector<emf::common::Diagnostic> out;
    if (!validator_ || !target) return out;
    // BATCH 模式：只执行 ConstraintMode::BATCH 约束
    // 对齐 Java IBatchValidator + EvaluationMode.BATCH
    auto diags = includeLive_
        ? validator_->validate(target)  // includeLive=true: 执行全部约束
        : validator_->validate(target, ConstraintMode::BATCH);
    out.insert(out.end(),
               std::make_move_iterator(diags.begin()),
               std::make_move_iterator(diags.end()));
    return out;
}

void ValidationService::collectAll(emf::common::EObject* root,
                                   std::vector<emf::common::EObject*>& out) {
    if (!root) return;
    out.push_back(root);
    std::vector<emf::common::EObject*> stack;
    for (auto* c : root->eContents()) {
        if (c) stack.push_back(c);
    }
    while (!stack.empty()) {
        emf::common::EObject* cur = stack.back();
        stack.pop_back();
        if (!cur) continue;
        out.push_back(cur);
        for (auto* c : cur->eContents()) {
            if (c) stack.push_back(c);
        }
    }
}

std::vector<emf::common::Diagnostic> ValidationService::validateAll(emf::common::EObject* root) {
    std::vector<emf::common::Diagnostic> result;
    if (!validator_ || !root) return result;
    std::vector<emf::common::EObject*> all;
    collectAll(root, all);
    if (!includeRoot_ && !all.empty()) {
        all.erase(all.begin());
    }

    // 性能优化：对象级并行验证（对齐 Java IBatchValidator 的并行模式）。
    // 约束执行是只读的（evaluator 只读 feature 值，不修改对象），可安全并行。
    // 小树（< 2000 对象）走串行避免线程创建开销；大树分块并行，按块顺序合并保持顺序一致。
    const size_t n = all.size();
    const size_t minParallel = 2000;
    if (n < minParallel) {
        // 串行
        for (auto* obj : all) {
            auto diags = includeLive_
                ? validator_->validate(obj)
                : validator_->validate(obj, ConstraintMode::BATCH);
            if (!diags.empty()) {
                result.reserve(result.size() + diags.size());
                for (auto& d : diags) result.push_back(std::move(d));
            }
        }
    } else {
        // 并行：分块执行，per-thread 收集后按块顺序合并
        unsigned hwThreads = std::thread::hardware_concurrency();
        if (hwThreads == 0) hwThreads = 4;
        size_t nThreads = std::min(n, static_cast<size_t>(hwThreads));
        size_t chunkSize = (n + nThreads - 1) / nThreads;
        // 调整实际线程数（避免空块）
        nThreads = (n + chunkSize - 1) / chunkSize;

        std::vector<std::thread> threads;
        threads.reserve(nThreads);
        std::vector<std::vector<emf::common::Diagnostic>> perThread(nThreads);

        for (size_t t = 0; t < nThreads; ++t) {
            size_t start = t * chunkSize;
            size_t end = std::min(start + chunkSize, n);
            if (start >= end) break;
            threads.emplace_back([this, &all, &perThread, t, start, end]() {
                auto& local = perThread[t];
                for (size_t i = start; i < end; ++i) {
                    auto diags = includeLive_
                        ? validator_->validate(all[i])
                        : validator_->validate(all[i], ConstraintMode::BATCH);
                    if (!diags.empty()) {
                        for (auto& d : diags) local.push_back(std::move(d));
                    }
                }
            });
        }
        for (auto& th : threads) th.join();
        // 按块顺序合并（保持与串行一致的 diagnostic 顺序）
        size_t total = 0;
        for (const auto& v : perThread) total += v.size();
        result.reserve(total);
        for (auto& v : perThread) {
            for (auto& d : v) result.push_back(std::move(d));
        }
    }

    // 模型级 UUID 全局唯一性校验（对齐 artop FixUuidConflictsAction）。
    // per-object 约束无法检测跨对象重复，需整树遍历去重。
    // O(N) 单次遍历，与 per-object 约束的 O(N * features) 相比开销可忽略。
    // 串行执行（需全树遍历去重，无法并行）。
    auto uuidDiags = validateUuidUniqueness(root);
    if (!uuidDiags.empty()) {
        result.reserve(result.size() + uuidDiags.size());
        for (auto& d : uuidDiags) result.push_back(std::move(d));
    }
    return result;
}

}  // namespace emf::validation
