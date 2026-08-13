// AbstractMetaModelService.h
// 对齐 Java org.eclipse.sphinx.emf.metamodel.services.AbstractMetaModelService
#pragma once

#include "emf/sphinx/metamodel/services/IMetaModelService.h"

namespace emf::sphinx::metamodel::services {

class AbstractMetaModelService : public IMetaModelService {
public:
    AbstractMetaModelService() = default;
    ~AbstractMetaModelService() override = default;
    std::string getId() const override { return id_; }
    void setId(const std::string& v) { id_ = v; }
    const std::vector<IMetaModelDescriptor*>& getMetaModelDescriptors() const override { return descriptors_; }
    void setMetaModelDescriptors(const std::vector<IMetaModelDescriptor*>& v) { descriptors_ = v; }
    bool isApplicableTo(IMetaModelDescriptor* mm) const override;
private:
    std::string id_;
    std::vector<IMetaModelDescriptor*> descriptors_;
};

}  // namespace emf::sphinx::metamodel::services
