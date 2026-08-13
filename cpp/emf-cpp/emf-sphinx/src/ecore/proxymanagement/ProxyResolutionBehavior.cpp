// ProxyResolutionBehavior.cpp
#include "emf/sphinx/ecore/proxymanagement/ProxyResolutionBehavior.h"
#include "emf/sphinx/ecore/proxymanagement/IProxyResolver.h"
#include "emf/common/EObject.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/common/URI.h"

#include <algorithm>

namespace emf::sphinx::ecore::proxymanagement {

// 静态 resolvers 列表访问器（函数内 static，避免全局构造顺序/链接问题）
std::vector<IProxyResolver*>& ProxyResolutionBehavior::resolvers() {
    static std::vector<IProxyResolver*> r;
    return r;
}

emf::common::EObject* ProxyResolutionBehavior::eResolveProxy(emf::common::EObject* /*self*/, emf::common::EObject* proxy) {
    if (!proxy) return nullptr;
    if (proxy->eIsProxy()) {
        // 依次询问已注册的 resolver，首个能处理的负责解析
        for (auto* resolver : resolvers()) {
            if (resolver && resolver->canResolve(proxy)) {
                // context=nullptr, loadOnDemand=true（对齐 Sphinx 默认行为）
                return resolver->getEObject(proxy, nullptr, true);
            }
        }
        // 默认：调用 EMF 默认实现（内部 resolve）
        return proxy->eResolveProxy(proxy);
    }
    return proxy;
}

void ProxyResolutionBehavior::addResolver(IProxyResolver* resolver) {
    if (resolver) resolvers().push_back(resolver);
}

void ProxyResolutionBehavior::removeResolver(IProxyResolver* resolver) {
    auto& v = resolvers();
    v.erase(std::remove(v.begin(), v.end(), resolver), v.end());
}

}  // namespace emf::sphinx::ecore::proxymanagement
