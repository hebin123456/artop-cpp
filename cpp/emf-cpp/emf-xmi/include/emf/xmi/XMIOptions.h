// emf::xmi —— XMI 序列化选项
// 对齐 Java: org.eclipse.emf.ecore.xmi.XMIResource.XMIResourceOptions
#pragma once

#include <string>

namespace emf::xmi {

// XMI 序列化选项集合
struct XMIOptions {
    // 是否在根元素上声明 xmi:xmi / xmi:version="2.0"
    bool declareXmi = true;
    // XMI 版本字符串
    std::string xmiVersion = "2.0";
    // 是否写出 xsi:type 当对象实际类型与属性声明类型不一致
    bool declareXsiType = true;
    // 是否用 xmi:id 标注每个根对象
    bool assignIDs = true;
    // 缩进字符串
    std::string indent = "  ";
    // XML 编码（空=跟随 resource.getEncoding()，对齐 Java EcoreResourceFactoryImpl 默认 "UTF-8"）
    // 非 ASCII 场景需显式设置；对齐 Java: OPTION_ENCODING 覆盖 resource encoding
    std::string encoding;
    // 是否写出 <?xml ?> declaration
    bool xmlDeclaration = true;
    // 行宽换行阈值（对齐 Java EcoreResourceFactoryImpl: OPTION_LINE_WIDTH=80）
    // 0 表示不换行；属性写入前检查 currentLineWidth > lineWidth
    int lineWidth = 80;
    // 对齐 Java XMLResource.OPTION_USE_ENCODED_ATTRIBUTE_STYLE：
    // true 时强制所有 feature 输出为 attribute 风格（ecore 文件默认），
    // 覆盖 ExtendedMetaData kind=element 注解；containment 引用仍需为子元素。
    bool useEncodedAttributeStyle = false;
    // 对齐 Java XMLResource.OPTION_RECORD_UNKNOWN_FEATURE：
    // true 时，loader 遇到无法映射到 EStructuralFeature 的 XML 属性/子元素，
    // 不丢弃，而是记录到 Resource 的 unknownFeatures_ 表（owner EObject* + UnknownElement 树）。
    // saver 在保存 owner 后原样输出这些未知元素，实现 round-trip 保持。
    // 对齐 Java ARTOP createFeatureFromSkippedElement 的"不丢内容"语义。
    bool recordUnknownFeature = false;
};

}  // namespace emf::xmi
