// AbortExecutionException.h
// 对齐 Java org.eclipse.emf.common.command.AbortExecutionException
#pragma once

#include <stdexcept>
#include <string>

namespace emf::common::command {

// Java: AbortExecutionException extends RuntimeException
class AbortExecutionException : public std::runtime_error {
public:
    AbortExecutionException() : std::runtime_error("") {}
    explicit AbortExecutionException(const std::string& msg) : std::runtime_error(msg) {}
    AbortExecutionException(const std::string& msg, const std::exception& cause)
        : std::runtime_error(msg) {
        (void)cause;  // 骨架：c++ 嵌套异常复杂，先不实现 chained cause
    }
};

}  // namespace emf::common::command
