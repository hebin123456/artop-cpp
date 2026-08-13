// ExtendedMinimalEObject.cpp
// C++ 专有：轻量级 EObject
#include "emf/sphinx/ecore/ExtendedMinimalEObject.h"

namespace emf::sphinx::ecore {

ExtendedMinimalEObject::ExtendedMinimalEObject(emf::ecore::EClass* eClass) : eClass_(eClass) {}

ExtendedMinimalEObject::~ExtendedMinimalEObject() = default;

}  // namespace emf::sphinx::ecore
