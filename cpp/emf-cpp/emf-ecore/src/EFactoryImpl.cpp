// EFactoryImpl.cpp - 方案 3 Java 风格
#include "emf/ecore/EcoreImpls.h"
#include "emf/ecore/EcorePackage.h"
#include "emf/ecore/DynamicEObject.h"
#include "emf/common/EObject.h"

namespace emf::ecore {

emf::common::EObject* EFactoryImpl::create(const EClass* eClass) const {
    if (!eClass) return nullptr;
    auto* obj = EcoreFactory::instance().create(eClass);
    if (obj) return obj;
    // 用户模型类（非 Ecore 元类）→ DynamicEObject 动态实例化
    // 对齐 Java DynamicEObjectImpl：EFactoryImpl.create 对未知 EClass 回退到 DynamicEObject
    return new DynamicEObject(const_cast<EClass*>(eClass));
}

std::any EFactoryImpl::createFromString(const EClassifier* classifier, const std::string& literal) const {
    if (classifier) {
        return EcoreFactory::instance().createFromString(classifier, literal);
    }
    return std::any{literal};
}

std::string EFactoryImpl::convertToString(const EClassifier* classifier, const std::any& value) const {
    if (classifier) {
        return EcoreFactory::instance().convertToString(classifier, value);
    }
    if (auto* v = std::any_cast<std::string>(&value)) return *v;
    return {};
}

std::any EFactoryImpl::eGet(const EStructuralFeature* feature) const {
    if (feature && feature->getFeatureID() == ::emf::common::FeatureID::EFACTORY_EPACKAGE) {
        return std::any{ePackage_};
    }
    return emf::common::EObjectImpl::eGet(feature);
}

void EFactoryImpl::eSet(const EStructuralFeature* feature, std::any value) {
    if (feature && feature->getFeatureID() == ::emf::common::FeatureID::EFACTORY_EPACKAGE) {
        if (auto* v = std::any_cast<EPackage*>(&value)) ePackage_ = *v;
        return;
    }
    emf::common::EObjectImpl::eSet(feature, std::move(value));
}

bool EFactoryImpl::eIsSet(const EStructuralFeature* feature) const {
    if (feature && feature->getFeatureID() == ::emf::common::FeatureID::EFACTORY_EPACKAGE) {
        return ePackage_ != nullptr;
    }
    return emf::common::EObjectImpl::eIsSet(feature);
}

void EFactoryImpl::eUnset(const EStructuralFeature* feature) {
    if (feature && feature->getFeatureID() == ::emf::common::FeatureID::EFACTORY_EPACKAGE) {
        ePackage_ = nullptr;
        return;
    }
    emf::common::EObjectImpl::eUnset(feature);
}

}  // namespace emf::ecore
