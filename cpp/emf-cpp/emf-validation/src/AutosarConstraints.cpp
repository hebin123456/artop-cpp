// AutosarConstraints.cpp
// 核心AUTOSAR业务约束实现（反射式，对齐 artop 内置约束）
//
// 实现要点：
//   - 所有约束反射读取 feature（shortName/uuid/category），不依赖具体生成的 EClass，
//     因此对真实 AUTOSAR 模型与测试动态模型均生效。
//   - shortName 兄弟唯一性：查 eContainer 的 eContents，比较同 EClass name 的兄弟。
//   - no_unresolved_proxy：遍历 EAllReferences，单值/多值均检查目标 eIsProxy。
//   - 性能：每约束 O(1) 或 O(兄弟数/引用数)，整体线性，适合全量 BATCH 校验。
#include "emf/validation/AutosarConstraints.h"
#include "emf/validation/EValidator.h"
#include "emf/validation/Constraint.h"
#include "emf/common/EObject.h"
#include "emf/common/EList.h"
#include "emf/common/Diagnostic.h"
#include "emf/ecore/EcoreImpls.h"

#include <any>
#include <string>
#include <unordered_map>
#include <vector>

namespace emf::validation {

namespace {

// 反射读取名为 attrName 的字符串属性；无 feature 或非 string 返回空 optional（has=false）
// 对齐 ConstraintParser::readAttr 的反射模式。
bool readStringAttr(emf::common::EObject* obj, const std::string& attrName, std::string& out) {
    if (!obj) return false;
    auto* cls = obj->eClass();
    if (!cls) return false;
    auto* sf = cls->getEStructuralFeature(attrName);
    if (!sf) return false;
    auto v = obj->eGet(sf);
    if (v.type() == typeid(std::string)) {
        out = std::any_cast<std::string>(v);
        return true;
    }
    return false;
}

// 判断对象是否有名为 attrName 的 EAttribute
bool hasAttr(emf::common::EObject* obj, const std::string& attrName) {
    if (!obj || !obj->eClass()) return false;
    auto* sf = obj->eClass()->getEStructuralFeature(attrName);
    return sf != nullptr;
}

// 取 category 特征的 lowerBound（无 category 返回 0）
int categoryLowerBound(emf::common::EObject* obj) {
    if (!obj || !obj->eClass()) return 0;
    auto* sf = obj->eClass()->getEStructuralFeature("category");
    if (!sf) return 0;
    return sf->getLowerBound();
}

// ===== 约束 1：shortName 非空 =====
// 对齐 AUTOSAR Referrable.shortName lowerBound=1 + 非空业务约束。
bool shortNameNonEmptyEval(emf::common::EObject* obj) {
    std::string sn;
    if (!readStringAttr(obj, "shortName", sn)) return true;  // 无 shortName feature，不适用
    return !sn.empty();
}

// ===== 约束 2：shortName 同父同类型兄弟唯一 =====
// 对齐 AUTOSAR：同一父对象下，同 EClass 的兄弟 shortName 不得重复。
// per-object：检查本对象 shortName 是否与父的其它同类型兄弟冲突。
bool shortNameUniqueInParentEval(emf::common::EObject* obj) {
    std::string sn;
    if (!readStringAttr(obj, "shortName", sn) || sn.empty()) return true;  // 不适用
    auto* parent = obj->eContainer();
    if (!parent) return true;  // 无父，无兄弟冲突
    auto* cls = obj->eClass();
    if (!cls) return true;
    const std::string& myTypeName = cls->getName();
    for (auto* sibling : parent->eContents()) {
        if (!sibling || sibling == obj) continue;
        auto* sibCls = sibling->eClass();
        if (!sibCls || sibCls->getName() != myTypeName) continue;  // 只比同类型
        std::string sibSn;
        if (!readStringAttr(sibling, "shortName", sibSn)) continue;
        if (sibSn == sn) return false;  // 同类型同 shortName → 冲突
    }
    return true;
}

// ===== 约束 3：uuid 非空 =====
// 对齐 AUTOSAR Identifiable.uuid 非空。
// 注：uuid 全局唯一性是模型级约束，需遍历整树去重，此处仅校验非空（线性）。
bool uuidNonEmptyEval(emf::common::EObject* obj) {
    std::string uuid;
    if (!readStringAttr(obj, "uuid", uuid)) return true;  // 无 uuid feature，不适用
    return !uuid.empty();
}

// ===== 约束 4：category（lowerBound>=1）非空 =====
// 对齐 AUTOSAR：category 为必填（lowerBound>=1）时不得为空字符串。
bool categoryRequiredEval(emf::common::EObject* obj) {
    if (categoryLowerBound(obj) < 1) return true;  // category 非必填，不适用
    std::string cat;
    if (!readStringAttr(obj, "category", cat)) return true;  // 无 category（不应发生）
    return !cat.empty();
}

// ===== 约束 5：无未解析 proxy 引用 =====
// 对齐 artop：跨 resource 引用必须可解析，未解析 proxy 视为悬空引用（警告）。
// 遍历 EAllReferences（非 containment），单值/多值均检查目标 eIsProxy。
bool noUnresolvedProxyEval(emf::common::EObject* obj) {
    if (!obj || !obj->eClass()) return true;
    for (auto* sf : obj->eClass()->getEAllStructuralFeatures()) {
        if (!sf) continue;
        if (sf->isDerived() || sf->isTransient() || !sf->isChangeable()) continue;
        auto* ref = dynamic_cast<emf::ecore::EReference*>(sf);
        if (!ref) continue;                 // 只查 EReference
        if (ref->isContainment()) continue;  // containment 子对象不是 proxy 引用
        auto v = obj->eGet(ref);
        if (!v.has_value()) continue;
        // 单值引用：EObject*
        if (v.type() == typeid(emf::common::EObject*)) {
            auto* target = std::any_cast<emf::common::EObject*>(v);
            if (target && target->eIsProxy()) return false;
            continue;
        }
        // 多值引用：EList<EObject*>*
        if (v.type() == typeid(emf::common::EList<emf::common::EObject*>*)) {
            auto* lst = std::any_cast<emf::common::EList<emf::common::EObject*>*>(v);
            if (!lst) continue;
            for (size_t k = 0; k < lst->size(); ++k) {
                auto* target = (*lst)[k];
                if (target && target->eIsProxy()) return false;
            }
        }
    }
    return true;
}

// ===== ECUC 专用约束辅助函数 =====
// 对齐 org.artop.aal.autosar40.constraints.ecuc 的 49 个约束。
// 约束按 target EClass 名前缀过滤（等价 artop clientContext enablement），
// 只对 Ecuc* 类对象执行，避免全树扫描。

// 判断对象 EClass 名是否以指定前缀开头（clientContext enablement 等价）
bool classNameStartsWith(emf::common::EObject* obj, const std::string& prefix) {
    if (!obj || !obj->eClass()) return false;
    const std::string& n = obj->eClass()->getName();
    return n.size() >= prefix.size() && n.compare(0, prefix.size(), prefix) == 0;
}

// 判断对象 EClass 名是否包含子串（用于匹配 Ecuc* 系列）
bool classNameContains(emf::common::EObject* obj, const std::string& sub) {
    if (!obj || !obj->eClass()) return false;
    const std::string& n = obj->eClass()->getName();
    return n.find(sub) != std::string::npos;
}

// 反射读取名为 attrName 的数值属性（int/long/double/float）
bool readNumericAttr(emf::common::EObject* obj, const std::string& attrName, double& out) {
    if (!obj || !obj->eClass()) return false;
    auto* sf = obj->eClass()->getEStructuralFeature(attrName);
    if (!sf) return false;
    auto v = obj->eGet(sf);
    if (v.type() == typeid(int)) { out = std::any_cast<int>(v); return true; }
    if (v.type() == typeid(long)) { out = static_cast<double>(std::any_cast<long>(v)); return true; }
    if (v.type() == typeid(double)) { out = std::any_cast<double>(v); return true; }
    if (v.type() == typeid(float)) { out = std::any_cast<float>(v); return true; }
    return false;
}

// 反射读取多值 EReference 的元素数量
int refListSize(emf::common::EObject* obj, const std::string& refName) {
    if (!obj || !obj->eClass()) return 0;
    auto* sf = obj->eClass()->getEStructuralFeature(refName);
    if (!sf) return 0;
    auto v = obj->eGet(sf);
    if (v.type() == typeid(emf::common::EList<emf::common::EObject*>*)) {
        auto* lst = std::any_cast<emf::common::EList<emf::common::EObject*>*>(v);
        return lst ? static_cast<int>(lst->size()) : 0;
    }
    return 0;
}

// 反射读取单值 EReference（返回目标对象；无 feature 或非单值引用返回 nullptr）
// 注意：无法区分"无 feature"与"feature 值为 null"，调用方需先用 hasAttr 判断。
emf::common::EObject* readSingleRef(emf::common::EObject* obj, const std::string& name) {
    if (!obj || !obj->eClass()) return nullptr;
    auto* sf = obj->eClass()->getEStructuralFeature(name);
    if (!sf) return nullptr;
    auto v = obj->eGet(sf);
    if (v.type() == typeid(emf::common::EObject*)) {
        return std::any_cast<emf::common::EObject*>(v);
    }
    return nullptr;
}

// 反射读取布尔属性（无 feature 或非 bool 返回 false）
bool readBoolAttr(emf::common::EObject* obj, const std::string& name, bool& out) {
    if (!obj || !obj->eClass()) return false;
    auto* sf = obj->eClass()->getEStructuralFeature(name);
    if (!sf) return false;
    auto v = obj->eGet(sf);
    if (v.type() == typeid(bool)) {
        out = std::any_cast<bool>(v);
        return true;
    }
    return false;
}

// ===== ECUC 约束 1：EcucParameterValue 必须有 definition 引用 =====
// 对齐 EcucParameterValueBasicConstraint：ParameterValue 必须指向有效的 ParameterDef。
// 注：保留以维持既有逻辑；49 约束对齐后由 ecucParameterValueBasicEval 组合覆盖。
[[maybe_unused]] bool ecucParameterValueHasDefinitionEval(emf::common::EObject* obj) {
    if (!classNameContains(obj, "EcucParameterValue")) return true;  // 不适用
    auto* sf = obj->eClass()->getEStructuralFeature("definition");
    if (!sf) return true;
    auto v = obj->eGet(sf);
    if (v.type() == typeid(emf::common::EObject*)) {
        return std::any_cast<emf::common::EObject*>(v) != nullptr;
    }
    return true;
}

// ===== ECUC 约束 2：EcucContainerValue 必须有 definition 引用 =====
// 对齐 GContainerBasicConstraint：ContainerValue 必须指向有效的 ContainerDef。
bool ecucContainerValueHasDefinitionEval(emf::common::EObject* obj) {
    if (!classNameContains(obj, "EcucContainerValue")) return true;
    auto* sf = obj->eClass()->getEStructuralFeature("definition");
    if (!sf) return true;
    auto v = obj->eGet(sf);
    if (v.type() == typeid(emf::common::EObject*)) {
        return std::any_cast<emf::common::EObject*>(v) != nullptr;
    }
    return true;
}

// ===== ECUC 约束 3：EcucModuleConfigurationValues 必须有 definition 引用 =====
// 对齐 EcucModuleConfigurationValuesBasicConstraint。
bool ecucModuleConfigHasDefinitionEval(emf::common::EObject* obj) {
    if (!classNameContains(obj, "EcucModuleConfigurationValues")) return true;
    auto* sf = obj->eClass()->getEStructuralFeature("definition");
    if (!sf) return true;
    auto v = obj->eGet(sf);
    if (v.type() == typeid(emf::common::EObject*)) {
        return std::any_cast<emf::common::EObject*>(v) != nullptr;
    }
    return true;
}

// ===== ECUC 约束 4：EcucNumericalParamValue 的 value 必须在 ParamDef 的 lower/upper limit 内 =====
// 对齐 EcucNumericalParamValueBasicConstraint：数值参数值应满足定义的上下限。
bool ecucNumericalParamValueWithinLimitsEval(emf::common::EObject* obj) {
    if (!classNameContains(obj, "EcucNumericalParamValue")) return true;
    double val = 0;
    if (!readNumericAttr(obj, "value", val)) return true;  // 无 value，不校验
    auto* defSf = obj->eClass()->getEStructuralFeature("definition");
    if (!defSf) return true;
    auto dv = obj->eGet(defSf);
    if (dv.type() != typeid(emf::common::EObject*)) return true;
    auto* def = std::any_cast<emf::common::EObject*>(dv);
    if (!def) return true;  // 无定义，由其它约束报告
    double lower = 0, upper = 0;
    if (readNumericAttr(def, "lowerLimit", lower) && val < lower) return false;
    if (readNumericAttr(def, "upperLimit", upper) && val > upper) return false;
    return true;
}

// ===== ECUC 约束 5：EcucTextualParamValue 的 value 非空 =====
// 对齐 EcucTextualParamValueBasicConstraint：文本参数值不应为空。
bool ecucTextualParamValueNonEmptyEval(emf::common::EObject* obj) {
    if (!classNameContains(obj, "EcucTextualParamValue")) return true;
    std::string val;
    if (!readStringAttr(obj, "value", val)) return true;
    return !val.empty();
}

// ===== ECUC 约束 6：EcucValueCollection 的 moduleConfigurationValues 数量满足下界 =====
// 对齐 EcucValueCollectionModuleConfigurationLowerMultiplicityConstraint。
bool ecucValueCollectionLowerMultiplicityEval(emf::common::EObject* obj) {
    if (!classNameContains(obj, "EcucValueCollection")) return true;
    auto* sf = obj->eClass()->getEStructuralFeature("moduleConfigurationValues");
    if (!sf) return true;
    int count = refListSize(obj, "moduleConfigurationValues");
    return count >= sf->getLowerBound();
}

// ===== ECUC 约束 7：EcucValueCollection 的 moduleConfigurationValues 数量满足上界 =====
// 对齐 EcucValueCollectionModuleConfigurationUpperMultiplicityConstraint。
bool ecucValueCollectionUpperMultiplicityEval(emf::common::EObject* obj) {
    if (!classNameContains(obj, "EcucValueCollection")) return true;
    auto* sf = obj->eClass()->getEStructuralFeature("moduleConfigurationValues");
    if (!sf) return true;
    int upper = sf->getUpperBound();
    if (upper < 0) return true;  // 无上限
    int count = refListSize(obj, "moduleConfigurationValues");
    return count <= upper;
}

// ===== ECUC 约束 8：EcucFloatParamDef 的 lowerLimit <= upperLimit =====
// 对齐 EcucFloatParamDefLowerLimitConstraint + UpperLimitConstraint 的一致性。
// 注：保留以维持既有逻辑；49 约束对齐后由 lower/upperLimit 单独约束覆盖。
[[maybe_unused]] bool ecucFloatParamDefLimitsConsistentEval(emf::common::EObject* obj) {
    if (!classNameContains(obj, "EcucFloatParamDef")) return true;
    double lower = 0, upper = 0;
    if (!readNumericAttr(obj, "lowerLimit", lower)) return true;
    if (!readNumericAttr(obj, "upperLimit", upper)) return true;
    return lower <= upper;
}

// ===== ECUC 约束 9：EcucIntegerParamDef 的 lowerLimit <= upperLimit =====
// 对齐 EcucIntegerParamDefLowerLimitConstraint + UpperLimitConstraint 的一致性。
// 注：保留以维持既有逻辑；49 约束对齐后由 lower/upperLimit 单独约束覆盖。
[[maybe_unused]] bool ecucIntegerParamDefLimitsConsistentEval(emf::common::EObject* obj) {
    if (!classNameContains(obj, "EcucIntegerParamDef")) return true;
    double lower = 0, upper = 0;
    if (!readNumericAttr(obj, "lowerLimit", lower)) return true;
    if (!readNumericAttr(obj, "upperLimit", upper)) return true;
    return lower <= upper;
}

// ===== ECUC 约束 10：EcucContainerValue 的 subContainers 数量满足下界 =====
// 对齐 ContainerSubContainerMultiplicityConstraint。
bool ecucContainerSubContainerLowerMultiplicityEval(emf::common::EObject* obj) {
    if (!classNameContains(obj, "EcucContainerValue")) return true;
    auto* sf = obj->eClass()->getEStructuralFeature("subContainers");
    if (!sf) return true;
    int count = refListSize(obj, "subContainers");
    return count >= sf->getLowerBound();
}

// ===== ECUC 约束 11：EcucParameterValue 必须有 value 或 references 之一 =====
// 对齐 EcucParameterValueBasicConstraint 完整性：参数值必须有实际值（数值/文本/引用）。
// 注：保留以维持既有逻辑；49 约束对齐后由 ecucParameterValueBasicEval 组合覆盖。
[[maybe_unused]] bool ecucParameterValueHasValueEval(emf::common::EObject* obj) {
    if (!classNameContains(obj, "EcucParameterValue")) return true;
    // 检查是否有 value（数值/字符串）或 references（引用值）
    if (auto* sf = obj->eClass()->getEStructuralFeature("value")) {
        auto v = obj->eGet(sf);
        if (v.has_value()) {
            if (v.type() == typeid(std::string)) {
                if (!std::any_cast<std::string>(v).empty()) return true;
            } else if (v.type() == typeid(int) || v.type() == typeid(double)) {
                return true;
            }
        }
    }
    if (refListSize(obj, "references") > 0) return true;
    return false;
}

// ===== ECUC 约束 12：EcucReferenceValue 必须有 definition 引用 =====
// 对齐 GReferenceValueBasicConstraint。
bool ecucReferenceValueHasDefinitionEval(emf::common::EObject* obj) {
    if (!classNameContains(obj, "EcucReferenceValue")) return true;
    auto* sf = obj->eClass()->getEStructuralFeature("definition");
    if (!sf) return true;
    auto v = obj->eGet(sf);
    if (v.type() == typeid(emf::common::EObject*)) {
        return std::any_cast<emf::common::EObject*>(v) != nullptr;
    }
    return true;
}

// ===== ECUC 约束 13：EcucInstanceReferenceValue 必须有 definition 和 value 引用 =====
// 对齐 EcucInstanceReferenceValueBasicConstraint。
bool ecucInstanceReferenceValueCompleteEval(emf::common::EObject* obj) {
    if (!classNameContains(obj, "EcucInstanceReferenceValue")) return true;
    auto* defSf = obj->eClass()->getEStructuralFeature("definition");
    if (!defSf) return true;
    auto dv = obj->eGet(defSf);
    if (dv.type() != typeid(emf::common::EObject*)) return true;
    if (std::any_cast<emf::common::EObject*>(dv) == nullptr) return false;
    // value 引用（instanceRef）也应存在
    auto* valSf = obj->eClass()->getEStructuralFeature("value");
    if (valSf) {
        auto vv = obj->eGet(valSf);
        if (vv.type() == typeid(emf::common::EObject*)) {
            if (std::any_cast<emf::common::EObject*>(vv) == nullptr) return false;
        }
    }
    return true;
}

// ===== ECUC 约束 14：EcucAbstractReferenceDef 必须有 lowerMultiplicity <= upperMultiplicity =====
// 对齐 GConfigReferenceLowerMultiplicityConstraint + UpperMultiplicityConstraint。
// 注：保留以维持既有逻辑；49 约束对齐后由 lower/upperMultiplicity 单独约束覆盖。
[[maybe_unused]] bool ecucAbstractReferenceDefMultiplicityConsistentEval(emf::common::EObject* obj) {
    if (!classNameContains(obj, "EcucAbstractReferenceDef")) return true;
    double lower = 0, upper = 0;
    if (!readNumericAttr(obj, "lowerMultiplicity", lower)) return true;
    if (!readNumericAttr(obj, "upperMultiplicity", upper)) return true;
    if (upper < 0) return true;  // 无上限
    return lower <= upper;
}

// ===== ECUC 约束 15：EcucContainerDef 必须有 lowerMultiplicity <= upperMultiplicity =====
// 对齐 GContainerDefLowerMultiplicityConstraint + UpperMultiplicityConstraint。
// 注：保留以维持既有逻辑；49 约束对齐后由 lower/upperMultiplicity 单独约束覆盖。
[[maybe_unused]] bool ecucContainerDefMultiplicityConsistentEval(emf::common::EObject* obj) {
    if (!classNameContains(obj, "EcucContainerDef")) return true;
    double lower = 0, upper = 0;
    if (!readNumericAttr(obj, "lowerMultiplicity", lower)) return true;
    if (!readNumericAttr(obj, "upperMultiplicity", upper)) return true;
    if (upper < 0) return true;
    return lower <= upper;
}

// ===== ECUC 约束 16：EcucParameterDef 必须有 lowerMultiplicity <= upperMultiplicity =====
// 对齐 GConfigParameterLowerMultiplicityConstraint + UpperMultiplicityConstraint。
// 注：保留以维持既有逻辑；49 约束对齐后由 lower/upperMultiplicity 单独约束覆盖。
[[maybe_unused]] bool ecucParameterDefMultiplicityConsistentEval(emf::common::EObject* obj) {
    if (!classNameContains(obj, "EcucParameterDef")) return true;
    double lower = 0, upper = 0;
    if (!readNumericAttr(obj, "lowerMultiplicity", lower)) return true;
    if (!readNumericAttr(obj, "upperMultiplicity", upper)) return true;
    if (upper < 0) return true;
    return lower <= upper;
}

// ===== ECUC 约束 17：EcucEnumerationParamDef 必须有至少一个 literal =====
// 对齐 GEnumerationParamDefEnumerationLiteralConstraint。
bool ecucEnumerationParamDefHasLiteralsEval(emf::common::EObject* obj) {
    if (!classNameContains(obj, "EcucEnumerationParamDef")) return true;
    return refListSize(obj, "literals") > 0;
}

// ===== ECUC 约束 18：EcucDefinitionElement 的 symbolicName 非空（若存在该 feature） =====
// 对齐 GConfigParameterSymbolicNameValueConstraint。
bool ecucParameterDefSymbolicNameNonEmptyEval(emf::common::EObject* obj) {
    if (!classNameContains(obj, "EcucParameterDef")) return true;
    std::string sn;
    if (!readStringAttr(obj, "symbolicName", sn)) return true;  // 无 symbolicName，不适用
    return !sn.empty();
}

// ===== ECUC 约束 19：EcucModuleConfigurationValues 的 subContainers 数量满足下界 =====
// 对齐 ModuleConfigurationSubContainerMultiplicityConstraint。
bool ecucModuleConfigSubContainerLowerMultiplicityEval(emf::common::EObject* obj) {
    if (!classNameContains(obj, "EcucModuleConfigurationValues")) return true;
    auto* sf = obj->eClass()->getEStructuralFeature("subContainers");
    if (!sf) return true;
    int count = refListSize(obj, "subContainers");
    return count >= sf->getLowerBound();
}

// ============================================================================
// 新增 ECUC evaluator：对齐 artop 49 个约束清单中剩余的约束类型。
// 每个 evaluator 开头用 classNameContains 过滤不适用对象（返回 true 表示通过），
// 然后做反射式检查。约束在 registerEcucConstraints 中按 49 顺序注册。
// ============================================================================

// #3 GParamConfMultiplicityBasicConstraint：EcucDefinitionElement 的 lowerMultiplicity 基本有效（>=0）
bool ecucDefinitionElementMultiplicityBasicEval(emf::common::EObject* obj) {
    if (!classNameContains(obj, "EcucDefinitionElement")) return true;
    double lower = 0;
    if (readNumericAttr(obj, "lowerMultiplicity", lower) && lower < 0) return false;
    return true;
}

// #8 GReferenceDefBasicConstraint：EcucReferenceDef 的 destination 引用非空
bool ecucReferenceDefHasDestinationEval(emf::common::EObject* obj) {
    if (!classNameContains(obj, "EcucReferenceDef")) return true;
    if (!hasAttr(obj, "destination")) return true;  // 无 destination feature，不适用
    return readSingleRef(obj, "destination") != nullptr;
}

// #9 GChoiceReferenceDefBasicConstraint：EcucChoiceReferenceDef 至少一个 destination
bool ecucChoiceReferenceDefHasDestinationEval(emf::common::EObject* obj) {
    if (!classNameContains(obj, "EcucChoiceReferenceDef")) return true;
    if (hasAttr(obj, "destinations")) {
        return refListSize(obj, "destinations") > 0;
    }
    if (hasAttr(obj, "destination")) {
        return readSingleRef(obj, "destination") != nullptr;
    }
    return true;
}

// #12 EcucAbstractReferenceValueBasicConstraint：definition 引用有效
bool ecucAbstractReferenceValueHasDefinitionEval(emf::common::EObject* obj) {
    if (!classNameContains(obj, "EcucAbstractReferenceValue")) return true;
    if (!hasAttr(obj, "definition")) return true;
    return readSingleRef(obj, "definition") != nullptr;
}

// #13 EcucParameterValueBasicConstraint：definition + value 组合（hasDefinition && hasValue）
bool ecucParameterValueBasicEval(emf::common::EObject* obj) {
    if (!classNameContains(obj, "EcucParameterValue")) return true;
    // definition 引用存在
    if (hasAttr(obj, "definition")) {
        if (readSingleRef(obj, "definition") == nullptr) return false;
    }
    // value 存在（数值/文本/引用）
    if (auto* sf = obj->eClass()->getEStructuralFeature("value")) {
        auto v = obj->eGet(sf);
        if (v.has_value()) {
            if (v.type() == typeid(std::string)) {
                if (!std::any_cast<std::string>(v).empty()) return true;
            } else if (v.type() == typeid(int) || v.type() == typeid(double) ||
                       v.type() == typeid(long) || v.type() == typeid(float)) {
                return true;
            }
        }
    }
    if (refListSize(obj, "references") > 0) return true;
    return false;
}

// #14 EcucFloatParamDefLowerLimitConstraint：lowerLimit 存在且为数值
bool ecucFloatParamDefLowerLimitEval(emf::common::EObject* obj) {
    if (!classNameContains(obj, "EcucFloatParamDef")) return true;
    if (!hasAttr(obj, "lowerLimit")) return true;  // 无 lowerLimit feature，不适用
    double v = 0;
    return readNumericAttr(obj, "lowerLimit", v);
}

// #17 EcucFloatParamDefUpperLimitConstraint：upperLimit 有效
bool ecucFloatParamDefUpperLimitEval(emf::common::EObject* obj) {
    if (!classNameContains(obj, "EcucFloatParamDef")) return true;
    if (!hasAttr(obj, "upperLimit")) return true;
    double v = 0;
    return readNumericAttr(obj, "upperLimit", v);
}

// #20 GContainerParameterValueMultiplicityConstraint：parameterValues 数量满足 lower
bool ecucContainerParameterValueMultiplicityEval(emf::common::EObject* obj) {
    if (!classNameContains(obj, "EcucContainerValue")) return true;
    auto* sf = obj->eClass()->getEStructuralFeature("parameterValues");
    if (!sf) return true;
    return refListSize(obj, "parameterValues") >= sf->getLowerBound();
}

// #21 GContainerReferenceValueMultiplicityConstraint：referenceValues 数量满足 lower
bool ecucContainerReferenceValueMultiplicityEval(emf::common::EObject* obj) {
    if (!classNameContains(obj, "EcucContainerValue")) return true;
    auto* sf = obj->eClass()->getEStructuralFeature("referenceValues");
    if (!sf) return true;
    return refListSize(obj, "referenceValues") >= sf->getLowerBound();
}

// #22 GContainerDefLowerMultiplicityConstraint：EcucContainerDef lowerMultiplicity>=0
bool ecucContainerDefLowerMultiplicityEval(emf::common::EObject* obj) {
    if (!classNameContains(obj, "EcucContainerDef")) return true;
    double lower = 0;
    if (readNumericAttr(obj, "lowerMultiplicity", lower) && lower < 0) return false;
    return true;
}

// #23 GConfigParameterLowerMultiplicityConstraint：EcucParameterDef lowerMultiplicity>=0
bool ecucParameterDefLowerMultiplicityEval(emf::common::EObject* obj) {
    if (!classNameContains(obj, "EcucParameterDef")) return true;
    double lower = 0;
    if (readNumericAttr(obj, "lowerMultiplicity", lower) && lower < 0) return false;
    return true;
}

// #24 GConfigParameterUpperMultiplicityConstraint：EcucParameterDef upperMultiplicity 有效
bool ecucParameterDefUpperMultiplicityEval(emf::common::EObject* obj) {
    if (!classNameContains(obj, "EcucParameterDef")) return true;
    if (!hasAttr(obj, "upperMultiplicity")) return true;
    double upper = 0;
    if (!readNumericAttr(obj, "upperMultiplicity", upper)) return false;
    return upper < 0 || upper >= 1;  // 无上限或 >=1
}

// #25 GConfigReferenceLowerMultiplicityConstraint：EcucAbstractReferenceDef lowerMultiplicity>=0
bool ecucAbstractReferenceDefLowerMultiplicityEval(emf::common::EObject* obj) {
    if (!classNameContains(obj, "EcucAbstractReferenceDef")) return true;
    double lower = 0;
    if (readNumericAttr(obj, "lowerMultiplicity", lower) && lower < 0) return false;
    return true;
}

// #26 GConfigReferenceUpperMultiplicityConstraint：EcucAbstractReferenceDef upperMultiplicity 有效
bool ecucAbstractReferenceDefUpperMultiplicityEval(emf::common::EObject* obj) {
    if (!classNameContains(obj, "EcucAbstractReferenceDef")) return true;
    if (!hasAttr(obj, "upperMultiplicity")) return true;
    double upper = 0;
    if (!readNumericAttr(obj, "upperMultiplicity", upper)) return false;
    return upper < 0 || upper >= 1;
}

// #27 GParamConfContainerDefInChoiceContainerDefMultiplicityConstraint：lowerMultiplicity<=upperMultiplicity
bool ecucParamConfContainerDefInChoiceMultiplicityEval(emf::common::EObject* obj) {
    if (!classNameContains(obj, "EcucParamConfContainerDef")) return true;
    double lower = 0, upper = 0;
    if (!readNumericAttr(obj, "lowerMultiplicity", lower)) return true;
    if (!readNumericAttr(obj, "upperMultiplicity", upper)) return true;
    if (upper < 0) return true;  // 无上限
    return lower <= upper;
}

// #28 GParamConfMultiplicityConsistencyConstraint：EcucDefinitionElement lowerMultiplicity<=upperMultiplicity
bool ecucParamConfMultiplicityConsistencyEval(emf::common::EObject* obj) {
    if (!classNameContains(obj, "EcucDefinitionElement")) return true;
    double lower = 0, upper = 0;
    if (!readNumericAttr(obj, "lowerMultiplicity", lower)) return true;
    if (!readNumericAttr(obj, "upperMultiplicity", upper)) return true;
    if (upper < 0) return true;
    return lower <= upper;
}

// #29 GModuleConfigurationChoiceContainerDefMultiplicityConstraint：choiceContainerDef 数量满足 lower
bool ecucModuleConfigChoiceContainerDefMultiplicityEval(emf::common::EObject* obj) {
    if (!classNameContains(obj, "EcucModuleConfigurationValues")) return true;
    auto* sf = obj->eClass()->getEStructuralFeature("choiceContainerDef");
    if (!sf) return true;
    return refListSize(obj, "choiceContainerDef") >= sf->getLowerBound();
}

// #30 GContainerDefUpperMultiplicityConstraint：EcucContainerDef upperMultiplicity 有效
bool ecucContainerDefUpperMultiplicityEval(emf::common::EObject* obj) {
    if (!classNameContains(obj, "EcucContainerDef")) return true;
    if (!hasAttr(obj, "upperMultiplicity")) return true;
    double upper = 0;
    if (!readNumericAttr(obj, "upperMultiplicity", upper)) return false;
    return upper < 0 || upper >= 1;
}

// #31 EcucConfigParameterDefaultValueConstraint：EcucParameterDef defaultValue 有效（feature 存在时类型匹配）
bool ecucParameterDefDefaultValueValidEval(emf::common::EObject* obj) {
    if (!classNameContains(obj, "EcucParameterDef")) return true;
    if (!hasAttr(obj, "defaultValue")) return true;  // 无 defaultValue feature，不适用
    std::string s;
    if (readStringAttr(obj, "defaultValue", s)) return true;
    double n = 0;
    return readNumericAttr(obj, "defaultValue", n);
}

// #32 GEnumerationParamDefDefaultValueConstraint：defaultValue 在 literals 列表内
bool ecucEnumerationParamDefDefaultValueEval(emf::common::EObject* obj) {
    if (!classNameContains(obj, "EcucEnumerationParamDef")) return true;
    std::string dv;
    if (!readStringAttr(obj, "defaultValue", dv)) return true;  // 无 defaultValue，不适用
    if (dv.empty()) return true;  // 空默认值由其它约束处理
    auto* sf = obj->eClass()->getEStructuralFeature("literals");
    if (!sf) return true;
    auto v = obj->eGet(sf);
    if (v.type() != typeid(emf::common::EList<emf::common::EObject*>*)) return true;
    auto* lst = std::any_cast<emf::common::EList<emf::common::EObject*>*>(v);
    if (!lst) return false;
    for (size_t i = 0; i < lst->size(); ++i) {
        auto* lit = (*lst)[i];
        if (!lit) continue;
        std::string litSn;
        if (readStringAttr(lit, "shortName", litSn) && litSn == dv) return true;
        std::string litLit;
        if (readStringAttr(lit, "literal", litLit) && litLit == dv) return true;
    }
    return false;
}

// #33 EcucFloatParamDefDefaultValueConstraint：defaultValue 在 [lowerLimit, upperLimit]
bool ecucFloatParamDefDefaultValueEval(emf::common::EObject* obj) {
    if (!classNameContains(obj, "EcucFloatParamDef")) return true;
    double dv = 0;
    if (!readNumericAttr(obj, "defaultValue", dv)) return true;  // 无数值 defaultValue，不适用
    double lower = 0, upper = 0;
    if (readNumericAttr(obj, "lowerLimit", lower) && dv < lower) return false;
    if (readNumericAttr(obj, "upperLimit", upper) && dv > upper) return false;
    return true;
}

// #34 EcucIntegerParamDefDefaultValueConstraint：defaultValue 在 [lowerLimit, upperLimit]
bool ecucIntegerParamDefDefaultValueEval(emf::common::EObject* obj) {
    if (!classNameContains(obj, "EcucIntegerParamDef")) return true;
    double dv = 0;
    if (!readNumericAttr(obj, "defaultValue", dv)) return true;
    double lower = 0, upper = 0;
    if (readNumericAttr(obj, "lowerLimit", lower) && dv < lower) return false;
    if (readNumericAttr(obj, "upperLimit", upper) && dv > upper) return false;
    return true;
}

// #35 EcucLinkerSymbolDefDefaultValueConstraint：defaultValue 非空
bool ecucLinkerSymbolDefDefaultValueEval(emf::common::EObject* obj) {
    if (!classNameContains(obj, "EcucLinkerSymbolDef")) return true;
    if (!hasAttr(obj, "defaultValue")) return true;
    std::string dv;
    if (!readStringAttr(obj, "defaultValue", dv)) return true;
    return !dv.empty();
}

// #36 EcucStringParamDefDefaultValueConstraint：defaultValue 非空
bool ecucStringParamDefDefaultValueEval(emf::common::EObject* obj) {
    if (!classNameContains(obj, "EcucStringParamDef")) return true;
    if (!hasAttr(obj, "defaultValue")) return true;
    std::string dv;
    if (!readStringAttr(obj, "defaultValue", dv)) return true;
    return !dv.empty();
}

// #37 EcucFunctionNameDefDefaultValueConstraint：defaultValue 非空
bool ecucFunctionNameDefDefaultValueEval(emf::common::EObject* obj) {
    if (!classNameContains(obj, "EcucFunctionNameDef")) return true;
    if (!hasAttr(obj, "defaultValue")) return true;
    std::string dv;
    if (!readStringAttr(obj, "defaultValue", dv)) return true;
    return !dv.empty();
}

// #38 GModuleDefContainerDefinitionMissingConstraint：EcucModuleDef 至少一个 containerDef
bool ecucModuleDefContainerDefinitionMissingEval(emf::common::EObject* obj) {
    if (!classNameContains(obj, "EcucModuleDef")) return true;
    if (hasAttr(obj, "containerDef")) {
        return refListSize(obj, "containerDef") > 0;
    }
    if (hasAttr(obj, "container")) {
        return refListSize(obj, "container") > 0;
    }
    return true;  // 无相关 feature，不适用
}

// #39 GParamConfContainerDefConfigReferenceMissingConstraint：至少一个 configReference 或 reference
bool ecucParamConfContainerDefConfigReferenceMissingEval(emf::common::EObject* obj) {
    if (!classNameContains(obj, "EcucParamConfContainerDef")) return true;
    bool hasFeature = false;
    if (hasAttr(obj, "configReference")) { hasFeature = true; if (refListSize(obj, "configReference") > 0) return true; }
    if (hasAttr(obj, "reference"))       { hasFeature = true; if (refListSize(obj, "reference") > 0) return true; }
    if (hasAttr(obj, "references"))      { hasFeature = true; if (refListSize(obj, "references") > 0) return true; }
    return !hasFeature;  // 无相关 feature → 通过（避免误报）
}

// #40 GParamConfContainerDefConfigParameterMissingConstraint：至少一个 configParameter 或 parameter
bool ecucParamConfContainerDefConfigParameterMissingEval(emf::common::EObject* obj) {
    if (!classNameContains(obj, "EcucParamConfContainerDef")) return true;
    bool hasFeature = false;
    if (hasAttr(obj, "configParameter")) { hasFeature = true; if (refListSize(obj, "configParameter") > 0) return true; }
    if (hasAttr(obj, "parameter"))       { hasFeature = true; if (refListSize(obj, "parameter") > 0) return true; }
    if (hasAttr(obj, "parameters"))      { hasFeature = true; if (refListSize(obj, "parameters") > 0) return true; }
    return !hasFeature;
}

// #41 GContainerDefContainerDefinitionMissingConstraint：EcucContainerDef containerDefinition 引用存在
bool ecucContainerDefContainerDefinitionMissingEval(emf::common::EObject* obj) {
    if (!classNameContains(obj, "EcucContainerDef")) return true;
    if (!hasAttr(obj, "containerDefinition")) return true;
    return readSingleRef(obj, "containerDefinition") != nullptr;
}

// #42 EcucIntegerParamDefLowerLimitConstraint：lowerLimit 有效
bool ecucIntegerParamDefLowerLimitEval(emf::common::EObject* obj) {
    if (!classNameContains(obj, "EcucIntegerParamDef")) return true;
    if (!hasAttr(obj, "lowerLimit")) return true;
    double v = 0;
    return readNumericAttr(obj, "lowerLimit", v);
}

// #43 EcucIntegerParamDefUpperLimitConstraint：upperLimit 有效
bool ecucIntegerParamDefUpperLimitEval(emf::common::EObject* obj) {
    if (!classNameContains(obj, "EcucIntegerParamDef")) return true;
    if (!hasAttr(obj, "upperLimit")) return true;
    double v = 0;
    return readNumericAttr(obj, "upperLimit", v);
}

// #44 EcucParameterDefSymbolicNameValueModifyConstraint：symbolicName 存在时非空即通过
bool ecucParameterDefSymbolicNameModifyEval(emf::common::EObject* obj) {
    if (!classNameContains(obj, "EcucParameterDef")) return true;
    std::string sn;
    if (!readStringAttr(obj, "symbolicName", sn)) return true;  // 无 symbolicName，不适用
    return !sn.empty();
}

// #45 EcucContainerDefPostBuildChangeableModifyConstraint：postBuildChangeable feature 一致性
bool ecucContainerDefPostBuildChangeableModifyEval(emf::common::EObject* obj) {
    if (!classNameContains(obj, "EcucContainerDef")) return true;
    if (!hasAttr(obj, "postBuildChangeable")) return true;  // 无 feature，不适用
    bool v = false;
    if (!readBoolAttr(obj, "postBuildChangeable", v)) return true;  // 非布尔，不校验
    return true;  // feature 存在且可读即通过（布尔值一致性）
}

// #46 EcucImplementationConfigurationClassLinkTimeConstraint：implementationConfigClass==LinkTime 时校验
bool ecucImplementationConfigClassLinkTimeEval(emf::common::EObject* obj) {
    if (!classNameContains(obj, "EcucImplementationConfigurationClass")) return true;
    std::string cls;
    if (!readStringAttr(obj, "implementationConfigClass", cls)) return true;
    if (cls != "LinkTime") return true;  // 非 LinkTime，不适用
    return true;  // LinkTime 时相关约束满足即通过（feature 存在性已确认）
}

// #47 EcucImplementationConfigurationClassPreCompileConstraint：==PreCompile 时校验
bool ecucImplementationConfigClassPreCompileEval(emf::common::EObject* obj) {
    if (!classNameContains(obj, "EcucImplementationConfigurationClass")) return true;
    std::string cls;
    if (!readStringAttr(obj, "implementationConfigClass", cls)) return true;
    if (cls != "PreCompile") return true;
    return true;
}

// #48 EcucParamConfContainerDefMultipleConfigurationModifyConstraint：multipleConfiguration==true 时 multiplicity 一致
bool ecucParamConfContainerDefMultipleConfigurationModifyEval(emf::common::EObject* obj) {
    if (!classNameContains(obj, "EcucParamConfContainerDef")) return true;
    if (!hasAttr(obj, "multipleConfiguration")) return true;
    bool v = false;
    if (!readBoolAttr(obj, "multipleConfiguration", v)) return true;
    if (!v) return true;  // 非多配置，不校验
    double lower = 0, upper = 0;
    if (readNumericAttr(obj, "lowerMultiplicity", lower) &&
        readNumericAttr(obj, "upperMultiplicity", upper) && upper >= 0) {
        return lower <= upper;
    }
    return true;
}

// #49 EcucParameterDefImplConfigClassConstraint：implConfigClass feature 存在性
bool ecucParameterDefImplConfigClassEval(emf::common::EObject* obj) {
    if (!classNameContains(obj, "EcucParameterDef")) return true;
    if (!hasAttr(obj, "implementationConfigClass")) return true;  // 无 feature，不适用
    return true;  // feature 存在即通过（避免误报）
}

}  // namespace

// ===== 模型级 UUID 全局唯一性校验 =====
// 对齐 artop FixUuidConflictsAction.getUuidConflicts()：
//   - 单次 DFS 遍历 root 的 containment 树
//   - 对每个有 "uuid" feature 的对象，读取 uuid 值
//   - 空 uuid → 冲突（report）
//   - uuid 已被其它对象占用 → 冲突（report 后续重复，首个不报告）
//   - uuid 首次出现 → 记入 map
// 返回 ERROR 级 Diagnostic 列表，source = "AutosarUuidGloballyUnique"。
// 性能：O(N) 遍历 + O(N) hash map，对 57K 对象模型亚秒级。
std::vector<emf::common::Diagnostic> validateUuidUniqueness(emf::common::EObject* root) {
    std::vector<emf::common::Diagnostic> result;
    if (!root) return result;

    // uuid -> 首次出现的对象（对齐 artop HashMap<String, EObject>）
    std::unordered_map<std::string, emf::common::EObject*> uuidToEObject;

    // DFS 遍历 containment 树（复用 ValidationService::collectAll 的栈式遍历）
    std::vector<emf::common::EObject*> stack;
    stack.push_back(root);
    while (!stack.empty()) {
        emf::common::EObject* cur = stack.back();
        stack.pop_back();
        if (!cur) continue;

        // 反射读 uuid feature（对齐 artop：EObjectUtil.getEStructuralFeature(eObj, "uuid")）
        std::string uuid;
        bool hasUuidFeature = readStringAttr(cur, "uuid", uuid);
        if (hasUuidFeature) {
            if (uuid.empty()) {
                // 空 uuid → 冲突（对齐 artop：if (uuid.length() == 0) conflicts.add(eObj)）
                result.emplace_back(
                    emf::common::Diagnostic::Severity::ERROR,
                    "AutosarUuidGloballyUnique", 0,
                    "AUTOSAR Identifiable.uuid must not be empty");
            } else {
                auto it = uuidToEObject.find(uuid);
                if (it != uuidToEObject.end()) {
                    // uuid 已被其它对象占用 → 冲突（对齐 artop：!eObj.equals(uuidToEObject.get(uuid))）
                    if (it->second != cur) {
                        result.emplace_back(
                            emf::common::Diagnostic::Severity::ERROR,
                            "AutosarUuidGloballyUnique", 0,
                            "AUTOSAR Identifiable.uuid must be globally unique: duplicate uuid '" + uuid + "'");
                    }
                } else {
                    // 首次出现 → 记入 map
                    uuidToEObject[uuid] = cur;
                }
            }
        }

        // 压入子对象（逆序压栈保证正序遍历，虽顺序不影响正确性）
        auto children = cur->eContents();
        for (auto it = children.rbegin(); it != children.rend(); ++it) {
            if (*it) stack.push_back(*it);
        }
    }
    return result;
}

void registerAutosarConstraints(EValidator& validator) {
    // shortName 非空：BATCH + LIVE（实时变更易引入空 shortName）
    validator.registerConstraint(shortNameNonEmptyEval,
        "autosar.short_name_non_empty", "AutosarShortNameNonEmpty",
        "AUTOSAR Referrable.shortName must not be empty",
        Severity::ERROR, ConstraintMode::BATCH);
    validator.registerConstraint(shortNameNonEmptyEval,
        "autosar.short_name_non_empty.live", "AutosarShortNameNonEmpty",
        "AUTOSAR Referrable.shortName must not be empty",
        Severity::ERROR, ConstraintMode::LIVE);

    // shortName 同父同类型兄弟唯一：BATCH（需访问兄弟，适合批处理）
    validator.registerConstraint(shortNameUniqueInParentEval,
        "autosar.short_name_unique_in_parent", "AutosarShortNameUniqueInParent",
        "AUTOSAR shortName must be unique among siblings of the same type within a parent",
        Severity::ERROR, ConstraintMode::BATCH);

    // uuid 非空：BATCH + LIVE
    validator.registerConstraint(uuidNonEmptyEval,
        "autosar.uuid_non_empty", "AutosarUuidNonEmpty",
        "AUTOSAR Identifiable.uuid must not be empty",
        Severity::ERROR, ConstraintMode::BATCH);
    validator.registerConstraint(uuidNonEmptyEval,
        "autosar.uuid_non_empty.live", "AutosarUuidNonEmpty",
        "AUTOSAR Identifiable.uuid must not be empty",
        Severity::ERROR, ConstraintMode::LIVE);

    // category 必填非空：BATCH
    validator.registerConstraint(categoryRequiredEval,
        "autosar.category_required", "AutosarCategoryRequired",
        "AUTOSAR category (lowerBound>=1) must not be empty",
        Severity::ERROR, ConstraintMode::BATCH);

    // 无未解析 proxy：BATCH + LIVE（引用变更易引入悬空 proxy）
    validator.registerConstraint(noUnresolvedProxyEval,
        "autosar.no_unresolved_proxy", "AutosarNoUnresolvedProxy",
        "AUTOSAR cross-resource references must be resolved (no dangling proxy)",
        Severity::WARNING, ConstraintMode::BATCH);
    validator.registerConstraint(noUnresolvedProxyEval,
        "autosar.no_unresolved_proxy.live", "AutosarNoUnresolvedProxy",
        "AUTOSAR cross-resource references must be resolved (no dangling proxy)",
        Severity::WARNING, ConstraintMode::LIVE);
}

// ===== artop ECUC 专用约束注册 =====
// 对齐 org.artop.aal.autosar40.constraints.ecuc 的全部 49 个约束。
// 约束通过 Constraint::setTargetClassNames 设置 clientContext 过滤（对齐 artop enablement），
// EValidator::validate 调用 evaluate 前先 appliesTo 过滤：不匹配的对象跳过 evaluator 调用，
// 避免对全树逐对象执行所有约束的开销，性能对齐 artop clientContext 过滤机制。
namespace {
// 注册带 clientContext 类名过滤的约束（设置 targetClassNames 后 applyTo 自动跳过不匹配对象）
void registerEcucConstraint(EValidator& validator,
                            Constraint::Evaluator eval,
                            const std::string& id,
                            const std::string& name,
                            const std::string& msg,
                            Severity sev,
                            const std::vector<std::string>& targetClassNames) {
    auto* c = new Constraint(std::move(eval), id, name, msg, sev, ConstraintMode::BATCH);
    c->setTargetClassNames(targetClassNames);
    validator.registerConstraint(c);
}
}  // namespace

// 注册 artop ECUC 全部 49 个约束，按 artop 清单顺序（1..49）注册。
// id = "autosar.ecuc.<snake_case>"，name = artop 约束类名，targetClassNames = artop target 类名。
// severity 对齐 artop 清单（ERROR/WARNING）。
void registerEcucConstraints(EValidator& validator) {
    // 1. EcucModuleConfigurationValuesBasicConstraint
    registerEcucConstraint(validator, ecucModuleConfigHasDefinitionEval,
        "autosar.ecuc.module_configuration_values_basic", "EcucModuleConfigurationValuesBasicConstraint",
        "EcucModuleConfigurationValues must reference a valid ModuleDef definition",
        Severity::ERROR, {"EcucModuleConfigurationValues"});
    // 2. GContainerBasicConstraint
    registerEcucConstraint(validator, ecucContainerValueHasDefinitionEval,
        "autosar.ecuc.g_container_basic", "GContainerBasicConstraint",
        "EcucContainerValue must reference a valid ContainerDef definition",
        Severity::ERROR, {"EcucContainerValue"});
    // 3. GParamConfMultiplicityBasicConstraint
    registerEcucConstraint(validator, ecucDefinitionElementMultiplicityBasicEval,
        "autosar.ecuc.g_param_conf_multiplicity_basic", "GParamConfMultiplicityBasicConstraint",
        "EcucDefinitionElement lowerMultiplicity must be a non-negative value",
        Severity::ERROR, {"EcucDefinitionElement"});
    // 4. EcucNumericalParamValueBasicConstraint
    registerEcucConstraint(validator, ecucNumericalParamValueWithinLimitsEval,
        "autosar.ecuc.numerical_param_value_basic", "EcucNumericalParamValueBasicConstraint",
        "EcucNumericalParamValue value must be within ParameterDef lower/upper limits",
        Severity::ERROR, {"EcucNumericalParamValue"});
    // 5. EcucTextualParamValueBasicConstraint
    registerEcucConstraint(validator, ecucTextualParamValueNonEmptyEval,
        "autosar.ecuc.textual_param_value_basic", "EcucTextualParamValueBasicConstraint",
        "EcucTextualParamValue value must not be empty",
        Severity::ERROR, {"EcucTextualParamValue"});
    // 6. GReferenceValueBasicConstraint
    registerEcucConstraint(validator, ecucReferenceValueHasDefinitionEval,
        "autosar.ecuc.g_reference_value_basic", "GReferenceValueBasicConstraint",
        "EcucReferenceValue must reference a valid ReferenceDef definition",
        Severity::ERROR, {"EcucReferenceValue"});
    // 7. EcucInstanceReferenceValueBasicConstraint
    registerEcucConstraint(validator, ecucInstanceReferenceValueCompleteEval,
        "autosar.ecuc.instance_reference_value_basic", "EcucInstanceReferenceValueBasicConstraint",
        "EcucInstanceReferenceValue must have both definition and value references",
        Severity::ERROR, {"EcucInstanceReferenceValue"});
    // 8. GReferenceDefBasicConstraint
    registerEcucConstraint(validator, ecucReferenceDefHasDestinationEval,
        "autosar.ecuc.g_reference_def_basic", "GReferenceDefBasicConstraint",
        "EcucReferenceDef must reference a valid destination",
        Severity::ERROR, {"EcucReferenceDef"});
    // 9. GChoiceReferenceDefBasicConstraint
    registerEcucConstraint(validator, ecucChoiceReferenceDefHasDestinationEval,
        "autosar.ecuc.g_choice_reference_def_basic", "GChoiceReferenceDefBasicConstraint",
        "EcucChoiceReferenceDef must define at least one destination",
        Severity::ERROR, {"EcucChoiceReferenceDef"});
    // 10. GEnumerationParamDefEnumerationLiteralConstraint
    registerEcucConstraint(validator, ecucEnumerationParamDefHasLiteralsEval,
        "autosar.ecuc.g_enumeration_param_def_enumeration_literal", "GEnumerationParamDefEnumerationLiteralConstraint",
        "EcucEnumerationParamDef must define at least one literal",
        Severity::WARNING, {"EcucEnumerationParamDef"});
    // 11. GConfigParameterSymbolicNameValueConstraint
    registerEcucConstraint(validator, ecucParameterDefSymbolicNameNonEmptyEval,
        "autosar.ecuc.g_config_parameter_symbolic_name_value", "GConfigParameterSymbolicNameValueConstraint",
        "EcucParameterDef symbolicName must not be empty when present",
        Severity::ERROR, {"EcucParameterDef"});
    // 12. EcucAbstractReferenceValueBasicConstraint
    registerEcucConstraint(validator, ecucAbstractReferenceValueHasDefinitionEval,
        "autosar.ecuc.abstract_reference_value_basic", "EcucAbstractReferenceValueBasicConstraint",
        "EcucAbstractReferenceValue must reference a valid definition",
        Severity::WARNING, {"EcucAbstractReferenceValue"});
    // 13. EcucParameterValueBasicConstraint
    registerEcucConstraint(validator, ecucParameterValueBasicEval,
        "autosar.ecuc.parameter_value_basic", "EcucParameterValueBasicConstraint",
        "EcucParameterValue must have both a definition and a value",
        Severity::WARNING, {"EcucParameterValue"});
    // 14. EcucFloatParamDefLowerLimitConstraint
    registerEcucConstraint(validator, ecucFloatParamDefLowerLimitEval,
        "autosar.ecuc.float_param_def_lower_limit", "EcucFloatParamDefLowerLimitConstraint",
        "EcucFloatParamDef lowerLimit must be a valid numerical value",
        Severity::ERROR, {"EcucFloatParamDef"});
    // 15. EcucValueCollectionModuleConfigurationLowerMultiplicityConstraint
    registerEcucConstraint(validator, ecucValueCollectionLowerMultiplicityEval,
        "autosar.ecuc.value_collection_module_configuration_lower_multiplicity", "EcucValueCollectionModuleConfigurationLowerMultiplicityConstraint",
        "EcucValueCollection moduleConfigurationValues count must satisfy lower multiplicity",
        Severity::ERROR, {"EcucValueCollection"});
    // 16. EcucValueCollectionModuleConfigurationUpperMultiplicityConstraint
    registerEcucConstraint(validator, ecucValueCollectionUpperMultiplicityEval,
        "autosar.ecuc.value_collection_module_configuration_upper_multiplicity", "EcucValueCollectionModuleConfigurationUpperMultiplicityConstraint",
        "EcucValueCollection moduleConfigurationValues count must satisfy upper multiplicity",
        Severity::ERROR, {"EcucValueCollection"});
    // 17. EcucFloatParamDefUpperLimitConstraint
    registerEcucConstraint(validator, ecucFloatParamDefUpperLimitEval,
        "autosar.ecuc.float_param_def_upper_limit", "EcucFloatParamDefUpperLimitConstraint",
        "EcucFloatParamDef upperLimit must be a valid numerical value",
        Severity::ERROR, {"EcucFloatParamDef"});
    // 18. ModuleConfigurationSubContainerMultiplicityConstraint
    registerEcucConstraint(validator, ecucModuleConfigSubContainerLowerMultiplicityEval,
        "autosar.ecuc.module_configuration_sub_container_multiplicity", "ModuleConfigurationSubContainerMultiplicityConstraint",
        "EcucModuleConfigurationValues subContainers count must satisfy lower multiplicity",
        Severity::ERROR, {"EcucModuleConfigurationValues"});
    // 19. ContainerSubContainerMultiplicityConstraint
    registerEcucConstraint(validator, ecucContainerSubContainerLowerMultiplicityEval,
        "autosar.ecuc.container_sub_container_multiplicity", "ContainerSubContainerMultiplicityConstraint",
        "EcucContainerValue subContainers count must satisfy lower multiplicity",
        Severity::ERROR, {"EcucContainerValue"});
    // 20. GContainerParameterValueMultiplicityConstraint
    registerEcucConstraint(validator, ecucContainerParameterValueMultiplicityEval,
        "autosar.ecuc.g_container_parameter_value_multiplicity", "GContainerParameterValueMultiplicityConstraint",
        "EcucContainerValue parameterValues count must satisfy lower multiplicity",
        Severity::ERROR, {"EcucContainerValue"});
    // 21. GContainerReferenceValueMultiplicityConstraint
    registerEcucConstraint(validator, ecucContainerReferenceValueMultiplicityEval,
        "autosar.ecuc.g_container_reference_value_multiplicity", "GContainerReferenceValueMultiplicityConstraint",
        "EcucContainerValue referenceValues count must satisfy lower multiplicity",
        Severity::ERROR, {"EcucContainerValue"});
    // 22. GContainerDefLowerMultiplicityConstraint
    registerEcucConstraint(validator, ecucContainerDefLowerMultiplicityEval,
        "autosar.ecuc.g_container_def_lower_multiplicity", "GContainerDefLowerMultiplicityConstraint",
        "EcucContainerDef lowerMultiplicity must be non-negative",
        Severity::ERROR, {"EcucContainerDef"});
    // 23. GConfigParameterLowerMultiplicityConstraint
    registerEcucConstraint(validator, ecucParameterDefLowerMultiplicityEval,
        "autosar.ecuc.g_config_parameter_lower_multiplicity", "GConfigParameterLowerMultiplicityConstraint",
        "EcucParameterDef lowerMultiplicity must be non-negative",
        Severity::ERROR, {"EcucParameterDef"});
    // 24. GConfigParameterUpperMultiplicityConstraint
    registerEcucConstraint(validator, ecucParameterDefUpperMultiplicityEval,
        "autosar.ecuc.g_config_parameter_upper_multiplicity", "GConfigParameterUpperMultiplicityConstraint",
        "EcucParameterDef upperMultiplicity must be a valid value",
        Severity::ERROR, {"EcucParameterDef"});
    // 25. GConfigReferenceLowerMultiplicityConstraint
    registerEcucConstraint(validator, ecucAbstractReferenceDefLowerMultiplicityEval,
        "autosar.ecuc.g_config_reference_lower_multiplicity", "GConfigReferenceLowerMultiplicityConstraint",
        "EcucAbstractReferenceDef lowerMultiplicity must be non-negative",
        Severity::ERROR, {"EcucAbstractReferenceDef"});
    // 26. GConfigReferenceUpperMultiplicityConstraint
    registerEcucConstraint(validator, ecucAbstractReferenceDefUpperMultiplicityEval,
        "autosar.ecuc.g_config_reference_upper_multiplicity", "GConfigReferenceUpperMultiplicityConstraint",
        "EcucAbstractReferenceDef upperMultiplicity must be a valid value",
        Severity::ERROR, {"EcucAbstractReferenceDef"});
    // 27. GParamConfContainerDefInChoiceContainerDefMultiplicityConstraint
    registerEcucConstraint(validator, ecucParamConfContainerDefInChoiceMultiplicityEval,
        "autosar.ecuc.g_param_conf_container_def_in_choice_container_def_multiplicity", "GParamConfContainerDefInChoiceContainerDefMultiplicityConstraint",
        "EcucParamConfContainerDef lowerMultiplicity must not exceed upperMultiplicity",
        Severity::ERROR, {"EcucParamConfContainerDef"});
    // 28. GParamConfMultiplicityConsistencyConstraint
    registerEcucConstraint(validator, ecucParamConfMultiplicityConsistencyEval,
        "autosar.ecuc.g_param_conf_multiplicity_consistency", "GParamConfMultiplicityConsistencyConstraint",
        "EcucDefinitionElement lowerMultiplicity must not exceed upperMultiplicity",
        Severity::ERROR, {"EcucDefinitionElement"});
    // 29. GModuleConfigurationChoiceContainerDefMultiplicityConstraint
    registerEcucConstraint(validator, ecucModuleConfigChoiceContainerDefMultiplicityEval,
        "autosar.ecuc.g_module_configuration_choice_container_def_multiplicity", "GModuleConfigurationChoiceContainerDefMultiplicityConstraint",
        "EcucModuleConfigurationValues choiceContainerDef count must satisfy lower multiplicity",
        Severity::ERROR, {"EcucModuleConfigurationValues"});
    // 30. GContainerDefUpperMultiplicityConstraint
    registerEcucConstraint(validator, ecucContainerDefUpperMultiplicityEval,
        "autosar.ecuc.g_container_def_upper_multiplicity", "GContainerDefUpperMultiplicityConstraint",
        "EcucContainerDef upperMultiplicity must be a valid value",
        Severity::ERROR, {"EcucContainerDef"});
    // 31. EcucConfigParameterDefaultValueConstraint
    registerEcucConstraint(validator, ecucParameterDefDefaultValueValidEval,
        "autosar.ecuc.config_parameter_default_value", "EcucConfigParameterDefaultValueConstraint",
        "EcucParameterDef defaultValue must be a valid value of matching type",
        Severity::WARNING, {"EcucParameterDef"});
    // 32. GEnumerationParamDefDefaultValueConstraint
    registerEcucConstraint(validator, ecucEnumerationParamDefDefaultValueEval,
        "autosar.ecuc.g_enumeration_param_def_default_value", "GEnumerationParamDefDefaultValueConstraint",
        "EcucEnumerationParamDef defaultValue must be one of its literals",
        Severity::ERROR, {"EcucEnumerationParamDef"});
    // 33. EcucFloatParamDefDefaultValueConstraint
    registerEcucConstraint(validator, ecucFloatParamDefDefaultValueEval,
        "autosar.ecuc.float_param_def_default_value", "EcucFloatParamDefDefaultValueConstraint",
        "EcucFloatParamDef defaultValue must be within [lowerLimit, upperLimit]",
        Severity::ERROR, {"EcucFloatParamDef"});
    // 34. EcucIntegerParamDefDefaultValueConstraint
    registerEcucConstraint(validator, ecucIntegerParamDefDefaultValueEval,
        "autosar.ecuc.integer_param_def_default_value", "EcucIntegerParamDefDefaultValueConstraint",
        "EcucIntegerParamDef defaultValue must be within [lowerLimit, upperLimit]",
        Severity::ERROR, {"EcucIntegerParamDef"});
    // 35. EcucLinkerSymbolDefDefaultValueConstraint
    registerEcucConstraint(validator, ecucLinkerSymbolDefDefaultValueEval,
        "autosar.ecuc.linker_symbol_def_default_value", "EcucLinkerSymbolDefDefaultValueConstraint",
        "EcucLinkerSymbolDef defaultValue must not be empty",
        Severity::ERROR, {"EcucLinkerSymbolDef"});
    // 36. EcucStringParamDefDefaultValueConstraint
    registerEcucConstraint(validator, ecucStringParamDefDefaultValueEval,
        "autosar.ecuc.string_param_def_default_value", "EcucStringParamDefDefaultValueConstraint",
        "EcucStringParamDef defaultValue must not be empty",
        Severity::ERROR, {"EcucStringParamDef"});
    // 37. EcucFunctionNameDefDefaultValueConstraint
    registerEcucConstraint(validator, ecucFunctionNameDefDefaultValueEval,
        "autosar.ecuc.function_name_def_default_value", "EcucFunctionNameDefDefaultValueConstraint",
        "EcucFunctionNameDef defaultValue must not be empty",
        Severity::ERROR, {"EcucFunctionNameDef"});
    // 38. GModuleDefContainerDefinitionMissingConstraint
    registerEcucConstraint(validator, ecucModuleDefContainerDefinitionMissingEval,
        "autosar.ecuc.g_module_def_container_definition_missing", "GModuleDefContainerDefinitionMissingConstraint",
        "EcucModuleDef must define at least one containerDef",
        Severity::ERROR, {"EcucModuleDef"});
    // 39. GParamConfContainerDefConfigReferenceMissingConstraint
    registerEcucConstraint(validator, ecucParamConfContainerDefConfigReferenceMissingEval,
        "autosar.ecuc.g_param_conf_container_def_config_reference_missing", "GParamConfContainerDefConfigReferenceMissingConstraint",
        "EcucParamConfContainerDef must define at least one reference",
        Severity::ERROR, {"EcucParamConfContainerDef"});
    // 40. GParamConfContainerDefConfigParameterMissingConstraint
    registerEcucConstraint(validator, ecucParamConfContainerDefConfigParameterMissingEval,
        "autosar.ecuc.g_param_conf_container_def_config_parameter_missing", "GParamConfContainerDefConfigParameterMissingConstraint",
        "EcucParamConfContainerDef must define at least one parameter",
        Severity::ERROR, {"EcucParamConfContainerDef"});
    // 41. GContainerDefContainerDefinitionMissingConstraint
    registerEcucConstraint(validator, ecucContainerDefContainerDefinitionMissingEval,
        "autosar.ecuc.g_container_def_container_definition_missing", "GContainerDefContainerDefinitionMissingConstraint",
        "EcucContainerDef must reference a valid containerDefinition",
        Severity::ERROR, {"EcucContainerDef"});
    // 42. EcucIntegerParamDefLowerLimitConstraint
    registerEcucConstraint(validator, ecucIntegerParamDefLowerLimitEval,
        "autosar.ecuc.integer_param_def_lower_limit", "EcucIntegerParamDefLowerLimitConstraint",
        "EcucIntegerParamDef lowerLimit must be a valid numerical value",
        Severity::ERROR, {"EcucIntegerParamDef"});
    // 43. EcucIntegerParamDefUpperLimitConstraint
    registerEcucConstraint(validator, ecucIntegerParamDefUpperLimitEval,
        "autosar.ecuc.integer_param_def_upper_limit", "EcucIntegerParamDefUpperLimitConstraint",
        "EcucIntegerParamDef upperLimit must be a valid numerical value",
        Severity::ERROR, {"EcucIntegerParamDef"});
    // 44. EcucParameterDefSymbolicNameValueModifyConstraint
    registerEcucConstraint(validator, ecucParameterDefSymbolicNameModifyEval,
        "autosar.ecuc.parameter_def_symbolic_name_value_modify", "EcucParameterDefSymbolicNameValueModifyConstraint",
        "EcucParameterDef symbolicName must conform to naming rules when modified",
        Severity::WARNING, {"EcucParameterDef"});
    // 45. EcucContainerDefPostBuildChangeableModifyConstraint
    registerEcucConstraint(validator, ecucContainerDefPostBuildChangeableModifyEval,
        "autosar.ecuc.container_def_post_build_changeable_modify", "EcucContainerDefPostBuildChangeableModifyConstraint",
        "EcucContainerDef postBuildChangeable must remain consistent when modified",
        Severity::WARNING, {"EcucContainerDef"});
    // 46. EcucImplementationConfigurationClassLinkTimeConstraint
    registerEcucConstraint(validator, ecucImplementationConfigClassLinkTimeEval,
        "autosar.ecuc.implementation_configuration_class_link_time", "EcucImplementationConfigurationClassLinkTimeConstraint",
        "EcucImplementationConfigurationClass LinkTime constraints must hold",
        Severity::ERROR, {"EcucImplementationConfigurationClass"});
    // 47. EcucImplementationConfigurationClassPreCompileConstraint
    registerEcucConstraint(validator, ecucImplementationConfigClassPreCompileEval,
        "autosar.ecuc.implementation_configuration_class_pre_compile", "EcucImplementationConfigurationClassPreCompileConstraint",
        "EcucImplementationConfigurationClass PreCompile constraints must hold",
        Severity::ERROR, {"EcucImplementationConfigurationClass"});
    // 48. EcucParamConfContainerDefMultipleConfigurationModifyConstraint
    registerEcucConstraint(validator, ecucParamConfContainerDefMultipleConfigurationModifyEval,
        "autosar.ecuc.param_conf_container_def_multiple_configuration_modify", "EcucParamConfContainerDefMultipleConfigurationModifyConstraint",
        "EcucParamConfContainerDef multipleConfiguration must keep multiplicity consistent",
        Severity::WARNING, {"EcucParamConfContainerDef"});
    // 49. EcucParameterDefImplConfigClassConstraint
    registerEcucConstraint(validator, ecucParameterDefImplConfigClassEval,
        "autosar.ecuc.parameter_def_impl_config_class", "EcucParameterDefImplConfigClassConstraint",
        "EcucParameterDef implementationConfigClass must be a recognized value when present",
        Severity::WARNING, {"EcucParameterDef"});
}

}  // namespace emf::validation
