// ExtendedSAXXMLHandler.h
// 对齐 Java org.eclipse.sphinx.emf.resource.ExtendedSAXXMLHandler
#pragma once

#include <string>
#include <vector>

namespace emf::sphinx::resource {

class ExtendedSAXXMLHandler {
public:
    ExtendedSAXXMLHandler() = default;
    virtual ~ExtendedSAXXMLHandler() = default;

    // 顶层注释
    std::vector<std::string> getTopLevelComments() const { return comments_; }
    void setTopLevelComments(const std::vector<std::string>& c) { comments_ = c; }

private:
    std::vector<std::string> comments_;
};

}  // namespace emf::sphinx::resource
