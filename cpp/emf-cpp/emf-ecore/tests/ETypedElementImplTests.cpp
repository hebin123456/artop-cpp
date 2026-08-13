// ETypedElementImpl 单元测试
// 对齐 Java: org.eclipse.emf.ecore.impl.ETypedElementImpl
// 覆盖：默认值 / eType 设置（含 proxy） / 边界（lower/upper/ordered/unique）
//       eGet / eSet / eIsSet / eUnset
//       isMany 语义 / getEGenericType() 懒加载 + 与 eType 同步
//       union 表示（嵌套 EGenericType）
#include "test_main.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/ecore/EcorePackage.h"
#include "emf/common/EObject.h"

#include <string>
#include <vector>

using emf::ecore::ETypedElementImpl;
using emf::ecore::EGenericTypeImpl;
using emf::ecore::EClassifier;
using emf::ecore::EDataTypeImpl;
using emf::ecore::EStructuralFeature;
using emf::ecore::EAttribute;
using emf::ecore::EAttributeImpl;
using emf::ecore::EcorePackage;
using emf::common::EObject;

// 构造测试用 EAttribute（featureID 匹配 ETypedElement 的某个 feature）
static EAttribute* mkAttr(int fid, const std::string& name) {
    auto* a = new EAttributeImpl();
    a->setName(name);
    a->setFeatureID(fid);
    return a;
}

// 测试 1：默认值
EMF_TEST(ETypedElement_Defaults) {
    ETypedElementImpl e;
    EXPECT_NULL(e.getEType());
    EXPECT_EQ(e.getLowerBound(), 0);
    EXPECT_EQ(e.getUpperBound(), 1);
    EXPECT_TRUE(e.isOrdered());
    EXPECT_TRUE(e.isUnique());
}

// 测试 2：setEType / getEType
EMF_TEST(ETypedElement_SetEType) {
    ETypedElementImpl e;
    auto* dt = new EDataTypeImpl();
    dt->setName("MyType");
    e.setEType(dt);
    EXPECT_TRUE(e.getEType() == dt);
}

// 测试 3：eGet for all 5 features
EMF_TEST(ETypedElement_EGet) {
    ETypedElementImpl e;
    auto* dt = new EDataTypeImpl();
    e.setEType(dt);
    e.setLowerBound(2);
    e.setUpperBound(-1);
    e.setOrdered(false);
    e.setUnique(false);

    auto* fEType  = mkAttr(::emf::common::FeatureID::ETYPED_ELEMENT_ETYPE, "eType");
    auto* fLower  = mkAttr(::emf::common::FeatureID::ETYPED_ELEMENT_ELOWERBOUND, "eLowerBound");
    auto* fUpper  = mkAttr(::emf::common::FeatureID::ETYPED_ELEMENT_EUPPERBOUND, "eUpperBound");
    auto* fOrder  = mkAttr(::emf::common::FeatureID::ETYPED_ELEMENT_EORDERED, "eOrdered");
    auto* fUnique = mkAttr(::emf::common::FeatureID::ETYPED_ELEMENT_EUNIQUE, "eUnique");

    auto vt = std::any_cast<EClassifier*>(e.eGet(fEType));
    EXPECT_TRUE(vt == dt);
    EXPECT_EQ(std::any_cast<int>(e.eGet(fLower)), 2);
    EXPECT_EQ(std::any_cast<int>(e.eGet(fUpper)), -1);
    EXPECT_FALSE(std::any_cast<bool>(e.eGet(fOrder)));
    EXPECT_FALSE(std::any_cast<bool>(e.eGet(fUnique)));
}

