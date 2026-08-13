// EContentsEList / ECrossReferenceEList 实现
// 对齐 Java: org.eclipse.emf.ecore.util.EContentsEList / ECrossReferenceEList
#include "emf/ecore/util/EContentsEList.h"
#include "emf/ecore/EcoreImpls.h"

namespace emf::ecore::util {

using emf::common::EObject;
using emf::common::EList;
using emf::ecore::EReference;

// ===== EContentsEList =====
// 构造时快照 owner 的 containment 子对象到 contents_。
// 对齐 Java EContentsEList 的遍历语义（resolveProxies=false 的 containment 视图）。
EContentsEList::EContentsEList(EObject* owner) : owner_(owner) {
    if (owner_) {
        for (auto* c : owner_->eContents()) {
            contents_.push_back(c);
        }
    }
}

// ===== ECrossReferenceEList =====
// 构造时遍历 owner 的所有非 containment EReference，收集引用目标到 refs_。
// 对齐 Java ECrossReferenceEList。
ECrossReferenceEList::ECrossReferenceEList(EObject* owner) : owner_(owner) {
    if (!owner_) return;
    auto* cls = owner_->eClass();
    if (!cls) return;
    for (auto* f : cls->getEStructuralFeatures()) {
        auto* ref = dynamic_cast<EReference*>(f);
        if (!ref || ref->isContainment()) continue;
        auto v = owner_->eGet(f);
        if (ref->getUpperBound() != 1) {
            // 多值：eGet 返回 EList<EObject*>*（容器指针）
            if (v.type() == typeid(EList<EObject*>*)) {
                auto* listPtr = std::any_cast<EList<EObject*>*>(v);
                if (listPtr) {
                    for (size_t i = 0; i < listPtr->size(); ++i) {
                        refs_.push_back(listPtr->get(i));
                    }
                }
            }
        } else {
            // 单值：eGet 返回 EObject*
            if (v.type() == typeid(EObject*)) {
                EObject* o = std::any_cast<EObject*>(v);
                if (o) refs_.push_back(o);
            }
        }
    }
}

}  // namespace emf::ecore::util
