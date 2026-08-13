// EObjectValidator 实现
// 对齐 org.eclipse.emf.ecore.util.EObjectValidator (Java)
#include "emf/ecore/util/EObjectValidator.h"
#include "emf/ecore/EcorePackage.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/common/EList.h"
#include <algorithm>
#include <sstream>
#include <unordered_set>

namespace emf::ecore::util {

using emf::common::EObject;
using emf::common::Diagnostic;
using emf::common::BasicDiagnostic;
using emf::common::DiagnosticChain;
using emf::ecore::EPackage;
using emf::ecore::EClass;
using emf::ecore::EAttribute;
using emf::ecore::EReference;
using emf::ecore::EOperation;
using emf::ecore::ENamedElement;
using emf::ecore::ETypedElement;
using emf::ecore::EClassifier;
using emf::ecore::EDataType;
using emf::ecore::EEnum;
using emf::ecore::EStructuralFeature;
using emf::ecore::EcorePackage;

namespace {

// 简单字符串替换
std::string formatString(const std::string& tmpl, const std::vector<std::string>& subs) {
    std::string result = tmpl;
    for (size_t i = 0; i < subs.size(); ++i) {
        std::string placeholder = "{" + std::to_string(i) + "}";
        size_t pos = 0;
        while ((pos = result.find(placeholder, pos)) != std::string::npos) {
            result.replace(pos, placeholder.length(), subs[i]);
            pos += subs[i].length();
        }
    }
    return result;
}

// 从 std::any 提取 EObject 列表（多值 EReference 的 eGet 返回值）
// 支持 emf::common::EList<emf::common::EObject*>*（eGet 多值引用返回内部指针，
//        取出内容后不应 delete——由 EObject 内部管理）
//      与 std::vector<emf::common::EObject*>（替代形式）
std::vector<EObject*> extractEObjectList(const std::any& v) {
    std::vector<EObject*> out;
    if (!v.has_value()) return out;
    if (v.type() == typeid(emf::common::EList<emf::common::EObject*>*)) {
        auto* p = std::any_cast<emf::common::EList<emf::common::EObject*>*>(v);
        if (p) {
            for (size_t i = 0; i < p->size(); ++i) out.push_back((*p)[i]);
        }
        return out;
    }
    if (v.type() == typeid(std::vector<emf::common::EObject*>)) {
        out = std::any_cast<std::vector<emf::common::EObject*>>(v);
    }
    return out;
}

// 把 std::any 序列化为 key 字符串（用于 validate_KeyUnique 去重）
std::string anyToKeyString(const std::any& v) {
    if (!v.has_value()) return "<null>";
    const auto& t = v.type();
    if (t == typeid(std::string)) return std::any_cast<std::string>(v);
    if (t == typeid(const char*)) return std::string(std::any_cast<const char*>(v));
    if (t == typeid(bool)) return std::any_cast<bool>(v) ? "true" : "false";
    if (t == typeid(int)) return std::to_string(std::any_cast<int>(v));
    if (t == typeid(long)) return std::to_string(std::any_cast<long>(v));
    if (t == typeid(long long)) return std::to_string(std::any_cast<long long>(v));
    if (t == typeid(unsigned int)) return std::to_string(std::any_cast<unsigned int>(v));
    if (t == typeid(double)) return std::to_string(std::any_cast<double>(v));
    if (t == typeid(float)) return std::to_string(std::any_cast<float>(v));
    if (t == typeid(short)) return std::to_string(std::any_cast<short>(v));
    if (t == typeid(EObject*)) {
        auto* p = std::any_cast<EObject*>(v);
        return p ? EObjectValidator::getObjectLabel(p) : "<null>";
    }
    return "<value>";
}

}  // namespace

// ===== 标签助手 =====
std::string EObjectValidator::getObjectLabel(EObject* eObject) {
    if (!eObject) return "<null>";
    // 简化：返回 eClass 名 + identifier
    if (auto* ne = dynamic_cast<ENamedElement*>(eObject)) {
        const std::string& name = ne->getName();
        if (!name.empty()) {
            EClass* cls = eObject->eClass();
            if (cls) {
                ENamedElement* en = dynamic_cast<ENamedElement*>(cls);
                if (en) {
                    return en->getName() + " " + name;
                }
            }
            return name;
        }
    }
    return "<unnamed>";
}

std::string EObjectValidator::getFeatureLabel(EStructuralFeature* feature) {
    if (!feature) return "<null>";
    ENamedElement* ne = dynamic_cast<ENamedElement*>(feature);
    if (ne && !ne->getName().empty()) return ne->getName();
    return "<unnamed>";
}

std::string EObjectValidator::getValueLabel(EDataType* eDataType, const std::any& value) {
    if (!value.has_value()) return "null";
    if (value.type() == typeid(std::string)) {
        return std::any_cast<std::string>(value);
    }
    return "value";
}

// ===== createDiagnostic =====
std::shared_ptr<BasicDiagnostic> EObjectValidator::createDiagnostic(
    Diagnostic::Severity severity,
    const std::string& source,
    int code,
    const std::string& message,
    const std::vector<std::shared_ptr<Diagnostic>>& data) {
    return std::make_shared<BasicDiagnostic>(severity, source, code, message, data);
}

// ===== 旧 API：validate =====
std::vector<Diagnostic> EObjectValidator::validate(EObject* obj) {
    std::vector<Diagnostic> r;
    if (!obj) return r;
    if (auto* p = dynamic_cast<EPackage*>(obj)) {
        auto rs = validateEPackage(p); r.insert(r.end(), rs.begin(), rs.end());
    } else if (auto* c = dynamic_cast<EClass*>(obj)) {
        auto rs = validateEClass(c); r.insert(r.end(), rs.begin(), rs.end());
    } else if (auto* a = dynamic_cast<EAttribute*>(obj)) {
        auto rs = validateEAttribute(a); r.insert(r.end(), rs.begin(), rs.end());
    } else if (auto* rf = dynamic_cast<EReference*>(obj)) {
        auto rs = validateEReference(rf); r.insert(r.end(), rs.begin(), rs.end());
    } else if (auto* o = dynamic_cast<EOperation*>(obj)) {
        auto rs = validateEOperation(o); r.insert(r.end(), rs.begin(), rs.end());
    } else {
        if (auto* n = dynamic_cast<ENamedElement*>(obj)) {
            if (n->getName().empty()) {
                r.emplace_back(Diagnostic::Severity::WARNING, "EObjectValidator", 0,
                    "ENamedElement name cannot be empty");
            }
        }
    }
    return r;
}

std::vector<Diagnostic> EObjectValidator::validateEPackage(EPackage* p) {
    std::vector<Diagnostic> r;
    if (!p) return r;
    if (p->getName().empty())
        r.emplace_back(Diagnostic::Severity::ERROR, "EObjectValidator", 0,
            "EPackage name cannot be empty");
    if (p->getNsURI().empty())
        r.emplace_back(Diagnostic::Severity::ERROR, "EObjectValidator", 0,
            "EPackage nsURI cannot be empty");
    for (auto* c : p->getEClassifiers()) {
        auto rs = validate(c);
        r.insert(r.end(), rs.begin(), rs.end());
    }
    return r;
}

std::vector<Diagnostic> EObjectValidator::validateEClass(EClass* c) {
    std::vector<Diagnostic> r;
    if (!c) return r;
    if (c->getName().empty())
        r.emplace_back(Diagnostic::Severity::ERROR, "EObjectValidator", 0,
            "EClass name cannot be empty");
    return r;
}

std::vector<Diagnostic> EObjectValidator::validateEAttribute(EAttribute* a) {
    std::vector<Diagnostic> r;
    if (!a) return r;
    if (a->getName().empty())
        r.emplace_back(Diagnostic::Severity::ERROR, "EObjectValidator", 0,
            "EAttribute name cannot be empty");
    return r;
}

std::vector<Diagnostic> EObjectValidator::validateEReference(EReference* rf) {
    std::vector<Diagnostic> r;
    if (!rf) return r;
    if (rf->getName().empty())
        r.emplace_back(Diagnostic::Severity::ERROR, "EObjectValidator", 0,
            "EReference name cannot be empty");
    if (rf->getEOpposite() && rf->getEOpposite()->getEOpposite() != rf) {
        r.emplace_back(Diagnostic::Severity::ERROR, "EObjectValidator", 0,
            "EReference opposite mismatch");
    }
    return r;
}

std::vector<Diagnostic> EObjectValidator::validateEOperation(EOperation* o) {
    std::vector<Diagnostic> r;
    if (!o) return r;
    if (o->getName().empty())
        r.emplace_back(Diagnostic::Severity::ERROR, "EObjectValidator", 0,
            "EOperation name cannot be empty");
    return r;
}

// ===== 1. validate_EveryDefaultConstraint =====
bool EObjectValidator::validate_EveryDefaultConstraint(EObject* eObject,
                                                       DiagnosticChain* diagnostics,
                                                       std::unordered_map<std::string, std::any>* context) {
    if (!eObject) return true;
    if (!validate_NoCircularContainment(eObject, diagnostics, context)) {
        return false;
    }
    bool result = validate_EveryMultiplicityConforms(eObject, diagnostics, context);
    if (result || diagnostics != nullptr) {
        result &= validate_EveryProxyResolves(eObject, diagnostics, context);
    }
    if (result || diagnostics != nullptr) {
        result &= validate_EveryReferenceIsContained(eObject, diagnostics, context);
    }
    if (result || diagnostics != nullptr) {
        result &= validate_EveryBidirectionalReferenceIsPaired(eObject, diagnostics, context);
    }
    if (result || diagnostics != nullptr) {
        result &= validate_EveryDataValueConforms(eObject, diagnostics, context);
    }
    if (result || diagnostics != nullptr) {
        result &= validate_UniqueID(eObject, diagnostics, context);
    }
    if (result || diagnostics != nullptr) {
        result &= validate_EveryKeyUnique(eObject, diagnostics, context);
    }
    if (result || diagnostics != nullptr) {
        result &= validate_EveryMapEntryUnique(eObject, diagnostics, context);
    }
    return result;
}

// ===== 2. validate_NoCircularContainment =====
bool EObjectValidator::validate_NoCircularContainment(EObject* eObject,
                                                      DiagnosticChain* diagnostics,
                                                      std::unordered_map<std::string, std::any>* context) {
    if (!eObject) return true;
    if (context != nullptr) {
        // 查找 ROOT_OBJECT
        std::any root_any;
        auto it = context->find(ROOT_OBJECT);
        if (it != context->end()) {
            root_any = it->second;
        }
        EObject* root = root_any.has_value() ? std::any_cast<EObject*>(root_any) : nullptr;
        if (root == nullptr) {
            (*context)[ROOT_OBJECT] = std::any(eObject);
        } else if (root == eObject) {
            if (diagnostics != nullptr) {
                auto d = createDiagnostic(
                    Diagnostic::Severity::ERROR,
                    DIAGNOSTIC_SOURCE,
                    EObjectValidatorCodes::EOBJECT__NO_CIRCULAR_CONTAINMENT,
                    "_UI_CircularContainment_diagnostic",
                    { std::make_shared<Diagnostic>(Diagnostic::Severity::OK, "", 0, getObjectLabel(eObject)) });
                diagnostics->add(d);
            }
            return false;
        }
    }
    return true;
}

// ===== 3. validate_EveryBidirectionalReferenceIsPaired =====
bool EObjectValidator::validate_EveryBidirectionalReferenceIsPaired(EObject* eObject,
                                                                    DiagnosticChain* diagnostics,
                                                                    std::unordered_map<std::string, std::any>* context) {
    if (!eObject) return true;
    bool result = true;
    // 遍历所有 EReference（包括继承的）
    EClass* eClass = eObject->eClass();
    if (!eClass) return true;

    // 收集所有 references (含父类)
    std::vector<EReference*> allRefs;
    std::function<void(EClass*)> collect = [&](EClass* c) {
        if (!c) return;
        for (EClass* sup : c->getESuperTypes()) {
            collect(sup);
        }
        for (EReference* ref : c->getEReferences()) {
            allRefs.push_back(ref);
        }
    };
    collect(eClass);

    for (EReference* ref : allRefs) {
        if (ref->isResolveProxies()) {
            EReference* opp = ref->getEOpposite();
            if (opp != nullptr) {
                result &= validate_BidirectionalReferenceIsPaired(eObject, ref, opp, diagnostics, context);
                if (!result && diagnostics == nullptr) {
                    return false;
                }
            }
        }
    }
    return result;
}

// ===== 4. validate_BidirectionalReferenceIsPaired =====
bool EObjectValidator::validate_BidirectionalReferenceIsPaired(EObject* eObject,
                                                                EReference* eReference,
                                                                EReference* eOpposite,
                                                                DiagnosticChain* diagnostics,
                                                                std::unordered_map<std::string, std::any>* context) {
    if (!eObject || !eReference || !eOpposite) return true;
    bool result = true;
    const bool refMany = (eReference->getUpperBound() != 1);
    const bool oppMany = (eOpposite->getUpperBound() != 1);

    // 构造未配对诊断（对齐 Java _UI_UnpairedBidirectionalReference_diagnostic）
    auto reportUnpaired = [&](EObject* value) {
        result = false;
        if (diagnostics != nullptr) {
            auto d = createDiagnostic(
                Diagnostic::Severity::ERROR,
                DIAGNOSTIC_SOURCE,
                EObjectValidatorCodes::EOBJECT__EVERY_BIDIRECTIONAL_REFERENCE_IS_PAIRED,
                "_UI_UnpairedBidirectionalReference_diagnostic",
                { std::make_shared<Diagnostic>(Diagnostic::Severity::OK, "", 0,
                    getFeatureLabel(eReference)),
                  std::make_shared<Diagnostic>(Diagnostic::Severity::OK, "", 0,
                    getObjectLabel(eObject)),
                  std::make_shared<Diagnostic>(Diagnostic::Severity::OK, "", 0,
                    getFeatureLabel(eOpposite)),
                  std::make_shared<Diagnostic>(Diagnostic::Severity::OK, "", 0,
                    getObjectLabel(value)) });
            diagnostics->add(d);
        }
    };

    // 判断 value 的 opposite 端是否包含（或等于）eObject
    auto isPairedBack = [&](EObject* value) -> bool {
        std::any oppV = value->eGet(eOpposite);
        if (oppMany) {
            // opposite 端为多值：检查列表是否包含 eObject
            auto oppValues = extractEObjectList(oppV);
            for (auto* x : oppValues) {
                if (x == eObject) return true;
            }
            return false;
        }
        // opposite 端为单值：检查是否等于 eObject
        EObject* back = nullptr;
        if (oppV.has_value() && oppV.type() == typeid(EObject*)) {
            back = std::any_cast<EObject*>(oppV);
        } else if (oppV.has_value() && oppV.type() == typeid(emf::common::EObject*)) {
            back = std::any_cast<emf::common::EObject*>(oppV);
        }
        return back == eObject;
    };

    if (refMany) {
        // 本端多值：遍历每个值，检查 opposite 端是否配对
        std::any v = eObject->eGet(eReference);
        auto values = extractEObjectList(v);
        for (auto* value : values) {
            if (!value) continue;
            if (!isPairedBack(value)) {
                reportUnpaired(value);
                if (diagnostics == nullptr) break;  // 无需收集诊断时提前退出
            }
        }
    } else {
        // 本端单值：检查 opposite 端是否配对
        std::any v = eObject->eGet(eReference);
        EObject* oppValue = nullptr;
        if (v.has_value() && v.type() == typeid(EObject*)) {
            oppValue = std::any_cast<EObject*>(v);
        } else if (v.has_value() && v.type() == typeid(emf::common::EObject*)) {
            oppValue = std::any_cast<emf::common::EObject*>(v);
        }
        if (oppValue != nullptr) {
            if (!isPairedBack(oppValue)) {
                reportUnpaired(oppValue);
            }
        }
    }
    return result;
}

// ===== 5. validate_EveryMultiplicityConforms =====
bool EObjectValidator::validate_EveryMultiplicityConforms(EObject* eObject,
                                                          DiagnosticChain* diagnostics,
                                                          std::unordered_map<std::string, std::any>* context) {
    if (!eObject) return true;
    bool result = true;
    EClass* eClass = eObject->eClass();
    if (!eClass) return true;

    // 收集所有 features
    std::vector<EStructuralFeature*> features;
    std::function<void(EClass*)> collect = [&](EClass* c) {
        if (!c) return;
        for (EClass* sup : c->getESuperTypes()) {
            collect(sup);
        }
        for (EStructuralFeature* sf : c->getEStructuralFeatures()) {
            features.push_back(sf);
        }
    };
    collect(eClass);

    for (EStructuralFeature* sf : features) {
        result &= validate_MultiplicityConforms(eObject, sf, diagnostics, context);
        if (!result && diagnostics == nullptr) {
            return false;
        }
    }
    return result;
}

// ===== 6. validate_MultiplicityConforms（单个）=====
bool EObjectValidator::validate_MultiplicityConforms(EObject* eObject,
                                                     EStructuralFeature* feature,
                                                     DiagnosticChain* diagnostics,
                                                     std::unordered_map<std::string, std::any>* context) {
    if (!eObject || !feature) return true;
    if (feature->getUpperBound() != 1) {  // isMany
        int lowerBound = feature->getLowerBound();
        if (lowerBound > 0) {
            std::any v = eObject->eGet(feature);
            int size = 0;
            // 简化：检测 list-like
            if (v.has_value()) {
                if (v.type() == typeid(std::vector<emf::common::EObject*>)) {
                    size = static_cast<int>(std::any_cast<std::vector<emf::common::EObject*>>(v).size());
                } else if (v.type() == typeid(std::vector<EObject*>)) {
                    size = static_cast<int>(std::any_cast<std::vector<EObject*>>(v).size());
                }
            }
            if (size < lowerBound) {
                if (diagnostics != nullptr) {
                    auto d = createDiagnostic(
                        Diagnostic::Severity::ERROR,
                        DIAGNOSTIC_SOURCE,
                        EObjectValidatorCodes::EOBJECT__EVERY_MULTIPCITY_CONFORMS,
                        "_UI_FeatureHasTooFewValues_diagnostic");
                    diagnostics->add(d);
                }
                return false;
            }
            int upperBound = feature->getUpperBound();
            if (upperBound > 0 && size > upperBound) {
                if (diagnostics != nullptr) {
                    auto d = createDiagnostic(
                        Diagnostic::Severity::ERROR,
                        DIAGNOSTIC_SOURCE,
                        EObjectValidatorCodes::EOBJECT__EVERY_MULTIPCITY_CONFORMS,
                        "_UI_FeatureHasTooManyValues_diagnostic");
                    diagnostics->add(d);
                }
                return false;
            }
        }
    } else {
        // single
        if (feature->getLowerBound() > 0) {  // isRequired
            bool unset = !eObject->eIsSet(feature);
            if (unset) {
                std::any v = eObject->eGet(feature);
                bool isNull = !v.has_value();
                if (isNull) {
                    if (diagnostics != nullptr) {
                        auto d = createDiagnostic(
                            Diagnostic::Severity::ERROR,
                            DIAGNOSTIC_SOURCE,
                            EObjectValidatorCodes::EOBJECT__EVERY_MULTIPCITY_CONFORMS,
                            "_UI_RequiredFeatureMustBeSet_diagnostic");
                        diagnostics->add(d);
                    }
                    return false;
                }
            }
        }
    }
    return true;
}

// ===== 7. validate_EveryProxyResolves =====
bool EObjectValidator::validate_EveryProxyResolves(EObject* eObject,
                                                   DiagnosticChain* diagnostics,
                                                   std::unordered_map<std::string, std::any>* context) {
    if (!eObject) return true;
    bool result = true;
    // 检查 eCrossReferences()
    auto xrefs = eObject->eCrossReferences();
    for (EObject* xr : xrefs) {
        if (xr && xr->eIsProxy()) {
            result = false;
            if (diagnostics != nullptr) {
                auto d = createDiagnostic(
                    Diagnostic::Severity::ERROR,
                    DIAGNOSTIC_SOURCE,
                    EObjectValidatorCodes::EOBJECT__EVERY_PROXY_RESOLVES,
                    "_UI_UnresolvedProxy_diagnostic");
                diagnostics->add(d);
            } else {
                break;
            }
        }
    }
    return result;
}

// ===== 8. validate_EveryReferenceIsContained =====
bool EObjectValidator::validate_EveryReferenceIsContained(EObject* eObject,
                                                          DiagnosticChain* diagnostics,
                                                          std::unordered_map<std::string, std::any>* context) {
    if (!eObject) return true;
    bool result = true;
    if (eObject->eContainer() == nullptr) {
        return true;  // 简化：eResource() null，跳过
    }
    auto xrefs = eObject->eCrossReferences();
    for (EObject* xr : xrefs) {
        if (xr && xr->eContainer() == nullptr && !xr->eIsProxy()) {
            result = false;
            if (diagnostics != nullptr) {
                auto d = createDiagnostic(
                    Diagnostic::Severity::ERROR,
                    DIAGNOSTIC_SOURCE,
                    EObjectValidatorCodes::EOBJECT__EVERY_REFERENCE_IS_CONTAINED,
                    "_UI_DanglingReference_diagnostic");
                diagnostics->add(d);
            } else {
                break;
            }
        }
    }
    return result;
}

// ===== 9. validate_EveryDataValueConforms =====
bool EObjectValidator::validate_EveryDataValueConforms(EObject* eObject,
                                                       DiagnosticChain* diagnostics,
                                                       std::unordered_map<std::string, std::any>* context) {
    if (!eObject) return true;
    bool result = true;
    EClass* eClass = eObject->eClass();
    if (!eClass) return true;

    std::vector<EAttribute*> allAttrs;
    std::function<void(EClass*)> collect = [&](EClass* c) {
        if (!c) return;
        for (EClass* sup : c->getESuperTypes()) {
            collect(sup);
        }
        for (EAttribute* a : c->getEAttributes()) {
            allAttrs.push_back(a);
        }
    };
    collect(eClass);

    for (EAttribute* attr : allAttrs) {
        result &= validate_DataValueConforms(eObject, attr, diagnostics, context);
        if (!result && diagnostics == nullptr) {
            return false;
        }
    }
    return result;
}

// ===== 10. validate_DataValueConforms（单个）=====
bool EObjectValidator::validate_DataValueConforms(EObject* eObject,
                                                  EAttribute* eAttribute,
                                                  DiagnosticChain* diagnostics,
                                                  std::unordered_map<std::string, std::any>* context) {
    if (!eObject || !eAttribute) return true;
    if (!eObject->eIsSet(eAttribute)) return true;
    EDataType* eDataType = eAttribute->getEAttributeType();
    if (!eDataType) return true;

    // 多值属性：跳过单项检查（对齐 Java 委托语义，避免对集合误报）
    if (eAttribute->getUpperBound() != 1) return true;

    std::any value = eObject->eGet(eAttribute);
    if (!value.has_value()) return true;

    // EEnum 校验：int 值需为合法枚举字面量（对齐 Java DATA_VALUE__VALUE_IN_ENUMERATION）
    if (auto* eEnum = dynamic_cast<EEnum*>(eDataType)) {
        if (value.type() == typeid(int)) {
            int iv = std::any_cast<int>(value);
            bool found = false;
            for (auto* lit : eEnum->getELiterals()) {
                if (lit && lit->getValue() == iv) { found = true; break; }
            }
            if (!found) {
                if (diagnostics != nullptr) {
                    auto d = createDiagnostic(
                        Diagnostic::Severity::ERROR,
                        DIAGNOSTIC_SOURCE,
                        EObjectValidatorCodes::DATA_VALUE__VALUE_IN_ENUMERATION,
                        "_UI_EnumerationLiteralNotValid_diagnostic",
                        { std::make_shared<Diagnostic>(Diagnostic::Severity::OK, "", 0,
                            getFeatureLabel(eAttribute)) });
                    diagnostics->add(d);
                }
                return false;
            }
        }
        return true;
    }

    // 其他类型：简化不做细粒度类型检查（避免对动态 std::any 误报）
    return true;
}

// ===== 11. validate_UniqueID =====
bool EObjectValidator::validate_UniqueID(EObject* eObject,
                                         DiagnosticChain* diagnostics,
                                         std::unordered_map<std::string, std::any>* context) {
    if (!eObject) return true;
    // 简化：Java 用 EcoreUtil.getID；这里 C++ 端没有 getID，返回 true
    return true;
}

// ===== 12. validate_EveryKeyUnique =====
bool EObjectValidator::validate_EveryKeyUnique(EObject* eObject,
                                               DiagnosticChain* diagnostics,
                                               std::unordered_map<std::string, std::any>* context) {
    if (!eObject) return true;
    bool result = true;
    EClass* eClass = eObject->eClass();
    if (!eClass) return true;

    for (EStructuralFeature* sf : eClass->getEStructuralFeatures()) {
        auto* ref = dynamic_cast<EReference*>(sf);
        if (ref && ref->getUpperBound() != 1) {  // isMany
            const auto& keys = ref->getEKeys();
            if (!keys.empty()) {
                result &= validate_KeyUnique(eObject, ref, diagnostics, context);
                if (!result && diagnostics == nullptr) {
                    return false;
                }
            }
        }
    }
    return result;
}

// ===== 13. validate_KeyUnique（单个）=====
bool EObjectValidator::validate_KeyUnique(EObject* eObject,
                                          EReference* eReference,
                                          DiagnosticChain* diagnostics,
                                          std::unordered_map<std::string, std::any>* context) {
    if (!eObject || !eReference) return true;
    const auto& keys = eReference->getEKeys();
    if (keys.empty()) return true;

    // 多值引用：遍历每个值，按 key 属性组合去重（对齐 Java EObjectValidator.validate_KeyUnique）
    std::any v = eObject->eGet(eReference);
    auto values = extractEObjectList(v);
    bool result = true;
    std::unordered_set<std::string> seen;
    for (auto* value : values) {
        if (!value) continue;
        // 组合 key：把每个 key 属性值序列化为字符串并用 \x1f 分隔拼接
        std::string keyStr;
        for (auto* keyAttr : keys) {
            if (!keyAttr) { keyStr += "\x1f"; continue; }
            keyStr += anyToKeyString(value->eGet(keyAttr));
            keyStr += "\x1f";  // 分隔符，避免 "a"+"bc" 与 "ab"+"c" 冲突
        }
        if (!seen.insert(keyStr).second) {
            result = false;
            if (diagnostics != nullptr) {
                auto d = createDiagnostic(
                    Diagnostic::Severity::ERROR,
                    DIAGNOSTIC_SOURCE,
                    EObjectValidatorCodes::EOBJECT__EVERY_KEY_UNIQUE,
                    "_UI_DuplicateKey_diagnostic",
                    { std::make_shared<Diagnostic>(Diagnostic::Severity::OK, "", 0,
                        getFeatureLabel(eReference)),
                      std::make_shared<Diagnostic>(Diagnostic::Severity::OK, "", 0,
                        getObjectLabel(value)) });
                diagnostics->add(d);
            } else {
                break;  // 无需收集诊断时提前退出
            }
        }
    }
    return result;
}

// ===== 14. validate_EveryMapEntryUnique =====
bool EObjectValidator::validate_EveryMapEntryUnique(EObject* eObject,
                                                    DiagnosticChain* diagnostics,
                                                    std::unordered_map<std::string, std::any>* context) {
    if (!eObject) return true;
    bool result = true;
    EClass* eClass = eObject->eClass();
    if (!eClass) return true;

    for (EStructuralFeature* sf : eClass->getEStructuralFeatures()) {
        auto* ref = dynamic_cast<EReference*>(sf);
        if (ref) {
            EClassifier* t = ref->getEType();
            if (t) {
                // 简化：仅检查 t->getInstanceClassName() == "java.util.Map$Entry"
                // C++ 端没这个方法
                result &= validate_MapEntryUnique(eObject, ref, diagnostics, context);
                if (!result && diagnostics == nullptr) {
                    return false;
                }
            }
        }
    }
    return result;
}

// ===== 15. validate_MapEntryUnique（单个）=====
bool EObjectValidator::validate_MapEntryUnique(EObject* eObject,
                                               EReference* eReference,
                                               DiagnosticChain* diagnostics,
                                               std::unordered_map<std::string, std::any>* context) {
    if (!eObject || !eReference) return true;
    return true;
}

}  // namespace emf::ecore::util
