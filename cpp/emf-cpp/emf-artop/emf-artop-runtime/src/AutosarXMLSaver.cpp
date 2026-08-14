// emf::artop::runtime —— AutosarXMLSaver 实现
// 对齐 Java: org.artop.aal.common.resource.impl.AutosarXMLSaveImpl
//
// arxml 序列化核心实现：
//   1. 用 PugiDomWriter（pugixml DOM 中间表示 + 自定义遍历输出）构建 XML，
//      接口与原 XmlStreamWriter 兼容，业务层无感知
//   2. 根元素 <AUTOSAR xmlns="http://autosar.org/schema/r4.0" ...>
//   3. APRXML 规则 0012/0015/0016/默认 决定 containment 引用的包装方式
//   4. 非 containment 引用写成 <FEATURE DEST="TypeXmlName">short-name-path</FEATURE>
//   5. short name path 沿 eContainer 链收集 shortName，以 "/" 拼接
//   6. EAttribute 按 isXmlAttribute 决定写为 XML 属性或子元素
//   7. 无序列表按 short name 字典序排序
#include "emf/artop/runtime/AutosarXMLSaver.h"
#include <cstdint>

#include "emf/artop/runtime/AutosarResource.h"
#include "emf/artop/runtime/IdentifiableUtil.h"
#include "emf/common/EList.h"
#include "emf/common/EPackageRegistry.h"
#include "emf/ecore/codegen/EAnnotationReader.h"
#include "emf/ecore/EcorePackage.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/xmi/XMIResource.h"
#include "emf/xmi/XMIHelper.h"
#include "pugixml.hpp"

#include <algorithm>
#include <any>
#include <cstdio>
#include <cstdlib>
#include <iostream>
#include <set>
#include <sstream>
#include <string>
#include <typeinfo>
#include <unordered_map>
#include <utility>
#include <vector>

namespace emf::artop::runtime {

// Defined in AutosarXMLLoader.cpp — 全局 mixed text 存储（loader 写入、saver 读取）
// 对齐 Java EMF mixed FeatureMap TEXT 条目：C++ codegen 未生成 "mixed" feature，
// 改用全局 map 存储以避免修改 EObjectImpl 布局（ABI 兼容）。
std::unordered_map<emf::common::EObject*, std::string>& mixedTextStore();

// Defined in AutosarXMLLoader.cpp — 全局注释存储（loader 写入、saver 读取）
// 对齐 Java EMF eObjectToExtensionMap + AnyType.mixed 的 COMMENT 条目。
std::unordered_map<emf::common::EObject*, std::vector<std::string>>& commentStore();

// Defined in AutosarXMLLoader.cpp — mixed content 完整序列存储。
// 对齐 Java EMF mixed FeatureMap：保留 TEXT/COMMENT/ELEMENT 的原始顺序和空白。
struct MixedContentEntry {
    enum Kind { kText, kComment, kElement } kind;
    std::string text;
    emf::common::EObject* child;
};
std::unordered_map<emf::common::EObject*, std::vector<MixedContentEntry>>& mixedContentStore();

// Defined in AutosarXMLLoader.cpp — xml.name fallback 注册表。
// 注解缺失的 EClass 通过实例化 eXmlName() 获取正确的 xml.name。
struct XmlNameFallback {
    std::unordered_map<emf::ecore::EClass*, std::string> classToName;
    std::unordered_map<std::string, emf::ecore::EClass*> nameToClass;
    bool built = false;
};
XmlNameFallback& xmlNameFallback();
void buildXmlNameFallback(emf::ecore::EPackage* root);

// Defined in AutosarXMLLoader.cpp — REF 原始 DEST 存储（按 owner,ref,target 三元组）。
// 对齐 Java：REF 元素的 DEST 属性保留原始值（可能是抽象基类，如 ECUC-DEFINITION-ELEMENT），
// 不用目标对象的实际 eClass() xmlName 重新计算。Saver 输出 DEST 时优先查询此 map。
std::unordered_map<std::string, std::string>& refDestStore();
std::string refDestKey(emf::common::EObject* owner, emf::ecore::EStructuralFeature* ref,
                       emf::common::EObject* target);

// Defined in AutosarXMLLoader.cpp — REF isDefault 存储（按 target/proxy → bool）。
// 对齐 Java RelativeReference.isDefault：isDefault=true 时 Saver 不输出 BASE 属性。
std::unordered_map<emf::common::EObject*, bool>& refIsDefaultStore();

namespace {

// ===== APRXML 规则常量（对齐 Java AutosarTaggedValues / AutosarPersistenceRules）=====
// 由 EAnnotation 的 roleElement/roleWrapperElement/typeElement/typeWrapperElement 标志推导
constexpr int kAprxml0012 = 12;  // role+type 合一元素（upperBound<=1）
constexpr int kAprxml0015 = 15;  // 纯 type 元素（无 role）
constexpr int kAprxml0016 = 16;  // 普通 containment（四个标志全 false，跳过 wrapper）
constexpr int kAprxmlDefault = 0;  // 默认：标准 EMF + role-wrapper 包裹

// AUTOSAR R4.0 默认命名空间与 schema
constexpr const char* kAutosarNsURI = "http://autosar.org/schema/r4.0";
constexpr const char* kXsiNsURI = "http://www.w3.org/2001/XMLSchema-instance";
constexpr const char* kDefaultSchemaLocation =
    "http://autosar.org/schema/r4.0 AUTOSAR_00048.xsd";

// 旧版本 NsURI 前缀（2.0.0 不写 DEST 属性）
constexpr const char* kAutosar20NsURIPrefix = "http://autosar.org/2.0.0";

// ===== 辅助：从 std::any 提取 EObject 列表（对齐 XMISaver.cpp::extractList）=====
std::vector<emf::common::EObject*> extractObjectList(const std::any& v) {
    std::vector<emf::common::EObject*> r;
    if (!v.has_value()) return r;
    // 单值 reference：直接是 EObject*
    if (v.type() == typeid(emf::common::EObject*)) {
        auto* t = std::any_cast<emf::common::EObject*>(v);
        if (t) r.push_back(t);
        return r;
    }
    // 多值 reference：EObjectRefView（零拷贝视图，fast-path）
    if (v.type() == typeid(emf::common::EObjectRefView)) {
        auto view = std::any_cast<emf::common::EObjectRefView>(v);
        r.reserve(view.size());
        for (auto* p : view) r.push_back(p);
        return r;
    }
    // 多值 reference：vector<EObject*> 或 vector<EObject*>*
    if (v.type() == typeid(std::vector<emf::common::EObject*>)) {
        return std::any_cast<std::vector<emf::common::EObject*>>(v);
    }
    if (v.type() == typeid(std::vector<emf::common::EObject*>*)) {
        auto* p = std::any_cast<std::vector<emf::common::EObject*>*>(v);
        if (p) return *p;
    }
    if (v.type() == typeid(emf::common::EList<emf::common::EObject*>*)) {
        auto* p = std::any_cast<emf::common::EList<emf::common::EObject*>*>(v);
        if (p) for (size_t i = 0; i < p->size(); ++i) r.push_back((*p)[i]);
    }
    return r;
}

// ===== 辅助：从 std::any 提取 EObjectRefView（零拷贝，不转 vector）=====
// 用于 saveContainment/saveReference fast-path，避免 vector 拷贝。
// 返回空 view 表示不是 EObjectRefView 类型（调用方 fallback 到 extractObjectList）。
emf::common::EObjectRefView extractRefView(const std::any& v) {
    if (!v.has_value()) return {};
    if (v.type() == typeid(emf::common::EObjectRefView)) {
        return std::any_cast<emf::common::EObjectRefView>(v);
    }
    return {};
}

// ===== 辅助：从 std::any 提取字符串列表（多值 EAttribute）=====
std::vector<std::string> extractStringList(const std::any& v) {
    std::vector<std::string> r;
    if (!v.has_value()) return r;
    if (v.type() == typeid(std::string)) {
        r.push_back(std::any_cast<std::string>(v));
        return r;
    }
    if (v.type() == typeid(std::vector<std::string>)) {
        return std::any_cast<std::vector<std::string>>(v);
    }
    if (v.type() == typeid(std::vector<std::string>*)) {
        auto* p = std::any_cast<std::vector<std::string>*>(v);
        if (p) return *p;
    }
    // 多值 EAttribute 的 eGet 返回 EList<std::string>*（内部列表）
    if (v.type() == typeid(emf::common::EList<std::string>*)) {
        auto* elist = std::any_cast<emf::common::EList<std::string>*>(v);
        if (elist) {
            for (size_t i = 0; i < elist->size(); ++i) r.push_back((*elist)[i]);
        }
    }
    return r;
}

// ===== 辅助：从 std::any 提取整型值（用于 enum）=====
int extractIntValue(const std::any& v) {
    if (!v.has_value()) return 0;
    if (v.type() == typeid(int)) return std::any_cast<int>(v);
    if (v.type() == typeid(int32_t)) return std::any_cast<int32_t>(v);
    if (v.type() == typeid(int64_t)) return static_cast<int>(std::any_cast<int64_t>(v));
    if (v.type() == typeid(long)) return static_cast<int>(std::any_cast<long>(v));
    return 0;
}

// ===== 辅助：去掉首尾空白 =====
std::string trim(const std::string& s) {
    size_t b = 0;
    while (b < s.size() && std::isspace((unsigned char)s[b])) ++b;
    size_t e = s.size();
    while (e > b && std::isspace((unsigned char)s[e - 1])) --e;
    return s.substr(b, e - b);
}

// 查找 EClass 的 simple content feature（对齐 Java ExtendedMetaData.getSimpleFeature）。
// 仅当 EClass 的 contentKind=="simple" 时，遍历其 features 返回第一个
// featureKind=="simple" 的 EAttribute（即 ExtendedMetaData name=":0" 的 feature）。
// 该 feature 承载元素的文本内容（如 Tt.term、ForeignModelReference.ref）。
emf::ecore::EAttribute* findSimpleFeature(emf::ecore::EClass* cls) {
    if (!cls) return nullptr;
    for (auto* sf : cls->getEAllStructuralFeatures()) {
        if (!sf) continue;
        auto meta = emf::ecore::codegen::EAnnotationReader::readFeatureMeta(sf);
        if (meta.featureKind == "simple") {
            return dynamic_cast<emf::ecore::EAttribute*>(sf);
        }
    }
    return nullptr;
}

// StreamWriter：流式 XML writer，直接写入 ostream，不构建 pugixml DOM。
// 接口与原 PugiDomWriter 兼容（beginElement/endElement/writeAttribute/writeText/
// writeComment/setIndent），业务层（AutosarSaver）无需改动。
//
// 优化点 7：消除 pugixml DOM 中间表示。原 PugiDomWriter 先构建完整 DOM 树（doc_），
// 再遍历输出。400MB arxml 的 DOM 节点开销远超 400MB，导致 OOM。
// StreamWriter 用"延迟开标签"模式：beginElement 只压栈，首个子元素/文本到达时
// 才写开标签，endElement 时决定自闭合 <TAG/> 还是 </TAG>。
// 输出缓冲 buf_ 定期 flush（~64KB），峰值内存 O(栈深 + buf_) 而非 O(文件大小)。
//
// 编码与空元素格式与原实现完全一致（复用 encodeText/encodeAttributeValue），
// 保证 MD5 字节级 round-trip 不变。
class PugiDomWriter {
    std::string indent_ = "  ";
    // 输出缓冲：定期 flush 到 sink_，避免大文件（400m）时累积 ~400MB 触发 OOM。
    // buf_ 上限约 64KB（每处理一个元素前 flushIfLarge），sink_ 为最终输出流。
    std::string buf_;
    std::ostream* sink_ = nullptr;
    int depth_ = 0;

