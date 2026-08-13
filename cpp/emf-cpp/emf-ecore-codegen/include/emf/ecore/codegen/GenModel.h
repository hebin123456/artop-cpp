// GenModel.h —— 二次模型数据结构
// 对齐 Java: org.eclipse.emf.codegen.ecore.genmodel.*
//
// 这是 EMF 代码生成器的"二次模型"：在 Ecore 元模型之上又包了一层 GenModel
// 元模型，保存所有 codegen 用的控制信息（命名空间、模板风格、文件后缀、生成
// 标志位等等），并把每个 Ecore 元素（EPackage/EClass/EStructuralFeature/...）
// 桥接到一个 Gen* 元素。
//
// 1:1 对应 Java 侧：
//   GenModel      <-> org.eclipse.emf.codegen.ecore.genmodel.GenModel
//   GenPackage    <-> org.eclipse.emf.codegen.ecore.genmodel.GenPackage
//   GenClass      <-> org.eclipse.emf.codegen.ecore.genmodel.GenClass
//   GenFeature    <-> org.eclipse.emf.codegen.ecore.genmodel.GenFeature
//   GenEnum       <-> org.eclipse.emf.codegen.ecore.genmodel.GenEnum
//   GenDataType   <-> org.eclipse.emf.codegen.ecore.genmodel.GenDataType
//
// 真正的 GenModel EMF 模型（70+ Java 文件）我们不会 100% 翻译，仅实现 codegen
// 实际需要的子集：足以让 .genmodel XMI 加载、调度、走模板。
#pragma once

#include "emf/ecore/EcorePackage.h"
#include <memory>
#include <string>
#include <vector>

namespace emf::ecore::codegen {

class GenModel;
class GenPackage;
class GenClassifier;
class GenClass;
class GenFeature;
class GenEnum;
class GenEnumLiteral;
class GenDataType;
class GenOperation;
class GenParameter;
class GenBase;
class GenTypedElement;

// ===== GenModel: 顶层二次模型根 =====
// 对应 Java: GenModel（XML 根节点 <genmodel:GenModel>）
struct GenModel {
    std::string copyrightText;
    std::string modelDirectory;
    std::string modelPluginID;          // e.g. "org.example.library"
    std::string modelName;              // e.g. "Library"
    std::string basePackage;            // 字段 basePackage（GenModel 上可有可无）
    std::string editPluginID;
    std::string editorPluginID;
    std::string complianceLevel;        // "5.0"/"8.0"...
    std::string language = "C++";       // 我们扩：C++ 而不是 Java
    std::string runtimePlatform;        // "Cpp" 之类（自定义）

    // 行为标志
    bool creationCommands = true;
    bool creationIcons = true;
    bool creationSubmenus = false;
    bool runtimeJar = false;
    bool forceOverwrite = true;
    bool codeFormatting = false;
    bool commentFormatting = false;
    bool bundleManifest = true;
    bool updateClasspath = true;
    bool generateSchema = false;
    bool nonNLSMarkers = false;
    bool suppressInterfaces = false;
    bool suppressEMFTypes = false;
    bool suppressEMFMetaData = false;
    bool suppressEMFModelTags = false;
    bool suppressGenModelAnnotations = true;
    bool copyrightFields = true;
    bool binaryCompatibleReflectiveMethods = false;
    bool publicConstructors = true;
    bool containmentProxies = true;
    bool minimalReflectiveMethods = true;
    bool suppressContainment = false;
    bool suppressNotification = false;
    bool arrayAccessors = true;
    bool suppressUnsettable = false;
    bool richClientPlatform = false;
    bool reflectiveDelegation = false;
    bool runtimeCompatibility = true;

    // 关联 EPackage 列表（一个 GenModel 可有多个 GenPackage）
    std::vector<std::shared_ptr<GenPackage>> genPackages;

    // 辅助
    std::string getEffectiveBasePackage() const;
    const GenPackage* findGenPackageByNSURI(const std::string& nsURI) const;
};

// ===== GenBase: 所有 Gen* 元素的基类 =====
// 对应 Java: org.eclipse.emf.codegen.ecore.genmodel.GenBase
struct GenBase {
    GenModel* genModel = nullptr;       // 反向指针
    GenPackage* genPackage = nullptr;   // 所属 GenPackage
    std::string ecorePath;              // 元素在 .ecore 文件里的路径（XMI id 引用）
    std::string name;                   // 元素名（feature/classifier/...）
    std::string qualifiedName;          // 限定名（Class::Feature 形式）

    virtual ~GenBase() = default;
};

// ===== GenTypedElement: 带类型的元素基类 =====
// 对应 Java: GenTypedElement
struct GenTypedElement : GenBase {
    std::shared_ptr<emf::ecore::EClassifier> ecoreType;   // 类型 EClassifier（int/string/other Class）
    std::string type;                                     // 简化：typename 字符串（Java String）
    std::string defaultValueLiteral;
    bool unsettable = false;
    bool many = false;
    bool ordered = true;
    bool unique = true;
    int lowerBound = 0;
    int upperBound = 1;       // -1 表示 *
};

// ===== GenPackage: 二次模型中的"一个 EPackage" =====
// 对应 Java: GenPackage
struct GenPackage : GenBase {
    std::string prefix;                  // e.g. "Library"
    std::string basePackage;             // e.g. "com.example"（Java 路径用，我们用做 C++ 命名空间）
    std::string disposableProviderFactory = "true";
    std::string fileExtensions;
    std::string contentTypeIdentifier;

