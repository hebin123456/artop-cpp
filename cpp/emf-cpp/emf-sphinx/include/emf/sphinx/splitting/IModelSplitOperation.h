// IModelSplitOperation.h
// 对齐 Java org.eclipse.sphinx.emf.splitting.IModelSplitOperation
#pragma once

#include "emf/common/EObject.h"
#include <vector>

namespace emf::sphinx::splitting {

class IModelSplitOperation {
public:
    virtual ~IModelSplitOperation() = default;
    virtual emf::common::EObject* getContext() const = 0;
    virtual std::vector<emf::common::EObject*> getRoots() const = 0;
};

}  // namespace emf::sphinx::splitting
