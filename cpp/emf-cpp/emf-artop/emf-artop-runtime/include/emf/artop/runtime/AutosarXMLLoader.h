// emf::artop::runtime —— AutosarXMLLoader
// 对齐 Java: org.artop.aal.common.resource.impl.AutosarXMLLoadImpl
//            + AutosarSAXXMLHandler（反序列化核心逻辑）
//
// AUTOSAR arxml 格式反序列化器（R4.0）：
//   - 识别根元素 <AUTOSAR xmlns="http://autosar.org/schema/r4.0">
//   - 不要求 xmi:id / xmi:version，用 shortName path 定位对象
//   - 元素名通过 EClass 反射匹配 EStructuralFeature
//     （先按 feature 名直接匹配，再用 EAnnotation 的 xml.name 兜底）
//   - containment 引用直接展开为子元素并递归构建
//   - EAttribute：isXmlAttribute=true 从 XML 属性读取，否则从子元素文本读取
//   - 非 containment 引用（REF/TREF/IREF）：DEST 属性 → EClass，文本 → short name path
//     先创建代理对象（设 proxyURI），加载完成后用 path 索引解析
//   - InstanceRef 反序列化：IREF 是 containment 对象（如 ComponentInSystemInstanceRef/
//     ECUC-INSTANCE-REFERENCE-VALUE），其内部的 CONTEXT-*-REF / TARGET-REF 是普通非 containment
//     引用，由 handleReferenceElement 复用同一代理机制处理（无需特殊代码路径）
//   - createFeatureFromSkippedElement：0016(wrapper)/0012(role+type) 引用的模型驱动匹配
//   - short name path 索引：加载时为每个 GReferrable 建立 path → EObject* 映射
//   - 跨文档 demand-load：本资源 pathIndex 未命中时回退到全局 AutosarLibraryIndex
//     （由预加载的 library resource 通过 AutosarResource::indexLibrary() 注册）
//
// 简化项（后续任务实现）：
//   - 延迟引用机制（deferredReferences）：当前用加载后一次性解析替代
//   - fLastUnserializedDescendants 容错：当前简化
#pragma once

#include "emf/xmi/XMLLoad.h"
#include "emf/xmi/XMIResource.h"
#include "emf/xmi/XMIOptions.h"

namespace emf::artop::runtime {

// AUTOSAR arxml 反序列化器（对齐 Java AutosarXMLLoadImpl + AutosarSAXXMLHandler）
class AutosarXMLLoader : public emf::xmi::XMLLoad {
public:
    AutosarXMLLoader() = default;
    ~AutosarXMLLoader() override = default;

    // 解析 arxml 流到 resource
    // 流程：pugixml 解析 → 识别 <AUTOSAR> 根 → 递归构建 EObject 树
    //       → 建立 short name path 索引 → 解析 REF 代理引用
    void load(emf::xmi::XMIResource* resource, std::istream& input,
              const emf::xmi::XMIOptions& options) override;
};

}  // namespace emf::artop::runtime
