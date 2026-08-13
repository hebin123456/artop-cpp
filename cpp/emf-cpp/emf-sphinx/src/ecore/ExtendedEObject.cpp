// ExtendedEObject.cpp
// 对齐 Java org.eclipse.sphinx.emf.ecore.ExtendedEObjectImpl
#include "emf/sphinx/ecore/ExtendedEObject.h"

#include "emf/sphinx/ecore/proxymanagement/ProxyResolutionBehavior.h"
#include "emf/ecore/EcoreImpls.h"

namespace emf::sphinx::ecore {

ExtendedEObject::~ExtendedEObject() = default;

emf::common::EObject* ExtendedEObject::eResolveProxy(emf::common::EObject* proxy) const {
    if (proxy == nullptr) {
        return nullptr;
    }
    // 对齐 Java：委托 ProxyResolutionBehavior 集中解析
    emf::common::EObject* self = const_cast<ExtendedEObject*>(this);
    return proxymanagement::ProxyResolutionBehavior::eResolveProxy(self, proxy);
}

emf::ecore::EAnnotation* ExtendedEObject::getExtendedMetaDataAnnotation() const {
    emf::ecore::EClass* cls = eClass();
    if (cls == nullptr) {
        return nullptr;
    }
    // EClass 继承 EModelElement，提供 getEAnnotation(source)
    return cls->getEAnnotation("extendedMetaData");
}

void ExtendedEObject::eNotify(const emf::common::Notification& notification) {
    // 默认 no-op：EObjectImpl 未维护 adapter 列表，由子类/容器覆盖
    (void)notification;
}

}  // namespace emf::sphinx::ecore
