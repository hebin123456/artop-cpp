// EMF XSD: XSDAttributeGroupDefinition 实现
#include "emf/xsd/XSDAttributeGroupDefinition.h"
#include "emf/xsd/XSDAttributeDeclaration.h"
#include "emf/xsd/XSDWildcard.h"
#include "emf/xsd/XSDPackage.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/ecore/EcoreMetadata.h"

namespace emf::xsd {

void XSDAttributeGroupDefinition::addAttributeUse(XSDAttributeDeclaration* a) {
    if (a) {
        a->setEContainer(this);
        attributeUses_.add(a);
    }
}

void XSDAttributeGroupDefinition::setAttributeWildcard(XSDWildcard* w) {
    attributeWildcard_ = w;
    if (w) w->setEContainer(this);
}

emf::ecore::EClass* XSDAttributeGroupDefinition::eClass() const {
    return XSDPackage::instance().getEClass_XSDAttributeGroupDefinition();
}

std::any XSDAttributeGroupDefinition::eGet(const emf::ecore::EStructuralFeature* feature) const {
    auto* ef = asEStructuralFeature(feature);
    if (!ef) return std::any{};
    const std::string& n = ef->getName();
    if (n == "name") return std::any{name_};
    if (n == "attributeUses") {
        return std::any{reinterpret_cast<const emf::common::EList<XSDAttributeDeclaration*>&>(attributeUses_)};
    }
    if (n == "attributeWildcard") return std::any{static_cast<emf::common::EObject*>(attributeWildcard_)};
    return std::any{};
}

void XSDAttributeGroupDefinition::eSet(const emf::ecore::EStructuralFeature* feature, std::any value) {
    auto* ef = asEStructuralFeature(feature);
    if (!ef) return;
    const std::string& n = ef->getName();
    if (n == "name" && value.type() == typeid(std::string)) {
        name_ = std::any_cast<std::string>(value);
    } else if (n == "attributeWildcard" && value.type() == typeid(emf::common::EObject*)) {
        setAttributeWildcard(dynamic_cast<XSDWildcard*>(std::any_cast<emf::common::EObject*>(value)));
    }
}

bool XSDAttributeGroupDefinition::eIsSet(const emf::ecore::EStructuralFeature* feature) const {
    auto* ef = asEStructuralFeature(feature);
    if (!ef) return false;
    const std::string& n = ef->getName();
    if (n == "name") return !name_.empty();
    if (n == "attributeUses") return !attributeUses_.empty();
    if (n == "attributeWildcard") return attributeWildcard_ != nullptr;
    return false;
}

void XSDAttributeGroupDefinition::eUnset(const emf::ecore::EStructuralFeature* feature) {
    auto* ef = asEStructuralFeature(feature);
    if (!ef) return;
    const std::string& n = ef->getName();
    if (n == "name") name_.clear();
    else if (n == "attributeUses") attributeUses_.clear();
    else if (n == "attributeWildcard") attributeWildcard_ = nullptr;
}

std::vector<emf::common::EObject*> XSDAttributeGroupDefinition::eContents() const {
    std::vector<emf::common::EObject*> r;
    for (size_t i = 0; i < attributeUses_.size(); ++i) {
        if (auto* o = attributeUses_.get(i)) r.push_back(o);
    }
    if (attributeWildcard_) r.push_back(attributeWildcard_);
    return r;
}

}  // namespace emf::xsd
