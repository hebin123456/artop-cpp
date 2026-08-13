// ECrossReferenceAdapterFactory.h
// 对齐 Java org.eclipse.sphinx.emf.ecore.ECrossReferenceAdapterFactory
// 工厂：为 EObject 安装 eCrossReferenceAdapter
#pragma once

#include "emf/common/AdapterFactory.h"
#include "emf/common/EList.h"
#include <vector>
#include <string>

namespace emf::sphinx::ecore {

class ECrossReferenceAdapterFactory : public emf::common::AdapterFactory {
public:
    ECrossReferenceAdapterFactory() = default;
    ~ECrossReferenceAdapterFactory() override = default;

    bool isFactoryForType(const std::any& type) const override;
    emf::common::Adapter* createAdapter(emf::common::Notifier* target) override;
    emf::common::Adapter* adapt(emf::common::Notifier* target, emf::common::Adapter* existing) override;
};

}  // namespace emf::sphinx::ecore
