// ModelDescriptorRegistry.h
// 对齐 Java org.eclipse.sphinx.emf.model.ModelDescriptorRegistry
// 单例：跟踪所有 IModelDescriptor
#pragma once

#include "emf/sphinx/model/IModelDescriptor.h"
#include "emf/common/URI.h"
#include <unordered_map>
#include <vector>
#include <memory>

namespace emf::sphinx::model {

class IModelDescriptorChangeListener {
public:
    virtual ~IModelDescriptorChangeListener() = default;
    virtual void modelDescriptorAdded(IModelDescriptor* md) = 0;
    virtual void modelDescriptorRemoved(IModelDescriptor* md) = 0;
};

class ModelDescriptorRegistry {
public:
    static ModelDescriptorRegistry& instance() {
        static ModelDescriptorRegistry inst;
        return inst;
    }

    void addDescriptor(std::shared_ptr<IModelDescriptor> md);
    void removeDescriptor(IModelDescriptor* md);

    // 查询
    IModelDescriptor* getDescriptor(const emf::common::URI& rootUri) const;
    IModelDescriptor* getDescriptor(emf::common::Resource* res) const;
    std::vector<std::shared_ptr<IModelDescriptor>> getAll() const;

    // 监听
    void addListener(IModelDescriptorChangeListener* l);
    void removeListener(IModelDescriptorChangeListener* l);

    void clear();

private:
    ModelDescriptorRegistry() = default;
    std::vector<std::shared_ptr<IModelDescriptor>> descriptors_;
    std::vector<IModelDescriptorChangeListener*> listeners_;
};

}  // namespace emf::sphinx::model
