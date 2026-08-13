// BasicEObject 单元测试
// 对齐 org.eclipse.emf.ecore.impl.BasicEObjectImpl
// 覆盖：eDynamicGet/Set/IsSet/Unset（动态值存储）、eInverseELists 注册表、
//       eInverseAdd/eInverseRemove、eContainer、eClass
#include "test_main.h"
#include "emf/ecore/EcorePackage.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/ecore/impl/BasicEObject.h"
#include "emf/common/EInverseList.h"
#include "emf/common/Notification.h"
#include <any>
#include <string>
#include <vector>

using emf::common::EObject;
using emf::common::EInverseList;
using emf::ecore::EcoreFactory;
using emf::ecore::EcorePackage;
using emf::ecore::EClass;
using emf::ecore::EAttribute;
using emf::ecore::EStructuralFeature;
using emf::ecore::impl::BasicEObject;

namespace {

// 可设置 eClass 的 BasicEObject 测试子类（直接测试 eDynamic* 接口，不被 DynamicEObject 覆盖）
class TestBasicEObject : public BasicEObject {
public:
    void setEClass(EClass* cls) { eClass_ = cls; }
    EClass* eClass() const override { return eClass_; }
private:
    EClass* eClass_ = nullptr;
};

// 简单的反向列表桩：记录 basicAdd/basicRemove 调用
class InverseListStub : public EInverseList {
public:
    void basicAdd(EObject* otherEnd) override { added_.push_back(otherEnd); }
    void basicRemove(EObject* otherEnd) override {
        for (auto it = added_.begin(); it != added_.end(); ++it) {
            if (*it == otherEnd) { added_.erase(it); return; }
        }
    }
    const std::vector<EObject*>& items() const { return added_; }
    int count() const { return (int)added_.size(); }
    bool contains(EObject* o) const {
        for (auto* x : added_) if (x == o) return true;
        return false;
    }
private:
    std::vector<EObject*> added_;
};

// 构建带一个 name attribute 的 EClass
EClass* makeSimpleClass() {
    auto* cls = EcoreFactory::instance().createEClass();
    cls->setName("Simple");
    auto* name = EcoreFactory::instance().createEAttribute();
    name->setName("name");
    name->setEAttributeType(EcorePackage::instance().getEDataType_EString());
    name->setFeatureID(0);
    cls->addEStructuralFeature(name);
    return cls;
}

}  // namespace

// ===== eClass / eContainer =====

EMF_TEST(BasicEObject_EClass_ReturnsSetClass) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* cls = makeSimpleClass();
    TestBasicEObject obj;
    obj.setEClass(cls);
    EXPECT_EQ(obj.eClass(), cls);
}

EMF_TEST(BasicEObject_EClass_DefaultNull) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    TestBasicEObject obj;
    EXPECT_NULL(obj.eClass());
}

EMF_TEST(BasicEObject_EContainer_DefaultNull) {
    TestBasicEObject obj;
    EXPECT_NULL(obj.eContainer());
}

EMF_TEST(BasicEObject_EContainer_AfterSetEContainer) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    TestBasicEObject parent;
    TestBasicEObject child;
    child.setEContainer(&parent);
    EXPECT_EQ(child.eContainer(), &parent);
    EXPECT_EQ(child.eContainerConst(), &parent);
}

// ===== eDynamicGet / eDynamicSet =====

EMF_TEST(BasicEObject_EDynamicGet_Unset_ReturnsEmpty) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* cls = makeSimpleClass();
    auto* name = cls->getEStructuralFeature("name");
    TestBasicEObject obj;
    obj.setEClass(cls);
    // 未 set 过：返回空 any
    std::any v = obj.eDynamicGet(name);
    EXPECT_FALSE(v.has_value());
}

EMF_TEST(BasicEObject_EDynamicGet_NullFeature_ReturnsEmpty) {
    TestBasicEObject obj;
    std::any v = obj.eDynamicGet(nullptr);
    EXPECT_FALSE(v.has_value());
}

