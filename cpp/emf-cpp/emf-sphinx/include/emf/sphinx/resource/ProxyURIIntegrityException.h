// ProxyURIIntegrityException.h
// 对齐 Java org.eclipse.sphinx.emf.resource.ProxyURIIntegrityException
#pragma once

#include <stdexcept>
#include <string>

namespace emf::sphinx::resource {

class ProxyURIIntegrityException : public std::runtime_error {
public:
    explicit ProxyURIIntegrityException(const std::string& msg) : std::runtime_error(msg) {}
};

}  // namespace emf::sphinx::resource
