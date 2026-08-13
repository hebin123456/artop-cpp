// DefaultResourceScope.h
// 对齐 Java org.eclipse.sphinx.emf.scoping.DefaultResourceScope
// 默认实现：所有 URI 都属于 scope
#pragma once

#include "emf/sphinx/scoping/AbstractResourceScope.h"

namespace emf::sphinx::scoping {

class DefaultResourceScope : public AbstractResourceScope {
public:
    DefaultResourceScope() = default;
    ~DefaultResourceScope() override = default;

    emf::common::URI getRootURI() const override { return root_; }
    void setRootURI(const emf::common::URI& u) { root_ = u; }
    std::vector<emf::common::URI> getReferencedRootURIs() const override { return refRoots_; }
    void setReferencedRootURIs(const std::vector<emf::common::URI>& v) { refRoots_ = v; }
    std::vector<emf::common::URI> getReferencingRootURIs() const override { return refingRoots_; }
    void setReferencingRootURIs(const std::vector<emf::common::URI>& v) { refingRoots_ = v; }

    bool belongsTo(const emf::common::URI& uri, bool includeReferencedScopes) const override;
    bool belongsTo(emf::common::Resource* res, bool includeReferencedScopes) const override;
    std::vector<emf::common::URI> getPersistedFiles(bool includeReferencedScopes) const override;
    bool didBelongTo(const emf::common::URI& uri, bool includeReferencedScopes) const override { return belongsTo(uri, includeReferencedScopes); }
    bool didBelongTo(emf::common::Resource* res, bool includeReferencedScopes) const override { return belongsTo(res, includeReferencedScopes); }
    bool isShared(const emf::common::URI& uri) const override;
    bool isShared(emf::common::Resource* res) const override;

private:
    emf::common::URI root_;
    std::vector<emf::common::URI> refRoots_;
    std::vector<emf::common::URI> refingRoots_;
};

}  // namespace emf::sphinx::scoping
