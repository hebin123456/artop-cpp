// EMF XSD: XSDAnnotation
// 对齐 Java: org.eclipse.xsd.XSDAnnotation
#pragma once

#include "emf/xsd/XSDConcreteComponent.h"
#include "emf/common/EList.h"
#include <string>
#include <vector>

namespace emf::xsd {

// XSD 注解（documentation + appInfo）
class XSDAnnotation : public XSDConcreteComponent {
public:
    XSDAnnotation() = default;
    ~XSDAnnotation() override = default;

    virtual const std::string& getUserInformation() const { return userInformation_; }
    virtual void setUserInformation(const std::string& u) { userInformation_ = u; }

    virtual emf::common::EList<XSDAnnotation*>& getApplicationInformation() { return applicationInformation_; }
    virtual const emf::common::EList<XSDAnnotation*>& getApplicationInformation() const { return applicationInformation_; }
    void addAppInfo(XSDAnnotation* ann);

    emf::ecore::EClass* eClass() const override;
    std::any eGet(const emf::ecore::EStructuralFeature* feature) const override;
    void eSet(const emf::ecore::EStructuralFeature* feature, std::any value) override;
    bool eIsSet(const emf::ecore::EStructuralFeature* feature) const override;
    void eUnset(const emf::ecore::EStructuralFeature* feature) override;
    std::vector<emf::common::EObject*> eContents() const override;

private:
    std::string userInformation_;
    emf::common::EList<XSDAnnotation*> applicationInformation_;
};

}  // namespace emf::xsd
