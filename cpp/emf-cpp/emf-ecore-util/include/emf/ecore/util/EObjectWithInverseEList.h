// EObjectWithInverseEList.h
// 对齐 Java: org.eclipse.emf.ecore.util.EObjectWithInverseEList
// 多值双向引用（非 containment）。与 EObjectEList 的区别：
//   - hasInverse = true
//   - hasNavigableInverse = true
//   - 持有 inverseFeatureID 用于 inverseAdd / inverseRemove
//   - getInverseFeatureID / getInverseFeatureClass
//
// 同时实现 emf::common::EInverseList 接口：构造时把自己注册到
// owner 的 eInverseELists_（BasicEObject 内），这样对端
// eInverseAdd(otherEnd, inverseFeatureID, ...) 能直接命中本实例并
// basicAdd 到本实例的 data_ —— 实现"两个独立 EList 共享同一份反向引用状态"。
#pragma once

#include "emf/ecore/util/EObjectEList.h"
#include "emf/common/EObject.h"
#include "emf/common/EInverseList.h"
#include "emf/ecore/EcorePackage.h"
#include "emf/ecore/impl/BasicEObject.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/ecore/DynamicEObject.h"

namespace emf::ecore::util {

template <typename E>
class EObjectWithInverseEList;

// ===== ManyInverse 嵌套类（对齐 Java EObjectWithInverseEList.ManyInverse）=====
// 必须先于主类定义，因为它在主类中使用
template <typename E>
class EObjectWithInverseEList_ManyInverse;

template <typename E = emf::common::EObject*>
class EObjectWithInverseEList : public EObjectEList<E>, public emf::common::EInverseList {
public:
    EObjectWithInverseEList(emf::ecore::EClass* dataClass, emf::common::EObject* owner,
                            int featureID, int inverseFeatureID)
        : EObjectEList<E>(dataClass, owner, featureID), inverseFeatureID_(inverseFeatureID) {
        // 注册到 owner 的 eInverseELists_ —— 把自己作为"featureID 上的具体 EList"
        // 支持 BasicEObject 和 DynamicEObject 两种 owner。
        if (auto* basic = dynamic_cast<emf::ecore::impl::BasicEObject*>(this->owner_)) {
            basic->eRegisterInverseList(this->getFeatureID(), this);
        } else if (auto* dyn = dynamic_cast<emf::ecore::DynamicEObject*>(this->owner_)) {
            dyn->eRegisterInverseList(this->getFeatureID(), this);
        }
    }

    ~EObjectWithInverseEList() override {
        // 析构时反注册
        if (auto* basic = dynamic_cast<emf::ecore::impl::BasicEObject*>(this->owner_)) {
            basic->eUnregisterInverseList(this->getFeatureID(), this);
        } else if (auto* dyn = dynamic_cast<emf::ecore::DynamicEObject*>(this->owner_)) {
            dyn->eUnregisterInverseList(this->getFeatureID(), this);
        }
    }

    bool hasInverse() const override { return true; }
    bool hasNavigableInverse() const override { return true; }

    int getInverseFeatureID() const override { return inverseFeatureID_; }
    emf::ecore::EClass* getInverseFeatureClass() const override { return this->dataClass_; }

    // EInverseList 接口：basicAdd / basicRemove
    //
    // 由 BasicEObject.eInverseAdd/eInverseRemove 在命中本实例时调用。
    // 必须直接写到 data_，不调用 didAdd（否则会再次触发 inverseAdd 循环）。
    // 对齐 Java EList 内部实现：basicAdd/basicRemove 不发通知、不做 unique 检查。
    void basicAdd(emf::common::EObject* otherEnd) override {
        if (!otherEnd) return;
        E eObj = static_cast<E>(otherEnd);
        // 直接 grow + assign + size_++，跳过 didAdd 回调
        this->grow(this->size_ + 1);
        this->assign(this->size_, eObj);
        ++this->size_;
    }
    void basicRemove(emf::common::EObject* otherEnd) override {
        if (!otherEnd) return;
        for (int i = 0; i < this->size_; ++i) {
            if (this->data_[static_cast<std::size_t>(i)] == static_cast<E>(otherEnd)) {
                // 与 BasicEList::remove 行为对齐（移动元素 + didRemove）
                this->emf::common::util::AbstractEList<E>::modCount_++;
                int shifted = this->size_ - i - 1;
                if (shifted > 0) {
                    for (int k = i; k < this->size_ - 1; ++k) {
                        this->data_[static_cast<std::size_t>(k)] =
                            this->data_[static_cast<std::size_t>(k + 1)];
                    }
                }
                this->data_[static_cast<std::size_t>(--this->size_)] = E{};
                return;
            }
        }
    }

protected:
    // didAdd 钩子：调用 EcoreEList 的 inverseAdd，驱动对端 eInverseAdd →
    // BasicEObject::eInverseAdd → basicAdd(basicAdd 命中本实例自身注册)
    //
    // 修复（原 gap：(void)msgs 丢弃返回的通知，反向端通知永不发射）：
    //   现统一用 emf::common::NotificationChain.merge() 合并并 dispatch，
    //   复用 NotificationChain::add 的 SET+SET 合并 / ADD+REMOVE 抵消语义，
    //   消除本类原有的重复合并逻辑（Gap 5 统一）。
    void didAdd(int index, const E& object) override {
        // EObjectEList 没 override didAdd；EcoreEList 也没 override。
        // 主动触发 EcoreEList.inverseAdd 以维护反向引用。
        if (!object) return;
        auto msgs = this->inverseAdd(object, {});
        dispatchChain(msgs);
    }

