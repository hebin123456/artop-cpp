// ExtendedXMISave.h
// 对齐 Java org.eclipse.sphinx.emf.resource.ExtendedXMISaveImpl
// XMI 专属保存实现：继承 ExtendedXMLSave，增加 XMI 根元素（xmi:XMI）、
// xmi:id、xmi:version、xmlns:xmi、xmi:type 处理。
#pragma once

#include "emf/sphinx/resource/ExtendedXMLSave.h"
#include <string>

namespace emf::sphinx::resource {

// ExtendedXMISave: 对齐 Java ExtendedXMISaveImpl。
// 是 XMISaveImpl 的 Sphinx 替代品：继承 ExtendedXMLSaveImpl 而非 XMLSaveImpl，
// 处理 XMI 专属的根元素属性与命名空间声明。
class ExtendedXMISave : public ExtendedXMLSave {
public:
    ExtendedXMISave() = default;
    explicit ExtendedXMISave(ExtendedResource* extendedResource)
        : ExtendedXMLSave(extendedResource) {}
    ~ExtendedXMISave() override = default;

    // XMI 命名空间 / 属性常量（对齐 Java ExtendedXMISaveImpl 中的静态字段）。
    static constexpr const char* XMI_NS = "xmi";
    static constexpr const char* XMI_URI = "http://www.omg.org/XMI";
    static constexpr const char* XMI_ID = "id";
    static constexpr const char* XMI_TAG_NAME = "XMI";
    static constexpr const char* XMI_TAG_NS = "xmi:XMI";        // xmi:XMI
    static constexpr const char* XMI_ID_NS = "xmi:id";          // xmi:id
    static constexpr const char* XMI_TYPE_NS = "xmi:type";      // xmi:type
    static constexpr const char* XMI_VER_NS = "xmi:version";    // xmi:version
    static constexpr const char* XMI_XMLNS = "xmlns:xmi";       // xmlns:xmi

    // 是否使用 xmi:type 而非 xsi:type（对齐 Java: XMIResource.OPTION_USE_XMI_TYPE）。
    bool isUseXmiType() const { return useXmiType_; }
    void setUseXmiType(bool b) { useXmiType_ = b; }

    // 保存入口：确保 XMI 版本与命名空间声明后委托给 ExtendedXMLSave::save。
    void save(const emf::xmi::XMIResource* resource, std::ostream& output,
              const emf::xmi::XMIOptions& options) override;

protected:
    bool useXmiType_ = false;
};

}  // namespace emf::sphinx::resource
