// EcorePackage.cpp —— ecore 元模型包单例 + EcoreFactory 单例
// 对齐 org.eclipse.emf.ecore.impl.EcorePackageImpl + EcoreFactoryImpl
//
// 职责：
//   1. 创建 ecore 元模型自身的 19 个 meta EClass（EClass_EClass/EAttribute/...）
//      与 15 个内建 EDataType（EString/EBoolean/EInt/...）
//   2. 设置它们的继承关系、name、instanceClassName、abstract 标志
//   3. 注册到全局 EPackageRegistry（key = eNS_URI）
//   4. 提供 EcoreFactory 的 create/createFromString/convertToString
//   5. 提供 EClassImpl/EGenericTypeImpl/EFactoryImpl 的 eClass() override
//      （返回对应的 meta EClass，使反射 eGet/eSet 能正确分发）
//
// 设计：
//   - 单例状态用文件静态变量保存（不改 header，EcorePackage 对象本身是 façade）
//   - 两阶段 init（对齐 Java createPackageContents + initializePackageContents）
//   - 无 lambda（与旧 C++ 实现一致；用 static helper 函数）
#include "emf/ecore/EcoreImpls.h"
#include "emf/ecore/EcorePackage.h"
#include "emf/ecore/EcoreMetadata.h"
#include "emf/common/EObject.h"
#include "emf/common/EPackageRegistry.h"

#include <mutex>
#include <cstdlib>

