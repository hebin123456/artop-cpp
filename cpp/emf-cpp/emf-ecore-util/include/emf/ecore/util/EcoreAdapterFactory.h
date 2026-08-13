// EMF Ecore-util: EcoreAdapterFactory
// 对齐 Java: org.eclipse.emf.ecore.util.EcoreAdapterFactory
//
// 为 Ecore 元模型对象创建 Adapter 的工厂。按目标 EClass 名分派到对应的
// createXxxAdapter 钩子（基类默认返回 nullptr，子类按需 override）。
#pragma once

#include "emf/common/EObject.h"

namespace emf::common { class EAdapter; class Notifier; }
namespace emf::ecore { class EClass; }

namespace emf::ecore::util {

class EcoreAdapterFactory {
public:
    static EcoreAdapterFactory& instance();

    // 按 eClass 名分派
    emf::common::EAdapter* adapt(emf::common::Notifier* n, emf::ecore::EClass* eClass);

    // ===== createXxxAdapter 钩子（默认返回 nullptr）=====
    virtual emf::common::EAdapter* createEClassAdapter()             { return nullptr; }
    virtual emf::common::EAdapter* createEAttributeAdapter()         { return nullptr; }
    virtual emf::common::EAdapter* createEReferenceAdapter()         { return nullptr; }
    virtual emf::common::EAdapter* createEDataTypeAdapter()          { return nullptr; }
    virtual emf::common::EAdapter* createEEnumAdapter()              { return nullptr; }
    virtual emf::common::EAdapter* createEEnumLiteralAdapter()       { return nullptr; }
    virtual emf::common::EAdapter* createEOperationAdapter()         { return nullptr; }
    virtual emf::common::EAdapter* createEParameterAdapter()         { return nullptr; }
    virtual emf::common::EAdapter* createEAnnotationAdapter()        { return nullptr; }
    virtual emf::common::EAdapter* createEPackageAdapter()           { return nullptr; }
    virtual emf::common::EAdapter* createEFactoryAdapter()           { return nullptr; }
    virtual emf::common::EAdapter* createEStructuralFeatureAdapter() { return nullptr; }
    virtual emf::common::EAdapter* createENamedElementAdapter()      { return nullptr; }
    virtual emf::common::EAdapter* createEClassifierAdapter()        { return nullptr; }
    virtual emf::common::EAdapter* createEModelElementAdapter()      { return nullptr; }
    virtual emf::common::EAdapter* createEObjectAdapter()            { return nullptr; }
};

}  // namespace emf::ecore::util
