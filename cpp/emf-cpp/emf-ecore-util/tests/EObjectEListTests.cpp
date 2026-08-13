// EObjectEList 单元测试
// 对齐 Java org.eclipse.emf.ecore.util.EObjectEList
// 测试多值 EObject 引用列表的增删改查、唯一性、null 约束及 EcoreEList 钩子
#include "test_main.h"
#include "emf/ecore/util/EObjectEList.h"
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
using emf::ecore::DynamicEObject;
using emf::ecore::util::EObjectEList;

namespace {

// 测试夹具：创建一个 EClass 用于构造 EObject 元素
EClass* makeElementClass() {
    auto* cls = EcoreFactory::instance().createEClass();
    cls->setName("Element");
    return cls;
}

EObject* makeElement(EClass* cls) {
    return new DynamicEObject(cls);
}

}  // namespace

// ===== 构造与基本属性 =====
EMF_TEST(EObjectEList_Construct_StoresFeatureID) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* cls = makeElementClass();
    EObjectEList<EObject*> list(cls, nullptr, 7);
    EXPECT_EQ(list.getFeatureID(), 7);
    EXPECT_EQ(list.size(), 0);
    EXPECT_TRUE(list.isEmpty());
}

EMF_TEST(EObjectEList_Construct_DefaultFeatureID_NegativeOne) {
    // EObjectEList 默认 featureID_ = -1
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* cls = makeElementClass();
    EObjectEList<EObject*> list(cls, nullptr, 5);
    EXPECT_EQ(list.featureID_, 5);
}

EMF_TEST(EObjectEList_Owner_DataClass_Stored) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* cls = makeElementClass();
    EObjectEList<EObject*> list(cls, nullptr, 3);
    EXPECT_EQ(list.dataClass(), cls);
    EXPECT_NULL(list.owner());
}

// ===== EcoreEList 钩子（对齐 Java EObjectEList 语义）=====
EMF_TEST(EObjectEList_UseEquals_False) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* cls = makeElementClass();
    EObjectEList<EObject*> list(cls, nullptr, 0);
    // EObjectEList 按指针比较，不走 equals
    EXPECT_FALSE(list.useEquals());
}

EMF_TEST(EObjectEList_IsUnique_True) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* cls = makeElementClass();
    EObjectEList<EObject*> list(cls, nullptr, 0);
    EXPECT_TRUE(list.isUnique());
}

EMF_TEST(EObjectEList_HasInverse_False) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* cls = makeElementClass();
    EObjectEList<EObject*> list(cls, nullptr, 0);
    EXPECT_FALSE(list.hasInverse());
}

EMF_TEST(EObjectEList_IsEObject_True) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* cls = makeElementClass();
    EObjectEList<EObject*> list(cls, nullptr, 0);
    EXPECT_TRUE(list.isEObject());
}

EMF_TEST(EObjectEList_CanContainNull_False) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* cls = makeElementClass();
    EObjectEList<EObject*> list(cls, nullptr, 0);
    EXPECT_FALSE(list.canContainNull());
}

// ===== add / size =====
EMF_TEST(EObjectEList_Add_IncreasesSize) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* cls = makeElementClass();
    EObjectEList<EObject*> list(cls, nullptr, 0);
    auto* a = makeElement(cls);
    EXPECT_TRUE(list.add(a));
    EXPECT_EQ(list.size(), 1);
    EXPECT_FALSE(list.isEmpty());
    delete a;
}

EMF_TEST(EObjectEList_Add_Multiple_PreservesOrder) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* cls = makeElementClass();
    EObjectEList<EObject*> list(cls, nullptr, 0);
    auto* a = makeElement(cls);
    auto* b = makeElement(cls);
    auto* c = makeElement(cls);
    list.add(a);
    list.add(b);
    list.add(c);
    EXPECT_EQ(list.size(), 3);
    EXPECT_EQ(list.get(0), a);
    EXPECT_EQ(list.get(1), b);
    EXPECT_EQ(list.get(2), c);
    delete a; delete b; delete c;
}

// ===== add 拒绝重复（isUnique=true）=====
EMF_TEST(EObjectEList_Add_Duplicate_Rejected) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* cls = makeElementClass();
    EObjectEList<EObject*> list(cls, nullptr, 0);
    auto* a = makeElement(cls);
    EXPECT_TRUE(list.add(a));
    EXPECT_FALSE(list.add(a));  // 重复指针被拒绝
    EXPECT_EQ(list.size(), 1);
    delete a;
}

EMF_TEST(EObjectEList_AddUnique_Duplicate_NotAdded) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* cls = makeElementClass();
    EObjectEList<EObject*> list(cls, nullptr, 0);
    auto* a = makeElement(cls);
    list.addUnique(a);
    list.addUnique(a);  // addUnique 不检查重复，直接追加
    // 注意：addUnique 绕过唯一性检查（对齐 Java 语义：调用方需自行保证唯一）
    EXPECT_EQ(list.size(), 2);
    delete a;
}

