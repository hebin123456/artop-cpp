// ModelDescriptorRegistry.cpp
// 对齐 Java org.eclipse.sphinx.emf.model.ModelDescriptorRegistry
#include "emf/sphinx/model/ModelDescriptorRegistry.h"
#include "emf/common/Resource.h"
#include "emf/common/URI.h"

namespace emf::sphinx::model {

void ModelDescriptorRegistry::addDescriptor(std::shared_ptr<IModelDescriptor> md) {
    if (!md) return;
    descriptors_.push_back(std::move(md));
    for (auto* l : listeners_) {
        if (l) l->modelDescriptorAdded(descriptors_.back().get());
    }
}

void ModelDescriptorRegistry::removeDescriptor(IModelDescriptor* md) {
    for (auto it = descriptors_.begin(); it != descriptors_.end(); ++it) {
        if (it->get() == md) {
            for (auto* l : listeners_) {
                if (l) l->modelDescriptorRemoved(md);
            }
            descriptors_.erase(it);
            return;
        }
    }
}

IModelDescriptor* ModelDescriptorRegistry::getDescriptor(const emf::common::URI& rootUri) const {
    for (auto& d : descriptors_) {
        if (d && d->getRootURI() == rootUri) return d.get();
    }
    return nullptr;
}

IModelDescriptor* ModelDescriptorRegistry::getDescriptor(emf::common::Resource* res) const {
    if (!res) return nullptr;
    return getDescriptor(res->getURI());
}

std::vector<std::shared_ptr<IModelDescriptor>> ModelDescriptorRegistry::getAll() const { return descriptors_; }

void ModelDescriptorRegistry::addListener(IModelDescriptorChangeListener* l) { listeners_.push_back(l); }
void ModelDescriptorRegistry::removeListener(IModelDescriptorChangeListener* l) {
    for (auto it = listeners_.begin(); it != listeners_.end(); ++it) {
        if (*it == l) { listeners_.erase(it); return; }
    }
}

void ModelDescriptorRegistry::clear() {
    descriptors_.clear();
    listeners_.clear();
}

}  // namespace emf::sphinx::model
