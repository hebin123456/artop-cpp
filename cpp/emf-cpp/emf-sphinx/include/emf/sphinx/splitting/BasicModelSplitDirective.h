// BasicModelSplitDirective.h
// 对齐 Java org.eclipse.sphinx.emf.splitting.BasicModelSplitDirective
#pragma once

#include "emf/sphinx/splitting/IModelSplitDirective.h"
#include "emf/common/URI.h"
#include "emf/common/EObject.h"
#include <string>

namespace emf::sphinx::splitting {

class BasicModelSplitDirective : public IModelSplitDirective {
public:
    BasicModelSplitDirective() = default;
    BasicModelSplitDirective(emf::common::EObject* root, const emf::common::URI& uri, const std::string& contentType)
        : root_(root), uri_(uri), ct_(contentType) {}

    emf::common::EObject* getRoot() const override { return root_; }
    emf::common::URI getTargetURI() const override { return uri_; }
    std::string getTargetContentType() const override { return ct_; }

private:
    emf::common::EObject* root_ = nullptr;
    emf::common::URI uri_;
    std::string ct_;
};

}  // namespace emf::sphinx::splitting
