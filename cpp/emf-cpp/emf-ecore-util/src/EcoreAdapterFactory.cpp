// EcoreAdapterFactory 实现
#include "emf/ecore/util/EcoreAdapterFactory.h"
#include "emf/ecore/EcoreImpls.h"

namespace emf::ecore::util {

EcoreAdapterFactory& EcoreAdapterFactory::instance() {
    static EcoreAdapterFactory f;
    return f;
}

emf::common::EAdapter* EcoreAdapterFactory::adapt(emf::common::Notifier* n, EClass* eClass) {
    if (!n || !eClass) return nullptr;
    const std::string& clsName = eClass->getName();
    if (clsName == "EClass") return createEClassAdapter();
    if (clsName == "EAttribute") return createEAttributeAdapter();
    if (clsName == "EReference") return createEReferenceAdapter();
    if (clsName == "EDataType") return createEDataTypeAdapter();
    if (clsName == "EEnum") return createEEnumAdapter();
    if (clsName == "EEnumLiteral") return createEEnumLiteralAdapter();
    if (clsName == "EOperation") return createEOperationAdapter();
    if (clsName == "EParameter") return createEParameterAdapter();
    if (clsName == "EAnnotation") return createEAnnotationAdapter();
    if (clsName == "EPackage") return createEPackageAdapter();
    if (clsName == "EFactory") return createEFactoryAdapter();
    if (clsName == "EStructuralFeature") return createEStructuralFeatureAdapter();
    if (clsName == "ENamedElement") return createENamedElementAdapter();
    if (clsName == "EClassifier") return createEClassifierAdapter();
    if (clsName == "EModelElement") return createEModelElementAdapter();
    return createEObjectAdapter();
}

}  // namespace emf::ecore::util
