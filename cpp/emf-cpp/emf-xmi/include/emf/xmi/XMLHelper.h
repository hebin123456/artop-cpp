// emf::xmi —— XMLHelper
// 对齐 Java: org.eclipse.emf.ecore.xmi.XMLHelper
//
// XML 序列化/反序列化的配置类。
// 负责：
//   - EPackage / XMLMap / ExtendedMetaData 配置
//   - URI 解析 / 反解析（href / xsi:schemaLocation）
//   - 前缀/命名空间上下文管理（push/pop/addPrefix/getURI）
//   - 编码转换（getXMLEncoding / getJavaEncoding）
//   - EStructuralFeature 查询（getFeature / getFeatureKind）
//   - 值转换（setValue / convertToString / getQName）
#pragma once

#include "emf/common/EObject.h"
#include "emf/common/Resource.h"
#include "emf/common/URI.h"
#include <string>
#include <vector>
#include <unordered_map>
#include <any>
#include <functional>

namespace emf::ecore { class EPackage; class EClass; class EDataType; class EFactory; class EClassifier; class EStructuralFeature; }
namespace emf::xmi {

class XMLResource;

// ===== XMLHelper 抽象接口（对齐 Java XMLHelper） =====
class XMLHelper {
public:
    // Feature kind 常量（对齐 Java XMLHelper.DATATYPE_SINGLE 等）
    static constexpr int DATATYPE_SINGLE  = 1;
    static constexpr int DATATYPE_IS_MANY = 2;
    static constexpr int IS_MANY_ADD      = 3;
    static constexpr int IS_MANY_MOVE     = 4;
    static constexpr int OTHER            = 5;

    virtual ~XMLHelper() = default;

    // ----- 配置 -----
    virtual void setOptions(const std::unordered_map<std::string, std::any>& options) = 0;
    virtual void setNoNamespacePackage(::emf::ecore::EPackage* pkg) = 0;
    virtual ::emf::ecore::EPackage* getNoNamespacePackage() const = 0;
    virtual void setXMLMap(void* map) = 0;  // XMLResource.XMLMap（仅占位）

    // ----- 资源 -----
    virtual void setResource(emf::common::Resource* res) = 0;
    virtual emf::common::Resource* getResource() const = 0;

    // ----- 编码 -----
    virtual std::string getXMLEncoding(const std::string& javaEncoding) const = 0;
    virtual std::string getJavaEncoding(const std::string& xmlEncoding) const = 0;

    // ----- 命名空间上下文（对齐 Java XMLHelper.addPrefix/pushContext/popContext/getURI） -----
    virtual void pushContext() = 0;
    virtual void popContext() = 0;
    virtual void addPrefix(const std::string& prefix, const std::string& uri) = 0;
    virtual std::string getURI(const std::string& prefix) const = 0;
    virtual std::string getPrefix(const std::string& namespaceURI) const = 0;
    virtual std::string getNamespaceURI(const std::string& prefix) const = 0;
    virtual void recordPrefixToURIMapping() = 0;

    // ----- ID/HREF -----
    virtual std::string getID(::emf::common::EObject* obj) const = 0;
    virtual std::string getIDREF(::emf::common::EObject* obj) const = 0;
    virtual std::string getHREF(::emf::common::EObject* obj) const = 0;

    // ----- Feature 查询 -----
    virtual ::emf::ecore::EStructuralFeature* getFeature(::emf::ecore::EClass* eClass,
                                                          const std::string& namespaceURI,
                                                          const std::string& name) = 0;
    virtual int getFeatureKind(::emf::ecore::EStructuralFeature* feature) = 0;

    // ----- 值操作 -----
    virtual void setValue(::emf::common::EObject* obj,
                          ::emf::ecore::EStructuralFeature* feature,
                          const std::any& value, int position) = 0;
    virtual std::any getValue(::emf::common::EObject* obj,
                              ::emf::ecore::EStructuralFeature* feature) = 0;
    virtual std::string convertToString(::emf::ecore::EFactory* factory,
                                        ::emf::ecore::EDataType* dataType,
                                        const std::any& value) = 0;

    // ----- URI 解析 -----
    virtual emf::common::URI deresolve(const emf::common::URI& uri) = 0;
    virtual emf::common::URI resolve(const emf::common::URI& relative,
                                    const emf::common::URI& base) = 0;

    // ----- 工厂方法 -----
    virtual ::emf::common::EObject* createObject(::emf::ecore::EFactory* factory,
                                                ::emf::ecore::EClassifier* type) = 0;
    virtual ::emf::ecore::EClassifier* getType(::emf::ecore::EFactory* factory,
                                               const std::string& typeName) = 0;
};

// ===== XMLHelperImpl — 对应 Java XMLHelperImpl 的最常用子集 =====
class XMLHelperImpl : public XMLHelper {
public:
    XMLHelperImpl();
    explicit XMLHelperImpl(emf::common::Resource* res);
    ~XMLHelperImpl() override = default;

