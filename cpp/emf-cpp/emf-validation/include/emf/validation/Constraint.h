// EMF Validation: Constraint / IConstraintListener
// 对齐 org.eclipse.emf.validation.model.IConstraintConstraint (Java)
//      org.eclipse.emf.validation.model.IConstraintListener
#pragma once

#include <functional>
#include <string>
#include <vector>

namespace emf::common {
class EObject;
}

namespace emf::ecore {
class EClass;
}

namespace emf::validation {

// 约束严重级别（对齐 Java Severity）
enum class Severity {
    OK,
    INFO,
    WARNING,
    ERROR,
    CANCEL
};

// 校验模式（对齐 Java IValidationConstraint.LIVE/BATCH）
// 对齐 org.eclipse.emf.validation.model.EvaluationMode: BATCH/LIVE
enum class ConstraintMode {
    LIVE,    // 实时校验（响应对象变化）
    BATCH    // 批校验
};

// Constraint：单个校验约束（对齐 Java IConstraintConstraint）
// 持有一个求值函数 evaluator：传入 EObject*，返回 true=通过，false=违反
//
// clientContext 按 EClass 过滤（对齐 artop EMF Validation 的 clientContext enablement 机制）：
//   - targets 为空 → 对所有 EObject 生效（兼容旧语义，通用约束）
//   - targets 非空 → 只对 EObject.eClass() 命中 targets（含子类）的对象执行 evaluator
//   - 这避免了"全树逐对象执行所有约束"的开销，artop 的 49 个 ECUC 约束正是靠此机制
//     只对匹配 EClass（如 EcucParameterValue）执行，从而远快于通用约束。
class Constraint {
public:
    using Evaluator = std::function<bool(emf::common::EObject*)>;

    Constraint() = default;
    Constraint(Evaluator eval,
               std::string id,
               std::string name,
               std::string message,
               Severity severity,
               ConstraintMode mode = ConstraintMode::BATCH)
        : evaluator_(std::move(eval)),
          id_(std::move(id)),
          name_(std::move(name)),
          message_(std::move(message)),
          severity_(severity),
          mode_(mode) {}

    const std::string& getId() const { return id_; }
    void setId(const std::string& s) { id_ = s; }

    const std::string& getName() const { return name_; }
    void setName(const std::string& s) { name_ = s; }

    const std::string& getMessage() const { return message_; }
    void setMessage(const std::string& s) { message_ = s; }

    Severity getSeverity() const { return severity_; }
    void setSeverity(Severity s) { severity_ = s; }

    ConstraintMode getMode() const { return mode_; }
    void setMode(ConstraintMode m) { mode_ = m; }

    const Evaluator& getEvaluator() const { return evaluator_; }
    void setEvaluator(Evaluator e) { evaluator_ = std::move(e); }

    // ===== clientContext EClass 过滤（对齐 artop constraintBindings/clientContext） =====
    // targets 为空 → 适用所有 EObject；非空 → 仅适用 EClass 命中（含子类）的对象
    const std::vector<emf::ecore::EClass*>& getTargets() const { return targets_; }
    void addTarget(emf::ecore::EClass* cls) { if (cls) targets_.push_back(cls); }
    void setTargets(std::vector<emf::ecore::EClass*> t) { targets_ = std::move(t); }

    // 按类名过滤（动态模型场景，无法提前拿到 EClass* 时用）：
    // targetClassNames 为空 → 不按类名过滤；非空 → eClass().getName() 包含任一子串才适用
    // 对齐 artop clientContext enablement 的 instanceof 语义（用子串匹配覆盖 Ecuc* 继承族）。
    const std::vector<std::string>& getTargetClassNames() const { return targetClassNames_; }
    void addTargetClassName(const std::string& n) { if (!n.empty()) targetClassNames_.push_back(n); }
    void setTargetClassNames(std::vector<std::string> t) { targetClassNames_ = std::move(t); }

    // 判断 target 是否适用本约束：
    //   targets_ 为空且 targetClassNames_ 为空 → 适用所有
    //   targets_ 非空 → eClass 命中（含子类）
    //   targetClassNames_ 非空 → eClass.name 包含任一子串
    bool appliesTo(emf::common::EObject* target) const;

    // 求值：evaluator 为空时视为通过；appliesTo 为 false 时跳过（视为通过）
    bool evaluate(emf::common::EObject* target) const {
        if (!target) return true;
        if (!appliesTo(target)) return true;
        return evaluator_ ? evaluator_(target) : true;
    }

private:
    Evaluator evaluator_;
    std::string id_;
    std::string name_;
    std::string message_;
    Severity severity_ = Severity::WARNING;
    ConstraintMode mode_ = ConstraintMode::BATCH;
    std::vector<emf::ecore::EClass*> targets_;           // clientContext EClass 过滤
    std::vector<std::string> targetClassNames_;           // 按类名子串过滤（动态模型）
};

// 约束监听器（对齐 Java IConstraintListener）
class IConstraintListener {
public:
    virtual ~IConstraintListener() = default;
    virtual void constraintRegistered(Constraint* c) = 0;
    virtual void constraintUnregistered(Constraint* c) = 0;
};

}  // namespace emf::validation
