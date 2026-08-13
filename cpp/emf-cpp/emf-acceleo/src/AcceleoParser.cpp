// AcceleoParser.cpp — .mtl 模板解析
// 对齐 Java: org.eclipse.acceleo.model.mtpl.util.MtlResourceImpl
//
// 解析策略：扫描源码，区分静态文本与 [...] 块。
// [...] 块内按首 token 分派：template/query/for/if/let/file/protected/import/extends/module
// 其它 [...] 视为表达式求值块。
#include "emf/acceleo/AcceleoParser.h"
#include <cctype>
#include <sstream>

namespace emf::acceleo {

static bool isIdentStart(char c) { return std::isalpha(static_cast<unsigned char>(c)) || c == '_'; }
static bool isIdentPart(char c)  { return std::isalnum(static_cast<unsigned char>(c)) || c == '_'; }

AcceleoParser::AcceleoParser(const std::string& src) : src_(src) {}

std::shared_ptr<Module> AcceleoParser::parse(const std::string& source) {
    AcceleoParser p(source);
    return p.parseModule();
}

// ===== 词法 =====
void AcceleoParser::skipWhitespace() {
    while (pos_ < src_.size() && std::isspace(static_cast<unsigned char>(src_[pos_]))) ++pos_;
}

bool AcceleoParser::peek(const std::string& s) {
    if (pos_ + s.size() > src_.size()) return false;
    return src_.compare(pos_, s.size(), s) == 0;
}

bool AcceleoParser::consume(const std::string& s) {
    if (peek(s)) { pos_ += s.size(); return true; }
    return false;
}

void AcceleoParser::expect(const std::string& s, const std::string& what) {
    if (!consume(s)) {
        throw AcceleoParseException("expected '" + s + "' " + what + " at pos " + std::to_string(pos_));
    }
}

// 对齐 Acceleo/MTL 行为：块开标签（[template]/[file]/[for]/[if]/[let]/[protected]）
// 后紧跟的单独换行被消费（让模板可把标签独占一行而不污染输出）。
// 仅消费一个 \n（或 \r\n），不消费其他空白。
void AcceleoParser::skipLineAfterTag() {
    if (pos_ < src_.size() && src_[pos_] == '\r') {
        ++pos_;
        if (pos_ < src_.size() && src_[pos_] == '\n') ++pos_;
    } else if (pos_ < src_.size() && src_[pos_] == '\n') {
        ++pos_;
    }
}

std::string AcceleoParser::parseIdentifier() {
    skipWhitespace();
    size_t start = pos_;
    while (pos_ < src_.size() && isIdentPart(src_[pos_])) ++pos_;
    if (pos_ == start) {
        throw AcceleoParseException("expected identifier at pos " + std::to_string(pos_));
    }
    return src_.substr(start, pos_ - start);
}

std::string AcceleoParser::parseQualifiedName() {
    std::string n = parseIdentifier();
    while (true) {
        skipWhitespace();
        // 注意：'.' 之后可能是属性访问，这里只解析限定名
        if (pos_ < src_.size() && src_[pos_] == '.' &&
            pos_ + 1 < src_.size() && isIdentStart(src_[pos_ + 1])) {
            ++pos_;
            n += '.';
            n += parseIdentifier();
        } else break;
    }
    return n;
}

std::string AcceleoParser::parseStringLiteral() {
    skipWhitespace();
    // Acceleo AQL 用单引号 '...' 表示字符串；容忍双引号 "..."
    char quote = '"';
    if (pos_ < src_.size() && (src_[pos_] == '\'' || src_[pos_] == '"')) {
        quote = src_[pos_++];
    } else {
        throw AcceleoParseException("expected string literal at pos " + std::to_string(pos_));
    }
    std::string out;
    while (pos_ < src_.size() && src_[pos_] != quote) {
        char c = src_[pos_++];
        if (c == '\\' && pos_ < src_.size()) {
            char e = src_[pos_++];
            switch (e) {
                case 'n': out += '\n'; break;
                case 't': out += '\t'; break;
                case 'r': out += '\r'; break;
                case '\'': out += '\''; break;
                case '"': out += '"'; break;
                case '\\': out += '\\'; break;
                default: out += '\\'; out += e; break;
            }
        } else {
            out += c;
        }
    }
    if (pos_ < src_.size()) ++pos_;  // 消费结束引号
    return out;
}

long AcceleoParser::parseInteger() {
    skipWhitespace();
    size_t start = pos_;
    if (pos_ < src_.size() && (src_[pos_] == '-' || src_[pos_] == '+')) ++pos_;
    while (pos_ < src_.size() && std::isdigit(static_cast<unsigned char>(src_[pos_]))) ++pos_;
    if (pos_ == start) {
        throw AcceleoParseException("expected integer at pos " + std::to_string(pos_));
    }
    try { return std::stol(src_.substr(start, pos_ - start)); }
    catch (...) { throw AcceleoParseException("invalid integer at pos " + std::to_string(start)); }
}

// ===== 模块解析 =====
std::shared_ptr<Module> AcceleoParser::parseModule() {
    auto m = std::make_shared<Module>();
    skipWhitespace();
    // 必须 [module ...]
    expect("[", "to start module");
    if (!consume("module")) {
        throw AcceleoParseException("expected 'module' after [ at pos " + std::to_string(pos_));
    }
    parseModuleHeader(*m);
    // 消费 ] 结束 module 声明
    skipWhitespace();
    expect("]", "to end module header");

    parseModuleBody(*m);
    // 期望 [/module]
    expect("[", "for [/module]");
    expect("/", "for [/module]");
    if (!consume("module")) {
        throw AcceleoParseException("expected '[/module]' at pos " + std::to_string(pos_));
    }
    expect("]", "for [/module]");
    return m;
}

void AcceleoParser::parseModuleHeader(Module& m) {
    m.name = parseIdentifier();
    // 可选 (params)
    skipWhitespace();
    if (consume("(")) {
        if (!peek(")")) {
            m.params.push_back(parseParam());
            while (consume(",")) m.params.push_back(parseParam());
        }
        expect(")", "to close module params");
    }
    // 可选 extends A, B（对齐 Java: [module name(params) extends parent1, parent2]）
    // 也容忍空白行/注释混入（此处只处理 header 内的 extends）
    skipWhitespace();
    if (peek("extends")) {
        consume("extends");
        m.extends.push_back(parseQualifiedName());
        while (consume(",")) m.extends.push_back(parseQualifiedName());
    }
}

Param AcceleoParser::parseParam() {
    Param p;
    p.name = parseIdentifier();
    skipWhitespace();
    expect(":", "in parameter declaration");
    p.typeName = parseQualifiedName();
    return p;
}

void AcceleoParser::parseModuleBody(Module& m) {
    while (true) {
        // 跳过块外空白
        skipWhitespace();
        if (peek("[/module]") || pos_ >= src_.size()) break;
        // [import .../] [extends .../] [template ...] [query ...]
        if (!consume("[")) {
            // 非法字符
            throw AcceleoParseException("expected '[' at pos " + std::to_string(pos_));
        }
        if (consume("import")) {
            m.imports.push_back(parseStringLiteral());
            skipWhitespace();
            // 期望 /] 或 ]
            if (consume("/")) { expect("]", "after import"); }
            else { expect("]", "after import"); }
        } else if (consume("extends")) {
            m.extends.push_back(parseQualifiedName());
            while (consume(",")) m.extends.push_back(parseQualifiedName());
            skipWhitespace();
            if (consume("/")) { expect("]", "after extends"); }
            else { expect("]", "after extends"); }
        } else if (consume("template")) {
            auto t = parseTemplate();
            m.templates.push_back(t);
        } else if (consume("query")) {
            auto q = parseQuery();
            m.queries.push_back(q);
        } else {
            throw AcceleoParseException("unknown directive in module body at pos " + std::to_string(pos_));
        }
    }
}

std::shared_ptr<Template> AcceleoParser::parseTemplate() {
    auto t = std::make_shared<Template>();
    // 可选 public/private/protected（忽略可见性，对齐 Java 行为不影响求值）
    skipWhitespace();
    while (peek("public") || peek("private") || peek("protected")) {
        if (peek("public")) { consume("public"); t->isPublic = true; }
        else if (peek("private")) { consume("private"); t->isPublic = false; }
        else { consume("protected"); }
        skipWhitespace();
    }
    t->name = parseIdentifier();
    skipWhitespace();
    if (consume("(")) {
        if (!peek(")")) {
            t->params.push_back(parseParam());
            while (consume(",")) t->params.push_back(parseParam());
        }
        expect(")", "to close template params");
    }
    // 可选 post(...) （罕见，忽略但不报错）
    skipWhitespace();
    if (peek("post")) {
        consume("post");
        skipWhitespace();
        if (consume("(")) {
            // 跳过到匹配 )
            int depth = 1;
            while (pos_ < src_.size() && depth > 0) {
                if (src_[pos_] == '(') ++depth;
                else if (src_[pos_] == ')') --depth;
                if (depth > 0) ++pos_;
            }
            if (pos_ < src_.size()) ++pos_;  // 消费 )
        }
    }
    expect("]", "to end template header");
    skipLineAfterTag();

    // 模板体：块序列直到 [/template]
    t->body = parseBlocks({"template"});

    // 消费 [/template]
    expect("[", "after template body");
    expect("/", "after template body");
    if (!consume("template")) {
        throw AcceleoParseException("expected '[/template]' at pos " + std::to_string(pos_));
    }
    expect("]", "after template body");
    return t;
}

std::shared_ptr<Query> AcceleoParser::parseQuery() {
    auto q = std::make_shared<Query>();
    skipWhitespace();
    while (peek("public") || peek("private") || peek("protected")) {
        if (peek("public")) consume("public");
        else if (peek("private")) consume("private");
        else consume("protected");
        skipWhitespace();
    }
    q->name = parseIdentifier();
    skipWhitespace();
    if (consume("(")) {
        if (!peek(")")) {
            q->params.push_back(parseParam());
            while (consume(",")) q->params.push_back(parseParam());
        }
        expect(")", "to close query params");
    }
    skipWhitespace();
    expect(":", "in query return type");
    q->returnTypeName = parseQualifiedName();
    skipWhitespace();
    expect("=", "in query body");
    q->body = parseExpression();
    skipWhitespace();
    if (consume("/")) expect("]", "after query");
    else expect("]", "after query");
    return q;
}

// ===== 块解析 =====

std::string AcceleoParser::parseStaticText() {
    std::string out;
    while (pos_ < src_.size() && src_[pos_] != '[') {
        out += src_[pos_++];
    }
    return out;
}

std::vector<BlockPtr> AcceleoParser::parseBlocks(const std::vector<std::string>& endTags) {
    std::vector<BlockPtr> blocks;
    while (pos_ < src_.size()) {
        // 检测 [/endTag  —— 结束标签
        if (peek("[/")) {
            return blocks;
        }
        // 检测 [elseif / [else  —— 这些是 if 块的中间标签，应让 if 解析器处理
        if (peek("[elseif") || peek("[else")) {
            return blocks;
        }
        // 检测 [（块或表达式）
        if (peek("[")) {
            auto b = parseBracketBlock();
            if (b) blocks.push_back(b);
        } else {
            // 静态文本
            std::string txt = parseStaticText();
            if (!txt.empty()) {
                Block b; b.node = TextBlock{txt};
                blocks.push_back(std::make_shared<Block>(std::move(b)));
            }
        }
    }
    return blocks;
}

BlockPtr AcceleoParser::parseBracketBlock() {
    size_t startPos = pos_;
    expect("[", "for block");
    // 分派关键字
    skipWhitespace();
    if (consume("for")) return parseForBlock();
    if (consume("if")) return parseIfBlock();
    if (consume("let")) return parseLetBlock();
    if (consume("file")) return parseFileBlock();
    if (consume("protected")) return parseProtectedBlock();
    // 否则是表达式块 [expr/]
    // 回退到 '[' 之前，让 parseExprBlock 重新从 '[' 开始
    pos_ = startPos;
    return parseExprBlock();
}

BlockPtr AcceleoParser::parseExprBlock() {
    expect("[", "for expression block");
    ExprPtr e = parseExpression();
    skipWhitespace();
    // 期望 /]
    if (consume("/")) {
        expect("]", "to close expr block");
    } else {
        expect("]", "to close expr block");
    }
    Block b; b.node = ExprBlock{e};
    return std::make_shared<Block>(std::move(b));
}

BlockPtr AcceleoParser::parseForBlock() {
    auto f = std::make_shared<ForBlock>();
    skipWhitespace();
    expect("(", "in for");
    // (var | collection)  或  (var : Type | collection)
    f->varName = parseIdentifier();
    skipWhitespace();
    if (consume(":")) {
        f->varTypeName = parseQualifiedName();
        skipWhitespace();
    }
    expect("|", "in for");
    f->collection = parseExpression();
    expect(")", "in for");
    // 可选 sep (sepExpr)
    skipWhitespace();
    if (consume("sep")) {
        f->hasSeparator = true;
        skipWhitespace();
        expect("(", "in for sep");
        // sep 表达式通常为字符串字面量
        // 简化：求值为字符串字面量
        if (pos_ < src_.size() && (src_[pos_] == '\'' || src_[pos_] == '"')) {
            f->separator = parseStringLiteral();
        } else {
            // 一般表达式，存为字符串表示（本子集不支持复杂 sep）
            auto e = parseExpression();
            (void)e;
        }
        expect(")", "in for sep");
    }
    expect("]", "to end for header");
    skipLineAfterTag();

    f->body = parseBlocks({"for"});

    // 消费 [/for]
    expect("[", "after for body");
    expect("/", "after for body");
    if (!consume("for")) {
        throw AcceleoParseException("expected '[/for]' at pos " + std::to_string(pos_));
    }
    expect("]", "after for body");
    Block b; b.node = *f;
    return std::make_shared<Block>(std::move(b));
}

BlockPtr AcceleoParser::parseIfBlock() {
    auto ifb = std::make_shared<IfBlock>();
    skipWhitespace();
    expect("(", "in if");
    ifb->cond = parseExpression();
    expect(")", "in if");
    expect("]", "to end if header");
    skipLineAfterTag();

    ifb->thenBody = parseBlocks({"if", "elseif", "else"});

    // 可选 elseif
    while (peek("[elseif")) {
        expect("[", "for elseif");
        consume("elseif");
        skipWhitespace();
        expect("(", "in elseif");
        ExprPtr c = parseExpression();
        expect(")", "in elseif");
        expect("]", "to end elseif header");
        skipLineAfterTag();
        auto body = parseBlocks({"if", "elseif", "else"});
        ifb->elseIfs.emplace_back(c, body);
    }
    // 可选 else
    if (peek("[else")) {
        expect("[", "for else");
        consume("else");
        expect("]", "to end else header");
        skipLineAfterTag();
        ifb->elseBody = parseBlocks({"if"});
    }
    // 消费 [/if]
    expect("[", "after if body");
    expect("/", "after if body");
    if (!consume("if")) {
        throw AcceleoParseException("expected '[/if]' at pos " + std::to_string(pos_));
    }
    expect("]", "after if body");
    Block b; b.node = *ifb;
    return std::make_shared<Block>(std::move(b));
}

BlockPtr AcceleoParser::parseLetBlock() {
    auto lb = std::make_shared<LetBlock>();
    skipWhitespace();
    lb->varName = parseIdentifier();
    skipWhitespace();
    if (consume(":")) {
        lb->varTypeName = parseQualifiedName();
        skipWhitespace();
    }
    expect("=", "in let");
    lb->value = parseExpression();
    expect("]", "to end let header");
    skipLineAfterTag();
    lb->body = parseBlocks({"let"});
    // 消费 [/let]
    expect("[", "after let body");
    expect("/", "after let body");
    if (!consume("let")) {
        throw AcceleoParseException("expected '[/let]' at pos " + std::to_string(pos_));
    }
    expect("]", "after let body");
    Block b; b.node = *lb;
    return std::make_shared<Block>(std::move(b));
}

BlockPtr AcceleoParser::parseFileBlock() {
    auto fb = std::make_shared<FileBlock>();
    skipWhitespace();
    expect("(", "in file");
    fb->path = parseExpression();
    skipWhitespace();
    if (consume(",")) {
        // 第二参：是否 append（bool 表达式，简化为 true/false 字面量）
        skipWhitespace();
        if (peek("true")) { consume("true"); fb->append = true; }
        else if (peek("false")) { consume("false"); fb->append = false; }
        // 第三参可选 charset
        skipWhitespace();
        if (consume(",")) {
            fb->charset = parseStringLiteral();
        }
    }
    expect(")", "in file");
    expect("]", "to end file header");
    skipLineAfterTag();
    fb->body = parseBlocks({"file"});
    // 消费 [/file]
    expect("[", "after file body");
    expect("/", "after file body");
    if (!consume("file")) {
        throw AcceleoParseException("expected '[/file]' at pos " + std::to_string(pos_));
    }
    expect("]", "after file body");
    Block b; b.node = *fb;
    return std::make_shared<Block>(std::move(b));
}

BlockPtr AcceleoParser::parseProtectedBlock() {
    auto pb = std::make_shared<ProtectedBlock>();
    skipWhitespace();
    // 可选 (id)：id 可以是字符串字面量 'id' 或裸 identifier id
    // 对齐 Java Acceleo: [protected ('id')] 与 [protected (id)] 均允许
    if (consume("(")) {
        skipWhitespace();
        if (pos_ < src_.size() && (src_[pos_] == '\'' || src_[pos_] == '"')) {
            pb->id = parseStringLiteral();
        } else {
            pb->id = parseIdentifier();
        }
        expect(")", "in protected");
    }
    expect("]", "to end protected header");
    skipLineAfterTag();
    pb->body = parseBlocks({"protected"});
    // 消费 [/protected]
    expect("[", "after protected body");
    expect("/", "after protected body");
    if (!consume("protected")) {
        throw AcceleoParseException("expected '[/protected]' at pos " + std::to_string(pos_));
    }
    expect("]", "after protected body");
    Block b; b.node = *pb;
    return std::make_shared<Block>(std::move(b));
}

// ===== 表达式解析 =====

ExprPtr AcceleoParser::parseExpression() {
    // 逻辑 or：a or b
    ExprPtr left = parseAnd();
    skipWhitespace();
    while (peek("or")) {
        consume("or");
        ExprPtr right = parseAnd();
        auto e = std::make_shared<Expr>();
        // 用 CallExpr 模拟：or(left, right) —— 求值时按 bool 处理
        e->node = CallExpr{nullptr, "or", {left, right}, false};
        left = e;
        skipWhitespace();
    }
    return left;
}

ExprPtr AcceleoParser::parseAnd() {
    ExprPtr left = parseEquality();
    skipWhitespace();
    while (peek("and")) {
        consume("and");
        ExprPtr right = parseEquality();
        auto e = std::make_shared<Expr>();
        e->node = CallExpr{nullptr, "and", {left, right}, false};
        left = e;
        skipWhitespace();
    }
    return left;
}

ExprPtr AcceleoParser::parseEquality() {
    ExprPtr left = parseAdditive();
    skipWhitespace();
    while (true) {
        if (peek("==")) {
            consume("==");
            ExprPtr right = parseAdditive();
            auto e = std::make_shared<Expr>();
            e->node = CallExpr{nullptr, "==", {left, right}, false};
            left = e;
        } else if (peek("!=")) {
            consume("!=");
            ExprPtr right = parseAdditive();
            auto e = std::make_shared<Expr>();
            e->node = CallExpr{nullptr, "!=", {left, right}, false};
            left = e;
        } else if (peek("<>")) {
            consume("<>");
            ExprPtr right = parseAdditive();
            auto e = std::make_shared<Expr>();
            e->node = CallExpr{nullptr, "!=", {left, right}, false};
            left = e;
        } else if (peek("=") && !peek("==")) {
            // Acceleo AQL 用 = 表示相等（对齐 OCL）
            consume("=");
            ExprPtr right = parseAdditive();
            auto e = std::make_shared<Expr>();
            e->node = CallExpr{nullptr, "==", {left, right}, false};
            left = e;
        } else break;
        skipWhitespace();
    }
    return left;
}

ExprPtr AcceleoParser::parseAdditive() {
    ExprPtr left = parseConditional();
    skipWhitespace();
    while (peek("+") || peek("-")) {
        std::string op;
        if (peek("+")) { consume("+"); op = "+"; }
        else { consume("-"); op = "-"; }
        ExprPtr right = parseConditional();
        auto e = std::make_shared<Expr>();
        e->node = CallExpr{left, op, {right}, false};
        left = e;
        skipWhitespace();
    }
    return left;
}

ExprPtr AcceleoParser::parseConditional() {
    ExprPtr cond = parsePostfix();
    skipWhitespace();
    if (peek("?")) {
        consume("?");
        ExprPtr thenE = parseExpression();
        skipWhitespace();
        expect(":", "in conditional");
        ExprPtr elseE = parseExpression();
        auto e = std::make_shared<Expr>();
        e->node = IfExpr{cond, thenE, elseE};
        return e;
    }
    return cond;
}

ExprPtr AcceleoParser::parsePrimary() {
    skipWhitespace();
    // 字符串字面量（单引号或双引号）
    if (pos_ < src_.size() && (src_[pos_] == '\'' || src_[pos_] == '"')) {
        auto e = std::make_shared<Expr>();
        e->node = StringLitExpr{parseStringLiteral()};
        return e;
    }
    // 整数
    if (pos_ < src_.size() && (std::isdigit(static_cast<unsigned char>(src_[pos_])) ||
                               (src_[pos_] == '-' && pos_ + 1 < src_.size() &&
                                std::isdigit(static_cast<unsigned char>(src_[pos_ + 1]))))) {
        auto e = std::make_shared<Expr>();
        e->node = IntLitExpr{parseInteger()};
        return e;
    }
    // true / false
    if (peek("true")) {
        consume("true");
        auto e = std::make_shared<Expr>();
        e->node = BoolLitExpr{true};
        return e;
    }
    if (peek("false")) {
        consume("false");
        auto e = std::make_shared<Expr>();
        e->node = BoolLitExpr{false};
        return e;
    }
    // (expr)
    if (peek("(")) {
        consume("(");
        ExprPtr inner = parseExpression();
        expect(")", "in parenthesized expr");
        return inner;
    }
    // 变量名 / 裸函数调用 name(args)
    // 注意：只解析单个标识符，不要用 parseQualifiedName() —— 否则会把
    // "c.name" 整体吞掉变成 VarExpr{"c.name"}，而 '.' 应由 parsePostfix
    // 处理为 NavExpr（属性导航）。AQL 中命名空间限定用 '::' 不用 '.'。
    std::string name = parseIdentifier();
    skipWhitespace();
    // 裸函数调用：name(args) —— 无 target，对齐 AQL 全局服务/查询调用
    if (peek("(")) {
        consume("(");
        std::vector<ExprPtr> args;
        if (!peek(")")) {
            args.push_back(parseExpression());
            while (consume(",")) args.push_back(parseExpression());
        }
        expect(")", "in bare call args");
        auto e = std::make_shared<Expr>();
        e->node = CallExpr{nullptr, name, args, false};
        return e;
    }
    auto e = std::make_shared<Expr>();
    e->node = VarExpr{name};
    return e;
}

ExprPtr AcceleoParser::parsePostfix() {
    ExprPtr e = parsePrimary();
    while (true) {
        skipWhitespace();
        if (peek("->")) {
            consume("->");
            std::string name = parseIdentifier();
            e = parseCallArgs(e, name, true);
        } else if (peek(".")) {
            // .name 但要区分 .name(args) 与 .name
            size_t saved = pos_;
            consume(".");
            // 必须后面是标识符
            if (pos_ < src_.size() && isIdentStart(src_[pos_])) {
                std::string name = parseIdentifier();
                e = parseCallArgs(e, name, false);
            } else {
                pos_ = saved;
                break;
            }
        } else {
            break;
        }
    }
    return e;
}

ExprPtr AcceleoParser::parseCallArgs(ExprPtr target, const std::string& name, bool arrow) {
    skipWhitespace();
    if (peek("(")) {
        consume("(");
        std::vector<ExprPtr> args;
        if (!peek(")")) {
            args.push_back(parseCallArg());
            while (consume(",")) args.push_back(parseCallArg());
        }
        expect(")", "in call args");
        auto e = std::make_shared<Expr>();
        e->node = CallExpr{target, name, args, arrow};
        return e;
    }
    // 无 ()：当作属性导航（->name 也允许，对齐 AQL 的 size / first 等）
    auto e = std::make_shared<Expr>();
    if (arrow) {
        // ->name 无 () 也可视为 0 参 call（对齐 ->size）
        e->node = CallExpr{target, name, {}, true};
    } else {
        e->node = NavExpr{target, name};
    }
    return e;
}

// 解析 call 的单个参数：尝试识别 lambda 形式（var | body），否则退回普通表达式。
// 对齐 AQL: collect(e | e.name) / select(e | e.active) 等。
// 语法：identifier | expression  —— identifier 不能是关键字，且 | 紧跟其后。
ExprPtr AcceleoParser::parseCallArg() {
    skipWhitespace();
    // 尝试 lambda：identifier | expr
    size_t saved = pos_;
    // 先看是否是 identifier
    if (pos_ < src_.size() && (isIdentStart(src_[pos_]) || src_[pos_] == '_')) {
        // 解析标识符但不消费（用临时解析）
        size_t idStart = pos_;
        while (pos_ < src_.size() && isIdentPart(src_[pos_])) ++pos_;
        std::string id = src_.substr(idStart, pos_ - idStart);
        skipWhitespace();
        if (peek("|")) {
            consume("|");
            auto lam = std::make_shared<Expr>();
            ExprPtr body = parseExpression();
            lam->node = LambdaExpr{id, body};
            return lam;
        }
        // 不是 lambda，回退
        pos_ = saved;
    }
    return parseExpression();
}

}  // namespace emf::acceleo
