// EMF XSD: XSDMaxFacet
// 对齐 Java: org.eclipse.xsd.XSDMaxFacet
#pragma once

#include "emf/xsd/XSDFixedFacet.h"
#include <any>

namespace emf::xsd {

// Max Facet（abstract）: maxExclusive / maxInclusive 的共同父类
// 注意：Java 中此 facet 是 1.1 版新增
class XSDMaxFacet : public XSDFixedFacet {
public:
    XSDMaxFacet() = default;
    ~XSDMaxFacet() override = default;

    virtual std::any getValue() const { return value_; }
    virtual void setValue(const std::any& v) { value_ = v; }

    virtual bool isInclusive() const;
    virtual bool isExclusive() const;

    emf::ecore::EClass* eClass() const override = 0;

private:
    std::any value_;
};

}  // namespace emf::xsd