// 测试 4：eSet for all 5 features
EMF_TEST(ETypedElement_ESet) {
    ETypedElementImpl e;
    auto* fEType  = mkAttr(emf::common::FeatureID::ETYPED_ELEMENT_ETYPE, "eType");
    auto* fLower  = mkAttr(emf::common::FeatureID::ETYPED_ELEMENT_ELOWERBOUND, "eLowerBound");
    auto* fUpper  = mkAttr(emf::common::FeatureID::ETYPED_ELEMENT_EUPPERBOUND, "eUpperBound");
    auto* fOrder  = mkAttr(emf::common::FeatureID::ETYPED_ELEMENT_EORDERED, "eOrdered");
    auto* fUnique = mkAttr(emf::common::FeatureID::ETYPED_ELEMENT_EUNIQUE, "eUnique");

    e.eSet(fLower, std::any{0});
    e.eSet(fUpper, std::any{5});
    e.eSet(fOrder, std::any{false});
    e.eSet(fUnique, std::any{false});
    EXPECT_EQ(e.getLowerBound(), 0);
    EXPECT_EQ(e.getUpperBound(), 5);
    EXPECT_FALSE(e.isOrdered());
    EXPECT_FALSE(e.isUnique());

    // eSet eType (EClassifier*)
    auto* dt = new EDataTypeImpl();
    e.eSet(fEType, std::any{static_cast<EClassifier*>(dt)});
    EXPECT_TRUE(e.getEType() == dt);

    // eSet eType (EObject* proxy)
    auto* dt2 = new EDataTypeImpl();
    e.eSet(fEType, std::any{static_cast<EObject*>(dt2)});
    EXPECT_TRUE(e.getEType() == dt2);
}

// 测试 5：eIsSet 默认状态（Java 语义：未 set 过 → false）
EMF_TEST(ETypedElement_EIsSet_Defaults) {
    ETypedElementImpl e;
    auto* fEType  = mkAttr(emf::common::FeatureID::ETYPED_ELEMENT_ETYPE, "eType");
    auto* fLower  = mkAttr(emf::common::FeatureID::ETYPED_ELEMENT_ELOWERBOUND, "eLowerBound");
    auto* fUpper  = mkAttr(emf::common::FeatureID::ETYPED_ELEMENT_EUPPERBOUND, "eUpperBound");
    auto* fOrder  = mkAttr(emf::common::FeatureID::ETYPED_ELEMENT_EORDERED, "eOrdered");
    auto* fUnique = mkAttr(emf::common::FeatureID::ETYPED_ELEMENT_EUNIQUE, "eUnique");
    EXPECT_FALSE(e.eIsSet(fEType));
    EXPECT_FALSE(e.eIsSet(fLower));
    EXPECT_FALSE(e.eIsSet(fUpper));
    EXPECT_FALSE(e.eIsSet(fOrder));
    EXPECT_FALSE(e.eIsSet(fUnique));
}

// 测试 6：eIsSet 设置后
EMF_TEST(ETypedElement_EIsSet_AfterSet) {
    ETypedElementImpl e;
    auto* dt = new EDataTypeImpl();
    e.setEType(dt);
    e.setLowerBound(2);
    e.setUpperBound(3);
    e.setOrdered(false);
    e.setUnique(false);
    auto* fEType  = mkAttr(emf::common::FeatureID::ETYPED_ELEMENT_ETYPE, "eType");
    auto* fLower  = mkAttr(emf::common::FeatureID::ETYPED_ELEMENT_ELOWERBOUND, "eLowerBound");
    auto* fUpper  = mkAttr(emf::common::FeatureID::ETYPED_ELEMENT_EUPPERBOUND, "eUpperBound");
    auto* fOrder  = mkAttr(emf::common::FeatureID::ETYPED_ELEMENT_EORDERED, "eOrdered");
    auto* fUnique = mkAttr(emf::common::FeatureID::ETYPED_ELEMENT_EUNIQUE, "eUnique");
    EXPECT_TRUE(e.eIsSet(fEType));
    EXPECT_TRUE(e.eIsSet(fLower));
    EXPECT_TRUE(e.eIsSet(fUpper));
    EXPECT_TRUE(e.eIsSet(fOrder));
    EXPECT_TRUE(e.eIsSet(fUnique));
}

