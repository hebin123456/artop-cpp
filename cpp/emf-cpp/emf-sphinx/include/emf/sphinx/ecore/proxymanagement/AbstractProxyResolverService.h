// AbstractProxyResolverService.h
// 对齐 Java org.eclipse.sphinx.emf.ecore.proxymanagement.AbstractProxyResolverService
#pragma once

#include "emf/sphinx/ecore/proxymanagement/IProxyResolver.h"
#include "emf/sphinx/ecore/proxymanagement/IProxyResolverService.h"
#include "emf/sphinx/metamodel/services/AbstractMetaModelService.h"
#include <vector>

namespace emf::sphinx::ecore::proxymanagement {

class AbstractProxyResolverService
    : public emf::sphinx::metamodel::services::AbstractMetaModelService
    , public IProxyResolverService
    , public IProxyResolver {
public:
    AbstractProxyResolverService() = default;
    explicit AbstractProxyResolverService(const std::vector<emf::sphinx::metamodel::IMetaModelDescriptor*>& mmDescriptors);
    ~AbstractProxyResolverService() override = default;

    const std::vector<emf::sphinx::metamodel::IMetaModelDescriptor*>& getMetaModelDescriptors() const override { return mmDescriptors_; }
    bool isApplicableTo(emf::sphinx::metamodel::IMetaModelDescriptor* mm) const override;

    // IProxyResolver 默认实现（子类可覆盖）
    emf::common::EObject* getEObject(emf::common::EObject* proxy, emf::common::EObject* context, bool loadOnDemand) override;
    emf::common::EObject* getEObject(const emf::common::URI& uri, emf::ecore::EClass* targetClass,
                                      emf::sphinx::resource::ExtendedResourceSet* rs, void* context, bool loadOnDemand) override;

private:
    std::vector<emf::sphinx::metamodel::IMetaModelDescriptor*> mmDescriptors_;
};

}  // namespace emf::sphinx::ecore::proxymanagement
