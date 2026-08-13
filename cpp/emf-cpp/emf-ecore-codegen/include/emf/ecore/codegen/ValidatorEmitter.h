// ValidatorEmitter: 生成 <Pkg>Validator.h/.cpp
// 对齐 Java: <Pkg>Validator.java (org.eclipse.emf.ecore.util.EObjectValidator 子类)
#pragma once

#include "emf/ecore/codegen/IndentedWriter.h"
#include "emf/ecore/codegen/StringUtils.h"
#include "emf/ecore/EcorePackage.h"
#include <string>

namespace emf::ecore::codegen {

class ValidatorEmitter {
public:
    ValidatorEmitter(emf::ecore::EPackage* package, const std::string& baseNamespace,
                     const std::string& parentPath = std::string{});

    std::string emitHeader() const;
    std::string emitSource() const;

    std::string className() const { return capitalizeFirst(package_->getName()) + "Validator"; }

private:
    emf::ecore::EPackage* package_;
    std::string baseNamespace_;
    std::string parentPath_;
};

}  // namespace emf::ecore::codegen
