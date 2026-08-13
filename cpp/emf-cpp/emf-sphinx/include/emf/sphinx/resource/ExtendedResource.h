// ExtendedResource.h
// 对齐 Java org.eclipse.sphinx.emf.resource.ExtendedResource
// Resource 增强接口（额外选项 + proxy URI 处理 + schema location）
#pragma once

#include "emf/common/Resource.h"
#include "emf/common/URI.h"
#include <any>
#include <map>
#include <string>
#include <vector>
#include <regex>

namespace emf::ecore {
class EClass;
class EStructuralFeature;
}

namespace emf::sphinx::scoping {
class IResourceScope;
}

namespace emf::sphinx::metamodel {
class IMetaModelDescriptor;
}

namespace emf::sphinx::resource {

class ExtendedResource : public emf::common::Resource {
public:
    // URI 分隔符常量
    static constexpr const char* URI_SCHEME_SEPARATOR = ":";
    static constexpr const char* URI_SEGMENT_SEPARATOR = "/";
    static constexpr const char* URI_QUERY_SEPARATOR = "?";
    static constexpr const char* URI_QUERY_FIELD_SEPARATOR = "&";
    static constexpr const char* URI_QUERY_KEY_VALUE_SEPARATOR = "=";
    static constexpr const char* URI_FRAGMENT_SEPARATOR = "#";

    // Options
    static constexpr const char* OPTION_RESOURCE_VERSION_DESCRIPTOR = "RESOURCE_VERSION_DESCRIPTOR";
    static constexpr const char* OPTION_USE_CONTEXT_AWARE_PROXY_URIS = "USE_CONTEXT_AWARE_PROXY_URIS";
    static constexpr const char* OPTION_TARGET_METAMODEL_DESCRIPTOR_ID = "TARGET_METAMODEL_DESCRIPTOR_ID";
    static constexpr const char* OPTION_SCHEMA_LOCATION_CATALOG = "SCHEMA_LOCATION_CATALOG";
    static constexpr const char* OPTION_ENABLE_SCHEMA_VALIDATION = "ENABLE_SCHEMA_VALIDATION";
    static constexpr const char* OPTION_PROBLEM_MARKER_FACTORY = "PROBLEM_MARKER_FACTORY";
    static constexpr const char* OPTION_MAX_PROBLEM_MARKER_COUNT = "MAX_PROBLEM_MARKER_COUNT";
    static constexpr const char* OPTION_UNLOAD_MEMORY_OPTIMIZED = "UNLOAD_MEMORY_OPTIMIZED";
    static constexpr const char* OPTION_PROGRESS_MONITOR = "PROGRESS_MONITOR";

    // URI Query 解析 regex
    static const std::regex URI_QUERY_FIELD_PATTERN;

    // 资源/对象 URI
    virtual emf::common::URI getURI(emf::common::EObject* obj, bool resolve) = 0;
    virtual emf::common::URI getURI(emf::common::EObject* oldOwner, emf::ecore::EStructuralFeature* oldFeat, emf::common::EObject* obj, bool resolve) = 0;

    // Proxy URI 工具
    virtual emf::common::URI createURI(const std::string& literal, emf::ecore::EClass* cls) = 0;
    virtual emf::common::URI getHREF(emf::common::EObject* obj) = 0;
    virtual std::string nomalizeURIFragment(const std::string& fragment) = 0;
    virtual emf::common::URI trimProxyContextInfo(const emf::common::URI& proxyUri) = 0;

    // 跨文档引用 - 加上上下文信息
    virtual void augmentToContextAwareProxy(emf::common::EObject* proxy) = 0;

    // Schema location
    virtual std::map<std::string, std::string> getSchemaLocationEntries(const std::map<std::string, std::any>& options) = 0;

    // 默认选项
    virtual std::map<std::string, std::any> getDefaultLoadOptions() const = 0;
    virtual std::map<std::string, std::any> getDefaultSaveOptions() const = 0;
    virtual std::map<std::string, std::any> getProblemHandlingOptions() const = 0;

    // Resource scope
    virtual emf::sphinx::scoping::IResourceScope* getScope() const = 0;
    virtual void setScope(emf::sphinx::scoping::IResourceScope* scope) = 0;

    // MetaModel descriptor
    virtual emf::sphinx::metamodel::IMetaModelDescriptor* getMetaModelDescriptor() const = 0;
    virtual void setMetaModelDescriptor(emf::sphinx::metamodel::IMetaModelDescriptor* mm) = 0;

    // 内存优化卸载
    virtual bool isFullyLoaded() const = 0;
    virtual void unloaded(emf::common::EObject* obj) = 0;
};

}  // namespace emf::sphinx::resource
