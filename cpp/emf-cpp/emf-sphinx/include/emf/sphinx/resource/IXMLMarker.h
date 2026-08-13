// IXMLMarker.h
// 对齐 Java org.eclipse.sphinx.emf.resource.IXMLMarker
#pragma once

#include <string>

namespace emf::sphinx::resource {

class IXMLMarker {
public:
    virtual ~IXMLMarker() = default;

    // XML 问题类型
    static constexpr const char* XML_WELLFORMEDNESS_PROBLEM = "XML_WELLFORMEDNESS_PROBLEM";
    static constexpr const char* XML_VALIDITY_PROBLEM = "XML_VALIDITY_PROBLEM";

    virtual std::string getType() const = 0;
    virtual std::string getMessage() const = 0;
    virtual int getLine() const = 0;
    virtual int getColumn() const = 0;
};

}  // namespace emf::sphinx::resource
