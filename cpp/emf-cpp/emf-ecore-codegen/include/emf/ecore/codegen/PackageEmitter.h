// PackageEmitter: 生成 <Pkg>Package.h/.cpp
// 对齐 Java: <Pkg>Package.java / <Pkg>PackageImpl.java
//
// 输出内容：
//   - eINSTANCE 单例
//   - 所有 EClass/EAttribute/EReference/EOperation 静态字段
//   - initialize() 静态方法（创建并注册 EClass）
//   - create<X>() 工厂入口（PackageImpl 兼任 factory role 与 Java 兼容）
#pragma once

#include "emf/ecore/codegen/IndentedWriter.h"
#include "emf/ecore/codegen/StringUtils.h"
#include "emf/ecore/EcorePackage.h"
#include <string>

namespace emf::ecore::codegen {

class PackageEmitter {
public:
    // parentPath 用 "/" 分隔父包路径（如 "gswcomponents/gcomponents"）
    // 用于构造完整 nsPath 和跨包 include 路径
    PackageEmitter(emf::ecore::EPackage* package, const std::string& baseNamespace,
                   const std::string& parentPath = std::string{});

    // 生成 <Pkg>Package.h 的完整内容
    std::string emitHeader() const;

    // 生成 <Pkg>Package.cpp 的完整内容
    std::string emitSource() const;

    // 类名（不含 .h/.cpp）：对齐 Java 习惯，把 EPackage.name 首字母大写后拼上 "Package"
    std::string className() const { return capitalizeFirst(package_->getName()) + "Package"; }

    // include guard / pragma once 不变，输出文件主名
    std::string includeGuard() const;

    // 包名（用于 namespace 拼装：完整父路径 + EPackage.name，"::" 分隔）
    std::string nsPath() const;

private:
    emf::ecore::EPackage* package_;
    std::string baseNamespace_;
    std::string parentPath_;  // 父包路径（"/" 分隔），空表示当前是根包
};

// 给定一个 EPackage，反推其完整父包路径（"/" 分隔，从根到该包）。
// 用 getESuperPackage() 链向上走，累加 name 倒序拼接。
// 对齐 Java EMF codegen：每个 EPackage 在磁盘上的目录 = 完整父路径 + 当前包名。
// 用于跨包 include / namespace 拼接时定位目标包所在目录。
std::string epackageFullPath(emf::ecore::EPackage* pkg);

// 把 "/path" 转 "::path"（用于 namespace）
std::string pathToNs(const std::string& slashPath);

}  // namespace emf::ecore::codegen
