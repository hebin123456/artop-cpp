// ResourceScopeValidationService.h
// 对齐 Java org.eclipse.sphinx.emf.internal.scoping.ResourceScopeValidationService
#pragma once

#include "emf/sphinx/scoping/IResourceScope.h"
#include <vector>

namespace emf::sphinx::internal::scoping {

class ResourceScopeValidationService {
public:
    static ResourceScopeValidationService& instance() {
        static ResourceScopeValidationService inst;
        return inst;
    }

    void validate(const std::vector<emf::sphinx::scoping::IResourceScope*>& scopes);
    bool isValid(emf::sphinx::scoping::IResourceScope* scope);

private:
    ResourceScopeValidationService() = default;
};

}  // namespace emf::sphinx::internal::scoping
