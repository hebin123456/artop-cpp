// EMF Validation: AUTOSAR 核心业务约束
// 对齐 artop 内置 AUTOSAR 业务约束（Java 端 org.artop.aal.* 中的 common constraints）。
//
// 这些约束以反射方式作用于 EObject：检查 EClass 上是否存在相应 feature
// （shortName / uuid / category），因此既适用于真实 AUTOSAR 模型（Referrable /
// Identifiable 派生类），也适用于测试模型（带同名 feature 的动态 EClass）。
//
// 注册的约束（对齐 artop 常见约束语义）：
//   autosar.short_name_non_empty        Referrable.shortName 必须非空
//                                        （AUTOSAR 元模型 shortName lowerBound=1）
//   autosar.short_name_unique_in_parent 同一父对象下同类型兄弟的 shortName 必须唯一
//                                        （AUTOSAR 要求同类型兄弟 shortName 不重复）
//   autosar.uuid_non_empty              Identifiable.uuid 必须非空
//   autosar.uuid_globally_unique        Identifiable.uuid 全局唯一（模型级，整树去重）
//                                        （对齐 artop FixUuidConflictsAction）
//   autosar.category_required           有 category 且 lowerBound>=1 时 category 必须非空
//   autosar.no_unresolved_proxy         所有非 containment 引用不得为未解析 proxy
//                                        （跨 resource 引用必须可解析）
//
// 性能：per-object 约束均为 O(1) 或 O(兄弟数) 每对象；uuid 全局唯一性为 O(N) 单次遍历。
#pragma once

#include "emf/common/Diagnostic.h"
#include "emf/common/EObject.h"
#include <vector>

namespace emf::validation {

class EValidator;

// 注册核心 AUTOSAR 业务约束到 validator。
// 幂等：重复调用会按 id 替换旧约束。
// 注册 shortName/uuid 非空与 no_unresolved_proxy 为 BATCH+LIVE 双模式，
// 其余为 BATCH 模式（唯一性与 category 校验适合批处理）。
void registerAutosarConstraints(EValidator& validator);

// 注册 artop ECUC 专用约束（对齐 org.artop.aal.autosar40.constraints.ecuc 的 49 个约束）。
// 这些约束按 target EClass 名前缀过滤（clientContext enablement 等价），
// 只对 Ecuc* 类对象执行，避免对全树扫描，性能对齐 artop clientContext 过滤机制。
// 约束包括：EcucParameterValue/ContainerValue/ModuleConfigurationValues 等的
// multiplicity、bounds、completeness、consistency 校验。
void registerEcucConstraints(EValidator& validator);

// 模型级 UUID 全局唯一性校验（对齐 artop FixUuidConflictsAction.getUuidConflicts）。
// 单次 DFS 遍历 root 的 containment 树，收集所有对象的 uuid feature 值，
// 报告空 uuid 和重复 uuid（首个重复不报告，后续重复报告，与 artop 语义一致）。
// 返回 ERROR 级 Diagnostic 列表，source = "AutosarUuidGloballyUnique"。
// 性能：O(N) 单次遍历 + O(N) hash map 操作。
std::vector<emf::common::Diagnostic> validateUuidUniqueness(emf::common::EObject* root);

}  // namespace emf::validation
