// ModelDescriptorRegistryInitializer.h
// 对齐 Java org.eclipse.sphinx.emf.internal.model.ModelDescriptorRegistryInitializer
// 初始化时扫描注册所有 IModelDescriptor
#pragma once

#include "emf/sphinx/model/ModelDescriptorRegistry.h"

namespace emf::sphinx::internal::model {

class ModelDescriptorRegistryInitializer {
public:
    static ModelDescriptorRegistryInitializer& instance() {
        static ModelDescriptorRegistryInitializer inst;
        return inst;
    }

    void initialize(emf::sphinx::model::ModelDescriptorRegistry* reg);

private:
    ModelDescriptorRegistryInitializer() = default;
};

}  // namespace emf::sphinx::internal::model
