// IMetaModelService.h
// 对齐 Java org.eclipse.sphinx.emf.metamodel.services.IMetaModelService
#pragma once

#include "emf/sphinx/metamodel/IMetaModelDescriptor.h"
#include <string>
#include <vector>

namespace emf::sphinx::metamodel::services {

class IMetaModelService {
public:
    virtual ~IMetaModelService() = default;
    virtual const std::vector<IMetaModelDescriptor*>& getMetaModelDescriptors() const = 0;
    virtual bool isApplicableTo(IMetaModelDescriptor* mm) const = 0;
    virtual std::string getId() const = 0;
};

}  // namespace emf::sphinx::metamodel::services
