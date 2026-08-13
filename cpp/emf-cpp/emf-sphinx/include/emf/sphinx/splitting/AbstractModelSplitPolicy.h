// AbstractModelSplitPolicy.h
// 对齐 Java org.eclipse.sphinx.emf.splitting.AbstractModelSplitPolicy
#pragma once

#include "emf/sphinx/splitting/IModelSplitPolicy.h"

namespace emf::sphinx::splitting {

class AbstractModelSplitPolicy : public IModelSplitPolicy {
public:
    AbstractModelSplitPolicy() = default;
    ~AbstractModelSplitPolicy() override = default;
};

}  // namespace emf::sphinx::splitting
