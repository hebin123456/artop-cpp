// AutosarLibraryIndex 实现
// 对齐 Java ARTOP Library 机制：跨文档 shortName path 索引
#include "emf/artop/runtime/AutosarLibraryIndex.h"
#include "emf/ecore/codegen/EAnnotationReader.h"
#include "emf/ecore/EcoreImpls.h"
#include <algorithm>

namespace emf::artop::runtime {

AutosarLibraryIndex& AutosarLibraryIndex::instance() {
    static AutosarLibraryIndex inst;
    return inst;
}

// 读取对象的 shortName 值（与 Loader 的 getShortNameValue 同逻辑）
static std::string getShortNameValue(emf::common::EObject* obj) {
    if (!obj) return {};
    auto* cls = obj->eClass();
    if (!cls) return {};
    // 1. 直接查常见 feature 名
    if (auto* sf = cls->getEStructuralFeature("shortName")) {
        std::any v = obj->eGet(sf);
        if (v.type() == typeid(std::string)) return std::any_cast<std::string>(v);
    }
    // 2. 查 "SHORT-NAME"
    if (auto* sf = cls->getEStructuralFeature("SHORT-NAME")) {
        std::any v = obj->eGet(sf);
        if (v.type() == typeid(std::string)) return std::any_cast<std::string>(v);
    }
    // 3. 按 xml.name 注解查 EAttribute
    for (auto* a : cls->getEAllAttributes()) {
        if (!a) continue;
        auto meta = emf::ecore::codegen::EAnnotationReader::readFeatureMeta(a);
        if (meta.xmlName == "SHORT-NAME") {
            std::any v = obj->eGet(a);
            if (v.type() == typeid(std::string)) return std::any_cast<std::string>(v);
        }
    }
    return {};
}

// 沿 eContainer 链收集 shortName，构建绝对路径 "/sn1/sn2/.../snN"
// 对齐 Java AutosarURIFactory.getAbsoluteQualifiedName
static std::string buildShortNamePath(emf::common::EObject* obj) {
    std::vector<std::string> parts;
    emf::common::EObject* cur = obj;
    while (cur) {
        std::string sn = getShortNameValue(cur);
        if (!sn.empty()) {
            parts.push_back(sn);
        } else {
            // 无 shortName（如根 AUTOSAR 元素）—— 添加 "AUTOSAR" 段
            // 对齐 Java addURIFragmentSegment：GAUTOSAR 根元素添加 "/"
            auto* cls = cur->eClass();
            if (cls && (cls->getName() == "AUTOSAR" || cls->getName() == "GAUTOSAR")) {
                parts.push_back("AUTOSAR");
            }
            break;
        }
        cur = cur->eContainer();
    }
    if (parts.empty()) return {};
    std::reverse(parts.begin(), parts.end());
    std::string path;
    for (auto& p : parts) {
        path += "/";
        path += p;
    }
    return path;
}

void AutosarLibraryIndex::indexResource(emf::common::Resource* res) {
    if (!res) return;
    for (auto* root : res->getContents()) {
        indexObject(root);
    }
}

void AutosarLibraryIndex::indexObject(emf::common::EObject* obj) {
    if (!obj) return;
    std::string sn = getShortNameValue(obj);
    if (!sn.empty()) {
        std::string path = buildShortNamePath(obj);
        if (!path.empty()) {
            pathIndex_[path] = obj;
        }
    }
    auto contents = obj->eContents();
    for (size_t i = 0; i < contents.size(); ++i) {
        indexObject(contents[i]);
    }
}

emf::common::EObject* AutosarLibraryIndex::lookup(const std::string& path) const {
    auto it = pathIndex_.find(path);
    if (it == pathIndex_.end()) return nullptr;
    return it->second;
}

bool AutosarLibraryIndex::contains(const std::string& path) const {
    return pathIndex_.find(path) != pathIndex_.end();
}

void AutosarLibraryIndex::clear() {
    pathIndex_.clear();
}

}  // namespace emf::artop::runtime
