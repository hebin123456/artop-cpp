// XMILoader.cpp —— XMI / XML 反序列化实现
// 对齐 Java: org.eclipse.emf.ecore.xmi.impl.XMLLoadImpl + SAXXMIHandler
//
// 把输入流解析成 EObject 树，挂到 XMIResource::contents。
//
// 支持两种文档形态（覆盖 codegen/artop 全部场景）：
//   (1) Ecore 元模型文档：根元素 <ecore:EPackage>，子元素 <eClassifiers xsi:type="ecore:EClass|EDataType|EEnum">
//       —— 用于加载 .ecore 文件（codegen 输入）。
//   (2) XMI 实例文档：根元素 <xmi:XMI> 或带 nsURI 的实例根 <prefix:Type>，
//       —— 用于加载业务模型实例（如 library.xmi）。
//
// 设计：pugixml 解析 → build*/apply* 直接消费 pugi::xml_node → EObject（两阶段构建）。
// 跳过中间 XmlNode 层，pugixml 的 name()/value() 直接指向文档缓冲区，无需字符串拷贝。
#include "emf/xmi/XMIResource.h"
#include "emf/xmi/XMIOptions.h"
#include "emf/xmi/XMIHelper.h"
#include "emf/ecore/EcorePackage.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/ecore/util/BasicExtendedMetaData.h"
#include "emf/common/EPackageRegistry.h"

#include <algorithm>
#include <cctype>
#include <cstdint>
#include <cstring>
#include <iostream>
#include <sstream>
#include <stdexcept>
#include <unordered_map>
#include <vector>

#include "pugixml.hpp"

namespace emf::xmi {

namespace {

// 多值 EReference 追加辅助：eGet 返回内部 EList 指针（DynamicEObject）或
// 堆副本（codegen 生成类）。统一用 copy+eSet 模式保证对两种实现都正确：
// 提取当前列表 → 追加新值 → eSet 回写。
// 注：eGet 返回的指针不应 delete（DynamicEObject 内部管理；codegen 副本由 eSet 回写后丢弃）。
void appendToMultiValue(emf::common::EObject* obj, emf::ecore::EStructuralFeature* sf,
                        emf::common::EObject* value) {
    if (!obj || !sf || !value) return;
    std::vector<emf::common::EObject*> v;
    auto any = obj->eGet(sf);
    if (any.has_value()) {
        if (any.type() == typeid(emf::common::EList<emf::common::EObject*>*)) {
            auto* elist = std::any_cast<emf::common::EList<emf::common::EObject*>*>(any);
            if (elist) {
                for (size_t i = 0; i < elist->size(); ++i) v.push_back((*elist)[i]);
            }
        } else if (any.type() == typeid(emf::common::EObjectRefView)) {
            // EObjectRefView 零拷贝视图（codegen 多值 reference eGet fast-path）
            auto view = std::any_cast<emf::common::EObjectRefView>(any);
            for (auto* p : view) v.push_back(p);
        } else if (any.type() == typeid(std::vector<emf::common::EObject*>)) {
            v = std::any_cast<std::vector<emf::common::EObject*>>(any);
        } else if (any.type() == typeid(std::vector<emf::common::EObject*>*)) {
            auto* p = std::any_cast<std::vector<emf::common::EObject*>*>(any);
            if (p) v = *p;
        }
    }
    v.push_back(value);
    obj->eSet(sf, std::any(std::move(v)));
}

// 把 Unicode code point 编码为 UTF-8 字节串（对齐 Java SAX 解析器对字符引用的解码结果）
std::string encodeUtf8(uint32_t cp) {
    std::string out;
    if (cp <= 0x7F) {
        out += (char)cp;
    } else if (cp <= 0x7FF) {
        out += (char)(0xC0 | (cp >> 6));
        out += (char)(0x80 | (cp & 0x3F));
    } else if (cp <= 0xFFFF) {
        out += (char)(0xE0 | (cp >> 12));
        out += (char)(0x80 | ((cp >> 6) & 0x3F));
        out += (char)(0x80 | (cp & 0x3F));
    } else {
        out += (char)(0xF0 | (cp >> 18));
        out += (char)(0x80 | ((cp >> 12) & 0x3F));
        out += (char)(0x80 | ((cp >> 6) & 0x3F));
        out += (char)(0x80 | (cp & 0x3F));
    }
    return out;
}

// ===== pugi::xml_node 访问辅助 =====
// pugixml 的 name()/value() 返回 const char*（指向文档缓冲区，无需拷贝）。
// 下面这些辅助函数避免在调用点重复解析 prefix:local。

// 从 pugi::xml_node 提取 prefix 和 local（"ecore:EPackage" -> prefix="ecore", local="EPackage"）
void splitNodeName(const pugi::xml_node& node, std::string& prefix, std::string& local) {
    const char* name = node.name();
    const char* colon = std::strchr(name, ':');
    if (colon) {
        prefix.assign(name, static_cast<size_t>(colon - name));
        local.assign(colon + 1);
    } else {
        prefix.clear();
        local.assign(name);
    }
}

// 仅取 local 部分（"ecore:EPackage" -> "EPackage"）
std::string getNodeLocal(const pugi::xml_node& node) {
    const char* name = node.name();
    const char* colon = std::strchr(name, ':');
    return colon ? std::string(colon + 1) : std::string(name);
}

// 仅取 prefix 部分（"ecore:EPackage" -> "ecore"）
std::string getNodePrefix(const pugi::xml_node& node) {
    const char* name = node.name();
    const char* colon = std::strchr(name, ':');
    return colon ? std::string(name, static_cast<size_t>(colon - name)) : std::string();
}

// 判断 local 部分是否等于 expected（无字符串分配）
bool localNameIs(const pugi::xml_node& node, const char* expected) {
    const char* name = node.name();
    const char* colon = std::strchr(name, ':');
    const char* local = colon ? colon + 1 : name;
    return std::strcmp(local, expected) == 0;
}

// 获取元素的累积文本（pcdata + cdata，对齐原 XmlNode::text 累积语义）
std::string getNodeText(const pugi::xml_node& node) {
    std::string text;
    for (pugi::xml_node child : node.children()) {
        const auto t = child.type();
        if (t == pugi::node_pcdata || t == pugi::node_cdata) {
            text += child.value();
        }
    }
    return text;
}

// 把 pugi::xml_node 序列化回 XML 字符串（含子元素、属性、文本），用于记录未知元素。
// 对齐 Java EMF OPTION_RECORD_UNKNOWN_FEATURE：保留原始 XML 片段供 saver 原样输出。
std::string nodeToXmlString(const pugi::xml_node& node) {
    std::string s;
    // 原始标签名（含前缀）
    s += "<";
    s += node.name();
    // 属性
    for (pugi::xml_attribute a : node.attributes()) {
        s += " ";
        s += a.name();
        s += "=\"";
        // 简单转义引号
        const char* v = a.value();
        for (const char* p = v; *p; ++p) {
            if (*p == '"') s += "&quot;";
            else if (*p == '&') s += "&amp;";
            else if (*p == '<') s += "&lt;";
            else s += *p;
        }
        s += "\"";
    }
    // 子节点 + 文本
    bool hasChildren = false;
    for (pugi::xml_node child : node.children()) {
        if (!hasChildren) { s += ">"; hasChildren = true; }
        auto t = child.type();
        if (t == pugi::node_pcdata || t == pugi::node_cdata) {
            const char* v = child.value();
            for (const char* p = v; *p; ++p) {
                if (*p == '<') s += "&lt;";
                else if (*p == '&') s += "&amp;";
                else s += *p;
            }
        } else if (t == pugi::node_element) {
            s += nodeToXmlString(child);
        }
    }
    if (!hasChildren) {
        s += "/>";   // 空元素自闭合
    } else {
        s += "</";
        s += node.name();
        s += ">";
    }
    return s;
}

// ===== XML 解析器（基于 pugixml，MIT 许可证） =====
// 用 pugixml 的高速 in-situ DOM 解析器直接产出 pugi::xml_node，下游 build*/apply*
// 方法直接消费 pugi::xml_node，省掉一层 XmlNode 字符串拷贝。
// 对齐 Java SAX 解析器的行为：实体展开、注释丢弃、文本累积。
// 注意：XmlParser 必须在解析返回的 pugi::xml_node 使用期间保持存活，
// 因为 pugi::xml_node 内部指向 doc_ 的缓冲区。
class XmlParser {
public:
    explicit XmlParser(const std::string& s) : in_(s) {
        extractEncoding();
    }

    // 解析并返回根元素。返回的 pugi::xml_node 及其所有后代节点引用 doc_ 的内部缓冲区，
    // 因此调用方使用返回节点期间必须保持本对象存活。
    pugi::xml_node parse() {
        // parse_default 含实体展开/行尾归一化/CDATA 处理；
        // 加 parse_ws_pcdata 保留所有文本节点（与原手写解析器行为一致）
        unsigned flags = pugi::parse_default | pugi::parse_ws_pcdata;
        pugi::xml_parse_result result = doc_.load_buffer(in_.data(), in_.size(), flags, pugi::encoding_auto);
        if (!result) {
            throw std::runtime_error(std::string("XmlParser: pugixml parse error: ") + result.description());
        }
        // 找到根元素节点（跳过声明、注释、PI）
        for (pugi::xml_node child : doc_.children()) {
            if (child.type() == pugi::node_element) return child;
        }
        throw std::runtime_error("XmlParser: no root element");
    }

    const std::string& getEncoding() const { return encoding_; }

private:
    const std::string& in_;
    pugi::xml_document doc_;  // 持有解析后的 DOM 树，必须存活到所有 xml_node 使用完毕
    std::string encoding_;

