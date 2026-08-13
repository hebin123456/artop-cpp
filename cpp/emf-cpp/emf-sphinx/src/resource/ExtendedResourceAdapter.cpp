#include "emf/sphinx/resource/ExtendedResourceAdapter.h"
#include "emf/common/Resource.h"

namespace emf::sphinx::resource {

void ExtendedResourceAdapter::setTarget(emf::common::Notifier* newTarget) {
    target_ = dynamic_cast<emf::common::Resource*>(newTarget);
    emf::common::Adapter::setTarget(newTarget);
}

ExtendedResource* ExtendedResourceAdapter::adapt(emf::common::Resource* res) {
    // 骨架：直接返回 dynamic_cast 结果；后续会调用 ExtendedResourceAdapterFactory
    if (!res) return nullptr;
    return dynamic_cast<ExtendedResource*>(res);
}

}  // namespace emf::sphinx::resource
