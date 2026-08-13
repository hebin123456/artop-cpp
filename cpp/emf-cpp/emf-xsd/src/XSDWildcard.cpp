// EMF XSD: XSDWildcard 实现
#include "emf/xsd/XSDWildcard.h"
#include "emf/xsd/XSDPackage.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/ecore/EcoreMetadata.h"

namespace emf::xsd {

void XSDWildcard::addNamespaceConstraint(const std::string& ns) {
    namespaceConstraint_.insert(ns);
}

void XSDWildcard::setNamespaceConstraint(std::unordered_set<std::string> ns) {
    namespaceConstraint_ = std::move(ns);
}

emf::ecore::EClass* XSDWildcard::eClass() const {
    return XSDPackage::instance().getEClass_XSDWildcard();
}

std::any XSDWildcard::eGet(const emf::ecore::EStructuralFeature* feature) const {
    auto* ef = asEStructuralFeature(feature);
    if (!ef) return std::any{};
    const std::string& n = ef->getName();
    if (n == "namespaceConstraint") {
        // 序列化为字符串 "ns1,ns2,ns3"
        std::string s;
        for (const auto& ns : namespaceConstraint_) {
            if (!s.empty()) s += ",";
            s += ns;
        }
        return std::any{s};
    }
    if (n == "processContents") return std::any{processContents_};
    return std::any{};
}

void XSDWildcard::eSet(const emf::ecore::EStructuralFeature* feature, std::any value) {
    auto* ef = asEStructuralFeature(feature);
    if (!ef) return;
    const std::string& n = ef->getName();
    if (n == "namespaceConstraint" && value.type() == typeid(std::string)) {
        namespaceConstraint_.clear();
        const auto& s = std::any_cast<std::string>(value);
        size_t start = 0, pos;
        while ((pos = s.find(',', start)) != std::string::npos) {
            namespaceConstraint_.insert(s.substr(start, pos - start));
            start = pos + 1;
        }
        if (start < s.size()) namespaceConstraint_.insert(s.substr(start));
    } else if (n == "processContents" && value.type() == typeid(std::string)) {
        processContents_ = std::any_cast<std::string>(value);
    }
}

bool XSDWildcard::eIsSet(const emf::ecore::EStructuralFeature* feature) const {
    auto* ef = asEStructuralFeature(feature);
    if (!ef) return false;
    const std::string& n = ef->getName();
    if (n == "namespaceConstraint") return !namespaceConstraint_.empty();
    if (n == "processContents") return !processContents_.empty();
    return false;
}

void XSDWildcard::eUnset(const emf::ecore::EStructuralFeature* feature) {
    auto* ef = asEStructuralFeature(feature);
    if (!ef) return;
    const std::string& n = ef->getName();
    if (n == "namespaceConstraint") namespaceConstraint_.clear();
    else if (n == "processContents") processContents_.clear();
}

std::vector<emf::common::EObject*> XSDWildcard::eContents() const {
    return {};
}

}  // namespace emf::xsd