    // 从 XML 声明中提取 encoding（如 "UTF-8"）；无声明则空
    void extractEncoding() {
        size_t pos = 0;
        while (pos < in_.size() && std::isspace((unsigned char)in_[pos])) ++pos;
        if (pos + 5 <= in_.size() && in_[pos] == '<' && in_[pos+1] == '?') {
            size_t declEnd = pos;
            while (declEnd < in_.size() &&
                   !(in_[declEnd] == '?' && declEnd+1 < in_.size() && in_[declEnd+1] == '>')) ++declEnd;
            if (declEnd+1 < in_.size()) {
                std::string decl = in_.substr(pos, declEnd - pos);
                size_t encPos = decl.find("encoding");
                if (encPos != std::string::npos) {
                    size_t eq = decl.find('=', encPos);
                    if (eq != std::string::npos) {
                        size_t q1 = decl.find('"', eq);
                        if (q1 != std::string::npos) {
                            size_t q2 = decl.find('"', q1 + 1);
                            if (q2 != std::string::npos) {
                                encoding_ = decl.substr(q1 + 1, q2 - q1 - 1);
                            }
                        }
                    }
                }
            }
        }
    }
};

// 判断 href 是否为自引用（对齐 Java XMLHelperImpl：href path 等于当前 resource URI 时视为同文档）。
// 处理两种情况：
//   1. href path == 当前 resource 的完整文件路径
//   2. href path 的文件名 == 当前 resource 的文件名（容忍路径迁移）
bool isSelfReference(const std::string& hrefPath, const emf::common::Resource& res) {
    if (hrefPath.empty()) return false;
    emf::common::URI baseUri = res.getURI();
    std::string basePath = baseUri.toFilePath();
    if (basePath.empty()) basePath = baseUri.path();
    // 1. 完整路径匹配
    if (hrefPath == basePath) return true;
    // 2. 文件名匹配（取最后一段）
    auto lastSeg = [](const std::string& p) -> std::string {
        auto pos = p.find_last_of("/\\");
        return pos == std::string::npos ? p : p.substr(pos + 1);
    };
    std::string hfn = lastSeg(hrefPath);
    std::string bfn = lastSeg(basePath);
    if (!hfn.empty() && hfn == bfn) return true;
    return false;
}

// ===== 工具：解析 eType href =====
// 形式：
//   "#//Book"                              -> 同包 EClass "Book"
//   "ecore:EDataType http://...#//EString" -> EcorePackage 内建类型 "EString"
//   "ecore:EClass http://...#//EClass"     -> EcorePackage 元 EClass "EClass"
//   "library.ecore#//Library"              -> 外部文件（暂不解析，返回 nullptr）
struct TypeInfo { std::string kind; std::string nsURI; std::string name; bool isExternal; };
TypeInfo parseTypeAttr(const std::string& s) {
    TypeInfo t;
    std::string rest = s;
    auto sp = rest.find(' ');
    if (sp != std::string::npos) {
        t.kind = rest.substr(0, sp);
        rest = rest.substr(sp + 1);
    }
    auto hash = rest.find('#');
    if (hash == std::string::npos) {
        t.isExternal = !rest.empty();
        return t;
    }
    std::string path = rest.substr(0, hash);
    std::string frag = rest.substr(hash + 1);
    // frag 形如 "//EString" 或 "//Book/author"
    while (!frag.empty() && frag[0] == '/') frag = frag.substr(1);
    auto slash = frag.find('/');
    t.name = (slash == std::string::npos) ? frag : frag.substr(0, slash);
    t.nsURI = path;
    t.isExternal = !path.empty();
    return t;
}

bool parseBool(const std::string& s, bool def = false) {
    if (s.empty()) return def;
    return s == "true" || s == "1" || s == "TRUE";
}

// 在 EcorePackage 中按名字找内建 EDataType / 元 EClass
emf::ecore::EClassifier* findEcoreBuiltin(const std::string& name) {
    auto* ep = emf::ecore::EcorePackage::instance().getEPackage();
    if (!ep) return nullptr;
    for (auto* c : ep->getEClassifiers()) {
        if (c && c->getName() == name) return c;
    }
    return nullptr;
}

// 在 EPackage（含子包）中按名字找 EClassifier
emf::ecore::EClassifier* findClassifierRecursive(emf::ecore::EPackage* pkg, const std::string& name) {
    if (!pkg) return nullptr;
    for (auto* c : pkg->getEClassifiers()) {
        if (c && c->getName() == name) return c;
    }
    for (auto* sp : pkg->getESubPackages()) {
        if (auto* found = findClassifierRecursive(sp, name)) return found;
    }
    return nullptr;
}

// 提取 href 中 '#' 之后的 fragment 路径，去掉前导 '/'。
// 如 "#//ggenericstructure/ginfrastructure/GIdentifiable" → "ggenericstructure/ginfrastructure/GIdentifiable"
// 如 "#//EModelElement/eAnnotations" → "EModelElement/eAnnotations"
std::string extractFragment(const std::string& href) {
    auto hash = href.find('#');
    if (hash == std::string::npos) return "";
    std::string frag = href.substr(hash + 1);
    while (!frag.empty() && frag[0] == '/') frag = frag.substr(1);
    return frag;
}

// 取 fragment 路径的最后一段（类名或 feature 名）。
// 如 "ggenericstructure/ginfrastructure/GIdentifiable" → "GIdentifiable"
std::string lastFragmentSegment(const std::string& href) {
    std::string frag = extractFragment(href);
    auto slash = frag.rfind('/');
    return (slash == std::string::npos) ? frag : frag.substr(slash + 1);
}

// 按 containment 路径从根 EPackage 逐段导航到最终 EModelElement。
// 对齐 Java EMF XMLHelperImpl.getEObject: fragment 是 containment 路径，
// 每段按名字在当前 EPackage 的 classifiers/subpackages 或 EClass 的 features 中查找。
// 如 "ggenericstructure/ginfrastructure/GIdentifiable" → EClass GIdentifiable
// 如 "EModelElement/eAnnotations" → EStructuralFeature eAnnotations
emf::ecore::EModelElement* navigateContainmentPath(emf::ecore::EPackage* root,
                                                     const std::string& fragPath) {
    if (!root || fragPath.empty()) return nullptr;
    emf::ecore::EModelElement* current = root;
    std::string remaining = fragPath;
    while (!remaining.empty() && current) {
        auto slash = remaining.find('/');
        std::string seg = (slash == std::string::npos) ? remaining : remaining.substr(0, slash);
        if (slash != std::string::npos) remaining = remaining.substr(slash + 1);
        else remaining = "";
        if (seg.empty()) continue;

        if (auto* pkg = dynamic_cast<emf::ecore::EPackage*>(current)) {
            emf::ecore::EModelElement* next = nullptr;
            for (auto* c : pkg->getEClassifiers()) {
                if (c && c->getName() == seg) { next = c; break; }
            }
            if (!next) {
                for (auto* sp : pkg->getESubPackages()) {
                    if (sp && sp->getName() == seg) { next = sp; break; }
                }
            }
            current = next;
        } else if (auto* cls = dynamic_cast<emf::ecore::EClass*>(current)) {
            emf::ecore::EModelElement* next = nullptr;
            for (auto* sf : cls->getEStructuralFeatures()) {
                if (sf && sf->getName() == seg) { next = sf; break; }
            }
            current = next;
        } else {
            return nullptr;
        }
    }
    return current;
}

// 从任意 EPackage 沿 eContainer 链向上找到根 EPackage（对齐 Java: resource 的根 EObject）。
// #// 路径总是从根开始导航，因此跨子包引用需要从根出发。
// 优先用 getESuperPackage()（EPackage 的 containment 链），回退到 eContainer()。
emf::ecore::EPackage* findRootPackage(emf::ecore::EPackage* pkg) {
    if (!pkg) return nullptr;
    emf::ecore::EPackage* root = pkg;
    while (root) {
        auto* sup = root->getESuperPackage();
        if (sup) { root = sup; continue; }
        // 回退：eContainer()（某些实现可能通过 eContainer 维持父子关系）
        auto* cont = root->eContainer();
        auto* p = cont ? dynamic_cast<emf::ecore::EPackage*>(cont) : nullptr;
        if (p) { root = p; continue; }
        break;
    }
    return root;
}

// 同文档 #// 路径解析 EClassifier：
// 先从 ctxPkg 导航，失败则从根包导航，最后按最后段名在根包递归搜。
// 对齐 Java: XMLHelperImpl.getEObject(fragment) 从 resource 根开始 containment 路径导航。
emf::ecore::EClassifier* resolveSameDocClassifier(emf::ecore::EPackage* ctxPkg,
                                                   const std::string& href) {
    if (!ctxPkg) return nullptr;
    std::string frag = extractFragment(href);
    // 1. 从 ctxPkg 导航（同包引用）
    auto* el = navigateContainmentPath(ctxPkg, frag);
    auto* target = dynamic_cast<emf::ecore::EClassifier*>(el);
    if (target) return target;
    // 2. 从根包导航（跨子包 #// 路径，如 #//swcomponent/.../SwcServiceDependency）
    auto* root = findRootPackage(ctxPkg);
    if (root && root != ctxPkg) {
        el = navigateContainmentPath(root, frag);
        target = dynamic_cast<emf::ecore::EClassifier*>(el);
        if (target) return target;
    }
    // 3. 按最后段名在根包递归搜（兼容路径段名与 containment 不一致）
    auto* c = findClassifierRecursive(root ? root : ctxPkg, lastFragmentSegment(href));
    return c;
}

// ===== Loader 上下文 =====
struct Loader {
    XMIResource& res;
    const XMIOptions& opts;
    // 延迟引用：owner + attrName + 原始 href + 上下文包（用于 #// 解析）
    // 对齐 Java XMLHelperImpl.setReferenceValue + pending cross-doc
    struct PendingRef {
        emf::common::EObject* owner;
        std::string attrName;
        std::string href;
        emf::ecore::EPackage* contextPkg;
        // 实例 EReference 的延迟解析使用：feature 是 EReference*，isMany 区分单值/多值
        emf::ecore::EStructuralFeature* feature = nullptr;
        bool isMany = false;
    };
    std::vector<PendingRef> pendingRefs;
    // xmi:id -> EObject（本资源内）
    std::unordered_map<std::string, emf::common::EObject*> idMap;
    // ExtendedMetaData 查询器（对齐 Java XMLLoadImpl.extendedMetaData）
    emf::ecore::util::BasicExtendedMetaData extMetaData_;

