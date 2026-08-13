// EcoreIndex.h
// 对齐 Java org.eclipse.sphinx.emf.internal.ecore.proxymanagement.lookupresolver.EcoreIndex
// 通过 EClass / EReference 索引实现 lookup 解析
#pragma once

#include <string>
#include <unordered_map>
#include <vector>

namespace emf::ecore {
class EClass;
class EReference;
}

namespace emf::common {
class EObject;
}

namespace emf::sphinx::internal::ecore::proxymanagement::lookupresolver {

class EcoreIndex {
public:
    static EcoreIndex& instance() {
        static EcoreIndex inst;
        return inst;
    }

    // 注册 EObject
    void registerObject(emf::common::EObject* obj);
    void unregisterObject(emf::common::EObject* obj);

    // 通过 EClass + 名字查找
    std::vector<emf::common::EObject*> findByName(emf::ecore::EClass* cls, const std::string& name);

    // 通过 EReference + 值的引用查找
    std::vector<emf::common::EObject*> findByReference(emf::ecore::EReference* ref, emf::common::EObject* target);

private:
    EcoreIndex() = default;
    std::unordered_map<std::string, std::vector<emf::common::EObject*>> classIndex_;
    std::unordered_map<std::string, std::vector<emf::common::EObject*>> refIndex_;
};

}  // namespace emf::sphinx::internal::ecore::proxymanagement::lookupresolver
