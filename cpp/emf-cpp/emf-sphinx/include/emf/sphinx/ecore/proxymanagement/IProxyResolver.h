// IProxyResolver.h
// 对齐 Java org.eclipse.sphinx.emf.ecore.proxymanagement.IProxyResolver
#pragma once

#include <string>

namespace emf::common {
class EObject;
class URI;
}

namespace emf::ecore {
class EClass;
}

namespace emf::sphinx::resource {
class ExtendedResourceSet;
}

namespace emf::sphinx::ecore::proxymanagement {

class IProxyResolver {
public:
    virtual ~IProxyResolver() = default;

    virtual bool canResolve(emf::ecore::EClass* type) = 0;
    virtual bool canResolve(emf::common::EObject* proxy) = 0;
    virtual emf::common::EObject* getEObject(emf::common::EObject* proxy, emf::common::EObject* context, bool loadOnDemand) = 0;
    virtual emf::common::EObject* getEObject(const emf::common::URI& uri, emf::ecore::EClass* targetClass,
                                              emf::sphinx::resource::ExtendedResourceSet* rs, void* context, bool loadOnDemand) = 0;
};

}  // namespace emf::sphinx::ecore::proxymanagement