    explicit Loader(XMIResource& r, const XMIOptions& o) : res(r), opts(o) {}

    // 查询 feature 是否有 ExtendedMetaData kind 注解（对齐 Java XMLLoadImpl 基于 getFeatureKind 决策）。
    // 返回值：'e'=element, 'a'=attribute, '\0'=无注解（保持默认行为）
    char featureKindAnnotation(emf::ecore::EStructuralFeature* sf) {
        if (!sf) return '\0';
        auto* a = extMetaData_.getAnnotation(sf, false);
        if (!a) return '\0';
        std::string kind = a->getDetail("kind");
        if (kind == "element") return 'e';
        if (kind == "attribute") return 'a';
        return '\0';
    }

    // ===== Ecore 元模型构建 =====
    emf::ecore::EPackage* buildEPackage(const pugi::xml_node& node) {
        auto* pkg = emf::ecore::EcoreFactory::instance().createEPackage();
        applyAttrsToEPackage(pkg, node);
        // 注册 xmi:id
        registerXmiId(node, pkg);
        // 子元素：eClassifiers / eSubpackages
        for (pugi::xml_node child : node.children()) {
            if (child.type() != pugi::node_element) continue;
            if (localNameIs(child, "eClassifiers")) {
                auto* cls = buildEClassifier(child, pkg);
                if (cls) pkg->addEClassifier(cls);
            } else if (localNameIs(child, "eSubpackages")) {
                auto* sub = buildEPackage(child);
                if (sub) {
                    // 直接调用 EPackageImpl::addESubpackage 追加到 subpackages_ 列表。
                    // 不能用 eSet(EPACKAGE_ESUBPACKAGES, ...)，因为 EPackageImpl::eSet
                    // 对 multi-valued feature 是整体替换而非追加，多次调用只保留最后一个子包。
                    sub->setESuperPackage(pkg);
                    if (auto* impl = dynamic_cast<emf::ecore::EPackageImpl*>(pkg)) {
                        impl->addESubpackage(sub);
                    }
                }
            } else if (localNameIs(child, "eAnnotations") || localNameIs(child, "eTypeParameters") ||
                       localNameIs(child, "eFactoryInstance")) {
                // 已知但由 applyEAnnotations 等后续处理，此处跳过
                continue;
            } else {
                // 未知子元素：记录到 resource（对齐 Java OPTION_RECORD_UNKNOWN_FEATURE）
                if (res.isRecordUnknownFeature()) {
                    res.addUnknownContent(pkg, nodeToXmlString(child));
                }
            }
        }
        applyEAnnotations(node, pkg);
        return pkg;
    }

    void applyAttrsToEPackage(emf::ecore::EPackage* pkg, const pugi::xml_node& node) {
        std::string name = node.attribute("name").value();
        if (!name.empty()) pkg->setName(name);
        std::string nsURI = node.attribute("nsURI").value();
        if (!nsURI.empty()) pkg->setNsURI(nsURI);
        std::string nsPrefix = node.attribute("nsPrefix").value();
        if (!nsPrefix.empty()) pkg->setNsPrefix(nsPrefix);
    }

    emf::ecore::EClassifier* buildEClassifier(const pugi::xml_node& node, emf::ecore::EPackage* pkg) {
        // xsi:type 决定具体类型
        std::string xsiType = node.attribute("xsi:type").value();
        if (xsiType.empty()) xsiType = node.attribute("ecore:type").value();
        // 去掉 "ecore:" 前缀
        auto colon = xsiType.find(':');
        if (colon != std::string::npos) xsiType = xsiType.substr(colon + 1);

        if (xsiType == "EClass") {
            return buildEClass(node, pkg);
        } else if (xsiType == "EDataType") {
            return buildEDataType(node, pkg);
        } else if (xsiType == "EEnum") {
            return buildEEnum(node, pkg);
        }
        // 默认按 EClass 处理
        return buildEClass(node, pkg);
    }

    emf::ecore::EClass* buildEClass(const pugi::xml_node& node, emf::ecore::EPackage* pkg) {
        auto* cls = emf::ecore::EcoreFactory::instance().createEClass();
        std::string name = node.attribute("name").value();
        if (!name.empty()) cls->setName(name);
        if (!node.attribute("abstract").empty()) cls->setAbstract(parseBool(node.attribute("abstract").value()));
        if (!node.attribute("interface").empty()) cls->setInterface(parseBool(node.attribute("interface").value()));
        // eSuperTypes: 形如 "#//Base" 或多个空格分隔（简化：只取第一个，多 super 罕见）
        std::string supers = node.attribute("eSuperTypes").value();
        if (!supers.empty()) {
            pendingRefs.push_back({cls, "eSuperTypes", supers, pkg});
        }
        // instanceClassName
        std::string icn = node.attribute("instanceClassName").value();
        if (!icn.empty()) cls->setInstanceClassName(icn);
        registerXmiId(node, cls);
        // 子元素：eStructuralFeatures / eOperations / eTypeParameters / eGenericSuperTypes
        for (pugi::xml_node child : node.children()) {
            if (child.type() != pugi::node_element) continue;
            if (localNameIs(child, "eStructuralFeatures")) {
                auto* sf = buildEStructuralFeature(child, cls);
                if (sf) cls->addEStructuralFeature(sf);
            } else if (localNameIs(child, "eOperations")) {
                auto* op = buildEOperation(child, cls);
                if (op) cls->addEOperation(op);
            } else if (localNameIs(child, "eTypeParameters")) {
                // EClassifierImpl::addTypeParameter
                auto* tp = buildETypeParameter(child);
                if (tp) {
                    if (auto* clsImpl = dynamic_cast<emf::ecore::EClassifierImpl*>(cls)) {
                        clsImpl->addTypeParameter(tp);
                    }
                }
            } else if (localNameIs(child, "eGenericSuperTypes")) {
                // 对齐 Java：eGenericSuperTypes 是真实 containment 存储槽（非 derived）。
                // 构建 EGenericType 并 addEGenericSuperType（含 eTypeArguments 等泛型信息）。
                // eClassifier 在 buildEGenericType 内挂 pendingRef，resolvePending 阶段解析。
                // superTypes_ 同步在 resolvePending 末尾通过 syncSuperTypesFromGeneric 完成。
                auto* gt = buildEGenericType(child);
                if (gt) {
                    if (auto* clsImpl = dynamic_cast<emf::ecore::EClassImpl*>(cls)) {
                        clsImpl->addEGenericSuperType(gt);
                    }
                }
            }
        }
        applyEAnnotations(node, cls);
        return cls;
    }

    emf::ecore::EDataType* buildEDataType(const pugi::xml_node& node, emf::ecore::EPackage* pkg) {
        auto* dt = emf::ecore::EcoreFactory::instance().createEDataType();
        std::string name = node.attribute("name").value();
        if (!name.empty()) dt->setName(name);
        std::string icn = node.attribute("instanceClassName").value();
        if (!icn.empty()) dt->setInstanceClassName(icn);
        // serializable 属性（对齐 Java EDataType.serializable，默认 true）
        if (!node.attribute("serializable").empty()) dt->setSerializable(parseBool(node.attribute("serializable").value(), true));
        registerXmiId(node, dt);
        // eTypeParameters 子元素（对齐 Java EClassifier.eTypeParameters containment）
        // 例：<eClassifiers xsi:type="ecore:EDataType" name="MyMap"><eTypeParameters name="K"/>...</eClassifiers>
        for (pugi::xml_node child : node.children()) {
            if (child.type() != pugi::node_element) continue;
            if (localNameIs(child, "eTypeParameters")) {
                auto* tp = buildETypeParameter(child);
                if (tp) {
                    if (auto* clsImpl = dynamic_cast<emf::ecore::EClassifierImpl*>(dt)) {
                        clsImpl->addTypeParameter(tp);
                    }
                }
            }
        }
        applyEAnnotations(node, dt);
        return dt;
    }

    emf::ecore::EEnum* buildEEnum(const pugi::xml_node& node, emf::ecore::EPackage* pkg) {
        auto* en = emf::ecore::EcoreFactory::instance().createEEnum();
        std::string name = node.attribute("name").value();
        if (!name.empty()) en->setName(name);
        registerXmiId(node, en);
        // 子元素：eLiterals
        int nextValue = 0;
        for (pugi::xml_node child : node.children()) {
            if (child.type() != pugi::node_element) continue;
            if (!localNameIs(child, "eLiterals")) continue;
            auto* lit = emf::ecore::EcoreFactory::instance().createEEnumLiteral();
            std::string lname = child.attribute("name").value();
            if (!lname.empty()) lit->setName(lname);
            std::string valStr = child.attribute("value").value();
            if (!valStr.empty()) {
                try { nextValue = std::stoi(valStr); } catch (...) {}
            }
            lit->setValue(nextValue);
            std::string litStr = child.attribute("literal").value();
            if (!litStr.empty()) lit->setLiteral(litStr);
            registerXmiId(child, lit);
            // EEnumLiteral 也是 EModelElement，支持 eAnnotations
            applyEAnnotations(child, lit);
            // EEnumImpl::addELiteral 直接调用（公开方法）
            if (auto* enImpl = dynamic_cast<emf::ecore::EEnumImpl*>(en)) {
                enImpl->addELiteral(lit);
            }
            ++nextValue;
        }
        applyEAnnotations(node, en);
        return en;
    }

