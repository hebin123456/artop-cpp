// ModelSplitProcessor.h
// 对齐 Java org.eclipse.sphinx.emf.splitting.ModelSplitProcessor
// 根据 policy 把 model 拆成多个
#pragma once

#include "emf/sphinx/splitting/IModelSplitPolicy.h"
#include "emf/sphinx/splitting/IModelSplitOperation.h"
#include <memory>

namespace emf::sphinx::splitting {

class ModelSplitProcessor {
public:
    ModelSplitProcessor() = default;
    ~ModelSplitProcessor() = default;

    // 用给定 policy 处理 operation
    void process(IModelSplitOperation* op, IModelSplitPolicy* policy);
};

}  // namespace emf::sphinx::splitting
