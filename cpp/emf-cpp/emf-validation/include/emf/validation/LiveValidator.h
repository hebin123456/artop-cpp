// EMF Validation: 实时校验 hook
// 对齐 org.eclipse.emf.validation.service.IValidationListener
//      org.eclipse.emf.ecore.util.EContentAdapter（live 模式）
//      org.eclipse.emf.validation.service.ILiveValidator
#pragma once

#include "EValidator.h"
#include "emf/common/Diagnostic.h"
#include "emf/common/ENotifier.h"
#include "emf/common/EObject.h"
#include "emf/common/Notification.h"

#include <memory>
#include <vector>
#include <functional>

namespace emf::validation {

// 监听器：每次校验完成时回调（live 或 batch）
// Java: IValidationListener.validationOccurred(IValidationContext)
class IValidationListener {
public:
    virtual ~IValidationListener() = default;
    // diagnostics: 本次校验产出的所有 Diagnostic
    // target: 触发校验的对象（live 模式下是被改动的对象；batch 下是根）
    virtual void validationOccurred(emf::common::EObject* target,
                                    const std::vector<emf::common::Diagnostic>& diagnostics) = 0;
};

// 函数式 listener 包装
using ValidationListenerFn = std::function<void(emf::common::EObject*,
                                                const std::vector<emf::common::Diagnostic>&)>;

// 适配器：把 std::function 包成 IValidationListener
class FunctionalValidationListener : public IValidationListener {
public:
    explicit FunctionalValidationListener(ValidationListenerFn fn) : fn_(std::move(fn)) {}
    void validationOccurred(emf::common::EObject* target,
                            const std::vector<emf::common::Diagnostic>& diagnostics) override {
        if (fn_) fn_(target, diagnostics);
    }
private:
    ValidationListenerFn fn_;
};

// LiveValidator：将 ValidationService 包装成"挂到 EObject 树上的 Adapter"
// - 继承 EContentAdapter，attach 到 root 后自动递归到所有 containment 子对象
// - 当对象被修改（eSet / add / remove）时，notifyChanged 触发重新校验
// - 校验结果通过 IValidationListener 投递
//
// 用法：
//   auto* live = new ValidationLiveAdapter(validator);
//   live->addListener(...);
//   live->attach(root);   // 挂上 root，递归 attach 所有子对象
//   ...
//   live->detach();       // 反挂，释放
class ValidationLiveAdapter : public emf::common::EContentAdapter {
public:
    explicit ValidationLiveAdapter(EValidator& validator);
    ~ValidationLiveAdapter() override;

    // 监听器
    void addListener(IValidationListener* l);
    void removeListener(IValidationListener* l);
    void addListener(ValidationListenerFn fn);  // 函数式便捷

    // 挂到 root（对齐 Java ValidationLiveValidator.attach()）
    void attach(emf::common::EObject* root);
    // 反挂
    void detach();

    // 手动触发一次校验（不依赖 notification）
    std::vector<emf::common::Diagnostic> validateNow(emf::common::EObject* target);

    // 设置：是否在每次修改后立即重校验（默认 true）
    // false 时只保留 attachment，需要用户手动调 validateNow
    void setEnabled(bool b) { enabled_ = b; }
    bool isEnabled() const { return enabled_; }

    // 事件过滤：哪些 eventType 触发重校验（默认：SET/ADD/REMOVE）
    void setTriggerOnSet(bool b) { triggerOnSet_ = b; }
    void setTriggerOnAdd(bool b) { triggerOnAdd_ = b; }
    void setTriggerOnRemove(bool b) { triggerOnRemove_ = b; }

    // EContentAdapter 协议
    void notifyChanged(const emf::common::Notification& notification) override;

private:
    void dispatch(emf::common::EObject* target,
                  const std::vector<emf::common::Diagnostic>& diags);

    EValidator& validator_;
    std::vector<IValidationListener*> listeners_;
    // 函数式 listener 拥有权
    std::vector<std::unique_ptr<FunctionalValidationListener>> ownedListeners_;
    emf::common::EObject* attachedRoot_ = nullptr;
    bool enabled_ = true;
    bool triggerOnSet_ = true;
    bool triggerOnAdd_ = true;
    bool triggerOnRemove_ = true;
};

}  // namespace emf::validation
