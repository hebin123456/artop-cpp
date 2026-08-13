// ConstraintParser.cpp
// OCL 子集约束表达式解析器（递归下降），对齐 Eclipse OCL / EMF Validation OCL 引擎。
//
// 支持的 OCL 子集（对齐 org.eclipse.ocl 求值语义）：
//   逻辑运算（优先级低→高）：
//     implies（右结合，最低）, or, xor, and, not/!
//   比较运算：= / ==, <> / !=, >, <, >=, <=
//   算术运算：+ - * /（+ 同时支持字符串拼接）
//   集合迭代：source->forAll(v | boolExpr), source->exists(v | boolExpr)
//             source->collect(v | expr), source->select(v | boolExpr),
//             source->reject(v | boolExpr), source->any(v | boolExpr),
//             source->iterate(v; acc : Type = init | expr)
//             （collect 支持嵌套并自动扁平化；迭代可任意嵌套 select/forAll/exists）
//   集合操作：source->size(), source->isEmpty(), source->notEmpty()
//   SortedSet/OrderedSet 操作：sortedBy(v | expr), first(), last(), at(i),
//             indexOf(e), count(e), includes(e), excludes(e),
//             includesAll(col), excludesAll(col), union(col),
//             intersection(col), difference(col), asBag(), asSequence(),
//             asSet(), asOrderedSet(), flatten(), sum()
//   集合比较：col1 = col2 / col1 <> col2（Bag/Set 多集语义，顺序无关）
//   路径导航：self, 迭代变量, self.attr.subattr, attr（隐式 self）
//   对象操作：obj.attr.size()（集合大小或字符串长度）
//   let 表达式：let v : Type = expr in body（支持嵌套）
//   Tuple 类型：Tuple { name (: Type)? = expr, ... } 字面量 + .name 按名访问 +
//               Tuple = / <> 相等（按部分逐项比较，顺序无关）
//   String 操作：size/length, toUpper, toLower, substring, concat, indexOf,
//                startsWith, endsWith, trim, oclIsKindOf, oclIsTypeOf
//   Integer/Real 操作：abs, floor, ceil, round, max, min, toString, mod, div,
//                      toInteger, toReal
//   通用对象操作：oclIsUndefined, oclIsInvalid, oclType, asSequence
//   字面量：null, '', 'str', true, false, 数字（含负数）
//   分组：(expr)
//   条件：if expr then expr else expr endif
//
//   OCL 语义要点（对齐 Eclipse OCL）：
//   - 空集合 forAll → true, exists → false
//   - implies: A implies B = (not A) or B
//   - = / <> 为 OCL 标准等值运算（同时兼容 == / !=）
//   - 单值引用上使用 -> 迭代视为单元素集合（OCL Collection(source) 语义）
//   - 迭代变量在 body 中可作 self 之外的对象上下文（v.attr）
//   - let 变量在 body 作用域内可见，支持嵌套
//
// 容错策略：解析失败时返回恒 true（对齐 Java constraint 语法容错）。
#include "emf/validation/ConstraintParser.h"

#include "emf/common/EObject.h"
#include "emf/common/EList.h"
#include "emf/ecore/EcoreImpls.h"

#include <algorithm>
#include <any>
#include <cctype>
#include <cmath>
#include <functional>
#include <memory>
#include <string>
#include <utility>
#include <vector>

