// NotifyingList.cpp
#include "emf/common/util/NotifyingList.h"

namespace emf::common::util {

template class NotifyingList<int>;
template class NotifyingList<long>;
template class NotifyingList<std::string>;
template class NotifyingList<void*>;

}  // namespace emf::common::util
