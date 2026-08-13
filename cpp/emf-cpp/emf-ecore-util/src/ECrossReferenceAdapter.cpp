// ECrossReferenceAdapter 实现
#include "emf/ecore/util/ECrossReferenceAdapter.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/common/Notification.h"
#include <algorithm>

namespace emf::ecore::util {

using emf::common::EObject;
using emf::common::Notifier;
using emf::common::EAdapter;
using emf::common::Notification;
using emf::ecore::EReference;
using emf::ecore::EStructuralFeature;

void ECrossReferenceAdapter::notifyChanged(const Notification& n) {
    if (n.eventType() != Notification::EventType::SET &&
        n.eventType() != Notification::EventType::ADD &&
        n.eventType() != Notification::EventType::REMOVE) {
        return;
    }
    // Notification::feature() 返回 const emf::ecore::EStructuralFeature*
    // （之前被前向声明成 emf::common::EStructuralFeature 并 reinterpret_cast），
    // 现在直接拿到正确类型，不需要再 cast。
    const EStructuralFeature* f = n.feature();
    if (!f) return;
    auto* ref = dynamic_cast<const EReference*>(f);
    if (!ref) return;
    if (ref->isContainment()) return;
    // 处理 old/new
    if (n.eventType() == Notification::EventType::SET) {
        if (n.oldValue().type() == typeid(EObject*)) {
            EObject* old = std::any_cast<EObject*>(n.oldValue());
            if (old) nonContainRefs_.erase(old);
        }
        if (n.newValue().type() == typeid(EObject*)) {
            EObject* ne = std::any_cast<EObject*>(n.newValue());
            if (ne) nonContainRefs_.insert(ne);
        }
    } else if (n.eventType() == Notification::EventType::ADD) {
        if (n.newValue().type() == typeid(EObject*)) {
            EObject* ne = std::any_cast<EObject*>(n.newValue());
            if (ne) nonContainRefs_.insert(ne);
        }
    } else if (n.eventType() == Notification::EventType::REMOVE) {
        if (n.oldValue().type() == typeid(EObject*)) {
            EObject* old = std::any_cast<EObject*>(n.oldValue());
            if (old) nonContainRefs_.erase(old);
        }
    }
}

void ECrossReferenceAdapter::addAdapterTo(EObject* eObj) {
    if (!eObj) return;
    if (std::find(attached_.begin(), attached_.end(), eObj) != attached_.end()) return;
    attached_.push_back(eObj);
    eObj->addAdapter(this);
    for (auto* c : eObj->eContents()) addAdapterTo(c);
}

void ECrossReferenceAdapter::removeAdapterFrom(EObject* eObj) {
    if (!eObj) return;
    auto it = std::find(attached_.begin(), attached_.end(), eObj);
    if (it == attached_.end()) return;
    attached_.erase(it);
    eObj->removeAdapter(this);
    for (auto* c : eObj->eContents()) removeAdapterFrom(c);
}

}  // namespace emf::ecore::util
