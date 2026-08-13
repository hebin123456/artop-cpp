// EMF XSD: XSDNumericFacet 实现
#include "emf/xsd/XSDNumericFacet.h"
#include "emf/xsd/XSDPackage.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/ecore/EcoreMetadata.h"

namespace emf::xsd {

emf::ecore::EClass* XSDNumericFacet::eClass() const {
    return XSDPackage::instance().getEClass_XSDNumericFacet();
}

}  // namespace emf::xsd
