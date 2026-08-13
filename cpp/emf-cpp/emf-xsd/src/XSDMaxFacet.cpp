// EMF XSD: XSDMaxFacet 实现
#include "emf/xsd/XSDMaxFacet.h"
#include "emf/xsd/XSDPackage.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/ecore/EcoreMetadata.h"

namespace emf::xsd {

bool XSDMaxFacet::isInclusive() const {
    if (auto* c = eClass()) {
        return c == XSDPackage::instance().getEClass_XSDMaxInclusiveFacet();
    }
    return false;
}

bool XSDMaxFacet::isExclusive() const {
    if (auto* c = eClass()) {
        return c == XSDPackage::instance().getEClass_XSDMaxExclusiveFacet();
    }
    return false;
}

}  // namespace emf::xsd
