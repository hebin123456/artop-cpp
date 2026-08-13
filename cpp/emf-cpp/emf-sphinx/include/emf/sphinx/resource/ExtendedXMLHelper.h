// ExtendedXMLHelper.h
// 对齐 Java org.eclipse.sphinx.emf.resource.ExtendedXMLHelperImpl
// Sphinx 扩展的 XML helper：提供上下文相关的 proxy URI / HREF 解析工具。
//
// 注意：C++ emf-xmi 的 XMIHelper 是自由函数集合（见 emf/xmi/XMIHelper.h），
// 而非可继承的类，因此本类不继承 XMIHelper，而是提供与之互补的、
// Sphinx 专属的 URI/proxy 解析工具，并持有 ExtendedResource 引用以委托
// HREF 创建与 proxy context 修剪。
#pragma once

#include "emf/common/URI.h"
#include <string>

namespace emf::common {
class EObject;
class Resource;
}  // namespace emf::common

namespace emf::sphinx::resource {

class ExtendedResource;

// ExtendedXMLHelper: 对齐 Java ExtendedXMLHelperImpl。
// 关键行为：getHREF() 将 HREF 创建委托给 ExtendedResource，并对 proxy
// 调用 trimProxyContextInfo() 去除上下文信息（对齐 Java 的同名 override）。
class ExtendedXMLHelper {
public:
    ExtendedXMLHelper() = default;
    explicit ExtendedXMLHelper(ExtendedResource* extendedResource);
    virtual ~ExtendedXMLHelper() = default;

    void setExtendedResource(ExtendedResource* r) { extendedResource_ = r; }
    ExtendedResource* getExtendedResource() const { return extendedResource_; }

    // 关联的 resource（用于 deresolve 基 URI 与 id 查询）。
    void setResource(emf::common::Resource* r) { resource_ = r; }
    emf::common::Resource* getResource() const { return resource_; }

    // 返回 obj 的 HREF 字符串。委托给 ExtendedResource（若存在），
    // 对 proxy 修剪上下文信息后 deresolve。
    // 对齐 Java: ExtendedXMLHelperImpl.getHREF(EObject)。
    virtual std::string getHREF(emf::common::EObject* obj);

    // 相对当前 resource URI 反解析绝对 URI。
    // 对齐 Java: XMLHelperImpl.deresolve(URI)。
    virtual emf::common::URI deresolve(const emf::common::URI& uri) const;

    // 处理悬空 HREF（对象无 resource 且无 id）。默认返回空 URI。
    // 对齐 Java: XMLHelperImpl.handleDanglingHREF(EObject)。
    virtual emf::common::URI handleDanglingHREF(emf::common::EObject* obj);

protected:
    ExtendedResource* extendedResource_ = nullptr;
    emf::common::Resource* resource_ = nullptr;
};

}  // namespace emf::sphinx::resource
