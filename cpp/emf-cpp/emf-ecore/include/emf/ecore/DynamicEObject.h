// EMF Ecore: DynamicEObject.h
// DynamicEObject —— 动态模型对象（无生成代码时的运行时实例化回退）。
// 对齐 org.eclipse.emf.ecore.impl.DynamicEObjectImpl
//
// 当 EFactory::create(EClass*) 无生成的静态类可返回时，回退到 new DynamicEObject(cls)。
// 所有 feature 访问通过 eClass() 的元数据驱动，值存储在 BasicEObject::eDynamicSettings_。
//
// 语义（对齐 Java DynamicEObjectImpl，详见 ARCHITECTURE.md）：
//   - 单值 EAttribute：eGet 返回存储值或默认值；eSet 直接存储。
//   - 多值 EAttribute/EReference：eGet 返回内部 EList 指针（lazy-created），直接修改即生效。
//   - containment EReference 的子对象自动设置 eContainer（通过 ContainmentEList::add）。
#pragma once

#include "emf/ecore/EcoreImpls.h"
#include "emf/ecore/impl/BasicEObject.h"
#include "emf/common/EList.h"
#include <memory>

namespace emf::ecore {

// ===== ContainmentEList =====
// 对齐 Java DynamicEObjectImpl.DynamicEList 的 containment 行为：
// add 时自动设置子对象 eContainer + eContainingFeature；
// remove/clear 时自动清除。
class ContainmentEList : public emf::common::EList<emf::common::EObject*> {
public:
    ContainmentEList(emf::common::EObject* owner, const EStructuralFeature* feature)
        : owner_(owner), feature_(feature) {}

    void add(emf::common::EObject* value) override {
        if (value) {
            setContainer(value, owner_, feature_);
        }
        emf::common::EList<emf::common::EObject*>::add(value);
    }

    bool remove(emf::common::EObject* const& value) override {
        // 先清除 eContainer，再委托基类删除
        if (contains(value)) {
            clearContainer(value);
        }
        return emf::common::EList<emf::common::EObject*>::remove(value);
    }

    void clear() override {
        for (size_t i = 0; i < size(); ++i) {
            clearContainer(get(i));
        }
        emf::common::EList<emf::common::EObject*>::clear();
    }

private:
    emf::common::EObject* owner_;
    const EStructuralFeature* feature_;

    static void setContainer(emf::common::EObject* obj,
                              emf::common::EObject* owner,
                              const EStructuralFeature* feat) {
        if (auto* impl = dynamic_cast<emf::common::EObjectImpl*>(obj)) {
            impl->setEContainer(owner);
            impl->setEContainingFeature(const_cast<EStructuralFeature*>(feat));
        }
    }
    static void clearContainer(emf::common::EObject* obj) {
        setContainer(obj, nullptr, nullptr);
    }
};

class DynamicEObject : public emf::ecore::impl::BasicEObject {
public:
    explicit DynamicEObject(EClass* eClass);
    ~DynamicEObject() override;

    emf::ecore::EClass* eClass() const override { return eClass_; }

    // ===== 反射访问（按 EStructuralFeature）=====
    std::any eGet(const EStructuralFeature* feature) const override;
    std::any eGet(const EStructuralFeature* feature, bool resolve) const override;
    void eSet(const EStructuralFeature* feature, std::any value) override;
    bool eIsSet(const EStructuralFeature* feature) const override;
    void eUnset(const EStructuralFeature* feature) override;

    // ===== 反射访问（按 featureID）=====
    std::any eGet(int featureID) const override;
    void eSet(int featureID, std::any value) override;
    bool eIsSet(int featureID) const override;
    void eUnset(int featureID) override;

    // ===== containment 内容收集 =====
    std::vector<emf::common::EObject*> eContents() const override;

protected:
    EClass* eClass_ = nullptr;

private:
    // 多值 feature 的 list 存储（featureID -> 共享 EList）
    // 对齐 Java DynamicEObjectImpl.eLists：使用 EList<EObject*> 代替裸 vector
    // containment 引用用 ContainmentEList（自动维护 eContainer）
    // 非 containment 引用用普通 EList<EObject*>
    // featureID 由 EClassImpl::addEStructuralFeature 按 eAllSuperTypes feature 数偏移分配，
    // 保证跨包继承时不冲突（对齐 Java）。
    mutable std::unordered_map<int, std::shared_ptr<emf::common::EList<emf::common::EObject*>>> eLists_;
    // 单值 feature 的值存储复用 BasicEObject::eDynamicSettings_（按 featureID 索引）
    // eContents 缓存
    mutable std::vector<emf::common::EObject*> eContentsCache_;
    mutable bool eContentsCached_ = false;

    // 辅助：按 featureID 取 EStructuralFeature
    const EStructuralFeature* featureByID(int featureID) const;
    // 辅助：多值 list 取/建（根据 feature 类型创建 ContainmentEList 或普通 EList）
    std::shared_ptr<emf::common::EList<emf::common::EObject*>> getOrCreateList(int featureID) const;
};

// 向后兼容别名（历史代码 / demo 使用 DynamicEObjectImpl 名字）
using DynamicEObjectImpl = DynamicEObject;

}  // namespace emf::ecore
