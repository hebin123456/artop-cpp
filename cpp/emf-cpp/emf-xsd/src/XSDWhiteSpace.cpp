// EMF XSD: XSDWhiteSpace 枚举实现
#include "emf/xsd/XSDWhiteSpace.h"

namespace emf::xsd {

const XSDWhiteSpace XSDWhiteSpace::PRESERVE_LITERAL(0, "preserve", "preserve");
const XSDWhiteSpace XSDWhiteSpace::REPLACE_LITERAL(1, "replace", "replace");
const XSDWhiteSpace XSDWhiteSpace::COLLAPSE_LITERAL(2, "collapse", "collapse");

XSDWhiteSpace XSDWhiteSpace::get(int v) {
    switch (v) {
        case 0: return PRESERVE_LITERAL;
        case 1: return REPLACE_LITERAL;
        case 2: return COLLAPSE_LITERAL;
        default: return PRESERVE_LITERAL;  // 兜底
    }
}

XSDWhiteSpace XSDWhiteSpace::get(const std::string& literal) {
    if (literal == "preserve") return PRESERVE_LITERAL;
    if (literal == "replace") return REPLACE_LITERAL;
    if (literal == "collapse") return COLLAPSE_LITERAL;
    return PRESERVE_LITERAL;
}

XSDWhiteSpace XSDWhiteSpace::getByName(const std::string& name) {
    if (name == "preserve") return PRESERVE_LITERAL;
    if (name == "replace") return REPLACE_LITERAL;
    if (name == "collapse") return COLLAPSE_LITERAL;
    return PRESERVE_LITERAL;
}

}  // namespace emf::xsd
