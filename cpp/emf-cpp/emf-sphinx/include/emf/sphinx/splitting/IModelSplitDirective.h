// IModelSplitDirective.h
// 对齐 Java org.eclipse.sphinx.emf.splitting.IModelSplitDirective
// 拆分指令：描述如何将一个 root 拆到新的 resource
#pragma once

#include "emf/common/URI.h"
#include "emf/common/EObject.h"
#include <string>

namespace emf::sphinx::splitting {

class IModelSplitDirective {
public:
    virtual ~IModelSplitDirective() = default;
    virtual emf::common::EObject* getRoot() const = 0;
    virtual emf::common::URI getTargetURI() const = 0;
    virtual std::string getTargetContentType() const = 0;
};

}  // namespace emf::sphinx::splitting
