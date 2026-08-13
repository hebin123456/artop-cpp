// EMF Validation: OCL 子集约束表达式解析器（递归下降）
// 对齐 Eclipse OCL / EMF Validation OCL 引擎（org.eclipse.ocl）求值语义。
//
// 支持的语法（OCL 子集）：
//   逻辑运算（优先级低→高）：implies, or, xor, and, not/!
//   比较运算：= / ==, <> / !=, >, <, >=, <=
//   集合迭代：source->forAll(v | boolExpr), source->exists(v | boolExpr)
//   集合操作：source->size(), source->isEmpty(), source->notEmpty()
//   路径导航：self, 迭代变量v, self.attr.subattr, attr（隐式 self）
//   对象操作：obj.attr.size()（集合大小或字符串长度）
//   字面量：null, '', 'str', true, false, 数字（含负数）
//   分组：(expr)
//   条件：if expr then expr else expr endif
//
//   OCL 语义要点：
//   - 空集合 forAll → true, exists → false
//   - implies: A implies B = (not A) or B（右结合）
//   - = / <> 为 OCL 标准等值运算（同时兼容 == / !=）
//   - 单值引用上 -> 迭代视为单元素集合
//
// 容错：解析失败返回恒 true（对齐 Java constraint 语法容错）。
#pragma once

#include "emf/validation/Constraint.h"
#include "emf/validation/EValidator.h"
#include "emf/common/EObject.h"
#include <string>
#include <functional>
#include <any>

namespace emf::validation {

// ExpressionEvaluator：求值函数
//   接受 target 和 value，返回是否通过
using ExpressionEvaluator = std::function<bool(emf::common::EObject* target, const std::any& value)>;

class ConstraintParser {
public:
    // 直接将表达式编译为求值函数；解析失败时返回的函数恒返回 true（不抛）
    static ExpressionEvaluator compile(const std::string& expr);

    // 解析并生成一个 Constraint（所有权归调用方，需 delete）
    static Constraint* parse(const std::string& source,
                             const std::string& name,
                             const std::string& expr,
                             Severity sev = Severity::WARNING);
};

// 便捷：直接注册一个表达式到 validator
Constraint* registerConstraintFromString(EValidator& validator,
                                         const std::string& source,
                                         const std::string& name,
                                         const std::string& expr,
                                         Severity sev = Severity::WARNING);

}  // namespace emf::validation
