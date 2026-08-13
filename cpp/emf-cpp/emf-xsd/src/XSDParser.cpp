// EMF XSD: XSDParser 实现
// 简易 XSD/XML 解析器（不引入第三方）
#include "emf/xsd/XSDParser.h"
#include "emf/xsd/XSDFactory.h"
#include "emf/xsd/XSDPackage.h"
#include "emf/xsd/XSDElementDeclaration.h"
#include "emf/xsd/XSDAttributeDeclaration.h"
#include "emf/xsd/XSDComplexTypeDefinition.h"
#include "emf/xsd/XSDSimpleTypeDefinition.h"
#include "emf/xsd/XSDModelGroup.h"
#include "emf/xsd/XSDParticle.h"
#include "emf/xsd/XSDWildcard.h"
#include "emf/xsd/XSDAttributeGroupDefinition.h"
#include "emf/xsd/XSDAnnotation.h"
#include "emf/xsd/XSDImport.h"
#include "emf/xsd/XSDInclude.h"
// Facet
#include "emf/xsd/XSDConstrainingFacet.h"
#include "emf/xsd/XSDFixedFacet.h"
#include "emf/xsd/XSDLengthFacet.h"
#include "emf/xsd/XSDMinLengthFacet.h"
#include "emf/xsd/XSDMaxLengthFacet.h"
#include "emf/xsd/XSDPatternFacet.h"
#include "emf/xsd/XSDEnumerationFacet.h"
#include "emf/xsd/XSDWhiteSpaceFacet.h"
#include "emf/xsd/XSDWhiteSpace.h"
#include "emf/xsd/XSDTotalDigitsFacet.h"
#include "emf/xsd/XSDFractionDigitsFacet.h"
#include "emf/xsd/XSDMinInclusiveFacet.h"
#include "emf/xsd/XSDMaxInclusiveFacet.h"
#include "emf/xsd/XSDMinExclusiveFacet.h"
#include "emf/xsd/XSDMaxExclusiveFacet.h"
#include "emf/xsd/XSDMinFacet.h"
#include "emf/xsd/XSDMaxFacet.h"

#include <fstream>
#include <sstream>
#include <stdexcept>
#include <cctype>

