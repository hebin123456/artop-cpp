// EMF Compare: EquivalenceEngine 实现
// 对齐 org.eclipse.emf.compare.internal.EquivalenceEngine (Java)
#include "emf/compare/EquivalenceEngine.h"

#include "emf/common/EObject.h"
#include "emf/common/EList.h"
#include "emf/ecore/EcoreImpls.h"  // EReference / EStructuralFeature

#include <any>
#include <unordered_map>
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

}  // namespace

// P0-8：计算 Equivalence（对齐 Java EMFCompare 的 Equivalence 计算）
// 遍历 comparison 的所有 match，对每个 match 对象的非 containment EReference：
//   若引用的目标对象也在 comparison 中有对应 Match，则把这两个 Match 加入同一 Equivalence。
// 这样调用方可以知道：source match 的 ADD/DELETE 隐含 reference change。
void EquivalenceEngine::computeEquivalences(Comparison& comp) {
    // 构建 left/right EObject -> Match* 映射
    std::unordered_map<emf::common::EObject*, Match*> leftObjToMatch;
    std::unordered_map<emf::common::EObject*, Match*> rightObjToMatch;
    for (auto& m : comp.getMatches()) {
        if (m.getLeft()) leftObjToMatch[m.getLeft()] = &m;
        if (m.getRight()) rightObjToMatch[m.getRight()] = &m;
    }
    // 收集非 containment EReference 的目标对象（含单值和多值）
    auto collectRefTargets = [&](emf::common::EObject* src,
                                  std::vector<emf::common::EObject*>& targets) {
        if (!src) return;
        auto* cls = src->eClass();
        if (!cls) return;
        for (auto* sf : cls->getEAllStructuralFeatures()) {
            if (!sf) continue;
            auto* ref = dynamic_cast<emf::ecore::EReference*>(sf);
            if (!ref || ref->isContainment()) continue;  // 只看非 containment
            std::any v = src->eGet(ref);
            if (!v.has_value()) continue;
            if (ref->isMany()) {
                auto list = getEObjectList(v);
                for (auto* t : list) if (t) targets.push_back(t);
            } else {
                // 单值 reference
                if (v.type() == typeid(emf::common::EObject*)) {
                    auto* t = std::any_cast<emf::common::EObject*>(v);
                    if (t) targets.push_back(t);
                }
            }
        }
    };
    for (auto& m : comp.getMatches()) {
        emf::common::EObject* src = m.getLeft() ? m.getLeft() : m.getRight();
        if (!src) continue;
        std::vector<emf::common::EObject*> targets;
        collectRefTargets(src, targets);
        for (auto* target : targets) {
            if (!target) continue;
            Match* targetMatch = nullptr;
            auto lit = leftObjToMatch.find(target);
            if (lit != leftObjToMatch.end()) targetMatch = lit->second;
            if (!targetMatch) {
                auto rit = rightObjToMatch.find(target);
                if (rit != rightObjToMatch.end()) targetMatch = rit->second;
            }
            if (targetMatch && targetMatch != &m) {
                Equivalence eq;
                eq.addMatch(&m);
                eq.addMatch(targetMatch);
                comp.getEquivalences().push_back(std::move(eq));
            }
        }
    }
}

}  // namespace emf::compare
