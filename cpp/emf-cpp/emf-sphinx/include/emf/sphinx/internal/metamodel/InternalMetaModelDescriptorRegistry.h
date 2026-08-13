// InternalMetaModelDescriptorRegistry.h
// 对齐 Java org.eclipse.sphinx.emf.internal.metamodel.InternalMetaModelDescriptorRegistry
// 内部版本：跟踪 file → MetaModelDescriptor 的对应
#pragma once

#include "emf/sphinx/metamodel/IMetaModelDescriptor.h"
#include <unordered_map>
#include <string>

namespace emf::sphinx::internal::metamodel {

class InternalMetaModelDescriptorRegistry {
public:
    static InternalMetaModelDescriptorRegistry& instance() {
        static InternalMetaModelDescriptorRegistry inst;
        return inst;
    }

    emf::sphinx::metamodel::IMetaModelDescriptor* get(const std::string& uri) const;
    void put(const std::string& uri, emf::sphinx::metamodel::IMetaModelDescriptor* d);
    void remove(const std::string& uri);
    void clear();

private:
    InternalMetaModelDescriptorRegistry() = default;
    std::unordered_map<std::string, emf::sphinx::metamodel::IMetaModelDescriptor*> map_;
};

}  // namespace emf::sphinx::internal::metamodel
