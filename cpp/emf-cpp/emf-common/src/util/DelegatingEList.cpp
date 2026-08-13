// DelegatingEList.cpp
#include "emf/common/util/DelegatingEList.h"

namespace emf::common::util {

template class DelegatingEList<int>;
template class DelegatingEList<long>;
template class DelegatingEList<std::string>;
template class DelegatingEList<void*>;

template class UnmodifiableDelegatingEList<int>;
template class UnmodifiableDelegatingEList<long>;
template class UnmodifiableDelegatingEList<std::string>;
template class UnmodifiableDelegatingEList<void*>;

}  // namespace emf::common::util
