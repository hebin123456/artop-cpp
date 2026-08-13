// AbstractProxyResolverService.cpp
#include "emf/sphinx/ecore/proxymanagement/AbstractProxyResolverService.h"
#include "emf/sphinx/metamodel/IMetaModelDescriptor.h"
#include "emf/common/EObject.h"
#include "emf/common/Resource.h"
#include "emf/common/Resource.h"
#include "emf/common/URI.h"
#include "emf/sphinx/resource/ExtendedResourceSet.h"

namespace emf::sphinx::ecore::proxymanagement {

AbstractProxyResolverService::AbstractProxyResolverService(
    const std::vector<emf::sphinx::metamodel::IMetaModelDescriptor*>& mmDescriptors)
    : mmDescriptors_(mmDescriptors) {}

bool AbstractProxyResolverService::isApplicableTo(emf::sphinx::metamodel::IMetaModelDescriptor* mm) const {
    if (!mm) return false;
    for (auto* d : mmDescriptors_) {
        if (d == mm) return true;
    }
    return false;
}

emf::common::EObject* AbstractProxyResolverService::getEObject(emf::common::EObject* /*proxy*/,
                                                                emf::common::EObject* /*context*/, bool /*loadOnDemand*/) {
    return nullptr;
}

emf::common::EObject* AbstractProxyResolverService::getEObject(const emf::common::URI& /*uri*/, emf::ecore::EClass* /*cls*/,
                                                                emf::sphinx::resource::ExtendedResourceSet* /*rs*/, void* /*ctx*/,
                                                                bool /*loadOnDemand*/) {
    return nullptr;
}

}  // namespace emf::sphinx::ecore::proxymanagement
