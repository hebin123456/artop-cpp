// DynamicEObject.cpp —— 动态模型对象实现
// 对齐 org.eclipse.emf.ecore.impl.DynamicEObjectImpl
// 元数据驱动：所有 feature 访问经 eClass() 反射，值存于 BasicEObject 动态存储。
//
// 存储策略（对齐 Java DynamicEObjectImpl）：
//   - 单值 feature：eDynamicSettings_[featureID]（vector 按 ID 索引，高性能）
//   - 多值 feature：eLists_[featureID]（共享 EList 指针）
//   - featureID 由 EClassImpl::addEStructuralFeature 按 eAllSuperTypes feature 数偏移分配，
//     保证 eAllStructuralFeatures 中每个 featureID 唯一（对齐 Java）。
#include "emf/ecore/DynamicEObject.h"
#include "emf/ecore/EcorePackage.h"
#include "emf/common/Notification.h"
#include <stdexcept>

namespace emf::ecore {

DynamicEObject::DynamicEObject(EClass* eClass) : eClass_(eClass) {}

DynamicEObject::~DynamicEObject() = default;

// 按 featureID 查找 EStructuralFeature（经 eClass 反射）
const EStructuralFeature* DynamicEObject::featureByID(int featureID) const {
    if (!eClass_ || featureID < 0) return nullptr;
    return eClass_->getEStructuralFeature(featureID);
}

std::shared_ptr<emf::common::EList<emf::common::EObject*>> DynamicEObject::getOrCreateList(int featureID) const {
    auto it = eLists_.find(featureID);
    if (it != eLists_.end()) return it->second;
    auto* sf = featureByID(featureID);
    std::shared_ptr<emf::common::EList<emf::common::EObject*>> list;
    if (sf) {
        if (auto* ref = dynamic_cast<const EReference*>(sf)) {
            if (ref->isContainment()) {
                // containment 引用：使用 ContainmentEList 自动维护子对象 eContainer
                list = std::make_shared<ContainmentEList>(const_cast<DynamicEObject*>(this), sf);
            }
        }
    }
    if (!list) {
        // 非 containment 多值引用：普通 EList
        list = std::make_shared<emf::common::EList<emf::common::EObject*>>();
    }
    eLists_[featureID] = list;
    return list;
}

// ===== 按 EStructuralFeature 反射访问（委托 featureID 版本）=====
std::any DynamicEObject::eGet(const EStructuralFeature* feature) const {
    if (!feature) return std::any{};
    return eGet(feature->getFeatureID());
}

std::any DynamicEObject::eGet(const EStructuralFeature* feature, bool /*resolve*/) const {
    return eGet(feature);
}

void DynamicEObject::eSet(const EStructuralFeature* feature, std::any value) {
    if (!feature) return;
    eSet(feature->getFeatureID(), std::move(value));
}

bool DynamicEObject::eIsSet(const EStructuralFeature* feature) const {
    if (!feature) return false;
    return eIsSet(feature->getFeatureID());
}

void DynamicEObject::eUnset(const EStructuralFeature* feature) {
    if (!feature) return;
    eUnset(feature->getFeatureID());
}

// ===== 按 featureID 反射访问（高性能：按 ID 索引存储）=====
std::any DynamicEObject::eGet(int featureID) const {
    auto* sf = featureByID(featureID);
    if (!sf) return std::any{};
    // 多值 EReference：返回内部 EList 指针（lazy-created，对齐 Java DynamicEObjectImpl
    // 及生成类 eGet 语义——返回内部列表，直接修改即生效）。
    // 调用方不应 delete 返回的指针（由 DynamicEObject 通过 shared_ptr 管理）。
    if (sf->isMany()) {
        if (dynamic_cast<const EReference*>(sf)) {
            auto list = getOrCreateList(featureID);
            return std::any{list.get()};
        }
    }
    // 单值：从 BasicEObject 动态存储取（按 featureID 索引）
    auto it = eDynamicSettings_.find(featureID);
    if (it != eDynamicSettings_.end()) return it->second;
    return std::any{};
}

void DynamicEObject::eSet(int featureID, std::any value) {
    auto* sf = featureByID(featureID);
    if (!sf) return;
    // 多值 EReference：写入 ContainmentEList（对齐 eGet 从 getOrCreateList 取）。
    // 之前 eSet 把 vector<EObject*> 存到 eDynamicSettings_，但 eGet 从
    // getOrCreateList 取（ContainmentEList），导致数据丢失。
    if (sf->isMany() && dynamic_cast<const EReference*>(sf)) {
        auto list = getOrCreateList(featureID);
        list->clear();
        // 从 value 提取 EObject* 列表并 add 到内部 list
        if (value.type() == typeid(std::vector<emf::common::EObject*>)) {
            auto& v = std::any_cast<std::vector<emf::common::EObject*>&>(value);
            for (auto* o : v) {
                if (o) {
                    list->add(o);
                    // containment：设置 eContainer
                    auto* ref = dynamic_cast<const EReference*>(sf);
                    if (ref && ref->isContainment()) {
                        if (auto* impl = dynamic_cast<emf::common::EObjectImpl*>(o)) {
                            impl->setEContainer(this);
                            impl->setEContainingFeature(ref);
                        }
                    }
                }
            }
        } else if (value.type() == typeid(emf::common::EObject*)) {
            auto* o = std::any_cast<emf::common::EObject*>(value);
            if (o) {
                list->add(o);
                auto* ref = dynamic_cast<const EReference*>(sf);
                if (ref && ref->isContainment()) {
                    if (auto* impl = dynamic_cast<emf::common::EObjectImpl*>(o)) {
                        impl->setEContainer(this);
                        impl->setEContainingFeature(ref);
                    }
                }
            }
        } else if (value.type() == typeid(emf::common::EList<emf::common::EObject*>*)) {
            auto* p = std::any_cast<emf::common::EList<emf::common::EObject*>*>(value);
            if (p) {
                for (size_t i = 0; i < p->size(); ++i) {
                    auto* o = (*p)[i];
                    if (o) {
                        list->add(o);
                        auto* ref = dynamic_cast<const EReference*>(sf);
                        if (ref && ref->isContainment()) {
                            if (auto* impl = dynamic_cast<emf::common::EObjectImpl*>(o)) {
                                impl->setEContainer(this);
                                impl->setEContainingFeature(ref);
                            }
                        }
                    }
                }
            }
        }
        eDynamicSetFlags_.insert(featureID);
        return;
    }
    // containment 单值引用：设置子对象 eContainer / eContainingFeature
    if (auto* ref = dynamic_cast<const EReference*>(sf)) {
        if (ref->isContainment() && !sf->isMany()) {
            if (auto* v = std::any_cast<emf::common::EObject*>(&value)) {
                if (*v) {
                    if (auto* impl = dynamic_cast<emf::common::EObjectImpl*>(*v)) {
                        impl->setEContainer(this);
                        impl->setEContainingFeature(ref);
                    }
                }
            }
        }
    }
    // 对齐 Java DynamicEObjectImpl.eDynamicSet：触发 SET notification
    // （codegen 静态类同样在 setter 中 eNotify，动态对象需对齐此行为，
    //   否则 EContentAdapter / LiveValidator 对动态模型失效）
    bool notify = eNotificationRequired();
    std::any oldValue;
    if (notify) {
        auto it = eDynamicSettings_.find(featureID);
        if (it != eDynamicSettings_.end()) oldValue = it->second;
    }
    eDynamicSettings_[featureID] = value;
    eDynamicSetFlags_.insert(featureID);
    if (notify) {
        emf::common::Notification n(
            emf::common::Notification::EventType::SET, this,
            sf, featureID,
            std::move(oldValue), value);
        eNotify(n);
    }
}

bool DynamicEObject::eIsSet(int featureID) const {
    if (eDynamicSetFlags_.count(featureID) > 0) return true;
    auto it = eLists_.find(featureID);
    return it != eLists_.end() && !it->second->empty();
}

void DynamicEObject::eUnset(int featureID) {
    // 对齐 Java DynamicEObjectImpl.eDynamicUnset：发 UNSET 通知
    auto* sf = featureID >= 0 ? eClass()->getEStructuralFeature(featureID) : nullptr;
    bool notify = eNotificationRequired();
    std::any oldValue;
    if (notify) {
        auto it = eDynamicSettings_.find(featureID);
        if (it != eDynamicSettings_.end()) oldValue = it->second;
    }
    eDynamicSettings_.erase(featureID);
    eDynamicSetFlags_.erase(featureID);
    eLists_.erase(featureID);
    if (notify) {
        emf::common::Notification n(
            emf::common::Notification::EventType::UNSET, this,
            sf, featureID,
            std::move(oldValue), std::any());
        eNotify(n);
    }
}

// ===== containment 内容收集 =====
std::vector<emf::common::EObject*> DynamicEObject::eContents() const {
    if (eContentsCached_) return eContentsCache_;
    eContentsCache_.clear();
    if (eClass_) {
        for (auto* ref : eClass_->getEAllContainments()) {
            if (!ref) continue;
            int fid = ref->getFeatureID();
            if (ref->isMany()) {
                auto it = eLists_.find(fid);
                if (it != eLists_.end()) {
                    for (auto* child : *it->second) {
                        if (child) eContentsCache_.push_back(child);
                    }
                }
            } else {
                auto it = eDynamicSettings_.find(fid);
                if (it != eDynamicSettings_.end()) {
                    if (auto* v = std::any_cast<emf::common::EObject*>(&it->second)) {
                        if (*v) eContentsCache_.push_back(*v);
                    }
                }
            }
        }
    }
    eContentsCached_ = false;  // 内容可变，不长期缓存
    return eContentsCache_;
}

}  // namespace emf::ecore
