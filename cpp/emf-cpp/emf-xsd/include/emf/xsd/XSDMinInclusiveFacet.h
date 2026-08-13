// EMF XSD: XSDMinInclusiveFacet
// 对齐 Java: org.eclipse.xsd.XSDMinInclusiveFacet
#pragma once

#include "emf/xsd/XSDMinFacet.h"

namespace emf::xsd {

// Min Inclusive Facet
class XSDMinInclusiveFacet : public XSDMinFacet {
public:
    XSDMinInclusiveFacet() = default;
    ~XSDMinInclusiveFacet() override = default;

    emf::ecore::EClass* eClass() const override;
};

}  // namespace emf::xsd
