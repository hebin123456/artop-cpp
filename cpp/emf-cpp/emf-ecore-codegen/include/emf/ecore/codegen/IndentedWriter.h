// IndentedWriter: 带缩进的字符串构建器，对齐 Java EMF 的 JET 模板输出
#pragma once

#include <string>
#include <sstream>

namespace emf::ecore::codegen {

class IndentedWriter {
public:
    explicit IndentedWriter(std::string indent = "    ") : indent_(std::move(indent)) {}

    // 输出当前行内容（带缩进），并换行
    IndentedWriter& line(const std::string& s = "") {
        if (s.empty()) {
            oss_ << "\n";
        } else {
            for (int i = 0; i < depth_; ++i) oss_ << indent_;
            oss_ << s << "\n";
        }
        return *this;
    }

    // 不带缩进的原始行（用于宏 / 注释块）
    IndentedWriter& raw(const std::string& s) {
        oss_ << s;
        if (!s.empty() && s.back() != '\n') oss_ << "\n";
        return *this;
    }

    // 增加缩进层级
    IndentedWriter& push() { ++depth_; return *this; }
    IndentedWriter& pop() { if (depth_ > 0) --depth_; return *this; }

    int depth() const { return depth_; }
    const std::string str() const { return oss_.str(); }
    std::string take() { auto s = oss_.str(); oss_.str(""); return s; }

private:
    std::string indent_;
    int depth_ = 0;
    std::ostringstream oss_;
};

// RAII 风格的作用域缩进：构造时 push，析构时 pop
class IndentScope {
public:
    explicit IndentScope(IndentedWriter& w) : w_(w) { w_.push(); }
    ~IndentScope() { w_.pop(); }
private:
    IndentedWriter& w_;
};

}  // namespace emf::ecore::codegen
