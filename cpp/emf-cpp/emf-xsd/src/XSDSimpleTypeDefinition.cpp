// EMF XSD: XSDSimpleTypeDefinition 实现
#include "emf/xsd/XSDSimpleTypeDefinition.h"
#include "emf/xsd/XSDPackage.h"
#include "emf/xsd/XSDTypeDefinition.h"
#include "emf/xsd/XSDConstrainingFacet.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/ecore/EcoreMetadata.h"

namespace emf::xsd {

void XSDSimpleTypeDefinition::addMemberTypeDefinition(XSDTypeDefinition* t) {
    if (t) {
        t->setEContainer(this);
        memberTypeDefinitions_.add(t);
    }
}

void XSDSimpleTypeDefinition::addFacet(XSDConstrainingFacet* f) {
    if (f) {
        if (auto* obj = dynamic_cast<emf::common::EObject*>(f)) {
            obj->setEContainer(this);
        }
        facets_.add(f);
    }
}

void XSDSimpleTypeDefinition::addFacet(emf::common::EObject* f) {
    if (auto* cf = dynamic_cast<XSDConstrainingFacet*>(f)) {
        addFacet(cf);
    } else if (f) {
        // 兜底：dynamic_cast 失败，添加为 raw pointer（通过 reinterpret_cast 转换）
        f->setEContainer(this);
        facets_.add(reinterpret_cast<XSDConstrainingFacet*>(f));
    }
}

emf::ecore::EClass* XSDSimpleTypeDefinition::eClass() const {
    return XSDPackage::instance().getEClass_XSDSimpleTypeDefinition();
}

std::any XSDSimpleTypeDefinition::eGet(const emf::ecore::EStructuralFeature* feature) const {
    auto* ef = asEStructuralFeature(feature);
    if (!ef) return std::any{};
    const std::string& n = ef->getName();
    if (n == "name") return std::any{name_};
    if (n == "baseTypeDefinition") return std::any{baseType_};
    if (n == "lexicalPattern") return std::any{lexicalPattern_};
    if (n == "facets") {
        return std::any{std::reference_wrapper<emf::common::EList<XSDConstrainingFacet*>>(
            const_cast<emf::common::EList<XSDConstrainingFacet*>&>(facets_))};
    }
    return std::any{};
}

void XSDSimpleTypeDefinition::eSet(const emf::ecore::EStructuralFeature* feature, std::any value) {
    auto* ef = asEStructuralFeature(feature);
    if (!ef) return;
    const std::string& n = ef->getName();
    if (n == "name" && value.type() == typeid(std::string)) {
        name_ = std::any_cast<std::string>(value);
    } else if (n == "baseTypeDefinition" && value.type() == typeid(emf::common::EObject*)) {
        baseType_ = dynamic_cast<XSDTypeDefinition*>(std::any_cast<emf::common::EObject*>(value));
    } else if (n == "lexicalPattern" && value.type() == typeid(std::string)) {
        lexicalPattern_ = std::any_cast<std::string>(value);
    } else if (n == "facets") {
        if (value.type() == typeid(std::reference_wrapper<emf::common::EList<XSDConstrainingFacet*>>)) {
            const auto& src = std::any_cast<std::reference_wrapper<emf::common::EList<XSDConstrainingFacet*>>>(value).get();
            facets_.clear();
            for (size_t i = 0; i < src.size(); ++i) {
                addFacet(src.get(i));
            }
        }
    }
}

bool XSDSimpleTypeDefinition::eIsSet(const emf::ecore::EStructuralFeature* feature) const {
    auto* ef = asEStructuralFeature(feature);
    if (!ef) return false;
    const std::string& n = ef->getName();
    if (n == "name") return !name_.empty();
    if (n == "baseTypeDefinition") return baseType_ != nullptr;
    if (n == "lexicalPattern") return !lexicalPattern_.empty();
    if (n == "facets") return !facets_.empty();
    return false;
}

void XSDSimpleTypeDefinition::eUnset(const emf::ecore::EStructuralFeature* feature) {
    auto* ef = asEStructuralFeature(feature);
    if (!ef) return;
    const std::string& n = ef->getName();
    if (n == "name") name_.clear();
    else if (n == "baseTypeDefinition") baseType_ = nullptr;
    else if (n == "lexicalPattern") lexicalPattern_.clear();
    else if (n == "facets") facets_.clear();
}

std::vector<emf::common::EObject*> XSDSimpleTypeDefinition::eContents() const {
    std::vector<emf::common::EObject*> r;
    for (size_t i = 0; i < memberTypeDefinitions_.size(); ++i) {
        if (auto* o = memberTypeDefinitions_.get(i)) r.push_back(o);
    }
    if (itemTypeDefinition_) r.push_back(itemTypeDefinition_);
    if (primitiveTypeDefinition_) r.push_back(primitiveTypeDefinition_);
    for (size_t i = 0; i < facets_.size(); ++i) {
        if (auto* o = dynamic_cast<emf::common::EObject*>(facets_.get(i))) r.push_back(o);
    }
    return r;
}

}  // namespace emf::xsd
