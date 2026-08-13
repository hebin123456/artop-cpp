// EMF Compare: RequirementEngine 实现
// 对齐 org.eclipse.emf.compare.internal.RequirementEngine (Java)
//
// 计算 Diff 间的 requires 依赖，供 MergeEngine 拓扑序合并。
#include "emf/compare/RequirementEngine.h"
#include "emf/compare/Diff.h"
#include "emf/compare/Comparison.h"
#include "emf/common/EObject.h"

#include <unordered_map>
#include <vector>

namespace emf::compare {

namespace {

// 收集所有 ELEMENT_CHANGE 的 ADD/DELETE，建立 对象→Diff 索引：
//   addDiffByObj：right 端新增对象 → ADD Diff*
//   delDiffByObj：left 端删除对象 → DELETE Diff*
// （DiffEngine 中 ADD 的 added=d->getRight()、source=RIGHT；
//   DELETE 的 removed=d->getLeft()、source=LEFT。）
void indexElementDiffs(Comparison& comp,
                       std::unordered_map<emf::common::EObject*, Diff*>& addDiffByObj,
                       std::unordered_map<emf::common::EObject*, Diff*>& delDiffByObj) {
    for (auto* d : comp.getDifferences()) {
        if (!d || d->getType() != DiffType::ELEMENT_CHANGE) continue;
        if (d->getKind() == DiffKind::ADD) {
            if (auto* o = d->getRight()) addDiffByObj[o] = d;
        } else if (d->getKind() == DiffKind::DELETE) {
            if (auto* o = d->getLeft()) delDiffByObj[o] = d;
        }
    }
}

// 从 matches 构建 child → container 映射（左右各一份）。
// 每个 match 的对象遍历 eContents，将子对象映射到该 match 对象。
// 这覆盖 containment 子树（MatchEngine 递归 containment，故所有子对象均有 match）。
void buildContainerMaps(Comparison& comp,
                        std::unordered_map<emf::common::EObject*, emf::common::EObject*>& leftContainer,
                        std::unordered_map<emf::common::EObject*, emf::common::EObject*>& rightContainer) {
    for (auto& m : comp.getMatches()) {
        if (auto* l = m.getLeft()) {
            for (auto* child : l->eContents()) {
                if (child) leftContainer[child] = l;
            }
        }
        if (auto* r = m.getRight()) {
            for (auto* child : r->eContents()) {
                if (child) rightContainer[child] = r;
            }
        }
    }
}

// 从 std::any 取出 EObject*（仅当类型匹配），否则返回 nullptr。
emf::common::EObject* getEObjectFromAny(const std::any& v) {
    if (!v.has_value()) return nullptr;
    if (v.type() != typeid(emf::common::EObject*)) return nullptr;
    return std::any_cast<emf::common::EObject*>(v);
}

// 添加一条依赖（去重：同 (source,target) 不重复添加）。
void addRequirement(Comparison& comp, Diff* source, Diff* target,
                    std::vector<std::pair<Diff*, Diff*>>& seen) {
    if (!source || !target || source == target) return;
    for (auto& p : seen) {
        if (p.first == source && p.second == target) return;  // 已存在
    }
    seen.emplace_back(source, target);
    comp.getDependencies().emplace_back(source, target);
}

}  // namespace

void RequirementEngine::computeRequirements(Comparison& comp) {
    comp.getDependencies().clear();
    std::vector<std::pair<Diff*, Diff*>> seen;  // 去重缓冲

    std::unordered_map<emf::common::EObject*, Diff*> addDiffByObj;
    std::unordered_map<emf::common::EObject*, Diff*> delDiffByObj;
    indexElementDiffs(comp, addDiffByObj, delDiffByObj);

    std::unordered_map<emf::common::EObject*, emf::common::EObject*> leftContainer;
    std::unordered_map<emf::common::EObject*, emf::common::EObject*> rightContainer;
    buildContainerMaps(comp, leftContainer, rightContainer);

    for (auto* d : comp.getDifferences()) {
        if (!d) continue;
        switch (d->getKind()) {
            case DiffKind::ADD: {
                // 规则 1：ADD 子对象 依赖 ADD 父对象（containment 父子链）
                auto* added = d->getRight();  // right 端新增对象
                if (!added) break;
                auto it = rightContainer.find(added);
                if (it != rightContainer.end()) {
                    auto parentIt = addDiffByObj.find(it->second);
                    if (parentIt != addDiffByObj.end()) {
                        addRequirement(comp, d, parentIt->second, seen);
                    }
                }
                break;
            }
            case DiffKind::DELETE: {
                // 规则 3：DELETE 父对象 依赖 DELETE 子对象（先删子再删父）
                auto* removed = d->getLeft();  // left 端删除对象
                if (!removed) break;
                for (auto* child : removed->eContents()) {
                    if (!child) continue;
                    auto childIt = delDiffByObj.find(child);
                    if (childIt != delDiffByObj.end()) {
                        addRequirement(comp, d, childIt->second, seen);
                    }
                }
                break;
            }
            case DiffKind::CHANGE: {
                // 规则 2：REFERENCE_CHANGE 依赖 被引用对象的 ADD
                if (d->getType() != DiffType::REFERENCE_CHANGE) break;
                // newValue 是 right 端引用对象；若它被新增，需先 ADD
                if (auto* refObj = getEObjectFromAny(d->getNewValue())) {
                    auto it = addDiffByObj.find(refObj);
                    if (it != addDiffByObj.end()) {
                        addRequirement(comp, d, it->second, seen);
                    }
                }
                break;
            }
            case DiffKind::MOVE: {
                // 规则 4：MOVE 依赖 ADD（移动的元素若为新增，需先 ADD）
                if (auto* moved = d->getRight()) {
                    auto it = addDiffByObj.find(moved);
                    if (it != addDiffByObj.end()) {
                        addRequirement(comp, d, it->second, seen);
                    }
                }
                break;
            }
        }
    }
}

}  // namespace emf::compare
