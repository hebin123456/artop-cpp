// EMF Compare: EquivalenceEngine
// 对齐 org.eclipse.emf.compare.internal.EquivalenceEngine (Java)
//
// 计算 Equivalence：非 containment EReference 的目标对象若在 comparison 中有 Match，
// 则建立跨 containment 边界的等价关系。这样调用方可以知道：
// 一个 Match 的 ADD/DELETE 隐含另一个 Match 上的 reference change。
#pragma once

#include "Comparison.h"

namespace emf::compare {

class EquivalenceEngine {
public:
    EquivalenceEngine() = default;

    // 计算 comp 中所有跨 containment 边界的等价关系，写入 comp.getEquivalences()。
    void computeEquivalences(Comparison& comp);
};

}  // namespace emf::compare
