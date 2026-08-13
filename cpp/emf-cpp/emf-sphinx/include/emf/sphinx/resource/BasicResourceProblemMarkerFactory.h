// BasicResourceProblemMarkerFactory.h
// 对齐 Java org.eclipse.sphinx.emf.resource.BasicResourceProblemMarkerFactory
#pragma once

#include "emf/sphinx/resource/IResourceProblemMarkerFactory.h"

namespace emf::sphinx::resource {

class BasicResourceProblemMarkerFactory : public IResourceProblemMarkerFactory {
public:
    BasicResourceProblemMarkerFactory() = default;
    ~BasicResourceProblemMarkerFactory() override = default;
};

}  // namespace emf::sphinx::resource
