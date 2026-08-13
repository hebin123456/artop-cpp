// ModelDescriptorSynchronizerComplement.h
// 对齐 Java org.eclipse.sphinx.emf.internal.model.ModelDescriptorSynchronizerComplement
#pragma once

#include "emf/sphinx/internal/model/BasicModelDescriptorSynchronizerDelegate.h"

namespace emf::sphinx::internal::model {

class ModelDescriptorSynchronizerComplement : public BasicModelDescriptorSynchronizerDelegate {
public:
    ModelDescriptorSynchronizerComplement() = default;
    ~ModelDescriptorSynchronizerComplement() override = default;

    bool handlesRequest(IModelDescriptorSyncRequest* req) override;
    void handleRequest(IModelDescriptorSyncRequest* req) override;
};

}  // namespace emf::sphinx::internal::model
