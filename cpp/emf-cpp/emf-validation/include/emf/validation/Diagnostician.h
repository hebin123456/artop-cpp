// EMF Validation: Diagnostician
// 对齐 org.eclipse.emf.ecore.util.Diagnostician (Java)
//
// 职责：
//   1. 遍历 EObject 的 containment 树
//   2. 对每个对象，通过 EValidator.Registry 按 EPackage 查找已注册的 EValidator
//   3. 调用 EValidator.validate 执行约束校验
//   4. 累积 Diagnostic 返回
//
// 对齐 Java Diagnostician.INSTANCE.validate(EObject):
//   - validate(EObject) 创建 BasicDiagnostic 根
//   - validate(EObject, DiagnosticChain, Map) 遍历 containment 树
//   - validate(EClass, EObject, DiagnosticChain, Map) 按 EPackage 分派到 EValidator
#pragma once

#include "emf/validation/EValidator.h"
#include "emf/common/Diagnostic.h"
#include "emf/common/EObject.h"
#include "emf/ecore/util/EValidatorRegistryImpl.h"

#include <vector>

namespace emf::validation {

class Diagnostician {
public:
    // 对齐 Java Diagnostician.validate(EObject):
    // 遍历 root 及其 containment 树，对每个对象按 EPackage 查 Registry 分派校验。
    // mode 默认 BATCH（全量静态校验）。
    // 返回累积的 Diagnostic 列表（severity != OK 的）。
    static std::vector<emf::common::Diagnostic> validate(emf::common::EObject* root,
                                                          ConstraintMode mode = ConstraintMode::BATCH);

    // 单对象校验（不递归 containment 树）
    // 对齐 Java Diagnostician.validate(EClass, EObject, DiagnosticChain, Map)
    static std::vector<emf::common::Diagnostic> validateObject(emf::common::EObject* target,
                                                                ConstraintMode mode = ConstraintMode::BATCH);

    // ===== 带 DiagnosticChain + context 的重载（V7 修复）=====
    // 修复（原 gap：无此重载，调用方无法传 context、无法接收 Diagnostic 树）：
    // 对齐 Java Diagnostician.validate(EObject, DiagnosticChain, Map)：
    //   - 把校验结果直接追加到传入的 chain（而非只返回平铺 vector）
    //   - 接受 context Map（如 ROOT_OBJECT / STRICT_NAMED_ELEMENT_NAMES 等约束开关）
    //   - 返回是否全部通过（chain 中无 ERROR/CANCEL）
    // 单对象版（不递归 containment 树）
    static bool validate(emf::common::EObject* target,
                         emf::common::DiagnosticChain* chain,
                         emf::ecore::util::ValidationContext* context = nullptr,
                         ConstraintMode mode = ConstraintMode::BATCH);
    // 递归版（遍历 containment 树）
    static bool validateRecursive(emf::common::EObject* root,
                                  emf::common::DiagnosticChain* chain,
                                  emf::ecore::util::ValidationContext* context = nullptr,
                                  ConstraintMode mode = ConstraintMode::BATCH);

    // 注册内置 EValidator 到 EValidator::Registry（方案 A，对齐 Java EValidator.Registry.INSTANCE 联动）
    // 当前注册 EcorePackage → 桥接 EcoreValidator（ecore 元模型对象的元约束校验）
    // 幂等：重复调用安全。由 validate/validateObject 首次调用时 lazy 触发。
    static void initializeBuiltins();
};

}  // namespace emf::validation
