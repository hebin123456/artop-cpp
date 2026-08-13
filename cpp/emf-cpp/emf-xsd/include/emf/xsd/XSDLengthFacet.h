// EMF XSD: XSDLengthFacet
// 对齐 Java: org.eclipse.xsd.XSDLengthFacet
#pragma once

#include "emf/xsd/XSDFixedFacet.h"

namespace emf::xsd {

// Length Facet: 字符串长度
class XSDLengthFacet : public XSDFixedFacet {
public:
    XSDLengthFacet() = default;
    ~XSDLengthFacet() override = default;

    virtual int getValue() const { return value_; }
    virtual void setValue(int v) { value_ = v; }

    emf::ecore::EClass* eClass() const override;
    std::any eGet(const emf::ecore::EStructuralFeature* feature) const override;
    void eSet(const emf::ecore::EStructuralFeature* feature, std::any value) override;
    bool eIsSet(const emf::ecore::EStructuralFeature* feature) const override;
    void eUnset(const emf::ecore::EStructuralFeature* feature) override;

private:
    int value_ = -1;  // -1 表示未设置
};

}  // namespace emf::xsd
