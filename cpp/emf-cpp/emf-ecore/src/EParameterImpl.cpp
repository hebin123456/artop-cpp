// EParameterImpl.cpp - 方案 3 Java 风格
#include "emf/ecore/EcoreImpls.h"
#include "emf/ecore/EcorePackage.h"

namespace emf::ecore {

std::any EParameterImpl::eGet(const EStructuralFeature* feature) const {
    if (feature && feature->getFeatureID() == ::emf::common::FeatureID::EOPERATION_EPARAMETERS) {
        // EParameter 没有自己的 eOperation 之外的 feature
        return std::any{eOperation_};
    }
    return ETypedElementImpl::eGet(feature);
}

void EParameterImpl::eSet(const EStructuralFeature* feature, std::any value) {
    if (feature && feature->getFeatureID() == ::emf::common::FeatureID::EOPERATION_EPARAMETERS) {
        if (auto* v = std::any_cast<EOperation*>(&value)) eOperation_ = *v;
        return;
    }
    ETypedElementImpl::eSet(feature, std::move(value));
}

bool EParameterImpl::eIsSet(const EStructuralFeature* feature) const {
    if (feature && feature->getFeatureID() == ::emf::common::FeatureID::EOPERATION_EPARAMETERS) {
        return eOperation_ != nullptr;
    }
    return ETypedElementImpl::eIsSet(feature);
}

void EParameterImpl::eUnset(const EStructuralFeature* feature) {
    if (feature && feature->getFeatureID() == ::emf::common::FeatureID::EOPERATION_EPARAMETERS) {
        eOperation_ = nullptr;
        return;
    }
    ETypedElementImpl::eUnset(feature);
}

}  // namespace emf::ecore
