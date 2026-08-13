// EMF XSD: XSDMaxInclusiveFacet
// 对齐 Java: org.eclipse.xsd.XSDMaxInclusiveFacet
#pragma once

#include "emf/xsd/XSDMaxFacet.h"

namespace emf::xsd {

// Max Inclusive Facet
class XSDMaxInclusiveFacet : public XSDMaxFacet {
public:
    XSDMaxInclusiveFacet() = default;
    ~XSDMaxInclusiveFacet() override = default;

    emf::ecore::EClass* eClass() const override;
};

}  // namespace emf::xsd