namespace emf::validation {

namespace {

// ===================== OCL Tuple 类型 =====================
// 对齐 Eclipse OCL TupleType / TupleLiteralExp：有序命名部分的值聚合。
// 存储为 std::shared_ptr<OclTuple> 装入 std::any，便于在表达式中传递与按名访问。
// 语义：Tuple 是值类型——相等按部分逐项比较（顺序无关，按名匹配），
// 与 OCL Tuple::= 的语义一致；用于 collect/iterate 分组多值、约束中临时聚合。
struct OclTuple {
    std::vector<std::pair<std::string, std::any>> parts;
    const std::any* get(const std::string& name) const {
        for (const auto& p : parts) if (p.first == name) return &p.second;
        return nullptr;
    }
};

using OclTuplePtr = std::shared_ptr<OclTuple>;

bool asTuple(const std::any& v, OclTuplePtr& out) {
    if (!v.has_value()) return false;
    if (v.type() == typeid(OclTuplePtr)) {
        out = std::any_cast<OclTuplePtr>(v);
        return true;
    }
    return false;
}

// ===================== 值辅助函数 =====================

// 从 target 反射读取名为 attrName 的属性值（对齐生成类 eGet 语义）
std::any readAttr(emf::common::EObject* target, const std::string& attrName) {
    if (!target) return std::any{};
    auto* cls = target->eClass();
    if (!cls) return std::any{};
    auto* sf = cls->getEStructuralFeature(attrName);
    if (!sf) return std::any{};
    return target->eGet(sf);
}

// 取数值（double）：支持常见算术类型
bool asNumber(const std::any& v, double& out) {
    if (!v.has_value()) return false;
    const auto& t = v.type();
    if (t == typeid(int)) { out = static_cast<double>(std::any_cast<int>(v)); return true; }
    if (t == typeid(long)) { out = static_cast<double>(std::any_cast<long>(v)); return true; }
    if (t == typeid(long long)) { out = static_cast<double>(std::any_cast<long long>(v)); return true; }
    if (t == typeid(unsigned int)) { out = static_cast<double>(std::any_cast<unsigned int>(v)); return true; }
    if (t == typeid(double)) { out = std::any_cast<double>(v); return true; }
    if (t == typeid(float)) { out = static_cast<double>(std::any_cast<float>(v)); return true; }
    if (t == typeid(bool)) { out = std::any_cast<bool>(v) ? 1.0 : 0.0; return true; }
    if (t == typeid(short)) { out = static_cast<double>(std::any_cast<short>(v)); return true; }
    if (t == typeid(unsigned short)) { out = static_cast<double>(std::any_cast<unsigned short>(v)); return true; }
    if (t == typeid(char)) { out = static_cast<double>(std::any_cast<char>(v)); return true; }
    return false;
}

// 取字符串值
bool asString(const std::any& v, std::string& out) {
    if (!v.has_value()) return false;
    if (v.type() == typeid(std::string)) { out = std::any_cast<std::string>(v); return true; }
    if (v.type() == typeid(const char*)) { out = std::any_cast<const char*>(v); return true; }
    return false;
}

// 取 EObject*：引用类型属性的运行时表示
emf::common::EObject* asEObject(const std::any& v) {
    if (!v.has_value()) return nullptr;
    if (v.type() == typeid(emf::common::EObject*)) return std::any_cast<emf::common::EObject*>(v);
    return nullptr;
}

// 将 any 提取为 EObject* 元素列表（用于集合迭代）。
// 支持：EList<EObject*>*（eGet 多值引用的堆副本）、vector<EObject*>、
//       vector<std::any>（collect 结果中的 EObject 元素）、
//       单值 EObject*（OCL 中单值引用上 -> 迭代视为单元素集合）。
std::vector<emf::common::EObject*> asElementList(const std::any& v) {
    std::vector<emf::common::EObject*> result;
    if (!v.has_value()) return result;
    if (v.type() == typeid(emf::common::EList<emf::common::EObject*>*)) {
        auto* p = std::any_cast<emf::common::EList<emf::common::EObject*>*>(v);
        if (p) {
            for (auto* e : *p) result.push_back(e);
        }
    } else if (v.type() == typeid(std::vector<emf::common::EObject*>)) {
        result = std::any_cast<std::vector<emf::common::EObject*>>(v);
    } else if (v.type() == typeid(std::vector<std::any>)) {
        auto vec = std::any_cast<std::vector<std::any>>(v);
        for (auto& a : vec) {
            auto* e = asEObject(a);
            if (e) result.push_back(e);
        }
    } else if (v.type() == typeid(emf::common::EObject*)) {
        auto* e = std::any_cast<emf::common::EObject*>(v);
        if (e) result.push_back(e);  // 单值引用 → 单元素集合
    }
    return result;
}

// 将 any 提取为通用元素列表（vector<std::any>），元素可为 EObject*/string/number/bool。
// 用于 count/includes/sum/union 等需要对非对象元素操作的集合操作。
// 支持：EList<EObject*>*、vector<EObject*>、vector<std::any>、
//       单值 EObject*（非空 → 单元素）、单值标量（string/number → 单元素）。
std::vector<std::any> asAnyList(const std::any& v) {
    std::vector<std::any> result;
    if (!v.has_value()) return result;
    if (v.type() == typeid(emf::common::EList<emf::common::EObject*>*)) {
        auto* p = std::any_cast<emf::common::EList<emf::common::EObject*>*>(v);
        if (p) {
            for (auto* e : *p) result.push_back(std::any(e));
        }
    } else if (v.type() == typeid(std::vector<emf::common::EObject*>)) {
        auto vec = std::any_cast<std::vector<emf::common::EObject*>>(v);
        for (auto* e : vec) result.push_back(std::any(e));
    } else if (v.type() == typeid(std::vector<std::any>)) {
        result = std::any_cast<std::vector<std::any>>(v);
    } else if (v.type() == typeid(emf::common::EObject*)) {
        auto* e = std::any_cast<emf::common::EObject*>(v);
        if (e) result.push_back(v);  // 非空单值引用 → 单元素集合
    } else {
        result.push_back(v);  // 标量单值（string/number/bool）→ 单元素集合
    }
    return result;
}

// 判断 any 是否为集合类型（EList / vector<EObject*> / vector<std::any>）
bool isCollection(const std::any& v) {
    if (!v.has_value()) return false;
    return v.type() == typeid(emf::common::EList<emf::common::EObject*>*) ||
           v.type() == typeid(std::vector<emf::common::EObject*>) ||
           v.type() == typeid(std::vector<std::any>);
}

// 集合多集相等（顺序无关，重复计数）— 对齐 OCL Bag/Set = 语义
bool collectionsEqual(const std::vector<std::any>& a, const std::vector<std::any>& b);

// 取大小：集合元素数 或 字符串长度（对齐 OCL Collection::size / String::size）
int sizeOf(const std::any& v) {
    if (!v.has_value()) return 0;
    if (v.type() == typeid(emf::common::EList<emf::common::EObject*>*)) {
        auto* p = std::any_cast<emf::common::EList<emf::common::EObject*>*>(v);
        return p ? static_cast<int>(p->size()) : 0;
    }
    if (v.type() == typeid(std::vector<emf::common::EObject*>)) {
        return static_cast<int>(std::any_cast<std::vector<emf::common::EObject*>>(v).size());
    }
    if (v.type() == typeid(std::vector<std::any>)) {
        return static_cast<int>(std::any_cast<std::vector<std::any>>(v).size());
    }
    std::string s;
    if (asString(v, s)) return static_cast<int>(s.size());
    return 0;
}

// 布尔强转（裸值在布尔上下文中求值，如 self.flag 或 if 条件）
bool toBool(const std::any& v) {
    if (!v.has_value()) return false;
    if (v.type() == typeid(bool)) return std::any_cast<bool>(v);
    double n;
    if (asNumber(v, n)) return n != 0;
    std::string s;
    if (asString(v, s)) return !s.empty();
    if (v.type() == typeid(emf::common::EObject*)) return std::any_cast<emf::common::EObject*>(v) != nullptr;
    return false;
}

// 判断 any 是否为 null（无值 或 EObject*==nullptr）
bool isNullValue(const std::any& v) {
    if (!v.has_value()) return true;
    if (v.type() == typeid(emf::common::EObject*)) return std::any_cast<emf::common::EObject*>(v) == nullptr;
    return false;
}

// 值相等判断（对齐 OCL = / <> 语义）
bool valuesEqual(const std::any& a, const std::any& b) {
    bool aNull = isNullValue(a);
    bool bNull = isNullValue(b);
    if (aNull || bNull) return aNull && bNull;  // 两者皆 null 才相等

    // 集合相等（Bag/Set 语义：顺序无关、重复计数）
    if (isCollection(a) || isCollection(b)) {
        if (!isCollection(a) || !isCollection(b)) return false;
        return collectionsEqual(asAnyList(a), asAnyList(b));
    }

    std::string as, bs;
    if (asString(a, as) && asString(b, bs)) return as == bs;

    double an, bn;
    if (asNumber(a, an) && asNumber(b, bn)) return an == bn;

    if (a.type() == typeid(bool) && b.type() == typeid(bool))
        return std::any_cast<bool>(a) == std::any_cast<bool>(b);

    if (a.type() == typeid(emf::common::EObject*) && b.type() == typeid(emf::common::EObject*))
        return std::any_cast<emf::common::EObject*>(a) == std::any_cast<emf::common::EObject*>(b);

    // OCL Tuple 相等：部分数相同且按名逐项相等（顺序无关，对齐 OCL Tuple = 语义）
    OclTuplePtr ta, tb;
    if (asTuple(a, ta) || asTuple(b, tb)) {
        if (!asTuple(a, ta) || !asTuple(b, tb)) return false;
        if (ta->parts.size() != tb->parts.size()) return false;
        for (const auto& pa : ta->parts) {
            const auto* pb = tb->get(pa.first);
            if (!pb) return false;
            if (!valuesEqual(pa.second, *pb)) return false;
        }
        return true;
    }

    return false;
}

// 集合多集相等（顺序无关，重复计数）— 对齐 OCL Bag/Set = 语义
bool collectionsEqual(const std::vector<std::any>& a, const std::vector<std::any>& b) {
    if (a.size() != b.size()) return false;
    std::vector<bool> used(b.size(), false);
    for (const auto& av : a) {
        bool found = false;
        for (size_t i = 0; i < b.size(); ++i) {
            if (!used[i] && valuesEqual(av, b[i])) { used[i] = true; found = true; break; }
        }
        if (!found) return false;
    }
    return true;
}

// ===================== 求值上下文与求值器类型 =====================

// 求值上下文：携带 self（目标对象）、value（约束入参）、迭代变量绑定栈、
// let 变量绑定栈（支持 let 嵌套作用域）。
struct EvalContext {
    emf::common::EObject* self = nullptr;
    const std::any* value = nullptr;
    std::vector<std::pair<std::string, emf::common::EObject*>> vars;       // 迭代变量（EObject*）
    std::vector<std::pair<std::string, std::any>> letVars;                  // let 变量（任意值）

