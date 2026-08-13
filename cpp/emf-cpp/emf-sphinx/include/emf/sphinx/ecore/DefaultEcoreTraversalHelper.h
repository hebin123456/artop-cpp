// DefaultEcoreTraversalHelper.h
// 对齐 Java org.eclipse.sphinx.emf.ecore.DefaultEcoreTraversalHelper
#pragma once

#include "emf/sphinx/ecore/EcoreTraversalHelper.h"

namespace emf::sphinx::ecore {

class DefaultEcoreTraversalHelper : public EcoreTraversalHelper {
public:
    DefaultEcoreTraversalHelper() = default;
    ~DefaultEcoreTraversalHelper() override = default;
};

}  // namespace emf::sphinx::ecore
