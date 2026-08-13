// EEnumImpl.cpp - 方案 3 Java 风格
#include "emf/ecore/EcoreImpls.h"
#include "emf/ecore/EcorePackage.h"

namespace emf::ecore {

EEnumLiteral* EEnumImpl::getELiteral(const std::string& name) const {
    for (auto* l : eLiterals_) {
        if (l && l->getName() == name) return l;
    }
    return nullptr;
}

EEnumLiteral* EEnumImpl::getELiteralByValue(int value) const {
    for (auto* l : eLiterals_) {
        if (l && l->getValue() == value) return l;
    }
    return nullptr;
}

void EEnumImpl::addELiteral(EEnumLiteral* lit) {
    if (lit) eLiterals_.push_back(lit);
}

std::any EEnumImpl::eGet(const EStructuralFeature* feature) const {
    if (feature && feature->getFeatureID() == ::emf::common::FeatureID::EENUM_ELITERALS) {
        return std::any{eLiterals_};
    }
    return EClassifierImpl::eGet(feature);
}

void EEnumImpl::eSet(const EStructuralFeature* feature, std::any value) {
    if (feature && feature->getFeatureID() == ::emf::common::FeatureID::EENUM_ELITERALS) {
        if (auto* v = std::any_cast<std::vector<EEnumLiteral*>>(&value)) eLiterals_ = *v;
        return;
    }
    EClassifierImpl::eSet(feature, std::move(value));
}

bool EEnumImpl::eIsSet(const EStructuralFeature* feature) const {
    if (feature && feature->getFeatureID() == ::emf::common::FeatureID::EENUM_ELITERALS) {
        return !eLiterals_.empty();
    }
    return EClassifierImpl::eIsSet(feature);
}

void EEnumImpl::eUnset(const EStructuralFeature* feature) {
    if (feature && feature->getFeatureID() == ::emf::common::FeatureID::EENUM_ELITERALS) {
        eLiterals_.clear();
        return;
    }
    EClassifierImpl::eUnset(feature);
}

}  // namespace emf::ecore
