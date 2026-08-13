// ScopingResourceSet.h
// 对齐 Java org.eclipse.sphinx.emf.resource.ScopingResourceSet
// 在 scope 内查找 EObject 的 ResourceSet
#pragma once

#include "emf/sphinx/resource/ExtendedResourceSet.h"
#include <vector>

namespace emf::common {
class EObject;
class Resource;
}

namespace emf::sphinx::model {
class IModelDescriptor;
}

namespace emf::sphinx::resource {

class ScopingResourceSet : public virtual ExtendedResourceSet {
public:
    virtual ~ScopingResourceSet() = default;

    // 取得给定 model 中所有 resources
    virtual std::vector<emf::common::Resource*> getResourcesInModel(emf::sphinx::model::IModelDescriptor* md, bool includeReferencedScopes) = 0;
};

}  // namespace emf::sphinx::resource
