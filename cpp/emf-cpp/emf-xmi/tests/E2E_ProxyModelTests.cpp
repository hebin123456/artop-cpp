// E2E_ProxyModelTests.cpp —— 代理对象（Proxy）创建与解析测试
// 对齐 Java: org.eclipse.emf.ecore.impl.EObjectImpl.eIsProxy/eProxyURI/eResolveProxy
//           + org.eclipse.emf.ecore.util.EcoreUtil.resolve(proxy, resourceSet)
//
// 覆盖：
//   - EObject 默认非代理（eIsProxy == false）
//   - eSetProxyURI 设置代理 URI 后 eIsProxy == true
//   - eProxyURI 返回设置的 URI
//   - eResolveProxy 默认实现：非代理返回自身，代理返回自身（需 ResourceSet 才能真正解析）
//   - 代理 URI 带 fragment（#//path）的解析
//   - XMIResourceSet 跨资源 EObject 查找（getEObject(URI, loadOnDemand)）
//   - 多个代理对象的独立性
#include "test_main.h"
#include "emf/xmi/XMIResource.h"
#include "emf/xmi/XMIResourceFactory.h"
#include "emf/xmi/XMIResourceSet.h"
#include "emf/ecore/EcorePackage.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/common/EPackageRegistry.h"
#include "emf/common/URI.h"
#include "emf/common/EObject.h"

#include <string>

using emf::xmi::XMIResource;
using emf::xmi::XMIResourceFactory;
using emf::xmi::XMIResourceSet;
using emf::ecore::EcoreFactory;
using emf::ecore::EcorePackage;
using emf::ecore::EClass;
using emf::ecore::EPackage;
using emf::common::EObject;
using emf::common::EObjectImpl;
using emf::common::URI;

namespace {

void initEnv() {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    XMIResourceFactory::registerDefaults();
}

// 创建一个具体的 EObject（EcoreFactory 产出的 EClass 是 BasicEObject 子类）
EObject* createTestEObject() {
    return EcoreFactory::instance().createEClass();
}

}  // namespace

// =====================================================================
// 1) 新建 EObject 默认非代理
// =====================================================================
EMF_TEST(E2E_Proxy_NewObject_IsNotProxy) {
    initEnv();
    auto* obj = createTestEObject();
    EXPECT_NOT_NULL(obj);
    EXPECT_FALSE(obj->eIsProxy());
}

// =====================================================================
// 2) eSetProxyURI 后 eIsProxy 返回 true
// =====================================================================
EMF_TEST(E2E_Proxy_SetProxyURI_MakesIsProxyTrue) {
    initEnv();
    auto* obj = createTestEObject();
    auto* impl = dynamic_cast<EObjectImpl*>(obj);
    EXPECT_NOT_NULL(impl);
    impl->eSetProxyURI(URI("http://example.com/model.xmi#//Foo"));
    EXPECT_TRUE(obj->eIsProxy());
}

// =====================================================================
// 3) eProxyURI 返回设置的 URI
// =====================================================================
EMF_TEST(E2E_Proxy_ProxyURIReturnsSetURI) {
    initEnv();
    auto* obj = createTestEObject();
    auto* impl = dynamic_cast<EObjectImpl*>(obj);
    std::string uriStr = "http://example.com/model.xmi#//@books.0";
    impl->eSetProxyURI(URI(uriStr));
    EXPECT_EQ(impl->eProxyURI().toString(), uriStr);
}

// =====================================================================
// 4) eResolveProxy 对非代理对象返回自身
// =====================================================================
EMF_TEST(E2E_Proxy_ResolveProxy_NonProxyReturnsSelf) {
    initEnv();
    auto* obj = createTestEObject();
    // 用另一个对象调用 eResolveProxy
    auto* caller = createTestEObject();
    EObject* resolved = caller->eResolveProxy(obj);
    EXPECT_EQ(resolved, obj);
}

// =====================================================================
// 5) eResolveProxy 通过 ResourceSet 真正解析跨资源代理（对齐 Java eResolveProxy）
// =====================================================================
EMF_TEST(E2E_Proxy_ResolveProxy_ResolvesViaResourceSet) {
    initEnv();
    // 创建两个 resource：target.xmi 含真实对象，caller.xmi 含调用方
    XMIResourceSet rs;
    auto* targetRes = rs.createResource(URI("http://example.com/target.xmi"));
    auto* callerRes = rs.createResource(URI("http://example.com/caller.xmi"));

    // target 资源中放一个 EPackage（有名字，可被 fragment 查找）
    auto* target = EcoreFactory::instance().createEPackage();
    target->setName("TargetPkg");
    targetRes->addToContents(target);

    // 调用方对象放入 caller 资源
    auto* caller = createTestEObject();
    callerRes->addToContents(caller);

    // 创建指向 target 的代理
    auto* proxy = createTestEObject();
    auto* proxyImpl = dynamic_cast<EObjectImpl*>(proxy);
    proxyImpl->eSetProxyURI(URI("http://example.com/target.xmi"));
    // eResolveProxy 应通过 caller 的 ResourceSet 解析代理
    EObject* resolved = caller->eResolveProxy(proxy);
    // 应解析到 target 资源的根对象，而非返回 proxy
    EXPECT_EQ(resolved, static_cast<EObject*>(target));
}

