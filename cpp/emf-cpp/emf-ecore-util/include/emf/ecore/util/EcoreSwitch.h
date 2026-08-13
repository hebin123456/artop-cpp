// EMF Ecore-util: EcoreSwitch
// 对齐 Java: org.eclipse.emf.ecore.util.EcoreSwitch
//
// 模型对象Visitor基类：根据 EObject 的运行时 EClass 分派到对应的 caseXxx 方法。
// 子类 override 需要的 caseXxx 即可实现对特定元模型节点的处理。
#pragma once

#include "emf/common/EObject.h"

namespace emf::ecore {
class EClass;
class EDataType;
class EEnum;
class EEnumLiteral;
class EAttribute;
class EReference;
class EStructuralFeature;
class EOperation;
class EParameter;
class EAnnotation;
class ETypeParameter;
class EGenericType;
class EPackage;
class EFactory;
class EModelElement;
class ENamedElement;
class ETypedElement;
class EClassifier;
}  // namespace emf::ecore

namespace emf::ecore::util {

class EcoreSwitch {
public:
    virtual ~EcoreSwitch() = default;

    // 分派入口：按 dynamic_cast 顺序匹配最具体类型
    emf::common::EObject* doSwitch(emf::common::EObject* obj);

    // ===== caseXxx 钩子（默认返回 nullptr，对齐 Java EcoreSwitch 默认行为）=====
    virtual emf::common::EObject* caseEClass(emf::ecore::EClass* obj)             { (void)obj; return nullptr; }
    virtual emf::common::EObject* caseEDataType(emf::ecore::EDataType* obj)       { (void)obj; return nullptr; }
    virtual emf::common::EObject* caseEEnum(emf::ecore::EEnum* obj)               { (void)obj; return nullptr; }
    virtual emf::common::EObject* caseEEnumLiteral(emf::ecore::EEnumLiteral* obj) { (void)obj; return nullptr; }
    virtual emf::common::EObject* caseEAttribute(emf::ecore::EAttribute* obj)     { (void)obj; return nullptr; }
    virtual emf::common::EObject* caseEReference(emf::ecore::EReference* obj)     { (void)obj; return nullptr; }
    virtual emf::common::EObject* caseEStructuralFeature(emf::ecore::EStructuralFeature* obj) { (void)obj; return nullptr; }
    virtual emf::common::EObject* caseEOperation(emf::ecore::EOperation* obj)     { (void)obj; return nullptr; }
    virtual emf::common::EObject* caseEParameter(emf::ecore::EParameter* obj)     { (void)obj; return nullptr; }
    virtual emf::common::EObject* caseEAnnotation(emf::ecore::EAnnotation* obj)   { (void)obj; return nullptr; }
    virtual emf::common::EObject* caseETypeParameter(emf::ecore::ETypeParameter* obj) { (void)obj; return nullptr; }
    virtual emf::common::EObject* caseEGenericType(emf::ecore::EGenericType* obj) { (void)obj; return nullptr; }
    virtual emf::common::EObject* caseEPackage(emf::ecore::EPackage* obj)         { (void)obj; return nullptr; }
    virtual emf::common::EObject* caseEFactory(emf::ecore::EFactory* obj)         { (void)obj; return nullptr; }
    virtual emf::common::EObject* caseETypedElement(emf::ecore::ETypedElement* obj) { (void)obj; return nullptr; }
    virtual emf::common::EObject* caseEClassifier(emf::ecore::EClassifier* obj)   { (void)obj; return nullptr; }
    virtual emf::common::EObject* caseENamedElement(emf::ecore::ENamedElement* obj) { (void)obj; return nullptr; }
    virtual emf::common::EObject* caseEModelElement(emf::ecore::EModelElement* obj) { (void)obj; return nullptr; }
    // 兜底（对齐 Java caseEObject 默认返回 obj）
    virtual emf::common::EObject* caseEObject(emf::common::EObject* obj)          { return obj; }
};

}  // namespace emf::ecore::util
