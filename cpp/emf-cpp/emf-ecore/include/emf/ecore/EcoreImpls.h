// EMF Ecore: EcoreImpls.h
// 所有 Ecore 模型类的接口声明 + Impl 实现（方案 3：Java 风格 Impl 多继承接口+父Impl）
// 对齐 org.eclipse.emf.ecore.* 接口 + org.eclipse.emf.ecore.impl.*Impl 实现
//
// 继承链（对齐 Java Ecore 模型）：
//   EObject (emf::common)
//     └─ EModelElement → ENamedElement → ETypedElement → EClassifier
//         ├─ EClass
//         └─ EDataType → EEnum
//     EAnnotation : EModelElement
//     EGenericType : EModelElement
//     EFactory : EModelElement
//     EEnumLiteral : ENamedElement
//     EStructuralFeature : ETypedElement
//       ├─ EAttribute
//       └─ EReference
//     EOperation : ETypedElement
//     EParameter : ETypedElement
//     ETypeParameter : ENamedElement
//     EPackage : ENamedElement
//
// Impl 链：EObjectImpl → EModelElementImpl → ... → EXxxImpl : public EXxx, public <父>Impl
#pragma once

#include "emf/common/EObject.h"
#include "emf/common/EList.h"
#include "emf/common/Notification.h"
#include "emf/common/EPackage.h"
#include <any>
#include <string>
#include <vector>
#include <unordered_map>
#include <unordered_set>
#include <utility>

namespace emf::ecore {

// EObjectImpl 别名：生成代码引用 emf::ecore::EObjectImpl（对齐 emf::common::EObjectImpl）
using EObjectImpl = emf::common::EObjectImpl;

// ===== 前向声明（接口类）=====
class EAnnotation;
class EAttribute;
class EClass;
class EClassifier;
class EDataType;
class EEnum;
class EEnumLiteral;
class EFactory;
class EGenericType;
class EModelElement;
class ENamedElement;
class EOperation;
class EPackage;
class EParameter;
class EReference;
class EStructuralFeature;
class ETypeParameter;
class ETypedElement;

// ===== 前向声明（Impl 类）=====
class EAnnotationImpl;
class EAttributeImpl;
class EClassImpl;
class EClassifierImpl;
class EDataTypeImpl;
class EEnumImpl;
class EEnumLiteralImpl;
class EFactoryImpl;
class EGenericTypeImpl;
class EModelElementImpl;
class ENamedElementImpl;
class EOperationImpl;
class EPackageImpl;
class EParameterImpl;
class EReferenceImpl;
class EStructuralFeatureImpl;
class ETypeParameterImpl;
class ETypedElementImpl;

namespace util { class ConversionDelegate; }

// ============================================================================
// 接口类声明（对齐 Java org.eclipse.emf.ecore.* 接口）
// ============================================================================

// EModelElement：模型元素基类（对齐 Java EModelElement）
class EModelElement : public virtual emf::common::EObject {
public:
    virtual ~EModelElement() = default;
    virtual const std::vector<EAnnotation*>& getEAnnotations() const = 0;
    virtual EAnnotation* getEAnnotation(const std::string& source) const = 0;
    // addEAnnotation: 默认空实现，EModelElementImpl override 实际写入 annotations_
    // 对齐 Java EModelElementImpl.eAnnotations().add()（C++ 端用显式 add 方法替代可变 List）
    virtual void addEAnnotation(EAnnotation* /*ann*/) {}
};

// ENamedElement：命名元素（对齐 Java ENamedElement）
class ENamedElement : public virtual EModelElement {
public:
    virtual const std::string& getName() const = 0;
    virtual void setName(const std::string& value) = 0;
};

// ETypedElement：类型化元素（对齐 Java ETypedElement）
class ETypedElement : public virtual ENamedElement {
public:
    virtual EClassifier* getEType() const = 0;
    virtual void setEType(EClassifier* value) = 0;
    virtual EGenericType* getEGenericType() = 0;
    virtual void setEGenericType(EGenericType* value) = 0;
    virtual bool isOrdered() const = 0;
    virtual void setOrdered(bool value) = 0;
    virtual bool isUnique() const = 0;
    virtual void setUnique(bool value) = 0;
    virtual int getLowerBound() const = 0;
    virtual void setLowerBound(int value) = 0;
    virtual int getUpperBound() const = 0;
    virtual void setUpperBound(int value) = 0;
    virtual bool isMany() const = 0;
    virtual bool isRequired() const = 0;
};

// EClassifier：分类器（对齐 Java EClassifier）
class EClassifier : public virtual ETypedElement {
public:
    virtual const std::string& getInstanceClassName() const = 0;
    virtual void setInstanceClassName(const std::string& value) = 0;
    virtual std::vector<ETypeParameter*>& getETypeParameters() = 0;
    virtual EPackage* getEPackage() const = 0;
    virtual bool isInstance(emf::common::EObject* obj) const = 0;
};

// EClass：类（对齐 Java EClass）
class EClass : public virtual EClassifier {
public:
    virtual bool isAbstract() const = 0;
    virtual void setAbstract(bool value) = 0;
    virtual bool isInterface() const = 0;
    virtual void setInterface(bool value) = 0;
    // 对齐 Java EClass.isMapEntry（map entry 标志，默认 false）
    virtual bool isMapEntry() const = 0;
    virtual void setMapEntry(bool value) = 0;
    virtual const std::vector<EClass*>& getESuperTypes() const = 0;
    virtual const std::vector<EClass*>& getEAllSuperTypes() const = 0;
    virtual const std::vector<EStructuralFeature*>& getEStructuralFeatures() const = 0;
    virtual const std::vector<EStructuralFeature*>& getEAllStructuralFeatures() const = 0;
    virtual int getFeatureCount() const = 0;
    virtual EStructuralFeature* getEStructuralFeature(int featureID) const = 0;
    virtual EStructuralFeature* getEStructuralFeature(const std::string& name) const = 0;
    virtual int getFeatureID(EStructuralFeature* feature) const = 0;
    virtual int getFeatureID(const std::string& name) const = 0;
    virtual const std::vector<EAttribute*>& getEAttributes() const = 0;
    virtual const std::vector<EAttribute*>& getEAllAttributes() const = 0;
    virtual const std::vector<EReference*>& getEReferences() const = 0;
    virtual const std::vector<EReference*>& getEAllReferences() const = 0;
    virtual const std::vector<EReference*>& getEAllContainments() const = 0;
    virtual EAttribute* getEIDAttribute() const = 0;
    virtual const std::vector<EOperation*>& getEOperations() const = 0;
    virtual const std::vector<EOperation*>& getEAllOperations() const = 0;
    virtual int getOperationCount() const = 0;
    virtual EOperation* getEOperation(int operationID) const = 0;
    virtual int getOperationID(EOperation* operation) const = 0;
    virtual bool isSuperTypeOf(const EClass* someClass) const = 0;

