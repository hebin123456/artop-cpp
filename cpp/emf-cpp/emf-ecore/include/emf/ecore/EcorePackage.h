// EMF Ecore: EcorePackage.h
// 元模型包单例 + FeatureID 枚举
// 对齐 org.eclipse.emf.ecore.EcorePackage
//
// FeatureID 定义在 namespace emf::common（对齐 .cpp 中 ::emf::common::FeatureID::XXX 用法）
#pragma once

#include "emf/ecore/EcoreImpls.h"
#include <string>

// FeatureID 枚举：全局 FeatureID 命名空间（方案 3）
// 所有 ecore 元模型的 feature 都在此枚举，.cpp 用 ::emf::common::FeatureID::XXX 引用
namespace emf::common {

namespace FeatureID {
// EModelElement
constexpr int EMODEL_ELEMENT_EANNOTATIONS = 0;
// ENamedElement
constexpr int ENAMED_ELEMENT_ENAME = 1000;
// ETypedElement
constexpr int ETYPED_ELEMENT_ELOWERBOUND = 2000;
constexpr int ETYPED_ELEMENT_EUPPERBOUND = 2001;
constexpr int ETYPED_ELEMENT_EORDERED = 2002;
constexpr int ETYPED_ELEMENT_EUNIQUE = 2003;
constexpr int ETYPED_ELEMENT_ETYPE = 2004;
constexpr int ETYPED_ELEMENT_EGENERICTYPE = 2005;
// EClassifier
constexpr int ECLASSIFIER_EINSTANCECLASSNAME = 3000;
constexpr int ECLASSIFIER_EDEFAULTVALUE = 3001;
constexpr int ECLASSIFIER_ETYPEPARAMETERS = 3002;
// EClass
constexpr int ECLASS_EABSTRACT = 4000;
constexpr int ECLASS_EINTERFACE = 4001;
constexpr int ECLASS_ESUPERTYPES = 4002;
constexpr int ECLASS_ESTRUCTURALFEATURES = 4003;
constexpr int ECLASS_EOPERATIONS = 4004;
constexpr int ECLASS_EALLATTRIBUTES = 4005;
constexpr int ECLASS_EALLREFERENCES = 4006;
constexpr int ECLASS_EATTRIBUTES = 4007;
constexpr int ECLASS_EREFERENCES = 4008;
constexpr int ECLASS_EALLOPERATIONS = 4009;
constexpr int ECLASS_EALLSTRUCTURALFEATURES = 4010;
constexpr int ECLASS_EGENERICSUPERTYPES = 4011;
// EDataType
constexpr int EDATATYPE_ESERIALIZABLE = 5000;
// EEnum
constexpr int EENUM_ELITERALS = 6000;
// EEnumLiteral
constexpr int EENUMLITERAL_EVALUE = 7000;
constexpr int EENUMLITERAL_ELITERAL = 7001;
constexpr int EENUMLITERAL_EINSTANCE = 7002;
constexpr int EENUMLITERAL_EENUM = 7003;
// EFactory
constexpr int EFACTORY_EPACKAGE = 8000;
// EOperation
constexpr int EOPERATION_EPARAMETERS = 9000;
constexpr int EOPERATION_EEXCEPTIONS = 9001;
constexpr int EOPERATION_EBODY = 9002;
constexpr int EOPERATION_ETYPEPARAMETERS = 9003;
// EPackage
constexpr int EPACKAGE_ENSURI = 10000;
constexpr int EPACKAGE_ENSPREFIX = 10001;
constexpr int EPACKAGE_ECLASSIFIERS = 10002;
constexpr int EPACKAGE_EFACTORYINSTANCE = 10003;
constexpr int EPACKAGE_ESUPERPACKAGE_NEW = 10004;
constexpr int EPACKAGE_ESUBPACKAGES = 10005;
// EStructuralFeature
constexpr int ESTRUCTURALFEATURE_EFEATUREID = 11000;
constexpr int ESTRUCTURALFEATURE_ECHANGEABLE = 11001;
constexpr int ESTRUCTURALFEATURE_EVOLATILE = 11002;
constexpr int ESTRUCTURALFEATURE_ETRANSIENT = 11003;
constexpr int ESTRUCTURALFEATURE_EUNSETTABLE = 11004;
constexpr int ESTRUCTURALFEATURE_EDERIVED = 11005;
constexpr int ESTRUCTURALFEATURE_EDEFAULTVALUELITERAL = 11006;
constexpr int ESTRUCTURALFEATURE_EECONTAININGCLASS = 11007;
// EAttribute
constexpr int EATTRIBUTE_EATTRIBUTETYPE = 12000;
constexpr int EATTRIBUTE_EID = 12001;
// EReference
constexpr int EREFERENCE_EREFERENCETYPE = 13000;
constexpr int EREFERENCE_EOPPOSITE = 13001;
constexpr int EREFERENCE_ECONTAINMENT = 13002;
constexpr int EREFERENCE_ECONTAINER = 13003;
constexpr int EREFERENCE_ERESOLVEPROXIES = 13004;
// EParameter
constexpr int EPARAMETER_EOPERATION = 14000;
// EGenericType
constexpr int EGENERICTYPE_ECLASSIFIER = 15000;
constexpr int EGENERICTYPE_ETYPEARGUMENTS = 15001;
constexpr int EGENERICTYPE_EUPPERBOUND = 15002;
constexpr int EGENERICTYPE_ELOWERBOUND = 15003;
constexpr int EGENERICTYPE_ETYPEPARAMETER = 15004;
// EAnnotation
constexpr int EANNOTATION_ESOURCE = 16000;
constexpr int EANNOTATION_EDETAILS = 16001;
constexpr int EANNOTATION_ECONTENTS = 16002;
constexpr int EANNOTATION_EREFERENCES = 16003;
constexpr int EANNOTATION_EMODEL_ELEMENT = 16004;
// ETypeParameter
constexpr int ETYPEPARAMETER_EBOUNDS = 17000;
}  // namespace FeatureID

}  // namespace emf::common

