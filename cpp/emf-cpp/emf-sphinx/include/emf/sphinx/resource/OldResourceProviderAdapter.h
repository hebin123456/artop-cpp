// OldResourceProviderAdapter.h
// 对齐 Java org.eclipse.sphinx.emf.resource.OldResourceProviderAdapter
#pragma once

#include "emf/common/AdapterFactory.h"
#include "emf/common/EObject.h"
#include "emf/common/Resource.h"
#include "emf/common/URI.h"
#include "emf/sphinx/resource/OldResourceProvider.h"
#include <vector>
#include <string>

namespace emf::sphinx::resource {

class OldResourceProviderAdapter : public emf::common::Adapter, public OldResourceProvider {
public:
    OldResourceProviderAdapter() = default;
    ~OldResourceProviderAdapter() override = default;

    void notifyChanged(const emf::common::Notification& /*notification*/) override {}

    void cacheOldContent(emf::common::Resource* res) override;
    std::vector<emf::common::EObject*> getOldContents(emf::common::Resource* res) override;
    std::string getOldURIFragment(emf::common::EObject* obj) override;
    emf::common::EObject* getOldEObject(const emf::common::URI& uri) override;
    void clearCache(emf::common::Resource* res) override;
};

}  // namespace emf::sphinx::resource
