// EMF XSD: XSDMinFacet
// 对齐 Java: org.eclipse.xsd.XSDMinFacet
#pragma once

#include "emf/xsd/XSDFixedFacet.h"
#include <any>

namespace emf::xsd {

// Min Facet（abstract）: minExclusive / minInclusive 的共同父类
// 注意：Java 中此 facet 是 1.1 版新增
class XSDMinFacet : public XSDFixedFacet {
public:
    XSDMinFacet() = default;
    ~XSDMinFacet() override = default;

    virtual std::any getValue() const { return value_; }
    virtual void setValue(const std::any& v) { value_ = v; }

    // inclusive: 是否为 XSDMinInclusiveFacet
    virtual bool isInclusive() const;

    // exclusive: 是否为 XSDMinExclusiveFacet
    virtual bool isExclusive() const;

    emf::ecore::EClass* eClass() const override = 0;

private:
    std::any value_;
};

}  // namespace emf::xsd
