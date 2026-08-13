// EPackageImpl.cpp - 方案 3 Java 风格
#include "emf/ecore/EcoreImpls.h"
#include "emf/ecore/EcorePackage.h"
#include "emf/ecore/EcoreMetadata.h"
#include "emf/common/EObject.h"

namespace emf::ecore {

EClassifier* EPackageImpl::getEClassifier(const std::string& name) const {
    for (auto* c : classifiers_) {
        if (c && c->getName() == name) return c;
    }
    return nullptr;
}

void EPackageImpl::addEClassifier(EClassifier* c) {
    if (!c) return;
    // 同步 ePackage 引用
    if (auto* cimpl = dynamic_cast<EClassifierImpl*>(c)) {
        cimpl->setEPackage(this);
    }
    classifiers_.push_back(c);
}

std::any EPackageImpl::eGet(const EStructuralFeature* feature) const {
    if (feature) {
        int fid = feature->getFeatureID();
        switch (fid) {
            case ::emf::common::FeatureID::ENAMED_ELEMENT_ENAME:
                return std::any{name_};
            case ::emf::common::FeatureID::EPACKAGE_ENSURI:
                return std::any{nsURI_};
            case ::emf::common::FeatureID::EPACKAGE_ENSPREFIX:
                return std::any{nsPrefix_};
            case ::emf::common::FeatureID::EPACKAGE_ECLASSIFIERS:
                return std::any{classifiers_};
            case ::emf::common::FeatureID::EPACKAGE_EFACTORYINSTANCE:
                return std::any{factory_};
            case ::emf::common::FeatureID::EPACKAGE_ESUPERPACKAGE_NEW:
                return std::any{superPackage_};
            case ::emf::common::FeatureID::EPACKAGE_ESUBPACKAGES:
                return std::any{subpackages_};
        }
    }
    return EModelElementImpl::eGet(feature);
}

void EPackageImpl::eSet(const EStructuralFeature* feature, std::any value) {
    if (feature) {
        int fid = feature->getFeatureID();
        switch (fid) {
            case ::emf::common::FeatureID::ENAMED_ELEMENT_ENAME:
                if (auto* v = std::any_cast<std::string>(&value)) name_ = *v;
                return;
            case ::emf::common::FeatureID::EPACKAGE_ENSURI:
                if (auto* v = std::any_cast<std::string>(&value)) nsURI_ = *v;
                return;
            case ::emf::common::FeatureID::EPACKAGE_ENSPREFIX:
                if (auto* v = std::any_cast<std::string>(&value)) nsPrefix_ = *v;
                return;
            case ::emf::common::FeatureID::EPACKAGE_ECLASSIFIERS:
                if (auto* v = std::any_cast<std::vector<EClassifier*>>(&value)) {
                    classifiers_ = *v;
                    for (auto* c : classifiers_) {
                        if (auto* cimpl = dynamic_cast<EClassifierImpl*>(c)) {
                            cimpl->setEPackage(this);
                        }
                    }
                } else if (auto* v2 = std::any_cast<std::vector<EObject*>>(&value)) {
                    // 兼容 XMILoader 传入 std::vector<EObject*> 的场景：
                    //   eSet 的 any 类型不匹配 EPackage 期望的 std::vector<EClassifier*>
                    //   时，dynamic_cast 每个元素并累加（不清空原有 list）
                    for (auto* o : *v2) {
                        if (auto* c = dynamic_cast<EClassifier*>(o)) {
                            classifiers_.push_back(c);
                            if (auto* cimpl = dynamic_cast<EClassifierImpl*>(c)) {
                                cimpl->setEPackage(this);
                            }
                        }
                    }
                }
                return;
            case ::emf::common::FeatureID::EPACKAGE_EFACTORYINSTANCE:
                if (auto* v = std::any_cast<EFactory*>(&value)) factory_ = *v;
                return;
            case ::emf::common::FeatureID::EPACKAGE_ESUPERPACKAGE_NEW:
                if (auto* v = std::any_cast<EPackage*>(&value)) superPackage_ = *v;
                return;
            case ::emf::common::FeatureID::EPACKAGE_ESUBPACKAGES:
                if (auto* v = std::any_cast<std::vector<EPackage*>>(&value)) {
                    subpackages_ = *v;
                    for (auto* p : subpackages_) {
                        if (p) p->setESuperPackage(this);
                    }
                } else if (auto* v2 = std::any_cast<std::vector<EObject*>>(&value)) {
                    // 兼容 XMILoader 传入 std::vector<EObject*> 的场景
                    for (auto* o : *v2) {
                        if (auto* p = dynamic_cast<EPackage*>(o)) {
                            subpackages_.push_back(p);
                            p->setESuperPackage(this);
                        }
                    }
                }
                return;
        }
    }
    EModelElementImpl::eSet(feature, std::move(value));
}

bool EPackageImpl::eIsSet(const EStructuralFeature* feature) const {
    if (feature) {
        int fid = feature->getFeatureID();
        switch (fid) {
            case ::emf::common::FeatureID::ENAMED_ELEMENT_ENAME:    return !name_.empty();
            case ::emf::common::FeatureID::EPACKAGE_ENSURI:         return !nsURI_.empty();
            case ::emf::common::FeatureID::EPACKAGE_ENSPREFIX:      return !nsPrefix_.empty();
            case ::emf::common::FeatureID::EPACKAGE_ECLASSIFIERS:   return !classifiers_.empty();
            case ::emf::common::FeatureID::EPACKAGE_EFACTORYINSTANCE: return factory_ != nullptr;
            case ::emf::common::FeatureID::EPACKAGE_ESUPERPACKAGE_NEW:  return superPackage_ != nullptr;
            case ::emf::common::FeatureID::EPACKAGE_ESUBPACKAGES:   return !subpackages_.empty();
        }
    }
    return EModelElementImpl::eIsSet(feature);
}

void EPackageImpl::eUnset(const EStructuralFeature* feature) {
    if (feature) {
        int fid = feature->getFeatureID();
        switch (fid) {
            case ::emf::common::FeatureID::ENAMED_ELEMENT_ENAME:    name_.clear(); return;
            case ::emf::common::FeatureID::EPACKAGE_ENSURI:         nsURI_.clear(); return;
            case ::emf::common::FeatureID::EPACKAGE_ENSPREFIX:      nsPrefix_.clear(); return;
            case ::emf::common::FeatureID::EPACKAGE_ECLASSIFIERS:   classifiers_.clear(); return;
            case ::emf::common::FeatureID::EPACKAGE_EFACTORYINSTANCE: factory_ = nullptr; return;
            case ::emf::common::FeatureID::EPACKAGE_ESUPERPACKAGE_NEW:  superPackage_ = nullptr; return;
            case ::emf::common::FeatureID::EPACKAGE_ESUBPACKAGES:   subpackages_.clear(); return;
        }
    }
    EModelElementImpl::eUnset(feature);
}

}  // namespace emf::ecore
