// MetaModelServiceRegistry.h
// 对齐 Java org.eclipse.sphinx.emf.metamodel.services.MetaModelServiceRegistry
// 单例：跟踪所有 IMetaModelService
#pragma once

#include "emf/sphinx/metamodel/services/IMetaModelService.h"
#include <vector>

namespace emf::sphinx::metamodel::services {

class MetaModelServiceRegistry {
public:
    static MetaModelServiceRegistry& instance() {
        static MetaModelServiceRegistry inst;
        return inst;
    }

    void add(IMetaModelService* svc);
    void remove(IMetaModelService* svc);
    std::vector<IMetaModelService*> getAll() const { return services_; }
    std::vector<IMetaModelService*> getServicesFor(const class IMetaModelDescriptor* mm) const;
    void clear();

private:
    MetaModelServiceRegistry() = default;
    std::vector<IMetaModelService*> services_;
};

}  // namespace emf::sphinx::metamodel::services
