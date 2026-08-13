// EObjectMetaModelDescriptorProvider.h
// 对齐 Java org.eclipse.sphinx.emf.metamodel.providers.EObjectMetaModelDescriptorProvider
// 通过 EObject 的 EClass.getEPackage 找到对应元模型
#pragma once

#include "emf/sphinx/metamodel/providers/IMetaModelDescriptorProvider.h"

namespace emf::sphinx::metamodel::providers {

class EObjectMetaModelDescriptorProvider : public IMetaModelDescriptorProvider {
public:
    static EObjectMetaModelDescriptorProvider& instance() {
        static EObjectMetaModelDescriptorProvider inst;
        return inst;
    }

    IMetaModelDescriptor* getDescriptor(const emf::common::URI& uri) const override;
    IMetaModelDescriptor* getDescriptor(emf::common::Resource* res) const override;
    IMetaModelDescriptor* getDescriptor(emf::common::EObject* obj) const override;
    std::string getContentTypeId() const override { return ""; }
};

}  // namespace emf::sphinx::metamodel::providers