    emf::ecore::EStructuralFeature* buildEStructuralFeature(const pugi::xml_node& node, emf::ecore::EClass* cls) {
        std::string xsiType = node.attribute("xsi:type").value();
        if (xsiType.empty()) xsiType = node.attribute("ecore:type").value();
        auto colon = xsiType.find(':');
        if (colon != std::string::npos) xsiType = xsiType.substr(colon + 1);

        emf::ecore::EStructuralFeature* sf = nullptr;
        if (xsiType == "EAttribute") {
            auto* attr = emf::ecore::EcoreFactory::instance().createEAttribute();
            sf = attr;
        } else if (xsiType == "EReference") {
            auto* ref = emf::ecore::EcoreFactory::instance().createEReference();
            sf = ref;
        } else {
            // 默认按 EAttribute
            auto* attr = emf::ecore::EcoreFactory::instance().createEAttribute();
            sf = attr;
        }
        if (!sf) return nullptr;

        std::string name = node.attribute("name").value();
        if (!name.empty()) sf->setName(name);
        // ordered/unique（对齐 Java ETypedElement，默认 true，false 时解析）
        if (!node.attribute("ordered").empty()) sf->setOrdered(parseBool(node.attribute("ordered").value(), true));
        if (!node.attribute("unique").empty()) sf->setUnique(parseBool(node.attribute("unique").value(), true));
        std::string lb = node.attribute("lowerBound").value();
        if (!lb.empty()) { try { sf->setLowerBound(std::stoi(lb)); } catch (...) {} }
        std::string ub = node.attribute("upperBound").value();
        if (!ub.empty()) { try { sf->setUpperBound(std::stoi(ub)); } catch (...) {} }
        // defaultValueLiteral：用 attribute 存在性检测（对齐 Java isSet），
        // 区分"未设置"与"显式设为空字符串"。pugixml 的 attribute().empty() 仅在
        // 属性不存在时为 true（存在但值为空串时为 false），等价于原 hasAttr。
        if (!node.attribute("defaultValueLiteral").empty()) {
            sf->setDefaultValueLiteral(node.attribute("defaultValueLiteral").value());
        }
        if (!node.attribute("changeable").empty()) sf->setChangeable(parseBool(node.attribute("changeable").value(), true));
        if (!node.attribute("volatile").empty()) sf->setVolatile(parseBool(node.attribute("volatile").value()));
        if (!node.attribute("transient").empty()) sf->setTransient(parseBool(node.attribute("transient").value()));
        if (!node.attribute("unsettable").empty()) sf->setUnsettable(parseBool(node.attribute("unsettable").value()));
        if (!node.attribute("derived").empty()) sf->setDerived(parseBool(node.attribute("derived").value()));
        registerXmiId(node, sf);

        // eType 延迟解析
        std::string eType = node.attribute("eType").value();
        if (!eType.empty()) {
            pendingRefs.push_back({sf, "eType", eType, cls ? nullptr : nullptr});
            // contextPkg 在 resolvePending 里从 sf 所属 EClass 推导
        }

        // EReference 特有
        if (xsiType == "EReference") {
            auto* ref = dynamic_cast<emf::ecore::EReference*>(sf);
            if (ref) {
                if (!node.attribute("containment").empty()) ref->setContainment(parseBool(node.attribute("containment").value()));
                if (!node.attribute("resolveProxies").empty()) ref->setResolveProxies(parseBool(node.attribute("resolveProxies").value(), true));
                std::string eOpp = node.attribute("eOpposite").value();
                if (!eOpp.empty()) {
                    pendingRefs.push_back({ref, "eOpposite", eOpp, nullptr});
                }
            }
        }
        // EAttribute 特有：isID
        if (xsiType == "EAttribute" || xsiType.empty()) {
            auto* attr = dynamic_cast<emf::ecore::EAttribute*>(sf);
            if (attr && !node.attribute("iD").empty()) attr->setID(parseBool(node.attribute("iD").value()));
        }
        // eGenericType 子元素（对齐 Java ETypedElement.eGenericType containment）
        // 参数化类型时替代 eType 属性：<eGenericType eClassifier="..." ><eTypeArguments.../></eGenericType>
        for (pugi::xml_node child : node.children()) {
            if (child.type() != pugi::node_element) continue;
            if (localNameIs(child, "eGenericType")) {
                auto* gt = buildEGenericType(child);
                if (gt) sf->setEGenericType(gt);
            }
        }
        applyEAnnotations(node, sf);
        return sf;
    }

    // ===== 对齐 Java SAXXMIHandler：解析 EModelElement 共有的 eAnnotations 子元素 =====
    // 可挂在 EPackage / EClassifier / EStructuralFeature / EOperation / EParameter /
    // EEnumLiteral / ETypeParameter / EGenericType 上。
    void applyEAnnotations(const pugi::xml_node& node, emf::ecore::EModelElement* owner) {
        if (!owner) return;
        for (pugi::xml_node child : node.children()) {
            if (child.type() != pugi::node_element) continue;
            if (!localNameIs(child, "eAnnotations")) continue;
            auto* ann = emf::ecore::EcoreFactory::instance().createEAnnotation();
            std::string src = child.attribute("source").value();
            if (!src.empty()) ann->setSource(src);
            registerXmiId(child, ann);
            // details 子元素：<details key="k" value="v"/>
            for (pugi::xml_node d : child.children()) {
                if (d.type() != pugi::node_element) continue;
                if (!localNameIs(d, "details")) continue;
                std::string k = d.attribute("key").value();
                std::string v = d.attribute("value").value();
                ann->setDetail(k, v);
            }
            // references / contents 子元素（containment 引用，罕用，简化为收集 EObject）
            // 此处暂不处理 contents 的递归 build，保持最小覆盖。
            if (auto* impl = dynamic_cast<emf::ecore::EModelElementImpl*>(owner)) {
                impl->addEAnnotation(ann);
            }
        }
    }

    // ===== EOperation：对齐 Java EOperationImpl =====
    emf::ecore::EOperation* buildEOperation(const pugi::xml_node& node, emf::ecore::EClass* /*cls*/) {
        auto* op = emf::ecore::EcoreFactory::instance().createEOperation();
        std::string name = node.attribute("name").value();
        if (!name.empty()) op->setName(name);
        std::string lb = node.attribute("lowerBound").value();
        if (!lb.empty()) { try { op->setLowerBound(std::stoi(lb)); } catch (...) {} }
        std::string ub = node.attribute("upperBound").value();
        if (!ub.empty()) { try { op->setUpperBound(std::stoi(ub)); } catch (...) {} }
        std::string oid = node.attribute("operationID").value();
        if (!oid.empty()) { try { op->setOperationID(std::stoi(oid)); } catch (...) {} }
        registerXmiId(node, op);
        // eType 延迟解析（ETypedElement 共用路径）
        std::string eType = node.attribute("eType").value();
        if (!eType.empty()) {
            pendingRefs.push_back({op, "eType", eType, nullptr});
        }
        // eExceptions 延迟解析（对齐 Java EOperation.eExceptions，多值用空格分隔）
        std::string eExc = node.attribute("eExceptions").value();
        if (!eExc.empty()) {
            // eExceptions 可能是 "#//Ex1 #//Ex2" 多值形式，拆分后每个都加 pendingRef
            std::istringstream ess(eExc);
            std::string oneHref;
            while (ess >> oneHref) {
                pendingRefs.push_back({op, "eExceptions", oneHref, nullptr});
            }
        }
        auto* opImpl = dynamic_cast<emf::ecore::EOperationImpl*>(op);
        for (pugi::xml_node child : node.children()) {
            if (child.type() != pugi::node_element) continue;
            if (localNameIs(child, "eParameters")) {
                auto* p = buildEParameter(child, op);
                if (p && opImpl) opImpl->addEParameter(p);
            } else if (localNameIs(child, "eTypeParameters")) {
                auto* tp = buildETypeParameter(child);
                if (tp && opImpl) opImpl->addETypeParameter(tp);
            } else if (localNameIs(child, "eGenericType")) {
                // eGenericType 子元素（对齐 Java ETypedElement.eGenericType containment）
                // 替代 eType 属性表达参数化返回类型
                auto* gt = buildEGenericType(child);
                if (gt) op->setEGenericType(gt);
            }
        }
        applyEAnnotations(node, op);
        return op;
    }

    // ===== EParameter：对齐 Java EParameterImpl =====
    emf::ecore::EParameter* buildEParameter(const pugi::xml_node& node, emf::ecore::EOperation* op) {
        auto* p = emf::ecore::EcoreFactory::instance().createEParameter();
        std::string name = node.attribute("name").value();
        if (!name.empty()) p->setName(name);
        std::string lb = node.attribute("lowerBound").value();
        if (!lb.empty()) { try { p->setLowerBound(std::stoi(lb)); } catch (...) {} }
        std::string ub = node.attribute("upperBound").value();
        if (!ub.empty()) { try { p->setUpperBound(std::stoi(ub)); } catch (...) {} }
        registerXmiId(node, p);
        std::string eType = node.attribute("eType").value();
        if (!eType.empty()) {
            pendingRefs.push_back({p, "eType", eType, nullptr});
        }
        // eGenericType 子元素（对齐 Java ETypedElement.eGenericType containment）
        // 参数化类型时替代 eType 属性
        for (pugi::xml_node child : node.children()) {
            if (child.type() != pugi::node_element) continue;
            if (localNameIs(child, "eGenericType")) {
                auto* gt = buildEGenericType(child);
                if (gt) p->setEGenericType(gt);
            }
        }
        if (op) {
            if (auto* opImpl = dynamic_cast<emf::ecore::EOperationImpl*>(op)) {
                // EParameterImpl 未暴露 setEOperation，但 addEParameter 通常会回写。
                // 这里不强制回写，保持与 Impl 内部一致性。
            }
        }
        applyEAnnotations(node, p);
        return p;
    }

    // ===== ETypeParameter：对齐 Java ETypeParameterImpl =====
    emf::ecore::ETypeParameter* buildETypeParameter(const pugi::xml_node& node) {
        auto* tp = emf::ecore::EcoreFactory::instance().createETypeParameter();
        std::string name = node.attribute("name").value();
        if (!name.empty()) tp->setName(name);
        registerXmiId(node, tp);
        auto* tpImpl = dynamic_cast<emf::ecore::ETypeParameterImpl*>(tp);
        for (pugi::xml_node child : node.children()) {
            if (child.type() != pugi::node_element) continue;
            if (localNameIs(child, "eBounds")) {
                auto* gt = buildEGenericType(child);
                if (gt && tpImpl) tpImpl->addEBound(gt);
            }
        }
        applyEAnnotations(node, tp);
        return tp;
    }

