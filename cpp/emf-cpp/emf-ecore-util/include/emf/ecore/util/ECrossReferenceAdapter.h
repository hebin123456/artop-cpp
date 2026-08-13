// EMF Ecore-util: ECrossReferenceAdapter
// 对齐 Java: org.eclipse.emf.ecore.util.ECrossReferenceAdapter
//
// 维护一个 EObject 子树的非 containment 跨引用集合。挂到目标对象后，
// 监听 SET/ADD/REMOVE 通知，维护 nonContainRefs_ 集合，用于跨引用查询。
#pragma once

#include "emf/common/ENotifier.h"
#include "emf/common/Notification.h"

#include <unordered_set>
#include <vector>

namespace emf::common { class EObject; }
namespace emf::ecore { class EReference; class EStructuralFeature; }

namespace emf::ecore::util {

class ECrossReferenceAdapter : public emf::common::EAdapter {
public:
    // 监听通知，维护非 containment 引用集合
    void notifyChanged(const emf::common::Notification& n) override;

    // 把 adapter 递归挂到 eObj 及其 containment 子树
    void addAdapterTo(emf::common::EObject* eObj);
    // 从 eObj 及其 containment 子树摘除 adapter
    void removeAdapterFrom(emf::common::EObject* eObj);

    // 当前已收集的非 containment 引用目标集合
    const std::unordered_set<emf::common::EObject*>& getNonContainmentReferences() const {
        return nonContainRefs_;
    }

private:
    // 用指针哈希（unordered_set 默认支持指针哈希）
    std::unordered_set<emf::common::EObject*> nonContainRefs_;
    std::vector<emf::common::EObject*> attached_;
};

}  // namespace emf::ecore::util
