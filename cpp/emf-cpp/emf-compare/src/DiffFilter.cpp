// EMF Compare: DiffFilter 实现
// 对齐 org.eclipse.emf.compare.filter.IDifferenceFilter (Java)
#include "emf/compare/DiffFilter.h"

#include <algorithm>

namespace emf::compare {

// 应用过滤：从 Comparison.getDifferences() 移除 predicate 返回 false 的 diff，
// 同时从各 Match.getDiffs() 移除。注意：不释放内存（Diff 所有权在 Comparison）。
void DiffFilter::apply(Comparison& comp) const {
    auto& diffs = comp.getDifferences();
    diffs.erase(std::remove_if(diffs.begin(), diffs.end(),
                               [this](Diff* d) { return !retain(d); }),
                diffs.end());

    for (auto& m : comp.getMatches()) {
        auto& mdiffs = m.getDiffs();
        mdiffs.erase(std::remove_if(mdiffs.begin(), mdiffs.end(),
                                    [this](Diff* d) { return !retain(d); }),
                     mdiffs.end());
    }
}

}  // namespace emf::compare
