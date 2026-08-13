// EMF Ecore: impl/BasicEObject.h
// BasicEObject —— 带反向引用维护 + 动态值存储的 EObject 基础实现。
// 对齐 org.eclipse.emf.ecore.impl.BasicEObjectImpl
//
// 职责（对齐 Java BasicEObjectImpl）：
//   - 维护 eInverseELists_ 注册表（featureID -> EInverseList*），供 EObjectWithInverseEList
//     构造/析构时注册自身，使对端 eInverseAdd/eInverseRemove 能命中本实例的反向列表。
//   - eInverseAdd/eInverseRemove：查找注册表并调用 EInverseList::basicAdd/basicRemove。
//   - eDynamicGet/Set/IsSet/Unset：基于 featureID 的动态值存储（为未在生成 switch 中
//     处理的 feature 提供回退，也是 DynamicEObject 的存储后端）。
//
// 继承 emf::common::EObjectImpl（已提供 eContainer/eProxyURI/eResource/eContents 等）。
// eClass() 仍为纯虚，由子类（DynamicEObject 或生成的 Impl）override。
#pragma once

#include "emf/common/EObject.h"
#include "emf/common/EInverseList.h"
#include "emf/common/Notification.h"
#include <any>
#include <unordered_map>
#include <unordered_set>
#include <vector>

namespace emf::ecore::impl {

// NotificationChain —— 与 emf::ecore::util::NotificationChain 同型（std::vector<Notification>）。
// 此处独立定义以避免 emf-ecore 反向依赖 emf-ecore-util。
using NotificationChain = std::vector<emf::common::Notification>;

class BasicEObject : public emf::common::EObjectImpl {
public:
    BasicEObject() = default;
    ~BasicEObject() override;

    // ===== 反向引用注册表（对齐 Java BasicEObjectImpl.eInverseELists）=====
    // EObjectWithInverseEList 构造时调用 eRegisterInverseList 把自身注册到 owner。
    virtual void eRegisterInverseList(int featureID,
                                       emf::common::EInverseList* list);
    virtual void eUnregisterInverseList(int featureID,
                                         emf::common::EInverseList* list);

    // ===== 反向引用维护（对齐 Java BasicEObjectImpl.eInverseAdd/eInverseRemove）=====
    // 由 EcoreEList::inverseAdd 在对端调用：把 otherEnd 加入本对象 featureID 对应的反向列表。
    // override EObject 接口（使用 EObject::EObjectNotificationChain 类型别名）。
    emf::common::EObject::EObjectNotificationChain eInverseAdd(
        emf::common::EObject* otherEnd, int featureID,
        emf::ecore::EClass* inverseFeatureClass,
        emf::common::EObject::EObjectNotificationChain notifications) override;
    emf::common::EObject::EObjectNotificationChain eInverseRemove(
        emf::common::EObject* otherEnd, int featureID,
        emf::ecore::EClass* inverseFeatureClass,
        emf::common::EObject::EObjectNotificationChain notifications) override;

    // ===== 动态值存储（对齐 Java BasicEObjectImpl.eDynamicGet/Set/IsSet/Unset）=====
    // 基于 featureID 的 std::any 存储，供 DynamicEObject 及生成 Impl 的未处理 feature 回退。
    virtual std::any eDynamicGet(const emf::ecore::EStructuralFeature* feature) const;
    virtual void eDynamicSet(const emf::ecore::EStructuralFeature* feature, std::any value);
    virtual bool eDynamicIsSet(const emf::ecore::EStructuralFeature* feature) const;
    virtual void eDynamicUnset(const emf::ecore::EStructuralFeature* feature);

    // ===== eGet/eSet/eIsSet/eUnset(EStructuralFeature*) override =====
    // 对齐 Java BasicEObjectImpl：委托 eDynamic* 存储/读取，eSet/eUnset 触发 SET/UNSET 通知。
    // 修复（原 gap：继承 EObjectImpl 的静默空实现，BasicEObject 直接子类
    //   TestBasicEObject/MinimalEObject2/ExtendedMinimalEObject 经 eSet(feature*) 设值后
    //   既读不回也不发通知）。DynamicEObject 已自行 override eSet/eGet(feature*)（经
    //   eSet(int) 路径，已发通知），其更派生的 override 优先生效，此处不影响。
    // using 引入 EObjectImpl 的 int 重载，避免被 feature* override 名字隐藏。
    using emf::common::EObjectImpl::eGet;
    using emf::common::EObjectImpl::eSet;
    using emf::common::EObjectImpl::eIsSet;
    using emf::common::EObjectImpl::eUnset;
    std::any eGet(const emf::ecore::EStructuralFeature* feature) const override;
    std::any eGet(const emf::ecore::EStructuralFeature* feature, bool resolve) const override;
    void eSet(const emf::ecore::EStructuralFeature* feature, std::any value) override;
    bool eIsSet(const emf::ecore::EStructuralFeature* feature) const override;
    void eUnset(const emf::ecore::EStructuralFeature* feature) override;

protected:
    // featureID -> 反向 EList（非拥有，由 EObjectWithInverseEList 析构时反注册）
    std::unordered_map<int, emf::common::EInverseList*> eInverseELists_;
    // 动态值存储：featureID -> 值
    std::unordered_map<int, std::any> eDynamicSettings_;
    // 动态 isSet 标记：featureID -> 是否已设置
    std::unordered_set<int> eDynamicSetFlags_;
};

// 向后兼容别名（历史代码使用 BasicEObjectImpl 名字）
using BasicEObjectImpl = BasicEObject;

}  // namespace emf::ecore::impl
