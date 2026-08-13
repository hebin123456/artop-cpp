// ResourceHandlerRegistry.h
// 对齐 Java org.eclipse.sphinx.emf.resource.ResourceHandlerRegistry
// 注册 content-type → resource 工厂的映射
#pragma once

#include <string>
#include <functional>
#include <unordered_map>

namespace emf::common {
class Resource;
class URI;
}

namespace emf::sphinx::resource {

class ResourceHandlerRegistry {
public:
    static ResourceHandlerRegistry& instance() {
        static ResourceHandlerRegistry inst;
        return inst;
    }

    using Factory = std::function<emf::common::Resource*(const emf::common::URI&)>;

    void registerHandler(const std::string& contentType, Factory factory);
    void unregisterHandler(const std::string& contentType);
    emf::common::Resource* create(const std::string& contentType, const emf::common::URI& uri) const;

private:
    ResourceHandlerRegistry() = default;
    std::unordered_map<std::string, Factory> handlers_;
};

}  // namespace emf::sphinx::resource
