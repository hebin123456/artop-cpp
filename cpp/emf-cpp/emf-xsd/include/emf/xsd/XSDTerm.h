// EMF XSD: XSDTerm 基类
#pragma once

#include "emf/xsd/XSDComponent.h"

namespace emf::xsd {

// 项的抽象基类（XSDModelGroup / XSDElementDeclaration / XSDWildcard 等均继承之）
// 不再声明 eClass() pure virtual：具体实现由 XSDConcreteComponent 继承的
// EObjectImpl 给出；具体子类继续 override 返回各自的 EClass。
class XSDTerm : virtual public XSDComponent {
public:
    XSDTerm() = default;
    ~XSDTerm() override = default;
};

}  // namespace emf::xsd
