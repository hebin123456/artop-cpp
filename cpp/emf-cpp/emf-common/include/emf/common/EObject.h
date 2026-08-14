// EMF Common: EObject / EObjectImpl
// 对齐 org.eclipse.emf.ecore.EObject, org.eclipse.emf.ecore.impl.BasicEObjectImpl
//   - EObject: 模型对象抽象接口（继承 Notifier）
//   - EObjectImpl: 基础实现（含 eContainer / eProxyURI / eResource 关联）
// ARTOP 扩展：eXmlName / eFeatureXmlName / eFeatureAprxmlRule / eFeatureSequenceOffset
//   等方法用于 arxml 序列化/反序列化时按元数据驱动 XML 命名（对齐 Java ARTOP 的
//   eFeatureXmlName / eFeatureAprxmlRule 生成方法，C++ 端从 EAnnotation 读取）。
#pragma once

#include "ENotifier.h"
#include "Notification.h"
#include "URI.h"
#include <any>
#include <cstddef>
#include <cstdint>
#include <iterator>
#include <memory>
#include <string>
#include <vector>

// emf::ecore 前向声明（避免与 emf-ecore 的 EcoreImpls.h 循环依赖）
namespace emf::ecore {
class EClass;
class EStructuralFeature;
class EReference;
class EOperation;
class EFactory;
class EPackage;
}  // namespace emf::ecore

namespace emf::common {

class Resource;

// TreeIterator：懒迭代器（对齐 org.eclipse.emf.common.util.TreeIterator）
template <typename T>
class TreeIterator {
public:
    virtual ~TreeIterator() = default;
    virtual bool hasNext() = 0;
    virtual T next() = 0;
};

// EObjectRefView 定义在 EObjectImpl 之后（其访问器需要 EObjectImpl 完整类型，
// 以完成 EObjectImpl* → EObject* 的虚基类偏移调整）。

// EObject：模型对象接口
// 对齐 org.eclipse.emf.ecore.EObject（继承 Notifier）
// ARTOP 扩展方法以 eXml/eFeature 前缀标识，用于 arxml 元数据驱动序列化。
class EObject : public Notifier {
public:
    EObject() = default;
    virtual ~EObject();

    // ===== 标准 EMF 方法（对齐 Java EObject 接口）=====
    virtual emf::ecore::EClass* eClass() const = 0;
    virtual Resource* eResource() const = 0;
    // eSetResource：设置对象直接所属的 Resource（对齐 Java BasicEObjectImpl.eDirectResource）
    // 由 Resource.addToContents 调用，建立 EObject→Resource 反向引用
    virtual void eSetResource(Resource* /*resource*/) {}

    virtual EObject* eContainer() const = 0;
    virtual const emf::ecore::EStructuralFeature* eContainingFeature() const = 0;
    virtual const emf::ecore::EStructuralFeature* eContainmentFeature() const = 0;
    // setEContainer / setEContainingFeature：由 loader / attachTo_parent / 生成的 containment
    // setter 调用。默认空实现，EObjectImpl override 实际写入。
    // 对齐 Java InternalEObject.eSetContainer / eBasicSetContainer。
    // 提到 EObject 接口：当 containment 引用目标为 emf::common::EObject（原始 EObject）时，
    // 生成的 setter 仍可调用 setEContainer（codegen 不需要 cast 到 EObjectImpl）。
    virtual void setEContainer(EObject* /*container*/) {}
    virtual void setEContainingFeature(const emf::ecore::EStructuralFeature* /*feature*/) {}
    // 注意：eContents/eCrossReferences 返回值（对齐生成代码签名，生成类 override 返回值）
    virtual std::vector<EObject*> eContents() const = 0;
    virtual TreeIterator<EObject*>* eAllContents() const = 0;
    virtual bool eIsProxy() const = 0;
    virtual std::vector<EObject*> eCrossReferences() const = 0;

