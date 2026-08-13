// ExtendedXMISave.cpp
// 对齐 Java org.eclipse.sphinx.emf.resource.ExtendedXMISaveImpl
#include "emf/sphinx/resource/ExtendedXMISave.h"
#include "emf/xmi/XMIResource.h"
#include "emf/xmi/XMIOptions.h"

namespace emf::sphinx::resource {

void ExtendedXMISave::save(const emf::xmi::XMIResource* resource, std::ostream& output,
                           const emf::xmi::XMIOptions& options) {
    // XMI save: 确保根元素上的 xmi:version 声明。
    // 对齐 Java: ExtendedXMISaveImpl.addNamespaceDeclarations() 写出 xmi:version / xmlns:xmi。
    if (resource != nullptr) {
        const std::string version =
            options.xmiVersion.empty() ? std::string("2.0") : options.xmiVersion;
        const_cast<emf::xmi::XMIResource*>(resource)->setXmiVersion(version);
    }
    // 委托给 ExtendedXMLSave（其再委托 XMLSaveImpl）完成序列化，
    // Sphinx 的 schema location 处理由 ExtendedXMLSave 负责。
    ExtendedXMLSave::save(resource, output, options);
}

}  // namespace emf::sphinx::resource
