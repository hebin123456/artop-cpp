// CommonPlugin.h
// 对齐 Java org.eclipse.emf.common.CommonPlugin / EMFPlugin
// 头less 简化版：getString 返回硬编码英文，log 写 stderr。
#pragma once

#include <any>
#include <iostream>
#include <string>
#include <vector>

namespace emf::common {

class CommonPlugin {
public:
    static CommonPlugin& instance() {
        static CommonPlugin inst;
        return inst;
    }

    // 模拟 EMFPlugin.getString(String key) / getString(String, Object[])
    // 头less 模式下返回硬编码字符串 + 简单占位替换 {0} {1} ...
    std::string getString(const std::string& key) const {
        const std::string* v = strings().find(key);
        if (v == nullptr) return key;
        return *v;
    }

    std::string getString(const std::string& key, const std::vector<std::any>& subs) const {
        std::string fmt = getString(key);
        for (size_t i = 0; i < subs.size(); ++i) {
            std::string token = "{" + std::to_string(i) + "}";
            std::string val = anyToString(subs[i]);
            size_t pos = 0;
            while ((pos = fmt.find(token, pos)) != std::string::npos) {
                fmt.replace(pos, token.size(), val);
                pos += val.size();
            }
        }
        return fmt;
    }

    // 模拟 EMFPlugin.log(Object) - 写 stderr
    void log(void* logEntry) const {
        if (!logEntry) return;
        std::cerr << "[emf-common] " << logEntry << std::endl;
    }

    void log(const std::string& msg) const {
        std::cerr << "[emf-common] " << msg << std::endl;
    }

private:
    static const std::vector<std::pair<std::string, std::string>>& stringPairs() {
        static const std::vector<std::pair<std::string, std::string>> kPairs = {
            // CommonPlugin 提供的 key
            {"_UI_IgnoreException_exception", "Ignoring exception"},
            {"_UI_StringResourceNotFound_exception", "String resource not found"},
            {"_EXC_Method_not_implemented", "Method not implemented: {0}"},
            // AbstractCommand default label/description
            {"_UI_AbstractCommand_label", "Application Action"},
            {"_UI_AbstractCommand_description", "An application action"},
            // CompoundCommand
            {"_UI_CompoundCommand_label", "Compound Command"},
            {"_UI_CompoundCommand_description", "A compound command"},
            // UnexecutableCommand
            {"_UI_UnexecutableCommand_label", "Unexecutable Command"},
            {"_UI_UnexecutableCommand_description", "An unexecutable command"},
            // IdentityCommand
            {"_UI_IdentityCommand_label", "Identity Command"},
            {"_UI_IdentityCommand_description", "An identity command"},
            // CommandWrapper
            {"_UI_CommandWrapper_label", "Command Wrapper"},
            {"_UI_CommandWrapper_description", "A command that wraps another command"},
        };
        return kPairs;
    }

    // 把 std::string 链进来；用 anyToString 渲染
    static std::string anyToString(const std::any& a) {
        if (!a.has_value()) return "null";
        if (a.type() == typeid(std::string)) return std::any_cast<std::string>(a);
        if (a.type() == typeid(const char*)) return std::any_cast<const char*>(a);
        if (a.type() == typeid(int)) return std::to_string(std::any_cast<int>(a));
        if (a.type() == typeid(long)) return std::to_string(std::any_cast<long>(a));
        return "<any>";
    }

    // 用 vector::find 模拟 Java HashMap - 因为要避免头文件依赖
    struct PairMap {
        std::vector<std::pair<std::string, std::string>> data;
        const std::string* find(const std::string& k) const {
            for (auto& p : data) if (p.first == k) return &p.second;
            return nullptr;
        }
    };
    static const PairMap& strings() {
        static PairMap pm{stringPairs()};
        return pm;
    }
};

}  // namespace emf::common
