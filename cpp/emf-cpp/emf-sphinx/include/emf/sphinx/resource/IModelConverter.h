// IModelConverter.h
// 对齐 Java org.eclipse.sphinx.emf.resource.IModelConverter
// 模型转换器接口（如 UML → Ecore）
#pragma once

#include <any>
#include <string>

namespace emf::common {
class EObject;
class Resource;
}

namespace emf::sphinx::metamodel {
class IMetaModelDescriptor;
}

namespace emf::sphinx::resource {

class IModelConverter {
public:
    virtual ~IModelConverter() = default;

    virtual std::string getId() const = 0;
    virtual emf::sphinx::metamodel::IMetaModelDescriptor* getSourceMetaModelDescriptor() const = 0;
    virtual emf::sphinx::metamodel::IMetaModelDescriptor* getTargetMetaModelDescriptor() const = 0;
    virtual emf::common::Resource* convert(emf::common::Resource* source, const std::string& targetContentType) = 0;
};

}  // namespace emf::sphinx::resource
