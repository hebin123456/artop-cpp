// emf::xmi —— XMI 序列化/反序列化
// 对齐 Java: org.eclipse.emf.ecore.xmi
#pragma once

#include "emf/common/Resource.h"
#include "emf/xmi/XMLLoad.h"
#include "XMIOptions.h"
#include <memory>
#include <unordered_map>
#include <string>

namespace emf::xmi {

// XMIResource: 支持 XMI 序列化的 Resource
// 对齐 org.eclipse.emf.ecore.xmi.XMIResource (Java)
class XMIResource : public emf::common::Resource {
public:
    XMIResource() : emf::common::Resource(emf::common::URI()) {}
    explicit XMIResource(emf::common::URI uri) : emf::common::Resource(std::move(uri)) {}

    // XMI 中每个 EObject 通常会有 xmi:id，这里是 id -> EObject 映射
    const std::unordered_map<std::string, emf::common::EObject*>& getIDToEObjectMap() const { return idToEObject_; }
    std::unordered_map<std::string, emf::common::EObject*>& getIDToEObjectMap() { return idToEObject_; }

    // EObject -> id
    std::string getID(emf::common::EObject* obj) const;
    void setID(emf::common::EObject* obj, const std::string& id);
    emf::common::EObject* getEObjectByID(const std::string& id) const;

    // UUID 支持 —— 对齐 Java XMIResource.USE_UUIDs option
    // 生成 RFC 4122 v4 UUID（基于 std::mt19937_64）。
    // 若 useUUIDs_=true 且 obj 尚未分配 id，自动分配 UUID 并注册到 idToEObject_。
    std::string generateUUID();
    // 确保 obj 有一个 id：若 useUUIDs_=true 且无 id，自动分配；否则返回 ""
    std::string ensureID(emf::common::EObject* obj);

    // 通过 URI fragment 获取对象（支持 xmi:id 查找）
    // 对齐 Java: Resource.getEObject(String)
    emf::common::EObject* getEObject(const std::string& fragment) override;

    // 序列化 / 反序列化
    void save(std::ostream& os) override;
    void load(std::istream& is) override;

    // 带选项的序列化
    void save(std::ostream& os, const XMIOptions& opts);
    void load(std::istream& is, const XMIOptions& opts);

    // 字符串便利接口
    void loadFromString(const std::string& xml, const XMIOptions& opts = {});
    std::string saveToString(const XMIOptions& opts = {}) const;

    // XMLSave / XMLLoad 注入点（对齐 Java XMIResource.getXMLSave/getXMLLoad）
    // 默认 lazy 实例化为 XMLSaveImpl / XMLLoadImpl。
    // 用户可注入自定义实现（如自定义 XMLHandler 的子类）。
    void setXMLSave(std::shared_ptr<XMLSave> saver) { xmlSave_ = std::move(saver); }
    std::shared_ptr<XMLSave> getXMLSave() const;
    void setXMLLoad(std::shared_ptr<XMLLoad> loader) { xmlLoad_ = std::move(loader); }
    std::shared_ptr<XMLLoad> getXMLLoad() const;

    // 工厂方法（对齐 Java XMIResource.createXMLSave/createXMLLoad）
    // 子类（如 AutosarXMLResource）重写以注入自定义 saver/loader。
    // getXMLSave()/getXMLLoad() 在 lazy 创建时调用此方法。
    virtual std::shared_ptr<XMLSave> createXMLSave() const;
    virtual std::shared_ptr<XMLLoad> createXMLLoad() const;

    // XMI 选项（占位接口，简单存储标志位）
    void setUseUUIDs(bool b = true) { useUUIDs_ = b; }
    bool usesUUIDs() const { return useUUIDs_; }

    void setEncoding(const std::string& enc) { encoding_ = enc; }
    const std::string& getEncoding() const { return encoding_; }

    // XMI 版本
    void setXmiVersion(const std::string& v) { xmiVersion_ = v; }
    const std::string& getXmiVersion() const { return xmiVersion_; }