    // 构建方法（C++ 端把 Java EClassImpl 的 public add 暴露到接口，方便模型构建）
    virtual void addESuperType(EClass* c) = 0;
    // eGenericSuperTypes：真实 containment 存储槽（对齐 Java EClassImpl.eGenericSuperTypes）。
    // eSuperTypes 是其派生视图。序列化按 Java isSet 互斥逻辑在属性与子元素间二选一。
    virtual const std::vector<EGenericType*>& getEGenericSuperTypes() const = 0;
    virtual void addEGenericSuperType(EGenericType* gt) = 0;
    virtual bool isSetEGenericSuperTypes() const = 0;
    virtual void addEStructuralFeature(EStructuralFeature* sf) = 0;
    virtual void addEOperation(EOperation* op) = 0;
    virtual EAttribute* getEAttribute(const std::string& name) const = 0;
    virtual EReference* getEReference(const std::string& name) const = 0;
    virtual EOperation* getEOperation(const std::string& name) const = 0;
    virtual int getOperationID(const std::string& name) const = 0;
    virtual const std::vector<EGenericType*>& getEAllGenericSuperTypes() const = 0;
    virtual EOperation* getOverride(EOperation* operation) const = 0;
};

// EDataType：数据类型（对齐 Java EDataType）
class EDataType : public virtual EClassifier {
public:
    virtual bool isSerializable() const = 0;
    virtual void setSerializable(bool value) = 0;
};

// EEnum：枚举（对齐 Java EEnum）
class EEnum : public virtual EDataType {
public:
    virtual const std::vector<EEnumLiteral*>& getELiterals() const = 0;
    virtual EEnumLiteral* getELiteral(const std::string& name) const = 0;
    virtual EEnumLiteral* getELiteralByValue(int value) const = 0;
    // addELiteral: 默认空实现，EEnumImpl override 实际写入 eLiterals_
    virtual void addELiteral(EEnumLiteral* /*lit*/) {}
};

// EEnumLiteral：枚举字面量（对齐 Java EEnumLiteral）
class EEnumLiteral : public virtual ENamedElement {
public:
    virtual int getValue() const = 0;
    virtual void setValue(int value) = 0;
    virtual const std::string& getLiteral() const = 0;
    virtual void setLiteral(const std::string& value) = 0;
    virtual EEnum* getEEnum() const = 0;
};

// EStructuralFeature：结构特性（对齐 Java EStructuralFeature）
class EStructuralFeature : public virtual ETypedElement {
public:
    virtual bool isChangeable() const = 0;
    virtual void setChangeable(bool value) = 0;
    virtual bool isVolatile() const = 0;
    virtual void setVolatile(bool value) = 0;
    virtual bool isTransient() const = 0;
    virtual void setTransient(bool value) = 0;
    virtual bool isUnsettable() const = 0;
    virtual void setUnsettable(bool value) = 0;
    virtual bool isDerived() const = 0;
    virtual void setDerived(bool value) = 0;
    virtual const std::string& getDefaultValueLiteral() const = 0;
    virtual void setDefaultValueLiteral(const std::string& value) = 0;
    virtual int getFeatureID() const = 0;
    virtual void setFeatureID(int value) = 0;
    virtual EClass* getEContainingClass() const = 0;
    virtual void setEContainingClass(EClass* cls) = 0;
};

// EAttribute：属性（对齐 Java EAttribute）
class EAttribute : public virtual EStructuralFeature {
public:
    virtual EDataType* getEAttributeType() const = 0;
    virtual void setEAttributeType(EDataType* value) = 0;
    virtual bool isID() const = 0;
    virtual void setID(bool value) = 0;
};

// EReference：引用（对齐 Java EReference）
class EReference : public virtual EStructuralFeature {
public:
    virtual bool isContainment() const = 0;
    virtual void setContainment(bool value) = 0;
    virtual bool isContainer() const = 0;
    virtual bool isResolveProxies() const = 0;
    virtual void setResolveProxies(bool value) = 0;
    virtual EReference* getEOpposite() const = 0;
    virtual void setEOpposite(EReference* value) = 0;
    virtual EClass* getEReferenceType() const = 0;
    virtual void setEReferenceType(EClass* value) = 0;
    // 对齐 Java EReference.getEKeys（键属性列表，默认空）
    virtual const std::vector<EAttribute*>& getEKeys() const = 0;
    virtual void addEKey(EAttribute* key) = 0;
};

// EOperation：操作（对齐 Java EOperation）
class EOperation : public virtual ETypedElement {
public:
    virtual const std::vector<EParameter*>& getEParameters() const = 0;
    virtual const std::vector<ETypeParameter*>& getETypeParameters() const = 0;
    virtual int getOperationID() const = 0;
    virtual void setOperationID(int value) = 0;
    // 对齐 Java EOperation.getEContainingClass()：反查所属 EClass（用于 XMILoader 推导 contextPkg）
    virtual EClass* getEContainingClass() const = 0;
    virtual void setEContainingClass(EClass* cls) = 0;
    // eExceptions：EOperation 抛出的异常类型列表（对齐 Java EOperation.eExceptions）
    virtual const std::vector<EClassifier*>& getEExceptions() const = 0;
    virtual void addEException(EClassifier* ex) = 0;
    virtual void clearEExceptions() = 0;
};

// EParameter：参数（对齐 Java EParameter）
class EParameter : public virtual ETypedElement {
public:
    virtual EOperation* getEOperation() const = 0;
    virtual void setEOperation(EOperation* value) = 0;
};

// ETypeParameter：类型参数（对齐 Java ETypeParameter）
class ETypeParameter : public virtual ENamedElement {
public:
    virtual const std::vector<EGenericType*>& getEBounds() const = 0;
    virtual void addEBound(EGenericType* bound) = 0;
};

// EGenericType：泛型类型（对齐 Java EGenericType）
class EGenericType : public virtual EModelElement {
public:
    virtual EClassifier* getEClassifier() const = 0;
    virtual void setEClassifier(EClassifier* value) = 0;
    virtual const std::vector<EGenericType*>& getETypeArguments() const = 0;
    virtual EGenericType* getEUpperBound() const = 0;
    virtual void setEUpperBound(EGenericType* value) = 0;
    virtual EGenericType* getELowerBound() const = 0;
    virtual void setELowerBound(EGenericType* value) = 0;
    virtual ETypeParameter* getETypeParameter() const = 0;
    virtual void setETypeParameter(ETypeParameter* value) = 0;
};

// EAnnotation：注解（对齐 Java EAnnotation）
class EAnnotation : public virtual EModelElement {
public:
    virtual const std::string& getSource() const = 0;
    virtual void setSource(const std::string& value) = 0;
    virtual void setDetail(const std::string& key, const std::string& value) = 0;
    virtual std::string getDetail(const std::string& key) const = 0;
    virtual const std::vector<std::pair<std::string, std::string>>& getDetails() const = 0;
    virtual const std::vector<emf::common::EObject*>& getEContents() const = 0;
    virtual const std::vector<emf::common::EObject*>& getEReferences() const = 0;
    virtual emf::common::EObject* getEModelElement() const = 0;
    virtual void setEModelElement(emf::common::EObject* value) = 0;
};

// EFactory：工厂（对齐 Java EFactory）
class EFactory : public virtual EModelElement, public virtual emf::common::EFactory {
public:
    virtual EPackage* getEPackage() const = 0;
    virtual void setEPackage(EPackage* value) = 0;
    virtual emf::common::EObject* create(const EClass* eClass) const = 0;
    virtual std::any createFromString(const EClassifier* classifier, const std::string& literal) const = 0;
    virtual std::string convertToString(const EClassifier* classifier, const std::any& value) const = 0;
};

// EPackage：包（对齐 Java EPackage）
class EPackage : public virtual ENamedElement, public virtual emf::common::EPackage {
public:
    // 桥接 getName：ENamedElement 与 common::EPackage 都声明了同名纯虚，
    // 在本接口层显式 override 消除通过 EPackage* 调用的歧义。
    const std::string& getName() const override = 0;

