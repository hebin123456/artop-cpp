// EMF Compare: MergeEngine
// 对齐 org.eclipse.emf.compare.merge.DefaultMerger (Java)
//
// 合并策略（对齐 Java IMerger.merge(diff, leftToRight)）：
//   RIGHT_TO_LEFT (leftToRight=false)：把 RIGHT 的变更同步到 LEFT，target=left 根对象
//   LEFT_TO_RIGHT (leftToRight=true)：把 LEFT 的变更同步到 RIGHT，target=right 根对象
//
// 遍历 Comparison 所有 Match 的 Diff：
//   - CHANGE：把源端的 feature 值复制到目标端
//     P0-6：reference 的 CHANGE 时，把源端 EObject 映射到对应目标端 EObject（用 srcToDst）
//   - ADD：把源端新增对象克隆（EcoreUtil::copy）后挂到目标端容器
//     P0-5：不再直接复用源端指针，避免跨树共享对象
//     G6/G12：克隆后注册 srcToDst[added]=cloned，供后续 REFERENCE_CHANGE 映射引用
//   - DELETE：从目标端容器移除被删对象
//   - MOVE：在目标端的有序多值 reference 中调整位置
//   - P0-7：设置 reference 时维护 eOpposite（双向引用的对称端）
//
// G6/G12：合并前由 RequirementEngine 计算 Diff 依赖，按拓扑序逐 Diff 合并。
#include "emf/compare/MergeEngine.h"
#include "emf/compare/RequirementEngine.h"
#include "emf/compare/Diff.h"
#include "emf/compare/Comparison.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/ecore/util/EcoreUtil.h"  // P0-5：EcoreUtil::copy
#include "emf/common/EObject.h"
#include "emf/common/EList.h"

#include <any>
#include <functional>
#include <unordered_map>
#include <vector>

