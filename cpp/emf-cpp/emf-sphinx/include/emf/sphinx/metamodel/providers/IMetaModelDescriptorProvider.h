// IMetaModelDescriptorProvider.h
// 对齐 Java org.eclipse.sphinx.emf.metamodel.providers.IMetaModelDescriptorProvider
#pragma once

#include "emf/sphinx/metamodel/IMetaModelDescriptor.h"
#include <string>

namespace emf::common {
class EObject;
class Resource;
class URI;
}

namespace emf::sphinx::metamodel::providers {

class IMetaModelDescriptorProvider {
public:
    virtual ~IMetaModelDescriptorProvider() = default;

    virtual IMetaModelDescriptor* getDescriptor(const emf::common::URI& uri) const = 0;
    virtual IMetaModelDescriptor* getDescriptor(emf::common::Resource* res) const = 0;
    virtual IMetaModelDescriptor* getDescriptor(emf::common::EObject* obj) const = 0;
    virtual std::string getContentTypeId() const = 0;
};

}  // namespace emf::sphinx::metamodel::providers
