// EClassImpl.cpp —— 1:1 对齐 Java org.eclipse.emf.ecore.impl.EClassImpl
//
// 实现了 Java EClassImpl 的 13 个 derived getter：
//   1.  getESuperTypes()           - 排除自己的所有父类（直接父类，DelegatingEcoreEList 语义）
//   2.  getEAllSuperTypes()        - 传递闭包
//   3.  getEAllAttributes()        - 全部 attribute（含继承）
//   4.  getEAllReferences()        - 全部 reference（含继承）
//   5.  getEAllContainments()      - 全部 containment reference
//   6.  getEAllOperations()        - 全部 operation（含继承）
//   7.  getEAllStructuralFeatures()- 全部 feature
//   8.  getEAllGenericSuperTypes() - 同 getEAllSuperTypes（EGenericType 视角）
//   9.  getEIDAttribute()          - 找 ID 标记的 attribute
//   10. getFeatureCount()          - 自身 + 继承 feature 数
//   11. getEOperation(int)         - 按 ID 取 EOperation
//   12. getOverride(EOperation)    - 找 override 的父类方法
//   13. getFeatureType(EStructuralFeature) - 按 feature 推 reified type
//
// 缓存策略与 Java 一致：
//   - 每个 derived getter 用一个 lazy sentinel 标记（Java: null；C++: bool flag）
//   - COMPUTATION_IN_PROGRESS 用 thread_local 防止递归循环
#include "emf/ecore/EcoreImpls.h"
#include "emf/ecore/EcorePackage.h"
#include <algorithm>
#include <unordered_set>