// ===== add 拒绝 null（canContainNull=false）=====
EMF_TEST(EObjectEList_Add_Null_Throws) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* cls = makeElementClass();
    EObjectEList<EObject*> list(cls, nullptr, 0);
    EXPECT_THROWS(list.add(nullptr));
    EXPECT_EQ(list.size(), 0);
}

// ===== get / basicGet =====
EMF_TEST(EObjectEList_Get_InBounds) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* cls = makeElementClass();
    EObjectEList<EObject*> list(cls, nullptr, 0);
    auto* a = makeElement(cls);
    auto* b = makeElement(cls);
    list.add(a);
    list.add(b);
    EXPECT_EQ(list.get(0), a);
    EXPECT_EQ(list.get(1), b);
    delete a; delete b;
}

EMF_TEST(EObjectEList_Get_OutOfBounds_Throws) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* cls = makeElementClass();
    EObjectEList<EObject*> list(cls, nullptr, 0);
    EXPECT_THROWS(list.get(0));
}

EMF_TEST(EObjectEList_BasicGet_ReturnsStored) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* cls = makeElementClass();
    EObjectEList<EObject*> list(cls, nullptr, 0);
    auto* a = makeElement(cls);
    list.add(a);
    EXPECT_EQ(list.basicGet(0), a);
    delete a;
}

// ===== contains / indexOf =====
EMF_TEST(EObjectEList_Contains_Present_True) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* cls = makeElementClass();
    EObjectEList<EObject*> list(cls, nullptr, 0);
    auto* a = makeElement(cls);
    list.add(a);
    EXPECT_TRUE(list.contains(a));
    delete a;
}

EMF_TEST(EObjectEList_Contains_Absent_False) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* cls = makeElementClass();
    EObjectEList<EObject*> list(cls, nullptr, 0);
    auto* a = makeElement(cls);
    EXPECT_FALSE(list.contains(a));
    delete a;
}

EMF_TEST(EObjectEList_IndexOf_Found) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* cls = makeElementClass();
    EObjectEList<EObject*> list(cls, nullptr, 0);
    auto* a = makeElement(cls);
    auto* b = makeElement(cls);
    list.add(a);
    list.add(b);
    EXPECT_EQ(list.indexOf(a), 0);
    EXPECT_EQ(list.indexOf(b), 1);
    delete a; delete b;
}

EMF_TEST(EObjectEList_IndexOf_NotFound_NegativeOne) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* cls = makeElementClass();
    EObjectEList<EObject*> list(cls, nullptr, 0);
    auto* a = makeElement(cls);
    EXPECT_EQ(list.indexOf(a), -1);
    delete a;
}

// ===== remove(int) =====
EMF_TEST(EObjectEList_RemoveByIndex_ReturnsOld) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* cls = makeElementClass();
    EObjectEList<EObject*> list(cls, nullptr, 0);
    auto* a = makeElement(cls);
    auto* b = makeElement(cls);
    list.add(a);
    list.add(b);
    EObject* removed = list.remove(0);
    EXPECT_EQ(removed, a);
    EXPECT_EQ(list.size(), 1);
    EXPECT_EQ(list.get(0), b);
    delete a; delete b;
}

EMF_TEST(EObjectEList_RemoveByIndex_OutOfBounds_Throws) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* cls = makeElementClass();
    EObjectEList<EObject*> list(cls, nullptr, 0);
    EXPECT_THROWS(list.remove(0));
}

// ===== remove(value) =====
EMF_TEST(EObjectEList_RemoveValue_ReturnsTrue) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* cls = makeElementClass();
    EObjectEList<EObject*> list(cls, nullptr, 0);
    auto* a = makeElement(cls);
    auto* b = makeElement(cls);
    list.add(a);
    list.add(b);
    EXPECT_TRUE(list.removeByValue(a));
    EXPECT_EQ(list.size(), 1);
    EXPECT_FALSE(list.contains(a));
    EXPECT_TRUE(list.contains(b));
    delete a; delete b;
}

EMF_TEST(EObjectEList_RemoveValue_NotPresent_ReturnsFalse) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* cls = makeElementClass();
    EObjectEList<EObject*> list(cls, nullptr, 0);
    auto* a = makeElement(cls);
    list.add(a);
    auto* b = makeElement(cls);  // 未加入
    EXPECT_FALSE(list.removeByValue(b));
    EXPECT_EQ(list.size(), 1);
    delete a; delete b;
}

// ===== set =====
EMF_TEST(EObjectEList_Set_ReplacesOld) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* cls = makeElementClass();
    EObjectEList<EObject*> list(cls, nullptr, 0);
    auto* a = makeElement(cls);
    auto* b = makeElement(cls);
    list.add(a);
    // EcoreEList::set(std::any) 隐藏了基类 set(int, E)，用 setUnique 直接替换
    EObject* old = list.setUnique(0, b);
    EXPECT_EQ(old, a);
    EXPECT_EQ(list.get(0), b);
    delete a; delete b;
}

