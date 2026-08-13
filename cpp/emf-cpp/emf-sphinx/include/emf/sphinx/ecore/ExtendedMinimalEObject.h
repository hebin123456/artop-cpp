// ExtendedMinimalEObject.h
// C++ 专有：轻量级 EObject（最小开销）。
//
// Java sphinx 中没有完全对应的类（MinimalEObjectImpl 由 EMF 提供），
// 此处为 C++ 端提供的轻量包装：复用 BasicEObject 的动态值存储与反向引用维护，
// 仅维护一个可设置的 eClass 字段，可被直接实例化用作占位/动态对象。
#pragma once

#include "emf/ecore/impl/BasicEObject.h"

namespace emf::ecore {
class EClass;
}  // namespace emf::ecore

namespace emf::sphinx::ecore {

class ExtendedMinimalEObject : public emf::ecore::impl::BasicEObject {
public:
    ExtendedMinimalEObject() = default;
    explicit ExtendedMinimalEObject(emf::ecore::EClass* eClass);
    ~ExtendedMinimalEObject() override;

    emf::ecore::EClass* eClass() const override { return eClass_; }
    void setEClass(emf::ecore::EClass* cls) { eClass_ = cls; }

private:
    emf::ecore::EClass* eClass_ = nullptr;
};

}  // namespace emf::sphinx::ecore
