// EcoreSwitch 实现
#include "emf/ecore/util/EcoreSwitch.h"
#include "emf/ecore/EcoreImpls.h"

namespace emf::ecore::util {

using emf::common::EObject;
using emf::ecore::EClass;
using emf::ecore::EDataType;
using emf::ecore::EEnum;
using emf::ecore::EEnumLiteral;
using emf::ecore::EAttribute;
using emf::ecore::EReference;
using emf::ecore::EOperation;
using emf::ecore::EParameter;
using emf::ecore::EAnnotation;
using emf::ecore::ETypeParameter;
using emf::ecore::EGenericType;
using emf::ecore::EPackage;
using emf::ecore::EFactory;
using emf::ecore::EModelElement;
using emf::ecore::ENamedElement;
using emf::ecore::ETypedElement;
using emf::ecore::EClassifier;
using emf::ecore::EStructuralFeature;

EObject* EcoreSwitch::doSwitch(EObject* obj) {
    if (!obj) return nullptr;
    if (auto* c = dynamic_cast<EClass*>(obj)) return caseEClass(c);
    if (auto* c = dynamic_cast<EDataType*>(obj)) return caseEDataType(c);
    if (auto* c = dynamic_cast<EEnum*>(obj)) return caseEEnum(c);
    if (auto* c = dynamic_cast<EEnumLiteral*>(obj)) return caseEEnumLiteral(c);
    if (auto* c = dynamic_cast<EAttribute*>(obj)) return caseEAttribute(c);
    if (auto* c = dynamic_cast<EReference*>(obj)) return caseEReference(c);
    if (auto* c = dynamic_cast<EStructuralFeature*>(obj)) return caseEStructuralFeature(c);
    if (auto* c = dynamic_cast<EOperation*>(obj)) return caseEOperation(c);
    if (auto* c = dynamic_cast<EParameter*>(obj)) return caseEParameter(c);
    if (auto* c = dynamic_cast<EAnnotation*>(obj)) return caseEAnnotation(c);
    if (auto* c = dynamic_cast<ETypeParameter*>(obj)) return caseETypeParameter(c);
    if (auto* c = dynamic_cast<EGenericType*>(obj)) return caseEGenericType(c);
    if (auto* c = dynamic_cast<EPackage*>(obj)) return caseEPackage(c);
    if (auto* c = dynamic_cast<EFactory*>(obj)) return caseEFactory(c);
    if (auto* c = dynamic_cast<ETypedElement*>(obj)) return caseETypedElement(c);
    if (auto* c = dynamic_cast<EClassifier*>(obj)) return caseEClassifier(c);
    if (auto* c = dynamic_cast<ENamedElement*>(obj)) return caseENamedElement(c);
    if (auto* c = dynamic_cast<EModelElement*>(obj)) return caseEModelElement(c);
    return caseEObject(obj);
}

}  // namespace emf::ecore::util
