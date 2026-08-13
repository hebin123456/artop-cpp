// ResourceScopeProviderRegistry.h
// 对齐 Java org.eclipse.sphinx.emf.scoping.ResourceScopeProviderRegistry
// 单例：注册所有 IResourceScopeProvider
#pragma once

#include "emf/sphinx/scoping/IResourceScopeProvider.h"
#include <vector>
#include <memory>

namespace emf::common {
class EObject;
class Resource;
}

namespace emf::sphinx::scoping {

class ResourceScopeProviderRegistry {
public:
    static ResourceScopeProviderRegistry& instance() {
        static ResourceScopeProviderRegistry inst;
        return inst;
    }

    void registerProvider(IResourceScopeProvider* provider);
    void unregisterProvider(IResourceScopeProvider* provider);
    std::unique_ptr<IResourceScope> createScope(emf::common::Resource* res);
    std::unique_ptr<IResourceScope> createScope(emf::common::EObject* obj);
    std::unique_ptr<IResourceScope> createScope(const emf::common::URI& uri);

    bool isNotInAnyScope(const emf::common::URI& uri);

private:
    ResourceScopeProviderRegistry() = default;
    std::vector<IResourceScopeProvider*> providers_;
};

}  // namespace emf::sphinx::scoping
