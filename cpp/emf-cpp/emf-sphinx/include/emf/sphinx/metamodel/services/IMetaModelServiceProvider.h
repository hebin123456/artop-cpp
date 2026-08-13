// IMetaModelServiceProvider.h
// 对齐 Java org.eclipse.sphinx.emf.metamodel.services.IMetaModelServiceProvider
#pragma once

#include "emf/sphinx/metamodel/services/IMetaModelService.h"
#include <vector>

namespace emf::sphinx::metamodel::services {

class IMetaModelServiceProvider {
public:
    virtual ~IMetaModelServiceProvider() = default;
    virtual std::vector<IMetaModelService*> getMetaModelServices() const = 0;
};

}  // namespace emf::sphinx::metamodel::services