    bool isBoundVar(const std::string& n) const {
        for (auto it = vars.rbegin(); it != vars.rend(); ++it)
            if (it->first == n) return true;
        return false;
    }
    emf::common::EObject* lookupVar(const std::string& n) const {
        for (auto it = vars.rbegin(); it != vars.rend(); ++it)
            if (it->first == n) return it->second;
        return nullptr;
    }
    // 查找 let 变量（任意类型值）
    bool lookupLetVar(const std::string& n, std::any& out) const {
        for (auto it = letVars.rbegin(); it != letVars.rend(); ++it) {
            if (it->first == n) { out = it->second; return true; }
        }
        return false;
    }
};

// 统一表达式求值器：返回 std::any（布尔/数值/字符串/对象/集合）
using ExprEval = std::function<std::any(EvalContext&)>;

// ===================== 词法分析器 =====================

enum class Tok {
    Ident, Number, String,
    Arrow, Dot, Pipe, Comma, Minus, Plus, Star, Slash,
    Colon, Semicolon,
    LParen, RParen, LBrace, RBrace,
    RelOp,   // text 携带操作符：= == != <> > < >= <=
    And, Or, Not, Xor, Implies,
    True, False, Null,
    If, Then, Else, Endif,
    Let, In, Tuple,
    End, Error
};

struct Token {
    Tok type = Tok::End;
    std::string text;
    double num = 0.0;
};

bool isIdentStart(char c) { return std::isalpha(static_cast<unsigned char>(c)) || c == '_'; }
bool isIdentChar(char c) { return std::isalnum(static_cast<unsigned char>(c)) || c == '_'; }

std::vector<Token> tokenize(const std::string& s) {
    std::vector<Token> out;
    size_t i = 0, n = s.size();
    while (i < n) {
        char c = s[i];
        if (std::isspace(static_cast<unsigned char>(c))) { ++i; continue; }

        if (c == '-' && i + 1 < n && s[i + 1] == '>') { out.push_back({Tok::Arrow, "->"}); i += 2; continue; }
        if (c == '-') { out.push_back({Tok::Minus, "-"}); ++i; continue; }
        if (c == '+') { out.push_back({Tok::Plus, "+"}); ++i; continue; }
        if (c == '*') { out.push_back({Tok::Star, "*"}); ++i; continue; }
        if (c == '/') { out.push_back({Tok::Slash, "/"}); ++i; continue; }
        if (c == ':') { out.push_back({Tok::Colon, ":"}); ++i; continue; }
        if (c == ';') { out.push_back({Tok::Semicolon, ";"}); ++i; continue; }
        if (c == '.') { out.push_back({Tok::Dot, "."}); ++i; continue; }
        if (c == '|') { out.push_back({Tok::Pipe, "|"}); ++i; continue; }
        if (c == ',') { out.push_back({Tok::Comma, ","}); ++i; continue; }
        if (c == '(') { out.push_back({Tok::LParen, "("}); ++i; continue; }
        if (c == ')') { out.push_back({Tok::RParen, ")"}); ++i; continue; }
        if (c == '{') { out.push_back({Tok::LBrace, "{"}); ++i; continue; }
        if (c == '}') { out.push_back({Tok::RBrace, "}"}); ++i; continue; }

        if (c == '&' && i + 1 < n && s[i + 1] == '&') { out.push_back({Tok::And, "&&"}); i += 2; continue; }
        if (c == '|' && i + 1 < n && s[i + 1] == '|') { out.push_back({Tok::Or, "||"}); i += 2; continue; }
        if (c == '!') {
            if (i + 1 < n && s[i + 1] == '=') { out.push_back({Tok::RelOp, "!="}); i += 2; }
            else { out.push_back({Tok::Not, "!"}); ++i; }
            continue;
        }
        if (c == '=') {
            if (i + 1 < n && s[i + 1] == '=') { out.push_back({Tok::RelOp, "=="}); i += 2; }
            else { out.push_back({Tok::RelOp, "="}); ++i; }
            continue;
        }
        if (c == '<') {
            if (i + 1 < n && s[i + 1] == '=') { out.push_back({Tok::RelOp, "<="}); i += 2; }
            else if (i + 1 < n && s[i + 1] == '>') { out.push_back({Tok::RelOp, "<>"}); i += 2; }
            else { out.push_back({Tok::RelOp, "<"}); ++i; }
            continue;
        }
        if (c == '>') {
            if (i + 1 < n && s[i + 1] == '=') { out.push_back({Tok::RelOp, ">="}); i += 2; }
            else { out.push_back({Tok::RelOp, ">"}); ++i; }
            continue;
        }

        // 字符串字面量
        if (c == '\'' || c == '"') {
            char q = c; ++i;
            std::string str;
            while (i < n && s[i] != q) { str += s[i]; ++i; }
            if (i < n) ++i;  // 跳过闭合引号
            out.push_back({Tok::String, str});
            continue;
        }

        // 数字字面量
        if (std::isdigit(static_cast<unsigned char>(c))) {
            std::string num;
            while (i < n && (std::isdigit(static_cast<unsigned char>(s[i])) || s[i] == '.')) { num += s[i]; ++i; }
            double val = 0.0;
            try { val = std::stod(num); } catch (...) {}
            out.push_back({Tok::Number, num, val});
            continue;
        }

        // 标识符 / 关键字
        if (isIdentStart(c)) {
            std::string id;
            while (i < n && isIdentChar(s[i])) { id += s[i]; ++i; }
            if (id == "and") out.push_back({Tok::And, id});
            else if (id == "or") out.push_back({Tok::Or, id});
            else if (id == "not") out.push_back({Tok::Not, id});
            else if (id == "xor") out.push_back({Tok::Xor, id});
            else if (id == "implies") out.push_back({Tok::Implies, id});
            else if (id == "true") out.push_back({Tok::True, id});
            else if (id == "false") out.push_back({Tok::False, id});
            else if (id == "null") out.push_back({Tok::Null, id});
            else if (id == "if") out.push_back({Tok::If, id});
            else if (id == "then") out.push_back({Tok::Then, id});
            else if (id == "else") out.push_back({Tok::Else, id});
            else if (id == "endif") out.push_back({Tok::Endif, id});
            else if (id == "let") out.push_back({Tok::Let, id});
            else if (id == "in") out.push_back({Tok::In, id});
            else if (id == "Tuple") out.push_back({Tok::Tuple, id});
            else out.push_back({Tok::Ident, id});
            continue;
        }

        out.push_back({Tok::Error, std::string(1, c)});
        ++i;
    }
    out.push_back({Tok::End, ""});
    return out;
}

// ===================== 递归下降解析器 =====================
//
// 文法（优先级低→高）：
//   expr           := implies
//   implies        := or ('implies' implies)?          // 右结合
//   or             := xor (('or'|'||') xor)*
//   xor            := and ('xor' and)*
//   and            := unary (('and'|'&&') unary)*
//   unary          := ('not'|'!') unary | '-' unary | relational
//   relational     := additive (relop additive)?       // = <> == != > < >= <=
//   additive       := multiplicative (('+'|'-') multiplicative)*
//   multiplicative := primary (('*'|'/') primary)*
//   primary        := '(' expr ')'
//                   | 'if' expr 'then' expr 'else' expr 'endif'
//                   | 'let' ident (':' type)? '=' expr 'in' expr
//                   | atom ('.' postfix | '->' arrowOp)*
//   postfix        := ident '(' args ')'               // 方法调用
//                   | ident                              // 属性导航
//   arrowOp        := forAll/exists/collect/select/reject/any '(' ident (':' type)? '|' expr ')'
//                   | iterate '(' ident ';' ident (':' type)? '=' expr '|' expr ')'
//                   | size/isEmpty/notEmpty '(' ')'

class Parser {
public:
    explicit Parser(std::vector<Token> toks) : toks_(std::move(toks)) {}

