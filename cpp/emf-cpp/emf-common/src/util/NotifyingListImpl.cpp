// NotifyingListImpl.cpp
#include "emf/common/util/NotifyingListImpl.h"

namespace emf::common::util {

template class NotifyingListImpl<int>;
template class NotifyingListImpl<long>;
template class NotifyingListImpl<std::string>;
template class NotifyingListImpl<void*>;

}  // namespace emf::common::util
