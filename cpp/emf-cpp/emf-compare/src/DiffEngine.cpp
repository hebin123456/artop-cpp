// EMF Compare: DiffEngine
// 对齐 org.eclipse.emf.compare.diff.DefaultDiffEngine (Java)
//
// 增强（对齐 Java 语义）：
//   - DIFFERENT match：逐 feature 比较，为每个不同的 EAttribute/EReference 产 per-feature CHANGE diff
//   - ABSENT_LEFT match（right 存在，left 缺失）：产 ADD diff
//   - ABSENT_RIGHT match（left 存在，right 缺失）：产 DELETE diff
//   - 有序多值 containment reference 中元素位置变化：产 MOVE diff（对齐 Java ReferenceChange kind=MOVE）
//   - 每个 Diff 设置 match 反向引用（对齐 Java Diff.getMatch()）
#include "emf/compare/DiffEngine.h"
#include "emf/compare/Diff.h"  // Diff
#include "emf/ecore/EcorePackage.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/ecore/util/EcoreUtil.h"  // EcoreUtil::equalsValue（统一值比较）
#include "emf/common/EObject.h"
#include "emf/common/EList.h"

#include <algorithm>
#include <any>
#include <vector>
#include <unordered_map>

namespace emf::compare {

namespace {

// 值比较统一复用 EcoreUtil::equalsValue（对齐 Java EqualityHelper.haveEqualValue 语义）。
// 该函数覆盖基础类型、EObject*（proxy 按 eProxyURI，非 proxy 指针身份）、
// 多值 EAttribute（EList<T>*）、多值 EReference（vector<EObject*>*/EList<EObject*>*）。
// 旧 DiffEngine 内的本地 anyEquals 已删除以消除三处重复实现。

// 辅助：从 eGet 返回的 std::any 中取出 EObject 指针列表（多值 reference）
std::vector<emf::common::EObject*> getEObjectList(const std::any& v) {
    std::vector<emf::common::EObject*> out;
    if (!v.has_value()) return out;
    // EList<EObject*>* 形式
    if (v.type() == typeid(emf::common::EList<emf::common::EObject*>*)) {
        auto* lst = std::any_cast<emf::common::EList<emf::common::EObject*>*>(v);
        if (lst) {
            for (size_t i = 0; i < lst->size(); ++i) out.push_back((*lst)[i]);
        }
    }
    return out;
}

// 为每个不同的 EAttribute 产 per-attribute CHANGE diff
void diffAttributes(Match& m, emf::ecore::EClass* cls, Comparison& comp) {
    auto* left = m.getLeft();
    auto* right = m.getRight();
    for (auto* sf : cls->getEAllStructuralFeatures()) {
        if (!sf) continue;
        // 跳过 derived / non-changeable / transient（对齐 Java FeatureFilter）
        if (sf->isDerived() || sf->isTransient() || !sf->isChangeable()) continue;
        auto* attr = dynamic_cast<emf::ecore::EAttribute*>(sf);
        if (!attr) continue;  // 只处理 EAttribute
        std::any lv = left->eGet(attr);
        std::any rv = right->eGet(attr);
        if (!emf::ecore::util::EcoreUtil::equalsValue(lv, rv)) {
            auto* d = new Diff();
            d->setKind(DiffKind::CHANGE);
            d->setType(DiffType::ATTRIBUTE_CHANGE);  // P0-2：子类型
            d->setAttributeName(attr->getName());
            d->setLeft(left);
            d->setRight(right);
            d->setSource(DifferenceSource::RIGHT);
            d->setOldValue(lv);  // P0-2：旧值（left 端）
            d->setNewValue(rv);  // P0-2：新值（right 端）
            d->setMatch(&m);
            m.getDiffs().push_back(d);
            comp.getDifferences().push_back(d);
        }
    }
}

// 为每个不同的单值非 containment EReference 产 per-reference CHANGE diff
// 对齐 Java DefaultDiffEngine.ReferenceChangeChecker：
//   - containment reference 的差异由子 match 的 ADD/DELETE 体现，此处跳过
//   - 跨 resource 的 proxy reference 由 eObjectEquals 按 proxyURI 比较（已修复）
//   - 同 resource 内非 proxy 引用：查 Comparison 的 left→right Match 映射，
//     有 Match 关联则视为相等（对齐 Java DefaultEqualityHelper 按 Match 判断）
//   - 多值 reference 的 ADD/DELETE/MOVE 由子 match 的 ABSENT 和 detectMoves 处理
// leftToRight 由 diff() 入口一次性构建并传入，避免 per-match 重建导致 O(n²)。
void diffSingleValueReferences(Match& m, emf::ecore::EClass* cls, Comparison& comp,
                               const std::unordered_map<emf::common::EObject*, emf::common::EObject*>& leftToRight) {
    auto* left = m.getLeft();
    auto* right = m.getRight();
    for (auto* sf : cls->getEAllStructuralFeatures()) {
        if (!sf) continue;
        if (sf->isDerived() || sf->isTransient() || !sf->isChangeable()) continue;
        auto* ref = dynamic_cast<emf::ecore::EReference*>(sf);
        if (!ref || ref->isMany()) continue;  // 只处理单值 reference
        if (ref->isContainment()) continue;   // containment 由子 match 处理
        std::any lv = left->eGet(ref);
        std::any rv = right->eGet(ref);
        if (!emf::ecore::util::EcoreUtil::equalsValue(lv, rv)) {
            // 非 proxy 的跨对象引用：查 Match 映射，有对应则视为相等
            if (lv.has_value() && rv.has_value()
                && lv.type() == typeid(emf::common::EObject*)
                && rv.type() == typeid(emf::common::EObject*)) {
                auto* lo = std::any_cast<emf::common::EObject*>(lv);
                auto* ro = std::any_cast<emf::common::EObject*>(rv);
                if (lo && ro && !lo->eIsProxy() && !ro->eIsProxy()) {
                    auto it = leftToRight.find(lo);
                    if (it != leftToRight.end() && it->second == ro) {
                        continue;  // 有 Match 关联，视为相等
                    }
                }
            }
            auto* d = new Diff();
            d->setKind(DiffKind::CHANGE);
            d->setType(DiffType::REFERENCE_CHANGE);  // P0-2：子类型
            d->setAttributeName(ref->getName());
            d->setLeft(left);
            d->setRight(right);
            d->setSource(DifferenceSource::RIGHT);
            d->setOldValue(lv);  // P0-2：旧值（left 端引用）
            d->setNewValue(rv);  // P0-2：新值（right 端引用）
            d->setMatch(&m);
            m.getDiffs().push_back(d);
            comp.getDifferences().push_back(d);
        }
    }
}

// 检测有序多值 reference 中的 MOVE（位置变化）
// 对齐 Java：DefaultDiffEngine 在 ordered=true 的多值 reference 中，
// 若某元素在左右都存在（通过 Match 关联）但位置不同，产 ReferenceChange kind=MOVE。
//
// G8：用 LCS（最长公共子序列）计算最小 MOVE 集合，对齐 Java EMF Compare 的 LCS 算法。
//   - LCS 中的元素保持相对顺序不变 → 不产 MOVE（锚定）
//   - 不在 LCS 中但两端均存在的元素 → 产 MOVE（oldIdx→newIdx）
//   - 相比“每个 oldIdx!=newIdx 都产 MOVE”的朴素法，LCS 把 [A,B,C]→[A,C,B] 的 2 个 MOVE
//     降为 1 个（最小集），且单 MOVE 的 oldIdx/newIdx 在 merge 时应用 list->move 结果正确。
//   - 注意：朴素法对多 MOVE 顺序应用会因索引漂移而互相 revert，LCS 最小集避免此问题。
//
// 内存保护：LCS DP 为 O(n²) 空间。n > kLcsMaxN 时退化为朴素 O(n) 逐元素检测
// （对超大有序列表避免 GB 级分配；此类列表罕见，且朴素法仍是可接受的近似）。
//
// left/right 中的对象是不同指针，需通过 Comparison 的 Match 映射关联。
// leftToRight 由 diff() 入口一次性构建并传入，避免 per-match 重建导致 O(n²)。
void detectMoves(Match& m, emf::ecore::EClass* cls, Comparison& comp,
                 const std::unordered_map<emf::common::EObject*, emf::common::EObject*>& leftToRight) {
    auto* left = m.getLeft();
    auto* right = m.getRight();
    if (!left || !right) return;

    constexpr int kLcsMaxN = 2048;  // LCS DP 空间上限（2048² int ≈ 16MB）

    for (auto* sf : cls->getEAllStructuralFeatures()) {
        if (!sf) continue;
        if (sf->isDerived() || sf->isTransient() || !sf->isChangeable()) continue;
        auto* ref = dynamic_cast<emf::ecore::EReference*>(sf);
        if (!ref || !ref->isMany()) continue;
        // Java：只有有序 reference 才检测 MOVE
        if (!ref->isOrdered()) continue;

        auto leftList = getEObjectList(left->eGet(ref));
        auto rightList = getEObjectList(right->eGet(ref));
        if (leftList.size() != rightList.size()) continue;  // 数量不同由 ADD/DELETE 处理
        int n = (int)leftList.size();
        if (n == 0) continue;

        // 建立 right 端对象 -> 索引 的映射（用指针身份）
        std::unordered_map<emf::common::EObject*, int> rightIndex;
        for (int k = 0; k < n; ++k) {
            if (rightList[k]) rightIndex[rightList[k]] = k;
        }

        // matchPair(i,j)：leftList[i] 与 rightList[j] 是否通过 Match 配对
        auto matchPair = [&](int i, int j) -> bool {
            auto* lo = leftList[i];
            if (!lo) return false;
            auto it = leftToRight.find(lo);
            if (it == leftToRight.end()) return false;
            return it->second == rightList[j];
        };

        // 判定哪些 left 索引在 LCS 中（锚定，不产 MOVE）
        std::vector<bool> inLcsLeft(n, false);
        if (n <= kLcsMaxN) {
            // G8：LCS DP。dp[i][j] = leftList[0..i-1] 与 rightList[0..j-1] 的 LCS 长度
            std::vector<std::vector<int>> dp(n + 1, std::vector<int>(n + 1, 0));
            for (int i = 1; i <= n; ++i) {
                for (int j = 1; j <= n; ++j) {
                    if (matchPair(i - 1, j - 1)) {
                        dp[i][j] = dp[i - 1][j - 1] + 1;
                    } else {
                        dp[i][j] = std::max(dp[i - 1][j], dp[i][j - 1]);
                    }
                }
            }
            // 回溯标记 LCS 中的 left 索引
            std::vector<bool> inLcsRight(n, false);
            int i = n, j = n;
            while (i > 0 && j > 0) {
                if (matchPair(i - 1, j - 1) && dp[i][j] == dp[i - 1][j - 1] + 1) {
                    inLcsLeft[i - 1] = true;
                    inLcsRight[j - 1] = true;
                    --i; --j;
                } else if (dp[i - 1][j] >= dp[i][j - 1]) {
                    --i;
                } else {
                    --j;
                }
            }
            (void)inLcsRight;  // 仅 left 锚定标记用于产 MOVE
        }
        // n > kLcsMaxN 时 inLcsLeft 全 false → 退化为朴素逐元素检测（见下）

        // 不在 LCS 中但两端均存在的元素 → MOVE（最小集合）
        for (int oldIdx = 0; oldIdx < n; ++oldIdx) {
            if (inLcsLeft[oldIdx]) continue;  // 锚定，不移动
            auto* lo = leftList[oldIdx];
            if (!lo) continue;
            auto mapIt = leftToRight.find(lo);
            if (mapIt == leftToRight.end()) continue;  // 无匹配，由 DELETE 处理
            emf::common::EObject* ro = mapIt->second;
            auto it = rightIndex.find(ro);
            if (it == rightIndex.end()) continue;
            int newIdx = it->second;
            // LCS 路径：锚定元素已跳过，此处 oldIdx!=newIdx 必然成立；
            // 朴素路径（n>kLcsMaxN）：需显式比较，仅 oldIdx!=newIdx 才产 MOVE
            if (n <= kLcsMaxN || oldIdx != newIdx) {
                auto* d = new Diff();
                d->setKind(DiffKind::MOVE);
                d->setType(DiffType::REFERENCE_CHANGE);  // P0-2：子类型
                d->setAttributeName(ref->getName());
                d->setLeft(lo);
                d->setRight(ro);
                d->setSource(DifferenceSource::RIGHT);
                d->setOldIndex(oldIdx);
                d->setNewIndex(newIdx);
                d->setOldValue(std::any(lo));  // P0-2：移动元素指针
                d->setNewValue(std::any(ro));
                d->setMatch(&m);
                m.getDiffs().push_back(d);
                comp.getDifferences().push_back(d);
            }
        }
    }
}

}  // namespace

void DiffEngine::diff(Comparison& comp) {
    // 一次性构建 left→right 映射（通过 Match 关联），供 diffMatch 内的
    // diffSingleValueReferences / detectMoves 复用。
    // 性能关键：原实现在每个 match 内重建此映射，导致 O(n²)。
    // 对 57153 对象的 AUTOSAR 模型，O(n²) ≈ 3.27B 操作 → >90s；
    // 一次性构建后降为 O(n)，diff 总耗时从 >90s 降到亚秒级。
    leftToRight_.clear();
    leftToRight_.reserve(comp.getMatches().size());
    for (auto& m : comp.getMatches()) {
        if (m.getLeft() && m.getRight()) {
            leftToRight_[m.getLeft()] = m.getRight();
        }
    }

    for (auto& m : comp.getMatches()) {
        diffMatch(m, comp);
    }
}

void DiffEngine::diffMatch(Match& m, Comparison& comp) {
    auto* left = m.getLeft();
    auto* right = m.getRight();

    switch (m.getKind()) {
        case MatchKind::IDENTICAL:
            // 完全相同：仍检测 MOVE（顺序变化但内容相同的有序多值引用）
            // Java：IDENTICAL 的 match 不产 diff，但有序多值的位置变化会被单独检测
            // 这里保守地也检测 MOVE，确保不丢失位置差异
            {
                auto* cls = left ? left->eClass() : nullptr;
                if (cls && left && right) {
                    detectMoves(m, cls, comp, leftToRight_);
                }
            }
            break;

        case MatchKind::DIFFERENT: {
            // 逐 feature 比较，产 per-feature CHANGE diff
            auto* cls = left ? left->eClass() : (right ? right->eClass() : nullptr);
            if (cls && left && right) {
                diffAttributes(m, cls, comp);
                diffSingleValueReferences(m, cls, comp, leftToRight_);
                detectMoves(m, cls, comp, leftToRight_);
            }
            // 退化：feature 都相同但 match 仍判为 DIFFERENT（如 EClass 不同）
            if (m.getDiffs().empty()) {
                auto* d = new Diff();
                d->setKind(DiffKind::CHANGE);
                d->setType(DiffType::ELEMENT_CHANGE);  // P0-2：整对象 CHANGE
                d->setAttributeName("");
                d->setLeft(left);
                d->setRight(right);
                d->setSource(DifferenceSource::RIGHT);
                d->setOldValue(std::any(left));   // P0-2
                d->setNewValue(std::any(right));
                d->setMatch(&m);
                m.getDiffs().push_back(d);
                comp.getDifferences().push_back(d);
            }
            break;
        }

        case MatchKind::ABSENT_LEFT: {
            // right 存在，left 缺失 → ADD
            auto* d = new Diff();
            d->setKind(DiffKind::ADD);
            d->setType(DiffType::ELEMENT_CHANGE);  // P0-2：整对象 ADD
            d->setAttributeName("");
            d->setLeft(nullptr);
            d->setRight(right);
            d->setSource(DifferenceSource::RIGHT);
            d->setNewValue(std::any(right));  // P0-2：新增值
            d->setMatch(&m);
            m.getDiffs().push_back(d);
            comp.getDifferences().push_back(d);
            break;
        }

        case MatchKind::ABSENT_RIGHT: {
            // left 存在，right 缺失 → DELETE
            auto* d = new Diff();
            d->setKind(DiffKind::DELETE);
            d->setType(DiffType::ELEMENT_CHANGE);  // P0-2：整对象 DELETE
            d->setAttributeName("");
            d->setLeft(left);
            d->setRight(nullptr);
            d->setSource(DifferenceSource::LEFT);
            d->setOldValue(std::any(left));  // P0-2：删除值
            d->setMatch(&m);
            m.getDiffs().push_back(d);
            comp.getDifferences().push_back(d);
            break;
        }
    }
}

}  // namespace emf::compare
