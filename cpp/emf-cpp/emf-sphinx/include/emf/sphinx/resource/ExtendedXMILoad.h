// ExtendedXMILoad.h
// 对齐 Java org.eclipse.sphinx.emf.resource.ExtendedXMILoadImpl
// XMI 专属加载实现：继承 ExtendedXMLLoad，将 makeDefaultHandler() 改为
// 创建 ExtendedSAXXMIHandler（而非 ExtendedSAXXMLHandler）。
#pragma once

#include "emf/sphinx/resource/ExtendedXMLLoad.h"

namespace emf::sphinx::resource {

class ExtendedSAXXMIHandler;

// ExtendedXMILoad: 对齐 Java ExtendedXMILoadImpl。
// 是 XMILoadImpl 的 Sphinx 替代品：继承 ExtendedXMLLoadImpl 而非 XMLLoadImpl，
// 并创建 ExtendedSAXXMIHandler 而非 SAXXMIHandler。
class ExtendedXMILoad : public ExtendedXMLLoad {
public:
    ExtendedXMILoad() = default;
    explicit ExtendedXMILoad(ExtendedResource* extendedResource)
        : ExtendedXMLLoad(extendedResource) {}
    ~ExtendedXMILoad() override = default;

protected:
    // 创建 XMI 默认 handler（对齐 Java: ExtendedXMILoadImpl.makeDefaultHandler()）。
    // 返回 ExtendedSAXXMIHandler 而非 ExtendedSAXXMLHandler。
    ExtendedSAXXMLHandler* makeDefaultHandler() override;
};

}  // namespace emf::sphinx::resource
