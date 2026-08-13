// EMF XSD: XSDFractionDigitsFacet 实现
#include "emf/xsd/XSDFractionDigitsFacet.h"
#include "emf/xsd/XSDPackage.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/ecore/EcoreMetadata.h"

namespace emf::xsd {

emf::ecore::EClass* XSDFractionDigitsFacet::eClass() const {
    return XSDPackage::instance().getEClass_XSDFractionDigitsFacet();
}

std::any XSDFractionDigitsFacet::eGet(const emf::ecore::EStructuralFeature* feature) const {
    auto* ef = asEStructuralFeature(feature);
    if (!ef) return std::any{};
    const std::string& n = ef->getName();
    if (n == "value") return std::any{value_};
    if (n == "lexicalValue") return std::any{lexicalValue_};
    if (n == "fixed") return std::any{isFixed()};
    return std::any{};
}

void XSDFractionDigitsFacet::eSet(const emf::ecore::EStructuralFeature* feature, std::any value) {
    auto* ef = asEStructuralFeature(feature);
    if (!ef) return;
    const std::string& n = ef->getName();
    if (n == "value" && value.type() == typeid(int)) {
        value_ = std::any_cast<int>(value);
    } else if (n == "lexicalValue" && value.type() == typeid(std::string)) {
        lexicalValue_ = std::any_cast<std::string>(value);
    } else if (n == "fixed" && value.type() == typeid(bool)) {
        setFixed(std::any_cast<bool>(value));
    }
}

bool XSDFractionDigitsFacet::eIsSet(const emf::ecore::EStructuralFeature* feature) const {
    auto* ef = asEStructuralFeature(feature);
    if (!ef) return false;
    const std::string& n = ef->getName();
    if (n == "value") return value_ >= 0;
    if (n == "lexicalValue") return !lexicalValue_.empty();
    if (n == "fixed") return isSetFixed();
    return false;
}

void XSDFractionDigitsFacet::eUnset(const emf::ecore::EStructuralFeature* feature) {
    auto* ef = asEStructuralFeature(feature);
    if (!ef) return;
    const std::string& n = ef->getName();
    if (n == "value") value_ = -1;
    else if (n == "lexicalValue") lexicalValue_.clear();
    else if (n == "fixed") unsetFixed();
}

}  // namespace emf::xsd
