// Diagnostician.cpp
// 对齐 org.eclipse.emf.ecore.util.Diagnostician (Java)
//
// 遍历 containment 树，按 EPackage 分派到 EValidator.Registry 中注册的 EValidator。
// 对齐 Java:
//   Diagnostician.validate(EObject) ->
//     validate(EObject, DiagnosticChain, context) ->
//       遍历 EcoreUtil.getAllContents(eObject) ->
//         validate(EClass, EObject, DiagnosticChain, context) ->
//           registry.get(eClass.getEPackage()).validate(...)
#include "emf/validation/Diagnostician.h"
#include "emf/validation/EValidator.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/ecore/EcorePackage.h"
#include "emf/ecore/util/EcoreValidator.h"
#include "emf/ecore/util/EValidatorRegistryImpl.h"
#include "emf/common/Diagnostic.h"

#include <any>
#include <algorithm>
#include <thread>
#include <unordered_map>
#include <vector>

namespace emf::validation {

namespace {

// 收集 root 及其所有 containment 子对象（DFS）
// 对齐 Java EcoreUtil.getAllContents(EObject, true)
// 安全性：跳过 proxy 和 eClass 为 null 的对象（避免虚表崩溃）
void collectAll(emf::common::EObject* root, std::vector<emf::common::EObject*>& out) {
    if (!root) return;
    out.push_back(root);
    std::vector<emf::common::EObject*> stack;
    try {
        auto rc = root->eContents();
        for (auto* c : rc) {
            if (c) stack.push_back(c);
        }
    } catch (...) {}
    while (!stack.empty()) {
        auto* cur = stack.back();
        stack.pop_back();
        if (!cur) continue;
        // 跳过 proxy（未解析的跨文档引用，eContents 可能无效）
        if (cur->eIsProxy()) continue;
        out.push_back(cur);
        try {
            auto cc = cur->eContents();
            for (auto* c : cc) {
                if (c) stack.push_back(c);
            }
        } catch (...) {}
    }
}

// 默认 EValidator（当 Registry 中未注册时使用）
// 对齐 Java EValidatorRegistryImpl.delegatedGet(null) -> EObjectValidator.INSTANCE
EValidator& defaultValidator() {
    static EValidator inst;
    static bool initialized = false;
    if (!initialized) {
        inst.registerDefaultConstraints();
        initialized = true;
    }
    return inst;
}

// 把 DiagnosticChain 中非 OK 的诊断平铺为 Diagnostic 列表
std::vector<emf::common::Diagnostic> flattenChain(emf::common::DiagnosticChain& chain) {
    std::vector<emf::common::Diagnostic> out;
    for (auto& d : chain.get()) {
        if (d && d->severity() != emf::common::Diagnostic::Severity::OK) {
            out.emplace_back(d->severity(), d->source(), d->code(), d->message());
        }
    }
    return out;
}

// lazy 初始化内置 EValidator 注册（首次校验时触发）
// 对齐 Java EValidator.Registry.INSTANCE 联动：EcorePackage 注册 EcoreValidator
void ensureBuiltinsInitialized() {
    static bool inited = false;
    if (inited) return;
    Diagnostician::initializeBuiltins();
    inited = true;
}

}  // namespace

// 方案 A：注册 EcoreValidator 桥接到 EValidator::Registry
// emf-ecore 不能反向依赖 emf-validation，故注册代码放在 emf-validation 侧。
// 桥接 EValidator 内置一个 ecore-meta 约束：调用 EcoreValidator::validateEObject
// 执行 ecore 元模型对象的元约束校验（EValidator.validate 自身还会执行 EveryDefaultConstraint）。
void Diagnostician::initializeBuiltins() {
    emf::ecore::EcorePackage::initialize();
    auto* ecorePkg = emf::ecore::EcorePackage::instance().getEPackage();
    if (!ecorePkg) return;
    // 幂等：已注册则跳过
    if (EValidator::Registry::instance().containsKey(ecorePkg)) return;

    static EValidator ecoreBridge;  // 永生；Registry 持非所有权指针
    static bool bridgeReady = false;
    if (!bridgeReady) {
        // ecore 元约束：委托 EcoreValidator::validateEObject 收集诊断
        auto ecoreMetaEval = [](emf::common::EObject* target) -> bool {
            if (!target) return true;
            emf::common::DiagnosticChain chain;
            std::unordered_map<std::string, std::any> ctx;
            emf::ecore::util::EcoreValidator::validateEObject(target, &chain, &ctx);
            for (auto& d : chain.get()) {
                if (d && d->severity() != emf::common::Diagnostic::Severity::OK) return false;
            }
            return true;
        };
        ecoreBridge.registerConstraint(ecoreMetaEval,
            "emf.validation.builtin.ecore_meta", "EcoreMeta",
            "Ecore meta-model constraint violated", Severity::ERROR, ConstraintMode::BATCH);
        bridgeReady = true;
    }
    EValidator::Registry::instance().put(ecorePkg, &ecoreBridge);
}

std::vector<emf::common::Diagnostic> Diagnostician::validateObject(emf::common::EObject* target,
                                                                    ConstraintMode mode) {
    if (!target) return {};
    ensureBuiltinsInitialized();

    auto* cls = target->eClass();
    emf::ecore::EPackage* pkg = nullptr;
    if (cls) pkg = cls->getEPackage();

    // 1. 对齐 Java: registry.get(eClass.getEPackage()) 查找 validation 层 EValidator
    if (pkg) {
        EValidator* validator = EValidator::Registry::instance().get(pkg);
        if (validator) {
            return validator->validate(target, mode);
        }
    }

    // 2. fallback：ecore 层 EValidatorRegistryImpl（generated <Pkg>Validator 自我注册于此）
    //    对齐 Java EValidator.Registry.INSTANCE 的 ecore 层注册；将 emf::ecore::util::EValidator
    //    桥接为 Diagnostic 列表返回。
    if (pkg) {
        auto* ecoreVal = emf::ecore::util::EValidatorRegistryImpl::instance().getEValidator(pkg);
        if (ecoreVal) {
            emf::common::DiagnosticChain chain;
            ecoreVal->validate(target, &chain);
            return flattenChain(chain);
        }
    }

    // 3. 未注册时用默认 validator（对齐 Java registry.get(null) -> EObjectValidator.INSTANCE）
    return defaultValidator().validate(target, mode);
}

std::vector<emf::common::Diagnostic> Diagnostician::validate(emf::common::EObject* root,
                                                               ConstraintMode mode) {
    std::vector<emf::common::Diagnostic> result;
    if (!root) return result;
    // 遍历 containment 树（对齐 Java Diagnostician 遍历 EcoreUtil.getAllContents）
    std::vector<emf::common::EObject*> all;
    collectAll(root, all);

    // 性能优化：对象级并行验证（对齐 Java Diagnostician 的并行模式）。
    // validateObject 走 EValidator::Registry（只读查找）+ EValidator/EObjectValidator（只读执行），
    // 约束执行不修改对象，可安全并行。小树走串行避免线程开销。
    const size_t n = all.size();
    const size_t minParallel = 2000;
    if (n < minParallel) {
        // 串行
        for (auto* obj : all) {
            auto diags = validateObject(obj, mode);
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
        nThreads = (n + chunkSize - 1) / chunkSize;

        std::vector<std::thread> threads;
        threads.reserve(nThreads);
        std::vector<std::vector<emf::common::Diagnostic>> perThread(nThreads);

        for (size_t t = 0; t < nThreads; ++t) {
            size_t start = t * chunkSize;
            size_t end = std::min(start + chunkSize, n);
            if (start >= end) break;
            threads.emplace_back([&all, &perThread, t, start, end, mode]() {
                auto& local = perThread[t];
                for (size_t i = start; i < end; ++i) {
                    auto diags = Diagnostician::validateObject(all[i], mode);
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
    return result;
}

// ===== V7: 带 DiagnosticChain + context 的重载 =====
// 对齐 Java Diagnostician.validate(EObject, DiagnosticChain, Map)：
//   - 单对象版：把结果追加到传入 chain，返回是否通过
//   - 递归版：遍历 containment 树，对每个对象调单对象版
bool Diagnostician::validate(emf::common::EObject* target,
                              emf::common::DiagnosticChain* chain,
                              emf::ecore::util::ValidationContext* context,
                              ConstraintMode mode) {
    if (!target || !chain) return true;
    ensureBuiltinsInitialized();
    auto* cls = target->eClass();
    emf::ecore::EPackage* pkg = cls ? cls->getEPackage() : nullptr;
    bool ok = true;
    // 1. validation 层 EValidator::Registry
    if (pkg) {
        auto* validator = EValidator::Registry::instance().get(pkg);
        if (validator) {
            auto diags = validator->validate(target, mode);
            for (auto& d : diags) {
                chain->add(std::make_shared<emf::common::Diagnostic>(
                    d.severity(), d.source(), d.code(), d.message()));
                if (d.severity() == emf::common::Diagnostic::Severity::ERROR ||
                    d.severity() == emf::common::Diagnostic::Severity::CANCEL) ok = false;
            }
            return ok;
        }
    }
    // 2. ecore 层 EValidatorRegistryImpl（生成 <Pkg>Validator 注册于此）
    if (pkg) {
        auto* ecoreVal = emf::ecore::util::EValidatorRegistryImpl::instance().getEValidator(pkg);
        if (ecoreVal) {
            bool r = ecoreVal->validate(target, chain, context);
            if (!r) ok = false;
            return ok;
        }
    }
    // 3. 默认 validator（EObjectValidator）
    auto diags = defaultValidator().validate(target, mode);
    for (auto& d : diags) {
        chain->add(std::make_shared<emf::common::Diagnostic>(
            d.severity(), d.source(), d.code(), d.message()));
        if (d.severity() == emf::common::Diagnostic::Severity::ERROR ||
            d.severity() == emf::common::Diagnostic::Severity::CANCEL) ok = false;
    }
    return ok;
}

bool Diagnostician::validateRecursive(emf::common::EObject* root,
                                       emf::common::DiagnosticChain* chain,
                                       emf::ecore::util::ValidationContext* context,
                                       ConstraintMode mode) {
    if (!root || !chain) return true;
    bool ok = true;
    std::vector<emf::common::EObject*> all;
    collectAll(root, all);
    for (auto* obj : all) {
        if (!validate(obj, chain, context, mode)) ok = false;
    }
    return ok;
}

}  // namespace emf::validation
