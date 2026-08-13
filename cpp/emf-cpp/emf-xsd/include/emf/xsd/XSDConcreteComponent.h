// EMF XSD: XSDConcreteComponent 基类
// 对齐 Java: org.eclipse.xsd.XSDConcreteComponent
#pragma once

#include "emf/xsd/XSDComponent.h"
#include "emf/ecore/EcoreImpls.h"

namespace emf::xsd {

// 具体组件的基类
// 继承 EObjectImpl 拿到 EObject 全部 4 个虚函数（eGet/eSet/eIsSet/eUnset）
// 以及 eContents()、eIsProxy()、eClass() 的默认实现。各 XSD *Impl 子类
// 只需 override 各自关心的方法即可成为 concrete class。
class XSDConcreteComponent : virtual public XSDComponent, virtual public emf::ecore::EObjectImpl {
public:
    XSDConcreteComponent() = default;
    ~XSDConcreteComponent() override = default;

    // 对齐 Java: EObjectImpl.eInvoke 默认实现。
    std::any eInvoke(emf::ecore::EOperation* operation,
                     const std::vector<std::any>& arguments) override {
        if (!operation) return std::any{};
        return operation->invoke(this, arguments);
    }
};

}  // namespace emf::xsd
