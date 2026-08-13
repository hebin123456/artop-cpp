// EAnnotationImpl.cpp - 方案 3 Java 风格
#include "emf/ecore/EcoreImpls.h"
#include "emf/ecore/EcorePackage.h"

namespace emf::ecore {

void EAnnotationImpl::setDetail(const std::string& k, const std::string& v) {
    for (auto& p : details_) {
        if (p.first == k) { p.second = v; return; }
    }
    details_.push_back({k, v});
}

std::string EAnnotationImpl::getDetail(const std::string& k) const {
    for (const auto& p : details_) {
        if (p.first == k) return p.second;
    }
    return {};
}

void EAnnotationImpl::setEModelElement(emf::common::EObject* m) {
    eModelElement_ = m;
}

std::any EAnnotationImpl::eGet(const EStructuralFeature* feature) const {
    if (feature) {
        int fid = feature->getFeatureID();
        if (fid == ::emf::common::FeatureID::EANNOTATION_ESOURCE) {
            return std::any{source_};
        }
        if (fid == ::emf::common::FeatureID::EANNOTATION_EDETAILS) {
            return std::any{details_};
        }
        if (fid == ::emf::common::FeatureID::EANNOTATION_ECONTENTS) {
            return std::any{contents_};
        }
        if (fid == ::emf::common::FeatureID::EANNOTATION_EREFERENCES) {
            return std::any{references_};
        }
        if (fid == ::emf::common::FeatureID::EANNOTATION_EMODEL_ELEMENT) {
            return std::any{static_cast<emf::common::EObject*>(eModelElement_)};
        }
    }
    return EModelElementImpl::eGet(feature);
}

void EAnnotationImpl::eSet(const EStructuralFeature* feature, std::any value) {
    if (feature) {
        int fid = feature->getFeatureID();
        if (fid == ::emf::common::FeatureID::EANNOTATION_ESOURCE) {
            if (auto* v = std::any_cast<std::string>(&value)) source_ = *v;
            return;
        }
        if (fid == ::emf::common::FeatureID::EANNOTATION_EDETAILS) {
            if (auto* v = std::any_cast<std::vector<std::pair<std::string, std::string>>>(&value)) details_ = *v;
            return;
        }
        if (fid == ::emf::common::FeatureID::EANNOTATION_ECONTENTS) {
            if (auto* v = std::any_cast<std::vector<emf::common::EObject*>>(&value)) contents_ = *v;
            return;
        }
        if (fid == ::emf::common::FeatureID::EANNOTATION_EREFERENCES) {
            if (auto* v = std::any_cast<std::vector<emf::common::EObject*>>(&value)) references_ = *v;
            return;
        }
        if (fid == ::emf::common::FeatureID::EANNOTATION_EMODEL_ELEMENT) {
            if (auto* v = std::any_cast<emf::common::EObject*>(&value)) eModelElement_ = *v;
            return;
        }
    }
    EModelElementImpl::eSet(feature, std::move(value));
}

bool EAnnotationImpl::eIsSet(const EStructuralFeature* feature) const {
    if (feature) {
        int fid = feature->getFeatureID();
        if (fid == ::emf::common::FeatureID::EANNOTATION_ESOURCE)  return !source_.empty();
        if (fid == ::emf::common::FeatureID::EANNOTATION_EDETAILS) return !details_.empty();
        if (fid == ::emf::common::FeatureID::EANNOTATION_ECONTENTS) return !contents_.empty();
        if (fid == ::emf::common::FeatureID::EANNOTATION_EREFERENCES) return !references_.empty();
        if (fid == ::emf::common::FeatureID::EANNOTATION_EMODEL_ELEMENT) return eModelElement_ != nullptr;
    }
    return EModelElementImpl::eIsSet(feature);
}

void EAnnotationImpl::eUnset(const EStructuralFeature* feature) {
    if (feature) {
        int fid = feature->getFeatureID();
        if (fid == ::emf::common::FeatureID::EANNOTATION_ESOURCE) {
            source_.clear();
            return;
        }
        if (fid == ::emf::common::FeatureID::EANNOTATION_EDETAILS) {
            details_.clear();
            return;
        }
        if (fid == ::emf::common::FeatureID::EANNOTATION_ECONTENTS) {
            contents_.clear();
            return;
        }
        if (fid == ::emf::common::FeatureID::EANNOTATION_EREFERENCES) {
            references_.clear();
            return;
        }
        if (fid == ::emf::common::FeatureID::EANNOTATION_EMODEL_ELEMENT) {
            eModelElement_ = nullptr;
            return;
        }
    }
    EModelElementImpl::eUnset(feature);
}

}  // namespace emf::ecore
