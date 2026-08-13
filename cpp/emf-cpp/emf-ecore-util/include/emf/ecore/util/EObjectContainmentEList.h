// EObjectContainmentEList.h
// 对齐 Java: org.eclipse.emf.ecore.util.EObjectContainmentEList
// 多值 containment 引用列表（子对象归 owner 所有）。
//
// 与 EObjectEList 的区别：
//   - isContainment = true
//   - hasInverse    = true（反向是 container）
//   - hasNavigableInverse = false（反向不可导航，用 EOPPOSITE_FEATURE_BASE 协议）
//   - didAdd/didRemove 直接设置/清除子对象 eContainer + eContainingFeature
//
// 注意：BasicEObject::eInverseAdd 不识别 EOPPOSITE_FEATURE_BASE（负 featureID），
// 故 didAdd 不能仅靠 inverseAdd→eInverseAdd 来设 container，必须直接 setEContainer。
#pragma once

#include "emf/ecore/util/EObjectEList.h"
#include "emf/common/EObject.h"
#include "emf/ecore/EcorePackage.h"
#include "emf/ecore/impl/BasicEObject.h"

namespace emf::ecore::util {

template <typename E>
class EObjectContainmentEList;

// ===== Resolving（对齐 Java EObjectContainmentEList.Resolving）=====
template <typename E = emf::common::EObject*>
class EObjectContainmentEList_Resolving : public EObjectContainmentEList<E> {
public:
    EObjectContainmentEList_Resolving(emf::ecore::EClass* dataClass, emf::common::EObject* owner, int featureID)
        : EObjectContainmentEList<E>(dataClass, owner, featureID) {}
    bool hasProxies() const override { return true; }
};

// ===== Unsettable（对齐 Java EObjectContainmentEList.Unsettable）=====
template <typename E = emf::common::EObject*>
class EObjectContainmentEList_Unsettable : public EObjectContainmentEList<E> {
public:
    EObjectContainmentEList_Unsettable(emf::ecore::EClass* dataClass, emf::common::EObject* owner, int featureID)
        : EObjectContainmentEList<E>(dataClass, owner, featureID) {}

    bool isSetFlag_ = false;
    bool isSet() const override { return isSetFlag_; }

    void unset() override {
        EObjectContainmentEList<E>::unset();
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

// ===== Unsettable.Resolving（对齐 Java EObjectContainmentEList.Unsettable.Resolving）=====
template <typename E = emf::common::EObject*>
class EObjectContainmentEList_Unsettable_Resolving : public EObjectContainmentEList_Unsettable<E> {
public:
    EObjectContainmentEList_Unsettable_Resolving(emf::ecore::EClass* dataClass, emf::common::EObject* owner, int featureID)
        : EObjectContainmentEList_Unsettable<E>(dataClass, owner, featureID) {}
    bool hasProxies() const override { return true; }
};

// ===== 主类：EObjectContainmentEList<E> =====
template <typename E = emf::common::EObject*>
class EObjectContainmentEList : public EObjectEList<E> {
public:
    EObjectContainmentEList(emf::ecore::EClass* dataClass, emf::common::EObject* owner, int featureID)
        : EObjectEList<E>(dataClass, owner, featureID) {}

    bool isContainment() const override { return true; }
    bool hasInverse() const override { return true; }
    // hasNavigableInverse 默认 false（继承）—— containment 的反向用 EOPPOSITE_FEATURE_BASE

protected:
    // didAdd：直接设置子对象 eContainer + eContainingFeature
    // （BasicEObject::eInverseAdd 不处理 EOPPOSITE_FEATURE_BASE，故直接设）
    // setEContainer/setEContainingFeature 在 EObjectImpl 中声明，需 cast 到 EObjectImpl。
    void didAdd(int index, const E& object) override {
        (void)index;
        if (!object) return;
        auto* obj = static_cast<emf::common::EObject*>(object);
        if (auto* impl = dynamic_cast<emf::common::EObjectImpl*>(obj)) {
            impl->setEContainer(this->owner_);
            impl->setEContainingFeature(this->getEStructuralFeature());
        }
    }

    // didRemove：清除子对象 eContainer + eContainingFeature
    void didRemove(int index, const E& object) override {
        (void)index;
        if (!object) return;
        auto* obj = static_cast<emf::common::EObject*>(object);
        if (auto* impl = dynamic_cast<emf::common::EObjectImpl*>(obj)) {
            impl->setEContainer(nullptr);
            impl->setEContainingFeature(nullptr);
        }
    }
};

}  // namespace emf::ecore::util
