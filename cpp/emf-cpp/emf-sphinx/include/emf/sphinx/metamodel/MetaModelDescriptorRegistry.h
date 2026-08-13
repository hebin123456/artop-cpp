// MetaModelDescriptorRegistry.h
// 对齐 Java org.eclipse.sphinx.emf.metamodel.MetaModelDescriptorRegistry
// 单例：按 URI 索引元模型描述符
#pragma once

#include "emf/sphinx/metamodel/IMetaModelDescriptor.h"
#include "emf/common/URI.h"
#include <unordered_map>
#include <vector>
#include <string>

namespace emf::common {
class EObject;
class Resource;
}

namespace emf::sphinx::metamodel {

class MetaModelDescriptorRegistry {
public:
    static MetaModelDescriptorRegistry& instance() {
        static MetaModelDescriptorRegistry inst;
        return inst;
    }

    // 注册/注销
    void registerDescriptor(IMetaModelDescriptor* d);
    void unregisterDescriptor(IMetaModelDescriptor* d);

    // 查找
    IMetaModelDescriptor* getDescriptor(const std::string& nsURI) const;
    IMetaModelDescriptor* getDescriptor(const emf::common::URI& uri) const;
    IMetaModelDescriptor* getDescriptor(emf::common::EObject* obj) const;
    IMetaModelDescriptor* getDescriptor(emf::common::Resource* res) const;

    IMetaModelDescriptor* getTargetDescriptor(const emf::common::URI& uri) const;
    IMetaModelDescriptor* getTargetDescriptor(emf::common::Resource* res) const;

    IMetaModelDescriptor* getOldDescriptor(const emf::common::URI& uri) const;
    IMetaModelDescriptor* getOldDescriptor(emf::common::Resource* res) const;

    std::vector<std::string> keys() const;
    std::vector<IMetaModelDescriptor*> getAll() const;

    // 清理
    void clear();

private:
    MetaModelDescriptorRegistry() = default;
    std::unordered_map<std::string, IMetaModelDescriptor*> descriptors_;
    std::unordered_map<std::string, IMetaModelDescriptor*> targetDescriptors_;
    std::unordered_map<std::string, IMetaModelDescriptor*> oldDescriptors_;
};

}  // namespace emf::sphinx::metamodel