    bool ok() const { return ok_; }
    const Token& peek() const { return toks_[pos_]; }

    // 入口
    ExprEval parseTop() {
        ExprEval e = parseImplies();
        if (peek().type != Tok::End) ok_ = false;
        return e;
    }

private:
    std::vector<Token> toks_;
    size_t pos_ = 0;
    bool ok_ = true;

    void advance() { if (pos_ < toks_.size()) ++pos_; }
    void expect(Tok t) {
        if (peek().type != t) ok_ = false;
        else advance();
    }
    // 接受赋值 '=' 或 '=='（用于 let/iterate 中的 '=' 语法）
    void expectAssign() {
        if (peek().type == Tok::RelOp && (peek().text == "=" || peek().text == "==")) advance();
        else ok_ = false;
    }
    // 可选 ': Type' 类型注解（解析但忽略）
    void parseOptionalType() {
        if (peek().type == Tok::Colon) {
            advance();
            if (peek().type == Tok::Ident) advance();
            else ok_ = false;
        }
    }

    // implies（右结合，最低优先级）：A implies B = (not A) or B
    ExprEval parseImplies() {
        ExprEval left = parseOr();
        if (peek().type == Tok::Implies) {
            advance();
            ExprEval right = parseImplies();  // 右结合
            return [left, right](EvalContext& ctx) -> std::any {
                return std::any(!toBool(left(ctx)) || toBool(right(ctx)));
            };
        }
        return left;
    }

    ExprEval parseOr() {
        ExprEval left = parseXor();
        while (peek().type == Tok::Or) {
            advance();
            ExprEval right = parseXor();
            left = [left, right](EvalContext& ctx) -> std::any {
                return std::any(toBool(left(ctx)) || toBool(right(ctx)));
            };
        }
        return left;
    }

    ExprEval parseXor() {
        ExprEval left = parseAnd();
        while (peek().type == Tok::Xor) {
            advance();
            ExprEval right = parseAnd();
            left = [left, right](EvalContext& ctx) -> std::any {
                return std::any(toBool(left(ctx)) != toBool(right(ctx)));
            };
        }
        return left;
    }

    ExprEval parseAnd() {
        ExprEval left = parseUnary();
        while (peek().type == Tok::And) {
            advance();
            ExprEval right = parseUnary();
            left = [left, right](EvalContext& ctx) -> std::any {
                return std::any(toBool(left(ctx)) && toBool(right(ctx)));
            };
        }
        return left;
    }

    ExprEval parseUnary() {
        if (peek().type == Tok::Not) {
            advance();
            ExprEval operand = parseUnary();
            return [operand](EvalContext& ctx) -> std::any {
                return std::any(!toBool(operand(ctx)));
            };
        }
        if (peek().type == Tok::Minus) {
            advance();
            ExprEval operand = parseUnary();
            return [operand](EvalContext& ctx) -> std::any {
                double n;
                if (asNumber(operand(ctx), n)) return std::any(-n);
                return std::any{};
            };
        }
        return parseRelational();
    }

    // 比较运算：= <> == != > < >= <=
    ExprEval parseRelational() {
        ExprEval left = parseAdditive();
        if (peek().type == Tok::RelOp) {
            std::string op = peek().text;
            advance();
            ExprEval right = parseAdditive();
            return makeComparison(left, op, right);
        }
        return left;  // 裸值，由上层 toBool 强转
    }

    // 加减法（+ 同时支持字符串拼接）
    ExprEval parseAdditive() {
        ExprEval left = parseMultiplicative();
        while (peek().type == Tok::Plus || peek().type == Tok::Minus) {
            bool isPlus = (peek().type == Tok::Plus);
            advance();
            ExprEval right = parseMultiplicative();
            if (isPlus) {
                left = [left, right](EvalContext& ctx) -> std::any {
                    std::any lv = left(ctx), rv = right(ctx);
                    std::string ls, rs;
                    if (asString(lv, ls) && asString(rv, rs)) return std::any(ls + rs);
                    double ln, rn;
                    if (asNumber(lv, ln) && asNumber(rv, rn)) return std::any(ln + rn);
                    return std::any{};
                };
            } else {
                left = [left, right](EvalContext& ctx) -> std::any {
                    double ln, rn;
                    if (asNumber(left(ctx), ln) && asNumber(right(ctx), rn)) return std::any(ln - rn);
                    return std::any{};
                };
            }
        }
        return left;
    }

    // 乘除法
    ExprEval parseMultiplicative() {
        ExprEval left = parsePrimary();
        while (peek().type == Tok::Star || peek().type == Tok::Slash) {
            bool isMul = (peek().type == Tok::Star);
            advance();
            ExprEval right = parsePrimary();
            if (isMul) {
                left = [left, right](EvalContext& ctx) -> std::any {
                    double ln, rn;
                    if (asNumber(left(ctx), ln) && asNumber(right(ctx), rn)) return std::any(ln * rn);
                    return std::any{};
                };
            } else {
                left = [left, right](EvalContext& ctx) -> std::any {
                    double ln, rn;
                    if (asNumber(left(ctx), ln) && asNumber(right(ctx), rn) && rn != 0) return std::any(ln / rn);
                    return std::any{};
                };
            }
        }
        return left;
    }

    // 构建比较求值器
    ExprEval makeComparison(ExprEval left, const std::string& op, ExprEval right) {
        bool isEq = (op == "=" || op == "==");
        bool isNeq = (op == "<>" || op == "!=");
        if (isEq || isNeq) {
            return [left, right, isNeq](EvalContext& ctx) -> std::any {
                bool eq = valuesEqual(left(ctx), right(ctx));
                return std::any(isNeq ? !eq : eq);
            };
        }
        return [left, right, op](EvalContext& ctx) -> std::any {
            std::any lv = left(ctx), rv = right(ctx);
            double ln, rn;
            if (asNumber(lv, ln) && asNumber(rv, rn)) {
                if (op == ">") return std::any(ln > rn);
                if (op == "<") return std::any(ln < rn);
                if (op == ">=") return std::any(ln >= rn);
                if (op == "<=") return std::any(ln <= rn);
            }
            std::string ls, rs;
            if (asString(lv, ls) && asString(rv, rs)) {
                if (op == ">") return std::any(ls > rs);
                if (op == "<") return std::any(ls < rs);
                if (op == ">=") return std::any(ls >= rs);
                if (op == "<=") return std::any(ls <= rs);
            }
            return std::any(true);  // 非数值/字符串：容错通过
        };
    }

