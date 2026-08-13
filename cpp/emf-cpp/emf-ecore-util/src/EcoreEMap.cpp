// EcoreEMap.cpp
// 1:1 对齐 Java: org.eclipse.emf.ecore.util.EcoreEMap
//
// 说明：
//   Java 源 EcoreEMap 拥有较复杂的 Unsettable 子类、DelegateEObjectContainmentEList、
//   DelegateEObjectContainmentWithInverseEList、EStructuralFeature.Setting、Unsettable
//   等成员；C++ 头 EcoreEMap.h 中只声明了 BasicEMap 必需的钩子：
//       - Entry* newEntry(int hash, const K& key, const V& value) override
//       - K      getEStructuralFeature() const
//   这两个方法均已在头文件中 inline 定义。这里 .cpp 仅保证头被包含并提供
//   显式实例化符号（防止 BasicEMap<...> 链接时出现 undefined reference）。
#include "emf/ecore/util/EcoreEMap.h"

namespace emf::common::util {
// 显式实例化以确保 vtable / 符号在 .o 中 emit
template class BasicEMap<emf::ecore::EStructuralFeature*, std::any>;
}  // namespace emf::common::util
