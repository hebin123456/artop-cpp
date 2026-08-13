// MetaModelDescriptorCacheUpdater.h
// 对齐 Java org.eclipse.sphinx.emf.internal.metamodel.MetaModelDescriptorCacheUpdater
// 监听 model 变化以更新 metamodel cache
#pragma once

#include "emf/sphinx/metamodel/MetaModelDescriptorRegistry.h"
#include <string>

namespace emf::sphinx::internal::metamodel {

class MetaModelDescriptorCacheUpdater {
public:
    static MetaModelDescriptorCacheUpdater& instance() {
        static MetaModelDescriptorCacheUpdater inst;
        return inst;
    }

    void update(const std::string& resourceUri, const std::string& contentType);
    void clear(const std::string& resourceUri);

private:
    MetaModelDescriptorCacheUpdater() = default;
};

}  // namespace emf::sphinx::internal::metamodel
