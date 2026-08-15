// emf::artop::runtime —— AutosarResource / AutosarXMLResource 实现
#include "emf/artop/runtime/AutosarResource.h"
#include "emf/artop/runtime/AutosarXMLLoader.h"
#include "emf/artop/runtime/AutosarXMLSaver.h"
#include "emf/artop/runtime/AutosarLibraryIndex.h"
#include "emf/common/EObject.h"

#include <cstdio>
#include <cstdlib>
#if defined(__GLIBC__)
#  include <malloc.h>   // glibc: malloc_trim
#endif
#include <unordered_set>
#include <vector>

namespace emf::artop::runtime {

// Defined in AutosarXMLLoader.cpp — 清理本 resource 的 EObject 在全局 store 中的条目。
// 析构时调用，避免跨轮累积（EObject 释放后 EObject* key 变悬挂指针，store 节点永不释放）。
void clearAutosarStoresForObjects(const std::unordered_set<emf::common::EObject*>& objs);

// ===== AutosarResource =====

AutosarResource::AutosarResource(emf::common::URI uri)
    : emf::xmi::XMIResource(std::move(uri)) {}

AutosarResource::AutosarResource(emf::common::URI uri,
                                 std::shared_ptr<AutosarReleaseDescriptor> release)
    : emf::xmi::XMIResource(std::move(uri)),
      autosarRelease_(std::move(release)) {}

// AutosarResource 默认走 XMI 的 saver/loader（createXMLSave/createXMLLoad 已在 XMIResource 默认实现）

void AutosarResource::indexLibrary() {
    AutosarLibraryIndex::instance().indexResource(this);
}

std::unique_ptr<emf::xmi::XMLHelper> AutosarResource::createXMLHelper() {
    return std::make_unique<emf::xmi::XMLHelperImpl>(static_cast<emf::common::Resource*>(this));
}

// ===== AutosarXMLResource =====

std::shared_ptr<emf::xmi::XMLSave> AutosarXMLResource::createXMLSave() const {
    return std::make_shared<AutosarXMLSaver>();
}

std::shared_ptr<emf::xmi::XMLLoad> AutosarXMLResource::createXMLLoad() const {
    return std::make_shared<AutosarXMLLoader>();
}

std::unique_ptr<emf::xmi::XMLHelper> AutosarXMLResource::createXMLHelper() {
    return std::make_unique<emf::xmi::XMLHelperImpl>(static_cast<emf::common::Resource*>(this));
}

emf::common::EObject* AutosarXMLResource::getEObject(const std::string& fragment) {
    // Java AutosarXMLResourceImpl 重写 getEObject 用于处理 schemaLocationCache
    // C++ 简化为委托给 XMIResource::getEObject
    return emf::xmi::XMIResource::getEObject(fragment);
}

// 析构：递归释放 EObject containment 树 + 清理全局 store。
// Resource 基类 contents_ 是裸指针 vector（=default 析构不 delete），
// 若不显式释放，多轮 load 会导致 EObject 树持续累积泄漏（每轮 ~800MB @96MB 文件）。
// 全局 store（mixedTextStore 等）以 EObject* 为 key，EObject 释放后 key 变悬挂指针，
// map 节点不自动清理，跨轮累积。故析构时统一清理本 resource 的 EObject 相关条目。
AutosarXMLResource::~AutosarXMLResource() {
    // 1. DFS 收集所有 containment EObject 节点（root → 叶子）
    std::vector<emf::common::EObject*> all;
    {
        std::vector<emf::common::EObject*> stack(getContents().begin(), getContents().end());
        while (!stack.empty()) {
            auto* o = stack.back();
            stack.pop_back();
            if (!o) continue;
            all.push_back(o);
            for (auto* c : o->eContents()) {
                if (c) stack.push_back(c);
            }
        }
    }
    // 2. 清理全局 store 中本 resource EObject 的条目（在 delete 前清理，key 仍有效）
    if (!all.empty()) {
        std::unordered_set<emf::common::EObject*> objSet(all.begin(), all.end());
        clearAutosarStoresForObjects(objSet);
    }
    // 3. post-order delete（逆序：叶子先，root 后）。
    // codegen 类析构是 default，不递归 delete containment 子对象，故需显式逆序 delete。
    // 逆序保证 delete 父时其 EList 内的子指针虽悬挂但不被访问（析构不 deref 元素）。
    for (auto it = all.rbegin(); it != all.rend(); ++it) {
        delete *it;
    }
    // 4. 清空 contents_（避免基类 vector 析构访问已 delete 指针）
    getContents().clear();
    // 5. delete proxy 对象：proxy 是 loader createProxyFromNode 创建的非 containment 占位对象，
    // 不在 eContents() 树中，DFS 不会收集。在 containment 树 delete 完后统一 delete，
    // 此时 owner 的 EList 已销毁，proxy 指针不再被引用，delete 安全。
    {
        extern std::unordered_map<emf::common::Resource*, std::vector<emf::common::EObject*>>& proxyStore();
        auto it = proxyStore().find(this);
        if (it != proxyStore().end()) {
            for (auto* p : it->second) {
                delete p;
            }
            proxyStore().erase(it);
        }
    }
    // 6. TEMP 诊断：打印回收的 EObject 数 + 全局 store 残留 + 强制 glibc 还内存给 OS
    if (std::getenv("ARXML_DEBUG_MEM")) {
        extern std::unordered_map<emf::common::EObject*, std::string>& mixedTextStore();
        extern std::unordered_map<emf::common::EObject*, std::vector<std::string>>& commentStore();
        extern std::unordered_map<emf::common::EObject*, bool>& refIsDefaultStore();
        extern std::unordered_map<std::string, std::string>& refDestStore();
        extern std::unordered_map<emf::common::Resource*, std::vector<emf::common::EObject*>>& proxyStore();
        std::fprintf(stderr, "[MEM] ~AutosarXMLResource: collected=%zu EObjects, deleted | stores after cleanup: mixedText=%zu comment=%zu refIsDefault=%zu refDest=%zu proxyStore=%zu\n",
            all.size(),
            mixedTextStore().size(), commentStore().size(),
            refIsDefaultStore().size(), refDestStore().size(),
            proxyStore().size());
    }
#if defined(__GLIBC__)
    ::malloc_trim(0);  // glibc：归还空闲 arena 顶部给 OS
#endif
}

}  // namespace emf::artop::runtime
