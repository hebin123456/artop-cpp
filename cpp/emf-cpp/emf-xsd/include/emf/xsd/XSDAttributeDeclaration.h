// EMF XSD: XSDAttributeDeclaration
// 对齐 Java: org.eclipse.xsd.XSDAttributeDeclaration
#pragma once

#include "emf/xsd/XSDConcreteComponent.h"
#include "XSDElementDeclaration.h"  // for XSDScope
#include <string>
#include <vector>

namespace emf::xsd {

class XSDTypeDefinition;

// 属性声明
class XSDAttributeDeclaration : public XSDConcreteComponent {
public:
    XSDAttributeDeclaration() = default;
    ~XSDAttributeDeclaration() override = default;

    virtual const std::string& getName() const { return name_; }
    virtual void setName(const std::string& n) { name_ = n; }

    virtual XSDTypeDefinition* getTypeDefinition() const { return typeDefinition_; }
    virtual void setTypeDefinition(XSDTypeDefinition* t);

    virtual XSDScope getScope() const { return scope_; }
    virtual void setScope(XSDScope s) { scope_ = s; }

    emf::ecore::EClass* eClass() const override;
    std::any eGet(const emf::ecore::EStructuralFeature* feature) const override;
    void eSet(const emf::ecore::EStructuralFeature* feature, std::any value) override;
    bool eIsSet(const emf::ecore::EStructuralFeature* feature) const override;
    void eUnset(const emf::ecore::EStructuralFeature* feature) override;
    std::vector<emf::common::EObject*> eContents() const override;

private:
    std::string name_;
    XSDTypeDefinition* typeDefinition_ = nullptr;
    XSDScope scope_ = XSDScope::ABSENT;
};

}  // namespace emf::xsd
