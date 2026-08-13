// EMF XSD: XSDBoundedFacet
// 对齐 Java: org.eclipse.xsd.XSDBoundedFacet
#pragma once

#include "emf/xsd/XSDFundamentalFacet.h"

namespace emf::xsd {

// Bounded Facet: 是否有序
class XSDBoundedFacet : public XSDFundamentalFacet {
public:
    XSDBoundedFacet() = default;
    ~XSDBoundedFacet() override = default;

    virtual bool isValue() const { return value_; }
    virtual void setValue(bool v) { value_ = v; }

    emf::ecore::EClass* eClass() const override;

private:
    bool value_ = false;
};

}  // namespace emf::xsd
