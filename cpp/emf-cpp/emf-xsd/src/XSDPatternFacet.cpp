// EMF XSD: XSDPatternFacet 实现
#include "emf/xsd/XSDPatternFacet.h"
#include "emf/xsd/XSDPackage.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/ecore/EcoreMetadata.h"

namespace emf::xsd {

emf::ecore::EClass* XSDPatternFacet::eClass() const {
    return XSDPackage::instance().getEClass_XSDPatternFacet();
}

}  // namespace emf::xsd
