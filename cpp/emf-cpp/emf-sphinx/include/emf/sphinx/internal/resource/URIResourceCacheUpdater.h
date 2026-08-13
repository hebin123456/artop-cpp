// URIResourceCacheUpdater.h
// 对齐 Java org.eclipse.sphinx.emf.internal.resource.URIResourceCacheUpdater
#pragma once

#include "emf/common/Resource.h"

namespace emf::sphinx::internal::resource {

class URIResourceCacheUpdater {
public:
    static URIResourceCacheUpdater& instance() {
        static URIResourceCacheUpdater inst;
        return inst;
    }

    void update(emf::common::Resource* res);
    void evict(emf::common::Resource* res);

private:
    URIResourceCacheUpdater() = default;
};

}  // namespace emf::sphinx::internal::resource
