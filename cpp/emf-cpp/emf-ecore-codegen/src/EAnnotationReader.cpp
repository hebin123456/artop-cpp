// EAnnotationReader.cpp —— 从 EAnnotation 读取 AUTOSAR 序列化元数据
// 对齐 ARCHITECTURE.md：模型驱动取代 Java 常量表
//
// 实现要点：
//   - 注解 source = "TaggedValues"（AUTOSAR ecore 导出约定）
//   - 键名遵循 ecore 文件实际约定：xml.name / xml.namePlural / xml.sequenceOffset /
//     xml.roleElement / xml.roleWrapperElement / xml.typeElement / xml.typeWrapperElement /
//     xml.attribute / xml.nsPrefix / xml.nsUri / xml.ordered / xml.globalElement /
//     xml.extensionPoint / internal-xml-sequenceOffset
//   - 同时从 source="http:///org/eclipse/emf/ecore/util/ExtendedMetaData" 读取 kind/name
//   - aprxmlRule 从 roleElement/roleWrapperElement/typeElement 标志推导：
//       isXmlAttribute → APRXML0015 (5)
//       isRoleElement && isTypeElement → APRXML0012 (2)
//       isRoleWrapperElement && isTypeElement → APRXML0013 (3)
//       默认 → APRXML0015 (5)
//   - sequenceOffset 优先读 internal-xml-sequenceOffset（物理偏移），缺失时读 xml.sequenceOffset
#include "emf/ecore/codegen/EAnnotationReader.h"
#include "emf/ecore/EcoreImpls.h"

#include <cstdio>
#include <cstdlib>
#include <unordered_map>

