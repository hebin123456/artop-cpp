// ProjectResourceScope.h
// 对齐 Java org.eclipse.sphinx.emf.scoping.ProjectResourceScope
// 项目级 scope：包含 project 目录下所有相关文件
#pragma once

#include "emf/sphinx/scoping/AbstractResourceScope.h"

namespace emf::sphinx::scoping {

class ProjectResourceScope : public AbstractResourceScope {
public:
    ProjectResourceScope() = default;
    explicit ProjectResourceScope(const emf::common::URI& projectUri) : projectUri_(projectUri) {}
    ~ProjectResourceScope() override = default;

    emf::common::URI getRootURI() const override { return projectUri_; }
    std::vector<emf::common::URI> getReferencedRootURIs() const override { return referenced_; }
    void setReferencedRootURIs(const std::vector<emf::common::URI>& v) { referenced_ = v; }
    std::vector<emf::common::URI> getReferencingRootURIs() const override { return referencing_; }
    void setReferencingRootURIs(const std::vector<emf::common::URI>& v) { referencing_ = v; }

    bool belongsTo(const emf::common::URI& uri, bool includeReferencedScopes) const override;
    bool belongsTo(emf::common::Resource* res, bool includeReferencedScopes) const override;
    std::vector<emf::common::URI> getPersistedFiles(bool includeReferencedScopes) const override;
    bool didBelongTo(const emf::common::URI& uri, bool b) const override { return belongsTo(uri, b); }
    bool didBelongTo(emf::common::Resource* res, bool b) const override { return belongsTo(res, b); }
    bool isShared(const emf::common::URI&) const override { return false; }
    bool isShared(emf::common::Resource*) const override { return false; }

private:
    emf::common::URI projectUri_;
    std::vector<emf::common::URI> referenced_;
    std::vector<emf::common::URI> referencing_;
};

}  // namespace emf::sphinx::scoping
