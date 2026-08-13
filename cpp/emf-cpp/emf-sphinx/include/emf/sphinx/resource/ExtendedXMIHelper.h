// ExtendedXMIHelper.h
// 对齐 Java org.eclipse.sphinx.emf.resource.ExtendedXMIHelperImpl
// Sphinx 扩展的 XMI helper：继承 ExtendedXMLHelper，在上下文相关 HREF
// 解析基础上增加 XMI 专属工具（xmi:id 查询、XMI 命名空间常量）。
#pragma once

#include "emf/sphinx/resource/ExtendedXMLHelper.h"

namespace emf::common {
class EObject;
}  // namespace emf::common

namespace emf::sphinx::resource {

// ExtendedXMIHelper: 对齐 Java ExtendedXMIHelperImpl（继承 XMIHelperImpl）。
// C++ 端 XMIHelper 为自由函数集合，故此处继承 ExtendedXMLHelper 并补充
// XMI 专属工具方法。getHREF() 行为与 ExtendedXMLHelper 完全一致（对齐
// Java 中两者 getHREF 实现相同的事实）。
class ExtendedXMIHelper : public ExtendedXMLHelper {
public:
    ExtendedXMIHelper() = default;
    explicit ExtendedXMIHelper(ExtendedResource* extendedResource)
        : ExtendedXMLHelper(extendedResource) {}
    ~ExtendedXMIHelper() override = default;

    // XMI 命名空间常量（对齐 Java: XMIResource.XMI_NS / XMI_URI）。
    static constexpr const char* XMI_NS = "xmi";
    static constexpr const char* XMI_URI = "http://www.omg.org/XMI";

    // 返回 obj 在关联 XMIResource 中的 xmi:id；无 id 或无 XMIResource 时返回 ""。
    // 对齐 Java: XMIResource.getID(EObject)。
    std::string getXmiID(emf::common::EObject* obj) const;
};

}  // namespace emf::sphinx::resource
