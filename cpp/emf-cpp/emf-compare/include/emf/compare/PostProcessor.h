// EMF Compare: PostProcessor
// 对齐 org.eclipse.emf.compare.postprocessor.IPostProcessor (Java)
//
// 在 match/diff 完成后对 Comparison 做后处理（如统计、过滤、补充 diff）。
// 高级用户可注入自定义 PostProcessor，由 compare 流水线在对应阶段回调。
// 顶层 compare() 便利函数为保持向后兼容不强制接入，但提供接口供高级用户使用。
#pragma once

#include "Comparison.h"

#include <memory>
#include <vector>

namespace emf::compare {

class PostProcessor {
public:
    virtual ~PostProcessor() = default;
    // match 阶段完成后回调
    virtual void postMatch(Comparison& comp) {}
    // diff 阶段完成后回调
    virtual void postDiff(Comparison& comp) {}
    // requirement 计算完成后回调
    virtual void postRequirements(Comparison& comp) {}
    // equivalence 计算完成后回调
    virtual void postEquivalences(Comparison& comp) {}
    // conflict 检测完成后回调
    virtual void postConflicts(Comparison& comp) {}
};

// 简单组合容器，按顺序执行多个 PostProcessor
// 对齐 Java PostProcessorDescriptor 结构里组织多个 IPostProcessor 的语义。
class PostProcessorChain : public PostProcessor {
public:
    void add(std::unique_ptr<PostProcessor> p) { processors_.push_back(std::move(p)); }

    void postMatch(Comparison& comp) override {
        for (auto& p : processors_) p->postMatch(comp);
    }
    void postDiff(Comparison& comp) override {
        for (auto& p : processors_) p->postDiff(comp);
    }
    void postRequirements(Comparison& comp) override {
        for (auto& p : processors_) p->postRequirements(comp);
    }
    void postEquivalences(Comparison& comp) override {
        for (auto& p : processors_) p->postEquivalences(comp);
    }
    void postConflicts(Comparison& comp) override {
        for (auto& p : processors_) p->postConflicts(comp);
    }

private:
    std::vector<std::unique_ptr<PostProcessor>> processors_;
};

}  // namespace emf::compare
