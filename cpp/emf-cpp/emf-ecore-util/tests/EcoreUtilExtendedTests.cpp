// EcoreUtil 扩展单元测试
// 对齐 org.eclipse.emf.ecore.util.EcoreUtil (Java) 的扩展静态方法
// 覆盖：getAllContents / remove / copy / copyAll / resolve / resolveAll
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
using emf::ecore::DynamicEObject;
using emf::ecore::util::EcoreUtil;

namespace {

// Tree: Parent { child: Child (containment, single-valued) }
// Parent 还有一个 name(EString) attribute
struct TreeModel {
    EClass* parentCls;
    EClass* childCls;
    EReference* childRef;  // containment, single-valued
    EAttribute* nameAttr;

    TreeModel() {
        parentCls = EcoreFactory::instance().createEClass();
        parentCls->setName("Parent");

        nameAttr = EcoreFactory::instance().createEAttribute();
        nameAttr->setName("name");
        nameAttr->setEAttributeType(EcorePackage::instance().getEDataType_EString());
        nameAttr->setFeatureID(0);
        parentCls->addEStructuralFeature(nameAttr);

        childCls = EcoreFactory::instance().createEClass();
        childCls->setName("Child");
        auto* childName = EcoreFactory::instance().createEAttribute();
        childName->setName("cname");
        childName->setEAttributeType(EcorePackage::instance().getEDataType_EString());
        childName->setFeatureID(10);
        childCls->addEStructuralFeature(childName);

        childRef = EcoreFactory::instance().createEReference();
        childRef->setName("child");
        childRef->setContainment(true);
        childRef->setEReferenceType(childCls);
        childRef->setFeatureID(1);
        childRef->setUpperBound(1);
        parentCls->addEStructuralFeature(childRef);
    }
};

}  // namespace

// ===== getAllContents =====
EMF_TEST(EcoreUtil_GetAllContents_NullObject_Empty) {
    auto it = EcoreUtil::getAllContents(nullptr);
    EXPECT_FALSE(it->hasNext());
}

EMF_TEST(EcoreUtil_GetAllContents_NoChildren_Empty) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    TreeModel m;
    auto* parent = new DynamicEObject(m.parentCls);
    auto it = EcoreUtil::getAllContents(parent);
    // 对齐 Java getAllContents：不含 root 本身，只遍历内容
    EXPECT_FALSE(it->hasNext());
    delete parent;
}

EMF_TEST(EcoreUtil_GetAllContents_SingleChild) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    TreeModel m;
    auto* parent = new DynamicEObject(m.parentCls);
    auto* child = new DynamicEObject(m.childCls);
    parent->eSet(m.childRef, std::any((EObject*)child));
    auto it = EcoreUtil::getAllContents(parent);
    EXPECT_TRUE(it->hasNext());
    EObject* first = it->next();
    EXPECT_EQ(first, child);
    EXPECT_FALSE(it->hasNext());
    delete parent;
    delete child;
}

EMF_TEST(EcoreUtil_GetAllContents_NestedDepthFirst) {
    // parent → child1 → grandchild
    EcoreFactory::initialize();
    EcorePackage::initialize();
    TreeModel m;
    auto* parent = new DynamicEObject(m.parentCls);
    auto* child1 = new DynamicEObject(m.parentCls);  // 复用 parentCls 也有 childRef
    auto* grandchild = new DynamicEObject(m.childCls);
    parent->eSet(m.childRef, std::any((EObject*)child1));
    child1->eSet(m.childRef, std::any((EObject*)grandchild));

    auto it = EcoreUtil::getAllContents(parent);
    int count = 0;
    EObject* first = nullptr;
    EObject* second = nullptr;
    while (it->hasNext()) {
        if (count == 0) first = it->next();
        else if (count == 1) second = it->next();
        else it->next();
        ++count;
    }
    EXPECT_EQ(count, 2);
    // 深度优先：先 child1，再 grandchild
    EXPECT_EQ(first, child1);
    EXPECT_EQ(second, grandchild);
    delete parent;
    delete child1;
    delete grandchild;
}

// ===== remove =====
EMF_TEST(EcoreUtil_Remove_NullObject_NoCrash) {
    EcoreUtil::remove(nullptr);  // 不应崩溃
}

