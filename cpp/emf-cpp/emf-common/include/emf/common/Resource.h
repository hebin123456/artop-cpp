// EMF Common: Resource / ResourceSet
// 对齐 org.eclipse.emf.ecore.resource.Resource, ResourceSet (Java)
#pragma once

#include "EObject.h"
#include "URI.h"
#include "EPackage.h"
#include "URIConverter.h"
#include <string>
#include <vector>
#include <memory>
#include <unordered_map>
#include <fstream>
#include <sstream>
#include <stdexcept>

namespace emf::common {

// 前向声明
class ResourceSet;

// Resource: EMF 资源（文件）
// 对齐 org.eclipse.emf.ecore.resource.Resource
class Resource {
public:
    explicit Resource(URI uri) : uri_(std::move(uri)) {}
    virtual ~Resource() = default;

    // URI
    const URI& getURI() const { return uri_; }
    void setURI(URI u) { uri_ = std::move(u); }

    // 内容
    EObject* getRoot() const { return root_; }
    void setRoot(EObject* root) { root_ = root; }

    const std::vector<EObject*>& getContents() const { return contents_; }
    // 可变 contents 访问（对齐 Java Resource.getContents() 返回 EList<EObject>）
    std::vector<EObject*>& getContents() { return contents_; }
    void addToContents(EObject* obj) {
        contents_.push_back(obj);
        // 建立 EObject→Resource 反向引用（对齐 Java ETreeAndContentSetList 设置 eDirectResource）
        if (obj) obj->eSetResource(this);
    }

    // ResourceSet 关联
    virtual ResourceSet* getResourceSet() const { return resourceSet_; }
    virtual void setResourceSet(ResourceSet* rs) { resourceSet_ = rs; }

    // 序列化/反序列化
    virtual void save(std::ostream& os) { (void)os; }
    virtual void load(std::istream& is) { (void)is; }

    // 带 URI 加载（支持跨文件引用）
    virtual void load() {
        if (uri_.isFile()) {
            std::string path = uri_.toFilePath();
            if (path.empty()) path = uri_.toString();
            std::ifstream f(path);
            if (!f) throw std::runtime_error("Cannot open file: " + path);
            load(f);
        } else if (auto stream = uriConverter_.createInputStream(uri_)) {
            load(*stream);
        }
    }

    virtual void save() {
        if (uri_.isFile()) {
            std::string path = uri_.toFilePath();
            if (path.empty()) path = uri_.toString();
            std::ofstream f(path);
            if (!f) throw std::runtime_error("Cannot write file: " + path);
            save(f);
        } else if (auto stream = uriConverter_.createOutputStream(uri_)) {
            save(*stream);
        }
    }

    std::string toXmiString() {
        std::ostringstream oss;
        save(oss);
        return oss.str();
    }

    void fromXmiString(const std::string& s) {
        std::istringstream iss(s);
        load(iss);
    }

    // 通过 URI fragment 获取对象
    // 对齐 Java: Resource.getEObject(String fragment)
    virtual EObject* getEObject(const std::string& fragment);

    // 获取对象的 URI fragment
    // 对齐 Java: Resource.getURIFragment(EObject)
    virtual std::string getURIFragment(EObject* obj);

    // 是否已加载
    virtual bool isLoaded() const { return isLoaded_; }
    virtual void setLoaded(bool b) { isLoaded_ = b; }

    // 是否修改过
    virtual bool isModified() const { return isModified_; }
    virtual void setModified(bool b) { isModified_ = b; }

    // 错误/警告
    virtual std::vector<std::string>& getErrors() { return errors_; }
    virtual std::vector<std::string>& getWarnings() { return warnings_; }

protected:
    URI uri_;
    EObject* root_ = nullptr;
    std::vector<EObject*> contents_;
    ResourceSet* resourceSet_ = nullptr;
    bool isLoaded_ = false;
    bool isModified_ = false;
    std::vector<std::string> errors_;
    std::vector<std::string> warnings_;
    
    // 默认 URIConverter（用于 I/O）
    static URIConverter uriConverter_;
    
    // 辅助方法
    EObject* resolvePositionPath(const std::string& path);
    bool findFragmentRecursive(const std::vector<EObject*>& objs, EObject* target, std::string& frag);
    std::vector<EObject*> anyToEObjectList(const std::any& v);
};

// ResourceSet: 管理多个 Resource 的容器
// 对齐 org.eclipse.emf.ecore.resource.ResourceSet
class ResourceSet {
public:
    ResourceSet() {
        uriConverter_ = std::make_unique<URIConverter>();
    }

    // 创建 Resource
    // 对齐 Java: ResourceSet.createResource(URI)
    virtual Resource* createResource(const URI& uri) = 0;

    // 获取 Resource（可选是否按需加载）
    // 对齐 Java: ResourceSet.getResource(URI, boolean)
    virtual Resource* getResource(const URI& uri, bool loadOnDemand) = 0;

    // 通过完整 URI 获取 EObject（包括 fragment）
    // 对齐 Java: ResourceSet.getEObject(URI, boolean)
    virtual EObject* getEObject(const URI& uri, bool loadOnDemand) = 0;

    // 获取 URI 转换器
    URIConverter& getURIConverter() { return *uriConverter_; }
    const URIConverter& getURIConverter() const { return *uriConverter_; }

    // 获取所有 Resource
    virtual const std::vector<std::unique_ptr<Resource>>& getResources() const = 0;

    // 时间戳
    virtual long long getTimeStamp() const { return timeStamp_; }
    virtual void setTimeStamp(long long ts) { timeStamp_ = ts; }

protected:
    std::unique_ptr<URIConverter> uriConverter_;
    long long timeStamp_ = 0;
};

}  // namespace emf::common
