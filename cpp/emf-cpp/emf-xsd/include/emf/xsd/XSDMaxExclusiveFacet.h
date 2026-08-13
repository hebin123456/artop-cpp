// EMF XSD: XSDMaxExclusiveFacet
// 对齐 Java: org.eclipse.xsd.XSDMaxExclusiveFacet
#pragma once

#include "emf/xsd/XSDMaxFacet.h"

namespace emf::xsd {

// Max Exclusive Facet
class XSDMaxExclusiveFacet : public XSDMaxFacet {
public:
    XSDMaxExclusiveFacet() = default;
    ~XSDMaxExclusiveFacet() override = default;

    emf::ecore::EClass* eClass() const override;
};

}  // namespace emf::xsd
