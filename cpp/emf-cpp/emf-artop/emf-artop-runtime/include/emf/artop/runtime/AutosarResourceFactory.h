// emf::artop::runtime —— AutosarResourceFactory / AutosarXMLResourceFactory
// 对齐 Java:
//   org.artop.aal.common.resource.impl.AutosarResourceFactoryImpl
//   org.artop.aal.common.resource.impl.AutosarXMLResourceFactoryImpl
//
// ResourceFactory 的职责（对齐 Java 行为）：
//   1. 创建一个 Resource 实例（createResource(URI)）
//   2. 给 Resource 注入默认 load/save options
//   3. 维护 schema location catalog（namespace -> xsd 路径）
//   4. 维护 schema location base URIs
//
// 用户（codegen 输出的 Autosar448ResourceFactoryImpl）继承 AutosarResourceFactory，
// 重写 createResource() 提供 Autosar448ResourceImpl，重写 createSchemaLocationCatalog()
// 注册 r4.4 等的 xsi:schemaLocation。
#pragma once

#include "emf/artop/runtime/AutosarReleaseDescriptor.h"
#include "emf/artop/runtime/AutosarResource.h"
#include "emf/xmi/XMIResource.h"
#include "emf/common/Resource.h"
#include "emf/common/URI.h"

#include <functional>
#include <memory>
#include <string>
#include <unordered_map>

namespace emf::ecore {
class EPackage;
}

namespace emf::artop::runtime {

// Resource 构造器签名：给定 URI，返回一个 XMIResource
using AutosarResourceCreator =
    std::function<std::unique_ptr<emf::xmi::XMIResource>(const emf::common::URI&)>;

// ===== AutosarResourceFactory：抽象基类 =====
class AutosarResourceFactory {
public:
    explicit AutosarResourceFactory(std::shared_ptr<AutosarReleaseDescriptor> release)
        : autosarRelease_(std::move(release)) {}
    virtual ~AutosarResourceFactory() = default;

    // 关联 release
    const std::shared_ptr<AutosarReleaseDescriptor>& getAutosarRelease() const { return autosarRelease_; }
    void setAutosarRelease(std::shared_ptr<AutosarReleaseDescriptor> r) { autosarRelease_ = std::move(r); }

    // 创建 Resource（被 codegen 生成的 Autosar448ResourceFactoryImpl 重写）
    virtual std::unique_ptr<emf::common::Resource> createResource(const emf::common::URI& uri) = 0;

    // 注册 Resource 构造器（默认 creator 模板）
    void setResourceCreator(AutosarResourceCreator c) { resourceCreator_ = std::move(c); }

    // 给 Resource 注入默认 options（对齐 Java initResource）
    void initResource(emf::xmi::XMIResource* res);

    // 注入常用 load/save options（对齐 Java initDefaultOptions）
    virtual void initDefaultOptions(emf::xmi::XMIResource* res);

    // schema location catalog（namespace -> xsd 路径）
    const std::unordered_map<std::string, std::string>& getSchemaLocationCatalog() const {
        return schemaLocationCatalog_;
    }
    void addSchemaLocation(const std::string& ns, const std::string& schema) {
        schemaLocationCatalog_[ns] = schema;
    }

    // 注册 schema location base URIs（"platform:/plugin/..." 等）
    // 对齐 Java: initSchemaLocationBaseURIs()
    virtual void initSchemaLocationBaseURIs() = 0;

    // 从 .ecore 文件加载并注册 AUTOSAR 元模型到全局 EPackageRegistry
    // 对齐 Java: Autosar40Package.eINSTANCE 初始化时把 nsURI 注册到 EPackage.Registry
    // 加载后，arxml 实例文档的 <AUTOSAR xmlns="http://autosar.org/schema/r4.0">
    // 即可由 XMILoader 通过 nsURI 查到 EPackage 并实例化为 EObject。
    // ecorePath: .ecore 文件路径；返回注册的 EPackage（失败返回 nullptr）
    static emf::ecore::EPackage* registerMetamodelFromEcore(const std::string& ecorePath);

    // 注册内置的最小 autosar40 元模型（model/autosar40.ecore）
    // 幂等：重复调用安全（已注册则直接返回）
    static emf::ecore::EPackage* registerDefaultAutosar40Metamodel();

protected:
    std::shared_ptr<AutosarReleaseDescriptor> autosarRelease_;
    AutosarResourceCreator resourceCreator_;
    std::unordered_map<std::string, std::string> schemaLocationCatalog_;
};

// ===== AutosarXMLResourceFactory：XML 版本 =====
class AutosarXMLResourceFactory : public AutosarResourceFactory {
public:
    using AutosarResourceFactory::AutosarResourceFactory;

    // XML 资源工厂：默认 createResource 返回 AutosarXMLResource
    std::unique_ptr<emf::common::Resource> createResource(const emf::common::URI& uri) override;

    // 默认 createSchemaLocationCatalog：注册 baseNamespace -> xsd
    virtual std::unordered_map<std::string, std::string> createSchemaLocationCatalog();

    void initSchemaLocationBaseURIs() override;
};

}  // namespace emf::artop::runtime
