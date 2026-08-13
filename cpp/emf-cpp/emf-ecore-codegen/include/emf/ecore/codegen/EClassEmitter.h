// EClassEmitter: 为单个 EClass 生成 <ClassName>.h/.cpp
//
// 简化设计（替代旧 InterfaceEmitter + ImplEmitter 双类方案）：
//   - 一个 EClass 只生成一个 C++ 类，类名 = EClass 名（无 Impl 后缀）
//   - 单继承 emf::ecore::EObjectImpl（不区分 interface/impl）
//   - 保留全部运行时行为：eNotify / eDeliver / eAdapters / eGet / eSet /
//     eIsSet / eUnset / eContents / eCrossReferences / 反射 / Adapter
//   - 不需要 `static_cast<TImpl*>(EObject*)` 的多继承 this 调整
//     （单继承下 T* 与 EObject* 起始位置一致）
//   - 跨包 reference 命名空间前缀处理与旧实现一致
#pragma once

#include "emf/ecore/codegen/IndentedWriter.h"
#include "emf/ecore/EcorePackage.h"
#include <string>

namespace emf::ecore::codegen {

class EClassEmitter {
public:
    // parentPath 用 "/" 分隔父包路径（用于 namespace 嵌套）
    EClassEmitter(emf::ecore::EClass* eClass, const std::string& baseNamespace,
                  const std::string& parentPath = std::string{});

    std::string emitHeader() const;
    std::string emitSource() const;

    // 头/源文件名（无 Impl 后缀）
    std::string headerName() const { return eClass_->getName() + ".h"; }
    std::string sourceName() const { return eClass_->getName() + ".cpp"; }

private:
    emf::ecore::EClass* eClass_;
    std::string baseNamespace_;
    std::string parentPath_;  // 父包路径（"/" 分隔），空表示当前是根包
};

}  // namespace emf::ecore::codegen
