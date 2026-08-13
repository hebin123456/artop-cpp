// DynamicEObject 单元测试
// 对齐 org.eclipse.emf.ecore.impl.DynamicEObjectImpl
// 覆盖：eClass、eSet/eGet（单值属性、单值 containment 引用、多值引用）、
//       eSet/eGet by featureID、eIsSet/eUnset、eContents 收集、
//       containment 自动设置 eContainer、null feature 处理
#include "test_main.h"
#include "emf/ecore/EcorePackage.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/ecore/DynamicEObject.h"
#include "emf/common/EObject.h"
#include <any>
#include <string>
#include <vector>

using emf::common::EObject;
using emf::ecore::EcoreFactory;
using emf::ecore::EcorePackage;
using emf::ecore::EClass;
using emf::ecore::EAttribute;
using emf::ecore::EReference;
using emf::ecore::EStructuralFeature;
using emf::ecore::DynamicEObject;

namespace {

// 模型：Node EClass
//   - name : EString (single, featureID 0)
//   - value : EInt (single, featureID 1)
//   - child : Node (containment, single, featureID 2)
//   - children : Node (containment, many, featureID 3)
struct NodeModel {
    EClass* nodeCls;
    EAttribute* name;
    EAttribute* value;
    EReference* child;
    EReference* children;
};

NodeModel makeNodeModel() {
    NodeModel m;
    m.nodeCls = EcoreFactory::instance().createEClass();
    m.nodeCls->setName("Node");

    m.name = EcoreFactory::instance().createEAttribute();
    m.name->setName("name");
    m.name->setEAttributeType(EcorePackage::instance().getEDataType_EString());
    m.name->setFeatureID(0);
    m.name->setLowerBound(0);
    m.name->setUpperBound(1);
    m.nodeCls->addEStructuralFeature(m.name);

    m.value = EcoreFactory::instance().createEAttribute();
    m.value->setName("value");
    m.value->setEAttributeType(EcorePackage::instance().getEDataType_EInt());
    m.value->setFeatureID(1);
    m.value->setLowerBound(0);
    m.value->setUpperBound(1);
    m.nodeCls->addEStructuralFeature(m.value);

    m.child = EcoreFactory::instance().createEReference();
    m.child->setName("child");
    m.child->setContainment(true);
    m.child->setEReferenceType(m.nodeCls);
    m.child->setFeatureID(2);
    m.child->setLowerBound(0);
    m.child->setUpperBound(1);
    m.nodeCls->addEStructuralFeature(m.child);

    m.children = EcoreFactory::instance().createEReference();
    m.children->setName("children");
    m.children->setContainment(true);
    m.children->setEReferenceType(m.nodeCls);
    m.children->setFeatureID(3);
    m.children->setLowerBound(0);
    m.children->setUpperBound(-1);  // many
    m.nodeCls->addEStructuralFeature(m.children);

    return m;
}

}  // namespace

// ===== eClass =====

EMF_TEST(DynamicEObject_EClass_ReturnsConstructorClass) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto m = makeNodeModel();
    auto* obj = new DynamicEObject(m.nodeCls);
    EXPECT_EQ(obj->eClass(), m.nodeCls);
    delete obj;
}

EMF_TEST(DynamicEObject_EContents_DefaultEmpty) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto m = makeNodeModel();
    auto* obj = new DynamicEObject(m.nodeCls);
    EXPECT_EQ(obj->eContents().size(), (size_t)0);
    delete obj;
}

// ===== eSet / eGet by EStructuralFeature（单值属性）=====

EMF_TEST(DynamicEObject_ESetEGet_StringAttribute) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto m = makeNodeModel();
    auto* obj = new DynamicEObject(m.nodeCls);
    obj->eSet(m.name, std::any{std::string{"root"}});
    auto v = obj->eGet(m.name);
    auto* s = std::any_cast<std::string>(&v);
    EXPECT_NOT_NULL(s);
    EXPECT_EQ(*s, std::string("root"));
    delete obj;
}

EMF_TEST(DynamicEObject_ESetEGet_IntAttribute) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto m = makeNodeModel();
    auto* obj = new DynamicEObject(m.nodeCls);
    obj->eSet(m.value, std::any{42});
    auto v = obj->eGet(m.value);
    auto* i = std::any_cast<int>(&v);
    EXPECT_NOT_NULL(i);
    EXPECT_EQ(*i, 42);
    delete obj;
}

