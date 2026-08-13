// BasicModelDescriptorSynchronizerDelegate.h
// 对齐 Java org.eclipse.sphinx.emf.internal.model.BasicModelDescriptorSynchronizerDelegate
#pragma once

#include "emf/sphinx/internal/model/IModelDescriptorSyncRequest.h"

namespace emf::sphinx::internal::model {

class BasicModelDescriptorSynchronizerDelegate {
public:
    BasicModelDescriptorSynchronizerDelegate() = default;
    virtual ~BasicModelDescriptorSynchronizerDelegate() = default;

    virtual bool handlesRequest(IModelDescriptorSyncRequest* req) = 0;
    virtual void handleRequest(IModelDescriptorSyncRequest* req) = 0;
};

}  // namespace emf::sphinx::internal::model
