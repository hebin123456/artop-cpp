// EMF XSD: XSDEnumerationFacet 实现
#include "emf/xsd/XSDEnumerationFacet.h"
#include "emf/xsd/XSDPackage.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/ecore/EcoreMetadata.h"

namespace emf::xsd {

emf::ecore::EClass* XSDEnumerationFacet::eClass() const {
    return XSDPackage::instance().getEClass_XSDEnumerationFacet();
}

}  // namespace emf::xsd
