// EMF XSD: XSDAttributeDeclaration 实现
#include "emf/xsd/XSDAttributeDeclaration.h"
#include "emf/xsd/XSDTypeDefinition.h"
#include "emf/xsd/XSDPackage.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/ecore/EcoreMetadata.h"

namespace emf::xsd {

void XSDAttributeDeclaration::setTypeDefinition(XSDTypeDefinition* t) {
    typeDefinition_ = t;
    if (t) t->setEContainer(this);
}

emf::ecore::EClass* XSDAttributeDeclaration::eClass() const {
    return XSDPackage::instance().getEClass_XSDAttributeDeclaration();
}

std::any XSDAttributeDeclaration::eGet(const emf::ecore::EStructuralFeature* feature) const {
    auto* ef = asEStructuralFeature(feature);
    if (!ef) return std::any{};
    const std::string& n = ef->getName();
    if (n == "name") return std::any{name_};
    if (n == "typeDefinition") return std::any{static_cast<emf::common::EObject*>(typeDefinition_)};
    return std::any{};
}

void XSDAttributeDeclaration::eSet(const emf::ecore::EStructuralFeature* feature, std::any value) {
    auto* ef = asEStructuralFeature(feature);
    if (!ef) return;
    const std::string& n = ef->getName();
    if (n == "name" && value.type() == typeid(std::string)) {
        name_ = std::any_cast<std::string>(value);
    } else if (n == "typeDefinition" && value.type() == typeid(emf::common::EObject*)) {
        setTypeDefinition(dynamic_cast<XSDTypeDefinition*>(std::any_cast<emf::common::EObject*>(value)));
    }
}

bool XSDAttributeDeclaration::eIsSet(const emf::ecore::EStructuralFeature* feature) const {
    auto* ef = asEStructuralFeature(feature);
    if (!ef) return false;
    const std::string& n = ef->getName();
    if (n == "name") return !name_.empty();
    if (n == "typeDefinition") return typeDefinition_ != nullptr;
    return false;
}

void XSDAttributeDeclaration::eUnset(const emf::ecore::EStructuralFeature* feature) {
    auto* ef = asEStructuralFeature(feature);
    if (!ef) return;
    const std::string& n = ef->getName();
    if (n == "name") name_.clear();
    else if (n == "typeDefinition") typeDefinition_ = nullptr;
}

std::vector<emf::common::EObject*> XSDAttributeDeclaration::eContents() const {
    std::vector<emf::common::EObject*> r;
    if (typeDefinition_) r.push_back(typeDefinition_);
    return r;
}

}  // namespace emf::xsd
