// EMF Compare: RequirementEngine
// 对齐 org.eclipse.emf.compare.internal.RequirementEngine (Java)
//
// 计算 Diff 间的 requires 依赖关系，填充 Comparison.getDependencies()。
// Dependency 语义：source 依赖 target，即 target 必须在 source 之前合并。
//
// 依赖规则（对齐 Java RequirementEngine.computeRequirements）：
//   1. ADD 子对象 依赖 ADD 父对象（containment 父子链）：
//      新增对象需先有其容器存在才能挂载，故子 ADD 依赖父 ADD。
//   2. REFERENCE_CHANGE 依赖 被引用对象的 ADD：
//      引用指向新增对象时，该对象需先 ADD 才能设置引用。
//   3. DELETE 父对象 依赖 DELETE 子对象：
//      先删子（清理子上的引用/diff）再删父，避免操作已孤立对象。
//   4. MOVE 依赖 ADD：
//      移动的元素若为新增，需先 ADD 到目标端才能调整位置。
//
// 这些依赖供 MergeEngine 做拓扑序合并（G6/G12）。
#pragma once

#include "Comparison.h"

namespace emf::compare {

class RequirementEngine {
public:
    // 计算 comp 中所有 Diff 的依赖关系，写入 comp.getDependencies()。
    // 幂等：重复调用会先清空 comp.getDependencies() 再重算。
    void computeRequirements(Comparison& comp);
};

}  // namespace emf::compare
