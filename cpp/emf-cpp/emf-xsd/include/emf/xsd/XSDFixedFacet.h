// EMF XSD: XSDFixedFacet
// 对齐 Java: org.eclipse.xsd.XSDFixedFacet
#pragma once

#include "emf/xsd/XSDConstrainingFacet.h"

namespace emf::xsd {

// Fixed Facet（abstract）: 携带 fixed 标志
// 多个具体 facet（length, min/maxLength, totalDigits, fractionDigits,
// whiteSpace, min/max Inclusive/Exclusive）继承此抽象
class XSDFixedFacet : public XSDConstrainingFacet {
public:
    XSDFixedFacet() = default;
    ~XSDFixedFacet() override = default;

    virtual bool isFixed() const { return fixedSet_ && fixed_; }
    virtual void setFixed(bool v) { fixed_ = v; fixedSet_ = true; }
    virtual void unsetFixed() { fixed_ = false; fixedSet_ = false; }
    virtual bool isSetFixed() const { return fixedSet_; }

    emf::ecore::EClass* eClass() const override = 0;

private:
    bool fixed_ = false;
    bool fixedSet_ = false;
};

}  // namespace emf::xsd
