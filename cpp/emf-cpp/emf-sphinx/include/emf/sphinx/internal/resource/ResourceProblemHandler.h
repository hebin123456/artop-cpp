// ResourceProblemHandler.h
// 对齐 Java org.eclipse.sphinx.emf.internal.resource.ResourceProblemHandler
#pragma once

#include "emf/common/Resource.h"

namespace emf::sphinx::internal::resource {

class ResourceProblemHandler {
public:
    static ResourceProblemHandler& instance() {
        static ResourceProblemHandler inst;
        return inst;
    }

    void handleResourceChanged(emf::common::Resource* res);

private:
    ResourceProblemHandler() = default;
};

}  // namespace emf::sphinx::internal::resource
