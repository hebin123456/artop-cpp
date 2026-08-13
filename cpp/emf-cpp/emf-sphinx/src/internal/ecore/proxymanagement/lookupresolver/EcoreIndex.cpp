#include "emf/sphinx/internal/ecore/proxymanagement/lookupresolver/EcoreIndex.h"
#include "emf/common/EObject.h"
#include "emf/ecore/EcorePackage.h"
#include <any>
#include <algorithm>
#include <typeinfo>

namespace emf::sphinx::internal::ecore::proxymanagement::lookupresolver {

void EcoreIndex::registerObject(emf::common::EObject* obj) {
    if (!obj) return;
    auto* cls = obj->eClass();
    if (!cls) return;
    classIndex_[cls->getName()].push_back(obj);
}

void EcoreIndex::unregisterObject(emf::common::EObject* obj) {
    if (!obj) return;
    auto* cls = obj->eClass();
    if (!cls) return;
    auto it = classIndex_.find(cls->getName());
    if (it != classIndex_.end()) {
        auto& v = it->second;
        v.erase(std::remove(v.begin(), v.end(), obj), v.end());
    }
}

std::vector<emf::common::EObject*> EcoreIndex::findByName(emf::ecore::EClass* cls, const std::string& name) {
    if (!cls) return {};
    auto it = classIndex_.find(cls->getName());
    if (it == classIndex_.end()) return {};
    std::vector<emf::common::EObject*> r;
    for (auto* o : it->second) {
        std::any nval = o->eGet(cls->getEStructuralFeature("name"));
        if (nval.type() == typeid(std::string)) {
            if (std::any_cast<std::string>(nval) == name) r.push_back(o);
        }
    }
    return r;
}

std::vector<emf::common::EObject*> EcoreIndex::findByReference(emf::ecore::EReference* ref, emf::common::EObject* /*target*/) {
    if (!ref) return {};
    auto it = refIndex_.find(ref->getName());
    if (it == refIndex_.end()) return {};
    return it->second;
}

}  // namespace emf::sphinx::internal::ecore::proxymanagement::lookupresolver
