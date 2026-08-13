#include "emf/sphinx/resource/ExtendedResourceAdapterFactory.h"
#include "emf/sphinx/resource/ExtendedResourceAdapter.h"
#include "emf/sphinx/resource/ExtendedResource.h"
#include "emf/common/Resource.h"
#include <typeinfo>

namespace emf::sphinx::resource {

bool ExtendedResourceAdapterFactory::isFactoryForType(const std::any& t) const {
    return t.type() == typeid(ExtendedResource*);
}

emf::common::Adapter* ExtendedResourceAdapterFactory::createAdapter(emf::common::Notifier* target) {
    if (!target) return nullptr;
    auto* a = new ExtendedResourceAdapter();
    a->setTarget(target);
    return a;
}

emf::common::Adapter* ExtendedResourceAdapterFactory::adapt(emf::common::Notifier* target, emf::common::Adapter* existing) {
    if (existing) return existing;
    return createAdapter(target);
}

ExtendedResource* ExtendedResourceAdapterFactory::adapt(emf::common::Notifier* target) {
    if (!target) return nullptr;
    // 骨架：先尝试 dynamic_cast target 自身为 ExtendedResource，
    // 否则直接返回 nullptr
    return dynamic_cast<ExtendedResource*>(target);
}

}  // namespace emf::sphinx::resource