    // ===== EGenericType：对齐 Java EGenericTypeImpl =====
    // 出现在 eGenericSuperTypes（EClass 子元素）、eBounds（ETypeParameter 子元素）、
    // eTypeArguments / eUpperBound / eLowerBound（EGenericType 子元素，递归）、
    // eGenericType（ETypedElement 子元素，替代 eType 属性表达参数化类型）。
    emf::ecore::EGenericType* buildEGenericType(const pugi::xml_node& node) {
        auto* gt = emf::ecore::EcoreFactory::instance().createEGenericType();
        registerXmiId(node, gt);
        // eClassifier 属性（EClassifier 引用，延迟解析；与 eTypeParameter 互斥）
        std::string eCls = node.attribute("eClassifier").value();
        if (!eCls.empty()) {
            pendingRefs.push_back({gt, "eClassifier", eCls, nullptr});
        }
        // eTypeParameter 属性（ETypeParameter 引用，延迟解析；与 eClassifier 互斥）
        // 形如 eTypeParameter="#//MyClass/T" 或 "#//MyClass/bar/F"
        std::string eTp = node.attribute("eTypeParameter").value();
        if (!eTp.empty()) {
            pendingRefs.push_back({gt, "eTypeParameter", eTp, nullptr});
        }
        std::vector<emf::ecore::EGenericType*> typeArgs;
        for (pugi::xml_node child : node.children()) {
            if (child.type() != pugi::node_element) continue;
            if (localNameIs(child, "eUpperBound")) {
                auto* ub = buildEGenericType(child);
                if (ub) gt->setEUpperBound(ub);
            } else if (localNameIs(child, "eLowerBound")) {
                auto* lb = buildEGenericType(child);
                if (lb) gt->setELowerBound(lb);
            } else if (localNameIs(child, "eTypeArguments")) {
                auto* arg = buildEGenericType(child);
                if (arg) typeArgs.push_back(arg);
            }
        }
        if (auto* gtImpl = dynamic_cast<emf::ecore::EGenericTypeImpl*>(gt)) {
            if (!typeArgs.empty()) gtImpl->setETypeArguments(typeArgs);
        }
        return gt;
    }

    void registerXmiId(const pugi::xml_node& node, emf::common::EObject* obj) {
        if (!obj) return;
        std::string id = node.attribute("xmi:id").value();
        if (id.empty()) id = node.attribute("id").value();
        if (!id.empty()) {
            idMap[id] = obj;
            res.setID(obj, id);
        }
    }

    // ===== 跨文档 classifier 解析（文件路径形式 href） =====
    // 对齐 Java: XMLHelperImpl.getEObject → ResourceSet.getEObject(URI, loadOnDemand)
    // href 形如:
    //   "library.ecore#//Library"              —— 纯文件路径形式
    //   "ecore:EClass library.ecore#//Book"    —— 带 kind 前缀
    // 通过 ResourceSet demand-load 目标 .ecore 文件并按 fragment 解析 classifier。
    emf::ecore::EClassifier* resolveCrossDocClassifier(const std::string& href,
                                                       const std::string& name) {
        std::string frag = href;
        auto hash = frag.find('#');
        if (hash == std::string::npos) return nullptr;
        std::string docPart = frag.substr(0, hash);
        frag = frag.substr(hash + 1);
        // 跳过 kind 前缀（如 "ecore:EClass "），取最后的文件路径
        auto sp = docPart.find(' ');
        if (sp != std::string::npos) docPart = docPart.substr(sp + 1);
        if (docPart.empty()) return nullptr;

        auto* rs = res.getResourceSet();
        if (!rs) return nullptr;
        emf::common::URI hrefUri(docPart);
        emf::common::URI baseUri = res.getURI();
        emf::common::URI absUri = hrefUri.resolve(baseUri);
        emf::common::URI fullUri = absUri.appendFragment(frag);
        auto* obj = rs->getEObject(fullUri, true);
        if (!obj) return nullptr;
        // name 用于校验/辅助：若 fragment 解析失败可按 name 兜底
        if (auto* cls = dynamic_cast<emf::ecore::EClassifier*>(obj)) return cls;
        // 兜底：在返回对象所属包里按 name 找
        if (auto* pkg = dynamic_cast<emf::ecore::EPackage*>(obj)) {
            return findClassifierRecursive(pkg, name);
        }
        return nullptr;
    }

    // ===== 延迟引用解析 =====
    void resolvePending() {
        for (auto& pr : pendingRefs) {
            if (!pr.owner) continue;
            // 推导 contextPkg：从 owner 的 EClass 链找所属 EPackage
            emf::ecore::EPackage* ctxPkg = pr.contextPkg;
            if (!ctxPkg) {
                // EStructuralFeature -> EContainingClass -> EPackage
                if (auto* sf = dynamic_cast<emf::ecore::EStructuralFeature*>(pr.owner)) {
                    auto* cls = sf->getEContainingClass();
                    if (cls) {
                        // EClassifier.getEPackage()
                        ctxPkg = cls->getEPackage();
                    }
                } else if (auto* cls = dynamic_cast<emf::ecore::EClass*>(pr.owner)) {
                    ctxPkg = cls->getEPackage();
                } else if (auto* op = dynamic_cast<emf::ecore::EOperation*>(pr.owner)) {
                    // EOperation -> EContainingClass -> EPackage
                    auto* cls = op->getEContainingClass();
                    if (cls) ctxPkg = cls->getEPackage();
                } else if (auto* p = dynamic_cast<emf::ecore::EParameter*>(pr.owner)) {
                    // EParameter -> EOperation -> EContainingClass -> EPackage
                    auto* op = p->getEOperation();
                    if (op) {
                        auto* cls = op->getEContainingClass();
                        if (cls) ctxPkg = cls->getEPackage();
                    }
                }
            }
            resolveOne(pr, ctxPkg);
        }
        pendingRefs.clear();

        // ===== 对齐 Java EClassImpl：eGenericSuperTypes 的 eClassifier 延迟解析后，
        // 同步 superTypes_ derived view（去重）。保证 getESuperTypes()/getEAllSuperTypes() 正确。
        for (auto* rootObj : res.getContents()) {
            auto* pkg = dynamic_cast<emf::ecore::EPackage*>(rootObj);
            if (!pkg) continue;
            for (auto* cls : pkg->getEClassifiers()) {
                auto* ecls = dynamic_cast<emf::ecore::EClassImpl*>(cls);
                if (ecls) ecls->syncSuperTypesFromGeneric();
            }
        }

        // ===== 对齐 Java EClassImpl：所有 pendingRefs（含 eSuperTypes）解析完成后，
        // 重新分配动态 EClass 自有 feature 的 featureID = 继承 feature 总数 + 自有序号。
        // 这保证 eAllStructuralFeatures 中每个 featureID 唯一，避免跨包继承冲突
        // （如 base#Library.name 与 ext#AnnotatedLibrary.note 都默认 fid=0）。
        // 仅重分配动态 feature（featureID < 1000），保留 EcorePackage meta 常量（>= 1000）。
        for (auto* rootObj : res.getContents()) {
            auto* pkg = dynamic_cast<emf::ecore::EPackage*>(rootObj);
            if (!pkg) continue;
            for (auto* cls : pkg->getEClassifiers()) {
                auto* ecls = dynamic_cast<emf::ecore::EClass*>(cls);
                if (!ecls) continue;
                int inheritedCount = 0;
                for (auto* st : ecls->getESuperTypes()) {
                    if (st) inheritedCount += static_cast<int>(st->getEAllStructuralFeatures().size());
                }
                int idx = 0;
                for (auto* sf : ecls->getEStructuralFeatures()) {
                    if (sf) {
                        if (sf->getFeatureID() < 1000) {
                            sf->setFeatureID(inheritedCount + idx);
                        }
                        ++idx;
                    }
                }
            }
        }
    }

