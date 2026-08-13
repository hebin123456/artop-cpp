// AnnotationConstraintLoader 实现
// 对齐 Java: EObjectValidator 遍历 EAnnotation(OCL) + EcoreValidator 读取 constraints annotation
//
// Gap 6 修复：loadAll 增加 per-EClass 编译缓存。
// 原实现每次 validateInvariants 都重新 compile OCL 表达式，batch 全树校验
// N 对象 × compile = 极慢（benchmark 57153 对象 4546ms 主因之一）。
// 缓存后：每个 EClass 只编译一次，后续直接复用编译好的 evaluator 注册。
// 对齐 Java EObjectValidator 的 invariant 缓存（per-EClass 编译一次）。
#include "emf/validation/AnnotationConstraintLoader.h"
#include "emf/validation/ConstraintParser.h"
#include "emf/ecore/EcoreImpls.h"

#include <sstream>
#include <unordered_map>
#include <unordered_set>

namespace emf::validation {

namespace {

// 从 eClass 的 annotations 中找指定 source 的 annotation，返回其 details。
// 对齐 Java EModelElement.getEAnnotation(source).getDetails()。
const std::vector<std::pair<std::string, std::string>>* findAnnotationDetails(
        emf::ecore::EClass* eClass, const char* source) {
    if (!eClass) return nullptr;
    for (auto* ann : eClass->getEAnnotations()) {
        if (ann && ann->getSource() == source) {
            return &ann->getDetails();
        }
    }
    return nullptr;
}

// 把 ConstraintParser::ExpressionEvaluator（bool(EObject*, any)）适配为
// Constraint::Evaluator（bool(EObject*)）：value 固定传空 any。
Constraint::Evaluator adaptEval(ExpressionEvaluator eval) {
    return [eval](emf::common::EObject* target) {
        return eval(target, std::any{});
    };
}

// 已编译的约束条目（编译结果可复用，省去重复 parse/compile）。
// 注：ConstraintParser::compile 解析失败返回恒 true 的 evaluator，无法区分；
//     统一缓存（恒 true 不报错，对齐原 registerOne 容错行为）。
struct CompiledEntry {
    std::string name;
    std::string expr;
    std::string source;
    ExpressionEvaluator eval;
};

CompiledEntry compileOne(const std::string& name, const std::string& expr,
                         const std::string& source) {
    return {name, expr, source, ConstraintParser::compile(expr)};
}

bool registerCompiled(EValidator& validator, const CompiledEntry& e) {
    Constraint* c = new Constraint(
        adaptEval(e.eval),
        e.source + "#" + e.name,  // id
        e.name,
        "constraint '" + e.name + "' violated (expr: " + e.expr + ")",
        Severity::ERROR,
        ConstraintMode::BATCH);
    validator.registerConstraint(c);
    return true;
}

// per-EClass 编译缓存：EClass* → 已编译约束列表。
// EClass 是元模型单例（Package::eINSTANCE 持有），生命周期等同程序，作 key 安全。
// 线程安全：当前 batch/live 校验均为单线程（Diagnostician 同步遍历、LiveValidator
// notifyChanged 同步触发）。若未来多线程校验，需加 mutex 保护 g_compileCache。
std::unordered_map<emf::ecore::EClass*, std::vector<CompiledEntry>> g_compileCache;

// 编译 EClass 的 OCL + Named-constraints，首次编译后缓存，后续直接返回缓存引用。
const std::vector<CompiledEntry>& compileFor(emf::ecore::EClass* eClass) {
    auto it = g_compileCache.find(eClass);
    if (it != g_compileCache.end()) return it->second;

    std::vector<CompiledEntry> entries;
    // 1) OCL annotation：details 的每个 (name, expr) 编译
    if (const auto* details = findAnnotationDetails(eClass, AnnotationConstraintLoader::OCL_SOURCE)) {
        for (const auto& [name, expr] : *details) {
            if (name.empty() || expr.empty()) continue;
            entries.push_back(compileOne(name, expr, AnnotationConstraintLoader::OCL_SOURCE));
        }
    }
    // 2) Named-constraints annotation：收集 invariant 名，在 OCL annotation 中查同名表达式
    if (const auto* details = findAnnotationDetails(eClass, AnnotationConstraintLoader::CONSTRAINTS_SOURCE)) {
        std::unordered_set<std::string> wanted;
        for (const auto& [key, value] : *details) {
            std::istringstream iss(value);
            std::string name;
            while (iss >> name) {
                if (!name.empty()) wanted.insert(name);
            }
        }
        if (!wanted.empty()) {
            if (const auto* ocl = findAnnotationDetails(eClass, AnnotationConstraintLoader::OCL_SOURCE)) {
                for (const auto& [name, expr] : *ocl) {
                    if (wanted.count(name) == 0 || expr.empty()) continue;
                    entries.push_back(compileOne(name, expr, AnnotationConstraintLoader::CONSTRAINTS_SOURCE));
                }
            }
        }
    }
    it = g_compileCache.emplace(eClass, std::move(entries)).first;
    return it->second;
}

}  // namespace

int AnnotationConstraintLoader::loadOclConstraints(EValidator& validator,
                                                    emf::ecore::EClass* eClass) {
    const auto* details = findAnnotationDetails(eClass, OCL_SOURCE);
    if (!details) return 0;
    int count = 0;
    for (const auto& [name, expr] : *details) {
        if (name.empty() || expr.empty()) continue;
        if (registerCompiled(validator, compileOne(name, expr, OCL_SOURCE))) ++count;
    }
    return count;
}

int AnnotationConstraintLoader::loadNamedConstraints(EValidator& validator,
                                                      emf::ecore::EClass* eClass) {
    const auto* details = findAnnotationDetails(eClass, CONSTRAINTS_SOURCE);
    if (!details) return 0;
    // details 的 value 是空格分隔的 invariant 名列表（key 通常是 EClass 名或空）
    // 收集所有 invariant 名
    std::unordered_set<std::string> wantedNames;
    for (const auto& [key, value] : *details) {
        std::istringstream iss(value);
        std::string name;
        while (iss >> name) {
            if (!name.empty()) wantedNames.insert(name);
        }
    }
    if (wantedNames.empty()) return 0;
    // 在 OCL annotation 中查同名表达式
    const auto* oclDetails = findAnnotationDetails(eClass, OCL_SOURCE);
    if (!oclDetails) return 0;
    int count = 0;
    for (const auto& [name, expr] : *oclDetails) {
        if (wantedNames.count(name) == 0) continue;
        if (expr.empty()) continue;
        if (registerCompiled(validator, compileOne(name, expr, CONSTRAINTS_SOURCE))) ++count;
    }
    return count;
}

int AnnotationConstraintLoader::loadAll(EValidator& validator, emf::ecore::EClass* eClass) {
    // Gap 6 修复：用 per-EClass 编译缓存，避免每次 validateInvariants 重新 compile。
    // compileFor 首次编译并缓存，后续直接返回缓存引用；此处仅注册（构造 Constraint 对象）。
    const auto& entries = compileFor(eClass);
    int count = 0;
    for (const auto& e : entries) {
        if (registerCompiled(validator, e)) ++count;
    }
    return count;
}

}  // namespace emf::validation
