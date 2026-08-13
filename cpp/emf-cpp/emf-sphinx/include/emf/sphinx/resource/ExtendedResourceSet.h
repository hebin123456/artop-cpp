// ExtendedResourceSet.h
// 对齐 Java org.eclipse.sphinx.emf.resource.ExtendedResourceSet
// ResourceSet 增强接口
#pragma once

#include "emf/common/Resource.h"

namespace emf::common {
class EObject;
class Resource;
}

namespace emf::sphinx::resource {

class ExtendedResourceSet : public emf::common::ResourceSet {
public:
    // 在 scope 内查找 EObject
    virtual emf::common::EObject* getEObjectInScope(const emf::common::URI& uri, bool loadOnDemand) = 0;
    virtual emf::common::Resource* getResourceInScope(const emf::common::URI& uri, bool loadOnDemand) = 0;
};

}  // namespace emf::sphinx::resource
