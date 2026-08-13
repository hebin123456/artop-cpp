// BasicUnwrappingEList.h
// 对齐 Java org.eclipse.sphinx.emf.util.BasicUnwrappingEList
#pragma once

#include "emf/sphinx/util/AbstractUnwrappingEList.h"

namespace emf::sphinx::util {

template <typename W, typename U>
class BasicUnwrappingEList : public AbstractUnwrappingEList<W, U> {
public:
    using Base = AbstractUnwrappingEList<W, U>;
    using Base::Base;
};

}  // namespace emf::sphinx::util
