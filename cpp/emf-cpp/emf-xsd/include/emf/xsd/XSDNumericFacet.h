// EMF XSD: XSDNumericFacet
// 对齐 Java: org.eclipse.xsd.XSDNumericFacet
#pragma once

#include "emf/xsd/XSDFundamentalFacet.h"

namespace emf::xsd {

// Numeric Facet: 是否为数值
class XSDNumericFacet : public XSDFundamentalFacet {
public:
    XSDNumericFacet() = default;
    ~XSDNumericFacet() override = default;

    virtual bool isValue() const { return value_; }
    virtual void setValue(bool v) { value_ = v; }

    emf::ecore::EClass* eClass() const override;

private:
    bool value_ = false;
};

}  // namespace emf::xsd
