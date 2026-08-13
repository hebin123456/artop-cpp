// Copier 单元测试
// 对齐 org.eclipse.emf.ecore.util.EcoreUtil.Copier (Java)
// 覆盖：copy / copyAll / copyReferences / get（源->副本映射）
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
using emf::ecore::DynamicEObject;
using emf::ecore::util::Copier;
using emf::ecore::util::EcoreUtil;

namespace {

// Person { name:EString(id) } + friendRef:Person (非 containment, 单值)
struct Model {
    EClass* personCls;
    EAttribute* nameAttr;
    EReference* friendRef;  // 非 containment
    EPackage* pkg;

    Model() {
        pkg = EcoreFactory::instance().createEPackage();
        pkg->setName("Pkg");

        personCls = EcoreFactory::instance().createEClass();
        personCls->setName("Person");
        pkg->addEClassifier(personCls);

        nameAttr = EcoreFactory::instance().createEAttribute();
        nameAttr->setName("name");
        nameAttr->setEAttributeType(EcorePackage::instance().getEDataType_EString());
        nameAttr->setFeatureID(0);
        nameAttr->setID(true);
        personCls->addEStructuralFeature(nameAttr);

        friendRef = EcoreFactory::instance().createEReference();
        friendRef->setName("friend");
        friendRef->setEReferenceType(personCls);
        friendRef->setContainment(false);
        friendRef->setFeatureID(1);
        friendRef->setUpperBound(1);
        personCls->addEStructuralFeature(friendRef);
    }
};

}  // namespace

// ===== Copier::copy 单对象 =====
EMF_TEST(Copier_Copy_NullObject_Null) {
    Copier c;
    EXPECT_NULL(c.copy(nullptr));
}

EMF_TEST(Copier_Copy_ReturnsNewInstance) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    Model m;
    auto* src = new DynamicEObject(m.personCls);
    Copier c;
    auto* cp = c.copy(src);
    EXPECT_NOT_NULL(cp);
    EXPECT_NE(cp, src);
    EXPECT_EQ(cp->eClass(), m.personCls);
    delete src;
    delete cp;
}

EMF_TEST(Copier_Copy_PreservesAttribute) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    Model m;
    auto* src = new DynamicEObject(m.personCls);
    src->eSet(m.nameAttr, std::any(std::string("alice")));
    Copier c;
    auto* cp = c.copy(src);
    EXPECT_EQ(std::any_cast<std::string>(cp->eGet(m.nameAttr)), std::string("alice"));
    delete src;
    delete cp;
}

EMF_TEST(Copier_Copy_Idempotent_ReturnsSameInstance) {
    // 同一源对象第二次 copy 返回缓存的副本
    EcoreFactory::initialize();
    EcorePackage::initialize();
    Model m;
    auto* src = new DynamicEObject(m.personCls);
    Copier c;
    auto* cp1 = c.copy(src);
    auto* cp2 = c.copy(src);
    EXPECT_EQ(cp1, cp2);
    delete src;
    delete cp1;
}

// ===== Copier::get（源->副本映射）=====
EMF_TEST(Copier_Get_UnknownSource_Null) {
    Copier c;
    EcoreFactory::initialize();
    EcorePackage::initialize();
    Model m;
    auto* src = new DynamicEObject(m.personCls);
    EXPECT_NULL(c.get(src));
    delete src;
}

EMF_TEST(Copier_Get_AfterCopy_ReturnsCopy) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    Model m;
    auto* src = new DynamicEObject(m.personCls);
    Copier c;
    auto* cp = c.copy(src);
    EXPECT_EQ(c.get(src), cp);
    delete src;
    delete cp;
}

// ===== Copier::copyAll =====
EMF_TEST(Copier_CopyAll_EmptyInput_Empty) {
    Copier c;
    std::vector<EObject*> in;
    auto out = c.copyAll(in);
    EXPECT_TRUE(out.empty());
}

EMF_TEST(Copier_CopyAll_MultipleObjects) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    Model m;
    auto* a = new DynamicEObject(m.personCls);
    a->eSet(m.nameAttr, std::any(std::string("a")));
    auto* b = new DynamicEObject(m.personCls);
    b->eSet(m.nameAttr, std::any(std::string("b")));

    Copier c;
    auto out = c.copyAll({a, b});
    EXPECT_EQ(out.size(), (size_t)2);
    EXPECT_NE(out[0], a);
    EXPECT_NE(out[1], b);
    EXPECT_EQ(c.get(a), out[0]);
    EXPECT_EQ(c.get(b), out[1]);
    EXPECT_EQ(std::any_cast<std::string>(out[0]->eGet(m.nameAttr)), std::string("a"));
    delete a;
    delete b;
    for (auto* o : out) delete o;
}