    virtual const std::string& getNsURI() const = 0;
    virtual void setNsURI(const std::string& value) = 0;
    virtual const std::string& getNsPrefix() const = 0;
    virtual void setNsPrefix(const std::string& value) = 0;
    virtual const std::vector<EClassifier*>& getEClassifiers() const = 0;
    virtual EClassifier* getEClassifier(const std::string& name) const = 0;
    virtual void addEClassifier(EClassifier* c) = 0;
    virtual EFactory* getEFactoryInstance() const = 0;
    virtual void setEFactoryInstance(EFactory* value) = 0;
    virtual EPackage* getESuperPackage() const = 0;
    virtual void setESuperPackage(EPackage* value) = 0;
    virtual const std::vector<EPackage*>& getESubPackages() const = 0;
    // 对齐 Java EPackage.getESubpackages（与 getESubPackages 同义，Java 命名）
    virtual const std::vector<EPackage*>& getESubpackages() const = 0;
};

// ============================================================================
// Impl 类声明（对齐 Java org.eclipse.emf.ecore.impl.*Impl）
// 多继承：public EXxx（接口）+ public <父>Impl（实现）
// ============================================================================

// EModelElementImpl：模型元素实现基类
class EModelElementImpl : public virtual EModelElement, public emf::common::EObjectImpl {
public:
    EAnnotation* getEAnnotation(const std::string& source) const;
    EAnnotation* getEAnnotation(EClass* eReference, bool resolve) const;
    void addEAnnotation(EAnnotation* ann);
    const std::vector<EAnnotation*>& getEAnnotations() const override { return annotations_; }