    // eGet/eSet/eIsSet/eUnset：按 EStructuralFeature 反射访问
    // 注意：eSet 按 std::any 值传递（对齐生成代码签名）
    virtual std::any eGet(const emf::ecore::EStructuralFeature* feature) const = 0;
    virtual std::any eGet(const emf::ecore::EStructuralFeature* feature, bool resolve) const = 0;
    virtual void eSet(const emf::ecore::EStructuralFeature* feature, std::any value) = 0;
    virtual bool eIsSet(const emf::ecore::EStructuralFeature* feature) const = 0;
    virtual void eUnset(const emf::ecore::EStructuralFeature* feature) = 0;

    // 按 featureID（整数索引）反射访问 —— 生成的 Impl 用 switch(featureID) 实现
    // 注意：eSet 按 std::any 值传递（对齐生成代码签名）
    virtual std::any eGet(int featureID) const;
    virtual void eSet(int featureID, std::any value);
    virtual bool eIsSet(int featureID) const;
    virtual void eUnset(int featureID);

    // 类型化 eGet（方案 B 子集）：避免 std::any 装箱/拆箱开销。
    // 返回 true 表示命中（单值字段，out 已赋值）；false 表示未命中（多值/不匹配），
    // 调用方应 fallback 到 eGet(featureID)+std::any。
    // 生成的 Impl 用 switch(featureID) override，仅对单值 attribute/reference 返回 true。
    virtual bool eGetString(int featureID, std::string& out) const;
    virtual bool eGetInt64(int featureID, int64_t& out) const;
    virtual bool eGetBool(int featureID, bool& out) const;
    virtual bool eGetEObject(int featureID, EObject*& out) const;
    virtual bool eGetDouble(int featureID, double& out) const;

    // eInvoke：按 EOperation 反射调用
    virtual std::any eInvoke(emf::ecore::EOperation* operation, const std::vector<std::any>& args);
    virtual int eDerivedOperationID(emf::ecore::EOperation* operation) const;

    // eContainerConst：const 版本容器访问（内部用）
    virtual const EObject* eContainerConst() const;

    // ===== ARTOP 扩展方法（arxml 元数据驱动序列化）=====
    // 对齐 Java ARTOP 生成的 eXmlName / eFeatureXmlName / eFeatureAprxmlRule 等。
    // C++ 端默认实现返回空/默认值，由生成的 Impl override 或由 EAnnotation 元数据提供。

    // 类的 XML 元素名（如 "AUTOSAR"、"AR-PACKAGE"）
    // 注意：返回 const std::string&（对齐生成代码签名，生成类 override 返回静态变量引用）
    virtual const std::string& eXmlName() const;
    // 类的 XML 元素名（复数形式，如 "AR-PACKAGES"）
    virtual const std::string& eXmlNamePlural() const;
    // 类的 XML 命名空间前缀（如 "autosar"）
    virtual const std::string& eNsPrefix() const;
    // 内容类型标识（对齐 Java getContentKind）
    virtual const std::string& eContentKind() const;

    // 按 featureID 获取 XML 元素名
    virtual const std::string& eFeatureXmlName(int featureID) const;
    // 按 featureID 获取 APRXML 规则编号（0012-0016）
    virtual int eFeatureAprxmlRule(int featureID) const;
    // 按 featureID 获取序列化顺序偏移
    virtual int eFeatureSequenceOffset(int featureID) const;
    // 按 featureID 判断 feature 是否为 XML 属性（而非子元素）
    virtual bool eFeatureIsXmlAttribute(int featureID) const;
    // 按 featureID 判断是否为 role element / role wrapper / type element / type wrapper
    virtual bool eFeatureIsRoleElement(int featureID) const;
    virtual bool eFeatureIsRoleWrapperElement(int featureID) const;
    virtual bool eFeatureIsTypeElement(int featureID) const;
    virtual bool eFeatureIsTypeWrapperElement(int featureID) const;

    // 类的 feature 总数
    virtual int eFeatureCount() const;

    // 按 feature 名获取 featureID（对齐生成代码的 eFeatureID override）
    // 返回 -1 表示未找到
    virtual int eFeatureID(const std::string& /*name*/) const { return -1; }