EMF_TEST(DynamicEObject_EGet_UnsetAttribute_Empty) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto m = makeNodeModel();
    auto* obj = new DynamicEObject(m.nodeCls);
    auto v = obj->eGet(m.name);
    EXPECT_FALSE(v.has_value());
    delete obj;
}

EMF_TEST(DynamicEObject_EGet_NullFeature_Empty) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto m = makeNodeModel();
    auto* obj = new DynamicEObject(m.nodeCls);
    auto v = obj->eGet(nullptr);
    EXPECT_FALSE(v.has_value());
    delete obj;
}

EMF_TEST(DynamicEObject_EGet_WithResolveFlag) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto m = makeNodeModel();
    auto* obj = new DynamicEObject(m.nodeCls);
    obj->eSet(m.name, std::any{std::string{"x"}});
    // 带 resolve 参数的 eGet 应委托到无参版
    auto v = obj->eGet(m.name, false);
    auto* s = std::any_cast<std::string>(&v);
    EXPECT_NOT_NULL(s);
    EXPECT_EQ(*s, std::string("x"));
    delete obj;
}

// ===== eSet / eGet by featureID =====

EMF_TEST(DynamicEObject_ESetEGet_ByFeatureID) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto m = makeNodeModel();
    auto* obj = new DynamicEObject(m.nodeCls);
    obj->eSet(0, std::any{std::string{"byID"}});
    auto v = obj->eGet(0);
    auto* s = std::any_cast<std::string>(&v);
    EXPECT_NOT_NULL(s);
    EXPECT_EQ(*s, std::string("byID"));
    delete obj;
}

EMF_TEST(DynamicEObject_ESetEGet_ByFeatureID_Int) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto m = makeNodeModel();
    auto* obj = new DynamicEObject(m.nodeCls);
    obj->eSet(1, std::any{99});
    auto v = obj->eGet(1);
    EXPECT_EQ(*std::any_cast<int>(&v), 99);
    delete obj;
}

EMF_TEST(DynamicEObject_ESet_NullFeature_NoOp) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto m = makeNodeModel();
    auto* obj = new DynamicEObject(m.nodeCls);
    obj->eSet(nullptr, std::any{std::string{"x"}});  // 不崩溃
    delete obj;
}

// ===== eIsSet / eUnset =====

EMF_TEST(DynamicEObject_EIsSet_DefaultFalse) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto m = makeNodeModel();
    auto* obj = new DynamicEObject(m.nodeCls);
    EXPECT_FALSE(obj->eIsSet(m.name));
    EXPECT_FALSE(obj->eIsSet(m.value));
    delete obj;
}

EMF_TEST(DynamicEObject_EIsSet_AfterSetTrue) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto m = makeNodeModel();
    auto* obj = new DynamicEObject(m.nodeCls);
    obj->eSet(m.name, std::any{std::string{"x"}});
    EXPECT_TRUE(obj->eIsSet(m.name));
    delete obj;
}

EMF_TEST(DynamicEObject_EIsSet_NullFeature_False) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto m = makeNodeModel();
    auto* obj = new DynamicEObject(m.nodeCls);
    EXPECT_FALSE(obj->eIsSet(nullptr));
    delete obj;
}

EMF_TEST(DynamicEObject_EIsSet_ByFeatureID) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto m = makeNodeModel();
    auto* obj = new DynamicEObject(m.nodeCls);
    EXPECT_FALSE(obj->eIsSet(0));
    obj->eSet(0, std::any{std::string{"y"}});
    EXPECT_TRUE(obj->eIsSet(0));
    delete obj;
}

EMF_TEST(DynamicEObject_EUnset_ClearsValue) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto m = makeNodeModel();
    auto* obj = new DynamicEObject(m.nodeCls);
    obj->eSet(m.name, std::any{std::string{"x"}});
    EXPECT_TRUE(obj->eIsSet(m.name));
    obj->eUnset(m.name);
    EXPECT_FALSE(obj->eIsSet(m.name));
    EXPECT_FALSE(obj->eGet(m.name).has_value());
    delete obj;
}

EMF_TEST(DynamicEObject_EUnset_ByFeatureID) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto m = makeNodeModel();
    auto* obj = new DynamicEObject(m.nodeCls);
    obj->eSet(1, std::any{7});
    EXPECT_TRUE(obj->eIsSet(1));
    obj->eUnset(1);
    EXPECT_FALSE(obj->eIsSet(1));
    delete obj;
}

