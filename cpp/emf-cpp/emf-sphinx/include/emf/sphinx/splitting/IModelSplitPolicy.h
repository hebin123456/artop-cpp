// IModelSplitPolicy.h
// 对齐 Java org.eclipse.sphinx.emf.splitting.IModelSplitPolicy
// 拆分策略：将一个 model 拆成多个 model
#pragma once

#include "emf/sphinx/splitting/IModelSplitDirective.h"
#include <vector>

namespace emf::sphinx::splitting {

class IModelSplitPolicy {
public:
    virtual ~IModelSplitPolicy() = default;
    virtual std::vector<IModelSplitDirective*> getSplitDirectives(class IModelSplitOperation* op) = 0;
};

}  // namespace emf::sphinx::splitting
