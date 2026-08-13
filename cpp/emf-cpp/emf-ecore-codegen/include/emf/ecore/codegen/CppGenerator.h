// emf::ecore::codegen —— Ecore → C++17 代码生成器
// 对齐 Java: org.eclipse.emf.codegen.ecore (JET 模板)
//
// 设计目标：与 Java EMF 代码生成器的输出结构对齐（行为一致）。
// 每个 EPackage 生成一组 C++ 文件：
//   <Pkg>Package.h/.cpp          EPackage 单例 + 静态 EClass 字段
//   <Pkg>Factory.h/.cpp          EFactory 实现
//   <Pkg>Switch.h/.cpp           类型切换工具
//   <Pkg>AdapterFactory.h/.cpp   AdapterFactory
//   <Pkg>Validator.h/.cpp        EObjectValidator 子类
//   <ClassName>.h/.cpp           每个 EClass 一个类（单类单继承 EObjectImpl）
#pragma once

#include "emf/ecore/EcorePackage.h"
#include "emf/ecore/EcorePackage.h"
#include <string>
#include <vector>
#include <memory>

namespace emf::ecore::codegen {

// 前向声明
class IndentedWriter;
class PackageEmitter;
class FactoryEmitter;
class EClassEmitter;
class SwitchEmitter;
class AdapterFactoryEmitter;
class ValidatorEmitter;
class TypeMapper;

// ===== GenConfig: 生成选项 =====
struct GenConfig {
    std::string inputEcorePath;       // 输入 .ecore 文件路径（与 inputPackage 二选一）
    std::string outputDirectory;      // 输出根目录
    std::string baseNamespace = "emf";  // 基础命名空间（默认 emf）
    std::string copyrightHeader;      // 可选许可证头注释
    bool generateSwitch = true;       // 是否生成 <Pkg>Switch
    bool generateAdapterFactory = true;  // 是否生成 <Pkg>AdapterFactory
    bool generateValidator = true;    // 是否生成 <Pkg>Validator
};

// ===== CppGenerator: 顶层入口 =====
class CppGenerator {
public:
    explicit CppGenerator(GenConfig config);
    ~CppGenerator();

    // 从 .ecore 文件加载并生成
    void generateFromFile();

    // 从已加载的 EPackage 生成（默认 parentPath 为空，从根包开始）
    virtual void generateFromPackage(emf::ecore::EPackage* package);

    // 重载：递归子包时用 parentPath 维护完整父包路径（用 "/" 分隔）
    // 对齐 Java EMF codegen —— 子包生成到父包子目录下，命名空间嵌套
    void generateFromPackage(emf::ecore::EPackage* package, const std::string& parentPath);

    // 获取已加载的 Package（generateFromFile 后有效）
    emf::ecore::EPackage* package() const { return package_; }

private:
    void ensureOutputDir(const std::string& subdir) const;
    void writeFile(const std::string& relativePath, const std::string& content) const;

    GenConfig config_;
    emf::ecore::EPackage* package_ = nullptr;
};

}  // namespace emf::ecore::codegen
