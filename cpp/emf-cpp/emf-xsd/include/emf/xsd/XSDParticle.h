// EMF XSD: XSDParticle
// 对齐 Java: org.eclipse.xsd.XSDParticle
#pragma once

#include "emf/xsd/XSDConcreteComponent.h"
#include <string>
#include <vector>

namespace emf::xsd {

class XSDTerm;

// 粒子（term + 出现次数）
class XSDParticle : public XSDConcreteComponent {
public:
    XSDParticle() = default;
    ~XSDParticle() override = default;

    virtual XSDTerm* getTerm() const { return term_; }
    virtual void setTerm(XSDTerm* t);

    virtual int getMinOccurs() const { return minOccurs_; }
    virtual void setMinOccurs(int v) { minOccurs_ = v; }

    virtual int getMaxOccurs() const { return maxOccurs_; }
    virtual void setMaxOccurs(int v) { maxOccurs_ = v; }

    emf::ecore::EClass* eClass() const override;
    std::any eGet(const emf::ecore::EStructuralFeature* feature) const override;
    void eSet(const emf::ecore::EStructuralFeature* feature, std::any value) override;
    bool eIsSet(const emf::ecore::EStructuralFeature* feature) const override;
    void eUnset(const emf::ecore::EStructuralFeature* feature) override;
    std::vector<emf::common::EObject*> eContents() const override;

private:
    XSDTerm* term_ = nullptr;
    int minOccurs_ = 1;
    int maxOccurs_ = 1;
};

}  // namespace emf::xsd
