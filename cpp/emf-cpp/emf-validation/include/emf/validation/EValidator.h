// EMF Validation: 校验器
// 对齐 org.eclipse.emf.ecore.EValidator (Java)
//      org.eclipse.emf.validation.service.IValidator (Java)
#pragma once

#include "emf/validation/Constraint.h"
#include "emf/common/Diagnostic.h"
#include "emf/common/EObject.h"
#include <vector>
#include <memory>
#include <string>
#include <unordered_map>

namespace emf::ecore { class EPackage; }

namespace emf::validation {

// EValidator：对单个 EObject 执行所有已注册约束并返回 Diagnostic 列表
class EValidator {
public:
    EValidator();
    ~EValidator();

    EValidator(const EValidator&) = delete;
    EValidator& operator=(const EValidator&) = delete;

    // ===== 约束注册/反注册 =====
    // 取得约束所有权，重复注册同名 ID 将替换并返回旧指针（调用方负责 delete）
    Constraint* registerConstraint(Constraint* c);
    Constraint* registerConstraint(Constraint::Evaluator eval,
                                   const std::string& id,
                                   const std::string& name,
                                   const std::string& msg,
                                   Severity sev,
                                   ConstraintMode mode = ConstraintMode::BATCH);
    bool unregisterConstraint(const std::string& id);
    Constraint* getConstraint(const std::string& id) const;
    const std::vector<Constraint*>& getConstraints() const { return constraints_; }

    // ===== 监听器 =====
    void addListener(IConstraintListener* l) { listeners_.push_back(l); }
    void removeListener(IConstraintListener* l);

    // ===== 校验 =====
    // 对单个 EObject 执行所有约束，FAILURE -> Diagnostic（按 Constraint.severity 映射）
    // 不过滤 mode（兼容旧接口）
    std::vector<emf::common::Diagnostic> validate(emf::common::EObject* target);

    // 对齐 Java EvaluationMode：按 mode 过滤约束
    // BATCH 模式只执行 ConstraintMode::BATCH 约束
    // LIVE 模式只执行 ConstraintMode::LIVE 约束
    std::vector<emf::common::Diagnostic> validate(emf::common::EObject* target,
                                                    ConstraintMode mode);

    // ===== 内置默认约束 =====
    // 注册默认的 no_empty_name 和 no_null_required_ref（mode=BATCH）
    void registerDefaultConstraints();

    // ===== Registry（对齐 Java EValidator.Registry.INSTANCE） =====
    // 按 EPackage 注册/查找 EValidator，实现 Diagnostician 的 per-EPackage 分派
    class Registry {
    public:
        static Registry& instance();

        // 注册 validator 到 EPackage（不取得所有权，调用方保证生命周期）
        void put(emf::ecore::EPackage* pkg, EValidator* validator);
        EValidator* get(emf::ecore::EPackage* pkg) const;
        bool containsKey(emf::ecore::EPackage* pkg) const;
        void remove(emf::ecore::EPackage* pkg);

    private:
        std::unordered_map<emf::ecore::EPackage*, EValidator*> map_;
    };

private:
    void notifyRegistered(Constraint* c);
    void notifyUnregistered(Constraint* c);

    std::vector<Constraint*> constraints_;
    std::vector<IConstraintListener*> listeners_;
};

}  // namespace emf::validation
