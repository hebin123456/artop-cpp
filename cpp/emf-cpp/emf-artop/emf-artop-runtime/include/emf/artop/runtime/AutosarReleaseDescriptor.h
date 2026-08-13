// emf::artop::runtime —— AutosarReleaseDescriptor
// 对齐 Java: org.artop.aal.common.metamodel.AutosarReleaseDescriptor
//
// 描述一个 AUTOSAR release 的所有元数据：
//   - 版本号（AutosarMetaModelVersionData）
//   - base namespace URI（"http://autosar.org/schema/r4.0"）
//   - schema file 名称
//   - 默认 arxml content type id / file extension
//   - xsi:schemaLocation 内容
//   - 兼容的 release 列表
//
// 每个生成的 model（如 Autosar448）有一个 INSTANCE 描述符；
// 多个 release 共享一份代码（Autosar40ResourceFactoryImpl 内部维护
// 15 个版本描述符 4.0.1 ~ 4.4.8）。
#pragma once

#include "emf/artop/runtime/AutosarMetaModelVersionData.h"

#include <memory>
#include <string>
#include <vector>
#include <unordered_map>

namespace emf::artop::runtime {

class AutosarReleaseDescriptor {
public:
    // 公共常量
    static constexpr const char* ID                                            = "org.artop.aal.autosar.release";
    static constexpr const char* BASE_NAME                                     = "AUTOSAR";
    static constexpr const char* BASE_NAMESPACE                                = "http://autosar.org/schema/r4.0";
    static constexpr const char* ARXML_BASE_CONTENT_TYPE_ID                    = "org.artop.aal.autosar.contenttype";
    static constexpr const char* ARXML_DEFAULT_FILE_EXTENSION                  = "arxml";
    static constexpr const char* AUTOSAR_SCHEMA_FILE_NAME_PREFIX               = "AUTOSAR_";
    static constexpr const char* AUTOSAR_SCHEMA_VERSION_NUMBER_SEPARATOR      = "-";
    static constexpr const char* XSD_FILE_EXTENSION                            = "xsd";

    // 全局默认 INSTANCE（"兜底"）—— 对齐 Java INSTANCE
    static AutosarReleaseDescriptor& getInstance();

    AutosarReleaseDescriptor() = default;
    AutosarReleaseDescriptor(std::string id,
                              AutosarMetaModelVersionData version);

    // ---- 元数据访问 ----
    const std::string& getId() const { return id_; }
    const std::string& getName() const { return name_; }
    void setName(const std::string& n) { name_ = n; }

    const AutosarMetaModelVersionData& getAutosarVersionData() const { return versionData_; }
    void setAutosarVersionData(const AutosarMetaModelVersionData& v) { versionData_ = v; }

    // ---- 命名空间与 schema location ----
    const std::string& getBaseNamespace() const { return baseNamespace_; }
    void setBaseNamespace(const std::string& ns) { baseNamespace_ = ns; }

    // xsi:schemaLocation 内容："http://autosar.org/schema/r4.0 AUTOSAR_00048.xsd"
    std::string getSchemaLocationBase() const;
    std::string getSchemaLocation() const;
    bool matchesSchemaLocation(const std::string& sl) const;

    // ---- Content type ----
    std::string getDefaultContentTypeId() const;
    void setDefaultContentTypeId(const std::string& id) { defaultContentTypeId_ = id; }

    // ---- 兼容 release ----
    const std::vector<std::shared_ptr<AutosarReleaseDescriptor>>& getCompatibleDescriptors() const {
        return compatibleDescriptors_;
    }
    void addCompatibleDescriptor(std::shared_ptr<AutosarReleaseDescriptor> d) {
        compatibleDescriptors_.push_back(std::move(d));
    }

    // 比较：版本号升序
    int compareTo(const AutosarReleaseDescriptor& other) const {
        if (versionData_ < other.versionData_) return -1;
        if (other.versionData_ < versionData_) return 1;
        return 0;
    }
    bool operator==(const AutosarReleaseDescriptor& other) const {
        return versionData_ == other.versionData_;
    }
    bool operator!=(const AutosarReleaseDescriptor& other) const { return !(*this == other); }

    // ---- Library descriptor 简表 ----
    // Java: getAutosarLibraryDescriptors() 返回 Map<AutosarLibraryIDEnumerator, AutosarLibraryDescriptor>
    // C++ 简化为空 map 占位。
    const std::unordered_map<std::string, std::string>& getAutosarLibraryDescriptors() const {
        return libraries_;
    }
    void addAutosarLibraryDescriptor(const std::string& id, const std::string& desc) {
        libraries_[id] = desc;
    }

    // 工厂方法
    static std::shared_ptr<AutosarReleaseDescriptor> create(
        const std::string& id, const AutosarMetaModelVersionData& v);

private:
    std::string id_;
    std::string name_;
    AutosarMetaModelVersionData versionData_;
    std::string baseNamespace_ = BASE_NAMESPACE;
    std::string defaultContentTypeId_ = ARXML_BASE_CONTENT_TYPE_ID;
    std::vector<std::shared_ptr<AutosarReleaseDescriptor>> compatibleDescriptors_;
    std::unordered_map<std::string, std::string> libraries_;  // simplified
};

}  // namespace emf::artop::runtime
