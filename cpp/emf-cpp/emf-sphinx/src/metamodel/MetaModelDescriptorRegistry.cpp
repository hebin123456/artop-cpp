// MetaModelDescriptorRegistry.cpp
// 对齐 Java org.eclipse.sphinx.emf.metamodel.MetaModelDescriptorRegistry
#include "emf/sphinx/metamodel/MetaModelDescriptorRegistry.h"
#include "emf/sphinx/metamodel/IMetaModelDescriptor.h"
#include "emf/common/URI.h"
#include "emf/common/Resource.h"
#include "emf/common/EObject.h"
#include "emf/ecore/EcorePackage.h"

namespace emf::sphinx::metamodel {

void MetaModelDescriptorRegistry::registerDescriptor(IMetaModelDescriptor* d) {
    if (!d) return;
    std::string ns = d->getNamespace();
    if (ns.empty()) return;
    descriptors_[ns] = d;
    // target/old 共享同一注册入口（Java 行为：registerDescriptor 同时归类为可用 target）
    targetDescriptors_[ns] = d;
    oldDescriptors_[ns] = d;
}

void MetaModelDescriptorRegistry::unregisterDescriptor(IMetaModelDescriptor* d) {
    if (!d) return;
    std::string ns = d->getNamespace();
    if (ns.empty()) return;
    auto it = descriptors_.find(ns);
    if (it != descriptors_.end() && it->second == d) {
        descriptors_.erase(it);
    }
    auto it2 = targetDescriptors_.find(ns);
    if (it2 != targetDescriptors_.end() && it2->second == d) {
        targetDescriptors_.erase(it2);
    }
    auto it3 = oldDescriptors_.find(ns);
    if (it3 != oldDescriptors_.end() && it3->second == d) {
        oldDescriptors_.erase(it3);
    }
}

IMetaModelDescriptor* MetaModelDescriptorRegistry::getDescriptor(const std::string& nsURI) const {
    auto it = descriptors_.find(nsURI);
    if (it != descriptors_.end()) return it->second;
    return nullptr;
}

IMetaModelDescriptor* MetaModelDescriptorRegistry::getDescriptor(const emf::common::URI& uri) const {
    return getDescriptor(uri.toString());
}

IMetaModelDescriptor* MetaModelDescriptorRegistry::getDescriptor(emf::common::EObject* obj) const {
    if (!obj) return nullptr;
    // 用对象 eClass().getEPackage().getNsURI() 查找
    auto* cls = obj->eClass();
    if (!cls) return nullptr;
    auto* pkg = cls->getEPackage();
    if (!pkg) return nullptr;
    return getDescriptor(pkg->getNsURI());
}

IMetaModelDescriptor* MetaModelDescriptorRegistry::getDescriptor(emf::common::Resource* res) const {
    if (!res) return nullptr;
    auto contents = res->getContents();
    if (contents.empty()) return nullptr;
    return getDescriptor(static_cast<emf::common::EObject*>(contents.front()));
}

IMetaModelDescriptor* MetaModelDescriptorRegistry::getTargetDescriptor(const emf::common::URI& uri) const {
    auto it = targetDescriptors_.find(uri.toString());
    if (it != targetDescriptors_.end()) return it->second;
    return nullptr;
}

IMetaModelDescriptor* MetaModelDescriptorRegistry::getTargetDescriptor(emf::common::Resource* res) const {
    if (!res) return nullptr;
    return getTargetDescriptor(res->getURI());
}

IMetaModelDescriptor* MetaModelDescriptorRegistry::getOldDescriptor(const emf::common::URI& uri) const {
    auto it = oldDescriptors_.find(uri.toString());
    if (it != oldDescriptors_.end()) return it->second;
    return nullptr;
}

IMetaModelDescriptor* MetaModelDescriptorRegistry::getOldDescriptor(emf::common::Resource* res) const {
    if (!res) return nullptr;
    return getOldDescriptor(res->getURI());
}

std::vector<std::string> MetaModelDescriptorRegistry::keys() const {
    std::vector<std::string> r;
    r.reserve(descriptors_.size());
    for (auto& kv : descriptors_) r.push_back(kv.first);
    return r;
}

std::vector<IMetaModelDescriptor*> MetaModelDescriptorRegistry::getAll() const {
    std::vector<IMetaModelDescriptor*> r;
    r.reserve(descriptors_.size());
    for (auto& kv : descriptors_) r.push_back(kv.second);
    return r;
}

void MetaModelDescriptorRegistry::clear() {
    descriptors_.clear();
    targetDescriptors_.clear();
    oldDescriptors_.clear();
}

}  // namespace emf::sphinx::metamodel
