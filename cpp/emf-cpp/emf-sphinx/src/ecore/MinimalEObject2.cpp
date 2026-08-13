// MinimalEObject2.cpp
// 对齐 Java org.eclipse.sphinx.emf.ecore.MinimalEObjectImpl2
#include "emf/sphinx/ecore/MinimalEObject2.h"

namespace emf::sphinx::ecore {

MinimalEObject2::~MinimalEObject2() = default;

emf::ecore::EClass* MinimalEObject2::eClass() const {
    // 对齐 Java：动态 eClass 未设置时返回 nullptr（静态类）
    return eClass_;
}

void MinimalEObject2::eSetClass(emf::ecore::EClass* cls) {
    eClass_ = cls;
    if (cls != nullptr) {
        eFlags_ |= kClass;
    } else {
        eFlags_ &= ~kClass;
    }
}

void MinimalEObject2::eSetDeliver(bool deliver) {
    if (deliver) {
        eFlags_ &= ~kNoDeliver;
    } else {
        eFlags_ |= kNoDeliver;
    }
}

}  // namespace emf::sphinx::ecore
