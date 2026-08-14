// emf::artop::runtime —— AutosarXMLLoader 实现
// 对齐 Java: org.artop.aal.common.resource.impl.AutosarXMLLoadImpl
//            + AutosarSAXXMLHandler（反序列化核心逻辑）
//
// arxml 反序列化策略（pugixml DOM → EObject 树，两阶段构建）：
//   阶段 1（构建）：递归遍历 XML 元素，按元素名匹配 EStructuralFeature
//     - 根元素 <AUTOSAR> → 在 autosar40 EPackage 中找名为 "AUTOSAR" 的 EClass
//     - containment 引用 → 创建子 EObject 并挂到父 feature
//     - EAttribute → XML 属性或子元素文本（按 isXmlAttribute 决策）
//     - REF/TREF/IREF → 创建代理对象（DEST 解析类型，文本作 proxyURI/path）
//   阶段 2（索引）：遍历已构建的 EObject 树，为每个有 shortName 的 GReferrable
//     沿 eContainer 链收集 shortName，建立 path → EObject* 映射
//   阶段 3（解析）：遍历代理引用，用 path 在索引中查找真实目标，替换代理
//
// 元数据驱动（对齐 ARCHITECTURE.md）：xmlName / APRXML 规则从 EAnnotation 经
// EAnnotationReader 读取，取代 Java 端的 AutosarXMLRuleRegistry / 常量表。
#include "emf/artop/runtime/AutosarXMLLoader.h"
#include <cstdint>
#include "emf/artop/runtime/UnknownElement.h"
#include "emf/artop/runtime/AutosarLibraryIndex.h"
#include "emf/artop/runtime/AutosarResource.h"

#include "emf/ecore/codegen/EAnnotationReader.h"
#include "emf/ecore/EcorePackage.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/common/EPackageRegistry.h"
#include "emf/common/EList.h"
#include "emf/xmi/XMIResource.h"
#include "emf/xmi/XMIOptions.h"

#include <algorithm>
#include <atomic>
#include <cctype>
#include <chrono>
#include <cstring>
#include <functional>
#include <iostream>
#include <malloc.h>
#include <mutex>
#include <optional>
#include <sstream>
#include <stdexcept>
#include <string>
#include <typeinfo>
#include <unordered_map>
#include <unordered_set>
#include <vector>

#include "pugixml.hpp"

namespace emf::artop::runtime {

// ===== Profiling helpers（仅 ARXML_BENCH_DEBUG 时启用）=====
// 轻量实现：用 thread_local 累加，避免 mutex 开销。
struct PhaseStat {
    long long ns = 0;
    long long count = 0;
};
struct Profiler {
    std::unordered_map<std::string, PhaseStat> stats;
    static Profiler& instance() {
        static Profiler p;
        return p;
    }
    void record(const char* name, long long deltaNs) {
        auto& s = stats[name];
        s.ns += deltaNs;
        s.count += 1;
    }
    void dump() {
        for (auto& kv : stats) {
            std::fprintf(stderr, "[PROF] %-30s count=%-8lld total=%-8.0f ms avg=%.4f ms\n",
                kv.first.c_str(), kv.second.count,
                kv.second.ns / 1e6,
                kv.second.count ? kv.second.ns / 1e6 / kv.second.count : 0.0);
        }
    }
};
struct PhaseTimer {
    const char* name;
    std::chrono::high_resolution_clock::time_point start;
    PhaseTimer(const char* n) : name(n) {
        if (profilerEnabled())
            start = std::chrono::high_resolution_clock::now();
    }
    ~PhaseTimer() {
        if (!profilerEnabled()) return;
        auto end = std::chrono::high_resolution_clock::now();
        long long delta = std::chrono::duration_cast<std::chrono::nanoseconds>(end - start).count();
        Profiler::instance().record(name, delta);
    }
    static bool profilerEnabled() {
        static bool e = std::getenv("ARXML_BENCH_DEBUG") != nullptr;
        return e;
    }
};
#define ARXML_PROFILE(name) PhaseTimer _pt_##__LINE__(name)

// Mixed text store: EObject* → mixed content 文本。
// 对齐 Java EMF mixed FeatureMap 的 TEXT 条目：C++ codegen 未生成 "mixed" feature，
// 且不能修改 EObjectImpl 布局（会破坏与已编译 libautosar448.a 的 ABI），
// 改用全局 map 存储，loader 写入、saver 读取。
std::unordered_map<emf::common::EObject*, std::string>& mixedTextStore() {
    static std::unordered_map<emf::common::EObject*, std::string> m;
    return m;
}

// Comment store: EObject* → 前置注释列表（在第一个子元素之前的 XML 注释）。
// 对齐 Java EMF eObjectToExtensionMap + AnyType.mixed FeatureMap 的 COMMENT 条目：
// C++ 端无 FeatureMap 机制，改用全局 map 存储（类似 mixedTextStore）。
// 仅收集前置注释（第一个 element 子节点之前的注释），对齐 Java XMLHandler 的
// handleComment 行为：注释在 mixedTargets 栈中被收集，与子元素顺序一致。
std::unordered_map<emf::common::EObject*, std::vector<std::string>>& commentStore() {
    static std::unordered_map<emf::common::EObject*, std::vector<std::string>> m;
    return m;
}

// Mixed content 序列存储：EObject* → 有序的 (TEXT/COMMENT/ELEMENT) 条目列表。
// 对齐 Java EMF mixed FeatureMap：mixed content EClass 的子节点（文本、注释、元素）
// 以有序序列保存，序列化时按原顺序原样输出（包括空白缩进）。
// 这解决了 pugixml 统一重新格式化缩进的问题：Java EMF 保留原始空白（含 TAB）。
struct MixedContentEntry {
    enum Kind { kText, kComment, kElement } kind;
    std::string text;                    // kText: 文本（含空白）; kComment: 注释文本
    emf::common::EObject* child;         // kElement: 子对象（containment/reference）
};
std::unordered_map<emf::common::EObject*, std::vector<MixedContentEntry>>& mixedContentStore() {
    static std::unordered_map<emf::common::EObject*, std::vector<MixedContentEntry>> m;
    return m;
}

// REF isDefault 存储：按 proxy → isDefault（bool）。
// 对齐 Java RelativeReference.isDefault：
//   - 无 ReferenceBase 解析（absPath 空）：isDefault=true，Saver 不输出 BASE
//   - 有 ReferenceBase 解析（absPath 非空）：isDefault=ReferenceBase.IS-DEFAULT，
//     IS-DEFAULT=false 时 Saver 输出 BASE，IS-DEFAULT=true 时不输出
// Loader 在 resolvePendingRefs 3b 分支设置，Saver 查询决定是否输出 BASE。
std::unordered_map<emf::common::EObject*, bool>& refIsDefaultStore() {
    static std::unordered_map<emf::common::EObject*, bool> m;
    return m;
}

// REF 原始 DEST 存储：按 (owner, feature, target) 三元组 → 原始 DEST 值。
// 对齐 Java：REF 元素的 DEST 属性保留原始值（可能是抽象基类，如 ECUC-DEFINITION-ELEMENT），
// 不用目标对象的实际 eClass() xmlName 重新计算。
// 同一 target 可能被多个 REF 引用（如 LC-OBJECT-REF DEST="ECUC-DEFINITION-ELEMENT" 和
// DESTINATION-REF DEST="ECUC-PARAM-CONF-CONTAINER-DEF" 指向同一对象），各 REF 独立保留 DEST，
// 因此 key 必须包含 owner 和 feature，不能只按 target。
// Loader 在 handleReferenceElement 时存 (owner, ref, proxy)→DEST，replaceProxy 时转移到 (owner, ref, target)。
// Saver 输出 DEST 时优先查询此 map，无则用 target eClass() xmlName。
// key 格式："<owner_ptr>:<ref_ptr>:<target_ptr>"
std::string refDestKey(emf::common::EObject* owner, emf::ecore::EStructuralFeature* ref,
                       emf::common::EObject* target) {
    std::ostringstream os;
    os << reinterpret_cast<uintptr_t>(owner) << ":"
       << reinterpret_cast<uintptr_t>(ref) << ":"
       << reinterpret_cast<uintptr_t>(target);
    return os.str();
}
std::unordered_map<std::string, std::string>& refDestStore() {
    static std::unordered_map<std::string, std::string> m;
    return m;
}

// Proxy 注册表：resource → 该 resource 创建的所有 proxy EObject。
// Loader 在 createProxyFromNode 时注册，resource 析构时统一 delete。
// 对齐 Java：Java proxy 由 GC 回收，C++ 需显式管理。
// Proxy 是非 containment 引用的占位对象，不在 eContents() 树中，
// 故 resource 析构的 DFS 不会收集它们，需独立追踪。
std::unordered_map<emf::common::Resource*, std::vector<emf::common::EObject*>>& proxyStore() {
    static std::unordered_map<emf::common::Resource*, std::vector<emf::common::EObject*>> m;
    return m;
}

// 清理本 resource 的 EObject 在全局 store 中的条目。
// AutosarXMLResource 析构时调用：EObject 释放前清理 store 条目，
// 避免跨轮累积（EObject* key 释放后变悬挂指针，map 节点永不释放）。
// refDestStore 的 key 是 "owner:ref:target" 十进制指针串，需解析 owner/target 判断归属。
void clearAutosarStoresForObjects(const std::unordered_set<emf::common::EObject*>& objs) {
    for (auto* o : objs) {
        mixedTextStore().erase(o);
        commentStore().erase(o);
        mixedContentStore().erase(o);
        refIsDefaultStore().erase(o);
    }
    // refDestStore: key="owner:ref:target"（uintptr 十进制串），遍历一次 erase 含本 resource EObject 的条目
    auto& rds = refDestStore();
    for (auto it = rds.begin(); it != rds.end(); ) {
        const std::string& key = it->first;
        size_t c1 = key.find(':');
        size_t c2 = (c1 == std::string::npos) ? std::string::npos : key.find(':', c1 + 1);
        if (c1 == std::string::npos || c2 == std::string::npos) { ++it; continue; }
        try {
            uintptr_t ownerPtr = std::stoull(key.substr(0, c1));
            uintptr_t targetPtr = std::stoull(key.substr(c2 + 1));
            auto* owner = reinterpret_cast<emf::common::EObject*>(ownerPtr);
            auto* target = reinterpret_cast<emf::common::EObject*>(targetPtr);
            if (objs.count(owner) || objs.count(target)) {
                it = rds.erase(it);
                continue;
            }
        } catch (...) {
            // 解析失败跳过
        }
        ++it;
    }
    // 收缩 bucket 数组：erase 不释放 bucket，多轮 load 后 bucket 持续累积。
    // rehash(0) 让 unordered_map 按当前元素数重新分配最小 bucket（空 map → 最小 bucket）。
    mixedTextStore().rehash(0);
    commentStore().rehash(0);
    mixedContentStore().rehash(0);
    refIsDefaultStore().rehash(0);
    refDestStore().rehash(0);
}

// ===== Fallback registry: EClass* ↔ xml.name（对齐 Java ExtendedMetaData.getType）=====
// 部分 EClass 的 EAnnotation 在生成的 Package init 代码中缺失（旧版代码生成器 bug，
// 如 Collection_class_ 无 TaggedValues 注解），导致按注解查找失败。
// Fallback：用工厂创建临时实例，读取 eXmlName()（生成代码硬编码的正确值），
// 建立 xml.name ↔ EClass* 映射。惰性构建（仅一次），供 Loader/Saver 共享。
// 注意：必须在匿名 namespace 之外（外部链接），供 Saver extern 引用。
//
// 性能优化：此缓存现在覆盖所有 EClass（含抽象类，用注解 xml.name），
// 作为 findEClassByXmlNameRecursive 的主查找路径（O(1) 哈希），
// 避免每次暴力扫描 420 个包的 1925 个 EClass（原 O(N) → 现 O(1)）。
// 对齐 Java ExtendedMetaData 的全局类型索引。
struct XmlNameFallback {
    std::unordered_map<emf::ecore::EClass*, std::string> classToName;
    std::unordered_map<std::string, emf::ecore::EClass*> nameToClass;
    bool built = false;
};
XmlNameFallback& xmlNameFallback() {
    static XmlNameFallback r;
    return r;
}
static void buildXmlNameFallbackFromPkg(emf::ecore::EPackage* pkg) {
    if (!pkg) return;
    auto& r = xmlNameFallback();
    for (auto* c : pkg->getEClassifiers()) {
        auto* cls = dynamic_cast<emf::ecore::EClass*>(c);
        if (!cls) continue;
        if (r.classToName.count(cls)) continue;  // 已注册
        // 优先从 EAnnotation 读取 xml.name（覆盖抽象类与非抽象类）
        std::string xn;
        auto* tv = cls->getEAnnotation("TaggedValues");
        if (tv) xn = tv->getDetail("xml.name");
        if (xn.empty()) {
            auto* emd = cls->getEAnnotation(
                "http:///org/eclipse/emf/ecore/util/ExtendedMetaData");
            if (emd) xn = emd->getDetail("name");
        }
        // 注解缺失时，用工厂创建临时实例读 eXmlName()（仅非抽象类可用）
        if (xn.empty() && !cls->isAbstract()) {
            auto* fpkg = cls->getEPackage();
            auto* factory = fpkg ? fpkg->getEFactoryInstance() : nullptr;
            if (factory) {
                auto* tmp = factory->create(cls);
                if (tmp) {
                    xn = tmp->eXmlName();
                    delete tmp;
                }
            }
        }
        if (!xn.empty()) {
            r.classToName[cls] = xn;
            // 首个注册的同名 EClass 优先（保持与原扫描逻辑一致：先遍历的包先命中）
            if (!r.nameToClass.count(xn)) r.nameToClass[xn] = cls;
        }
    }
    for (auto* sp : pkg->getESubpackages()) {
        buildXmlNameFallbackFromPkg(sp);
    }
}
void buildXmlNameFallback(emf::ecore::EPackage* root) {
    auto& r = xmlNameFallback();
    if (r.built) return;
    r.built = true;
    buildXmlNameFallbackFromPkg(root);
    // 生成的 420 个 autosar40 子包是平级的（各自注册到 EPackageRegistry），
    // 不是根包的子包。遍历 EPackageRegistry 中所有包，对 nsURI 以
    // "http://autosar.org/schema/r4.0" 开头的包构建 fallback，确保所有 EClass
    // 可被 findEClassByXmlNameRecursive 的 fallback 路径找到。
    const std::string prefix = "http://autosar.org/schema/r4.0";
    for (auto* pkg : emf::common::EPackageRegistry::instance().values()) {
        if (!pkg) continue;
        const std::string& nsURI = pkg->getNsURI();
        if (nsURI.rfind(prefix, 0) != 0) continue;
        auto* ecorePkg = dynamic_cast<emf::ecore::EPackage*>(pkg);
        if (ecorePkg) buildXmlNameFallbackFromPkg(ecorePkg);
    }
}

namespace {

// AUTOSAR R4.0 命名空间（与 arxml 根 xmlns 一致）
constexpr const char* kAutosarNsURI = "http://autosar.org/schema/r4.0";
// 根元素名
constexpr const char* kAutosarRootElement = "AUTOSAR";
// shortName 的常见 feature 名 / xml 名
constexpr const char* kShortNameFeature = "shortName";
constexpr const char* kShortNameXmlName = "SHORT-NAME";
// REF 元素的 DEST 属性（声明引用目标的 EClass 名）
constexpr const char* kDestAttr = "DEST";

// ===== pugi::xml_node 访问辅助（对齐 XMILoader.cpp 的同名辅助）=====

// 仅取 local 部分（"ns:LOCAL" -> "LOCAL"）
std::string getNodeLocal(const pugi::xml_node& node) {
    const char* name = node.name();
    const char* colon = std::strchr(name, ':');
    return colon ? std::string(colon + 1) : std::string(name);
}

// 获取元素的累积文本（pcdata + cdata）
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

// 去掉首尾空白
std::string trim(const std::string& s) {
    size_t b = 0;
    while (b < s.size() && std::isspace((unsigned char)s[b])) ++b;
    size_t e = s.size();
    while (e > b && std::isspace((unsigned char)s[e - 1])) --e;
    return s.substr(b, e - b);
}

// ===== XML 解析器（基于 pugixml，对齐 XMILoader.cpp::XmlParser）=====
// 注意：XmlParser 必须在返回的 pugi::xml_node 使用期间保持存活（持有 doc_ 缓冲区）。
class XmlParser {
public:
    pugi::xml_node parse(const std::string& s) {
        extractEncoding(s);
        unsigned flags = pugi::parse_default | pugi::parse_ws_pcdata | pugi::parse_comments;
        pugi::xml_parse_result result =
            doc_.load_buffer(s.data(), s.size(), flags, pugi::encoding_auto);
        if (!result) {
            throw std::runtime_error(std::string("AutosarXMLLoader: pugixml parse error: ") +
                                     result.description());
        }
        for (pugi::xml_node child : doc_.children()) {
            if (child.type() == pugi::node_element) return child;
        }
        throw std::runtime_error("AutosarXMLLoader: no root element");
    }

