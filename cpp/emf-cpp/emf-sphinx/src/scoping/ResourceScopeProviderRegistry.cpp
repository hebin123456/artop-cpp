// ResourceScopeProviderRegistry.cpp
// 对齐 Java org.eclipse.sphinx.emf.scoping.ResourceScopeProviderRegistry
#include "emf/sphinx/scoping/ResourceScopeProviderRegistry.h"
#include "emf/common/Resource.h"
#include "emf/common/EObject.h"

namespace emf::sphinx::scoping {

void ResourceScopeProviderRegistry::registerProvider(IResourceScopeProvider* provider) {
    if (!provider) return;
    for (auto* p : providers_) if (p == provider) return;
    providers_.push_back(provider);
}

void ResourceScopeProviderRegistry::unregisterProvider(IResourceScopeProvider* provider) {
    for (auto it = providers_.begin(); it != providers_.end(); ++it) {
        if (*it == provider) { providers_.erase(it); return; }
    }
}

std::unique_ptr<IResourceScope> ResourceScopeProviderRegistry::createScope(emf::common::Resource* res) {
    for (auto* p : providers_) {
        if (p) {
            auto scope = p->createScope(res);
            if (scope) return scope;
        }
    }
    return nullptr;
}

std::unique_ptr<IResourceScope> ResourceScopeProviderRegistry::createScope(emf::common::EObject* obj) {
    for (auto* p : providers_) {
        if (p) {
            auto scope = p->createScope(obj);
            if (scope) return scope;
        }
    }
    return nullptr;
}

std::unique_ptr<IResourceScope> ResourceScopeProviderRegistry::createScope(const emf::common::URI& uri) {
    for (auto* p : providers_) {
        if (p) {
            auto scope = p->createScope(uri);
            if (scope) return scope;
        }
    }
    return nullptr;
}

bool ResourceScopeProviderRegistry::isNotInAnyScope(const emf::common::URI& uri) {
    for (auto* p : providers_) {
        if (!p) continue;
        auto scope = p->createScope(uri);
        if (scope && scope->belongsTo(uri, false)) return false;
    }
    return true;
}

}  // namespace emf::sphinx::scoping
