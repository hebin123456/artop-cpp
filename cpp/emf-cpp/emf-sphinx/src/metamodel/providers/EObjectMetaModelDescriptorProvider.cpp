#include "emf/sphinx/metamodel/providers/EObjectMetaModelDescriptorProvider.h"
#include "emf/common/EObject.h"
#include "emf/common/Resource.h"
#include "emf/common/EPackage.h"
#include "emf/ecore/EcorePackage.h"
#include "emf/sphinx/metamodel/AbstractMetaModelDescriptor.h"
#include <memory>
namespace emf::sphinx::metamodel::providers {

IMetaModelDescriptor* EObjectMetaModelDescriptorProvider::getDescriptor(const emf::common::URI& /*u*/) const { return nullptr; }

IMetaModelDescriptor* EObjectMetaModelDescriptorProvider::getDescriptor(emf::common::Resource* res) const {
    if (!res) return nullptr;
    if (res->getContents().empty()) return nullptr;
    auto* obj = res->getContents().front();
    return getDescriptor(obj);
}

IMetaModelDescriptor* EObjectMetaModelDescriptorProvider::getDescriptor(emf::common::EObject* obj) const {
    if (!obj) return nullptr;
    auto* cls = obj->eClass();
    if (!cls) return nullptr;
    auto* pkg = cls->getEPackage();
    if (!pkg) return nullptr;
    static thread_local std::unique_ptr<AbstractMetaModelDescriptor> d;
    if (!d) d.reset(new AbstractMetaModelDescriptor());
    d->setNamespaceURI(emf::common::URI(pkg->getNsURI()));
    d->setName(pkg->getName());
    return d.get();
}

}  // namespace emf::sphinx::metamodel::providers
