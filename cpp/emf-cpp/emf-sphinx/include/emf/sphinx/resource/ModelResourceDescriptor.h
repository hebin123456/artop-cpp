// ModelResourceDescriptor.h
// 对齐 Java org.eclipse.sphinx.emf.resource.ModelResourceDescriptor
#pragma once

#include "emf/common/URI.h"
#include <string>
#include <vector>

namespace emf::sphinx::metamodel {
class IMetaModelDescriptor;
}

namespace emf::sphinx::resource {

class ModelResourceDescriptor {
public:
    ModelResourceDescriptor() = default;
    ModelResourceDescriptor(const emf::common::URI& uri, emf::sphinx::metamodel::IMetaModelDescriptor* mm)
        : uri_(uri), mm_(mm) {}

    const emf::common::URI& getURI() const { return uri_; }
    emf::sphinx::metamodel::IMetaModelDescriptor* getMetaModelDescriptor() const { return mm_; }
    void setMetaModelDescriptor(emf::sphinx::metamodel::IMetaModelDescriptor* mm) { mm_ = mm; }

private:
    emf::common::URI uri_;
    emf::sphinx::metamodel::IMetaModelDescriptor* mm_ = nullptr;
};

}  // namespace emf::sphinx::resource
