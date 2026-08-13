// Resource 单元测试
// 对齐 org.eclipse.emf.ecore.resource.Resource (Java) 行为
// 测试 emf::common::Resource（emf/common/Resource.h）基础操作：
//   URI get/set、contents 管理、loaded/modified 标记、errors/warnings、EObject fragment
#include "test_main.h"
#include "emf/common/Resource.h"
#include "emf/common/URI.h"
#include "emf/common/EObject.h"

#include <any>
#include <sstream>
#include <string>
#include <vector>

using emf::common::Resource;
using emf::common::EObject;
using emf::common::URI;

namespace {

// 最小 EObject 桩，仅满足 Resource::addToContents 引用语义（不参与反射）
class StubEObject : public EObject {
public:
    emf::ecore::EClass* eClass() const override { return nullptr; }
    Resource* eResource() const override { return nullptr; }
    EObject* eContainer() const override { return nullptr; }
    const emf::ecore::EStructuralFeature* eContainingFeature() const override { return nullptr; }
    const emf::ecore::EStructuralFeature* eContainmentFeature() const override { return nullptr; }
    std::vector<EObject*> eContents() const override { return {}; }
    emf::common::TreeIterator<EObject*>* eAllContents() const override { return nullptr; }
    bool eIsProxy() const override { return false; }
    std::vector<EObject*> eCrossReferences() const override { return {}; }
    std::any eGet(const emf::ecore::EStructuralFeature*) const override { return std::any{}; }
    std::any eGet(const emf::ecore::EStructuralFeature*, bool) const override { return std::any{}; }
    void eSet(const emf::ecore::EStructuralFeature*, std::any) override {}
    bool eIsSet(const emf::ecore::EStructuralFeature*) const override { return false; }
    void eUnset(const emf::ecore::EStructuralFeature*) override {}
};

}  // namespace

// ===== URI 管理 =====
EMF_TEST(Resource_GetURI_Construction) {
    Resource r(URI("http://example.com/x.mi"));
    EXPECT_EQ(r.getURI().toString(), std::string("http://example.com/x.mi"));
}

EMF_TEST(Resource_SetURI_Updates) {
    Resource r(URI("http://a/b.mi"));
    r.setURI(URI("http://c/d.mi"));
    EXPECT_EQ(r.getURI().toString(), std::string("http://c/d.mi"));
}

EMF_TEST(Resource_DefaultURI_Empty) {
    Resource r(URI(""));
    EXPECT_TRUE(r.getURI().isEmpty());
}

// ===== contents 管理 =====
EMF_TEST(Resource_DefaultContents_Empty) {
    Resource r(URI("u"));
    EXPECT_TRUE(r.getContents().empty());
    EXPECT_EQ(r.getContents().size(), (size_t)0);
}

EMF_TEST(Resource_AddToContents_Appends) {
    Resource r(URI("u"));
    StubEObject o1, o2;
    r.addToContents(&o1);
    r.addToContents(&o2);
    EXPECT_EQ(r.getContents().size(), (size_t)2);
    EXPECT_EQ(r.getContents()[0], &o1);
    EXPECT_EQ(r.getContents()[1], &o2);
}

EMF_TEST(Resource_AddToContents_NullPointer) {
    // addToContents 不做非空校验（与 Java 不完全一致，但保持当前 C++ 行为）
    Resource r(URI("u"));
    r.addToContents(nullptr);
    EXPECT_EQ(r.getContents().size(), (size_t)1);
    EXPECT_NULL(r.getContents()[0]);
}

// ===== root 管理 =====
EMF_TEST(Resource_DefaultRoot_Null) {
    Resource r(URI("u"));
    EXPECT_NULL(r.getRoot());
}

EMF_TEST(Resource_SetRoot_ReturnsSame) {
    Resource r(URI("u"));
    StubEObject o;
    r.setRoot(&o);
    EXPECT_EQ(r.getRoot(), &o);
}

// ===== ResourceSet 关联 =====
EMF_TEST(Resource_DefaultResourceSet_Null) {
    Resource r(URI("u"));
    EXPECT_NULL(r.getResourceSet());
}

EMF_TEST(Resource_SetResourceSet_ReturnsSame) {
    Resource r(URI("u"));
    r.setResourceSet(reinterpret_cast<emf::common::ResourceSet*>(0x1234));
    EXPECT_NOT_NULL(r.getResourceSet());
    r.setResourceSet(nullptr);
    EXPECT_NULL(r.getResourceSet());
}

