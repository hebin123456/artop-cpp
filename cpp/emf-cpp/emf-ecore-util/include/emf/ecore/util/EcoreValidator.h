// EMF Ecore-util: EcoreValidator
// 对齐 org.eclipse.emf.ecore.util.EcoreValidator (Java)
// 51+ DIAGNOSTIC_CODE 常量 + 100+ validate 方法
#pragma once

#include "emf/common/Diagnostic.h"
#include "emf/common/EObject.h"
#include "emf/ecore/EcorePackage.h"
#include "emf/ecore/util/EObjectValidator.h"
#include <string>
#include <vector>
#include <unordered_map>
#include <any>

namespace emf::ecore::util {

// alias for backward compat (forward reference, declared at namespace scope to
// allow its use in the class body below)
using ECoreClass = emf::ecore::EClass;

// ===== EcoreValidator DIAGNOSTIC_CODE 常量 =====
// 对齐 EcoreValidator.java 第 93-357 行
class EcoreValidatorCodes {
public:
    // 严格按 Java 的定义顺序和值
    static constexpr int AT_MOST_ONE_ID = 1;
    static constexpr int CONSISTENT_ARGUMENTS_INCORRECT_NUMBER = 2;
    static constexpr int CONSISTENT_ARGUMENTS_INVALID_SUBSTITUTION = 3;
    static constexpr int CONSISTENT_ARGUMENTS_NONE = 4;
    static constexpr int CONSISTENT_ARGUMENTS_NONE_ALLOWED = 5;
    static constexpr int CONSISTENT_BOUNDS = 6;
    static constexpr int CONSISTENT_BOUNDS_NOT_ALLOWED = 7;
    static constexpr int CONSISTENT_BOUNDS_NO_BOUNDS_WITH_TYPE_PARAMETER_OR_CLASSIFIER = 8;
    static constexpr int CONSISTENT_BOUNDS_NO_LOWER_AND_UPPER = 9;
    static constexpr int CONSISTENT_KEYS = 10;
    static constexpr int CONSISTENT_OPPOSITE_BAD_TRANSIENT = 11;
    static constexpr int CONSISTENT_OPPOSITE_BOTH_CONTAINMENT = 12;
    static constexpr int CONSISTENT_OPPOSITE_NOT_FROM_TYPE = 13;
    static constexpr int CONSISTENT_OPPOSITE_NOT_MATCHING = 14;
    static constexpr int CONSISTENT_SUPER_TYPES_CONFLICT = 15;
    static constexpr int CONSISTENT_SUPER_TYPES_DUPLICATE = 16;
    static constexpr int CONSISTENT_TRANSIENT = 17;
    static constexpr int CONSISTENT_TYPE_CLASS_REQUIRED = 18;
    static constexpr int CONSISTENT_TYPE_CLASS_NOT_PERMITTED = 19;
    static constexpr int CONSISTENT_TYPE_DATA_TYPE_NOT_PERMITTED = 20;
    static constexpr int CONSISTENT_TYPE_NO_TYPE_PARAMETER_AND_CLASSIFIER = 21;
    static constexpr int CONSISTENT_TYPE_PRIMITIVE_TYPE_NOT_PERMITTED = 22;
    static constexpr int CONSISTENT_TYPE_TYPE_PARAMETER_NOT_IN_SCOPE = 23;
    static constexpr int CONSISTENT_TYPE_WILDCARD_NOT_PERMITTED = 24;
    static constexpr int INTERFACE_IS_ABSTRACT = 25;
    static constexpr int NO_CIRCULAR_SUPER_TYPES = 26;
    static constexpr int NO_REPEATING_VOID = 27;
    static constexpr int SINGLE_CONTAINER = 28;
    static constexpr int UNIQUE_CLASSIFIER_NAMES = 29;
    static constexpr int UNIQUE_ENUMERATOR_LITERALS = 30;
    static constexpr int UNIQUE_ENUMERATOR_NAMES = 31;
    static constexpr int UNIQUE_FEATURE_NAMES = 32;
    static constexpr int UNIQUE_NS_URIS = 33;
    static constexpr int UNIQUE_OPERATION_SIGNATURES = 34;
    static constexpr int UNIQUE_PARAMETER_NAMES = 35;
    static constexpr int UNIQUE_SUBPACKAGE_NAMES = 36;
    static constexpr int UNIQUE_TYPE_PARAMETER_NAMES = 37;
    static constexpr int VALID_DEFAULT_VALUE_LITERAL = 38;
    static constexpr int VALID_LOWER_BOUND = 39;
    static constexpr int VALID_TYPE = 40;
    static constexpr int VALID_UPPER_BOUND = 41;
    static constexpr int WELL_FORMED_INSTANCE_TYPE_NAME = 42;
    static constexpr int WELL_FORMED_MAP_ENTRY_CLASS = 43;
    static constexpr int WELL_FORMED_NAME = 44;
    static constexpr int WELL_FORMED_NS_PREFIX = 45;
    static constexpr int WELL_FORMED_NS_URI = 46;
    static constexpr int WELL_FORMED_SOURCE_URI = 47;
    static constexpr int DISJOINT_FEATURE_AND_OPERATION_SIGNATURES = 48;
    static constexpr int WELL_FORMED_MAP_ENTRY_NO_INSTANCE_CLASS_NAME = 49;
    static constexpr int CONSISTENT_UNIQUE = 50;
    static constexpr int CONSISTENT_CONTAINER = 51;

