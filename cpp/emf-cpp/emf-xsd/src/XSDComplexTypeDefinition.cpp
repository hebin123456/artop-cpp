// EMF XSD: XSDComplexTypeDefinition 实现
#include "emf/xsd/XSDComplexTypeDefinition.h"
#include "emf/xsd/XSDAttributeDeclaration.h"
#include "emf/xsd/XSDWildcard.h"
#include "emf/xsd/XSDModelGroup.h"
#include "emf/xsd/XSDPackage.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/ecore/EcoreMetadata.h"

namespace emf::xsd {

void XSDComplexTypeDefinition::addAttributeUse(XSDAttributeDeclaration* a) {
    if (a) {
        a->setEContainer(this);
        attributeUses_.add(a);
    }
}

void XSDComplexTypeDefinition::setParticle(XSDModelGroup* m) {
    particle_ = m;
    if (m) m->setEContainer(this);
}

void XSDComplexTypeDefinition::setAttributeWildcard(XSDWildcard* w) {
    attributeWildcard_ = w;
    if (w) w->setEContainer(this);
}

emf::ecore::EClass* XSDComplexTypeDefinition::eClass() const {
    return XSDPackage::instance().getEClass_XSDComplexTypeDefinition();
}

std::any XSDComplexTypeDefinition::eGet(const emf::ecore::EStructuralFeature* feature) const {
    auto* ef = asEStructuralFeature(feature);
    if (!ef) return std::any{};
    const std::string& n = ef->getName();
    if (n == "name") return std::any{name_};
    if (n == "abstract") return std::any{abstract_};
    if (n == "mixed") return std::any{mixed_};
    if (n == "content") return std::any{static_cast<emf::common::EObject*>(particle_)};
    if (n == "attributeDeclarations" || n == "attributeUses") {
        return std::any{reinterpret_cast<const emf::common::EList<XSDAttributeDeclaration*>&>(attributeUses_)};
    }
    if (n == "attributeWildcard") return std::any{static_cast<emf::common::EObject*>(attributeWildcard_)};
    return std::any{};
}

void XSDComplexTypeDefinition::eSet(const emf::ecore::EStructuralFeature* feature, std::any value) {
    auto* ef = asEStructuralFeature(feature);
    if (!ef) return;
    const std::string& n = ef->getName();
    if (n == "name" && value.type() == typeid(std::string)) {
        name_ = std::any_cast<std::string>(value);
    } else if (n == "abstract" && value.type() == typeid(bool)) {
        abstract_ = std::any_cast<bool>(value);
    } else if (n == "mixed" && value.type() == typeid(bool)) {
        mixed_ = std::any_cast<bool>(value);
    } else if (n == "content" && value.type() == typeid(emf::common::EObject*)) {
        setParticle(dynamic_cast<XSDModelGroup*>(std::any_cast<emf::common::EObject*>(value)));
    } else if (n == "attributeWildcard" && value.type() == typeid(emf::common::EObject*)) {
        setAttributeWildcard(dynamic_cast<XSDWildcard*>(std::any_cast<emf::common::EObject*>(value)));
    }
}

bool XSDComplexTypeDefinition::eIsSet(const emf::ecore::EStructuralFeature* feature) const {
    auto* ef = asEStructuralFeature(feature);
    if (!ef) return false;
    const std::string& n = ef->getName();
    if (n == "name") return !name_.empty();
    if (n == "abstract") return abstract_;
    if (n == "mixed") return mixed_;
    if (n == "content") return particle_ != nullptr;
    if (n == "attributeDeclarations" || n == "attributeUses") return !attributeUses_.empty();
    if (n == "attributeWildcard") return attributeWildcard_ != nullptr;
    return false;
}

void XSDComplexTypeDefinition::eUnset(const emf::ecore::EStructuralFeature* feature) {
    auto* ef = asEStructuralFeature(feature);
    if (!ef) return;
    const std::string& n = ef->getName();
    if (n == "name") name_.clear();
    else if (n == "abstract") abstract_ = false;
    else if (n == "mixed") mixed_ = false;
    else if (n == "content") particle_ = nullptr;
    else if (n == "attributeDeclarations" || n == "attributeUses") attributeUses_.clear();
    else if (n == "attributeWildcard") attributeWildcard_ = nullptr;
}

std::vector<emf::common::EObject*> XSDComplexTypeDefinition::eContents() const {
    std::vector<emf::common::EObject*> r;
    for (size_t i = 0; i < attributeUses_.size(); ++i) {
        if (auto* o = attributeUses_.get(i)) r.push_back(o);
    }
    if (attributeWildcard_) r.push_back(attributeWildcard_);
    if (particle_) r.push_back(particle_);
    return r;
}

}  // namespace emf::xsd
