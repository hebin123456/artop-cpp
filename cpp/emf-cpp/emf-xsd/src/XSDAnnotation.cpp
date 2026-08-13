// EMF XSD: XSDAnnotation 实现
#include "emf/xsd/XSDAnnotation.h"
#include "emf/xsd/XSDPackage.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/ecore/EcoreMetadata.h"

namespace emf::xsd {

void XSDAnnotation::addAppInfo(XSDAnnotation* ann) {
    if (ann) {
        ann->setEContainer(this);
        applicationInformation_.add(ann);
    }
}

emf::ecore::EClass* XSDAnnotation::eClass() const {
    return XSDPackage::instance().getEClass_XSDAnnotation();
}

std::any XSDAnnotation::eGet(const emf::ecore::EStructuralFeature* feature) const {
    auto* ef = asEStructuralFeature(feature);
    if (!ef) return std::any{};
    const std::string& n = ef->getName();
    if (n == "userInformation") return std::any{userInformation_};
    if (n == "applicationInformation") {
        return std::any{reinterpret_cast<const emf::common::EList<XSDAnnotation*>&>(applicationInformation_)};
    }
    return std::any{};
}

void XSDAnnotation::eSet(const emf::ecore::EStructuralFeature* feature, std::any value) {
    auto* ef = asEStructuralFeature(feature);
    if (!ef) return;
    const std::string& n = ef->getName();
    if (n == "userInformation" && value.type() == typeid(std::string)) {
        userInformation_ = std::any_cast<std::string>(value);
    }
}

bool XSDAnnotation::eIsSet(const emf::ecore::EStructuralFeature* feature) const {
    auto* ef = asEStructuralFeature(feature);
    if (!ef) return false;
    const std::string& n = ef->getName();
    if (n == "userInformation") return !userInformation_.empty();
    if (n == "applicationInformation") return !applicationInformation_.empty();
    return false;
}

void XSDAnnotation::eUnset(const emf::ecore::EStructuralFeature* feature) {
    auto* ef = asEStructuralFeature(feature);
    if (!ef) return;
    const std::string& n = ef->getName();
    if (n == "userInformation") userInformation_.clear();
    else if (n == "applicationInformation") applicationInformation_.clear();
}

std::vector<emf::common::EObject*> XSDAnnotation::eContents() const {
    std::vector<emf::common::EObject*> r;
    for (size_t i = 0; i < applicationInformation_.size(); ++i) {
        if (auto* o = applicationInformation_.get(i)) r.push_back(o);
    }
    return r;
}

}  // namespace emf::xsd
