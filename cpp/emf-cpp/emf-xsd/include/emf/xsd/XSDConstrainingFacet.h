// EMF XSD: XSDConstrainingFacet
// 对齐 Java: org.eclipse.xsd.XSDConstrainingFacet
#pragma once

#include "emf/xsd/XSDFacet.h"

namespace emf::xsd {

// Constraining Facet（abstract）: 增加 isConstraintSatisfied
class XSDConstrainingFacet : public XSDFacet {
public:
    XSDConstrainingFacet() = default;
    ~XSDConstrainingFacet() override = default;

    // 给定值是否满足 facet 约束
    virtual bool isConstraintSatisfied(const std::any& value) const { (void)value; return true; }
};

}  // namespace emf::xsd
