// EGenericTypeImpl.cpp - 方案 3 Java 风格
// 反射 eGet/eSet/eIsSet/eUnset 处理 4 个自有 feature（eClassifier/eTypeArguments/eUpperBound/eLowerBound），
// 未匹配的 feature 委托到 EModelElementImpl（处理 EMODEL_ELEMENT_EANNOTATIONS）。
// eClass() override 在 EcorePackage.cpp 中实现（返回 meta EClass_EGenericType）。
#include "emf/ecore/EcoreImpls.h"
#include "emf/ecore/EcorePackage.h"

namespace emf::ecore {

std::any EGenericTypeImpl::eGet(const EStructuralFeature* feature) const {
    if (feature) {
        int fid = feature->getFeatureID();
        switch (fid) {
            case ::emf::common::FeatureID::EGENERICTYPE_ECLASSIFIER:    return std::any{eClassifier_};
            case ::emf::common::FeatureID::EGENERICTYPE_ETYPEARGUMENTS: return std::any{eTypeArguments_};
            case ::emf::common::FeatureID::EGENERICTYPE_EUPPERBOUND:    return std::any{eUpperBound_};
            case ::emf::common::FeatureID::EGENERICTYPE_ELOWERBOUND:    return std::any{eLowerBound_};
            case ::emf::common::FeatureID::EGENERICTYPE_ETYPEPARAMETER: return std::any{eTypeParameter_};
        }
    }
    return EModelElementImpl::eGet(feature);
}

void EGenericTypeImpl::eSet(const EStructuralFeature* feature, std::any value) {
    if (feature) {
        int fid = feature->getFeatureID();
        switch (fid) {
            case ::emf::common::FeatureID::EGENERICTYPE_ECLASSIFIER:
                if (auto* v = std::any_cast<EClassifier*>(&value)) eClassifier_ = *v;
                return;
            case ::emf::common::FeatureID::EGENERICTYPE_ETYPEARGUMENTS:
                if (auto* v = std::any_cast<std::vector<EGenericType*>>(&value)) eTypeArguments_ = *v;
                return;
            case ::emf::common::FeatureID::EGENERICTYPE_EUPPERBOUND:
                if (auto* v = std::any_cast<EGenericType*>(&value)) eUpperBound_ = *v;
                return;
            case ::emf::common::FeatureID::EGENERICTYPE_ELOWERBOUND:
                if (auto* v = std::any_cast<EGenericType*>(&value)) eLowerBound_ = *v;
                return;
            case ::emf::common::FeatureID::EGENERICTYPE_ETYPEPARAMETER:
                if (auto* v = std::any_cast<ETypeParameter*>(&value)) eTypeParameter_ = *v;
                return;
        }
    }
    EModelElementImpl::eSet(feature, std::move(value));
}

bool EGenericTypeImpl::eIsSet(const EStructuralFeature* feature) const {
    if (feature) {
        int fid = feature->getFeatureID();
        switch (fid) {
            case ::emf::common::FeatureID::EGENERICTYPE_ECLASSIFIER:    return eClassifier_ != nullptr;
            case ::emf::common::FeatureID::EGENERICTYPE_ETYPEARGUMENTS: return !eTypeArguments_.empty();
            case ::emf::common::FeatureID::EGENERICTYPE_EUPPERBOUND:    return eUpperBound_ != nullptr;
            case ::emf::common::FeatureID::EGENERICTYPE_ELOWERBOUND:    return eLowerBound_ != nullptr;
            case ::emf::common::FeatureID::EGENERICTYPE_ETYPEPARAMETER: return eTypeParameter_ != nullptr;
        }
    }
    return EModelElementImpl::eIsSet(feature);
}

void EGenericTypeImpl::eUnset(const EStructuralFeature* feature) {
    if (feature) {
        int fid = feature->getFeatureID();
        switch (fid) {
            case ::emf::common::FeatureID::EGENERICTYPE_ECLASSIFIER:    eClassifier_ = nullptr; return;
            case ::emf::common::FeatureID::EGENERICTYPE_ETYPEARGUMENTS: eTypeArguments_.clear(); return;
            case ::emf::common::FeatureID::EGENERICTYPE_EUPPERBOUND:    eUpperBound_ = nullptr; return;
            case ::emf::common::FeatureID::EGENERICTYPE_ELOWERBOUND:    eLowerBound_ = nullptr; return;
            case ::emf::common::FeatureID::EGENERICTYPE_ETYPEPARAMETER: eTypeParameter_ = nullptr; return;
        }
    }
    EModelElementImpl::eUnset(feature);
}

}  // namespace emf::ecore