    void resolveOne(const PendingRef& pr, emf::ecore::EPackage* ctxPkg) {
        if (pr.attrName == "eType") {
            auto ti = parseTypeAttr(pr.href);
            // 自引用判断：href path 指向当前 resource，或指向不存在的绝对路径文件
            bool selfRef = ti.isExternal && (isSelfReference(ti.nsURI, res) ||
                      (!ti.nsURI.empty() && ti.nsURI[0] == '/' &&
                       ti.nsURI.find("://") == std::string::npos &&
                       !std::ifstream(ti.nsURI).good()));
            // 保留跨文档文件路径形式的原始 href（对齐 Java deresolve 输出）。
            // 自引用不保留 crossDocHref（saver 应输出同文档 #// 形式）
            if (ti.isExternal && !selfRef && ti.nsURI.find("://") == std::string::npos) {
                res.crossDocHrefs()[pr.owner] = pr.href;
            }
            emf::ecore::EClassifier* target = nullptr;
            if (ti.isExternal && !selfRef) {
                // ecore 内建类型
                if (ti.nsURI == kEcoreNsURI || ti.nsURI.find("Ecore") != std::string::npos) {
                    target = findEcoreBuiltin(ti.name);
                }
                // 其它外部包：通过 EPackageRegistry 查
                if (!target) {
                    auto* regPkg = emf::common::EPackageRegistry::instance().get(ti.nsURI);
                    if (regPkg) {
                        auto* ePkg = dynamic_cast<emf::ecore::EPackage*>(regPkg);
                        if (ePkg) {
                            auto* el = navigateContainmentPath(ePkg, extractFragment(pr.href));
                            target = dynamic_cast<emf::ecore::EClassifier*>(el);
                            if (!target) {
                                target = findClassifierRecursive(ePkg, lastFragmentSegment(pr.href));
                            }
                        }
                    }
                }
                // 文件路径形式 href：通过 ResourceSet demand-load 目标 .ecore 文件
                // 对齐 Java: XMLHelperImpl.getEObject → ResourceSet.getEObject(URI, loadOnDemand)
                if (!target && ti.nsURI.find("://") == std::string::npos) {
                    target = resolveCrossDocClassifier(pr.href, lastFragmentSegment(pr.href));
                }
            } else {
                // 同包 #//Name 或 #//subpkg/.../Name —— 按 containment 路径导航
                // 对跨子包引用（如 #//swcomponent/.../Foo），从根包导航
                // 自引用时 pr.href 是文件路径形式，取 fragment 部分
                std::string hrefForResolve = selfRef ? extractFragment(pr.href) : pr.href;
                if (!hrefForResolve.empty() && hrefForResolve[0] != '#') {
                    hrefForResolve = "#" + hrefForResolve;
                }
                target = resolveSameDocClassifier(ctxPkg, hrefForResolve);
                // 自引用兜底：按类名递归搜
                if (!target && selfRef) {
                    target = findClassifierRecursive(ctxPkg, ti.name);
                }
            }
            if (target) {
                // EAttribute 需走 setEAttributeType（同步 eAttributeType_ + eType_）
                if (auto* attr = dynamic_cast<emf::ecore::EAttribute*>(pr.owner)) {
                    if (auto* dt = dynamic_cast<emf::ecore::EDataType*>(target)) {
                        attr->setEAttributeType(dt);
                    } else {
                        attr->setEType(target);
                    }
                } else if (auto* ref = dynamic_cast<emf::ecore::EReference*>(pr.owner)) {
                    // EReference 可走 setEReferenceType 或 setEType
                    if (auto* cls = dynamic_cast<emf::ecore::EClass*>(target)) {
                        ref->setEReferenceType(cls);
                    } else {
                        ref->setEType(target);
                    }
                } else if (auto* te = dynamic_cast<emf::ecore::ETypedElement*>(pr.owner)) {
                    te->setEType(target);
                }
            }
        } else if (pr.attrName == "eExceptions") {
            // eExceptions 引用 EClassifier，解析逻辑与 eType 一致（多值已在 buildEOperation 拆分）
            auto ti = parseTypeAttr(pr.href);
            emf::ecore::EClassifier* target = nullptr;
            if (ti.isExternal) {
                if (ti.nsURI == kEcoreNsURI || ti.nsURI.find("Ecore") != std::string::npos) {
                    target = findEcoreBuiltin(ti.name);
                }
                if (!target) {
                    auto* regPkg = emf::common::EPackageRegistry::instance().get(ti.nsURI);
                    if (regPkg) {
                        auto* ePkg = dynamic_cast<emf::ecore::EPackage*>(regPkg);
                        if (ePkg) {
                            auto* el = navigateContainmentPath(ePkg, extractFragment(pr.href));
                            target = dynamic_cast<emf::ecore::EClassifier*>(el);
                            if (!target) {
                                target = findClassifierRecursive(ePkg, lastFragmentSegment(pr.href));
                            }
                        }
                    }
                }
                // 文件路径形式 href：通过 ResourceSet demand-load 目标 .ecore 文件
                if (!target && ti.nsURI.find("://") == std::string::npos) {
                    target = resolveCrossDocClassifier(pr.href, lastFragmentSegment(pr.href));
                }
            } else {
                target = resolveSameDocClassifier(ctxPkg, pr.href);
            }
            if (target) {
                if (auto* op = dynamic_cast<emf::ecore::EOperation*>(pr.owner)) {
                    op->addEException(target);
                }
            }
        } else if (pr.attrName == "eSuperTypes") {
            // 形如 "#//Base" 或 "#//subpkg/.../Base" 或多个以空格分隔
            // 用 navigateContainmentPath 按 containment 路径逐段导航（对齐 Java fragment 解析）
            std::istringstream iss(pr.href);
            std::string token;
            auto* cls = dynamic_cast<emf::ecore::EClass*>(pr.owner);
            if (!cls) return;
            while (iss >> token) {
                auto ti = parseTypeAttr(token);
                emf::ecore::EClass* base = nullptr;
                // 自引用判断：href path 指向当前 resource，或指向不存在的绝对路径文件
                // （对齐 Java：demand-load 失败时 fallback 到同文档解析）
                // 注意：仅对绝对路径生效，相对路径可能是合法的跨文件引用
                bool selfRef = ti.isExternal && (isSelfReference(ti.nsURI, res) ||
                          (!ti.nsURI.empty() && ti.nsURI[0] == '/' &&
                           ti.nsURI.find("://") == std::string::npos &&
                           !std::ifstream(ti.nsURI).good()));
                if (ti.isExternal && !selfRef) {
                    auto* regPkg = emf::common::EPackageRegistry::instance().get(ti.nsURI);
                    if (regPkg) {
                        auto* ePkg = dynamic_cast<emf::ecore::EPackage*>(regPkg);
                        if (ePkg) {
                            auto* el = navigateContainmentPath(ePkg, extractFragment(token));
                            base = dynamic_cast<emf::ecore::EClass*>(el);
                            // 回退：按最后一段类名递归搜
                            if (!base) {
                                auto* c = findClassifierRecursive(ePkg, lastFragmentSegment(token));
                                base = dynamic_cast<emf::ecore::EClass*>(c);
                            }
                        }
                    }
                    // 文件路径形式 href：通过 ResourceSet demand-load 目标 .ecore 文件
                    if (!base && ti.nsURI.find("://") == std::string::npos) {
                        auto* c = resolveCrossDocClassifier(token, lastFragmentSegment(token));
                        base = dynamic_cast<emf::ecore::EClass*>(c);
                    }
                } else if (ctxPkg) {
                    // 同文档引用或自引用：从 ctxPkg 按 fragment 解析
                    auto* c = resolveSameDocClassifier(ctxPkg, selfRef ? extractFragment(token) : token);
                    base = dynamic_cast<emf::ecore::EClass*>(c);
                    // 自引用兜底：按类名递归搜
                    if (!base && selfRef) {
                        base = dynamic_cast<emf::ecore::EClass*>(findClassifierRecursive(ctxPkg, ti.name));
                    }
                }
                if (base) {
                    // 保留跨文档文件路径形式的原始 href（对齐 Java deresolve 输出）。
                    // 按 base（super EClass）索引，saver 输出 eSuperTypes 时按 super 查找。
                    // 自引用不保留（saver 应输出同文档 #// 形式）。
                    if (ti.isExternal && !selfRef && ti.nsURI.find("://") == std::string::npos) {
                        res.crossDocHrefs()[base] = token;
                    }
                    cls->addESuperType(base);
                }
            }
        } else if (pr.attrName == "eOpposite") {
            // 形如 "#//ClassName/featureName" 或 "#//subpkg/.../ClassName/featureName"
            // 用 navigateContainmentPath 按 containment 路径直接导航到 EStructuralFeature
            auto ti = parseTypeAttr(pr.href);
            // 自引用判断：href path 指向当前 resource，或指向不存在的绝对路径文件
            bool selfRef = ti.isExternal && (isSelfReference(ti.nsURI, res) ||
                      (!ti.nsURI.empty() && ti.nsURI[0] == '/' &&
                       ti.nsURI.find("://") == std::string::npos &&
                       !std::ifstream(ti.nsURI).good()));
            if ((!ti.isExternal || selfRef) && ctxPkg) {
                std::string frag = extractFragment(pr.href);
                // 1. 从 ctxPkg 导航
                auto* el = navigateContainmentPath(ctxPkg, frag);
                auto* opp = dynamic_cast<emf::ecore::EReference*>(el);
                // 2. 从根包导航（跨子包）
                if (!opp) {
                    auto* root = findRootPackage(ctxPkg);
                    if (root && root != ctxPkg) {
                        el = navigateContainmentPath(root, frag);
                        opp = dynamic_cast<emf::ecore::EReference*>(el);
                    }
                }
                if (!opp) {
                    // 回退：最后一段是 featureName，倒数第二段是 ClassName
                    auto slash = frag.rfind('/');
                    if (slash != std::string::npos) {
                        std::string featureName = frag.substr(slash + 1);
                        std::string remaining = frag.substr(0, slash);
                        auto slash2 = remaining.rfind('/');
                        std::string clsName = (slash2 == std::string::npos) ? remaining
                                                                            : remaining.substr(slash2 + 1);
                        if (!clsName.empty() && !featureName.empty()) {
                            auto* root = findRootPackage(ctxPkg);
                            auto* c = findClassifierRecursive(root ? root : ctxPkg, clsName);
                            auto* cls = dynamic_cast<emf::ecore::EClass*>(c);
                            if (cls) {
                                opp = dynamic_cast<emf::ecore::EReference*>(
                                    cls->getEStructuralFeature(featureName));
                            }
                        }
                    }
                }
                if (opp) {
                    if (auto* ref = dynamic_cast<emf::ecore::EReference*>(pr.owner)) {
                        ref->setEOpposite(opp);
                    }
                }
            }
        } else if (pr.attrName == "eClassifier") {
            // EGenericType.eClassifier 引用：解析 EClassifier 并 setEClassifier。
            // ctxPkg 未在 buildEGenericType 传入，这里从 owner 的 eContainer 链推导（简化：用 registry）。
            auto ti = parseTypeAttr(pr.href);
            // 保留跨文档文件路径形式的原始 href（对齐 Java deresolve 输出）
            if (ti.isExternal && ti.nsURI.find("://") == std::string::npos) {
                res.crossDocHrefs()[pr.owner] = pr.href;
            }
            emf::ecore::EClassifier* target = nullptr;
            if (ti.isExternal) {
                if (ti.nsURI == kEcoreNsURI || ti.nsURI.find("Ecore") != std::string::npos) {
                    target = findEcoreBuiltin(ti.name);
                }
                if (!target) {
                    auto* regPkg = emf::common::EPackageRegistry::instance().get(ti.nsURI);
                    if (regPkg) {
                        auto* ePkg = dynamic_cast<emf::ecore::EPackage*>(regPkg);
                        if (ePkg) {
                            auto* el = navigateContainmentPath(ePkg, extractFragment(pr.href));
                            target = dynamic_cast<emf::ecore::EClassifier*>(el);
                            if (!target) {
                                target = findClassifierRecursive(ePkg, lastFragmentSegment(pr.href));
                            }
                        }
                    }
                }
            }
            if (!target && ctxPkg) {
                target = resolveSameDocClassifier(ctxPkg, pr.href);
            }
            // ctxPkg 可能为空（buildEGenericType 未传），回退：在所有已注册包里搜
            if (!target) {
                for (auto* rootObj : res.getContents()) {
                    auto* pkg = dynamic_cast<emf::ecore::EPackage*>(rootObj);
                    if (!pkg) continue;
                    auto* el = navigateContainmentPath(pkg, extractFragment(pr.href));
                    target = dynamic_cast<emf::ecore::EClassifier*>(el);
                    if (!target) {
                        target = findClassifierRecursive(pkg, lastFragmentSegment(pr.href));
                    }
                    if (target) break;
                }
            }
            if (target) {
                if (auto* gt = dynamic_cast<emf::ecore::EGenericType*>(pr.owner)) {
                    gt->setEClassifier(target);
                }
            }
        } else if (pr.attrName == "eTypeParameter") {
            // EGenericType.eTypeParameter 引用：解析路径并 setETypeParameter。
            // href 形如 "#//MyClass/T"（类级类型参数）或 "#//MyClass/bar/F"（操作级类型参数）。
            // 在所有根 EPackage 中搜索：先按类名找 EClassifier，再按路径深度找 ETypeParameter。
            std::string frag = pr.href;
            auto hash = frag.find('#');
            if (hash != std::string::npos) frag = frag.substr(hash + 1);
            while (!frag.empty() && frag[0] == '/') frag = frag.substr(1);
            // 切分路径段
            std::vector<std::string> segs;
            {
                size_t start = 0;
                while (start < frag.size()) {
                    auto slash = frag.find('/', start);
                    if (slash == std::string::npos) { segs.push_back(frag.substr(start)); break; }
                    segs.push_back(frag.substr(start, slash - start));
                    start = slash + 1;
                }
            }
            if (segs.size() >= 2) {
                const std::string& clsName = segs[0];
                const std::string& tpName = segs.back();
                emf::ecore::ETypeParameter* tp = nullptr;
                // 在 ctxPkg 和所有根 EPackage 中搜索
                std::vector<emf::ecore::EPackage*> pkgs;
                if (ctxPkg) pkgs.push_back(ctxPkg);
                for (auto* rootObj : res.getContents()) {
                    if (auto* p = dynamic_cast<emf::ecore::EPackage*>(rootObj)) pkgs.push_back(p);
                }
                for (auto* pkg : pkgs) {
                    if (!pkg) continue;
                    auto* clsr = findClassifierRecursive(pkg, clsName);
                    if (!clsr) continue;
                    if (segs.size() == 2) {
                        // 类级类型参数
                        for (auto* t : clsr->getETypeParameters()) {
                            if (t && t->getName() == tpName) { tp = t; break; }
                        }
                    } else if (segs.size() == 3) {
                        // 操作级类型参数：segs[1] 是操作名
                        auto* ecls = dynamic_cast<emf::ecore::EClass*>(clsr);
                        if (!ecls) break;
                        emf::ecore::EOperation* op = nullptr;
                        for (auto* o : ecls->getEOperations()) {
                            if (o && o->getName() == segs[1]) { op = o; break; }
                        }
                        if (op) {
                            for (auto* t : op->getETypeParameters()) {
                                if (t && t->getName() == tpName) { tp = t; break; }
                            }
                        }
                    }
                    if (tp) break;
                }
                if (tp) {
                    if (auto* gt = dynamic_cast<emf::ecore::EGenericType*>(pr.owner)) {
                        gt->setETypeParameter(tp);
                    }
                }
            }
        } else if (pr.feature != nullptr) {
            // 实例 EReference 的跨引用解析（对齐 Java XMLHelperImpl.getEObject）
            // pr.href 形如:
            //   "//@publishers.1"      —— position path（单资源内）
            //   "//_idA"               —— xmi:id 引用（单资源内）
            //   "library.xmi#//@books.0" —— 跨文件 href（fragment 部分）
            //   "#//@books.0"          —— 同文件 fragment
            std::string frag = pr.href;
            // 处理跨文件 href：取 '#' 后的 fragment
            auto hash = frag.find('#');
            std::string docPart;  // '#' 前的部分（文件路径或空）
            if (hash != std::string::npos) {
                docPart = frag.substr(0, hash);
                frag = frag.substr(hash + 1);
            }
            // 判断是否跨文件引用（docPart 非空且不是 '#' 开头的同文件 fragment）
            emf::common::EObject* target = nullptr;
            if (!docPart.empty()) {
                // 跨文件引用：通过 ResourceSet 加载目标文件
                // 对齐 Java: XMLHelperImpl.getEObject → ResourceSet.getEObject(URI, loadOnDemand)
                auto* rs = res.getResourceSet();
                if (rs) {
                    // resolve 相对路径为绝对路径（基于当前 resource URI）
                    emf::common::URI hrefUri(docPart);
                    emf::common::URI baseUri = res.getURI();
                    emf::common::URI absUri = hrefUri.resolve(baseUri);
                    emf::common::URI fullUri = absUri.appendFragment(frag);
                    target = rs->getEObject(fullUri, true);
                }
                // 无 ResourceSet 时无法跨文件解析，target 保持 nullptr（引用丢失）
            } else {
                // 同文件引用
                target = res.getEObject(frag);
            }
            if (target) {
                if (pr.isMany) {
                    appendToMultiValue(pr.owner, pr.feature, target);
                } else {
                    emf::common::EObject* tmp = target;
                    pr.owner->eSet(pr.feature, std::any(tmp));
                }
            }
        }
    }

