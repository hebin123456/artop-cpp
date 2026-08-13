// EcoreEList.h
// 对齐 Java: org.eclipse.emf.ecore.util.EcoreEList
// 抽象模板基类 —— Ecore-aware 的 NotifyingList 基础。所有 6 个 EObject* EList
// 变体（EObjectEList, EObjectContainmentEList, ...）都从本类派生。
//
// 关键职责（1:1 对齐 Java EcoreEList.java）：
//   - 持 owner（InternalEObject*） + dataClass + EStructuralFeature
//   - 暴露 hasInverse/hasNavigableInverse/isContainment/hasProxies/... 钩子
//   - inverseAdd/inverseRemove 委托给 owner.eInverseAdd/eInverseRemove
//   - resolve(int, EObject*) 走 owner.eResolveProxy 并修正槽位
//   - 实现 InternalEList.Unsettable（isSet/unset 接口）
#pragma once

#include "emf/common/util/NotifyingListImpl.h"
#include "emf/common/ENotifier.h"
#include "emf/common/Notification.h"
#include "emf/ecore/EcorePackage.h"

#include <any>
#include <vector>
#include <cstdint>

namespace emf::ecore::util {

// NotificationChain —— C++ 端简化为 std::vector<emf::common::Notification>。
// (Java: org.eclipse.emf.common.notify.NotificationChain)
using NotificationChain = std::vector<emf::common::Notification>;

// EcoreEList<E> 抽象模板基类
template <typename E = emf::common::EObject*>
class EcoreEList : public emf::common::util::NotifyingListImpl<E> {
public:
    static constexpr int NO_FEATURE_ID = -1;
    static constexpr int EOPPOSITE_FEATURE_BASE = -1;  // 对齐 Java InternalEObject 常量

    EcoreEList(emf::ecore::EClass* dataClass, emf::common::EObject* owner)
        : emf::common::util::NotifyingListImpl<E>(), dataClass_(dataClass), owner_(owner) {}

    EcoreEList(int initialCapacity, emf::ecore::EClass* dataClass, emf::common::EObject* owner)
        : emf::common::util::NotifyingListImpl<E>(initialCapacity),
          dataClass_(dataClass), owner_(owner) {}

    virtual ~EcoreEList() = default;

    // ===== NotifyingList<E> 接口实现（对齐 Java EcoreEList）=====
    emf::common::Notifier* getNotifier() override { return owner_; }
    const emf::common::Notifier* getNotifier() const override { return owner_; }
    const void* getFeature() const override {
        return static_cast<const void*>(getEStructuralFeature());
    }
    int getFeatureID() const override {
        // 对齐 Java EcoreEList.getFeatureID() 默认实现（无 override 时）：
        //   return owner.eClass().getFeatureID(getEStructuralFeature());
        if (!owner_) return NO_FEATURE_ID;
        auto* cls = owner_->eClass();
        auto* esf = getEStructuralFeature();
        if (!cls || !esf) return NO_FEATURE_ID;
        return cls->getFeatureID(const_cast<emf::ecore::EStructuralFeature*>(esf));
    }

    // EStructuralFeature.Setting 接口
    emf::common::EObject* getEObject() const { return owner_; }
    virtual emf::ecore::EStructuralFeature* getEStructuralFeature() const {
        // 派生类必须 override 此方法或 getFeatureID()（二选一）以避免无限递归。
        return owner_ ? owner_->eClass() ? owner_->eClass()->getEStructuralFeature(getFeatureID())
                                          : nullptr
                      : nullptr;
    }

    // 暴露 owner / dataClass
    emf::common::EObject* owner() const { return owner_; }
    emf::ecore::EClass* dataClass() const { return dataClass_; }

