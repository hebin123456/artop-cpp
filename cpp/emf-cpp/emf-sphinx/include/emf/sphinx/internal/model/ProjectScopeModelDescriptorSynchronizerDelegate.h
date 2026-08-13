// ProjectScopeModelDescriptorSynchronizerDelegate.h
// 对齐 Java org.eclipse.sphinx.emf.internal.model.ProjectScopeModelDescriptorSynchronizerDelegate
#pragma once

#include "emf/sphinx/internal/model/BasicModelDescriptorSynchronizerDelegate.h"

namespace emf::sphinx::internal::model {

class ProjectScopeModelDescriptorSynchronizerDelegate : public BasicModelDescriptorSynchronizerDelegate {
public:
    ProjectScopeModelDescriptorSynchronizerDelegate() = default;
    ~ProjectScopeModelDescriptorSynchronizerDelegate() override = default;

    bool handlesRequest(IModelDescriptorSyncRequest* req) override;
    void handleRequest(IModelDescriptorSyncRequest* req) override;
};

}  // namespace emf::sphinx::internal::model