// ===== Copier::copyReferences（非 containment 引用更新）=====
EMF_TEST(Copier_CopyReferences_NonContainmentRef_PointsToCopy) {
    // a.friend = b；拷贝后 a'.friend 应指向 b'（副本而非源 b）
    EcoreFactory::initialize();
    EcorePackage::initialize();
    Model m;
    auto* a = new DynamicEObject(m.personCls);
    a->eSet(m.nameAttr, std::any(std::string("a")));
    auto* b = new DynamicEObject(m.personCls);
    b->eSet(m.nameAttr, std::any(std::string("b")));
    a->eSet(m.friendRef, std::any((EObject*)b));

    Copier c;
    auto* aCopy = c.copy(a);
    auto* bCopy = c.copy(b);
    // copyReferences 之前 aCopy.friend 可能仍指向 b（源）
    c.copyReferences();
    auto v = aCopy->eGet(m.friendRef);
    EObject* refTarget = std::any_cast<EObject*>(v);
    EXPECT_NOT_NULL(refTarget);
    // 拷贝后应指向 b 的副本而非 b 本身
    EXPECT_EQ(refTarget, bCopy);
    EXPECT_NE(refTarget, b);
    delete a;
    delete b;
    delete aCopy;
    delete bCopy;
}

EMF_TEST(Copier_CopyReferences_NotCalled_RefStillPointsToSource) {
    // 不调用 copyReferences：非 containment 引用副本仍指向源对象（对齐 Java 行为）
    EcoreFactory::initialize();
    EcorePackage::initialize();
    Model m;
    auto* a = new DynamicEObject(m.personCls);
    auto* b = new DynamicEObject(m.personCls);
    a->eSet(m.friendRef, std::any((EObject*)b));

    Copier c;
    auto* aCopy = c.copy(b);  // 复制 b 不相关，主要看 a 的 friendRef
    (void)aCopy;
    auto* aC = c.copy(a);
    // 未 copyReferences，aC.friend 仍指向源 b
    auto v = aC->eGet(m.friendRef);
    EObject* refTarget = std::any_cast<EObject*>(v);
    EXPECT_EQ(refTarget, b);
    // aCopy 由 c.copy(b) 产生；在 delete b 之前取出 bCopy，避免 use-after-free
    // （delete b 后 c.get(b) 查 map 虽不 deref，但 heap 布局变化可能触发 SIGSEGV）。
    auto* bCopy = c.get(b);
    delete bCopy;
    delete a;
    delete b;
    delete aC;
}

// ===== copyReferences 后再读：源映射保持 =====
EMF_TEST(Copier_CopyReferences_KeepsMapping) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    Model m;
    auto* a = new DynamicEObject(m.personCls);
    Copier c;
    auto* aCopy = c.copy(a);
    c.copyReferences();
    EXPECT_EQ(c.get(a), aCopy);
    delete a;
    delete aCopy;
}

// ===== EcoreUtil::copy（封装 Copier）端到端 =====
EMF_TEST(EcoreUtil_Copy_WithContainment_DeepCopy) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    // 构造 Person + child containment
    auto* pkg = EcoreFactory::instance().createEPackage();
    pkg->setName("P");
    auto* parentCls = EcoreFactory::instance().createEClass();
    parentCls->setName("Parent");
    pkg->addEClassifier(parentCls);

    auto* nameAttr = EcoreFactory::instance().createEAttribute();
    nameAttr->setName("name");
    nameAttr->setEAttributeType(EcorePackage::instance().getEDataType_EString());
    nameAttr->setFeatureID(0);
    parentCls->addEStructuralFeature(nameAttr);

    auto* childRef = EcoreFactory::instance().createEReference();
    childRef->setName("child");
    childRef->setContainment(true);
    childRef->setEReferenceType(parentCls);
    childRef->setFeatureID(1);
    childRef->setUpperBound(1);
    parentCls->addEStructuralFeature(childRef);

    auto* root = new DynamicEObject(parentCls);
    root->eSet(nameAttr, std::any(std::string("root")));
    auto* kid = new DynamicEObject(parentCls);
    kid->eSet(nameAttr, std::any(std::string("kid")));
    root->eSet(childRef, std::any((EObject*)kid));

    auto* cp = EcoreUtil::copy(root);
    EXPECT_NOT_NULL(cp);
    auto v = cp->eGet(childRef);
    EObject* cpKid = std::any_cast<EObject*>(v);
    EXPECT_NOT_NULL(cpKid);
    EXPECT_NE(cpKid, kid);
    EXPECT_EQ(std::any_cast<std::string>(cpKid->eGet(nameAttr)), std::string("kid"));
    EXPECT_EQ(cpKid->eContainer(), cp);
    delete root;
    delete kid;
    delete cp;
}

// ===== copyReferences 对未复制的引用目标保持原样 =====
EMF_TEST(Copier_CopyReferences_UnknownTarget_KeepsOriginal) {
    // a.friend 指向 c（不在 Copier 范围内）→ copyReferences 后 aC.friend 仍指向源 c
    EcoreFactory::initialize();
    EcorePackage::initialize();
    Model m;
    auto* a = new DynamicEObject(m.personCls);
    auto* c = new DynamicEObject(m.personCls);
    a->eSet(m.friendRef, std::any((EObject*)c));

    Copier copier;
    auto* aC = copier.copy(a);  // 只复制 a，不复制 c
    copier.copyReferences();
    auto v = aC->eGet(m.friendRef);
    EObject* refTarget = std::any_cast<EObject*>(v);
    // c 未在映射中 → 保持原样指向源 c
    EXPECT_EQ(refTarget, c);
    delete a;
    delete c;
    delete aC;
}
