// Codegen 测试：输出/读文件工具
#pragma once

#include <iostream>
#include <filesystem>
#include <fstream>
#include <sstream>
#include <string>

// 每次测试运行的输出根目录（CMakeLists 会传 EMF_CODEGEN_TEST_OUTPUT）
#ifndef EMF_CODEGEN_TEST_OUTPUT_DIR
#define EMF_CODEGEN_TEST_OUTPUT_DIR "."
#endif

inline std::string makeTestDir(const std::string& sub) {
    std::string path = std::string(EMF_CODEGEN_TEST_OUTPUT_DIR) + "/" + sub;
    std::filesystem::create_directories(path);
    return path;
}

inline std::string readAll(const std::string& path) {
    std::ifstream f(path);
    if (!f.is_open()) return "";
    std::stringstream ss;
    ss << f.rdbuf();
    return ss.str();
}

inline bool fileContains(const std::string& path, const std::string& sub) {
    return readAll(path).find(sub) != std::string::npos;
}