    std::any eGet(const EStructuralFeature* feature) const override;
    void eSet(const EStructuralFeature* feature, std::any value) override;
    bool eIsSet(const EStructuralFeature* feature) const override;
    void eUnset(const EStructuralFeature* feature) override;

protected:
    std::vector<EAnnotation*> annotations_;
};

// ENamedElementImpl
class ENamedElementImpl : public virtual ENamedElement, public EModelElementImpl {
public:
    const std::string& getName() const override { return name_; }
    void setName(const std::string& value) override { name_ = value; }

    std::any eGet(const EStructuralFeature* feature) const override;
    void eSet(const EStructuralFeature* feature, std::any value) override;
    bool eIsSet(const EStructuralFeature* feature) const override;
    void eUnset(const EStructuralFeature* feature) override;

protected:
    std::string name_;
};

// ETypedElementImpl
class ETypedElementImpl : public virtual ETypedElement, public ENamedElementImpl {
public:
    EClassifier* getEType() const override { return eType_; }
    void setEType(EClassifier* value) override;
    EGenericType* getEGenericType() override;
    void setEGenericType(EGenericType* value) override;
    bool isOrdered() const override { return ordered_; }
    void setOrdered(bool value) override { ordered_ = value; }
    bool isUnique() const override { return unique_; }
    void setUnique(bool value) override { unique_ = value; }
    int getLowerBound() const override { return lowerBound_; }
    void setLowerBound(int value) override { lowerBound_ = value; }
    int getUpperBound() const override { return upperBound_; }
    void setUpperBound(int value) override { upperBound_ = value; }
    bool isMany() const override { return upperBound_ == -1 || upperBound_ > 1; }
    bool isRequired() const override { return lowerBound_ > 0; }
    // 对齐 Java ETypedElementImpl：eGenericType 是否参数化（eTypeParameter != null || !eTypeArguments.isEmpty()）。
    // 序列化 isSet 互斥：参数化时写 <eGenericType> 子元素，否则写 eType 属性。
    bool isEGenericTypeParameterized() const;

    // 中间基类也提供 eClass（返回 meta ETypedElement），使测试可直接实例化
    emf::ecore::EClass* eClass() const override;

    std::any eGet(const EStructuralFeature* feature) const override;
    void eSet(const EStructuralFeature* feature, std::any value) override;
    bool eIsSet(const EStructuralFeature* feature) const override;
    void eUnset(const EStructuralFeature* feature) override;

protected:
    EClassifier* eType_ = nullptr;
    int lowerBound_ = 0;
    int upperBound_ = 1;
    bool ordered_ = true;
    bool unique_ = true;
    EGenericType* eGenericType_ = nullptr;
};

// EClassifierImpl
class EClassifierImpl : public virtual EClassifier, public ETypedElementImpl {
public:
    const std::string& getInstanceClassName() const override { return instanceClassName_; }
    void setInstanceClassName(const std::string& value) override { instanceClassName_ = value; }
    std::vector<ETypeParameter*>& getETypeParameters() override { return typeParams_; }
    void addTypeParameter(ETypeParameter* tp);
    EPackage* getEPackage() const override { return ePackage_; }
    void setEPackage(EPackage* value) { ePackage_ = value; }
    bool isInstance(emf::common::EObject* obj) const override;

    std::any eGet(const EStructuralFeature* feature) const override;
    void eSet(const EStructuralFeature* feature, std::any value) override;
    bool eIsSet(const EStructuralFeature* feature) const override;
    void eUnset(const EStructuralFeature* feature) override;

protected:
    std::string instanceClassName_;
    std::any defaultValue_;
    std::vector<ETypeParameter*> typeParams_;
    EPackage* ePackage_ = nullptr;
};

// EClassImpl
class EClassImpl : public virtual EClass, public EClassifierImpl {
public:
    ~EClassImpl() override;