EMF_TEST(EcoreUtil_Remove_SingleValuedContainment) {
    // parent.child = child；remove(child) → parent.child 变 nullptr
    EcoreFactory::initialize();
    EcorePackage::initialize();
    TreeModel m;
    auto* parent = new DynamicEObject(m.parentCls);
    auto* child = new DynamicEObject(m.childCls);
    parent->eSet(m.childRef, std::any((EObject*)child));
    EXPECT_EQ(std::any_cast<EObject*>(parent->eGet(m.childRef)), child);

    EcoreUtil::remove(child);
    // 移除后 parent 的 child feature 应为 null
    auto v = parent->eGet(m.childRef);
    EObject* remaining = std::any_cast<EObject*>(v);
    EXPECT_NULL(remaining);
    delete parent;
    delete child;
}

EMF_TEST(EcoreUtil_Remove_RootObject_NoContainer_NoCrash) {
    // 无 eContainer、无 Resource：remove 不做任何事，不崩溃
    EcoreFactory::initialize();
    EcorePackage::initialize();
    TreeModel m;
    auto* parent = new DynamicEObject(m.parentCls);
    EcoreUtil::remove(parent);  // 不应崩溃
    delete parent;
}

// ===== copy =====
EMF_TEST(EcoreUtil_Copy_NullObject_Null) {
    EXPECT_NULL(EcoreUtil::copy(nullptr));
}

EMF_TEST(EcoreUtil_Copy_PreservesAttributeValues) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    TreeModel m;
    // 把 parentCls 放进一个 EPackage，让 Copier 能拿到 factory
    auto* pkg = EcoreFactory::instance().createEPackage();
    pkg->setName("P");
    pkg->addEClassifier(m.parentCls);
    pkg->addEClassifier(m.childCls);

    auto* parent = new DynamicEObject(m.parentCls);
    parent->eSet(m.nameAttr, std::any(std::string("root")));
    auto* cp = EcoreUtil::copy(parent);
    EXPECT_NOT_NULL(cp);
    EXPECT_NE(cp, parent);
    EXPECT_EQ(cp->eClass(), m.parentCls);
    auto v = cp->eGet(m.nameAttr);
    EXPECT_EQ(std::any_cast<std::string>(v), std::string("root"));
    delete parent;
    delete cp;
}

EMF_TEST(EcoreUtil_Copy_DeepCopyContainment) {
    // 拷贝 parent（含 child containment）→ 副本应包含 child 的副本，且 child.eContainer 指向副本
    EcoreFactory::initialize();
    EcorePackage::initialize();
    TreeModel m;
    auto* pkg = EcoreFactory::instance().createEPackage();
    pkg->setName("P");
    pkg->addEClassifier(m.parentCls);
    pkg->addEClassifier(m.childCls);

    auto* parent = new DynamicEObject(m.parentCls);
    parent->eSet(m.nameAttr, std::any(std::string("root")));
    auto* child = new DynamicEObject(m.childCls);
    child->eSet(m.childCls->getEStructuralFeature("cname"),
                std::any(std::string("c1")));
    parent->eSet(m.childRef, std::any((EObject*)child));

    auto* cp = EcoreUtil::copy(parent);
    EXPECT_NOT_NULL(cp);
    auto childV = cp->eGet(m.childRef);
    EObject* cpChild = std::any_cast<EObject*>(childV);
    EXPECT_NOT_NULL(cpChild);
    EXPECT_NE(cpChild, child);  // 副本是新对象
    // child 副本的 eContainer 应指向 parent 副本
    EXPECT_EQ(cpChild->eContainer(), cp);
    // child 副本名称保持
    auto cnameV = cpChild->eGet(m.childCls->getEStructuralFeature("cname"));
    EXPECT_EQ(std::any_cast<std::string>(cnameV), std::string("c1"));
    delete parent;
    delete child;
    delete cp;
}

EMF_TEST(EcoreUtil_Copy_DistinctInstance) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    TreeModel m;
    auto* pkg = EcoreFactory::instance().createEPackage();
    pkg->setName("P");
    pkg->addEClassifier(m.parentCls);

    auto* parent = new DynamicEObject(m.parentCls);
    auto* cp = EcoreUtil::copy(parent);
    EXPECT_NE(cp, parent);
    delete parent;
    delete cp;
}

