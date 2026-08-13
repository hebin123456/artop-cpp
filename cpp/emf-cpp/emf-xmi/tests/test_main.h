// 测试通用 main：极简测试框架（无需 gtest）
#pragma once

#include <iostream>
#include <string>
#include <vector>
#include <functional>
#include <sstream>
#include <any>

#include "emf/common/EObject.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/common/EList.h"

namespace emf_test {

// 多值 EReference 追加辅助：DynamicEObject 的 eGet 返回内部 EList 指针（对齐 Java
// DynamicEObjectImpl 语义——直接修改即生效）；生成类 eGet 返回 new EList 副本。
// 为兼容两者，统一采用"提取当前列表 → 追加新值 → eSet 回写"模式。
// 注意：返回的指针可能是内部指针（DynamicEObject，不可 delete）或副本（生成类），
//       不 delete 以避免对 DynamicEObject 造成 double-free；生成类副本在测试进程内泄漏可接受。
inline void addToContainment(emf::common::EObject* obj, emf::ecore::EStructuralFeature* sf,
                             emf::common::EObject* value) {
    if (!obj || !sf || !value) return;
    std::vector<emf::common::EObject*> v;
    auto any = obj->eGet(sf);
    if (any.has_value()) {
        if (any.type() == typeid(emf::common::EList<emf::common::EObject*>*)) {
            auto* elist = std::any_cast<emf::common::EList<emf::common::EObject*>*>(any);
            if (elist) {
                for (size_t i = 0; i < elist->size(); ++i) v.push_back((*elist)[i]);
            }
        } else if (any.type() == typeid(std::vector<emf::common::EObject*>)) {
            v = std::any_cast<std::vector<emf::common::EObject*>>(any);
        } else if (any.type() == typeid(std::vector<emf::common::EObject*>*)) {
            auto* p = std::any_cast<std::vector<emf::common::EObject*>*>(any);
            if (p) v = *p;
        }
    }
    v.push_back(value);
    obj->eSet(sf, std::any(std::move(v)));
}

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

#define RUN_ALL_TESTS() emf_test::Registry::instance().runAll()
