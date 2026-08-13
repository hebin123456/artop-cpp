// EValidatorRegistryImpl —— 按 EPackage 注册/查找 EValidator
// 对齐 Java: org.eclipse.emf.ecore.util.EValidatorRegistryImpl
//
// Java 的 EValidator.Registry 是 EPackage → EValidator（或 Descriptor）的 map。
// 本实现支持：
//   - 直接注册 EValidator*
//   - 通过 Descriptor 惰性创建（首次 get 时实例化并回写，避免重复创建）
//   - delegate registry 链：当前 registry 未命中时委托 delegate_ 查找
//   - null key fallback：delegatedGet(null) 返回 EObjectValidator::INSTANCE
//
// 注意：与 emf-validation 模块的 EValidator::Registry（全局单例）互补。
// emf-validation 的 Registry 是进程级单例，适合注册全局约束；
// 本 EValidatorRegistryImpl 支持实例化多个、Descriptor 惰性加载、delegate 链，
// 对齐 Java ResourceSet 级别持有独立 registry 的语义。
#pragma once

#include "emf/ecore/EcorePackage.h"
#include "emf/ecore/util/EObjectValidator.h"
#include <any>
#include <functional>
#include <string>
#include <unordered_map>
#include <vector>

namespace emf::ecore::util {

// EValidator 接口（本模块内部使用的轻量接口）
// 对齐 Java org.eclipse.emf.ecore.EValidator
//
// 修复（原 gap：validate 签名无 context Map 参数，调用方无法传 context）：
//   增加 context 参数（默认 nullptr 保持向后兼容），对齐 Java
//   EValidator.validate(EObject, DiagnosticChain, Map<Object, Object>)。
using ValidationContext = std::unordered_map<std::string, std::any>;

class EValidator {
public:
    virtual ~EValidator() = default;
    // 验证 EObject，返回是否通过；失败时向 chain 追加诊断
    // 对齐 Java EValidator.validate(EObject, DiagnosticChain, Map)
    virtual bool validate(emf::common::EObject* eObject,
                          emf::common::DiagnosticChain* chain,
                          ValidationContext* context = nullptr) = 0;
    // 按 classifierID 分派（对齐 Java validate(int, Object, DiagnosticChain, Map)）
    virtual bool validate(int classifierID, emf::common::EObject* eObject,
                          emf::common::DiagnosticChain* chain,
                          ValidationContext* context = nullptr) {
        return validate(eObject, chain, context);
    }
};

// Descriptor：惰性创建 EValidator 的工厂
// 对齐 Java EValidator.Descriptor
class EValidatorDescriptor {
public:
    virtual ~EValidatorDescriptor() = default;
    // 返回 EValidator 实例（调用方不取得所有权；descriptor 内部可缓存）
    virtual EValidator* getEValidator() = 0;
};

// 函数式 Descriptor：用 lambda 创建 EValidator
// 对齐 Java EValidator.Descriptor 的常见匿名实现
class FunctionEValidatorDescriptor : public EValidatorDescriptor {
public:
    using Factory = std::function<EValidator*()>;
    explicit FunctionEValidatorDescriptor(Factory f) : factory_(std::move(f)) {}
    EValidator* getEValidator() override {
        if (!validator_) validator_ = factory_();
        return validator_;
    }
private:
    Factory factory_;
    EValidator* validator_ = nullptr;
};

// EValidatorRegistryImpl：按 EPackage 查找 EValidator
// 对齐 Java org.eclipse.emf.ecore.util.EValidatorRegistryImpl
// extends HashMap<EPackage, Object> implements EValidator.Registry
class EValidatorRegistryImpl {
public:
    EValidatorRegistryImpl() = default;
    ~EValidatorRegistryImpl() = default;

    // 全局单例（对齐 Java EValidator.Registry.INSTANCE，ecore 层）
    // generated <Pkg>Validator 自我注册于此；emf-validation 的 Diagnostician 以其为 fallback 分派
    static EValidatorRegistryImpl& instance();

