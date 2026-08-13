// BasicEList.cpp
#include "emf/common/util/BasicEList.h"

namespace emf::common::util {

template class BasicEList<int>;
template class BasicEList<long>;
template class BasicEList<std::string>;
template class BasicEList<void*>;

template class FastCompareBasicEList<int>;
template class FastCompareBasicEList<long>;
template class FastCompareBasicEList<std::string>;
template class FastCompareBasicEList<void*>;

}  // namespace emf::common::util
