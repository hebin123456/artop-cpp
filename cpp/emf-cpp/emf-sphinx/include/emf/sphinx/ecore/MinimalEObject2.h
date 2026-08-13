// MinimalEObject2.h
// 对齐 Java org.eclipse.sphinx.emf.ecore.MinimalEObjectImpl2
//
// 空间紧凑的 EObject 实现：使用 bit-flag 跟踪惰性分配的字段，
// 提供 eDeliver/eSetDeliver 通知开关与可设置的 eClass。
// 复用 BasicEObject 的动态值存储与反向引用维护。
#pragma once

#include "emf/ecore/impl/BasicEObject.h"

namespace emf::ecore {
class EClass;
}  // namespace emf::ecore

namespace emf::sphinx::ecore {

class MinimalEObject2 : public emf::ecore::impl::BasicEObject {
public:
    MinimalEObject2() = default;
    ~MinimalEObject2() override;

    emf::ecore::EClass* eClass() const override;
    void eSetClass(emf::ecore::EClass* cls);

    // 通知投递开关（对齐 Java MinimalEObjectImpl2.eDeliver/eSetDeliver）
    bool eDeliver() const { return (eFlags_ & kNoDeliver) == 0; }
    void eSetDeliver(bool deliver);

    // bit-flag 字段常量（对齐 Java MinimalEObjectImpl2 的字段掩码）
    static constexpr int kNoDeliver = 1 << 0;
    static constexpr int kContainer = 1 << 1;
    static constexpr int kAdapter = 1 << 2;
    static constexpr int kClass = 1 << 4;
    static constexpr int kSetting = 1 << 5;
    static constexpr int kProxy = 1 << 6;
    static constexpr int kResource = 1 << 7;
    static constexpr int kFieldMask =
        kContainer | kAdapter | kClass | kSetting | kProxy | kResource;

    int eFlags() const { return eFlags_; }
    bool hasField(int field) const { return (eFlags_ & field) != 0; }

private:
    int eFlags_ = 0;
    emf::ecore::EClass* eClass_ = nullptr;
};

}  // namespace emf::sphinx::ecore