    // xsi:schemaLocation 内容（XMI root 元素的属性）
    // 对齐 Java: XMIResource.getXSISchemaLocation()
    void setXSISchemaLocation(const std::string& s) { xsiSchemaLocation_ = s; }
    const std::string& getXSISchemaLocation() const { return xsiSchemaLocation_; }

    // 设置/获取 ResourceSet（支持跨文件引用）
    void setResourceSet(emf::common::ResourceSet* rs) override;
    emf::common::ResourceSet* getResourceSet() const override { return resourceSet_; }
    
    // 解析 position path（供 loader 使用）
    emf::common::EObject* resolvePositionPath(const std::string& path);

    // 跨文档 href 保留（对齐 Java resource URI deresolve 输出）
    // loader 解析跨文档 eType/eClassifier 引用时，若原始 href 是文件路径形式
    // （如 ../../org.eclipse.emf.ecore/model/Ecore.ecore#//EBoolean，而非 nsURI），
    // 保留原始 href 字符串，以 owner EObject* 为 key。saver 优先输出保留的 href，
    // 实现与 Java EMF 一致的相对路径 deresolve。
    // 按 owner 键而非 target EClassifier 键：同一 EClassifier 可能被不同 owner 以
    // nsURI 与相对路径两种形式引用（如 GenModel.ecore 中 EString），按 owner 键可
    // 保留每条引用各自的原始形式。
    std::unordered_map<emf::common::EObject*, std::string>& crossDocHrefs() { return crossDocHrefs_; }
    const std::unordered_map<emf::common::EObject*, std::string>& crossDocHrefs() const { return crossDocHrefs_; }

    // ===== 未知元素记录（对齐 Java XMLResource.OPTION_RECORD_UNKNOWN_FEATURE）=====
    // 当 recordUnknownFeature_=true 时，loader 遇到无法映射到 EStructuralFeature
    // 的 XML 属性/子元素，记录到 unknownFeatures_：owner EObject* -> 子元素 UnknownElement 列表。
    // saver 在保存 owner 后原样输出，实现 round-trip 保持（不丢内容）。
    // 对齐 Java ARTOP createFeatureFromSkippedElement 的"不丢内容"语义。
    struct UnknownFeatureEntry {
        std::string ownerPath;  // owner 在 resource 内的路径（兜底 key，避免悬空指针）
    };
    // owner EObject* -> 该对象上记录的未知子元素（含原始 XML 文本/属性）
    // 用 owner 指针作 key，saver 遍历到同一 owner 时输出。
    std::vector<std::pair<emf::common::EObject*, std::string>>& unknownContents() { return unknownContents_; }
    const std::vector<std::pair<emf::common::EObject*, std::string>>& unknownContents() const { return unknownContents_; }
    void addUnknownContent(emf::common::EObject* owner, const std::string& xmlFragment) {
        unknownContents_.emplace_back(owner, xmlFragment);
    }
    bool isRecordUnknownFeature() const { return recordUnknownFeature_; }
    void setRecordUnknownFeature(bool b) { recordUnknownFeature_ = b; }

private:
    std::unordered_map<std::string, emf::common::EObject*> idToEObject_;
    std::unordered_map<emf::common::EObject*, std::string> eObjectToID_;
    bool useUUIDs_ = false;
    std::string encoding_ = "UTF-8";
    std::string xmiVersion_ = "2.0";
    emf::common::ResourceSet* resourceSet_ = nullptr;
    std::string xsiSchemaLocation_;
    // XMLSave / XMLLoad 实例（lazy 创建）；用户可通过 setter 注入。
    mutable std::shared_ptr<XMLSave> xmlSave_;
    mutable std::shared_ptr<XMLLoad> xmlLoad_;
    // 跨文档 href 保留（owner EObject* -> 原始 href 字符串）
    std::unordered_map<emf::common::EObject*, std::string> crossDocHrefs_;
    // 未知元素记录（owner EObject* -> 原始 XML 片段）
    std::vector<std::pair<emf::common::EObject*, std::string>> unknownContents_;
    bool recordUnknownFeature_ = false;

    std::vector<emf::common::EObject*> anyToEObjectList(const std::any& v);
};

}  // namespace emf::xmi
