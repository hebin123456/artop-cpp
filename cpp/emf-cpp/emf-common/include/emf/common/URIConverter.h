// emf::common::URIConverter —— URI 规范化、相对/绝对 URI 转换
// 对齐 Java: org.eclipse.emf.ecore.resource.URIConverter
#pragma once

#include "URI.h"
#include <string>
#include <unordered_map>
#include <fstream>
#include <memory>

namespace emf::common {

// URI 转换器：规范化 URI、相对/绝对转换、文件 I/O
// 对齐 Java: org.eclipse.emf.ecore.resource.URIConverter
class URIConverter {
public:
    // URI 映射：逻辑 URI -> 物理 URI
    using URIMap = std::unordered_map<std::string, std::string>;

    // 获取 URI 映射
    virtual const URIMap& getURIMap() const { return uriMap_; }
    virtual URIMap& getURIMap() { return uriMap_; }

    // 规范化 URI：应用 URIMap + 处理相对 URI
    // 对齐 Java: URIConverter.normalize(URI)
    // 相对 URI 如果有相对路径（包含 ./ 或 ../），自动加上 file: 前缀
    virtual URI normalize(const URI& uri) const;

    // 解析相对 URI 为绝对 URI（基于 base URI）
    // 对齐 Java: URIHandler.resolve(URI)
    virtual URI resolve(const URI& uri, const URI& base) const;

    // 反解析绝对 URI 为相对 URI（相对于 base）
    // 对齐 Java: URIHandler.deresolve(URI)
    virtual URI deresolve(const URI& uri, const URI& base) const;

    // 打开输入流
    // 对齐 Java: URIConverter.createInputStream(URI)
    virtual std::unique_ptr<std::istream> createInputStream(const URI& uri) const;

    // 打开输出流
    // 对齐 Java: URIConverter.createOutputStream(URI)
    virtual std::unique_ptr<std::ostream> createOutputStream(const URI& uri) const;

    // 检查 URI 是否存在
    // 对齐 Java: URIConverter.exists(URI)
    virtual bool exists(const URI& uri) const;

    // 获取 URI 的时间戳
    // 对齐 Java: URIConverter.getAttributes().get("timeStamp")
    virtual long long getTimeStamp(const URI& uri) const;

protected:
    URIMap uriMap_;
};

// URIHandler: 处理 URI 的解析/反解析
// 对齐 Java: org.eclipse.emf.ecore.xmi.XMLResource.URIHandler
class URIHandler {
public:
    URIHandler() = default;
    virtual ~URIHandler() = default;

    // 设置/获取基础 URI
    void setBaseURI(const URI& uri) { baseURI_ = uri; }
    const URI& getBaseURI() const { return baseURI_; }

    // 解析 URI：相对 URI 基于 baseURI 解析为绝对 URI
    // 对齐 Java: URIHandler.resolve(URI)
    virtual URI resolve(const URI& uri) const {
        if (!baseURI_.isEmpty() && !baseURI_.isRelative() && 
            uri.isRelative() && uri.hasRelativePath()) {
            return uri.resolve(baseURI_);
        }
        return uri;
    }

    // 反解析 URI：绝对 URI 转为相对 URI
    // 对齐 Java: URIHandler.deresolve(URI)
    virtual URI deresolve(const URI& uri) const {
        URI base = baseURI_;
        if (!base.isEmpty() && !base.isRelative() && !uri.isRelative()) {
            URI deresolved = uri.deresolve(base);
            if (deresolved.hasRelativePath()) {
                return deresolved;
            }
        }
        return uri;
    }

private:
    URI baseURI_;
};

}  // namespace emf::common
