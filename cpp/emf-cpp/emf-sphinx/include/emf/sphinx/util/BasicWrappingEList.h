// BasicWrappingEList.h
// 对齐 Java org.eclipse.sphinx.emf.util.BasicWrappingEList
#pragma once

#include "emf/sphinx/util/AbstractWrappingEList.h"

namespace emf::sphinx::util {

template <typename W, typename U>
class BasicWrappingEList : public AbstractWrappingEList<W, U> {
public:
    using Base = AbstractWrappingEList<W, U>;
    using Base::Base;
};

}  // namespace emf::sphinx::util