EMF_TEST(BasicEObject_EDynamicSet_ThenGet) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* cls = makeSimpleClass();
    auto* name = cls->getEStructuralFeature("name");
    TestBasicEObject obj;
    obj.setEClass(cls);
    obj.eDynamicSet(name, std::any{std::string{"Alice"}});
    std::any v = obj.eDynamicGet(name);
    EXPECT_TRUE(v.has_value());
    auto* s = std::any_cast<std::string>(&v);
    EXPECT_NOT_NULL(s);
    EXPECT_EQ(*s, std::string("Alice"));
}

EMF_TEST(BasicEObject_EDynamicSet_NullFeature_NoOp) {
    TestBasicEObject obj;
    // 不应崩溃
    obj.eDynamicSet(nullptr, std::any{std::string{"x"}});
}

EMF_TEST(BasicEObject_EDynamicSet_Overwrite) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* cls = makeSimpleClass();
    auto* name = cls->getEStructuralFeature("name");
    TestBasicEObject obj;
    obj.setEClass(cls);
    obj.eDynamicSet(name, std::any{std::string{"first"}});
    obj.eDynamicSet(name, std::any{std::string{"second"}});
    auto v = obj.eDynamicGet(name);
    auto* s = std::any_cast<std::string>(&v);
    EXPECT_NOT_NULL(s);
    EXPECT_EQ(*s, std::string("second"));
}

EMF_TEST(BasicEObject_EDynamicSet_DistinctFeatures) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* cls = EcoreFactory::instance().createEClass();
    cls->setName("Two");
    auto* a = EcoreFactory::instance().createEAttribute();
    a->setName("a");
    a->setEAttributeType(EcorePackage::instance().getEDataType_EString());
    a->setFeatureID(0);
    auto* b = EcoreFactory::instance().createEAttribute();
    b->setName("b");
    b->setEAttributeType(EcorePackage::instance().getEDataType_EInt());
    b->setFeatureID(1);
    cls->addEStructuralFeature(a);
    cls->addEStructuralFeature(b);

    TestBasicEObject obj;
    obj.setEClass(cls);
    obj.eDynamicSet(a, std::any{std::string{"hello"}});
    obj.eDynamicSet(b, std::any{42});

    auto va = obj.eDynamicGet(a);
    auto vb = obj.eDynamicGet(b);
    EXPECT_EQ(*std::any_cast<std::string>(&va), std::string("hello"));
    EXPECT_EQ(*std::any_cast<int>(&vb), 42);
}

// ===== eDynamicIsSet / eDynamicUnset =====

EMF_TEST(BasicEObject_EDynamicIsSet_DefaultFalse) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* cls = makeSimpleClass();
    auto* name = cls->getEStructuralFeature("name");
    TestBasicEObject obj;
    obj.setEClass(cls);
    EXPECT_FALSE(obj.eDynamicIsSet(name));
}

EMF_TEST(BasicEObject_EDynamicIsSet_AfterSetTrue) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* cls = makeSimpleClass();
    auto* name = cls->getEStructuralFeature("name");
    TestBasicEObject obj;
    obj.setEClass(cls);
    obj.eDynamicSet(name, std::any{std::string{"x"}});
    EXPECT_TRUE(obj.eDynamicIsSet(name));
}

EMF_TEST(BasicEObject_EDynamicIsSet_NullFeature_False) {
    TestBasicEObject obj;
    EXPECT_FALSE(obj.eDynamicIsSet(nullptr));
}

EMF_TEST(BasicEObject_EDynamicUnset_ClearsValue) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* cls = makeSimpleClass();
    auto* name = cls->getEStructuralFeature("name");
    TestBasicEObject obj;
    obj.setEClass(cls);
    obj.eDynamicSet(name, std::any{std::string{"x"}});
    EXPECT_TRUE(obj.eDynamicIsSet(name));
    obj.eDynamicUnset(name);
    EXPECT_FALSE(obj.eDynamicIsSet(name));
    EXPECT_FALSE(obj.eDynamicGet(name).has_value());
}

