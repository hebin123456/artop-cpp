// AbstractWrappingEList.h
// 对齐 Java org.eclipse.sphinx.emf.util.AbstractWrappingEList
#pragma once

#include "emf/common/EList.h"
#include <functional>

namespace emf::sphinx::util {

template <typename W, typename U>
class AbstractWrappingEList : public emf::common::EList<W*> {
public:
    using WrapFn = std::function<W*(U*)>;
    using UnwrapFn = std::function<U*(W*)>;
    AbstractWrappingEList(WrapFn wrap, UnwrapFn unwrap)
        : wrap_(std::move(wrap)), unwrap_(std::move(unwrap)) {}
protected:
    WrapFn wrap_;
    UnwrapFn unwrap_;
};

}  // namespace emf::sphinx::util
