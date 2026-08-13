// EMF XSD: XSDAttributeGroupDefinition
// 对齐 Java: org.eclipse.xsd.XSDAttributeGroupDefinition
#pragma once

#include "emf/xsd/XSDConcreteComponent.h"
#include "emf/common/EList.h"
#include <string>
#include <vector>

namespace emf::xsd {

class XSDAttributeDeclaration;
class XSDWildcard;

// 属性组定义
class XSDAttributeGroupDefinition : public XSDConcreteComponent {
public:
    XSDAttributeGroupDefinition() = default;
    ~XSDAttributeGroupDefinition() override = default;

    virtual const std::string& getName() const { return name_; }
    virtual void setName(const std::string& n) { name_ = n; }

    virtual emf::common::EList<XSDAttributeDeclaration*>& getAttributeUses() { return attributeUses_; }
    virtual const emf::common::EList<XSDAttributeDeclaration*>& getAttributeUses() const { return attributeUses_; }
    void addAttributeUse(XSDAttributeDeclaration* a);

    virtual XSDWildcard* getAttributeWildcard() const { return attributeWildcard_; }
    virtual void setAttributeWildcard(XSDWildcard* w);

    emf::ecore::EClass* eClass() const override;
    std::any eGet(const emf::ecore::EStructuralFeature* feature) const override;
    void eSet(const emf::ecore::EStructuralFeature* feature, std::any value) override;
    bool eIsSet(const emf::ecore::EStructuralFeature* feature) const override;
    void eUnset(const emf::ecore::EStructuralFeature* feature) override;
    std::vector<emf::common::EObject*> eContents() const override;

private:
    std::string name_;
    emf::common::EList<XSDAttributeDeclaration*> attributeUses_;
    XSDWildcard* attributeWildcard_ = nullptr;
};

}  // namespace emf::xsd