EMF_TEST(DynamicEObject_EUnset_NullFeature_NoOp) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto m = makeNodeModel();
    auto* obj = new DynamicEObject(m.nodeCls);
    obj->eUnset(nullptr);  // 不崩溃
    delete obj;
}

// ===== containment 单值引用：eSet 自动设置子对象 eContainer =====

EMF_TEST(DynamicEObject_Containment_Single_SetsEContainer) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto m = makeNodeModel();
    auto* parent = new DynamicEObject(m.nodeCls);
    auto* child = new DynamicEObject(m.nodeCls);
    parent->eSet(m.child, std::any{(EObject*)child});
    EXPECT_EQ(child->eContainer(), parent);
    EXPECT_EQ(child->eContainmentFeature(), m.child);
    delete parent;
    delete child;
}

EMF_TEST(DynamicEObject_Containment_Single_GetReturnsChild) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto m = makeNodeModel();
    auto* parent = new DynamicEObject(m.nodeCls);
    auto* child = new DynamicEObject(m.nodeCls);
    parent->eSet(m.child, std::any{(EObject*)child});
    auto v = parent->eGet(m.child);
    auto* got = std::any_cast<EObject*>(v);
    EXPECT_EQ(got, child);
    delete parent;
    delete child;
}

EMF_TEST(DynamicEObject_Containment_Single_IsSet) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto m = makeNodeModel();
    auto* parent = new DynamicEObject(m.nodeCls);
    auto* child = new DynamicEObject(m.nodeCls);
    EXPECT_FALSE(parent->eIsSet(m.child));
    parent->eSet(m.child, std::any{(EObject*)child});
    EXPECT_TRUE(parent->eIsSet(m.child));
    parent->eUnset(m.child);
    EXPECT_FALSE(parent->eIsSet(m.child));
    delete parent;
    delete child;
}

EMF_TEST(DynamicEObject_Containment_NullValue_NoEContainer) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto m = makeNodeModel();
    auto* parent = new DynamicEObject(m.nodeCls);
    // 设置 null 子对象：不应崩溃，不应设置 eContainer
    parent->eSet(m.child, std::any{(EObject*)nullptr});
    EXPECT_TRUE(parent->eIsSet(m.child));
    auto v = parent->eGet(m.child);
    auto* got = std::any_cast<EObject*>(v);
    EXPECT_NULL(got);
    delete parent;
}

// ===== containment 多值引用：eGet 返回 list 指针 =====

EMF_TEST(DynamicEObject_MultiValue_EGetReturnsListPointer) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto m = makeNodeModel();
    auto* parent = new DynamicEObject(m.nodeCls);
    auto v = parent->eGet(m.children);
    // 多值引用返回 std::vector<EObject*>*
    auto* listPtr = std::any_cast<emf::common::EList<EObject*>*>(v);
    EXPECT_NOT_NULL(listPtr);
    EXPECT_EQ(listPtr->size(), (size_t)0);
    delete parent;
}

EMF_TEST(DynamicEObject_MultiValue_MutateListDirectly) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto m = makeNodeModel();
    auto* parent = new DynamicEObject(m.nodeCls);
    auto* c1 = new DynamicEObject(m.nodeCls);
    auto* c2 = new DynamicEObject(m.nodeCls);

    auto v = parent->eGet(m.children);
    auto* listPtr = std::any_cast<emf::common::EList<EObject*>*>(v);
    listPtr->add(c1);
    listPtr->add(c2);

    // 再次 eGet 应返回同一 list（lazy-created 后复用）
    auto v2 = parent->eGet(m.children);
    auto* listPtr2 = std::any_cast<emf::common::EList<EObject*>*>(v2);
    EXPECT_EQ(listPtr2, listPtr);
    EXPECT_EQ(listPtr2->size(), (size_t)2);
    delete parent;
    delete c1;
    delete c2;
}

EMF_TEST(DynamicEObject_MultiValue_IsSet_AfterAdd) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto m = makeNodeModel();
    auto* parent = new DynamicEObject(m.nodeCls);
    EXPECT_FALSE(parent->eIsSet(m.children));  // 空 list 未 set
    auto v = parent->eGet(m.children);
    auto* listPtr = std::any_cast<emf::common::EList<EObject*>*>(v);
    auto* c1 = new DynamicEObject(m.nodeCls);
    listPtr->add(c1);
    EXPECT_TRUE(parent->eIsSet(m.children));  // 非空 list 视为 set
    delete parent;
    delete c1;
}

