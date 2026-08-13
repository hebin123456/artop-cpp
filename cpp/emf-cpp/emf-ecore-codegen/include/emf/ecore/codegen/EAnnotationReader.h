// EAnnotationReader.h —— 模型驱动：从 Ecore EAnnotation 读取 AUTOSAR 序列化元数据
// 对齐 ARCHITECTURE.md：
//   "C++ 用模型驱动：所有 xmlName / APRXML 规则 / atp.Splitkey / roleWrapperElement
//    从 EAnnotation 经 EAnnotationReader 读取"
//
// 取代 Java 侧的 AutosarXMLRuleRegistry / AutosarPersistenceRules /
// AutosarTaggedValues 常量表 —— 这些在 C++ 端没有对应物，由 EAnnotation 元数据机制取代。
//
// 注解约定（ecore 文件实际键名）：
//   source = "TaggedValues":
//     Package: xml.nsPrefix / xml.nsUri / xml.qualified
//     Class:   xml.name / xml.namePlural / xml.nsPrefix / xml.nsUri /
//              xml.globalElement / xml.extensionPoint / xml.ordered / Stereotype / atp.Splitkey
//     Feature: xml.name / xml.namePlural / xml.sequenceOffset / internal-xml-sequenceOffset /
//              xml.roleElement / xml.roleWrapperElement / xml.typeElement / xml.typeWrapperElement /
//              xml.attribute / xml.text / atp.Splitkey
//   source = "http:///org/eclipse/emf/ecore/util/ExtendedMetaData":
//     Class/Feature: name / kind / namespace / ordered
//
// APRXML 规则从标志推导（对齐 .build_cache 的 int 值）：
//   isXmlAttribute → 5 (APRXML0015)
//   isRoleElement && isTypeElement → 2 (APRXML0012)
//   isRoleWrapperElement && isTypeElement → 3 (APRXML0013)
//   默认 → 5 (APRXML0015)
#pragma once

#include "emf/ecore/EcorePackage.h"
#include <string>

namespace emf::ecore::codegen {

// 包级元数据
struct PackageMeta {
    std::string nsPrefix;
    std::string nsUri;
    bool isQualified = false;
};

// 类级元数据
struct ClassMeta {
    std::string xmlName;
    std::string xmlNamePlural;
    std::string contentKind;
    std::string namespace_;
    std::string nsPrefix;
    std::string nsUri;
    std::string stereotype;
    std::string splitkey;
    bool isGlobalElement   = false;
    bool isExtensionPoint  = false;
    bool isOrdered         = false;
};

// 特征级元数据
struct FeatureMeta {
    std::string xmlName;
    std::string xmlNamePlural;
    std::string featureKind;
    std::string namespace_;
    std::string splitkey;
    std::string nsPrefix;          // xml.nsPrefix（如 "xml" for xml:space）
    int  sequenceOffset         = 0;
    bool isRoleElement          = false;
    bool isRoleWrapperElement   = false;
    bool isTypeElement          = false;
    bool isTypeWrapperElement   = false;
    bool isXmlAttribute         = false;
    bool isTextContent          = false;
    int  aprxmlRule             = 0;  // 0=NONE, 2=APRXML0012, 3=APRXML0013, 5=APRXML0015
};

class EAnnotationReader {
public:
    // 读取 EPackage 的 TaggedValues 注解 → PackageMeta
    static PackageMeta readPackageMeta(emf::ecore::EPackage* pkg);

    // 读取 EClass 的 TaggedValues 注解 → ClassMeta
    static ClassMeta readClassMeta(emf::ecore::EClass* cls);

    // 读取 EStructuralFeature 的 TaggedValues 注解 → FeatureMeta
    static FeatureMeta readFeatureMeta(emf::ecore::EStructuralFeature* sf);

    // 读取 cls 对 superName 的泛化（继承）sequenceOffset
    static int getGeneralizationSequenceOffset(emf::ecore::EClass* cls,
                                               const std::string& superName);

    // 从 feature 标志推导 APRXML 规则 int 值
    static int parseAprxmlRule(const FeatureMeta& m);
};

}  // namespace emf::ecore::codegen
