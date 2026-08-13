// IFileMetaModelDescriptorCache.h
// 对齐 Java org.eclipse.sphinx.emf.internal.metamodel.IFileMetaModelDescriptorCache
#pragma once

#include "emf/sphinx/metamodel/IMetaModelDescriptor.h"
#include <unordered_map>
#include <string>

namespace emf::sphinx::internal::metamodel {

class IFileMetaModelDescriptorCache {
public:
    virtual ~IFileMetaModelDescriptorCache() = default;

    virtual emf::sphinx::metamodel::IMetaModelDescriptor* get(const std::string& uri) const = 0;
    virtual void put(const std::string& uri, emf::sphinx::metamodel::IMetaModelDescriptor* d) = 0;
    virtual void remove(const std::string& uri) = 0;
    virtual void clear() = 0;
    virtual bool contains(const std::string& uri) const = 0;
    virtual std::vector<std::string> keys() const = 0;
};

}  // namespace emf::sphinx::internal::metamodel
