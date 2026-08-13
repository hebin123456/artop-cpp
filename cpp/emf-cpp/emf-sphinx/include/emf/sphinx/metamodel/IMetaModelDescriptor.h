// IMetaModelDescriptor.h
// 对齐 Java org.eclipse.sphinx.emf.metamodel.IMetaModelDescriptor
// 元模型描述符：标识一个具体的 EPackage 系列
#pragma once

#include "emf/common/URI.h"
#include <string>
#include <vector>
#include <memory>

namespace emf::ecore {
class EPackage;
}

namespace emf::sphinx::metamodel {

class IMetaModelDescriptor {
public:
    virtual ~IMetaModelDescriptor() = default;

    // 标签格式
    static constexpr const char* LABEL_PATTERN = "%1s (%2s)";

    // 标识符
    virtual std::string getIdentifier() const = 0;
    // 命名空间 URI
    virtual emf::common::URI getNamespaceURI() const = 0;
    // 命名空间字符串
    virtual std::string getNamespace() const = 0;
    // 名称
    virtual std::string getName() const = 0;
    // 基础描述符
    virtual IMetaModelDescriptor* getBaseDescriptor() const = 0;
    // 自定义 URI scheme
    virtual std::string getCustomURIScheme() const = 0;
    // ordinal
    virtual int getOrdinal() const = 0;
    // ns URI 模式
    virtual std::string getEPackageNsURIPattern() const = 0;

    // 比较
    virtual bool matchesNamespace(const std::string& ns) const = 0;
    virtual bool matchesEPackageNsURIPattern(const std::string& ns) const = 0;
    virtual bool equals(const IMetaModelDescriptor* other) const = 0;

    // 资源版本
    virtual std::vector<IMetaModelDescriptor*> getCompatibleResourceVersionDescriptors() const = 0;
};

}  // namespace emf::sphinx::metamodel
