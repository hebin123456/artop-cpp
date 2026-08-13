// EMF Common: ENotifier / EAdapter / EContentAdapter
// 对齐 org.eclipse.emf.common.notify.Notifier, Adapter
//      org.eclipse.emf.ecore.util.EContentAdapter
#pragma once

#include "Notification.h"
#include <vector>
#include <memory>
#include <algorithm>
#include <any>

namespace emf::common {

class EObject;

// ===== 通知拦截器钩子（对齐 Java EMF Transaction 的通知延迟机制）=====
// 事务层（emf-edit::TransactionalEditingDomain）注册拦截器，事务期间累积通知，
// 事务结束后批量投递。emf-common 不依赖 emf-edit，通过函数指针解耦。
// 拦截器返回 true 表示已处理（累积），返回 false 表示未拦截（正常投递）。
using NotificationInterceptor = bool(*)(const Notifier*, const Notification&);
NotificationInterceptor getNotificationInterceptor();
void setNotificationInterceptor(NotificationInterceptor interceptor);

class EAdapter {
public:
    virtual ~EAdapter() = default;
    virtual void notifyChanged(const Notification& notification) = 0;
    virtual Notifier* getTarget() { return target_; }
    virtual void setTarget(Notifier* n) { target_ = n; }
    virtual bool isAdapterForType(Notifier* /*other*/) { return false; }

protected:
    Notifier* target_ = nullptr;
};

class Notifier {
public:
    virtual ~Notifier() = default;

    void addAdapter(class EAdapter* adapter) {
        // 对齐 Java BasicNotifierImpl.eBasicAddAdapter：先去重加入 eAdapters，
        // 再调 adapter.setTarget(this) 联动 target（EContentAdapter 等依赖此回调递归 attach）。
        if (!adapter) return;
        if (std::find(adapters_.begin(), adapters_.end(), adapter) == adapters_.end()) {
            adapters_.push_back(adapter);
        }
        adapter->setTarget(this);
    }

    void removeAdapter(class EAdapter* adapter) {
        // 对齐 Java BasicNotifierImpl.eBasicRemoveAdapter：发 REMOVING_ADAPTER 通知，
        // 从 eAdapters 移除，并清空 adapter 的 target（避免悬空指针）。
        if (!adapter) return;
        Notification n(Notification::EventType::REMOVING_ADAPTER, this, nullptr, -1, adapter, nullptr);
        // 对齐 Java BasicNotifierImpl.eBasicRemoveAdapter：通过 eNotify 投递，
        // eDeliver=false 时不发 REMOVING_ADAPTER 通知给其他 adapter。
        eNotify(n);
        adapters_.erase(std::remove(adapters_.begin(), adapters_.end(), adapter), adapters_.end());
        adapter->setTarget(nullptr);
    }

    const std::vector<class EAdapter*>& eAdapters() const { return adapters_; }
    std::vector<class EAdapter*>& eAdapters() { return adapters_; }

    // 对齐 Java Notifier.eDeliver / eSetDeliver：
    // 控制 eNotify 时是否真正把 notification 投递给 adapters。
    // Java 默认 eDeliver = true；C++ 端用同样默认值。
    bool eDeliver() const { return deliver_; }
    void eSetDeliver(bool deliver) { deliver_ = deliver; }

    void eNotify(const Notification& n) {
        // 对齐 Java BasicNotifierImpl.eNotify：在 eDeliver = false 时直接跳过
        // 整次 notify 循环（含 REMOVING_ADAPTER 触发的隐式 notify 也不投递）。
        if (!deliver_) return;
        // 事务通知延迟：若事务层注册了拦截器且当前在事务中，累积通知（不直接投递）
        // 对齐 Java EMF Transaction 的通知延迟机制
        auto* interceptor = getNotificationInterceptor();
        if (interceptor && interceptor(this, n)) {
            return;  // 已被事务层累积处理
        }
        notify(n);
    }

protected:
    void notify(const Notification& n) {
        // 对齐 Java BasicNotifierImpl.eNotify：快照迭代，不二次确认。
        // copy 保证 notifyChanged 回调中 addAdapter/removeAdapter 修改 adapters_ 时迭代安全。
        // 去掉原 O(n) find 确认（原代码对每 adapter 做 find 导致 O(n²)，adapter 多时显著）：
        //   - copy 中的 adapter 指针仍有效（removeAdapter 只从 list 移除，不析构 adapter）
        //   - 已从 list 移除的 adapter 仍会收到本次通知，这与 Java 快照语义一致
        //   - 调用方若在 notifyChanged 中 delete adapter，属误用（Java 同样不保证）
        auto copy = adapters_;
        for (auto* a : copy) {
            a->notifyChanged(n);
        }
    }

private:
    std::vector<class EAdapter*> adapters_;
    bool deliver_ = true;
};

// EContentAdapter: 递归挂到 containment 树
// 对齐 Java org.eclipse.emf.ecore.util.EContentAdapter
// 监听 containment 变化：当 containment feature 的子对象增删时，自动 attach/detach adapter。
// 使用：eObject->eAdapters().add(adapter) 后，adapter 会递归挂到所有 containment 子对象；
// 子对象增删时自动维护。
class EContentAdapter : public EAdapter {
public:
    // 通知入口：对齐 Java EContentAdapter.notifyChanged → selfAdapt
    void notifyChanged(const Notification& notification) override;

    // setTarget override：对齐 Java EContentAdapterImpl.setTarget 的 target 保护语义。
    // 仅在 target 为 null 时设置（保留首次 addAdapter 的根对象作为 target），
    // 避免递归 attach 子对象（子对象也会调 Notifier::addAdapter 触发 setTarget）时
    // 覆盖根 target。removeAdapter 时 Notifier 会调 setTarget(nullptr) 清空，可重新设。
    void setTarget(Notifier* n) override;

    // 手动挂到 EObject 的 containment 子树（对齐 Java setTarget(EObject) 的核心逻辑）
    void addAdapterTo(EObject* eObj);
    // 手动从 EObject 的 containment 子树移除（对齐 Java unsetTarget(EObject)）
    void removeAdapterFrom(EObject* eObj);

    // 已 attach 的 EObject 列表
    const std::vector<EObject*>& attached() const { return attached_; }

protected:
    // 自适应：按 notification 类型决定是否需要 handleContainment
    // 对齐 Java EContentAdapter.selfAdapt（virtual 供 emf-ecore-util 层精确过滤子类 override）
    virtual void selfAdapt(const Notification& notification);

    // 处理 containment 变化：ADD/ADD_MANY/REMOVE/REMOVE_MANY/SET/UNSET/RESOLVE
    // 对齐 Java EContentAdapter.handleContainment（virtual 供子类 override）
    virtual void handleContainment(const Notification& notification);

    // 内部：向 newValue 对象挂 adapter
    void addAdapter(EObject* eObj);
    // 内部：从 oldValue 对象移除 adapter
    void removeAdapter(EObject* eObj);

private:
    std::vector<EObject*> attached_;
};

}  // namespace emf::common
