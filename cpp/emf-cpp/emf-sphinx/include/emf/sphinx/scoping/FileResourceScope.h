// FileResourceScope.h
// 对齐 Java org.eclipse.sphinx.emf.scoping.FileResourceScope
// 单文件 scope
#pragma once

#include "emf/sphinx/scoping/AbstractResourceScope.h"

namespace emf::sphinx::scoping {

class FileResourceScope : public AbstractResourceScope {
public:
    FileResourceScope() = default;
    explicit FileResourceScope(const emf::common::URI& fileUri) : fileUri_(fileUri) {}
    ~FileResourceScope() override = default;

    emf::common::URI getRootURI() const override { return fileUri_; }
    std::vector<emf::common::URI> getReferencedRootURIs() const override { return {}; }
    std::vector<emf::common::URI> getReferencingRootURIs() const override { return {}; }
    bool belongsTo(const emf::common::URI& uri, bool) const override { return uri == fileUri_; }
    bool belongsTo(emf::common::Resource* res, bool) const override;
    std::vector<emf::common::URI> getPersistedFiles(bool) const override { return {fileUri_}; }
    bool didBelongTo(const emf::common::URI& uri, bool b) const override { return belongsTo(uri, b); }
    bool didBelongTo(emf::common::Resource* res, bool b) const override { return belongsTo(res, b); }
    bool isShared(const emf::common::URI&) const override { return false; }
    bool isShared(emf::common::Resource*) const override { return false; }

private:
    emf::common::URI fileUri_;
};

}  // namespace emf::sphinx::scoping