    bool isAbstract() const override { return abstract_; }
    void setAbstract(bool value) override { abstract_ = value; }
    bool isInterface() const override { return interface_; }
    void setInterface(bool value) override { interface_ = value; }
    bool isMapEntry() const override { return mapEntry_; }
    void setMapEntry(bool value) override { mapEntry_ = value; }
    const std::vector<EClass*>& getESuperTypes() const override { return superTypes_; }
    const std::vector<EClass*>& getEAllSuperTypes() const override;
    const std::vector<EStructuralFeature*>& getEStructuralFeatures() const override { return features_; }
    const std::vector<EStructuralFeature*>& getEAllStructuralFeatures() const override;
    int getFeatureCount() const override;
    EStructuralFeature* getEStructuralFeature(int featureID) const override;
    EStructuralFeature* getEStructuralFeature(const std::string& name) const override;
    int getFeatureID(EStructuralFeature* feature) const override;
    int getFeatureID(const std::string& name) const override;
    const std::vector<EAttribute*>& getEAttributes() const override;
    const std::vector<EAttribute*>& getEAllAttributes() const override;
    const std::vector<EReference*>& getEReferences() const override;
    const std::vector<EReference*>& getEAllReferences() const override;
    const std::vector<EReference*>& getEAllContainments() const override;
    EAttribute* getEIDAttribute() const override;
    const std::vector<EOperation*>& getEOperations() const override { return operations_; }
    const std::vector<EOperation*>& getEAllOperations() const override;
    int getOperationCount() const override;
    EOperation* getEOperation(int operationID) const override;
    int getOperationID(EOperation* operation) const override;
    bool isSuperTypeOf(const EClass* someClass) const override;

    void addESuperType(EClass* c) override;
    const std::vector<EGenericType*>& getEGenericSuperTypes() const override;
    void addEGenericSuperType(EGenericType* gt) override;
    bool isSetEGenericSuperTypes() const override;
    // 对齐 Java EClassImpl：eGenericSuperTypes 的 eClassifier 延迟解析后，
    // 重新同步 superTypes_ derived view（去重）。
    void syncSuperTypesFromGeneric();
    void addEStructuralFeature(EStructuralFeature* sf) override;
    void addEOperation(EOperation* op) override;
    EAttribute* getEAttribute(const std::string& name) const override;
    EReference* getEReference(const std::string& name) const override;
    EOperation* getEOperation(const std::string& name) const override;
    int getOperationID(const std::string& name) const override;
    const std::vector<EGenericType*>& getEAllGenericSuperTypes() const override;
    EOperation* getOverride(EOperation* operation) const override;

    emf::ecore::EClass* eClass() const override;

    std::any eGet(const EStructuralFeature* feature) const override;
    void eSet(const EStructuralFeature* feature, std::any value) override;
    bool eIsSet(const EStructuralFeature* feature) const override;
    void eUnset(const EStructuralFeature* feature) override;

private:
    void computeEAllAttributes() const;
    void computeEAllReferences() const;
    void computeEAllStructuralFeatures() const;
    void computeEAllContainments() const;
    void computeEAllOperations() const;
    void computeEAllSuperTypes() const;
    void computeEAllGenericSuperTypes() const;

protected:
    std::vector<EClass*> superTypes_;
    std::vector<EStructuralFeature*> features_;
    std::vector<EOperation*> operations_;
    bool abstract_ = false;
    bool interface_ = false;
    bool mapEntry_ = false;
    // eGenericSuperTypes 真实 containment 存储（对齐 Java EClassImpl.eGenericSuperTypes）。
    // 为空时走 eSuperTypes 属性路径（Case A）；非空且含参数化条目时走 <eGenericSuperTypes> 子元素路径（Case B）。
    std::vector<EGenericType*> genericSuperTypes_;

    // lazy 缓存
    mutable std::vector<EAttribute*> eAllAttributesCache_;
    mutable bool eAllAttributesCached_ = false;
    mutable std::vector<EReference*> eAllReferencesCache_;
    mutable bool eAllReferencesCached_ = false;
    mutable std::vector<EStructuralFeature*> eAllStructuralFeaturesCache_;
    mutable bool eAllStructuralFeaturesCached_ = false;
    mutable std::vector<EReference*> eAllContainmentsCache_;
    mutable bool eAllContainmentsCached_ = false;
    mutable std::vector<EOperation*> eAllOperationsCache_;
    mutable bool eAllOperationsCached_ = false;
    mutable std::vector<EClass*> eAllSuperTypesCache_;
    mutable bool eAllSuperTypesCached_ = false;
    mutable std::vector<EGenericType*> eAllGenericSuperTypesCache_;
    mutable bool eAllGenericSuperTypesCached_ = false;
    mutable EAttribute* eIDAttributeCache_ = nullptr;
    mutable bool eIDAttributeComputed_ = false;
    mutable std::unordered_map<EOperation*, EOperation*> eOperationToOverrideMap_;
    mutable bool eOperationToOverrideComputed_ = false;
    mutable std::unordered_map<std::string, EStructuralFeature*> eNameToFeatureMap_;
    mutable bool eNameToFeatureComputed_ = false;
};

// EDataTypeImpl
class EDataTypeImpl : public virtual EDataType, public EClassifierImpl {
public:
    bool isSerializable() const override { return serializable_; }
    void setSerializable(bool value) override { serializable_ = value; }
    util::ConversionDelegate* getConversionDelegate() const;
    void setConversionDelegate(util::ConversionDelegate* d);

