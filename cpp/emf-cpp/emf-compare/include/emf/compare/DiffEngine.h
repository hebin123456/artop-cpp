// EMF Compare: DiffEngine
// 对齐 org.eclipse.emf.compare.diff.DefaultDiffEngine (Java)
#pragma once

#include "Comparison.h"

#include <unordered_map>

namespace emf::compare {

class DiffEngine {
public:
    DiffEngine() = default;

    // 遍历 Comparison 中所有 match，比对左右两边所有 feature，输出 Diff
    void diff(Comparison& comp);

private:
    void diffMatch(Match& m, Comparison& comp);

    // left→right 映射（通过 Match 关联），在 diff() 入口一次性构建，
    // 供 diffSingleValueReferences / detectMoves 复用，避免 per-match 重建导致 O(n²)。
    std::unordered_map<emf::common::EObject*, emf::common::EObject*> leftToRight_;
};

}  // namespace emf::compare
