// EModelElementImpl.cpp - 方案 3 Java 风格
#include "emf/ecore/EcoreImpls.h"
#include "emf/ecore/EcorePackage.h"

namespace emf::ecore {

EAnnotation* EModelElementImpl::getEAnnotation(const std::string& source) const {
    for (auto* a : annotations_) {
        if (a && a->getSource() == source) return a;
    }
    return nullptr;
}

EAnnotation* EModelElementImpl::getEAnnotation(EClass* eReference, bool /*resolve*/) const {
    if (!eReference) return nullptr;
    for (auto* a : annotations_) {
        if (!a) continue;
        // EAnnotation 接口不直接继承 EObject，需 cross-cast 到 EObject 才能访问 eClass()
        auto* obj = dynamic_cast<emf::common::EObject*>(a);
        if (obj && obj->eClass() == eReference) return a;
    }
    return nullptr;
}

void EModelElementImpl::addEAnnotation(EAnnotation* ann) {
    if (ann) annotations_.push_back(ann);
}

std::any EModelElementImpl::eGet(const EStructuralFeature* feature) const {
    if (feature) {
        int fid = feature->getFeatureID();
        if (fid == ::emf::common::FeatureID::EMODEL_ELEMENT_EANNOTATIONS) {
            return std::any{annotations_};
        }
    }
    return emf::common::EObjectImpl::eGet(feature);
}

void EModelElementImpl::eSet(const EStructuralFeature* feature, std::any value) {
    if (feature) {
        int fid = feature->getFeatureID();
        if (fid == ::emf::common::FeatureID::EMODEL_ELEMENT_EANNOTATIONS) {
            if (auto* v = std::any_cast<std::vector<EAnnotation*>>(&value)) annotations_ = *v;
            return;
        }
    }
    emf::common::EObjectImpl::eSet(feature, std::move(value));
}

bool EModelElementImpl::eIsSet(const EStructuralFeature* feature) const {
    if (feature) {
        int fid = feature->getFeatureID();
        if (fid == ::emf::common::FeatureID::EMODEL_ELEMENT_EANNOTATIONS) {
            return !annotations_.empty();
        }
    }
    return emf::common::EObjectImpl::eIsSet(feature);
}

void EModelElementImpl::eUnset(const EStructuralFeature* feature) {
    if (feature) {
        int fid = feature->getFeatureID();
        if (fid == ::emf::common::FeatureID::EMODEL_ELEMENT_EANNOTATIONS) {
            annotations_.clear();
            return;
        }
    }
    emf::common::EObjectImpl::eUnset(feature);
}

}  // namespace emf::ecore