// ===== isLoaded / setLoaded =====
EMF_TEST(Resource_DefaultNotLoaded) {
    Resource r(URI("u"));
    EXPECT_FALSE(r.isLoaded());
}

EMF_TEST(Resource_SetLoaded_True) {
    Resource r(URI("u"));
    r.setLoaded(true);
    EXPECT_TRUE(r.isLoaded());
    r.setLoaded(false);
    EXPECT_FALSE(r.isLoaded());
}

// ===== isModified / setModified =====
EMF_TEST(Resource_DefaultNotModified) {
    Resource r(URI("u"));
    EXPECT_FALSE(r.isModified());
}

EMF_TEST(Resource_SetModified_True) {
    Resource r(URI("u"));
    r.setModified(true);
    EXPECT_TRUE(r.isModified());
    r.setModified(false);
    EXPECT_FALSE(r.isModified());
}

// ===== errors / warnings 列表 =====
EMF_TEST(Resource_DefaultEmptyErrorsAndWarnings) {
    Resource r(URI("u"));
    EXPECT_TRUE(r.getErrors().empty());
    EXPECT_TRUE(r.getWarnings().empty());
}

EMF_TEST(Resource_AddError_Persists) {
    Resource r(URI("u"));
    r.getErrors().push_back("bad element");
    r.getErrors().push_back("missing attr");
    EXPECT_EQ(r.getErrors().size(), (size_t)2);
    EXPECT_EQ(r.getErrors()[0], std::string("bad element"));
    EXPECT_EQ(r.getErrors()[1], std::string("missing attr"));
}

EMF_TEST(Resource_AddWarning_Persists) {
    Resource r(URI("u"));
    r.getWarnings().push_back("deprecated");
    EXPECT_EQ(r.getWarnings().size(), (size_t)1);
    EXPECT_EQ(r.getWarnings()[0], std::string("deprecated"));
}

// ===== save/load 默认空实现（不抛、不写）=====
EMF_TEST(Resource_SaveLoad_Stream_DefaultsNoThrow) {
    Resource r(URI("u"));
    std::ostringstream oss;
    r.save(oss);  // 默认空实现，不应抛
    EXPECT_TRUE(oss.str().empty());

    std::istringstream iss("anything");
    r.load(iss);  // 默认空实现，不应抛
    EXPECT_FALSE(r.isLoaded());  // 基类不修改 isLoaded
}

// ===== toXmiString / fromXmiString 委托 save/load =====
EMF_TEST(Resource_ToXmiString_DelegatesSave) {
    Resource r(URI("u"));
    std::string s = r.toXmiString();
    EXPECT_TRUE(s.empty());  // 基类 save 默认空实现
}

EMF_TEST(Resource_FromXmiString_DelegatesLoad_NoThrow) {
    Resource r(URI("u"));
    r.fromXmiString("<x/>");
    // 基类 load 默认空实现，不修改 isLoaded
    EXPECT_FALSE(r.isLoaded());
}

// ===== getEObject / getURIFragment 默认实现（无内容时返回空/null）=====
EMF_TEST(Resource_GetEObject_EmptyFragment) {
    Resource r(URI("u"));
    // 无内容，fragment 查找返回 nullptr
    EObject* got = r.getEObject("");
    EXPECT_NULL(got);
}

EMF_TEST(Resource_GetURIFragment_NullObject) {
    Resource r(URI("u"));
    // 对齐 Java：getURIFragment(null) 返回空字符串
    std::string frag = r.getURIFragment(nullptr);
    EXPECT_TRUE(frag.empty());
}

// ===== file:// URI 路径解析（load() 抛错路径）=====
EMF_TEST(Resource_Load_NonexistentFile_Throws) {
    Resource r(URI("file:///nonexistent/path/does/not/exist.mi"));
    EXPECT_THROWS(r.load());
}

EMF_TEST(Resource_Load_NonexistentFile_Save_Throws) {
    Resource r(URI("file:///nonexistent/path/does/not/exist.mi"));
    EXPECT_THROWS(r.save());
}

// ===== URI 类型影响 load 行为 =====
EMF_TEST(Resource_IsFile_FileScheme) {
    URI u("file:///tmp/x.mi");
    EXPECT_TRUE(u.isFile());
}

EMF_TEST(Resource_IsFile_PlatformScheme_NotFile) {
    URI u("platform:/resource/proj/x.mi");
    EXPECT_FALSE(u.isFile());
}