    ExprEval parsePrimary() {
        if (peek().type == Tok::LParen) {
            advance();
            ExprEval e = parseImplies();
            expect(Tok::RParen);
            return e;
        }
        if (peek().type == Tok::If) {
            return parseIf();
        }
        if (peek().type == Tok::Let) {
            return parseLet();
        }
        if (peek().type == Tok::Tuple) {
            return parseTupleLiteral();
        }
        ExprEval src = parseAtom();

        // 后缀：.method() / .attr / ->arrowOp
        while (ok_ && (peek().type == Tok::Dot || peek().type == Tok::Arrow)) {
            if (peek().type == Tok::Dot) {
                advance();
                src = parseDotPostfix(src);
            } else {
                advance();
                src = parseArrowPostfix(src);
            }
        }
        return src;
    }

    // if expr then expr else expr endif
    ExprEval parseIf() {
        expect(Tok::If);
        ExprEval cond = parseImplies();
        expect(Tok::Then);
        ExprEval thenE = parseImplies();
        expect(Tok::Else);
        ExprEval elseE = parseImplies();
        expect(Tok::Endif);
        return [cond, thenE, elseE](EvalContext& ctx) -> std::any {
            return toBool(cond(ctx)) ? thenE(ctx) : elseE(ctx);
        };
    }

    // let var (: Type)? = expr in body
    ExprEval parseLet() {
        expect(Tok::Let);
        std::string varName;
        if (peek().type == Tok::Ident) { varName = peek().text; advance(); }
        else { ok_ = false; }
        parseOptionalType();
        expectAssign();
        ExprEval initExpr = parseImplies();
        expect(Tok::In);
        ExprEval body = parseImplies();
        return [varName, initExpr, body](EvalContext& ctx) -> std::any {
            std::any initVal = initExpr(ctx);
            ctx.letVars.push_back({varName, initVal});
            std::any result = body(ctx);
            ctx.letVars.pop_back();
            return result;
        };
    }

    // Tuple 字面量（对齐 Eclipse OCL TupleLiteralExp）：
    //   Tuple { name1 (: Type1)? = expr1, name2 (: Type2)? = expr2, ... }
    // 类型注解解析但忽略（对齐 OCL 静态类型在动态求值中不参与）。
    // 每个 part 在求值时按出现顺序求值，存入 OclTuple.parts。
    // 空字面量 Tuple {} 合法（无 part），用于占位/哨兵。
    ExprEval parseTupleLiteral() {
        expect(Tok::Tuple);
        expect(Tok::LBrace);
        // part 列表：name (:Type)? = expr（',' 分隔）
        std::vector<std::pair<std::string, ExprEval>> partEvals;
        if (peek().type != Tok::RBrace) {
            do {
                if (peek().type != Tok::Ident) { ok_ = false; break; }
                std::string name = peek().text;
                advance();
                parseOptionalType();   // 可选 ': Type'（忽略）
                expectAssign();        // '=' 或 '=='
                ExprEval valExpr = parseImplies();
                partEvals.emplace_back(name, std::move(valExpr));
            } while (peek().type == Tok::Comma && (advance(), true));
        }
        expect(Tok::RBrace);
        return [partEvals](EvalContext& ctx) -> std::any {
            auto tup = std::make_shared<OclTuple>();
            tup->parts.reserve(partEvals.size());
            for (const auto& pe : partEvals) {
                tup->parts.emplace_back(pe.first, pe.second(ctx));
            }
            return std::any(tup);
        };
    }

    // 原子：null/true/false/数字/字符串/self/value/迭代变量/let变量/隐式self.attr
    ExprEval parseAtom() {
        const Token& t = peek();

        // 字面量
        if (t.type == Tok::Null) { advance(); return [](EvalContext&) -> std::any { return std::any{}; }; }
        if (t.type == Tok::True) { advance(); return [](EvalContext&) -> std::any { return std::any(true); }; }
        if (t.type == Tok::False) { advance(); return [](EvalContext&) -> std::any { return std::any(false); }; }
        if (t.type == Tok::Number) { double v = t.num; advance(); return [v](EvalContext&) -> std::any { return std::any(v); }; }
        if (t.type == Tok::String) { std::string v = t.text; advance(); return [v](EvalContext&) -> std::any { return std::any(v); }; }

        if (t.type != Tok::Ident) { ok_ = false; advance(); return [](EvalContext&) -> std::any { return std::any{}; }; }

        std::string first = t.text;
        advance();

        return [first](EvalContext& ctx) -> std::any {
            if (first == "value") return ctx.value ? *ctx.value : std::any{};
            if (first == "self") return std::any(ctx.self);
            std::any letVal;
            if (ctx.lookupLetVar(first, letVal)) return letVal;
            if (ctx.isBoundVar(first)) return std::any(ctx.lookupVar(first));
            // 隐式 self.attr
            return readAttr(ctx.self, first);
        };
    }

    // '.' 后缀：方法调用或属性导航
    ExprEval parseDotPostfix(ExprEval src) {
        if (peek().type != Tok::Ident) { ok_ = false; return src; }
        std::string name = peek().text;
        advance();

        if (peek().type == Tok::LParen) {
            // 方法调用
            advance();  // (
            std::vector<ExprEval> args;
            if (peek().type != Tok::RParen) {
                // oclIsKindOf/oclIsTypeOf 的参数是类型名（标识符），按字符串字面量处理
                if (name == "oclIsKindOf" || name == "oclIsTypeOf") {
                    if (peek().type == Tok::Ident) {
                        std::string typeName = peek().text;
                        advance();
                        args.push_back([typeName](EvalContext&) -> std::any { return std::any(typeName); });
                    } else {
                        ok_ = false;
                    }
                } else {
                    args.push_back(parseImplies());
                    while (peek().type == Tok::Comma) {
                        advance();
                        args.push_back(parseImplies());
                    }
                }
            }
            expect(Tok::RParen);
            return makeMethodCall(src, name, args);
        }
        // 属性导航
        return [src, name](EvalContext& ctx) -> std::any {
            std::any val = src(ctx);
            // OCL Tuple 按名访问（对齐 Eclipse OCL TupleLiteralPart 引用）
            OclTuplePtr tup;
            if (asTuple(val, tup)) {
                const auto* part = tup->get(name);
                return part ? *part : std::any{};
            }
            emf::common::EObject* obj = asEObject(val);
            if (!obj) return std::any{};
            return readAttr(obj, name);
        };
    }

