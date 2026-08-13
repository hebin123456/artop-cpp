// EMF XSD: XSDEnumerationFacet
// 对齐 Java: org.eclipse.xsd.XSDEnumerationFacet
#pragma once

#include "emf/xsd/XSDRepeatableFacet.h"
#include "emf/common/EList.h"
#include <string>

namespace emf::xsd {

// Enumeration Facet: 枚举值列表
// Java 端 EList<Object>；这里用 EList<std::string> 简化
class XSDEnumerationFacet : public XSDRepeatableFacet {
public:
    XSDEnumerationFacet() = default;
    ~XSDEnumerationFacet() override = default;

    emf::common::EList<std::string>& getValue() { return value_; }
    const emf::common::EList<std::string>& getValue() const { return value_; }

    emf::ecore::EClass* eClass() const override;

private:
    emf::common::EList<std::string> value_;
};

}  // namespace emf::xsd
