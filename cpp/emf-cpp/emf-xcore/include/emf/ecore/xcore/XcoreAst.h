// XcoreAst.h
// 对齐 Java: org.eclipse.emf.ecore.xcore（Xcore 文本 DSL 元模型）
//
// Xcore 是基于 Xtext 的 Ecore 文本 DSL，加载 .xcore 文件后派生 EPackage。
// 本头定义 Xcore 源码解析后的 AST 节点（与 Java XcorePackage 概念对应）：
//   XPackage       ← package 声明
//   XClass         ← class 声明（abstract / interface）
//   XAttribute     ← 属性（EAttribute 等价物，可 derived get{}）
//   XReference     ← contains / refers 引用
//   XOperation     ← op 声明（含 body 文本）
//   XEnum          ← enum 声明
//   XEnumLiteral   ← enum 字面量
//   XDataType      ← type ... wraps ...
//
// AST 节点只持有源码层信息；Ecore 元模型实例由 XcoreGenerator 派生。
// 对齐 Java: org.eclipse.emf.ecore.xcore.XcorePackage 的 EClass 集合。
#pragma once

#include <memory>
#include <string>
#include <vector>
#include <optional>

namespace emf::ecore::xcore {

// 前向声明
class XPackage;
class XClassifier;
class XClass;
class XAttribute;
class XReference;
class XOperation;
class XEnum;
class XEnumLiteral;
class XDataType;
class XAnnotationDirective;
class XAnnotation;

// 语法出现的关键字前缀（对齐 Xcore 的 contains/refers/op/derived/type）
enum class ReferenceKind {
    Containment,  // contains
    NonContainment,  // refers
    Plain  // 无关键字（等价 contains 当类型是 EClass）
};

// ===== 注解指令（annotation "..." as Name）=====
// 对齐 Java: org.eclipse.emf.ecore.xcore.XAnnotationDirective
struct XAnnotationDirective {
    std::string name;       // as 后的别名
    std::string sourceURI;  // annotation 后的 URI 字符串
};

// ===== 注解（@Directive(key=value, ...)）=====
// 对齐 Java: org.eclipse.emf.ecore.xcore.XAnnotation
struct XAnnotation {
    std::string directiveName;                 // 引用的 directive 别名
    std::vector<std::pair<std::string, std::string>> details;  // key=value
};

// ===== 基类：所有顶层声明 =====
class XNamedElement {
public:
    virtual ~XNamedElement() = default;
    std::string name;
    std::vector<XAnnotation> annotations;
};

// ===== 枚举字面量 =====
// 对齐 Java: org.eclipse.emf.ecore.xcore.XEnumLiteral
class XEnumLiteral : public XNamedElement {
public:
    std::optional<int> value;  // 显式 value，否则自动递增
    std::string literal;       // 字面量字符串
};

// ===== 枚举 =====
// 对齐 Java: org.eclipse.emf.ecore.xcore.XEnum
class XEnum : public XNamedElement {
public:
    std::vector<std::shared_ptr<XEnumLiteral>> literals;
};

// ===== 数据类型（type X wraps java.lang.Y）=====
// 对齐 Java: org.eclipse.emf.ecore.xcore.XDataType
class XDataType : public XNamedElement {
public:
    std::string wrappedClassName;  // wraps 后的 Java 类名
};

// ===== 属性 =====
// 对齐 Java: org.eclipse.emf.ecore.xcore.XAttribute
// 语法：[final|readonly|volatile|transient|unsettable|derived|id] Type[?] name [= default] [get { body }]
class XAttribute : public XNamedElement {
public:
    std::string typeName;        // Xcore 类型名（String/int/long/boolean/枚举名/包.类名）
    bool multi = false;          // [] 后缀 → upperBound = -1
    bool derived = false;
    bool transient = false;
    bool unsettable = false;
    bool readOnly = false;
    bool volatileFlag = false;
    bool idFlag = false;
    std::optional<std::string> defaultValueLiteral;
    std::optional<std::string> getterBody;  // derived long X get { ... } 的体
};

// ===== 引用 =====
// 对齐 Java: org.eclipse.emf.ecore.xcore.XReference
// 语法：[contains|refers|readonly|volatile|transient|unsettable|derived|resolve] Type[?] name [opposite Name]
class XReference : public XNamedElement {
public:
    std::string typeName;
    ReferenceKind kind = ReferenceKind::Plain;
    bool multi = false;
    bool derived = false;
    bool transient = false;
    bool unsettable = false;
    bool readOnly = false;
    bool volatileFlag = false;
    bool resolveProxies = true;
    std::optional<std::string> oppositeName;
    std::optional<std::string> getterBody;
};

// ===== 操作 =====
// 对齐 Java: org.eclipse.emf.ecore.xcore.XOperation
// 语法：op Type name(params) [throws E1, E2] { body }
class XParameter : public XNamedElement {
public:
    std::string typeName;
};

class XOperation : public XNamedElement {
public:
    std::string typeName;        // 返回类型
    std::vector<std::shared_ptr<XParameter>> parameters;
    std::vector<std::string> exceptions;
    std::optional<std::string> body;  // 操作体文本（不编译，仅保留）
};

// ===== 类 =====
// 对齐 Java: org.eclipse.emf.ecore.xcore.XClass
// 语法：[abstract|interface] class Name [extends Super1, Super2] { members }
class XClass : public XNamedElement {
public:
    bool isAbstract = false;
    bool isInterface = false;
    std::vector<std::string> superTypes;  // extends 后的类名
    std::vector<std::shared_ptr<XAttribute>> attributes;
    std::vector<std::shared_ptr<XReference>> references;
    std::vector<std::shared_ptr<XOperation>> operations;
};

// ===== 包 =====
// 对齐 Java: org.eclipse.emf.ecore.xcore.XPackage
// 语法：[@Ecore(nsURI="...")] package qualified.name { ... }
class XPackage : public XNamedElement {
public:
    std::string nsURI;        // @Ecore(nsURI="...")
    std::string nsPrefix;     // @Ecore(nsPrefix="...")，缺省为包最后一段
    std::vector<std::shared_ptr<XAnnotationDirective>> annotationDirectives;
    std::vector<std::shared_ptr<XClass>> classes;
    std::vector<std::shared_ptr<XEnum>> enums;
    std::vector<std::shared_ptr<XDataType>> dataTypes;
};

}  // namespace emf::ecore::xcore
