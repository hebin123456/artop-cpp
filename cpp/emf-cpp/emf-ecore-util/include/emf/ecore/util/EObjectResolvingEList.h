// EObjectResolvingEList.h
// 对齐 Java: org.eclipse.emf.ecore.util.EObjectResolvingEList
// 与 EObjectEList 的唯一区别是 hasProxies() = true，
// 因此 EObject 上的代理引用在 get(i) 时会被解析。
#pragma once

#include "emf/ecore/util/EObjectEList.h"
#include "emf/common/EObject.h"
#include "emf/ecore/EcorePackage.h"

namespace emf::ecore::util {

template <typename E = emf::common::EObject*>
class EObjectResolvingEList : public EObjectEList<E> {
public:
    EObjectResolvingEList(emf::ecore::EClass* dataClass, emf::common::EObject* owner, int featureID)
        : EObjectEList<E>(dataClass, owner, featureID) {}

    bool hasProxies() const override { return true; }
};

// ===== Unsettable（对齐 Java EObjectResolvingEList.Unsettable）=====
template <typename E = emf::common::EObject*>
class EObjectResolvingEList_Unsettable : public EObjectEList<E> {
public:
    EObjectResolvingEList_Unsettable(emf::ecore::EClass* dataClass, emf::common::EObject* owner, int featureID)
        : EObjectEList<E>(dataClass, owner, featureID) {}

    bool hasProxies() const override { return true; }
    bool isSetFlag_ = false;
    bool isSet() const override { return isSetFlag_; }

    void unset() override {
        EObjectEList<E>::unset();
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

}  // namespace emf::ecore::util
