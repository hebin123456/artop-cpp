// ExtendedXMLSave.h
// 对齐 Java org.eclipse.sphinx.emf.resource.ExtendedXMLSaveImpl
// Sphinx 扩展的 XML 保存实现：在 emf::xmi::XMLSaveImpl 基础上增加
//   - schema location catalog 处理（OPTION_SCHEMA_LOCATION_CATALOG）
//   - resource 定义的 schema location entries
//   - 通过 ExtendedResource 获取上下文相关 schema location
#pragma once

#include "emf/xmi/XMLLoad.h"  // XMLSaveImpl 基类
#include <map>
#include <string>

namespace emf::sphinx::resource {

class ExtendedResource;

// ExtendedXMLSave: 对齐 Java ExtendedXMLSaveImpl（继承 XMLSaveImpl）。
// Sphinx 的关键行为：在写出根元素前根据 ExtendedResource 提供的 schema
// location entries 或 OPTION_SCHEMA_LOCATION_CATALOG 计算 xsi:schemaLocation
// 并写到 resource 上，再委托基类 XMLSaveImpl::save 完成序列化。
class ExtendedXMLSave : public emf::xmi::XMLSaveImpl {
public:
    ExtendedXMLSave() = default;
    explicit ExtendedXMLSave(ExtendedResource* extendedResource);
    ~ExtendedXMLSave() override = default;

    // 保存入口：计算并设置 xsi:schemaLocation 后委托给基类 XMLSaveImpl::save。
    void save(const emf::xmi::XMIResource* resource, std::ostream& output,
              const emf::xmi::XMIOptions& options) override;

    void setExtendedResource(ExtendedResource* r) { extendedResource_ = r; }
    ExtendedResource* getExtendedResource() const { return extendedResource_; }

    // Schema location catalog: namespace -> location
    // （对齐 Java: ExtendedResource.OPTION_SCHEMA_LOCATION_CATALOG）。
    void setSchemaLocationCatalog(std::map<std::string, std::string> catalog) {
        schemaLocationCatalog_ = std::move(catalog);
    }
    const std::map<std::string, std::string>& getSchemaLocationCatalog() const {
        return schemaLocationCatalog_;
    }

protected:
    // 收集 schema location 条目：优先使用 ExtendedResource 定义的 entries，
    // 否则回退到 schema location catalog。
    // 对齐 Java: ExtendedXMLSaveImpl.addNamespaceDeclarations() 中的条目收集逻辑。
    virtual std::map<std::string, std::string> collectSchemaLocationEntries(
        const emf::xmi::XMIResource* resource);

    // 将 namespace -> location 条目拼成 xsi:schemaLocation 值字符串
    // （形如 "ns1 loc1 ns2 loc2"）。
    static std::string buildSchemaLocationString(
        const std::map<std::string, std::string>& entries);

    ExtendedResource* extendedResource_ = nullptr;
    std::map<std::string, std::string> schemaLocationCatalog_;
};

}  // namespace emf::sphinx::resource
