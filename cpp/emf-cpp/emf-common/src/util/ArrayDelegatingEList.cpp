// ArrayDelegatingEList.cpp
#include "emf/common/util/ArrayDelegatingEList.h"

namespace emf::common::util {

template class ArrayDelegatingEList<int>;
template class ArrayDelegatingEList<long>;
template class ArrayDelegatingEList<std::string>;
template class ArrayDelegatingEList<void*>;

}  // namespace emf::common::util
