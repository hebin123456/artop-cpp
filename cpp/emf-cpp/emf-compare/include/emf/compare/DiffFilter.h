// EMF Compare: DiffFilter
// 对齐 org.eclipse.emf.compare.filter.IDifferenceFilter (Java)
//
// 过滤 Diff：predicate 返回 true 保留，false 丢弃。
// 默认全放行（predicate 为空）。
#pragma once

#include "Diff.h"
#include "Comparison.h"

#include <functional>

namespace emf::compare {

class DiffFilter {
public:
    using Predicate = std::function<bool(const Diff*)>;

    DiffFilter() = default;
    explicit DiffFilter(Predicate p) : pred_(std::move(p)) {}

    // 判断单个 diff 是否保留：predicate 为空时全放行
    bool retain(const Diff* d) const { return !pred_ || pred_(d); }

    // 应用过滤：从 Comparison.getDifferences() 移除 predicate 返回 false 的 diff，
    // 同时从各 Match.getDiffs() 移除。注意：不释放内存（Diff 所有权在 Comparison）。
    void apply(Comparison& comp) const;

    void setPredicate(Predicate p) { pred_ = std::move(p); }

private:
    Predicate pred_;
};

}  // namespace emf::compare
