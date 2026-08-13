// EReferenceImpl.cpp - 方案 3 Java 风格
// 对齐 org.eclipse.emf.ecore.impl.EReferenceImpl
// 分发 EREFERENCE_* feature ID，其余委托父类 EStructuralFeatureImpl。
#include "emf/ecore/EcoreImpls.h"
#include "emf/ecore/EcorePackage.h"

namespace emf::ecore {

// getEReferenceType：优先返回 eReferenceType_，否则回退到 eType_ 的 EClass 视图
// 对齐 Java EReferenceImpl.getEReferenceType()
EClass* EReferenceImpl::getEReferenceType() const {
    if (eReferenceType_ != nullptr) return eReferenceType_;
    EClassifier* t = ETypedElementImpl::getEType();
    return dynamic_cast<EClass*>(t);
}

// setEReferenceType：同时设置 eReferenceType_ 与 eType_（保持两者一致）
void EReferenceImpl::setEReferenceType(EClass* value) {
    eReferenceType_ = value;
    if (value != nullptr) {
        ETypedElementImpl::setEType(value);
    }
}

// isContainer：派生属性 —— 反向引用为 containment 时即为 container
// 对齐 Java EReferenceImpl.isContainer(): getEOpposite()!=null && getEOpposite().isContainment()
bool EReferenceImpl::isContainer() const {
    EReference* opp = eOpposite_;
    return opp != nullptr && opp->isContainment();
}

std::any EReferenceImpl::eGet(const EStructuralFeature* feature) const {
    if (feature) {
        int fid = feature->getFeatureID();
        switch (fid) {
            case ::emf::common::FeatureID::EREFERENCE_EREFERENCETYPE:
                return std::any{getEReferenceType()};
            case ::emf::common::FeatureID::EREFERENCE_EOPPOSITE:
                return std::any{eOpposite_};
            case ::emf::common::FeatureID::EREFERENCE_ECONTAINMENT:
                return std::any{containment_};
            case ::emf::common::FeatureID::EREFERENCE_ECONTAINER:
                return std::any{isContainer()};
            case ::emf::common::FeatureID::EREFERENCE_ERESOLVEPROXIES:
                return std::any{resolveProxies_};
        }
    }
    return EStructuralFeatureImpl::eGet(feature);
}

void EReferenceImpl::eSet(const EStructuralFeature* feature, std::any value) {
    if (feature) {
        int fid = feature->getFeatureID();
        switch (fid) {
            case ::emf::common::FeatureID::EREFERENCE_EREFERENCETYPE:
            case ::emf::common::FeatureID::ETYPED_ELEMENT_ETYPE: {
                // 接受 EClass* 或 EClassifier*（XMI 加载器传 EClassifier*），统一存为 EClass*
                EClass* cls = nullptr;
                if (auto* v = std::any_cast<EClass*>(&value)) {
                    cls = *v;
                } else if (auto* v = std::any_cast<EClassifier*>(&value)) {
                    cls = dynamic_cast<EClass*>(*v);
                }
                if (cls) {
                    eReferenceType_ = cls;
                    ETypedElementImpl::setEType(cls);
                }
                return;
            }
            case ::emf::common::FeatureID::EREFERENCE_EOPPOSITE:
                if (auto* v = std::any_cast<EReference*>(&value)) eOpposite_ = *v;
                return;
            case ::emf::common::FeatureID::EREFERENCE_ECONTAINMENT:
                if (auto* v = std::any_cast<bool>(&value)) containment_ = *v;
                return;
            case ::emf::common::FeatureID::EREFERENCE_ERESOLVEPROXIES:
                if (auto* v = std::any_cast<bool>(&value)) resolveProxies_ = *v;
                return;
        }
    }
    EStructuralFeatureImpl::eSet(feature, std::move(value));
}

bool EReferenceImpl::eIsSet(const EStructuralFeature* feature) const {
    if (feature) {
        int fid = feature->getFeatureID();
        switch (fid) {
            case ::emf::common::FeatureID::EREFERENCE_EREFERENCETYPE:
                return eReferenceType_ != nullptr || ETypedElementImpl::getEType() != nullptr;
            case ::emf::common::FeatureID::EREFERENCE_EOPPOSITE:
                return eOpposite_ != nullptr;
            case ::emf::common::FeatureID::EREFERENCE_ECONTAINMENT:
                return containment_;               // 默认 false
            case ::emf::common::FeatureID::EREFERENCE_ECONTAINER:
                return isContainer();              // 派生，默认 false
            case ::emf::common::FeatureID::EREFERENCE_ERESOLVEPROXIES:
                return !resolveProxies_;           // 默认 true
        }
    }
    return EStructuralFeatureImpl::eIsSet(feature);
}

void EReferenceImpl::eUnset(const EStructuralFeature* feature) {
    if (feature) {
        int fid = feature->getFeatureID();
        switch (fid) {
            case ::emf::common::FeatureID::EREFERENCE_EREFERENCETYPE:
                eReferenceType_ = nullptr;
                return;
            case ::emf::common::FeatureID::EREFERENCE_EOPPOSITE:
                eOpposite_ = nullptr;
                return;
            case ::emf::common::FeatureID::EREFERENCE_ECONTAINMENT:
                containment_ = false;
                return;
            case ::emf::common::FeatureID::EREFERENCE_ERESOLVEPROXIES:
                resolveProxies_ = true;
                return;
        }
    }
    EStructuralFeatureImpl::eUnset(feature);
}

}  // namespace emf::ecore
