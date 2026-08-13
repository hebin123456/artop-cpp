// Scoping 测试
// 对齐 Java: org.eclipse.sphinx.emf.scoping.FileResourceScope
//          + org.eclipse.sphinx.emf.scoping.FileResourceScopeProvider
//          + org.eclipse.sphinx.emf.scoping.ResourceScopeProviderRegistry
#include "test_main.h"
#include "emf/sphinx/scoping/FileResourceScope.h"
#include "emf/sphinx/scoping/FileResourceScopeProvider.h"
#include "emf/sphinx/scoping/ResourceScopeProviderRegistry.h"
#include "emf/sphinx/scoping/IResourceScope.h"
#include "emf/common/Resource.h"
#include "emf/common/URI.h"

using emf::sphinx::scoping::FileResourceScope;
using emf::sphinx::scoping::FileResourceScopeProvider;
using emf::sphinx::scoping::ResourceScopeProviderRegistry;
using emf::sphinx::scoping::IResourceScope;
using emf::common::Resource;
using emf::common::URI;

// 测试 1：FileResourceScope 单文件 scope
EMF_TEST(FileResourceScope_BelongsToURI) {
    FileResourceScope scope(URI("file:///tmp/a.xmi"));
    EXPECT_TRUE(scope.belongsTo(URI("file:///tmp/a.xmi"), false));
    EXPECT_FALSE(scope.belongsTo(URI("file:///tmp/b.xmi"), false));
    EXPECT_TRUE(scope.didBelongTo(URI("file:///tmp/a.xmi"), false));
    EXPECT_FALSE(scope.didBelongTo(URI("file:///tmp/b.xmi"), false));
}

// 测试 2：FileResourceScope 根 URI 和持久化文件
EMF_TEST(FileResourceScope_RootAndPersisted) {
    FileResourceScope scope(URI("file:///tmp/a.xmi"));
    EXPECT_EQ(scope.getRootURI().toString(), std::string("file:///tmp/a.xmi"));
    auto persisted = scope.getPersistedFiles(false);
    EXPECT_EQ((int)persisted.size(), 1);
    EXPECT_EQ(persisted[0].toString(), std::string("file:///tmp/a.xmi"));
    // 无引用文件
    EXPECT_EQ((int)scope.getReferencedRootURIs().size(), 0);
    EXPECT_EQ((int)scope.getReferencingRootURIs().size(), 0);
}

// 测试 3：FileResourceScope isShared
EMF_TEST(FileResourceScope_IsShared) {
    FileResourceScope scope(URI("file:///tmp/a.xmi"));
    EXPECT_FALSE(scope.isShared(URI("file:///tmp/a.xmi")));
    EXPECT_FALSE(scope.isShared(static_cast<emf::common::Resource*>(nullptr)));
}

// 测试 4：FileResourceScope belongsTo(Resource*)
EMF_TEST(FileResourceScope_BelongsToResource) {
    FileResourceScope scope(URI("file:///tmp/a.xmi"));
    Resource r(URI("file:///tmp/a.xmi"));
    EXPECT_TRUE(scope.belongsTo(&r, false));
    Resource other(URI("file:///tmp/b.xmi"));
    EXPECT_FALSE(scope.belongsTo(&other, false));
    EXPECT_FALSE(scope.belongsTo(static_cast<emf::common::Resource*>(nullptr), false));
}

// 测试 5：FileResourceScopeProvider 通过 URI 创建
EMF_TEST(FileResourceScopeProvider_ByURI) {
    auto& p = FileResourceScopeProvider::instance();
    auto scope = p.createScope(URI("file:///tmp/a.xmi"));
    EXPECT_NOT_NULL(scope.get());
    EXPECT_EQ(scope->getRootURI().toString(), std::string("file:///tmp/a.xmi"));
}

// 测试 6：FileResourceScopeProvider 通过 Resource
EMF_TEST(FileResourceScopeProvider_ByResource) {
    auto& p = FileResourceScopeProvider::instance();
    Resource r(URI("file:///tmp/a.xmi"));
    auto scope = p.createScope(&r);
    EXPECT_NOT_NULL(scope.get());
    EXPECT_EQ(scope->getRootURI().toString(), std::string("file:///tmp/a.xmi"));
    EXPECT_NULL(p.createScope(static_cast<emf::common::Resource*>(nullptr)).get());
}

// 测试 7：FileResourceScopeProvider 通过 null EObject
EMF_TEST(FileResourceScopeProvider_ByEObject_Null) {
    auto& p = FileResourceScopeProvider::instance();
    EXPECT_NULL(p.createScope(static_cast<emf::common::EObject*>(nullptr)).get());
}

// 测试 8：ResourceScopeProviderRegistry 注册/查找
EMF_TEST(ResourceScopeProviderRegistry_RegisterAndCreate) {
    auto& reg = ResourceScopeProviderRegistry::instance();
    // 简化：直接测试 createScope
    Resource r(URI("file:///tmp/c.xmi"));
    auto scope = reg.createScope(&r);
    // 没有注册任何 provider，所以返回 null
    EXPECT_NULL(scope.get());
    EXPECT_TRUE(reg.isNotInAnyScope(URI("file:///tmp/d.xmi")));
}

// 测试 9：ResourceScopeProviderRegistry 注册后能用
EMF_TEST(ResourceScopeProviderRegistry_RegisterProvider) {
    auto& reg = ResourceScopeProviderRegistry::instance();
    auto& p = FileResourceScopeProvider::instance();
    reg.registerProvider(&p);
    Resource r(URI("file:///tmp/e.xmi"));
    auto scope = reg.createScope(&r);
    EXPECT_NOT_NULL(scope.get());
    EXPECT_EQ(scope->getRootURI().toString(), std::string("file:///tmp/e.xmi"));
    // isNotInAnyScope：注册的 FileResourceScope 与 file:///tmp/e.xmi 相等，应该 false
    EXPECT_FALSE(reg.isNotInAnyScope(URI("file:///tmp/e.xmi")));
    reg.unregisterProvider(&p);
}

// 测试 10：ResourceScopeProviderRegistry 重复注册不增加
EMF_TEST(ResourceScopeProviderRegistry_DuplicateRegister) {
    auto& reg = ResourceScopeProviderRegistry::instance();
    auto& p = FileResourceScopeProvider::instance();
    reg.registerProvider(&p);
    reg.registerProvider(&p);
    reg.unregisterProvider(&p);
    // 注销后应找不到
    Resource r(URI("file:///tmp/f.xmi"));
    EXPECT_NULL(reg.createScope(&r).get());
}