    // 设置 delegate registry（未命中当前 registry 时委托查找）
    // 对齐 Java setDelegateRegistry
    void setDelegateRegistry(EValidatorRegistryImpl* delegateRegistry) {
        delegate_ = delegateRegistry;
    }

    // 注册 EValidator（直接持有指针，不取得所有权）
    void put(emf::ecore::EPackage* ePackage, EValidator* validator);

    // 注册 Descriptor（惰性创建）
    void put(emf::ecore::EPackage* ePackage, EValidatorDescriptor* descriptor);

    // 获取 EValidator
    // 对齐 Java getEValidator(EPackage)：若值是 Descriptor 则 getEValidator() 并回写
    EValidator* getEValidator(emf::ecore::EPackage* ePackage);

    // delegatedGet：先查自身，未命中查 delegate_；key==null 返回 EObjectValidator::INSTANCE
    // 对齐 Java EValidatorRegistryImpl.delegatedGet
    EValidator* delegatedGet(emf::ecore::EPackage* ePackage);

    // containsKey：含 delegate 检查
    bool containsKey(emf::ecore::EPackage* ePackage) const;

    // remove
    void remove(emf::ecore::EPackage* ePackage);

    // 全局 EObjectValidator 实例（对齐 Java EObjectValidator.INSTANCE）
    // 注意：这是 emf-ecore-util 模块的 EObjectValidator 包装，对外暴露为 EValidator 接口
    static EValidator* getDefaultEObjectValidator();

private:
    // map 值：可能是 EValidator* 或 EValidatorDescriptor*
    struct Entry {
        EValidator* validator = nullptr;
        EValidatorDescriptor* descriptor = nullptr;
        bool isDescriptor = false;
    };
    std::unordered_map<emf::ecore::EPackage*, Entry> map_;
    EValidatorRegistryImpl* delegate_ = nullptr;

    // 默认 EObjectValidator 包装（getDefaultEObjectValidator 返回）
    // 懒初始化
    class EObjectValidatorAdapter;
    static EObjectValidatorAdapter* defaultAdapter_;
};

// ValidationDelegate：按约束 namespace 委托验证
// 对齐 Java org.eclipse.emf.ecore.EValidator.ValidationDelegate
class ValidationDelegate {
public:
    virtual ~ValidationDelegate() = default;
    // 验证 EObject 上指定约束
    // 对齐 Java ValidationDelegate.validate(EClass, EObject, DiagnosticChain, Map, Object)
    virtual bool validate(emf::ecore::EClass* eClass,
                          emf::common::EObject* eObject,
                          emf::common::DiagnosticChain* chain,
                          const std::string& constraint) = 0;
};

// ValidationDelegate.Descriptor
class ValidationDelegateDescriptor {
public:
    virtual ~ValidationDelegateDescriptor() = default;
    virtual ValidationDelegate* getValidationDelegate() = 0;
};

// ValidationDelegateRegistryImpl：按 namespace 查找 ValidationDelegate
// 对齐 Java org.eclipse.emf.ecore.util.ValidationDelegateRegistryImpl
class ValidationDelegateRegistryImpl {
public:
    ValidationDelegateRegistryImpl() = default;
    ~ValidationDelegateRegistryImpl() = default;

    void setDelegateRegistry(ValidationDelegateRegistryImpl* d) { delegate_ = d; }

    void put(const std::string& nsURI, ValidationDelegate* vd);
    void put(const std::string& nsURI, ValidationDelegateDescriptor* desc);

    ValidationDelegate* getValidationDelegate(const std::string& nsURI);
    ValidationDelegate* delegatedGet(const std::string& nsURI);
    bool containsKey(const std::string& nsURI) const;
    void remove(const std::string& nsURI);

private:
    struct Entry {
        ValidationDelegate* delegate = nullptr;
        ValidationDelegateDescriptor* descriptor = nullptr;
        bool isDescriptor = false;
    };
    std::unordered_map<std::string, Entry> map_;
    ValidationDelegateRegistryImpl* delegate_ = nullptr;
};

}  // namespace emf::ecore::util