    // 关联
    std::shared_ptr<emf::ecore::EPackage> ecorePackage;  // 真正要生成的 EPackage

    // 派生的 GenClassifiers
    std::vector<std::shared_ptr<GenClass>>     genClasses;
    std::vector<std::shared_ptr<GenEnum>>      genEnums;
    std::vector<std::shared_ptr<GenDataType>>  genDataTypes;
    std::vector<std::shared_ptr<GenPackage>>   nestedGenPackages;

    // ---- 命名 helper（对齐 Java GenPackage.getImportedPackageInterfaceName / ...）----
    std::string getPackageInterfaceName() const;
    std::string getFactoryInterfaceName() const;
    std::string getSwitchInterfaceName() const;
    std::string getAdapterFactoryClassName() const;
    std::string getValidatorClassName() const;
    std::string getInterfacePackageName() const;   // C++ 命名空间（basePackage::prefix）
    std::string getClassPackageName() const;
    std::string getQualifiedPackageInterfaceName() const;  // basePackage::prefix::prefixPackage

    // 简化的列表
    std::vector<std::shared_ptr<GenClassifier>> getGenClassifiers() const;
};

// ===== GenClassifier: 抽象基类 =====
// 对应 Java: GenClassifier
struct GenClassifier : GenTypedElement {
    std::string instanceClassName;       // e.g. "com.example.Book"（Java）/ "Book"（C++）
    std::string instanceType;            // e.g. "Book" / "BookImpl"
    std::string classifierID;            // GenModel classifier ID（与 EMF 持久化有关，可空）
    std::shared_ptr<emf::ecore::EClassifier> ecoreClassifier;  // 桥接
};

// ===== GenClass: 二次模型中的 EClass =====
// 对应 Java: GenClass
struct GenClass : GenClassifier {
    std::shared_ptr<emf::ecore::EClass> ecoreClass;
    std::vector<std::shared_ptr<GenFeature>>    genFeatures;
    std::vector<std::shared_ptr<GenOperation>>  genOperations;

    std::string getClassName() const;        // C++ 简单类名
    std::string getInterfaceName() const;
    std::string getImplClassName() const;
    bool isAbstract() const;
    bool isInterface() const;
    bool isMapEntry() const;

    std::shared_ptr<GenClass> getBaseClass() const;  // 第一个 ESuperType -> GenClass
};

// ===== GenFeature: EStructuralFeature 的 GenModel 包装 =====
// 对应 Java: GenFeature
struct GenFeature : GenTypedElement {
    std::shared_ptr<emf::ecore::EStructuralFeature> ecoreFeature;
    GenClass* genClass = nullptr;

    // 标志位
    bool reference = false;      // 是 EReference（否则 EAttribute）
    bool attribute = true;       // 是 EAttribute
    bool containment = false;
    bool container = false;
    bool derived = false;
    bool transient_ = false;
    bool volatile_ = false;
    bool changeable = true;
    bool defaultValueProvider = false;
    bool notify = false;
    bool unsettable = false;
    bool resolveProxies = true;
    bool unique = true;
    bool ordered = true;
    bool suppressed = false;     // GenModel.featureKind == Suppressed

    // 名称 helper
    std::string getFeatureName() const;        // "books"
    std::string getAccessorName() const;       // "Books"（首字母大写）
    std::string getGetterName() const;         // "getBooks"
    std::string getSetterName() const;         // "setBooks"
    std::string getUncapSafeName() const;      // C++ 字段名
    std::string getFeatureID() const;          // 0-based（包内全局）
    std::string getQualifiedFeatureAccessor() const;  // "LibraryPackage::LIBRARY__BOOKS"
    std::string getCppType() const;            // C++ 容器/标量类型
    std::string getCppFieldType() const;       // 字段类型（去引用）
    std::string getCppDefaultValue() const;    // 默认值字面量
};

// ===== GenOperation =====
struct GenOperation : GenBase {
    std::string name;
    std::string returnType;
    std::shared_ptr<emf::ecore::EClassifier> ecoreType;
    std::vector<std::shared_ptr<GenParameter>> genParameters;
};

// ===== GenParameter =====
struct GenParameter : GenBase {
    std::string name;
    std::string type;
};

// ===== GenEnum =====
struct GenEnum : GenClassifier {
    std::vector<std::shared_ptr<GenEnumLiteral>> genEnumLiterals;
    std::string getEnumName() const;
};

// ===== GenEnumLiteral =====
struct GenEnumLiteral : GenBase {
    std::string name;
    std::string literal;
    int value = 0;
};

// ===== GenDataType =====
struct GenDataType : GenClassifier {
    std::string getDataTypeName() const;
};

}  // namespace emf::ecore::codegen
