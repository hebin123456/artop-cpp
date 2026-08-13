// ResourceScopeMarkerSynchronizer.h
// 对齐 Java org.eclipse.sphinx.emf.scoping.ResourceScopeMarkerSynchronizer
#pragma once

#include <vector>

namespace emf::sphinx::scoping {
class IResourceScope;
}

namespace emf::sphinx::scoping {

class ResourceScopeMarkerSynchronizer {
public:
    static ResourceScopeMarkerSynchronizer& instance() {
        static ResourceScopeMarkerSynchronizer inst;
        return inst;
    }

    void sync(const std::vector<IResourceScope*>& scopes);

private:
    ResourceScopeMarkerSynchronizer() = default;
};

}  // namespace emf::sphinx::scoping
