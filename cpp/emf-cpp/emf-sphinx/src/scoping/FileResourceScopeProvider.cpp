// FileResourceScopeProvider.cpp
// 对齐 Java org.eclipse.sphinx.emf.scoping.FileResourceScopeProvider
#include "emf/sphinx/scoping/FileResourceScopeProvider.h"
#include "emf/sphinx/scoping/FileResourceScope.h"
#include "emf/common/Resource.h"
#include "emf/common/EObject.h"

namespace emf::sphinx::scoping {

std::unique_ptr<IResourceScope> FileResourceScopeProvider::createScope(emf::common::Resource* res) {
    if (!res) return nullptr;
    return std::make_unique<FileResourceScope>(res->getURI());
}

std::unique_ptr<IResourceScope> FileResourceScopeProvider::createScope(emf::common::EObject* obj) {
    if (!obj) return nullptr;
    // 通过 eResource() 找 resource
    auto* res = obj->eResource();
    if (!res) return nullptr;
    return std::make_unique<FileResourceScope>(res->getURI());
}

std::unique_ptr<IResourceScope> FileResourceScopeProvider::createScope(const emf::common::URI& uri) {
    return std::make_unique<FileResourceScope>(uri);
}

}  // namespace emf::sphinx::scoping
