// IModelConverterDescription.h
// 对齐 Java org.eclipse.sphinx.emf.resource.IModelConverterDescription
#pragma once

#include <string>

namespace emf::sphinx::metamodel {
class IMetaModelDescriptor;
}

namespace emf::sphinx::resource {

class IModelConverterDescription {
public:
    virtual ~IModelConverterDescription() = default;

    virtual std::string getId() const = 0;
    virtual emf::sphinx::metamodel::IMetaModelDescriptor* getSourceMetaModelDescriptor() const = 0;
    virtual emf::sphinx::metamodel::IMetaModelDescriptor* getTargetMetaModelDescriptor() const = 0;
};

}  // namespace emf::sphinx::resource