namespace emf::ecore::codegen {

namespace {

// 从 EModelElement 读取 TaggedValues 注解的某个 detail
std::string getTaggedValue(emf::ecore::EModelElement* el, const std::string& key) {
    if (!el) return "";
    auto* ann = el->getEAnnotation("TaggedValues");
    if (!ann) return "";
    return ann->getDetail(key);
}

// 从 EModelElement 读取 ExtendedMetaData 注解的某个 detail
std::string getExtendedMetaData(emf::ecore::EModelElement* el, const std::string& key) {
    if (!el) return "";
    auto* ann = el->getEAnnotation("http:///org/eclipse/emf/ecore/util/ExtendedMetaData");
    if (!ann) return "";
    return ann->getDetail(key);
}

bool parseBool(const std::string& s) {
    return s == "true" || s == "1" || s == "True" || s == "TRUE";
}

int parseInt(const std::string& s) {
    if (s.empty()) return 0;
    try {
        return std::stoi(s);
    } catch (...) {
        return 0;
    }
}

}  // namespace

int EAnnotationReader::parseAprxmlRule(const FeatureMeta& m) {
    // 从 feature 标志推导 APRXML 规则（对齐 .build_cache 生成代码的 int 值）
    if (m.isXmlAttribute) return 5;  // APRXML0015
    if (m.isRoleElement && m.isTypeElement) return 2;  // APRXML0012
    if (m.isRoleWrapperElement && m.isTypeElement) return 3;  // APRXML0013
    return 5;  // APRXML0015 默认
}

PackageMeta EAnnotationReader::readPackageMeta(emf::ecore::EPackage* pkg) {
    PackageMeta m;
    if (!pkg) return m;
    // 优先从 TaggedValues 注解读取；缺失时回退到 EPackage 自身属性
    m.nsPrefix = getTaggedValue(pkg, "xml.nsPrefix");
    if (m.nsPrefix.empty()) m.nsPrefix = pkg->getNsPrefix();
    m.nsUri = getTaggedValue(pkg, "xml.nsUri");
    if (m.nsUri.empty()) m.nsUri = pkg->getNsURI();
    m.isQualified = parseBool(getTaggedValue(pkg, "xml.qualified"));
    return m;
}

ClassMeta EAnnotationReader::readClassMeta(emf::ecore::EClass* cls) {
    ClassMeta m;
    if (!cls) return m;
    m.xmlName         = getTaggedValue(cls, "xml.name");
    m.xmlNamePlural   = getTaggedValue(cls, "xml.namePlural");
    m.contentKind     = getExtendedMetaData(cls, "kind");
    m.namespace_      = getExtendedMetaData(cls, "namespace");
    m.nsPrefix        = getTaggedValue(cls, "xml.nsPrefix");
    m.nsUri           = getTaggedValue(cls, "xml.nsUri");
    m.stereotype      = getTaggedValue(cls, "Stereotype");
    m.isGlobalElement  = parseBool(getTaggedValue(cls, "xml.globalElement"));
    m.isExtensionPoint = parseBool(getTaggedValue(cls, "xml.extensionPoint"));
    m.isOrdered        = parseBool(getTaggedValue(cls, "xml.ordered"));
    m.splitkey         = getTaggedValue(cls, "atp.Splitkey");
    // 缺省 xmlName 用类名兜底（对齐 Java 行为）
    if (m.xmlName.empty()) m.xmlName = cls->getName();
    if (m.xmlNamePlural.empty()) m.xmlNamePlural = m.xmlName;
    return m;
}

FeatureMeta EAnnotationReader::readFeatureMeta(emf::ecore::EStructuralFeature* sf) {
    if (!sf) return FeatureMeta{};
    // 缓存：EStructuralFeature 的注解元数据在包初始化后不可变，按指针缓存避免
    // 每次调用都做 15+ 次 getEAnnotation + getDetail（哈希+线性扫描）。
    // 对齐 Java ExtendedMetaData 的元数据缓存：Java 端 feature 元数据只解析一次。
    // 安全性：EStructuralFeature 是包初始化时创建的单例，注解在 init 后只读，
    // 缓存键（指针）稳定且内容不变，不会破坏正确性。
    static std::unordered_map<emf::ecore::EStructuralFeature*, FeatureMeta> cache;
    auto it = cache.find(sf);
    if (it != cache.end()) return it->second;

    FeatureMeta m;
    m.xmlName           = getTaggedValue(sf, "xml.name");
    m.xmlNamePlural     = getTaggedValue(sf, "xml.namePlural");
    m.featureKind       = getExtendedMetaData(sf, "kind");
    m.namespace_        = getExtendedMetaData(sf, "namespace");
    // sequenceOffset 优先读 internal-xml-sequenceOffset（物理偏移），缺失或为 "null" 时读 xml.sequenceOffset
    // 对齐 Java：部分 feature 的 internal-xml-sequenceOffset="null"（占位符，表示未设置），
    // 此时用 xml.sequenceOffset（逻辑偏移）作为 fallback，保证 feature 顺序正确。
    std::string seqOff = getTaggedValue(sf, "internal-xml-sequenceOffset");
    if (seqOff.empty() || seqOff == "null") seqOff = getTaggedValue(sf, "xml.sequenceOffset");
    m.sequenceOffset    = parseInt(seqOff);
    m.isRoleElement     = parseBool(getTaggedValue(sf, "xml.roleElement"));
    m.isRoleWrapperElement = parseBool(getTaggedValue(sf, "xml.roleWrapperElement"));
    m.isTypeElement        = parseBool(getTaggedValue(sf, "xml.typeElement"));
    m.isTypeWrapperElement = parseBool(getTaggedValue(sf, "xml.typeWrapperElement"));
    // DEBUG: 检查 EAnnotation 读取
    if (std::getenv("ARXML_DEBUG_WRAPPER")) {
        auto* ann = sf->getEAnnotation("TaggedValues");
        std::fprintf(stderr, "[META] feat=%s xmlName=%s roleWrap=%d typeElem=%d ann=%p details=%zu\n",
            sf->getName().c_str(), m.xmlName.c_str(),
            (int)m.isRoleWrapperElement, (int)m.isTypeElement,
            ann, ann ? ann->getDetails().size() : 0);
    }
    m.isXmlAttribute       = parseBool(getTaggedValue(sf, "xml.attribute"));
    m.isTextContent        = parseBool(getTaggedValue(sf, "xml.text"));
    m.nsPrefix             = getTaggedValue(sf, "xml.nsPrefix");
    m.splitkey             = getTaggedValue(sf, "atp.Splitkey");
    // 缺省 xmlName 用 feature 名兜底
    if (m.xmlName.empty()) m.xmlName = sf->getName();
    if (m.xmlNamePlural.empty()) m.xmlNamePlural = m.xmlName;
    // 推导 aprxmlRule
    m.aprxmlRule = parseAprxmlRule(m);
    cache[sf] = m;
    return m;
}

int EAnnotationReader::getGeneralizationSequenceOffset(emf::ecore::EClass* cls,
                                                       const std::string& superName) {
    if (!cls || superName.empty()) return 0;
    // 注解键约定：super.<SuperName>.sequenceOffset
    std::string key = "super." + superName + ".sequenceOffset";
    return parseInt(getTaggedValue(cls, key));
}

}  // namespace emf::ecore::codegen
