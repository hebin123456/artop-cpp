// AbstractUnwrappingEList.h
// 对齐 Java org.eclipse.sphinx.emf.util.AbstractUnwrappingEList
// 用于在 wrapper 和底层对象之间互相转换
#pragma once

#include "emf/common/EList.h"
#include <functional>

namespace emf::sphinx::util {

template <typename W, typename U>
class AbstractUnwrappingEList : public emf::common::EList<U*> {
public:
    using WrapFn = std::function<W*(U*)>;
    using UnwrapFn = std::function<U*(W*)>;
    AbstractUnwrappingEList(WrapFn wrap, UnwrapFn unwrap)
        : wrap_(std::move(wrap)), unwrap_(std::move(unwrap)) {}
protected:
    WrapFn wrap_;
    UnwrapFn unwrap_;
};

}  // namespace emf::sphinx::util
