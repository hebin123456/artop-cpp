// XcoreParser.h
// 对齐 Java: org.eclipse.emf.ecore.xcore.resource.XcoreResource（解析器部分）
//
// 手写递归下降解析器，将 .xcore 文本解析为 XPackage AST。
// 语法覆盖（对齐 Xcore 公开样本与规范）：
//   - package <qualified.name> { ... }
//   - annotation "uri" as Name
//   - @Directive @Directive(k=v,...) 修饰符
//   - [abstract|interface] class Name [extends A, B] { members }
//   - member: attributes / contains / refers / op / enum / type
//   - 属性修饰: final|readonly|volatile|transient|unsettable|derived|id
//   - Type[multi]? name [= default] [get { body }]
//   - op ReturnType name(params) [throws E1, E2] { body }
//   - enum Name { LITERAL [= v], ... }
//   - type Name wraps java.lang.TypeName
//
// 词法约定：
//   - 单行注释 // 和 /* */ 块注释
//   - 标识符 [A-Za-z_][A-Za-z0-9_]*
//   - 限定名 a.b.c
//   - 字符串字面量 "..."（带 \" 转义）
//   - 数字字面量
#pragma once

#include "emf/ecore/xcore/XcoreAst.h"
#include <memory>
#include <string>
#include <vector>
#include <stdexcept>

namespace emf::ecore::xcore {

// 解析异常
class XcoreParseException : public std::runtime_error {
public:
    explicit XcoreParseException(const std::string& msg)
        : std::runtime_error(msg) {}
};

// 解析器
// 对齐 Java: org.eclipse.emf.ecore.xcore.XcoreResource 的 parser 部分
class XcoreParser {
public:
    // 解析整段文本，返回 XPackage。失败抛 XcoreParseException。
    static std::shared_ptr<XPackage> parse(const std::string& source);

private:
    XcoreParser(const std::string& src);
    std::shared_ptr<XPackage> parsePackage();

    // ===== 词法 =====
    void skipWhitespaceAndComments();
    bool matchKeyword(const std::string& kw);
    bool consumeKeyword(const std::string& kw);
    std::string parseIdentifier();          // [A-Za-z_][A-Za-z0-9_]*
    std::string parseQualifiedName();       // a.b.c
    std::string parseStringLiteral();       // "..."
    long parseInteger();
    bool peekChar(char c);
    bool consumeChar(char c);
    void expectChar(char c, const std::string& what);

    // ===== AST 构造 =====
    void parseAnnotationDirectives(XPackage& pkg);
    std::vector<XAnnotation> parseAnnotations();
    void parsePackageBody(XPackage& pkg);
    std::shared_ptr<XClass> parseClass();
    std::shared_ptr<XEnum> parseEnum();
    std::shared_ptr<XDataType> parseDataType();
    void parseClassBody(XClass& cls);
    // 解析类成员的修饰符链（@注解 / abstract / readonly 等）
    struct MemberMods {
        std::vector<XAnnotation> annotations;
        bool readOnly = false;
        bool volatileFlag = false;
        bool transient = false;
        bool unsettable = false;
        bool derived = false;
        bool idFlag = false;
        bool unique = true;
        bool resolve = true;  // 默认 resolve=true
    };
    MemberMods parseMemberMods();
    // 判断当前位置是 attribute / reference / operation
    enum class MemberKind { Attribute, Reference, Operation };
    MemberKind classifyMember(const std::string& firstToken);

    // 解析方括号多重性：[?] 或 [] 或 [n..m]
    // 返回 multi=true 表示上界 -1；本子集只支持 [] 与单值。
    bool parseMultiplicity();
    // 解析花括号体 {...}（返回体内容，不解析内部语句）
    std::string parseBraceBody();

    const std::string& src_;
    size_t pos_ = 0;
};

}  // namespace emf::ecore::xcore
