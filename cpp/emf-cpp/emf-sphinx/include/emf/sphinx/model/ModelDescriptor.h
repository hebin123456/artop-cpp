// ModelDescriptor.h
// 对齐 Java org.eclipse.sphinx.emf.model.ModelDescriptor
// IModelDescriptor 的标准实现
#pragma once

#include "emf/sphinx/model/IModelDescriptor.h"
#include "emf/sphinx/scoping/IResourceScope.h"
#include "emf/sphinx/metamodel/IMetaModelDescriptor.h"
#include <memory>

namespace emf::sphinx::model {

class ModelDescriptor : public IModelDescriptor {
public:
    ModelDescriptor() = default;
    ModelDescriptor(emf::sphinx::metamodel::IMetaModelDescriptor* mm,
                    emf::sphinx::metamodel::IMetaModelDescriptor* targetMm,
                    emf::sphinx::scoping::IResourceScope* scope);
    ~ModelDescriptor() override = default;

    emf::sphinx::metamodel::IMetaModelDescriptor* getMetaModelDescriptor() const override { return mm_; }
    emf::sphinx::metamodel::IMetaModelDescriptor* getTargetMetaModelDescriptor() const override { return targetMm_; }
    emf::sphinx::scoping::IResourceScope* getScope() const override { return scope_.get(); }
    emf::common::URI getRootURI() const override;
    std::vector<emf::common::URI> getReferencedRootURIs() const override;
    std::vector<emf::common::URI> getReferencingRootURIs() const override;
    emf::common::ResourceSet* getEditingDomain() const override;
    std::vector<emf::common::Resource*> getLoadedResources(bool includeReferencedScopes) const override;
    std::vector<emf::common::URI> getPersistedFiles(bool includeReferencedScopes) const override;

    bool belongsTo(const emf::common::URI& uri, bool includeReferencedScopes) const override;
    bool belongsTo(emf::common::Resource* res, bool includeReferencedScopes) const override;
    bool didBelongTo(const emf::common::URI& uri, bool includeReferencedScopes) const override;
    bool didBelongTo(emf::common::Resource* res, bool includeReferencedScopes) const override;
    bool isShared(const emf::common::URI& uri) const override;
    bool isShared(emf::common::Resource* res) const override;

    bool equals(const IModelDescriptor* other) const;
    int hashCode() const;

private:
    emf::sphinx::metamodel::IMetaModelDescriptor* mm_ = nullptr;
    emf::sphinx::metamodel::IMetaModelDescriptor* targetMm_ = nullptr;
    std::shared_ptr<emf::sphinx::scoping::IResourceScope> scope_;
};

}  // namespace emf::sphinx::model