    // eResolveProxy：解析代理对象（对齐 Java EObject.eResolveProxy）
    virtual EObject* eResolveProxy(EObject* proxy) const;

    // ===== 反向引用维护（对齐 Java InternalEObject.eInverseAdd/eInverseRemove）=====
    // 由 EcoreEList::inverseAdd/inverseRemove 在对端调用，维护双向 EReference。
    // 默认实现返回 notifications 不做处理；BasicEObject override 查 eInverseELists_
    // 注册表并调用 EInverseList::basicAdd/basicRemove。
    // NotificationChain 用 std::vector<Notification> 表示（对齐 Java NotificationChain）。
    using EObjectNotificationChain = std::vector<Notification>;
    virtual EObjectNotificationChain eInverseAdd(EObject* otherEnd, int featureID,
                                                  emf::ecore::EClass* inverseFeatureClass,
                                                  EObjectNotificationChain notifications);
    virtual EObjectNotificationChain eInverseRemove(EObject* otherEnd, int featureID,
                                                     emf::ecore::EClass* inverseFeatureClass,
                                                     EObjectNotificationChain notifications);

    // eNotificationRequired：是否需要通知（对齐 Java eNotificationRequired）
    virtual bool eNotificationRequired() const;

    // eGetTextContent：获取文本内容（mixed content / 简单类型）
    virtual std::string eGetTextContent() const;
};

// EObjectImpl：EObject 基础实现
// 对齐 org.eclipse.emf.ecore.impl.BasicEObjectImpl
// 提供通用的 eContainer / eProxyURI / eResource 管理，
// 生成的模型类继承 EObjectImpl 并 override eGet/eSet/eIsSet/eUnset（按 featureID switch）。
class EObjectImpl : public virtual EObject {
public:
    EObjectImpl() = default;
    virtual ~EObjectImpl();

    // eClass：由子类 override 返回静态 EClass*
    emf::ecore::EClass* eClass() const override = 0;

    // eContainer / eContainingFeature 管理
    EObject* eContainer() const override { return eContainer_; }
    const EObject* eContainerConst() const override { return eContainer_; }
    const emf::ecore::EStructuralFeature* eContainingFeature() const override { return eContainingFeature_; }
    const emf::ecore::EStructuralFeature* eContainmentFeature() const override { return eContainingFeature_; }

    // setEContainer / setEContainingFeature：由 loader / attachToParent / 生成 setter 调用
    // override EObject 接口的默认空实现
    void setEContainer(EObject* container) override;
    void setEContainingFeature(const emf::ecore::EStructuralFeature* feature) override;

    // eContents / eCrossReferences：默认实现返回空列表
    std::vector<EObject*> eContents() const override;
    std::vector<EObject*> eCrossReferences() const override;
    TreeIterator<EObject*>* eAllContents() const override;

    // eResource：优先返回 eDirectResource，否则沿 eContainer 链向上找
    Resource* eResource() const override;
    // eSetResource：设置直接所属 Resource（对齐 Java eDirectResource）
    void eSetResource(Resource* resource) override { eDirectResource_ = resource; }

    // 代理对象支持
    bool eIsProxy() const override;
    const URI& eProxyURI() const { return eProxyURI_; }
    void eSetProxyURI(const URI& uri) { eProxyURI_ = std::move(uri); }

    // eResolveProxy：默认实现返回自身（非代理）
    EObject* eResolveProxy(EObject* proxy) const override;

    // eGetTextContent：默认返回空
    std::string eGetTextContent() const override;

    // eInverseAdd/eInverseRemove 默认实现：不做反向维护，直接返回 notifications。
    // BasicEObject override 查 eInverseELists_ 注册表。
    EObjectNotificationChain eInverseAdd(EObject* otherEnd, int featureID,
                                          emf::ecore::EClass* inverseFeatureClass,
                                          EObjectNotificationChain notifications) override;
    EObjectNotificationChain eInverseRemove(EObject* otherEnd, int featureID,
                                             emf::ecore::EClass* inverseFeatureClass,
                                             EObjectNotificationChain notifications) override;

