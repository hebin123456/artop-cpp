// EObjectContainmentEList.cpp
// 对齐 Java: org.eclipse.emf.ecore.util.EObjectContainmentEList
#include "emf/ecore/util/EObjectContainmentEList.h"

namespace emf::ecore::util {

// 显式实例化默认参数版本（与 Java 默认 E=EObject 行为对齐）。
// 嵌套类 Unsettable/Resolving 会在外层实例化时被一并实例化。
template class EObjectContainmentEList<emf::common::EObject*>;

}  // namespace emf::ecore::util