// ===== copyAll =====
EMF_TEST(EcoreUtil_CopyAll_EmptyInput_Empty) {
    std::vector<EObject*> in;
    auto out = EcoreUtil::copyAll(in);
    EXPECT_TRUE(out.empty());
}

EMF_TEST(EcoreUtil_CopyAll_MultipleObjects) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    TreeModel m;
    auto* pkg = EcoreFactory::instance().createEPackage();
    pkg->setName("P");
    pkg->addEClassifier(m.parentCls);

    auto* a = new DynamicEObject(m.parentCls);
    a->eSet(m.nameAttr, std::any(std::string("a")));
    auto* b = new DynamicEObject(m.parentCls);
    b->eSet(m.nameAttr, std::any(std::string("b")));

    auto out = EcoreUtil::copyAll({a, b});
    EXPECT_EQ(out.size(), (size_t)2);
    EXPECT_NE(out[0], a);
    EXPECT_NE(out[1], b);
    EXPECT_EQ(std::any_cast<std::string>(out[0]->eGet(m.nameAttr)), std::string("a"));
    EXPECT_EQ(std::any_cast<std::string>(out[1]->eGet(m.nameAttr)), std::string("b"));
    delete a;
    delete b;
    for (auto* o : out) delete o;
}

// ===== resolve / resolveAll =====
EMF_TEST(EcoreUtil_Resolve_NullProxy_Null) {
    EXPECT_NULL(EcoreUtil::resolve(nullptr, nullptr));
}

EMF_TEST(EcoreUtil_Resolve_NonProxy_ReturnsSame) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    TreeModel m;
    auto* obj = new DynamicEObject(m.parentCls);
    // 非 proxy → resolve 直接返回原对象
    EXPECT_EQ(EcoreUtil::resolve(obj, nullptr), obj);
    delete obj;
}

EMF_TEST(EcoreUtil_Resolve_ProxyNoResourceSet_ReturnsProxy) {
    // proxy + 无 ResourceSet → 无法解析，返回 proxy 本身
    EcoreFactory::initialize();
    EcorePackage::initialize();
    TreeModel m;
    auto* pkg = EcoreFactory::instance().createEPackage();
    pkg->setName("P");
    pkg->addEClassifier(m.parentCls);
    auto* obj = new DynamicEObject(m.parentCls);
    // 标记为 proxy
    auto* impl = dynamic_cast<emf::common::EObjectImpl*>(obj);
    EXPECT_NOT_NULL(impl);
    impl->eSetProxyURI(emf::common::URI("http://x/y#//p"));
    EXPECT_TRUE(obj->eIsProxy());
    // 无 ResourceSet → 返回 proxy
    EObject* r = EcoreUtil::resolve(obj, nullptr);
    EXPECT_EQ(r, obj);
    delete obj;
}

EMF_TEST(EcoreUtil_ResolveAll_NonProxy_NoCrash) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    TreeModel m;
    auto* parent = new DynamicEObject(m.parentCls);
    auto* child = new DynamicEObject(m.childCls);
    parent->eSet(m.childRef, std::any((EObject*)child));
    EcoreUtil::resolveAll(parent);  // 无 proxy，不应崩溃
    delete parent;
    delete child;
}

EMF_TEST(EcoreUtil_ResolveAll_NullObject_NoCrash) {
    EcoreUtil::resolveAll(nullptr);  // 不应崩溃
}

// ===== AllContentsIterator 直接使用 =====
EMF_TEST(AllContentsIterator_EmptyOnNullRoot) {
    emf::ecore::util::AllContentsIterator it(nullptr);
    EXPECT_FALSE(it.hasNext());
    EXPECT_NULL(it.next());
}

EMF_TEST(AllContentsIterator_SingleChild) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    TreeModel m;
    auto* parent = new DynamicEObject(m.parentCls);
    auto* child = new DynamicEObject(m.childCls);
    parent->eSet(m.childRef, std::any((EObject*)child));
    emf::ecore::util::AllContentsIterator it(parent);
    EXPECT_TRUE(it.hasNext());
    EXPECT_EQ(it.next(), child);
    EXPECT_FALSE(it.hasNext());
    delete parent;
    delete child;
}
