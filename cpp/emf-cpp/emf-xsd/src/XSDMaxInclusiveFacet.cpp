// EMF XSD: XSDMaxInclusiveFacet 实现
#include "emf/xsd/XSDMaxInclusiveFacet.h"
#include "emf/xsd/XSDPackage.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/ecore/EcoreMetadata.h"

namespace emf::xsd {

emf::ecore::EClass* XSDMaxInclusiveFacet::eClass() const {
    return XSDPackage::instance().getEClass_XSDMaxInclusiveFacet();
}

}  // namespace emf::xsd
