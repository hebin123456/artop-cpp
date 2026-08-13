// XcoreGenerator.h
// 对齐 Java: org.eclipse.emf.ecore.xcore.XcoreGenerator（AST → EPackage 派生）
//           + org.eclipse.emf.codegen.ecore.genmodel.GenModel（GenModel 生成）
//
// 将 XcoreParser 解析出的 XPackage AST 派生为 Ecore 元模型实例（EPackage），
// 并可输出 GenModel XML（对齐 Java Xcore 的 .genmodel 派生）。
#pragma once

#include "emf/ecore/xcore/XcoreAst.h"

#include <memory>
#include <string>

namespace emf::ecore {
class EPackage;
}

namespace emf::ecore::xcore {

// XcoreGenerator：遍历 XPackage AST，创建对应的 EPackage/EClass/EAttribute/
// EReference/EOperation/EEnum/EDataType 实例，并在派生 EReference 时
// 建立 EOpposite 双向链接、传播 EAnnotation、为 EOperation 设置
// EParameters 与返回 EType。
class XcoreGenerator {
public:
    XcoreGenerator() = default;
    ~XcoreGenerator() = default;

    // 派生入口：返回新建的 EPackage（所有权归调用方）。
    ::emf::ecore::EPackage* generate(const std::shared_ptr<XPackage>& xpackage);

    // 生成 GenModel XML 文本（对齐 Java .genmodel 序列化形式）。
    // modelDirectory 默认 "/src"，complianceLevel 默认 "8.0"。
    // 调用方在 generate() 之后调用本方法。
    std::string generateGenModel(const std::shared_ptr<XPackage>& xpackage,
                                  const std::string& modelDirectory = "/src",
                                  const std::string& complianceLevel = "8.0");
};

}  // namespace emf::ecore::xcore
