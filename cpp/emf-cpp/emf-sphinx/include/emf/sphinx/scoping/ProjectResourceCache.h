// ProjectResourceCache.h
// 对齐 Java org.eclipse.sphinx.emf.scoping.ProjectResourceCache
// 项目级别 URI 缓存
#pragma once

#include "emf/common/URI.h"
#include <unordered_set>
#include <string>

namespace emf::sphinx::scoping {

class ProjectResourceCache {
public:
    static ProjectResourceCache& instance() {
        static ProjectResourceCache inst;
        return inst;
    }

    bool contains(const emf::common::URI& projectUri, const emf::common::URI& resUri) const;
    void add(const emf::common::URI& projectUri, const emf::common::URI& resUri);
    void remove(const emf::common::URI& projectUri, const emf::common::URI& resUri);
    void clear(const emf::common::URI& projectUri);

private:
    ProjectResourceCache() = default;
    std::string key(const emf::common::URI& projectUri, const emf::common::URI& resUri) const;
    std::unordered_set<std::string> entries_;
};

}  // namespace emf::sphinx::scoping
