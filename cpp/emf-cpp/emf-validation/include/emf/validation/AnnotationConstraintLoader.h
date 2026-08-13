// EMF Validation: AnnotationConstraintLoader
// 对齐 Java: org.eclipse.emf.validation 模型驱动的约束加载
//
// 从 EClass 的 EAnnotation 读取约束表达式并注册到 EValidator：
//
// 1. OCL annotation（source = http://www.eclipse.org/emf/2002/Ecore/OCL）：
//    details 的 key 是约束名（invariant），value 是 OCL 表达式。
//    对齐 Java EObjectValidator 遍历 EAnnotation(OCL) 逐 invariant 求值。
//    C++ 端用 ConstraintParser 编译表达式（简化 OCL 子集，见 ConstraintParser 语法）。
//
// 2. Named-constraints annotation（source = http://www.eclipse.org/emf/2002/Ecore/Constraints）：
//    details 的 key 是 EClass 名，value 是空格分隔的 invariant 名列表。
//    每个 invariant 名对应一个同类 OCL annotation 中的同名表达式（按 source=OCL 查）。
//    对齐 Java EcoreValidator 读取 constraints annotation 决定执行哪些 invariant。
//
// 3. invariant 表达式执行语义：表达式以 target EObject 为 self 求值，返回 bool。
//    ConstraintParser 已支持 self.attr / attr != null / attr.size() > N 等语法。
//
// 修复（原 gap：OCL 与 Named-constraints annotation 零引用，模型内嵌约束不可达）。
#pragma once

#include "emf/validation/EValidator.h"
#include "emf/ecore/EcoreImpls.h"

#include <string>

namespace emf::validation {

class AnnotationConstraintLoader {
public:
    // Ecore OCL annotation source（对齐 Java EAnnotation.source）
    static constexpr const char* OCL_SOURCE = "http://www.eclipse.org/emf/2002/Ecore/OCL";
    // Ecore named-constraints annotation source
    static constexpr const char* CONSTRAINTS_SOURCE = "http://www.eclipse.org/emf/2002/Ecore/Constraints";

    // 从 EClass 的 EAnnotation 读取 OCL 表达式，编译为 Constraint 注册到 validator。
    // - 遍历 eClass->getEAnnotations()，找 source==OCL_SOURCE 的 annotation
    // - 对其 details 的每个 (name, expr) 对，用 ConstraintParser 编译并注册
    // - 解析失败的 expr 容错跳过（对齐 Java 容错行为）
    // - 重复调用幂等：同 name 的 constraint 替换旧约束
    // 返回注册的约束数量。
    static int loadOclConstraints(EValidator& validator, emf::ecore::EClass* eClass);

    // 从 EClass 的 constraints annotation 读取 invariant 名列表，
    // 在同 EClass 的 OCL annotation 中查对应表达式并注册。
    // 对齐 Java：constraints annotation 声明要执行的 invariant 名，
    // OCL annotation 提供表达式；二者配合才能激活 invariant。
    // 返回注册的约束数量。
    static int loadNamedConstraints(EValidator& validator, emf::ecore::EClass* eClass);

    // 便捷：同时加载 OCL + Named-constraints（先 OCL 全部，再 Named 过滤补充）。
    // 返回注册总数。
    static int loadAll(EValidator& validator, emf::ecore::EClass* eClass);
};

}  // namespace emf::validation
