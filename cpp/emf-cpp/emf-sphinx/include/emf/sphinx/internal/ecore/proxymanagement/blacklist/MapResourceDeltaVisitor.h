// MapResourceDeltaVisitor.h
// 对齐 Java org.eclipse.sphinx.emf.internal.ecore.proxymanagement.blacklist.MapResourceDeltaVisitor
// 资源变化访问者（headless 版，用 URI 替代 IResourceDelta）
#pragma once

#include "emf/sphinx/internal/ecore/proxymanagement/blacklist/MapModelIndex.h"
#include <string>
#include <vector>

namespace emf::sphinx::internal::ecore::proxymanagement::blacklist {

class MapResourceDeltaVisitor {
public:
    MapResourceDeltaVisitor(MapModelIndex* index) : index_(index) {}
    virtual ~MapResourceDeltaVisitor() = default;

    virtual void visitAdded(const std::string& resourceUri, const std::string& fragment);
    virtual void visitRemoved(const std::string& resourceUri, const std::string& fragment);
    virtual void visitChanged(const std::string& resourceUri, const std::string& fragment);

private:
    MapModelIndex* index_ = nullptr;
};

}  // namespace emf::sphinx::internal::ecore::proxymanagement::blacklist