// 测试 7：eUnset 恢复默认
EMF_TEST(ETypedElement_EUnset) {
    ETypedElementImpl e;
    auto* dt = new EDataTypeImpl();
    e.setEType(dt);
    e.setLowerBound(2);
    e.setUpperBound(3);
    e.setOrdered(false);
    e.setUnique(false);
    auto* fEType  = mkAttr(emf::common::FeatureID::ETYPED_ELEMENT_ETYPE, "eType");
    auto* fLower  = mkAttr(emf::common::FeatureID::ETYPED_ELEMENT_ELOWERBOUND, "eLowerBound");
    auto* fUpper  = mkAttr(emf::common::FeatureID::ETYPED_ELEMENT_EUPPERBOUND, "eUpperBound");
    auto* fOrder  = mkAttr(emf::common::FeatureID::ETYPED_ELEMENT_EORDERED, "eOrdered");
    auto* fUnique = mkAttr(emf::common::FeatureID::ETYPED_ELEMENT_EUNIQUE, "eUnique");
    e.eUnset(fEType);
    e.eUnset(fLower);
    e.eUnset(fUpper);
    e.eUnset(fOrder);
    e.eUnset(fUnique);
    EXPECT_NULL(e.getEType());
    EXPECT_EQ(e.getLowerBound(), 0);
    EXPECT_EQ(e.getUpperBound(), 1);
    EXPECT_TRUE(e.isOrdered());
    EXPECT_TRUE(e.isUnique());
}

// 测试 8：isMany 语义（Java EMF）：upper==-1 || upper>1
EMF_TEST(ETypedElement_IsMany) {
    ETypedElementImpl e;
    // 缺省 upper=1 → 单值
    EXPECT_FALSE(e.getUpperBound() == -1 || e.getUpperBound() > 1);
    e.setUpperBound(0);
    EXPECT_FALSE(e.getUpperBound() == -1 || e.getUpperBound() > 1);
    e.setUpperBound(2);
    EXPECT_TRUE(e.getUpperBound() == -1 || e.getUpperBound() > 1);
    e.setUpperBound(-1);
    EXPECT_TRUE(e.getUpperBound() == -1 || e.getUpperBound() > 1);
}

// 测试 9：eType proxy（存 EObject*）
EMF_TEST(ETypedElement_ETypeProxy) {
    ETypedElementImpl e;
    // 模拟 proxy：EDataType 既能当 EClassifier 也能当 EObject
    auto* dt = new EDataTypeImpl();
    e.setEType(dt);
    auto* fEType = mkAttr(emf::common::FeatureID::ETYPED_ELEMENT_ETYPE, "eType");
    // eGet 走 dynamic_cast 应该拿到 EClassifier*
    auto* cls = std::any_cast<EClassifier*>(e.eGet(fEType));
    EXPECT_TRUE(cls == dt);
    delete dt;
}

// 测试 10：getEGenericType() lazy 创建
EMF_TEST(ETypedElement_EGenericType_Lazy) {
    ETypedElementImpl e;
    // 第一次调用创建
    auto* g1 = e.getEGenericType();
    EXPECT_NOT_NULL(g1);
    // 第二次调用返回同一个
    EXPECT_TRUE(e.getEGenericType() == g1);
    // 此时 eType=null，eGenericType.eClassifier 也应是 null
    EXPECT_NULL(g1->getEClassifier());
}

// 测试 11：setEType 同步到 eGenericType.eClassifier
EMF_TEST(ETypedElement_SetEType_SyncsGenericType) {
    ETypedElementImpl e;
    auto* dt = new EDataTypeImpl();
    dt->setName("X");
    // 先调用 getEGenericType() 触发 lazy 创建
    auto* g = e.getEGenericType();
    // 此时 setEType 应同步
    e.setEType(dt);
    EXPECT_TRUE(g->getEClassifier() == dt);
    // 再调用 getEGenericType 仍应返回同一个
    EXPECT_TRUE(e.getEGenericType() == g);
}

