// EMF Common: AdapterFactory
// 对齐 org.eclipse.emf.common.notify.AdapterFactory
// 同时提供 Adapter 别名，等价于 EAdapter。
#pragma once

#include "ENotifier.h"

namespace emf::common {

// 与 EMF Java 一致：Adapter 是 Notifier，同时提供 getTarget/setTarget/isAdapterForType/notifyChanged
// 我们的 C++ ENotifier.h 中 EAdapter 已经是 Notifier 的子接口
// 这里把 EAdapter 暴露为 Adapter 名字（Java EMF 同样把 Adapter 命名为顶层接口）
using Adapter = EAdapter;

class AdapterFactory {
public:
    virtual ~AdapterFactory() = default;
    // Java: boolean isFactoryForType(Object type);
    virtual bool isFactoryForType(const std::any& type) const = 0;
    // Java: Adapter adapt(Notifier target, Object type);
    virtual Adapter* adapt(Notifier* target, Adapter* existing) = 0;
    // Java: Adapter createAdapter(Notifier target);
    virtual Adapter* createAdapter(Notifier* target) = 0;
};

}  // namespace emf::common
