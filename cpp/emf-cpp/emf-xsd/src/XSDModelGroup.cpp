// EMF XSD: XSDModelGroup 实现
#include "emf/xsd/XSDModelGroup.h"
#include "emf/xsd/XSDParticle.h"
#include "emf/xsd/XSDPackage.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/ecore/EcoreMetadata.h"

namespace emf::xsd {

void XSDModelGroup::addParticle(XSDParticle* p) {
    if (p) {
        p->setEContainer(this);
        particles_.add(p);
    }
}

emf::ecore::EClass* XSDModelGroup::eClass() const {
    return XSDPackage::instance().getEClass_XSDModelGroup();
}

std::any XSDModelGroup::eGet(const emf::ecore::EStructuralFeature* feature) const {
    auto* ef = asEStructuralFeature(feature);
    if (!ef) return std::any{};
    const std::string& n = ef->getName();
    if (n == "compositor") return std::any{static_cast<int>(compositor_)};
    if (n == "particles") {
        return std::any{reinterpret_cast<const emf::common::EList<XSDParticle*>&>(particles_)};
    }
    return std::any{};
}

void XSDModelGroup::eSet(const emf::ecore::EStructuralFeature* feature, std::any value) {
    auto* ef = asEStructuralFeature(feature);
    if (!ef) return;
    const std::string& n = ef->getName();
    if (n == "compositor" && value.type() == typeid(int)) {
        compositor_ = static_cast<XSDCompositor>(std::any_cast<int>(value));
    } else if (n == "compositor" && value.type() == typeid(std::string)) {
        const auto& s = std::any_cast<std::string>(value);
        if (s == "all") compositor_ = XSDCompositor::ALL;
        else if (s == "choice") compositor_ = XSDCompositor::CHOICE;
        else compositor_ = XSDCompositor::SEQUENCE;
    }
}

bool XSDModelGroup::eIsSet(const emf::ecore::EStructuralFeature* feature) const {
    auto* ef = asEStructuralFeature(feature);
    if (!ef) return false;
    const std::string& n = ef->getName();
    if (n == "compositor") return true;
    if (n == "particles") return !particles_.empty();
    return false;
}

void XSDModelGroup::eUnset(const emf::ecore::EStructuralFeature* feature) {
    auto* ef = asEStructuralFeature(feature);
    if (!ef) return;
    const std::string& n = ef->getName();
    if (n == "compositor") compositor_ = XSDCompositor::SEQUENCE;
    else if (n == "particles") particles_.clear();
}

std::vector<emf::common::EObject*> XSDModelGroup::eContents() const {
    std::vector<emf::common::EObject*> r;
    for (size_t i = 0; i < particles_.size(); ++i) {
        if (auto* o = particles_.get(i)) r.push_back(o);
    }
    return r;
}

}  // namespace emf::xsd