    emf::ecore::EClass* eClass() const override;
    std::any eGet(const EStructuralFeature* feature) const override;
    void eSet(const EStructuralFeature* feature, std::any value) override;
    bool eIsSet(const EStructuralFeature* feature) const override;
    void eUnset(const EStructuralFeature* feature) override;

protected:
    bool serializable_ = true;
    util::ConversionDelegate* conversionDelegate_ = nullptr;
    bool conversionDelegateIsSet_ = false;
};

// EEnumImpl
class EEnumImpl : public virtual EEnum, public EDataTypeImpl {
public:
    const std::vector<EEnumLiteral*>& getELiterals() const override { return eLiterals_; }
    EEnumLiteral* getELiteral(const std::string& name) const;
    EEnumLiteral* getELiteralByValue(int value) const;
    void addELiteral(EEnumLiteral* lit);

    emf::ecore::EClass* eClass() const override;
    std::any eGet(const EStructuralFeature* feature) const override;
    void eSet(const EStructuralFeature* feature, std::any value) override;
    bool eIsSet(const EStructuralFeature* feature) const override;
    void eUnset(const EStructuralFeature* feature) override;

protected:
    std::vector<EEnumLiteral*> eLiterals_;
};

// EEnumLiteralImpl
class EEnumLiteralImpl : public virtual EEnumLiteral, public ENamedElementImpl {
public:
    int getValue() const override { return value_; }
    void setValue(int value) override { value_ = value; }
    const std::string& getLiteral() const override { return literal_; }
    void setLiteral(const std::string& value) override { literal_ = value; }
    EEnum* getEEnum() const override { return eEnum_; }
    void setEEnum(EEnum* value) { eEnum_ = value; }

    emf::ecore::EClass* eClass() const override;
    std::any eGet(const EStructuralFeature* feature) const override;
    void eSet(const EStructuralFeature* feature, std::any value) override;
    bool eIsSet(const EStructuralFeature* feature) const override;
    void eUnset(const EStructuralFeature* feature) override;

protected:
    int value_ = 0;
    std::string literal_;
    EEnum* eEnum_ = nullptr;
};

// EStructuralFeatureImpl
class EStructuralFeatureImpl : public virtual EStructuralFeature, public ETypedElementImpl {
public:
    bool isChangeable() const override { return changeable_; }
    void setChangeable(bool value) override { changeable_ = value; }
    bool isVolatile() const override { return volatile_; }
    void setVolatile(bool value) override { volatile_ = value; }
    bool isTransient() const override { return transient_; }
    void setTransient(bool value) override { transient_ = value; }
    bool isUnsettable() const override { return unsettable_; }
    void setUnsettable(bool value) override { unsettable_ = value; }
    bool isDerived() const override { return derived_; }
    void setDerived(bool value) override { derived_ = value; }
    const std::string& getDefaultValueLiteral() const override { return defaultValueLiteral_; }
    void setDefaultValueLiteral(const std::string& value) override { defaultValueLiteral_ = value; defaultValueLiteralIsSet_ = true; }
    // 对齐 Java isSetDefaultValueLiteral：区分"未设置"与"显式设置为空字符串"。
    // std::string 无法区分两者，故用独立标志跟踪。saver 仅在 isSet 时输出 defaultValueLiteral。
    bool isSetDefaultValueLiteral() const { return defaultValueLiteralIsSet_; }
    int getFeatureID() const override { return featureID_; }
    void setFeatureID(int value) override { featureID_ = value; }
    EClass* getEContainingClass() const override { return containingClass_; }
    void setEContainingClass(EClass* cls) override { containingClass_ = cls; }
    EPackage* getEPackage() const;

    std::any eGet(const EStructuralFeature* feature) const override;
    void eSet(const EStructuralFeature* feature, std::any value) override;
    bool eIsSet(const EStructuralFeature* feature) const override;
    void eUnset(const EStructuralFeature* feature) override;

protected:
    int featureID_ = -1;
    bool changeable_ = true;
    bool volatile_ = false;
    bool transient_ = false;
    bool unsettable_ = false;
    bool derived_ = false;
    std::string defaultValueLiteral_;
    bool defaultValueLiteralIsSet_ = false;
    EClass* containingClass_ = nullptr;
};

// EAttributeImpl
class EAttributeImpl : public virtual EAttribute, public EStructuralFeatureImpl {
public:
    EDataType* getEAttributeType() const override { return eAttributeType_; }
    void setEAttributeType(EDataType* value) override;
    bool isID() const override { return iD_; }
    void setID(bool value) override { iD_ = value; }

    emf::ecore::EClass* eClass() const override;
    std::any eGet(const EStructuralFeature* feature) const override;
    void eSet(const EStructuralFeature* feature, std::any value) override;
    bool eIsSet(const EStructuralFeature* feature) const override;
    void eUnset(const EStructuralFeature* feature) override;

protected:
    EDataType* eAttributeType_ = nullptr;
    bool iD_ = false;
};

// EReferenceImpl
class EReferenceImpl : public virtual EReference, public EStructuralFeatureImpl {
public:
    bool isContainment() const override { return containment_; }
    void setContainment(bool value) override { containment_ = value; }
    bool isContainer() const override;  // 派生：getEOpposite()!=null && getEOpposite()->isContainment()（对齐 Java）
    bool isResolveProxies() const override { return resolveProxies_; }
    void setResolveProxies(bool value) override { resolveProxies_ = value; }
    EReference* getEOpposite() const override { return eOpposite_; }
    void setEOpposite(EReference* value) override { eOpposite_ = value; }
    EClass* getEReferenceType() const override;
    void setEReferenceType(EClass* value) override;
    const std::vector<EAttribute*>& getEKeys() const override { return eKeys_; }
    void addEKey(EAttribute* key) override { eKeys_.push_back(key); }