EMF_TEST(BasicEObject_EDynamicUnset_NullFeature_NoOp) {
    TestBasicEObject obj;
    obj.eDynamicUnset(nullptr);  // 不崩溃
}

EMF_TEST(BasicEObject_EDynamicUnset_NotPreviouslySet_NoOp) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* cls = makeSimpleClass();
    auto* name = cls->getEStructuralFeature("name");
    TestBasicEObject obj;
    obj.setEClass(cls);
    obj.eDynamicUnset(name);  // 未 set 过，不崩溃
    EXPECT_FALSE(obj.eDynamicIsSet(name));
}

// ===== eRegisterInverseList / eUnregisterInverseList =====

EMF_TEST(BasicEObject_ERegisterInverseList_EnablesInverseAdd) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    TestBasicEObject owner;
    InverseListStub list;
    owner.eRegisterInverseList(7, &list);

    TestBasicEObject other;
    EObject* otherPtr = &other;
    auto chain = owner.eInverseAdd(otherPtr, 7, nullptr, {});
    EXPECT_EQ(list.count(), 1);
    EXPECT_TRUE(list.contains(otherPtr));
}

EMF_TEST(BasicEObject_EInverseAdd_UnregisteredFeature_NoOp) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    TestBasicEObject owner;
    TestBasicEObject other;
    // 未注册任何 inverse list —— 不应崩溃，什么都不做
    auto chain = owner.eInverseAdd(&other, 999, nullptr, {});
    (void)chain;
}

EMF_TEST(BasicEObject_EInverseRemove_RemovesFromList) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    TestBasicEObject owner;
    InverseListStub list;
    owner.eRegisterInverseList(3, &list);

    TestBasicEObject a;
    TestBasicEObject b;
    owner.eInverseAdd(&a, 3, nullptr, {});
    owner.eInverseAdd(&b, 3, nullptr, {});
    EXPECT_EQ(list.count(), 2);

    owner.eInverseRemove(&a, 3, nullptr, {});
    EXPECT_EQ(list.count(), 1);
    EXPECT_FALSE(list.contains(&a));
    EXPECT_TRUE(list.contains(&b));
}

EMF_TEST(BasicEObject_EInverseRemove_UnregisteredFeature_NoOp) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    TestBasicEObject owner;
    TestBasicEObject other;
    // 未注册 —— 不崩溃
    owner.eInverseRemove(&other, 500, nullptr, {});
}

EMF_TEST(BasicEObject_EUnregisterInverseList_DisablesInverseAdd) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    TestBasicEObject owner;
    InverseListStub list;
    owner.eRegisterInverseList(1, &list);
    owner.eUnregisterInverseList(1, &list);

    TestBasicEObject other;
    owner.eInverseAdd(&other, 1, nullptr, {});
    EXPECT_EQ(list.count(), 0);
}

EMF_TEST(BasicEObject_EUnregisterInverseList_WrongList_NoOp) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    TestBasicEObject owner;
    InverseListStub list1;
    InverseListStub list2;
    owner.eRegisterInverseList(2, &list1);
    // 用不匹配的 list 反注册 —— 不应移除已注册的 list1
    owner.eUnregisterInverseList(2, &list2);

    TestBasicEObject other;
    owner.eInverseAdd(&other, 2, nullptr, {});
    EXPECT_EQ(list1.count(), 1);
}

EMF_TEST(BasicEObject_ERegisterInverseList_NullList_NoOp) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    TestBasicEObject owner;
    owner.eRegisterInverseList(0, nullptr);  // 不崩溃
    TestBasicEObject other;
    owner.eInverseAdd(&other, 0, nullptr, {});
}

// ===== eInverseAdd 返回 notifications 透传 =====