    // 延迟开标签帧：beginElement 压栈时不写 <TAG>，等首个子元素/文本到达时才写。
    struct Frame {
        std::string tag;
        std::string attrs;       // 累积的 " name=\"value\"" 属性串
        bool opened = false;     // 开标签 '>' 已写入 buf_
        bool hasElemChild = false;
        bool hasText = false;    // writeText 被调用过（即使空串），控制 <TAG></TAG> vs <TAG/>
    };
    std::vector<Frame> stack_;

    static void encodeText(const std::string& s, std::string& buf) {
        // < → &lt;, & → &amp;, " → &quot;, \r → &#xD;（> 不编码，对齐 Java EMF）
        size_t start = 0;
        for (size_t i = 0; i < s.size(); ++i) {
            char c = s[i];
            if (c == '<' || c == '&' || c == '"' || c == '\r') {
                if (i > start) buf.append(s.data() + start, i - start);
                switch (c) {
                    case '<': buf += "&lt;"; break;
                    case '&': buf += "&amp;"; break;
                    case '"': buf += "&quot;"; break;
                    case '\r': buf += "&#xD;"; break;
                }
                start = i + 1;
            }
        }
        if (s.size() > start) buf.append(s.data() + start, s.size() - start);
    }

    static void encodeAttributeValue(const std::string& s, std::string& buf) {
        // < → &lt;, & → &amp;, " → &quot;, \r → &#13;, \n → &#10;, \t → &#9;
        size_t start = 0;
        for (size_t i = 0; i < s.size(); ++i) {
            char c = s[i];
            if (c == '<' || c == '&' || c == '"' || c == '\r' ||
                c == '\n' || c == '\t') {
                if (i > start) buf.append(s.data() + start, i - start);
                switch (c) {
                    case '<': buf += "&lt;"; break;
                    case '&': buf += "&amp;"; break;
                    case '"': buf += "&quot;"; break;
                    case '\r': buf += "&#13;"; break;
                    case '\n': buf += "&#10;"; break;
                    case '\t': buf += "&#9;"; break;
                }
                start = i + 1;
            }
        }
        if (s.size() > start) buf.append(s.data() + start, s.size() - start);
    }

    void writeIndent() {
        for (int i = 0; i < depth_; ++i) buf_ += indent_;
    }

    // 把 buf_ 写入 sink_ 并清空，控制大文件 save 峰值内存
    void flushBuf() {
        if (sink_ && !buf_.empty()) {
            *sink_ << buf_;
            buf_.clear();
        }
    }
    void flushIfLarge() {
        if (buf_.size() > 65536) flushBuf();
    }

    // 延迟开标签：首个子元素/文本/注释到达时写出 <TAG attrs>
    void flushOpen() {
        if (!stack_.empty() && !stack_.back().opened) {
            buf_ += '<';
            buf_ += stack_.back().tag;
            buf_ += stack_.back().attrs;
            buf_ += '>';
            stack_.back().opened = true;
        }
    }

public:
    void setIndent(std::string ind) { indent_ = std::move(ind); }

    // 设置输出流并写 XML 声明（在 beginElement 之前调用）
    void setOutput(std::ostream& os, bool writeDecl, const std::string& enc) {
        sink_ = &os;
        buf_.clear();
        if (writeDecl) {
            buf_ += "<?xml version=\"1.0\" encoding=\"";
            buf_ += enc;
            buf_ += "\"?>\n";
        }
    }

    void beginElement(const std::string& tag) {
        flushIfLarge();
        // 父帧若有延迟开标签，现在 flush（父帧将获得元素子）
        if (!stack_.empty()) {
            flushOpen();
            stack_.back().hasElemChild = true;
            // 非文本父帧：元素子前加 \n+indent
            if (!stack_.back().hasText) {
                buf_ += '\n';
                writeIndent();
            }
        }
        Frame f;
        f.tag = tag;
        stack_.push_back(std::move(f));
        ++depth_;
    }

    void writeAttribute(const std::string& name, const std::string& value) {
        if (!stack_.empty() && !stack_.back().opened) {
            std::string& attrs = stack_.back().attrs;
            attrs += ' ';
            attrs += name;
            attrs += "=\"";
            encodeAttributeValue(value, attrs);
            attrs += '"';
        }
    }

    void writeText(const std::string& text) {
        if (!stack_.empty()) {
            // 即使空字符串也标记 hasText，使元素输出 <TAG></TAG>（非自闭合）
            flushOpen();
            stack_.back().hasText = true;
            if (!text.empty()) {
                encodeText(text, buf_);
            }
        }
    }

    void writeComment(const std::string& text) {
        if (!stack_.empty()) {
            flushOpen();
            buf_ += "<!--";
            buf_ += text;
            buf_ += "-->";
        }
    }

    void endElement() {
        flushIfLarge();
        if (stack_.empty()) return;
        --depth_;
        Frame& f = stack_.back();
        if (!f.opened) {
            // 无子元素/文本/注释 → 自闭合 <TAG attrs/>
            buf_ += '<';
            buf_ += f.tag;
            buf_ += f.attrs;
            buf_ += "/>";
        } else if (f.hasElemChild && !f.hasText) {
            // 有元素子且无文本 → \n+indent+</TAG>
            buf_ += '\n';
            writeIndent();
            buf_ += "</";
            buf_ += f.tag;
            buf_ += '>';
        } else {
            // 仅有文本/注释（或有文本+元素 mixed）→ inline </TAG>
            buf_ += "</";
            buf_ += f.tag;
            buf_ += '>';
        }
        stack_.pop_back();
    }

    // 最终 flush（在所有 endElement 之后调用）
    void finishStream() {
        flushBuf();
        sink_ = nullptr;
    }
};


// ===== AutosarSaver：实际序列化实现（对齐 Java AutosarXMLSaveImpl）=====
struct AutosarSaver {
    const emf::xmi::XMIResource& res;
    const emf::xmi::XMIOptions& opts;
    PugiDomWriter writer_;

    // short name path 缓存（避免对同一对象重复沿 eContainer 链遍历）
    std::unordered_map<emf::common::EObject*, std::string> snpCache_;

    // ===== 性能优化缓存（对齐 Java EMF 的 Lookup 缓存机制）=====
    // 这些元数据在单次 save 中不变，缓存避免对同一 EClass/EStructuralFeature
    // 反复调用 EAnnotationReader::readFeatureMeta（注解解析开销大）。
    std::unordered_map<emf::ecore::EStructuralFeature*,
                       emf::ecore::codegen::FeatureMeta> featureMetaCache_;
    std::unordered_map<emf::ecore::EClass*, std::string> typeXmlNameCache_;
    std::unordered_map<emf::ecore::EClass*,
                       std::vector<emf::ecore::EStructuralFeature*>> sortedFeaturesCache_;
    std::unordered_map<emf::ecore::EClass*, emf::ecore::EAttribute*> simpleFeatureCache_;
    std::unordered_map<emf::common::EObject*, std::string> shortNameCache_;

    // 缓存版的 readFeatureMeta（首次调用走 EAnnotationReader，后续命中缓存）
    const emf::ecore::codegen::FeatureMeta& cachedFeatureMeta(
            emf::ecore::EStructuralFeature* sf) {
        auto it = featureMetaCache_.find(sf);
        if (it != featureMetaCache_.end()) return it->second;
        auto meta = emf::ecore::codegen::EAnnotationReader::readFeatureMeta(sf);
        return featureMetaCache_[sf] = std::move(meta);
    }

    // 缓存版的 getTypeXmlName
    const std::string& cachedTypeXmlName(emf::ecore::EClass* cls) {
        auto it = typeXmlNameCache_.find(cls);
        if (it != typeXmlNameCache_.end()) return it->second;
        std::string name = getTypeXmlNameUncached(cls);
        return typeXmlNameCache_[cls] = std::move(name);
    }

    // 缓存版的 collectSortedFeatures
    const std::vector<emf::ecore::EStructuralFeature*>& cachedSortedFeatures(
            emf::ecore::EClass* cls) {
        auto it = sortedFeaturesCache_.find(cls);
        if (it != sortedFeaturesCache_.end()) return it->second;
        sortedFeaturesCache_[cls] = collectSortedFeaturesUncached(cls);
        return sortedFeaturesCache_[cls];
    }

    // 缓存版的 findSimpleFeature
    emf::ecore::EAttribute* cachedSimpleFeature(emf::ecore::EClass* cls) {
        auto it = simpleFeatureCache_.find(cls);
        if (it != simpleFeatureCache_.end()) return it->second;
        auto* sf = findSimpleFeature(cls);
        simpleFeatureCache_[cls] = sf;
        return sf;
    }

    // ===== 方案 B 子集：类型化 eGet 快速路径缓存 =====
    // ScalarKind：单值 attribute/reference 的类型种类，决定走哪个类型化 eGet。
    // 由 EAttribute 的 EDataType 元数据判定（与子类无关，按 feature* 缓存）。
    enum class ScalarKind { None, String, Int64, Bool, EObjectRef, Double };
    std::unordered_map<emf::ecore::EStructuralFeature*, ScalarKind> scalarKindCache_;
    // featureID 缓存：按 (eClass*, feature*) 缓存 eFeatureID 结果。
    // featureID 是跨继承链累积索引，同一 feature 指针在不同子类里 ID 不同，故需 eClass 维度。
    // key 用 EClass* + feature* 组合（EClass* 是单例，feature* 是 eINSTANCE 单例）。
    struct FeatureIDKey {
        emf::ecore::EClass* cls;
        emf::ecore::EStructuralFeature* sf;
        bool operator==(const FeatureIDKey& o) const { return cls == o.cls && sf == o.sf; }
    };
    struct FeatureIDKeyHash {
        size_t operator()(const FeatureIDKey& k) const {
            return reinterpret_cast<size_t>(k.cls) ^ (reinterpret_cast<size_t>(k.sf) << 16);
        }
    };
    std::unordered_map<FeatureIDKey, int, FeatureIDKeyHash> featureIDCache_;

    // 判定单值 attribute 的 ScalarKind（由 EDataType 元数据决定）。
    // enum → String（codegen 存为 std::string）；其他按 instanceClassName 映射。
    ScalarKind classifyAttribute(emf::ecore::EAttribute* attr) {
        auto it = scalarKindCache_.find(attr);
        if (it != scalarKindCache_.end()) return it->second;
        ScalarKind kind = ScalarKind::None;
        if (!attr->isMany()) {
            auto* dt = attr->getEAttributeType();
            if (dt) {
                if (dynamic_cast<emf::ecore::EEnum*>(dt)) {
                    kind = ScalarKind::String;  // enum 存为 std::string
                } else {
                    const std::string& icn = dt->getInstanceClassName();
                    if (icn == "java.lang.String" || icn.empty()) kind = ScalarKind::String;
                    else if (icn == "java.lang.Boolean") kind = ScalarKind::Bool;
                    else if (icn == "java.lang.Integer" || icn == "java.lang.Long") kind = ScalarKind::Int64;
                    else if (icn == "java.lang.Double") kind = ScalarKind::Double;
                    // java.lang.Float 不走快速路径（float→double 精度差异破坏 round-trip），fallback 到 std::any
                    else kind = ScalarKind::String;  // 未知类型默认 string（保守）
                }
            }
        }
        scalarKindCache_[attr] = kind;
        return kind;
    }

