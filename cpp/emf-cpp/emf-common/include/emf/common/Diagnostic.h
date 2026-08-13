// EMF Common: Diagnostic / BasicDiagnostic
// 对齐 org.eclipse.emf.common.util.Diagnostic, BasicDiagnostic (Java)
#pragma once

#include <string>
#include <vector>
#include <memory>
#include <utility>

namespace emf::common {

// ===== Diagnostic 接口 =====
// 对齐 org.eclipse.emf.common.util.Diagnostic
class Diagnostic {
public:
    enum class Severity : int {
        OK = 0,
        INFO = 1,
        WARNING = 2,
        ERROR = 3,
        CANCEL = 4
    };

    Diagnostic(Severity sev, std::string source, int code, std::string message,
               std::vector<std::shared_ptr<Diagnostic>> data = {})
        : severity_(sev), source_(std::move(source)), code_(code),
          message_(std::move(message)), data_(std::move(data)) {}

    virtual ~Diagnostic() = default;

    Severity severity() const { return severity_; }
    const std::string& source() const { return source_; }
    int code() const { return code_; }
    const std::string& message() const { return message_; }
    const std::vector<std::shared_ptr<Diagnostic>>& data() const { return data_; }
    virtual const std::vector<std::shared_ptr<Diagnostic>>& children() const { return children_; }

    static const char* severityName(Severity s) {
        switch (s) {
            case Severity::OK: return "OK";
            case Severity::INFO: return "INFO";
            case Severity::WARNING: return "WARNING";
            case Severity::ERROR: return "ERROR";
            case Severity::CANCEL: return "CANCEL";
        }
        return "UNKNOWN";
    }

    static int severityValue(Severity s) { return static_cast<int>(s); }

protected:
    Severity severity_;
    std::string source_;
    int code_;
    std::string message_;
    std::vector<std::shared_ptr<Diagnostic>> data_;
    std::vector<std::shared_ptr<Diagnostic>> children_;
};

// ===== BasicDiagnostic 实现 =====
// 对齐 org.eclipse.emf.common.util.BasicDiagnostic
class BasicDiagnostic : public Diagnostic {
public:
    BasicDiagnostic()
        : Diagnostic(Severity::OK, "", 0, "") {}

    BasicDiagnostic(Severity sev, const std::string& source, int code, const std::string& message,
                    const std::vector<std::shared_ptr<Diagnostic>>& data = {})
        : Diagnostic(sev, source, code, message, data) {}

    // Add a child diagnostic
    void add(std::shared_ptr<Diagnostic> child) {
        if (child) {
            children_.push_back(std::move(child));
            if (children_.back()->severity() > severity_) {
                severity_ = children_.back()->severity();
            }
        }
    }

    // 把另一个 BasicDiagnostic 的所有 children 合并
    void addAll(const std::shared_ptr<Diagnostic>& other) {
        if (!other) return;
        for (auto& c : other->children()) {
            children_.push_back(c);
        }
        if (other->severity() > severity_) {
            severity_ = other->severity();
        }
    }

    void merge(const std::shared_ptr<Diagnostic>& other) {
        if (!other) return;
        addAll(other);
    }

    int getCode() const { return code_; }
    const std::string& getMessage() const { return message_; }
    const std::string& getSource() const { return source_; }
};

// ===== DiagnosticChain（轻量桩实现）=====
// 对齐 org.eclipse.emf.common.util.DiagnosticChain
class DiagnosticChain {
public:
    // 接受左值（const ref），右值走 std::move 后 push_back 一次，避免与下方的 rvalue 重载冲突。
    void add(const std::shared_ptr<Diagnostic>& d) {
        if (d) diagnostics_.push_back(d);
    }
    // 接受 rvalue 所有权转移
    void add(std::shared_ptr<Diagnostic>&& d) {
        if (d) diagnostics_.push_back(std::move(d));
    }
    bool empty() const { return diagnostics_.empty(); }
    size_t size() const { return diagnostics_.size(); }
    const std::vector<std::shared_ptr<Diagnostic>>& get() const { return diagnostics_; }
    void clear() { diagnostics_.clear(); }
private:
    std::vector<std::shared_ptr<Diagnostic>> diagnostics_;
};

}  // namespace emf::common
