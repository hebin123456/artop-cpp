// IModelDescriptor.h
// 对齐 Java org.eclipse.sphinx.emf.model.IModelDescriptor
// 描述一个 model（root + meta-model + scope）
#pragma once

#include "emf/sphinx/metamodel/IMetaModelDescriptor.h"
#include "emf/sphinx/scoping/IResourceScope.h"
#include "emf/common/Resource.h"
#include "emf/common/Resource.h"
#include "emf/common/URI.h"
#include <vector>
#include <memory>
#include <any>

namespace emf::sphinx::model {

class IModelDescriptor {
public:
    virtual ~IModelDescriptor() = default;

    virtual emf::sphinx::metamodel::IMetaModelDescriptor* getMetaModelDescriptor() const = 0;
    virtual emf::sphinx::metamodel::IMetaModelDescriptor* getTargetMetaModelDescriptor() const = 0;
    virtual emf::sphinx::scoping::IResourceScope* getScope() const = 0;
    virtual emf::common::URI getRootURI() const = 0;
    virtual std::vector<emf::common::URI> getReferencedRootURIs() const = 0;
    virtual std::vector<emf::common::URI> getReferencingRootURIs() const = 0;
    virtual emf::common::ResourceSet* getEditingDomain() const = 0;
    virtual std::vector<emf::common::Resource*> getLoadedResources(bool includeReferencedScopes) const = 0;
    virtual std::vector<emf::common::URI> getPersistedFiles(bool includeReferencedScopes) const = 0;

    // belongsTo 系列
    virtual bool belongsTo(const emf::common::URI& uri, bool includeReferencedScopes) const = 0;
    virtual bool belongsTo(emf::common::Resource* res, bool includeReferencedScopes) const = 0;

    // didBelongTo 系列
    virtual bool didBelongTo(const emf::common::URI& uri, bool includeReferencedScopes) const = 0;
    virtual bool didBelongTo(emf::common::Resource* res, bool includeReferencedScopes) const = 0;

    // isShared
    virtual bool isShared(const emf::common::URI& uri) const = 0;
    virtual bool isShared(emf::common::Resource* res) const = 0;
};

}  // namespace emf::sphinx::model
