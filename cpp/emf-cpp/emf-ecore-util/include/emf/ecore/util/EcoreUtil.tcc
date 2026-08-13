// EcoreUtil 模板实现
// 对齐 Java EcoreUtil.setEList（deprecate）方法
// 注：Java 端 setEList(EList, Collection) 与 setEList(EList, List) 是两个重载。
// C++ 端 std::vector<T> 同时表达 Collection 和 List，所以只保留一个实现。
#pragma once

#include "EcoreUtil.h"
#include <algorithm>

namespace emf::ecore::util {

template <typename T>
void EcoreUtil::setEList(emf::common::EList<T>* eList, const std::vector<T>& prototypeCollection) {
    if (!eList) return;
    // 简化版：直接把 prototype 复制进去
    eList->clear();
    for (const auto& v : prototypeCollection) eList->add(v);
}

}  // namespace emf::ecore::util
