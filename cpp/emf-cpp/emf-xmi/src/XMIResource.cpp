// XMIResource.cpp
// XMIResource 接口的默认实现
//
// 本文件只做"接口 -> 实现"的分发。真正的序列化逻辑在 XMISaver.cpp / XMILoader.cpp 中。
#include "emf/xmi/XMIResource.h"
#include "emf/xmi/XMIResourceSet.h"
#include "emf/xmi/XMIOptions.h"
#include "emf/ecore/EcorePackage.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/ecore/EcoreMetadata.h"

#include <cstdint>
#include <random>
#include <sstream>

namespace emf::xmi {

// 实际 save/load 在 XMISaver.cpp / XMILoader.cpp 中实现
void saveInto(std::ostream& os, const XMIResource& res, const XMIOptions& opts);
void loadInto(std::istream& is, XMIResource& res, const XMIOptions& opts);

// ===== XMLSave / XMLLoad lazy 注入（默认 XMLSaveImpl / XMLLoadImpl）=====
std::shared_ptr<XMLSave> XMIResource::createXMLSave() const {
    return std::make_shared<XMLSaveImpl>();
}
std::shared_ptr<XMLLoad> XMIResource::createXMLLoad() const {
    return std::make_shared<XMLLoadImpl>();
}

std::shared_ptr<XMLSave> XMIResource::getXMLSave() const {
    if (!xmlSave_) xmlSave_ = createXMLSave();
    return xmlSave_;
}
std::shared_ptr<XMLLoad> XMIResource::getXMLLoad() const {
    if (!xmlLoad_) xmlLoad_ = createXMLLoad();
    return xmlLoad_;
}

std::string XMIResource::getID(emf::common::EObject* obj) const {
    if (!obj) return "";
    for (const auto& kv : idToEObject_) {
        if (kv.second == obj) return kv.first;
    }
    return "";
}

void XMIResource::setID(emf::common::EObject* obj, const std::string& id) {
    if (!obj) return;
    // 删除旧映射
    auto it = idToEObject_.begin();
    while (it != idToEObject_.end()) {
        if (it->second == obj) it = idToEObject_.erase(it);
        else ++it;
    }
    if (!id.empty()) idToEObject_[id] = obj;
}

emf::common::EObject* XMIResource::getEObjectByID(const std::string& id) const {
    auto it = idToEObject_.find(id);
    return it != idToEObject_.end() ? it->second : nullptr;
}

// ===== UUID 支持（对齐 Java XMIResource.USE_UUIDs option）=====
// RFC 4122 v4 UUID：8-4-4-4-12 hex，第 13 位 version=4，第 17 位 variant=10xx
// 用 std::mt19937_64 + thread_local 避免锁竞争。
std::string XMIResource::generateUUID() {
    static thread_local std::mt19937_64 rng{std::random_device{}()};
    uint64_t a = rng();
    uint64_t b = rng();
    a = (a & 0xFFFFFFFFFFFF0FFFULL) | 0x0000000000004000ULL;  // version 4
    b = (b & 0x3FFFFFFFFFFFFFFFULL) | 0x8000000000000000ULL;  // variant 10xx
    char buf[37];
    std::snprintf(buf, sizeof(buf), "%08x-%04x-%04x-%04x-%012llx",
                  static_cast<unsigned>(a >> 32),
                  static_cast<unsigned>((a >> 16) & 0xFFFF),
                  static_cast<unsigned>(a & 0xFFFF),
                  static_cast<unsigned>((b >> 48) & 0xFFFF),
                  static_cast<unsigned long long>(b & 0xFFFFFFFFFFFFULL));
    return std::string(buf);
}

std::string XMIResource::ensureID(emf::common::EObject* obj) {
    if (!obj) return "";
    // 已有 id 直接返回
    std::string existing = getID(obj);
    if (!existing.empty()) return existing;
    if (!useUUIDs_) return "";  // 不开 UUID 模式时不自动分配
    std::string uuid = generateUUID();
    setID(obj, uuid);
    return uuid;
}

// 通过 URI fragment 获取对象
// 对齐 Java: Resource.getEObject(String)
// 支持:
//   - "?id" -> 按 xmi:id 查找
//   - "/path" -> URI 路径风格 (作为 alias 走 position path)
//   - "//Class" -> 按 position path 查找
//   - "Class" -> 先按 id 查找（如果有），再按 classifier 名字 / 根对象名字
emf::common::EObject* XMIResource::getEObject(const std::string& fragment) {
    if (fragment.empty()) return nullptr;

    std::string frag = fragment;

    // ?xxx 格式：按 xmi:id 查找（对齐 Java XMLResourceImpl.getEObject("?id")）
    if (!frag.empty() && frag[0] == '?') {
        return getEObjectByID(frag.substr(1));
    }

    // //xxx 格式：position path（对齐 Java XMLResourceImpl.getEObject("//@feat.idx")）
    // 必须在去前导 / 之前检查，否则 //@feat.idx 会变成 @feat.idx 丢失 // 前缀
    if (frag.size() >= 2 && frag[0] == '/' && frag[1] == '/') {
        return resolvePositionPath(frag.substr(2));
    }

    // 去掉开头的 /（处理 "/id" 或遗留 "/feat" 形式）
    while (!frag.empty() && frag[0] == '/') {
        frag = frag.substr(1);
    }
    if (std::getenv("EMF_DEBUG_GEOBJECT")) {
        std::fprintf(stderr, "[DBG-XMIResource::getEObject] fragment=%s -> frag=%s\n",
                     fragment.c_str(), frag.c_str());
    }

    // 优先按 xmi:id 查找（这是 Java XMLHelperImpl 默认行为）：
    // href 解析时 XMILoader 传过来的 fragment 是 bare id（无 ? 前缀），
    // 走 contents 名字查找会失败（实例不是 classifier），所以 id 查找应优先。
    {
        emf::common::EObject* byId = getEObjectByID(frag);
        if (byId) return byId;
    }

    // 直接在 contents 中按名字查找
    for (auto* obj : getContents()) {
        // EPackage 下的 classifier
        if (auto* pkg = dynamic_cast<emf::ecore::EPackage*>(obj)) {
            for (auto* c : pkg->getEClassifiers()) {
                if (c->getName() == frag) {
                    return dynamic_cast<emf::common::EObject*>(c);
                }
            }
        }
        // 根对象
        if (auto* cls = obj->eClass()) {
            if (cls->getName() == frag) {
                return obj;
            }
        }
    }

    return nullptr;
}

// 解析 position path，如 "writers.0/books.1"
emf::common::EObject* XMIResource::resolvePositionPath(const std::string& path) {
    size_t pos = 0;
    emf::common::EObject* cur = getContents().empty() ? nullptr : getContents().front();
    if (std::getenv("EMF_DEBUG_GEOBJECT")) {
        std::fprintf(stderr, "[DBG-resolvePositionPath] path=%s contents=%zu cur=%p\n",
                     path.c_str(), getContents().size(), (void*)cur);
    }

    // 直接名字（不以 '@' 开头）：可能是 xmi:id 引用（如 "//w1" 去掉 // 后是 "w1"）
    // 对齐 Java XMLResourceImpl：position path 中的 bare name 先按 id 查找
    if (!path.empty() && path[0] != '@') {
        // 先尝试按 xmi:id 查找（Java XMLHelperImpl 对 //id 形式的处理）
        emf::common::EObject* byId = getEObjectByID(path);
        if (byId) return byId;
        if (auto* pkg = dynamic_cast<emf::ecore::EPackage*>(cur)) {
            if (std::getenv("EMF_DEBUG_GEOBJECT")) {
                std::fprintf(stderr, "[DBG-resolvePositionPath] pkg=%p nsURI=%s numClassifiers=%zu\n",
                             (void*)pkg, pkg->getNsURI().c_str(), pkg->getEClassifiers().size());
            }
            for (auto* c : pkg->getEClassifiers()) {
                if (c->getName() == path) return c;
            }
        }
        if (cur && cur->eClass() && cur->eClass()->getName() == path) return cur;
        return nullptr;
    }

    while (pos < path.size() && cur) {
        if (path[pos] != '@' && path[pos] != '.') {
            // 可能是直接的名字
            break;
        }
        if (path[pos] == '.') pos++;
        if (path[pos] != '@') break;
        pos++;

        size_t slash = path.find('/', pos);
        size_t end = (slash == std::string::npos) ? path.size() : slash;
        std::string seg = path.substr(pos, end - pos);

        // seg 形如 "feat" 或 "feat.index"
        size_t dot = seg.find('.');
        std::string fname = (dot == std::string::npos) ? seg : seg.substr(0, dot);
        int index = -1;
        if (dot != std::string::npos) index = std::stoi(seg.substr(dot + 1));

        if (std::getenv("EMF_DEBUG_GEOBJECT")) {
            std::fprintf(stderr, "[DBG-resolvePositionPath] seg=%s fname=%s index=%d cur=%p\n",
                         seg.c_str(), fname.c_str(), index, (void*)cur);
        }
        auto* cls = cur->eClass();
        if (!cls) return nullptr;

        emf::ecore::EStructuralFeature* sf = nullptr;
        for (auto* f : ::emf::ecore::collectAllStructuralFeatures(cls)) {
            if (f->getName() == fname) { sf = f; break; }
        }
        if (!sf) {
            if (std::getenv("EMF_DEBUG_GEOBJECT")) {
                std::fprintf(stderr, "[DBG-resolvePositionPath] feature %s not found on %s\n",
                             fname.c_str(), cls->getName().c_str());
            }
            return nullptr;
        }

        std::any v = cur->eGet(sf);
        std::vector<emf::common::EObject*> lst = anyToEObjectList(v);
        if (std::getenv("EMF_DEBUG_GEOBJECT")) {
            std::fprintf(stderr, "[DBG-resolvePositionPath] lst.size=%zu\n", lst.size());
        }
        if (lst.empty()) return nullptr;

        if (index >= 0 && (size_t)index < lst.size()) cur = lst[index];
        else cur = lst[0];

        pos = (slash == std::string::npos) ? path.size() : slash + 1;
    }

    return cur;
}

std::vector<emf::common::EObject*> XMIResource::anyToEObjectList(const std::any& v) {
    std::vector<emf::common::EObject*> r;
    if (v.type() == typeid(emf::common::EObject*)) {
        if (auto* p = std::any_cast<emf::common::EObject*>(v)) r.push_back(p);
    } else if (v.type() == typeid(emf::common::EObjectRefView)) {
        // EObjectRefView 零拷贝视图（codegen 多值 reference eGet fast-path）
        auto view = std::any_cast<emf::common::EObjectRefView>(v);
        r.reserve(view.size());
        for (auto* p : view) r.push_back(p);
    } else if (v.type() == typeid(std::vector<emf::common::EObject*>)) {
        for (auto* p : std::any_cast<std::vector<emf::common::EObject*>>(v)) r.push_back(p);
    } else if (v.type() == typeid(emf::common::EList<emf::common::EObject*>*)) {
        auto* p = std::any_cast<emf::common::EList<emf::common::EObject*>*>(v);
        if (p) for (size_t i = 0; i < p->size(); ++i) r.push_back((*p)[i]);
    }
    return r;
}

void XMIResource::setResourceSet(emf::common::ResourceSet* rs) {
    resourceSet_ = rs;
}

void XMIResource::save(std::ostream& os) {
    // 走注入的 XMLSave（默认 XMLSaveImpl）。对齐 Java: XMIResource.save(Writer)
    getXMLSave()->save(this, os, XMIOptions{});
}

void XMIResource::load(std::istream& is) {
    getXMLLoad()->load(this, is, XMIOptions{});
}

void XMIResource::save(std::ostream& os, const XMIOptions& opts) {
    getXMLSave()->save(this, os, opts);
}

void XMIResource::load(std::istream& is, const XMIOptions& opts) {
    getXMLLoad()->load(this, is, opts);
}

void XMIResource::loadFromString(const std::string& xml, const XMIOptions& opts) {
    // 通过 getXMLLoad() 分发，让子类（如 AutosarXMLResource）能注入自定义 Loader。
    // 直接调用 loadInto() 会绕过 createXMLLoad()，导致 AutosarXMLLoader 等不被使用。
    std::istringstream iss(xml);
    getXMLLoad()->load(this, iss, opts);
}

std::string XMIResource::saveToString(const XMIOptions& opts) const {
    // 通过 getXMLSave() 分发，让子类（如 AutosarXMLResource）能注入自定义 Saver。
    std::ostringstream oss;
    getXMLSave()->save(this, oss, opts);
    return oss.str();
}

}  // namespace emf::xmi
