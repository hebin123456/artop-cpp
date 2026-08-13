// MessageEObject.cpp
// 对齐 Java org.eclipse.sphinx.emf.ecore.MessageEObjectImpl
#include "emf/sphinx/ecore/MessageEObject.h"

namespace emf::sphinx::ecore {

MessageEObject::MessageEObject(std::string message) : message_(std::move(message)) {}

MessageEObject::~MessageEObject() = default;

}  // namespace emf::sphinx::ecore
