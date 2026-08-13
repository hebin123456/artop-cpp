// ExtendedResourceAdapterFactory.h
// 对齐 Java org.eclipse.sphinx.emf.resource.ExtendedResourceAdapterFactory
#pragma once

#include "emf/common/AdapterFactory.h"

namespace emf::sphinx::resource {

class ExtendedResourceAdapterFactory : public emf::common::AdapterFactory {
public:
    static ExtendedResourceAdapterFactory& instance() {
        static ExtendedResourceAdapterFactory inst;
        return inst;
    }

    bool isFactoryForType(const std::any& type) const override;
    emf::common::Adapter* createAdapter(emf::common::Notifier* target) override;
    emf::common::Adapter* adapt(emf::common::Notifier* target, emf::common::Adapter* existing) override;

    // 便捷：取或装
    class ExtendedResource* adapt(emf::common::Notifier* target);
};

}  // namespace emf::sphinx::resource
