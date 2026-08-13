// EObjectContainmentWithInverseEList.h
// 对齐 Java: org.eclipse.emf.ecore.util.EObjectContainmentWithInverseEList
// containment + 可导航双向引用。与 EObjectContainmentEList 的区别：
//   - hasNavigableInverse = true（反向是可导航 EReference）
//   - 多继承 emf::common::EInverseList（注册到 owner，使对端 eInverseAdd 命中）
//   - 持有 inverseFeatureID，override getInverseFeatureID/getInverseFeatureClass
//
// didAdd/didRemove 继承自 EObjectContainmentEList（直接 setEContainer），
// 反向引用维护由 EcoreEList 基类的 inverseAdd（hasNavigableInverse=true 分支）处理。
#pragma once

#include "emf/ecore/util/EObjectContainmentEList.h"
#include "emf/common/EInverseList.h"
#include "emf/ecore/impl/BasicEObject.h"
#include "emf/ecore/DynamicEObject.h"

namespace emf::ecore::util {

template <typename E>
class EObjectContainmentWithInverseEList;

// ===== Resolving（对齐 Java ...ContainmentWithInverseEList.Resolving）=====
template <typename E = emf::common::EObject*>
class EObjectContainmentWithInverseEList_Resolving : public EObjectContainmentWithInverseEList<E> {
public:
    EObjectContainmentWithInverseEList_Resolving(emf::ecore::EClass* dataClass, emf::common::EObject* owner,
                                                  int featureID, int inverseFeatureID)
        : EObjectContainmentWithInverseEList<E>(dataClass, owner, featureID, inverseFeatureID) {}
    bool hasProxies() const override { return true; }
};

// ===== Unsettable（对齐 Java ...ContainmentWithInverseEList.Unsettable）=====
template <typename E = emf::common::EObject*>
class EObjectContainmentWithInverseEList_Unsettable : public EObjectContainmentWithInverseEList<E> {
public:
    EObjectContainmentWithInverseEList_Unsettable(emf::ecore::EClass* dataClass, emf::common::EObject* owner,
                                                   int featureID, int inverseFeatureID)
        : EObjectContainmentWithInverseEList<E>(dataClass, owner, featureID, inverseFeatureID) {}

    bool isSetFlag_ = false;
    bool isSet() const override { return isSetFlag_; }

    void unset() override {
        EObjectContainmentWithInverseEList<E>::unset();
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
    void didChange() override { isSetFlag_ = true; }
};

// ===== Unsettable.Resolving（对齐 Java ...Unsettable.Resolving）=====
template <typename E = emf::common::EObject*>
class EObjectContainmentWithInverseEList_Unsettable_Resolving
    : public EObjectContainmentWithInverseEList_Unsettable<E> {
public:
    EObjectContainmentWithInverseEList_Unsettable_Resolving(emf::ecore::EClass* dataClass,
                                                             emf::common::EObject* owner,
                                                             int featureID, int inverseFeatureID)
        : EObjectContainmentWithInverseEList_Unsettable<E>(dataClass, owner, featureID, inverseFeatureID) {}
    bool hasProxies() const override { return true; }
};

// ===== 主类：EObjectContainmentWithInverseEList<E> =====
template <typename E = emf::common::EObject*>
class EObjectContainmentWithInverseEList
    : public EObjectContainmentEList<E>, public emf::common::EInverseList {
public:
    EObjectContainmentWithInverseEList(emf::ecore::EClass* dataClass, emf::common::EObject* owner,
                                       int featureID, int inverseFeatureID)
        : EObjectContainmentEList<E>(dataClass, owner, featureID), inverseFeatureID_(inverseFeatureID) {
        // 注册到 owner 的 eInverseELists_（支持 BasicEObject 与 DynamicEObject）
        if (auto* basic = dynamic_cast<emf::ecore::impl::BasicEObject*>(this->owner_)) {
            basic->eRegisterInverseList(this->getFeatureID(), this);
        } else if (auto* dyn = dynamic_cast<emf::ecore::DynamicEObject*>(this->owner_)) {
            dyn->eRegisterInverseList(this->getFeatureID(), this);
        }
    }

    ~EObjectContainmentWithInverseEList() override {
        if (auto* basic = dynamic_cast<emf::ecore::impl::BasicEObject*>(this->owner_)) {
            basic->eUnregisterInverseList(this->getFeatureID(), this);
        } else if (auto* dyn = dynamic_cast<emf::ecore::DynamicEObject*>(this->owner_)) {
            dyn->eUnregisterInverseList(this->getFeatureID(), this);
        }
    }

    // containment + 可导航双向
    bool hasNavigableInverse() const override { return true; }

    int getInverseFeatureID() const override { return inverseFeatureID_; }
    emf::ecore::EClass* getInverseFeatureClass() const override { return this->dataClass_; }

    // EInverseList 接口：basicAdd / basicRemove（直接写 data_，跳过 didAdd 避免递归）
    void basicAdd(emf::common::EObject* otherEnd) override {
        if (!otherEnd) return;
        E eObj = static_cast<E>(otherEnd);
        this->grow(this->size_ + 1);
        this->assign(this->size_, eObj);
        ++this->size_;
    }
    void basicRemove(emf::common::EObject* otherEnd) override {
        if (!otherEnd) return;
        for (int i = 0; i < this->size_; ++i) {
            if (this->data_[static_cast<std::size_t>(i)] == static_cast<E>(otherEnd)) {
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
    int inverseFeatureID_ = -1;
};

}  // namespace emf::ecore::util
