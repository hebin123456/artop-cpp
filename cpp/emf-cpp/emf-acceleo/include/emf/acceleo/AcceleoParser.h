// AcceleoParser.h
// 对齐 Java: org.eclipse.acceleo.engine.service.AcceleoService 的 parser 部分
//   + org.eclipse.acceleo.model.mtpl.util.MtlResourceImpl
//
// .mtl 模板解析器：手写递归下降。
// 语法覆盖（Acceleo MTL 子集）：
//   [module name(arg : Type) ...]
//   [import uri/]
//   [extends module1, module2/]
//   [template public name(arg : Type) ...] body [/template]
//   [query public name(arg : Type) : RetType = expr /]
//   块语法：
//     静态文本（[ ] 之间的非 [...] 字符）
//     [expr/]                         — 表达式求值输出
//     [for (v | col)] ... [/for]
//     [for (v | col) sep (sepExpr)] ... [/for]
//     [if (cond)] ... [elseif (c)] ... [else] ... [/if]
//     [let v : T = expr] ... [/let]
//     [file (path, false)] ... [/file]
//     [protected (id)] ... [/protected]
//   表达式（AQL 子集）：
//     self, varName
//     "string", 123, true, false
//     expr.name          — 属性导航
//     expr->name(args)   — 集合操作（size/select/collect/filter/reject/first/etc）
//     expr.name(args)    — 对象方法调用
//     cond ? a : b       — 条件表达式
#pragma once

#include "emf/acceleo/AcceleoAst.h"
#include <memory>
#include <string>
#include <stdexcept>

namespace emf::acceleo {

class AcceleoParseException : public std::runtime_error {
public:
    explicit AcceleoParseException(const std::string& msg)
        : std::runtime_error(msg) {}
};

class AcceleoParser {
public:
    // 解析 .mtl 文本，返回 Module AST
    static std::shared_ptr<Module> parse(const std::string& source);

private:
    AcceleoParser(const std::string& src);
    std::shared_ptr<Module> parseModule();

    // ===== 词法 =====
    void skipWhitespace();
    bool peek(const std::string& s);
    bool consume(const std::string& s);
    void expect(const std::string& s, const std::string& what);
    // 块开标签后紧跟的换行被消费（对齐 Acceleo 行为）
    void skipLineAfterTag();
    std::string parseIdentifier();
    std::string parseQualifiedName();
    std::string parseStringLiteral();   // "..."
    long parseInteger();

    // ===== 模块体 =====
    void parseModuleHeader(Module& m);
    Param parseParam();                 // name : Type
    void parseModuleBody(Module& m);

    // ===== 模板 / 查询 =====
    std::shared_ptr<Template> parseTemplate();
    std::shared_ptr<Query> parseQuery();

    // ===== 块解析 =====
    // 从 pos_ 解析直到遇到 endTag（如 "template" 或 "for" 或 "if" 或 "file"）。
    // endTags 是候选结束标签列表（不含 [/）。
    // 遇到 [/<tag> 返回已解析的块序列。
    std::vector<BlockPtr> parseBlocks(const std::vector<std::string>& endTags);
    // 判断下一个 [...] 是块开始还是表达式，分派到对应 parser
    BlockPtr parseBracketBlock();
    // 解析静态文本直到下一个 '['
    std::string parseStaticText();
    // 解析 [expr/] 表达式块
    BlockPtr parseExprBlock();
    // 解析 [for ...] ... [/for]
    BlockPtr parseForBlock();
    // 解析 [if ...] ... [/if]
    BlockPtr parseIfBlock();
    // 解析 [let ...] ... [/let]
    BlockPtr parseLetBlock();
    // 解析 [file ...] ... [/file]
    BlockPtr parseFileBlock();
    // 解析 [protected ...] ... [/protected]
    BlockPtr parseProtectedBlock();

    // ===== 表达式解析 =====
    ExprPtr parseExpression();   // 入口：or → and → equality → additive → conditional → postfix → primary
    ExprPtr parseAnd();          // a and b
    ExprPtr parseEquality();     // a = b / a == b / a != b / a <> b
    ExprPtr parseAdditive();     // a + b / a - b
    ExprPtr parseConditional();  // cond ? a : b
    ExprPtr parsePrimary();      // 字面量 / 变量 / (expr)
    ExprPtr parsePostfix();      // primary 后接 .name / ->name(args)
    ExprPtr parseCallArgs(ExprPtr target, const std::string& name, bool arrow);
    // 解析 call 的单个参数：尝试识别 lambda（var | body），否则退回普通表达式。
    ExprPtr parseCallArg();

    const std::string& src_;
    size_t pos_ = 0;
};

}  // namespace emf::acceleo