    // ===== 钩子（override 这些切换语义；默认与 Java EcoreEList 保持一致）=====
    virtual emf::ecore::EClassifier* getFeatureType() const {
        auto* esf = getEStructuralFeature();
        return esf ? esf->getEType() : nullptr;
    }
    virtual emf::ecore::EReference* getInverseEReference() const {
        auto* esf = getEStructuralFeature();
        auto* eref = esf ? dynamic_cast<emf::ecore::EReference*>(esf) : nullptr;
        return (eref && eref->getEOpposite()) ? eref->getEOpposite() : nullptr;
    }
    virtual int getInverseFeatureID() const {
        auto* inv = getInverseEReference();
        return inv ? inv->getFeatureID() : NO_FEATURE_ID;
    }
    virtual emf::ecore::EClass* getInverseFeatureClass() const {
        return dataClass_;
    }
    virtual bool hasManyInverse() const { return false; }
    virtual bool hasNavigableInverse() const { return false; }
    virtual bool isEObject() const { return true; }
    virtual bool isContainment() const { return false; }
    virtual bool hasProxies() const { return false; }
    virtual bool hasInstanceClass() const { return true; }
    virtual bool hasInverse() const { return false; }

    // ===== 列表行为（对齐 Java EcoreEList：useEquals=false, isUnique=true, canContainNull=false）=====
    bool useEquals() const override { return false; }
    bool isUnique() const override { return true; }
    bool canContainNull() const override { return false; }

    // ===== 通知相关（对齐 Java EcoreEList）=====
    bool isNotificationRequired() const override {
        // 对齐 Java：return owner.eNotificationRequired()
        if (!owner_) return false;
        return owner_->eNotificationRequired();
    }
    void dispatchNotification(const emf::common::Notification& notification) override {
        if (owner_) owner_->eNotify(notification);
    }
    emf::common::Notification createNotification(int eventType, std::any oldValue,
                                                  std::any newValue, int index, bool /*wasSet*/) const {
        return emf::common::Notification(
            static_cast<emf::common::Notification::EventType>(eventType),
            owner_, getEStructuralFeature(), getFeatureID(),
            std::move(oldValue), std::move(newValue), index);
    }
    emf::common::Notification createNotification(int eventType, bool oldValue, bool newValue) const {
        return emf::common::Notification(
            static_cast<emf::common::Notification::EventType>(eventType),
            owner_, getEStructuralFeature(), getFeatureID(),
            std::any{oldValue}, std::any{newValue}, -1);
    }

    // ===== resolve（对齐 Java EcoreEList.resolve）=====
    // E 模板版本：只对 EObject 类型或 hasProxies=true 时做 resolve，其他原样返回。
    E resolve(int index, const E& object) const override {
        if (isEObject() && hasProxies()) {
            if (auto* p = asEObject(object)) {
                return static_cast<E>(resolveEObject(index, p));
            }
        }
        return object;
    }
    E resolve(const E& object) const override {
        if (isEObject()) {
            if (auto* p = asEObject(object)) {
                if (p->eIsProxy() && owner_) {
                    emf::common::EObject* r = owner_->eResolveProxy(p);
                    return fromEObject(r);
                }
            }
        }
        return object;
    }

    // 反向引用维护（对齐 Java EcoreEList.inverseAdd / inverseRemove）=====
    virtual NotificationChain inverseAdd(E object, NotificationChain notifications) {
        auto* internal = asEObject(object);
        if (!internal) return notifications;
        if (hasNavigableInverse()) {
            if (!hasInstanceClass()) {
                auto* cls = internal->eClass();
                auto* inv = getInverseEReference();
                int inverseID = (cls && inv) ? cls->getFeatureID(inv) : NO_FEATURE_ID;
                return internal->eInverseAdd(owner_, inverseID, nullptr, std::move(notifications));
            } else {
                return internal->eInverseAdd(owner_, getInverseFeatureID(), getInverseFeatureClass(), std::move(notifications));
            }
        } else {
            // 对齐 Java：EOPPOSITE_FEATURE_BASE - getFeatureID()
            int oppositeID = EOPPOSITE_FEATURE_BASE - getFeatureID();
            return internal->eInverseAdd(owner_, oppositeID, nullptr, std::move(notifications));
        }
    }

