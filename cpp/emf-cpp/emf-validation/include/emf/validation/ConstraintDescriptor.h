// EMF Validation: 约束描述符 / 分组 / XML 解析
// 对齐 org.eclipse.emf.validation.service.IConstraintDescriptor
//      org.eclipse.emf.validation.model.ConstraintCategory
//      org.eclipse.emf.validation.xml.IXmlConstraintDescriptor
#pragma once

#include "Constraint.h"
#include "emf/common/EObject.h"

#include <memory>
#include <string>
#include <vector>

namespace emf::validation {

// 约束分组（对齐 Java ConstraintCategory）
// 约束可以属于某个 category，便于按 category 过滤启用/禁用
class ConstraintCategory {
public:
    ConstraintCategory() = default;
    ConstraintCategory(std::string id, std::string name, std::string path = "")
        : id_(std::move(id)), name_(std::move(name)), path_(std::move(path)) {}

    const std::string& getId() const { return id_; }
    void setId(const std::string& s) { id_ = s; }
    const std::string& getName() const { return name_; }
    void setName(const std::string& s) { name_ = s; }
    // 分组路径："/categoryA/categoryB"，子组用 / 分隔
    const std::string& getPath() const { return path_; }
    void setPath(const std::string& s) { path_ = s; }

private:
    std::string id_;
    std::string name_;
    std::string path_;
};

// 约束描述符（Java IConstraintDescriptor 的轻量等价物）
// 描述约束的元数据 + body（求值源，可由 ConstraintParser 编译为 Evaluator）
class ConstraintDescriptor {
public:
    ConstraintDescriptor() = default;

    // ===== ID / Name =====
    const std::string& getId() const { return id_; }
    void setId(const std::string& s) { id_ = s; }
    const std::string& getName() const { return name_; }
    void setName(const std::string& s) { name_ = s; }

    // ===== Description（可选） =====
    const std::string& getDescription() const { return description_; }
    void setDescription(const std::string& s) { description_ = s; }

    // ===== Message =====
    const std::string& getMessage() const { return message_; }
    void setMessage(const std::string& s) { message_ = s; }

    // ===== Severity =====
    Severity getSeverity() const { return severity_; }
    void setSeverity(Severity s) { severity_ = s; }

    // ===== Mode =====
    ConstraintMode getMode() const { return mode_; }
    void setMode(ConstraintMode m) { mode_ = m; }

    // ===== Body（求值源） =====
    // 简化：当前只支持 OCL-like 表达式，由 ConstraintParser 编译
    const std::string& getBody() const { return body_; }
    void setBody(const std::string& s) { body_ = s; }

    // ===== Language =====
    // 例如 "ocl" / "expr"（自研表达式）
    const std::string& getLanguage() const { return language_; }
    void setLanguage(const std::string& s) { language_ = s; }

    // ===== Category path =====
    const std::string& getCategoryPath() const { return categoryPath_; }
    void setCategoryPath(const std::string& s) { categoryPath_ = s; }

    // ===== Status code（默认 0） =====
    int getCode() const { return code_; }
    void setCode(int c) { code_ = c; }

    // 工厂：从 descriptor 直接编译为可执行的 Constraint
    // 返回的所有权归调用方
    Constraint* instantiate() const;

private:
    std::string id_;
    std::string name_;
    std::string description_;
    std::string message_;
    Severity severity_ = Severity::WARNING;
    ConstraintMode mode_ = ConstraintMode::BATCH;
    std::string body_;
    std::string language_ = "expr";
    std::string categoryPath_;
    int code_ = 0;
};

// ===== ConstraintDescriptorParser：从 XML 字符串解析多个 descriptor =====
//
// 期望的轻量 XML 形式（与 EMF validation 标准的 plugin.xml 子集对齐）：
//   <constraints>
//     <constraint id="..." name="..." message="..." severity="warning|info|error"
//                 mode="live|batch" language="expr" body="..." code="0"
//                 categoryPath="/catA/catB" description="..."/>
//     ...
//   </constraints>
//
// 注：自研简化 XML 解析，不依赖外部库（保证 emf-validation 无第三方依赖）。
class ConstraintDescriptorParser {
public:
    // 解析 XML 字符串并返回多个 descriptor
    // 解析失败时返回空 vector
    static std::vector<ConstraintDescriptor> parseDescriptors(const std::string& xml);

    // 解析并直接注册到 EValidator
    // 返回成功注册的 Constraint 数量
    static int parseAndRegister(const std::string& xml, class EValidator& validator);

    // 解析并按 category 过滤：只注册 categoryPath 匹配 prefix 的约束
    // prefix 为空表示接受所有 category
    static int parseAndRegisterFiltered(const std::string& xml,
                                        class EValidator& validator,
                                        const std::string& categoryPathPrefix);
};

}  // namespace emf::validation
