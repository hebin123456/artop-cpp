// EMF Ecore-util: EObjectValidator
// 对齐 org.eclipse.emf.ecore.util.EObjectValidator (Java)
// 19 validate_Xxx 方法 + 16 DIAGNOSTIC_CODE 常量
#pragma once

#include "emf/common/Diagnostic.h"
#include "emf/common/EObject.h"
#include "emf/ecore/EcorePackage.h"
#include <string>
#include <vector>
#include <unordered_map>
#include <any>

namespace emf::ecore::util {

// ===== EObjectValidator DIAGNOSTIC_CODE 常量 =====
// 对齐 EObjectValidator.java 第 63-80 行
class EObjectValidatorCodes {
public:
    static constexpr int EOBJECT__EVERY_MULTIPCITY_CONFORMS = 1;
    static constexpr int EOBJECT__EVERY_DATA_VALUE_CONFORMS = 2;
    static constexpr int EOBJECT__EVERY_REFERENCE_IS_CONTAINED = 3;
    static constexpr int EOBJECT__EVERY_PROXY_RESOLVES = 4;
    static constexpr int DATA_VALUE__VALUE_IN_RANGE = 5;
    static constexpr int DATA_VALUE__LENGTH_IN_RANGE = 6;
    static constexpr int DATA_VALUE__TYPE_CORRECT = 7;
    static constexpr int DATA_VALUE__VALUE_IN_ENUMERATION = 8;
    static constexpr int DATA_VALUE__MATCHES_PATTERN = 9;
    static constexpr int DATA_VALUE__TOTAL_DIGITS_IN_RANGE = 10;
    static constexpr int DATA_VALUE__FRACTION_DIGITS_IN_RANGE = 11;
    static constexpr int EOBJECT__UNIQUE_ID = 12;
    static constexpr int EOBJECT__EVERY_KEY_UNIQUE = 13;
    static constexpr int EOBJECT__EVERY_MAP_ENTRY_UNIQUE = 14;
    static constexpr int EOBJECT__NO_CIRCULAR_CONTAINMENT = 15;
    static constexpr int EOBJECT__EVERY_BIDIRECTIONAL_REFERENCE_IS_PAIRED = 16;

    // DIAGNOSTIC_SOURCE 常量
    static constexpr const char* DIAGNOSTIC_SOURCE = "org.eclipse.emf.ecore";
};

// ===== EObjectValidator 类 =====
// 对齐 EObjectValidator.java
class EObjectValidator {
public:
    // 诊断源
    static constexpr const char* DIAGNOSTIC_SOURCE = "org.eclipse.emf.ecore";
    // ROOT_OBJECT context key
    static constexpr const char* ROOT_OBJECT = "org.eclipse.emf.ecore.EObject_NoCircularContainment";

    // 标签助手
    static std::string getObjectLabel(emf::common::EObject* eObject);
    static std::string getFeatureLabel(emf::ecore::EStructuralFeature* feature);
    static std::string getValueLabel(emf::ecore::EDataType* eDataType, const std::any& value);

    // ===== validate 入口（默认 5 个 validate 重载的轻量版本）=====
    // 旧 API 保留 - 返回 std::vector<Diagnostic>
    static std::vector<emf::common::Diagnostic> validate(emf::common::EObject* obj);
    static std::vector<emf::common::Diagnostic> validateEPackage(emf::ecore::EPackage* pkg);
    static std::vector<emf::common::Diagnostic> validateEClass(emf::ecore::EClass* cls);
    static std::vector<emf::common::Diagnostic> validateEAttribute(emf::ecore::EAttribute* attr);
    static std::vector<emf::common::Diagnostic> validateEReference(emf::ecore::EReference* ref);
    static std::vector<emf::common::Diagnostic> validateEOperation(emf::ecore::EOperation* op);

    // ===== 19 个 validate_Xxx 方法 =====
    // 严格对齐 EObjectValidator.java 中的方法签名

    // 默认约束（所有子约束之和）
    static bool validate_EveryDefaultConstraint(emf::common::EObject* eObject,
                                                emf::common::DiagnosticChain* diagnostics,
                                                std::unordered_map<std::string, std::any>* context);

