// ContextAwareProxyURIHelper.h
// 对齐 Java org.eclipse.sphinx.emf.resource.ContextAwareProxyURIHelper
// proxy URI 增强：保留跨文档引用的上下文信息
#pragma once

#include "emf/common/URI.h"
#include <string>

namespace emf::ecore {
class EObject;
}

namespace emf::sphinx::resource {

class ContextAwareProxyURIHelper {
public:
    static ContextAwareProxyURIHelper& instance() {
        static ContextAwareProxyURIHelper inst;
        return inst;
    }

    // 取得一个 proxy URI（可能带上下文）
    emf::common::URI getProxyURI(emf::ecore::EObject* proxy, emf::ecore::EObject* context);

    // 取得 HREF（trim 掉上下文）
    emf::common::URI getHREF(emf::ecore::EObject* proxy);

    // 给已有 proxy URI 加上上下文
    emf::common::URI augment(emf::common::URI& uri, emf::ecore::EObject* context);

    // 去掉上下文
    emf::common::URI trim(const emf::common::URI& uri);

private:
    ContextAwareProxyURIHelper() = default;
};

}  // namespace emf::sphinx::resource
