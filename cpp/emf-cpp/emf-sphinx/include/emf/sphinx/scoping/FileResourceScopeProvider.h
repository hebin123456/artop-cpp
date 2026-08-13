// FileResourceScopeProvider.h
// 对齐 Java org.eclipse.sphinx.emf.scoping.FileResourceScopeProvider
#pragma once

#include "emf/sphinx/scoping/AbstractResourceScopeProvider.h"

namespace emf::sphinx::scoping {

class FileResourceScopeProvider : public AbstractResourceScopeProvider {
public:
    static FileResourceScopeProvider& instance() {
        static FileResourceScopeProvider inst;
        return inst;
    }

    std::unique_ptr<IResourceScope> createScope(emf::common::Resource* res) override;
    std::unique_ptr<IResourceScope> createScope(emf::common::EObject* obj) override;
    std::unique_ptr<IResourceScope> createScope(const emf::common::URI& uri) override;
};

}  // namespace emf::sphinx::scoping
