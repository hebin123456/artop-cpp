// EMF XSD: XSDBoundedFacet 实现
#include "emf/xsd/XSDBoundedFacet.h"
#include "emf/xsd/XSDPackage.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/ecore/EcoreMetadata.h"

namespace emf::xsd {

emf::ecore::EClass* XSDBoundedFacet::eClass() const {
    return XSDPackage::instance().getEClass_XSDBoundedFacet();
}

}  // namespace emf::xsd
