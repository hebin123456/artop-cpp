// AbstractMetaModelDescriptor.h
// 对齐 Java org.eclipse.sphinx.emf.metamodel.AbstractMetaModelDescriptor
#pragma once

#include "emf/sphinx/metamodel/IMetaModelDescriptor.h"
#include "emf/sphinx/metamodel/MetaModelVersionData.h"
#include <regex>

namespace emf::sphinx::metamodel {

// AbstractMetaModelDescriptor：IMetaModelDescriptor 的标准实现
// 携带 identifier / baseNamespace / ePackageNsURIPostfixPattern / name / version data
// 对齐 Java AbstractMetaModelDescriptor（org.eclipse.sphinx.emf.metamodel.AbstractMetaModelDescriptor）
class AbstractMetaModelDescriptor : public IMetaModelDescriptor {
public:
    AbstractMetaModelDescriptor() = default;
    ~AbstractMetaModelDescriptor() override = default;

    // 三参构造：identifier / namespace / name
    AbstractMetaModelDescriptor(const std::string& identifier,
                                const std::string& ns,
                                const std::string& name)
        : identifier_(identifier), name_(name), baseNamespaceURI_(ns) {
        initNamespace();
    }

    // 四参构造（多 EPackage）：identifier / baseNamespace / ePackageNsURIPostfixPattern / name
    AbstractMetaModelDescriptor(const std::string& identifier,
                                const std::string& baseNamespace,
                                const std::string& ePackageNsURIPostfixPattern,
                                const std::string& name)
        : identifier_(identifier), name_(name),
          baseNamespaceURI_(baseNamespace),
          ePackageNsURIPostfixPattern_(ePackageNsURIPostfixPattern) {
        initNamespace();
    }

    // 三参构造（带 version）：identifier / baseNamespace / versionData
    AbstractMetaModelDescriptor(const std::string& identifier,
                                const std::string& baseNamespace,
                                const MetaModelVersionData& versionData)
        : identifier_(identifier),
          baseNamespaceURI_(baseNamespace),
          versionData_(std::make_unique<MetaModelVersionData>(versionData)) {
        initNamespace();
        // 名字从 versionData 取
        if (name_.empty()) name_ = versionData.getName();
    }

    // ===== IMetaModelDescriptor 接口 =====
    std::string getIdentifier() const override { return identifier_; }
    void setIdentifier(const std::string& v) { identifier_ = v; }
    emf::common::URI getNamespaceURI() const override { return namespaceURI_; }
    void setNamespaceURI(const emf::common::URI& v) { namespaceURI_ = v; baseNamespaceURI_ = v.toString(); }
    std::string getNamespace() const override { return namespaceURI_.toString(); }
    std::string getName() const override { return name_; }
    void setName(const std::string& v) { name_ = v; }
    IMetaModelDescriptor* getBaseDescriptor() const override;
    std::string getCustomURIScheme() const override { return customURIScheme_; }
    void setCustomURIScheme(const std::string& v) { customURIScheme_ = v; }
    int getOrdinal() const override;
    std::string getEPackageNsURIPattern() const override;

    // 子类 hook：默认返回空字符串，对应 getDefaultContentTypeId()
    virtual std::string getDefaultContentTypeId() const { return ""; }

    // 比较
    bool matchesNamespace(const std::string& ns) const override;
    bool matchesEPackageNsURIPattern(const std::string& ns) const override;
    bool equals(const IMetaModelDescriptor* other) const override;

    // 兼容版本
    std::vector<IMetaModelDescriptor*> getCompatibleResourceVersionDescriptors() const override { return compatible_; }
    void addCompatibleResourceVersionDescriptor(IMetaModelDescriptor* d) { compatible_.push_back(d); }

    // 内部访问
    const std::string& getBaseNamespaceURI() const { return baseNamespaceURI_; }
    const MetaModelVersionData* getVersionData() const { return versionData_.get(); }
    void setVersionData(const MetaModelVersionData& v) {
        versionData_ = std::make_unique<MetaModelVersionData>(v);
        name_ = v.getName();
        initNamespace();
        initEPackageNsURIPattern();
    }

    // 哈希：按 identifier
    int hashCode() const;

protected:
    // 初始化 namespace：base + (version postfix)
    void initNamespace();
    // 初始化 EPackage ns URI 模式
    void initEPackageNsURIPattern();

    std::string identifier_;
    std::string baseNamespaceURI_;
    emf::common::URI namespaceURI_;
    std::string ePackageNsURIPostfixPattern_;
    std::string name_;
    std::unique_ptr<MetaModelVersionData> versionData_;
    std::string customURIScheme_;
    // EPackage ns URI pattern 缓存
    std::string ePackageNsURIPatternStr_;
    mutable std::regex ePackageNsURIPattern_;
    mutable bool ePackageNsURIPatternInit_ = false;
    std::vector<IMetaModelDescriptor*> compatible_;
};

}  // namespace emf::sphinx::metamodel
