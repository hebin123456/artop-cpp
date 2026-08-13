// EMF XSD: XSDOrderedFacet
// 对齐 Java: org.eclipse.xsd.XSDOrderedFacet
#pragma once

#include "emf/xsd/XSDFundamentalFacet.h"
#include <string>

namespace emf::xsd {

// Ordered Facet: 值用 XSDOrdered 枚举表示
// (Java 中 XSDOrdered 是独立 Enumerator；这里简化为字符串名)
class XSDOrderedFacet : public XSDFundamentalFacet {
public:
    XSDOrderedFacet() = default;
    ~XSDOrderedFacet() override = default;

    virtual std::string getValue() const { return value_; }
    virtual void setValue(const std::string& v) { value_ = v; }

    emf::ecore::EClass* eClass() const override;

private:
    std::string value_;  // "false" | "partial" | "total"
};

}  // namespace emf::xsd