    // 1. 循环包含检测
    static bool validate_NoCircularContainment(emf::common::EObject* eObject,
                                               emf::common::DiagnosticChain* diagnostics,
                                               std::unordered_map<std::string, std::any>* context);

    // 2. 双向引用（聚合）
    static bool validate_EveryBidirectionalReferenceIsPaired(emf::common::EObject* eObject,
                                                            emf::common::DiagnosticChain* diagnostics,
                                                            std::unordered_map<std::string, std::any>* context);

    // 3. 双向引用（单个）
    static bool validate_BidirectionalReferenceIsPaired(emf::common::EObject* eObject,
                                                        emf::ecore::EReference* eReference,
                                                        emf::ecore::EReference* eOpposite,
                                                        emf::common::DiagnosticChain* diagnostics,
                                                        std::unordered_map<std::string, std::any>* context);

    // 4. 多重数符合（聚合）
    static bool validate_EveryMultiplicityConforms(emf::common::EObject* eObject,
                                                   emf::common::DiagnosticChain* diagnostics,
                                                   std::unordered_map<std::string, std::any>* context);

    // 5. 多重数符合（单个）— protected
    static bool validate_MultiplicityConforms(emf::common::EObject* eObject,
                                              emf::ecore::EStructuralFeature* feature,
                                              emf::common::DiagnosticChain* diagnostics,
                                              std::unordered_map<std::string, std::any>* context);

    // 6. 跨引用 proxy 解析
    static bool validate_EveryProxyResolves(emf::common::EObject* eObject,
                                            emf::common::DiagnosticChain* diagnostics,
                                            std::unordered_map<std::string, std::any>* context);

    // 7. 引用必须被包含
    static bool validate_EveryReferenceIsContained(emf::common::EObject* eObject,
                                                   emf::common::DiagnosticChain* diagnostics,
                                                   std::unordered_map<std::string, std::any>* context);

    // 8. 数据值类型符合（聚合）
    static bool validate_EveryDataValueConforms(emf::common::EObject* eObject,
                                                emf::common::DiagnosticChain* diagnostics,
                                                std::unordered_map<std::string, std::any>* context);

    // 9. 数据值符合（单个）— protected
    static bool validate_DataValueConforms(emf::common::EObject* eObject,
                                           emf::ecore::EAttribute* eAttribute,
                                           emf::common::DiagnosticChain* diagnostics,
                                           std::unordered_map<std::string, std::any>* context);

    // 10. ID 唯一
    static bool validate_UniqueID(emf::common::EObject* eObject,
                                  emf::common::DiagnosticChain* diagnostics,
                                  std::unordered_map<std::string, std::any>* context);

    // 11. EMap key 唯一（聚合）
    static bool validate_EveryKeyUnique(emf::common::EObject* eObject,
                                        emf::common::DiagnosticChain* diagnostics,
                                        std::unordered_map<std::string, std::any>* context);

    // 12. EMap key 唯一（单个）— protected
    static bool validate_KeyUnique(emf::common::EObject* eObject,
                                   emf::ecore::EReference* eReference,
                                   emf::common::DiagnosticChain* diagnostics,
                                   std::unordered_map<std::string, std::any>* context);

    // 13. EMap 整体唯一（聚合）
    static bool validate_EveryMapEntryUnique(emf::common::EObject* eObject,
                                            emf::common::DiagnosticChain* diagnostics,
                                            std::unordered_map<std::string, std::any>* context);

    // 14. EMap 整体唯一（单个）— protected
    static bool validate_MapEntryUnique(emf::common::EObject* eObject,
                                        emf::ecore::EReference* eReference,
                                        emf::common::DiagnosticChain* diagnostics,
                                        std::unordered_map<std::string, std::any>* context);

    // ===== 辅助：构造 BasicDiagnostic =====
    static std::shared_ptr<emf::common::BasicDiagnostic> createDiagnostic(
        emf::common::Diagnostic::Severity severity,
        const std::string& source,
        int code,
        const std::string& message,
        const std::vector<std::shared_ptr<emf::common::Diagnostic>>& data = {});
};

}  // namespace emf::ecore::util
