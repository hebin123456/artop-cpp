// EcoreUtil 基础单元测试
// 对齐 org.eclipse.emf.ecore.util.EcoreUtil (Java) 的核心静态方法
// 覆盖：equals / equalsValue / getURI / getID / setID / isAncestor /
//       getEClassifier / createFromString / convertToString
#include "test_main.h"
#include "emf/ecore/util/EcoreUtil.h"
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
using emf::ecore::EPackage;
using emf::ecore::EDataType;
using emf::ecore::DynamicEObject;
using emf::ecore::util::EcoreUtil;

namespace {

// 构建一个 Person EClass：name(EString, id) + age(EInt)
EClass* makePersonClass() {
    auto* cls = EcoreFactory::instance().createEClass();
    cls->setName("Person");

    auto* name = EcoreFactory::instance().createEAttribute();
    name->setName("name");
    name->setEAttributeType(EcorePackage::instance().getEDataType_EString());
    name->setFeatureID(0);
    name->setID(true);
    cls->addEStructuralFeature(name);

    auto* age = EcoreFactory::instance().createEAttribute();
    age->setName("age");
    age->setEAttributeType(EcorePackage::instance().getEDataType_EInt());
    age->setFeatureID(1);
    cls->addEStructuralFeature(age);
    return cls;
}

}  // namespace

// ===== equals / equalsValue =====
EMF_TEST(EcoreUtil_Equals_SameObject_True) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* cls = makePersonClass();
    auto* obj = new DynamicEObject(cls);
    EXPECT_TRUE(EcoreUtil::equals(obj, obj));
    delete obj;
}

EMF_TEST(EcoreUtil_Equals_BothNull_True) {
    EXPECT_TRUE(EcoreUtil::equals(nullptr, nullptr));
}

EMF_TEST(EcoreUtil_Equals_OneNull_False) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* cls = makePersonClass();
    auto* obj = new DynamicEObject(cls);
    EXPECT_FALSE(EcoreUtil::equals(obj, nullptr));
    EXPECT_FALSE(EcoreUtil::equals(nullptr, obj));
    delete obj;
}

EMF_TEST(EcoreUtil_Equals_DifferentClasses_False) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* cls1 = makePersonClass();
    auto* cls2 = makePersonClass();  // 不同 EClass 指针
    auto* a = new DynamicEObject(cls1);
    auto* b = new DynamicEObject(cls2);
    EXPECT_FALSE(EcoreUtil::equals(a, b));
    delete a;
    delete b;
}

EMF_TEST(EcoreUtil_Equals_SameClassSameValues_True) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* cls = makePersonClass();
    auto* a = new DynamicEObject(cls);
    auto* b = new DynamicEObject(cls);
    a->eSet(cls->getEStructuralFeature("name"), std::any(std::string("alice")));
    b->eSet(cls->getEStructuralFeature("name"), std::any(std::string("alice")));
    EXPECT_TRUE(EcoreUtil::equals(a, b));
    delete a;
    delete b;
}

EMF_TEST(EcoreUtil_Equals_SameClassDifferentValues_False) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* cls = makePersonClass();
    auto* a = new DynamicEObject(cls);
    auto* b = new DynamicEObject(cls);
    a->eSet(cls->getEStructuralFeature("name"), std::any(std::string("alice")));
    b->eSet(cls->getEStructuralFeature("name"), std::any(std::string("bob")));
    EXPECT_FALSE(EcoreUtil::equals(a, b));
    delete a;
    delete b;
}

EMF_TEST(EcoreUtil_EqualsValue_String) {
    EXPECT_TRUE(EcoreUtil::equalsValue(std::any(std::string("x")), std::any(std::string("x"))));
    EXPECT_FALSE(EcoreUtil::equalsValue(std::any(std::string("x")), std::any(std::string("y"))));
}

EMF_TEST(EcoreUtil_EqualsValue_Int) {
    EXPECT_TRUE(EcoreUtil::equalsValue(std::any(42), std::any(42)));
    EXPECT_FALSE(EcoreUtil::equalsValue(std::any(42), std::any(43)));
}

EMF_TEST(EcoreUtil_EqualsValue_BothEmpty_True) {
    EXPECT_TRUE(EcoreUtil::equalsValue(std::any{}, std::any{}));
}

EMF_TEST(EcoreUtil_EqualsValue_OneEmpty_False) {
    EXPECT_FALSE(EcoreUtil::equalsValue(std::any{}, std::any(42)));
}

