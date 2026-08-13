// ENamedElementImpl.cpp - 方案 3 Java 风格
#include "emf/ecore/EcoreImpls.h"
#include "emf/ecore/EcorePackage.h"

namespace emf::ecore {

std::any ENamedElementImpl::eGet(const EStructuralFeature* feature) const {
    if (feature) {
        int fid = feature->getFeatureID();
        if (fid == ::emf::common::FeatureID::ENAMED_ELEMENT_ENAME) {
            return std::any{name_};
        }
    }
    return EModelElementImpl::eGet(feature);
}

void ENamedElementImpl::eSet(const EStructuralFeature* feature, std::any value) {
    if (feature) {
        int fid = feature->getFeatureID();
        if (fid == ::emf::common::FeatureID::ENAMED_ELEMENT_ENAME) {
            if (auto* v = std::any_cast<std::string>(&value)) name_ = *v;
            return;
        }
    }
    EModelElementImpl::eSet(feature, std::move(value));
}

bool ENamedElementImpl::eIsSet(const EStructuralFeature* feature) const {
    if (feature) {
        int fid = feature->getFeatureID();
        if (fid == ::emf::common::FeatureID::ENAMED_ELEMENT_ENAME) {
            return !name_.empty();
        }
    }
    return EModelElementImpl::eIsSet(feature);
}

void ENamedElementImpl::eUnset(const EStructuralFeature* feature) {
    if (feature) {
        int fid = feature->getFeatureID();
        if (fid == ::emf::common::FeatureID::ENAMED_ELEMENT_ENAME) {
            name_.clear();
            return;
        }
    }
    EModelElementImpl::eUnset(feature);
}

}  // namespace emf::ecore
