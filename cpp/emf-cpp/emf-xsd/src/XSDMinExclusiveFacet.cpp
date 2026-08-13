// EMF XSD: XSDMinExclusiveFacet 实现
#include "emf/xsd/XSDMinExclusiveFacet.h"
#include "emf/xsd/XSDPackage.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/ecore/EcoreMetadata.h"

namespace emf::xsd {

emf::ecore::EClass* XSDMinExclusiveFacet::eClass() const {
    return XSDPackage::instance().getEClass_XSDMinExclusiveFacet();
}

}  // namespace emf::xsd
