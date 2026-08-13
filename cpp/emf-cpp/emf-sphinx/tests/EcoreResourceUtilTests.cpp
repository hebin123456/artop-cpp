// EcoreResourceUtil 测试
// 对齐 Java: org.eclipse.sphinx.emf.util.EcoreResourceUtil
#include "test_main.h"
#include "emf/sphinx/util/EcoreResourceUtil.h"
#include "emf/common/Resource.h"
#include "emf/common/URI.h"
#include "emf/common/EObject.h"

using emf::sphinx::util::EcoreResourceUtil;
using emf::common::URI;
using emf::common::Resource;
using emf::common::ResourceSet;
using emf::common::EObject;

// 测试 1：convertToAbsoluteFileURI 相对 URI 转绝对
EMF_TEST(EcoreResourceUtil_ConvertToAbsoluteFileURI) {
    URI rel("relative/path.xmi");
    auto abs = EcoreResourceUtil::convertToAbsoluteFileURI(rel);
    // 简化模式：normalize 后的 URI 应非空
    EXPECT_FALSE(abs.toString().empty());
}

// 测试 2：convertToPlatformResourceURI 简化模式（headless）
EMF_TEST(EcoreResourceUtil_ConvertToPlatformResourceURI) {
    URI uri("file:///tmp/test.xmi");
    auto conv = EcoreResourceUtil::convertToPlatformResourceURI(uri);
    // headless 模式：直接 normalize，结果应非空
    EXPECT_FALSE(conv.toString().empty());
}

// 测试 3：exists 简单判断
EMF_TEST(EcoreResourceUtil_Exists) {
    EXPECT_FALSE(EcoreResourceUtil::exists(URI("file:///nonexistent/path.xmi")));
}

// 测试 4：getURI(null) 返回空 URI
EMF_TEST(EcoreResourceUtil_GetURI_NullObject) {
    auto u = EcoreResourceUtil::getURI(static_cast<EObject*>(nullptr));
    EXPECT_EQ(u.toString(), std::string(""));
}

// 测试 5：normalizeURIFragment null res 返回原 fragment
EMF_TEST(EcoreResourceUtil_NormalizeURIFragment_Null) {
    EXPECT_EQ(EcoreResourceUtil::normalizeURIFragment(nullptr, "foo/bar"), std::string("foo/bar"));
}

// 测试 6：readModelNamespace(null) 返回空
EMF_TEST(EcoreResourceUtil_ReadModelNamespace_Null) {
    EXPECT_EQ(EcoreResourceUtil::readModelNamespace(static_cast<Resource*>(nullptr)), std::string(""));
}

// 测试 7：readTargetNamespace(null) 返回空
EMF_TEST(EcoreResourceUtil_ReadTargetNamespace_Null) {
    EXPECT_EQ(EcoreResourceUtil::readTargetNamespace(static_cast<Resource*>(nullptr)), std::string(""));
}

// 测试 8：readRootElementComments 骨架返回空 vector
EMF_TEST(EcoreResourceUtil_ReadRootElementComments) {
    auto v = EcoreResourceUtil::readRootElementComments(nullptr);
    EXPECT_EQ((int)v.size(), 0);
}

// 测试 9：readSchemaLocationEntries 骨架返回空 map
EMF_TEST(EcoreResourceUtil_ReadSchemaLocationEntries) {
    auto m = EcoreResourceUtil::readSchemaLocationEntries(nullptr);
    EXPECT_EQ((int)m.size(), 0);
}

// 测试 10：getDefaultLoadOptions 至少一个选项
EMF_TEST(EcoreResourceUtil_DefaultLoadOptions) {
    auto m = EcoreResourceUtil::getDefaultLoadOptions();
    EXPECT_TRUE((int)m.size() >= 1);
}

// 测试 11：getDefaultSaveOptions
EMF_TEST(EcoreResourceUtil_DefaultSaveOptions) {
    auto m = EcoreResourceUtil::getDefaultSaveOptions();
    EXPECT_EQ((int)m.size(), 0);
}

// 测试 12：getModelRoot(null) 返回 null
EMF_TEST(EcoreResourceUtil_GetModelRoot_Null) {
    EXPECT_NULL(EcoreResourceUtil::getModelRoot(nullptr));
}

// 测试 13：isResourceLoaded(null) 返回 false
EMF_TEST(EcoreResourceUtil_IsResourceLoaded_Null) {
    EXPECT_FALSE(EcoreResourceUtil::isResourceLoaded(nullptr, URI("file:///tmp/test.xmi")));
}

// 测试 14：getModelName(null) 返回空
EMF_TEST(EcoreResourceUtil_GetModelName_Null) {
    EXPECT_EQ(EcoreResourceUtil::getModelName(nullptr), std::string(""));
}

// 测试 15：loadResource(null) 返回 null
EMF_TEST(EcoreResourceUtil_LoadResource_Null) {
    EXPECT_NULL(EcoreResourceUtil::loadResource(nullptr, URI("file:///tmp/test.xmi"), {}));
}

// 测试 16：loadEObject(null) 返回 null
EMF_TEST(EcoreResourceUtil_LoadEObject_Null) {
    EXPECT_NULL(EcoreResourceUtil::loadEObject(nullptr, URI("file:///tmp/test.xmi")));
}

// 测试 17：getEObject(null) 返回 null
EMF_TEST(EcoreResourceUtil_GetEObject_Null) {
    EXPECT_NULL(EcoreResourceUtil::getEObject(nullptr, URI("file:///tmp/test.xmi")));
}

// 测试 18：addNewModelResource(null) 返回 null
EMF_TEST(EcoreResourceUtil_AddNewModelResource_Null) {
    EXPECT_NULL(EcoreResourceUtil::addNewModelResource(nullptr, URI("file:///tmp/test.xmi"), "xmi", nullptr));
}

// 测试 19：saveModelResource(null) 不崩溃
EMF_TEST(EcoreResourceUtil_SaveModelResource_Null) {
    EcoreResourceUtil::saveModelResource(nullptr, {});
}

// 测试 20：unloadResource(null) 不崩溃
EMF_TEST(EcoreResourceUtil_UnloadResource_Null) {
    EcoreResourceUtil::unloadResource(static_cast<Resource*>(nullptr));
    EcoreResourceUtil::unloadResource(static_cast<ResourceSet*>(nullptr), URI("file:///tmp/test.xmi"));
}
