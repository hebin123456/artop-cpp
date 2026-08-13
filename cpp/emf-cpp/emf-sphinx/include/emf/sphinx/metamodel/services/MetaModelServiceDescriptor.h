// MetaModelServiceDescriptor.h
// 对齐 Java org.eclipse.sphinx.emf.metamodel.services.MetaModelServiceDescriptor
#pragma once

#include "emf/sphinx/metamodel/services/IMetaModelService.h"
#include "emf/sphinx/metamodel/IMetaModelDescriptor.h"
#include <string>

namespace emf::sphinx::metamodel::services {

class MetaModelServiceDescriptor {
public:
    MetaModelServiceDescriptor() = default;
    MetaModelServiceDescriptor(const std::string& id, const std::vector<IMetaModelDescriptor*>& descriptors)
        : id_(id), descriptors_(descriptors) {}

    std::string getId() const { return id_; }
    const std::vector<IMetaModelDescriptor*>& getMetaModelDescriptors() const { return descriptors_; }

private:
    std::string id_;
    std::vector<IMetaModelDescriptor*> descriptors_;
};

}  // namespace emf::sphinx::metamodel::services
