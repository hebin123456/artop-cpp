// EMF XSD: XSDSchemaCompositor 抽象基类
// 对齐 Java: org.eclipse.xsd.XSDSchemaCompositor
// 是 XSDInclude / XSDImport / XSDRedefine 的共同基类。
// XSDSchema.incorporate() 对其调用 setIncorporatedSchema(...)。
#pragma once

#include "emf/xsd/XSDConcreteComponent.h"
#include <string>
#include <vector>

namespace emf::xsd {

class XSDSchema;

// XSDSchemaCompositor 抽象：XSDInclude / XSDImport / XSDRedefine 的共同接口
class XSDSchemaCompositor : public XSDConcreteComponent {
public:
    XSDSchemaCompositor() = default;
    ~XSDSchemaCompositor() override = default;

    // 该 composor 所属的"redefining schema"（拥有此 composor 的 schema）
    virtual XSDSchema* getSchema() const { return schema_; }
    virtual void setSchema(XSDSchema* s) { schema_ = s; }

    // 该 composor 包含的 schema（被 include/import/redefine 进来的 schema）
    virtual XSDSchema* getIncorporatedSchema() const { return incorporatedSchema_; }
    virtual void setIncorporatedSchema(XSDSchema* s) { incorporatedSchema_ = s; }

private:
    XSDSchema* schema_ = nullptr;
    XSDSchema* incorporatedSchema_ = nullptr;
};

}  // namespace emf::xsd
