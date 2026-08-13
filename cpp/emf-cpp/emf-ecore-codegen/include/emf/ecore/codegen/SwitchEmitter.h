// SwitchEmitter: 生成 <Pkg>Switch.h/.cpp
// 对齐 Java: <Pkg>Switch.java（org.eclipse.emf.ecore.util.Switch<T> 子类）
#pragma once

#include "emf/ecore/codegen/IndentedWriter.h"
#include "emf/ecore/codegen/StringUtils.h"
#include "emf/ecore/EcorePackage.h"
#include <string>

namespace emf::ecore::codegen {

class SwitchEmitter {
public:
    SwitchEmitter(emf::ecore::EPackage* package, const std::string& baseNamespace,
                  const std::string& parentPath = std::string{});

    std::string emitHeader() const;
    std::string emitSource() const;

    std::string className() const { return capitalizeFirst(package_->getName()) + "Switch"; }

private:
    emf::ecore::EPackage* package_;
    std::string baseNamespace_;
    std::string parentPath_;
};

}  // namespace emf::ecore::codegen