    // DIAGNOSTIC_SOURCE 常量
    static constexpr const char* DIAGNOSTIC_SOURCE = "org.eclipse.emf.ecore.model";
};

// ===== EcoreValidator 类 =====
// 对齐 EcoreValidator.java - 继承自 EObjectValidator
class EcoreValidator : public EObjectValidator {
public:
    // DIAGNOSTIC_SOURCE
    static constexpr const char* DIAGNOSTIC_SOURCE = "org.eclipse.emf.ecore.model";
    // context key
    static constexpr const char* STRICT_NAMED_ELEMENT_NAMES = "org.eclipse.emf.ecore.model.ENamedElement_WellFormedName";

    // 旧 API - 返回 std::vector<Diagnostic>
    static std::vector<emf::common::Diagnostic> validate(EPackage* pkg);
    static std::vector<emf::common::Diagnostic> validateEClass(emf::ecore::EClass* cls);

    // ===== 入口：按 classifierID 分发 =====
    // 对应 EcoreValidator.validate(int classifierID, Object value, DiagnosticChain, Map)
    static bool validate(int classifierID, emf::common::EObject* value,
                         emf::common::DiagnosticChain* diagnostics,
                         std::unordered_map<std::string, std::any>* context);

    // ===== 顶层 validateXxx 调度方法（每个元模型类一个）=====
    // EClass
    static bool validateEClass(emf::ecore::EClass* eClass,
                               emf::common::DiagnosticChain* diagnostics,
                               std::unordered_map<std::string, std::any>* context);

    // EAttribute
    static bool validateEAttribute(emf::ecore::EAttribute* eAttribute,
                                   emf::common::DiagnosticChain* diagnostics,
                                   std::unordered_map<std::string, std::any>* context);

    // EAnnotation
    static bool validateEAnnotation(emf::ecore::EAnnotation* eAnnotation,
                                    emf::common::DiagnosticChain* diagnostics,
                                    std::unordered_map<std::string, std::any>* context);

    // EClassifier
    static bool validateEClassifier(emf::ecore::EClassifier* eClassifier,
                                    emf::common::DiagnosticChain* diagnostics,
                                    std::unordered_map<std::string, std::any>* context);

    // EDataType
    static bool validateEDataType(emf::ecore::EDataType* eDataType,
                                  emf::common::DiagnosticChain* diagnostics,
                                  std::unordered_map<std::string, std::any>* context);

    // EEnum
    static bool validateEEnum(emf::ecore::EEnum* eEnum,
                              emf::common::DiagnosticChain* diagnostics,
                              std::unordered_map<std::string, std::any>* context);

    // EEnumLiteral
    static bool validateEEnumLiteral(emf::ecore::EEnumLiteral* eEnumLiteral,
                                      emf::common::DiagnosticChain* diagnostics,
                                      std::unordered_map<std::string, std::any>* context);

    // EFactory
    static bool validateEFactory(emf::ecore::EFactory* eFactory,
                                 emf::common::DiagnosticChain* diagnostics,
                                 std::unordered_map<std::string, std::any>* context);

    // EModelElement
    static bool validateEModelElement(emf::ecore::EModelElement* eModelElement,
                                      emf::common::DiagnosticChain* diagnostics,
                                      std::unordered_map<std::string, std::any>* context);

