// ExtendedXMLSave.cpp
// 对齐 Java org.eclipse.sphinx.emf.resource.ExtendedXMLSaveImpl
#include "emf/sphinx/resource/ExtendedXMLSave.h"
#include "emf/sphinx/resource/ExtendedResource.h"
#include "emf/xmi/XMIResource.h"
#include "emf/xmi/XMIOptions.h"
#include <any>

namespace emf::sphinx::resource {

ExtendedXMLSave::ExtendedXMLSave(ExtendedResource* extendedResource)
    : extendedResource_(extendedResource) {}

void ExtendedXMLSave::save(const emf::xmi::XMIResource* resource, std::ostream& output,
                           const emf::xmi::XMIOptions& options) {
    // Sphinx save: 计算 xsi:schemaLocation 并写到 resource 上，
    // 再委托基类 XMLSaveImpl::save 完成序列化。
    // 对齐 Java: ExtendedXMLSaveImpl.addNamespaceDeclarations() 中的 schema location 写出。
    if (resource != nullptr) {
        std::map<std::string, std::string> entries = collectSchemaLocationEntries(resource);
        std::string schemaLoc = buildSchemaLocationString(entries);
        if (!schemaLoc.empty()) {
            const_cast<emf::xmi::XMIResource*>(resource)->setXSISchemaLocation(schemaLoc);
        }
    }
    emf::xmi::XMLSaveImpl::save(resource, output, options);
}

std::map<std::string, std::string> ExtendedXMLSave::collectSchemaLocationEntries(
    const emf::xmi::XMIResource* /*resource*/) {
    std::map<std::string, std::string> entries;
    // 1. 优先使用 ExtendedResource 定义的 schema location entries。
    //    对齐 Java: extendedResource.getSchemaLocationEntries(options)。
    if (extendedResource_ != nullptr) {
        std::map<std::string, std::any> saveOpts;  // Sphinx 保存选项（此处为默认空）
        entries = extendedResource_->getSchemaLocationEntries(saveOpts);
    }
    // 2. 回退到 schema location catalog（OPTION_SCHEMA_LOCATION_CATALOG）。
    if (entries.empty()) {
        entries = schemaLocationCatalog_;
    }
    return entries;
}

std::string ExtendedXMLSave::buildSchemaLocationString(
    const std::map<std::string, std::string>& entries) {
    // 对齐 Java: xsi:schemaLocation 内容是 "namespace location" 对组成。
    // 无命名空间条目（key 为空）对应 xsi:noNamespaceSchemaLocation，单独属性，
    // 此处不并入 xsi:schemaLocation 字符串。
    std::string result;
    for (const auto& kv : entries) {
        if (kv.first.empty()) continue;  // 跳过 no-namespace 条目
        if (!result.empty()) result += ' ';
        result += kv.first;
        result += ' ';
        result += kv.second;
    }
    return result;
}

}  // namespace emf::sphinx::resource
