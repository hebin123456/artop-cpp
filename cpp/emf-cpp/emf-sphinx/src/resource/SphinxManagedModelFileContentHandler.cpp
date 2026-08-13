// SphinxManagedModelFileContentHandler.cpp
// 对齐 Java org.eclipse.sphinx.emf.resource.SphinxManagedModelFileContentHandlerImpl
#include "emf/sphinx/resource/SphinxManagedModelFileContentHandler.h"

#include <algorithm>
#include <cctype>

namespace emf::sphinx::resource {

namespace {

std::string toLower(const std::string& s) {
    std::string r = s;
    std::transform(r.begin(), r.end(), r.begin(),
                   [](unsigned char c) { return static_cast<char>(std::tolower(c)); });
    return r;
}

}  // namespace

std::vector<std::string>& SphinxManagedModelFileContentHandler::managedExtensions() {
    // 对齐 Sphinx 托管的模型文件类型集合（默认扩展名）
    static std::vector<std::string> exts = {".xmi", ".xml", ".ecore", ".uml", ".model"};
    return exts;
}

void SphinxManagedModelFileContentHandler::registerManagedExtension(const std::string& ext) {
    if (ext.empty()) {
        return;
    }
    std::string e = toLower(ext);
    auto& exts = managedExtensions();
    if (std::find(exts.begin(), exts.end(), e) == exts.end()) {
        exts.push_back(e);
    }
}

bool SphinxManagedModelFileContentHandler::isManagedExtension(const std::string& ext) {
    if (ext.empty()) {
        return false;
    }
    std::string e = toLower(ext);
    const auto& exts = managedExtensions();
    return std::find(exts.begin(), exts.end(), e) != exts.end();
}

std::string SphinxManagedModelFileContentHandler::extractExtension(const emf::common::URI& uri) const {
    std::string p = uri.path();
    if (p.empty()) {
        p = uri.toString();
    }
    auto pos = p.find_last_of('.');
    if (pos == std::string::npos) {
        return "";
    }
    return toLower(p.substr(pos));
}

bool SphinxManagedModelFileContentHandler::canHandle(const emf::common::URI& uri) const {
    // 对齐 Java：URI 指向 Sphinx 托管的工作空间模型文件时返回 true。
    // C++ 端无 IFile/EcorePlatformUtil/ResourceScopeProviderRegistry，
    // 退化为按 scheme（platform 资源 URI）+ 已知扩展名判断。
    if (uri.isPlatform()) {
        return true;
    }
    return isManagedExtension(extractExtension(uri));
}

std::map<std::string, std::string> SphinxManagedModelFileContentHandler::contentDescription(
        const emf::common::URI& uri) const {
    // 对齐 Java：返回带 content type id 的内容描述（即使无确定类型也返回 VALID，
    // 避免被其他 handler 覆盖）。
    std::map<std::string, std::string> result;
    result[CONTENT_TYPE_PROPERTY] = extractExtension(uri);
    result["validity"] = VALIDITY_VALID;
    return result;
}

}  // namespace emf::sphinx::resource
