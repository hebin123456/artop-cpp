// ModelIndex.h
// 对齐 Java org.eclipse.sphinx.emf.internal.ecore.proxymanagement.blacklist.ModelIndex
// 模型索引：跟踪每个 Resource 内已知的 object 路径
#pragma once

#include "emf/common/URI.h"
#include <string>
#include <unordered_map>
#include <unordered_set>
#include <vector>

namespace emf::common {
class EObject;
}

namespace emf::sphinx::internal::ecore::proxymanagement::blacklist {

class ModelIndex {
public:
    ModelIndex() = default;
    virtual ~ModelIndex() = default;

    // 索引一个 object 在其资源内的 fragment path
    virtual void index(const emf::common::URI& resourceUri, const std::string& fragment) = 0;
    virtual void unindex(const emf::common::URI& resourceUri, const std::string& fragment) = 0;

    // 解析 fragment → 资源 URI 集合（用于黑名单）
    virtual std::vector<emf::common::URI> getCandidateResources(const std::string& fragment) const = 0;

    // 添加/移除 fragment 黑名单
    virtual void addToBlackList(const emf::common::URI& resourceUri, const std::string& fragment) = 0;
    virtual void removeFromBlackList(const emf::common::URI& resourceUri, const std::string& fragment) = 0;
    virtual bool isBlackListed(const emf::common::URI& resourceUri, const std::string& fragment) const = 0;
};

}  // namespace emf::sphinx::internal::ecore::proxymanagement::blacklist
