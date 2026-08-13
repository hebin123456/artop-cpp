// DefaultMetaModelServiceProvider.h
// 对齐 Java org.eclipse.sphinx.emf.metamodel.services.DefaultMetaModelServiceProvider
#pragma once

#include "emf/sphinx/metamodel/services/IMetaModelServiceProvider.h"

namespace emf::sphinx::metamodel::services {

class DefaultMetaModelServiceProvider : public IMetaModelServiceProvider {
public:
    DefaultMetaModelServiceProvider() = default;
    ~DefaultMetaModelServiceProvider() override = default;
    std::vector<IMetaModelService*> getMetaModelServices() const override;
};

}  // namespace emf::sphinx::metamodel::services
