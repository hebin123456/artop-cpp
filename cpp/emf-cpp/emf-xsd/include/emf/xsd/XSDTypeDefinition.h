// EMF XSD: XSDTypeDefinition 基接口
// 对齐 Java: org.eclipse.xsd.XSDTypeDefinition
#pragma once

#include "emf/xsd/XSDComponent.h"
#include <string>

namespace emf::xsd {

class XSDTypeDefinition;

// 类型定义基接口
class XSDTypeDefinition : virtual public XSDComponent {
public:
    XSDTypeDefinition() = default;
    ~XSDTypeDefinition() override = default;

    virtual XSDTypeDefinition* getBaseType() const { return baseType_; }
    virtual void setBaseType(XSDTypeDefinition* base) { baseType_ = base; }

    virtual const std::string& getName() const { return name_; }
    virtual void setName(const std::string& n) { name_ = n; }

    virtual bool isAbstract() const { return abstract_; }
    virtual void setAbstract(bool b) { abstract_ = b; }

    // 命名空间/名称 形式
    virtual std::string getQName() const;

    virtual const std::string& getLexicalValue() const { return lexicalValue_; }
    virtual void setLexicalValue(const std::string& v) { lexicalValue_ = v; }

    // 不再声明 eClass() pure virtual：具体 XSDComplexTypeDefinition /
    // XSDSimpleTypeDefinition override 即可；XSDTypeDefinition 自身作为接口
    // 仍是抽象（没有 eClass() / eGet() 等具体实现），但 concrete 子类通过
    // 继承 XSDConcreteComponent 获得 EObjectImpl 默认实现。

protected:
    XSDTypeDefinition* baseType_ = nullptr;
    std::string name_;
    std::string lexicalValue_;
    bool abstract_ = false;
};

}  // namespace emf::xsd
