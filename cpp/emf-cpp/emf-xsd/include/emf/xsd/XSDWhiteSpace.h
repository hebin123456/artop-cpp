// EMF XSD: XSDWhiteSpace 枚举
// 对齐 Java: org.eclipse.xsd.XSDWhiteSpace
#pragma once

#include "emf/common/Enumerator.h"
#include <string>

namespace emf::xsd {

// White Space 取值：preserve / replace / collapse
// 对应 EAttribute 类型，Java 中是 EEnum；这里简化为 Enumerator 风格
class XSDWhiteSpace : public emf::ecore::Enumerator {
public:
    static const int PRESERVE = 0;
    static const int REPLACE = 1;
    static const int COLLAPSE = 2;

    static const XSDWhiteSpace PRESERVE_LITERAL;
    static const XSDWhiteSpace REPLACE_LITERAL;
    static const XSDWhiteSpace COLLAPSE_LITERAL;

    XSDWhiteSpace(int v, const std::string& n, const std::string& l)
        : emf::ecore::Enumerator(n, l, v) {}

    static XSDWhiteSpace get(int v);
    static XSDWhiteSpace get(const std::string& literal);
    static XSDWhiteSpace getByName(const std::string& name);
};

}  // namespace emf::xsd
