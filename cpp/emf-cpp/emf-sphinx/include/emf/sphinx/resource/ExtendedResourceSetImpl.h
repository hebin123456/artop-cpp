// ExtendedResourceSetImpl.h
// 对齐 Java org.eclipse.sphinx.emf.resource.ExtendedResourceSetImpl
#pragma once

#include "emf/sphinx/resource/ExtendedResourceSet.h"

namespace emf::sphinx::resource {

class ExtendedResourceSetImpl : public ExtendedResourceSet {
public:
    ExtendedResourceSetImpl() = default;
    ~ExtendedResourceSetImpl() = default;

    emf::common::EObject* getEObjectInScope(const emf::common::URI& uri, bool loadOnDemand) override;
    emf::common::Resource* getResourceInScope(const emf::common::URI& uri, bool loadOnDemand) override;
};

}  // namespace emf::sphinx::resource
