// EMF Common: EPackageRegistry
// 对齐 org.eclipse.emf.ecore.EPackage.Registry (Java)
#pragma once

#include "EPackage.h"
#include <unordered_map>
#include <string>
#include <mutex>
#include <vector>

namespace emf::common {

class EPackageRegistry {
public:
    static EPackageRegistry& instance() {
        static EPackageRegistry r;
        return r;
    }

    void put(const std::string& id, EPackage* p) {
        std::lock_guard<std::mutex> lk(mu_);
        byNsURI_[id] = p;
        if (p) {
            byName_[p->getName()] = p;
            std::string prefix = p->getNsPrefix();
            if (!prefix.empty()) byPrefix_[prefix] = p;
        }
    }

    EPackage* get(const std::string& id) const {
        std::lock_guard<std::mutex> lk(mu_);
        auto it = byNsURI_.find(id);
        if (it != byNsURI_.end()) return it->second;
        it = byName_.find(id);
        if (it != byName_.end()) return it->second;
        it = byPrefix_.find(id);
        if (it != byPrefix_.end()) return it->second;
        return nullptr;
    }

    bool containsKey(const std::string& id) const { return get(id) != nullptr; }
    void remove(const std::string& id) { std::lock_guard<std::mutex> lk(mu_); byNsURI_.erase(id); }
    std::vector<std::string> keys() const {
        std::lock_guard<std::mutex> lk(mu_);
        std::vector<std::string> r;
        r.reserve(byNsURI_.size());
        for (auto& kv : byNsURI_) r.push_back(kv.first);
        return r;
    }
    // values：返回所有已注册的 EPackage（对齐 Java EPackage.Registry.values()）
    // 用于 loader 遍历所有注册的包查找 EClass（对齐 Java ExtendedMetaData.getType 的全局查找行为）
    std::vector<EPackage*> values() const {
        std::lock_guard<std::mutex> lk(mu_);
        std::vector<EPackage*> r;
        r.reserve(byNsURI_.size());
        for (auto& kv : byNsURI_) r.push_back(kv.second);
        return r;
    }

private:
    mutable std::mutex mu_;
    std::unordered_map<std::string, EPackage*> byNsURI_;
    std::unordered_map<std::string, EPackage*> byName_;
    std::unordered_map<std::string, EPackage*> byPrefix_;
};

}  // namespace emf::common
