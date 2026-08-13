// EMF XSD: XSDElementDeclaration
// 对齐 Java: org.eclipse.xsd.XSDElementDeclaration
#pragma once

#include "emf/xsd/XSDConcreteComponent.h"
#include "emf/xsd/XSDTerm.h"
#include <string>
#include <vector>

namespace emf::xsd {

class XSDTypeDefinition;
class XSDSchema;

// 元素声明 scope 枚举
enum class XSDScope {
    ABSENT,
    GLOBAL,
    LOCAL
};

// 元素声明
class XSDElementDeclaration : virtual public XSDConcreteComponent, virtual public XSDTerm {
public:
    XSDElementDeclaration() = default;
    ~XSDElementDeclaration() override = default;

    virtual const std::string& getName() const { return name_; }
    virtual void setName(const std::string& n) { name_ = n; }

    virtual const std::string& getTargetNamespace() const { return targetNamespace_; }
    virtual void setTargetNamespace(const std::string& ns) { targetNamespace_ = ns; }

    virtual XSDTypeDefinition* getTypeDefinition() const { return typeDefinition_; }
    virtual void setTypeDefinition(XSDTypeDefinition* t);

    virtual std::string getQName() const;

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
    std::string targetNamespace_;
    XSDTypeDefinition* typeDefinition_ = nullptr;
    XSDScope scope_ = XSDScope::ABSENT;
};

}  // namespace emf::xsd
