// IResourceScopeMarker.h
// 对齐 Java org.eclipse.sphinx.emf.internal.scoping.IResourceScopeMarker
#pragma once

#include "emf/sphinx/scoping/IResourceScope.h"

namespace emf::sphinx::internal::scoping {

class IResourceScopeMarker {
public:
    virtual ~IResourceScopeMarker() = default;
    virtual void install(emf::sphinx::scoping::IResourceScope* scope) = 0;
    virtual void uninstall(emf::sphinx::scoping::IResourceScope* scope) = 0;
};

}  // namespace emf::sphinx::internal::scoping
