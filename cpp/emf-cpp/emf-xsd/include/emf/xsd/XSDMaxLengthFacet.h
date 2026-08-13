// EMF XSD: XSDMaxLengthFacet
// 对齐 Java: org.eclipse.xsd.XSDMaxLengthFacet
#pragma once

#include "emf/xsd/XSDFixedFacet.h"

namespace emf::xsd {

// Max Length Facet
class XSDMaxLengthFacet : public XSDFixedFacet {
public:
    XSDMaxLengthFacet() = default;
    ~XSDMaxLengthFacet() override = default;

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
