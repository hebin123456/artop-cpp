// EMF XSD: XSDFacet 实现
#include "emf/xsd/XSDFacet.h"
#include "emf/xsd/XSDAnnotation.h"
#include "emf/xsd/XSDSimpleTypeDefinition.h"

namespace emf::xsd {

std::string XSDFacet::getFacetName() const {
    if (auto* c = eClass()) return c->getName();
    return "";
}

std::any XSDFacet::getEffectiveValue() const {
    return std::any{lexicalValue_};
}

void XSDFacet::setAnnotation(XSDAnnotation* a) {
    annotation_ = a;
    if (a) {
        if (auto* obj = dynamic_cast<emf::common::EObject*>(a)) {
            obj->setEContainer(this);
        }
    }
}

XSDSimpleTypeDefinition* XSDFacet::getSimpleTypeDefinition() const {
    if (auto* c = dynamic_cast<emf::common::EObject*>(const_cast<XSDFacet*>(this))) {
        return dynamic_cast<XSDSimpleTypeDefinition*>(c->eContainer());
    }
    return nullptr;
}

}  // namespace emf::xsd