    // 判定单值 reference 的 ScalarKind
    ScalarKind classifyReference(emf::ecore::EReference* ref) {
        auto it = scalarKindCache_.find(ref);
        if (it != scalarKindCache_.end()) return it->second;
        ScalarKind kind = (!ref->isMany()) ? ScalarKind::EObjectRef : ScalarKind::None;
        scalarKindCache_[ref] = kind;
        return kind;
    }

    // 缓存版的 eFeatureID：按 (eClass*, feature*) 缓存 eFeatureID(name) 结果。
    // featureID 是跨继承链累积索引，同一 feature 指针在不同子类里 ID 不同，故需 eClass 维度。
    // 返回 -1 表示未找到（调用方应 fallback）。
    int cachedFeatureID(emf::common::EObject* obj, emf::ecore::EStructuralFeature* sf) {
        auto* cls = obj->eClass();
        FeatureIDKey key{cls, sf};
        auto it = featureIDCache_.find(key);
        if (it != featureIDCache_.end()) return it->second;
        int id = obj->eFeatureID(sf->getName());
        featureIDCache_[key] = id;
        return id;
    }


    // 缓存版的 readShortName
    const std::string* cachedShortNamePtr(emf::common::EObject* obj) {
        auto it = shortNameCache_.find(obj);
        if (it != shortNameCache_.end()) return &it->second;
        std::string sn = readShortNameUncached(obj);
        auto [insIt, _] = shortNameCache_.emplace(obj, std::move(sn));
        return &insIt->second;
    }

    AutosarSaver(const emf::xmi::XMIResource& r, const emf::xmi::XMIOptions& o)
        : res(r), opts(o) {
        // 确保 xml.name fallback 注册表已构建（Loader 通常已构建，此处兜底）
        if (!xmlNameFallback().built) {
            auto* pkg = dynamic_cast<emf::ecore::EPackage*>(
                emf::common::EPackageRegistry::instance().get(kAutosarNsURI));
            buildXmlNameFallback(pkg);
        }
    }

    // ===== pugixml 节点直接写入 writer（用于 unknown contents 片段）=====
    void writePugiNode(pugi::xml_node node) {
        switch (node.type()) {
            case pugi::node_element: {
                writer_.beginElement(node.name());
                for (auto attr = node.first_attribute(); attr; attr = attr.next_attribute()) {
                    writer_.writeAttribute(attr.name(), attr.value());
                }
                for (auto child = node.first_child(); child; child = child.next_sibling()) {
                    writePugiNode(child);
                }
                writer_.endElement();
                break;
            }
            case pugi::node_pcdata:
            case pugi::node_cdata:
                writer_.writeText(node.value());
                break;
            case pugi::node_comment:
                writer_.writeComment(node.value());
                break;
            default:
                break;
        }
    }

    // ===== 主入口 =====
    void save(std::ostream& output) {
        std::string indent = !opts.indent.empty() ? opts.indent : std::string("  ");
        writer_.setIndent(indent);

        // XML 声明 encoding
        std::string enc = !opts.encoding.empty() ? opts.encoding
            : (res.getEncoding().empty() ? std::string("UTF-8") : res.getEncoding());

        // 流式输出：设置输出流并写 XML 声明（优化点 7：消除 DOM，直接流式写入）
        writer_.setOutput(output, opts.xmlDeclaration, enc);

        // 根元素 <AUTOSAR xmlns="..." xmlns:xsi="..." xsi:schemaLocation="...">
        writer_.beginElement("AUTOSAR");
        writer_.writeAttribute("xmlns", kAutosarNsURI);
        writer_.writeAttribute("xmlns:xsi", kXsiNsURI);

        // xsi:schemaLocation：优先从 AutosarResource 读取，否则用默认值
        std::string schemaLocation;
        auto* ares = dynamic_cast<const AutosarResource*>(&res);
        if (ares) {
            schemaLocation = ares->getSchemaLocation();
        }
        if (schemaLocation.empty()) {
            // 回退到 XMIResource 的 xsiSchemaLocation
            schemaLocation = res.getXSISchemaLocation();
        }
        if (schemaLocation.empty()) {
            schemaLocation = kDefaultSchemaLocation;
        }
        writer_.writeAttribute("xsi:schemaLocation", schemaLocation);

        // 序列化 resource 的顶层内容对象到根元素下
        // 对齐 Java AutosarXMLSaveImpl.traverse：contents 通常是单个 AUTOSAR 实例
        auto& contents = const_cast<emf::xmi::XMIResource&>(res).getContents();
        for (auto* obj : contents) {
            if (obj) saveObjectContent(obj);
        }

        writer_.endElement();  // </AUTOSAR>

        // flush 残余缓冲
        writer_.finishStream();
        output << '\n';  // 对齐 Java EMF：根元素后追加换行符
    }

    // ===== 序列化对象内容（feature 列表）直接写入 writer =====
    // 对齐 Java XMLPersistenceMappingSaveImpl.saveFeatures(o, attributesAndElements, false)
    // elementsOnly=true 时对齐 saveFeatures(o, elementsOnly, true)（0016 strategy 0000 调用）
    void saveObjectContent(emf::common::EObject* obj, bool elementsOnly = false) {
        if (!obj) return;
        auto* cls = obj->eClass();
        if (!cls) return;

        // 收集所有 feature（EMF 标准顺序，不按 sequenceOffset 排序）— 走缓存
        const auto& sortedFeatures = cachedSortedFeatures(cls);
        // 使用 obj->eContentKind() 而非 readClassMeta(cls).contentKind：
        // 生成的 eStaticContentKind() 直接硬编码（来自 ecore ExtendedMetaData），
        // 比 readClassMeta 从运行时 EAnnotation 读取更可靠。
        bool isMixedContent = (obj->eContentKind() == "mixed");
        bool isSimpleContent = (obj->eContentKind() == "simple");

        // ===== Simple content（对齐 Java XMLSaveImpl SIMPLE_CONTENT 路径）=====
        // contentKind=="simple" 的 EClass（如 Tt、ForeignModelReference）：
        //   Java 在 saveFeatures 中对 SIMPLE_CONTENT 设 doc.setMixed(true)，
        //   :0 feature (featureKind==SIMPLE_FEATURE) 映射为 DATATYPE_ELEMENT_SINGLE，
        //   遍历到该 feature 时因 contentKind==SIMPLE_CONTENT 调用
        //   getDataTypeElementSingleSimple(o, f) 取字符串值，
        //   endSaveFeatures(o, CONTENT_ELEMENT, content) 把字符串写成元素文本。
        // C++ 端：先输出 XML 属性，再把 :0 feature 值作为元素文本输出。
        if (isSimpleContent) {
            // 1. 输出 XML 属性（elementsOnly 模式跳过）
            if (!elementsOnly) {
                for (auto* sf : sortedFeatures) {
                    if (!sf) continue;
                    if (!obj->eIsSet(sf)) continue;
                    if (auto* attr = dynamic_cast<emf::ecore::EAttribute*>(sf)) {
                        auto attrMeta = cachedFeatureMeta(attr);
                        if (attrMeta.isXmlAttribute) {
                            saveAttribute(obj, attr);
                        }
                    }
                }
            }
            // 2. 输出 :0 feature 值作为元素文本
            auto* simpleAttr = cachedSimpleFeature(cls);
            if (simpleAttr && obj->eIsSet(simpleAttr)) {
                // 方案 B 子集：单值 attribute 优先走类型化快速路径
                std::string valStr;
                if (!tryGetAttrString(obj, simpleAttr, valStr)) {
                    std::any v = obj->eGet(simpleAttr);  // fallback：反射式 eGet
                    valStr = attrValueToString(simpleAttr, v);
                }
                if (!valStr.empty()) {
                    writer_.writeText(valStr);
                }
            }
            return;
        }

        // ===== Mixed content：从完整序列驱动输出 =====
        // 对齐 Java EMF XMLSaveImpl 遍历 mixed FeatureMap：
        //   TEXT → doc.addText(stringValue)（保留原始空白，含 TAB 缩进）
        //   COMMENT → doc.addComment(stringValue)
        //   ELEMENT → saveElement(feature, value)
        // 不依赖 pugixml 重新格式化，原样保留原始空白和顺序。
        if (isMixedContent) {
            auto& mcStore = mixedContentStore();
            auto mcIt = mcStore.find(obj);
            bool hasMixedSequence = (mcIt != mcStore.end());

            if (hasMixedSequence) {
                // 1. 输出 XML 属性（elementsOnly 模式跳过）
                if (!elementsOnly) {
                    for (auto* sf : sortedFeatures) {
                        if (!sf) continue;
                        if (sf->getName() == "mixed") continue;
                        if (!obj->eIsSet(sf)) continue;
                        if (auto* attr = dynamic_cast<emf::ecore::EAttribute*>(sf)) {
                            auto attrMeta = cachedFeatureMeta(attr);
                            if (attrMeta.isXmlAttribute) {
                                saveAttribute(obj, attr);
                            }
                        }
                    }
                }

                // 2. 从 mixed content 序列驱动子节点输出
                // 对齐 Java EMF XMLSaveImpl 遍历 mixed FeatureMap：
                //   - 非 wrapper feature（如 TT）：每个 ELEMENT 条目单独输出，保留交错顺序
                //   - wrapper feature（如 arPackages）：首次出现时输出 wrapper+全部子对象，
                //     后续出现跳过（已在 wrapper 内）。对齐 Java AR-PACKAGES 包裹行为。
                std::set<const emf::ecore::EReference*> wrapperRefsDone;
                // 单值 containment/reference feature 去重：对齐 Java EMF mixed FeatureMap 一致性。
                // Java FeatureMap 在单值 feature 被 eSet 覆盖时自动移除旧条目，只保留当前值。
                // C++ mixedContentStore 未移除旧条目，这里首次出现时用当前模型值输出，后续跳过。
                std::set<const emf::ecore::EReference*> singleValuedRefsDone;
                for (const auto& entry : mcIt->second) {
                    if (entry.kind == MixedContentEntry::kText) {
                        writer_.writeText(entry.text);
                    } else if (entry.kind == MixedContentEntry::kComment) {
                        writer_.writeComment(entry.text);
                    } else if (entry.kind == MixedContentEntry::kElement && entry.child) {
                        auto* feature = entry.child->eContainingFeature();
                        if (!feature) continue;
                        auto* ref = dynamic_cast<const emf::ecore::EReference*>(feature);
                        if (!ref) continue;
                        auto* refMut = const_cast<emf::ecore::EReference*>(ref);
                        auto meta = cachedFeatureMeta(refMut);
                        // wrapper feature：首次出现时用 saveContainment/saveReference 输出
                        // 全部子对象（含 wrapper），后续跳过
                        if (ref->isMany() && meta.isRoleWrapperElement
                            && !meta.xmlNamePlural.empty()
                            && meta.xmlNamePlural != meta.xmlName) {
                            if (wrapperRefsDone.find(ref) != wrapperRefsDone.end()) continue;
                            wrapperRefsDone.insert(ref);
                            if (ref->isContainment()) {
                                saveContainment(obj, refMut);
                            } else {
                                saveReference(obj, refMut);
                            }
                        } else if (!ref->isMany()) {
                            // 单值 feature：首次出现时用当前模型值输出（entry.child 可能是
                            // 已被 eSet 覆盖的旧对象），后续跳过。
                            // 对齐 Java mixed FeatureMap：覆盖时旧条目移除，只留当前值。
                            if (singleValuedRefsDone.find(ref) != singleValuedRefsDone.end()) continue;
                            singleValuedRefsDone.insert(ref);
                            // 方案 B 子集：优先用 eGetEObject 避免装箱
                            emf::common::EObject* curChild = nullptr;
                            if (!tryGetEObject(obj, refMut, curChild)) {
                                // fallback：反射式 eGet + std::any
                                std::any curV = obj->eGet(refMut);
                                if (curV.has_value()) {
                                    if (curV.type() == typeid(emf::common::EObject*)) {
                                        curChild = std::any_cast<emf::common::EObject*>(curV);
                                    } else if (curV.type() == typeid(emf::common::EList<emf::common::EObject*>*)) {
                                        auto* el = std::any_cast<emf::common::EList<emf::common::EObject*>*>(curV);
                                        if (el && el->size() > 0) curChild = (*el)[0];
                                        // 不 delete：生成类 eGet 返回内部 EList 指针（非拷贝），delete 会破坏对象内存
                                    }
                                }
                            }
                            if (curChild) {
                                saveSingleMixedElement(obj, refMut, curChild);
                            }
                        } else {
                            // 多值非 wrapper feature：按条目单独输出（保留交错顺序）
                            saveSingleMixedElement(obj, refMut, entry.child);
                        }
                    }
                }

                // 3. Fallback：输出序列中未覆盖的非 XML 属性型 EAttribute 子元素
                // （EAttribute 子元素在序列中 child=nullptr，不会被 ELEMENT 分支输出）
                for (auto* sf : sortedFeatures) {
                    if (!sf) continue;
                    if (sf->getName() == "mixed") continue;
                    if (!obj->eIsSet(sf)) continue;
                    if (auto* attr = dynamic_cast<emf::ecore::EAttribute*>(sf)) {
                        auto attrMeta = cachedFeatureMeta(attr);
                        if (elementsOnly && attrMeta.isXmlAttribute) continue;
                        if (!attrMeta.isXmlAttribute) {
                            saveAttribute(obj, attr);
                        }
                    }
                }
                return;
            }

            // 无序列的 mixed content：回退到原逻辑（mixedTextStore + commentStore）
            // 输出前置注释
            {
                auto& store = commentStore();
                auto it = store.find(obj);
                if (it != store.end()) {
                    for (const auto& c : it->second) {
                        writer_.writeComment(c);
                    }
                }
            }
            for (auto* sf : sortedFeatures) {
                if (!sf) continue;
                if (sf->getName() == "mixed") continue;
                if (!obj->eIsSet(sf)) continue;
                if (auto* attr = dynamic_cast<emf::ecore::EAttribute*>(sf)) {
                    if (elementsOnly) {
                        auto attrMeta = cachedFeatureMeta(attr);
                        if (attrMeta.isXmlAttribute) continue;
                    }
                    saveAttribute(obj, attr);
                } else if (auto* ref = dynamic_cast<emf::ecore::EReference*>(sf)) {
                    if (ref->isContainment()) {
                        saveContainment(obj, ref);
                    } else {
                        saveReference(obj, ref);
                    }
                }
            }
            // mixed text fallback
            {
                auto& store = mixedTextStore();
                auto it = store.find(obj);
                if (it != store.end()) {
                    const std::string& text = it->second;
                    if (!trim(text).empty()) {
                        writer_.writeText(text);
                    }
                }
            }
            return;
        }

        // ===== 非 mixed content：原有逻辑 =====
        // 对齐 Java：非 mixed content EClass 不输出注释（Java 只在 mixed content 时保留注释）。
        // commentStore 仅由 mixed content 路径（上方）输出。
        // 两遍遍历：先输出所有 isXmlAttribute 属性（必须在子元素之前，否则
        // beginElement 会先关闭父标签 '>'，导致属性被写到错误位置），再输出子元素。
        for (auto* sf : sortedFeatures) {
            if (!sf) continue;
            if (sf->getName() == "mixed") continue;
            if (!obj->eIsSet(sf)) continue;
            if (auto* attr = dynamic_cast<emf::ecore::EAttribute*>(sf)) {
                auto attrMeta = cachedFeatureMeta(attr);
                if (attrMeta.isXmlAttribute && !elementsOnly) {
                    saveAttribute(obj, attr);
                }
            }
        }
        for (auto* sf : sortedFeatures) {
            if (!sf) continue;
            if (sf->getName() == "mixed") continue;
            if (!obj->eIsSet(sf)) continue;

            if (auto* attr = dynamic_cast<emf::ecore::EAttribute*>(sf)) {
                auto attrMeta = cachedFeatureMeta(attr);
                if (attrMeta.isXmlAttribute) continue;  // 已在第一遍输出
                saveAttribute(obj, attr);
            } else if (auto* ref = dynamic_cast<emf::ecore::EReference*>(sf)) {
                if (ref->isContainment()) {
                    saveContainment(obj, ref);
                } else {
                    saveReference(obj, ref);
                }
            }
        }
        // 未知 XML 内容原样回写（对齐 Java EMF AnyType round-trip）。
        // AutosarXMLLoader 在 recordUnknownFeature_=true 时把无法映射的 XML 元素
        // 序列化为字符串存入 unknownContents_，saver 在所有 feature 输出完后原样追加。
        // 用 pugixml 解析片段后 writePugiNode 直接写入 writer，保持 XML 结构完整。
        if (!elementsOnly) {
            const auto& unknown = res.unknownContents();
            for (const auto& [owner, fragment] : unknown) {
                if (owner != obj) continue;
                if (fragment.empty()) continue;
                // 解析 XML 片段（可能含多个顶层节点）并写入 writer
                pugi::xml_document fragDoc;
                // 用包装根元素解析多片段
                std::string wrapped = "<__frag_root__>" + fragment + "</__frag_root__>";
                pugi::xml_parse_result pr = fragDoc.load_string(wrapped.c_str(),
                    pugi::parse_default | pugi::parse_ws_pcdata);
                if (!pr) continue;
                auto fragRoot = fragDoc.first_child();
                if (!fragRoot) continue;
                for (auto child = fragRoot.first_child(); child; child = child.next_sibling()) {
                    writePugiNode(child);
                }
            }
        }
    }

