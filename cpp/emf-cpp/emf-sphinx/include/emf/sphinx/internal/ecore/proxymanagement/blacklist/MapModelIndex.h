// MapModelIndex.h
// 对齐 Java org.eclipse.sphinx.emf.internal.ecore.proxymanagement.blacklist.MapModelIndex
// 用 hashmap 实现的 ModelIndex
#pragma once

#include "emf/sphinx/internal/ecore/proxymanagement/blacklist/ModelIndex.h"
#include <unordered_map>
#include <set>
#include <string>

namespace emf::sphinx::internal::ecore::proxymanagement::blacklist {

class MapModelIndex : public ModelIndex {
public:
    MapModelIndex() = default;
    ~MapModelIndex() override = default;

    void index(const emf::common::URI& resourceUri, const std::string& fragment) override;
    void unindex(const emf::common::URI& resourceUri, const std::string& fragment) override;
    std::vector<emf::common::URI> getCandidateResources(const std::string& fragment) const override;
    void addToBlackList(const emf::common::URI& resourceUri, const std::string& fragment) override;
    void removeFromBlackList(const emf::common::URI& resourceUri, const std::string& fragment) override;
    bool isBlackListed(const emf::common::URI& resourceUri, const std::string& fragment) const override;

private:
    // fragment -> set of resource URIs
    std::unordered_map<std::string, std::set<std::string>> fragmentToResources_;
    // (resourceUri, fragment) -> blacklisted
    std::set<std::pair<std::string, std::string>> blackList_;
};

}  // namespace emf::sphinx::internal::ecore::proxymanagement::blacklist
