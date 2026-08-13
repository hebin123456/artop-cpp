// EcoreContentAdapter.h
// 对齐 Java: org.eclipse.emf.ecore.util.EContentAdapter（精确 containment 过滤版）
//
// emf-common 层的 emf::common::EContentAdapter 无法调用 EReference::isContainment()
// （emf-common 不依赖 emf-ecore），故对所有 EObject* 通知都触发 attach/detach。
// 本类在 emf-ecore-util 层 override selfAdapt，用 EReference::isContainment() 精确过滤，
// 只对 containment feature 的变更触发 handleContainment，对齐 Java 严格语义。
#pragma once

#include "emf/common/ENotifier.h"
#include "emf/common/Notification.h"
#include "emf/ecore/EcoreImpls.h"

namespace emf::ecore::util {

class EcoreContentAdapter : public emf::common::EContentAdapter {
public:
    // override selfAdapt：仅当 feature 是 containment EReference 时才调 handleContainment。
    // 对齐 Java EContentAdapter.selfAdapt 的严格 containment 过滤。
    void selfAdapt(const emf::common::Notification& notification) override {
        auto* notifier = notification.notifier();
        if (!notifier) return;
        auto* eObj = dynamic_cast<emf::common::EObject*>(notifier);
        if (!eObj) return;
        // 精确过滤：feature 必须是 EReference 且 isContainment
        auto* feature = notification.feature();
        if (feature) {
            auto* ref = dynamic_cast<const emf::ecore::EReference*>(feature);
            if (ref && ref->isContainment()) {
                handleContainment(notification);
            }
        } else {
            // feature 为空（如 eSet/eUnset 走 int featureID 路径）时，无法判断 containment，
            // 退化为对所有 EObject* 值触发（与基类行为一致，保持兼容）
            handleContainment(notification);
        }
    }
};

}  // namespace emf::ecore::util
