// OldResourceProvider.h
// 对齐 Java org.eclipse.sphinx.emf.resource.OldResourceProvider
// 在文件被修改前缓存其内容（用于 reload 时找回旧引用）
#pragma once

#include "emf/common/Resource.h"
#include "emf/common/URI.h"
#include <any>
#include <string>
#include <vector>

namespace emf::sphinx::resource {

class OldResourceProvider {
public:
    OldResourceProvider() = default;
    virtual ~OldResourceProvider() = default;

    // 缓存一个 resource 的内容
    virtual void cacheOldContent(emf::common::Resource* res) = 0;

    // 取得旧内容
    virtual std::vector<emf::common::EObject*> getOldContents(emf::common::Resource* res) = 0;
    virtual std::string getOldURIFragment(emf::common::EObject* obj) = 0;
    virtual emf::common::EObject* getOldEObject(const emf::common::URI& uri) = 0;

    // 清除缓存
    virtual void clearCache(emf::common::Resource* res) = 0;
};

}  // namespace emf::sphinx::resource