    const std::string& getEncoding() const { return encoding_; }

private:
    pugi::xml_document doc_;
    std::string encoding_;

    void extractEncoding(const std::string& in) {
        size_t pos = 0;
        while (pos < in.size() && std::isspace((unsigned char)in[pos])) ++pos;
        if (pos + 5 <= in.size() && in[pos] == '<' && in[pos + 1] == '?') {
            size_t declEnd = pos;
            while (declEnd < in.size() &&
                   !(in[declEnd] == '?' && declEnd + 1 < in.size() &&
                     in[declEnd + 1] == '>')) {
                ++declEnd;
            }
            if (declEnd + 1 < in.size()) {
                std::string decl = in.substr(pos, declEnd - pos);
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

// ===== 元数据驱动辅助 =====

// 在 EPackage（含子包）中按 xml.name 找 EClass。
// 优先按 EClassifier 名直接匹配，失败则按 EAnnotation 的 TaggedValues.xml.name 匹配。
// 对齐 Java AutosarSAXXMLHandler 的元素名 → EClass 映射。
emf::ecore::EClass* findEClassByXmlName(emf::ecore::EPackage* pkg, const std::string& xmlName) {
    if (!pkg) return nullptr;
    // 0. 优先查全局缓存（O(1) 哈希，对齐 Java ExtendedMetaData 全局类型索引）
    if (xmlNameFallback().built) {
        auto it = xmlNameFallback().nameToClass.find(xmlName);
        if (it != xmlNameFallback().nameToClass.end()) return it->second;
    }
    // 1. 直接按名匹配
    for (auto* c : pkg->getEClassifiers()) {
        if (c && c->getName() == xmlName) {
            if (auto* cls = dynamic_cast<emf::ecore::EClass*>(c)) return cls;
        }
    }
    // 2. 按 TaggedValues.xml.name / ExtendedMetaData.name 注解匹配（不走 readClassMeta 类名兜底）
    for (auto* c : pkg->getEClassifiers()) {
        auto* cls = dynamic_cast<emf::ecore::EClass*>(c);
        if (!cls) continue;
        auto* tv = cls->getEAnnotation("TaggedValues");
        if (tv && tv->getDetail("xml.name") == xmlName) return cls;
        auto* emd = cls->getEAnnotation(
            "http:///org/eclipse/emf/ecore/util/ExtendedMetaData");
        if (emd && emd->getDetail("name") == xmlName) return cls;
    }
    // 3. 递归子包
    for (auto* sp : pkg->getESubpackages()) {
        if (auto* found = findEClassByXmlName(sp, xmlName)) return found;
    }
    // 4. Fallback: 实例化注册表（首次触发构建）
    if (!xmlNameFallback().built) buildXmlNameFallback(pkg);
    auto it = xmlNameFallback().nameToClass.find(xmlName);
    if (it != xmlNameFallback().nameToClass.end()) return it->second;
    return nullptr;
}

// 递归查找 EClass(扩展版:同时按 ExtendedMetaData.name 匹配)
// 对齐 Java ExtendedMetaData.getType(namespace, name)
emf::ecore::EClass* findEClassByXmlNameRecursive(emf::ecore::EPackage* pkg, const std::string& xmlName) {
    if (!pkg) return nullptr;
    // 0. 优先查全局缓存（O(1) 哈希，对齐 Java ExtendedMetaData 全局类型索引）
    // 缓存在 ArxmlLoader 构造时已构建，覆盖所有 autosar40 EClass（含抽象类）。
    // 关键优化：缓存覆盖所有 420 个 autosar40 子包的 1925 个 EClass（通过 EPackageRegistry
    // 遍历构建），比递归扫描 getESubpackages() 更全面（生成的子包是平级注册的，非嵌套）。
    // 因此缓存 built 后，miss 即表示该名称不是任何 EClass 的 xml.name（可能是 feature 名
    // 如 SHORT-NAME/CATEGORY），直接返回 nullptr，避免对 1925 个 EClass 逐个做注解查找
    // （原 O(N) 递归扫描在 57K 次调用中导致 53 秒——大部分是 feature 名 miss 触发的全扫描）。
    if (xmlNameFallback().built) {
        auto it = xmlNameFallback().nameToClass.find(xmlName);
        if (it != xmlNameFallback().nameToClass.end()) return it->second;
        return nullptr;  // 缓存已覆盖所有 EClass，miss 即不存在
    }
    for (auto* c : pkg->getEClassifiers()) {
        auto* cls = dynamic_cast<emf::ecore::EClass*>(c);
        if (!cls) continue;
        // 直接查 TaggedValues 注解的 xml.name（不走 readClassMeta 的类名兜底，避免误匹配）
        auto* tv = cls->getEAnnotation("TaggedValues");
        if (tv && tv->getDetail("xml.name") == xmlName) return cls;
        // 按 ExtendedMetaData.name 匹配
        auto* emd = cls->getEAnnotation(
            "http:///org/eclipse/emf/ecore/util/ExtendedMetaData");
        if (emd && emd->getDetail("name") == xmlName) return cls;
    }
    for (auto* sp : pkg->getESubpackages()) {
        if (auto* found = findEClassByXmlNameRecursive(sp, xmlName)) return found;
    }
    // Fallback: 注解缺失时查询实例化注册表（对齐 Java 按类型 xml.name 查找）
    if (!xmlNameFallback().built) buildXmlNameFallback(pkg);
    auto it = xmlNameFallback().nameToClass.find(xmlName);
    if (it != xmlNameFallback().nameToClass.end()) return it->second;
    // 生成的 420 个 autosar40 子包是平级的，不通过 getESubpackages 关联。
    // 遍历 EPackageRegistry 中所有 autosar 包查找（对齐 Java 全局类型查找）。
    const std::string prefix = "http://autosar.org/schema/r4.0";
    for (auto* p : emf::common::EPackageRegistry::instance().values()) {
        if (!p) continue;
        const std::string& nsURI = p->getNsURI();
        if (nsURI.rfind(prefix, 0) != 0) continue;
        auto* ecorePkg = dynamic_cast<emf::ecore::EPackage*>(p);
        if (!ecorePkg || ecorePkg == pkg) continue;  // 已遍历
        for (auto* c : ecorePkg->getEClassifiers()) {
            auto* cls = dynamic_cast<emf::ecore::EClass*>(c);
            if (!cls) continue;
            auto* tv = cls->getEAnnotation("TaggedValues");
            if (tv && tv->getDetail("xml.name") == xmlName) return cls;
            auto* emd = cls->getEAnnotation(
                "http:///org/eclipse/emf/ecore/util/ExtendedMetaData");
            if (emd && emd->getDetail("name") == xmlName) return cls;
        }
    }
    return nullptr;
}

// 在 EClass 的 EAllStructuralFeatures 中按 xml 名匹配 feature。
// 先按 feature 名直接匹配，失败则按 EAnnotation 的 xml.name / xml.namePlural 匹配。
// 对齐 Java AutosarSAXXMLHandler 的元素名 → EStructuralFeature 映射。
// 注意：multi-valued feature 的 xml.name 是单数（如 "AR-PACKAGE"），
// xml.namePlural 是复数（如 "AR-PACKAGES"）。arxml 中 wrapper 元素用复数，
// 内层元素用单数，因此两种都要匹配。
//
// 性能优化：按 EClass* 缓存 xml.name → EStructuralFeature* 哈希表，
// 首次构建 O(F)（F = EAllStructuralFeatures 数），后续 O(1) 查找。
// 对齐 Java ExtendedMetaData 的 feature 索引。
struct FeatureXmlNameCache {
    // EClass* → (xml.name/xml.namePlural → EStructuralFeature*)
    std::unordered_map<emf::ecore::EClass*,
                       std::unordered_map<std::string, emf::ecore::EStructuralFeature*>> cache;
    // 标记已构建过的 EClass（含查找结果为空的 EClass，避免重复构建）
    std::unordered_set<emf::ecore::EClass*> built;
};
FeatureXmlNameCache& featureXmlNameCache() {
    static FeatureXmlNameCache c;
    return c;
}
emf::ecore::EStructuralFeature* findFeatureByXmlName(emf::ecore::EClass* cls,
                                                       const std::string& xmlName) {
    ARXML_PROFILE("findFeatureByXmlName");
    if (!cls) return nullptr;
    // 1. 直接按名匹配（覆盖最小 autosar40.ecore：feature 名即 xml 名，如 "AR-PACKAGE"）
    if (auto* sf = cls->getEStructuralFeature(xmlName)) return sf;
    // 2. 查 EClass 级缓存（O(1) 哈希）
    auto& fc = featureXmlNameCache();
    if (fc.built.count(cls)) {
        auto it = fc.cache.find(cls);
        if (it != fc.cache.end()) {
            auto fit = it->second.find(xmlName);
            if (fit != it->second.end()) return fit->second;
            return nullptr;  // 已构建但未命中，避免重复扫描
        }
        // built 但 cache 中无条目（全空），直接返回 nullptr
        return nullptr;
    }
    // 3. 首次构建：遍历 EAllStructuralFeatures，建立 xml.name/xml.namePlural 索引
    std::unordered_map<std::string, emf::ecore::EStructuralFeature*> m;
    for (auto* sf : cls->getEAllStructuralFeatures()) {
        if (!sf) continue;
        auto meta = emf::ecore::codegen::EAnnotationReader::readFeatureMeta(sf);
        if (!meta.xmlName.empty()) {
            m.emplace(meta.xmlName, sf);  // 不覆盖：首个优先（对齐 getEStructuralFeature 语义）
        }
        if (!meta.xmlNamePlural.empty() && meta.xmlNamePlural != meta.xmlName) {
            m.emplace(meta.xmlNamePlural, sf);
        }
    }
    fc.built.insert(cls);
    auto it = fc.cache.find(cls);
    if (it == fc.cache.end()) {
        it = fc.cache.emplace(cls, std::move(m)).first;
    }
    auto fit = it->second.find(xmlName);
    return (fit != it->second.end()) ? fit->second : nullptr;
}

// 在 EClass 的 EAllReferences 中按 xml 名匹配 EReference（仅引用类型）
emf::ecore::EReference* findReferenceByXmlName(emf::ecore::EClass* cls,
                                                 const std::string& xmlName) {
    auto* sf = findFeatureByXmlName(cls, xmlName);
    return dynamic_cast<emf::ecore::EReference*>(sf);
}

// 读取对象的 shortName 值（兼容 feature 名 "shortName" 与 "SHORT-NAME"，
// 以及带 xml.name="SHORT-NAME" 注解的 EAttribute）。
// 对齐 Java GReferrable.getShortName()。
std::string getShortNameValue(emf::common::EObject* obj) {
    if (!obj) return {};
    auto* cls = obj->eClass();
    if (!cls) return {};
    // 1. 直接查常见 feature 名
    if (auto* sf = cls->getEStructuralFeature(kShortNameFeature)) {
        std::any v = obj->eGet(sf);
        if (v.type() == typeid(std::string)) return std::any_cast<std::string>(v);
    }
    // 2. 查 "SHORT-NAME"（最小 autosar40.ecore 的 feature 名）
    if (auto* sf = cls->getEStructuralFeature(kShortNameXmlName)) {
        std::any v = obj->eGet(sf);
        if (v.type() == typeid(std::string)) return std::any_cast<std::string>(v);
    }
    // 3. 按 xml.name 注解查 EAttribute
    for (auto* a : cls->getEAllAttributes()) {
        if (!a) continue;
        auto meta = emf::ecore::codegen::EAnnotationReader::readFeatureMeta(a);
        if (meta.xmlName == kShortNameXmlName) {
            std::any v = obj->eGet(a);
            if (v.type() == typeid(std::string)) return std::any_cast<std::string>(v);
        }
    }
    return {};
}

// 判断 EStructuralFeature 是否从 XML 属性读取（isXmlAttribute 注解）
bool isXmlAttributeFeature(emf::ecore::EStructuralFeature* sf) {
    if (!sf) return false;
    auto meta = emf::ecore::codegen::EAnnotationReader::readFeatureMeta(sf);
    return meta.isXmlAttribute;
}

// 判断 EReference 是否为 0016(wrapper)/0012(role+type) 类型（用于 createFeatureFromSkippedElement）。
// 对齐 Java AutosarPersistenceRules.isCompositePropertyRepresentation00XX：
//   - isRoleWrapperElement（wrapper，对应 0016/0013）
//   - isRoleElement && isTypeElement（role+type，对应 0012）
bool isWrapperOrRoleTypeReference(emf::ecore::EReference* ref) {
    if (!ref) return false;
    auto meta = emf::ecore::codegen::EAnnotationReader::readFeatureMeta(ref);
    if (meta.isRoleWrapperElement) return true;  // 0016/0013 wrapper
    if (meta.isRoleElement && meta.isTypeElement) return true;  // 0012 role+type
    return false;
}

// 判断 EReference 是否为 0016 inline containment（4 个 xml 标志全 false）。
// 对齐 Java AutosarPersistenceRules.isCompositePropertyRepresentation0016：
//   当 4 标志全 false 时，被包含对象的内容直接内联到父元素的 XML 内容中，
//   无独立包装元素。例如：
//     Item.itemContents (eType=DocumentationBlock) → DocumentationBlock 内容内联到 <ITEM>
//     Compu.compuContent (eType=CompuContent 抽象) → 具体子类（如 CompuScales）的内容
//       通过 type element <COMPU-SCALES> 内联到 <COMPU-PHYS-TO-INTERNAL>
bool isInlineContainment(emf::ecore::EReference* ref) {
    if (!ref || !ref->isContainment()) return false;
    auto meta = emf::ecore::codegen::EAnnotationReader::readFeatureMeta(ref);
    return !meta.isRoleElement && !meta.isRoleWrapperElement
           && !meta.isTypeElement && !meta.isTypeWrapperElement;
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

// 收集 EClass 的所有子类型（扫描 EPackage 内所有 EClass，isSuperTypeOf 判定）。
// 对齐 Java createFeatureFromSkippedElement 在子类 references 中查找的逻辑。
// 性能优化：缓存 EClass* → subtypes 列表。
// collectSubtypes 原本每次递归遍历 420 个包的 1925 个 EClass 调 isSuperTypeOf，
// 在 createFeatureFromSkippedElement/tryInlineMatch 中被频繁调用。
// 缓存首次构建结果，后续 O(1) 返回（对齐 Java EClass.getESubtypes() 的缓存）。
std::unordered_map<emf::ecore::EClass*, std::vector<emf::ecore::EClass*>>& subtypeCache() {
    static std::unordered_map<emf::ecore::EClass*, std::vector<emf::ecore::EClass*>> c;
    return c;
}
void collectSubtypes(emf::ecore::EPackage* root, emf::ecore::EClass* base,
                     std::vector<emf::ecore::EClass*>& out) {
    if (!root || !base) return;
    // 查缓存
    auto& cache = subtypeCache();
    auto cit = cache.find(base);
    if (cit != cache.end()) {
        out.insert(out.end(), cit->second.begin(), cit->second.end());
        return;
    }
    // 首次构建：递归遍历所有包，收集 base 的所有子类型
    std::vector<emf::ecore::EClass*> collected;
    std::function<void(emf::ecore::EPackage*)> walk = [&](emf::ecore::EPackage* pkg) {
        if (!pkg) return;
        for (auto* c : pkg->getEClassifiers()) {
            auto* cls = dynamic_cast<emf::ecore::EClass*>(c);
            if (!cls || cls == base) continue;
            if (base->isSuperTypeOf(cls)) collected.push_back(cls);
        }
        for (auto* sp : pkg->getESubPackages()) {
            walk(sp);
        }
    };
    walk(root);
    // 也遍历 EPackageRegistry 中所有 autosar 包（生成的 420 个子包是平级的）
    const std::string prefix = "http://autosar.org/schema/r4.0";
    for (auto* p : emf::common::EPackageRegistry::instance().values()) {
        if (!p) continue;
        const std::string& nsURI = p->getNsURI();
        if (nsURI.rfind(prefix, 0) != 0) continue;
        auto* ecorePkg = dynamic_cast<emf::ecore::EPackage*>(p);
        if (!ecorePkg || ecorePkg == root) continue;
        walk(ecorePkg);
    }
    cache[base] = collected;
    out.insert(out.end(), collected.begin(), collected.end());
}

// 前向声明：nodeToXmlString 定义在 ArxmlLoader 之后，供其成员函数记录未知元素时调用
std::string nodeToXmlString(const pugi::xml_node& node);

// ===== 辅助：从 std::any 提取 EObject 列表（对齐 Saver 端 extractObjectList）=====
std::vector<emf::common::EObject*> extractObjectList(const std::any& v) {
    std::vector<emf::common::EObject*> r;
    if (!v.has_value()) return r;
    if (v.type() == typeid(emf::common::EObject*)) {
        auto* t = std::any_cast<emf::common::EObject*>(v);
        if (t) r.push_back(t);
        return r;
    }
    // EObjectRefView 零拷贝视图（eGet fast-path）
    if (v.type() == typeid(emf::common::EObjectRefView)) {
        auto view = std::any_cast<emf::common::EObjectRefView>(v);
        r.reserve(view.size());
        for (auto* p : view) r.push_back(p);
        return r;
    }
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

// ===== Loader 上下文 =====
struct ArxmlLoader {
    emf::xmi::XMIResource& res;
    const emf::xmi::XMIOptions& opts;
    // 已注册的 autosar40 根 EPackage（从 EPackageRegistry 查得）
    emf::ecore::EPackage* autosarPkg = nullptr;

    // short name path 索引："/PkgA/PkgB/Elem" -> EObject*
    std::unordered_map<std::string, emf::common::EObject*> pathIndex;

    // 待解析的代理引用（owner + feature + path + 代理对象）
    struct PendingRef {
        emf::common::EObject* owner;
        emf::ecore::EStructuralFeature* feature;
        std::string path;       // REF 文本（绝对或相对 short name path）
        std::string base;       // BASE 属性值（ReferenceBase 的 shortLabel，空表示绝对路径）
        bool isMany;
        emf::common::EObject* proxy;  // 已挂到 owner 的代理对象，解析后替换
    };
    std::vector<PendingRef> pendingRefs;

    // 最近创建的子对象（供 buildObject 的 mixed content 序列构建使用）。
    // handleContainment/handleReferenceElement 创建子对象后设置，
    // buildObject 据此在 mixed content 序列中记录 ELEMENT 条目。
    emf::common::EObject* lastCreatedChild_ = nullptr;

    explicit ArxmlLoader(emf::xmi::XMIResource& r, const emf::xmi::XMIOptions& o)
        : res(r), opts(o) {
        autosarPkg = dynamic_cast<emf::ecore::EPackage*>(
            emf::common::EPackageRegistry::instance().get(kAutosarNsURI));
        // 预构建 xml.name fallback 注册表（注解缺失的 EClass 通过实例化获取 xml.name）
        buildXmlNameFallback(autosarPkg);
        if (std::getenv("ARXML_BENCH_DEBUG")) {
            std::fprintf(stderr, "[BENCH] fallback built=%d, nameToClass size=%zu\n",
                (int)xmlNameFallback().built, xmlNameFallback().nameToClass.size());
        }
    }

    // ===== 主入口 =====
    void load(std::string xml) {
        emf::common::EObject* rootObj = nullptr;
        std::chrono::high_resolution_clock::time_point t1, t2;
        {
            XmlParser parser;
            pugi::xml_node root = parser.parse(xml);

            // parse 后立即释放原始字符串（pugixml load_buffer 已复制 buffer），
            // 避免 load 期间 string(400MB@400m) + pugixml DOM + EObject 三者并存 OOM。
            std::string().swap(xml);

            // 同步 encoding 到 resource（对齐 Java AutosarXMLLoadImpl 跟随输入文件）
            const std::string& enc = parser.getEncoding();
            if (!enc.empty()) res.setEncoding(enc);

            // 识别根元素 <AUTOSAR>（local name 必须为 "AUTOSAR"）
            std::string rootLocal = getNodeLocal(root);
            if (rootLocal != kAutosarRootElement) {
                // 非标准根：抛错（对齐 Java AutosarSAXXMLHandler 校验根元素）
                throw std::runtime_error(
                    "AutosarXMLLoader: 期望根元素 <AUTOSAR>，实际为 <" + rootLocal + ">");
            }
            if (!autosarPkg) {
                throw std::runtime_error(
                    "AutosarXMLLoader: autosar40 元模型未注册（nsURI=" +
                    std::string(kAutosarNsURI) + "），请先调用 "
                    "AutosarResourceFactory::registerDefaultAutosar40Metamodel()");
            }

            // 根元素名 → AUTOSAR EClass
            // 先用扩展版查找(按 xml.name + ExtendedMetaData.name 递归子包)
            auto* autosarClass = findEClassByXmlNameRecursive(autosarPkg, kAutosarRootElement);
            if (!autosarClass) {
                // 回退到原版查找(按 EClassifier 名)
                autosarClass = findEClassByXmlName(autosarPkg, kAutosarRootElement);
            }
            if (!autosarClass) {
                throw std::runtime_error(
                    "AutosarXMLLoader: 元模型中找不到 EClass \"AUTOSAR\"");
            }

            // 阶段 1：构建根对象树
            t1 = std::chrono::high_resolution_clock::now();
            rootObj = buildObject(root, autosarClass);
            t2 = std::chrono::high_resolution_clock::now();
            // parser 在此 scope 结束析构，pugixml DOM 立即释放（阶段 2/3 不再需要 DOM）。
            // 这是降低大文件内存峰值的关键：避免 pugixml DOM(~1.2GB@400m) 与 EObject 树
            // 在 save 阶段并存导致 OOM。
        }
        if (rootObj) {
            res.addToContents(rootObj);
        }
        // 归还 pugixml DOM 内存给 OS（glibc free 后未必立即归还，malloc_trim 强制归还）
        ::malloc_trim(0);

        // 阶段 2：建立 short name path 索引
        buildShortNamePathIndex();
        auto t3 = std::chrono::high_resolution_clock::now();

        // 阶段 3：解析代理引用
        resolvePendingRefs();
        auto t4 = std::chrono::high_resolution_clock::now();

        if (std::getenv("ARXML_BENCH_DEBUG")) {
            std::fprintf(stderr, "[BENCH] phase1(buildObject)=%.0f ms, phase2(pathIndex)=%.0f ms, phase3(resolveRefs)=%.0f ms\n",
                std::chrono::duration<double, std::milli>(t2 - t1).count(),
                std::chrono::duration<double, std::milli>(t3 - t2).count(),
                std::chrono::duration<double, std::milli>(t4 - t3).count());
            Profiler::instance().dump();
        }
    }

    // ===== 阶段 1：构建 EObject 树 =====
    // 根据 eClass 创建对象，应用 XML 属性，递归处理子元素。
    // 对齐 Java EMF: 用 eClass.getEPackage().getEFactoryInstance() 取所属子包的工厂，
    // 而非根包工厂——子包工厂的 create(EClass) override 才能分派到正确的生成 C++ 类。
    emf::common::EObject* buildObject(const pugi::xml_node& node,
                                       emf::ecore::EClass* eClass) {
        ARXML_PROFILE("buildObject");
        if (!eClass) return nullptr;
        auto* pkg = eClass->getEPackage();
        auto* factory = pkg ? pkg->getEFactoryInstance() : nullptr;
        if (!factory) return nullptr;
        emf::common::EObject* obj = nullptr;
        {
            ARXML_PROFILE("factory->create");
            obj = factory->create(eClass);
        }
        if (!obj) return nullptr;

        // 应用 XML 属性（isXmlAttribute=true 的 EAttribute）
        applyAttributes(obj, node);

        // 使用 obj->eContentKind() 而非 readClassMeta(eClass).contentKind：
        // 生成的 eStaticContentKind() 直接硬编码（来自 ecore ExtendedMetaData），
        // 比 readClassMeta 从运行时 EAnnotation 读取更可靠（EAnnotation 可能未设置）。
        bool isMixed = (obj->eContentKind() == "mixed");
        bool isSimple = (obj->eContentKind() == "simple");

        // ===== Simple content（对齐 Java XMLHandler 的 isSimpleFeature 路径）=====
        // contentKind=="simple" 的 EClass（如 Tt、ForeignModelReference）：
        //   元素文本通过 featureKind=="simple" 的 EAttribute（即 ExtendedMetaData name=":0"）
        //   存储。Java 在 startElement 时 getSimpleFeature(eClass) 找到 :0 feature，
        //   characters() 收集文本，endElement 时 setFeatureValue(obj, :0feature, text)。
        // C++ 端：applyAttributes 处理 XML 属性后，直接读元素文本设到 :0 feature。
        if (isSimple) {
            auto* simpleAttr = findSimpleFeature(eClass);
            if (simpleAttr) {
                std::string text = getNodeText(node);
                if (!text.empty()) {
                    setAttributeValue(obj, simpleAttr, text);
                }
            }
            return obj;
        }

        if (isMixed) {
            // mixed content：构建完整序列（TEXT/COMMENT/ELEMENT 按原始顺序）
            // 对齐 Java EMF mixed FeatureMap：保留原始空白（含 TAB 缩进）、注释、元素顺序。
            // 序列化时按原顺序输出，不依赖 pugixml 重新格式化。
            auto& seq = mixedContentStore()[obj];
            for (pugi::xml_node child : node.children()) {
                auto t = child.type();
                if (t == pugi::node_pcdata || t == pugi::node_cdata) {
                    std::string text = child.value();
                    if (!text.empty()) {
                        seq.push_back({MixedContentEntry::kText, text, nullptr});
                    }
                } else if (t == pugi::node_comment) {
                    seq.push_back({MixedContentEntry::kComment, child.value(), nullptr});
                } else if (t == pugi::node_element) {
                    lastCreatedChild_ = nullptr;
                    applyChildElement(obj, child);
                    seq.push_back({MixedContentEntry::kElement, "", lastCreatedChild_});
                }
            }
        } else {
            // 非 mixed content：收集前置注释 + 递归处理子元素（原有逻辑）
            {
                std::vector<std::string> comments;
                for (pugi::xml_node child : node.children()) {
                    if (child.type() == pugi::node_element) break;
                    if (child.type() == pugi::node_comment) {
                        comments.push_back(child.value());
                    }
                }
                if (!comments.empty()) {
                    commentStore()[obj] = std::move(comments);
                }
            }
            for (pugi::xml_node child : node.children()) {
                if (child.type() != pugi::node_element) continue;
                applyChildElement(obj, child);
            }
        }
        return obj;
    }

    // 应用 XML 属性到对象（对齐 Java AutosarSAXXMLHandler.handleAttributes）
    void applyAttributes(emf::common::EObject* obj, const pugi::xml_node& node) {
        ARXML_PROFILE("applyAttributes");
        auto* cls = obj->eClass();
        for (pugi::xml_attribute a : node.attributes()) {
            const char* aname = a.name();
            // 跳过 xmlns / xsi / DEST 等 XML 命名空间与引用专属属性
            if (std::strncmp(aname, "xmlns", 5) == 0) continue;       // xmlns / xmlns:*
            if (std::strncmp(aname, "xsi:", 4) == 0) continue;        // xsi:type 等
            if (std::strcmp(aname, kDestAttr) == 0) continue;         // REF 的 DEST 属性
            if (std::strcmp(aname, "BASE") == 0) continue;            // 相对引用 BASE（暂不支持）

            // 剥离命名空间前缀（如 "xml:space" -> "space"），对齐 Java SAX 的 local name
            std::string localName = aname;
            const char* colon = std::strchr(aname, ':');
            if (colon) localName = colon + 1;

            // 按属性名匹配 feature（arxml 属性名通常即 feature 名或 xml.name）
            auto* sf = findFeatureByXmlName(cls, localName);
            if (!sf) {
                // 未知属性：记录到 resource（对齐 OPTION_RECORD_UNKNOWN_FEATURE）
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
            // 仅 EAttribute 且 isXmlAttribute=true 时从属性读取；
            // EReference / 非 isXmlAttribute 的 feature 留给子元素处理
            auto* attr = dynamic_cast<emf::ecore::EAttribute*>(sf);
            if (!attr) continue;
            if (!isXmlAttributeFeature(sf)) continue;
            setAttributeValue(obj, attr, a.value());
        }
    }

    // 处理单个子元素（对齐 Java AutosarSAXXMLHandler 的元素分派）
    void applyChildElement(emf::common::EObject* obj, const pugi::xml_node& child) {
        ARXML_PROFILE("applyChildElement");
        auto* cls = obj->eClass();
        std::string local = getNodeLocal(child);

        // 1. 按 xml 名匹配 feature
        auto* sf = findFeatureByXmlName(cls, local);

        // 2. 直接匹配失败 → createFeatureFromSkippedElement（0016/0012 wrapper 匹配）
        if (!sf) {
            auto wrapped = createFeatureFromSkippedElement(cls, local);
            if (wrapped) {
                applyWrappedElement(obj, child, wrapped->outerRef, wrapped->innerRef);
                return;
            }
            // 3. 第 3 层 fallback：0016 inline containment 透传
            //    对齐 Java AutosarSAXXMLHandler 的 type element / inline feature 匹配
            if (tryInlineMatch(obj, cls, child)) {
                return;
            }
            // 真正未知：记录原始 XML 片段，供 saver round-trip
            if (res.isRecordUnknownFeature()) {
                res.addUnknownContent(obj, nodeToXmlString(child));
            }
            return;
        }

        // 3. 按 feature 类型分派
        if (auto* ref = dynamic_cast<emf::ecore::EReference*>(sf)) {
            if (ref->isContainment()) {
                handleContainment(obj, ref, child);
            } else {
                // 非 containment 引用元素（REF/TREF/IREF 风格）
                handleReferenceElement(obj, ref, child);
            }
            return;
        }

        // EAttribute：从子元素文本读取（isXmlAttribute=true 的已由 applyAttributes 处理）
        auto* attr = dynamic_cast<emf::ecore::EAttribute*>(sf);
        if (attr) {
            auto meta = emf::ecore::codegen::EAnnotationReader::readFeatureMeta(attr);
            // 判断 eType 是否为需保留空白的类型（如 VerbatimString）
            // 对齐 Java：VerbatimString 文档明确 "white-space needs to be preserved"
            auto* dt = attr->getEAttributeType();
            bool preserveWs = dt && dt->getName() == "VerbatimString";
            // 多值 EAttribute wrapper 模式（roleWrapperElement=true）：
            // child 名匹配 xmlNamePlural（复数 wrapper），内层子元素用 xmlName（单数）
            // 对齐 Java ARTOP <GLOBAL-ELEMENTS><GLOBAL-ELEMENT>val</GLOBAL-ELEMENT></GLOBAL-ELEMENTS>
            if (attr->isMany() && meta.isRoleWrapperElement
                && !meta.xmlNamePlural.empty() && local == meta.xmlNamePlural
                && local != meta.xmlName) {
                // 收集所有内层值到 vector 后一次性 eSet（生成的 eSet 对多值会 clear+重添）
                std::vector<std::string> collected;
                for (pugi::xml_node inner : child.children()) {
                    if (inner.type() != pugi::node_element) continue;
                    std::string innerText = preserveWs ? getNodeText(inner) : trim(getNodeText(inner));
                    if (!innerText.empty()) {
                        collected.push_back(std::move(innerText));
                    }
                }
                if (!collected.empty()) {
                    obj->eSet(attr, std::any(std::move(collected)));
                }
                return;
            }
            // 多值 EAttribute 非.wrapper：每个子元素一个值
            if (attr->isMany()) {
                std::string text = preserveWs ? getNodeText(child) : trim(getNodeText(child));
                if (!text.empty()) {
                    addAttributeValue(obj, attr, text);
                }
                return;
            }
            std::string text = preserveWs ? getNodeText(child) : trim(getNodeText(child));
            // 对齐 Java EMF：EAttribute 子元素存在即设置值（包括空字符串），
            // 区分"元素不存在"（未 set）与"元素存在但空"（set 为空）。
            // Saver 用 eIsSet 判断输出空元素（如 <DESTINATION-TYPE></DESTINATION-TYPE>）。
            setAttributeValue(obj, attr, text);
            return;
        }
    }

    // containment 引用处理：创建子对象并挂到父 feature（对齐 Java handleObjectAttribValue）
    void handleContainment(emf::common::EObject* obj, emf::ecore::EReference* ref,
                            const pugi::xml_node& node) {
        ARXML_PROFILE("handleContainment");
        // 检测 wrapper 模式（APRXML 0013/0016）：
        // 对齐 Java AutosarSAXXMLHandler 的 wrapper defer 机制：
        //   isRoleWrapperElement=true 的 feature 在外层 wrapper 元素（xml.namePlural）
        //   上 DEFER（不创建对象），内层 role 元素（xml.name 单数）REUSE deferred feature。
        // C++ 用 DOM 直接遍历：仅当元素名 == xml.namePlural（复数 wrapper 名）时为 wrapper，
        // 遍历其子元素逐个构建；元素名 == xml.name（单数 role 名）时为单个 role 元素。
        // autosar448.ecore 中所有 feature 的 xml.name != xml.namePlural（已验证），
        // 因此用 local == xmlNamePlural 区分 wrapper 是安全的。
        auto meta = emf::ecore::codegen::EAnnotationReader::readFeatureMeta(ref);
        std::string local = getNodeLocal(node);
        bool isWrapperElement = (meta.isRoleWrapperElement || meta.isTypeWrapperElement)
                                && ref->isMany()
                                && !meta.xmlNamePlural.empty()
                                && local == meta.xmlNamePlural;
        if (isWrapperElement) {
            // wrapper 模式：遍历子元素，每个子元素都是单数形式的 containment 对象
            // 对齐 Java wrapper defer + reuse：所有子元素归到同一个 multi-valued feature。
            // 关键修复：生成的 eSet 对多值 feature 只接受 vector<EObject*>（会 clear+重添），
            // 不接受单个 EObject*。因此收集所有子对象到 vector 后一次性 eSet，
            // 避免 eSet 多次调用互相覆盖（且每次传单值时类型不匹配导致静默丢弃）。
            std::vector<emf::common::EObject*> collected;
            for (pugi::xml_node child : node.children()) {
                if (child.type() != pugi::node_element) continue;
                std::string childLocal = getNodeLocal(child);
                // 子元素名应匹配单数 xmlName（或具体子类型名）
                emf::ecore::EClass* childClass = determineChildClass(child, ref);
                if (!childClass) {
                    if (res.isRecordUnknownFeature()) {
                        res.addUnknownContent(obj, nodeToXmlString(child));
                    }
                    continue;
                }
                auto* childObj = buildObject(child, childClass);
                if (!childObj) continue;
                collected.push_back(childObj);
                lastCreatedChild_ = childObj;
            }
            if (!collected.empty()) {
                // 追加到已有列表：同一 feature 可能有多个 wrapper 元素（如重复的 <AR-PACKAGES>），
                // 每个 wrapper 的子对象都应保留（对齐 Java EList.addAll）。
                appendBatchOrSet(obj, ref, std::move(collected));
            }
            return;
        }

        // 普通模式：node 直接是一个 containment 对象
        // 决定子对象的 EClass：默认用 ref.getEReferenceType()；
        // 支持 xsi:type 指定子类型；若声明类型抽象，尝试按元素名匹配具体子类。
        emf::ecore::EClass* childClass = determineChildClass(node, ref);
        if (!childClass) {
            // 无法确定类型：记录未知元素
            if (res.isRecordUnknownFeature()) {
                res.addUnknownContent(obj, nodeToXmlString(node));
            }
            return;
        }
        auto* childObj = buildObject(node, childClass);
        if (!childObj) return;
        addOrSet(obj, ref, childObj);
        lastCreatedChild_ = childObj;
    }

    // 决定 containment 子元素的 EClass（对齐 Java createObjectFromTypeName 的类型决策）
    // Java 通过 ExtendedMetaData.getType(eFactory, name) 按元素名查找 EClass，
    // 不依赖声明类型是否抽象。C++ 同样优先按元素名匹配，找不到再退到声明类型。
    emf::ecore::EClass* determineChildClass(const pugi::xml_node& node,
                                             emf::ecore::EReference* ref) {
        ARXML_PROFILE("determineChildClass");
        auto* declared = ref->getEReferenceType();
        // 1. xsi:type 显式指定子类型
        std::string xsiType = node.attribute("xsi:type").value();
        if (!xsiType.empty()) {
            auto colon = xsiType.find(':');
            if (colon != std::string::npos) xsiType = xsiType.substr(colon + 1);
            if (auto* c = findEClassByXmlName(autosarPkg, xsiType)) return c;
        }
        // 2. 按元素名匹配具体子类（对齐 Java ExtendedMetaData.getType）
        std::string local = getNodeLocal(node);
        if (!local.empty()) {
            emf::ecore::EClass* c = nullptr;
            { ARXML_PROFILE("determineChildClass::findEClass"); c = findEClassByXmlNameRecursive(autosarPkg, local); }
            if (c) {
                // 若有声明类型，校验 c 是 declared 的子类型（避免误匹配）
                if (!declared) return c;
                bool isSub = false;
                { ARXML_PROFILE("determineChildClass::isSuperTypeOf"); isSub = declared->isSuperTypeOf(c); }
                if (isSub) return c;
            }
        }
        // 3. 兜底：用声明的引用类型
        if (declared && !declared->isAbstract()) return declared;
        return declared;
    }

    // 非 containment 引用元素处理（REF/TREF/IREF）：
    //   DEST 属性 → EClass，文本 → short name path → 创建代理对象
    //   BASE 属性 → 相对路径前缀（ReferenceBase 的 shortLabel），不进 EMF 模型，
    //              解析时转为绝对路径（对齐 Java AutosarReferenceHelper）
    // 对齐 Java AutosarSAXXMLHandler.handleReference + createProxy
    void handleReferenceElement(emf::common::EObject* obj, emf::ecore::EReference* ref,
                                 const pugi::xml_node& node) {
        ARXML_PROFILE("handleReferenceElement");
        // 检测 wrapper 模式（APRXML 0013/0016）：
        // 与 handleContainment 保持一致的判断逻辑：仅当元素名 == xml.namePlural 时为 wrapper。
        // 对齐 Java AutosarSAXXMLHandler 的 wrapper defer 机制（DOM 直接遍历等价实现）。
        auto meta = emf::ecore::codegen::EAnnotationReader::readFeatureMeta(ref);
        std::string local = getNodeLocal(node);
        bool isWrapperElement = (meta.isRoleWrapperElement || meta.isTypeWrapperElement)
                                && ref->isMany()
                                && !meta.xmlNamePlural.empty()
                                && local == meta.xmlNamePlural;
        if (isWrapperElement) {
            // wrapper 模式：收集所有子 REF 的 proxy 到 vector 后一次性 eSet
            // （与 handleContainment 同样的理由：生成的 eSet 对多值 feature 只接受 vector）
            std::vector<emf::common::EObject*> collected;
            std::vector<PendingRef> pendingForWrapper;
            for (pugi::xml_node child : node.children()) {
                if (child.type() != pugi::node_element) continue;
                auto* proxy = createProxyFromNode(child, ref);
                if (!proxy) continue;
                collected.push_back(proxy);
                lastCreatedChild_ = proxy;
                // 保存原始 DEST（按 owner,ref,proxy 三元组，对齐 Java 保留原始 DEST 值）
                std::string childDest = child.attribute(kDestAttr).value();
                if (!childDest.empty()) {
                    refDestStore()[refDestKey(obj, ref, proxy)] = childDest;
                }
                std::string childPath = trim(getNodeText(child));
                std::string childBase = child.attribute("BASE").value();
                if (!childPath.empty()) {
                    pendingForWrapper.push_back({obj, ref, childPath, childBase, true, proxy});
                }
            }
            if (!collected.empty()) {
                // 追加到已有列表：同一 feature 可能有多个 wrapper 元素，每个都应保留
                appendBatchOrSet(obj, ref, std::move(collected));
            }
            for (auto& pr : pendingForWrapper) {
                pendingRefs.push_back(std::move(pr));
            }
            return;
        }

        auto* proxy = createProxyFromNode(node, ref);
        if (proxy) {
            addOrSet(obj, ref, proxy);
            // 保存原始 DEST（按 owner,ref,proxy 三元组，对齐 Java 保留原始 DEST 值）
            std::string dest = node.attribute(kDestAttr).value();
            if (!dest.empty()) {
                refDestStore()[refDestKey(obj, ref, proxy)] = dest;
            }
            std::string path = trim(getNodeText(node));
            std::string base = node.attribute("BASE").value();
            if (!path.empty()) {
                pendingRefs.push_back({obj, ref, path, base, ref->isMany(), proxy});
            }
            lastCreatedChild_ = proxy;
        }
    }

    // 从 REF 元素节点创建代理对象（DEST → EClass，文本 → proxyURI/path）
    emf::common::EObject* createProxyFromNode(const pugi::xml_node& node,
                                                emf::ecore::EReference* ref) {
        ARXML_PROFILE("createProxyFromNode");
        std::string dest = node.attribute(kDestAttr).value();
        std::string path = trim(getNodeText(node));

        // 解析 DEST → EClass（无 DEST 时用 ref.getEReferenceType 兜底）
        emf::ecore::EClass* targetClass = nullptr;
        if (!dest.empty()) {
            targetClass = findEClassByXmlName(autosarPkg, dest);
        }
        if (!targetClass) {
            targetClass = ref->getEReferenceType();
        }
        if (!targetClass) return nullptr;

        // 用 targetClass 所属子包的工厂，确保创建正确的生成 C++ 类
        auto* tpkg = targetClass->getEPackage();
        auto* factory = tpkg ? tpkg->getEFactoryInstance() : nullptr;
        if (!factory) return nullptr;
        auto* proxy = factory->create(targetClass);
        if (!proxy) return nullptr;
        if (auto* impl = dynamic_cast<emf::common::EObjectImpl*>(proxy)) {
            impl->eSetProxyURI(emf::common::URI(path));
        }
        // 注册到 proxyStore，供 resource 析构时统一 delete（proxy 不在 containment 树中）
        proxyStore()[&res].push_back(proxy);
        // DEST 由 handleReferenceElement 按 (owner, ref, proxy) 存储（需要对齐 Java 保留原始 DEST）。
        return proxy;
    }

    // createFeatureFromSkippedElement（模型驱动匹配未知元素）。
    // 对齐 Java AutosarSAXXMLHandler.createFeatureFromSkippedElement：
    //   对 owner 的每个 reference（逆序遍历）：
    //     if ref 是 0016(wrapper) 或 0012(role+type)：
    //       在 ref.getEReferenceType().getEAllReferences() 中找 qName 匹配的子 reference
    //       若没找到，遍历 ref.getEReferenceType() 的子类型，在每个子类的 references 里找
    //       命中则返回 [外层 wrapper ref, 内层子 ref]
    struct WrappedFeature {
        emf::ecore::EReference* outerRef;  // 外层 wrapper reference（owner 上的 containment）
        emf::ecore::EReference* innerRef;  // 内层子 reference（wrapper 类型上的引用）
    };

    std::optional<WrappedFeature> createFeatureFromSkippedElement(emf::ecore::EClass* ownerClass,
                                                                    const std::string& qname) {
        ARXML_PROFILE("createFeatureFromSkippedElement");
        if (!ownerClass) return std::nullopt;
        const auto& allRefs = ownerClass->getEAllReferences();
        // 逆序遍历 owner 的 reference
        for (auto it = allRefs.rbegin(); it != allRefs.rend(); ++it) {
            auto* ref = *it;
            if (!ref || !isWrapperOrRoleTypeReference(ref)) continue;
            auto* wrapperType = ref->getEReferenceType();
            if (!wrapperType) continue;

            // 在 wrapperType 的 EAllReferences 中找 qName 匹配的子 reference
            if (auto* inner = findReferenceByXmlName(wrapperType, qname)) {
                return WrappedFeature{ref, inner};
            }
            // 未找到 → 遍历 wrapperType 的子类型，在每个子类的 references 里找
            std::vector<emf::ecore::EClass*> subtypes;
            collectSubtypes(autosarPkg, wrapperType, subtypes);
            for (auto* sub : subtypes) {
                if (auto* inner = findReferenceByXmlName(sub, qname)) {
                    return WrappedFeature{ref, inner};
                }
            }
        }
        return std::nullopt;
    }

    // 处理 wrapper 包裹的元素（createFeatureFromSkippedElement 命中后）。
    // 语义：元素 qname 是外层 wrapper ref 类型上的内层 innerRef。
    //   - 创建 wrapper 对象（outerRef.getEReferenceType 实例，wrapper 无独立 XML 表示）
    //   - 根据 innerRef 类型构建内层内容（复用 handleContainment/handleReferenceElement，
    //     由它们内部的 buildObject/applyAttributes 处理 node 的属性与子元素）并挂到 wrapper.innerRef
    //   - wrapper 挂到 owner.outerRef
    void applyWrappedElement(emf::common::EObject* obj, const pugi::xml_node& node,
                              emf::ecore::EReference* outerRef,
                              emf::ecore::EReference* innerRef) {
        auto* wrapperType = outerRef->getEReferenceType();
        if (!wrapperType) return;
        // 用 wrapperType 所属子包的工厂创建 wrapper 对象
        auto* wpkg = wrapperType->getEPackage();
        auto* factory = wpkg ? wpkg->getEFactoryInstance() : nullptr;
        if (!factory) return;
        auto* wrapperObj = factory->create(wrapperType);
        if (!wrapperObj) return;

        // 内层 innerRef 的处理：把当前元素当作 wrapperObj 上对应 innerRef 的内容。
        // handleContainment/handleReferenceElement 内部会处理 node 的属性与子元素，
        // 构建出的内层对象挂到 wrapperObj.innerRef。
        if (innerRef->isContainment()) {
            handleContainment(wrapperObj, innerRef, node);
        } else {
            // innerRef 是非 containment 引用：元素文本即 path
            handleReferenceElement(wrapperObj, innerRef, node);
        }

        // wrapper 挂到 owner.outerRef
        addOrSet(obj, outerRef, wrapperObj);
    }

    // ===== 第 3 层 fallback：0016 inline containment 透传 =====
    // 对齐 Java AutosarSAXXMLHandler 处理 0016 inline feature 的逻辑：
    //   当 owner 有 0016 inline containment feature（4 标志全 false）时，
    //   被包含对象的内容直接内联到 owner 的 XML 内容中，无独立包装元素。
    //
    // 两种匹配场景：
    //   A) 元素名匹配 inlineType 的某个 feature
    //      例：Item.itemContents (0016, eType=DocumentationBlock)
    //          <P> 匹配 DocumentationBlock.ps (xml.name="P")
    //      → 获取或创建 inline 对象，在其上下文中处理 child
    //
    //   B) 元素名匹配某个 EClass 的 xml.name，且该 EClass 是 inlineType 的子类型
    //      例：Compu.compuContent (0016, eType=CompuContent 抽象)
    //          <COMPU-SCALES> 匹配 CompuScales (CompuContent 的子类, xml.name="COMPU-SCALES")
    //      → 创建该子类型的对象，处理其属性与子元素，挂到 owner.inlineRef

    // 创建 EObject（不读 XML 节点，仅工厂创建）
    emf::common::EObject* createEObject(emf::ecore::EClass* eClass) {
        if (!eClass) return nullptr;
        auto* pkg = eClass->getEPackage();
        auto* factory = pkg ? pkg->getEFactoryInstance() : nullptr;
        if (!factory) return nullptr;
        return factory->create(eClass);
    }

    // 获取或创建 owner 上 inlineRef 的对象（单值 inline containment）
    // 多次调用复用同一对象（对齐 Java EMF 的 containment 反向引用一致性）
    emf::common::EObject* getOrCreateInlineObject(emf::common::EObject* owner,
                                                    emf::ecore::EReference* inlineRef) {
        if (!owner || !inlineRef) return nullptr;
        // 检查是否已有值（单值 containment）
        std::any v = owner->eGet(inlineRef);
        auto existing = extractObjectList(v);
        if (!existing.empty() && existing[0]) return existing[0];
        // 创建新对象
        auto* inlineType = inlineRef->getEReferenceType();
        if (!inlineType || inlineType->isAbstract()) return nullptr;
        auto* obj = createEObject(inlineType);
        if (!obj) return nullptr;
        addOrSet(owner, inlineRef, obj);
        return obj;
    }

    // 递归尝试 inline 匹配（depth 防止无限递归）
    bool tryInlineMatch(emf::common::EObject* owner, emf::ecore::EClass* ownerClass,
                         const pugi::xml_node& child, int depth = 0) {
        ARXML_PROFILE("tryInlineMatch");
        if (!owner || !ownerClass || depth > 8) return false;
        std::string local = getNodeLocal(child);

        for (auto* sf : ownerClass->getEAllStructuralFeatures()) {
            auto* inlineRef = dynamic_cast<emf::ecore::EReference*>(sf);
            if (!inlineRef || !isInlineContainment(inlineRef)) continue;
            auto* inlineType = inlineRef->getEReferenceType();
            if (!inlineType) continue;

            // Case A: 元素名匹配 inlineType 的某个 feature（P case）
            if (findFeatureByXmlName(inlineType, local)) {
                auto* inlineObj = getOrCreateInlineObject(owner, inlineRef);
                if (inlineObj) {
                    applyChildElement(inlineObj, child);
                    return true;
                }
            }

            // Case D: 0016 containment 反向 inline 匹配（独立于 Case B，先于 Case B 判断）
            //   对齐 Java ExtendedSAXXMLHandler2.handleFeature fallback：
            //     createObjectFromFactory + 遍历 EAllReferences 找 eType.isInstance 匹配。
            //   当元素名不匹配 inlineType 的 feature（Case A 未命中），但匹配 inlineType 某个子类型
            //   的 feature 时（如 VT 匹配 CompuConstTextContent.vt，COMPU-CONST 匹配
            //   CompuScaleConstantContents.compuConst，COMPU-RATIONAL-COEFFS 匹配
            //   CompuScaleRationalFormula.compuRationalCoeffs）：
            //     创建该子类型实例，设置到 0016 feature，再处理元素作为该子类型的 feature。
            //   注意：不依赖 inlineType->isAbstract()（代码生成器可能未正确设置 abstract 标志），
            //   也不依赖元素名是否匹配某个 EClass（元素名可能是 EAttribute 的 xml.name）。
            {
                // 1. 复用已有 inline 对象（同一父节点下多个 sibling feature 共享一个 inline 容器）
                std::any cur = owner->eGet(inlineRef);
                auto existing = extractObjectList(cur);
                for (auto* ex : existing) {
                    if (ex && findFeatureByXmlName(ex->eClass(), local)) {
                        applyChildElement(ex, child);
                        return true;
                    }
                }
                // 单值且已有不同子类型的对象：不覆盖，跳过此 inlineRef
                if (existing.empty() || inlineRef->isMany()) {
                    // 2. 搜索 inlineType 的所有子类型，找拥有匹配元素名 feature 的子类型
                    std::vector<emf::ecore::EClass*> subtypes;
                    collectSubtypes(autosarPkg, inlineType, subtypes);
                    for (auto* subtype : subtypes) {
                        if (!findFeatureByXmlName(subtype, local)) continue;
                        auto* inlineObj = createEObject(subtype);
                        if (!inlineObj) continue;
                        addOrSet(owner, inlineRef, inlineObj);
                        // 3. 处理元素作为该子类型的 feature（递归 applyChildElement，
                        //    会找到匹配的 containment/reference/attribute 并处理）
                        applyChildElement(inlineObj, child);
                        return true;
                    }
                }
            }

            // Case B: 元素名匹配 EClass 的 xml.name，且该 EClass 是 inlineType 的子类型
            //         （COMPU-SCALES case）
            if (auto* eClass = findEClassByXmlNameRecursive(autosarPkg, local)) {
                if (inlineType->isSuperTypeOf(eClass)) {
                    auto* inlineObj = createEObject(eClass);
                    if (!inlineObj) continue;
                    // 处理属性与子元素（复用 buildObject 的逻辑，但不重新创建对象）
                    applyAttributes(inlineObj, child);
                    for (pugi::xml_node grandchild : child.children()) {
                        if (grandchild.type() != pugi::node_element) continue;
                        applyChildElement(inlineObj, grandchild);
                    }
                    // mixed content 文本捕获
                    if (inlineObj->eContentKind() == "mixed") {
                        std::string text = getNodeText(child);
                        if (!text.empty() && !trim(text).empty()) {
                            mixedTextStore()[inlineObj] = text;
                        }
                    }
                    addOrSet(owner, inlineRef, inlineObj);
                    return true;
                }
            }

            // Case C: 递归 inline（多层 0016 嵌套）
            //         仅当 inlineType 具体时可下钻
            if (!inlineType->isAbstract()) {
                auto* inlineObj = getOrCreateInlineObject(owner, inlineRef);
                if (inlineObj && tryInlineMatch(inlineObj, inlineObj->eClass(), child, depth + 1)) {
                    return true;
                }
            }
        }
        return false;
    }

    // 设置 EAttribute 值（字符串 → 目标类型，对齐 Java EFactory.createFromString）
    // 用数据类型所属子包的工厂，确保 enum/自定义类型转换正确
    void setAttributeValue(emf::common::EObject* obj, emf::ecore::EAttribute* attr,
                            const std::string& raw) {
        ARXML_PROFILE("setAttributeValue");
        if (!attr || !obj) return;
        auto* dt = attr->getEAttributeType();
        auto* dtPkg = dt ? dt->getEPackage() : nullptr;
        auto* factory = dtPkg ? dtPkg->getEFactoryInstance() : (autosarPkg ? autosarPkg->getEFactoryInstance() : nullptr);
        if (dt && factory) {
            try {
                std::any v = factory->createFromString(dt, raw);
                obj->eSet(attr, v);
                return;
            } catch (const std::exception&) {
                // 转换失败 → 兜底按字符串存储
            }
        }
        obj->eSet(attr, std::any(raw));
    }

    // 多值 EAttribute 追加单个值（对齐 Java EList.add on EAttribute）
    // 生成的 eSet(int) 接受 vector<T>（替换）或 T（追加），故用单值 T 触发追加。
    // 先用 factory.createFromString 把字符串转目标类型，再 eSet。
    void addAttributeValue(emf::common::EObject* obj, emf::ecore::EAttribute* attr,
                            const std::string& raw) {
        if (!attr || !obj) return;
        // 多值 EAttribute：生成的 eSet 会 clear+重添，不能直接 eSet 单值。
        // eGet 返回内部 EList（可变），直接 add 到内部列表（O(1)）。
        // 对齐 Java EList.add 的追加语义。
        if (attr->isMany()) {
            auto any = obj->eGet(attr);
            if (any.type() == typeid(emf::common::EList<std::string>*)) {
                auto* elist = std::any_cast<emf::common::EList<std::string>*>(any);
                if (elist) {
                    // 转换为属性类型（如 enum）后追加；字符串类型直接追加
                    auto* dt = attr->getEAttributeType();
                    auto* dtPkg = dt ? dt->getEPackage() : nullptr;
                    auto* factory = dtPkg ? dtPkg->getEFactoryInstance() : nullptr;
                    if (dt && factory) {
                        try {
                            std::any v = factory->createFromString(dt, raw);
                            if (v.type() == typeid(std::string)) {
                                elist->add(std::any_cast<std::string>(v));
                                return;
                            }
                        } catch (const std::exception&) {}
                    }
                    elist->add(raw);  // 兜底按字符串追加
                    return;
                }
            }
            // eGet 未返回 EList<string>*（其他类型）：fall back to eSet + accumulate
            // 收集当前值 + 新值到 vector 后 eSet
            std::vector<std::string> v;
            // （简化：仅处理 string 类型，其他类型的多值 EAttribute 暂未覆盖）
            v.push_back(raw);
            try {
                obj->eSet(attr, std::any(std::move(v)));
            } catch (const std::exception&) {}
            return;
        }
        // 单值 EAttribute：直接 eSet
        auto* dt = attr->getEAttributeType();
        auto* dtPkg = dt ? dt->getEPackage() : nullptr;
        auto* factory = dtPkg ? dtPkg->getEFactoryInstance() : (autosarPkg ? autosarPkg->getEFactoryInstance() : nullptr);
        if (dt && factory) {
            try {
                std::any v = factory->createFromString(dt, raw);
                obj->eSet(attr, v);
                return;
            } catch (const std::exception&) {
                // 转换失败 → 兜底按字符串
            }
        }
        try {
            obj->eSet(attr, std::any(raw));
        } catch (const std::exception&) {
            // 兜底也失败：忽略
        }
    }

    // 多值 feature 追加 / 单值 feature 设置（对齐 Java EList.add / eSet）
    void addOrSet(emf::common::EObject* obj, emf::ecore::EStructuralFeature* sf,
                  emf::common::EObject* value) {
        ARXML_PROFILE("addOrSet");
        if (!obj || !sf || !value) return;
        if (sf->isMany()) {
            // 多值 feature：生成的 eSet 只接受 vector<EObject*>（会 clear+重添），
            // 不接受单个 EObject*。因此 eGet 当前列表 → 提取到 vector → 追加新值 → eSet 一次。
            // 对齐 Java EList.add 的追加语义。O(n²) 但保证正确性。
            // 注：eGet 返回的指针不应 delete（DynamicEObject 内部管理；codegen 副本由 eSet 回写后丢弃）。
            std::vector<emf::common::EObject*> v;
            auto any = obj->eGet(sf);
            if (any.has_value()) {
                if (any.type() == typeid(emf::common::EList<emf::common::EObject*>*)) {
                    auto* elist = std::any_cast<emf::common::EList<emf::common::EObject*>*>(any);
                    if (elist) {
                        for (size_t i = 0; i < elist->size(); ++i) v.push_back((*elist)[i]);
                    }
                } else if (any.type() == typeid(emf::common::EObjectRefView)) {
                    // EObjectRefView 零拷贝视图（瘦身方案2：多值 reference eGet 返回此类型）
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
        } else {
            // 单值 EReference：eSet 替换
            obj->eSet(sf, std::any(value));
        }
    }

    // 多值 EReference 批量追加（对齐 Java EList.addAll）：
    // 同一 multi-valued feature 可能对应多个 wrapper 元素（如重复的 <AR-PACKAGES>），
    // 每个 wrapper 的子对象都应追加到已有列表而非覆盖（生成的 eSet 对多值是 clear+重添）。
    // 提取当前列表 → 追加全部新值 → eSet 一次。
    // 注：eGet 返回的指针不应 delete（DynamicEObject 内部管理；codegen 副本由 eSet 回写后丢弃）。
    void appendBatchOrSet(emf::common::EObject* obj, emf::ecore::EReference* ref,
                          std::vector<emf::common::EObject*> values) {
        if (!obj || !ref || values.empty()) return;
        if (!ref->isMany()) {
            obj->eSet(ref, std::any(values.back()));
            return;
        }
        std::vector<emf::common::EObject*> v;
        auto any = obj->eGet(ref);
        if (any.has_value()) {
            if (any.type() == typeid(emf::common::EList<emf::common::EObject*>*)) {
                auto* elist = std::any_cast<emf::common::EList<emf::common::EObject*>*>(any);
                if (elist) {
                    for (size_t i = 0; i < elist->size(); ++i) v.push_back((*elist)[i]);
                }
            } else if (any.type() == typeid(emf::common::EObjectRefView)) {
                // EObjectRefView 零拷贝视图（瘦身方案2：多值 reference eGet 返回此类型）
                auto view = std::any_cast<emf::common::EObjectRefView>(any);
                for (auto* p : view) v.push_back(p);
            } else if (any.type() == typeid(std::vector<emf::common::EObject*>)) {
                v = std::any_cast<std::vector<emf::common::EObject*>>(any);
            } else if (any.type() == typeid(std::vector<emf::common::EObject*>*)) {
                auto* p = std::any_cast<std::vector<emf::common::EObject*>*>(any);
                if (p) v = *p;
            }
        }
        for (auto* c : values) v.push_back(c);
        obj->eSet(ref, std::any(std::move(v)));
    }

    // ===== 阶段 2：建立 short name path 索引 =====
    // 遍历 resource 内所有 EObject，为每个有非空 shortName 的 GReferrable
    // 沿 eContainer 链收集 shortName，建立 path → EObject* 映射。
    // 对齐 Java ReferenceHelper.buildShortNamePathIndex。
    void buildShortNamePathIndex() {
        for (auto* root : res.getContents()) {
            indexObject(root);
        }
    }

    // 递归索引 obj 及其 eContents
    void indexObject(emf::common::EObject* obj, int depth = 0) {
        if (!obj) return;
        std::string sn = getShortNameValue(obj);
        if (!sn.empty()) {
            std::string path = buildShortNamePath(obj);
            if (!path.empty()) {
                pathIndex[path] = obj;
            }
        }
        // 拷贝 eContents 以避免 eContentsCache_ 引用失效
        auto contents = obj->eContents();
        for (size_t i = 0; i < contents.size(); ++i) {
            indexObject(contents[i], depth + 1);
        }
    }

    // 沿 eContainer 链收集 shortName，构建绝对路径 "/sn1/sn2/.../snN"
    std::string buildShortNamePath(emf::common::EObject* obj) {
        std::vector<std::string> parts;
        emf::common::EObject* cur = obj;
        while (cur) {
            std::string sn = getShortNameValue(cur);
            if (!sn.empty()) parts.push_back(sn);
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

    // ===== 阶段 3：解析代理引用 =====
    // 两阶段：
    //   3a. 解析无 BASE 的 pendingRefs（绝对路径，直接在 pathIndex 查找）
    //   3b. 解析有 BASE 的 pendingRefs（相对路径）：
    //       沿 owner.eContainer 链找最近的 ARPackage，遍历其 referenceBases
    //       找 shortLabel == base 的 ReferenceBase，读其 package 引用（已解析）
    //       的绝对 path 作为 prefix，最终 path = prefix + "/" + relative
    // 对齐 Java AutosarReferenceHelper.convertRelativeReferenceToAbsoluteReference
    void resolvePendingRefs() {
        // 对齐 Java ARTOP demand-load：跨文档引用通过全局 Library 索引解析。
        // AutosarLibraryIndex 由预加载的 library resource 注册（indexResource），
        // 路径未在本资源 pathIndex 命中时回退到全局索引查询，命中则替换 proxy。
        auto& libIndex = AutosarLibraryIndex::instance();

        // 对齐 Java ResourceSet demand-load：若本资源属于 ResourceSet，
        // 遍历 ResourceSet 中所有尚未 indexLibrary 的 autosar resource 并索引，
        // 使跨文档引用可解析（懒索引：用户未显式 loadLibrary 时也能解析）。
        if (auto* rs = res.getResourceSet()) {
            for (auto& r : rs->getResources()) {
                if (r.get() == &res) continue;  // 跳过自己
                if (auto* ar = dynamic_cast<AutosarResource*>(r.get())) {
                    // 检查是否已索引（用任意已知 path 探测，避免重复索引）
                    // 简单策略：AutosarLibraryIndex::indexResource 幂等性由调用者保证，
                    // 这里直接调 indexLibrary（内部 indexObject 会覆盖重复 key，无副作用）
                    ar->indexLibrary();
                }
            }
        }

        // 3a. 绝对路径
        for (auto& pr : pendingRefs) {
            if (!pr.base.empty()) continue;  // 留给 3b
            if (!pr.owner || !pr.feature) continue;
            emf::common::EObject* target = nullptr;
            auto it = pathIndex.find(pr.path);
            if (it != pathIndex.end()) {
                target = it->second;
            } else if (libIndex.contains(pr.path)) {
                // 跨文档 demand-load：从全局 Library 索引解析（对齐 Java ARTOP
                // AutosarReferenceHelper.resolveProxy 跨资源查找）
                target = libIndex.lookup(pr.path);
            }
            if (!target) continue;
            replaceProxy(pr, target);
            pr.owner = nullptr;  // 标记已处理
        }
        // 3b. BASE 相对路径
        for (auto& pr : pendingRefs) {
            if (pr.base.empty()) continue;
            if (!pr.owner || !pr.feature) continue;
            auto resolved = resolveRelativePath(pr.owner, pr.base, pr.path);
            if (resolved.absPath.empty()) {
                // ReferenceBase 未解析（无 ReferenceBase 配置或跨文档引用）。
                // 保留原始 BASE + 相对路径到 proxyURI，格式：autosar-proxy://base=XXX/path=YYY
                // isDefault=true（对齐 Java：无 ReferenceBase 时 isDefault=true，Saver 不输出 BASE）
                if (auto* impl = dynamic_cast<emf::common::EObjectImpl*>(pr.proxy)) {
                    std::string encoded = "autosar-proxy://base=" + pr.base + "/path=" + pr.path;
                    impl->eSetProxyURI(emf::common::URI(encoded));
                }
                refIsDefaultStore()[pr.proxy] = true;
                pr.owner = nullptr;
                continue;
            }
            // 即使目标未找到（跨文档引用），也更新 proxyURI 为绝对路径，
            // 供 Saver 反向计算 BASE + 相对文本（对齐 Java 未解析 proxy 行为）
            if (auto* impl = dynamic_cast<emf::common::EObjectImpl*>(pr.proxy)) {
                impl->eSetProxyURI(emf::common::URI(resolved.absPath));
            }
            refIsDefaultStore()[pr.proxy] = resolved.isDefault;
            emf::common::EObject* target = nullptr;
            auto it = pathIndex.find(resolved.absPath);
            if (it != pathIndex.end()) {
                target = it->second;
            } else if (libIndex.contains(resolved.absPath)) {
                // 跨文档 demand-load：从全局 Library 索引解析
                target = libIndex.lookup(resolved.absPath);
            }
            if (!target) {
                pr.owner = nullptr;  // 标记已处理（proxy 保留，URI 已更新）
                continue;
            }
            // 转移 isDefault 从 proxy 到 target
            auto idIt = refIsDefaultStore().find(pr.proxy);
            if (idIt != refIsDefaultStore().end()) {
                refIsDefaultStore()[target] = idIt->second;
            }
            replaceProxy(pr, target);
            pr.owner = nullptr;
        }
        pendingRefs.clear();
    }

    // 解析 BASE 相对路径为绝对路径。
    // 对齐 Java AutosarReferenceHelper.convertRelativeReferenceToAbsoluteReference：
    //   沿 owner.eContainer 链找最近的 ARPackage，遍历其 referenceBases，
    //   按 shortLabel == base 找 ReferenceBase，读其 package 引用的绝对 path。
    // 返回 {absPath, isDefault}：isDefault = ReferenceBase.IS-DEFAULT（未找到 ReferenceBase 时 true）。
    struct ResolvedRelative {
        std::string absPath;
        bool isDefault = true;
    };
    ResolvedRelative resolveRelativePath(emf::common::EObject* owner, const std::string& base,
                                          const std::string& relative) {
        ResolvedRelative result;
        if (!owner || base.empty() || relative.empty()) return result;
        // 沿 eContainer 链找 ARPackage
        emf::common::EObject* cur = owner;
        while (cur) {
            auto* cls = cur->eClass();
            std::string curName = cls ? cls->getName() : std::string("null");
            if (cls && (curName == "ARPackage")) {
                // 在 ARPackage.referenceBases 中找 shortLabel == base 的 ReferenceBase
                auto* refBasesFeat = cls->getEStructuralFeature("referenceBases");
                if (!refBasesFeat) {
                    // 按 xml.name 找
                    for (auto* sf : cls->getEAllStructuralFeatures()) {
                        if (!sf) continue;
                        auto m = emf::ecore::codegen::EAnnotationReader::readFeatureMeta(sf);
                        if (m.xmlNamePlural == "REFERENCE-BASES" || m.xmlName == "REFERENCE-BASE") {
                            refBasesFeat = sf;
                            break;
                        }
                    }
                }
                if (refBasesFeat) {
                    std::any v = cur->eGet(refBasesFeat);
                    auto refs = extractObjectList(v);
                    for (auto* rb : refs) {
                        if (!rb) continue;
                        std::string sl = getFeatureStringValue(rb, "shortLabel");
                        if (sl != base) continue;
                        // 命中 ReferenceBase：读其 package 引用的绝对 path
                        std::string prefix = getReferenceBasePrefix(rb);
                        if (!prefix.empty()) {
                            result.absPath = prefix + "/" + relative;
                            // 读 IS-DEFAULT（默认 false）
                            std::string isDef = getFeatureStringValue(rb, "isDefault");
                            result.isDefault = (isDef == "true");
                            return result;
                        }
                    }
                }
            }
            cur = cur->eContainer();
        }
        return result;
    }

    // 读 EObject 上某字符串/标量 feature 的字符串值（按 feature 名匹配）
    // 支持 string/bool/int 等（对齐 Java EObject.eGet + toString）
    std::string getFeatureStringValue(emf::common::EObject* obj, const std::string& featName) {
        if (!obj) return {};
        auto* cls = obj->eClass();
        if (!cls) return {};
        auto* sf = cls->getEStructuralFeature(featName);
        if (!sf) {
            // 按 xml.name 找
            for (auto* a : cls->getEAllAttributes()) {
                if (!a) continue;
                auto m = emf::ecore::codegen::EAnnotationReader::readFeatureMeta(a);
                std::string xn = m.xmlName.empty() ? a->getName() : m.xmlName;
                if (xn == featName || a->getName() == featName) {
                    sf = a;
                    break;
                }
            }
        }
        if (!sf) return {};
        std::any v = obj->eGet(sf);
        if (v.type() == typeid(std::string)) return std::any_cast<std::string>(v);
        if (v.type() == typeid(bool)) return std::any_cast<bool>(v) ? "true" : "false";
        if (v.type() == typeid(int)) return std::to_string(std::any_cast<int>(v));
        if (v.type() == typeid(int32_t)) return std::to_string(std::any_cast<int32_t>(v));
        if (v.type() == typeid(int64_t)) return std::to_string(std::any_cast<int64_t>(v));
        return {};
    }

    // 读 ReferenceBase 的 prefix：
    //   - baseIsThisPackage=true → prefix = ReferenceBase 所在 ARPackage 的绝对 path
    //   - 否则 → prefix = ReferenceBase.package 引用的绝对 path
    // 对齐 Java AutosarReferenceHelper 的 baseIsThisPackage 分支
    std::string getReferenceBasePrefix(emf::common::EObject* refBase) {
        if (!refBase) return {};
        bool isThisPkg = false;
        std::string bitp = getFeatureStringValue(refBase, "BASE-IS-THIS-PACKAGE");
        if (bitp == "true") isThisPkg = true;
        // 也尝试按 feature 名
        if (!isThisPkg) {
            std::string v = getFeatureStringValue(refBase, "baseIsThisPackage");
            if (v == "true" || v == "1") isThisPkg = true;
        }
        if (isThisPkg) {
            // prefix = 所在 ARPackage 的绝对 path
            emf::common::EObject* pkg = refBase->eContainer();
            while (pkg && pkg->eClass() && pkg->eClass()->getName() != "ARPackage") {
                pkg = pkg->eContainer();
            }
            return buildShortNamePath(pkg);
        }
        // prefix = ReferenceBase.package 引用的绝对 path
        auto* cls = refBase->eClass();
        auto* pkgFeat = cls ? cls->getEStructuralFeature("package") : nullptr;
        if (!pkgFeat && cls) {
            for (auto* sf : cls->getEAllReferences()) {
                if (!sf) continue;
                auto m = emf::ecore::codegen::EAnnotationReader::readFeatureMeta(sf);
                if (m.xmlName == "PACKAGE-REF" || sf->getName() == "package") {
                    pkgFeat = sf;
                    break;
                }
            }
        }
        if (!pkgFeat) return {};
        std::any v = refBase->eGet(pkgFeat);
        auto refs = extractObjectList(v);
        if (refs.empty()) return {};
        emf::common::EObject* target = refs[0];
        if (!target) return {};
        // 跨文档引用：PACKAGE-REF target 是 proxy（未解析），proxyURI 即原始绝对路径
        // （如 "/AUTOSAR/AISpecification/KeywordSets_Blueprint"）。
        // 对齐 Java：ReferenceBase.package 的 GReferrable.getShortNamePath() 返回绝对路径，
        // 即使是 proxy 也用其 URI 文本（Java 端 demand-load 未触发时同理）。
        if (target->eIsProxy()) {
            if (auto* impl = dynamic_cast<emf::common::EObjectImpl*>(target)) {
                return impl->eProxyURI().toString();
            }
            return {};
        }
        return buildShortNamePath(target);
    }

    // 用 target 替换 owner.feature 中的 proxy
    void replaceProxy(const PendingRef& pr, emf::common::EObject* target) {
        // 转移原始 DEST 存储从 (owner,ref,proxy) 到 (owner,ref,target)
        // 对齐 Java 保留原始 DEST 值（可能是抽象基类，如 ECUC-DEFINITION-ELEMENT）
        std::string oldKey = refDestKey(pr.owner, pr.feature, pr.proxy);
        auto destIt = refDestStore().find(oldKey);
        if (destIt != refDestStore().end()) {
            refDestStore()[refDestKey(pr.owner, pr.feature, target)] = destIt->second;
            refDestStore().erase(destIt);
        }
        // 清理 proxy 在 refIsDefaultStore 中的条目（3b 未解析阶段可能已设置）
        refIsDefaultStore().erase(pr.proxy);
        if (pr.isMany) {
            std::any v = pr.owner->eGet(pr.feature);
            if (auto* elist = std::any_cast<emf::common::EList<emf::common::EObject*>*>(&v)) {
                if (*elist) {
                    int idx = (*elist)->indexOf(pr.proxy);
                    if (idx >= 0) {
                        (*elist)->set(idx, target);  // 原地替换代理
                    } else {
                        (*elist)->add(target);  // 兜底：找不到代理则追加
                    }
                }
            } else if (auto* listPtr =
                           std::any_cast<std::vector<emf::common::EObject*>*>(&v)) {
                if (*listPtr) {
                    auto& vec = **listPtr;
                    auto it = std::find(vec.begin(), vec.end(), pr.proxy);
                    if (it != vec.end()) {
                        *it = target;
                    } else {
                        vec.push_back(target);
                    }
                }
            } else if (v.type() == typeid(emf::common::EObjectRefView)) {
                // EObjectRefView（零拷贝视图）：需通过 eSet 回写替换。
                // 视图指向 EList 内部 vector，不能直接修改，需提取→替换→eSet。
                auto view = std::any_cast<emf::common::EObjectRefView>(v);
                std::vector<emf::common::EObject*> tmp(view.begin(), view.end());
                bool found = false;
                for (auto& p : tmp) {
                    if (p == pr.proxy) { p = target; found = true; break; }
                }
                if (!found) tmp.push_back(target);
                // eSet 回写（addOrSet 已有 EObjectRefView 分支处理）
                pr.owner->eSet(pr.feature, std::any(tmp));
            }
        } else {
            // 单值：eSet 直接覆盖代理
            pr.owner->eSet(pr.feature, std::any(target));
        }
        // proxy 的 delete 延迟到 resource 析构（proxyStore 统一回收），
        // 避免 eSet 回写过程中 EList callback deref 已 delete 的 proxy。
    }
};

// 把 pugi::xml_node 序列化回 XML 字符串（含子元素、属性、文本），用于记录未知元素。
// 对齐 Java OPTION_RECORD_UNKNOWN_FEATURE：保留原始 XML 片段供 saver 原样输出。
std::string nodeToXmlString(const pugi::xml_node& node) {
    std::string s;
    s += "<";
    s += node.name();
    for (pugi::xml_attribute a : node.attributes()) {
        s += " ";
        s += a.name();
        s += "=\"";
        const char* v = a.value();
        for (const char* p = v; *p; ++p) {
            if (*p == '"') s += "&quot;";
            else if (*p == '&') s += "&amp;";
            else if (*p == '<') s += "&lt;";
            else s += *p;
        }
        s += "\"";
    }
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
        s += "/>";
    } else {
        s += "</";
        s += node.name();
        s += ">";
    }
    return s;
}

}  // namespace

// ===== 公开入口 =====
void AutosarXMLLoader::load(emf::xmi::XMIResource* resource, std::istream& input,
                             const emf::xmi::XMIOptions& options) {
    if (!resource) return;

    // 读取整个输入流到字符串（对齐 XMILoader::loadInto 的块读取，避免 stringstream 双重复制）
    std::string xml;
    constexpr size_t kChunk = 1 << 20;  // 1MB chunks
    char buf[kChunk];
    while (true) {
        input.read(buf, kChunk);
        xml.append(buf, static_cast<size_t>(input.gcount()));
        if (input.gcount() < static_cast<std::streamsize>(kChunk)) break;
    }

    // 同步 recordUnknownFeature 标志到 resource（供 applyChildElement 检查）
    resource->setRecordUnknownFeature(options.recordUnknownFeature);

    ArxmlLoader loader(*resource, options);
    loader.load(std::move(xml));
}

}  // namespace emf::artop::runtime
