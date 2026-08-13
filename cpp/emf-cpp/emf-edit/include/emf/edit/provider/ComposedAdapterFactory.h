// ComposedAdapterFactory.h
// 对齐 Java: org.eclipse.emf.edit.provider.ComposedAdapterFactory
// 状态: 框架骨架（最小可编译实现）
#pragma once

#include "emf/common/AdapterFactory.h"

#include <vector>

namespace emf::edit::provider {

// ComposedAdapterFactory：组合多个 AdapterFactory，按顺序委派 adapt（对齐 Java ComposedAdapterFactory）
class ComposedAdapterFactory : public emf::common::AdapterFactory {
public:
    ComposedAdapterFactory() = default;
    ~ComposedAdapterFactory() override;

    void addAdapterFactory(emf::common::AdapterFactory* factory);
    void removeAdapterFactory(emf::common::AdapterFactory* factory);
    const std::vector<emf::common::AdapterFactory*>& getChildFactories() const { return factories_; }

    // AdapterFactory 接口实现
    bool isFactoryForType(const std::any& type) const override;
    emf::common::Adapter* adapt(emf::common::Notifier* target,
                                emf::common::Adapter* existing) override;
    emf::common::Adapter* createAdapter(emf::common::Notifier* target) override;

private:
    std::vector<emf::common::AdapterFactory*> factories_;
};

}  // namespace emf::edit::provider
