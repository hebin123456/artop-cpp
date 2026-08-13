#include "emf/sphinx/resource/ScopingResourceSetImpl.h"
#include "emf/common/Resource.h"

namespace emf::sphinx::resource {

std::vector<emf::common::Resource*> ScopingResourceSetImpl::getResourcesInModel(emf::sphinx::model::IModelDescriptor* /*md*/, bool /*b*/) {
    std::vector<emf::common::Resource*> r;
    // 通过子对象直接访问（ResourceSet::getResources 在 emf-common 暴露）
    auto impl = static_cast<ExtendedResourceSetImpl*>(this);
    const auto& res = impl->emf::common::ResourceSet::getResources();
    for (const auto& up : res) r.push_back(up.get());
    return r;
}

}  // namespace emf::sphinx::resource
