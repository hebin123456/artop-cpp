// EMF XSD: XSDCardinalityFacet 实现
#include "emf/xsd/XSDCardinalityFacet.h"
#include "emf/xsd/XSDPackage.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/ecore/EcoreMetadata.h"

namespace emf::xsd {

emf::ecore::EClass* XSDCardinalityFacet::eClass() const {
    return XSDPackage::instance().getEClass_XSDCardinalityFacet();
}

}  // namespace emf::xsd
