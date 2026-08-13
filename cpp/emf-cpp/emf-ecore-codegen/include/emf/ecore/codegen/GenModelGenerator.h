// GenModelGenerator.h —— 顶层调度器
// 对齐 Java: org.eclipse.emf.codegen.ecore.Generator / GenBaseGenerator
//
// Java 侧的 GenModelGenerator 流程（简化）：
//   1. 加载 .genmodel XMI → GenModel EObject
//   2. 对每个 GenPackage 调度 generatorAdapter（JetEmitter）生成 Java 文件
//   3. 多个 GeneratorAdapter（Model / Edit / Editor）分别负责 Model / Edit / Editor 子项目
//
// 我们只实现 Model 子项目（即"接口+实现+Package+Factory+Switch+AdapterFactory+Validator"），
// 流程：
//   1. 加载 .genmodel → GenModel（GenModelLoader）
//   2. 遍历每个 GenPackage，调用 CppTemplates 中的 emitXxx 模板
//   3. 把模板渲染结果写到 outputDirectory/<package_name>/<FileName>.{h,cpp}
#pragma once

#include "emf/ecore/codegen/GenModel.h"
#include <memory>
#include <string>

namespace emf::ecore::codegen {

class GenModelGenerator {
public:
    // 选项
    struct Options {
        std::string outputDirectory;
        std::string baseNamespace = "emf";  // 用户填的 base namespace（保底）
        bool generateSwitch = true;
        bool generateAdapterFactory = true;
        bool generateValidator = true;
        bool generateInterfaces = true;     // Java 默认 true；我们默认 true
    };

    explicit GenModelGenerator(Options opt);
    ~GenModelGenerator();

    // 主入口：生成
    // genModel: 已经加载的 GenModel（必须非空）
    // 返回写入的文件数；< 0 表示错误
    int generate(std::shared_ptr<GenModel> genModel);

    // 便捷：从 .genmodel 文件直接生成
    int generateFromFile(const std::string& genModelPath);

    // 便捷：从一个已加载的 EPackage（无 .genmodel）走 wrapEcore 兜底
    int generateFromEcore(emf::ecore::EPackage* ecorePkg);

    const Options& options() const { return options_; }

private:
    void writeFile(const std::string& relativePath, const std::string& content);
    void mkdirsFor(const std::string& fullPath);
    int  generateForPackage(GenPackage* gp, std::shared_ptr<GenModel> gm);

    Options options_;
};

}  // namespace emf::ecore::codegen
