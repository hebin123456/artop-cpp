// ETypeParameterImpl.cpp - 方案 3 Java 风格
// 对齐 org.eclipse.emf.ecore.impl.ETypeParameterImpl
// 反射 eGet/eSet/eIsSet/eUnset 处理自有 feature eBounds（ETYPEPARAMETER_EBOUNDS），
// 未匹配的委托到 ENamedElementImpl（处理 ENAMED_ELEMENT_ENAME + EMODEL_ELEMENT_EANNOTATIONS）。
#include "emf/ecore/EcoreImpls.h"
#include "emf/ecore/EcorePackage.h"

namespace emf::ecore {

std::any ETypeParameterImpl::eGet(const EStructuralFeature* feature) const {
    if (feature && feature->getFeatureID() == ::emf::common::FeatureID::ETYPEPARAMETER_EBOUNDS) {
        return std::any{bounds_};
    }
    return ENamedElementImpl::eGet(feature);
}

void ETypeParameterImpl::eSet(const EStructuralFeature* feature, std::any value) {
    if (feature && feature->getFeatureID() == ::emf::common::FeatureID::ETYPEPARAMETER_EBOUNDS) {
        if (auto* v = std::any_cast<std::vector<EGenericType*>>(&value)) bounds_ = *v;
        return;
    }
    ENamedElementImpl::eSet(feature, std::move(value));
}

bool ETypeParameterImpl::eIsSet(const EStructuralFeature* feature) const {
    if (feature && feature->getFeatureID() == ::emf::common::FeatureID::ETYPEPARAMETER_EBOUNDS) {
        return !bounds_.empty();
    }
    return ENamedElementImpl::eIsSet(feature);
}

void ETypeParameterImpl::eUnset(const EStructuralFeature* feature) {
    if (feature && feature->getFeatureID() == ::emf::common::FeatureID::ETYPEPARAMETER_EBOUNDS) {
        bounds_.clear();
        return;
    }
    ENamedElementImpl::eUnset(feature);
}

}  // namespace emf::ecore
