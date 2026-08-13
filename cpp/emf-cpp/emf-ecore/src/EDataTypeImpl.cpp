// EDataTypeImpl.cpp - 方案 3 Java 风格
#include "emf/ecore/EcoreImpls.h"
#include "emf/ecore/EcorePackage.h"

namespace emf::ecore {

// ===== EDataType::Internal 内部接口实现（对齐 Java EDataTypeImpl）=====
//
// Java:
//   public ConversionDelegate getConversionDelegate() {
//     if (!conversionDelegateIsSet) {
//       conversionDelegate = EcoreUtil.getRegisteredConversionDelegate(getEPackage());
//       ...
//       conversionDelegateIsSet = true;
//     }
//     return conversionDelegate;
//   }
//
// C++ 端默认返回 nullptr（委托由 emf_ecore_util 层的代码根据包注册表设置），
// 避免 emf_ecore 反向依赖 emf_ecore_util 导致循环依赖。
emf::ecore::util::ConversionDelegate* EDataTypeImpl::getConversionDelegate() const {
    if (conversionDelegateIsSet_) return conversionDelegate_;
    // 委托发现交给 emf_ecore_util 层（EcoreFactory 初始化时设置）。
    // 这里返回 nullptr，让调用方在需要时显式 setConversionDelegate。
    return nullptr;
}

void EDataTypeImpl::setConversionDelegate(emf::ecore::util::ConversionDelegate* d) {
    conversionDelegate_ = d;
    conversionDelegateIsSet_ = (d != nullptr);
}

std::any EDataTypeImpl::eGet(const EStructuralFeature* feature) const {
    if (feature && feature->getFeatureID() == ::emf::common::FeatureID::EDATATYPE_ESERIALIZABLE) {
        return std::any{serializable_};
    }
    return EClassifierImpl::eGet(feature);
}

void EDataTypeImpl::eSet(const EStructuralFeature* feature, std::any value) {
    if (feature && feature->getFeatureID() == ::emf::common::FeatureID::EDATATYPE_ESERIALIZABLE) {
        if (auto* v = std::any_cast<bool>(&value)) serializable_ = *v;
        return;
    }
    EClassifierImpl::eSet(feature, std::move(value));
}

bool EDataTypeImpl::eIsSet(const EStructuralFeature* feature) const {
    if (feature && feature->getFeatureID() == ::emf::common::FeatureID::EDATATYPE_ESERIALIZABLE) {
        return !serializable_;
    }
    return EClassifierImpl::eIsSet(feature);
}

void EDataTypeImpl::eUnset(const EStructuralFeature* feature) {
    if (feature && feature->getFeatureID() == ::emf::common::FeatureID::EDATATYPE_ESERIALIZABLE) {
        serializable_ = true;
        return;
    }
    EClassifierImpl::eUnset(feature);
}

}  // namespace emf::ecore
