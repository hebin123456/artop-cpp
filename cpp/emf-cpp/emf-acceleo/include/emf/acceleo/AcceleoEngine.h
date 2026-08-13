// AcceleoEngine.h
// 对齐 Java: org.eclipse.acceleo.engine.generation.AcceleoEngine
//           + org.eclipse.acceleo.engine.service.AcceleoService
//
// 引擎职责：
//   1. 接收已解析的 Module AST
//   2. 求值模板（Template），输出文本
//   3. 支持 [file] 块写入文件
//   4. 支持 [protected] 块保留已存在文件的受保护区
//   5. AQL 表达式求值（基于 emf::ecore EObject 反射）
//
// 对齐 Java AcceleoService.doGenerate(IModel, Module, outputFolder)：
//   - doGenerate(model, module, outDir)
//   - evaluate(template, args) → string
//
// 服务注册（对齐 Java services）：可通过 registerService(name, fn) 注入
// C++ 函数，在模板里以 [name(args)/] 调用。
#pragma once

#include "emf/acceleo/AcceleoAst.h"
#include "emf/common/EObject.h"
#include <functional>
#include <memory>
#include <string>
#include <unordered_map>
#include <vector>
#include <any>

namespace emf::acceleo {

// 求值上下文：变量名 → 值（std::any）
// 对齐 Java org.eclipse.acceleo.engine.AcceleoEvaluationContext
struct EvalContext {
    std::unordered_map<std::string, std::any> vars;
    // 父上下文（用于 let/for 嵌套作用域）
    EvalContext* parent = nullptr;

    // 查找变量（沿父链向上）
    const std::any* lookup(const std::string& name) const {
        auto it = vars.find(name);
        if (it != vars.end()) return &it->second;
        if (parent) return parent->lookup(name);
        return nullptr;
    }
    // 设置/覆盖当前作用域变量
    void set(const std::string& name, std::any v) {
        vars[name] = std::move(v);
    }
};

// 前向声明
struct Query;

// 服务函数签名：args 是调用参数，ctx 是当前求值上下文
// 对齐 Java org.eclipse.acceleo.engine.service.AcceleoService 的 Java services
using ServiceFn = std::function<std::any(const std::vector<std::any>&, EvalContext&)>;

// AcceleoEngine：模板求值引擎
// 对齐 Java org.eclipse.acceleo.engine.generation.AcceleoEngine
class AcceleoEngine {
public:
    AcceleoEngine();

    // 注册 C++ 服务（对齐 Java 在 plugin.xml 注册 service）
    void registerService(const std::string& name, ServiceFn fn);
    bool hasService(const std::string& name) const;
    ServiceFn getService(const std::string& name) const;

    // 绑定模块的 queries，供模板表达式内以 queryName(args) 调用。
    // 对齐 Java: 模块加载后所有 query 自动可被本模块模板调用。
    void setModuleQueries(const std::vector<std::shared_ptr<Query>>& queries);
    // 查找模块级 query（按名），返回 nullptr 若不存在
    const Query* lookupQuery(const std::string& name) const;

    // 注册被 extends/import 的模块（对齐 Java 模块继承/导入）。
    // 主模块求值前，调用方需把被继承/导入的模块按名注册。
    // 引擎在查找 template/query 时会沿 extends 链查找。
    void registerModule(const std::shared_ptr<Module>& m);
    // 设置当前主模块（建立 extends 链查找上下文）
    void setCurrentModule(const std::shared_ptr<Module>& m);
    // 按名查找 template（含 extends 链）：先本模块，再 registeredModules_ 中 extends 列表
    std::shared_ptr<Template> lookupTemplate(const std::string& name) const;

    // 求值整个模板，返回生成的文本
    // 对齐 Java AcceleoEngine.evaluate(Template, args)
    std::string evaluate(const Template& tpl, const std::vector<std::any>& args);

    // 求值模块的主模板，把 [file] 块写入 outDir。
    // 对齐 Java AcceleoService.doGenerate(model, module, outDir)
    void doGenerate(const std::shared_ptr<Module>& module,
                    ::emf::common::EObject* model,
                    const std::string& outDir);

private:
    // 求值块序列，追加到 out
    void evalBlocks(const std::vector<BlockPtr>& blocks,
                    EvalContext& ctx,
                    std::string& out);
    // 求值单个块
    void evalBlock(const Block& b, EvalContext& ctx, std::string& out);
    // 求值表达式 → std::any
    std::any evalExpr(const Expr& e, EvalContext& ctx);
    // any 转字符串（对齐 Java toString）
    std::string anyToString(const std::any& v);

    // 把 [file] 块写入文件（处理 protected 区保留）
    void writeFile(const FileBlock& fb, EvalContext& ctx);

    // 合并 protected 区：newContent 中以 BEGIN/END 标记包围的区，
    // 若 oldContent 中有同 ID 的区，则用 old 的内容替换 new 的内容。
    // 对齐 Java Acceleo ProtectedAreaWriter 的合并语义：保留用户手改内容。
    std::string mergeProtectedRegions(const std::string& newContent,
                                       const std::string& oldContent);

    std::unordered_map<std::string, ServiceFn> services_;
    // 模块级 queries（按 name 索引，对齐 Java 模块内 query 自动可被模板调用）
    std::unordered_map<std::string, std::shared_ptr<Query>> queries_;
    // 已注册模块（按 module name 索引），用于 extends/import 解析
    std::unordered_map<std::string, std::shared_ptr<Module>> registeredModules_;
    // 当前主模块（求值时设置，用于沿 extends 链查找 template/query）
    std::shared_ptr<Module> currentModule_;
    std::string outDir_;
};

// AcceleoService：高层 API，对齐 Java org.eclipse.acceleo.engine.service.AcceleoService
class AcceleoService {
public:
    AcceleoService();

    // 注册服务
    void registerService(const std::string& name, ServiceFn fn);

    // 从 .mtl 源码生成：解析 + 求值 + 写文件
    // 对齐 Java AcceleoService.doGenerate(uri, args, outputFolder)
    void doGenerate(const std::string& mtlSource,
                    ::emf::common::EObject* model,
                    const std::string& outDir);

    // 求值单个模板（返回字符串，不写文件）
    std::string evaluateTemplate(const std::string& mtlSource,
                                 const std::string& templateName,
                                 const std::vector<std::any>& args);

    AcceleoEngine& engine() { return engine_; }

private:
    AcceleoEngine engine_;
};

}  // namespace emf::acceleo