    // ENamedElement
    static bool validateENamedElement(emf::ecore::ENamedElement* eNamedElement,
                                      emf::common::DiagnosticChain* diagnostics,
                                      std::unordered_map<std::string, std::any>* context);

    // EObject
    static bool validateEObject(emf::common::EObject* eObject,
                                emf::common::DiagnosticChain* diagnostics,
                                std::unordered_map<std::string, std::any>* context);

    // EOperation
    static bool validateEOperation(emf::ecore::EOperation* eOperation,
                                   emf::common::DiagnosticChain* diagnostics,
                                   std::unordered_map<std::string, std::any>* context);

    // EPackage
    static bool validateEPackage(emf::ecore::EPackage* ePackage,
                                 emf::common::DiagnosticChain* diagnostics,
                                 std::unordered_map<std::string, std::any>* context);

    // EParameter
    static bool validateEParameter(emf::ecore::EParameter* eParameter,
                                   emf::common::DiagnosticChain* diagnostics,
                                   std::unordered_map<std::string, std::any>* context);

    // EReference
    static bool validateEReference(emf::ecore::EReference* eReference,
                                   emf::common::DiagnosticChain* diagnostics,
                                   std::unordered_map<std::string, std::any>* context);

    // EStructuralFeature
    static bool validateEStructuralFeature(emf::ecore::EStructuralFeature* eStructuralFeature,
                                          emf::common::DiagnosticChain* diagnostics,
                                          std::unordered_map<std::string, std::any>* context);

    // ETypedElement
    static bool validateETypedElement(emf::ecore::ETypedElement* eTypedElement,
                                      emf::common::DiagnosticChain* diagnostics,
                                      std::unordered_map<std::string, std::any>* context);

    // EGenericType
    static bool validateEGenericType(emf::ecore::EGenericType* eGenericType,
                                     emf::common::DiagnosticChain* diagnostics,
                                     std::unordered_map<std::string, std::any>* context);

    // ETypeParameter
    static bool validateETypeParameter(emf::ecore::ETypeParameter* eTypeParameter,
                                       emf::common::DiagnosticChain* diagnostics,
                                       std::unordered_map<std::string, std::any>* context);

    // EStringToStringMapEntry
    static bool validateEStringToStringMapEntry(std::pair<std::string, std::string>* entry,
                                                 emf::common::DiagnosticChain* diagnostics,
                                                 std::unordered_map<std::string, std::any>* context);

