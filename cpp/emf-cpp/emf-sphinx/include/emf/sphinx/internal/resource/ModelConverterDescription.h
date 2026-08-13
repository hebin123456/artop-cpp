// ModelConverterDescription.h
// 对齐 Java org.eclipse.sphinx.emf.internal.resource.ModelConverterDescription
#pragma once

#include "emf/sphinx/resource/IModelConverterDescription.h"

namespace emf::sphinx::internal::resource {

class ModelConverterDescription : public emf::sphinx::resource::IModelConverterDescription {
public:
    ModelConverterDescription() = default;
    ModelConverterDescription(const std::string& id, emf::sphinx::metamodel::IMetaModelDescriptor* src, emf::sphinx::metamodel::IMetaModelDescriptor* tgt)
        : id_(id), src_(src), tgt_(tgt) {}

    std::string getId() const override { return id_; }
    emf::sphinx::metamodel::IMetaModelDescriptor* getSourceMetaModelDescriptor() const override { return src_; }
    emf::sphinx::metamodel::IMetaModelDescriptor* getTargetMetaModelDescriptor() const override { return tgt_; }

private:
    std::string id_;
    emf::sphinx::metamodel::IMetaModelDescriptor* src_ = nullptr;
    emf::sphinx::metamodel::IMetaModelDescriptor* tgt_ = nullptr;
};

}  // namespace emf::sphinx::internal::resource
