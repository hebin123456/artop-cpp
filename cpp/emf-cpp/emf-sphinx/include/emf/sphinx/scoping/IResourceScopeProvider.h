// IResourceScopeProvider.h
// 对齐 Java org.eclipse.sphinx.emf.scoping.IResourceScopeProvider
#pragma once

#include "emf/sphinx/scoping/IResourceScope.h"
#include <memory>

namespace emf::common {
class EObject;
class Resource;
}

namespace emf::sphinx::scoping {

class IResourceScopeProvider {
public:
    virtual ~IResourceScopeProvider() = default;

    // 给定一个 resource / eobject / URI，创建对应 scope
    virtual std::unique_ptr<IResourceScope> createScope(emf::common::Resource* res) = 0;
    virtual std::unique_ptr<IResourceScope> createScope(emf::common::EObject* obj) = 0;
    virtual std::unique_ptr<IResourceScope> createScope(const emf::common::URI& uri) = 0;
};

}  // namespace emf::sphinx::scoping