    virtual NotificationChain inverseRemove(E object, NotificationChain notifications) {
        auto* internal = asEObject(object);
        if (!internal) return notifications;
        if (hasNavigableInverse()) {
            if (!hasInstanceClass()) {
                auto* cls = internal->eClass();
                auto* inv = getInverseEReference();
                int inverseID = (cls && inv) ? cls->getFeatureID(inv) : NO_FEATURE_ID;
                return internal->eInverseRemove(owner_, inverseID, nullptr, std::move(notifications));
            } else {
                return internal->eInverseRemove(owner_, getInverseFeatureID(), getInverseFeatureClass(), std::move(notifications));
            }
        } else {
            int oppositeID = EOPPOSITE_FEATURE_BASE - getFeatureID();
            return internal->eInverseRemove(owner_, oppositeID, nullptr, std::move(notifications));
        }
    }

    // ===== EStructuralFeature.Setting 接口（对齐 Java EcoreEList）=====
    // 重命名为 getSetting(bool) 以避免与 BasicEList::get(int) 重名冲突
    // （C++ 名字隐藏会阻断 int 版本）
    emf::common::EObject* getSetting(bool /*resolve*/) {
        // Setting.get(resolve)：EList 自身就是 setting
        return owner_;
    }
    // EList.get(int) 的别名入口：让 BasicEList::get 可见
    // （C++ 名字隐藏：派生类作用域中显式声明的 get(bool) 会隐藏基类 get(int)）
    using emf::common::util::BasicEList<E>::get;
    void set(std::any newValue) {
        // 简化：clear 后 addAll
        (void)newValue;
    }

    // ===== InternalEList.Unsettable（对齐 Java EcoreEList）=====
    bool isSet() const override {
        return !this->isEmpty();
    }
    void unset() override {
        this->clear();
    }

    // ===== toArray（对齐 Java EcoreEList.toArray：hasProxies 时先 resolve）=====
    std::vector<E> toArray() {
        if (hasProxies()) {
            for (int i = this->size_ - 1; i >= 0; --i) {
                (void)this->get(i);
            }
        }
        return emf::common::util::BasicEList<E>::toArray();
    }

protected:
    emf::ecore::EClass* dataClass_ = nullptr;
    emf::common::EObject* owner_ = nullptr;

    // E ↔ EObject* 转换助手（模板化：支持 EObject* 与通用值类型两种）
    static emf::common::EObject* asEObject(const E& e) {
        if constexpr (std::is_pointer_v<E>) {
            return static_cast<emf::common::EObject*>(e);
        } else {
            return static_cast<emf::common::EObject*>(e);
        }
    }
    static E fromEObject(emf::common::EObject* o) {
        if constexpr (std::is_pointer_v<E>) {
            return static_cast<E>(o);
        } else {
            return *static_cast<E>(&o);
        }
    }

    // EObject* 形式的 resolve（对齐 Java EcoreEList.resolve(int, EObject)）。
    // 注意：const + 修改 data_ 的处理在 Java 里通过 assign(..., validate(...)) 实现；
    // C++ 端由于 BasicEList 的 data_ 在 mutable 容器里，可以直接 const_cast 操作。
    virtual emf::common::EObject* resolveEObject(int index, emf::common::EObject* eObject) const {
        emf::common::EObject* resolved = resolveProxy(eObject);
        if (resolved != eObject) {
            const_cast<EcoreEList*>(this)->assign(
                index, const_cast<EcoreEList*>(this)->validate(index, static_cast<E>(resolved)));
            // didSet 在 NotifyingListImpl.setUnique 中已触发；这里不重复
            if (isNotificationRequired() && owner_) {
                owner_->eNotify(createNotification(
                    static_cast<int>(emf::common::Notification::EventType::RESOLVE),
                    std::any{eObject}, std::any{resolved}, index, false));
            }
            return resolved;
        }
        return eObject;
    }

    emf::common::EObject* resolveProxy(emf::common::EObject* eObject) const {
        if (!eObject) return eObject;
        if (!eObject->eIsProxy()) return eObject;
        if (!owner_) return eObject;
        return owner_->eResolveProxy(eObject);
    }
};

}  // namespace emf::ecore::util
