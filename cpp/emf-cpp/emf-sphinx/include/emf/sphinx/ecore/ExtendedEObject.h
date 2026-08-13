// ExtendedEObject.h
// 对齐 Java org.eclipse.sphinx.emf.ecore.ExtendedEObjectImpl
//
// EObjectImpl 的扩展子类：
//   - override eResolveProxy，委托 ProxyResolutionBehavior 做集中式代理解析
//     （对齐 Java ExtendedEObjectImpl.eResolveProxy）
//   - 提供 getExtendedMetaDataAnnotation() 访问 eClass() 上的 extendedMetaData 注解
//   - 提供 eNotify() 通知分发覆盖点（默认 no-op，由子类/容器覆盖）
//
// 与 Java 一致，本类保持抽象：eClass() 仍由具体子类提供。
#pragma once

#include "emf/common/EObject.h"
#include "emf/common/Notification.h"

namespace emf::ecore {
class EAnnotation;
}  // namespace emf::ecore

namespace emf::sphinx::ecore {

class ExtendedEObject : public emf::common::EObjectImpl {
public:
    ExtendedEObject() = default;
    ~ExtendedEObject() override;

    // 代理解析钩子：委托 ProxyResolutionBehavior（对齐 Java eResolveProxy）
    emf::common::EObject* eResolveProxy(emf::common::EObject* proxy) const override;

    // 扩展元数据访问：返回 eClass() 上 source=="extendedMetaData" 的 EAnnotation
    emf::ecore::EAnnotation* getExtendedMetaDataAnnotation() const;

    // 通知分发辅助：当 eNotificationRequired() 时投递通知。
    // C++ 端 EObjectImpl 未内置 adapter 列表，此处作为覆盖点，默认 no-op。
    virtual void eNotify(const emf::common::Notification& notification);
};

}  // namespace emf::sphinx::ecore
