// LiveValidator.cpp
// 对齐 org.eclipse.emf.validation.internal.service.ValidationLiveValidator
#include "emf/validation/LiveValidator.h"
#include "emf/common/ENotifier.h"
#include "emf/common/Notification.h"

#include <algorithm>
#include <utility>

namespace emf::validation {

ValidationLiveAdapter::ValidationLiveAdapter(EValidator& validator)
    : validator_(validator) {}

ValidationLiveAdapter::~ValidationLiveAdapter() {
    detach();
}

void ValidationLiveAdapter::addListener(IValidationListener* l) {
    if (l && std::find(listeners_.begin(), listeners_.end(), l) == listeners_.end()) {
        listeners_.push_back(l);
    }
}

void ValidationLiveAdapter::removeListener(IValidationListener* l) {
    listeners_.erase(std::remove(listeners_.begin(), listeners_.end(), l), listeners_.end());
    // 同时从 ownedListeners 释放
    for (auto it = ownedListeners_.begin(); it != ownedListeners_.end();) {
        if (it->get() == l) it = ownedListeners_.erase(it);
        else ++it;
    }
}

void ValidationLiveAdapter::addListener(ValidationListenerFn fn) {
    auto* l = new FunctionalValidationListener(std::move(fn));
    ownedListeners_.emplace_back(l);
    addListener(l);
}

void ValidationLiveAdapter::attach(emf::common::EObject* root) {
    if (!root) return;
    if (attachedRoot_) detach();
    attachedRoot_ = root;
    addAdapterTo(root);  // EContentAdapter：递归 attach
}

void ValidationLiveAdapter::detach() {
    if (attachedRoot_) {
        removeAdapterFrom(attachedRoot_);  // EContentAdapter：递归反挂
        attachedRoot_ = nullptr;
    }
}

std::vector<emf::common::Diagnostic> ValidationLiveAdapter::validateNow(emf::common::EObject* target) {
    if (!target) return {};
    // LIVE 模式：只执行 ConstraintMode::LIVE 约束
    // 对齐 Java ILiveValidator + EvaluationMode.LIVE
    auto diags = validator_.validate(target, ConstraintMode::LIVE);
    dispatch(target, diags);
    return diags;
}

void ValidationLiveAdapter::dispatch(emf::common::EObject* target,
                                      const std::vector<emf::common::Diagnostic>& diags) {
    // 复制一份 listeners 防止回调里 add/remove
    auto copy = listeners_;
    for (auto* l : copy) {
        if (l) l->validationOccurred(target, diags);
    }
}

void ValidationLiveAdapter::notifyChanged(const emf::common::Notification& notification) {
    // EContentAdapter 协议：自己处理 add/remove child
    EContentAdapter::notifyChanged(notification);

    if (!enabled_) return;
    // 只在 SET/ADD/REMOVE 时触发校验
    emf::common::Notification::EventType t = notification.eventType();
    bool trigger = false;
    if (triggerOnSet_    && t == emf::common::Notification::EventType::SET)    trigger = true;
    if (triggerOnAdd_    && t == emf::common::Notification::EventType::ADD)    trigger = true;
    if (triggerOnRemove_ && t == emf::common::Notification::EventType::REMOVE) trigger = true;
    if (!trigger) return;

    // notification.notifier() 是被改动的 EObject
    auto* notifier = notification.notifier();
    auto* target = dynamic_cast<emf::common::EObject*>(notifier);
    if (!target) return;
    validateNow(target);
}

}  // namespace emf::validation