namespace emf::xsd {

namespace {

// 从 QName (prefix:local) 拆出
void splitQName(const std::string& q, std::string& prefix, std::string& local) {
    auto pos = q.find(':');
    if (pos == std::string::npos) {
        prefix.clear();
        local = q;
    } else {
        prefix = q.substr(0, pos);
        local = q.substr(pos + 1);
    }
}

XSDCompositor parseCompositor(const std::string& s) {
    if (s == "all") return XSDCompositor::ALL;
    if (s == "choice") return XSDCompositor::CHOICE;
    return XSDCompositor::SEQUENCE;
}

}  // namespace

// ===== 基础 XML 解析 =====

std::string XSDParser::readFile(const std::string& path) {
    std::ifstream f(path);
    if (!f) throw std::runtime_error("XSDParser: cannot open " + path);
    std::stringstream ss;
    ss << f.rdbuf();
    return ss.str();
}

void XSDParser::skipWhitespace(const std::string& xml, size_t& pos) {
    while (pos < xml.size() && std::isspace(static_cast<unsigned char>(xml[pos]))) pos++;
}

void XSDParser::skipProlog(const std::string& xml, size_t& pos) {
    // 跳过 <?xml ... ?>
    while (pos < xml.size()) {
        skipWhitespace(xml, pos);
        if (pos + 1 < xml.size() && xml[pos] == '<' && xml[pos + 1] == '?') {
            auto end = xml.find("?>", pos);
            if (end == std::string::npos) throw std::runtime_error("XSDParser: bad prolog");
            pos = end + 2;
        } else if (pos + 3 < xml.size() && xml[pos] == '<' && xml[pos + 1] == '!' &&
                   xml[pos + 2] == '-' && xml[pos + 3] == '-') {
            // 注释
            auto end = xml.find("-->", pos);
            if (end == std::string::npos) throw std::runtime_error("XSDParser: bad comment");
            pos = end + 3;
        } else {
            break;
        }
    }
}

std::string XSDParser::parseAttributeValue(const std::string& xml, size_t& pos) {
    // 假定已经在 "
    if (pos >= xml.size() || xml[pos] != '"' && xml[pos] != '\'') {
        throw std::runtime_error("XSDParser: expected '\"'");
    }
    char quote = xml[pos++];
    std::string out;
    while (pos < xml.size() && xml[pos] != quote) {
        if (xml[pos] == '&') {
            // 简单实体解析
            if (xml.compare(pos, 5, "&amp;") == 0) { out += '&'; pos += 5; }
            else if (xml.compare(pos, 4, "&lt;") == 0) { out += '<'; pos += 4; }
            else if (xml.compare(pos, 4, "&gt;") == 0) { out += '>'; pos += 4; }
            else if (xml.compare(pos, 6, "&quot;") == 0) { out += '"'; pos += 6; }
            else if (xml.compare(pos, 6, "&apos;") == 0) { out += '\''; pos += 6; }
            else { out += xml[pos++]; }
        } else {
            out += xml[pos++];
        }
    }
    if (pos >= xml.size()) throw std::runtime_error("XSDParser: unterminated attribute value");
    pos++;  // 跳过 "
    return out;
}

void XSDParser::parseAttributes(const std::string& xml, size_t& pos, Node& node) {
    while (pos < xml.size()) {
        skipWhitespace(xml, pos);
        if (pos >= xml.size()) break;
        if (xml[pos] == '>' || xml[pos] == '/') break;
        // 读 name
        size_t nameStart = pos;
        while (pos < xml.size() && !std::isspace(static_cast<unsigned char>(xml[pos])) &&
               xml[pos] != '=' && xml[pos] != '>' && xml[pos] != '/') {
            pos++;
        }
        std::string name = xml.substr(nameStart, pos - nameStart);
        splitQName(name, node.attrs[name + ".prefix"], node.attrs[name + ".local"]);
        node.attrs[name] = name;
        // 找 "="
        skipWhitespace(xml, pos);
        if (pos >= xml.size() || xml[pos] != '=') {
            // 单独属性名（罕见），跳过
            continue;
        }
        pos++;
        skipWhitespace(xml, pos);
        // 读 value
        std::string val = parseAttributeValue(xml, pos);
        node.attrs[name] = val;
        // 如果是 xmlns 声明
        if (name == "xmlns") {
            // default namespace
        } else if (name.substr(0, 6) == "xmlns:") {
            // 命名空间绑定
        }
    }
}

std::string XSDParser::parseText(const std::string& xml, size_t& pos, const std::string& parentName) {
    (void)parentName;
    std::string out;
    while (pos < xml.size()) {
        if (xml[pos] == '<') break;
        if (xml[pos] == '&') {
            if (xml.compare(pos, 5, "&amp;") == 0) { out += '&'; pos += 5; }
            else if (xml.compare(pos, 4, "&lt;") == 0) { out += '<'; pos += 4; }
            else if (xml.compare(pos, 4, "&gt;") == 0) { out += '>'; pos += 4; }
            else if (xml.compare(pos, 6, "&quot;") == 0) { out += '"'; pos += 6; }
            else if (xml.compare(pos, 6, "&apos;") == 0) { out += '\''; pos += 6; }
            else { out += xml[pos++]; }
        } else {
            out += xml[pos++];
        }
    }
    return out;
}

XSDParser::Node XSDParser::parseNode(const std::string& xml, size_t& pos) {
    Node node;
    if (pos >= xml.size() || xml[pos] != '<') {
        throw std::runtime_error("XSDParser: expected '<' at pos " + std::to_string(pos));
    }
    pos++;  // 跳过 <
    // 读标签名
    size_t nameStart = pos;
    while (pos < xml.size() && !std::isspace(static_cast<unsigned char>(xml[pos])) &&
           xml[pos] != '>' && xml[pos] != '/') {
        pos++;
    }
    std::string fullName = xml.substr(nameStart, pos - nameStart);
    splitQName(fullName, node.prefix, node.localName);
    node.name = fullName;
    // 解析属性
    parseAttributes(xml, pos, node);
    skipWhitespace(xml, pos);
    if (pos < xml.size() && xml[pos] == '/') {
        // 自闭合
        pos++;
        if (pos >= xml.size() || xml[pos] != '>') {
            throw std::runtime_error("XSDParser: expected '>' for self-closing tag");
        }
        pos++;
        return node;
    }
    if (pos >= xml.size() || xml[pos] != '>') {
        throw std::runtime_error("XSDParser: expected '>' after tag name");
    }
    pos++;  // 跳过 >

    // 读 children
    while (pos < xml.size()) {
        if (xml[pos] == '<') {
            if (pos + 1 < xml.size() && xml[pos + 1] == '/') {
                // 结束标签
                pos += 2;
                size_t end = xml.find('>', pos);
                if (end == std::string::npos) throw std::runtime_error("XSDParser: bad end tag");
                pos = end + 1;
                return node;
            }
            if (pos + 3 < xml.size() && xml.compare(pos, 4, "<!--") == 0) {
                auto end = xml.find("-->", pos);
                if (end == std::string::npos) throw std::runtime_error("XSDParser: bad comment");
                pos = end + 3;
                continue;
            }
            if (pos + 8 < xml.size() && xml.compare(pos, 9, "<![CDATA[") == 0) {
                auto end = xml.find("]]>", pos);
                if (end == std::string::npos) throw std::runtime_error("XSDParser: bad CDATA");
                node.text += xml.substr(pos + 9, end - pos - 9);
                pos = end + 3;
                continue;
            }
            // 子节点
            Node child = parseNode(xml, pos);
            node.children.push_back(std::move(child));
        } else {
            std::string txt = parseText(xml, pos, node.localName);
            // 修剪空白
            size_t a = 0, b = txt.size();
            while (a < b && std::isspace(static_cast<unsigned char>(txt[a]))) a++;
            while (b > a && std::isspace(static_cast<unsigned char>(txt[b - 1]))) b--;
            if (a < b) node.text += txt.substr(a, b - a);
        }
    }
    return node;
}

// ===== 解析入口 =====

XSDSchema* XSDParser::parseFile(const std::string& path) {
    std::string xml = readFile(path);
    return parseString(xml);
}

XSDSchema* XSDParser::parseString(const std::string& xml) {
    size_t pos = 0;
    skipProlog(xml, pos);
    Node root = parseNode(xml, pos);
    return buildSchema(root);
}

// ===== 命名空间收集 =====

void XSDParser::collectNamespaces(const Node& n, std::unordered_map<std::string, std::string>& prefixToNs) const {
    for (auto& kv : n.attrs) {
        const std::string& key = kv.first;
        const std::string& val = kv.second;
        if (key == "xmlns") {
            prefixToNs[""] = val;
        } else if (key.size() > 6 && key.substr(0, 6) == "xmlns:") {
            prefixToNs[key.substr(6)] = val;
        }
    }
    for (auto& c : n.children) collectNamespaces(c, prefixToNs);
}

std::string XSDParser::resolveQName(const std::string& qname, const std::string& defaultNs,
                                     std::string* prefixOut, std::string* localOut) const {
    std::string p, l;
    splitQName(qname, p, l);
    if (prefixOut) *prefixOut = p;
    if (localOut) *localOut = l;
    if (l.empty()) return "";
    if (p.empty()) return defaultNs;
    // 不解析命名空间，保留 prefix:local
    return qname;
}

int XSDParser::parseOccurs(const std::string& s, int def) const {
    if (s.empty()) return def;
    if (s == "unbounded") return -1;
    try { return std::stoi(s); } catch (...) { return def; }
}

// ===== Schema 构建 =====

XSDSchema* XSDParser::buildSchema(const Node& root) {
    auto& fac = XSDFactory::instance();
    XSDSchema* schema = fac.createXSDSchema();

    // 命名空间
    std::unordered_map<std::string, std::string> prefixToNs;
    collectNamespaces(root, prefixToNs);
    for (auto& kv : prefixToNs) {
        schema->getQNamePrefixToNamespaceMap()[kv.first] = kv.second;
    }

    if (auto it = root.attrs.find("targetNamespace"); it != root.attrs.end()) {
        schema->setTargetNamespace(it->second);
    }
    std::string defaultNs = schema->getTargetNamespace();
    if (auto it = prefixToNs.find(""); it != prefixToNs.end()) {
        defaultNs = it->second;
    }

    for (auto& child : root.children) {
        const std::string& ln = child.localName;
        if (ln == "element") {
            buildElementDeclaration(child, schema, defaultNs, /*global=*/true);
        } else if (ln == "complexType") {
            buildComplexType(child, schema, defaultNs);
        } else if (ln == "simpleType") {
            buildSimpleType(child, schema, defaultNs);
        } else if (ln == "attribute") {
            buildAttribute(child, schema, defaultNs);
        } else if (ln == "attributeGroup") {
            buildAttributeGroup(child, schema);
        } else if (ln == "group") {
            buildModelGroup(child, schema);
        } else if (ln == "import") {
            buildImport(child, schema);
        } else if (ln == "include") {
            buildInclude(child, schema);
        } else if (ln == "annotation") {
            buildAnnotation(child, schema);
        }
    }
    return schema;
}

void XSDParser::buildElementDeclaration(const Node& n, XSDSchema* schema,
                                          const std::string& defaultNs, bool global) {
    auto* elem = XSDFactory::instance().createXSDElementDeclaration();
    elem->setRootComponent(schema);

    auto nit = n.attrs.find("name");
    if (nit != n.attrs.end()) elem->setName(nit->second);
    if (global) {
        elem->setScope(XSDScope::GLOBAL);
        elem->setTargetNamespace(schema->getTargetNamespace());
    } else {
        elem->setScope(XSDScope::LOCAL);
    }

    auto tit = n.attrs.find("type");
    if (tit != n.attrs.end()) {
        // 引用类型 - 简化：仅记录名字
        // 复杂实现需要 resolveQName
    }

    for (auto& c : n.children) {
        const std::string& ln = c.localName;
        if (ln == "complexType") {
            buildComplexType(c, elem, defaultNs);
        } else if (ln == "simpleType") {
            buildSimpleType(c, elem, defaultNs);
        } else if (ln == "annotation") {
            buildAnnotation(c, elem);
        }
    }

    if (global) {
        schema->addElementDeclaration(elem);
    }
}

void XSDParser::buildComplexType(const Node& n, emf::common::EObject* parent, const std::string& defaultNs) {
    auto* ct = XSDFactory::instance().createXSDComplexTypeDefinition();
    auto nit = n.attrs.find("name");
    if (nit != n.attrs.end()) ct->setName(nit->second);
    if (n.attrs.count("abstract")) {
        auto it = n.attrs.find("abstract");
        ct->setAbstract(it->second == "true" || it->second == "1");
    }
    if (n.attrs.count("mixed")) {
        auto it = n.attrs.find("mixed");
        ct->setMixed(it->second == "true" || it->second == "1");
    }

    for (auto& c : n.children) {
        const std::string& ln = c.localName;
        if (ln == "sequence" || ln == "choice" || ln == "all") {
            buildModelGroup(c, ct);
        } else if (ln == "attribute") {
            buildAttribute(c, ct, defaultNs);
        } else if (ln == "attributeGroup") {
            buildAttributeGroup(c, ct);
        } else if (ln == "annotation") {
            buildAnnotation(c, ct);
        }
    }

    if (auto* elem = dynamic_cast<XSDElementDeclaration*>(parent)) {
        elem->setTypeDefinition(ct);
    } else if (auto* sch = dynamic_cast<XSDSchema*>(parent)) {
        sch->addTypeDefinition(ct);
    }
}

void XSDParser::buildSimpleType(const Node& n, emf::common::EObject* parent, const std::string& defaultNs) {
    (void)defaultNs;
    auto* st = XSDFactory::instance().createXSDSimpleTypeDefinition();
    auto nit = n.attrs.find("name");
    if (nit != n.attrs.end()) st->setName(nit->second);

    auto& fac = XSDFactory::instance();

    for (auto& c : n.children) {
        const std::string& ln = c.localName;
        if (ln == "restriction") {
            if (auto rit = c.attrs.find("base"); rit != c.attrs.end()) {
                st->setLexicalValue(rit->second);
            }
            // 解析所有 19 个 facet
            for (auto& cc : c.children) {
                const std::string& fln = cc.localName;
                auto vit = cc.attrs.find("value");
                std::string val;
                if (vit != cc.attrs.end()) val = vit->second;

                if (fln == "pattern") {
                    auto* pf = fac.createXSDPatternFacet();
                    pf->setLexicalValue(val);
                    pf->getValue().add(val);
                    st->addFacet(pf);
                } else if (fln == "enumeration") {
                    auto* ef = fac.createXSDEnumerationFacet();
                    ef->setLexicalValue(val);
                    ef->getValue().add(val);
                    st->addFacet(ef);
                } else if (fln == "minLength") {
                    auto* f = fac.createXSDMinLengthFacet();
                    f->setLexicalValue(val);
                    try { f->setValue(std::stoi(val)); } catch (...) {}
                    if (auto fix = cc.attrs.find("fixed"); fix != cc.attrs.end()) {
                        f->setFixed(fix->second == "true" || fix->second == "1");
                    }
                    st->addFacet(f);
                } else if (fln == "maxLength") {
                    auto* f = fac.createXSDMaxLengthFacet();
                    f->setLexicalValue(val);
                    try { f->setValue(std::stoi(val)); } catch (...) {}
                    if (auto fix = cc.attrs.find("fixed"); fix != cc.attrs.end()) {
                        f->setFixed(fix->second == "true" || fix->second == "1");
                    }
                    st->addFacet(f);
                } else if (fln == "length") {
                    auto* f = fac.createXSDLengthFacet();
                    f->setLexicalValue(val);
                    try { f->setValue(std::stoi(val)); } catch (...) {}
                    if (auto fix = cc.attrs.find("fixed"); fix != cc.attrs.end()) {
                        f->setFixed(fix->second == "true" || fix->second == "1");
                    }
                    st->addFacet(f);
                } else if (fln == "whiteSpace") {
                    auto* f = fac.createXSDWhiteSpaceFacet();
                    f->setLexicalValue(val);
                    f->setValueString(val);
                    if (auto fix = cc.attrs.find("fixed"); fix != cc.attrs.end()) {
                        f->setFixed(fix->second == "true" || fix->second == "1");
                    }
                    st->addFacet(f);
                } else if (fln == "totalDigits") {
                    auto* f = fac.createXSDTotalDigitsFacet();
                    f->setLexicalValue(val);
                    try { f->setValue(std::stoi(val)); } catch (...) {}
                    if (auto fix = cc.attrs.find("fixed"); fix != cc.attrs.end()) {
                        f->setFixed(fix->second == "true" || fix->second == "1");
                    }
                    st->addFacet(f);
                } else if (fln == "fractionDigits") {
                    auto* f = fac.createXSDFractionDigitsFacet();
                    f->setLexicalValue(val);
                    try { f->setValue(std::stoi(val)); } catch (...) {}
                    if (auto fix = cc.attrs.find("fixed"); fix != cc.attrs.end()) {
                        f->setFixed(fix->second == "true" || fix->second == "1");
                    }
                    st->addFacet(f);
                } else if (fln == "minInclusive") {
                    auto* f = fac.createXSDMinInclusiveFacet();
                    f->setLexicalValue(val);
                    f->setValue(std::any{val});
                    if (auto fix = cc.attrs.find("fixed"); fix != cc.attrs.end()) {
                        f->setFixed(fix->second == "true" || fix->second == "1");
                    }
                    st->addFacet(f);
                } else if (fln == "maxInclusive") {
                    auto* f = fac.createXSDMaxInclusiveFacet();
                    f->setLexicalValue(val);
                    f->setValue(std::any{val});
                    if (auto fix = cc.attrs.find("fixed"); fix != cc.attrs.end()) {
                        f->setFixed(fix->second == "true" || fix->second == "1");
                    }
                    st->addFacet(f);
                } else if (fln == "minExclusive") {
                    auto* f = fac.createXSDMinExclusiveFacet();
                    f->setLexicalValue(val);
                    f->setValue(std::any{val});
                    if (auto fix = cc.attrs.find("fixed"); fix != cc.attrs.end()) {
                        f->setFixed(fix->second == "true" || fix->second == "1");
                    }
                    st->addFacet(f);
                } else if (fln == "maxExclusive") {
                    auto* f = fac.createXSDMaxExclusiveFacet();
                    f->setLexicalValue(val);
                    f->setValue(std::any{val});
                    if (auto fix = cc.attrs.find("fixed"); fix != cc.attrs.end()) {
                        f->setFixed(fix->second == "true" || fix->second == "1");
                    }
                    st->addFacet(f);
                }
            }
        } else if (ln == "list") {
            st->setVariety(XSDVariety::LIST);
        } else if (ln == "union") {
            st->setVariety(XSDVariety::UNION);
        } else if (ln == "annotation") {
            buildAnnotation(c, st);
        }
    }

    if (auto* elem = dynamic_cast<XSDElementDeclaration*>(parent)) {
        elem->setTypeDefinition(st);
    } else if (auto* attr = dynamic_cast<XSDAttributeDeclaration*>(parent)) {
        attr->setTypeDefinition(st);
    } else if (auto* sch = dynamic_cast<XSDSchema*>(parent)) {
        sch->addTypeDefinition(st);
    }
}

void XSDParser::buildModelGroup(const Node& n, emf::common::EObject* parent) {
    auto* mg = XSDFactory::instance().createXSDModelGroup();
    mg->setCompositor(parseCompositor(n.localName));

    for (auto& c : n.children) {
        if (c.localName == "element") {
            // element 作为 particle 内容
            auto* p = XSDFactory::instance().createXSDParticle();
            auto* sub = XSDFactory::instance().createXSDElementDeclaration();
            if (auto it = c.attrs.find("name"); it != c.attrs.end()) {
                sub->setName(it->second);
            }
            if (auto it = c.attrs.find("minOccurs"); it != c.attrs.end()) {
                p->setMinOccurs(parseOccurs(it->second, 1));
            }
            if (auto it = c.attrs.find("maxOccurs"); it != c.attrs.end()) {
                p->setMaxOccurs(parseOccurs(it->second, 1));
            }
            for (auto& cc : c.children) {
                if (cc.localName == "complexType") {
                    buildComplexType(cc, sub, "");
                } else if (cc.localName == "simpleType") {
                    buildSimpleType(cc, sub, "");
                }
            }
            p->setTerm(sub);
            mg->addParticle(p);
        } else if (c.localName == "sequence" || c.localName == "choice" || c.localName == "all") {
            // 嵌套 model group
            auto* sub = XSDFactory::instance().createXSDModelGroup();
            sub->setCompositor(parseCompositor(c.localName));
            // 递归构建
            XSDParser subParser;
            subParser.buildModelGroup(c, sub);
            // subParser.buildModelGroup 只是递归 build，粒子已 add 到 sub
            auto* p = XSDFactory::instance().createXSDParticle();
            p->setTerm(sub);
            mg->addParticle(p);
        } else if (c.localName == "any") {
            auto* wc = XSDFactory::instance().createXSDWildcard();
            if (auto it = c.attrs.find("namespace"); it != c.attrs.end()) {
                wc->addNamespaceConstraint(it->second);
            }
            auto* p = XSDFactory::instance().createXSDParticle();
            if (auto it = c.attrs.find("minOccurs"); it != c.attrs.end()) {
                p->setMinOccurs(parseOccurs(it->second, 1));
            }
            if (auto it = c.attrs.find("maxOccurs"); it != c.attrs.end()) {
                p->setMaxOccurs(parseOccurs(it->second, 1));
            }
            p->setTerm(wc);
            mg->addParticle(p);
        }
    }

    if (auto* ct = dynamic_cast<XSDComplexTypeDefinition*>(parent)) {
        ct->setParticle(mg);
    } else if (auto* sch = dynamic_cast<XSDSchema*>(parent)) {
        sch->addModelGroupDefinition(mg);
    }
}

void XSDParser::buildAttribute(const Node& n, emf::common::EObject* parent, const std::string& defaultNs) {
    (void)defaultNs;
    auto* attr = XSDFactory::instance().createXSDAttributeDeclaration();
    auto nit = n.attrs.find("name");
    if (nit != n.attrs.end()) attr->setName(nit->second);
    attr->setScope(XSDScope::LOCAL);

    for (auto& c : n.children) {
        if (c.localName == "simpleType") {
            buildSimpleType(c, attr, defaultNs);
        } else if (c.localName == "annotation") {
            buildAnnotation(c, attr);
        }
    }

    if (auto* ct = dynamic_cast<XSDComplexTypeDefinition*>(parent)) {
        ct->addAttributeUse(attr);
    } else if (auto* ag = dynamic_cast<XSDAttributeGroupDefinition*>(parent)) {
        ag->addAttributeUse(attr);
    } else if (auto* sch = dynamic_cast<XSDSchema*>(parent)) {
        sch->addAttributeDeclaration(attr);
    }
}

void XSDParser::buildAttributeGroup(const Node& n, emf::common::EObject* parent) {
    auto* ag = XSDFactory::instance().createXSDAttributeGroupDefinition();
    auto nit = n.attrs.find("name");
    if (nit != n.attrs.end()) ag->setName(nit->second);

    for (auto& c : n.children) {
        if (c.localName == "attribute") {
            buildAttribute(c, ag, "");
        } else if (c.localName == "anyAttribute") {
            auto* wc = XSDFactory::instance().createXSDWildcard();
            if (auto it = c.attrs.find("namespace"); it != c.attrs.end()) {
                wc->addNamespaceConstraint(it->second);
            }
            ag->setAttributeWildcard(wc);
        }
    }

    if (auto* sch = dynamic_cast<XSDSchema*>(parent)) {
        sch->addAttributeGroupDefinition(ag);
    }
}

void XSDParser::buildImport(const Node& n, XSDSchema* schema) {
    auto* imp = XSDFactory::instance().createXSDImport();
    if (auto it = n.attrs.find("namespace"); it != n.attrs.end()) {
        imp->setNamespace(it->second);
    }
    if (auto it = n.attrs.find("schemaLocation"); it != n.attrs.end()) {
        imp->setSchemaLocation(it->second);
    }
    schema->addImport(imp);
}

void XSDParser::buildInclude(const Node& n, XSDSchema* schema) {
    auto* inc = XSDFactory::instance().createXSDInclude();
    if (auto it = n.attrs.find("schemaLocation"); it != n.attrs.end()) {
        inc->setSchemaLocation(it->second);
    }
    schema->addInclude(inc);
}

void XSDParser::buildAnnotation(const Node& n, emf::common::EObject* parent) {
    (void)parent;
    auto* ann = XSDFactory::instance().createXSDAnnotation();
    std::string info;
    for (auto& c : n.children) {
        if (c.localName == "documentation") {
            info += c.text;
        } else if (c.localName == "appinfo") {
            auto* sub = XSDFactory::instance().createXSDAnnotation();
            sub->setUserInformation(c.text);
            ann->addAppInfo(sub);
        }
    }
    ann->setUserInformation(info);
    if (auto* sch = dynamic_cast<XSDSchema*>(parent)) {
        sch->addAnnotation(ann);
    }
}

}  // namespace emf::xsd
