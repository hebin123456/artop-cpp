// NotifyingList.h
// 对齐 Java org.eclipse.emf.common.notify.NotifyingList
#pragma once

#include "emf/common/util/AbstractEList.h"

namespace emf::common {
class Notifier;
}

// NotifyingList：与 AbstractEList 不共享继承，仅作接口（用 CRTP / 鸭子类型）
// 在 C++ 里为了避免菱形继承，对应到 BasicEList<E> 上的多接口。
namespace emf::common::util {

template <typename E>
class NotifyingList {
public:
    virtual ~NotifyingList() = default;
    virtual emf::common::Notifier* getNotifier() = 0;
    virtual const emf::common::Notifier* getNotifier() const = 0;
    virtual const void* getFeature() const = 0;
    virtual int getFeatureID() const = 0;
};

}  // namespace emf::common::util
