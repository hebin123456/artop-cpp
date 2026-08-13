// UniqueEList.cpp
#include "emf/common/util/UniqueEList.h"

namespace emf::common::util {

template class UniqueEList<int>;
template class UniqueEList<long>;
template class UniqueEList<std::string>;
template class UniqueEList<void*>;

template class FastCompareUniqueEList<int>;
template class FastCompareUniqueEList<long>;
template class FastCompareUniqueEList<std::string>;
template class FastCompareUniqueEList<void*>;

}  // namespace emf::common::util
