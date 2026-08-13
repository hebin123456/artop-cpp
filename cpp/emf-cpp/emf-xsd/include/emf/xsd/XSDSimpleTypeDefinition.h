// EMF XSD: XSDSimpleTypeDefinition
// 对齐 Java: org.eclipse.xsd.XSDSimpleTypeDefinition
#pragma once

#include "emf/xsd/XSDConcreteComponent.h"
#include "emf/xsd/XSDTypeDefinition.h"
#include "emf/common/EList.h"
#include <string>
#include <vector>

namespace emf::xsd {

class XSDConstrainingFacet;

// 简单类型 variety 枚举
enum class XSDVariety {
    ABSENT,
    ATOMIC,
    LIST,
    UNION
};

// 简单类型定义
class XSDSimpleTypeDefinition : virtual public XSDConcreteComponent, virtual public XSDTypeDefinition {
public:
    XSDSimpleTypeDefinition() = default;
    ~XSDSimpleTypeDefinition() override = default;

    virtual XSDVariety getVariety() const { return variety_; }
    virtual void setVariety(XSDVariety v) { variety_ = v; }

    virtual emf::common::EList<XSDTypeDefinition*>& getMemberTypeDefinitions() { return memberTypeDefinitions_; }
    virtual const emf::common::EList<XSDTypeDefinition*>& getMemberTypeDefinitions() const { return memberTypeDefinitions_; }
    void addMemberTypeDefinition(XSDTypeDefinition* t);

    virtual XSDTypeDefinition* getItemTypeDefinition() const { return itemTypeDefinition_; }
    virtual void setItemTypeDefinition(XSDTypeDefinition* t) { itemTypeDefinition_ = t; }

    virtual XSDTypeDefinition* getPrimitiveTypeDefinition() const { return primitiveTypeDefinition_; }
    virtual void setPrimitiveTypeDefinition(XSDTypeDefinition* t) { primitiveTypeDefinition_ = t; }

    virtual const std::string& getLexicalPattern() const { return lexicalPattern_; }
    virtual void setLexicalPattern(const std::string& p) { lexicalPattern_ = p; }

    // ===== Facet 集合（对齐 Java XSDSimpleTypeDefinition#getFacets）=====
    // Java 类型：EList<XSDConstrainingFacet>
    virtual emf::common::EList<XSDConstrainingFacet*>& getFacets() { return facets_; }
    virtual const emf::common::EList<XSDConstrainingFacet*>& getFacets() const { return facets_; }
    void addFacet(XSDConstrainingFacet* f);
    void addFacet(emf::common::EObject* f);

    emf::ecore::EClass* eClass() const override;
    std::any eGet(const emf::ecore::EStructuralFeature* feature) const override;
    void eSet(const emf::ecore::EStructuralFeature* feature, std::any value) override;
    bool eIsSet(const emf::ecore::EStructuralFeature* feature) const override;
    void eUnset(const emf::ecore::EStructuralFeature* feature) override;
    std::vector<emf::common::EObject*> eContents() const override;

private:
    XSDVariety variety_ = XSDVariety::ABSENT;
    emf::common::EList<XSDTypeDefinition*> memberTypeDefinitions_;
    XSDTypeDefinition* itemTypeDefinition_ = nullptr;
    XSDTypeDefinition* primitiveTypeDefinition_ = nullptr;
    std::string lexicalPattern_;
    emf::common::EList<XSDConstrainingFacet*> facets_;
};

}  // namespace emf::xsd
