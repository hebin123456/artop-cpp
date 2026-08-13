// EMF XSD: XSDParticle 实现
#include "emf/xsd/XSDParticle.h"
#include "emf/xsd/XSDTerm.h"
#include "emf/xsd/XSDPackage.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/ecore/EcoreMetadata.h"

namespace emf::xsd {

void XSDParticle::setTerm(XSDTerm* t) {
    term_ = t;
    if (t) {
        if (auto* obj = dynamic_cast<emf::common::EObject*>(t)) {
            obj->setEContainer(this);
        }
    }
}

emf::ecore::EClass* XSDParticle::eClass() const {
    return XSDPackage::instance().getEClass_XSDParticle();
}

std::any XSDParticle::eGet(const emf::ecore::EStructuralFeature* feature) const {
    auto* ef = asEStructuralFeature(feature);
    if (!ef) return std::any{};
    const std::string& n = ef->getName();
    if (n == "minOccurs") return std::any{minOccurs_};
    if (n == "maxOccurs") return std::any{maxOccurs_};
    if (n == "content") return std::any{static_cast<emf::common::EObject*>(term_)};
    return std::any{};
}

void XSDParticle::eSet(const emf::ecore::EStructuralFeature* feature, std::any value) {
    auto* ef = asEStructuralFeature(feature);
    if (!ef) return;
    const std::string& n = ef->getName();
    if (n == "minOccurs" && value.type() == typeid(int)) {
        minOccurs_ = std::any_cast<int>(value);
    } else if (n == "minOccurs" && value.type() == typeid(std::string)) {
        const auto& s = std::any_cast<std::string>(value);
        if (s == "unbounded") minOccurs_ = -1;
        else try { minOccurs_ = std::stoi(s); } catch (...) { minOccurs_ = 1; }
    } else if (n == "maxOccurs" && value.type() == typeid(int)) {
        maxOccurs_ = std::any_cast<int>(value);
    } else if (n == "maxOccurs" && value.type() == typeid(std::string)) {
        const auto& s = std::any_cast<std::string>(value);
        if (s == "unbounded") maxOccurs_ = -1;
        else try { maxOccurs_ = std::stoi(s); } catch (...) { maxOccurs_ = 1; }
    } else if (n == "content" && value.type() == typeid(emf::common::EObject*)) {
        setTerm(dynamic_cast<XSDTerm*>(std::any_cast<emf::common::EObject*>(value)));
    }
}

bool XSDParticle::eIsSet(const emf::ecore::EStructuralFeature* feature) const {
    auto* ef = asEStructuralFeature(feature);
    if (!ef) return false;
    const std::string& n = ef->getName();
    if (n == "minOccurs") return minOccurs_ != 1;
    if (n == "maxOccurs") return maxOccurs_ != 1;
    if (n == "content") return term_ != nullptr;
    return false;
}

void XSDParticle::eUnset(const emf::ecore::EStructuralFeature* feature) {
    auto* ef = asEStructuralFeature(feature);
    if (!ef) return;
    const std::string& n = ef->getName();
    if (n == "minOccurs") minOccurs_ = 1;
    else if (n == "maxOccurs") maxOccurs_ = 1;
    else if (n == "content") term_ = nullptr;
}

std::vector<emf::common::EObject*> XSDParticle::eContents() const {
    std::vector<emf::common::EObject*> r;
    if (term_) r.push_back(term_);
    return r;
}

}  // namespace emf::xsd
