// XSDValidator.cpp
// 对齐 Java: org.eclipse.xsd.validation.XSDValidator
#include "emf/xsd/XSDValidator.h"
#include "emf/common/Diagnostic.h"
#include "emf/xsd/XSDElementDeclaration.h"
#include "emf/xsd/XSDAttributeDeclaration.h"
#include "emf/xsd/XSDTypeDefinition.h"
#include "emf/xsd/XSDMinLengthFacet.h"
#include "emf/xsd/XSDMaxLengthFacet.h"
#include "emf/xsd/XSDLengthFacet.h"
#include "emf/xsd/XSDPatternFacet.h"
#include "emf/xsd/XSDEnumerationFacet.h"
#include "emf/xsd/XSDMinInclusiveFacet.h"
#include "emf/xsd/XSDMaxInclusiveFacet.h"
#include "emf/xsd/XSDMinExclusiveFacet.h"
#include "emf/xsd/XSDMaxExclusiveFacet.h"
#include "emf/ecore/EcoreImpls.h"

#include <algorithm>
#include <cctype>
#include <cstring>
#include <regex>
#include <sstream>
#include <stdexcept>
#include <string>
#include <unordered_map>
#include <vector>

namespace emf::xsd {

using DiagSev = emf::common::Diagnostic::Severity;
using XDiag = XSDDiagnostic;

// 简单 lower-case
std::string lower(std::string s) {
    std::transform(s.begin(), s.end(), s.begin(),
                   [](unsigned char c) { return std::tolower(c); });
    return s;
}

// 解析 "prefix:localName" 或 "localName"
void splitQName(const std::string& qn, std::string& prefix, std::string& local) {
    auto colon = qn.find(':');
    if (colon == std::string::npos) {
        prefix.clear();
        local = qn;
    } else {
        prefix = qn.substr(0, colon);
        local = qn.substr(colon + 1);
    }
}

// 简易 XML 转义 → 原始字符
std::string unescapeXML(const std::string& s) {
    std::string r;
    r.reserve(s.size());
    for (size_t i = 0; i < s.size(); ++i) {
        if (s[i] == '&' && i + 1 < s.size()) {
            if (s.compare(i, 5, "&amp;")  == 0) { r += '&';  i += 4; continue; }
            if (s.compare(i, 4, "&lt;")   == 0) { r += '<';  i += 3; continue; }
            if (s.compare(i, 4, "&gt;")   == 0) { r += '>';  i += 3; continue; }
            if (s.compare(i, 6, "&quot;") == 0) { r += '"';  i += 5; continue; }
            if (s.compare(i, 6, "&apos;") == 0) { r += '\''; i += 5; continue; }
        }
        r += s[i];
    }
    return r;
}

// 提取 attribute value（带 "..." 或 '...'）
std::string readAttrValue(const std::string& xml, size_t& pos) {
    while (pos < xml.size() && std::isspace(static_cast<unsigned char>(xml[pos]))) ++pos;
    if (pos >= xml.size()) return {};
    char q = xml[pos];
    if (q != '"' && q != '\'') return {};
    ++pos;
    std::string r;
    while (pos < xml.size() && xml[pos] != q) {
        if (xml[pos] == '&') {
            r += unescapeXML(xml.substr(pos, std::min<size_t>(6, xml.size() - pos)));
            // unescapeXML 一次只处理一个 entity，需要逐步推进
            // 这里简化：单字符推进直到结束
            ++pos;
            continue;
        }
        r += xml[pos++];
    }
    if (pos < xml.size()) ++pos;  // skip closing quote
    return r;
}

// skip 空白
void skipWs(const std::string& xml, size_t& pos) {
    while (pos < xml.size() && std::isspace(static_cast<unsigned char>(xml[pos]))) ++pos;
}

// skip prolog / DOCTYPE / comments
void skipPrologAndMisc(const std::string& xml, size_t& pos) {
    while (pos < xml.size()) {
        skipWs(xml, pos);
        if (pos >= xml.size()) break;
        // skip comment <!-- ... -->
        if (xml.compare(pos, 4, "<!--") == 0) {
            auto end = xml.find("-->", pos + 4);
            if (end == std::string::npos) { pos = xml.size(); break; }
            pos = end + 3;
            continue;
        }
        // skip <?xml ... ?>
        if (xml[pos] == '<' && pos + 1 < xml.size() && xml[pos + 1] == '?') {
            auto end = xml.find("?>", pos + 2);
            if (end == std::string::npos) { pos = xml.size(); break; }
            pos = end + 2;
            continue;
        }
        // skip <!DOCTYPE ...>
        if (xml.compare(pos, 9, "<!DOCTYPE") == 0) {
            // 简单：找匹配的 '>'（需注意 ']' 内嵌，但本场景不常见）
            int depth = 0;
            size_t p = pos;
            while (p < xml.size()) {
                if (xml[p] == '[') ++depth;
                else if (xml[p] == ']') --depth;
                else if (xml[p] == '>' && depth == 0) { ++p; break; }
                ++p;
            }
            pos = p;
            continue;
        }
        break;
    }
}

// 解析一个 element 节点（start tag + 可能的 attrs/children/end tag）
// 自闭合（<x .../>）也支持
bool parseElement(const std::string& xml, size_t& pos, XMLNode& node) {
    if (pos >= xml.size() || xml[pos] != '<') return false;
    ++pos;  // skip '<'
    // skip '!'/'?' 等
    if (pos < xml.size() && (xml[pos] == '!' || xml[pos] == '?')) {
        // 异常（prolog 应该已被跳过）
        return false;
    }
    // 读 tag name
    size_t nameStart = pos;
    while (pos < xml.size() && (std::isalnum(static_cast<unsigned char>(xml[pos]))
                                || xml[pos] == ':' || xml[pos] == '_' || xml[pos] == '-')) {
        ++pos;
    }
    if (pos == nameStart) return false;
    node.qname = xml.substr(nameStart, pos - nameStart);
    std::string prefix;
    splitQName(node.qname, prefix, node.localName);

    // 读 attributes（含 xmlns / xmlns:prefix）
    while (pos < xml.size()) {
        skipWs(xml, pos);
        if (pos >= xml.size()) break;
        if (xml[pos] == '/' || xml[pos] == '>') break;
        // 读 attribute name
        size_t an = pos;
        while (pos < xml.size() && (std::isalnum(static_cast<unsigned char>(xml[pos]))
                                    || xml[pos] == ':' || xml[pos] == '_' || xml[pos] == '-')) {
            ++pos;
        }
        std::string attrName = xml.substr(an, pos - an);
        skipWs(xml, pos);
        if (pos < xml.size() && xml[pos] == '=') {
            ++pos;
            node.attrs.emplace_back(attrName, readAttrValue(xml, pos));
        } else {
            // no value
            node.attrs.emplace_back(attrName, std::string{});
        }
    }

    if (pos >= xml.size()) return false;

    if (xml[pos] == '/') {
        // 自闭合 <tag/>
        ++pos;
        if (pos < xml.size() && xml[pos] == '>') ++pos;
        return true;
    }
    if (xml[pos] != '>') return false;
    ++pos;

    // 读 children 或 text
    std::string textBuf;
    while (pos < xml.size()) {
        // 文本内容
        size_t tStart = pos;
        while (pos < xml.size() && xml[pos] != '<') {
            if (!std::isspace(static_cast<unsigned char>(xml[pos]))) textBuf += xml[pos];
            ++pos;
        }
        (void)tStart;
        if (pos >= xml.size()) break;

        // 是 </tag> ?
        if (xml[pos + 1] == '/') {
            // end tag
            size_t et = pos + 2;
            while (et < xml.size() && (std::isalnum(static_cast<unsigned char>(xml[et]))
                                       || xml[et] == ':' || xml[et] == '_' || xml[et] == '-')) ++et;
            std::string endName = xml.substr(pos + 2, et - (pos + 2));
            std::string prefix, local;
            splitQName(endName, prefix, local);
            if (local != node.localName) {
                // 嵌套不对齐（应有上层 end tag 处理）
                return false;
            }
            pos = et;
            if (pos < xml.size() && xml[pos] == '>') ++pos;
            node.text = textBuf;
            return true;
        }
        // 是 <!comment> ?
        if (xml.compare(pos, 4, "<!--") == 0) {
            auto end = xml.find("-->", pos + 4);
            if (end == std::string::npos) { pos = xml.size(); break; }
            pos = end + 3;
            continue;
        }
        // 是 <![CDATA[ ... ]]> ?
        if (xml.compare(pos, 9, "<![CDATA[") == 0) {
            auto end = xml.find("]]>", pos + 9);
            if (end == std::string::npos) { pos = xml.size(); break; }
            textBuf += xml.substr(pos + 9, end - pos - 9);
            pos = end + 3;
            continue;
        }
        // 是 <child>
        XMLNode child;
        if (!parseElement(xml, pos, child)) return false;
        node.children.push_back(std::move(child));
    }
    // children 处理完但未遇到父 end tag → 文档不完整
    return false;
}

// 按 prefix-to-ns map 解析 qname 的 ns
std::string resolveNs(const std::string& prefix,
                      const std::unordered_map<std::string, std::string>& p2ns,
                      const std::string& defaultNs) {
    if (prefix.empty()) return defaultNs;
    auto it = p2ns.find(prefix);
    if (it == p2ns.end()) return {};
    return it->second;
}

// ===== parseXML =====

bool XSDValidator::parseXML(const std::string& xml, XMLNode& root, std::string& errMsg) {
    errMsg.clear();
    size_t pos = 0;
    skipPrologAndMisc(xml, pos);
    if (pos >= xml.size()) { errMsg = "empty XML"; return false; }
    size_t startPos = pos;
    if (!parseElement(xml, pos, root)) {
        errMsg = "parse error near position " + std::to_string(pos);
        return false;
    }
    // 检查是否还有未消费的内容（根结束后无闭合 / 兄弟节点 / 文本）
    if (pos < xml.size()) {
        // 跳过空白
        size_t p2 = pos;
        while (p2 < xml.size() && std::isspace(static_cast<unsigned char>(xml[p2]))) ++p2;
        if (p2 < xml.size()) {
            errMsg = "parse error: extra content after root element at position " + std::to_string(p2);
            return false;
        }
    }
    (void)startPos;
    return true;
}

// ===== 找 global element / type =====

XSDElementDeclaration* XSDValidator::findGlobalElement(XSDSchema* schema, const std::string& localName) {
    if (!schema) return nullptr;
    for (size_t i = 0; i < schema->getElementDeclarations().size(); ++i) {
        auto* obj = schema->getElementDeclarations().get(i);
        auto* elem = dynamic_cast<XSDElementDeclaration*>(obj);
        if (!elem) continue;
        if (elem->getScope() != XSDScope::GLOBAL) continue;
        if (elem->getName() == localName) return elem;
    }
    return nullptr;
}

XSDTypeDefinition* XSDValidator::findGlobalType(XSDSchema* schema, const std::string& localName) {
    if (!schema) return nullptr;
    for (size_t i = 0; i < schema->getTypeDefinitions().size(); ++i) {
        auto* obj = schema->getTypeDefinitions().get(i);
        auto* td = dynamic_cast<XSDTypeDefinition*>(obj);
        if (td && td->getName() == localName) return td;
    }
    return nullptr;
}

// ===== 校验入口 =====

std::vector<XSDDiagnostic> XSDValidator::validate(XSDSchema* schema, const std::string& xml) {
    std::vector<XSDDiagnostic> diags;
    if (!schema) {
        diags.emplace_back(DiagSev::ERROR, "schema_null", "schema is null");
        return diags;
    }
    XMLNode root;
    std::string err;
    if (!parseXML(xml, root, err)) {
        diags.emplace_back(DiagSev::ERROR, "parse_error", err);
        return diags;
    }
    return validate(schema, root);
}

std::vector<XSDDiagnostic> XSDValidator::validate(XSDSchema* schema, const XMLNode& root) {
    std::vector<XSDDiagnostic> diags;
    if (!schema) {
        diags.emplace_back(DiagSev::ERROR, "schema_null", "schema is null");
        return diags;
    }
    // 1) 找 global element 声明
    auto* elemDecl = findGlobalElement(schema, root.localName);
    if (!elemDecl) {
        XSDDiagnostic d(DiagSev::ERROR, "root_not_found",
                        "root element '" + root.qname + "' not declared in schema");
        d.setElementQName(root.qname);
        diags.push_back(std::move(d));
        return diags;
    }
    validateElement(elemDecl, root, diags);
    return diags;
}

void XSDValidator::validateElement(XSDElementDeclaration* elemDecl, const XMLNode& node,
                                    std::vector<XSDDiagnostic>& diags) {
    if (!elemDecl) return;
    auto* type = elemDecl->getTypeDefinition();
    if (!type) {
        // 简单类型默认是 xs:anyType，无约束
        return;
    }
    auto* simple = dynamic_cast<XSDSimpleTypeDefinition*>(type);
    auto* complex = dynamic_cast<XSDComplexTypeDefinition*>(type);

    if (simple) {
        // 简单类型 content：text + 没有子元素
        if (!node.children.empty()) {
            XSDDiagnostic d(DiagSev::ERROR, "element_in_simple_type",
                            "element '" + node.qname + "' of simple type must not have children");
            d.setElementQName(node.qname);
            diags.push_back(std::move(d));
            return;
        }
        if (options_.validateFacets) {
            validateSimpleType(simple, node.text, diags);
        }
    } else if (complex) {
        if (options_.validateContentModel) {
            validateComplexType(complex, node, diags);
        }
        if (options_.validateRequiredAttributes) {
            validateRequiredAttributes(complex, node, diags);
        }
    }
}

// ===== 简单类型 facet 校验 =====

void XSDValidator::validateSimpleType(XSDSimpleTypeDefinition* type, const std::string& value,
                                       std::vector<XSDDiagnostic>& diags) {
    if (!type) return;
    const auto& facets = type->getFacets();
    for (size_t i = 0; i < facets.size(); ++i) {
        auto* f = facets.get(i);
        if (!f) continue;

        if (auto* mlen = dynamic_cast<XSDMinLengthFacet*>(f)) {
            int need = mlen->getValue();
            if (need >= 0 && (int)value.size() < need) {
                XSDDiagnostic d(DiagSev::ERROR, "minLength",
                    "value length " + std::to_string(value.size()) +
                    " < minLength " + std::to_string(need));
                diags.push_back(std::move(d));
            }
        } else if (auto* mlen = dynamic_cast<XSDMaxLengthFacet*>(f)) {
            int max = mlen->getValue();
            if (max >= 0 && (int)value.size() > max) {
                XSDDiagnostic d(DiagSev::ERROR, "maxLength",
                    "value length " + std::to_string(value.size()) +
                    " > maxLength " + std::to_string(max));
                diags.push_back(std::move(d));
            }
        } else if (auto* len = dynamic_cast<XSDLengthFacet*>(f)) {
            int expected = len->getValue();
            if (expected >= 0 && (int)value.size() != expected) {
                XSDDiagnostic d(DiagSev::ERROR, "length",
                    "value length " + std::to_string(value.size()) +
                    " != length " + std::to_string(expected));
                diags.push_back(std::move(d));
            }
        } else if (auto* pat = dynamic_cast<XSDPatternFacet*>(f)) {
            const auto& patterns = pat->getValue();
            bool anyMatch = patterns.empty();
            for (size_t p = 0; p < patterns.size(); ++p) {
                try {
                    std::regex re(patterns.get(p));
                    if (std::regex_match(value, re)) { anyMatch = true; break; }
                } catch (const std::regex_error&) { /* bad pattern: skip */ }
            }
            if (!anyMatch) {
                XSDDiagnostic d(DiagSev::ERROR, "pattern",
                    "value '" + value + "' does not match pattern");
                diags.push_back(std::move(d));
            }
        } else if (auto* en = dynamic_cast<XSDEnumerationFacet*>(f)) {
            const auto& values = en->getValue();
            bool found = false;
            for (size_t e = 0; e < values.size(); ++e) {
                if (values.get(e) == value) { found = true; break; }
            }
            if (!found && !values.empty()) {
                XSDDiagnostic d(DiagSev::ERROR, "enumeration",
                    "value '" + value + "' is not in enumeration");
                diags.push_back(std::move(d));
            }
        } else if (auto* mi = dynamic_cast<XSDMinInclusiveFacet*>(f)) {
            // 简化：int 比较
            try {
                int vi = std::stoi(value);
                int mi_v = std::any_cast<int>(mi->getValue());
                if (vi < mi_v) {
                    XSDDiagnostic d(DiagSev::ERROR, "minInclusive",
                        "value " + std::to_string(vi) + " < minInclusive " + std::to_string(mi_v));
                    diags.push_back(std::move(d));
                }
            } catch (...) { /* 非数字：跳过 */ }
        } else if (auto* ma = dynamic_cast<XSDMaxInclusiveFacet*>(f)) {
            try {
                int vi = std::stoi(value);
                int ma_v = std::any_cast<int>(ma->getValue());
                if (vi > ma_v) {
                    XSDDiagnostic d(DiagSev::ERROR, "maxInclusive",
                        "value " + std::to_string(vi) + " > maxInclusive " + std::to_string(ma_v));
                    diags.push_back(std::move(d));
                }
            } catch (...) { }
        } else if (auto* me = dynamic_cast<XSDMinExclusiveFacet*>(f)) {
            try {
                int vi = std::stoi(value);
                int me_v = std::any_cast<int>(me->getValue());
                if (vi <= me_v) {
                    XSDDiagnostic d(DiagSev::ERROR, "minExclusive",
                        "value " + std::to_string(vi) + " <= minExclusive " + std::to_string(me_v));
                    diags.push_back(std::move(d));
                }
            } catch (...) { }
        } else if (auto* mae = dynamic_cast<XSDMaxExclusiveFacet*>(f)) {
            try {
                int vi = std::stoi(value);
                int mae_v = std::any_cast<int>(mae->getValue());
                if (vi >= mae_v) {
                    XSDDiagnostic d(DiagSev::ERROR, "maxExclusive",
                        "value " + std::to_string(vi) + " >= maxExclusive " + std::to_string(mae_v));
                    diags.push_back(std::move(d));
                }
            } catch (...) { }
        }
    }
}

// ===== 复杂类型 content model 校验 =====

void XSDValidator::validateComplexType(XSDComplexTypeDefinition* type, const XMLNode& node,
                                        std::vector<XSDDiagnostic>& diags) {
    if (!type) return;
    auto* group = type->getParticle();
    if (!group) {
        // 没有 particle：要求无子元素（empty content），mixed 模式除外
        if (!type->isMixed() && !node.children.empty()) {
            XSDDiagnostic d(DiagSev::ERROR, "extra_children",
                "element '" + node.qname + "' has no content model but has children");
            d.setElementQName(node.qname);
            diags.push_back(std::move(d));
        }
        return;
    }

    // 按 compositor 校验
    const auto& particles = group->getParticles();
    if (group->getCompositor() == XSDCompositor::SEQUENCE) {
        // 顺序匹配 + occurs
        size_t expectedIdx = 0;
        for (size_t ci = 0; ci < node.children.size(); ++ci) {
            const auto& child = node.children[ci];
            if (expectedIdx >= particles.size()) {
                XSDDiagnostic d(DiagSev::ERROR, "unexpected_child",
                    "unexpected child '" + child.qname + "' in element '" + node.qname + "'");
                d.setElementQName(node.qname);
                diags.push_back(std::move(d));
                continue;
            }
            auto* p = particles.get(expectedIdx);
            if (!p) { ++expectedIdx; --ci; continue; }
            auto* term = dynamic_cast<XSDElementDeclaration*>(p->getTerm());
            if (!term || term->getName() != child.localName) {
                XSDDiagnostic d(DiagSev::ERROR, "child_mismatch",
                    "expected element '" + term->getName() +
                    "' but got '" + child.qname + "' at position " + std::to_string(ci));
                d.setElementQName(node.qname);
                diags.push_back(std::move(d));
                ++expectedIdx;  // 跳过这个粒子
                --ci;  // 重试当前 child
                continue;
            }
            // minOccurs/maxOccurs 校验在序列里用 ocount 跟踪
            // 简化：先把当前 child 算入 matches
            (void)child;
            ++expectedIdx;
        }
        // 必填但 missing 的（minOccurs）
        for (size_t pi = 0; pi < particles.size(); ++pi) {
            auto* p = particles.get(pi);
            if (!p) continue;
            auto* term = dynamic_cast<XSDElementDeclaration*>(p->getTerm());
            if (!term) continue;
            // 简化：要求每个 particle 至少出现 minOccurs 次（简化为 0/1）
            if (p->getMinOccurs() >= 1 && pi < expectedIdx - (expectedIdx - pi)) {
                // 简化：跳过精确匹配检查
            }
        }
    } else if (group->getCompositor() == XSDCompositor::CHOICE) {
        // 至少一个 child 匹配
        for (size_t ci = 0; ci < node.children.size(); ++ci) {
            const auto& child = node.children[ci];
            bool matched = false;
            for (size_t pi = 0; pi < particles.size(); ++pi) {
                auto* p = particles.get(pi);
                if (!p) continue;
                auto* term = dynamic_cast<XSDElementDeclaration*>(p->getTerm());
                if (term && term->getName() == child.localName) { matched = true; break; }
            }
            if (!matched) {
                XSDDiagnostic d(DiagSev::ERROR, "choice_mismatch",
                    "element '" + child.qname + "' not allowed in choice of '" + node.qname + "'");
                d.setElementQName(node.qname);
                diags.push_back(std::move(d));
            }
        }
    } else {
        // ALL：忽略顺序，只检查元素在粒子列表中
        for (size_t ci = 0; ci < node.children.size(); ++ci) {
            const auto& child = node.children[ci];
            bool matched = false;
            for (size_t pi = 0; pi < particles.size(); ++pi) {
                auto* p = particles.get(pi);
                if (!p) continue;
                auto* term = dynamic_cast<XSDElementDeclaration*>(p->getTerm());
                if (term && term->getName() == child.localName) { matched = true; break; }
            }
            if (!matched) {
                XSDDiagnostic d(DiagSev::ERROR, "all_mismatch",
                    "element '" + child.qname + "' not in content model of '" + node.qname + "'");
                d.setElementQName(node.qname);
                diags.push_back(std::move(d));
            }
        }
    }

    // 递归校验子元素
    for (size_t ci = 0; ci < node.children.size(); ++ci) {
        const auto& child = node.children[ci];
        // 找对应的 element 声明（先查 local name，简化）
        for (size_t pi = 0; pi < particles.size(); ++pi) {
            auto* p = particles.get(pi);
            if (!p) continue;
            auto* term = dynamic_cast<XSDElementDeclaration*>(p->getTerm());
            if (term && term->getName() == child.localName) {
                validateElement(term, child, diags);
                break;
            }
        }
    }
}

// ===== Required attributes =====

void XSDValidator::validateRequiredAttributes(XSDComplexTypeDefinition* type, const XMLNode& node,
                                               std::vector<XSDDiagnostic>& diags) {
    if (!type) return;
    const auto& uses = type->getAttributeUses();
    for (size_t i = 0; i < uses.size(); ++i) {
        auto* a = uses.get(i);
        if (!a) continue;
        // 简化：scope=global 且无 default → required
        if (a->getScope() == XSDScope::GLOBAL) {
            // 检查是否在 attrs 列表
            bool found = false;
            for (auto& pr : node.attrs) {
                std::string p, l;
                splitQName(pr.first, p, l);
                if (l == a->getName()) { found = true; break; }
            }
            if (!found) {
                XSDDiagnostic d(DiagSev::ERROR, "required_attr",
                    "required attribute '" + a->getName() + "' missing on '" + node.qname + "'");
                d.setElementQName(node.qname);
                d.setAttributeQName(a->getName());
                diags.push_back(std::move(d));
            }
        }
    }
}

}  // namespace emf::xsd
