// ResourceScopeMarkerSyncRequest.h
// 对齐 Java org.eclipse.sphinx.emf.internal.scoping.ResourceScopeMarkerSyncRequest
#pragma once

#include "emf/sphinx/scoping/IResourceScope.h"

namespace emf::sphinx::internal::scoping {

class ResourceScopeMarkerSyncRequest {
public:
    ResourceScopeMarkerSyncRequest() = default;
    ResourceScopeMarkerSyncRequest(emf::sphinx::scoping::IResourceScope* scope, bool add)
        : scope_(scope), add_(add) {}

    emf::sphinx::scoping::IResourceScope* getScope() const { return scope_; }
    bool isAdd() const { return add_; }

private:
    emf::sphinx::scoping::IResourceScope* scope_ = nullptr;
    bool add_ = true;
};

}  // namespace emf::sphinx::internal::scoping
