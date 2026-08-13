// EInvocationDelegate.h
// 对齐 Java: org.eclipse.emf.ecore.EOperation.Internal.InvocationDelegate
// 行为体：把 EOperation 在某个 EObject（target）上执行，返回 std::any。
// 不抛 checked exception；C++ 用 std::any 表达"无返回值"——空 any 表示 void。
// 用 shared_ptr 持有（不同 EOperation 共享同一 delegate 对象）。
#pragma once

#include "emf/common/EObject.h"
#include <any>
#include <memory>
#include <vector>

namespace emf::ecore {

class EOperation;

class EInvocationDelegate {
public:
    virtual ~EInvocationDelegate() = default;
    // 在 target 上执行 operation 行为，arguments 是参数列表（与 operation 的
    // eParameters 一一对应；空 any 表示 null / 默认）。
    virtual std::any dynamicInvoke(emf::common::EObject* target,
                                  const std::vector<std::any>& arguments) = 0;
};

}  // namespace emf::ecore
