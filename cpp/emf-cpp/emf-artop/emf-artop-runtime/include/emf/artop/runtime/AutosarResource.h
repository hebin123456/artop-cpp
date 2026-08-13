// emf::artop::runtime —— AutosarResource / AutosarXMLResource
// 对齐 Java:
//   org.artop.aal.common.resource.impl.AutosarResourceImpl
//   org.artop.aal.common.resource.impl.AutosarXMLResourceImpl
//
// 职责：
//   1. 在 XMIResource 基础上挂载 AutosarReleaseDescriptor
//   2. AutosarXMLResource 重写 createXMLSave/createXMLLoad 注入 AutosarXMLSaver/AutosarXMLLoader
//   3. 提供 schemaLocation / encoding 等 arxml 特有属性
//
// arxml 格式（R4.0）与标准 XMI 的差异：
//   - 根元素 <AUTOSAR> 而非 <xmi:XMI>
//   - 无 xmi:id / xmi:version，用 shortName path 定位
//   - xsi:schemaLocation 指向 AUTOSAR_xxxxx.xsd
#pragma once

#include "emf/xmi/XMIResource.h"
#include "emf/xmi/XMLLoad.h"
#include "emf/xmi/XMLHelper.h"
#include "emf/artop/runtime/AutosarReleaseDescriptor.h"

#include <memory>
#include <string>

namespace emf::artop::runtime {

// 前向声明（打破循环 include）
class AutosarXMLLoader;
class AutosarXMLSaver;

// ===== AutosarResource：带 AUTOSAR release 描述符的 XMIResource =====
class AutosarResource : public emf::xmi::XMIResource {
public:
    AutosarResource() = default;
    explicit AutosarResource(emf::common::URI uri);
    AutosarResource(emf::common::URI uri,
                    std::shared_ptr<AutosarReleaseDescriptor> release);

    virtual ~AutosarResource() = default;

    // 关联的 AUTOSAR release 描述符
    const std::shared_ptr<AutosarReleaseDescriptor>& getAutosarRelease() const { return autosarRelease_; }
    void setAutosarRelease(std::shared_ptr<AutosarReleaseDescriptor> r) { autosarRelease_ = std::move(r); }

    // schemaLocation（覆盖 XMIResource 的 xsi:schemaLocation）
    const std::string& getSchemaLocation() const { return schemaLocation_; }
    void setSchemaLocation(const std::string& s) { schemaLocation_ = s; }

    // XMLHelper 工厂（对齐 Java AutosarResourceImpl.createXMLHelper）
    virtual std::unique_ptr<emf::xmi::XMLHelper> createXMLHelper();

    // 将本 resource 的所有 GReferrable shortName path 注册到全局
    // AutosarLibraryIndex，供其他 resource 的跨文档引用 demand-load 解析。
    // 对齐 Java ARTOP Library 机制：library resource 加载后注册全局 path 索引。
    // 典型用法：加载 library arxml 后调用 libRes->indexLibrary();
    void indexLibrary();

private:
    std::shared_ptr<AutosarReleaseDescriptor> autosarRelease_;
    std::string schemaLocation_;
};

// ===== AutosarXMLResource：XML（arxml）专用 Resource =====
// 对齐 Java: AutosarXMLResourceImpl
class AutosarXMLResource : public AutosarResource {
public:
    AutosarXMLResource() = default;
    explicit AutosarXMLResource(emf::common::URI uri)
        : AutosarResource(std::move(uri)) {}
    AutosarXMLResource(emf::common::URI uri,
                       std::shared_ptr<AutosarReleaseDescriptor> release)
        : AutosarResource(std::move(uri), std::move(release)) {}

    ~AutosarXMLResource() override;

    // 注入 AutosarXMLSaver / AutosarXMLLoader（对齐 Java 重写 createXMLSave/createXMLLoad）
    std::shared_ptr<emf::xmi::XMLSave> createXMLSave() const override;
    std::shared_ptr<emf::xmi::XMLLoad> createXMLLoad() const override;

    std::unique_ptr<emf::xmi::XMLHelper> createXMLHelper() override;

    // getEObject：对齐 Java AutosarXMLResourceImpl.getEObject
    // 处理 schemaLocationCache，简化为委托父类
    emf::common::EObject* getEObject(const std::string& fragment) override;
};

}  // namespace emf::artop::runtime
