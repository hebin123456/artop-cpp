// EClassifierImpl.cpp - 方案 3 Java 风格
#include "emf/ecore/EcoreImpls.h"
#include "emf/ecore/EcorePackage.h"

namespace emf::ecore {

bool EClassifierImpl::isInstance(emf::common::EObject* /*obj*/) const {
    return false;
}

void EClassifierImpl::addTypeParameter(ETypeParameter* tp) {
    if (!tp) return;
    typeParams_.push_back(tp);
    // 设置 container back-reference，用于 XMISaver 构造 eTypeParameter href（#//Class/Type 或 #//DataType/Type）
    if (auto* tpImpl = dynamic_cast<ETypeParameterImpl*>(tp)) {
        tpImpl->setEContainer(this);
    }
}

std::any EClassifierImpl::eGet(const EStructuralFeature* feature) const {
    if (feature) {
        int fid = feature->getFeatureID();
        switch (fid) {
            case ::emf::common::FeatureID::ECLASSIFIER_EINSTANCECLASSNAME:
                return std::any{instanceClassName_};
            case ::emf::common::FeatureID::ECLASSIFIER_EDEFAULTVALUE:
                return defaultValue_;
            case ::emf::common::FeatureID::ECLASSIFIER_ETYPEPARAMETERS:
                return std::any{typeParams_};
        }
    }
    return ETypedElementImpl::eGet(feature);
}

void EClassifierImpl::eSet(const EStructuralFeature* feature, std::any value) {
    if (feature) {
        int fid = feature->getFeatureID();
        switch (fid) {
            case ::emf::common::FeatureID::ECLASSIFIER_EINSTANCECLASSNAME:
                if (auto* v = std::any_cast<std::string>(&value)) instanceClassName_ = *v;
                return;
            case ::emf::common::FeatureID::ECLASSIFIER_EDEFAULTVALUE:
                defaultValue_ = std::move(value);
                return;
            case ::emf::common::FeatureID::ECLASSIFIER_ETYPEPARAMETERS:
                if (auto* v = std::any_cast<std::vector<ETypeParameter*>>(&value)) typeParams_ = *v;
                return;
        }
    }
    ETypedElementImpl::eSet(feature, std::move(value));
}

bool EClassifierImpl::eIsSet(const EStructuralFeature* feature) const {
    if (feature) {
        int fid = feature->getFeatureID();
        switch (fid) {
            case ::emf::common::FeatureID::ECLASSIFIER_EINSTANCECLASSNAME:
                return !instanceClassName_.empty();
            case ::emf::common::FeatureID::ECLASSIFIER_EDEFAULTVALUE:
                return defaultValue_.has_value();
            case ::emf::common::FeatureID::ECLASSIFIER_ETYPEPARAMETERS:
                return !typeParams_.empty();
        }
    }
    return ETypedElementImpl::eIsSet(feature);
}

void EClassifierImpl::eUnset(const EStructuralFeature* feature) {
    if (feature) {
        int fid = feature->getFeatureID();
        switch (fid) {
            case ::emf::common::FeatureID::ECLASSIFIER_EINSTANCECLASSNAME:
                instanceClassName_.clear();
                return;
            case ::emf::common::FeatureID::ECLASSIFIER_EDEFAULTVALUE:
                defaultValue_.reset();
                return;
            case ::emf::common::FeatureID::ECLASSIFIER_ETYPEPARAMETERS:
                typeParams_.clear();
                return;
        }
    }
    ETypedElementImpl::eUnset(feature);
}

}  // namespace emf::ecore
