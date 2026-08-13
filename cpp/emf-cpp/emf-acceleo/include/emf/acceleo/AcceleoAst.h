// AcceleoAst.h
// 对齐 Java: org.eclipse.acceleo.model.mtpl（MTL 模板模块元模型）
//
// .mtl 模板文件解析后的 AST 节点：
//   Module       ← [module gen(...)] ... [/module]
//   Template     ← [template public name(arg : Type)] ... [/template]
//   Query        ← [query public name(arg : Type) : RetType = expr /]
//   Block        ← 块表达式基类
//   TextBlock    ← 静态文本（[ ... ] 之间的字面量）
//   ExprBlock    ← [expr/] 表达式求值后输出
//   ForBlock     ← [for (var | col)] ... [/for]
//   IfBlock      ← [if (cond)] ... [elseif] ... [else] ... [/if]
//   LetBlock     ← [let var : T = expr] ... [/let]
//   FileBlock    ← [file (path, false)] ... [/file]
//   ProtectedBlock ← [protected] ... [/protected]
//
// 对齐 Java org.eclipse.acceleo.model.mtpl 的 EClass 集合。
#pragma once

#include <memory>
#include <string>
#include <vector>
#include <variant>

namespace emf::acceleo {

// 表达式 AST（AQL 子集）
// 对齐 Java org.eclipse.acceleo.query.ast
struct Expr;
using ExprPtr = std::shared_ptr<Expr>;

// 变量引用：self, var
struct VarExpr {
    std::string name;  // "self" 或其他变量名
};

// 字符串字面量："..."
struct StringLitExpr {
    std::string value;
};

// 整数字面量
struct IntLitExpr {
    long value = 0;
};

// 布尔字面量：true / false
struct BoolLitExpr {
    bool value = false;
};

// 属性访问：expr.name（导航到 EStructuralFeature）
struct NavExpr {
    ExprPtr target;
    std::string name;
};

// 方法调用：expr->name(args)  或  expr.name(args)
struct CallExpr {
    ExprPtr target;  // 可空（全局调用）
    std::string name;
    std::vector<ExprPtr> args;
    bool arrow = false;  // -> 还是 .
};

// 集合字面量：Collection { e1, e2 }（简化，本子集少用）
struct CollectionLitExpr {
    std::vector<ExprPtr> elements;
};

// if 表达式：cond ? then : else
struct IfExpr {
    ExprPtr cond;
    ExprPtr thenExpr;
    ExprPtr elseExpr;
};

// lambda 表达式：var | body（用于 collect/select/reject/forAll/exists 的回调参数）
// 对齐 AQL: e | expr  —— 把 e 作为迭代变量，body 中可引用 e
struct LambdaExpr {
    std::string varName;
    ExprPtr body;
};

// 表达式联合体
struct Expr {
    std::variant<VarExpr, StringLitExpr, IntLitExpr, BoolLitExpr,
                 NavExpr, CallExpr, CollectionLitExpr, IfExpr, LambdaExpr> node;
};

// ===== 模板参数 =====
struct Param {
    std::string name;
    std::string typeName;
};

// ===== 块 AST =====
// 对齐 Java org.eclipse.acceleo.model.block.Block
struct Block;
using BlockPtr = std::shared_ptr<Block>;

struct TextBlock {
    std::string text;  // 静态文本，原样输出
};

struct ExprBlock {
    ExprPtr expr;  // 求值后输出（转字符串）
};

struct ForBlock {
    std::string varName;      // 迭代变量
    std::string varTypeName;  // 可选类型
    ExprPtr collection;       // 被迭代的集合
    bool hasSeparator = false;
    std::string separator;    // [for (v | c) sep (s)] 的分隔符
    std::vector<BlockPtr> body;
};

struct IfBlock {
    ExprPtr cond;
    std::vector<BlockPtr> thenBody;
    std::vector<std::pair<ExprPtr, std::vector<BlockPtr>>> elseIfs;
    std::vector<BlockPtr> elseBody;  // 空 = 无 else
};

struct LetBlock {
    std::string varName;
    std::string varTypeName;
    ExprPtr value;
    std::vector<BlockPtr> body;
};

struct FileBlock {
    ExprPtr path;        // 文件路径表达式
    bool append = false; // [file (path, false)] 第二参：是否追加
    std::string charset; // 可选 charset
    std::vector<BlockPtr> body;
};

struct ProtectedBlock {
    std::string id;  // 可选 id（默认用模板名）
    std::vector<BlockPtr> body;
};

struct Block {
    // 一个 Block 是上述之一
    std::variant<TextBlock, ExprBlock, ForBlock, IfBlock, LetBlock,
                 FileBlock, ProtectedBlock> node;
};

// ===== 模板 / 查询 =====
// 对齐 Java org.eclipse.acceleo.model.mtpl.Template
struct Template {
    std::string name;
    std::vector<Param> params;
    std::vector<Param> overrides;  // [template public name(arg) overrides?] 罕见
    bool isPublic = true;
    bool isMain = false;
    std::vector<BlockPtr> body;
    std::string postLiteral;  // [post(...)] 后置文本（罕见，忽略）
};

// 对齐 Java org.eclipse.acceleo.model.mtpl.Query
struct Query {
    std::string name;
    std::vector<Param> params;
    std::string returnTypeName;
    ExprPtr body;  // = expr 中的表达式
};

// ===== 模块 =====
// 对齐 Java org.eclipse.acceleo.model.mtpl.Module
struct Module {
    std::string name;             // [module name(...)]
    std::vector<Param> params;    // 模块入口参数（通常 1 个 EObject 模型）
    std::vector<std::string> imports;  // [import uri]
    std::vector<std::string> extends;  // [extends module1, module2]
    std::vector<std::shared_ptr<Template>> templates;
    std::vector<std::shared_ptr<Query>> queries;
};

}  // namespace emf::acceleo
