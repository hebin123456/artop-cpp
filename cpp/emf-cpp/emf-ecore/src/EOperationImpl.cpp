// EOperationImpl.cpp - 方案 3 Java 风格
// 对齐 org.eclipse.emf.ecore.impl.EOperationImpl
// 反射 eGet/eSet/eIsSet/eUnset 处理自有 feature eParameters/eTypeParameters/eBody，
// 未匹配的委托到 ETypedElementImpl（处理 ETYPED_ELEMENT_* + ENAMED_ELEMENT_* + EMODEL_ELEMENT_*）。
#include "emf/ecore/EcoreImpls.h"
#include "emf/ecore/EcorePackage.h"

namespace emf::ecore {

void EOperationImpl::addEParameter(EParameter* p) {
    if (p) {
        parameters_.push_back(p);
        // 对齐 Java EOperationImpl.addEParameter：回写 eOperation，保证 EParameter 能反查所属 EOperation
        // （用于 XMILoader 解析 EParameter.eType pendingRef 时推导 contextPkg）。
        p->setEOperation(this);
    }
}

void EOperationImpl::addETypeParameter(ETypeParameter* tp) {
    if (!tp) return;
    typeParams_.push_back(tp);
    // 设置 container back-reference，用于 XMISaver 构造 eTypeParameter href（#//Class/op/Type）
    if (auto* tpImpl = dynamic_cast<ETypeParameterImpl*>(tp)) {
        tpImpl->setEContainer(this);
    }
}

std::any EOperationImpl::eGet(const EStructuralFeature* feature) const {
    if (feature) {
        int fid = feature->getFeatureID();
        switch (fid) {
            case ::emf::common::FeatureID::EOPERATION_EPARAMETERS:   return std::any{parameters_};
            case ::emf::common::FeatureID::EOPERATION_ETYPEPARAMETERS: return std::any{typeParams_};
            case ::emf::common::FeatureID::EOPERATION_EBODY:         return std::any{body_};
        }
    }
    return ETypedElementImpl::eGet(feature);
}

void EOperationImpl::eSet(const EStructuralFeature* feature, std::any value) {
    if (feature) {
        int fid = feature->getFeatureID();
        switch (fid) {
            case ::emf::common::FeatureID::EOPERATION_EPARAMETERS:
                if (auto* v = std::any_cast<std::vector<EParameter*>>(&value)) parameters_ = *v;
                return;
            case ::emf::common::FeatureID::EOPERATION_ETYPEPARAMETERS:
                if (auto* v = std::any_cast<std::vector<ETypeParameter*>>(&value)) {
                    typeParams_ = *v;
                    for (auto* tp : typeParams_) {
                        if (auto* tpImpl = dynamic_cast<ETypeParameterImpl*>(tp)) {
                            tpImpl->setEContainer(this);
                        }
                    }
                }
                return;
            case ::emf::common::FeatureID::EOPERATION_EBODY:
                if (auto* v = std::any_cast<std::string>(&value)) body_ = *v;
                return;
        }
    }
    ETypedElementImpl::eSet(feature, std::move(value));
}

bool EOperationImpl::eIsSet(const EStructuralFeature* feature) const {
    if (feature) {
        int fid = feature->getFeatureID();
        switch (fid) {
            case ::emf::common::FeatureID::EOPERATION_EPARAMETERS:    return !parameters_.empty();
            case ::emf::common::FeatureID::EOPERATION_ETYPEPARAMETERS: return !typeParams_.empty();
            case ::emf::common::FeatureID::EOPERATION_EBODY:          return !body_.empty();
        }
    }
    return ETypedElementImpl::eIsSet(feature);
}

void EOperationImpl::eUnset(const EStructuralFeature* feature) {
    if (feature) {
        int fid = feature->getFeatureID();
        switch (fid) {
            case ::emf::common::FeatureID::EOPERATION_EPARAMETERS:    parameters_.clear(); return;
            case ::emf::common::FeatureID::EOPERATION_ETYPEPARAMETERS: typeParams_.clear(); return;
            case ::emf::common::FeatureID::EOPERATION_EBODY:          body_.clear(); return;
        }
    }
    ETypedElementImpl::eUnset(feature);
}

}  // namespace emf::ecore
