// XcoreParser.cpp — .xcore 文本解析为 AST
// 对齐 Java: org.eclipse.emf.ecore.xcore.resource.XcoreResource 的 parser 部分
//
// 实现：手写递归下降，字符级词法。
// 严格对齐 Xcore 公开样本（仓库内 3 个 .xcore 文件）所体现的语法：
//   - 单行 // / 块 /* */ 注释
//   - annotation "uri" as Name
//   - @Directive / @Directive(k=v, k2="str") 修饰符
//   - package qualified.name { ... }
//   - class 内成员关键字：contains / refers / op / 包级 enum / type
//   - 属性前缀：final|readonly|volatile|transient|unsettable|derived|id|unique|resolve
//   - Type[multi]? name [= default] [opposite Name] [get { body }]
//   - op ReturnType name(params) [throws E1, E2] { body }
//   - enum Name { LIT [= v], ... }
//   - type Name wraps qualified.TypeName
#include "emf/ecore/xcore/XcoreParser.h"
#include <cctype>
#include <sstream>

namespace emf::ecore::xcore {

// 关键字集合
static const std::vector<std::string> kAttrModifiers = {
    "final", "readonly", "volatile", "transient", "unsettable",
    "derived", "id", "unique", "resolve"
};
static const std::vector<std::string> kRefKeywords = {
    "contains", "refers"
};
static const std::vector<std::string> kClassModifiers = {
    "abstract", "interface"
};

XcoreParser::XcoreParser(const std::string& src) : src_(src) {}

// 静态入口
std::shared_ptr<XPackage> XcoreParser::parse(const std::string& source) {
    XcoreParser p(source);
    return p.parsePackage();
}

// ===== 词法辅助 =====

static bool isIdentStart(char c) { return std::isalpha(static_cast<unsigned char>(c)) || c == '_'; }
static bool isIdentPart(char c)  { return std::isalnum(static_cast<unsigned char>(c)) || c == '_'; }

void XcoreParser::skipWhitespaceAndComments() {
    while (pos_ < src_.size()) {
        char c = src_[pos_];
        if (std::isspace(static_cast<unsigned char>(c))) {
            ++pos_;
            continue;
        }
        // 单行注释
        if (c == '/' && pos_ + 1 < src_.size() && src_[pos_ + 1] == '/') {
            pos_ += 2;
            while (pos_ < src_.size() && src_[pos_] != '\n') ++pos_;
            continue;
        }
        // 块注释
        if (c == '/' && pos_ + 1 < src_.size() && src_[pos_ + 1] == '*') {
            pos_ += 2;
            while (pos_ + 1 < src_.size() &&
                   !(src_[pos_] == '*' && src_[pos_ + 1] == '/')) ++pos_;
            if (pos_ + 1 < src_.size()) pos_ += 2;
            continue;
        }
        break;
    }
}

bool XcoreParser::peekChar(char c) {
    skipWhitespaceAndComments();
    return pos_ < src_.size() && src_[pos_] == c;
}

bool XcoreParser::consumeChar(char c) {
    if (peekChar(c)) { ++pos_; return true; }
    return false;
}

void XcoreParser::expectChar(char c, const std::string& what) {
    if (!consumeChar(c)) {
        throw XcoreParseException("expected '" + std::string(1, c) + "' " + what +
                                  " at pos " + std::to_string(pos_));
    }
}

// 匹配关键字：要求后跟非标识符字符（避免 prefix 匹配）
bool XcoreParser::matchKeyword(const std::string& kw) {
    skipWhitespaceAndComments();
    if (pos_ + kw.size() > src_.size()) return false;
    if (src_.compare(pos_, kw.size(), kw) != 0) return false;
    // 后一字符必须不是 ident part
    size_t after = pos_ + kw.size();
    if (after < src_.size() && isIdentPart(src_[after])) return false;
    return true;
}

bool XcoreParser::consumeKeyword(const std::string& kw) {
    if (matchKeyword(kw)) {
        pos_ += kw.size();
        return true;
    }
    return false;
}

std::string XcoreParser::parseIdentifier() {
    skipWhitespaceAndComments();
    size_t start = pos_;
    while (pos_ < src_.size() && isIdentPart(src_[pos_])) ++pos_;
    if (pos_ == start) {
        throw XcoreParseException("expected identifier at pos " + std::to_string(pos_));
    }
    return src_.substr(start, pos_ - start);
}

std::string XcoreParser::parseQualifiedName() {
    std::string name = parseIdentifier();
    while (peekChar('.')) {
        ++pos_;
        name += '.';
        name += parseIdentifier();
    }
    return name;
}

std::string XcoreParser::parseStringLiteral() {
    skipWhitespaceAndComments();
    expectChar('"', "for string literal");
    std::string out;
    while (pos_ < src_.size() && src_[pos_] != '"') {
        char c = src_[pos_++];
        if (c == '\\' && pos_ < src_.size()) {
            char e = src_[pos_++];
            switch (e) {
                case 'n': out += '\n'; break;
                case 't': out += '\t'; break;
                case 'r': out += '\r'; break;
                case '"': out += '"'; break;
                case '\\': out += '\\'; break;
                default: out += '\\'; out += e; break;
            }
        } else {
            out += c;
        }
    }
    expectChar('"', "to close string literal");
    return out;
}

long XcoreParser::parseInteger() {
    skipWhitespaceAndComments();
    size_t start = pos_;
    if (pos_ < src_.size() && (src_[pos_] == '-' || src_[pos_] == '+')) ++pos_;
    while (pos_ < src_.size() && std::isdigit(static_cast<unsigned char>(src_[pos_]))) ++pos_;
    if (pos_ == start) {
        throw XcoreParseException("expected integer at pos " + std::to_string(pos_));
    }
    try {
        return std::stol(src_.substr(start, pos_ - start));
    } catch (...) {
        throw XcoreParseException("invalid integer at pos " + std::to_string(start));
    }
}

// ===== 包级解析 =====

std::shared_ptr<XPackage> XcoreParser::parsePackage() {
    auto pkg = std::make_shared<XPackage>();

    // 可选：@Ecore(...) @Foo(...) 等包级注解
    auto pkgAnnots = parseAnnotations();
    for (auto& a : pkgAnnots) {
        // 识别 @Ecore(nsURI="...", nsPrefix="...")
        if (a.directiveName == "Ecore") {
            for (auto& kv : a.details) {
                if (kv.first == "nsURI") pkg->nsURI = kv.second;
                else if (kv.first == "nsPrefix") pkg->nsPrefix = kv.second;
            }
        }
        pkg->annotations.push_back(std::move(a));
    }

    if (!consumeKeyword("package")) {
        throw XcoreParseException("expected 'package' at pos " + std::to_string(pos_));
    }
    pkg->name = parseQualifiedName();

    // 包体内可先有 annotation 指令
    parseAnnotationDirectives(*pkg);

    // Xcore 包体没有大括号：package 后直接是顶层声明序列，直到 EOF 或下一个 package
    parsePackageBody(*pkg);

    // 缺省 nsPrefix：取包名最后一段（对齐 Java XcorePackageManager）
    if (pkg->nsPrefix.empty()) {
        auto dot = pkg->name.find_last_of('.');
        pkg->nsPrefix = (dot == std::string::npos) ? pkg->name : pkg->name.substr(dot + 1);
    }
    // 缺省 nsURI：对齐 Java 行为，用包名作 nsURI
    if (pkg->nsURI.empty()) {
        pkg->nsURI = "http://" + pkg->name;
    }

    return pkg;
}

void XcoreParser::parseAnnotationDirectives(XPackage& pkg) {
    while (matchKeyword("annotation")) {
        consumeKeyword("annotation");
        std::string uri = parseStringLiteral();
        if (!consumeKeyword("as")) {
            throw XcoreParseException("expected 'as' after annotation URI");
        }
        auto dir = std::make_shared<XAnnotationDirective>();
        dir->sourceURI = uri;
        dir->name = parseIdentifier();
        pkg.annotationDirectives.push_back(std::move(dir));
    }
}

// 解析 0 个或多个 @Directive @Directive(k=v,...) 注解
std::vector<XAnnotation> XcoreParser::parseAnnotations() {
    std::vector<XAnnotation> out;
    while (peekChar('@')) {
        ++pos_;  // 消费 '@'
        XAnnotation a;
        a.directiveName = parseIdentifier();
        if (peekChar('(')) {
            ++pos_;  // 消费 '('
            skipWhitespaceAndComments();
            while (!peekChar(')')) {
                std::string key = parseIdentifier();
                expectChar('=', "in annotation detail");
                // value 可以是字符串或标识符或 true/false/数字
                skipWhitespaceAndComments();
                std::string val;
                if (peekChar('"')) {
                    val = parseStringLiteral();
                } else {
                    // 读到 , 或 )
                    size_t start = pos_;
                    while (pos_ < src_.size() && src_[pos_] != ',' && src_[pos_] != ')'
                           && !std::isspace(static_cast<unsigned char>(src_[pos_]))) {
                        ++pos_;
                    }
                    val = src_.substr(start, pos_ - start);
                }
                a.details.emplace_back(key, val);
                skipWhitespaceAndComments();
                if (peekChar(',')) { ++pos_; skipWhitespaceAndComments(); }
            }
            expectChar(')', "to close annotation details");
        }
        out.push_back(std::move(a));
        skipWhitespaceAndComments();
    }
    return out;
}

void XcoreParser::parsePackageBody(XPackage& pkg) {
    skipWhitespaceAndComments();
    // 包体直到 EOF 或下一个 package 关键字
    while (pos_ < src_.size() && !matchKeyword("package")) {
        // 包级注解指令可以在体内再次出现
        if (matchKeyword("annotation")) {
            parseAnnotationDirectives(pkg);
            continue;
        }
        // import 语句：忽略（Java import 仅用于 op body 的类型解析，本子集不解析 body）
        if (consumeKeyword("import")) {
            parseQualifiedName();  // 消费 import 路径
            // 可选 alias as Name（罕见，忽略）
            if (consumeKeyword("as")) parseIdentifier();
            continue;
        }
        // 包级注解（如 @Ecore）
        auto anns = parseAnnotations();
        // 接下来应该是 enum / class / type
        if (matchKeyword("class")) {
            auto cls = parseClass();
            cls->annotations = anns;
            pkg.classes.push_back(std::move(cls));
        } else if (matchKeyword("enum")) {
            auto e = parseEnum();
            e->annotations = anns;
            pkg.enums.push_back(std::move(e));
        } else if (matchKeyword("type")) {
            auto t = parseDataType();
            t->annotations = anns;
            pkg.dataTypes.push_back(std::move(t));
        } else {
            throw XcoreParseException("expected class/enum/type in package body at pos "
                                      + std::to_string(pos_));
        }
        skipWhitespaceAndComments();
    }
}

std::shared_ptr<XClass> XcoreParser::parseClass() {
    if (!consumeKeyword("class")) {
        throw XcoreParseException("expected 'class'");
    }
    auto cls = std::make_shared<XClass>();
    cls->name = parseIdentifier();
    // extends
    if (consumeKeyword("extends")) {
        cls->superTypes.push_back(parseQualifiedName());
        while (consumeChar(',')) {
            cls->superTypes.push_back(parseQualifiedName());
        }
    }
    expectChar('{', "to open class body");
    parseClassBody(*cls);
    expectChar('}', "to close class body");
    return cls;
}

std::shared_ptr<XEnum> XcoreParser::parseEnum() {
    if (!consumeKeyword("enum")) {
        throw XcoreParseException("expected 'enum'");
    }
    auto e = std::make_shared<XEnum>();
    e->name = parseIdentifier();
    expectChar('{', "to open enum body");
    skipWhitespaceAndComments();
    int nextVal = 0;
    while (!peekChar('}')) {
        auto lit = std::make_shared<XEnumLiteral>();
        // 可选 @Annotation
        auto litAnnots = parseAnnotations();
        lit->annotations = litAnnots;
        lit->name = parseIdentifier();
        // 显式 value
        if (consumeChar('=')) {
            long v = parseInteger();
            lit->value = static_cast<int>(v);
            nextVal = static_cast<int>(v) + 1;
        } else {
            lit->value = nextVal++;
        }
        lit->literal = lit->name;
        e->literals.push_back(std::move(lit));
        skipWhitespaceAndComments();
        if (peekChar(',')) { ++pos_; skipWhitespaceAndComments(); }
    }
    expectChar('}', "to close enum body");
    return e;
}

std::shared_ptr<XDataType> XcoreParser::parseDataType() {
    if (!consumeKeyword("type")) {
        throw XcoreParseException("expected 'type'");
    }
    auto dt = std::make_shared<XDataType>();
    dt->name = parseIdentifier();
    if (!consumeKeyword("wraps")) {
        throw XcoreParseException("expected 'wraps' in type declaration");
    }
    dt->wrappedClassName = parseQualifiedName();
    return dt;
}

void XcoreParser::parseClassBody(XClass& cls) {
    skipWhitespaceAndComments();
    while (!peekChar('}')) {
        // 解析成员前置注解 + 修饰符
        MemberMods mods = parseMemberMods();
        // 识别第一个 token 决定成员类型
        // 关键字：contains / refers / op
        // 否则是属性：[final|readonly|...]? Type name
        if (matchKeyword("op")) {
            consumeKeyword("op");
            // op ReturnType name(params) [throws E1,E2] { body }
            auto op = std::make_shared<XOperation>();
            op->annotations = mods.annotations;
            op->typeName = parseQualifiedName();
            op->name = parseIdentifier();
            expectChar('(', "to open op params");
            skipWhitespaceAndComments();
            while (!peekChar(')')) {
                auto p = std::make_shared<XParameter>();
                p->typeName = parseQualifiedName();
                p->name = parseIdentifier();
                // 可选 [] 多重性
                if (peekChar('[')) {
                    ++pos_;
                    expectChar(']', "in param multiplicity");
                }
                op->parameters.push_back(std::move(p));
                skipWhitespaceAndComments();
                if (peekChar(',')) { ++pos_; skipWhitespaceAndComments(); }
            }
            expectChar(')', "to close op params");
            // 可选 throws
            if (consumeKeyword("throws")) {
                op->exceptions.push_back(parseQualifiedName());
                while (consumeChar(',')) op->exceptions.push_back(parseQualifiedName());
            }
            // 可选 { body }
            if (peekChar('{')) {
                op->body = parseBraceBody();
            }
            cls.operations.push_back(std::move(op));
        } else if (matchKeyword("contains") || matchKeyword("refers")) {
            // 引用
            auto ref = std::make_shared<XReference>();
            ref->annotations = mods.annotations;
            ref->kind = matchKeyword("contains") ? ReferenceKind::Containment
                                                 : ReferenceKind::NonContainment;
            consumeKeyword(ref->kind == ReferenceKind::Containment ? "contains" : "refers");
            ref->typeName = parseQualifiedName();
            ref->multi = parseMultiplicity();
            ref->name = parseIdentifier();
            // 应用 mods
            ref->readOnly = mods.readOnly;
            ref->volatileFlag = mods.volatileFlag;
            ref->transient = mods.transient;
            ref->unsettable = mods.unsettable;
            ref->derived = mods.derived;
            // opposite Name
            if (consumeKeyword("opposite")) {
                ref->oppositeName = parseIdentifier();
            }
            // resolve true|false
            // 已通过 mods.resolve 设置（默认 true）。除非显式 `resolve false`
            ref->resolveProxies = mods.resolve;
            // 可选 get { body }
            if (peekChar('{') && matchKeyword("get") == false) {
                // 此处简化：花括号 + get 关键字 → getterBody
            }
            // derived X get { body } 形式
            // 由于 derived 是修饰符已消费，这里若看到 get 关键字则取体
            // （样本里 derived long averageTime get { ... }）
            // 我们已经在 mods 里读过 derived，这里再读 get
            skipWhitespaceAndComments();
            if (matchKeyword("get")) {
                consumeKeyword("get");
                ref->getterBody = parseBraceBody();
                ref->derived = true;
            }
            cls.references.push_back(std::move(ref));
        } else {
            // 属性：[Type] name [= default] [get { body }]
            auto attr = std::make_shared<XAttribute>();
            attr->annotations = mods.annotations;
            attr->typeName = parseQualifiedName();
            attr->multi = parseMultiplicity();
            attr->name = parseIdentifier();
            // 应用 mods
            attr->readOnly = mods.readOnly;
            attr->volatileFlag = mods.volatileFlag;
            attr->transient = mods.transient;
            attr->unsettable = mods.unsettable;
            attr->derived = mods.derived;
            attr->idFlag = mods.idFlag;
            // 默认值
            if (consumeChar('=')) {
                // 字面量（字符串或数字或标识符）
                skipWhitespaceAndComments();
                if (peekChar('"')) {
                    attr->defaultValueLiteral = parseStringLiteral();
                } else {
                    size_t start = pos_;
                    while (pos_ < src_.size() &&
                           !std::isspace(static_cast<unsigned char>(src_[pos_])) &&
                           src_[pos_] != ',' && src_[pos_] != '}') {
                        ++pos_;
                    }
                    attr->defaultValueLiteral = src_.substr(start, pos_ - start);
                }
            }
            // derived ... get { body }
            skipWhitespaceAndComments();
            if (matchKeyword("get")) {
                consumeKeyword("get");
                attr->getterBody = parseBraceBody();
                attr->derived = true;
            }
            cls.attributes.push_back(std::move(attr));
        }
        skipWhitespaceAndComments();
        // 成员间分隔（Xcore 不强制分号，但容忍）
        if (peekChar(',')) { ++pos_; skipWhitespaceAndComments(); }
    }
}

XcoreParser::MemberMods XcoreParser::parseMemberMods() {
    MemberMods m;
    // 先解析 @Annotation
    m.annotations = parseAnnotations();
    // 修饰符关键字
    while (true) {
        bool matched = false;
        for (auto& kw : kAttrModifiers) {
            if (matchKeyword(kw)) {
                consumeKeyword(kw);
                matched = true;
                if (kw == "readonly") m.readOnly = true;
                else if (kw == "volatile") m.volatileFlag = true;
                else if (kw == "transient") m.transient = true;
                else if (kw == "unsettable") m.unsettable = true;
                else if (kw == "derived") m.derived = true;
                else if (kw == "id") m.idFlag = true;
                else if (kw == "unique") m.unique = true;
                else if (kw == "resolve") {
                    // resolve true|false
                    skipWhitespaceAndComments();
                    if (matchKeyword("true")) { consumeKeyword("true"); m.resolve = true; }
                    else if (matchKeyword("false")) { consumeKeyword("false"); m.resolve = false; }
                    else m.resolve = true;
                }
                // final 等暂忽略
                break;
            }
        }
        if (!matched) break;
    }
    return m;
}

// 多重性：[?] 或 [] 或 [n..m]
// 本子集只解析 []（multi=true）与单值（multi=false）。
bool XcoreParser::parseMultiplicity() {
    if (!peekChar('[')) return false;
    ++pos_;  // 消费 '['
    // 跳过内部直到 ']'
    // [] → multi，[1] → 单值，[*] → multi，[0..*] → multi
    skipWhitespaceAndComments();
    bool multi = false;
    if (peekChar('*') || peekChar(']')) {
        multi = true;
    } else {
        // 数字
        long lower = parseInteger();
        skipWhitespaceAndComments();
        if (peekChar('.')) {
            ++pos_;
            if (peekChar('.')) ++pos_;
            skipWhitespaceAndComments();
            if (peekChar('*')) { multi = true; ++pos_; }
            else { long upper = parseInteger(); (void)upper; multi = (lower != upper); }
        }
    }
    // 跳过剩余直到 ]
    while (pos_ < src_.size() && src_[pos_] != ']') ++pos_;
    expectChar(']', "to close multiplicity");
    return multi;
}

// 解析 { ... }，返回内部文本（保留换行）。pos_ 停在 } 之后。
std::string XcoreParser::parseBraceBody() {
    expectChar('{', "to open brace body");
    int depth = 1;
    std::string body;
    while (pos_ < src_.size() && depth > 0) {
        char c = src_[pos_++];
        if (c == '{') { ++depth; body += c; }
        else if (c == '}') {
            --depth;
            if (depth == 0) break;
            body += c;
        }
        // 字符串字面量：原样保留（避免字符串里的 { } 干扰深度）
        else if (c == '"') {
            body += c;
            while (pos_ < src_.size() && src_[pos_] != '"') {
                body += src_[pos_++];
            }
            if (pos_ < src_.size()) { body += src_[pos_++]; }
        }
        else {
            body += c;
        }
    }
    return body;
}

}  // namespace emf::ecore::xcore