    // ===== 收集 feature 列表（对齐 Java EMF feature 顺序）=====
    // Java Sphinx/EMF 的 XMLPersistenceMappingSaveImpl.saveFeatures 使用
    // featureTable.getFeatures(eClass)，其顺序来自 Lookup.listFeatures：
    //   1. cls.getEAllStructuralFeatures() 的 EMF 标准顺序（继承层顺序 + 每层声明顺序）
    //   2. 元素型 feature（ELEMENT_FEATURE）后置
    // Java 的 ecore 声明顺序与 internal-xml-sequenceOffset 一致。
    // 但 C++ codegen 重排了 feature 声明顺序（如 Identifiable 中 category 在 desc 之前，
    // 而 ecore 中 desc 在 category 之前），导致 getEAllStructuralFeatures 顺序与 Java 不一致。
    // 修复：按 (继承层顺序, internal-xml-sequenceOffset) 两级排序：
    //   - 继承层：父类 feature 在子类之前（getEAllStructuralFeatures 已保证）
    //   - 同层内：按 sequenceOffset 排序（对齐 ecore 声明顺序）
    // 注意：部分 feature 的 internal-xml-sequenceOffset="null"（如 longName），
    //       parseInt 返回 0，不能用全局 sequenceOffset 排序（会跨越继承层），
    //       必须限制在同一 eContainingClass 内排序。
    std::vector<emf::ecore::EStructuralFeature*> collectSortedFeaturesUncached(
            emf::ecore::EClass* cls) {
        const auto& all = cls->getEAllStructuralFeatures();
        // 记录每个 eContainingClass 首次出现的索引（即继承层顺序）
        std::unordered_map<emf::ecore::EClass*, size_t> classFirstIdx;
        struct Entry {
            emf::ecore::EStructuralFeature* sf;
            int seqOffset;
            size_t origIdx;
            size_t classIdx;
        };
        std::vector<Entry> entries;
        entries.reserve(all.size());
        for (size_t i = 0; i < all.size(); ++i) {
            auto* sf = all[i];
            if (!sf) continue;
            auto* containingCls = sf->getEContainingClass();
            if (!classFirstIdx.count(containingCls)) {
                classFirstIdx[containingCls] = i;
            }
            auto meta = emf::ecore::codegen::EAnnotationReader::readFeatureMeta(sf);
            entries.push_back({sf, meta.sequenceOffset, i, classFirstIdx[containingCls]});
        }
        // 稳定排序：先按继承层（classIdx），再按 sequenceOffset，最后保持原始顺序
        std::stable_sort(entries.begin(), entries.end(),
            [](const Entry& a, const Entry& b) {
                if (a.classIdx != b.classIdx) return a.classIdx < b.classIdx;
                return a.seqOffset < b.seqOffset;
            });
        std::vector<emf::ecore::EStructuralFeature*> result;
        result.reserve(entries.size());
        for (auto& e : entries) {
            result.push_back(e.sf);
        }
        return result;
    }

    // ===== 方案 B 子集：单值 attribute 类型化快速取值 =====
    // 尝试用类型化 eGet（eGetString/eGetInt64/eGetBool）直接取字段值，避免 std::any 装箱。
    // 返回 true 表示命中（valStr 已赋值）；false 表示未命中，调用方 fallback 到 eGet+std::any。
    bool tryGetAttrString(emf::common::EObject* obj, emf::ecore::EAttribute* attr,
                          std::string& valStr) {
        ScalarKind kind = classifyAttribute(attr);
        if (kind == ScalarKind::None) return false;  // 多值/未知，fallback
        int fid = cachedFeatureID(obj, attr);
        if (fid < 0) return false;  // featureID 未找到，fallback
        if (kind == ScalarKind::String) {
            std::string s;
            if (!obj->eGetString(fid, s)) return false;
            // enum literal 规范化（与 attrValueToString 的 EEnum 分支一致）
            auto* dt = attr->getEAttributeType();
            if (auto* en = dynamic_cast<emf::ecore::EEnum*>(dt)) {
                for (auto* lit : en->getELiterals()) {
                    if (lit && (lit->getLiteral() == s || lit->getName() == s)) {
                        const std::string& litStr = lit->getLiteral();
                        valStr = !litStr.empty() ? litStr : lit->getName();
                        return true;
                    }
                }
                valStr = s;  // 未匹配，原样返回（保证 round-trip）
                return true;
            }
            valStr = s;
            return true;
        }
        if (kind == ScalarKind::Int64) {
            int64_t n;
            if (!obj->eGetInt64(fid, n)) return false;
            valStr = std::to_string(n);
            return true;
        }
        if (kind == ScalarKind::Bool) {
            bool b;
            if (!obj->eGetBool(fid, b)) return false;
            valStr = b ? "true" : "false";
            return true;
        }
        if (kind == ScalarKind::Double) {
            double d;
            if (!obj->eGetDouble(fid, d)) return false;
            valStr = std::to_string(d);
            return true;
        }
        return false;
    }

    // ===== 方案 B 子集：单值 reference 类型化快速取值 =====
    // 用 eGetEObject 直接取 EObject* 字段，避免 eGet(ref)+std::any 装箱 + extractObjectList 拆箱。
    // 返回 true 表示命中（out 已赋值，可能为 nullptr 表示空引用）；false 表示未命中，调用方 fallback。
    bool tryGetEObject(emf::common::EObject* obj, emf::ecore::EReference* ref,
                       emf::common::EObject*& out) {
        if (ref->isMany()) return false;  // 多值不走快速路径（已有 EObjectRefView 零拷贝）
        int fid = cachedFeatureID(obj, ref);
        if (fid < 0) return false;  // featureID 未找到，fallback
        return obj->eGetEObject(fid, out);  // 返回 true 表示命中
    }

