// MetaModelVersionData.h
// 对齐 Java org.eclipse.sphinx.emf.metamodel.MetaModelVersionData
#pragma once

#include <string>
#include <memory>

namespace emf::sphinx::metamodel {
class IMetaModelDescriptor;
}

namespace emf::sphinx::metamodel {

// 版本数据：标识一个具体版本的元模型（ns postfix / ePackage ns postfix pattern / name / base descriptor / ordinal）
// 对齐 Java MetaModelVersionData（org.eclipse.sphinx.emf.metamodel.MetaModelVersionData）
class MetaModelVersionData {
public:
    MetaModelVersionData() = default;

    // 三参构造：postfix / ePackage pattern / name
    MetaModelVersionData(const std::string& nsPostfix,
                         const std::string& ePackageNsURIPostfixPattern,
                         const std::string& name)
        : nsPostfix_(nsPostfix),
          ePackageNsURIPostfixPattern_(ePackageNsURIPostfixPattern),
          name_(name) {}

    // 四参构造（带 ordinal，已 deprecated）
    MetaModelVersionData(const std::string& nsPostfix,
                         const std::string& ePackageNsURIPostfixPattern,
                         const std::string& name,
                         int ordinal)
        : nsPostfix_(nsPostfix),
          ePackageNsURIPostfixPattern_(ePackageNsURIPostfixPattern),
          name_(name),
          ordinal_(ordinal) {}

    // 五参构造（带 base descriptor）
    MetaModelVersionData(const std::string& nsPostfix,
                         const std::string& ePackageNsURIPostfixPattern,
                         const std::string& name,
                         IMetaModelDescriptor* baseDescriptor)
        : nsPostfix_(nsPostfix),
          ePackageNsURIPostfixPattern_(ePackageNsURIPostfixPattern),
          name_(name),
          baseDescriptor_(baseDescriptor) {}

    // 访问器
    const std::string& getNsPostfix() const { return nsPostfix_; }
    const std::string& getEPackageNsURIPostfixPattern() const { return ePackageNsURIPostfixPattern_; }
    const std::string& getName() const { return name_; }
    IMetaModelDescriptor* getBaseDescriptor() const { return baseDescriptor_; }
    int getOrdinal() const { return ordinal_; }

    void setNsPostfix(const std::string& v) { nsPostfix_ = v; }
    void setEPackageNsURIPostfixPattern(const std::string& v) { ePackageNsURIPostfixPattern_ = v; }
    void setName(const std::string& v) { name_ = v; }
    void setBaseDescriptor(IMetaModelDescriptor* d) { baseDescriptor_ = d; }
    void setOrdinal(int v) { ordinal_ = v; }

    // equals/hashCode
    bool equals(const MetaModelVersionData& other) const {
        return nsPostfix_ == other.nsPostfix_
            && ePackageNsURIPostfixPattern_ == other.ePackageNsURIPostfixPattern_
            && name_ == other.name_
            && baseDescriptor_ == other.baseDescriptor_
            && ordinal_ == other.ordinal_;
    }

private:
    std::string nsPostfix_;
    std::string ePackageNsURIPostfixPattern_;
    std::string name_;
    IMetaModelDescriptor* baseDescriptor_ = nullptr;
    int ordinal_ = -1;
};

}  // namespace emf::sphinx::metamodel