// 测试 12：setEType 后 getEGenericType 自动跟进
EMF_TEST(ETypedElement_GenericTypeFollowsEType) {
    ETypedElementImpl e;
    auto* dt1 = new EDataTypeImpl();
    auto* dt2 = new EDataTypeImpl();
    e.setEType(dt1);
    auto* g = e.getEGenericType();
    EXPECT_TRUE(g->getEClassifier() == dt1);
    e.setEType(dt2);
    EXPECT_TRUE(g->getEClassifier() == dt2);
}

// 测试 13：union 语义（嵌套 EGenericType via eTypeArguments）
// Java 行为：union 由嵌套 EGenericType 表示
//  outer.eClassifier = null
//  outer.eTypeArguments = [g1, g2, g3]
//  每个 gX.eClassifier = union member type
EMF_TEST(ETypedElement_Union_GenericTypeArguments) {
    auto* outer = new EGenericTypeImpl();
    auto* m1 = new EGenericTypeImpl(); m1->setEClassifier(new EDataTypeImpl());
    auto* m2 = new EGenericTypeImpl(); m2->setEClassifier(new EDataTypeImpl());
    auto* m3 = new EGenericTypeImpl(); m3->setEClassifier(new EDataTypeImpl());
    std::vector<emf::ecore::EGenericType*> args = {m1, m2, m3};
    outer->setETypeArguments(args);
    EXPECT_EQ((int)outer->getETypeArguments().size(), 3);
    EXPECT_TRUE(outer->getETypeArguments()[0] == m1);
    EXPECT_TRUE(outer->getETypeArguments()[1] == m2);
    EXPECT_TRUE(outer->getETypeArguments()[2] == m3);
}

// 测试 14：wildcard 语义（lowerBound / upperBound 都是 EGenericType*）
EMF_TEST(ETypedElement_GenericTypeWildcard) {
    auto* g = new EGenericTypeImpl();
    auto* ub = new EGenericTypeImpl();
    ub->setEClassifier(new EDataTypeImpl());
    g->setEUpperBound(ub);
    EXPECT_TRUE(g->getEUpperBound() == ub);
    auto* lb = new EGenericTypeImpl();
    g->setELowerBound(lb);
    EXPECT_TRUE(g->getELowerBound() == lb);
}

// 测试 15：union（EDataType 通过 eTypeArguments 链表示）
// 在 Java XSD 中，union 表达是：EDataType 自身是 union 容器，
// 它的 memberTypes 列表每个都是一个 EDataType。
// 在 EGenericType 视角下，union 表示为 EGenericType 嵌套 eTypeArguments 链。
EMF_TEST(ETypedElement_Union_AsNestedGenericType) {
    auto* unionGt = new EGenericTypeImpl();
    // union 自身没有 eClassifier，但有多个 eTypeArguments（每个是一个 EDataType）
    auto* stringT = new EGenericTypeImpl(); stringT->setEClassifier(new EDataTypeImpl());
    auto* intT    = new EGenericTypeImpl(); intT->setEClassifier(new EDataTypeImpl());
    std::vector<emf::ecore::EGenericType*> args = {stringT, intT};
    unionGt->setETypeArguments(args);
    EXPECT_EQ((int)unionGt->getETypeArguments().size(), 2);
    // typeArguments 中每个都有自己的 eClassifier
    EXPECT_NOT_NULL(unionGt->getETypeArguments()[0]->getEClassifier());
    EXPECT_NOT_NULL(unionGt->getETypeArguments()[1]->getEClassifier());
    // union 自身没有 eClassifier
    EXPECT_NULL(unionGt->getEClassifier());
}