    // ===== 方案 B 子集：通用 EStructuralFeature* 快速取值（用于辅助函数）=====
    // 与 tryGetAttrString/tryGetEObject 的区别：接收 EStructuralFeature*，内部 dynamic_cast 分派。
    // 供 readShortNameUncached / getSplitkeyValue / readReferenceBase* 等辅助函数使用，
    // 这些函数按名查找 feature 得到 EStructuralFeature*，无法直接调 EAttribute*/EReference* 版本。
    // 命中返回 true（out 已赋值）；未命中返回 false，调用方 fallback 到 eGet+std::any。
    bool tryGetStringFast(emf::common::EObject* obj, emf::ecore::EStructuralFeature* sf,
                          std::string& out) {
        auto* attr = dynamic_cast<emf::ecore::EAttribute*>(sf);
        if (!attr || attr->isMany()) return false;
        int fid = cachedFeatureID(obj, sf);
        if (fid < 0) return false;
        return obj->eGetString(fid, out);
    }
    bool tryGetBoolFast(emf::common::EObject* obj, emf::ecore::EStructuralFeature* sf,
                        bool& out) {
        auto* attr = dynamic_cast<emf::ecore::EAttribute*>(sf);
        if (!attr || attr->isMany()) return false;
        int fid = cachedFeatureID(obj, sf);
        if (fid < 0) return false;
        return obj->eGetBool(fid, out);
    }
    bool tryGetEObjectFast(emf::common::EObject* obj, emf::ecore::EStructuralFeature* sf,
                           emf::common::EObject*& out) {
        auto* ref = dynamic_cast<emf::ecore::EReference*>(sf);
        if (!ref || ref->isMany()) return false;
        int fid = cachedFeatureID(obj, sf);
        if (fid < 0) return false;
        return obj->eGetEObject(fid, out);
    }

    // ===== EAttribute 序列化 =====
    // 对齐 Java AutosarXMLSaveImpl.saveElementFeature / saveAttribute：
    //   isXmlAttribute=true → 写为 XML 属性（writer_.writeAttribute）
    //   否则写为子元素 <xmlName>value</xmlName>
    void saveAttribute(emf::common::EObject* obj, emf::ecore::EAttribute* attr) {
        auto meta = cachedFeatureMeta(attr);
        // 方案 B 子集：单值 attribute 优先走类型化 eGet 快速路径，避免 std::any 装箱
        std::any v;  // 延迟构造，仅快速路径未命中时才 eGet
        std::string fastValStr;
        bool fastHit = (!attr->isMany()) && tryGetAttrString(obj, attr, fastValStr);
        if (!fastHit) {
            v = obj->eGet(attr);  // fallback：反射式 eGet + std::any
        }

        if (meta.isXmlAttribute) {
            // 写为 XML 属性（对齐 Java isXmlAttribute=true 路径）
            // 若有 nsPrefix（如 xml:space），输出带前缀的属性名
            std::string attrName = meta.xmlName;
            if (!meta.nsPrefix.empty()) {
                attrName = meta.nsPrefix + ":" + meta.xmlName;
            }
            std::string valStr = fastHit ? std::move(fastValStr) : attrValueToString(attr, v);
            if (!valStr.empty()) {
                writer_.writeAttribute(attrName, valStr);
            }
        } else {
            // 写为子元素
            // 多值 EAttribute：roleWrapperElement=true 时用 xmlNamePlural 作 wrapper，
            // 内层用 xmlName 元素（对齐 Java ARTOP <GLOBAL-ELEMENTS><GLOBAL-ELEMENT>）
            if (attr->isMany()) {
                std::vector<std::string> vals = extractStringList(v);
                if (vals.empty()) return;
                bool useWrapper = meta.isRoleWrapperElement && !meta.xmlNamePlural.empty()
                                  && meta.xmlNamePlural != meta.xmlName;
                if (useWrapper) {
                    writer_.beginElement(meta.xmlNamePlural);
                    for (const auto& valStr : vals) {
                        if (valStr.empty()) continue;
                        writer_.beginElement(meta.xmlName);
                        writer_.writeText(valStr);
                        writer_.endElement();
                    }
                    writer_.endElement();
                } else {
                    for (const auto& valStr : vals) {
                        if (valStr.empty()) continue;
                        writer_.beginElement(meta.xmlName);
                        writer_.writeText(valStr);
                        writer_.endElement();
                    }
                }
            } else {
                std::string valStr = fastHit ? std::move(fastValStr) : attrValueToString(attr, v);
                // 对齐 Java EMF：用 eIsSet 判断是否输出元素（区分"未 set"与"set 为空"）。
                // writer 自然处理空字符串：writeText("") 写 '>' 但无文本，
                // endElement() 写 </TAG>，产生 <TAG></TAG>（非自闭合 <TAG/>）。
                if (obj->eIsSet(attr)) {
                    writer_.beginElement(meta.xmlName);
                    writer_.writeText(valStr);  // 空字符串 → <TAG></TAG>，非空 → <TAG>val</TAG>
                    writer_.endElement();
                }
            }
        }
    }

    // ===== EAttribute 值转字符串（对齐 Java EFactory.convertToString）=====
    std::string attrValueToString(emf::ecore::EAttribute* attr, const std::any& value) {
        if (!value.has_value()) return "";
        auto* dt = attr->getEAttributeType();

        // enum 类型：输出 literal 字符串（对齐 Java EFactory.convertToString 对 EEnum
        // 的行为：instanceValue.toString() == EEnumLiteral.getLiteral()）。
        // C++ codegen 把 EEnum 存为 std::string（literal 或 name），故优先按字符串处理。
        if (auto* en = dynamic_cast<emf::ecore::EEnum*>(dt)) {
            // 1) 字符串值：尝试按 literal / name 查找规范 literal（与 Java 输出一致）
            if (auto* sp = std::any_cast<std::string>(&value)) {
                for (auto* lit : en->getELiterals()) {
                    if (lit && (lit->getLiteral() == *sp || lit->getName() == *sp)) {
                        const std::string& litStr = lit->getLiteral();
                        return !litStr.empty() ? litStr : lit->getName();
                    }
                }
                // 未匹配：原样返回（已是 XML literal 文本，保证 round-trip）
                return *sp;
            }
            // 2) 整型值：按 value 查找 literal（兼容存 int 的场景）
            int enumVal = extractIntValue(value);
            for (auto* lit : en->getELiterals()) {
                if (lit && lit->getValue() == enumVal) {
                    const std::string& litStr = lit->getLiteral();
                    return !litStr.empty() ? litStr : lit->getName();
                }
            }
            return "";
        }

        // 优先用 factory.convertToString（对齐 XMISaver.cpp）
        auto* cls = attr->getEContainingClass();
        auto* pkg = cls ? cls->getEPackage() : nullptr;
        if (dt && pkg) {
            auto* factory = pkg->getEFactoryInstance();
            if (factory) {
                try { return factory->convertToString(dt, value); }
                catch (...) {}
            }
        }

        // fallback：常见类型直接 any_cast
        if (value.type() == typeid(std::string)) {
            return std::any_cast<std::string>(value);
        }
        if (value.type() == typeid(bool)) {
            return std::any_cast<bool>(value) ? "true" : "false";
        }
        if (value.type() == typeid(int)) {
            return std::to_string(std::any_cast<int>(value));
        }
        if (value.type() == typeid(int32_t)) {
            return std::to_string(std::any_cast<int32_t>(value));
        }
        if (value.type() == typeid(int64_t)) {
            return std::to_string(std::any_cast<int64_t>(value));
        }
        if (value.type() == typeid(unsigned int)) {
            return std::to_string(std::any_cast<unsigned int>(value));
        }
        if (value.type() == typeid(double)) {
            return std::to_string(std::any_cast<double>(value));
        }
        if (value.type() == typeid(float)) {
            return std::to_string(std::any_cast<float>(value));
        }
        if (value.type() == typeid(const char*)) {
            return std::any_cast<const char*>(value);
        }
        return "";
    }

    // ===== Containment 引用序列化（APRXML 规则分派核心）=====
    // 对齐 Java AutosarXMLSaveImpl.saveContainmentFeature + APRXML 规则分派
    void saveContainment(emf::common::EObject* obj, emf::ecore::EReference* ref) {
        auto meta = cachedFeatureMeta(ref);
        bool isMany = ref->isMany();
        int rule = resolveAprxmlRule(meta, isMany);

        // 提取子对象列表（fast-path: 优先用 EObjectRefView 零拷贝视图）
        std::vector<emf::common::EObject*> children;  // 单值/排序/fallback 时使用
        emf::common::EObjectRefView view;  // 多值零拷贝视图
        bool needSort = isMany && !isFeatureOrdered(ref);

        // 方案 B 子集：单值 containment 优先用 eGetEObject 避免装箱
        emf::common::EObject* fastChild = nullptr;
        if (!isMany && tryGetEObject(obj, ref, fastChild)) {
            // 命中类型化快速路径：直接取字段指针，无 std::any 装箱
            if (fastChild) children.push_back(fastChild);
            if (children.empty()) return;  // 单值为空，无内容输出
        } else {
            // fallback：反射式 eGet + std::any（多值 / 快速路径未命中）
            std::any v = obj->eGet(ref);
            view = extractRefView(v);
            if (!view.empty()) {
                // fast-path: EObjectRefView 可用，零拷贝遍历
                if (needSort) {
                    children.assign(view.begin(), view.end());
                    sortChildrenBySplitkey(children, ref);
                    view = {};  // 改走 children 路径
                }
            } else {
                // fallback: 非 EObjectRefView（单值 reference 等）
                children = extractObjectList(v);
                if (needSort) sortChildrenBySplitkey(children, ref);
                if (children.empty()) return;
                view = {};
            }
        }

        // 后续遍历：needSort/fallback/单值 时用 children，否则用 view（零拷贝）
        auto forEachChild = [&](auto&& fn) {
            if (needSort || view.empty()) {
                for (auto* child : children) fn(child);
            } else {
                for (auto* child : view) fn(child);
            }
        };

        // DEBUG: wrapper 标签诊断
        if (std::getenv("ARXML_DEBUG_WRAPPER")) {
            size_t cnt = needSort ? children.size() : view.size();
            std::fprintf(stderr, "[WRAP] feat=%s isMany=%d isRoleWrapper=%d isTypeElem=%d rule=%d xmlName=%s xmlNamePlural=%s children=%zu\n",
                ref->getName().c_str(), (int)isMany, (int)meta.isRoleWrapperElement,
                (int)meta.isTypeElement, rule,
                meta.xmlName.c_str(), meta.xmlNamePlural.c_str(), cnt);
        }

        // ===== APRXML 0016：四个标志全 false，跳过 wrapper，子内容内联到父元素 =====
        // 对齐 Java XMLPersistenceMappingSaveImpl.saveEReferenceContained0000Single/Many：
        //   strategy 0000 不发射任何元素，直接调用 saveFeatures(value, elementsOnly, true)
        //   把子对象的 element features 写入当前父节点。
        //   - 同型（child.eClass() == 声明 eType）：纯内联
        //     例：Item.itemContents (eType=DocumentationBlock) → DocumentationBlock 内容内联到 <ITEM>
        //   - 多态（child.eClass() != 声明 eType，通常因声明类型抽象）：同样不发射 type element，
        //     子对象的 features 直接内联。子类型通过其内部首个 feature 元素名隐式表达
        //     （如 CompuScales 的 compuScales feature 发射 <COMPU-SCALES>，恰好匹配类型 xml.name，
        //     loader 的 tryInlineMatch Case B 据此识别子类型）。
        //     例：Compu.compuContent (eType=CompuContent 抽象) → CompuScales.compuScales
        //     发射 <COMPU-SCALES><COMPU-SCALE>...</COMPU-SCALE></COMPU-SCALES> 内联到父元素
        if (rule == kAprxml0016) {
            forEachChild([&](emf::common::EObject* child) {
                if (!child) return;
                // 0016 不发射任何 wrapper/type element，子对象 features 直接写入父节点
                // 对齐 Java saveEReferenceContained0000Single: saveFeatures(value, elementsOnly, true)
                // elementsOnly=true：跳过 XML 属性型 feature（对齐 Java elementsOnly 模式）
                saveObjectContent(child, true);
            });
            return;
        }

        // ===== APRXML 0012：role+type 合一元素（upperBound<=1）=====
        // 对齐 Java APRXML0012：标签用类型 QName（带前缀的类型 QName）
        // R4.0 单一默认命名空间，无前缀，标签 = 类型 xmlName
        if (rule == kAprxml0012) {
            forEachChild([&](emf::common::EObject* child) {
                if (!child) return;
                auto* childCls = child->eClass();
                std::string tag = cachedTypeXmlName(childCls);
                writer_.beginElement(tag);
                saveObjectContent(child);
                writer_.endElement();
            });
            return;
        }

        // ===== APRXML 0015：纯 type 元素（无 role，无 wrapper）=====
        // 对齐 Java APRXML0015：标签用类型 XSD 名（无前缀）
        if (rule == kAprxml0015) {
            forEachChild([&](emf::common::EObject* child) {
                if (!child) return;
                auto* childCls = child->eClass();
                std::string tag = cachedTypeXmlName(childCls);
                writer_.beginElement(tag);
                saveObjectContent(child);
                writer_.endElement();
            });
            return;
        }

        // ===== 默认规则：标准 EMF + role-wrapper 包裹 =====
        // 对齐 Java XMLPersistenceMappingSaveImpl 各 saveEReferenceContained{策略}Many：
        //   - 策略 1001（roleWrapper+typeElement）：外层 xmlNamePlural，内层 类型 xmlName
        //   - 策略 1100（roleWrapper+roleElement）：外层 xmlNamePlural，内层 角色 xmlName
        //   - 策略 0100（roleElement only）：无外层，内层 角色 xmlName
        //   - 策略 0001（typeElement only）：无外层，内层 类型 xmlName
        // 核心规则：typeElement=true → 内层用 getClassifierQName(child.eClass)
        //          否则 → 内层用 getFeatureQName(feature) = meta.xmlName
        if (isMany && meta.isRoleWrapperElement) {
            // <xmlNamePlural> wrapper（对齐 Java roleWrapperElement 包裹）
            std::string wrapperTag = meta.xmlNamePlural.empty()
                ? meta.xmlName : meta.xmlNamePlural;
            writer_.beginElement(wrapperTag);
            forEachChild([&](emf::common::EObject* child) {
                if (!child) return;
                // 对齐 Java：typeElement=true 时用类型 xmlName，否则用 role xmlName
                std::string itemTag = meta.isTypeElement
                    ? cachedTypeXmlName(child->eClass())
                    : meta.xmlName;
                writer_.beginElement(itemTag);
                saveObjectContent(child);
                writer_.endElement();
            });
            writer_.endElement();
        } else {
            // 无 wrapper，直接包裹每个子对象
            forEachChild([&](emf::common::EObject* child) {
                if (!child) return;
                std::string itemTag = meta.isTypeElement
                    ? cachedTypeXmlName(child->eClass())
                    : meta.xmlName;
                writer_.beginElement(itemTag);
                saveObjectContent(child);
                writer_.endElement();
            });
        }
    }

