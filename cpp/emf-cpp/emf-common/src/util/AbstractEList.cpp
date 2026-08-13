// AbstractEList.cpp
#include "emf/common/util/AbstractEList.h"

namespace emf::common::util {

template class EList<int>;
template class EList<long>;
template class EList<std::string>;
template class EList<void*>;

template class AbstractEList<int>;
template class AbstractEList<long>;
template class AbstractEList<std::string>;
template class AbstractEList<void*>;

}  // namespace emf::common::util