    // '->' 后缀：集合操作
    ExprEval parseArrowPostfix(ExprEval src) {
        if (peek().type != Tok::Ident) { ok_ = false; return src; }
        std::string opName = peek().text;
        advance();

        if (opName == "forAll" || opName == "exists") {
            expect(Tok::LParen);
            std::string varName;
            if (peek().type == Tok::Ident) { varName = peek().text; advance(); }
            else { ok_ = false; }
            parseOptionalType();
            expect(Tok::Pipe);
            ExprEval body = parseImplies();
            expect(Tok::RParen);
            return makeIterator(src, opName, varName, body);
        }
        if (opName == "collect") {
            expect(Tok::LParen);
            std::string varName;
            if (peek().type == Tok::Ident) { varName = peek().text; advance(); }
            else { ok_ = false; }
            parseOptionalType();
            expect(Tok::Pipe);
            ExprEval body = parseImplies();
            expect(Tok::RParen);
            return makeCollect(src, varName, body);
        }
        if (opName == "select" || opName == "reject") {
            expect(Tok::LParen);
            std::string varName;
            if (peek().type == Tok::Ident) { varName = peek().text; advance(); }
            else { ok_ = false; }
            parseOptionalType();
            expect(Tok::Pipe);
            ExprEval body = parseImplies();
            expect(Tok::RParen);
            return makeSelectReject(src, opName, varName, body);
        }
        if (opName == "any") {
            expect(Tok::LParen);
            std::string varName;
            if (peek().type == Tok::Ident) { varName = peek().text; advance(); }
            else { ok_ = false; }
            parseOptionalType();
            expect(Tok::Pipe);
            ExprEval body = parseImplies();
            expect(Tok::RParen);
            return makeAny(src, varName, body);
        }
        if (opName == "iterate") {
            expect(Tok::LParen);
            std::string varName;
            if (peek().type == Tok::Ident) { varName = peek().text; advance(); }
            else { ok_ = false; }
            expect(Tok::Semicolon);
            std::string accName;
            if (peek().type == Tok::Ident) { accName = peek().text; advance(); }
            else { ok_ = false; }
            parseOptionalType();
            expectAssign();
            ExprEval init = parseImplies();
            expect(Tok::Pipe);
            ExprEval body = parseImplies();
            expect(Tok::RParen);
            return makeIterate(src, varName, accName, init, body);
        }
        if (opName == "size") {
            expect(Tok::LParen); expect(Tok::RParen);
            ExprEval s = src;
            return [s](EvalContext& ctx) -> std::any { return std::any(sizeOf(s(ctx))); };
        }
        if (opName == "isEmpty") {
            expect(Tok::LParen); expect(Tok::RParen);
            ExprEval s = src;
            return [s](EvalContext& ctx) -> std::any { return std::any(sizeOf(s(ctx)) == 0); };
        }
        if (opName == "notEmpty") {
            expect(Tok::LParen); expect(Tok::RParen);
            ExprEval s = src;
            return [s](EvalContext& ctx) -> std::any { return std::any(sizeOf(s(ctx)) > 0); };
        }
        // ===== SortedSet / OrderedSet 标准库操作 =====

        // sortedBy(v | expr)：按 expr 排序，返回 OrderedSet（vector<EObject*>）
        if (opName == "sortedBy") {
            expect(Tok::LParen);
            std::string varName;
            if (peek().type == Tok::Ident) { varName = peek().text; advance(); }
            else { ok_ = false; }
            parseOptionalType();
            expect(Tok::Pipe);
            ExprEval body = parseImplies();
            expect(Tok::RParen);
            return makeSortedBy(src, varName, body);
        }
        // first() / last()：取首/末元素（空集合返回 null）
        if (opName == "first" || opName == "last") {
            expect(Tok::LParen); expect(Tok::RParen);
            ExprEval s = src;
            bool isFirst = (opName == "first");
            return [s, isFirst](EvalContext& ctx) -> std::any {
                auto list = asAnyList(s(ctx));
                if (list.empty()) return std::any{};
                return isFirst ? list.front() : list.back();
            };
        }
        // at(index)：1-based 索引取元素（越界返回 null）
        if (opName == "at") {
            expect(Tok::LParen);
            ExprEval arg = parseImplies();
            expect(Tok::RParen);
            ExprEval s = src;
            return [s, arg](EvalContext& ctx) -> std::any {
                auto list = asAnyList(s(ctx));
                double idx;
                if (!asNumber(arg(ctx), idx)) return std::any{};
                int i = static_cast<int>(idx);
                if (i < 1 || i > static_cast<int>(list.size())) return std::any{};
                return list[static_cast<size_t>(i - 1)];
            };
        }
        // indexOf(elem)：返回元素首次出现的 1-based 索引（未找到返回 0）
        if (opName == "indexOf") {
            expect(Tok::LParen);
            ExprEval arg = parseImplies();
            expect(Tok::RParen);
            ExprEval s = src;
            return [s, arg](EvalContext& ctx) -> std::any {
                auto list = asAnyList(s(ctx));
                std::any target = arg(ctx);
                for (size_t i = 0; i < list.size(); ++i) {
                    if (valuesEqual(list[i], target)) return std::any(static_cast<int>(i + 1));
                }
                return std::any(0);
            };
        }
        // count(elem)：元素出现次数
        if (opName == "count") {
            expect(Tok::LParen);
            ExprEval arg = parseImplies();
            expect(Tok::RParen);
            ExprEval s = src;
            return [s, arg](EvalContext& ctx) -> std::any {
                auto list = asAnyList(s(ctx));
                std::any target = arg(ctx);
                int cnt = 0;
                for (auto& v : list) if (valuesEqual(v, target)) ++cnt;
                return std::any(cnt);
            };
        }
        // includes(elem) / excludes(elem)
        if (opName == "includes" || opName == "excludes") {
            expect(Tok::LParen);
            ExprEval arg = parseImplies();
            expect(Tok::RParen);
            ExprEval s = src;
            bool isIncludes = (opName == "includes");
            return [s, arg, isIncludes](EvalContext& ctx) -> std::any {
                auto list = asAnyList(s(ctx));
                std::any target = arg(ctx);
                for (auto& v : list) if (valuesEqual(v, target)) return std::any(isIncludes);
                return std::any(!isIncludes);
            };
        }
        // includesAll(col2) / excludesAll(col2)
        if (opName == "includesAll" || opName == "excludesAll") {
            expect(Tok::LParen);
            ExprEval arg = parseImplies();
            expect(Tok::RParen);
            ExprEval s = src;
            bool isIncludesAll = (opName == "includesAll");
            return [s, arg, isIncludesAll](EvalContext& ctx) -> std::any {
                auto list = asAnyList(s(ctx));
                auto other = asAnyList(arg(ctx));
                for (auto& o : other) {
                    bool found = false;
                    for (auto& v : list) if (valuesEqual(v, o)) { found = true; break; }
                    if (isIncludesAll && !found) return std::any(false);
                    if (!isIncludesAll && found) return std::any(false);
                }
                return std::any(true);
            };
        }
        // union(col2) / intersection(col2) / difference(col2)：集合运算（Bag 多集语义）
        if (opName == "union" || opName == "intersection" || opName == "difference") {
            expect(Tok::LParen);
            ExprEval arg = parseImplies();
            expect(Tok::RParen);
            ExprEval s = src;
            std::string op = opName;
            return [s, arg, op](EvalContext& ctx) -> std::any {
                auto a = asAnyList(s(ctx));
                auto b = asAnyList(arg(ctx));
                if (op == "union") {
                    for (auto& v : b) a.push_back(v);
                    return std::any(a);
                }
                std::vector<bool> consumed(b.size(), false);
                std::vector<std::any> result;
                for (auto& av : a) {
                    size_t mi = b.size();
                    for (size_t i = 0; i < b.size(); ++i) {
                        if (!consumed[i] && valuesEqual(av, b[i])) { mi = i; break; }
                    }
                    if (op == "intersection") {
                        if (mi < b.size()) { consumed[mi] = true; result.push_back(av); }
                    } else {  // difference：在 b 中匹配则消耗丢弃，否则保留
                        if (mi < b.size()) { consumed[mi] = true; }
                        else { result.push_back(av); }
                    }
                }
                return std::any(result);
            };
        }
        // asBag / asSequence / asOrderedSet：保持 vector 形式（统一表示）
        if (opName == "asBag" || opName == "asSequence" || opName == "asOrderedSet") {
            expect(Tok::LParen); expect(Tok::RParen);
            ExprEval s = src;
            return [s](EvalContext& ctx) -> std::any { return std::any(asAnyList(s(ctx))); };
        }
        // asSet：去重
        if (opName == "asSet") {
            expect(Tok::LParen); expect(Tok::RParen);
            ExprEval s = src;
            return [s](EvalContext& ctx) -> std::any {
                auto list = asAnyList(s(ctx));
                std::vector<std::any> result;
                for (auto& v : list) {
                    bool dup = false;
                    for (auto& r : result) if (valuesEqual(r, v)) { dup = true; break; }
                    if (!dup) result.push_back(v);
                }
                return std::any(result);
            };
        }
        // flatten()：递归扁平化嵌套集合
        if (opName == "flatten") {
            expect(Tok::LParen); expect(Tok::RParen);
            ExprEval s = src;
            return [s](EvalContext& ctx) -> std::any {
                auto list = asAnyList(s(ctx));
                std::vector<std::any> result;
                std::function<void(const std::any&)> rec = [&](const std::any& v) {
                    if (v.type() == typeid(std::vector<std::any>)) {
                        auto vec = std::any_cast<std::vector<std::any>>(v);
                        for (auto& e : vec) rec(e);
                    } else if (v.type() == typeid(std::vector<emf::common::EObject*>)) {
                        auto vec = std::any_cast<std::vector<emf::common::EObject*>>(v);
                        for (auto* e : vec) result.push_back(std::any(e));
                    } else if (v.type() == typeid(emf::common::EList<emf::common::EObject*>*)) {
                        auto* p = std::any_cast<emf::common::EList<emf::common::EObject*>*>(v);
                        if (p) for (auto* e : *p) result.push_back(std::any(e));
                    } else if (v.has_value()) {
                        result.push_back(v);
                    }
                };
                for (auto& v : list) rec(v);
                return std::any(result);
            };
        }
        // sum()：元素求和（数值累加 或 字符串拼接）
        if (opName == "sum") {
            expect(Tok::LParen); expect(Tok::RParen);
            ExprEval s = src;
            return [s](EvalContext& ctx) -> std::any {
                auto list = asAnyList(s(ctx));
                double total = 0.0;
                bool allNum = true;
                for (auto& v : list) {
                    double n;
                    if (asNumber(v, n)) total += n;
                    else { allNum = false; break; }
                }
                if (allNum) return std::any(total);
                std::string acc;
                for (auto& v : list) {
                    std::string sv;
                    if (asString(v, sv)) acc += sv;
                }
                return std::any(acc);
            };
        }
        ok_ = false;  // 不支持的集合操作
        return src;
    }

