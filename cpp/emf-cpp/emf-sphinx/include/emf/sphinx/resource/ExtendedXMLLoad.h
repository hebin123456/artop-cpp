// ExtendedXMLLoad.h
// 对齐 Java org.eclipse.sphinx.emf.resource.ExtendedXMLLoadImpl
// Sphinx 扩展的 XML 加载实现：在 emf::xmi::XMLLoadImpl 基础上增加
//   - OPTION_ENABLE_SCHEMA_VALIDATION 控制 schema 校验
//   - 加载过程中的 problem 收集（不抛异常，让 resource 尽量加载完整）
//   - 通过 makeDefaultHandler() 创建 ExtendedSAXXMLHandler
#pragma once

#include "emf/xmi/XMLLoad.h"  // XMLLoadImpl 基类

namespace emf::sphinx::resource {

class ExtendedResource;
class ExtendedSAXXMLHandler;

// ExtendedXMLLoad: 对齐 Java ExtendedXMLLoadImpl（继承 XMLLoadImpl）。
// Sphinx 的关键行为：handleErrors() 不抛异常 —— 加载期间遇到的问题以
// problem marker 形式记录，保证资源能尽可能完整地加载。
class ExtendedXMLLoad : public emf::xmi::XMLLoadImpl {
public:
    ExtendedXMLLoad() = default;
    explicit ExtendedXMLLoad(ExtendedResource* extendedResource);
    ~ExtendedXMLLoad() override = default;

    // 加载入口：设置 Sphinx 上下文后委托给基类 XMLLoadImpl::load，
    // 捕获加载异常并通过 handleErrors() 处理（不向外抛出）。
    void load(emf::xmi::XMIResource* resource, std::istream& input,
              const emf::xmi::XMIOptions& options) override;

    // Schema 校验开关（对齐 Java: OPTION_ENABLE_SCHEMA_VALIDATION）。
    bool isSchemaValidationEnabled() const { return enableSchemaValidation_; }
    void setEnableSchemaValidation(bool b) { enableSchemaValidation_ = b; }

    void setExtendedResource(ExtendedResource* r) { extendedResource_ = r; }
    ExtendedResource* getExtendedResource() const { return extendedResource_; }

protected:
    // 创建 SAX 默认 handler（对齐 Java: ExtendedXMLLoadImpl.makeDefaultHandler()）。
    // 返回 ExtendedSAXXMLHandler 而非默认 SAXXMLHandler。
    virtual ExtendedSAXXMLHandler* makeDefaultHandler();

    // 处理加载期间收集的错误。不抛异常 —— 问题以 marker 形式记录，
    // 资源仍能被加载（对齐 Java: ExtendedXMLLoadImpl.handleErrors()）。
    virtual void handleErrors();

    bool enableSchemaValidation_ = false;
    ExtendedResource* extendedResource_ = nullptr;
    emf::xmi::XMIResource* resource_ = nullptr;
};

}  // namespace emf::sphinx::resource
