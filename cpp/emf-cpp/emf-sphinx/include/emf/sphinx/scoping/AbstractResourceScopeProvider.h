// AbstractResourceScopeProvider.h
// 对齐 Java org.eclipse.sphinx.emf.scoping.AbstractResourceScopeProvider
#pragma once

#include "emf/sphinx/scoping/IResourceScopeProvider.h"

namespace emf::sphinx::scoping {

class AbstractResourceScopeProvider : public IResourceScopeProvider {
public:
    AbstractResourceScopeProvider() = default;
    ~AbstractResourceScopeProvider() override = default;
};

}  // namespace emf::sphinx::scoping
