// EObjectWithInverseResolvingEList.h
// 对齐 Java: org.eclipse.emf.ecore.util.EObjectWithInverseResolvingEList
// 与 EObjectWithInverseEList 的唯一区别：hasProxies() = true，
// 使代理引用在 get(i) 时被解析。反向引用维护完全继承自父类。
#pragma once

#include "emf/ecore/util/EObjectWithInverseEList.h"

namespace emf::ecore::util {

template <typename E = emf::common::EObject*>
class EObjectWithInverseResolvingEList : public EObjectWithInverseEList<E> {
public:
    EObjectWithInverseResolvingEList(emf::ecore::EClass* dataClass, emf::common::EObject* owner,
                                     int featureID, int inverseFeatureID)
        : EObjectWithInverseEList<E>(dataClass, owner, featureID, inverseFeatureID) {}

    bool hasProxies() const override { return true; }
};

// ===== Unsettable（对齐 Java EObjectWithInverseResolvingEList.Unsettable）=====
template <typename E = emf::common::EObject*>
class EObjectWithInverseResolvingEList_Unsettable : public EObjectWithInverseResolvingEList<E> {
public:
    EObjectWithInverseResolvingEList_Unsettable(emf::ecore::EClass* dataClass, emf::common::EObject* owner,
                                                 int featureID, int inverseFeatureID)
        : EObjectWithInverseResolvingEList<E>(dataClass, owner, featureID, inverseFeatureID) {}

    // ManyInverse 嵌套类（对齐 Java ...Unsettable.ManyInverse）
    class ManyInverse;

    bool isSetFlag_ = false;
    bool isSet() const override { return isSetFlag_; }

    void unset() override {
        EObjectWithInverseResolvingEList<E>::unset();
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

// ManyInverse 定义（类外，需要 Unsettable 完整定义）
template <typename E>
class EObjectWithInverseResolvingEList_Unsettable<E>::ManyInverse
    : public EObjectWithInverseResolvingEList_Unsettable<E> {
public:
    ManyInverse(emf::ecore::EClass* dataClass, emf::common::EObject* owner,
                int featureID, int inverseFeatureID)
        : EObjectWithInverseResolvingEList_Unsettable<E>(dataClass, owner, featureID, inverseFeatureID) {}
    bool hasManyInverse() const override { return true; }
};

}  // namespace emf::ecore::util
