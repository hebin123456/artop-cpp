// EMF XSD: XSDModelGroup
// 对齐 Java: org.eclipse.xsd.XSDModelGroup
#pragma once

#include "emf/xsd/XSDConcreteComponent.h"
#include "emf/xsd/XSDTerm.h"
#include "emf/common/EList.h"
#include <string>
#include <vector>

namespace emf::xsd {

class XSDParticle;

// 模型组 compositor 枚举
enum class XSDCompositor {
    ALL,
    CHOICE,
    SEQUENCE
};

// 模型组（sequence/choice/all）
class XSDModelGroup : virtual public XSDConcreteComponent, virtual public XSDTerm {
public:
    XSDModelGroup() = default;
    ~XSDModelGroup() override = default;

    virtual XSDCompositor getCompositor() const { return compositor_; }
    virtual void setCompositor(XSDCompositor c) { compositor_ = c; }

    virtual emf::common::EList<XSDParticle*>& getParticles() { return particles_; }
    virtual const emf::common::EList<XSDParticle*>& getParticles() const { return particles_; }
    void addParticle(XSDParticle* p);

    emf::ecore::EClass* eClass() const override;
    std::any eGet(const emf::ecore::EStructuralFeature* feature) const override;
    void eSet(const emf::ecore::EStructuralFeature* feature, std::any value) override;
    bool eIsSet(const emf::ecore::EStructuralFeature* feature) const override;
    void eUnset(const emf::ecore::EStructuralFeature* feature) override;
    std::vector<emf::common::EObject*> eContents() const override;

private:
    XSDCompositor compositor_ = XSDCompositor::SEQUENCE;
    emf::common::EList<XSDParticle*> particles_;
};

}  // namespace emf::xsd
