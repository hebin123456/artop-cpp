// ComposedAdapterFactory.cpp
// 对齐 Java: org.eclipse.emf.edit.provider.ComposedAdapterFactory
#include "emf/edit/provider/ComposedAdapterFactory.h"

#include <algorithm>

namespace emf::edit::provider {

ComposedAdapterFactory::~ComposedAdapterFactory() = default;

void ComposedAdapterFactory::addAdapterFactory(emf::common::AdapterFactory* factory) {
    if (factory) factories_.push_back(factory);
}

void ComposedAdapterFactory::removeAdapterFactory(emf::common::AdapterFactory* factory) {
    factories_.erase(std::remove(factories_.begin(), factories_.end(), factory), factories_.end());
}

bool ComposedAdapterFactory::isFactoryForType(const std::any& /*type*/) const {
    // 极简策略：只要持有任意子 factory 即视为可处理
    return !factories_.empty();
}

emf::common::Adapter* ComposedAdapterFactory::adapt(emf::common::Notifier* target,
                                                    emf::common::Adapter* existing) {
    // 委派给第一个能 adapt 的子 factory
    for (auto* f : factories_) {
        if (!f) continue;
        auto* a = f->adapt(target, existing);
        if (a) return a;
    }
    return nullptr;
}

emf::common::Adapter* ComposedAdapterFactory::createAdapter(emf::common::Notifier* target) {
    for (auto* f : factories_) {
        if (!f) continue;
        auto* a = f->createAdapter(target);
        if (a) return a;
    }
    return nullptr;
}

}  // namespace emf::edit::provider