    // sortedBy：对集合元素按 expr 求值结果排序，返回 OrderedSet（vector<EObject*>）
    ExprEval makeSortedBy(ExprEval source, const std::string& varName, ExprEval body) {
        return [source, varName, body](EvalContext& ctx) -> std::any {
            auto elems = asElementList(source(ctx));
            std::vector<std::pair<std::any, emf::common::EObject*>> keyed;
            keyed.reserve(elems.size());
            for (auto* e : elems) {
                ctx.vars.push_back({varName, e});
                std::any key = body(ctx);
                ctx.vars.pop_back();
                keyed.push_back({key, e});
            }
            std::stable_sort(keyed.begin(), keyed.end(), [](const auto& a, const auto& b) {
                double an, bn;
                if (asNumber(a.first, an) && asNumber(b.first, bn)) return an < bn;
                std::string as, bs;
                if (asString(a.first, as) && asString(b.first, bs)) return as < bs;
                return false;
            });
            std::vector<emf::common::EObject*> result;
            result.reserve(keyed.size());
            for (auto& p : keyed) result.push_back(p.second);
            return std::any(result);
        };
    }

    // 构建迭代求值器（forAll / exists）
    // OCL 语义：空集合 forAll→true, exists→false；单值引用视为单元素集合。
    ExprEval makeIterator(ExprEval source, const std::string& op,
                          const std::string& varName, ExprEval body) {
        bool isForAll = (op == "forAll");
        return [source, varName, body, isForAll](EvalContext& ctx) -> std::any {
            auto elems = asElementList(source(ctx));
            if (isForAll) {
                for (auto* e : elems) {
                    ctx.vars.push_back({varName, e});
                    bool pass = toBool(body(ctx));
                    ctx.vars.pop_back();
                    if (!pass) return std::any(false);
                }
                return std::any(true);
            } else {  // exists
                for (auto* e : elems) {
                    ctx.vars.push_back({varName, e});
                    bool pass = toBool(body(ctx));
                    ctx.vars.pop_back();
                    if (pass) return std::any(true);
                }
                return std::any(false);
            }
        };
    }

    // collect：对每个元素求值 body，返回值集合（vector<std::any>）。
    // 嵌套 collect/select 自动扁平化（对齐 OCL Bag 语义）：若 body 返回
    // vector<std::any>（内层 collect）或 vector<EObject*>（select），
    // 将其元素逐个加入结果；其它值（含原始 EList 导航、标量）直接加入。
    // 原始 EList 嵌套可通过 ->flatten() 显式扁平化。
    ExprEval makeCollect(ExprEval source, const std::string& varName, ExprEval body) {
        return [source, varName, body](EvalContext& ctx) -> std::any {
            auto elems = asElementList(source(ctx));
            std::vector<std::any> result;
            for (auto* e : elems) {
                ctx.vars.push_back({varName, e});
                std::any v = body(ctx);
                ctx.vars.pop_back();
                if (v.type() == typeid(std::vector<std::any>)) {
                    auto vec = std::any_cast<std::vector<std::any>>(v);
                    for (auto& x : vec) result.push_back(x);
                } else if (v.type() == typeid(std::vector<emf::common::EObject*>)) {
                    auto vec = std::any_cast<std::vector<emf::common::EObject*>>(v);
                    for (auto* x : vec) result.push_back(std::any(x));
                } else {
                    result.push_back(v);
                }
            }
            return std::any(result);
        };
    }

    // select/reject：返回满足/不满足条件的子集（vector<EObject*>）
    ExprEval makeSelectReject(ExprEval source, const std::string& op,
                              const std::string& varName, ExprEval body) {
        bool isSelect = (op == "select");
        return [source, varName, body, isSelect](EvalContext& ctx) -> std::any {
            auto elems = asElementList(source(ctx));
            std::vector<emf::common::EObject*> result;
            for (auto* e : elems) {
                ctx.vars.push_back({varName, e});
                bool pass = toBool(body(ctx));
                ctx.vars.pop_back();
                if (pass == isSelect) result.push_back(e);
            }
            return std::any(result);
        };
    }

    // any：返回首个满足条件的元素，无则 null
    ExprEval makeAny(ExprEval source, const std::string& varName, ExprEval body) {
        return [source, varName, body](EvalContext& ctx) -> std::any {
            auto elems = asElementList(source(ctx));
            for (auto* e : elems) {
                ctx.vars.push_back({varName, e});
                bool pass = toBool(body(ctx));
                ctx.vars.pop_back();
                if (pass) return std::any(e);
            }
            return std::any{};  // null
        };
    }

