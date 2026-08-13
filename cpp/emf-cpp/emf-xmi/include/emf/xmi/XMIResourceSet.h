// emf::xmi —— XMIResourceSet
// 对齐 Java: org.eclipse.emf.ecore.resource.impl.ResourceSetImpl
//
// 职责：
//   1. 创建并持有多个 XMIResource（按 URI 分配）
//   2. 按 URI 查找已加载的资源（loadOnDemand 时按需加载）
//   3. 跨资源解析 EObject（URI#fragment）
//
// 设计：header-only（无 .cpp）。所有资源以 unique_ptr 持有，
// ResourceSet 析构时自动释放。createResource 返回裸指针（对齐 Java
// ResourceSet.createResource 返回 Resource*，所有权归 ResourceSet）。
#pragma once

#include "emf/common/Resource.h"
#include "emf/common/URI.h"
#include "XMIResource.h"
#include "XMIResourceFactory.h"

#include <memory>
#include <unordered_map>
#include <vector>
#include <fstream>
#include <sstream>

namespace emf::xmi {

class XMIResourceSet : public emf::common::ResourceSet {
public:
    XMIResourceSet() = default;
    ~XMIResourceSet() = default;

    // 创建一个 XMIResource 并加入资源集
    // 对齐 Java: ResourceSet.createResource(URI)
    emf::common::Resource* createResource(const emf::common::URI& uri) override {
        auto res = emf::xmi::XMIResourceFactory::createResourceFor(uri);
        res->setResourceSet(this);
        auto* raw = res.get();
        uriToIndex_[uri.toString()] = resources_.size();
        resources_.push_back(std::move(res));
        return raw;
    }

    // 获取或按需加载资源
    // 对齐 Java: ResourceSet.getResource(URI, boolean loadOnDemand)
    emf::common::Resource* getResource(const emf::common::URI& uri, bool loadOnDemand) override {
        auto it = uriToIndex_.find(uri.toString());
        if (it != uriToIndex_.end()) {
            return resources_[it->second].get();
        }
        if (!loadOnDemand) return nullptr;
        auto* res = createResource(uri);
        if (res) res->load();
        return res;
    }

    // 跨资源获取 EObject
    // 对齐 Java: ResourceSet.getEObject(URI, boolean)
    // URI 形如 "file:///path/to/file.xmi#//ElementId"
    emf::common::EObject* getEObject(const emf::common::URI& uri, bool loadOnDemand) override {
        // 分离 resource URI 和 fragment
        std::string full = uri.toString();
        auto hashPos = full.find('#');
        emf::common::URI resUri = (hashPos == std::string::npos)
            ? uri
            : emf::common::URI(full.substr(0, hashPos));
        std::string fragment = (hashPos == std::string::npos)
            ? std::string{}
            : full.substr(hashPos + 1);

        auto* res = getResource(resUri, loadOnDemand);
        if (!res) return nullptr;
        if (fragment.empty()) {
            auto& c = res->getContents();
            return c.empty() ? nullptr : c.front();
        }
        return res->getEObject(fragment);
    }

    const std::vector<std::unique_ptr<emf::common::Resource>>& getResources() const override {
        return resources_;
    }

private:
    std::vector<std::unique_ptr<emf::common::Resource>> resources_;
    std::unordered_map<std::string, size_t> uriToIndex_;
protected:
    // 暴露给子类（如 AutosarResourceSet）以便重写 createResource/getResource
    std::vector<std::unique_ptr<emf::common::Resource>>& resourcesMutable() { return resources_; }
    std::unordered_map<std::string, size_t>& uriToIndexMutable() { return uriToIndex_; }
};

}  // namespace emf::xmi