namespace emf::compare {

namespace {

// 从 eGet 返回的 std::any 中取出 EObject 指针列表（多值 reference）
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

// 把 EList<EObject*>* 写回（多值 reference 设置）
emf::common::EList<emf::common::EObject*>* getEObjectListRef(const std::any& v) {
    if (!v.has_value()) return nullptr;
    if (v.type() == typeid(emf::common::EList<emf::common::EObject*>*)) {
        return std::any_cast<emf::common::EList<emf::common::EObject*>*>(v);
    }
    return nullptr;
}

// 递归构建 child* -> container* 映射（遍历 containment 树）
void buildContainerMap(emf::common::EObject* root,
                       std::unordered_map<emf::common::EObject*, emf::common::EObject*>& out) {
    if (!root) return;
    for (auto* child : root->eContents()) {
        if (!child) continue;
        out[child] = root;
        buildContainerMap(child, out);
    }
}

// 在 container 中查找包含 obj 的多值 containment reference，返回引用 + 索引
struct ListLoc {
    emf::common::EList<emf::common::EObject*>* list = nullptr;
    int index = -1;
    emf::ecore::EReference* ref = nullptr;
};
ListLoc findInContainer(emf::common::EObject* container, emf::common::EObject* obj) {
    ListLoc loc;
    if (!container || !obj) return loc;
    auto* cls = container->eClass();
    if (!cls) return loc;
    for (auto* sf : cls->getEAllStructuralFeatures()) {
        if (!sf) continue;
        auto* ref = dynamic_cast<emf::ecore::EReference*>(sf);
        if (!ref || !ref->isMany() || !ref->isContainment()) continue;
        auto list = getEObjectList(container->eGet(ref));
        for (int i = 0; i < (int)list.size(); ++i) {
            if (list[i] == obj) {
                loc.list = getEObjectListRef(container->eGet(ref));
                loc.index = i;
                loc.ref = ref;
                return loc;
            }
        }
    }
    return loc;
}

// 在 container 中查找名为 name 的多值 containment reference，返回其 reference 名
std::string findContainmentRefName(emf::common::EObject* container, emf::common::EObject* obj) {
    if (!container || !obj) return "";
    auto* cls = container->eClass();
    if (!cls) return "";
    for (auto* sf : cls->getEAllStructuralFeatures()) {
        if (!sf) continue;
        auto* r = dynamic_cast<emf::ecore::EReference*>(sf);
        if (!r || !r->isMany() || !r->isContainment()) continue;
        auto list = getEObjectList(container->eGet(r));
        for (auto* o : list) {
            if (o == obj) return r->getName();
        }
    }
    return "";
}

// 在 container 中查找名为 name 的多值 reference，返回其 EList 引用
emf::common::EList<emf::common::EObject*>* findManyListByName(emf::common::EObject* container,
                                                                const std::string& name,
                                                                emf::ecore::EReference** outRef = nullptr) {
    if (!container) return nullptr;
    auto* cls = container->eClass();
    if (!cls) return nullptr;
    for (auto* sf : cls->getEAllStructuralFeatures()) {
        if (!sf || sf->getName() != name) continue;
        auto* ref = dynamic_cast<emf::ecore::EReference*>(sf);
        if (!ref || !ref->isMany()) continue;
        if (outRef) *outRef = ref;
        return getEObjectListRef(container->eGet(ref));
    }
    return nullptr;
}

// P0-7：维护 eOpposite
// 当在 dstObj 上设置 refFeat = value 后，若 refFeat 有 eOpposite，则反向设置 value 的 opposite 端
// - 单值 opposite：value.eSet(opposite, dstObj)
// - 多值 opposite：value 的 opposite list 添加 dstObj
// 对齐 Java BasicEObjectImpl.eInverseAdd / eInverseRemove 的双向维护语义
void maintainEOpposite(emf::common::EObject* dstObj,
                       emf::ecore::EReference* refFeat,
                       emf::common::EObject* value,
                       bool isAdd) {
    if (!dstObj || !refFeat || !value) return;
    auto* opp = refFeat->getEOpposite();
    if (!opp) return;
    // 反向引用的 dstObj 即为正向的 value，反向的 value 即为正向的 dstObj
    if (opp->isMany()) {
        auto* oppList = getEObjectListRef(value->eGet(opp));
        if (oppList) {
            if (isAdd) {
                if (!oppList->contains(dstObj)) oppList->add(dstObj);
            } else {
                oppList->remove(dstObj);
            }
        }
    } else {
        if (isAdd) {
            value->eSet(opp, std::any(dstObj));
        } else {
            // 仅当当前值就是 dstObj 时才 unset
            auto cur = value->eGet(opp);
            if (cur.has_value() && cur.type() == typeid(emf::common::EObject*)) {
                if (std::any_cast<emf::common::EObject*>(cur) == dstObj) {
                    value->eUnset(opp);
                }
            }
        }
    }
}

// G6/G12：拓扑排序 Comparison 的 Diff，返回被依赖者（target）在前的序列。
// Dependency(source, target) 表示 source 依赖 target，target 须先合并。
// DFS 后序：访问 node 前先访问其所有 requires（target），再 emit node。
// 环检测：遇到 visiting 状态的节点视为环，打破（不再递归）以避免死循环，
//         保持原始顺序的稳定性（对齐 Java 对环 diff 的容错处理）。
std::vector<Diff*> topologicalSort(Comparison& comp) {
    std::vector<Diff*> all = comp.getDifferences();  // 拷贝（保持原始顺序作遍历起点）
    // requires[node] = node 所依赖的 target diff 列表
    std::unordered_map<Diff*, std::vector<Diff*>> requires;
    for (auto& dep : comp.getDependencies()) {
        requires[dep.getSource()].push_back(dep.getTarget());
    }
    std::unordered_map<Diff*, int> state;  // 0=unvisited, 1=visiting, 2=done
    std::vector<Diff*> result;
    result.reserve(all.size());
    std::function<void(Diff*)> visit = [&](Diff* node) {
        if (!node) return;
        int& st = state[node];
        if (st == 2) return;  // 已完成
        if (st == 1) return;  // 环：打破，不再递归
        st = 1;
        auto it = requires.find(node);
        if (it != requires.end()) {
            for (auto* tgt : it->second) visit(tgt);
        }
        st = 2;
        result.push_back(node);  // 依赖全部 emit 后才 emit 本节点
    };
    for (auto* d : all) visit(d);
    return result;
}

}  // namespace

bool MergeEngine::merge(Comparison& comp, emf::common::EObject* target) {
    return merge(comp, target, MergeDirection::RIGHT_TO_LEFT);
}

bool MergeEngine::merge(Comparison& comp, emf::common::EObject* target, MergeDirection direction) {
    if (!target) return false;

    // 构建源→目标 对象映射（用于 ADD 时定位目标容器、CHANGE reference 时映射 EObject）
    // RIGHT_TO_LEFT：src=right，dst=left
    // LEFT_TO_RIGHT：src=left，dst=right
    // G6/G12：srcToDst 改为可变映射，ADD 克隆新对象后注册进来，供后续 REFERENCE_CHANGE 映射。
    std::unordered_map<emf::common::EObject*, emf::common::EObject*> srcToDst;
    emf::common::EObject* srcRoot = nullptr;
    for (auto& m : comp.getMatches()) {
        emf::common::EObject* src = nullptr;
        emf::common::EObject* dst = nullptr;
        if (direction == MergeDirection::RIGHT_TO_LEFT) {
            src = m.getRight();
            dst = m.getLeft();
        } else {
            src = m.getLeft();
            dst = m.getRight();
        }
        if (src && dst) {
            srcToDst[src] = dst;
        }
        if (!srcRoot && src) srcRoot = src;
    }

    // 源端 / 目标端容器映射
    std::unordered_map<emf::common::EObject*, emf::common::EObject*> srcContainer;
    buildContainerMap(srcRoot, srcContainer);
    std::unordered_map<emf::common::EObject*, emf::common::EObject*> dstContainer;
    buildContainerMap(target, dstContainer);

    // G6/G12：计算 Diff 依赖并拓扑排序，被依赖者先合并。
    RequirementEngine reqEngine;
    reqEngine.computeRequirements(comp);
    auto ordered = topologicalSort(comp);

    // 按拓扑序逐 Diff 合并
    for (auto* d : ordered) {
        if (!d) continue;
        auto* m = d->getMatch();
        if (!m) continue;
        mergeDiff(d, *m, comp, srcContainer, dstContainer, srcToDst, direction);
    }
    return true;
}

void MergeEngine::mergeDiff(Diff* d, Match& m, Comparison& /*comp*/,
                            const std::unordered_map<emf::common::EObject*, emf::common::EObject*>& srcContainer,
                            const std::unordered_map<emf::common::EObject*, emf::common::EObject*>& dstContainer,
                            std::unordered_map<emf::common::EObject*, emf::common::EObject*>& srcToDst,
                            MergeDirection direction) {
    if (!d) return;
    // 在 RIGHT_TO_LEFT 方向：dst=left，src=right
    // 在 LEFT_TO_RIGHT 方向：dst=right，src=left
    auto* dst = (direction == MergeDirection::RIGHT_TO_LEFT) ? m.getLeft() : m.getRight();
    auto* src = (direction == MergeDirection::RIGHT_TO_LEFT) ? m.getRight() : m.getLeft();

    switch (d->getKind()) {
        case DiffKind::CHANGE: {
            // 把 src 的 feature 值复制到 dst
            if (!dst || !src) break;
            auto* cls = src->eClass();
            if (!cls) break;
            auto& name = d->getAttributeName();
            if (name.empty()) break;
            for (auto* sf : cls->getEAllStructuralFeatures()) {
                if (!sf || sf->getName() != name) continue;
                if (sf->isDerived() || sf->isTransient() || !sf->isChangeable()) continue;
                auto* ref = dynamic_cast<emf::ecore::EReference*>(sf);
                if (ref && ref->isMany()) break;  // 多值 reference 由 ADD/DELETE/MOVE 处理
                std::any srcVal = src->eGet(sf);
                // P0-6：reference 的 CHANGE，把 src 端 EObject 映射到对应 dst 端 EObject
                if (ref && srcVal.has_value()
                    && srcVal.type() == typeid(emf::common::EObject*)) {
                    auto* srcObj = std::any_cast<emf::common::EObject*>(srcVal);
                    if (srcObj) {
                        auto it = srcToDst.find(srcObj);
                        if (it != srcToDst.end() && it->second) {
                            // 旧 opposite 维护（解除原引用）
                            auto oldDstVal = dst->eGet(sf);
                            if (oldDstVal.has_value()
                                && oldDstVal.type() == typeid(emf::common::EObject*)) {
                                auto* oldObj = std::any_cast<emf::common::EObject*>(oldDstVal);
                                if (oldObj) maintainEOpposite(dst, ref, oldObj, false);
                            }
                            dst->eSet(sf, std::any(it->second));
                            // P0-7：维护 eOpposite
                            maintainEOpposite(dst, ref, it->second, true);
                            break;
                        }
                        // 无映射：可能是 containment 子对象，按原指针设置
                    }
                }
                // 普通 attribute 或无映射 reference：直接复制值
                dst->eSet(sf, srcVal);
                // P0-7：若 ref 有 eOpposite，维护反向（仅非 containment reference）
                if (ref && !ref->isContainment() && srcVal.has_value()
                    && srcVal.type() == typeid(emf::common::EObject*)) {
                    auto* v = std::any_cast<emf::common::EObject*>(srcVal);
                    if (v) maintainEOpposite(dst, ref, v, true);
                }
                break;
            }
            break;
        }
        case DiffKind::ADD: {
            // 源端新增了对象，需克隆（P0-5）后挂到目标端容器
            emf::common::EObject* added = (direction == MergeDirection::RIGHT_TO_LEFT)
                ? d->getRight() : d->getLeft();
            if (!added) break;
            // 源端容器
            auto scIt = srcContainer.find(added);
            if (scIt == srcContainer.end()) break;
            emf::common::EObject* srcCont = scIt->second;
            // 对应的目标端容器
            auto sdIt = srcToDst.find(srcCont);
            if (sdIt == srcToDst.end()) break;
            emf::common::EObject* dstCont = sdIt->second;
            // 找到源端容器中包含 added 的 reference 名
            std::string refName = findContainmentRefName(srcCont, added);
            if (refName.empty()) break;
            // P0-5：克隆源端对象，避免跨树共享
            emf::common::EObject* cloned = emf::ecore::util::EcoreUtil::copy(added);
            if (!cloned) break;
            // 在目标端容器同名 reference 中追加克隆
            emf::ecore::EReference* ref = nullptr;
            auto* dstList = findManyListByName(dstCont, refName, &ref);
            if (dstList) {
                dstList->add(cloned);
                // P0-7：维护 eOpposite（containment 的反向通常是 container 引用）
                if (ref) maintainEOpposite(dstCont, ref, cloned, true);
            }
            // G6/G12：把克隆注册到 srcToDst，供后续 CHANGE reference 映射引用。
            // 原 P0 注释指出 srcToDst 是 const 无法修改——本次重构改为可变映射，
            // 解决“ADD 的对象被后续 REFERENCE_CHANGE 引用却映射不到目标端克隆”的缺陷。
            srcToDst[added] = cloned;
            break;
        }
        case DiffKind::DELETE: {
            // 目标端对象被删除，从目标端容器移除
            emf::common::EObject* removed = (direction == MergeDirection::RIGHT_TO_LEFT)
                ? d->getLeft() : d->getRight();
            if (!removed) break;
            auto dcIt = dstContainer.find(removed);
            if (dcIt == dstContainer.end()) break;
            emf::common::EObject* dstCont = dcIt->second;
            auto loc = findInContainer(dstCont, removed);
            if (loc.list && loc.index >= 0) {
                loc.list->removeByIndex(loc.index);
                // P0-7：维护 eOpposite（移除 containment 的反向 container 引用）
                if (loc.ref) maintainEOpposite(dstCont, loc.ref, removed, false);
            }
            break;
        }
        case DiffKind::MOVE: {
            // 在 dst 的有序多值 reference 中调整位置：oldIndex → newIndex
            if (!dst) break;
            auto* cls = dst->eClass();
            if (!cls) break;
            auto& name = d->getAttributeName();
            if (name.empty()) break;
            for (auto* sf : cls->getEAllStructuralFeatures()) {
                if (!sf || sf->getName() != name) continue;
                auto* ref = dynamic_cast<emf::ecore::EReference*>(sf);
                if (!ref || !ref->isMany()) break;
                auto* list = getEObjectListRef(dst->eGet(ref));
                if (!list) break;
                int oldIdx = d->getOldIndex();
                int newIdx = d->getNewIndex();
                if (oldIdx < 0 || newIdx < 0) break;
                if (oldIdx >= (int)list->size() || newIdx >= (int)list->size()) break;
                // 用 EList::move(targetIndex, sourceIndex)（对齐 Java EList.move）
                list->move(newIdx, oldIdx);
                break;
            }
            break;
        }
    }
    // 合并完成后标记为 MERGED（对齐 Java Diff.setState(MERGED)）
    d->setState(DifferenceState::MERGED);
}

}  // namespace emf::compare