    emf::ecore::EClass* eClass() const override;
    std::any eGet(const EStructuralFeature* feature) const override;
    void eSet(const EStructuralFeature* feature, std::any value) override;
    bool eIsSet(const EStructuralFeature* feature) const override;
    void eUnset(const EStructuralFeature* feature) override;

protected:
    EClass* eReferenceType_ = nullptr;
    EReference* eOpposite_ = nullptr;
    bool containment_ = false;
    bool container_ = false;
    bool resolveProxies_ = true;
    std::vector<EAttribute*> eKeys_;
};

// EOperationImpl
class EOperationImpl : public virtual EOperation, public ETypedElementImpl {
public:
    const std::vector<EParameter*>& getEParameters() const override { return parameters_; }
    const std::vector<ETypeParameter*>& getETypeParameters() const override { return typeParams_; }
    void addEParameter(EParameter* p);
    void addETypeParameter(ETypeParameter* tp);
    int getOperationID() const override { return operationID_; }
    void setOperationID(int value) override { operationID_ = value; }
    EClass* getEContainingClass() const override { return eContainingClass_; }
    void setEContainingClass(EClass* cls) override { eContainingClass_ = cls; }
    const std::vector<EClassifier*>& getEExceptions() const override { return eExceptions_; }
    void addEException(EClassifier* ex) override { if (ex) eExceptions_.push_back(ex); }
    void clearEExceptions() override { eExceptions_.clear(); }

    emf::ecore::EClass* eClass() const override;
    std::any eGet(const EStructuralFeature* feature) const override;
    void eSet(const EStructuralFeature* feature, std::any value) override;
    bool eIsSet(const EStructuralFeature* feature) const override;
    void eUnset(const EStructuralFeature* feature) override;

protected:
    std::vector<EParameter*> parameters_;
    std::vector<ETypeParameter*> typeParams_;
    std::string body_;
    int operationID_ = -1;
    EClass* eContainingClass_ = nullptr;
    std::vector<EClassifier*> eExceptions_;
};

// EParameterImpl
class EParameterImpl : public virtual EParameter, public ETypedElementImpl {
public:
    EOperation* getEOperation() const override { return eOperation_; }
    void setEOperation(EOperation* value) override { eOperation_ = value; }

    emf::ecore::EClass* eClass() const override;
    std::any eGet(const EStructuralFeature* feature) const override;
    void eSet(const EStructuralFeature* feature, std::any value) override;
    bool eIsSet(const EStructuralFeature* feature) const override;
    void eUnset(const EStructuralFeature* feature) override;

protected:
    EOperation* eOperation_ = nullptr;
};

// ETypeParameterImpl
class ETypeParameterImpl : public virtual ETypeParameter, public ENamedElementImpl {
public:
    const std::vector<EGenericType*>& getEBounds() const override { return bounds_; }
    void addEBound(EGenericType* bound) override { bounds_.push_back(bound); }
    // container back-reference：由 EClassifierImpl::addTypeParameter / EOperationImpl::addETypeParameter 设置。
    // 用于 XMISaver 构造 eTypeParameter href（#//Class/Type 或 #//Class/op/Type）。
    void setEContainer(EModelElement* c) { eContainer_ = c; }
    EModelElement* getEContainer() const { return eContainer_; }

    emf::ecore::EClass* eClass() const override;
    std::any eGet(const EStructuralFeature* feature) const override;
    void eSet(const EStructuralFeature* feature, std::any value) override;
    bool eIsSet(const EStructuralFeature* feature) const override;
    void eUnset(const EStructuralFeature* feature) override;

protected:
    std::vector<EGenericType*> bounds_;
    EModelElement* eContainer_ = nullptr;
};

// EGenericTypeImpl
class EGenericTypeImpl : public virtual EGenericType, public EModelElementImpl {
public:
    EClassifier* getEClassifier() const override { return eClassifier_; }
    void setEClassifier(EClassifier* value) override { eClassifier_ = value; }
    const std::vector<EGenericType*>& getETypeArguments() const override { return eTypeArguments_; }
    void setETypeArguments(const std::vector<EGenericType*>& value) { eTypeArguments_ = value; }
    void addETypeArgument(EGenericType* arg) { eTypeArguments_.push_back(arg); }
    EGenericType* getEUpperBound() const override { return eUpperBound_; }
    void setEUpperBound(EGenericType* value) override { eUpperBound_ = value; }
    EGenericType* getELowerBound() const override { return eLowerBound_; }
    void setELowerBound(EGenericType* value) override { eLowerBound_ = value; }
    ETypeParameter* getETypeParameter() const override { return eTypeParameter_; }
    void setETypeParameter(ETypeParameter* value) override { eTypeParameter_ = value; }

