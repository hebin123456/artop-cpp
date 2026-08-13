// EObjectEList.cpp
// 对齐 Java: org.eclipse.emf.ecore.util.EObjectEList
// 当前实现为模板全 inline 化（见 EObjectEList.h），本 .cpp 仅显式实例化
// 默认模板参数 E = emf::common::EObject*，以便静态链接器能正确生成符号。
#include "emf/ecore/util/EObjectEList.h"

namespace emf::ecore::util {

// 显式实例化默认参数版本（与 Java 默认 E=EObject 行为对齐）
template class EObjectEList<emf::common::EObject*>;

}  // namespace emf::ecore::util
