// XMLResourceProblemMarkerFactory.h
// 对齐 Java org.eclipse.sphinx.emf.resource.XMLResourceProblemMarkerFactory
#pragma once

#include "emf/sphinx/resource/IResourceProblemMarkerFactory.h"
#include "emf/sphinx/resource/IXMLMarker.h"

namespace emf::sphinx::resource {

class XMLResourceProblemMarkerFactory : public IResourceProblemMarkerFactory {
public:
    XMLResourceProblemMarkerFactory() = default;
    ~XMLResourceProblemMarkerFactory() override = default;

    // 创建 IXMLMarker
    virtual IXMLMarker* createXMLMarker(const std::string& type, const std::string& msg, int line, int col) = 0;
};

}  // namespace emf::sphinx::resource
