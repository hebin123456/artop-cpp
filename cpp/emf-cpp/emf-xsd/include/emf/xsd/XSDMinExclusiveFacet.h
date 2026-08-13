// EMF XSD: XSDMinExclusiveFacet
// 对齐 Java: org.eclipse.xsd.XSDMinExclusiveFacet
#pragma once

#include "emf/xsd/XSDMinFacet.h"

namespace emf::xsd {

// Min Exclusive Facet
class XSDMinExclusiveFacet : public XSDMinFacet {
public:
    XSDMinExclusiveFacet() = default;
    ~XSDMinExclusiveFacet() override = default;

    emf::ecore::EClass* eClass() const override;
};

}  // namespace emf::xsd