    // ===== EDataType 基础类型（40+ 桩）=====
    static bool validateEBigDecimal(const std::any& value,
                                    emf::common::DiagnosticChain* diagnostics,
                                    std::unordered_map<std::string, std::any>* context);
    static bool validateEBigInteger(const std::any& value,
                                    emf::common::DiagnosticChain* diagnostics,
                                    std::unordered_map<std::string, std::any>* context);
    static bool validateEBoolean(bool value, emf::common::DiagnosticChain* diagnostics,
                                 std::unordered_map<std::string, std::any>* context);
    static bool validateEBooleanObject(const std::any& value,
                                       emf::common::DiagnosticChain* diagnostics,
                                       std::unordered_map<std::string, std::any>* context);
    static bool validateEByte(const std::any& value, emf::common::DiagnosticChain* diagnostics,
                              std::unordered_map<std::string, std::any>* context);
    static bool validateEByteArray(const std::any& value,
                                   emf::common::DiagnosticChain* diagnostics,
                                   std::unordered_map<std::string, std::any>* context);
    static bool validateEByteObject(const std::any& value,
                                    emf::common::DiagnosticChain* diagnostics,
                                    std::unordered_map<std::string, std::any>* context);
    static bool validateEChar(const std::any& value, emf::common::DiagnosticChain* diagnostics,
                              std::unordered_map<std::string, std::any>* context);
    static bool validateECharacterObject(const std::any& value,
                                         emf::common::DiagnosticChain* diagnostics,
                                         std::unordered_map<std::string, std::any>* context);
    static bool validateEDate(const std::any& value, emf::common::DiagnosticChain* diagnostics,
                              std::unordered_map<std::string, std::any>* context);
    static bool validateEDiagnosticChain(const std::any& value,
                                         emf::common::DiagnosticChain* diagnostics,
                                         std::unordered_map<std::string, std::any>* context);
    static bool validateEDouble(double value, emf::common::DiagnosticChain* diagnostics,
                                std::unordered_map<std::string, std::any>* context);
    static bool validateEDoubleObject(const std::any& value,
                                      emf::common::DiagnosticChain* diagnostics,
                                      std::unordered_map<std::string, std::any>* context);
    static bool validateEEList(const std::any& value, emf::common::DiagnosticChain* diagnostics,
                               std::unordered_map<std::string, std::any>* context);
    static bool validateEEnumerator(const std::any& value,
                                    emf::common::DiagnosticChain* diagnostics,
                                    std::unordered_map<std::string, std::any>* context);
    static bool validateEFeatureMap(const std::any& value,
                                    emf::common::DiagnosticChain* diagnostics,
                                    std::unordered_map<std::string, std::any>* context);
    static bool validateEFeatureMapEntry(const std::any& value,
                                         emf::common::DiagnosticChain* diagnostics,
                                         std::unordered_map<std::string, std::any>* context);
    static bool validateEFloat(float value, emf::common::DiagnosticChain* diagnostics,
                               std::unordered_map<std::string, std::any>* context);
    static bool validateEFloatObject(const std::any& value,
                                     emf::common::DiagnosticChain* diagnostics,
                                     std::unordered_map<std::string, std::any>* context);
    static bool validateEInt(int value, emf::common::DiagnosticChain* diagnostics,
                             std::unordered_map<std::string, std::any>* context);
    static bool validateEIntegerObject(const std::any& value,
                                       emf::common::DiagnosticChain* diagnostics,
                                       std::unordered_map<std::string, std::any>* context);
    static bool validateEJavaClass(const std::any& value,
                                   emf::common::DiagnosticChain* diagnostics,
                                   std::unordered_map<std::string, std::any>* context);
    static bool validateEJavaObject(const std::any& value,
                                    emf::common::DiagnosticChain* diagnostics,
                                    std::unordered_map<std::string, std::any>* context);
    static bool validateELong(long long value, emf::common::DiagnosticChain* diagnostics,
                              std::unordered_map<std::string, std::any>* context);
    static bool validateELongObject(const std::any& value,
                                    emf::common::DiagnosticChain* diagnostics,
                                    std::unordered_map<std::string, std::any>* context);
    static bool validateEMap(const std::any& value, emf::common::DiagnosticChain* diagnostics,
                             std::unordered_map<std::string, std::any>* context);
    static bool validateEResource(const std::any& value, emf::common::DiagnosticChain* diagnostics,
                                  std::unordered_map<std::string, std::any>* context);
    static bool validateEResourceSet(const std::any& value,
                                     emf::common::DiagnosticChain* diagnostics,
                                     std::unordered_map<std::string, std::any>* context);
    static bool validateEShort(short value, emf::common::DiagnosticChain* diagnostics,
                               std::unordered_map<std::string, std::any>* context);
    static bool validateEShortObject(const std::any& value,
                                     emf::common::DiagnosticChain* diagnostics,
                                     std::unordered_map<std::string, std::any>* context);
    static bool validateEString(const std::string& value,
                                emf::common::DiagnosticChain* diagnostics,
                                std::unordered_map<std::string, std::any>* context);
    static bool validateETreeIterator(const std::any& value,
                                      emf::common::DiagnosticChain* diagnostics,
                                      std::unordered_map<std::string, std::any>* context);
    static bool validateEInvocationTargetException(const std::any& value,
                                                   emf::common::DiagnosticChain* diagnostics,
                                                   std::unordered_map<std::string, std::any>* context);

    // ===== 60+ validateXxx_Yyy 约束方法（按 EClass 分组）=====

