// emf::artop::codegen —— ARTOP 静态模型代码生成器（继承 emf-ecore-codegen）
// 对齐 Java:
//   - org.artop.aal.gautosar.codegen
//   - org.artop.aal.autosar40.codegen
//   - org.artop.aal.autosar448.codegen
//   + Acceleo 模板
//
// 设计：
//   ArtopCppGenerator 继承 CppGenerator
//   - 重写 generateFromPackage()：在基类生成的基础上，注入 ARTOP 特有步骤
//   - 输出额外的 Resource / ResourceFactory / SchemaLocation 文件
//   - 在根 EObject 注入 mixed/extensions/g* 字段
#pragma once

#include "emf/ecore/codegen/CppGenerator.h"

#include <string>
#include <vector>

namespace emf::artop::codegen {

// ARTOP 特有的生成配置
struct ArtopGenConfig {
    // 基础配置
    emf::ecore::codegen::GenConfig base;
    // AUTOSAR release id（"org.artop.aal.autosar448"）
    std::string releaseId = "org.artop.aal.autosar448";
    // major.minor.revision（如 "4.4.8"）
    std::string version = "4.4.8";
    // base namespace URI（"http://autosar.org/schema/r4.0"）
    std::string baseNamespaceUri = "http://autosar.org/schema/r4.0";
    // xsi:schemaLocation 完整内容
    std::string schemaLocation = "http://autosar.org/schema/r4.0 AUTOSAR_4-4-8.xsd";
    // 是否生成 ResourceImpl/ResourceFactoryImpl
    bool generateResource = true;
    // 是否注入根对象的 mixed/extensions 字段
    bool injectRootExtensions = true;
};

// ===== ArtopCppGenerator =====
class ArtopCppGenerator : public emf::ecore::codegen::CppGenerator {
public:
    explicit ArtopCppGenerator(ArtopGenConfig config);

    // 在基类 generateFromPackage 之后追加 ARTOP 特有步骤
    void generateFromPackage(emf::ecore::EPackage* package);

    // 一站式入口：load ecore + 基类 generate + ARTOP extensions
    void generateFromFile();

private:
    // 生成 Autosar448ResourceImpl / ResourceFactoryImpl
    void generateResourceAndFactory(emf::ecore::EPackage* package);

    // 在根 EClass（顶层 AUTOSAR 类型）注入 mixed/extensions 字段
    void generateRootExtensions(emf::ecore::EPackage* package);

    void writeFile(const std::string& relativePath, const std::string& content) const;

    ArtopGenConfig artopConfig_;
};

}  // namespace emf::artop::codegen