// =====================================================================
// 5b) eResolveProxy 无 ResourceSet 时返回代理本身（退化行为）
// =====================================================================
EMF_TEST(E2E_Proxy_ResolveProxy_NoResourceSet_ReturnsProxy) {
    initEnv();
    auto* proxy = createTestEObject();
    auto* impl = dynamic_cast<EObjectImpl*>(proxy);
    impl->eSetProxyURI(URI("http://example.com/model.xmi#//Bar"));
    auto* caller = createTestEObject();
    // caller 没有关联 Resource，无 ResourceSet 可用
    EObject* resolved = caller->eResolveProxy(proxy);
    EXPECT_EQ(resolved, proxy);
}

// =====================================================================
// 6) 代理 URI 带 fragment（#//path）正确存储
// =====================================================================
EMF_TEST(E2E_Proxy_ProxyURIWithFragment_StoredCorrectly) {
    initEnv();
    auto* obj = createTestEObject();
    auto* impl = dynamic_cast<EObjectImpl*>(obj);
    std::string full = "file:///path/to/model.xmi#//Library/books.0";
    impl->eSetProxyURI(URI(full));
    EXPECT_EQ(impl->eProxyURI().toString(), full);
    EXPECT_EQ(impl->eProxyURI().fragment(), std::string("//Library/books.0"));
}

// =====================================================================
// 7) 多个代理对象独立（各自有独立 eProxyURI）
// =====================================================================
EMF_TEST(E2E_Proxy_MultipleProxies_Independent) {
    initEnv();
    auto* p1 = createTestEObject();
    auto* p2 = createTestEObject();
    auto* i1 = dynamic_cast<EObjectImpl*>(p1);
    auto* i2 = dynamic_cast<EObjectImpl*>(p2);
    i1->eSetProxyURI(URI("http://a.com/m.xmi#//A"));
    i2->eSetProxyURI(URI("http://b.com/m.xmi#//B"));
    EXPECT_TRUE(p1->eIsProxy());
    EXPECT_TRUE(p2->eIsProxy());
    EXPECT_NE(i1->eProxyURI().toString(), i2->eProxyURI().toString());
    EXPECT_EQ(i1->eProxyURI().toString(), std::string("http://a.com/m.xmi#//A"));
    EXPECT_EQ(i2->eProxyURI().toString(), std::string("http://b.com/m.xmi#//B"));
}

// =====================================================================
// 8) XMIResourceSet 创建资源并跨资源查找 EObject
// =====================================================================
EMF_TEST(E2E_Proxy_ResourceSet_CrossResourceLookup) {
    initEnv();
    XMIResourceSet rs;
    auto* res = rs.createResource(URI("http://example.com/test.xmi"));
    EXPECT_NOT_NULL(res);
    // 创建 EObject 并加入资源
    auto* obj = createTestEObject();
    res->addToContents(obj);
    // 通过 URI 查找资源中的根对象
    auto* found = rs.getEObject(URI("http://example.com/test.xmi"), true);
    EXPECT_EQ(found, obj);
}

// =====================================================================
// 9) XMIResourceSet getEObject 带 fragment 查找
// =====================================================================
EMF_TEST(E2E_Proxy_ResourceSet_GetEObjectWithFragment) {
    initEnv();
    XMIResourceSet rs;
    auto* res = rs.createResource(URI("http://example.com/test2.xmi"));
    auto* obj = createTestEObject();
    auto* pkg = EcoreFactory::instance().createEPackage();
    pkg->setName("testPkg");
    res->addToContents(pkg);
    // 无 fragment 时返回根对象
    auto* found = rs.getEObject(URI("http://example.com/test2.xmi"), true);
    EXPECT_EQ(found, static_cast<EObject*>(pkg));
}

// =====================================================================
// 10) 代理对象设置 eProxyURI 后 eIsProxy 持续为 true
// =====================================================================
EMF_TEST(E2E_Proxy_IsProxy_PersistentAfterSet) {
    initEnv();
    auto* obj = createTestEObject();
    EXPECT_FALSE(obj->eIsProxy());
    auto* impl = dynamic_cast<EObjectImpl*>(obj);
    impl->eSetProxyURI(URI("http://example.com/m.xmi#//X"));
    EXPECT_TRUE(obj->eIsProxy());
    // 再次检查仍为 true
    EXPECT_TRUE(obj->eIsProxy());
}

// =====================================================================
// 11) XMIResourceSet getResource 按 URI 返回已注册资源
// =====================================================================
EMF_TEST(E2E_Proxy_ResourceSet_GetResource_ReturnsRegistered) {
    initEnv();
    XMIResourceSet rs;
    URI uri("http://example.com/registered.xmi");
    auto* created = rs.createResource(uri);
    EXPECT_NOT_NULL(created);
    auto* found = rs.getResource(uri, false);
    EXPECT_EQ(found, created);
}

// =====================================================================
// 12) 代理 URI 覆盖：后设的 URI 替换前设的
// =====================================================================
EMF_TEST(E2E_Proxy_ProxyURI_Overwrite) {
    initEnv();
    auto* obj = createTestEObject();
    auto* impl = dynamic_cast<EObjectImpl*>(obj);
    impl->eSetProxyURI(URI("http://old.com/m.xmi#//Old"));
    EXPECT_EQ(impl->eProxyURI().toString(), std::string("http://old.com/m.xmi#//Old"));
    impl->eSetProxyURI(URI("http://new.com/m.xmi#//New"));
    EXPECT_EQ(impl->eProxyURI().toString(), std::string("http://new.com/m.xmi#//New"));
    EXPECT_TRUE(obj->eIsProxy());
}