    // --- EClass 系列 (10) ---
    static bool validateEClass_AtMostOneID(emf::ecore::EClass* eClass,
                                           emf::common::DiagnosticChain* diagnostics,
                                           std::unordered_map<std::string, std::any>* context);
    static bool validateEClass_InterfaceIsAbstract(emf::ecore::EClass* eClass,
                                                   emf::common::DiagnosticChain* diagnostics,
                                                   std::unordered_map<std::string, std::any>* context);
    static bool validateEClass_UniqueFeatureNames(emf::ecore::EClass* eClass,
                                                  emf::common::DiagnosticChain* diagnostics,
                                                  std::unordered_map<std::string, std::any>* context);
    static bool validateEClass_UniqueOperationSignatures(emf::ecore::EClass* eClass,
                                                         emf::common::DiagnosticChain* diagnostics,
                                                         std::unordered_map<std::string, std::any>* context);
    static bool validateEClass_NoCircularSuperTypes(emf::ecore::EClass* eClass,
                                                    emf::common::DiagnosticChain* diagnostics,
                                                    std::unordered_map<std::string, std::any>* context);
    static bool validateEClass_WellFormedMapEntryClass(emf::ecore::EClass* eClass,
                                                       emf::common::DiagnosticChain* diagnostics,
                                                       std::unordered_map<std::string, std::any>* context);
    static bool validateEClass_ConsistentSuperTypes(emf::ecore::EClass* eClass,
                                                    emf::common::DiagnosticChain* diagnostics,
                                                    std::unordered_map<std::string, std::any>* context);
    static bool validateEClass_DisjointFeatureAndOperationSignatures(emf::ecore::EClass* eClass,
                                                                     emf::common::DiagnosticChain* diagnostics,
                                                                     std::unordered_map<std::string, std::any>* context);

    // --- EAttribute 系列 (1) ---
    static bool validateEAttribute_ConsistentTransient(emf::ecore::EAttribute* eAttribute,
                                                       emf::common::DiagnosticChain* diagnostics,
                                                       std::unordered_map<std::string, std::any>* context);

    // --- EAnnotation 系列 (2) ---
    static bool validateEAnnotation_WellFormed(emf::ecore::EAnnotation* eAnnotation,
                                                emf::common::DiagnosticChain* diagnostics,
                                                std::unordered_map<std::string, std::any>* context);
    static bool validateEAnnotation_WellFormedSourceURI(emf::ecore::EAnnotation* eAnnotation,
                                                         emf::common::DiagnosticChain* diagnostics,
                                                         std::unordered_map<std::string, std::any>* context);

    // --- EClassifier 系列 (2) ---
    static bool validateEClassifier_WellFormedInstanceTypeName(emf::ecore::EClassifier* eClassifier,
                                                                emf::common::DiagnosticChain* diagnostics,
                                                                std::unordered_map<std::string, std::any>* context);
    static bool validateEClassifier_UniqueTypeParameterNames(emf::ecore::EClassifier* eClassifier,
                                                              emf::common::DiagnosticChain* diagnostics,
                                                              std::unordered_map<std::string, std::any>* context);

    // --- EEnum 系列 (2) ---
    static bool validateEEnum_UniqueEnumeratorNames(emf::ecore::EEnum* eEnum,
                                                     emf::common::DiagnosticChain* diagnostics,
                                                     std::unordered_map<std::string, std::any>* context);
    static bool validateEEnum_UniqueEnumeratorLiterals(emf::ecore::EEnum* eEnum,
                                                       emf::common::DiagnosticChain* diagnostics,
                                                       std::unordered_map<std::string, std::any>* context);

    // --- EOperation 系列 (3) ---
    static bool validateEOperation_UniqueParameterNames(emf::ecore::EOperation* eOperation,
                                                         emf::common::DiagnosticChain* diagnostics,
                                                         std::unordered_map<std::string, std::any>* context);
    static bool validateEOperation_UniqueTypeParameterNames(emf::ecore::EOperation* eOperation,
                                                             emf::common::DiagnosticChain* diagnostics,
                                                             std::unordered_map<std::string, std::any>* context);
    static bool validateEOperation_NoRepeatingVoid(emf::ecore::EOperation* eOperation,
                                                   emf::common::DiagnosticChain* diagnostics,
                                                   std::unordered_map<std::string, std::any>* context);

    // --- EPackage 系列 (5) ---
    static bool validateEPackage_WellFormedNsURI(emf::ecore::EPackage* ePackage,
                                                  emf::common::DiagnosticChain* diagnostics,
                                                  std::unordered_map<std::string, std::any>* context);
    static bool validateEPackage_WellFormedNsPrefix(emf::ecore::EPackage* ePackage,
                                                     emf::common::DiagnosticChain* diagnostics,
                                                     std::unordered_map<std::string, std::any>* context);
    static bool validateEPackage_UniqueSubpackageNames(emf::ecore::EPackage* ePackage,
                                                        emf::common::DiagnosticChain* diagnostics,
                                                        std::unordered_map<std::string, std::any>* context);
    static bool validateEPackage_UniqueClassifierNames(emf::ecore::EPackage* ePackage,
                                                       emf::common::DiagnosticChain* diagnostics,
                                                       std::unordered_map<std::string, std::any>* context);
    static bool validateEPackage_UniqueNsURIs(emf::ecore::EPackage* ePackage,
                                              emf::common::DiagnosticChain* diagnostics,
                                              std::unordered_map<std::string, std::any>* context);

