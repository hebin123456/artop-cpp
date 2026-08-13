// ModelIndexUpdater.h
// 对齐 Java org.eclipse.sphinx.emf.internal.ecore.proxymanagement.blacklist.ModelIndexUpdater
// 监听 model 变化并更新 index
#pragma once

#include "emf/sphinx/internal/ecore/proxymanagement/blacklist/MapModelIndex.h"

namespace emf::sphinx::internal::ecore::proxymanagement::blacklist {

class ModelIndexUpdater {
public:
    ModelIndexUpdater() = default;
    virtual ~ModelIndexUpdater() = default;

    virtual void update(MapModelIndex* index);

    static ModelIndexUpdater& instance() {
        static ModelIndexUpdater inst;
        return inst;
    }
};

}  // namespace emf::sphinx::internal::ecore::proxymanagement::blacklist