// ===== getID / setID =====
EMF_TEST(EcoreUtil_GetID_NoIdSet_Empty) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* cls = makePersonClass();
    auto* obj = new DynamicEObject(cls);
    EXPECT_EQ(EcoreUtil::getID(obj), std::string(""));
    delete obj;
}

EMF_TEST(EcoreUtil_SetID_ThenGetID) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* cls = makePersonClass();
    auto* obj = new DynamicEObject(cls);
    EcoreUtil::setID(obj, "alice-001");
    EXPECT_EQ(EcoreUtil::getID(obj), std::string("alice-001"));
    delete obj;
}

EMF_TEST(EcoreUtil_SetID_Overwrites) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* cls = makePersonClass();
    auto* obj = new DynamicEObject(cls);
    EcoreUtil::setID(obj, "first");
    EcoreUtil::setID(obj, "second");
    EXPECT_EQ(EcoreUtil::getID(obj), std::string("second"));
    delete obj;
}

EMF_TEST(EcoreUtil_GetID_NullObject_Empty) {
    EXPECT_EQ(EcoreUtil::getID(nullptr), std::string(""));
}

EMF_TEST(EcoreUtil_SetID_NullObject_NoCrash) {
    EcoreUtil::setID(nullptr, "x");  // 不应崩溃
}

EMF_TEST(EcoreUtil_GetID_NoIdAttribute_Empty) {
    // EClass 无 id 标记的 attribute → 返回空
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* cls = EcoreFactory::instance().createEClass();
    cls->setName("NoId");
    auto* a = EcoreFactory::instance().createEAttribute();
    a->setName("x");
    a->setEAttributeType(EcorePackage::instance().getEDataType_EString());
    a->setFeatureID(0);
    cls->addEStructuralFeature(a);
    auto* obj = new DynamicEObject(cls);
    obj->eSet(a, std::any(std::string("v")));
    EXPECT_EQ(EcoreUtil::getID(obj), std::string(""));
    delete obj;
}

// ===== getURI =====
EMF_TEST(EcoreUtil_GetURI_NullObject_Empty) {
    auto uri = EcoreUtil::getURI(nullptr);
    EXPECT_TRUE(uri.isEmpty());
}

EMF_TEST(EcoreUtil_GetURI_RootObject_NoResource_ContainsUrn) {
    // 无 Resource / 无 eContainer：返回 urn:emf 形式的路径
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* cls = makePersonClass();
    auto* obj = new DynamicEObject(cls);
    auto uri = EcoreUtil::getURI(obj);
    EXPECT_TRUE(uri.toString().find("urn:emf") != std::string::npos);
    delete obj;
}

// ===== isAncestor =====
EMF_TEST(EcoreUtil_IsAncestor_SameClass_True) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* cls = makePersonClass();
    auto* obj = new DynamicEObject(cls);
    EXPECT_TRUE(EcoreUtil::isAncestor(cls, obj));
    delete obj;
}

EMF_TEST(EcoreUtil_IsAncestor_SuperType_True) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* parent = EcoreFactory::instance().createEClass();
    parent->setName("Parent");
    auto* child = EcoreFactory::instance().createEClass();
    child->setName("Child");
    child->addESuperType(parent);
    auto* obj = new DynamicEObject(child);
    EXPECT_TRUE(EcoreUtil::isAncestor(parent, obj));
    delete obj;
}

EMF_TEST(EcoreUtil_IsAncestor_Unrelated_False) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* parent = EcoreFactory::instance().createEClass();
    parent->setName("Parent");
    auto* other = makePersonClass();
    auto* obj = new DynamicEObject(other);
    EXPECT_FALSE(EcoreUtil::isAncestor(parent, obj));
    delete obj;
}

EMF_TEST(EcoreUtil_IsAncestor_NullArgs_False) {
    // 显式指定 EClass* 重载（与同组 isAncestor 测试一致），消除 nullptr 的重载歧义
    EXPECT_FALSE(EcoreUtil::isAncestor(static_cast<EClass*>(nullptr), nullptr));
}

// ===== getEClassifier（EPackage 内查找 EDataType）=====
EMF_TEST(EcoreUtil_GetEClassifier_Found) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* pkg = EcoreFactory::instance().createEPackage();
    pkg->setName("P");
    auto* dt = EcoreFactory::instance().createEDataType();
    dt->setName("MyType");
    pkg->addEClassifier(dt);
    auto* found = EcoreUtil::getEClassifier(pkg, "MyType");
    EXPECT_NOT_NULL(found);
    EXPECT_EQ(found->getName(), std::string("MyType"));
}