    // ===== APRXML 规则推导（对齐 Java AutosarPersistenceRules）=====
    // 由 EAnnotation 的 roleElement/roleWrapperElement/typeElement/typeWrapperElement 标志判定
    int resolveAprxmlRule(
            const emf::ecore::codegen::FeatureMeta& m, bool isMany) {
        bool upperBoundLe1 = !isMany;  // upperBound<=1 即非多值
        // 0012：upperBound<=1 && isRoleElement && isTypeElement && !isRoleWrapperElement && !isTypeWrapperElement
        if (upperBoundLe1 && m.isRoleElement && m.isTypeElement
                && !m.isRoleWrapperElement && !m.isTypeWrapperElement) {
            return kAprxml0012;
        }
        // 0015：!isRoleWrapperElement && !isRoleElement && !isTypeWrapperElement && isTypeElement
        if (!m.isRoleWrapperElement && !m.isRoleElement
                && !m.isTypeWrapperElement && m.isTypeElement) {
            return kAprxml0015;
        }
        // 0016：四个标志全为 false
        if (!m.isRoleWrapperElement && !m.isRoleElement
                && !m.isTypeWrapperElement && !m.isTypeElement) {
            return kAprxml0016;
        }
        return kAprxmlDefault;  // 默认：标准 EMF + role-wrapper 包裹
    }

    // ===== 非 containment 引用序列化（对齐 Java saveElementReference）=====
    // 输出：<FEATURE DEST="TargetTypeXmlName">short-name-path</FEATURE>
    //   - 文本 = short name path（AutosarURIFactory.getAbsoluteQualifiedName）
    //   - DEST = 目标 EClass 的 xmlName（除非 NsURI 以 http://autosar.org/2.0.0 开头）
    //   - BASE/INDEX 暂不实现（先用绝对路径）
    void saveReference(emf::common::EObject* obj, emf::ecore::EReference* ref) {
        auto meta = cachedFeatureMeta(ref);

        // 提取目标对象列表
        std::vector<emf::common::EObject*> targets;  // 单值/排序/fallback 时使用
        emf::common::EObjectRefView view;  // 多值零拷贝视图
        bool needSort = ref->isMany() && !isFeatureOrdered(ref);

        // 方案 B 子集：单值 reference 优先用 eGetEObject 避免装箱
        emf::common::EObject* fastChild = nullptr;
        if (!ref->isMany() && tryGetEObject(obj, ref, fastChild)) {
            // 命中类型化快速路径：直接取字段指针，无 std::any 装箱
            if (fastChild) targets.push_back(fastChild);
        } else {
            // fallback：反射式 eGet + std::any（多值 / 快速路径未命中）
            std::any v = obj->eGet(ref);
            view = extractRefView(v);
            if (!view.empty()) {
                if (needSort) {
                    targets.assign(view.begin(), view.end());
                    sortChildrenBySplitkey(targets, ref);
                }
            } else {
                targets = extractObjectList(v);
                if (needSort) sortChildrenBySplitkey(targets, ref);
                view = {};
            }
        }
        // 统一遍历接口：needSort 或 view 不可用时用 targets，否则用 view
        auto forEachTarget = [&](auto&& fn) {
            if (needSort || view.empty()) {
                for (auto* t : targets) fn(t);
            } else {
                for (auto* t : view) fn(t);
            }
        };

        // wrapper 模式（APRXML 0016/0013）：isMany + isRoleWrapperElement 时
        // 用 xmlNamePlural 包裹所有 ref 元素（对齐 saveContainment 的 wrapper 逻辑）
        bool useWrapper = ref->isMany() && meta.isRoleWrapperElement
            && !meta.xmlNamePlural.empty() && meta.xmlNamePlural != meta.xmlName;
        if (useWrapper) {
            writer_.beginElement(meta.xmlNamePlural);
        }

        forEachTarget([&](emf::common::EObject* target) {
            if (!target) return;
            // 元素标签 = feature 的 xmlName
            std::string tag = meta.xmlName;
            writer_.beginElement(tag);

            // DEST 属性 = 目标 EClass 的 xmlName
            // 对齐 Java：除非 NsURI 以 http://autosar.org/2.0.0 开头则不写 DEST
            // DEST 值优先用 Loader 保存的原始 DEST（按 owner,ref,target 三元组查询，
            // 可能是抽象基类如 ECUC-DEFINITION-ELEMENT），无则用 target eClass() 的 xmlName。
            auto* targetCls = target->eClass();
            if (targetCls) {
                auto* pkg = targetCls->getEPackage();
                std::string nsURI = pkg ? pkg->getNsURI() : "";
                if (nsURI.find(kAutosar20NsURIPrefix) != 0) {
                    // 不以 2.0.0 开头，写 DEST
                    std::string dest;
                    auto destIt = refDestStore().find(refDestKey(obj, ref, target));
                    if (destIt != refDestStore().end()) {
                        dest = destIt->second;
                    } else {
                        dest = cachedTypeXmlName(targetCls);
                    }
                    if (!dest.empty()) {
                        writer_.writeAttribute("DEST", dest);
                    }
                }
            }

            // 文本 = short name path（对齐 Java AutosarURIFactory.getAbsoluteQualifiedName）
            std::string path = getShortNamePath(target);

            // 未解析 proxy：输出 proxyURI 文本作为引用文本（对齐 Java 未解析引用行为）。
            // proxyURI 可能是原始相对路径（无 ReferenceBase 的跨文档引用）或
            // autosar-proxy://base=XXX/path=YYY（编码了 BASE 的跨文档引用）。
            if (path.empty() && target->eIsProxy()) {
                if (auto* impl = dynamic_cast<emf::common::EObjectImpl*>(target)) {
                    std::string uri = impl->eProxyURI().toString();
                    // 解码 autosar-proxy 格式取 path 部分
                    const std::string kPrefix = "autosar-proxy://base=";
                    if (uri.rfind(kPrefix, 0) == 0) {
                        size_t pathPos = uri.find("/path=", kPrefix.size());
                        if (pathPos != std::string::npos) {
                            path = uri.substr(pathPos + 6);
                        } else {
                            path = uri;
                        }
                    } else {
                        path = uri;
                    }
                }
            }

            // 跨文档引用：proxyURI 编码了原始 BASE + 相对路径（格式：autosar-proxy://base=XXX/path=YYY）
            // 直接解码输出 BASE 属性 + 相对文本（对齐 Java 跨文档引用行为）
            std::string encodedBase;
            std::string encodedPath;
            if (target->eIsProxy()) {
                if (auto* impl = dynamic_cast<emf::common::EObjectImpl*>(target)) {
                    std::string uri = impl->eProxyURI().toString();
                    const std::string kPrefix = "autosar-proxy://base=";
                    if (uri.rfind(kPrefix, 0) == 0) {
                        // 解码 autosar-proxy://base=XXX/path=YYY
                        size_t pathPos = uri.find("/path=", kPrefix.size());
                        if (pathPos != std::string::npos) {
                            encodedBase = uri.substr(kPrefix.size(), pathPos - kPrefix.size());
                            encodedPath = uri.substr(pathPos + 6);
                        }
                    }
                }
            }

            if (!encodedBase.empty()) {
                // 跨文档引用（autosar-proxy 格式）：
                // 对齐 Java RelativeReference.isDefault 逻辑——isDefault=true 时不输出 BASE。
                // autosar-proxy 格式都是无 ReferenceBase 解析的（isDefault=true），不输出 BASE。
                writer_.writeText(encodedPath);
                writer_.endElement();
                return;
            }

            // BASE 属性反向计算（对齐 Java AutosarReferenceHelper.getRelativeReference）：
            // 沿 obj.eContainer 链找最近的 ARPackage，遍历其 referenceBases，
            // 找 prefix 匹配 path 前缀的 ReferenceBase，输出 BASE + 相对文本。
            // 对齐 Java RelativeReference.isDefault：
            //   isDefault=true（ReferenceBase.IS-DEFAULT=true）→ 输出相对路径，不输出 BASE
            //   isDefault=false（ReferenceBase.IS-DEFAULT=false 或未设置）→ 输出 BASE + 相对路径
            //   无匹配 ReferenceBase → 输出绝对路径（fallback）
            auto rel = tryComputeBaseRelative(obj, path);
            if (rel.found) {
                if (!rel.isDefault) {
                    writer_.writeAttribute("BASE", rel.base);
                }
                writer_.writeText(rel.text);
            } else {
                // 无匹配 ReferenceBase：输出绝对路径（fallback）
                writer_.writeText(path);
            }
            writer_.endElement();
        });

        if (useWrapper) {
            writer_.endElement();
        }
    }

