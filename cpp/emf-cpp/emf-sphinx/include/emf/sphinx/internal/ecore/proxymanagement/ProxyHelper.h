// ProxyHelper.h
// 对齐 Java org.eclipse.sphinx.emf.internal.ecore.proxymanagement.ProxyHelper
// adapter 实现：在 EObject 上追踪其 proxy URI
#pragma once

#include "emf/common/AdapterFactory.h"
#include "emf/common/EObject.h"
#include "emf/common/URI.h"
#include "emf/common/ENotifier.h"
#include <string>
#include <unordered_map>

namespace emf::sphinx::internal::ecore::proxymanagement {

class ProxyHelper : public emf::common::Adapter {
public:
    ProxyHelper() = default;
    ~ProxyHelper() override = default;

    // C++ 骨架：保留 EAdapter 已有的 notifyChanged / getTarget / setTarget
    // isAdapterForType 在父类已提供默认实现
    void notifyChanged(const emf::common::Notification& /*notification*/) override {}

    // 设置/获取 proxy URI
    void setProxyURI(const emf::common::URI& uri) { proxyURI_ = uri; }
    emf::common::URI getProxyURI() const { return proxyURI_; }

    // 记录 unresolved 状态
    bool isUnresolved() const { return unresolved_; }
    void setUnresolved(bool v) { unresolved_ = v; }

private:
    emf::common::URI proxyURI_;
    bool unresolved_ = true;
};

}  // namespace emf::sphinx::internal::ecore::proxymanagement