EMF_TEST(EcoreUtil_GetEClassifier_NotFound_Null) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* pkg = EcoreFactory::instance().createEPackage();
    pkg->setName("P");
    EXPECT_NULL(EcoreUtil::getEClassifier(pkg, "Missing"));
}

EMF_TEST(EcoreUtil_GetEClassifier_NullPackage_Null) {
    EXPECT_NULL(EcoreUtil::getEClassifier(nullptr, "anything"));
}

EMF_TEST(EcoreUtil_GetEClassifier_NonDataType_ReturnsNull) {
    // getEClassifier 返回 EDataType*，传入 EClass（非 EDataType）应 dynamic_cast 失败 → null
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* pkg = EcoreFactory::instance().createEPackage();
    pkg->setName("P");
    auto* cls = EcoreFactory::instance().createEClass();
    cls->setName("C");
    pkg->addEClassifier(cls);
    EXPECT_NULL(EcoreUtil::getEClassifier(pkg, "C"));
}

// ===== createFromString / convertToString =====
EMF_TEST(EcoreUtil_CreateFromString_EString) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* dt = EcorePackage::instance().getEDataType_EString();
    auto v = EcoreUtil::createFromString(dt, "hello");
    EXPECT_EQ(std::any_cast<std::string>(v), std::string("hello"));
}

EMF_TEST(EcoreUtil_CreateFromString_EInt) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* dt = EcorePackage::instance().getEDataType_EInt();
    auto v = EcoreUtil::createFromString(dt, "123");
    EXPECT_EQ(std::any_cast<int>(v), 123);
}

EMF_TEST(EcoreUtil_CreateFromString_EBoolean) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* dt = EcorePackage::instance().getEDataType_EBoolean();
    EXPECT_EQ(std::any_cast<bool>(EcoreUtil::createFromString(dt, "true")), true);
    EXPECT_EQ(std::any_cast<bool>(EcoreUtil::createFromString(dt, "false")), false);
}

EMF_TEST(EcoreUtil_ConvertToString_EString) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* dt = EcorePackage::instance().getEDataType_EString();
    EXPECT_EQ(EcoreUtil::convertToString(dt, std::any(std::string("hi"))), std::string("hi"));
}

EMF_TEST(EcoreUtil_ConvertToString_EInt) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* dt = EcorePackage::instance().getEDataType_EInt();
    EXPECT_EQ(EcoreUtil::convertToString(dt, std::any(42)), std::string("42"));
}

EMF_TEST(EcoreUtil_ConvertToString_EBoolean) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* dt = EcorePackage::instance().getEDataType_EBoolean();
    EXPECT_EQ(EcoreUtil::convertToString(dt, std::any(true)), std::string("true"));
    EXPECT_EQ(EcoreUtil::convertToString(dt, std::any(false)), std::string("false"));
}

EMF_TEST(EcoreUtil_CreateFromString_NullDataType_Empty) {
    auto v = EcoreUtil::createFromString(nullptr, "x");
    EXPECT_FALSE(v.has_value());
}

EMF_TEST(EcoreUtil_ConvertToString_NullDataType_Empty) {
    EXPECT_EQ(EcoreUtil::convertToString(nullptr, std::any(42)), std::string(""));
}

EMF_TEST(EcoreUtil_CreateConvert_RoundTrip_EInt) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* dt = EcorePackage::instance().getEDataType_EInt();
    auto v = EcoreUtil::createFromString(dt, "99");
    EXPECT_EQ(EcoreUtil::convertToString(dt, v), std::string("99"));
}

// ===== EqualityHelper（基本委托 EcoreUtil）=====
EMF_TEST(EcoreUtil_EqualityHelper_EqualsSameObject) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* cls = makePersonClass();
    auto* obj = new DynamicEObject(cls);
    emf::ecore::util::EqualityHelper eh;
    EXPECT_TRUE(eh.equals(obj, obj));
    delete obj;
}

EMF_TEST(EcoreUtil_EqualityHelper_HashCode_String) {
    emf::ecore::util::EqualityHelper eh;
    double h = eh.hashCode(std::any(std::string("a")));
    EXPECT_TRUE(h != 0);  // "a" 哈希非零
}

EMF_TEST(EcoreUtil_EqualityHelper_HashCode_NullObject_Zero) {
    emf::ecore::util::EqualityHelper eh;
    EXPECT_EQ(eh.hashCode((EObject*)nullptr), 0.0);
}