    // ===== 单个 mixed content 子元素输出（对齐 Java XMLSaveImpl.saveElement）=====
    // mixed content 序列中每个 ELEMENT 条目对应一个子对象，按其位置单独输出。
    // 不发射 wrapper（roleWrapperElement 的复数包裹元素）——mixed content 中每个
    // 值独立出现在序列中，Java EMF 的 saveElement(feature, value) 也是单值输出。
    // 这修复了 multi-valued feature 在 mixed content 中的交错问题：
    //   原行为（savedRefs 去重）：所有同类型子元素输出在第一个位置
    //   新行为（按条目输出）：每个子元素输出在自己的序列位置
    void saveSingleMixedElement(emf::common::EObject* parent,
                                  emf::ecore::EReference* ref,
                                  emf::common::EObject* child) {
        if (!ref || !child) return;
        auto meta = cachedFeatureMeta(ref);

        if (ref->isContainment()) {
            int rule = resolveAprxmlRule(meta, false);  // 单值视角
            // 0016 inline：子对象 features 直接内联到当前节点（不发射元素）
            if (rule == kAprxml0016) {
                saveObjectContent(child, true);
                return;
            }
            // 0012/0015：标签用类型 xmlName
            // 默认：isTypeElement → 类型 xmlName；否则 → role xmlName (meta.xmlName)
            std::string tag;
            if (rule == kAprxml0012 || rule == kAprxml0015 || meta.isTypeElement) {
                tag = cachedTypeXmlName(child->eClass());
            } else {
                tag = meta.xmlName;
            }
            if (tag.empty()) tag = ref->getName();
            writer_.beginElement(tag);
            saveObjectContent(child);
            writer_.endElement();
        } else {
            // 非 containment 引用：单个 <FEATURE DEST="Type">path</FEATURE>
            std::string tag = meta.xmlName.empty() ? ref->getName() : meta.xmlName;
            writer_.beginElement(tag);
            auto* targetCls = child->eClass();
            if (targetCls) {
                auto* pkg = targetCls->getEPackage();
                std::string nsURI = pkg ? pkg->getNsURI() : "";
                if (nsURI.find(kAutosar20NsURIPrefix) != 0) {
                    // DEST 优先用 Loader 保存的原始值（按 parent,ref,child 三元组查询）
                    std::string dest;
                    auto destIt = refDestStore().find(refDestKey(parent, ref, child));
                    if (destIt != refDestStore().end()) {
                        dest = destIt->second;
                    } else {
                        dest = cachedTypeXmlName(targetCls);
                    }
                    if (!dest.empty()) {
                        writer_.writeAttribute("DEST", dest);
                    }
                }
            }
            std::string path = getShortNamePath(child);
            // 未解析 proxy：输出 proxyURI 文本
            if (path.empty() && child->eIsProxy()) {
                if (auto* impl = dynamic_cast<emf::common::EObjectImpl*>(child)) {
                    std::string uri = impl->eProxyURI().toString();
                    const std::string kPrefix = "autosar-proxy://base=";
                    if (uri.rfind(kPrefix, 0) == 0) {
                        size_t pathPos = uri.find("/path=", kPrefix.size());
                        if (pathPos != std::string::npos) {
                            path = uri.substr(pathPos + 6);
                        } else {
                            path = uri;
                        }
                    } else {
                        path = uri;
                    }
                }
            }
            // 跨文档引用解码
            std::string encodedBase;
            std::string encodedPath;
            if (child->eIsProxy()) {
                if (auto* impl = dynamic_cast<emf::common::EObjectImpl*>(child)) {
                    std::string uri = impl->eProxyURI().toString();
                    const std::string kPrefix = "autosar-proxy://base=";
                    if (uri.rfind(kPrefix, 0) == 0) {
                        size_t pathPos = uri.find("/path=", kPrefix.size());
                        if (pathPos != std::string::npos) {
                            encodedBase = uri.substr(kPrefix.size(), pathPos - kPrefix.size());
                            encodedPath = uri.substr(pathPos + 6);
                        }
                    }
                }
            }
            if (!encodedBase.empty()) {
                // 跨文档引用：对齐 Java，不输出 BASE（isDefault 默认 true），只输出相对路径文本
                writer_.writeText(encodedPath);
                writer_.endElement();
                return;
            }
            // 对齐 Java RelativeReference.isDefault：
            //   isDefault=true → 输出相对路径，不输出 BASE
            //   isDefault=false → 输出 BASE + 相对路径
            //   无匹配 ReferenceBase → 输出绝对路径（fallback）
            auto rel = tryComputeBaseRelative(parent, path);
            if (rel.found) {
                if (!rel.isDefault) {
                    writer_.writeAttribute("BASE", rel.base);
                }
                writer_.writeText(rel.text);
            } else {
                writer_.writeText(path);
            }
            writer_.endElement();
        }
    }

    // 尝试计算 BASE 相对路径（对齐 Java AutosarReferenceHelper.getRelativeReference）。
    // 沿 obj.eContainer 链找 ARPackage，遍历其 referenceBases，
    // 选择规则：最长匹配前缀；等长时列表靠后者胜（对齐 Java bestPrefix 逻辑）
    // 返回 {base.shortLabel, path 去掉 prefix + "/" 后的相对部分, isDefault from ReferenceBase}。
    // isDefault 来自 ReferenceBase.IS-DEFAULT：true 时不输出 BASE（对齐 Java）。
    struct BaseRelative {
        std::string base;
        std::string text;
        bool isDefault = false;
        bool found = false;
    };
    BaseRelative tryComputeBaseRelative(
            emf::common::EObject* obj, const std::string& path) {
        BaseRelative result;
        if (!obj || path.empty()) return result;
        emf::common::EObject* cur = obj;
        while (cur) {
            auto* cls = cur->eClass();
            if (cls && cls->getName() == "ARPackage") {
                // 找 referenceBases feature
                emf::ecore::EStructuralFeature* rbFeat = nullptr;
                for (auto* sf : cls->getEAllStructuralFeatures()) {
                    if (!sf) continue;
                    auto m = cachedFeatureMeta(sf);
                    if (m.xmlNamePlural == "REFERENCE-BASES" || sf->getName() == "referenceBases") {
                        rbFeat = sf;
                        break;
                    }
                }
                if (rbFeat) {
                    std::any v = cur->eGet(rbFeat);
                    auto refs = extractObjectList(v);
                    std::string bestPrefix;
                    emf::common::EObject* bestBase = nullptr;
                    for (auto* rb : refs) {
                        if (!rb) continue;
                        if (rb == obj) continue;  // 跳过指向自身的 ReferenceBase
                        std::string prefix = getReferenceBasePrefix(rb);
                        if (prefix.empty()) continue;
                        // path 必须以 prefix 为前缀（对齐 Java absolutePath.startsWith(prefix)）
                        if (path.compare(0, prefix.size(), prefix) != 0) continue;
                        if (path.size() <= prefix.size()) continue;  // 至少要有 "/"
                        if (path[prefix.size()] != '/') continue;
                        // 选择逻辑：bestPrefix==null 或 prefix.startsWith(bestPrefix) 则更新
                        // 等价于最长匹配；等长时靠后者胜
                        if (bestPrefix.empty() || prefix.size() >= bestPrefix.size()) {
                            bestPrefix = prefix;
                            bestBase = rb;
                        }
                    }
                    if (bestBase) {
                        std::string base = readReferenceBaseShortLabel(bestBase);
                        if (!base.empty()) {
                            result.base = base;
                            result.text = path.substr(bestPrefix.size() + 1);
                            result.isDefault = readReferenceBaseIsDefault(bestBase);
                            result.found = true;
                            return result;
                        }
                    }
                }
            }
            cur = cur->eContainer();
        }
        return result;
    }

    // 读 ReferenceBase.shortLabel
    std::string readReferenceBaseShortLabel(emf::common::EObject* rb) {
        if (!rb) return {};
        auto* cls = rb->eClass();
        if (!cls) return {};
        auto* sf = cls->getEStructuralFeature("shortLabel");
        if (!sf) {
            for (auto* a : cls->getEAllAttributes()) {
                if (!a) continue;
                auto m = cachedFeatureMeta(a);
                if (m.xmlName == "SHORT-LABEL" || a->getName() == "shortLabel") {
                    sf = a;
                    break;
                }
            }
        }
        if (!sf) return {};
        // 方案 B 子集：优先走类型化 eGetString 快速路径
        std::string s;
        if (tryGetStringFast(rb, sf, s)) return s;
        // fallback：反射式 eGet + std::any
        std::any v = rb->eGet(sf);
        if (v.type() == typeid(std::string)) return std::any_cast<std::string>(v);
        return {};
    }

    // 读 ReferenceBase.isDefault（对齐 Java getRelativeReference 读取 IS-DEFAULT）
    // 未设置时返回 false（对齐 Java Boolean.FALSE 默认值）。
    bool readReferenceBaseIsDefault(emf::common::EObject* rb) {
        if (!rb) return false;
        auto* cls = rb->eClass();
        if (!cls) return false;
        auto* sf = cls->getEStructuralFeature("isDefault");
        if (!sf) {
            for (auto* a : cls->getEAllAttributes()) {
                if (!a) continue;
                auto m = cachedFeatureMeta(a);
                if (m.xmlName == "IS-DEFAULT" || a->getName() == "isDefault") {
                    sf = a;
                    break;
                }
            }
        }
        if (!sf) return false;
        // 方案 B 子集：优先走类型化 eGetBool 快速路径
        bool b = false;
        if (tryGetBoolFast(rb, sf, b)) return b;
        // fallback：反射式 eGet + std::any（部分类型可能存为 string "true"）
        std::any v = rb->eGet(sf);
        if (v.type() == typeid(bool)) {
            return std::any_cast<bool>(v);
        }
        if (v.type() == typeid(std::string)) {
            std::string sv = std::any_cast<std::string>(v);
            return sv == "true";
        }
        return false;
    }

