// EMF Validation: 校验服务
// 对齐 org.eclipse.emf.validation.service.ModelValidationService (Java)
#pragma once

#include "emf/validation/EValidator.h"
#include "emf/common/Diagnostic.h"
#include "emf/common/EObject.h"
#include <vector>
#include <memory>

namespace emf::validation {

// ValidationService：聚合 EValidator，对整个对象树执行校验
class ValidationService {
public:
    ValidationService();
    ~ValidationService();

    // 取/换底层 validator
    EValidator& validator() { return *validator_; }
    const EValidator& validator() const { return *validator_; }
    void setValidator(std::unique_ptr<EValidator> v);

    // 对单个对象执行校验（BATCH 模式：只执行 ConstraintMode::BATCH 约束）
    std::vector<emf::common::Diagnostic> validate(emf::common::EObject* target);

    // 递归校验 root 及其所有 eContents()（BATCH 模式）
    std::vector<emf::common::Diagnostic> validateAll(emf::common::EObject* root);

    // 是否在 validateAll 时同时校验 root 自身（默认 true）
    void setIncludeRoot(bool b) { includeRoot_ = b; }
    bool getIncludeRoot() const { return includeRoot_; }

    // 对齐 Java IBatchValidator.setIncludeLiveConstraints：
    // true 时 batch 校验同时执行 LIVE 约束（默认 false）
    void setIncludeLiveConstraints(bool b) { includeLive_ = b; }
    bool getIncludeLiveConstraints() const { return includeLive_; }

private:
    std::unique_ptr<EValidator> validator_;
    bool includeRoot_ = true;
    bool includeLive_ = false;
    void collectAll(emf::common::EObject* root,
                    std::vector<emf::common::EObject*>& out);
};

}  // namespace emf::validation
