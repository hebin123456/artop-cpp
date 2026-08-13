// EMF Common: FeatureMap + FeatureMapEntry 实现
#include "emf/common/FeatureMap.h"

#include <algorithm>
#include <stdexcept>

namespace emf::common {

// ---- FeatureMapEntry ----

emf::common::EObject* FeatureMapEntry::getValueAsObject() const {
    if (value.type() == typeid(emf::common::EObject*)) {
        return std::any_cast<emf::common::EObject*>(value);
    }
    return nullptr;
}

bool FeatureMapEntry::isProxy() const {
    auto* obj = getValueAsObject();
    return obj != nullptr && obj->eIsProxy();
}

// ---- FeatureMap ----

EList<FeatureMapEntry> FeatureMap::list(const emf::ecore::EStructuralFeature* feature) const {
    EList<FeatureMapEntry> result;
    for (const auto& e : entries_) {
        if (e.feature == feature) {
            result.add(e);
        }
    }
    return result;
}

int FeatureMap::removeByFeature(const emf::ecore::EStructuralFeature* feature) {
    int removed = 0;
    auto it = entries_.begin();
    while (it != entries_.end()) {
        if (it->feature == feature) {
            it = entries_.erase(it);
            ++removed;
        } else {
            ++it;
        }
    }
    return removed;
}

bool FeatureMap::hasFeature(const emf::ecore::EStructuralFeature* feature) const {
    for (const auto& e : entries_) {
        if (e.feature == feature) return true;
    }
    return false;
}

}  // namespace emf::common
