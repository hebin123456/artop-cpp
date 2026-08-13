// ScopingResourceSetImpl.h
// 对齐 Java org.eclipse.sphinx.emf.resource.ScopingResourceSetImpl
#pragma once

#include "emf/sphinx/resource/ScopingResourceSet.h"
#include "emf/sphinx/resource/ExtendedResourceSetImpl.h"

namespace emf::sphinx::resource {

class ScopingResourceSetImpl : public ScopingResourceSet, public ExtendedResourceSetImpl {
public:
    ScopingResourceSetImpl() = default;
    ~ScopingResourceSetImpl() = default;

    std::vector<emf::common::Resource*> getResourcesInModel(emf::sphinx::model::IModelDescriptor* md, bool includeReferencedScopes) override;
};

}  // namespace emf::sphinx::resource
