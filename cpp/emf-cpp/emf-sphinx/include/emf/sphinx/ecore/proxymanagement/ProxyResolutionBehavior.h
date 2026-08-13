// ProxyResolutionBehavior.h
// 对齐 Java org.eclipse.sphinx.emf.ecore.proxymanagement.ProxyResolutionBehavior
//
// Sphinx 代理解析行为入口：维护一组 IProxyResolver，提供 eResolveProxy 静态方法。
// EObjectImpl::eResolveProxy 可委托此处，实现 Sphinx 风格的集中式代理解析。
#pragma once

#include "emf/sphinx/ecore/proxymanagement/IProxyResolver.h"

#include <vector>

namespace emf::common {
class EObject;
}

namespace emf::sphinx::ecore::proxymanagement {

// 代理解析行为（静态工具类）
// 对齐 Java: ProxyResolutionBehavior 是静态方法集合 + 静态 resolvers 列表
class ProxyResolutionBehavior {
public:
    // 解析代理对象
    // 对齐 Java: ProxyResolutionBehavior.eResolveProxy(EObject, EObject)
    static emf::common::EObject* eResolveProxy(emf::common::EObject* self,
                                                 emf::common::EObject* proxy);

    // 注册/注销代理解析器
    static void addResolver(IProxyResolver* resolver);
    static void removeResolver(IProxyResolver* resolver);

private:
    static std::vector<IProxyResolver*>& resolvers();
};

}  // namespace emf::sphinx::ecore::proxymanagement
