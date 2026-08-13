// EMF XSD: XSDMinFacet 实现
#include "emf/xsd/XSDMinFacet.h"
#include "emf/xsd/XSDPackage.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/ecore/EcoreMetadata.h"

namespace emf::xsd {

bool XSDMinFacet::isInclusive() const {
    if (auto* c = eClass()) {
        return c == XSDPackage::instance().getEClass_XSDMinInclusiveFacet();
    }
    return false;
}

bool XSDMinFacet::isExclusive() const {
    if (auto* c = eClass()) {
        return c == XSDPackage::instance().getEClass_XSDMinExclusiveFacet();
    }
    return false;
}

}  // namespace emf::xsd
