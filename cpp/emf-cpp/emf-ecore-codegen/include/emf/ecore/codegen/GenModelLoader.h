// GenModelLoader.h —— 从 .genmodel XMI 加载到内存中的 GenModel 数据结构
// 对齐 Java: org.eclipse.emf.codegen.ecore.genmodel.util.GenModelUtil + .impl.GenModelImpl
// Java 是把 .genmodel 当作 Ecore 模型加载（用 Ecore reflective 框架解析）。
// 我们用 C++ 时：直接用现成的 XMI 解析器（emf::xmi::XMIResource），把 XML
// 属性（xsi:type / xmi:idref）映射到 GenModel/GenPackage/GenClass 等结构上。
#pragma once

#include "emf/ecore/codegen/GenModel.h"
#include <memory>
#include <string>

namespace emf::ecore::codegen {

class GenModelLoader {
public:
    // 从 .genmodel 路径加载。
    // 流程：先把 .genmodel 当成 XMI 加载到 EObject 树，
    // 再由我们自己写的映射把 <genmodel:GenModel> 节点转成 GenModel 结构。
    // 我们还会触发 .ecore 加载（因为 .genmodel 里 ecorePackage="...ecore#/"），
    // 把 genPackage.ecorePackage 填好。
    // 返回非空 shared_ptr 表示成功。
    static std::shared_ptr<GenModel> load(const std::string& genModelPath);

    // 轻量：从已有 XMI 字符串解析（测试用）
    static std::shared_ptr<GenModel> loadFromString(const std::string& xml,
                                                   const std::string& baseDir = "");

    // 把已经加载的 EPackage 包装成单个 GenPackage，挂在一个临时 GenModel 上
    // （供 "我没 .genmodel 只有 .ecore" 的旧路径使用 —— CppGenerator.cpp 用得到）
    static std::shared_ptr<GenModel> wrapEcore(emf::ecore::EPackage* ecorePkg,
                                               const std::string& baseNamespace = "emf");
};

}  // namespace emf::ecore::codegen