EMF_TEST(DynamicEObject_MultiValue_EUnset_ClearsList) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto m = makeNodeModel();
    auto* parent = new DynamicEObject(m.nodeCls);
    auto* c1 = new DynamicEObject(m.nodeCls);
    auto v = parent->eGet(m.children);
    auto* listPtr = std::any_cast<emf::common::EList<EObject*>*>(v);
    listPtr->add(c1);
    EXPECT_TRUE(parent->eIsSet(m.children));
    parent->eUnset(m.children);
    EXPECT_FALSE(parent->eIsSet(m.children));
    delete parent;
    delete c1;
}

// ===== eContents 收集 =====

EMF_TEST(DynamicEObject_EContents_SingleContainment) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto m = makeNodeModel();
    auto* parent = new DynamicEObject(m.nodeCls);
    auto* child = new DynamicEObject(m.nodeCls);
    parent->eSet(m.child, std::any{(EObject*)child});

    const auto& contents = parent->eContents();
    EXPECT_EQ(contents.size(), (size_t)1);
    EXPECT_EQ(contents[0], child);
    delete parent;
    delete child;
}

EMF_TEST(DynamicEObject_EContents_MultiContainment) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto m = makeNodeModel();
    auto* parent = new DynamicEObject(m.nodeCls);
    auto* c1 = new DynamicEObject(m.nodeCls);
    auto* c2 = new DynamicEObject(m.nodeCls);
    auto* c3 = new DynamicEObject(m.nodeCls);

    auto v = parent->eGet(m.children);
    auto* listPtr = std::any_cast<emf::common::EList<EObject*>*>(v);
    listPtr->add(c1);
    listPtr->add(c2);
    listPtr->add(c3);

    const auto& contents = parent->eContents();
    EXPECT_EQ(contents.size(), (size_t)3);
    EXPECT_EQ(contents[0], c1);
    EXPECT_EQ(contents[1], c2);
    EXPECT_EQ(contents[2], c3);
    delete parent;
    delete c1;
    delete c2;
    delete c3;
}

EMF_TEST(DynamicEObject_EContents_BothSingleAndMulti) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto m = makeNodeModel();
    auto* parent = new DynamicEObject(m.nodeCls);
    auto* single = new DynamicEObject(m.nodeCls);
    auto* multi1 = new DynamicEObject(m.nodeCls);
    auto* multi2 = new DynamicEObject(m.nodeCls);

    parent->eSet(m.child, std::any{(EObject*)single});
    auto v = parent->eGet(m.children);
    auto* listPtr = std::any_cast<emf::common::EList<EObject*>*>(v);
    listPtr->add(multi1);
    listPtr->add(multi2);

    const auto& contents = parent->eContents();
    EXPECT_EQ(contents.size(), (size_t)3);
    delete parent;
    delete single;
    delete multi1;
    delete multi2;
}

EMF_TEST(DynamicEObject_EContents_NoContainment_Empty) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto m = makeNodeModel();
    auto* obj = new DynamicEObject(m.nodeCls);
    // 只设普通属性，无 containment
    obj->eSet(m.name, std::any{std::string{"x"}});
    obj->eSet(m.value, std::any{1});
    EXPECT_EQ(obj->eContents().size(), (size_t)0);
    delete obj;
}

// ===== 覆盖属性值 =====

EMF_TEST(DynamicEObject_ESet_OverwriteAttribute) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto m = makeNodeModel();
    auto* obj = new DynamicEObject(m.nodeCls);
    obj->eSet(m.name, std::any{std::string{"first"}});
    obj->eSet(m.name, std::any{std::string{"second"}});
    auto v = obj->eGet(m.name);
    EXPECT_EQ(*std::any_cast<std::string>(&v), std::string("second"));
    delete obj;
}

EMF_TEST(DynamicEObject_ESet_OverwriteContainmentChild) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto m = makeNodeModel();
    auto* parent = new DynamicEObject(m.nodeCls);
    auto* c1 = new DynamicEObject(m.nodeCls);
    auto* c2 = new DynamicEObject(m.nodeCls);
    parent->eSet(m.child, std::any{(EObject*)c1});
    EXPECT_EQ(c1->eContainer(), parent);
    parent->eSet(m.child, std::any{(EObject*)c2});
    EXPECT_EQ(c2->eContainer(), parent);
    auto v = parent->eGet(m.child);
    EXPECT_EQ(*std::any_cast<EObject*>(&v), c2);  // 取最后一个
    delete parent;
    delete c1;
    delete c2;
}
