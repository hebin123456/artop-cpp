// ExtendedResourceAdapter.h
// 对齐 Java org.eclipse.sphinx.emf.resource.ExtendedResourceAdapter
// 把普通 Resource 适配为 ExtendedResource
#pragma once

#include "emf/common/AdapterFactory.h"
#include "emf/common/EObject.h"
#include "emf/common/Resource.h"
#include "emf/common/URI.h"
#include "emf/sphinx/resource/ExtendedResource.h"

namespace emf::sphinx::resource {

// 骨架：保留 Java 适配器形态，移除大量未在 emf-common 暴露的虚函数 override
class ExtendedResourceAdapter : public emf::common::Adapter {
public:
    ExtendedResourceAdapter() = default;
    ~ExtendedResourceAdapter() override = default;

    void notifyChanged(const emf::common::Notification& /*notification*/) override {}

    void setTarget(emf::common::Notifier* newTarget) override;
    emf::common::Resource* getTargetResource() const { return target_; }

    // 便捷：尝试把 target_ 转成 ExtendedResource*
    ExtendedResource* adapt(emf::common::Resource* res);

private:
    emf::common::Resource* target_ = nullptr;
};

}  // namespace emf::sphinx::resource