namespace emf::ecore {

// ============================================================================
// 静态常量（对齐 Java EcorePackage.eNAME/eNS_URI/eNS_PREFIX）
// ============================================================================
const char* EcorePackage::eNS_URI     = "http://www.eclipse.org/emf/2002/Ecore";
const char* EcorePackage::eNS_PREFIX  = "ecore";
const char* EcorePackage::eNAME       = "ecore";

const char* EcoreFactory::eNS_URI     = "http://www.eclipse.org/emf/2002/Ecore";
const char* EcoreFactory::eNS_PREFIX  = "ecore";
const char* EcoreFactory::eNAME       = "ecore";

// ============================================================================
// 文件静态单例状态
// ============================================================================
namespace {

EPackageImpl* g_ePackage = nullptr;
EFactoryImpl* g_eFactory = nullptr;

// 19 个 meta EClass（顺序对齐 EcorePackage.h 的 getter 声明）
EClass* g_eClass_EModelElement       = nullptr;
EClass* g_eClass_ENamedElement       = nullptr;
EClass* g_eClass_ETypedElement       = nullptr;
EClass* g_eClass_EClassifier         = nullptr;
EClass* g_eClass_EClass              = nullptr;
EClass* g_eClass_EDataType           = nullptr;
EClass* g_eClass_EEnum               = nullptr;
EClass* g_eClass_EEnumLiteral        = nullptr;
EClass* g_eClass_EFactory            = nullptr;
EClass* g_eClass_EOperation          = nullptr;
EClass* g_eClass_EParameter          = nullptr;
EClass* g_eClass_EReference          = nullptr;
EClass* g_eClass_EStructuralFeature  = nullptr;
EClass* g_eClass_ETypeParameter      = nullptr;
EClass* g_eClass_EGenericType        = nullptr;
EClass* g_eClass_EAnnotation         = nullptr;
EClass* g_eClass_EPackage            = nullptr;
EClass* g_eClass_EObject             = nullptr;
EClass* g_eClass_EAttribute          = nullptr;
// 内建 EClass：EStringToStringMapEntry（对齐 Java EcorePackageImpl，XMLType.ecore 引用）
EClass* g_eClass_EStringToStringMapEntry = nullptr;

// 15 个内建 EDataType
EDataType* g_eDataType_EString          = nullptr;
EDataType* g_eDataType_EBoolean         = nullptr;
EDataType* g_eDataType_EInt             = nullptr;
EDataType* g_eDataType_EDouble          = nullptr;
EDataType* g_eDataType_EFloat           = nullptr;
EDataType* g_eDataType_ELong            = nullptr;
EDataType* g_eDataType_EShort           = nullptr;
EDataType* g_eDataType_EByte            = nullptr;
EDataType* g_eDataType_EChar            = nullptr;
EDataType* g_eDataType_EDate            = nullptr;
EDataType* g_eDataType_EBigInteger      = nullptr;
EDataType* g_eDataType_EBigDecimal      = nullptr;
EDataType* g_eDataType_EJavaObject      = nullptr;
EDataType* g_eDataType_EFeatureMapEntry = nullptr;
EDataType* g_eDataType_EFeatureMap      = nullptr;
// 补齐 Java EcorePackageImpl 缺失的 18 个内建 EDataType
EDataType* g_eDataType_EBooleanObject          = nullptr;
EDataType* g_eDataType_EByteArray              = nullptr;
EDataType* g_eDataType_EByteObject             = nullptr;
EDataType* g_eDataType_ECharacterObject        = nullptr;
EDataType* g_eDataType_EDiagnosticChain        = nullptr;
EDataType* g_eDataType_EDoubleObject           = nullptr;
EDataType* g_eDataType_EEList                  = nullptr;
EDataType* g_eDataType_EEnumerator             = nullptr;
EDataType* g_eDataType_EFloatObject            = nullptr;
EDataType* g_eDataType_EIntegerObject          = nullptr;
EDataType* g_eDataType_EJavaClass              = nullptr;
EDataType* g_eDataType_ELongObject             = nullptr;
EDataType* g_eDataType_EMap                    = nullptr;
EDataType* g_eDataType_EResource               = nullptr;
EDataType* g_eDataType_EResourceSet            = nullptr;
EDataType* g_eDataType_EShortObject            = nullptr;
EDataType* g_eDataType_ETreeIterator           = nullptr;
EDataType* g_eDataType_EInvocationTargetException = nullptr;

bool g_initialized = false;
bool g_factoryInitialized = false;
std::mutex g_initMutex;

// ---- helper：创建 meta EClass 并登记到 package ----
EClass* newMetaEClass(const char* name) {
    auto* c = dynamic_cast<EClassImpl*>(EcoreFactory::instance().createEClass());
    c->setName(name);
    g_ePackage->addEClassifier(c);
    return c;
}

// ---- helper：创建内建 EDataType 并登记到 package ----
EDataType* newBuiltinDataType(const char* name, const char* instanceClassName) {
    auto* d = dynamic_cast<EDataTypeImpl*>(EcoreFactory::instance().createEDataType());
    d->setName(name);
    d->setInstanceClassName(instanceClassName);
    d->setSerializable(true);
    g_ePackage->addEClassifier(d);
    return d;
}

// ---- helper：创建 meta EAttribute 并安装到 owner EClass（对齐 Java initEAttribute）----
// featureID 必须使用 EcorePackage.h 中预设的 FeatureID 常量（>= 1000），
// addEStructuralFeature 对 featureID>=0 保留，故 meta 常量不会被自动重分配。
// derived 标志对齐 Java：derived feature 反射可读但不被 XMI 序列化。
EAttribute* createMetaAttribute(EClass* owner, const char* name, EDataType* type,
                                int featureID, int lowerBound = 0, int upperBound = 1,
                                bool isID = false, const char* defaultLiteral = nullptr,
                                bool derived = false, bool volatile_ = false, bool transient_ = false) {
    auto* a = EcoreFactory::instance().createEAttribute();
    a->setName(name);
    if (type) a->setEAttributeType(type);
    a->setFeatureID(featureID);
    a->setLowerBound(lowerBound);
    a->setUpperBound(upperBound);
    a->setID(isID);
    if (defaultLiteral) a->setDefaultValueLiteral(defaultLiteral);
    a->setDerived(derived);
    a->setVolatile(volatile_);
    a->setTransient(transient_);
    if (derived) a->setChangeable(false);
    owner->addEStructuralFeature(a);
    return a;
}

// ---- helper：创建 meta EReference 并安装到 owner EClass（对齐 Java initEReference）----
EReference* createMetaReference(EClass* owner, const char* name, EClass* type,
                                int featureID, int lowerBound = 0, int upperBound = 1,
                                bool containment = false, bool resolveProxies = true,
                                EReference* opposite = nullptr,
                                bool derived = false, bool volatile_ = false, bool transient_ = false) {
    auto* r = EcoreFactory::instance().createEReference();
    r->setName(name);
    if (type) r->setEReferenceType(type);
    r->setFeatureID(featureID);
    r->setLowerBound(lowerBound);
    r->setUpperBound(upperBound);
    r->setContainment(containment);
    r->setResolveProxies(resolveProxies);
    if (opposite) r->setEOpposite(opposite);
    r->setDerived(derived);
    r->setVolatile(volatile_);
    r->setTransient(transient_);
    if (derived) r->setChangeable(false);
    owner->addEStructuralFeature(r);
    return r;
}

// ---- Phase 1: createPackageContents ----
void createPackageContents() {
    g_ePackage = new EPackageImpl();
    g_eFactory = new EFactoryImpl();

    // 19 meta EClass（顺序：基类在前，子类在后，便于设置 superType）
    g_eClass_EModelElement      = newMetaEClass("EModelElement");
    g_eClass_ENamedElement      = newMetaEClass("ENamedElement");
    g_eClass_ETypedElement      = newMetaEClass("ETypedElement");
    g_eClass_EClassifier        = newMetaEClass("EClassifier");
    g_eClass_EClass             = newMetaEClass("EClass");
    g_eClass_EDataType          = newMetaEClass("EDataType");
    g_eClass_EEnum              = newMetaEClass("EEnum");
    g_eClass_EEnumLiteral       = newMetaEClass("EEnumLiteral");
    g_eClass_EFactory           = newMetaEClass("EFactory");
    g_eClass_EOperation         = newMetaEClass("EOperation");
    g_eClass_EParameter         = newMetaEClass("EParameter");
    g_eClass_EStructuralFeature = newMetaEClass("EStructuralFeature");
    g_eClass_EReference         = newMetaEClass("EReference");
    g_eClass_EAttribute         = newMetaEClass("EAttribute");
    g_eClass_ETypeParameter     = newMetaEClass("ETypeParameter");
    g_eClass_EGenericType       = newMetaEClass("EGenericType");
    g_eClass_EAnnotation        = newMetaEClass("EAnnotation");
    g_eClass_EPackage           = newMetaEClass("EPackage");
    g_eClass_EObject            = newMetaEClass("EObject");
    // 内建 EClass：EStringToStringMapEntry（XMLType.ecore 的 eType 引用，对齐 Java EcorePackageImpl）
    g_eClass_EStringToStringMapEntry = newMetaEClass("EStringToStringMapEntry");

    // 15 内建 EDataType（instanceClassName 对齐 Java EcorePackageImpl）
    g_eDataType_EString          = newBuiltinDataType("EString",          "java.lang.String");
    g_eDataType_EBoolean         = newBuiltinDataType("EBoolean",         "boolean");
    g_eDataType_EInt             = newBuiltinDataType("EInt",             "int");
    g_eDataType_EDouble          = newBuiltinDataType("EDouble",          "double");
    g_eDataType_EFloat           = newBuiltinDataType("EFloat",           "float");
    g_eDataType_ELong            = newBuiltinDataType("ELong",            "long");
    g_eDataType_EShort           = newBuiltinDataType("EShort",           "short");
    g_eDataType_EByte            = newBuiltinDataType("EByte",            "byte");
    g_eDataType_EChar            = newBuiltinDataType("EChar",            "char");
    g_eDataType_EDate            = newBuiltinDataType("EDate",            "java.util.Date");
    g_eDataType_EBigInteger      = newBuiltinDataType("EBigInteger",      "java.math.BigInteger");
    g_eDataType_EBigDecimal      = newBuiltinDataType("EBigDecimal",      "java.math.BigDecimal");
    g_eDataType_EJavaObject      = newBuiltinDataType("EJavaObject",      "java.lang.Object");
    g_eDataType_EFeatureMapEntry = newBuiltinDataType("EFeatureMapEntry", "org.eclipse.emf.ecore.util.FeatureMap$Entry");
    g_eDataType_EFeatureMap      = newBuiltinDataType("EFeatureMap",      "org.eclipse.emf.ecore.util.FeatureMap");
    // 补齐 Java EcorePackageImpl 缺失的 18 个内建 EDataType（instanceClassName 对齐 Java）
    g_eDataType_EBooleanObject          = newBuiltinDataType("EBooleanObject",          "java.lang.Boolean");
    g_eDataType_EByteArray              = newBuiltinDataType("EByteArray",              "[B");
    g_eDataType_EByteObject             = newBuiltinDataType("EByteObject",             "java.lang.Byte");
    g_eDataType_ECharacterObject        = newBuiltinDataType("ECharacterObject",        "java.lang.Character");
    g_eDataType_EDiagnosticChain        = newBuiltinDataType("EDiagnosticChain",        "org.eclipse.emf.common.util.DiagnosticChain");
    g_eDataType_EDoubleObject           = newBuiltinDataType("EDoubleObject",           "java.lang.Double");
    g_eDataType_EEList                  = newBuiltinDataType("EEList",                  "org.eclipse.emf.common.util.EList");
    g_eDataType_EEnumerator             = newBuiltinDataType("EEnumerator",             "org.eclipse.emf.common.util.Enumerator");
    g_eDataType_EFloatObject            = newBuiltinDataType("EFloatObject",            "java.lang.Float");
    g_eDataType_EIntegerObject          = newBuiltinDataType("EIntegerObject",          "java.lang.Integer");
    g_eDataType_EJavaClass              = newBuiltinDataType("EJavaClass",              "java.lang.Class");
    g_eDataType_ELongObject             = newBuiltinDataType("ELongObject",             "java.lang.Long");
    g_eDataType_EMap                    = newBuiltinDataType("EMap",                    "java.util.Map");
    g_eDataType_EResource               = newBuiltinDataType("EResource",               "org.eclipse.emf.ecore.resource.Resource");
    g_eDataType_EResourceSet            = newBuiltinDataType("EResourceSet",            "org.eclipse.emf.ecore.resource.ResourceSet");
    g_eDataType_EShortObject            = newBuiltinDataType("EShortObject",            "java.lang.Short");
    g_eDataType_ETreeIterator           = newBuiltinDataType("ETreeIterator",           "org.eclipse.emf.common.util.TreeIterator");
    g_eDataType_EInvocationTargetException = newBuiltinDataType("EInvocationTargetException", "java.lang.reflect.InvocationTargetException");
}

// ---- helper：设置 meta EClass 的 superType + abstract + instanceClassName ----
void initMetaEClass(EClass* cls, EClass* superType, const char* instanceClassName, bool isAbstract) {
    auto* impl = dynamic_cast<EClassImpl*>(cls);
    if (superType) impl->addESuperType(superType);
    impl->setAbstract(isAbstract);
    impl->setInterface(false);
    if (instanceClassName) impl->setInstanceClassName(instanceClassName);
}

// ---- 安装 meta EStructuralFeature（对齐 Java EcorePackageImpl.initEAttribute/initEReference）----
// 为每个 meta EClass 安装 EAttribute/EReference，featureID 使用 EcorePackage.h 预设常量（>= 1000）。
// addEStructuralFeature 对 featureID>=0 保留预设值，故 meta 常量不会被 renumbering 重分配
// （renumbering 仅处理 XMIResource 中的动态 EPackage，且只重分配 featureID<1000 的 feature）。
// 跳过 derived/volatile/transient 的 derived-only feature（eAllAttributes/eSuperPackage/eInstance 等），
// 这些在 C++ 中通过专用 API（如 getEAllStructuralFeatures）直接访问，不通过反射。
// opposite/back-pointer 暂不设（Impl 直接 set/get，不依赖双向同步）。
void initMetaFeatures() {
    namespace FID = ::emf::common::FeatureID;
    auto& P = EcorePackage::instance();
    auto* EString     = P.getEDataType_EString();
    auto* EBoolean    = P.getEDataType_EBoolean();
    auto* EInt        = P.getEDataType_EInt();

    // ---- EModelElement ----
    // eAnnotations : EAnnotation[*] containment
    createMetaReference(g_eClass_EModelElement, "eAnnotations",
                        g_eClass_EAnnotation, FID::EMODEL_ELEMENT_EANNOTATIONS,
                        0, -1, true, true);

    // ---- ENamedElement ----
    // name : EString[1] id
    createMetaAttribute(g_eClass_ENamedElement, "name", EString,
                        FID::ENAMED_ELEMENT_ENAME, 0, 1, true);

    // ---- ETypedElement ----
    createMetaAttribute(g_eClass_ETypedElement, "ordered", EBoolean,
                        FID::ETYPED_ELEMENT_EORDERED, 0, 1, false, "true");
    createMetaAttribute(g_eClass_ETypedElement, "unique", EBoolean,
                        FID::ETYPED_ELEMENT_EUNIQUE, 0, 1, false, "true");
    createMetaAttribute(g_eClass_ETypedElement, "lowerBound", EInt,
                        FID::ETYPED_ELEMENT_ELOWERBOUND, 0, 1, false, "0");
    createMetaAttribute(g_eClass_ETypedElement, "upperBound", EInt,
                        FID::ETYPED_ELEMENT_EUPPERBOUND, 0, 1, false, "1");
    // eType : EClassifier[1]
    createMetaReference(g_eClass_ETypedElement, "eType",
                        g_eClass_EClassifier, FID::ETYPED_ELEMENT_ETYPE,
                        0, 1, false, true);
    // eGenericType : EGenericType[1] containment（对齐 Java ETypedElement.eGenericType）
    // 参数化时序列化为 <eGenericType> 子元素，替代 eType 属性
    createMetaReference(g_eClass_ETypedElement, "eGenericType",
                        g_eClass_EGenericType, FID::ETYPED_ELEMENT_EGENERICTYPE,
                        0, 1, true, true);

    // ---- EClassifier (extends ETypedElement) ----
    createMetaAttribute(g_eClass_EClassifier, "instanceClassName", EString,
                        FID::ECLASSIFIER_EINSTANCECLASSNAME, 0, 1);
    createMetaReference(g_eClass_EClassifier, "eTypeParameters",
                        g_eClass_ETypeParameter, FID::ECLASSIFIER_ETYPEPARAMETERS,
                        0, -1, true, true);
    // defaultValue : EJavaObject derived/volatile/transient - 跳过

    // ---- EClass (extends EClassifier) ----
    createMetaAttribute(g_eClass_EClass, "abstract", EBoolean,
                        FID::ECLASS_EABSTRACT, 0, 1, false, "false");
    createMetaAttribute(g_eClass_EClass, "interface", EBoolean,
                        FID::ECLASS_EINTERFACE, 0, 1, false, "false");
    createMetaReference(g_eClass_EClass, "eSuperTypes",
                        g_eClass_EClass, FID::ECLASS_ESUPERTYPES,
                        0, -1, false, true);
    createMetaReference(g_eClass_EClass, "eOperations",
                        g_eClass_EOperation, FID::ECLASS_EOPERATIONS,
                        0, -1, true, true);
    createMetaReference(g_eClass_EClass, "eStructuralFeatures",
                        g_eClass_EStructuralFeature, FID::ECLASS_ESTRUCTURALFEATURES,
                        0, -1, true, true);
    // eGenericSuperTypes : EGenericType[*] containment（对齐 Java EClass.eGenericSuperTypes）
    // isSet 互斥：列表中任一 EGenericType 参数化（eTypeParameter!=null || !eTypeArguments.isEmpty()）才视为 set；
    // 此时序列化跳过 eSuperTypes 属性，所有父类型写为 <eGenericSuperTypes> 子元素
    createMetaReference(g_eClass_EClass, "eGenericSuperTypes",
                        g_eClass_EGenericType, FID::ECLASS_EGENERICSUPERTYPES,
                        0, -1, true, true);
    // 对齐 Java：eAllAttributes/eAllReferences/eAttributes/eReferences/eAllOperations/eAllStructuralFeatures
    // 都是 derived/volatile/transient，changeable=false。反射 eGet 动态计算（EClassImpl 已实现）。
    // XMI saver 跳过 derived feature（对齐 Java 默认不保存 derived）
    createMetaReference(g_eClass_EClass, "eAllAttributes",
                        g_eClass_EAttribute, FID::ECLASS_EALLATTRIBUTES,
                        0, -1, false, true, nullptr, true, true, true);
    createMetaReference(g_eClass_EClass, "eAllReferences",
                        g_eClass_EReference, FID::ECLASS_EALLREFERENCES,
                        0, -1, false, true, nullptr, true, true, true);
    createMetaReference(g_eClass_EClass, "eAttributes",
                        g_eClass_EAttribute, FID::ECLASS_EATTRIBUTES,
                        0, -1, false, true, nullptr, true, true, true);
    createMetaReference(g_eClass_EClass, "eReferences",
                        g_eClass_EReference, FID::ECLASS_EREFERENCES,
                        0, -1, false, true, nullptr, true, true, true);
    createMetaReference(g_eClass_EClass, "eAllOperations",
                        g_eClass_EOperation, FID::ECLASS_EALLOPERATIONS,
                        0, -1, false, true, nullptr, true, true, true);
    createMetaReference(g_eClass_EClass, "eAllStructuralFeatures",
                        g_eClass_EStructuralFeature, FID::ECLASS_EALLSTRUCTURALFEATURES,
                        0, -1, false, true, nullptr, true, true, true);

    // ---- EDataType (extends EClassifier) ----
    createMetaAttribute(g_eClass_EDataType, "serializable", EBoolean,
                        FID::EDATATYPE_ESERIALIZABLE, 0, 1, false, "true");

    // ---- EEnum (extends EDataType) ----
    createMetaReference(g_eClass_EEnum, "eLiterals",
                        g_eClass_EEnumLiteral, FID::EENUM_ELITERALS,
                        0, -1, true, true);

    // ---- EEnumLiteral (extends ENamedElement) ----
    createMetaAttribute(g_eClass_EEnumLiteral, "value", EInt,
                        FID::EENUMLITERAL_EVALUE, 0, 1, false, "0");
    createMetaAttribute(g_eClass_EEnumLiteral, "literal", EString,
                        FID::EENUMLITERAL_ELITERAL, 0, 1);
    createMetaReference(g_eClass_EEnumLiteral, "eEnum",
                        g_eClass_EEnum, FID::EENUMLITERAL_EENUM,
                        0, 1, false, true);
    // instance : EJavaObject derived/volatile - 跳过

    // ---- EFactory (extends EModelElement) ----
    createMetaReference(g_eClass_EFactory, "ePackage",
                        g_eClass_EPackage, FID::EFACTORY_EPACKAGE,
                        0, 1, false, true);

    // ---- EOperation (extends ETypedElement) ----
    // eTypeParameters : ETypeParameter[*] containment（对齐 Java EOperation.eTypeParameters）
    // 顺序对齐 Java：eContainingClass(transient) → eTypeParameters → eParameters → eExceptions
    createMetaReference(g_eClass_EOperation, "eTypeParameters",
                        g_eClass_ETypeParameter, FID::EOPERATION_ETYPEPARAMETERS,
                        0, -1, true, true);
    createMetaReference(g_eClass_EOperation, "eParameters",
                        g_eClass_EParameter, FID::EOPERATION_EPARAMETERS,
                        0, -1, true, true);
    createMetaReference(g_eClass_EOperation, "eExceptions",
                        g_eClass_EClassifier, FID::EOPERATION_EEXCEPTIONS,
                        0, -1, false, true);
    // eBody : derived - 跳过

    // ---- EParameter (extends ETypedElement) ----
    createMetaReference(g_eClass_EParameter, "eOperation",
                        g_eClass_EOperation, FID::EPARAMETER_EOPERATION,
                        0, 1, false, true);

    // ---- EStructuralFeature (extends ETypedElement) ----
    createMetaAttribute(g_eClass_EStructuralFeature, "featureID", EInt,
                        FID::ESTRUCTURALFEATURE_EFEATUREID, 0, 1, false, "0");
    createMetaAttribute(g_eClass_EStructuralFeature, "changeable", EBoolean,
                        FID::ESTRUCTURALFEATURE_ECHANGEABLE, 0, 1, false, "true");
    createMetaAttribute(g_eClass_EStructuralFeature, "volatile", EBoolean,
                        FID::ESTRUCTURALFEATURE_EVOLATILE, 0, 1, false, "false");
    createMetaAttribute(g_eClass_EStructuralFeature, "transient", EBoolean,
                        FID::ESTRUCTURALFEATURE_ETRANSIENT, 0, 1, false, "false");
    createMetaAttribute(g_eClass_EStructuralFeature, "unsettable", EBoolean,
                        FID::ESTRUCTURALFEATURE_EUNSETTABLE, 0, 1, false, "false");
    createMetaAttribute(g_eClass_EStructuralFeature, "derived", EBoolean,
                        FID::ESTRUCTURALFEATURE_EDERIVED, 0, 1, false, "false");
    createMetaAttribute(g_eClass_EStructuralFeature, "defaultValueLiteral", EString,
                        FID::ESTRUCTURALFEATURE_EDEFAULTVALUELITERAL, 0, 1);
    createMetaReference(g_eClass_EStructuralFeature, "eContainingClass",
                        g_eClass_EClass, FID::ESTRUCTURALFEATURE_EECONTAININGCLASS,
                        0, 1, false, true);

    // ---- EAttribute (extends EStructuralFeature) ----
    createMetaAttribute(g_eClass_EAttribute, "iD", EBoolean,
                        FID::EATTRIBUTE_EID, 0, 1, false, "false");
    createMetaReference(g_eClass_EAttribute, "eAttributeType",
                        g_eClass_EDataType, FID::EATTRIBUTE_EATTRIBUTETYPE,
                        0, 1, false, true);

    // ---- EReference (extends EStructuralFeature) ----
    createMetaAttribute(g_eClass_EReference, "containment", EBoolean,
                        FID::EREFERENCE_ECONTAINMENT, 0, 1, false, "false");
    createMetaAttribute(g_eClass_EReference, "resolveProxies", EBoolean,
                        FID::EREFERENCE_ERESOLVEPROXIES, 0, 1, false, "true");
    createMetaReference(g_eClass_EReference, "eReferenceType",
                        g_eClass_EClass, FID::EREFERENCE_EREFERENCETYPE,
                        0, 1, false, true);
    createMetaReference(g_eClass_EReference, "eOpposite",
                        g_eClass_EReference, FID::EREFERENCE_EOPPOSITE,
                        0, 1, false, true);
    // eContainer : derived/transient - 跳过

    // ---- ETypeParameter (extends ENamedElement) ----
    // eBounds : EGenericType[*] containment（对齐 Java ETypeParameter.eBounds）
    // featureID 使用预设常量 ETYPEPARAMETER_EBOUNDS，对齐 ETypeParameterImpl 反射分支
    createMetaReference(g_eClass_ETypeParameter, "eBounds",
                        g_eClass_EGenericType, FID::ETYPEPARAMETER_EBOUNDS,
                        0, -1, true, true);

    // ---- EGenericType (extends EModelElement) ----
    createMetaReference(g_eClass_EGenericType, "eClassifier",
                        g_eClass_EClassifier, FID::EGENERICTYPE_ECLASSIFIER,
                        0, 1, false, true);
    createMetaReference(g_eClass_EGenericType, "eTypeArguments",
                        g_eClass_EGenericType, FID::EGENERICTYPE_ETYPEARGUMENTS,
                        0, -1, true, true);
    createMetaReference(g_eClass_EGenericType, "eUpperBound",
                        g_eClass_EGenericType, FID::EGENERICTYPE_EUPPERBOUND,
                        0, 1, true, true);
    createMetaReference(g_eClass_EGenericType, "eLowerBound",
                        g_eClass_EGenericType, FID::EGENERICTYPE_ELOWERBOUND,
                        0, 1, true, true);
    // eTypeParameter : ETypeParameter[1] non-containment（对齐 Java EGenericType.eTypeParameter）
    // 与 eClassifier 互斥作 XML 属性；用于引用宿主类的类型参数（如 #//MyClass/T）
    createMetaReference(g_eClass_EGenericType, "eTypeParameter",
                        g_eClass_ETypeParameter, FID::EGENERICTYPE_ETYPEPARAMETER,
                        0, 1, false, true);

    // ---- EAnnotation (extends EModelElement) ----
    createMetaAttribute(g_eClass_EAnnotation, "source", EString,
                        FID::EANNOTATION_ESOURCE, 0, 1);
    // details : EFeatureMapEntry - 跳过（EAnnotationImpl 用 std::map<string,string>，不通过反射）
    createMetaReference(g_eClass_EAnnotation, "contents",
                        g_eClass_EObject, FID::EANNOTATION_ECONTENTS,
                        0, -1, true, true);
    createMetaReference(g_eClass_EAnnotation, "references",
                        g_eClass_EObject, FID::EANNOTATION_EREFERENCES,
                        0, -1, false, true);
    createMetaReference(g_eClass_EAnnotation, "eModelElement",
                        g_eClass_EModelElement, FID::EANNOTATION_EMODEL_ELEMENT,
                        0, 1, false, true);

    // ---- EPackage (extends ENamedElement) ----
    createMetaAttribute(g_eClass_EPackage, "nsURI", EString,
                        FID::EPACKAGE_ENSURI, 0, 1);
    createMetaAttribute(g_eClass_EPackage, "nsPrefix", EString,
                        FID::EPACKAGE_ENSPREFIX, 0, 1);
    createMetaReference(g_eClass_EPackage, "eClassifiers",
                        g_eClass_EClassifier, FID::EPACKAGE_ECLASSIFIERS,
                        0, -1, true, true);
    createMetaReference(g_eClass_EPackage, "eFactoryInstance",
                        g_eClass_EFactory, FID::EPACKAGE_EFACTORYINSTANCE,
                        0, 1, false, true);
    createMetaReference(g_eClass_EPackage, "eSubpackages",
                        g_eClass_EPackage, FID::EPACKAGE_ESUBPACKAGES,
                        0, -1, true, true);
    // 对齐 Java：eSuperPackage 是 transient（非 derived），eSubpackages 的 opposite back-reference。
    // changeable=false，反射 eGet 返回 superPackage_ 字段（EPackageImpl 已实现）。
    createMetaReference(g_eClass_EPackage, "eSuperPackage",
                        g_eClass_EPackage, FID::EPACKAGE_ESUPERPACKAGE_NEW,
                        0, 1, false, true, nullptr, false, false, true);

    // EObject : 抽象根，无自有 feature
}

// ---- Phase 2: initializePackageContents ----
void initializePackageContents() {
    // 包元数据
    g_ePackage->setName(EcorePackage::eNAME);
    g_ePackage->setNsURI(EcorePackage::eNS_URI);
    g_ePackage->setNsPrefix(EcorePackage::eNS_PREFIX);
    g_ePackage->setEFactoryInstance(g_eFactory);
    g_eFactory->setEPackage(g_ePackage);

    // 继承关系（对齐 Java EcorePackageImpl.initializePackageContents）
    // EModelElement: 抽象，无父类
    initMetaEClass(g_eClass_EModelElement,      nullptr,                       "org.eclipse.emf.ecore.EModelElement", true);
    // ENamedElement : EModelElement
    initMetaEClass(g_eClass_ENamedElement,      g_eClass_EModelElement,        "org.eclipse.emf.ecore.ENamedElement", true);
    // ETypedElement : ENamedElement
    initMetaEClass(g_eClass_ETypedElement,      g_eClass_ENamedElement,        "org.eclipse.emf.ecore.ETypedElement", true);
    // EClassifier : ETypedElement
    initMetaEClass(g_eClass_EClassifier,        g_eClass_ETypedElement,        "org.eclipse.emf.ecore.EClassifier",   true);
    // EClass : EClassifier
    initMetaEClass(g_eClass_EClass,             g_eClass_EClassifier,          "org.eclipse.emf.ecore.EClass",        false);
    // EDataType : EClassifier
    initMetaEClass(g_eClass_EDataType,          g_eClass_EClassifier,          "org.eclipse.emf.ecore.EDataType",     false);
    // EEnum : EDataType
    initMetaEClass(g_eClass_EEnum,              g_eClass_EDataType,            "org.eclipse.emf.ecore.EEnum",         false);
    // EEnumLiteral : ENamedElement
    initMetaEClass(g_eClass_EEnumLiteral,       g_eClass_ENamedElement,        "org.eclipse.emf.ecore.EEnumLiteral",  false);
    // EFactory : EModelElement
    initMetaEClass(g_eClass_EFactory,           g_eClass_EModelElement,        "org.eclipse.emf.ecore.EFactory",      false);
    // EOperation : ETypedElement
    initMetaEClass(g_eClass_EOperation,         g_eClass_ETypedElement,        "org.eclipse.emf.ecore.EOperation",    false);
    // EParameter : ETypedElement
    initMetaEClass(g_eClass_EParameter,         g_eClass_ETypedElement,        "org.eclipse.emf.ecore.EParameter",    false);
    // EStructuralFeature : ETypedElement
    initMetaEClass(g_eClass_EStructuralFeature, g_eClass_ETypedElement,        "org.eclipse.emf.ecore.EStructuralFeature", true);
    // EReference : EStructuralFeature
    initMetaEClass(g_eClass_EReference,         g_eClass_EStructuralFeature,   "org.eclipse.emf.ecore.EReference",    false);
    // EAttribute : EStructuralFeature
    initMetaEClass(g_eClass_EAttribute,         g_eClass_EStructuralFeature,   "org.eclipse.emf.ecore.EAttribute",    false);
    // ETypeParameter : ENamedElement
    initMetaEClass(g_eClass_ETypeParameter,     g_eClass_ENamedElement,        "org.eclipse.emf.ecore.ETypeParameter", false);
    // EGenericType : EModelElement
    initMetaEClass(g_eClass_EGenericType,       g_eClass_EModelElement,        "org.eclipse.emf.ecore.EGenericType",  false);
    // EAnnotation : EModelElement
    initMetaEClass(g_eClass_EAnnotation,        g_eClass_EModelElement,        "org.eclipse.emf.ecore.EAnnotation",   false);
    // EPackage : ENamedElement
    initMetaEClass(g_eClass_EPackage,           g_eClass_ENamedElement,        "org.eclipse.emf.ecore.EPackage",      false);
    // EObject : 抽象根（对齐 Java EObjectEClass，无父类）
    initMetaEClass(g_eClass_EObject,            nullptr,                       "org.eclipse.emf.ecore.EObject",       true);
    // EStringToStringMapEntry : EModelElement（对齐 Java EcorePackageImpl，instanceClassName=java.util.Map$Entry）
    initMetaEClass(g_eClass_EStringToStringMapEntry, g_eClass_EModelElement,  "java.util.Map$Entry",                 false);

    // 安装 meta EStructuralFeature（对齐 Java EcorePackageImpl.initializePackageContents 末尾的 initEAttribute/initEReference）
    // 必须在所有 superType 设置完成后调用，以保证 getEAllStructuralFeatures 正确包含继承 feature
    initMetaFeatures();
}

}  // anonymous namespace

// ============================================================================
// EcoreFactory 实现
// ============================================================================

EcoreFactory& EcoreFactory::instance() {
    static EcoreFactory inst;
    return inst;
}

void EcoreFactory::initialize() {
    // 不单独加锁：要么由 EcorePackage::initialize（已持 g_initMutex）内部调用，
    // 要么单独调用时仅设置一个 bool 标志，无并发风险。
    g_factoryInitialized = true;
}

EClass* EcoreFactory::createEClass()         { return new EClassImpl(); }
EAttribute* EcoreFactory::createEAttribute() { return new EAttributeImpl(); }
EReference* EcoreFactory::createEReference() { return new EReferenceImpl(); }
EOperation* EcoreFactory::createEOperation() { return new EOperationImpl(); }
EParameter* EcoreFactory::createEParameter() { return new EParameterImpl(); }
EPackage* EcoreFactory::createEPackage()     { return new EPackageImpl(); }
EEnum* EcoreFactory::createEEnum()           { return new EEnumImpl(); }
EEnumLiteral* EcoreFactory::createEEnumLiteral() { return new EEnumLiteralImpl(); }
EDataType* EcoreFactory::createEDataType()   { return new EDataTypeImpl(); }
EAnnotation* EcoreFactory::createEAnnotation() { return new EAnnotationImpl(); }
ETypeParameter* EcoreFactory::createETypeParameter() { return new ETypeParameterImpl(); }
EGenericType* EcoreFactory::createEGenericType() { return new EGenericTypeImpl(); }
EFactory* EcoreFactory::createEFactory()     { return new EFactoryImpl(); }

emf::common::EObject* EcoreFactory::create(const EClass* eClass) {
    if (!eClass) return nullptr;
    // 确保 EcorePackage 已初始化（反射调用可能先于显式 initialize）
    if (!g_initialized) EcorePackage::initialize();

    auto& p = EcorePackage::instance();
    // 按 meta EClass 指针身份分派（对齐 Java EcoreFactoryImpl.create 按 classifierID switch）
    if (eClass == p.getEClass_EClass())             return createEClass();
    if (eClass == p.getEClass_EAttribute())         return createEAttribute();
    if (eClass == p.getEClass_EReference())         return createEReference();
    if (eClass == p.getEClass_EOperation())         return createEOperation();
    if (eClass == p.getEClass_EParameter())         return createEParameter();
    if (eClass == p.getEClass_EPackage())           return createEPackage();
    if (eClass == p.getEClass_EEnum())              return createEEnum();
    if (eClass == p.getEClass_EEnumLiteral())       return createEEnumLiteral();
    if (eClass == p.getEClass_EDataType())          return createEDataType();
    if (eClass == p.getEClass_EAnnotation())        return createEAnnotation();
    if (eClass == p.getEClass_ETypeParameter())     return createETypeParameter();
    if (eClass == p.getEClass_EGenericType())       return createEGenericType();
    if (eClass == p.getEClass_EFactory())           return createEFactory();
    // 未知 EClass（用户模型类）→ 返回 nullptr，调用方 EFactoryImpl 上层走 DynamicEObject 回退
    return nullptr;
}

// Java instanceClassName → Ecore 内建 EDataType 名（对齐 Java EcoreFactoryImpl 按
// eDataType.getInstanceClass() 分派转换器的行为）。用户模型 EDataType（如 ARTOP 的
// Boolean/Integer/String，instanceClassName="java.lang.Boolean" 等）通过此表映射到
// 对应的 Ecore 内建转换器，无需特判。
static const char* ecoreNameByInstanceClass(const std::string& icn) {
    if (icn == "boolean" || icn == "java.lang.Boolean")     return "EBoolean";
    if (icn == "int"     || icn == "java.lang.Integer")     return "EInt";
    if (icn == "long"    || icn == "java.lang.Long")        return "ELong";
    if (icn == "short"   || icn == "java.lang.Short")       return "EShort";
    if (icn == "byte"    || icn == "java.lang.Byte")        return "EByte";
    if (icn == "double"  || icn == "java.lang.Double")      return "EDouble";
    if (icn == "float"   || icn == "java.lang.Float")       return "EFloat";
    if (icn == "char"    || icn == "java.lang.Character")   return "EChar";
    if (icn == "java.lang.String")                          return "EString";
    if (icn == "java.util.Date")                            return "EDate";
    if (icn == "java.math.BigInteger")                      return "EBigInteger";
    if (icn == "java.math.BigDecimal")                      return "EBigDecimal";
    return nullptr;
}

std::any EcoreFactory::createFromString(const EClassifier* cls, const std::string& literal) {
    if (!cls) return std::any{literal};
    // EDataType：优先按 instanceClassName 分派（对齐 Java EcoreFactoryImpl 按
    // eDataType.getInstanceClass() 选转换器），否则按 name 分派（Ecore 内建 EDataType）
    if (auto* dt = dynamic_cast<const EDataType*>(cls)) {
        const std::string& icn = dt->getInstanceClassName();
        if (!icn.empty()) {
            if (auto* ecoreName = ecoreNameByInstanceClass(icn)) {
                return DataTypeUtil::fromString(ecoreName, literal);
            }
            // 未知 instanceClassName（含 org.eclipse.emf.common.util.Enumerator 等
            // 枚举标记类型）→ 原样字符串（C++ 端枚举存字面量名）
            return std::any{std::string{literal}};
        }
        return DataTypeUtil::fromString(dt->getName(), literal);
    }
    // EEnum：按字面量名/字面量值查找（C++ 端枚举存字面量名，对齐 codegen TypeMapper）
    if (auto* e = dynamic_cast<const EEnum*>(cls)) {
        return std::any{std::string{literal}};
    }
    return std::any{literal};
}

std::string EcoreFactory::convertToString(const EClassifier* cls, const std::any& value) {
    if (!cls) return {};
    if (auto* dt = dynamic_cast<const EDataType*>(cls)) {
        const std::string& icn = dt->getInstanceClassName();
        if (!icn.empty()) {
            if (auto* ecoreName = ecoreNameByInstanceClass(icn)) {
                return DataTypeUtil::toString(ecoreName, value);
            }
            // 未知 instanceClassName：尝试按 string 取
            if (auto* p = std::any_cast<std::string>(&value)) return *p;
            return {};
        }
        return DataTypeUtil::toString(dt->getName(), value);
    }
    if (auto* e = dynamic_cast<const EEnum*>(cls)) {
        if (auto* p = std::any_cast<std::string>(&value)) return *p;
        return {};
    }
    return {};
}

// ============================================================================
// EcorePackage 实现
// ============================================================================

EcorePackage& EcorePackage::instance() {
    static EcorePackage inst;
    return inst;
}

void EcorePackage::initialize() {
    std::lock_guard<std::mutex> lk(g_initMutex);
    if (g_initialized) return;
    g_initialized = true;

    EcoreFactory::initialize();
    createPackageContents();
    initializePackageContents();

    // 注册到全局 EPackageRegistry（对齐 Java EPackage.Registry.INSTANCE.put(eNS_URI, ...)）
    emf::common::EPackageRegistry::instance().put(eNS_URI, g_ePackage);
}

EPackage* EcorePackage::getEPackage() {
    if (!g_initialized) initialize();
    return g_ePackage;
}

EFactory* EcorePackage::getEFactory() {
    if (!g_initialized) initialize();
    return g_eFactory;
}

// ---- meta EClass getters ----
EClass* EcorePackage::getEClass_EModelElement()       { if (!g_initialized) initialize(); return g_eClass_EModelElement; }
EClass* EcorePackage::getEClass_ENamedElement()       { if (!g_initialized) initialize(); return g_eClass_ENamedElement; }
EClass* EcorePackage::getEClass_ETypedElement()       { if (!g_initialized) initialize(); return g_eClass_ETypedElement; }
EClass* EcorePackage::getEClass_EClassifier()         { if (!g_initialized) initialize(); return g_eClass_EClassifier; }
EClass* EcorePackage::getEClass_EClass()              { if (!g_initialized) initialize(); return g_eClass_EClass; }
EClass* EcorePackage::getEClass_EDataType()           { if (!g_initialized) initialize(); return g_eClass_EDataType; }
EClass* EcorePackage::getEClass_EEnum()               { if (!g_initialized) initialize(); return g_eClass_EEnum; }
EClass* EcorePackage::getEClass_EEnumLiteral()        { if (!g_initialized) initialize(); return g_eClass_EEnumLiteral; }
EClass* EcorePackage::getEClass_EFactory()            { if (!g_initialized) initialize(); return g_eClass_EFactory; }
EClass* EcorePackage::getEClass_EOperation()          { if (!g_initialized) initialize(); return g_eClass_EOperation; }
EClass* EcorePackage::getEClass_EParameter()          { if (!g_initialized) initialize(); return g_eClass_EParameter; }
EClass* EcorePackage::getEClass_EReference()          { if (!g_initialized) initialize(); return g_eClass_EReference; }
EClass* EcorePackage::getEClass_EStructuralFeature()  { if (!g_initialized) initialize(); return g_eClass_EStructuralFeature; }
EClass* EcorePackage::getEClass_ETypeParameter()      { if (!g_initialized) initialize(); return g_eClass_ETypeParameter; }
EClass* EcorePackage::getEClass_EGenericType()        { if (!g_initialized) initialize(); return g_eClass_EGenericType; }
EClass* EcorePackage::getEClass_EAnnotation()         { if (!g_initialized) initialize(); return g_eClass_EAnnotation; }
EClass* EcorePackage::getEClass_EPackage()            { if (!g_initialized) initialize(); return g_eClass_EPackage; }
EClass* EcorePackage::getEClass_EObject()             { if (!g_initialized) initialize(); return g_eClass_EObject; }
EClass* EcorePackage::getEClass_EAttribute()          { if (!g_initialized) initialize(); return g_eClass_EAttribute; }
EClass* EcorePackage::getEClass_EStringToStringMapEntry() { if (!g_initialized) initialize(); return g_eClass_EStringToStringMapEntry; }

// ---- 内建 EDataType getters ----
EDataType* EcorePackage::getEDataType_EString()          { if (!g_initialized) initialize(); return g_eDataType_EString; }
EDataType* EcorePackage::getEDataType_EBoolean()         { if (!g_initialized) initialize(); return g_eDataType_EBoolean; }
EDataType* EcorePackage::getEDataType_EInt()             { if (!g_initialized) initialize(); return g_eDataType_EInt; }
EDataType* EcorePackage::getEDataType_EDouble()          { if (!g_initialized) initialize(); return g_eDataType_EDouble; }
EDataType* EcorePackage::getEDataType_EFloat()           { if (!g_initialized) initialize(); return g_eDataType_EFloat; }
EDataType* EcorePackage::getEDataType_ELong()            { if (!g_initialized) initialize(); return g_eDataType_ELong; }
EDataType* EcorePackage::getEDataType_EShort()           { if (!g_initialized) initialize(); return g_eDataType_EShort; }
EDataType* EcorePackage::getEDataType_EByte()            { if (!g_initialized) initialize(); return g_eDataType_EByte; }
EDataType* EcorePackage::getEDataType_EChar()            { if (!g_initialized) initialize(); return g_eDataType_EChar; }
EDataType* EcorePackage::getEDataType_EDate()            { if (!g_initialized) initialize(); return g_eDataType_EDate; }
EDataType* EcorePackage::getEDataType_EBigInteger()      { if (!g_initialized) initialize(); return g_eDataType_EBigInteger; }
EDataType* EcorePackage::getEDataType_EBigDecimal()      { if (!g_initialized) initialize(); return g_eDataType_EBigDecimal; }
EDataType* EcorePackage::getEDataType_EJavaObject()      { if (!g_initialized) initialize(); return g_eDataType_EJavaObject; }
EDataType* EcorePackage::getEDataType_EFeatureMapEntry() { if (!g_initialized) initialize(); return g_eDataType_EFeatureMapEntry; }
EDataType* EcorePackage::getEDataType_EFeatureMap()      { if (!g_initialized) initialize(); return g_eDataType_EFeatureMap; }
EDataType* EcorePackage::getEDataType_EBooleanObject()          { if (!g_initialized) initialize(); return g_eDataType_EBooleanObject; }
EDataType* EcorePackage::getEDataType_EByteArray()              { if (!g_initialized) initialize(); return g_eDataType_EByteArray; }
EDataType* EcorePackage::getEDataType_EByteObject()             { if (!g_initialized) initialize(); return g_eDataType_EByteObject; }
EDataType* EcorePackage::getEDataType_ECharacterObject()        { if (!g_initialized) initialize(); return g_eDataType_ECharacterObject; }
EDataType* EcorePackage::getEDataType_EDiagnosticChain()        { if (!g_initialized) initialize(); return g_eDataType_EDiagnosticChain; }
EDataType* EcorePackage::getEDataType_EDoubleObject()           { if (!g_initialized) initialize(); return g_eDataType_EDoubleObject; }
EDataType* EcorePackage::getEDataType_EEList()                  { if (!g_initialized) initialize(); return g_eDataType_EEList; }
EDataType* EcorePackage::getEDataType_EEnumerator()             { if (!g_initialized) initialize(); return g_eDataType_EEnumerator; }
EDataType* EcorePackage::getEDataType_EFloatObject()            { if (!g_initialized) initialize(); return g_eDataType_EFloatObject; }
EDataType* EcorePackage::getEDataType_EIntegerObject()          { if (!g_initialized) initialize(); return g_eDataType_EIntegerObject; }
EDataType* EcorePackage::getEDataType_EJavaClass()              { if (!g_initialized) initialize(); return g_eDataType_EJavaClass; }
EDataType* EcorePackage::getEDataType_ELongObject()             { if (!g_initialized) initialize(); return g_eDataType_ELongObject; }
EDataType* EcorePackage::getEDataType_EMap()                    { if (!g_initialized) initialize(); return g_eDataType_EMap; }
EDataType* EcorePackage::getEDataType_EResource()               { if (!g_initialized) initialize(); return g_eDataType_EResource; }
EDataType* EcorePackage::getEDataType_EResourceSet()            { if (!g_initialized) initialize(); return g_eDataType_EResourceSet; }
EDataType* EcorePackage::getEDataType_EShortObject()            { if (!g_initialized) initialize(); return g_eDataType_EShortObject; }
EDataType* EcorePackage::getEDataType_ETreeIterator()           { if (!g_initialized) initialize(); return g_eDataType_ETreeIterator; }
EDataType* EcorePackage::getEDataType_EInvocationTargetException() { if (!g_initialized) initialize(); return g_eDataType_EInvocationTargetException; }

// ---- FeatureID getters（直接返回全局常量，对齐方案 3 全局 FeatureID 命名空间）----
int EcorePackage::getFeatureID_EClass_eSuperTypes() {
    return ::emf::common::FeatureID::ECLASS_ESUPERTYPES;
}
int EcorePackage::getFeatureID_EClass_eStructuralFeatures() {
    return ::emf::common::FeatureID::ECLASS_ESTRUCTURALFEATURES;
}
int EcorePackage::getFeatureID_EPackage_eClassifiers() {
    return ::emf::common::FeatureID::EPACKAGE_ECLASSIFIERS;
}
int EcorePackage::getFeatureID_EPackage_eNsURI() {
    return ::emf::common::FeatureID::EPACKAGE_ENSURI;
}

// ============================================================================
// EClassImpl / EGenericTypeImpl / EFactoryImpl 的 eClass() override
// 返回对应的 meta EClass，使反射 eGet/eSet 能正确分发，并满足
// EModelElementImpl::getEAnnotation(EClass*, bool) 的 a->eClass()==eReference 比较。
// ============================================================================
emf::ecore::EClass* EClassImpl::eClass() const {
    return EcorePackage::instance().getEClass_EClass();
}

emf::ecore::EClass* EGenericTypeImpl::eClass() const {
    return EcorePackage::instance().getEClass_EGenericType();
}

emf::ecore::EClass* EFactoryImpl::eClass() const {
    return EcorePackage::instance().getEClass_EFactory();
}

// 中间基类 eClass（使测试可直接实例化）
emf::ecore::EClass* ETypedElementImpl::eClass() const {
    return EcorePackage::instance().getEClass_ETypedElement();
}

// 各具体 Impl 的 eClass override（对齐 Java XXXImpl.eClass() 返回静态 meta EClass）
emf::ecore::EClass* EDataTypeImpl::eClass() const {
    return EcorePackage::instance().getEClass_EDataType();
}
emf::ecore::EClass* EEnumImpl::eClass() const {
    return EcorePackage::instance().getEClass_EEnum();
}
emf::ecore::EClass* EEnumLiteralImpl::eClass() const {
    return EcorePackage::instance().getEClass_EEnumLiteral();
}
emf::ecore::EClass* EAttributeImpl::eClass() const {
    return EcorePackage::instance().getEClass_EAttribute();
}
emf::ecore::EClass* EReferenceImpl::eClass() const {
    return EcorePackage::instance().getEClass_EReference();
}
emf::ecore::EClass* EOperationImpl::eClass() const {
    return EcorePackage::instance().getEClass_EOperation();
}
emf::ecore::EClass* EParameterImpl::eClass() const {
    return EcorePackage::instance().getEClass_EParameter();
}
emf::ecore::EClass* ETypeParameterImpl::eClass() const {
    return EcorePackage::instance().getEClass_ETypeParameter();
}
emf::ecore::EClass* EAnnotationImpl::eClass() const {
    return EcorePackage::instance().getEClass_EAnnotation();
}
emf::ecore::EClass* EPackageImpl::eClass() const {
    return EcorePackage::instance().getEClass_EPackage();
}

}  // namespace emf::ecore
