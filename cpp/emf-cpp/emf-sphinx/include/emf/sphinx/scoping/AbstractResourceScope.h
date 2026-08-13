// AbstractResourceScope.h
// 对齐 Java org.eclipse.sphinx.emf.scoping.AbstractResourceScope
#pragma once

#include "emf/sphinx/scoping/IResourceScope.h"

namespace emf::sphinx::scoping {

class AbstractResourceScope : public IResourceScope {
public:
    AbstractResourceScope() = default;
    ~AbstractResourceScope() override = default;
};

}  // namespace emf::sphinx::scoping
