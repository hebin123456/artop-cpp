// EMF XSD: XSDMinInclusiveFacet 实现
#include "emf/xsd/XSDMinInclusiveFacet.h"
#include "emf/xsd/XSDPackage.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/ecore/EcoreMetadata.h"

namespace emf::xsd {

emf::ecore::EClass* XSDMinInclusiveFacet::eClass() const {
    return XSDPackage::instance().getEClass_XSDMinInclusiveFacet();
}

}  // namespace emf::xsd
