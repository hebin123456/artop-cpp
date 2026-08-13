// Sphinx 测试：通用主入口（拷贝自 emf-ecore-util/tests/test_main.h）
#pragma once

#include <iostream>
#include <string>
#include <vector>
#include <functional>
#include <sstream>

namespace emf_test {

struct TestCase {
    std::string name;
    std::function<void()> fn;
};

class Registry {
public:
    static Registry& instance() {
        static Registry r;
        return r;
    }
    void add(const std::string& name, std::function<void()> fn) {
        cases_.push_back({name, std::move(fn)});
    }
    int runAll() {
        int failed = 0;
        for (auto& c : cases_) {
            std::cout << "[ RUN      ] " << c.name << std::endl;
            try {
                c.fn();
                std::cout << "[       OK ] " << c.name << std::endl;
            } catch (const std::exception& e) {
                std::cout << "[  FAILED  ] " << c.name << ": " << e.what() << std::endl;
                failed++;
            } catch (...) {
                std::cout << "[  FAILED  ] " << c.name << ": unknown exception" << std::endl;
                failed++;
            }
        }
        std::cout << "\n=========================================" << std::endl;
        std::cout << "Total: " << cases_.size() << "  Failed: " << failed << std::endl;
        return failed == 0 ? 0 : 1;
    }
private:
    std::vector<TestCase> cases_;
};

struct AutoRegister {
    AutoRegister(const std::string& name, std::function<void()> fn) {
        Registry::instance().add(name, std::move(fn));
    }
};

}  // namespace emf_test

#define EMF_TEST(name) \
    static void emf_test_##name(); \
    static emf_test::AutoRegister emf_test_reg_##name(#name, emf_test_##name); \
    static void emf_test_##name()

#define EXPECT_TRUE(x) do { if (!(x)) { std::ostringstream oss; oss << "EXPECT_TRUE failed at " << __FILE__ << ":" << __LINE__ << ": " << #x; throw std::runtime_error(oss.str()); } } while(0)
#define EXPECT_FALSE(x) do { if ((x)) { std::ostringstream oss; oss << "EXPECT_FALSE failed at " << __FILE__ << ":" << __LINE__ << ": " << #x; throw std::runtime_error(oss.str()); } } while(0)
#define EXPECT_EQ(a, b) do { auto _a = (a); auto _b = (b); if (!(_a == _b)) { std::ostringstream oss; oss << "EXPECT_EQ failed at " << __FILE__ << ":" << __LINE__ << ": " << #a << " (" << _a << ") != " << #b << " (" << _b << ")"; throw std::runtime_error(oss.str()); } } while(0)
#define EXPECT_NE(a, b) do { auto _a = (a); auto _b = (b); if (_a == _b) { std::ostringstream oss; oss << "EXPECT_NE failed at " << __FILE__ << ":" << __LINE__ << ": " << #a << " == " << #b; throw std::runtime_error(oss.str()); } } while(0)
#define EXPECT_NOT_NULL(x) do { if ((x) == nullptr) { std::ostringstream oss; oss << "EXPECT_NOT_NULL failed at " << __FILE__ << ":" << __LINE__; throw std::runtime_error(oss.str()); } } while(0)
#define EXPECT_NULL(x) do { if ((x) != nullptr) { std::ostringstream oss; oss << "EXPECT_NULL failed at " << __FILE__ << ":" << __LINE__; throw std::runtime_error(oss.str()); } } while(0)
#define EXPECT_THROWS(x) do { bool _threw = false; try { x; } catch (...) { _threw = true; } if (!_threw) { std::ostringstream oss; oss << "EXPECT_THROWS failed at " << __FILE__ << ":" << __LINE__; throw std::runtime_error(oss.str()); } } while(0)
#define EXPECT_CONTAINS(haystack, needle) do { auto _h = (haystack); auto _n = (needle); if (_h.find(_n) == std::string::npos) { std::ostringstream oss; oss << "EXPECT_CONTAINS failed at " << __FILE__ << ":" << __LINE__ << ": '" << _n << "' not in '" << _h.substr(0, 200) << "...'"; throw std::runtime_error(oss.str()); } } while(0)

#define RUN_ALL_TESTS() emf_test::Registry::instance().runAll()
