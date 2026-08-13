// IProxyResolverService.h
// 对齐 Java org.eclipse.sphinx.emf.ecore.proxymanagement.IProxyResolverService
#pragma once

#include <any>
#include <vector>

namespace emf::sphinx::metamodel {
class IMetaModelDescriptor;
}

namespace emf::sphinx::ecore::proxymanagement {

class IProxyResolverService {
public:
    virtual ~IProxyResolverService() = default;

    virtual const std::vector<emf::sphinx::metamodel::IMetaModelDescriptor*>& getMetaModelDescriptors() const = 0;
    virtual bool isApplicableTo(emf::sphinx::metamodel::IMetaModelDescriptor* mm) const = 0;
};

}  // namespace emf::sphinx::ecore::proxymanagement