    // didRemove 钩子：调用 EcoreEList 的 inverseRemove，驱动对端 eInverseRemove →
    // BasicEObject::eInverseRemove → basicRemove(basicRemove 命中本实例自身注册)
    void didRemove(int index, const E& object) override {
        if (!object) return;
        auto msgs = this->inverseRemove(object, {});
        dispatchChain(msgs);
    }

    // 派发通知链（std::vector<Notification>）到 owner_。
    // 对齐 Java NotificationChainImpl.dispatch()：先做合并优化，再逐个调 notifier.eNotify。
    //
    // 统一（Gap 5）：合并逻辑复用 emf::common::NotificationChain::add，
    // 不再在本类重复实现 SET+SET 合并 / ADD+REMOVE 抵消。
    // msgs 是 EObject::EObjectNotificationChain（typedef std::vector<Notification>），
    // merge(vector<Notification>) 重载直接接收。
    void dispatchChain(std::vector<emf::common::Notification>& msgs) {
        if (msgs.empty()) return;
        emf::common::NotificationChain chain;
        chain.merge(std::move(msgs));  // merge 后 msgs 被清空，chain 累积合并后的通知
        chain.dispatch();  // 逐个调 notifier.eNotify，并标记 dispatched
    }

    int inverseFeatureID_ = -1;
};

// ===== 独立继承的 ManyInverse（对齐 Java EObjectWithInverseEList.ManyInverse）=====
template <typename E = emf::common::EObject*>
class EObjectWithInverseEList_ManyInverse : public EObjectWithInverseEList<E> {
public:
    EObjectWithInverseEList_ManyInverse(emf::ecore::EClass* dataClass, emf::common::EObject* owner,
                                        int featureID, int inverseFeatureID)
        : EObjectWithInverseEList<E>(dataClass, owner, featureID, inverseFeatureID) {}
    bool hasManyInverse() const override { return true; }
};

// ===== 独立继承的 Unsettable（对齐 Java EObjectWithInverseEList.Unsettable）=====
// 独立类命名（避免嵌套 incomplete 引用问题），与 Java 嵌套类语义对齐
template <typename E = emf::common::EObject*>
class EObjectWithInverseEList_Unsettable : public EObjectWithInverseEList<E> {
public:
    EObjectWithInverseEList_Unsettable(emf::ecore::EClass* dataClass, emf::common::EObject* owner,
                                        int featureID, int inverseFeatureID)
        : EObjectWithInverseEList<E>(dataClass, owner, featureID, inverseFeatureID) {}

    // ManyInverse 嵌套类（对齐 Java ...Unsettable.ManyInverse）
    // 前向声明，定义在类外（clang 不允许继承不完整类型）
    class ManyInverse;

    bool isSetFlag_ = false;
    bool isSet() const override { return isSetFlag_; }

    void unset() override {
        EObjectWithInverseEList<E>::unset();
        if (this->isNotificationRequired()) {
            bool oldIsSet = isSetFlag_;
            isSetFlag_ = false;
            this->dispatchNotification(this->createNotification(
                static_cast<int>(emf::common::Notification::EventType::UNSET),
                oldIsSet, false));
        } else {
            isSetFlag_ = false;
        }
    }

protected:
    void didChange() override {
        isSetFlag_ = true;
    }
};

// ManyInverse 定义（在类外，因为需要 EObjectWithInverseEList_Unsettable 完整定义）
template <typename E>
class EObjectWithInverseEList_Unsettable<E>::ManyInverse : public EObjectWithInverseEList_Unsettable<E> {
public:
    ManyInverse(emf::ecore::EClass* dataClass, emf::common::EObject* owner,
                int featureID, int inverseFeatureID)
        : EObjectWithInverseEList_Unsettable<E>(dataClass, owner, featureID, inverseFeatureID) {}
    bool hasManyInverse() const override { return true; }
};

}  // namespace emf::ecore::util
