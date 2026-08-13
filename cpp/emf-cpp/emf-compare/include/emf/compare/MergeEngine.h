// EMF Compare: MergeEngine
// 对齐 org.eclipse.emf.compare.merge.DefaultMerger (Java)
//
// G6/G12 增强：合并前先由 RequirementEngine 计算 Diff 间依赖，
// 再按拓扑序（被依赖者先合并）逐 Diff 应用，避免：
//   - REFERENCE_CHANGE 指向尚未 ADD 的对象（引用跨树共享/失效）
//   - 嵌套 ADD 子先于父合并（找不到容器）
//   - DELETE 父先于子合并（操作已孤立对象）
// 同时 srcToDst 改为可变映射：ADD 克隆新对象后注册到 srcToDst，
// 供后续 REFERENCE_CHANGE 把引用映射到目标端克隆（而非源端指针）。
#pragma once

#include "Comparison.h"

#include <unordered_map>

namespace emf::common {
class EObject;
}

namespace emf::compare {

// 合并方向（对齐 Java IMerger.merge(diff, leftToRight) 的 leftToRight 参数）
// RIGHT_TO_LEFT：把 RIGHT 的变更同步到 LEFT（target 通常是 left 根对象）
// LEFT_TO_RIGHT：把 LEFT 的变更同步到 RIGHT（target 通常是 right 根对象）
enum class MergeDirection {
    RIGHT_TO_LEFT,
    LEFT_TO_RIGHT
};

class MergeEngine {
public:
    MergeEngine() = default;

    // 默认方向 RIGHT_TO_LEFT：以 RIGHT 为准，把 RIGHT 的所有差异同步到 target
    // target 通常是 LEFT（或其副本），即 leftToRight=false 语义
    // 返回 true 表示成功，false 表示失败
    bool merge(Comparison& comp, emf::common::EObject* target);

    // 带方向的合并（对齐 Java IMerger.merge(diff, leftToRight)）
    // RIGHT_TO_LEFT：target=left 根对象，把 right 变更同步到 left
    // LEFT_TO_RIGHT：target=right 根对象，把 left 变更同步到 right
    bool merge(Comparison& comp, emf::common::EObject* target, MergeDirection direction);

private:
    // 按 Diff 粒度合并单个差异（G6/G12：拓扑序逐 diff 应用）。
    // srcToDst 可变：ADD 克隆新对象后注册 src→dst，供后续 REFERENCE_CHANGE 映射引用。
    void mergeDiff(Diff* d, Match& m, Comparison& comp,
                   const std::unordered_map<emf::common::EObject*, emf::common::EObject*>& srcContainer,
                   const std::unordered_map<emf::common::EObject*, emf::common::EObject*>& dstContainer,
                   std::unordered_map<emf::common::EObject*, emf::common::EObject*>& srcToDst,
                   MergeDirection direction);
};

}  // namespace emf::compare
