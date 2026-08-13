// BasicEMap.cpp —— BasicEMap 模板显式实例化
#include "emf/common/util/BasicEMap.h"

#include <string>

namespace emf::common::util {

// 测试中用到的典型类型组合
template class BasicEMap<std::string, int>;
template class BasicEMap<int, std::string>;
template class BasicEMap<std::string, std::string>;

}  // namespace emf::common::util
