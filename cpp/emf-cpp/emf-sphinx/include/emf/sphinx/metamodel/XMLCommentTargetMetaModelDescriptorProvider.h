// XMLCommentTargetMetaModelDescriptorProvider.h
// 对齐 Java org.eclipse.sphinx.emf.metamodel.XMLCommentTargetMetaModelDescriptorProvider
#pragma once

#include "emf/sphinx/metamodel/ITargetMetaModelDescriptorProvider.h"
#include <string>

namespace emf::sphinx::metamodel {

class XMLCommentTargetMetaModelDescriptorProvider : public ITargetMetaModelDescriptorProvider {
public:
    static XMLCommentTargetMetaModelDescriptorProvider& instance() {
        static XMLCommentTargetMetaModelDescriptorProvider inst;
        return inst;
    }

    // 从 XML 注释解析 target descriptor
    IMetaModelDescriptor* getTargetMetaModelDescriptor(const std::string& content) const override;

private:
    XMLCommentTargetMetaModelDescriptorProvider() = default;
};

}  // namespace emf::sphinx::metamodel