    emf::ecore::EClass* eClass() const override;
    // 对齐 Java EGenericTypeImpl：反射 eGet/eSet/eIsSet/eUnset 处理 5 个自有 feature
    std::any eGet(const EStructuralFeature* feature) const override;
    void eSet(const EStructuralFeature* feature, std::any value) override;
    bool eIsSet(const EStructuralFeature* feature) const override;
    void eUnset(const EStructuralFeature* feature) override;

protected:
    EClassifier* eClassifier_ = nullptr;
    std::vector<EGenericType*> eTypeArguments_;
    EGenericType* eUpperBound_ = nullptr;
    EGenericType* eLowerBound_ = nullptr;
    ETypeParameter* eTypeParameter_ = nullptr;
};

// EAnnotationImpl
class EAnnotationImpl : public virtual EAnnotation, public EModelElementImpl {
public:
    const std::string& getSource() const override { return source_; }
    void setSource(const std::string& value) override { source_ = value; }
    void setDetail(const std::string& key, const std::string& value) override;
    std::string getDetail(const std::string& key) const override;
    const std::vector<std::pair<std::string, std::string>>& getDetails() const override { return details_; }
    const std::vector<emf::common::EObject*>& getEContents() const override { return contents_; }
    const std::vector<emf::common::EObject*>& getEReferences() const override { return references_; }
    emf::common::EObject* getEModelElement() const override { return eModelElement_; }
    void setEModelElement(emf::common::EObject* value) override;

    emf::ecore::EClass* eClass() const override;
    std::any eGet(const EStructuralFeature* feature) const override;
    void eSet(const EStructuralFeature* feature, std::any value) override;
    bool eIsSet(const EStructuralFeature* feature) const override;
    void eUnset(const EStructuralFeature* feature) override;

protected:
    std::string source_;
    std::vector<std::pair<std::string, std::string>> details_;
    std::vector<emf::common::EObject*> contents_;
    std::vector<emf::common::EObject*> references_;
    emf::common::EObject* eModelElement_ = nullptr;
};

// EFactoryImpl
class EFactoryImpl : public virtual EFactory, public EModelElementImpl {
public:
    emf::common::EObject* create(const EClass* eClass) const override;
    std::any createFromString(const EClassifier* classifier, const std::string& literal) const override;
    std::string convertToString(const EClassifier* classifier, const std::any& value) const override;
    EPackage* getEPackage() const override { return ePackage_; }
    void setEPackage(EPackage* value) override { ePackage_ = value; }

    std::any eGet(const EStructuralFeature* feature) const override;
    void eSet(const EStructuralFeature* feature, std::any value) override;
    bool eIsSet(const EStructuralFeature* feature) const override;
    void eUnset(const EStructuralFeature* feature) override;

    emf::ecore::EClass* eClass() const override;

protected:
    EPackage* ePackage_ = nullptr;
};

// EPackageImpl
class EPackageImpl : public virtual EPackage, public ENamedElementImpl {
public:
    const std::string& getNsURI() const override { return nsURI_; }
    void setNsURI(const std::string& value) override { nsURI_ = value; }
    const std::string& getNsPrefix() const override { return nsPrefix_; }
    void setNsPrefix(const std::string& value) override { nsPrefix_ = value; }
    const std::vector<EClassifier*>& getEClassifiers() const override { return classifiers_; }
    EClassifier* getEClassifier(const std::string& name) const override;
    void addEClassifier(EClassifier* c) override;
    EFactory* getEFactoryInstance() const override {
        // 对齐 Java EPackageImpl.getEFactoryInstance()：动态包未设置 factory 时懒创建
        if (!factory_) {
            factory_ = new EFactoryImpl();
            factory_->setEPackage(const_cast<EPackageImpl*>(this));
        }
        return factory_;
    }
    void setEFactoryInstance(EFactory* value) override { factory_ = value; }
    EPackage* getESuperPackage() const override { return superPackage_; }
    void setESuperPackage(EPackage* value) override { superPackage_ = value; }
    const std::vector<EPackage*>& getESubPackages() const override { return subpackages_; }
    const std::vector<EPackage*>& getESubpackages() const override { return subpackages_; }
    // 添加直接子包（对齐 Java EPackageImpl.eSet 子包注册）
    // 生成的根包 initialize 调用此方法把子包加入 subpackages_ 列表,
    // 供 Loader 遍历子包查找 EClass。
    void addESubpackage(EPackage* sub) {
        if (!sub) return;
        for (auto* s : subpackages_) if (s == sub) return;
        subpackages_.push_back(sub);
    }

    // 桥接 common::EPackage::getName（已在 EPackage 接口层 override，此处提供实现）
    const std::string& getName() const override { return ENamedElementImpl::getName(); }

    emf::ecore::EClass* eClass() const override;
    std::any eGet(const EStructuralFeature* feature) const override;
    void eSet(const EStructuralFeature* feature, std::any value) override;
    bool eIsSet(const EStructuralFeature* feature) const override;
    void eUnset(const EStructuralFeature* feature) override;

protected:
    std::string nsURI_;
    std::string nsPrefix_;
    std::vector<EClassifier*> classifiers_;
    mutable EFactory* factory_ = nullptr;
    EPackage* superPackage_ = nullptr;
    std::vector<EPackage*> subpackages_;
};

}  // namespace emf::ecore
