// ETypedElementImpl.cpp - 方案 3 Java 风格
// 对齐 org.eclipse.emf.ecore.impl.ETypedElementImpl
// 分发 ETYPED_ELEMENT_* feature ID，其余委托父类 ENamedElementImpl。
#include "emf/ecore/EcoreImpls.h"
#include "emf/ecore/EcorePackage.h"

namespace emf::ecore {

// setEType：设置 eType_，并同步到已存在的 eGenericType.eClassifier（对齐 Java ETypedElementImpl.setEType 维护双向关联）
void ETypedElementImpl::setEType(EClassifier* value) {
    eType_ = value;
    if (eGenericType_) {
        eGenericType_->setEClassifier(value);
    }
}

// getEGenericType：lazy 创建并缓存，并用当前 eType_ 初始化 eClassifier
// （对齐测试期望：setEType 后再 getEGenericType 时 eClassifier 自动跟进）
EGenericType* ETypedElementImpl::getEGenericType() {
    if (!eGenericType_) {
        eGenericType_ = EcoreFactory::instance().createEGenericType();
        if (eType_) {
            eGenericType_->setEClassifier(eType_);
        }
    }
    return eGenericType_;
}

// setEGenericType：直接替换 containment 槽（对齐 Java ETypedElementImpl.basicSetEGenericType）。
// 用于 XMILoader 解析 <eGenericType> 子元素后注入；同时同步 eType_ = eGenericType.eClassifier（若有），
// 保证 getEType()/继承解析在泛型场景下仍可用。
void ETypedElementImpl::setEGenericType(EGenericType* value) {
    eGenericType_ = value;
    if (eGenericType_ && eGenericType_->getEClassifier()) {
        eType_ = eGenericType_->getEClassifier();
    }
}

// isEGenericTypeParameterized：对齐 Java ETypedElementImpl.isSetEGenericType 的核心判定。
// eGenericType 参数化 = eTypeParameter != null || !eTypeArguments.isEmpty()。
// 参数化时无法用 eType 属性表达，必须写 <eGenericType> 子元素。
bool ETypedElementImpl::isEGenericTypeParameterized() const {
    return eGenericType_ &&
           (eGenericType_->getETypeParameter() != nullptr ||
            !eGenericType_->getETypeArguments().empty());
}

std::any ETypedElementImpl::eGet(const EStructuralFeature* feature) const {
    if (feature) {
        int fid = feature->getFeatureID();
        switch (fid) {
            case ::emf::common::FeatureID::ETYPED_ELEMENT_ELOWERBOUND:
                return std::any{lowerBound_};
            case ::emf::common::FeatureID::ETYPED_ELEMENT_EUPPERBOUND:
                return std::any{upperBound_};
            case ::emf::common::FeatureID::ETYPED_ELEMENT_EORDERED:
                return std::any{ordered_};
            case ::emf::common::FeatureID::ETYPED_ELEMENT_EUNIQUE:
                return std::any{unique_};
            case ::emf::common::FeatureID::ETYPED_ELEMENT_ETYPE:
                return std::any{eType_};
            case ::emf::common::FeatureID::ETYPED_ELEMENT_EGENERICTYPE:
                return std::any{eGenericType_};
        }
    }
    return ENamedElementImpl::eGet(feature);
}

void ETypedElementImpl::eSet(const EStructuralFeature* feature, std::any value) {
    if (feature) {
        int fid = feature->getFeatureID();
        switch (fid) {
            case ::emf::common::FeatureID::ETYPED_ELEMENT_ELOWERBOUND:
                if (auto* v = std::any_cast<int>(&value)) lowerBound_ = *v;
                return;
            case ::emf::common::FeatureID::ETYPED_ELEMENT_EUPPERBOUND:
                if (auto* v = std::any_cast<int>(&value)) upperBound_ = *v;
                return;
            case ::emf::common::FeatureID::ETYPED_ELEMENT_EORDERED:
                if (auto* v = std::any_cast<bool>(&value)) ordered_ = *v;
                return;
            case ::emf::common::FeatureID::ETYPED_ELEMENT_EUNIQUE:
                if (auto* v = std::any_cast<bool>(&value)) unique_ = *v;
                return;
            case ::emf::common::FeatureID::ETYPED_ELEMENT_ETYPE:
                // 接受 EClassifier* 或 EObject*（XMI 加载器/测试可能传 EObject*，需 cross-cast）
                if (auto* v = std::any_cast<EClassifier*>(&value)) eType_ = *v;
                else if (auto* v = std::any_cast<emf::common::EObject*>(&value)) {
                    eType_ = dynamic_cast<EClassifier*>(*v);
                }
                return;
            case ::emf::common::FeatureID::ETYPED_ELEMENT_EGENERICTYPE:
                if (auto* v = std::any_cast<EGenericType*>(&value)) {
                    eGenericType_ = *v;
                    if (eGenericType_ && eGenericType_->getEClassifier()) {
                        eType_ = eGenericType_->getEClassifier();
                    }
                }
                return;
        }
    }
    ENamedElementImpl::eSet(feature, std::move(value));
}

bool ETypedElementImpl::eIsSet(const EStructuralFeature* feature) const {
    if (feature) {
        int fid = feature->getFeatureID();
        switch (fid) {
            case ::emf::common::FeatureID::ETYPED_ELEMENT_ELOWERBOUND:
                return lowerBound_ != 0;          // 默认 0
            case ::emf::common::FeatureID::ETYPED_ELEMENT_EUPPERBOUND:
                return upperBound_ != 1;          // 默认 1
            case ::emf::common::FeatureID::ETYPED_ELEMENT_EORDERED:
                return !ordered_;                 // 默认 true
            case ::emf::common::FeatureID::ETYPED_ELEMENT_EUNIQUE:
                return !unique_;                  // 默认 true
            case ::emf::common::FeatureID::ETYPED_ELEMENT_ETYPE:
                // 对齐 Java isSet 互斥：eType 仅当无参数化 eGenericType 时视为 set
                return eType_ != nullptr && !isEGenericTypeParameterized();
            case ::emf::common::FeatureID::ETYPED_ELEMENT_EGENERICTYPE:
                // 对齐 Java：eGenericType 仅当参数化（无法用 eType 属性表达）时视为 set
                return isEGenericTypeParameterized();
        }
    }
    return ENamedElementImpl::eIsSet(feature);
}

void ETypedElementImpl::eUnset(const EStructuralFeature* feature) {
    if (feature) {
        int fid = feature->getFeatureID();
        switch (fid) {
            case ::emf::common::FeatureID::ETYPED_ELEMENT_ELOWERBOUND:
                lowerBound_ = 0;
                return;
            case ::emf::common::FeatureID::ETYPED_ELEMENT_EUPPERBOUND:
                upperBound_ = 1;
                return;
            case ::emf::common::FeatureID::ETYPED_ELEMENT_EORDERED:
                ordered_ = true;
                return;
            case ::emf::common::FeatureID::ETYPED_ELEMENT_EUNIQUE:
                unique_ = true;
                return;
            case ::emf::common::FeatureID::ETYPED_ELEMENT_ETYPE:
                eType_ = nullptr;
                return;
            case ::emf::common::FeatureID::ETYPED_ELEMENT_EGENERICTYPE:
                eGenericType_ = nullptr;
                return;
        }
    }
    ENamedElementImpl::eUnset(feature);
}

}  // namespace emf::ecore
