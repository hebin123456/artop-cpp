// EMF XSD: XSDWhiteSpaceFacet 实现
#include "emf/xsd/XSDWhiteSpaceFacet.h"
#include "emf/xsd/XSDPackage.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/ecore/EcoreMetadata.h"

namespace emf::xsd {

XSDWhiteSpace XSDWhiteSpaceFacet::getValue() const {
    return XSDWhiteSpace::get(value_);
}

void XSDWhiteSpaceFacet::setValue(const XSDWhiteSpace& v) {
    value_ = v.literal;
}

std::string XSDWhiteSpaceFacet::getNormalizedLiteral(const std::string& literal) const {
    if (value_ == "preserve") return literal;
    if (value_ == "replace") {
        // 替换所有空白字符序列为单个空格
        std::string out;
        out.reserve(literal.size());
        bool lastWasSpace = false;
        for (char c : literal) {
            if (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
                if (!lastWasSpace) {
                    out += ' ';
                    lastWasSpace = true;
                }
            } else {
                out += c;
                lastWasSpace = false;
            }
        }
        return out;
    }
    if (value_ == "collapse") {
        std::string out;
        out.reserve(literal.size());
        bool lastWasSpace = false;
        for (char c : literal) {
            if (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
                if (!lastWasSpace) {
                    out += ' ';
                    lastWasSpace = true;
                }
            } else {
                out += c;
                lastWasSpace = false;
            }
        }
        // 去掉首尾空格
        size_t a = 0, b = out.size();
        while (a < b && (out[a] == ' ' || out[a] == '\t' || out[a] == '\n' || out[a] == '\r')) a++;
        while (b > a && (out[b-1] == ' ' || out[b-1] == '\t' || out[b-1] == '\n' || out[b-1] == '\r')) b--;
        return out.substr(a, b - a);
    }
    return literal;
}

emf::ecore::EClass* XSDWhiteSpaceFacet::eClass() const {
    return XSDPackage::instance().getEClass_XSDWhiteSpaceFacet();
}

}  // namespace emf::xsd
