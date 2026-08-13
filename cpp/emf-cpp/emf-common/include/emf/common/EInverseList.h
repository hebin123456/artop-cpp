// EMF Common: EInverseList
// 对齐 org.eclipse.emf.ecore.util.EObjectWithInverseEList 的反向引用接口
//
// 这是个纯接口：提供 basicAdd/basicRemove，供 BasicEObject::eInverseAdd
// 在命中注册表时直接调用（跳过 didAdd 避免递归）。
//
// 注意：Java 的 EObjectWithInverseEList 继承 EObjectEList 并实现 InternalEList；
// C++ 端 EInverseList 不继承 EList，避免与 EObjectEList→...→EList 形成菱形继承。
// EObjectWithInverseEList 多继承 EObjectEList<E> + EInverseList（接口），无菱形。
#pragma once

#include "EObject.h"

namespace emf::common {

class EInverseList {
public:
    virtual ~EInverseList() = default;

    // basicAdd/basicRemove：由对端 eInverseAdd/eInverseRemove 命中本实例时直接调用。
    // 直接写入底层存储，不发通知、不做 unique 检查（避免 inverseAdd 递归循环）。
    // 对齐 Java EObjectWithInverseEList 内部 basicAdd/basicRemove 语义。
    virtual void basicAdd(EObject* otherEnd) = 0;
    virtual void basicRemove(EObject* otherEnd) = 0;
};

}  // namespace emf::common
