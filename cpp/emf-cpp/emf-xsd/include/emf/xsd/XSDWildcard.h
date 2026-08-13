// EMF XSD: XSDWildcard
// 对齐 Java: org.eclipse.xsd.XSDWildcard
#pragma once

#include "emf/xsd/XSDConcreteComponent.h"
#include "emf/xsd/XSDTerm.h"
#include <string>
#include <unordered_set>
#include <vector>

namespace emf::xsd {

// 通配符（any / anyAttribute）
class XSDWildcard : virtual public XSDConcreteComponent, virtual public XSDTerm {
public:
    XSDWildcard() = default;
    ~XSDWildcard() override = default;

    // 命名空间约束
    virtual const std::unordered_set<std::string>& getNamespaceConstraint() const { return namespaceConstraint_; }
    virtual std::unordered_set<std::string>& getNamespaceConstraint() { return namespaceConstraint_; }
    void addNamespaceConstraint(const std::string& ns);
    void setNamespaceConstraint(std::unordered_set<std::string> ns);

    // 命名空间约束类别
    virtual const std::string& getNamespaceConstraintCategory() const { return namespaceConstraintCategory_; }
    virtual void setNamespaceConstraintCategory(const std::string& c) { namespaceConstraintCategory_ = c; }

    // processContents
    virtual const std::string& getProcessContents() const { return processContents_; }
    virtual void setProcessContents(const std::string& p) { processContents_ = p; }

    emf::ecore::EClass* eClass() const override;
    std::any eGet(const emf::ecore::EStructuralFeature* feature) const override;
    void eSet(const emf::ecore::EStructuralFeature* feature, std::any value) override;
    bool eIsSet(const emf::ecore::EStructuralFeature* feature) const override;
    void eUnset(const emf::ecore::EStructuralFeature* feature) override;
    std::vector<emf::common::EObject*> eContents() const override;

private:
    std::unordered_set<std::string> namespaceConstraint_;
    std::string namespaceConstraintCategory_;
    std::string processContents_ = "strict";
};

}  // namespace emf::xsd
