// EEnumLiteralImpl.cpp - 方案 3 Java 风格
#include "emf/ecore/EcoreImpls.h"
#include "emf/ecore/EcorePackage.h"

namespace emf::ecore {

std::any EEnumLiteralImpl::eGet(const EStructuralFeature* feature) const {
    if (feature) {
        int fid = feature->getFeatureID();
        switch (fid) {
            case ::emf::common::FeatureID::EENUMLITERAL_EVALUE:    return std::any{value_};
            case ::emf::common::FeatureID::EENUMLITERAL_ELITERAL:  return std::any{literal_};
            case ::emf::common::FeatureID::EENUMLITERAL_EINSTANCE: return std::any{value_};
            case ::emf::common::FeatureID::EENUMLITERAL_EENUM:      return std::any{eEnum_};
        }
    }
    return ENamedElementImpl::eGet(feature);
}

void EEnumLiteralImpl::eSet(const EStructuralFeature* feature, std::any value) {
    if (feature) {
        int fid = feature->getFeatureID();
        switch (fid) {
            case ::emf::common::FeatureID::EENUMLITERAL_EVALUE:
                if (auto* v = std::any_cast<int>(&value)) value_ = *v;
                return;
            case ::emf::common::FeatureID::EENUMLITERAL_ELITERAL:
                if (auto* v = std::any_cast<std::string>(&value)) literal_ = *v;
                return;
            case ::emf::common::FeatureID::EENUMLITERAL_EENUM:
                if (auto* v = std::any_cast<EEnum*>(&value)) eEnum_ = *v;
                return;
        }
    }
    ENamedElementImpl::eSet(feature, std::move(value));
}

bool EEnumLiteralImpl::eIsSet(const EStructuralFeature* feature) const {
    if (feature) {
        int fid = feature->getFeatureID();
        switch (fid) {
            case ::emf::common::FeatureID::EENUMLITERAL_EVALUE:    return value_ != 0;
            case ::emf::common::FeatureID::EENUMLITERAL_ELITERAL:  return !literal_.empty();
            case ::emf::common::FeatureID::EENUMLITERAL_EINSTANCE: return value_ != 0;
            case ::emf::common::FeatureID::EENUMLITERAL_EENUM:      return eEnum_ != nullptr;
        }
    }
    return ENamedElementImpl::eIsSet(feature);
}

void EEnumLiteralImpl::eUnset(const EStructuralFeature* feature) {
    if (feature) {
        int fid = feature->getFeatureID();
        switch (fid) {
            case ::emf::common::FeatureID::EENUMLITERAL_EVALUE:    value_ = 0; return;
            case ::emf::common::FeatureID::EENUMLITERAL_ELITERAL:  literal_.clear(); return;
            case ::emf::common::FeatureID::EENUMLITERAL_EENUM:      eEnum_ = nullptr; return;
        }
    }
    ENamedElementImpl::eUnset(feature);
}

}  // namespace emf::ecore
