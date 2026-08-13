// EContentAdapter 实现
// 对齐 Java: org.eclipse.emf.ecore.util.EContentAdapter
//
// 监听 containment 子树变化：notifyChanged → selfAdapt → handleContainment。
// 处理 ADD/ADD_MANY/REMOVE/REMOVE_MANY/SET/UNSET/RESOLVE 事件类型，
// 自动向新增子对象挂 adapter、从移除子对象摘 adapter。
//
// 限制：emf-common 不依赖 emf-ecore，无法调用 EReference::isContainment() 精确判断
// feature 是否为 containment。改为：对任何 EObject* 值都尝试 attach/detach，
// 依赖 addAdapterTo 的 attached_ 去重避免重复。对齐 Java 语义的精确 containment
// 过滤可在 emf-ecore-util 层子类化补充。
#include "emf/common/ENotifier.h"
#include "emf/common/EObject.h"
#include "emf/common/Notification.h"
#include <algorithm>
#include <vector>

namespace emf::common {

// ===== 通知拦截器全局注册（对齐 Java EMF Transaction 通知延迟）=====
// 事务层（emf-edit::TransactionalEditingDomain）启动时注册拦截器，
// 事务期间 eNotify 调用拦截器累积通知，事务结束后批量投递。
// 全局函数指针，无锁（单线程注册，启动时设置一次即可）。
namespace {
NotificationInterceptor gNotificationInterceptor = nullptr;
}

NotificationInterceptor getNotificationInterceptor() {
    return gNotificationInterceptor;
}

void setNotificationInterceptor(NotificationInterceptor interceptor) {
    gNotificationInterceptor = interceptor;
}

void EContentAdapter::setTarget(Notifier* n) {
    // 对齐 Java EContentAdapterImpl.setTarget 的 target 保护：
    // 仅在 target 为 null 时设置，保留首次 addAdapter 的根对象。
    // Notifier::removeAdapter 会调 setTarget(nullptr) 清空，可重新设。
    if (target_ == nullptr) {
        target_ = n;
    }
}

void EContentAdapter::notifyChanged(const Notification& notification) {
    // 对齐 Java EContentAdapter.notifyChanged → selfAdapt
    selfAdapt(notification);
}

void EContentAdapter::selfAdapt(const Notification& notification) {
    // 对齐 Java EContentAdapter.selfAdapt：
    // 仅当 notifier 是 EObject 且 feature 涉及 containment 时调 handleContainment。
    // C++ 简化：对 EObject notifier 的所有事件调 handleContainment，
    // 由 handleContainment 内部按事件类型处理 EObject* 值。
    auto* notifier = notification.notifier();
    if (!notifier) return;
    // Notifier 可能是 EObject 或 Resource；仅 EObject 参与 containment 监听
    auto* eObj = dynamic_cast<EObject*>(notifier);
    if (!eObj) return;
    // 跳过 feature==nullptr 的通知（如 EObjectImpl::setEContainer 发的 containment
    // 反向通知：notifier=child, newValue=container）。这类通知的值是父对象而非
    // containment 子对象，handleContainment 会对 REMOVE 误调 removeAdapter(父对象)
    // 导致父对象 adapter 被错误移除。正向 containment 通知 feature 非 null。
    if (notification.feature() == nullptr) return;
    handleContainment(notification);
}

void EContentAdapter::handleContainment(const Notification& notification) {
    // 对齐 Java EContentAdapter.handleContainment：
    // 按 eventType 处理 EObject* 值的 attach/detach
    using ET = Notification::EventType;
    ET et = notification.eventType();

    switch (et) {
        case ET::ADD: {
            // 单值添加：向 newValue 挂 adapter
            const std::any& nv = notification.newValue();
            if (nv.type() == typeid(EObject*)) {
                addAdapter(std::any_cast<EObject*>(nv));
            }
            break;
        }
        case ET::ADD_MANY: {
            // 多值添加：newExpectedValue 是 vector<EObject*>
            const std::any& nv = notification.newValue();
            if (nv.type() == typeid(std::vector<EObject*>)) {
                for (auto* o : std::any_cast<std::vector<EObject*>>(nv)) {
                    addAdapter(o);
                }
            }
            break;
        }
        case ET::REMOVE: {
            // 单值移除：从 oldValue 摘 adapter
            const std::any& ov = notification.oldValue();
            if (ov.type() == typeid(EObject*)) {
                removeAdapter(std::any_cast<EObject*>(ov));
            }
            break;
        }
        case ET::REMOVE_MANY: {
            const std::any& ov = notification.oldValue();
            if (ov.type() == typeid(std::vector<EObject*>)) {
                for (auto* o : std::any_cast<std::vector<EObject*>>(ov)) {
                    removeAdapter(o);
                }
            }
            break;
        }
        case ET::SET: {
            // SET：detach 旧值，attach 新值
            const std::any& ov = notification.oldValue();
            const std::any& nv = notification.newValue();
            if (ov.type() == typeid(EObject*)) {
                removeAdapter(std::any_cast<EObject*>(ov));
            }
            if (nv.type() == typeid(EObject*)) {
                addAdapter(std::any_cast<EObject*>(nv));
            }
            break;
        }
        case ET::UNSET: {
            // UNSET：detach 旧值
            const std::any& ov = notification.oldValue();
            if (ov.type() == typeid(EObject*)) {
                removeAdapter(std::any_cast<EObject*>(ov));
            }
            break;
        }
        case ET::RESOLVE: {
            // RESOLVE：代理被解析为真实对象，detach 旧 proxy，attach 新 resolved
            const std::any& ov = notification.oldValue();
            const std::any& nv = notification.newValue();
            if (ov.type() == typeid(EObject*)) {
                removeAdapter(std::any_cast<EObject*>(ov));
            }
            if (nv.type() == typeid(EObject*)) {
                addAdapter(std::any_cast<EObject*>(nv));
            }
            break;
        }
        default:
            // CREATE/MOVE/REMOVING_ADAPTER/CONTENT_TYPE：不涉及 containment attach
            break;
    }
}

void EContentAdapter::addAdapter(EObject* eObj) {
    if (!eObj) return;
    eObj->addAdapter(this);
    // 递归挂到子对象（对齐 Java setTarget(EObject) 的 useRecursion 分支）
    for (auto* c : eObj->eContents()) {
        addAdapter(c);
    }
}

void EContentAdapter::removeAdapter(EObject* eObj) {
    if (!eObj) return;
    // 对齐 Java EContentAdapterImpl.removeAdapter：仅当对象既不直接属于 Resource
    // （eDirectResource == null）也不被任何 container 持有（eInternalContainer == null）
    // 时才移除 adapter。避免跨 containment 树共享对象时误删其他路径上的 adapter。
    // C++ 用 eContainer()==null && eResource()==null 等价判断（eResource 沿链找，
    // eContainer 为 null 时 eResource 只反映 eDirectResource）。
    if (eObj->eContainer() == nullptr && eObj->eResource() == nullptr) {
        eObj->removeAdapter(this);
    }
    for (auto* c : eObj->eContents()) {
        removeAdapter(c);
    }
}

void EContentAdapter::addAdapterTo(EObject* eObj) {
    // 对齐 Java setTarget(EObject)：递归 attach 到 containment 子树
    if (!eObj) return;
    if (std::find(attached_.begin(), attached_.end(), eObj) != attached_.end()) return;
    attached_.push_back(eObj);
    eObj->addAdapter(this);
    for (auto* c : eObj->eContents()) {
        addAdapterTo(c);
    }
}

void EContentAdapter::removeAdapterFrom(EObject* eObj) {
    // 对齐 Java unsetTarget(EObject)：递归 detach
    if (!eObj) return;
    auto it = std::find(attached_.begin(), attached_.end(), eObj);
    if (it != attached_.end()) {
        attached_.erase(it);
    }
    eObj->removeAdapter(this);
    for (auto* c : eObj->eContents()) {
        removeAdapterFrom(c);
    }
}

// ===== NotificationChain::dispatch 实现 =====
// 实现在此（而非 Notification.h）是因为需要 Notifier 完整定义以调用 eNotify。
// ENotifier.h include Notification.h，本文件 include ENotifier.h，故 Notifier 完整可见。
void NotificationChain::dispatch() {
    if (dispatched_) return;
    dispatched_ = true;
    for (auto& n : notifications_) {
        if (auto* notif = n.notifier()) {
            notif->eNotify(n);
        }
    }
    notifications_.clear();
}

}  // namespace emf::common
