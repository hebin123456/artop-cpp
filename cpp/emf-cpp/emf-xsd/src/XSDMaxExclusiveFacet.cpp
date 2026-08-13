// EMF XSD: XSDMaxExclusiveFacet 实现
#include "emf/xsd/XSDMaxExclusiveFacet.h"
#include "emf/xsd/XSDPackage.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/ecore/EcoreMetadata.h"

namespace emf::xsd {

emf::ecore::EClass* XSDMaxExclusiveFacet::eClass() const {
    return XSDPackage::instance().getEClass_XSDMaxExclusiveFacet();
}

}  // namespace emf::xsd
