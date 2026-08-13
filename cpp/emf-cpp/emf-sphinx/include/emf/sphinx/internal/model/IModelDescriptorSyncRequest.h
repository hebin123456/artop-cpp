// IModelDescriptorSyncRequest.h
// 对齐 Java org.eclipse.sphinx.emf.internal.model.IModelDescriptorSyncRequest
#pragma once

#include "emf/sphinx/model/IModelDescriptor.h"

namespace emf::sphinx::internal::model {

class IModelDescriptorSyncRequest {
public:
    virtual ~IModelDescriptorSyncRequest() = default;
    virtual emf::sphinx::model::IModelDescriptor* getModelDescriptor() const = 0;
    virtual bool isAdd() const = 0;
};

}  // namespace emf::sphinx::internal::model