namespace emf::ecore {

// EcoreFactory：ecore 元模型工厂单例
// 对齐 org.eclipse.emf.ecore.EcoreFactory
class EcoreFactory {
public:
    static const char* eNS_URI;
    static const char* eNS_PREFIX;
    static const char* eNAME;

    static void initialize();
    static EcoreFactory& instance();

    EClass* createEClass();
    EAttribute* createEAttribute();
    EReference* createEReference();
    EOperation* createEOperation();
    EParameter* createEParameter();
    EPackage* createEPackage();
    EEnum* createEEnum();
    EEnumLiteral* createEEnumLiteral();
    EDataType* createEDataType();
    EAnnotation* createEAnnotation();
    ETypeParameter* createETypeParameter();
    EGenericType* createEGenericType();
    EFactory* createEFactory();

    emf::common::EObject* create(const EClass* eClass);
    std::any createFromString(const EClassifier* cls, const std::string& literal);
    std::string convertToString(const EClassifier* cls, const std::any& value);
};

// EcorePackage：ecore 元模型包单例
// 对齐 org.eclipse.emf.ecore.EcorePackage
class EcorePackage {
public:
    static const char* eNS_URI;
    static const char* eNS_PREFIX;
    static const char* eNAME;

    static void initialize();
    static EcorePackage& instance();

    EPackage* getEPackage();
    EFactory* getEFactory();

    // 元 EClass getter
    EClass* getEClass_EModelElement();
    EClass* getEClass_ENamedElement();
    EClass* getEClass_ETypedElement();
    EClass* getEClass_EClassifier();
    EClass* getEClass_EClass();
    EClass* getEClass_EDataType();
    EClass* getEClass_EEnum();
    EClass* getEClass_EEnumLiteral();
    EClass* getEClass_EFactory();
    EClass* getEClass_EOperation();
    EClass* getEClass_EParameter();
    EClass* getEClass_EReference();
    EClass* getEClass_EStructuralFeature();
    EClass* getEClass_ETypeParameter();
    EClass* getEClass_EGenericType();
    EClass* getEClass_EAnnotation();
    EClass* getEClass_EPackage();
    EClass* getEClass_EObject();
    EClass* getEClass_EAttribute();
    // 内建 EClass：EStringToStringMapEntry（对齐 Java EcorePackageImpl，XMLType.ecore 引用）
    EClass* getEClass_EStringToStringMapEntry();

    // 内建 EDataType getter
    EDataType* getEDataType_EString();
    EDataType* getEDataType_EBoolean();
    EDataType* getEDataType_EInt();
    EDataType* getEDataType_EDouble();
    EDataType* getEDataType_EFloat();
    EDataType* getEDataType_ELong();
    EDataType* getEDataType_EShort();
    EDataType* getEDataType_EByte();
    EDataType* getEDataType_EChar();
    EDataType* getEDataType_EDate();
    EDataType* getEDataType_EBigInteger();
    EDataType* getEDataType_EBigDecimal();
    EDataType* getEDataType_EJavaObject();
    EDataType* getEDataType_EFeatureMapEntry();
    EDataType* getEDataType_EFeatureMap();
    // 对齐 Java EcorePackageImpl 全部 33 个内建 EDataType（补齐缺失的 18 个）
    EDataType* getEDataType_EBooleanObject();
    EDataType* getEDataType_EByteArray();
    EDataType* getEDataType_EByteObject();
    EDataType* getEDataType_ECharacterObject();
    EDataType* getEDataType_EDiagnosticChain();
    EDataType* getEDataType_EDoubleObject();
    EDataType* getEDataType_EEList();
    EDataType* getEDataType_EEnumerator();
    EDataType* getEDataType_EFloatObject();
    EDataType* getEDataType_EIntegerObject();
    EDataType* getEDataType_EJavaClass();
    EDataType* getEDataType_ELongObject();
    EDataType* getEDataType_EMap();
    EDataType* getEDataType_EResource();
    EDataType* getEDataType_EResourceSet();
    EDataType* getEDataType_EShortObject();
    EDataType* getEDataType_ETreeIterator();
    EDataType* getEDataType_EInvocationTargetException();

    // FeatureID getter
    int getFeatureID_EClass_eSuperTypes();
    int getFeatureID_EClass_eStructuralFeatures();
    int getFeatureID_EPackage_eClassifiers();
    int getFeatureID_EPackage_eNsURI();
};

}  // namespace emf::ecore
