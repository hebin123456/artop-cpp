// emf::artop::runtime —— AutosarResourceSet
// 对齐 Java: org.artop.aal.common.resource.impl.AutosarResourceSetImpl
//            + org.eclipse.emf.ecore.resource.impl.ResourceSetImpl
//
// 职责（对齐 Java ResourceSet 行为）：
//   1. 创建并持有多个 AutosarResource（按 URI 分配）
//   2. 按 URI 查找已加载的资源（loadOnDemand 时按需加载）
//   3. load 后自动 indexLibrary()（对齐 Java ARTOP Library 预加载机制）
//   4. 跨文档引用通过 AutosarLibraryIndex 自动解析（demand-load 效果）
//   5. 跨资源获取 EObject（URI#fragment，对齐 Java ResourceSet.getEObject）
//
// 与 XMIResourceSet 的差异：
//   - createResource 返回 AutosarXMLResource（而非标准 XMIResource）
//   - getResource(uri, true) load 后自动 indexLibrary()
//   - 支持注入 ResourceCreator（用于 codegen 生成的专用 Resource，如 Autosar40ResourceImpl）
//
// 用法：
//   AutosarResourceSet rs;
//   rs.setResourceCreator([](const URI& uri) {
//       return std::make_unique<Autosar40ResourceImpl>(uri);
//   });
//   rs.loadLibrary(URI::createFileURI("lib.arxml"));  // 预加载 library + indexLibrary
//   auto* main = rs.getResource(URI::createFileURI("main.arxml"), true);  // 自动 load + indexLibrary
//   // main 中跨文档引用通过 AutosarLibraryIndex 自动解析
#pragma once

#include "emf/xmi/XMIResourceSet.h"
#include "emf/artop/runtime/AutosarResource.h"
#include "emf/artop/runtime/AutosarResourceFactory.h"

#include <functional>
#include <memory>

namespace emf::artop::runtime {

class AutosarResourceSet : public emf::xmi::XMIResourceSet {
public:
    using ResourceCreator =
        std::function<std::unique_ptr<emf::common::Resource>(const emf::common::URI&)>;

    AutosarResourceSet() = default;
    ~AutosarResourceSet() = default;

    // 注入 Resource creator（用于 codegen 生成的专用 Resource，如 Autosar40ResourceImpl）
    // 对齐 Java: AutosarResourceFactory.setResourceCreator / codegen 重写 createResource
    void setResourceCreator(ResourceCreator c) { creator_ = std::move(c); }

    // 创建一个 AutosarResource 并加入资源集
    // 对齐 Java: ResourceSet.createResource(URI) + AutosarResourceFactory.createResource
    emf::common::Resource* createResource(const emf::common::URI& uri) override {
        std::unique_ptr<emf::common::Resource> res;
        if (creator_) {
            res = creator_(uri);
        } else {
            // 默认：用 AutosarXMLResourceFactory 创建
            AutosarXMLResourceFactory factory(nullptr);
            res = factory.createResource(uri);
        }
        if (!res) return nullptr;
        res->setResourceSet(this);
        auto* raw = res.get();
        auto& idx = uriToIndexMutable();
        idx[uri.toString()] = resourcesMutable().size();
        resourcesMutable().push_back(std::move(res));
        return raw;
    }

    // 获取或按需加载资源（load 后自动 indexLibrary）
    // 对齐 Java: ResourceSet.getResource(URI, boolean loadOnDemand)
    emf::common::Resource* getResource(const emf::common::URI& uri, bool loadOnDemand) override {
        // 先查已加载
        auto& idx = uriToIndexMutable();
        auto it = idx.find(uri.toString());
        if (it != idx.end()) {
            return resourcesMutable()[it->second].get();
        }
        if (!loadOnDemand) return nullptr;
        auto* res = createResource(uri);
        if (!res) return nullptr;
        res->load();
        // 对齐 Java ARTOP：load 后 indexLibrary，使跨文档引用可解析
        indexLibrary(res);
        return res;
    }

    // 便捷：预加载 library resource（load + indexLibrary）
    // 对齐 Java ARTOP: AutosarResourceSet.loadLibrary(URI)
    emf::common::Resource* loadLibrary(const emf::common::URI& uri) {
        return getResource(uri, true);
    }

    // 跨资源获取 EObject（URI#fragment）
    // 对齐 Java: ResourceSet.getEObject(URI, boolean)
    // 注：autosar 跨文档引用是 shortName path（非 URI#fragment），
    // 由 AutosarLibraryIndex 解析；此方法用于标准 URI#fragment 跨资源查找。
    emf::common::EObject* getEObject(const emf::common::URI& uri, bool loadOnDemand) override {
        return emf::xmi::XMIResourceSet::getEObject(uri, loadOnDemand);
    }

private:
    ResourceCreator creator_;

    // load 后 indexLibrary（对齐 Java ARTOP Library 注册）
    void indexLibrary(emf::common::Resource* res) {
        if (!res) return;
        if (auto* ar = dynamic_cast<AutosarResource*>(res)) {
            ar->indexLibrary();
        }
    }
};

}  // namespace emf::artop::runtime
