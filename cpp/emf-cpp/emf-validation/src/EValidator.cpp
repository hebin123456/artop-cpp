// EValidator.cpp
// 对齐 org.eclipse.emf.validation.service.IValidator (Java)
// 桥接 emf-ecore-util 的 EObjectValidator 内置约束（EveryDefaultConstraint 等）
#include "emf/validation/EValidator.h"
#include "emf/validation/ConstraintParser.h"
#include "emf/ecore/util/EObjectValidator.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/common/Diagnostic.h"
#include "emf/common/EObject.h"

#include <algorithm>

namespace emf::validation {

// Constraint::appliesTo 实现（clientContext EClass 过滤，对齐 artop enablement）
// 1. targets_ 与 targetClassNames_ 均为空 → 适用所有
// 2. targets_ 非空 → target.eClass() 命中任一 target（含子类，isSuperTypeOf 自反）
// 3. targetClassNames_ 非空 → target.eClass().getName() 包含任一子串（覆盖 Ecuc* 继承族）
bool Constraint::appliesTo(emf::common::EObject* target) const {
    if (targets_.empty() && targetClassNames_.empty()) return true;  // 通用约束，适用所有
    if (!target) return false;
    auto* cls = target->eClass();
    if (!cls) return false;
    // EClass 指针匹配（含子类）
    for (auto* t : targets_) {
        if (!t) continue;
        if (t->isSuperTypeOf(cls)) return true;
    }
    // 类名子串匹配（动态模型场景）
    const std::string& name = cls->getName();
    for (const auto& sub : targetClassNames_) {
        if (!sub.empty() && name.find(sub) != std::string::npos) return true;
    }
    return false;
}

namespace {

emf::common::Diagnostic::Severity mapSeverity(Severity s) {
    switch (s) {
        case Severity::OK:      return emf::common::Diagnostic::Severity::OK;
        case Severity::INFO:    return emf::common::Diagnostic::Severity::INFO;
        case Severity::WARNING: return emf::common::Diagnostic::Severity::WARNING;
        case Severity::ERROR:   return emf::common::Diagnostic::Severity::ERROR;
        case Severity::CANCEL:  return emf::common::Diagnostic::Severity::CANCEL;
    }
    return emf::common::Diagnostic::Severity::WARNING;
}

}  // namespace

EValidator::EValidator() = default;

EValidator::~EValidator() {
    for (auto* c : constraints_) {
        delete c;
    }
}

Constraint* EValidator::registerConstraint(Constraint* c) {
    if (!c) return nullptr;
    Constraint* old = nullptr;
    auto it = std::find_if(constraints_.begin(), constraints_.end(),
                           [&](Constraint* x) { return x && x->getId() == c->getId(); });
    if (it != constraints_.end()) {
        old = *it;
        *it = c;
    } else {
        constraints_.push_back(c);
    }
    notifyRegistered(c);
    return old;
}

Constraint* EValidator::registerConstraint(Constraint::Evaluator eval,
                                           const std::string& id,
                                           const std::string& name,
                                           const std::string& msg,
                                           Severity sev,
                                           ConstraintMode mode) {
    return registerConstraint(new Constraint(std::move(eval), id, name, msg, sev, mode));
}

bool EValidator::unregisterConstraint(const std::string& id) {
    auto it = std::find_if(constraints_.begin(), constraints_.end(),
                           [&](Constraint* x) { return x && x->getId() == id; });
    if (it == constraints_.end()) return false;
    Constraint* c = *it;
    constraints_.erase(it);
    notifyUnregistered(c);
    delete c;
    return true;
}

Constraint* EValidator::getConstraint(const std::string& id) const {
    for (auto* c : constraints_) {
        if (c && c->getId() == id) return c;
    }
    return nullptr;
}

void EValidator::removeListener(IConstraintListener* l) {
    listeners_.erase(std::remove(listeners_.begin(), listeners_.end(), l), listeners_.end());
}

std::vector<emf::common::Diagnostic> EValidator::validate(emf::common::EObject* target) {
    std::vector<emf::common::Diagnostic> out;
    if (!target) return out;
    // 1. 用户注册的 constraint（全部执行，不过滤 mode —— 兼容旧接口）
    for (auto* c : constraints_) {
        if (!c) continue;
        if (!c->evaluate(target)) {
            out.emplace_back(mapSeverity(c->getSeverity()),
                             c->getName(),
                             0,
                             c->getMessage());
        }
    }
    // 2. 桥接 emf-ecore-util EObjectValidator 的内置约束（对齐 Java EObjectValidator）
    //    用 DiagnosticChain 收集，再转为平铺 Diagnostic 返回
    {
        emf::common::DiagnosticChain chain;
        std::unordered_map<std::string, std::any> ctx;
        emf::ecore::util::EObjectValidator::validate_EveryDefaultConstraint(target, &chain, &ctx);
        for (auto& d : chain.get()) {
            if (d && d->severity() != emf::common::Diagnostic::Severity::OK) {
                out.emplace_back(d->severity(), d->source(), d->code(), d->message());
            }
        }
    }
    return out;
}

// 对齐 Java EvaluationMode：按 mode 过滤约束
// BATCH 模式只执行 ConstraintMode::BATCH 约束
// LIVE 模式只执行 ConstraintMode::LIVE 约束
// EObjectValidator 内置约束（EveryDefaultConstraint）在两种模式下都执行
//   （对齐 Java EObjectValidator 作为默认 validator 注册在 registry.get(null)，
//    不受 mode 过滤影响）
std::vector<emf::common::Diagnostic> EValidator::validate(emf::common::EObject* target,
                                                            ConstraintMode mode) {
    std::vector<emf::common::Diagnostic> out;
    if (!target) return out;
    // 1. 用户注册的 constraint，按 mode 过滤
    for (auto* c : constraints_) {
        if (!c) continue;
        if (c->getMode() != mode) continue;  // mode 过滤
        if (!c->evaluate(target)) {
            out.emplace_back(mapSeverity(c->getSeverity()),
                             c->getName(),
                             0,
                             c->getMessage());
        }
    }
    // 2. EObjectValidator 内置约束（两种模式都执行）
    {
        emf::common::DiagnosticChain chain;
        std::unordered_map<std::string, std::any> ctx;
        emf::ecore::util::EObjectValidator::validate_EveryDefaultConstraint(target, &chain, &ctx);
        for (auto& d : chain.get()) {
            if (d && d->severity() != emf::common::Diagnostic::Severity::OK) {
                out.emplace_back(d->severity(), d->source(), d->code(), d->message());
            }
        }
    }
    return out;
}

void EValidator::registerDefaultConstraints() {
    // no_empty_name：ENamedElement 子类不应有空 name（对齐 Java 语义）
    // 对齐 Java EObjectValidator：内置约束不区分 BATCH/LIVE，两种模式都执行
    auto noEmptyNameEval = [](emf::common::EObject* obj) {
        if (!obj) return true;
        auto* cls = obj->eClass();
        if (!cls) return true;
        auto* sf = cls->getEStructuralFeature("name");
        if (!sf) return true;
        auto v = obj->eGet(sf);
        if (v.type() == typeid(std::string)) {
            return !std::any_cast<std::string>(v).empty();
        }
        return true;
    };
    registerConstraint(noEmptyNameEval,
        "emf.validation.default.no_empty_name", "NoEmptyName",
        "Object name must not be empty", Severity::WARNING, ConstraintMode::BATCH);
    registerConstraint(noEmptyNameEval,
        "emf.validation.default.no_empty_name.live", "NoEmptyName",
        "Object name must not be empty", Severity::WARNING, ConstraintMode::LIVE);

    // no_null_required_ref：lowerBound>=1 的单值 reference 不应为 null（对齐 Java 语义）
    auto noNullReqRefEval = [](emf::common::EObject* obj) {
        if (!obj) return true;
        auto* cls = obj->eClass();
        if (!cls) return true;
        for (auto* sf : cls->getEAllStructuralFeatures()) {
            if (!sf) continue;
            auto* ref = dynamic_cast<emf::ecore::EReference*>(sf);
            if (!ref || ref->isMany()) continue;
            if (ref->getLowerBound() >= 1) {
                auto v = obj->eGet(ref);
                emf::common::EObject* val = nullptr;
                if (v.has_value() && v.type() == typeid(emf::common::EObject*)) {
                    val = std::any_cast<emf::common::EObject*>(v);
                }
                if (val == nullptr) return false;
            }
        }
        return true;
    };
    registerConstraint(noNullReqRefEval,
        "emf.validation.default.no_null_required_ref", "NoNullRequiredRef",
        "Required reference must not be null", Severity::ERROR, ConstraintMode::BATCH);
    registerConstraint(noNullReqRefEval,
        "emf.validation.default.no_null_required_ref.live", "NoNullRequiredRef",
        "Required reference must not be null", Severity::ERROR, ConstraintMode::LIVE);
}

void EValidator::notifyRegistered(Constraint* c) {
    auto copy = listeners_;
    for (auto* l : copy) {
        if (l) l->constraintRegistered(c);
    }
}

void EValidator::notifyUnregistered(Constraint* c) {
    auto copy = listeners_;
    for (auto* l : copy) {
        if (l) l->constraintUnregistered(c);
    }
}

// ===== EValidator::Registry（对齐 Java EValidator.Registry.INSTANCE） =====
EValidator::Registry& EValidator::Registry::instance() {
    static Registry inst;
    return inst;
}

void EValidator::Registry::put(emf::ecore::EPackage* pkg, EValidator* validator) {
    map_[pkg] = validator;
}

EValidator* EValidator::Registry::get(emf::ecore::EPackage* pkg) const {
    auto it = map_.find(pkg);
    if (it != map_.end()) return it->second;
    return nullptr;
}

bool EValidator::Registry::containsKey(emf::ecore::EPackage* pkg) const {
    return map_.find(pkg) != map_.end();
}

void EValidator::Registry::remove(emf::ecore::EPackage* pkg) {
    map_.erase(pkg);
}

}  // namespace emf::validation
