// ProxyHelperAdapterFactory.h
// 对齐 Java org.eclipse.sphinx.emf.internal.ecore.proxymanagement.ProxyHelperAdapterFactory
#pragma once

#include "emf/common/AdapterFactory.h"

namespace emf::sphinx::internal::ecore::proxymanagement {

class ProxyHelperAdapterFactory : public emf::common::AdapterFactory {
public:
    static ProxyHelperAdapterFactory& instance() {
        static ProxyHelperAdapterFactory inst;
        return inst;
    }

    bool isFactoryForType(const std::any& type) const override;
    emf::common::Adapter* createAdapter(emf::common::Notifier* target) override;
    emf::common::Adapter* adapt(emf::common::Notifier* target, emf::common::Adapter* existing) override;
};

}  // namespace emf::sphinx::internal::ecore::proxymanagement
