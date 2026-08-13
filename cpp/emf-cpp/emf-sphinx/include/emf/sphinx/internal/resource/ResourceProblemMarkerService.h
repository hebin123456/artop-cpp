// ResourceProblemMarkerService.h
// 对齐 Java org.eclipse.sphinx.emf.internal.resource.ResourceProblemMarkerService
#pragma once

#include "emf/common/Resource.h"
#include <vector>

namespace emf::sphinx::internal::resource {

class ResourceProblemMarkerService {
public:
    static ResourceProblemMarkerService& instance() {
        static ResourceProblemMarkerService inst;
        return inst;
    }

    void addMarker(emf::common::Resource* res, const std::string& type, const std::string& msg, int line, int col);
    void clearMarkers(emf::common::Resource* res);
    std::vector<std::string> getMarkers(emf::common::Resource* res) const;

private:
    ResourceProblemMarkerService() = default;
};

}  // namespace emf::sphinx::internal::resource
