// BasicEObject.cpp —— impl::BasicEObject 实现
// 对齐 org.eclipse.emf.ecore.impl.BasicEObjectImpl（反向引用维护 + 动态值存储子集）
#include "emf/ecore/impl/BasicEObject.h"
#include "emf/ecore/EcoreImpls.h"

namespace emf::ecore::impl {

BasicEObject::~BasicEObject() = default;

// ===== 反向引用注册表 =====
void BasicEObject::eRegisterInverseList(int featureID,
                                         emf::common::EInverseList* list) {
    if (!list) return;
    eInverseELists_[featureID] = list;
}

void BasicEObject::eUnregisterInverseList(int featureID,
                                           emf::common::EInverseList* list) {
    auto it = eInverseELists_.find(featureID);
    if (it != eInverseELists_.end() && it->second == list) {
        eInverseELists_.erase(it);
    }
}

// ===== 反向引用维护 =====
// 对齐 Java BasicEObjectImpl.eInverseAdd：查找 featureID 对应的反向 EList，
// 命中则直接 basicAdd(otherEnd)（不发通知、不触发 didAdd 回调，避免递归）。
// 修复：
//   1.（原 gap：累积的 notifications 被丢弃）现追加反向端 ADD/REMOVE 通知，
//      由调用方（EObjectWithInverseEList::didAdd）统一 dispatch。
//   2.（原 gap：feature=nullptr 导致 EcoreContentAdapter 退化、ECrossReferenceAdapter 丢弃）
//      现用 eClass()->getEStructuralFeature(featureID) 反查真实 feature，
//      使 containment 精确过滤与跨引用索引生效。
emf::common::EObject::EObjectNotificationChain BasicEObject::eInverseAdd(
    emf::common::EObject* otherEnd, int featureID,
    emf::ecore::EClass* /*inverseFeatureClass*/,
    emf::common::EObject::EObjectNotificationChain notifications) {
    auto it = eInverseELists_.find(featureID);
    if (it != eInverseELists_.end() && it->second) {
        it->second->basicAdd(otherEnd);
        if (eNotificationRequired()) {
            auto* sf = (featureID >= 0) ? eClass()->getEStructuralFeature(featureID) : nullptr;
            notifications.emplace_back(
                emf::common::Notification::EventType::ADD,
                this, sf, featureID,
                std::any(), std::any(static_cast<emf::common::EObject*>(otherEnd)), -1);
        }
    }
    return notifications;
}

emf::common::EObject::EObjectNotificationChain BasicEObject::eInverseRemove(
    emf::common::EObject* otherEnd, int featureID,
    emf::ecore::EClass* /*inverseFeatureClass*/,
    emf::common::EObject::EObjectNotificationChain notifications) {
    auto it = eInverseELists_.find(featureID);
    if (it != eInverseELists_.end() && it->second) {
        it->second->basicRemove(otherEnd);
        if (eNotificationRequired()) {
            auto* sf = (featureID >= 0) ? eClass()->getEStructuralFeature(featureID) : nullptr;
            notifications.emplace_back(
                emf::common::Notification::EventType::REMOVE,
                this, sf, featureID,
                std::any(static_cast<emf::common::EObject*>(otherEnd)), std::any(), -1);
        }
    }
    return notifications;
}

// ===== 动态值存储（featureID -> std::any）=====
std::any BasicEObject::eDynamicGet(const emf::ecore::EStructuralFeature* feature) const {
    if (!feature) return std::any{};
    int fid = feature->getFeatureID();
    auto it = eDynamicSettings_.find(fid);
    if (it != eDynamicSettings_.end()) return it->second;
    return std::any{};
}

void BasicEObject::eDynamicSet(const emf::ecore::EStructuralFeature* feature, std::any value) {
    if (!feature) return;
    int fid = feature->getFeatureID();
    eDynamicSettings_[fid] = std::move(value);
    eDynamicSetFlags_.insert(fid);
}

bool BasicEObject::eDynamicIsSet(const emf::ecore::EStructuralFeature* feature) const {
    if (!feature) return false;
    return eDynamicSetFlags_.count(feature->getFeatureID()) > 0;
}

void BasicEObject::eDynamicUnset(const emf::ecore::EStructuralFeature* feature) {
    if (!feature) return;
    int fid = feature->getFeatureID();
    eDynamicSettings_.erase(fid);
    eDynamicSetFlags_.erase(fid);
}

// ===== eGet/eSet/eIsSet/eUnset(EStructuralFeature*) =====
// 对齐 Java BasicEObjectImpl：委托 eDynamic* 读写，eSet/eUnset 发 SET/UNSET 通知。
// 注意 oldValue 经 eDynamicGet 捕获（不依赖 EObjectImpl 的空 eGet）。
std::any BasicEObject::eGet(const emf::ecore::EStructuralFeature* feature) const {
    return eDynamicGet(feature);
}

std::any BasicEObject::eGet(const emf::ecore::EStructuralFeature* feature, bool /*resolve*/) const {
    return eDynamicGet(feature);
}

void BasicEObject::eSet(const emf::ecore::EStructuralFeature* feature, std::any value) {
    if (!feature) return;
    bool notify = eNotificationRequired();
    std::any oldValue;
    if (notify) oldValue = eDynamicGet(feature);
    eDynamicSet(feature, value);  // 存储副本（eDynamicSet 按值接收并 move 入存储）
    if (notify) {
        emf::common::Notification n(
            emf::common::Notification::EventType::SET, this,
            feature, feature->getFeatureID(),
            std::move(oldValue), std::move(value));
        eNotify(n);
    }
}

bool BasicEObject::eIsSet(const emf::ecore::EStructuralFeature* feature) const {
    return eDynamicIsSet(feature);
}

void BasicEObject::eUnset(const emf::ecore::EStructuralFeature* feature) {
    if (!feature) return;
    bool notify = eNotificationRequired();
    std::any oldValue;
    if (notify) oldValue = eDynamicGet(feature);
    eDynamicUnset(feature);
    if (notify) {
        emf::common::Notification n(
            emf::common::Notification::EventType::UNSET, this,
            feature, feature->getFeatureID(),
            std::move(oldValue), std::any());
        eNotify(n);
    }
}

}  // namespace emf::ecore::impl
