// ModelDescriptorSyncRequest.h
// 对齐 Java org.eclipse.sphinx.emf.internal.model.ModelDescriptorSyncRequest
#pragma once

#include "emf/sphinx/internal/model/IModelDescriptorSyncRequest.h"
#include "emf/sphinx/model/IModelDescriptor.h"

namespace emf::sphinx::internal::model {

class ModelDescriptorSyncRequest : public IModelDescriptorSyncRequest {
public:
    ModelDescriptorSyncRequest() = default;
    ModelDescriptorSyncRequest(emf::sphinx::model::IModelDescriptor* md, bool add) : md_(md), add_(add) {}
    emf::sphinx::model::IModelDescriptor* getModelDescriptor() const override { return md_; }
    bool isAdd() const override { return add_; }
private:
    emf::sphinx::model::IModelDescriptor* md_ = nullptr;
    bool add_ = true;
};

}  // namespace emf::sphinx::internal::model