    // ===== 实例文档构建（library.xmi 风格） =====
    // 根元素形如 <prefix:Type xmlns:prefix="nsURI" ...>，
    // 通过 nsURI 在 EPackageRegistry 找 EPackage -> EFactory -> create(EClass)
    //
    // XML 命名空间继承（对齐 Java SAX 解析器）：
    //   xmlns 声明由父元素向所有后代元素继承。pugi::xml_node 不带继承上下文，
    //   因此通过 inheritedNs 参数把祖先的 xmlns 声明逐层传下去。
    using NsMap = std::unordered_map<std::string, std::string>;  // prefix -> nsURI（"" 表示默认命名空间）

    // 收集节点自身的 xmlns 声明，合并到 inherited 形成子节点应继承的命名空间表
    NsMap collectAndMergeNs(const pugi::xml_node& node, const NsMap& inherited) {
        NsMap ns = inherited;
        for (pugi::xml_attribute a : node.attributes()) {
            const char* aname = a.name();
            if (std::strcmp(aname, "xmlns") == 0) {
                ns[""] = a.value();
            } else if (std::strncmp(aname, "xmlns:", 6) == 0) {
                ns[aname + 6] = a.value();
            }
        }
        return ns;
    }
    // 按 prefix 在（节点自身 + 继承）命名空间表中查 nsURI
    std::string lookupNs(const pugi::xml_node& /*node*/, const std::string& prefix, const NsMap& ns) {
        auto it = ns.find(prefix);
        if (it != ns.end()) return it->second;
        return "";
    }

    emf::common::EObject* buildInstanceObject(const pugi::xml_node& node,
                                              const NsMap& inheritedNs = {}) {
        // 合并本节点 xmlns 声明（对齐 Java SAX 命名空间继承）
        NsMap ns = collectAndMergeNs(node, inheritedNs);
        // 收集 xmlns 声明，找 nsURI
        std::string nodePrefix = getNodePrefix(node);
        std::string nsURI = lookupNs(node, nodePrefix, ns);
        // 也支持 xmi:type 显式指定类型
        std::string xmiType = node.attribute("xmi:type").value();
        if (!xmiType.empty()) {
            auto colon = xmiType.find(':');
            if (colon != std::string::npos) {
                std::string typePrefix = xmiType.substr(0, colon);
                std::string typeName = xmiType.substr(colon + 1);
                nsURI = lookupNs(node, typePrefix, ns);
            }
        }
        // 对齐 Java：xsi:type 在实例层用于指定子类型（如 <books xsi:type="lib:Magazine">）
        std::string xsiType = node.attribute("xsi:type").value();
        std::string effectiveLocal = getNodeLocal(node);
        std::string effectiveNsURI = nsURI;
        if (!xsiType.empty()) {
            auto colon = xsiType.find(':');
            if (colon != std::string::npos) {
                std::string typePrefix = xsiType.substr(0, colon);
                std::string typeName = xsiType.substr(colon + 1);
                // 找该 prefix 对应的 nsURI（从当前节点 xmlns 声明或父节点继承）
                effectiveNsURI = lookupNs(node, typePrefix, ns);
                effectiveLocal = typeName;
            } else {
                effectiveLocal = xsiType;
            }
        }
        if (effectiveNsURI.empty()) return nullptr;
        auto* regPkg = emf::common::EPackageRegistry::instance().get(effectiveNsURI);
        if (!regPkg) return nullptr;
        auto* ePkg = dynamic_cast<emf::ecore::EPackage*>(regPkg);
        if (!ePkg) return nullptr;
        auto* cls = ePkg->getEClassifier(effectiveLocal);
        auto* eClass = dynamic_cast<emf::ecore::EClass*>(cls);
        if (!eClass) return nullptr;
        auto* factory = ePkg->getEFactoryInstance();
        if (!factory) return nullptr;
        auto* obj = factory->create(eClass);
        if (!obj) return nullptr;
        registerXmiId(node, obj);
        applyInstanceAttrs(obj, eClass, node, ePkg);
        // 子元素 -> 多值/单值 feature（传递合并后的命名空间表）
        for (pugi::xml_node child : node.children()) {
            if (child.type() != pugi::node_element) continue;
            applyInstanceChild(obj, eClass, child, ePkg, ns);
        }
        return obj;
    }

