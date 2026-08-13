// EMF XSD: XSDInclude
// 对齐 Java: org.eclipse.xsd.XSDInclude
// XSDInclude 有两个 schema 引用：
//   - resolvedSchema_ (XSDInclude 私有)：被 resolved 后的 XSDSchema
//   - incorporatedSchema_ (继承自 XSDSchemaCompositor)：被 include 后的"已合并"schema
// Java 端这两个字段语义不同，C++ 端保留两者。
#pragma once

#include "emf/xsd/XSDSchemaCompositor.h"
#include <string>
#include <vector>

namespace emf::xsd {

class XSDSchema;

// include 指令（继承 XSDSchemaCompositor，对齐 Java）
class XSDInclude : public XSDSchemaCompositor {
public:
    XSDInclude() = default;
    ~XSDInclude() override = default;

    virtual const std::string& getSchemaLocation() const { return schemaLocation_; }
    virtual void setSchemaLocation(const std::string& loc) { schemaLocation_ = loc; }

    virtual XSDSchema* getResolvedSchema() const { return resolvedSchema_; }
    virtual void setResolvedSchema(XSDSchema* s) { resolvedSchema_ = s; }

    emf::ecore::EClass* eClass() const override;
    std::any eGet(const emf::ecore::EStructuralFeature* feature) const override;
    void eSet(const emf::ecore::EStructuralFeature* feature, std::any value) override;
    bool eIsSet(const emf::ecore::EStructuralFeature* feature) const override;
    void eUnset(const emf::ecore::EStructuralFeature* feature) override;
    std::vector<emf::common::EObject*> eContents() const override;

private:
    std::string schemaLocation_;
    XSDSchema* resolvedSchema_ = nullptr;
};

}  // namespace emf::xsd
