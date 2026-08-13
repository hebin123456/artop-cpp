// emf::artop::runtime —— AutosarXMLSaver
// 对齐 Java: org.artop.aal.common.resource.impl.AutosarXMLSaveImpl
//
// AUTOSAR arxml 格式序列化器（R4.0）：
//   - 根元素 <AUTOSAR xmlns="http://autosar.org/schema/r4.0"
//       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
//       xsi:schemaLocation="http://autosar.org/schema/r4.0 AUTOSAR_00048.xsd">
//   - 不写 xmi:id / xmi:version / xsi:type
//   - 元素命名使用 EAnnotation 的 xml.name / xml.namePlural（经 EAnnotationReader 读取）
//   - feature 按 internal-xml-sequenceOffset 排序输出
//   - APRXML 规则 0012/0015/0016/默认 决定 containment 引用的包装方式
//   - 非 containment 引用写成 <FEATURE DEST="TARGET">short-name-path</FEATURE>
//   - short name path：沿 eContainer 链收集 shortName，用 "/" 连接
//   - 使用 pugixml 构建 XML 文档（避免手动转义）
#pragma once

#include "emf/xmi/XMLLoad.h"
#include "emf/xmi/XMIResource.h"
#include "emf/xmi/XMIOptions.h"

namespace emf::artop::runtime {

// AutosarXMLSaver：arxml 格式序列化器
// 对齐 Java AutosarXMLSaveImpl —— 继承 emf::xmi::XMLSave 抽象接口
class AutosarXMLSaver : public emf::xmi::XMLSave {
public:
    AutosarXMLSaver() = default;
    ~AutosarXMLSaver() override = default;

    // XMLSave 接口：把 resource 序列化到 output 流
    void save(const emf::xmi::XMIResource* resource, std::ostream& output,
              const emf::xmi::XMIOptions& options) override;
};

}  // namespace emf::artop::runtime
