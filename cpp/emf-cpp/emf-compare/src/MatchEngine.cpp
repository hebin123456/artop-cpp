// EMF Compare: MatchEngine
// 对齐 org.eclipse.emf.compare.match.DefaultMatchEngine (Java)
//
// 增强（对齐 Java 语义）：
//   - 子对象匹配采用 proximity（贪心最佳相似度）匹配，而非纯按索引配对
//     对齐 Java ProximityEObjectMatcher：同 EClass 中按相似度贪心 1:1 配对
//   - 3-way origin 匹配采用结构位置匹配，确保变更对象也能与 origin 对应
#include "emf/compare/MatchEngine.h"

#include "emf/common/EObject.h"
#include "emf/common/EList.h"
#include "emf/ecore/EcorePackage.h"
#include "emf/ecore/util/EcoreUtil.h"  // EcoreUtil::equalsValue（统一值比较）

#include <vector>
#include <algorithm>

namespace emf::compare {

namespace {

// 值比较统一复用 EcoreUtil::equalsValue（对齐 Java EqualityHelper.haveEqualValue 语义）。
// 覆盖基础类型、EObject*（proxy 按 eProxyURI，非 proxy 指针身份）、多值 EAttribute/EReference。
// 旧 MatchEngine 内的本地 anyEquals 已删除以消除三处重复实现。

// 自动 ID 检测（P0-1 对齐 Java DefaultMatchEngine）：
// 查 EClass 的 EAllAttributes 中是否有 isID()==true 的 EAttribute，
// 若有则取其 eGet 值作为 identifier 返回（空字符串表示无 ID）。
// 对齐 Java EcoreUtil.getID(EObject) 语义。
std::string getAutoId(emf::common::EObject* obj) {
    if (!obj) return "";
    auto* cls = obj->eClass();
    if (!cls) return "";
    for (auto* sf : cls->getEAllStructuralFeatures()) {
        if (!sf) continue;
        auto* attr = dynamic_cast<emf::ecore::EAttribute*>(sf);
        if (!attr || !attr->isID()) continue;
        std::any v = obj->eGet(attr);
        if (!v.has_value()) continue;
        // 常见 ID 类型：string / int / long
        if (v.type() == typeid(std::string)) {
            return std::any_cast<std::string>(v);
        }
        if (v.type() == typeid(int)) {
            return std::to_string(std::any_cast<int>(v));
        }
        if (v.type() == typeid(long)) {
            return std::to_string(std::any_cast<long>(v));
        }
    }
    return "";
}

// 计算两个对象在所有可比较单值 feature 上的相似度（0~1）
// 对齐 Java AbstractSimilarityChecker 的 attribute 相似度：
//   - 只看 EAttribute + 单值非 containment EReference
//   - containment reference 的差异由子 match 体现，不参与父对象相似度
//   - proxy reference 由 EcoreUtil::equalsValue 按 eProxyURI 比较
//   - 非 proxy 的跨对象 reference：match 阶段尚无 Match 映射，跳过（不计入 total），
//     避免指针不同导致的相似度下降（对齐 Java：reference 相似度由子 match 后计算）
double computeSimilarity(emf::common::EObject* left, emf::common::EObject* right) {
    if (!left || !right) return 0.0;
    if (left == right) return 1.0;
    auto* lc = left->eClass();
    auto* rc = right->eClass();
    if (!lc || !rc || lc->getName() != rc->getName()) return 0.0;
    double similar = 0.0, total = 0.0;
    for (auto* sf : lc->getEAllStructuralFeatures()) {
        if (!sf) continue;
        if (!sf->isChangeable() || sf->isDerived() || sf->isTransient()) continue;
        if (auto* ref = dynamic_cast<emf::ecore::EReference*>(sf)) {
            if (ref->isMany()) continue;
            if (ref->isContainment()) continue;  // containment 由子 match 体现
            // 非 proxy 的跨对象 reference：match 阶段无 Match 映射，跳过
            std::any lv = left->eGet(sf);
            std::any rv = right->eGet(sf);
            if (lv.has_value() && rv.has_value()
                && lv.type() == typeid(emf::common::EObject*)
                && rv.type() == typeid(emf::common::EObject*)) {
                auto* lo = std::any_cast<emf::common::EObject*>(lv);
                auto* ro = std::any_cast<emf::common::EObject*>(rv);
                if (lo && ro && !lo->eIsProxy() && !ro->eIsProxy() && lo != ro) {
                    continue;  // 非 proxy 跨对象引用，跳过
                }
            }
        }
        total += 1.0;
        if (emf::ecore::util::EcoreUtil::equalsValue(left->eGet(sf), right->eGet(sf))) similar += 1.0;
    }
    return total > 0.0 ? similar / total : 1.0;
}

}  // namespace

void MatchEngine::match(emf::common::EObject* left,
                        emf::common::EObject* right,
                        Comparison& comp) {
    // 顶层 matchOne
    Match* root = matchOne(left, right, comp);
    if (!root) return;
    if (!left || !right) return;

    auto leftContents = left->eContents();
    auto rightContents = right->eContents();
    std::vector<bool> rightUsed(rightContents.size(), false);

    // P0-1：先做自动 ID 匹配（对齐 Java DefaultMatchEngine 的 IdentifierEObjectMatcher）
    // 对齐 Java 语义：有 ID 的对象严格按 ID 匹配，不回退到 proximity；
    //   无 ID 的对象才走 proximity。这样不同 ID 的对象不会被错误配对。
    // ID 来源：手动 registerIdentifier > IdentifierProvider（artop 注入）> 自动 ID 属性
    bool idMatchEnabled = useIdMatcher_ || useIdAttr_ || static_cast<bool>(idProvider_);
    // right 端 (EClass name, ID) -> index 映射
    std::unordered_map<std::string, int> rightIdIndex;
    // rightHasId[j]：rightContents[j] 是否有 ID（有 ID 的 right 不参与 proximity）
    std::vector<bool> rightHasId(rightContents.size(), false);
    if (idMatchEnabled) {
        for (size_t j = 0; j < rightContents.size(); ++j) {
            auto* rc = rightContents[j];
            if (!rc) continue;
            auto [id, hasId] = resolveId(rc);
            if (hasId) {
                rightHasId[j] = true;
                auto* rcc = rc->eClass();
                if (rcc) {
                    std::string key = rcc->getName() + "\x01" + id;
                    rightIdIndex[key] = (int)j;
                }
            }
        }
    }

    // P1 性能优化：无 ID 的 right 按 EClass name 分桶，proximity 只在同类型桶内匹配。
    // 原 O(L×R) 降到 O(Σ L_k×R_k)，对类型多样的模型（如 AUTOSAR 数百种 EClass）显著加速。
    // 对齐 Java ProximityEObjectMatcher 的类型预过滤。
    // 注：相似度缓存未实现——当前调用模式下每对 (left,right) 的 computeSimilarity 只算一次
    // （2-way 与 3-way 的 left-origin 是不同对），缓存无收益反而增 hash 开销。
    std::unordered_map<std::string, std::vector<int>> rightTypeBuckets;
    for (size_t j = 0; j < rightContents.size(); ++j) {
        if (rightHasId[j] || rightUsed[j] || !rightContents[j]) continue;
        auto* rcc = rightContents[j]->eClass();
        if (rcc) rightTypeBuckets[rcc->getName()].push_back((int)j);
    }

    // 1) 每个 left 子对象：有 ID 则严格按 ID 匹配（不回退），无 ID 则 proximity 匹配
    for (size_t li = 0; li < leftContents.size(); ++li) {
        auto* lc = leftContents[li];
        if (!lc) continue;

        // 取 left 对象的 ID（手动注册 > provider > 自动 ID 属性）
        auto [leftId, hasLeftId] = idMatchEnabled ? resolveId(lc) : std::make_pair(std::string{}, false);

        bool matched = false;
        if (hasLeftId) {
            // P0-1：有 ID 的对象严格按 ID 匹配，不回退到 proximity
            // 对齐 Java IdentifierEObjectMatcher：ID 不同 → 不配对 → 成为 ADD/DELETE
            auto* lcc = lc->eClass();
            if (lcc) {
                std::string key = lcc->getName() + "\x01" + leftId;
                auto rit = rightIdIndex.find(key);
                if (rit != rightIdIndex.end() && !rightUsed[rit->second]) {
                    int idx = rit->second;
                    rightUsed[idx] = true;
                    match(lc, rightContents[idx], comp);
                    matched = true;
                }
            }
            // 无 ID 匹配 → 留待下方作为 ABSENT_RIGHT 处理
        } else {
            // 无 ID → proximity 匹配（对齐 Java ProximityEObjectMatcher 贪心 1:1 配对）
            // P1：只在同 EClass name 的桶内匹配（类型分桶），省去遍历不同类型的开销
            int bestIdx = -1;
            double bestSim = -1.0;
            auto* lcc = lc->eClass();
            // P1 同位置优先：对 identical 模型（同文件加载两次），left[i] 与 right[i]
            // 相似度=1.0。先试同位置 right，命中则 O(1)，避免桶内 O(K) 扫描。
            // perfect（sim>=1.0）直接采纳；否则记为初值，桶循环找更好的（含同位置自身）。
            if (lcc && li < rightContents.size() && !rightUsed[li]
                && rightContents[li] && !rightHasId[li]) {
                auto* rc = rightContents[li];
                auto* rcc = rc->eClass();
                if (rcc && lcc->getName() == rcc->getName()) {
                    double sim = computeSimilarity(lc, rc);
                    bestIdx = (int)li;
                    bestSim = sim;
                }
            }
            // 未 perfect 命中 → proximity 桶内搜索更佳匹配（带早停）
            // 跳过 bestIdx（同位置已算过），避免重算；允许桶内找到 sim 更高的替换。
            if (bestSim < 1.0 && lcc) {
                auto bit = rightTypeBuckets.find(lcc->getName());
                if (bit != rightTypeBuckets.end()) {
                    for (int j : bit->second) {
                        if (rightUsed[j] || j == bestIdx) continue;  // 跳过已算的同位置
                        auto* rc = rightContents[j];
                        double sim = computeSimilarity(lc, rc);
                        if (sim > bestSim) {
                            bestSim = sim;
                            bestIdx = j;
                        }
                        // P1 早停：perfect match（sim>=1.0）是最优，无需继续搜索。
                        if (bestSim >= 1.0) break;
                    }
                }
            }
            if (bestIdx >= 0) {
                rightUsed[bestIdx] = true;
                match(lc, rightContents[bestIdx], comp);
                matched = true;
            }
        }

        if (!matched) {
            // 无可匹配的 right → ABSENT_RIGHT（DELETE）
            match(lc, nullptr, comp);
        }
    }
    // 2) 未配对的 right 子对象 → ABSENT_LEFT（ADD）
    for (size_t j = 0; j < rightContents.size(); ++j) {
        if (!rightUsed[j] && rightContents[j]) {
            match(nullptr, rightContents[j], comp);
        }
    }
}

Match* MatchEngine::matchOne(emf::common::EObject* left,
                             emf::common::EObject* right,
                             Comparison& comp) {
    // 同对象 -> IDENTICAL
    if (left == right && left != nullptr) {
        return &comp.addMatch(left, right, MatchKind::IDENTICAL, 1.0);
    }

    // 一边缺失
    if (left == nullptr && right != nullptr) {
        return &comp.addMatch(nullptr, right, MatchKind::ABSENT_LEFT, 0.0);
    }
    if (right == nullptr && left != nullptr) {
        return &comp.addMatch(left, nullptr, MatchKind::ABSENT_RIGHT, 0.0);
    }
    if (left == nullptr && right == nullptr) {
        return nullptr;
    }

    // IdentifierEObjectMatcher：xmi:id 匹配（跨文件场景）
    if (useIdMatcher_) {
        auto lid = idMap_.find(left);
        auto rid = idMap_.find(right);
        if (lid != idMap_.end() && rid != idMap_.end()
            && lid->second == rid->second
            && !lid->second.empty()) {
            auto* lc = left->eClass();
            auto* rc = right->eClass();
            if (lc && rc && lc->getName() == rc->getName()) {
                return &comp.addMatch(left, right, MatchKind::IDENTICAL, 1.0);
            }
        }
    }

    // 比较 EClass
    auto* lc = left->eClass();
    auto* rc = right->eClass();
    if (!lc || !rc || lc->getName() != rc->getName()) {
        return &comp.addMatch(left, right, MatchKind::DIFFERENT, 0.0);
    }

    // 相似度
    double sim = computeSimilarity(left, right);
    MatchKind kind = (sim >= threshold_) ? MatchKind::IDENTICAL : MatchKind::DIFFERENT;
    return &comp.addMatch(left, right, kind, sim);
}

void MatchEngine::registerIdentifier(emf::common::EObject* obj, const std::string& id) {
    if (obj) idMap_[obj] = id;
}
const std::string* MatchEngine::getIdentifier(emf::common::EObject* obj) const {
    auto it = idMap_.find(obj);
    return it != idMap_.end() ? &it->second : nullptr;
}

// 取对象的 ID：手动注册 > IdentifierProvider > 自动 ID 属性
// 对齐 artop IdentifiableUtil：artop 层注入 provider 返回 shortName/uuid，
// emf-compare 层透明使用。返回 {id, hasId}：hasId=true 表示有 ID（严格按 ID 匹配）。
std::pair<std::string, bool> MatchEngine::resolveId(emf::common::EObject* obj) {
    if (!obj) return {"", false};
    // 1) 手动 registerIdentifier（最高优先级）
    auto it = idMap_.find(obj);
    if (it != idMap_.end()) {
        return {it->second, !it->second.empty()};
    }
    // 2) 外部 IdentifierProvider（artop 层注入，对齐 IdentifiableUtil）
    if (idProvider_) {
        std::string id = idProvider_(obj);
        if (!id.empty()) return {std::move(id), true};
    }
    // 3) 自动 ID 属性（EClass 有 isID()==true 的 EAttribute）
    if (useIdAttr_) {
        std::string id = getAutoId(obj);
        if (!id.empty()) return {std::move(id), true};
    }
    return {"", false};
}

// ===== 3-way match（对齐 Java DefaultMatchEngine 3-way） =====
// 策略：先做 2-way left-right match（proximity），再通过 origin-left 结构位置匹配
// 为每个 match 设置 origin。
namespace {

// 递归按结构位置匹配 origin 与 left，建立 left* → origin* 映射
// 对齐 Java：3-way 中 origin 与 left/right 的匹配同 2-way，按结构对应
void matchOriginRecursive(emf::common::EObject* left,
                          emf::common::EObject* origin,
                          std::unordered_map<emf::common::EObject*, emf::common::EObject*>& leftToOrigin) {
    if (!left || !origin) return;
    // 同 EClass 才对应
    auto* lc = left->eClass();
    auto* oc = origin->eClass();
    if (!lc || !oc || lc->getName() != oc->getName()) return;
    leftToOrigin[left] = origin;

    auto leftChildren = left->eContents();
    auto originChildren = origin->eContents();
    // 按结构位置配对（同 2-way 的 proximity 思路，但 origin 匹配用位置）
    // P1：origin 按 EClass name 分桶，proximity 只在同类型桶内匹配
    std::vector<bool> originUsed(originChildren.size(), false);
    std::unordered_map<std::string, std::vector<int>> originTypeBuckets;
    for (size_t j = 0; j < originChildren.size(); ++j) {
        if (!originChildren[j]) continue;
        auto* occ = originChildren[j]->eClass();
        if (occ) originTypeBuckets[occ->getName()].push_back((int)j);
    }
    for (auto* lchild : leftChildren) {
        if (!lchild) continue;
        int bestIdx = -1;
        double bestSim = -1.0;
        auto* lcc = lchild->eClass();
        if (lcc) {
            auto bit = originTypeBuckets.find(lcc->getName());
            if (bit != originTypeBuckets.end()) {
                for (int j : bit->second) {
                    if (originUsed[j]) continue;
                    auto* ochild = originChildren[j];
                    double sim = computeSimilarity(lchild, ochild);
                    if (sim > bestSim) {
                        bestSim = sim;
                        bestIdx = j;
                    }
                    if (bestSim >= 1.0) break;  // P1 早停
                }
            }
        }
        if (bestIdx >= 0) {
            originUsed[bestIdx] = true;
            matchOriginRecursive(lchild, originChildren[bestIdx], leftToOrigin);
        }
    }
}

}  // namespace

void MatchEngine::match(emf::common::EObject* left,
                        emf::common::EObject* right,
                        emf::common::EObject* origin,
                        Comparison& comp) {
    comp.setThreeWay(true);
    // 1. 先做 2-way left-right match
    match(left, right, comp);

    // 2. 通过 origin-left 结构位置匹配，为每个 match 设置 origin
    if (!origin) return;
    std::unordered_map<emf::common::EObject*, emf::common::EObject*> leftToOrigin;
    if (left) {
        matchOriginRecursive(left, origin, leftToOrigin);
    }
    // 设置 origin 到已创建的 match 上
    for (auto& m : comp.getMatches()) {
        if (m.getLeft() && leftToOrigin.count(m.getLeft())) {
            m.setOrigin(leftToOrigin[m.getLeft()]);
        }
    }
}

}  // namespace emf::compare