    // --- EReference 系列 (5) ---
    static bool validateEReference_ConsistentOpposite(emf::ecore::EReference* eReference,
                                                       emf::common::DiagnosticChain* diagnostics,
                                                       std::unordered_map<std::string, std::any>* context);
    static bool validateEReference_SingleContainer(emf::ecore::EReference* eReference,
                                                   emf::common::DiagnosticChain* diagnostics,
                                                   std::unordered_map<std::string, std::any>* context);
    static bool validateEReference_ConsistentKeys(emf::ecore::EReference* eReference,
                                                  emf::common::DiagnosticChain* diagnostics,
                                                  std::unordered_map<std::string, std::any>* context);
    static bool validateEReference_ConsistentUnique(emf::ecore::EReference* eReference,
                                                    emf::common::DiagnosticChain* diagnostics,
                                                    std::unordered_map<std::string, std::any>* context);
    static bool validateEReference_ConsistentContainer(emf::ecore::EReference* eReference,
                                                       emf::common::DiagnosticChain* diagnostics,
                                                       std::unordered_map<std::string, std::any>* context);

    // --- EStructuralFeature 系列 (1) ---
    static bool validateEStructuralFeature_ValidDefaultValueLiteral(emf::ecore::EStructuralFeature* eStructuralFeature,
                                                                     emf::common::DiagnosticChain* diagnostics,
                                                                     std::unordered_map<std::string, std::any>* context);

    // --- ETypedElement 系列 (4) ---
    static bool validateETypedElement_ValidLowerBound(emf::ecore::ETypedElement* eTypedElement,
                                                      emf::common::DiagnosticChain* diagnostics,
                                                      std::unordered_map<std::string, std::any>* context);
    static bool validateETypedElement_ValidUpperBound(emf::ecore::ETypedElement* eTypedElement,
                                                      emf::common::DiagnosticChain* diagnostics,
                                                      std::unordered_map<std::string, std::any>* context);
    static bool validateETypedElement_ConsistentBounds(emf::ecore::ETypedElement* eTypedElement,
                                                        emf::common::DiagnosticChain* diagnostics,
                                                        std::unordered_map<std::string, std::any>* context);
    static bool validateETypedElement_ValidType(emf::ecore::ETypedElement* eTypedElement,
                                                emf::common::DiagnosticChain* diagnostics,
                                                std::unordered_map<std::string, std::any>* context);

    // --- ENamedElement 系列 (1) ---
    static bool validateENamedElement_WellFormedName(emf::ecore::ENamedElement* eNamedElement,
                                                      emf::common::DiagnosticChain* diagnostics,
                                                      std::unordered_map<std::string, std::any>* context);

    // --- EGenericType 系列 (3) ---
    static bool validateEGenericType_ConsistentType(emf::ecore::EGenericType* eGenericType,
                                                    emf::common::DiagnosticChain* diagnostics,
                                                    std::unordered_map<std::string, std::any>* context);
    static bool validateEGenericType_ConsistentBounds(emf::ecore::EGenericType* eGenericType,
                                                      emf::common::DiagnosticChain* diagnostics,
                                                      std::unordered_map<std::string, std::any>* context);
    static bool validateEGenericType_ConsistentArguments(emf::ecore::EGenericType* eGenericType,
                                                         emf::common::DiagnosticChain* diagnostics,
                                                         std::unordered_map<std::string, std::any>* context);

    // ===== 辅助方法 =====
    static std::shared_ptr<emf::common::BasicDiagnostic> createDiagnostic(
        emf::common::Diagnostic::Severity severity,
        const std::string& source,
        int code,
        const std::string& message,
        const std::vector<std::shared_ptr<emf::common::Diagnostic>>& data = {});

    static std::string getString(const std::string& key,
                                 const std::vector<std::string>& substitutions = {});

    // Java 工具方法
    static bool isWellFormedURI(const std::string& uri);
    static bool isWellFormedJavaIdentifier(const std::string& name);
    static bool isEffectivelyTransient(emf::ecore::EStructuralFeature* eStructuralFeature);
    static bool isBuiltinEDataType(emf::ecore::EDataType* eDataType);
};

}  // namespace emf::ecore::util
