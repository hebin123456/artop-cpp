// AbstractFilteringEList.h
// 对齐 Java org.eclipse.sphinx.emf.util.AbstractFilteringEList
// 支持按 predicate 过滤元素的基础 EList
#pragma once

#include "emf/common/EList.h"
#include <functional>
#include <any>

namespace emf::sphinx::util {

template <typename E>
class AbstractFilteringEList : public emf::common::EList<E> {
public:
    using Predicate = std::function<bool(const E&)>;

    explicit AbstractFilteringEList(Predicate filter) : filter_(std::move(filter)) {}

protected:
    Predicate filter_;
    bool accept(const E& e) const { return filter_(e); }
};

}  // namespace emf::sphinx::util
