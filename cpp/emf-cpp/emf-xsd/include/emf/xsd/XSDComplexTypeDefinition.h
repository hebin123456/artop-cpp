// EMF XSD: XSDComplexTypeDefinition
// 对齐 Java: org.eclipse.xsd.XSDComplexTypeDefinition
#pragma once

#include "emf/xsd/XSDConcreteComponent.h"
#include "emf/xsd/XSDTypeDefinition.h"
#include "emf/common/EList.h"
#include <string>
#include <vector>

namespace emf::xsd {

class XSDAttributeDeclaration;
class XSDAttributeUse;
class XSDWildcard;
class XSDModelGroup;
class XSDParticle;

// 复杂类型 contentType 枚举
enum class XSDContentType {
    EMPTY,
    SIMPLE,
    ELEMENT_ONLY,
    MIXED,
    MIXED_CONTENT_SIMPLE
};

// 复杂类型定义
class XSDComplexTypeDefinition : virtual public XSDConcreteComponent, virtual public XSDTypeDefinition {
public:
    XSDComplexTypeDefinition() = default;
    ~XSDComplexTypeDefinition() override = default;

    virtual XSDContentType getContentType() const { return contentType_; }
    virtual void setContentType(XSDContentType c) { contentType_ = c; }

    virtual emf::common::EList<XSDAttributeDeclaration*>& getAttributeUses() { return attributeUses_; }
    virtual const emf::common::EList<XSDAttributeDeclaration*>& getAttributeUses() const { return attributeUses_; }
    void addAttributeUse(XSDAttributeDeclaration* a);

    virtual XSDWildcard* getAttributeWildcard() const { return attributeWildcard_; }
    virtual void setAttributeWildcard(XSDWildcard* w);

    virtual XSDModelGroup* getParticle() const { return particle_; }
    virtual void setParticle(XSDModelGroup* m);

    // 重写：复杂类型可以独立设置 abstract
    bool isAbstract() const override { return XSDTypeDefinition::isAbstract(); }
    void setAbstract(bool b) override { XSDTypeDefinition::setAbstract(b); }

    virtual bool isMixed() const { return mixed_; }
    virtual void setMixed(bool m) { mixed_ = m; }

    emf::ecore::EClass* eClass() const override;
    std::any eGet(const emf::ecore::EStructuralFeature* feature) const override;
    void eSet(const emf::ecore::EStructuralFeature* feature, std::any value) override;
    bool eIsSet(const emf::ecore::EStructuralFeature* feature) const override;
    void eUnset(const emf::ecore::EStructuralFeature* feature) override;
    std::vector<emf::common::EObject*> eContents() const override;

private:
    XSDContentType contentType_ = XSDContentType::ELEMENT_ONLY;
    emf::common::EList<XSDAttributeDeclaration*> attributeUses_;
    XSDWildcard* attributeWildcard_ = nullptr;
    XSDModelGroup* particle_ = nullptr;
    bool mixed_ = false;
};

}  // namespace emf::xsd
