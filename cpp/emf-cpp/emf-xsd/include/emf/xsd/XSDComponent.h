// EMF XSD: XSDComponent 基接口
// 对齐 Java: org.eclipse.xsd.XSDComponent
#pragma once

#include "emf/common/EObject.h"

namespace emf::xsd {

class XSDSchema;
class XSDElementDeclaration;

// XSD 组件的基接口
// 对应 EMF 元模型里所有 XSD 节点类型的根
class XSDComponent : virtual public emf::common::EObject {
public:
    XSDComponent() = default;
    ~XSDComponent() override = default;

    virtual XSDSchema* getRootComponent() const { return rootComponent_; }
    virtual void setRootComponent(XSDSchema* schema) { rootComponent_ = schema; }

    virtual XSDElementDeclaration* getElement() const { return element_; }
    virtual void setElement(XSDElementDeclaration* elem) { element_ = elem; }

    // 不再声明 eClass() 为 pure virtual：由 EObjectImpl（XSDConcreteComponent
    // 继承）提供默认实现 eClass_ 字段；各 *Impl 子类再 override 返回具体 EClass。

protected:
    XSDSchema* rootComponent_ = nullptr;
    XSDElementDeclaration* element_ = nullptr;
};

}  // namespace emf::xsd