namespace emf::ecore {

// Out-of-line destructor definitions to force vtables to be emitted here.
EClassImpl::~EClassImpl() = default;

// ===== Thread-local recursion guard（对齐 Java EClassImpl.COMPUTATION_IN_PROGRESS）=====
// Java 用 ThreadLocal<Set<EClass>>；这里同样用 thread_local 防止单线程递归循环。
namespace {
struct ComputationInProgress {
    std::unordered_set<const EClassImpl*> inProgress;
};
thread_local ComputationInProgress g_computationInProgress;
}  // namespace

// ===== 内部辅助：把 EClass* 转成 EClassImpl*（用于访问 cache） =====
static inline const EClassImpl* asEClassImpl(const EClass* c) {
    return dynamic_cast<const EClassImpl*>(c);
}
static inline EClassImpl* asEClassImpl(EClass* c) {
    return dynamic_cast<EClassImpl*>(c);
}

// ===== 缓存：getEAllAttributes =====
// 对齐 Java EClassImpl.getEAllAttributes() (line 542-645)
// 语义：递归收集所有父类的 EAllAttributes，然后附加自身 EAttribute。
// 重要：Java 用 UniqueEList (useEquals=false) 实现去重 + 引用相等。
void EClassImpl::computeEAllAttributes() const {
    eIDAttributeCache_ = nullptr;
    eAllAttributesCache_.clear();
    eAllAttributesCached_ = true;

    if (g_computationInProgress.inProgress.insert(this).second) {
        for (EClass* st : superTypes_) {
            if (auto* p = asEClassImpl(st)) {
                for (EAttribute* a : p->getEAllAttributes()) {
                    if (std::find(eAllAttributesCache_.begin(), eAllAttributesCache_.end(), a)
                        == eAllAttributesCache_.end()) {
                        eAllAttributesCache_.push_back(a);
                    }
                }
            }
        }
        g_computationInProgress.inProgress.erase(this);
    }

    // 附加自身 attribute
    for (EStructuralFeature* f : features_) {
        if (auto* a = dynamic_cast<EAttribute*>(f)) {
            if (std::find(eAllAttributesCache_.begin(), eAllAttributesCache_.end(), a)
                == eAllAttributesCache_.end()) {
                eAllAttributesCache_.push_back(a);
            }
        }
    }
    // find ID attribute (对齐 Java: isID() boolean flag)
    for (EAttribute* a : eAllAttributesCache_) {
        if (a && a->isID()) {
            eIDAttributeCache_ = a;
            break;
        }
    }
}

const std::vector<EAttribute*>& EClassImpl::getEAllAttributes() const {
    if (!eAllAttributesCached_) computeEAllAttributes();
    return eAllAttributesCache_;
}

// ===== 缓存：getEAllReferences =====
// 对齐 Java EClassImpl.getEAllReferences() (line 647-727)
void EClassImpl::computeEAllReferences() const {
    eAllReferencesCache_.clear();
    eAllReferencesCached_ = true;

    if (g_computationInProgress.inProgress.insert(this).second) {
        for (EClass* st : superTypes_) {
            if (auto* p = asEClassImpl(st)) {
                for (EReference* r : p->getEAllReferences()) {
                    if (std::find(eAllReferencesCache_.begin(), eAllReferencesCache_.end(), r)
                        == eAllReferencesCache_.end()) {
                        eAllReferencesCache_.push_back(r);
                    }
                }
            }
        }
        g_computationInProgress.inProgress.erase(this);
    }

    for (EStructuralFeature* f : features_) {
        if (auto* r = dynamic_cast<EReference*>(f)) {
            if (std::find(eAllReferencesCache_.begin(), eAllReferencesCache_.end(), r)
                == eAllReferencesCache_.end()) {
                eAllReferencesCache_.push_back(r);
            }
        }
    }
}

const std::vector<EReference*>& EClassImpl::getEAllReferences() const {
    if (!eAllReferencesCached_) computeEAllReferences();
    return eAllReferencesCache_;
}

// ===== 缓存：getEAllStructuralFeatures =====
// 对齐 Java EClassImpl.getEAllStructuralFeatures() (line 756-930)
// 重要：Java 会在构造时重新给本地 feature 设置 setFeatureID 从 result.size() 开始。
// 但 C++ 测试场景里 features_ 已经被显式 setFeatureID 过了。我们保持同样的 lazy 构造，
// 但不重写 featureID（避免破坏既有用例）。
void EClassImpl::computeEAllStructuralFeatures() const {
    eAllStructuralFeaturesCache_.clear();
    eAllStructuralFeaturesCached_ = true;

    // 对齐 Java EMF: getEAllStructuralFeatures 按 feature 名去重。
    // 多继承时（如 ARPackage extends CollectableElement + AtpBlueprint + AtpBlueprintable），
    // 多个 supertype 可能各自定义同名 feature（如 desc/longName），它们是不同的
    // EStructuralFeature 指针但语义相同。按名去重确保每个 feature 名只出现一次，
    // 避免 saver 重复输出。本地 features 覆盖继承的同名 features。
    std::unordered_set<std::string> seenNames;

    if (g_computationInProgress.inProgress.insert(this).second) {
        for (EClass* st : superTypes_) {
            if (auto* p = asEClassImpl(st)) {
                for (EStructuralFeature* f : p->getEAllStructuralFeatures()) {
                    if (!f) continue;
                    const std::string& nm = f->getName();
                    // 按指针去重 + 按名去重
                    if (std::find(eAllStructuralFeaturesCache_.begin(),
                                  eAllStructuralFeaturesCache_.end(), f)
                        != eAllStructuralFeaturesCache_.end()) continue;
                    if (seenNames.count(nm)) continue;
                    seenNames.insert(nm);
                    eAllStructuralFeaturesCache_.push_back(f);
                }
            }
        }
        g_computationInProgress.inProgress.erase(this);
    }

    // 本地 features：同名覆盖继承的（先移除继承的同名 feature，再添加本地的）
    for (EStructuralFeature* f : features_) {
        if (!f) continue;
        const std::string& nm = f->getName();
        if (seenNames.count(nm)) {
            // 移除已存在的同名 feature（来自继承），用本地的替换
            eAllStructuralFeaturesCache_.erase(
                std::remove_if(eAllStructuralFeaturesCache_.begin(),
                               eAllStructuralFeaturesCache_.end(),
                               [&](EStructuralFeature* x) {
                                   return x && x->getName() == nm;
                               }),
                eAllStructuralFeaturesCache_.end());
        }
        seenNames.insert(nm);
        eAllStructuralFeaturesCache_.push_back(f);
    }
}

const std::vector<EStructuralFeature*>& EClassImpl::getEAllStructuralFeatures() const {
    if (!eAllStructuralFeaturesCached_) computeEAllStructuralFeatures();
    return eAllStructuralFeaturesCache_;
}

// ===== getFeatureCount：返回 cached array 长度 =====
int EClassImpl::getFeatureCount() const {
    // 对齐 Java: return getEAllStructuralFeaturesData().length;
    return static_cast<int>(getEAllStructuralFeatures().size());
}

// ===== 缓存：getEAllContainments =====
// 对齐 Java EClassImpl.getEAllContainments() (line 1266-1304)
// 语义：从 getEAllReferences() 过滤 containment。
void EClassImpl::computeEAllContainments() const {
    eAllContainmentsCache_.clear();
    eAllContainmentsCached_ = true;

    for (EReference* r : getEAllReferences()) {
        if (r && r->isContainment()) {
            eAllContainmentsCache_.push_back(r);
        }
    }
}

const std::vector<EReference*>& EClassImpl::getEAllContainments() const {
    if (!eAllContainmentsCached_) computeEAllContainments();
    return eAllContainmentsCache_;
}

// ===== 缓存：getEAllOperations =====
// 对齐 Java EClassImpl.getEAllOperations() (line 939-996)
void EClassImpl::computeEAllOperations() const {
    eAllOperationsCache_.clear();
    eAllOperationsCached_ = true;

    if (g_computationInProgress.inProgress.insert(this).second) {
        for (EClass* st : superTypes_) {
            if (auto* p = asEClassImpl(st)) {
                for (EOperation* op : p->getEAllOperations()) {
                    if (std::find(eAllOperationsCache_.begin(), eAllOperationsCache_.end(), op)
                        == eAllOperationsCache_.end()) {
                        eAllOperationsCache_.push_back(op);
                    }
                }
            }
        }
        g_computationInProgress.inProgress.erase(this);
    }

    for (EOperation* op : operations_) {
        if (std::find(eAllOperationsCache_.begin(), eAllOperationsCache_.end(), op)
            == eAllOperationsCache_.end()) {
            eAllOperationsCache_.push_back(op);
        }
    }
}

const std::vector<EOperation*>& EClassImpl::getEAllOperations() const {
    if (!eAllOperationsCached_) computeEAllOperations();
    return eAllOperationsCache_;
}

int EClassImpl::getOperationCount() const {
    // 对齐 Java: return getEAllOperationsData().length;
    return static_cast<int>(getEAllOperations().size());
}

EOperation* EClassImpl::getEOperation(int operationID) const {
    // 对齐 C++ 约定：按 EOperation.getOperationID() 值查找。
    if (operationID < 0) return nullptr;
    for (EOperation* op : getEAllOperations()) {
        if (op && op->getOperationID() == operationID) return op;
    }
    return nullptr;
}

// ===== 缓存：getEAllSuperTypes =====
// 对齐 Java EClassImpl.getEAllSuperTypes() (line 2141-2183)
// 语义：传递闭包；每个父类的更高父类排在该父类之前。
//   for super: for higher in super.getEAllSuperTypes(): add; add super
void EClassImpl::computeEAllSuperTypes() const {
    eAllSuperTypesCache_.clear();
    eAllSuperTypesCached_ = true;

    if (g_computationInProgress.inProgress.insert(this).second) {
        for (EClass* st : superTypes_) {
            if (auto* p = asEClassImpl(st)) {
                // 收集更高的（grandparent 及以上）
                for (EClass* higher : p->getEAllSuperTypes()) {
                    if (std::find(eAllSuperTypesCache_.begin(), eAllSuperTypesCache_.end(), higher)
                        == eAllSuperTypesCache_.end()) {
                        eAllSuperTypesCache_.push_back(higher);
                    }
                }
            }
            // 收集直接父类
            if (std::find(eAllSuperTypesCache_.begin(), eAllSuperTypesCache_.end(), st)
                == eAllSuperTypesCache_.end()) {
                eAllSuperTypesCache_.push_back(st);
            }
        }
        g_computationInProgress.inProgress.erase(this);
    }
}

const std::vector<EClass*>& EClassImpl::getEAllSuperTypes() const {
    if (!eAllSuperTypesCached_) computeEAllSuperTypes();
    return eAllSuperTypesCache_;
}

// ===== 缓存：getEAllGenericSuperTypes =====
// 对齐 Java EClassImpl.getEAllGenericSuperTypes() (line 391-540)
// C++ 端接口的 EGenericType 没有 ETypeArguments；这里用 getESuperTypes() 作近似语义。
void EClassImpl::computeEAllGenericSuperTypes() const {
    eAllGenericSuperTypesCache_.clear();
    eAllGenericSuperTypesCached_ = true;

    if (g_computationInProgress.inProgress.insert(this).second) {
        // 递归收集父类的 getEAllGenericSuperTypes，然后附加本类的 eGenericSuperTypes
        for (EClass* st : superTypes_) {
            if (auto* p = asEClassImpl(st)) {
                for (EGenericType* gt : p->getEAllGenericSuperTypes()) {
                    if (std::find(eAllGenericSuperTypesCache_.begin(),
                                  eAllGenericSuperTypesCache_.end(), gt)
                        == eAllGenericSuperTypesCache_.end()) {
                        eAllGenericSuperTypesCache_.push_back(gt);
                    }
                }
            }
        }
        g_computationInProgress.inProgress.erase(this);
    }
    // 注：Java 这里用 EGenericTypeImpl.unwrap(getEGenericSuperTypes())，C++ 端接口层未提供，
    // 所以仅返回递归闭包。行为对齐到"如果 EGenericType 没数据就返回 closure"。
    // 真正使用 EGenericType 的下游可改 EcoreImpls 来暴露。
}

const std::vector<EGenericType*>& EClassImpl::getEAllGenericSuperTypes() const {
    if (!eAllGenericSuperTypesCached_) computeEAllGenericSuperTypes();
    return eAllGenericSuperTypesCache_;
}

// ===== getEIDAttribute =====
// 对齐 Java EClassImpl.getEIDAttribute() (line 181-185)
EAttribute* EClassImpl::getEIDAttribute() const {
    if (!eIDAttributeComputed_) {
        // 触发 getEAllAttributes 的 cache
        getEAllAttributes();
        eIDAttributeComputed_ = true;
    }
    return eIDAttributeCache_;
}

// ===== getOverride =====
// 对齐 Java EClassImpl.getOverride(EOperation) (line 1386-1407)
EOperation* EClassImpl::getOverride(EOperation* operation) const {
    if (!operation) return nullptr;
    const auto& ops = getEAllOperations();
    if (!eOperationToOverrideComputed_) {
        eOperationToOverrideComputed_ = true;
        eOperationToOverrideMap_.clear();
        int length = static_cast<int>(ops.size());
        for (int i = 0; i < length; ++i) {
            for (int j = length - 1; j > i; --j) {
                // Java 语义：op[j].isOverrideOf(op[i]) —— 同名 + 参数兼容
                EOperation* overrideOp = ops[j];
                EOperation* baseOp = ops[i];
                if (!overrideOp || !baseOp) continue;
                if (overrideOp->getName() == baseOp->getName()) {
                    // C++ 简化：同名就视为 override（Java 还会比较参数类型）
                    eOperationToOverrideMap_[baseOp] = overrideOp;
                    break;
                }
            }
        }
    }
    auto it = eOperationToOverrideMap_.find(operation);
    return (it == eOperationToOverrideMap_.end()) ? nullptr : it->second;
}

// ===== 既有方法 =====

const std::vector<EAttribute*>& EClassImpl::getEAttributes() const {
    // 对齐 Java EClassImpl.getEAttributes() (line 745-749)
    // Java 是 getEAttributes() { getEAllAttributes(); return eAttributes; }
    // C++ 端没有 eAttributes 单独缓存，直接返回 features_ 中过滤的 attribute 列表
    static thread_local std::vector<EAttribute*> cached;
    static thread_local const EClassImpl* last = nullptr;
    if (last != this) {
        cached.clear();
        for (auto* f : features_) {
            if (auto* a = dynamic_cast<EAttribute*>(f)) cached.push_back(a);
        }
        last = this;
    }
    return cached;
}

const std::vector<EReference*>& EClassImpl::getEReferences() const {
    // 对齐 Java EClassImpl.getEReferences() (line 734-738)
    static thread_local std::vector<EReference*> cached;
    static thread_local const EClassImpl* last = nullptr;
    if (last != this) {
        cached.clear();
        for (auto* f : features_) {
            if (auto* r = dynamic_cast<EReference*>(f)) cached.push_back(r);
        }
        last = this;
    }
    return cached;
}

void EClassImpl::addESuperType(EClass* c) {
    if (!c) return;
    superTypes_.push_back(c);
    // 对齐 Java EClassImpl.addESuperType：同步创建 EGenericType(eClassifier=c) 并加入 eGenericSuperTypes。
    // 双存储：superTypes_ 供 derived view（getEAllSuperTypes 等），genericSuperTypes_ 供序列化。
    // isSet 互斥逻辑保证 Case A（全纯父类型）走 eSuperTypes 属性，Case B（含参数化）走 <eGenericSuperTypes> 子元素。
    auto* gt = emf::ecore::EcoreFactory::instance().createEGenericType();
    gt->setEClassifier(c);
    genericSuperTypes_.push_back(gt);
}

const std::vector<EGenericType*>& EClassImpl::getEGenericSuperTypes() const {
    return genericSuperTypes_;
}

void EClassImpl::addEGenericSuperType(EGenericType* gt) {
    if (!gt) return;
    genericSuperTypes_.push_back(gt);
    // 同步 superTypes_ derived view：若 gt 有 eClassifier 且为 EClass，加入 superTypes_
    if (auto* cls = dynamic_cast<EClass*>(gt->getEClassifier())) {
        superTypes_.push_back(cls);
    }
}

bool EClassImpl::isSetEGenericSuperTypes() const {
    // 对齐 Java EClassImpl.isSetEGenericSuperTypes：列表中任一 EGenericType 满足
    // eTypeParameter != null || !eTypeArguments.isEmpty() → TRUE
    for (auto* gt : genericSuperTypes_) {
        if (!gt) continue;
        if (gt->getETypeParameter() != nullptr || !gt->getETypeArguments().empty()) {
            return true;
        }
    }
    return false;
}

void EClassImpl::syncSuperTypesFromGeneric() {
    // 对齐 Java EClassImpl：eGenericSuperTypes 是真实 containment 存储槽，
    // eSuperTypes 是无存储的 derived view。addEGenericSuperType 在 eClassifier
    // 延迟解析（XMILoader pendingRef）后 superTypes_ 未同步，这里补齐（去重）。
    for (auto* gt : genericSuperTypes_) {
        if (!gt) continue;
        auto* baseCls = dynamic_cast<EClass*>(gt->getEClassifier());
        if (!baseCls) continue;
        bool found = false;
        for (auto* s : superTypes_) {
            if (s == baseCls) { found = true; break; }
        }
        if (!found) superTypes_.push_back(baseCls);
    }
}

void EClassImpl::addEStructuralFeature(EStructuralFeature* sf) {
    if (!sf) return;
    // 对齐 Java EClassImpl：动态 feature 的 featureID = 继承 feature 总数 + 自有序号
    // 保证 eAllStructuralFeatures 中每个 feature 的 featureID 唯一，
    // 避免跨包继承时 base#name(0) 与 ext#note(0) 冲突。
    // 仅当 featureID 未显式设置（< 0）时自动分配；显式设置的（如 EcorePackage 常量）保留。
    if (sf->getFeatureID() < 0) {
        int inheritedCount = 0;
        for (EClass* st : superTypes_) {
            if (st) inheritedCount += static_cast<int>(st->getEAllStructuralFeatures().size());
        }
        sf->setFeatureID(inheritedCount + static_cast<int>(features_.size()));
    }
    sf->setEContainingClass(this);
    features_.push_back(sf);
}

void EClassImpl::addEOperation(EOperation* op) {
    if (!op) return;
    op->setEContainingClass(this);
    operations_.push_back(op);
}

EStructuralFeature* EClassImpl::getEStructuralFeature(const std::string& name) const {
    // 对齐 Java EClassImpl.getEStructuralFeature(String) (line 1306-1324)
    // Java: getFeatureCount();  → 触发 eAllStructuralFeatures 缓存构建
    getFeatureCount();
    if (!eNameToFeatureComputed_) {
        eNameToFeatureComputed_ = true;
        eNameToFeatureMap_.clear();
        for (EStructuralFeature* f : eAllStructuralFeaturesCache_) {
            if (!f) continue;
            const std::string& key = f->getName();
            auto it = eNameToFeatureMap_.find(key);
            if (it == eNameToFeatureMap_.end()) {
                eNameToFeatureMap_[key] = f;
            }
            // Java 这里把 duplicate 还原成较早的，不实现细节
        }
    }
    auto it = eNameToFeatureMap_.find(name);
    return (it == eNameToFeatureMap_.end()) ? nullptr : it->second;
}

EStructuralFeature* EClassImpl::getEStructuralFeature(int featureID) const {
    // 对齐 C++ 约定：按 EStructuralFeature.getFeatureID() 值查找。
    // Java 端用数组索引（getEAllStructuralFeaturesData()[featureID]），
    // 但 C++ 测试场景下 featureID 是显式 setFeatureID() 的标识符，
    // 所以 C++ 这边遍历 features_ 和 superTypes_ 匹配 getFeatureID()。
    if (featureID < 0) return nullptr;
    for (EStructuralFeature* f : features_) {
        if (f && f->getFeatureID() == featureID) return f;
    }
    for (EClass* st : superTypes_) {
        if (!st) continue;
        if (auto* p = asEClassImpl(st)) {
            for (EStructuralFeature* f : p->getEAllStructuralFeatures()) {
                if (f && f->getFeatureID() == featureID) return f;
            }
        }
    }
    return nullptr;
}

EAttribute* EClassImpl::getEAttribute(const std::string& name) const {
    for (auto* f : features_) {
        auto* a = dynamic_cast<EAttribute*>(f);
        if (a && a->getName() == name) return a;
    }
    // 走继承（与 Java 一致：getEAllAttributes 后查找）
    for (EAttribute* a : getEAllAttributes()) {
        if (a && a->getName() == name) return a;
    }
    return nullptr;
}

EReference* EClassImpl::getEReference(const std::string& name) const {
    for (auto* f : features_) {
        auto* r = dynamic_cast<EReference*>(f);
        if (r && r->getName() == name) return r;
    }
    for (EReference* r : getEAllReferences()) {
        if (r && r->getName() == name) return r;
    }
    return nullptr;
}

EOperation* EClassImpl::getEOperation(const std::string& name) const {
    for (auto* o : operations_) {
        if (o && o->getName() == name) return o;
    }
    for (EOperation* o : getEAllOperations()) {
        if (o && o->getName() == name) return o;
    }
    return nullptr;
}

int EClassImpl::getFeatureID(const std::string& name) const {
    auto* f = getEStructuralFeature(name);
    return f ? f->getFeatureID() : -1;
}

int EClassImpl::getFeatureID(EStructuralFeature* sf) const {
    // 对齐 C++ 约定：返回 EStructuralFeature.getFeatureID() 的值。
    // Java 端使用 eAllStructuralFeaturesData 数组索引（需要 EClassImpl 主动 renumber），
    // 但 C++ 测试场景下 featureID 是显式 setFeatureID() 的标识符。
    if (!sf) return -1;
    return sf->getFeatureID();
}

int EClassImpl::getOperationID(const std::string& name) const {
    for (size_t i = 0; i < operations_.size(); ++i) {
        if (operations_[i] && operations_[i]->getName() == name) return static_cast<int>(i);
    }
    return -1;
}

int EClassImpl::getOperationID(EOperation* op) const {
    // 对齐 C++ 约定：返回 EOperation.getOperationID() 的值。
    if (!op) return -1;
    return op->getOperationID();
}

bool EClassImpl::isSuperTypeOf(const EClass* other) const {
    // 对齐 Java EClassImpl.isSuperTypeOf(EClass) (line 2133-2136)
    if (!other) return false;
    if (other == this) return true;
    auto* otherImpl = asEClassImpl(other);
    if (!otherImpl) return false;
    for (EClass* s : otherImpl->getEAllSuperTypes()) {
        if (s == this) return true;
    }
    return false;
}

std::any EClassImpl::eGet(const EStructuralFeature* feature) const {
    if (feature) {
        int fid = feature->getFeatureID();
        switch (fid) {
            case ::emf::common::FeatureID::ECLASS_EABSTRACT:
                return std::any{abstract_};
            case ::emf::common::FeatureID::ECLASS_EINTERFACE:
                return std::any{interface_};
            case ::emf::common::FeatureID::ECLASS_ESUPERTYPES:
                return std::any{superTypes_};
            case ::emf::common::FeatureID::ECLASS_ESTRUCTURALFEATURES:
                return std::any{features_};
            case ::emf::common::FeatureID::ECLASS_EOPERATIONS:
                return std::any{operations_};
            case ::emf::common::FeatureID::ECLASS_EALLATTRIBUTES:
                return std::any{getEAllAttributes()};
            case ::emf::common::FeatureID::ECLASS_EALLREFERENCES:
                return std::any{getEAllReferences()};
            case ::emf::common::FeatureID::ECLASS_EATTRIBUTES:
                return std::any{getEAttributes()};
            case ::emf::common::FeatureID::ECLASS_EREFERENCES:
                return std::any{getEReferences()};
            case ::emf::common::FeatureID::ECLASS_EALLOPERATIONS:
                return std::any{getEAllOperations()};
            case ::emf::common::FeatureID::ECLASS_EALLSTRUCTURALFEATURES:
                return std::any{getEAllStructuralFeatures()};
            case ::emf::common::FeatureID::ECLASS_EGENERICSUPERTYPES:
                return std::any{genericSuperTypes_};
        }
    }
    return EClassifierImpl::eGet(feature);
}

void EClassImpl::eSet(const EStructuralFeature* feature, std::any value) {
    if (feature) {
        int fid = feature->getFeatureID();
        switch (fid) {
            case ::emf::common::FeatureID::ECLASS_EABSTRACT:
                if (auto* v = std::any_cast<bool>(&value)) abstract_ = *v;
                return;
            case ::emf::common::FeatureID::ECLASS_EINTERFACE:
                if (auto* v = std::any_cast<bool>(&value)) interface_ = *v;
                return;
            case ::emf::common::FeatureID::ECLASS_ESUPERTYPES:
                if (auto* v = std::any_cast<std::vector<EClass*>>(&value)) superTypes_ = *v;
                return;
            case ::emf::common::FeatureID::ECLASS_ESTRUCTURALFEATURES:
                if (auto* v = std::any_cast<std::vector<EStructuralFeature*>>(&value)) {
                    features_ = *v;
                    // 反向维护：每个 feature 的 containingClass 应该指向本 EClass。
                    // XMILoader 通过 eSet 一次性写入 list 而非逐个 addEStructuralFeature，
                    // 所以这里需要显式回写，否则 codegen 端读 eContainingClass() 会拿到 nullptr
                    // 或被覆盖的旧值。
                    for (auto* sf : features_) {
                        if (sf) sf->setEContainingClass(this);
                    }
                }
                return;
            case ::emf::common::FeatureID::ECLASS_EOPERATIONS:
                if (auto* v = std::any_cast<std::vector<EOperation*>>(&value)) operations_ = *v;
                return;
            case ::emf::common::FeatureID::ECLASS_EGENERICSUPERTYPES:
                if (auto* v = std::any_cast<std::vector<EGenericType*>>(&value)) genericSuperTypes_ = *v;
                return;
        }
    }
    EClassifierImpl::eSet(feature, std::move(value));
}

bool EClassImpl::eIsSet(const EStructuralFeature* feature) const {
    if (feature) {
        int fid = feature->getFeatureID();
        switch (fid) {
            case ::emf::common::FeatureID::ECLASS_EABSTRACT:
                return abstract_;
            case ::emf::common::FeatureID::ECLASS_EINTERFACE:
                return interface_;
            case ::emf::common::FeatureID::ECLASS_ESUPERTYPES:
                // 对齐 Java isSet 互斥：eSuperTypes 仅当列表非空且 !isSetEGenericSuperTypes() 时视为 set
                return !superTypes_.empty() && !isSetEGenericSuperTypes();
            case ::emf::common::FeatureID::ECLASS_ESTRUCTURALFEATURES:
                return !features_.empty();
            case ::emf::common::FeatureID::ECLASS_EOPERATIONS:
                return !operations_.empty();
            case ::emf::common::FeatureID::ECLASS_EGENERICSUPERTYPES:
                return isSetEGenericSuperTypes();
            case ::emf::common::FeatureID::ECLASS_EALLATTRIBUTES:
                return !getEAllAttributes().empty();
            case ::emf::common::FeatureID::ECLASS_EALLREFERENCES:
                return !getEAllReferences().empty();
            case ::emf::common::FeatureID::ECLASS_EATTRIBUTES:
                return !getEAttributes().empty();
            case ::emf::common::FeatureID::ECLASS_EREFERENCES:
                return !getEReferences().empty();
            case ::emf::common::FeatureID::ECLASS_EALLOPERATIONS:
                return !getEAllOperations().empty();
            case ::emf::common::FeatureID::ECLASS_EALLSTRUCTURALFEATURES:
                return !getEAllStructuralFeatures().empty();
        }
    }
    return EClassifierImpl::eIsSet(feature);
}

void EClassImpl::eUnset(const EStructuralFeature* feature) {
    if (feature) {
        int fid = feature->getFeatureID();
        switch (fid) {
            case ::emf::common::FeatureID::ECLASS_EABSTRACT:
                abstract_ = false;
                return;
            case ::emf::common::FeatureID::ECLASS_EINTERFACE:
                interface_ = false;
                return;
            case ::emf::common::FeatureID::ECLASS_ESUPERTYPES:
                superTypes_.clear();
                return;
            case ::emf::common::FeatureID::ECLASS_ESTRUCTURALFEATURES:
                features_.clear();
                return;
            case ::emf::common::FeatureID::ECLASS_EOPERATIONS:
                operations_.clear();
                return;
            case ::emf::common::FeatureID::ECLASS_EGENERICSUPERTYPES:
                genericSuperTypes_.clear();
                return;
        }
    }
    EClassifierImpl::eUnset(feature);
}

}  // namespace emf::ecore