    // 读 ReferenceBase 的 prefix（对齐 Loader 端 getReferenceBasePrefix）
    std::string getReferenceBasePrefix(emf::common::EObject* refBase) {
        if (!refBase) return {};
        // 读 baseIsThisPackage
        bool isThisPkg = false;
        auto* cls = refBase->eClass();
        if (cls) {
            auto* sf = cls->getEStructuralFeature("baseIsThisPackage");
            if (!sf) {
                for (auto* a : cls->getEAllAttributes()) {
                    if (!a) continue;
                    auto m = cachedFeatureMeta(a);
                    if (m.xmlName == "BASE-IS-THIS-PACKAGE" || a->getName() == "baseIsThisPackage") {
                        sf = a;
                        break;
                    }
                }
            }
            if (sf) {
                // 方案 B 子集：优先走类型化 eGetBool 快速路径
                bool b = false;
                if (tryGetBoolFast(refBase, sf, b)) {
                    isThisPkg = b;
                } else {
                    // fallback：反射式 eGet + std::any
                    std::any v = refBase->eGet(sf);
                    if (v.type() == typeid(bool)) isThisPkg = std::any_cast<bool>(v);
                }
            }
        }
        if (isThisPkg) {
            emf::common::EObject* pkg = refBase->eContainer();
            while (pkg && pkg->eClass() && pkg->eClass()->getName() != "ARPackage") {
                pkg = pkg->eContainer();
            }
            return getShortNamePath(pkg);
        }
        // prefix = ReferenceBase.package 引用的绝对 path
        if (!cls) return {};
        emf::ecore::EStructuralFeature* pkgFeat = cls->getEStructuralFeature("package");
        if (!pkgFeat) {
            for (auto* sf : cls->getEAllReferences()) {
                if (!sf) continue;
                auto m = cachedFeatureMeta(sf);
                if (m.xmlName == "PACKAGE-REF" || sf->getName() == "package") {
                    pkgFeat = sf;
                    break;
                }
            }
        }
        if (!pkgFeat) return {};
        // 方案 B 子集：优先走类型化 eGetEObject 快速路径（单值 reference）
        emf::common::EObject* fastTarget = nullptr;
        if (tryGetEObjectFast(refBase, pkgFeat, fastTarget)) {
            if (!fastTarget) return {};
            return getShortNamePath(fastTarget);
        }
        // fallback：反射式 eGet + extractObjectList（多值/未生成 eGetEObject）
        std::any v = refBase->eGet(pkgFeat);
        auto refs = extractObjectList(v);
        if (refs.empty()) return {};
        emf::common::EObject* target = refs[0];
        if (!target) return {};
        return getShortNamePath(target);
    }

    // ===== Short name path 生成（对齐 Java AutosarURIFactory.getAbsoluteQualifiedName）=====
    // 沿 eContainer() 链向上收集每个 GReferrable 的 shortName，以 "/" 拼接
    // 根元素（如 AUTOSAR）的 shortName 不参与（或为空）
    // 结果形如 "/PkgA/PkgB/Element"
    std::string getShortNamePath(emf::common::EObject* obj) {
        if (!obj) return "";
        // 查缓存
        auto it = snpCache_.find(obj);
        if (it != snpCache_.end()) return it->second;

        // proxy 对象：返回 proxyURI（对齐 Java 未解析引用输出 proxy URI）
        if (obj->eIsProxy()) {
            if (auto* impl = dynamic_cast<emf::common::EObjectImpl*>(obj)) {
                std::string uri = impl->eProxyURI().toString();
                snpCache_[obj] = uri;
                return uri;
            }
        }

        std::vector<std::string> parts;
        emf::common::EObject* cur = obj;
        while (cur) {
            // 读取 shortName（EMF 特征名 "shortName"，对齐 IdentifiableUtil）
            const std::string* snPtr = cachedShortNamePtr(cur);
            std::string sn = snPtr ? *snPtr : std::string{};
            if (!sn.empty()) {
                parts.push_back(sn);
            } else {
                // 无 shortName（如根 AUTOSAR 元素）—— 停止向上收集
                break;
            }
            cur = cur->eContainer();
        }
        // 反转：从根到叶
        std::reverse(parts.begin(), parts.end());
        // 以 "/" 拼接，前导 "/"
        std::string path;
        for (size_t i = 0; i < parts.size(); ++i) {
            path += "/";
            path += parts[i];
        }

        snpCache_[obj] = path;
        return path;
    }

    // ===== 读取对象的 shortName（EMF 特征名，非 XML 名）=====
    // 对齐 IdentifiableUtil::getShortName：反射查找名为 "shortName" 的 EAttribute
    std::string readShortNameUncached(emf::common::EObject* obj) {
        if (!obj) return "";
        auto* cls = obj->eClass();
        if (!cls) return "";
        for (auto* sf : cls->getEAllStructuralFeatures()) {
            if (!sf) continue;
            // EMF 特征名是 "shortName"（camelCase），XML 名是 "SHORT-NAME"
            if (sf->getName() == "shortName") {
                // 方案 B 子集：优先走类型化 eGetString 快速路径，避免 std::any 装箱
                std::string s;
                if (tryGetStringFast(obj, sf, s)) return s;
                // fallback：反射式 eGet + std::any（enum/多值/未生成 eGetString 等情况）
                std::any v = obj->eGet(sf);
                if (v.type() == typeid(std::string)) {
                    return std::any_cast<std::string>(v);
                }
                return "";
            }
        }
        return "";
    }

    // ===== 获取 EClass 的 xmlName（对齐 Java extendedMetaData.getName / helper.getQName）=====
    std::string getTypeXmlNameUncached(emf::ecore::EClass* cls) {
        if (!cls) return "";
        // 1. 直接查 TaggedValues 注解的 xml.name（不走 readClassMeta 的类名兜底）
        auto* ann = cls->getEAnnotation("TaggedValues");
        if (ann) {
            const std::string& xn = ann->getDetail("xml.name");
            if (!xn.empty()) return xn;
        }
        // 2. 查 ExtendedMetaData 注解的 name
        auto* emd = cls->getEAnnotation(
            "http:///org/eclipse/emf/ecore/util/ExtendedMetaData");
        if (emd) {
            const std::string& xn = emd->getDetail("name");
            if (!xn.empty()) return xn;
        }
        // 3. Fallback: 实例化注册表（注解缺失的 EClass，如 Collection_class_）
        auto& fb = xmlNameFallback();
        auto it = fb.classToName.find(cls);
        if (it != fb.classToName.end()) return it->second;
        // 4. 兜底：类名
        return cls->getName();
    }

    // ===== 判断 feature 是否有序（对齐 Java ExtendedMetaData.ordered）=====
    // 从 ExtendedMetaData 注解读取 "ordered" detail，默认 true
    bool isFeatureOrdered(emf::ecore::EStructuralFeature* sf) {
        if (!sf) return true;
        auto* ann = sf->getEAnnotation(
            "http:///org/eclipse/emf/ecore/util/ExtendedMetaData");
        if (!ann) return true;
        std::string ordered = ann->getDetail("ordered");
        if (ordered == "false") return false;
        return true;
    }

    // ===== 按 splitkey 排序子对象列表 =====
    // 对齐 Java AtpSplitkeyAwareComparator：
    //   1. 读取 feature 的 atp.Splitkey（feature path，如 "shortLabel" 或 "a.b"）
    //   2. 沿 path eGet 取值，若值是 GIdentifiable 则取 shortName
    //   3. 按 Comparable 排序
    //   splitkey 为空时默认用 "shortName"
    std::string getSplitkeyValue(emf::common::EObject* obj, const std::vector<std::string>& path) {
        emf::common::EObject* cur = obj;
        std::string result;
        for (size_t i = 0; i < path.size(); ++i) {
            if (!cur) break;
            auto* cls = cur->eClass();
            if (!cls) break;
            auto* sf = cls->getEStructuralFeature(path[i]);
            if (!sf) break;
            // 方案 B 子集：优先走类型化快速路径，避免 std::any 装箱
            // 中间节点：单值 reference → EObject*，继续沿 path 下钻
            emf::common::EObject* nextChild = nullptr;
            if (tryGetEObjectFast(cur, sf, nextChild)) {
                cur = nextChild;
                continue;
            }
            // 叶子节点：单值 attribute → string
            std::string s;
            if (tryGetStringFast(cur, sf, s)) {
                result = s;
                break;
            }
            // fallback：反射式 eGet + std::any（多值/未生成类型化 eGet 等情况）
            std::any v = cur->eGet(sf);
            // 若是 EObject，继续沿 path 下钻
            if (v.type() == typeid(emf::common::EObject*)) {
                cur = std::any_cast<emf::common::EObject*>(v);
                continue;
            }
            // 叶子节点：取字符串值
            if (v.type() == typeid(std::string)) {
                result = std::any_cast<std::string>(v);
            }
            break;
        }
        // 对齐 Java：若最终值是 GIdentifiable，取 gGetShortName
        // C++ 端无 GIdentifiable 接口，但 splitkey path 通常已指向 shortName/shortLabel 特征，
        // 直接返回字符串值即可。
        return result;
    }

    void sortChildrenBySplitkey(std::vector<emf::common::EObject*>& children,
                                  emf::ecore::EStructuralFeature* feature) {
        // 读取 feature 的 atp.Splitkey，默认 "shortName"
        std::string splitkey = "shortName";
        if (feature) {
            auto meta = cachedFeatureMeta(feature);
            if (!meta.splitkey.empty()) splitkey = meta.splitkey;
        }
        // 解析 splitkey 为 feature path 列表（对齐 Java getAtpSplitkeyFeaturePaths）：
        //   "a, b.c" → [["a"], ["b", "c"]]
        // 先按 "," 分割成多个 splitkey，每个再按 "." 分割成 path
        std::vector<std::vector<std::string>> paths;
        std::string skBuf;
        std::istringstream skStream(splitkey);
        while (std::getline(skStream, skBuf, ',')) {
            std::vector<std::string> path;
            std::string seg;
            std::istringstream pStream(skBuf);
            while (std::getline(pStream, seg, '.')) {
                // trim 空白（对齐 Java String.trim()）
                size_t b = 0;
                while (b < seg.size() && std::isspace((unsigned char)seg[b])) ++b;
                size_t e = seg.size();
                while (e > b && std::isspace((unsigned char)seg[e - 1])) --e;
                seg = seg.substr(b, e - b);
                if (!seg.empty()) path.push_back(seg);
            }
            if (!path.empty()) paths.push_back(path);
        }
        // 无 path 时用默认 ["shortName"]
        if (paths.empty()) paths.push_back({"shortName"});

        // Schwartzian transform（decorate-sort-undecorate）：预计算每个 child 的 splitkey
        // 一次（O(n)），排序时仅做字符串比较（O(n log n)），避免原实现中每次比较都调用
        // getSplitkeyValue（涉及 eGet 路径导航，O(n log n) 次重复计算）。
        struct KeyedChild {
            std::vector<std::string> keys;  // 每个 path 一个 key
            emf::common::EObject* child;
        };
        std::vector<KeyedChild> keyed;
        keyed.reserve(children.size());
        for (auto* child : children) {
            KeyedChild kc;
            kc.child = child;
            kc.keys.reserve(paths.size());
            for (const auto& path : paths) {
                kc.keys.push_back(getSplitkeyValue(child, path));
            }
            keyed.push_back(std::move(kc));
        }

        // 对齐 Java AtpSplitkeyAwareComparator.compare：逐个 path 比较
        std::stable_sort(keyed.begin(), keyed.end(),
            [](const KeyedChild& a, const KeyedChild& b) {
                if (!a.child) return b.child != nullptr;
                if (!b.child) return false;
                for (size_t i = 0; i < a.keys.size(); ++i) {
                    if (a.keys[i] != b.keys[i]) return a.keys[i] < b.keys[i];
                }
                return false;  // 全部相等，保持稳定
            });

        // 提取排序后的 children 顺序
        for (size_t i = 0; i < keyed.size(); ++i) {
            children[i] = keyed[i].child;
        }
    }

    // 兼容旧调用（无 splitkey 信息，默认 shortName）
    void sortChildrenByShortName(std::vector<emf::common::EObject*>& children) {
        sortChildrenBySplitkey(children, nullptr);
    }
};

}  // namespace

// ===== AutosarXMLSaver 公开接口实现 =====
void AutosarXMLSaver::save(const emf::xmi::XMIResource* resource,
                            std::ostream& output,
                            const emf::xmi::XMIOptions& options) {
    if (!resource) return;

    // 委托给 AutosarSaver 实现类完成实际序列化
    AutosarSaver saver(*resource, options);
    saver.save(output);
}

}  // namespace emf::artop::runtime
