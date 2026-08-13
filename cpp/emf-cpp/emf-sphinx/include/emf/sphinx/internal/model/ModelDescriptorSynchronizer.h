// ModelDescriptorSynchronizer.h
// 对齐 Java org.eclipse.sphinx.emf.internal.model.ModelDescriptorSynchronizer
#pragma once

#include "emf/sphinx/internal/model/BasicModelDescriptorSynchronizerDelegate.h"
#include <vector>

namespace emf::sphinx::internal::model {

class ModelDescriptorSynchronizer {
public:
    static ModelDescriptorSynchronizer& instance() {
        static ModelDescriptorSynchronizer inst;
        return inst;
    }

    void addDelegate(BasicModelDescriptorSynchronizerDelegate* d);
    void removeDelegate(BasicModelDescriptorSynchronizerDelegate* d);
    void sync(class IModelDescriptorSyncRequest* req);

private:
    ModelDescriptorSynchronizer() = default;
    std::vector<BasicModelDescriptorSynchronizerDelegate*> delegates_;
};

}  // namespace emf::sphinx::internal::model
