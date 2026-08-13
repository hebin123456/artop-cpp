// EMF XSD: XSDWhiteSpaceFacet
// 对齐 Java: org.eclipse.xsd.XSDWhiteSpaceFacet
#pragma once

#include "emf/xsd/XSDFixedFacet.h"
#include "emf/xsd/XSDWhiteSpace.h"
#include <string>

namespace emf::xsd {

// White Space Facet
class XSDWhiteSpaceFacet : public XSDFixedFacet {
public:
    XSDWhiteSpaceFacet() = default;
    ~XSDWhiteSpaceFacet() override = default;

    // 简化：用字符串表示 white space（"preserve"|"replace"|"collapse"）
    virtual std::string getValueString() const { return value_; }
    virtual void setValueString(const std::string& v) { value_ = v; }

    // Java 中返回 XSDWhiteSpace 类型；这里返回 XSDWhiteSpace 拷贝
    XSDWhiteSpace getValue() const;
    void setValue(const XSDWhiteSpace& v);

    // 将字面量按 white space 规则规范化
    std::string getNormalizedLiteral(const std::string& literal) const;

    emf::ecore::EClass* eClass() const override;

private:
    std::string value_ = "preserve";
};

}  // namespace emf::xsd
