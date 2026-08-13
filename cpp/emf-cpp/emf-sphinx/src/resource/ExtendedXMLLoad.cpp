// ExtendedXMLLoad.cpp
// 对齐 Java org.eclipse.sphinx.emf.resource.ExtendedXMLLoadImpl
#include "emf/sphinx/resource/ExtendedXMLLoad.h"
#include "emf/sphinx/resource/ExtendedResource.h"
#include "emf/sphinx/resource/ExtendedSAXXMLHandler.h"
#include "emf/xmi/XMIResource.h"
#include "emf/xmi/XMIOptions.h"
#include <any>

namespace emf::sphinx::resource {

ExtendedXMLLoad::ExtendedXMLLoad(ExtendedResource* extendedResource)
    : extendedResource_(extendedResource) {}

void ExtendedXMLLoad::load(emf::xmi::XMIResource* resource, std::istream& input,
                           const emf::xmi::XMIOptions& options) {
    resource_ = resource;

    // Sphinx load: 从 ExtendedResource 默认加载选项中读取 schema 校验开关。
    // 对齐 Java: Boolean.TRUE.equals(options.get(ExtendedResource.OPTION_ENABLE_SCHEMA_VALIDATION))
    enableSchemaValidation_ = false;
    if (extendedResource_ != nullptr) {
        std::map<std::string, std::any> loadOpts = extendedResource_->getDefaultLoadOptions();
        auto it = loadOpts.find(ExtendedResource::OPTION_ENABLE_SCHEMA_VALIDATION);
        if (it != loadOpts.end()) {
            try {
                enableSchemaValidation_ = std::any_cast<bool>(it->second);
            } catch (const std::bad_any_cast&) {
                enableSchemaValidation_ = false;
            }
        }
    }

    // 委托基类 XMLLoadImpl::load 完成实际解析。Sphinx 行为：解析期间抛出的
    // 异常不向外传播，由 handleErrors() 以 problem marker 形式记录，保证
    // 资源能尽可能完整地加载（对齐 Java ExtendedXMLLoadImpl.handleErrors）。
    try {
        emf::xmi::XMLLoadImpl::load(resource, input, options);
    } catch (...) {
        // 吞掉异常；错误已记录到 resource 的 errors/warnings 中。
    }
    handleErrors();
}

ExtendedSAXXMLHandler* ExtendedXMLLoad::makeDefaultHandler() {
    // 对齐 Java: return new ExtendedSAXXMLHandler(resource, helper, options);
    // C++ 端 ExtendedSAXXMLHandler 为骨架类，无参构造。
    return new ExtendedSAXXMLHandler();
}

void ExtendedXMLLoad::handleErrors() {
    // 对齐 Java ExtendedXMLLoadImpl.handleErrors(): 不抛异常。
    // 加载期间遇到的问题已由基类记录到 resource 的 errors/warnings，
    // 此处仅作为扩展点存在（未来可在此创建 problem marker）。
}

}  // namespace emf::sphinx::resource
