// IModelLoadService.h
// 对齐 Java org.eclipse.sphinx.emf.loading.IModelLoadService
#pragma once

#include "emf/sphinx/model/IModelDescriptor.h"
#include <functional>
#include <string>
#include <memory>

namespace emf::sphinx::loading {

class IModelLoadService {
public:
    virtual ~IModelLoadService() = default;

    using Progress = std::function<void(int /*done*/, int /*total*/)>;

    // 加载
    virtual void loadModel(emf::sphinx::model::IModelDescriptor* md, bool async, const Progress& progress = {}) = 0;
    virtual void loadModels(const std::vector<emf::sphinx::model::IModelDescriptor*>& mds, bool async, const Progress& progress = {}) = 0;

    // 是否加载
    virtual bool isModelLoaded(emf::sphinx::model::IModelDescriptor* md) = 0;
    virtual bool areModelsLoaded(const std::vector<emf::sphinx::model::IModelDescriptor*>& mds) = 0;
};

}  // namespace emf::sphinx::loading
