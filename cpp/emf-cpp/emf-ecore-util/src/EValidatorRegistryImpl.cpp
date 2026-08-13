// EValidatorRegistryImpl 实现
// 对齐 Java: org.eclipse.emf.ecore.util.EValidatorRegistryImpl
//            org.eclipse.emf.ecore.util.ValidationDelegateRegistryImpl
#include "emf/ecore/util/EValidatorRegistryImpl.h"

namespace emf::ecore::util {

// ===== EValidatorRegistryImpl =====

// 全局单例（对齐 Java EValidator.Registry.INSTANCE，ecore 层）
EValidatorRegistryImpl& EValidatorRegistryImpl::instance() {
    static EValidatorRegistryImpl inst;
    return inst;
}

void EValidatorRegistryImpl::put(emf::ecore::EPackage* ePackage, EValidator* validator) {
    if (!ePackage) return;
    Entry e;
    e.validator = validator;
    e.isDescriptor = false;
    map_[ePackage] = e;
}

void EValidatorRegistryImpl::put(emf::ecore::EPackage* ePackage,
                                  EValidatorDescriptor* descriptor) {
    if (!ePackage) return;
    Entry e;
    e.descriptor = descriptor;
    e.isDescriptor = true;
    map_[ePackage] = e;
}

EValidator* EValidatorRegistryImpl::getEValidator(emf::ecore::EPackage* ePackage) {
    auto it = map_.find(ePackage);
    if (it == map_.end()) return nullptr;
    Entry& e = it->second;
    if (e.isDescriptor && e.descriptor) {
        // 惰性创建并回写（对齐 Java getEValidator：descriptor.getEValidator() 后回写到 map）
        EValidator* v = e.descriptor->getEValidator();
        if (v) {
            e.validator = v;
            e.isDescriptor = false;
        }
        return v;
    }
    return e.validator;
}

EValidator* EValidatorRegistryImpl::delegatedGet(emf::ecore::EPackage* ePackage) {
    // null key fallback：返回默认 EObjectValidator
    // 对齐 Java EValidatorRegistryImpl.delegatedGet(null) → EObjectValidator.INSTANCE
    if (!ePackage) {
        return getDefaultEObjectValidator();
    }
    EValidator* v = getEValidator(ePackage);
    if (v) return v;
    // 委托 delegate registry
    if (delegate_) {
        return delegate_->delegatedGet(ePackage);
    }
    // 无 delegate：返回默认 EObjectValidator
    return getDefaultEObjectValidator();
}

bool EValidatorRegistryImpl::containsKey(emf::ecore::EPackage* ePackage) const {
    if (map_.find(ePackage) != map_.end()) return true;
    if (delegate_) return delegate_->containsKey(ePackage);
    return false;
}

void EValidatorRegistryImpl::remove(emf::ecore::EPackage* ePackage) {
    map_.erase(ePackage);
}

// 默认 EObjectValidator 包装器：将 emf-ecore-util 的 EObjectValidator 静态方法
// 适配为 EValidator 接口
//
// 修复（原 gap：调用旧 EObjectValidator::validate(EObject*) 静态方法，
//   该方法是 dynamic_cast 分派的简化版，仅检查 name 非空等少量约束，
//   不触发 validate_EveryDefaultConstraint 的 8 个内置约束）：
//   改为调 validate_EveryDefaultConstraint(EObject*, DiagnosticChain*, context*)，
//   对齐 Java EObjectValidator.validate_EveryDefaultConstraint 的完整约束集
//   （NoCircularContainment / EveryMultiplicityConforms / EveryProxyResolves /
//    EveryReferenceIsContained / EveryBidirectionalReferenceIsPaired /
//    EveryDataValueConforms / UniqueID / EveryKeyUnique / EveryMapEntryUnique）。
class EValidatorRegistryImpl::EObjectValidatorAdapter : public EValidator {
public:
    bool validate(emf::common::EObject* eObject,
                  emf::common::DiagnosticChain* chain,
                  ValidationContext* context = nullptr) override {
        if (!eObject) return true;
        if (!chain) return true;
        // 委托 validate_EveryDefaultConstraint 直接写入 chain
        return EObjectValidator::validate_EveryDefaultConstraint(eObject, chain, context);
    }
};

EValidatorRegistryImpl::EObjectValidatorAdapter* EValidatorRegistryImpl::defaultAdapter_ = nullptr;

EValidator* EValidatorRegistryImpl::getDefaultEObjectValidator() {
    if (!defaultAdapter_) {
        defaultAdapter_ = new EObjectValidatorAdapter();
    }
    return defaultAdapter_;
}

// ===== ValidationDelegateRegistryImpl =====

void ValidationDelegateRegistryImpl::put(const std::string& nsURI, ValidationDelegate* vd) {
    Entry e;
    e.delegate = vd;
    e.isDescriptor = false;
    map_[nsURI] = e;
}

void ValidationDelegateRegistryImpl::put(const std::string& nsURI,
                                          ValidationDelegateDescriptor* desc) {
    Entry e;
    e.descriptor = desc;
    e.isDescriptor = true;
    map_[nsURI] = e;
}

ValidationDelegate* ValidationDelegateRegistryImpl::getValidationDelegate(const std::string& nsURI) {
    auto it = map_.find(nsURI);
    if (it == map_.end()) return nullptr;
    Entry& e = it->second;
    if (e.isDescriptor && e.descriptor) {
        ValidationDelegate* vd = e.descriptor->getValidationDelegate();
        if (vd) {
            e.delegate = vd;
            e.isDescriptor = false;
        }
        return vd;
    }
    return e.delegate;
}

ValidationDelegate* ValidationDelegateRegistryImpl::delegatedGet(const std::string& nsURI) {
    ValidationDelegate* vd = getValidationDelegate(nsURI);
    if (vd) return vd;
    if (delegate_) return delegate_->delegatedGet(nsURI);
    return nullptr;
}

bool ValidationDelegateRegistryImpl::containsKey(const std::string& nsURI) const {
    if (map_.find(nsURI) != map_.end()) return true;
    if (delegate_) return delegate_->containsKey(nsURI);
    return false;
}

void ValidationDelegateRegistryImpl::remove(const std::string& nsURI) {
    map_.erase(nsURI);
}

}  // namespace emf::ecore::util
