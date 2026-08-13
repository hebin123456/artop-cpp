// EMF XSD: XSDPatternFacet
// 对齐 Java: org.eclipse.xsd.XSDPatternFacet
#pragma once

#include "emf/xsd/XSDRepeatableFacet.h"
#include "emf/common/EList.h"
#include <string>
#include <vector>

namespace emf::xsd {

// Pattern Facet: 正则表达式列表
class XSDPatternFacet : public XSDRepeatableFacet {
public:
    XSDPatternFacet() = default;
    ~XSDPatternFacet() override = default;

    // 多个 pattern 的字符串列表
    emf::common::EList<std::string>& getValue() { return value_; }
    const emf::common::EList<std::string>& getValue() const { return value_; }

    emf::ecore::EClass* eClass() const override;

private:
    emf::common::EList<std::string> value_;
};

}  // namespace emf::xsd