    // ----- 配置 -----
    void setOptions(const std::unordered_map<std::string, std::any>& options) override;
    void setNoNamespacePackage(::emf::ecore::EPackage* pkg) override;
    ::emf::ecore::EPackage* getNoNamespacePackage() const override;
    void setXMLMap(void* map) override { xmlMap_ = map; }
    void* getXMLMap() const { return xmlMap_; }

    // ----- 资源 -----
    void setResource(emf::common::Resource* res) override;
    emf::common::Resource* getResource() const override { return resource_; }

    // ----- 编码 -----
    std::string getXMLEncoding(const std::string& javaEncoding) const override;
    std::string getJavaEncoding(const std::string& xmlEncoding) const override;

    // ----- 命名空间上下文 -----
    void pushContext() override;
    void popContext() override;
    void addPrefix(const std::string& prefix, const std::string& uri) override;
    std::string getURI(const std::string& prefix) const override;
    std::string getPrefix(const std::string& namespaceURI) const override;
    std::string getNamespaceURI(const std::string& prefix) const override;
    void recordPrefixToURIMapping() override;
    void setPrefixToNamespaceMap(const std::unordered_map<std::string, std::string>& m);
    const std::unordered_map<std::string, std::string>& getPrefixToNamespaceMap() const { return prefixesToURIs_; }

    // ----- ID/HREF -----
    std::string getID(::emf::common::EObject* obj) const override;
    std::string getIDREF(::emf::common::EObject* obj) const override;
    std::string getHREF(::emf::common::EObject* obj) const override;

    // ----- Feature 查询 -----
    ::emf::ecore::EStructuralFeature* getFeature(::emf::ecore::EClass* eClass,
                                                  const std::string& namespaceURI,
                                                  const std::string& name) override;
    int getFeatureKind(::emf::ecore::EStructuralFeature* feature) override;

    // ----- 值操作 -----
    void setValue(::emf::common::EObject* obj,
                  ::emf::ecore::EStructuralFeature* feature,
                  const std::any& value, int position) override;
    std::any getValue(::emf::common::EObject* obj,
                      ::emf::ecore::EStructuralFeature* feature) override;
    std::string convertToString(::emf::ecore::EFactory* factory,
                                ::emf::ecore::EDataType* dataType,
                                const std::any& value) override;

    // ----- URI 解析 -----
    emf::common::URI deresolve(const emf::common::URI& uri) override;
    emf::common::URI resolve(const emf::common::URI& relative,
                             const emf::common::URI& base) override;

    // ----- 工厂方法 -----
    ::emf::common::EObject* createObject(::emf::ecore::EFactory* factory,
                                        ::emf::ecore::EClassifier* type) override;
    ::emf::ecore::EClassifier* getType(::emf::ecore::EFactory* factory,
                                       const std::string& typeName) override;

    // ----- base URI（HREF 解析相关，对齐 Java XMLHelper 的隐含 base） -----
    void setBaseURI(const emf::common::URI& u) { baseURI_ = u; }
    const emf::common::URI& getBaseURI() const { return baseURI_; }

    // ----- checkForDuplicates（IS_MANY_ADD 时去重） -----
    void setCheckForDuplicates(bool b) { checkForDuplicates_ = b; }
    bool getCheckForDuplicates() const { return checkForDuplicates_; }

protected:
    // 命名空间栈（对齐 Java NamespaceSupport）
    struct NamespaceContext {
        // 当前上下文（栈）的 namespace 数组
        // 存储 [prefix, uri, prefix, uri, ...] 形式
        std::vector<std::string> table;
        std::vector<int> contextMarks;  // 每个 context 的边界
        int currentContext = -1;
    };
    NamespaceContext nsCtx_;
    std::unordered_map<std::string, std::string> prefixesToURIs_;
    std::unordered_map<std::string, std::string> urisToPrefixes_;

    void declarePrefix(const std::string& prefix, const std::string& uri);
    std::string lookupURI(const std::string& prefix) const;
    std::string lookupPrefix(const std::string& uri) const;
    void popContextImpl(bool removeFromFactories);

    // 计算 feature kind
    int computeFeatureKind(::emf::ecore::EStructuralFeature* feature);

    ::emf::ecore::EPackage* noNamespacePackage_ = nullptr;
    void* xmlMap_ = nullptr;  // XMLResource.XMLMap*（占位，类型未引用以避免循环依赖）
    emf::common::Resource* resource_ = nullptr;
    emf::common::URI resourceURI_;
    emf::common::URI baseURI_;
    std::unordered_map<::emf::ecore::EStructuralFeature*, int> featuresToKinds_;
    bool checkForDuplicates_ = false;
    bool deresolve_ = false;
};

}  // namespace emf::xmi