EMF_TEST(BasicEObject_EInverseAdd_PassesThroughNotifications) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    TestBasicEObject owner;
    InverseListStub list;
    owner.eRegisterInverseList(4, &list);

    emf::common::Notification n(emf::common::Notification::EventType::ADD,
                                &owner, nullptr, 4, std::any{}, std::any{});
    std::vector<emf::common::Notification> chain;
    chain.push_back(n);

    TestBasicEObject other;
    auto result = owner.eInverseAdd(&other, 4, nullptr, chain);
    EXPECT_EQ(result.size(), (size_t)1);  // 透传原 chain
}

EMF_TEST(BasicEObject_EInverseRemove_PassesThroughNotifications) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    TestBasicEObject owner;
    InverseListStub list;
    owner.eRegisterInverseList(4, &list);

    std::vector<emf::common::Notification> chain;
    TestBasicEObject other;
    auto result = owner.eInverseRemove(&other, 4, nullptr, chain);
    EXPECT_EQ(result.size(), (size_t)0);  // 空 chain 透传
}

// ===== eNotificationRequired =====

EMF_TEST(BasicEObject_ENotificationRequired_NoAdapters_False) {
    TestBasicEObject obj;
    EXPECT_FALSE(obj.eNotificationRequired());
}

// ===== eSet/eUnset(EStructuralFeature*) 通知验证（P0-4）=====
// 对齐 Java BasicEObjectImpl：eSet/eUnset(feature*) 委托 eDynamic* 并发 SET/UNSET 通知。
// 验证 BasicEObject 直接子类（不经 DynamicEObject 覆盖）能正确投递通知且值可经 eGet 读回。

namespace {
class RecordingAdapter2 : public emf::common::EAdapter {
public:
    void notifyChanged(const emf::common::Notification& n) override {
        received_.push_back(n);
    }
    const std::vector<emf::common::Notification>& received() const { return received_; }
private:
    std::vector<emf::common::Notification> received_;
};
}  // namespace

EMF_TEST(BasicEObject_ESet_Feature_FiresSetAndReadsBack) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* cls = makeSimpleClass();
    auto* name = cls->getEStructuralFeature("name");
    TestBasicEObject obj;
    obj.setEClass(cls);
    obj.eSet(name, std::any{std::string{"first"}});  // 先设值（无 adapter，不发通知但存储）

    RecordingAdapter2 adapter;
    obj.eAdapters().push_back(&adapter);

    obj.eSet(name, std::any{std::string{"second"}});
    EXPECT_EQ(adapter.received().size(), (size_t)1);
    const auto& n = adapter.received()[0];
    EXPECT_EQ((int)n.eventType(), (int)emf::common::Notification::EventType::SET);
    EXPECT_EQ(n.feature(), name);
    EXPECT_EQ(*std::any_cast<std::string>(&n.oldValue()), std::string("first"));
    EXPECT_EQ(*std::any_cast<std::string>(&n.newValue()), std::string("second"));
    // 值可经 eGet(feature*) 读回（修复前 EObjectImpl 空实现返回空）
    auto v = obj.eGet(name);
    EXPECT_EQ(*std::any_cast<std::string>(&v), std::string("second"));
}

EMF_TEST(BasicEObject_EUnset_Feature_FiresUnsetNotification) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* cls = makeSimpleClass();
    auto* name = cls->getEStructuralFeature("name");
    TestBasicEObject obj;
    obj.setEClass(cls);
    obj.eSet(name, std::any{std::string{"x"}});

    RecordingAdapter2 adapter;
    obj.eAdapters().push_back(&adapter);

    obj.eUnset(name);
    EXPECT_EQ(adapter.received().size(), (size_t)1);
    EXPECT_EQ((int)adapter.received()[0].eventType(), (int)emf::common::Notification::EventType::UNSET);
    EXPECT_FALSE(obj.eIsSet(name));
    EXPECT_FALSE(obj.eGet(name).has_value());
}
