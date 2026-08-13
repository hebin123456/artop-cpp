// emf-artop-runtime 测试主入口（独立子工程测试框架）
// 简单自定义，避免循环依赖
#pragma once

#include <cstdio>
#include <string>
#include <stdexcept>
#include <sstream>

#define EMF_ASSERT(cond) do { \
    if (!(cond)) { \
        std::ostringstream oss; \
        oss << "ASSERT failed at " << __FILE__ << ":" << __LINE__ << ": " << #cond; \
        throw std::runtime_error(oss.str()); \
    } \
} while(0)

#define EMF_ASSERT_EQ(a, b) do { \
    auto _a = (a); auto _b = (b); \
    if (!(_a == _b)) { \
        std::ostringstream oss; \
        oss << "ASSERT_EQ failed at " << __FILE__ << ":" << __LINE__ \
            << ": " << #a << " (" << _a << ") != " << #b << " (" << _b << ")"; \
        throw std::runtime_error(oss.str()); \
    } \
} while(0)

#define EMF_ASSERT_NE(a, b) do { \
    auto _a = (a); auto _b = (b); \
    if (_a == _b) { \
        std::ostringstream oss; \
        oss << "ASSERT_NE failed at " << __FILE__ << ":" << __LINE__; \
        throw std::runtime_error(oss.str()); \
    } \
} while(0)

#define EMF_RUN(fn) ([&]() -> bool { \
    std::printf("  [ RUN ] %s\n", #fn); \
    try { \
        if (fn()) { std::printf("  [ OK  ] %s\n", #fn); return true; } \
        std::printf("  [FAIL ] %s\n", #fn); return false; \
    } catch (const std::exception& e) { \
        std::printf("  [FAIL ] %s: %s\n", #fn, e.what()); return false; \
    } catch (...) { \
        std::printf("  [FAIL ] %s: unknown\n", #fn); return false; \
    } \
})()