    // iterate：通用迭代，accumulator 模式
    //   iterate(v; acc : Type = init | body) — body 中 v 和 acc 均可见
    ExprEval makeIterate(ExprEval source, const std::string& varName,
                         const std::string& accName, ExprEval init, ExprEval body) {
        return [source, varName, accName, init, body](EvalContext& ctx) -> std::any {
            auto elems = asElementList(source(ctx));
            std::any acc = init(ctx);
            for (auto* e : elems) {
                ctx.vars.push_back({varName, e});
                ctx.letVars.push_back({accName, acc});
                acc = body(ctx);
                ctx.letVars.pop_back();
                ctx.vars.pop_back();
            }
            return acc;
        };
    }

    // 方法调用分发：String/Integer/Real/通用对象操作
    ExprEval makeMethodCall(ExprEval src, const std::string& name, std::vector<ExprEval> args) {
        return [src, name, args](EvalContext& ctx) -> std::any {
            std::any val = src(ctx);

            // ===== 通用操作（适用于任意值）=====
            if (name == "size" || name == "length") {
                return std::any(sizeOf(val));
            }
            if (name == "oclIsUndefined") {
                return std::any(isNullValue(val));
            }
            if (name == "oclIsInvalid") {
                return std::any(false);  // 简化：始终 false
            }
            if (name == "oclType") {
                auto* obj = asEObject(val);
                if (obj && obj->eClass()) return std::any(obj->eClass());
                return std::any{};
            }
            if (name == "asSequence") {
                return std::any(asElementList(val));
            }
            if (name == "oclIsKindOf" || name == "oclIsTypeOf") {
                std::string typeName;
                if (!args.empty()) asString(args[0](ctx), typeName);
                auto* obj = asEObject(val);
                if (!obj || !obj->eClass()) return std::any(false);
                std::string clsName = obj->eClass()->getName();
                if (name == "oclIsTypeOf") return std::any(clsName == typeName);
                // oclIsKindOf: 自身或任一超类型匹配
                if (clsName == typeName) return std::any(true);
                for (auto* st : obj->eClass()->getEAllSuperTypes()) {
                    if (st && st->getName() == typeName) return std::any(true);
                }
                return std::any(false);
            }

            // ===== String 操作 =====
            std::string s;
            if (asString(val, s)) {
                if (name == "toUpper") {
                    std::string r;
                    r.reserve(s.size());
                    for (char c : s) r += static_cast<char>(std::toupper(static_cast<unsigned char>(c)));
                    return std::any(r);
                }
                if (name == "toLower") {
                    std::string r;
                    r.reserve(s.size());
                    for (char c : s) r += static_cast<char>(std::tolower(static_cast<unsigned char>(c)));
                    return std::any(r);
                }
                if (name == "trim") {
                    size_t a = s.find_first_not_of(" \t\n\r\f\v");
                    size_t b = s.find_last_not_of(" \t\n\r\f\v");
                    if (a == std::string::npos) return std::any(std::string{});
                    return std::any(s.substr(a, b - a + 1));
                }
                if (name == "concat" && !args.empty()) {
                    std::string other;
                    if (asString(args[0](ctx), other)) return std::any(s + other);
                    return std::any(s);
                }
                if (name == "substring" && args.size() >= 2) {
                    double sd, ed;
                    if (asNumber(args[0](ctx), sd) && asNumber(args[1](ctx), ed)) {
                        int si = static_cast<int>(sd), ei = static_cast<int>(ed);
                        if (si < 1) si = 1;
                        if (ei > static_cast<int>(s.size())) ei = static_cast<int>(s.size());
                        if (si > ei) return std::any(std::string{});
                        return std::any(s.substr(si - 1, ei - si + 1));
                    }
                    return std::any(std::string{});
                }
                if (name == "indexOf" && !args.empty()) {
                    std::string sub;
                    if (asString(args[0](ctx), sub)) {
                        size_t pos = s.find(sub);
                        return std::any(pos == std::string::npos ? 0 : static_cast<int>(pos + 1));
                    }
                    return std::any(0);
                }
                if (name == "startsWith" && !args.empty()) {
                    std::string sub;
                    if (asString(args[0](ctx), sub))
                        return std::any(s.size() >= sub.size() && s.compare(0, sub.size(), sub) == 0);
                    return std::any(false);
                }
                if (name == "endsWith" && !args.empty()) {
                    std::string sub;
                    if (asString(args[0](ctx), sub))
                        return std::any(s.size() >= sub.size() &&
                                        s.compare(s.size() - sub.size(), sub.size(), sub) == 0);
                    return std::any(false);
                }
                if (name == "toString") {
                    return std::any(s);
                }
            }

            // ===== Integer/Real 操作 =====
            double n;
            if (asNumber(val, n)) {
                if (name == "abs") return std::any(std::abs(n));
                if (name == "floor") return std::any(std::floor(n));
                if (name == "ceil") return std::any(std::ceil(n));
                if (name == "round") return std::any(std::round(n));
                if (name == "toString") return std::any(std::to_string(n));
                if (name == "toInteger") return std::any(static_cast<long long>(n));
                if (name == "toReal") return std::any(n);
                if (name == "max" && !args.empty()) {
                    double m;
                    if (asNumber(args[0](ctx), m)) return std::any(n > m ? n : m);
                    return std::any(n);
                }
                if (name == "min" && !args.empty()) {
                    double m;
                    if (asNumber(args[0](ctx), m)) return std::any(n < m ? n : m);
                    return std::any(n);
                }
                if (name == "mod" && !args.empty()) {
                    double m;
                    if (asNumber(args[0](ctx), m) && m != 0) return std::any(std::fmod(n, m));
                    return std::any(0.0);
                }
                if (name == "div" && !args.empty()) {
                    double m;
                    if (asNumber(args[0](ctx), m) && m != 0) return std::any(std::floor(n / m));
                    return std::any(0.0);
                }
            }

            // 未匹配的方法 → invalid（空 any）
            return std::any{};
        };
    }
};

}  // namespace

// ===================== 公共 API =====================

ExpressionEvaluator ConstraintParser::compile(const std::string& expr) {
    Parser parser(tokenize(expr));
    ExprEval e = parser.parseTop();
    if (!parser.ok()) {
        // 解析失败：容错返回恒 true（对齐 Java constraint 语法容错）
        return [](emf::common::EObject*, const std::any&) { return true; };
    }
    // 顶层 null-safe（对齐 EObjectValidator：target 为 null 时视为通过）
    return [e](emf::common::EObject* target, const std::any& value) -> bool {
        if (!target) return true;
        EvalContext ctx{target, &value, {}, {}};
        return toBool(e(ctx));
    };
}

Constraint* ConstraintParser::parse(const std::string& source,
                                    const std::string& name,
                                    const std::string& expr,
                                    Severity sev) {
    auto eval = compile(expr);
    Constraint::Evaluator wrapped = [eval](emf::common::EObject* target) {
        return eval(target, std::any{});
    };
    return new Constraint(wrapped, source, name, "constraint '" + name + "' failed", sev);
}

Constraint* registerConstraintFromString(EValidator& validator,
                                         const std::string& source,
                                         const std::string& name,
                                         const std::string& expr,
                                         Severity sev) {
    Constraint* c = ConstraintParser::parse(source, name, expr, sev);
    delete validator.registerConstraint(c);
    return c;
}

}  // namespace emf::validation