EMF_TEST(EObjectEList_Set_DuplicateAtDifferentIndex_Throws) {
    // isUnique=true：在另一个位置已有同元素 → set 抛 invalid_argument
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* cls = makeElementClass();
    EObjectEList<EObject*> list(cls, nullptr, 0);
    auto* a = makeElement(cls);
    auto* b = makeElement(cls);
    list.add(a);
    list.add(b);
    // EcoreEList::set(std::any) 隐藏了基类 set(int, E)，用限定名调用以触发唯一性检查
    EXPECT_THROWS(list.emf::common::util::BasicEList<EObject*>::set(1, a));  // a 已在 index 0
    delete a; delete b;
}

// ===== clear =====
EMF_TEST(EObjectEList_Clear_EmptiesList) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* cls = makeElementClass();
    EObjectEList<EObject*> list(cls, nullptr, 0);
    auto* a = makeElement(cls);
    auto* b = makeElement(cls);
    list.add(a);
    list.add(b);
    list.clear();
    EXPECT_EQ(list.size(), 0);
    EXPECT_TRUE(list.isEmpty());
    delete a; delete b;
}

// ===== move =====
EMF_TEST(EObjectEList_Move_Reorders) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* cls = makeElementClass();
    EObjectEList<EObject*> list(cls, nullptr, 0);
    auto* a = makeElement(cls);
    auto* b = makeElement(cls);
    auto* c = makeElement(cls);
    list.add(a);
    list.add(b);
    list.add(c);
    // 把 index 0 移到 index 2
    EObject* moved = list.move(2, 0);
    EXPECT_EQ(moved, a);
    EXPECT_EQ(list.get(0), b);
    EXPECT_EQ(list.get(1), c);
    EXPECT_EQ(list.get(2), a);
    delete a; delete b; delete c;
}

// ===== toArray =====
EMF_TEST(EObjectEList_ToArray_ReturnsElements) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* cls = makeElementClass();
    EObjectEList<EObject*> list(cls, nullptr, 0);
    auto* a = makeElement(cls);
    auto* b = makeElement(cls);
    list.add(a);
    list.add(b);
    auto arr = list.toArray();
    EXPECT_EQ(arr.size(), (size_t)2);
    EXPECT_EQ(arr[0], a);
    EXPECT_EQ(arr[1], b);
    delete a; delete b;
}

// ===== isSet / unset（继承自 EcoreEList）=====
EMF_TEST(EObjectEList_IsSet_TrueWhenNonEmpty) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* cls = makeElementClass();
    EObjectEList<EObject*> list(cls, nullptr, 0);
    EXPECT_FALSE(list.isSet());
    auto* a = makeElement(cls);
    list.add(a);
    EXPECT_TRUE(list.isSet());
    list.unset();
    EXPECT_FALSE(list.isSet());
    EXPECT_EQ(list.size(), 0);
    delete a;
}

// ===== addAllUnique（批量添加）=====
EMF_TEST(EObjectEList_AddAllUnique_AppendsAll) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* cls = makeElementClass();
    EObjectEList<EObject*> list(cls, nullptr, 0);
    auto* a = makeElement(cls);
    auto* b = makeElement(cls);
    std::vector<EObject*> in = {a, b};
    EXPECT_TRUE(list.addAllUnique(in));
    EXPECT_EQ(list.size(), 2);
    EXPECT_EQ(list.get(0), a);
    EXPECT_EQ(list.get(1), b);
    delete a; delete b;
}

EMF_TEST(EObjectEList_AddAllUnique_EmptyInput_ReturnsFalse) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* cls = makeElementClass();
    EObjectEList<EObject*> list(cls, nullptr, 0);
    std::vector<EObject*> empty;
    EXPECT_FALSE(list.addAllUnique(empty));
}

// ===== Unsettable 变体 =====
EMF_TEST(EObjectEList_Unsettable_IsSet_Flag) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* cls = makeElementClass();
    emf::ecore::util::EObjectEList_Unsettable<EObject*> list(cls, nullptr, 4);
    EXPECT_FALSE(list.isSet());
    auto* a = makeElement(cls);
    list.add(a);
    EXPECT_TRUE(list.isSet());
    list.unset();
    EXPECT_FALSE(list.isSet());
    EXPECT_EQ(list.size(), 0);
    delete a;
}

EMF_TEST(EObjectEList_Unsettable_FeatureID) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* cls = makeElementClass();
    emf::ecore::util::EObjectEList_Unsettable<EObject*> list(cls, nullptr, 9);
    EXPECT_EQ(list.getFeatureID(), 9);
}
