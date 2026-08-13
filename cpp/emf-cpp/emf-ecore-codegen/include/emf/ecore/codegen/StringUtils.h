// StringUtils.h —— 代码生成器内部字符串工具
#pragma once
#include <string>
#include <cctype>

namespace emf::ecore::codegen {

inline std::string capitalizeFirst(const std::string& s) {
    if (s.empty()) return s;
    std::string r = s;
    r[0] = static_cast<char>(std::toupper(static_cast<unsigned char>(r[0])));
    return r;
}

inline std::string toUpper(const std::string& s) {
    std::string r = s;
    for (auto& c : r) c = static_cast<char>(std::toupper(static_cast<unsigned char>(c)));
    return r;
}

inline std::string toLower(const std::string& s) {
    std::string r = s;
    for (auto& c : r) c = static_cast<char>(std::tolower(static_cast<unsigned char>(c)));
    return r;
}

// 转为 C++ 合法标识符（替换非 [A-Za-z0-9_]）
inline std::string safeIdent(const std::string& s) {
    std::string out;
    for (char c : s) {
        if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') ||
            (c >= '0' && c <= '9') || c == '_') out += c;
        else out += '_';
    }
    if (!out.empty() && std::isdigit(static_cast<unsigned char>(out[0]))) out = "_" + out;
    return out;
}

}  // namespace emf::ecore::codegen
