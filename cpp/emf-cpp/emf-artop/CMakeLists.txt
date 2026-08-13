# emf::artop —— AUTOSAR 工具平台 (ARTOP) 的 C++ 重写
# 对齐 Java: org.artop.aal.*
#
# 总设计：headless C++ 工具，只保留 Java ARTOP 中与运行时/序列化相关的核心。
# Java ARTOP 6 个 bundle 的对应关系：
#   org.artop.aal.common          → emf-artop-runtime（实质实现）
#   org.artop.aal.serialization   → 已合并进 emf-artop-runtime
#                                   （AutosarXMLLoader/Saver 吸收 AutosarXMLLoadImpl/SaveImpl，
#                                    EAnnotation 元数据机制取代 AutosarXMLRuleRegistry/TaggedValues）
#   org.artop.aal.gautosar.codegen + autosar40.codegen + autosar448.codegen
#                                  → emf-artop-codegen（骨架实现）
#   org.artop.aal.extender        → 不实现（Eclipse extension point 机制不适用；
#                                    SDG 厂商扩展数据按需在 runtime 补 ResourceHandler）
#   org.artop.aal.validation      → 不实现（核心已在 emf-validation；
#                                    autosar 约束直接 registerConstraint 到 emf-validation）
#   org.artop.aal.workspace       → 不实现（纯 Eclipse IDE 集成 IProject/IMarker/nature，
#                                    headless 工具按 URI 直接加载/保存）
#
# 依赖层级（自底向上）：
#   第 1 层 emf-*  (emf-common / emf-ecore / emf-xmi / emf-sphinx / ...)
#        ↓
#   第 2 层 emf-artop-runtime   ← emf_common / emf_ecore / emf_xmi / emf_sphinx
#        ↓ (codegen 输出)
#   emf-artop-codegen           ← emf-ecore-codegen + emf-artop-runtime（离线工具）
#
# 运行时机制（继承自 emf-xmi / emf）：
#   - 序列化/反序列化：AutosarXMLResource / AutosarResourceFactory
#   - 多文件编辑：ResourceSet / CrossReferenceAdapter
#   - 运行时通知：EMF Notification/NotificationChain
#   - 校验：emf-validation（直接调用，无需 artop 特化层）
#   - undo/redo：emf-edit (Command/EditingDomain)
#
# 逆向源：
#   /workspace/decompiler/autosar448/    autosar448.ecore + 已编译 .class
#   /workspace/decompiler/gautosar/      gautosar.ecore + 已编译 .class
#   /workspace/decompiler/common/        AutosarResourceFactoryImpl 等
#   /workspace/decompiler/serialization/ AutosarXMLHelper / XMLLoad / XMLSave（已吸收进 runtime）

add_subdirectory(emf-artop-runtime)

# codegen 是离线工具，最后加
add_subdirectory(emf-artop-codegen)
