// EMF XSD: XSDCardinalityFacet
// 对齐 Java: org.eclipse.xsd.XSDCardinalityFacet
#pragma once

#include "emf/xsd/XSDFundamentalFacet.h"
#include <string>

namespace emf::xsd {

// Cardinality Facet: 基数（"finite" | "countably infinite" | ...）
class XSDCardinalityFacet : public XSDFundamentalFacet {
public:
    XSDCardinalityFacet() = default;
    ~XSDCardinalityFacet() override = default;

    virtual std::string getValue() const { return value_; }
    virtual void setValue(const std::string& v) { value_ = v; }

    emf::ecore::EClass* eClass() const override;

private:
    std::string value_;
};

}  // namespace emf::xsd
