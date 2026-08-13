// CppTemplates.h —— C++ 模板字符串 + 占位符替换 + JET 子集（each/if/unless）
// 对齐 Java: org.eclipse.emf.codegen.ecore.templates.model.{Class,FactoryClass,...}.javajet
//
// Java 侧用 JET（Java Emitter Templates）—— 一种 JSP 风格的字符串模板引擎，
// 把 GenModel 对象作为变量注入模板中。
//
// C++ 侧在简单 {{name}} 占位符之上，扩展以下 JET 子集：
//   {{#each rows}}     循环：rows 是 std::vector<std::map<std::string,std::string>>，
//     {{field}}        块内可直接访问 row 的字段。
//   {{/each}}
//   {{#if cond}}       条件：cond 在 vars 中存在且非空字符串时渲染。
//     ...
//   {{/if}}
//   {{#unless cond}}   反向条件：cond 不存在或为空字符串时渲染。
//     ...
//   {{/unless}}
// 支持 1 层 each 嵌套（each 块内可再有 each）。不支持 if 内 each 嵌套。
// 每张模板对应 Java 侧的一个 .javajet 文件：
//   PackageClass.javajet     -> templates::emitPackageHeader / emitPackageSource
//   FactoryClass.javajet     -> templates::emitFactoryHeader / emitFactorySource
//   SwitchClass.javajet      -> templates::emitSwitchHeader / emitSwitchSource
//   AdapterFactoryClass.javajet -> templates::emitAdapterFactoryHeader / emitAdapterFactorySource
//   ValidatorClass.javajet   -> templates::emitValidatorHeader / emitValidatorSource
//   Class.javajet            -> templates::emitInterfaceHeader / emitImplHeader / emitImplSource
#pragma once

#include "emf/ecore/codegen/GenModel.h"
#include <map>
#include <string>
#include <vector>

namespace emf::ecore::codegen {

// 模板上下文：把要注入到模板里的变量集中起来
struct TemplateContext {
    GenModel*     genModel  = nullptr;
    GenPackage*   genPackage = nullptr;     // 当前包
    GenClass*     genClass   = nullptr;     // 当前类（生成 Class 模板时设置）
    std::string   baseNamespace;            // e.g. "emf"（由调用方传）
    // 自由变量
    std::map<std::string, std::string> vars;
};

// 占位符替换：把 {{name}} 替换成 vars[name]
// 简单实现：仅一次扫描，不处理嵌套。
std::string renderTemplate(const std::string& tmpl, const std::map<std::string, std::string>& vars);

// JET 子集渲染：支持 {{#each list}}...{{/each}} / {{#if cond}}...{{/if}} / {{#unless cond}}...{{/unless}}
//  - list 类型：std::vector<std::map<std::string, std::string>>（each 的每一行）
//  - cond 真假：vars[cond] 存在且非空字符串
//  实现策略：递归解析控制块，从外到内依次处理。
//  返回：展开后的字符串
std::string renderJetTemplate(
    const std::string& tmpl,
    const std::map<std::string, std::string>& vars,
    const std::map<std::string, std::vector<std::map<std::string, std::string>>>& lists);

// ===== 模板：包头 =====
std::string emitPackageHeader(const TemplateContext& ctx);
std::string emitPackageSource(const TemplateContext& ctx);

// ===== 模板：Factory 头/源 =====
std::string emitFactoryHeader(const TemplateContext& ctx);
std::string emitFactorySource(const TemplateContext& ctx);

// ===== 模板：Switch 头/源 =====
std::string emitSwitchHeader(const TemplateContext& ctx);
std::string emitSwitchSource(const TemplateContext& ctx);

// ===== 模板：AdapterFactory 头/源 =====
std::string emitAdapterFactoryHeader(const TemplateContext& ctx);
std::string emitAdapterFactorySource(const TemplateContext& ctx);

// ===== 模板：Validator 头/源 =====
std::string emitValidatorHeader(const TemplateContext& ctx);
std::string emitValidatorSource(const TemplateContext& ctx);

// ===== 模板：Interface 头（一个 EClass 一份） =====
std::string emitInterfaceHeader(const TemplateContext& ctx);

// ===== 模板：Impl 头/源（一个 EClass 一份） =====
std::string emitImplHeader(const TemplateContext& ctx);
std::string emitImplSource(const TemplateContext& ctx);

// 暴露给测试：让单元测试能直接拿到模板字符串进行 assert
const std::string& packageHeaderTemplate();
const std::string& packageSourceTemplate();
const std::string& factoryHeaderTemplate();
const std::string& factorySourceTemplate();
const std::string& switchHeaderTemplate();
const std::string& switchSourceTemplate();
const std::string& adapterFactoryHeaderTemplate();
const std::string& adapterFactorySourceTemplate();
const std::string& validatorHeaderTemplate();
const std::string& validatorSourceTemplate();
const std::string& interfaceHeaderTemplate();
const std::string& implHeaderTemplate();
const std::string& implSourceTemplate();

}  // namespace emf::ecore::codegen
