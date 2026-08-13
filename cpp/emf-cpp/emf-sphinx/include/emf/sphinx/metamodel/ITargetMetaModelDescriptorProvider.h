// ITargetMetaModelDescriptorProvider.h
// 对齐 Java org.eclipse.sphinx.emf.metamodel.ITargetMetaModelDescriptorProvider
#pragma once

#include "emf/sphinx/metamodel/IMetaModelDescriptor.h"
#include <string>

namespace emf::sphinx::metamodel {

class ITargetMetaModelDescriptorProvider {
public:
    virtual ~ITargetMetaModelDescriptorProvider() = default;
    virtual IMetaModelDescriptor* getTargetMetaModelDescriptor(const std::string& content) const = 0;
};

}  // namespace emf::sphinx::metamodel
