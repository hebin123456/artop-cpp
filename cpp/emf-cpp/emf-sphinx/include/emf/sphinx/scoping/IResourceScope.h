// IResourceScope.h
// 对齐 Java org.eclipse.sphinx.emf.scoping.IResourceScope
// 一个 model 包含的资源范围（root + referenced roots）
#pragma once

#include "emf/common/URI.h"
#include <vector>
#include <any>

namespace emf::common {
class EObject;
class Resource;
}

namespace emf::sphinx::scoping {

class IResourceScope {
public:
    virtual ~IResourceScope() = default;

    // 根（文件路径或 URI）
    virtual emf::common::URI getRootURI() const = 0;

    // 引用的其他根
    virtual std::vector<emf::common::URI> getReferencedRootURIs() const = 0;
    virtual std::vector<emf::common::URI> getReferencingRootURIs() const = 0;

    // 检查给定 URI / 资源是否在 scope 内
    virtual bool belongsTo(const emf::common::URI& uri, bool includeReferencedScopes) const = 0;
    virtual bool belongsTo(emf::common::Resource* res, bool includeReferencedScopes) const = 0;

    // 持久化文件列表
    virtual std::vector<emf::common::URI> getPersistedFiles(bool includeReferencedScopes) const = 0;

    // 历史 belongsTo
    virtual bool didBelongTo(const emf::common::URI& uri, bool includeReferencedScopes) const = 0;
    virtual bool didBelongTo(emf::common::Resource* res, bool includeReferencedScopes) const = 0;

    // shared
    virtual bool isShared(const emf::common::URI& uri) const = 0;
    virtual bool isShared(emf::common::Resource* res) const = 0;
};

}  // namespace emf::sphinx::scoping
