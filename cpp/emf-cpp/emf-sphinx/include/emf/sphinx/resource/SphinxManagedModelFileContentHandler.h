// SphinxManagedModelFileContentHandler.h
// 对齐 Java org.eclipse.sphinx.emf.resource.SphinxManagedModelFileContentHandlerImpl
//
// Sphinx 托管模型文件的内容类型处理器。C++ 端去除 Eclipse 平台依赖
// （无 IProject/IFile/IWorkspace），退化为依据 URI scheme 与文件扩展名
// 判断是否为 Sphinx 托管模型文件，并给出内容类型描述。
#pragma once

#include "emf/common/URI.h"
#include <map>
#include <string>
#include <vector>

namespace emf::sphinx::resource {

class SphinxManagedModelFileContentHandler {
public:
    SphinxManagedModelFileContentHandler() = default;
    ~SphinxManagedModelFileContentHandler() = default;

    // 是否由 Sphinx 托管：platform 资源 URI 或已知模型文件扩展名
    // 对齐 Java canHandle(URI)
    bool canHandle(const emf::common::URI& uri) const;

    // 内容描述：返回 content type id 等属性
    // 对齐 Java contentDescription(URI, InputStream, options, context)
    std::map<std::string, std::string> contentDescription(const emf::common::URI& uri) const;

    // 注册/查询托管扩展名（对齐 Sphinx 管理的模型文件类型集合）
    static void registerManagedExtension(const std::string& ext);
    static bool isManagedExtension(const std::string& ext);

    // 内容类型属性键（对齐 Java ContentHandler.CONTENT_TYPE_PROPERTY）
    static constexpr const char* CONTENT_TYPE_PROPERTY = "contentTypeId";
    static constexpr const char* VALIDITY_VALID = "VALID";

private:
    std::string extractExtension(const emf::common::URI& uri) const;
    static std::vector<std::string>& managedExtensions();
};

}  // namespace emf::sphinx::resource
