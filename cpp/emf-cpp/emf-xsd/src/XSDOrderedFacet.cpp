// EMF XSD: XSDOrderedFacet 实现
#include "emf/xsd/XSDOrderedFacet.h"
#include "emf/xsd/XSDPackage.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/ecore/EcoreMetadata.h"

namespace emf::xsd {

emf::ecore::EClass* XSDOrderedFacet::eClass() const {
    return XSDPackage::instance().getEClass_XSDOrderedFacet();
}

}  // namespace emf::xsd