    void applyInstanceAttrs(emf::common::EObject* obj, emf::ecore::EClass* cls,
                            const pugi::xml_node& node, emf::ecore::EPackage* pkg) {
        for (pugi::xml_attribute a : node.attributes()) {
            const char* aname = a.name();
            // 跳过 xmlns / xmi:* / xsi:type
            if (std::strncmp(aname, "xmlns", 5) == 0) continue;  // 匹配 "xmlns" 和 "xmlns:*"
            if (std::strncmp(aname, "xmi:", 4) == 0) continue;
            if (std::strcmp(aname, "xsi:type") == 0) continue;
            auto* sf = cls->getEStructuralFeature(aname);
            if (!sf) {
                // 未知属性：记录到 resource（对齐 Java OPTION_RECORD_UNKNOWN_FEATURE）
                if (res.isRecordUnknownFeature()) {
                    std::string frag = " ";
                    frag += aname;
                    frag += "=\"";
                    frag += a.value();
                    frag += "\"";
                    res.addUnknownContent(obj, frag);
                }
                continue;
            }
            // ExtendedMetaData kind=element 的 feature 只从子元素读取（对齐 Java XMLLoadImpl）
            if (featureKindAnnotation(sf) == 'e') continue;
            setFeatureValue(obj, sf, a.value(), pkg);
        }
    }

    void applyInstanceChild(emf::common::EObject* obj, emf::ecore::EClass* cls,
                            const pugi::xml_node& node, emf::ecore::EPackage* pkg,
                            const NsMap& inheritedNs = {}) {
        std::string local = getNodeLocal(node);
        auto* sf = cls->getEStructuralFeature(local);
        if (!sf) {
            // 未知子元素：记录到 resource（对齐 Java OPTION_RECORD_UNKNOWN_FEATURE）
            // 保留原始 XML 片段，saver 原样输出，实现 round-trip 保持
            if (res.isRecordUnknownFeature()) {
                res.addUnknownContent(obj, nodeToXmlString(node));
            }
            return;
        }
        // ExtendedMetaData kind=attribute 的 feature 只从属性读取（对齐 Java XMLLoadImpl）。
        // 但 containment 引用必须从子元素构建（树结构），不受 kind=attribute 影响。
        if (featureKindAnnotation(sf) == 'a') {
            auto* refCheck = dynamic_cast<emf::ecore::EReference*>(sf);
            if (!refCheck || !refCheck->isContainment()) return;
        }

        // containment EReference：递归构建子 EObject 并挂到父对象
        // 对齐 Java SAXXMIHandler.handleObjectAttribValue + createEObject
        if (auto* ref = dynamic_cast<emf::ecore::EReference*>(sf)) {
            if (ref->isContainment()) {
                // 用 buildInstanceObject 构建（支持 xsi:type 子类型，对齐 Java）
                // 传递继承的命名空间表（对齐 Java SAX 命名空间继承）
                auto* childObj = buildInstanceObject(node, inheritedNs);
                if (!childObj) {
                    // 回退：用声明类型构建（node.local 是 feature 名而非 class 名，
                    // buildInstanceObject 找不到 EClassifier 时走此分支）
                    auto* childClass = ref->getEReferenceType();
                    if (!childClass) return;
                    auto* factory = pkg ? pkg->getEFactoryInstance() : nullptr;
                    if (!factory) return;
                    childObj = factory->create(childClass);
                    if (!childObj) return;
                    // 注册 xmi:id（对齐 buildInstanceObject 的 registerXmiId 调用，
                    // 否则 <writers xmi:id="w1"> 的 id 不会进 idMap，导致 //w1 引用解析失败）
                    registerXmiId(node, childObj);
                    applyInstanceAttrs(childObj, childClass, node, pkg);
                    // 子节点继续传递命名空间表
                    NsMap childNs = collectAndMergeNs(node, inheritedNs);
                    for (pugi::xml_node gc : node.children()) {
                        if (gc.type() != pugi::node_element) continue;
                        applyInstanceChild(childObj, childClass, gc, pkg, childNs);
                    }
                }
                // 挂到父对象：多值 push_back，单值 eSet
                if (sf->isMany()) {
                    appendToMultiValue(obj, sf, childObj);
                } else {
                    emf::common::EObject* tmp = childObj;
                    obj->eSet(sf, std::any(tmp));
                }
                return;
            }
            // 非 containment EReference：检查 href 属性（Java 风格 <feat href="..."/>）
            {
                std::string href = node.attribute("href").value();
                if (!href.empty()) {
                    // 延迟到 resolvePending 解析（跨引用）
                    pendingRefs.push_back({obj, sf->getName(), href, pkg, sf, sf->isMany()});
                    return;
                }
            }
            return;
        }

        // EAttribute：子元素文本作为值
        std::string text = getNodeText(node);
        // 去掉首尾空白
        while (!text.empty() && std::isspace((unsigned char)text.back())) text.pop_back();
        size_t s = 0;
        while (s < text.size() && std::isspace((unsigned char)text[s])) ++s;
        if (s > 0) text = text.substr(s);
        if (!text.empty()) {
            setFeatureValue(obj, sf, text, pkg);
        }
    }

    void setFeatureValue(emf::common::EObject* obj, emf::ecore::EStructuralFeature* sf,
                         const std::string& raw, emf::ecore::EPackage* pkg) {
        // EAttribute：字符串/数值
        auto* attr = dynamic_cast<emf::ecore::EAttribute*>(sf);
        if (attr) {
            auto* dt = attr->getEAttributeType();
            if (!dt) {
                // 无类型信息时按字符串处理
                obj->eSet(sf, std::any(raw));
                return;
            }
            auto* factory = pkg ? pkg->getEFactoryInstance() : nullptr;
            if (factory) {
                try {
                    std::any v = factory->createFromString(dt, raw);
                    obj->eSet(sf, v);
                    return;
                } catch (...) {}
            }
            obj->eSet(sf, std::any(raw));
            return;
        }
        // EReference：跨引用属性形式（如 publisher="//@publishers.1"），延迟解析
        // 对齐 Java XMLHelperImpl.setReferenceValue
        if (auto* ref = dynamic_cast<emf::ecore::EReference*>(sf)) {
            // 多值 reference：raw 可能是空格分隔的多个 href（//@x.0 //@x.1）
            if (ref->isMany()) {
                std::istringstream iss(raw);
                std::string token;
                while (iss >> token) {
                    pendingRefs.push_back({obj, sf->getName(), token, pkg, sf, true});
                }
            } else {
                pendingRefs.push_back({obj, sf->getName(), raw, pkg, sf, false});
            }
        }
    }

    // ===== 主入口 =====
    void load(const std::string& xml) {
        XmlParser parser(xml);
        pugi::xml_node root = parser.parse();
        // parser 必须在 root 使用期间保持存活（持有 pugi::xml_document）；
        // 它是本函数的局部变量，覆盖整个 load 过程。

        // 从 XML 声明提取 encoding 并设置到 resource（对齐 Java XMLResourceImpl：跟随输入文件）
        const std::string& enc = parser.getEncoding();
        if (!enc.empty()) {
            res.setEncoding(enc);
        }

        // 记录 XMI 版本
        for (pugi::xml_attribute a : root.attributes()) {
            const char* aname = a.name();
            if (std::strcmp(aname, "xmi:version") == 0 || std::strcmp(aname, "xmlns:xmi") == 0) {
                if (std::strstr(a.value(), "2.0") != nullptr) {
                    res.setXmiVersion("2.0");
                }
            }
        }

        // 判断文档形态
        std::string rootLocal = getNodeLocal(root);
        std::string rootPrefix = getNodePrefix(root);
        std::string ecoreNsAttr = root.attribute("xmlns:ecore").value();
        bool isEcoreDoc = (rootLocal == "EPackage" &&
                          (rootPrefix == "ecore" || ecoreNsAttr == kEcoreNsURI));
        bool isXmiWrapper = (rootLocal == "XMI" && rootPrefix == "xmi");

        if (isXmiWrapper) {
            // <xmi:XMI> 包裹多个实例根
            // 收集根元素的 xmlns 声明，向子元素传递（对齐 Java SAX 命名空间继承）
            NsMap rootNs = collectAndMergeNs(root, {});
            for (pugi::xml_node child : root.children()) {
                if (child.type() != pugi::node_element) continue;
                auto* obj = buildInstanceObject(child, rootNs);
                if (obj) res.addToContents(obj);
            }
            resolvePending();
            return;
        }

        if (isEcoreDoc) {
            auto* pkg = buildEPackage(root);
            if (pkg) {
                res.addToContents(pkg);
                // 注册到 EPackageRegistry（便于后续 codegen / 实例加载）
                if (!pkg->getNsURI().empty()) {
                    emf::common::EPackageRegistry::instance().put(pkg->getNsURI(), pkg);
                }
            }
            resolvePending();
            return;
        }

        // 默认按实例文档处理
        auto* obj = buildInstanceObject(root);
        if (obj) res.addToContents(obj);
        resolvePending();
    }
};

}  // namespace

// ===== 公开入口（XMLLoadImpl / XMIResource 通过此函数委托）=====
void loadInto(std::istream& is, XMIResource& res, const XMIOptions& opts) {
    // 优化：直接读取到 std::string，避免 stringstream 中间层双重复制
    // （原先 stringstream + str() 会多一次拷贝，对 96MB 文件影响显著）
    std::string xml;
    constexpr size_t kChunk = 1 << 20;  // 1MB chunks
    char buf[kChunk];
    while (true) {
        is.read(buf, kChunk);
        xml.append(buf, static_cast<size_t>(is.gcount()));
        if (is.gcount() < static_cast<std::streamsize>(kChunk)) break;
    }
    Loader loader(res, opts);
    // 同步 recordUnknownFeature 标志到 resource，供 applyInstanceChild/Attrs 检查
    res.setRecordUnknownFeature(opts.recordUnknownFeature);
    loader.load(xml);
}

}  // namespace emf::xmi
