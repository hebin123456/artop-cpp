// EStructuralFeatureImpl.cpp - 方案 3 Java 风格
#include "emf/ecore/EcoreImpls.h"
#include "emf/ecore/EcorePackage.h"

namespace emf::ecore {

EPackage* EStructuralFeatureImpl::getEPackage() const {
    if (auto* cc = containingClass_) {
        return cc->getEPackage();
    }
    return nullptr;
}

std::any EStructuralFeatureImpl::eGet(const EStructuralFeature* feature) const {
    if (feature) {
        int fid = feature->getFeatureID();
        switch (fid) {
            case ::emf::common::FeatureID::ESTRUCTURALFEATURE_EFEATUREID:
                return std::any{featureID_};
            case ::emf::common::FeatureID::ESTRUCTURALFEATURE_ECHANGEABLE:
                return std::any{changeable_};
            case ::emf::common::FeatureID::ESTRUCTURALFEATURE_EVOLATILE:
                return std::any{volatile_};
            case ::emf::common::FeatureID::ESTRUCTURALFEATURE_ETRANSIENT:
                return std::any{transient_};
            case ::emf::common::FeatureID::ESTRUCTURALFEATURE_EUNSETTABLE:
                return std::any{unsettable_};
            case ::emf::common::FeatureID::ESTRUCTURALFEATURE_EDERIVED:
                return std::any{derived_};
            case ::emf::common::FeatureID::ESTRUCTURALFEATURE_EDEFAULTVALUELITERAL:
                return std::any{defaultValueLiteral_};
            case ::emf::common::FeatureID::ESTRUCTURALFEATURE_EECONTAININGCLASS:
                return std::any{containingClass_};
        }
    }
    return ETypedElementImpl::eGet(feature);
}

void EStructuralFeatureImpl::eSet(const EStructuralFeature* feature, std::any value) {
    if (feature) {
        int fid = feature->getFeatureID();
        switch (fid) {
            case ::emf::common::FeatureID::ESTRUCTURALFEATURE_EFEATUREID:
                if (auto* v = std::any_cast<int>(&value)) featureID_ = *v;
                return;
            case ::emf::common::FeatureID::ESTRUCTURALFEATURE_ECHANGEABLE:
                if (auto* v = std::any_cast<bool>(&value)) changeable_ = *v;
                return;
            case ::emf::common::FeatureID::ESTRUCTURALFEATURE_EVOLATILE:
                if (auto* v = std::any_cast<bool>(&value)) volatile_ = *v;
                return;
            case ::emf::common::FeatureID::ESTRUCTURALFEATURE_ETRANSIENT:
                if (auto* v = std::any_cast<bool>(&value)) transient_ = *v;
                return;
            case ::emf::common::FeatureID::ESTRUCTURALFEATURE_EUNSETTABLE:
                if (auto* v = std::any_cast<bool>(&value)) unsettable_ = *v;
                return;
            case ::emf::common::FeatureID::ESTRUCTURALFEATURE_EDERIVED:
                if (auto* v = std::any_cast<bool>(&value)) derived_ = *v;
                return;
            case ::emf::common::FeatureID::ESTRUCTURALFEATURE_EDEFAULTVALUELITERAL:
                if (auto* v = std::any_cast<std::string>(&value)) defaultValueLiteral_ = *v;
                return;
            case ::emf::common::FeatureID::ESTRUCTURALFEATURE_EECONTAININGCLASS:
                if (auto* v = std::any_cast<EClass*>(&value)) containingClass_ = *v;
                return;
        }
    }
    ETypedElementImpl::eSet(feature, std::move(value));
}

bool EStructuralFeatureImpl::eIsSet(const EStructuralFeature* feature) const {
    if (feature) {
        int fid = feature->getFeatureID();
        switch (fid) {
            case ::emf::common::FeatureID::ESTRUCTURALFEATURE_EFEATUREID:
                return featureID_ >= 0;
            case ::emf::common::FeatureID::ESTRUCTURALFEATURE_ECHANGEABLE:
                return !changeable_;
            case ::emf::common::FeatureID::ESTRUCTURALFEATURE_EVOLATILE:
                return volatile_;
            case ::emf::common::FeatureID::ESTRUCTURALFEATURE_ETRANSIENT:
                return transient_;
            case ::emf::common::FeatureID::ESTRUCTURALFEATURE_EUNSETTABLE:
                return unsettable_;
            case ::emf::common::FeatureID::ESTRUCTURALFEATURE_EDERIVED:
                return derived_;
            case ::emf::common::FeatureID::ESTRUCTURALFEATURE_EDEFAULTVALUELITERAL:
                return defaultValueLiteralIsSet_;
            case ::emf::common::FeatureID::ESTRUCTURALFEATURE_EECONTAININGCLASS:
                return containingClass_ != nullptr;
        }
    }
    return ETypedElementImpl::eIsSet(feature);
}

void EStructuralFeatureImpl::eUnset(const EStructuralFeature* feature) {
    if (feature) {
        int fid = feature->getFeatureID();
        switch (fid) {
            case ::emf::common::FeatureID::ESTRUCTURALFEATURE_EFEATUREID:
                featureID_ = -1;
                return;
            case ::emf::common::FeatureID::ESTRUCTURALFEATURE_ECHANGEABLE:
                changeable_ = true;
                return;
            case ::emf::common::FeatureID::ESTRUCTURALFEATURE_EVOLATILE:
                volatile_ = false;
                return;
            case ::emf::common::FeatureID::ESTRUCTURALFEATURE_ETRANSIENT:
                transient_ = false;
                return;
            case ::emf::common::FeatureID::ESTRUCTURALFEATURE_EUNSETTABLE:
                unsettable_ = false;
                return;
            case ::emf::common::FeatureID::ESTRUCTURALFEATURE_EDERIVED:
                derived_ = false;
                return;
            case ::emf::common::FeatureID::ESTRUCTURALFEATURE_EDEFAULTVALUELITERAL:
                defaultValueLiteral_.clear();
                defaultValueLiteralIsSet_ = false;
                return;
            case ::emf::common::FeatureID::ESTRUCTURALFEATURE_EECONTAININGCLASS:
                containingClass_ = nullptr;
                return;
        }
    }
    ETypedElementImpl::eUnset(feature);
}

}  // namespace emf::ecore
