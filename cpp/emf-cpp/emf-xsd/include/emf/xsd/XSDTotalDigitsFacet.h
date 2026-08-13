// EMF XSD: XSDTotalDigitsFacet
// 对齐 Java: org.eclipse.xsd.XSDTotalDigitsFacet
#pragma once

#include "emf/xsd/XSDFixedFacet.h"

namespace emf::xsd {

// Total Digits Facet
class XSDTotalDigitsFacet : public XSDFixedFacet {
public:
    XSDTotalDigitsFacet() = default;
    ~XSDTotalDigitsFacet() override = default;

    virtual int getValue() const { return value_; }
    virtual void setValue(int v) { value_ = v; }

    emf::ecore::EClass* eClass() const override;
    std::any eGet(const emf::ecore::EStructuralFeature* feature) const override;
    void eSet(const emf::ecore::EStructuralFeature* feature, std::any value) override;
    bool eIsSet(const emf::ecore::EStructuralFeature* feature) const override;
    void eUnset(const emf::ecore::EStructuralFeature* feature) override;

private:
    int value_ = -1;
};

}  // namespace emf::xsd