    // eGet/eSet/eIsSet/eUnset 默认实现：抛异常（子类 override 处理 featureID）
    // EModelElementImpl 等子类 override 这些方法分发到父类
    // using 声明引入 EObject 的 int 重载，避免被 EStructuralFeature* 重载隐藏
    using EObject::eGet;
    using EObject::eSet;
    using EObject::eIsSet;
    using EObject::eUnset;
    std::any eGet(const emf::ecore::EStructuralFeature* feature) const override;
    std::any eGet(const emf::ecore::EStructuralFeature* feature, bool resolve) const override;
    void eSet(const emf::ecore::EStructuralFeature* feature, std::any value) override;
    bool eIsSet(const emf::ecore::EStructuralFeature* feature) const override;
    void eUnset(const emf::ecore::EStructuralFeature* feature) override;

protected:
    EObject* eContainer_ = nullptr;
    const emf::ecore::EStructuralFeature* eContainingFeature_ = nullptr;
    URI eProxyURI_;
    Resource* eDirectResource_ = nullptr;  // 对齐 Java BasicEObjectImpl.eDirectResource

    // eContents 的默认空列表（避免每次返回临时对象）
    static const std::vector<EObject*> kEmptyContents_;
};

// EObjectRefView：多值 reference feature 的零拷贝视图。
// 指向 EList<T*> 内部 vector 的连续指针数组。
//
// 安全性（关键）：
//   生成模型类 T 单一非虚继承自 EObjectImpl（offset 0），故 T* 与 EObjectImpl*
//   比特一致，reinterpret_cast<T* const*> → const EObjectImpl* const* 安全。
//   但 EObjectImpl 虚继承 EObject（class EObjectImpl : public virtual EObject），
//   EObjectImpl* → EObject* 需虚基类偏移调整，reinterpret_cast 不安全（指向错误
//   子对象，vptr 错乱，曾导致 saveObjectContent 的 eIsSet 虚调用 segfault）。
//   故本视图存储 const EObjectImpl* const*，访问时通过 static_cast<const EObject*>
//   完成虚基类偏移调整（一次 vtable 偏移加载，远廉价于 vector 堆分配）。
//   对齐 EClassEmitter.cpp 中 eSet 路径已有的 dynamic_cast<EObjectImpl*> 修复。
// 用于 eGet fast-path，替代 vector<EObject*> 拷贝，消除 save 热路径的堆分配。
struct EObjectRefView {
    const EObjectImpl* const* data;  // EList<T*> 内部数组（T* 比特 == EObjectImpl* 比特）
    size_t count;
    EObjectRefView() : data(nullptr), count(0) {}
    EObjectRefView(const EObjectImpl* const* d, size_t n) : data(d), count(n) {}
    bool empty() const { return count == 0; }
    size_t size() const { return count; }
    EObject* operator[](size_t i) const {
        return const_cast<EObject*>(static_cast<const EObject*>(data[i]));
    }

    // 代理迭代器：解引用时完成 EObjectImpl* → EObject* 虚基类偏移调整。
    // value_type=EObject*，使 vector::assign(view.begin(),view.end()) 与
    // range-for (auto* child : view) 均得到正确的 EObject*。
    struct const_iterator {
        const EObjectImpl* const* p = nullptr;
        const_iterator() = default;
        explicit const_iterator(const EObjectImpl* const* p_) : p(p_) {}
        EObject* operator*() const {
            return const_cast<EObject*>(static_cast<const EObject*>(*p));
        }
        const_iterator& operator++() { ++p; return *this; }
        const_iterator operator++(int) { auto t = *this; ++p; return t; }
        bool operator!=(const const_iterator& o) const { return p != o.p; }
        bool operator==(const const_iterator& o) const { return p == o.p; }
        using iterator_category = std::input_iterator_tag;
        using value_type = EObject*;
        using difference_type = std::ptrdiff_t;
        using pointer = void;
        using reference = EObject*;
    };
    const_iterator begin() const { return const_iterator(data); }
    const_iterator end() const { return const_iterator(data + count); }
};

}  // namespace emf::common
