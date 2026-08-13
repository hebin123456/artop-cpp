// XMIResourceFactory.cpp
#include "emf/xmi/XMIResourceFactory.h"
#include "emf/common/URI.h"

#include <algorithm>
#include <cctype>

namespace emf::xmi {

std::unique_ptr<XMIResource> XMIResourceFactory::createResource(const emf::common::URI& uri) const {
    return std::unique_ptr<XMIResource>(new XMIResource(uri));
}

std::unordered_map<std::string, XMIResourceCreator>& XMIResourceFactory::registry() {
    static std::unordered_map<std::string, XMIResourceCreator> r;
    return r;
}

void XMIResourceFactory::registerFactory(const std::string& extension, XMIResourceCreator creator) {
    if (extension.empty()) return;
    std::string ext = extension;
    std::transform(ext.begin(), ext.end(), ext.begin(), [](unsigned char c) { return std::tolower(c); });
    registry()[ext] = std::move(creator);
}

void XMIResourceFactory::registerDefaults() {
    auto creator = [](const emf::common::URI& uri) -> std::unique_ptr<XMIResource> {
        return std::unique_ptr<XMIResource>(new XMIResource(uri));
    };
    registerFactory("xmi", creator);
    registerFactory("ecore", creator);
}

std::unique_ptr<XMIResource> XMIResourceFactory::createResourceFor(const emf::common::URI& uri) {
    // 提取后缀：URI 路径最后一段的扩展名
    const std::string& p = uri.path();
    auto dot = p.find_last_of('.');
    auto slash = p.find_last_of('/');
    if (dot == std::string::npos || (slash != std::string::npos && dot < slash)) {
        // 无后缀，fallback 到 XMIResource
        return std::unique_ptr<XMIResource>(new XMIResource(uri));
    }
    std::string ext = p.substr(dot + 1);
    std::transform(ext.begin(), ext.end(), ext.begin(), [](unsigned char c) { return std::tolower(c); });

    auto& r = registry();
    auto it = r.find(ext);
    if (it != r.end()) {
        return it->second(uri);
    }
    return std::unique_ptr<XMIResource>(new XMIResource(uri));
}

}  // namespace emf::xmi
