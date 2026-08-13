// EMF Compare: ConflictDetector
// 对齐 org.eclipse.emf.compare.internal.ConflictDetector (Java)
//
// 3-way 冲突检测：left/right 相对 origin 都修改同一 feature，
// 改后值相同→PSEUDO，不同→REAL。
#pragma once

#include "Comparison.h"

namespace emf::compare {

class ConflictDetector {
public:
    ConflictDetector() = default;

    // 检测 comp 中的 3-way 冲突，写入 comp.getConflicts()。
    // 仅 3-way 比较（isThreeWay()==true）才会真正执行。
    void detectConflicts(Comparison& comp);
};

}  // namespace emf::compare
