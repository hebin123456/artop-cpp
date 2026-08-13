// EMF XSD: XSDFundamentalFacet
// 对齐 Java: org.eclipse.xsd.XSDFundamentalFacet
#pragma once

#include "emf/xsd/XSDFacet.h"

namespace emf::xsd {

// Fundamental Facet（abstract）: ordered / bounded / cardinality / numeric
// 自身不增加新方法，仅作为分类根
class XSDFundamentalFacet : public XSDFacet {
public:
    XSDFundamentalFacet() = default;
    ~XSDFundamentalFacet() override = default;
};

}  // namespace emf::xsd
