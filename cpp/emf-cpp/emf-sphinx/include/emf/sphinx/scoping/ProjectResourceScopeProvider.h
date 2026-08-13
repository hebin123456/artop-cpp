// ProjectResourceScopeProvider.h
// 对齐 Java org.eclipse.sphinx.emf.scoping.ProjectResourceScopeProvider
#pragma once

#include "emf/sphinx/scoping/AbstractResourceScopeProvider.h"

namespace emf::sphinx::scoping {

class ProjectResourceScopeProvider : public AbstractResourceScopeProvider {
public:
    static ProjectResourceScopeProvider& instance() {
        static ProjectResourceScopeProvider inst;
        return inst;
    }

    std::unique_ptr<IResourceScope> createScope(emf::common::Resource* res) override;
    std::unique_ptr<IResourceScope> createScope(emf::common::EObject* obj) override;
    std::unique_ptr<IResourceScope> createScope(const emf::common::URI& uri) override;
};

}  // namespace emf::sphinx::scoping
