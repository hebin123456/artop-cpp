// EMF Compare: ConflictDetector 实现
// 对齐 org.eclipse.emf.compare.internal.ConflictDetector (Java)
#include "emf/compare/ConflictDetector.h"
#include "emf/compare/Diff.h"  // Diff::getAttributeName（Comparison.h 仅前置声明）

#include "emf/common/EObject.h"
#include "emf/common/EList.h"
#include "emf/ecore/EcoreImpls.h"  // EReference / EStructuralFeature
#include "emf/ecore/util/EcoreUtil.h"  // EcoreUtil::equalsValue（统一值比较）

#include <any>
#include <unordered_map>
#include <unordered_set>
#include <vector>

namespace emf::compare {

namespace {

// 从 eGet 返回的 std::any 中取出 EObject 指针列表（多值 reference）
// 注：Comparison.cpp 匿名命名空间内有同名 helper，此处为编译单元本地副本，
// 避免跨翻译单元链接问题（按任务约束不共享）。
std::vector<emf::common::EObject*> getEObjectList(const std::any& v) {
    std::vector<emf::common::EObject*> out;
    if (!v.has_value()) return out;
    if (v.type() == typeid(emf::common::EList<emf::common::EObject*>*)) {
        auto* lst = std::any_cast<emf::common::EList<emf::common::EObject*>*>(v);
        if (lst) {
            for (size_t i = 0; i < lst->size(); ++i) out.push_back((*lst)[i]);
        }
    }
    return out;
}

// 计算多值 feature 的集合差：left 相对 origin 添加/删除了哪些元素
// 对齐 Java SetDifference：通过 sideToOrigin 映射把 side 端元素映射到 origin 端比较
// sideToOrigin：side（left 或 right）EObject -> origin EObject 映射，从 Match.getOrigin() 构建
void computeSetDelta(const std::vector<emf::common::EObject*>& sideList,
                     const std::vector<emf::common::EObject*>& originList,
                     const std::unordered_map<emf::common::EObject*, emf::common::EObject*>& sideToOrigin,
                     std::vector<emf::common::EObject*>& added,
                     std::vector<emf::common::EObject*>& removed) {
    auto containsInOrigin = [&](emf::common::EObject* sobj) -> bool {
        if (!sobj) return false;
        for (auto* o : originList) {
            if (!o) continue;
            if (o == sobj) return true;  // 同指针
            // 通过 sideToOrigin 映射比较
            auto it = sideToOrigin.find(sobj);
            if (it != sideToOrigin.end() && it->second == o) return true;
        }
        return false;
    };
    auto containsInSide = [&](emf::common::EObject* oobj) -> bool {
        if (!oobj) return false;
        for (auto* s : sideList) {
            if (!s) continue;
            if (s == oobj) return true;
            auto it = sideToOrigin.find(s);
            if (it != sideToOrigin.end() && it->second == oobj) return true;
        }
        return false;
    };
    for (auto* s : sideList) {
        if (s && !containsInOrigin(s)) added.push_back(s);
    }
    for (auto* o : originList) {
        if (o && !containsInSide(o)) removed.push_back(o);
    }
}

}  // namespace

// 3-way 冲突检测（对齐 Java ConflictDetector）
// 遍历 comparison 的 match（含 origin），对每个 match：
//   - left 和 right 相对 origin 都修改了同一 feature 且改后值不同 → REAL 冲突
//   - left 和 right 相对 origin 都修改了同一 feature 但改后值相同 → PSEUDO 冲突
// 同时 left 端和 right 端各自的 diff 也参与判定。
// P0-3：扩展到多值 feature：left/right 各自相对 origin 添加/删除了元素，
//   若元素集合不同 → REAL；相同 → PSEUDO
void ConflictDetector::detectConflicts(Comparison& comp) {
    if (!comp.isThreeWay()) return;
    // 构建 side→origin 映射（从 Match.getOrigin()），用于多值 feature 中跨端元素比较
    // leftToOrigin / rightToOrigin：side 端 EObject -> 其对应 origin EObject
    std::unordered_map<emf::common::EObject*, emf::common::EObject*> leftToOrigin;
    std::unordered_map<emf::common::EObject*, emf::common::EObject*> rightToOrigin;
    // leftToRight：用于多值 feature 冲突判定（left-added 与 right-added 是否配对）
    std::unordered_map<emf::common::EObject*, emf::common::EObject*> leftToRight;
    for (auto& mm : comp.getMatches()) {
        if (mm.getLeft() && mm.getOrigin()) leftToOrigin[mm.getLeft()] = mm.getOrigin();
        if (mm.getRight() && mm.getOrigin()) rightToOrigin[mm.getRight()] = mm.getOrigin();
        if (mm.getLeft() && mm.getRight()) leftToRight[mm.getLeft()] = mm.getRight();
    }
    for (auto& m : comp.getMatches()) {
        auto* left = m.getLeft();
        auto* right = m.getRight();
        auto* origin = m.getOrigin();
        if (!left || !right || !origin) continue;
        auto* cls = left->eClass();
        if (!cls) continue;
        // 逐 feature 比较：left vs origin、right vs origin
        for (auto* sf : cls->getEAllStructuralFeatures()) {
            if (!sf) continue;
            if (sf->isDerived() || sf->isTransient() || !sf->isChangeable()) continue;
            auto* ref = dynamic_cast<emf::ecore::EReference*>(sf);

            std::any ov = origin->eGet(sf);
            std::any lv = left->eGet(sf);
            std::any rv = right->eGet(sf);
            bool leftChanged, rightChanged, sameResult;

            if (ref && ref->isMany()) {
                // P0-3：多值 feature 冲突检测
                // 用元素集合比较：left/right 各自相对 origin 添加/删除了哪些元素
                auto oList = getEObjectList(ov);
                auto lList = getEObjectList(lv);
                auto rList = getEObjectList(rv);
                std::vector<emf::common::EObject*> lAdded, lRemoved;
                computeSetDelta(lList, oList, leftToOrigin, lAdded, lRemoved);
                std::vector<emf::common::EObject*> rAdded, rRemoved;
                computeSetDelta(rList, oList, rightToOrigin, rAdded, rRemoved);
                leftChanged = !lAdded.empty() || !lRemoved.empty();
                rightChanged = !rAdded.empty() || !rRemoved.empty();
                if (!leftChanged || !rightChanged) continue;
                // PSEUDO 判定（对齐 Java ConflictKind）：
                //   added：left-added 的每个元素在 right-added 中有匹配伙伴（通过 leftToRight）
                //   removed：lRemoved 与 rRemoved 引用同一组 origin 对象（直接集合相等）
                //   两边 added/removed 都"等价" → PSEUDO；否则 → REAL
                bool addedEquivalent = (lAdded.size() == rAdded.size());
                if (addedEquivalent) {
                    std::unordered_set<emf::common::EObject*> rAddedSet(rAdded.begin(), rAdded.end());
                    for (auto* la : lAdded) {
                        auto it = leftToRight.find(la);
                        if (it == leftToRight.end() ||
                            rAddedSet.find(it->second) == rAddedSet.end()) {
                            addedEquivalent = false;
                            break;
                        }
                    }
                }
                bool removedEquivalent = (lRemoved.size() == rRemoved.size());
                if (removedEquivalent) {
                    std::unordered_set<emf::common::EObject*> lRemovedSet(lRemoved.begin(), lRemoved.end());
                    for (auto* rr : rRemoved) {
                        if (lRemovedSet.find(rr) == lRemovedSet.end()) {
                            removedEquivalent = false;
                            break;
                        }
                    }
                }
                sameResult = addedEquivalent && removedEquivalent;
            } else {
                // 单值 feature：直接值比较
                leftChanged = !emf::ecore::util::EcoreUtil::equalsValue(lv, ov);
                rightChanged = !emf::ecore::util::EcoreUtil::equalsValue(rv, ov);
                if (!leftChanged || !rightChanged) continue;
                sameResult = emf::ecore::util::EcoreUtil::equalsValue(lv, rv);
            }

            // 两边都改了
            Conflict c(sameResult ? ConflictKind::PSEUDO : ConflictKind::REAL);
            // 关联该 match 上针对此 feature 的 diff
            for (auto* d : m.getDiffs()) {
                if (d && d->getAttributeName() == sf->getName()) {
                    c.getDifferences().push_back(d);
                }
            }
            comp.getConflicts().push_back(std::move(c));
        }
    }
}

}  // namespace emf::compare
