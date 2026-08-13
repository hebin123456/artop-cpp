// EAttributeImpl.cpp - 方案 3 Java 风格
#include "emf/ecore/EcoreImpls.h"
#include "emf/ecore/EcorePackage.h"

namespace emf::ecore {

// setEAttributeType：对齐 Java EAttributeImpl.setEAttributeType —— 设置 eAttributeType 并同步 eType
void EAttributeImpl::setEAttributeType(EDataType* value) {
    eAttributeType_ = value;
    ETypedElementImpl::setEType(value);
}

std::any EAttributeImpl::eGet(const EStructuralFeature* feature) const {
    if (feature) {
        int fid = feature->getFeatureID();
        if (fid == ::emf::common::FeatureID::EATTRIBUTE_EATTRIBUTETYPE) {
            return std::any{eAttributeType_};
        }
        if (fid == ::emf::common::FeatureID::EATTRIBUTE_EID) {
            return std::any{iD_};
        }
    }
    return EStructuralFeatureImpl::eGet(feature);
}

void EAttributeImpl::eSet(const EStructuralFeature* feature, std::any value) {
    if (feature) {
        int fid = feature->getFeatureID();
        if (fid == ::emf::common::FeatureID::EATTRIBUTE_EATTRIBUTETYPE ||
            fid == ::emf::common::FeatureID::ETYPED_ELEMENT_ETYPE) {
            // 接受 EDataType* 或 EClassifier*（XMI 加载器传 EClassifier*），
            // 内部统一存为 EDataType*（dynamic_cast）
            EDataType* dt = nullptr;
            if (auto* v = std::any_cast<EDataType*>(&value)) {
                dt = *v;
            } else if (auto* v = std::any_cast<EClassifier*>(&value)) {
                dt = dynamic_cast<EDataType*>(*v);
            }
            if (std::getenv("EMF_DEBUG_ATTR")) {
                std::fprintf(stderr, "[ATTR-DBG] eSet EAttribute eType dt=%p (cast ok=%d)\n",
                    (void*)dt, dt ? 1 : 0);
            }
            if (dt) {
                eAttributeType_ = dt;
                ETypedElementImpl::setEType(dt);
            }
            return;
        }
        if (fid == ::emf::common::FeatureID::EATTRIBUTE_EID) {
            // iD_ 是 bool；接受 bool 或 "true"/"false" 字符串（XMI 加载器传字符串）
            if (auto* v = std::any_cast<bool>(&value)) iD_ = *v;
            else if (auto* v = std::any_cast<std::string>(&value)) {
                iD_ = (*v == "true" || *v == "1");
            }
            return;
        }
    }
    EStructuralFeatureImpl::eSet(feature, std::move(value));
}

bool EAttributeImpl::eIsSet(const EStructuralFeature* feature) const {
    if (feature) {
        int fid = feature->getFeatureID();
        if (fid == ::emf::common::FeatureID::EATTRIBUTE_EATTRIBUTETYPE) return eAttributeType_ != nullptr;
        if (fid == ::emf::common::FeatureID::EATTRIBUTE_EID) return iD_;
    }
    return EStructuralFeatureImpl::eIsSet(feature);
}

void EAttributeImpl::eUnset(const EStructuralFeature* feature) {
    if (feature) {
        int fid = feature->getFeatureID();
        if (fid == ::emf::common::FeatureID::EATTRIBUTE_EATTRIBUTETYPE) eAttributeType_ = nullptr;
        else if (fid == ::emf::common::FeatureID::EATTRIBUTE_EID) iD_ = false;
        else EStructuralFeatureImpl::eUnset(feature);
        return;
    }
    EStructuralFeatureImpl::eUnset(feature);
}

}  // namespace emf::ecore
