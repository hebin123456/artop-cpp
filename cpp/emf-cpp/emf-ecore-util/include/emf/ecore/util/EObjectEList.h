// EObjectEList.h
// 对齐 Java: org.eclipse.emf.ecore.util.EObjectEList
// 多值 EObject 引用列表（非 containment、非 resolving），是其他 5 个变体的
// 直接父类。
//
// 关键语义：
//   - useEquals = false   (EObject 按指针比较)
//   - isUnique  = true    (不可重复)
//   - isEObject = true
//   - hasProxies = false
//   - hasInverse/hasNavigableInverse/isContainment = false
//   - resolve(int, E) 直接返回 object (EObjectEList 不解析代理)
#pragma once

#include "emf/ecore/util/EcoreEList.h"
#include "emf/common/ENotifier.h"
#include "emf/common/Notification.h"
#include "emf/common/EObject.h"
#include "emf/ecore/EcorePackage.h"

#include <any>
#include <vector>

namespace emf::ecore::util {

template <typename E>
class EObjectEList;

// ===== Unsettable（对齐 Java EObjectEList.Unsettable）=====
// 独立类（避免嵌套 incomplete 引用问题）。默认参数 E = emf::common::EObject*。
template <typename E = emf::common::EObject*>
class EObjectEList_Unsettable : public EcoreEList<E> {
public:
    EObjectEList_Unsettable(emf::ecore::EClass* dataClass, emf::common::EObject* owner, int featureID)
        : EcoreEList<E>(dataClass, owner), featureID_(featureID) {}

    int getFeatureID() const override { return featureID_; }
    bool isSetFlag_ = false;
    bool isSet() const override { return isSetFlag_; }

    void unset() override {
        EcoreEList<E>::unset();
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
    int featureID_ = -1;
};

// ===== 主类：EObjectEList<E> =====
template <typename E = emf::common::EObject*>
class EObjectEList : public EcoreEList<E> {
public:
    EObjectEList(emf::ecore::EClass* dataClass, emf::common::EObject* owner, int featureID)
        : EcoreEList<E>(dataClass, owner), featureID_(featureID) {}

    int getFeatureID() const override { return featureID_; }

    bool useEquals() const override { return false; }
    bool isUnique() const override { return true; }
    bool hasInverse() const override { return false; }
    bool isEObject() const override { return true; }
    bool canContainNull() const override { return false; }

protected:
    // EObjectEList 不做代理解析——但仍要走 EcoreEList::resolveEObject（让
    // EObjectResolvingEList 通过 hasProxies()=true 接入）。Java 端
    // EObjectEList.resolve 走 EcoreEList 默认；C++ 这里不要截断。
    // 注意：hasProxies() 默认 false，所以 EObjectEList 上不会真的解析；
    //       EObjectResolvingEList 覆盖 hasProxies()=true 后，会走
    //       EcoreEList::resolveEObject（带 owner.eResolveProxy 协议）。

public:  // 让 EObjectWithInverseEList 也能访问 featureID_
    int featureID_ = -1;
};

}  // namespace emf::ecore::util
