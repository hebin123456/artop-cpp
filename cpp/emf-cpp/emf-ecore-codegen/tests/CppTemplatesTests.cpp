// CppTemplatesTests.cpp —— 单元测试：CppTemplates（JET 模板的 C++ 实现）
// 对应 Java: org.eclipse.emf.codegen.ecore.templates.model.* (Class.java, FactoryClass.java, ...)
#include "test_main.h"
#include "test_helpers.h"

#include "emf/ecore/codegen/CppTemplates.h"
#include "emf/ecore/codegen/GenModel.h"
#include "emf/ecore/codegen/GenModelLoader.h"
#include "emf/ecore/EcorePackage.h"
#include "emf/ecore/EcoreImpls.h"

#include <cstdlib>
#include <filesystem>
#include <fstream>
#include <memory>
#include <string>

using namespace emf;
using namespace emf::ecore;
using namespace emf::ecore::codegen;

namespace {

std::shared_ptr<GenModel> buildSampleGenModel() {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* pkg = EcoreFactory::instance().createEPackage();
    pkg->setName("library");
    pkg->setNsURI("http://example.com/library/1.0");
    pkg->setNsPrefix("library");

    auto* library = EcoreFactory::instance().createEClass();
    library->setName("Library");
    auto* name = EcoreFactory::instance().createEAttribute();
    name->setName("name");
    name->setEAttributeType(EcorePackage::instance().getEDataType_EString());
    library->addEStructuralFeature(name);

    auto* book = EcoreFactory::instance().createEClass();
    book->setName("Book");
    auto* title = EcoreFactory::instance().createEAttribute();
    title->setName("title");
    title->setEAttributeType(EcorePackage::instance().getEDataType_EString());
    book->addEStructuralFeature(title);
    auto* pages = EcoreFactory::instance().createEAttribute();
    pages->setName("pages");
    pages->setEAttributeType(EcorePackage::instance().getEDataType_EInt());
    book->addEStructuralFeature(pages);

    pkg->addEClassifier(library);
    pkg->addEClassifier(book);
    return GenModelLoader::wrapEcore(pkg, "emf");
}

}  // namespace

// ===== 1. renderTemplate 基本替换 =====
EMF_TEST(CppTemplates_renderTemplate_basicSubstitution) {
    std::string tpl = "Hello {{name}}, you are {{age}} years old.";
    std::map<std::string, std::string> v = { {"name", "Alice"}, {"age", "30"} };
    auto out = renderTemplate(tpl, v);
    EXPECT_EQ(out, std::string("Hello Alice, you are 30 years old."));
}

// ===== 2. renderTemplate 未知占位符保留 =====
EMF_TEST(CppTemplates_renderTemplate_unknownKeptAsIs) {
    std::string tpl = "a={{a}}, b={{b}}, c={{c}}";
    std::map<std::string, std::string> v = { {"a", "1"} };
    auto out = renderTemplate(tpl, v);
    EXPECT_TRUE(out.find("a=1") != std::string::npos);
    EXPECT_TRUE(out.find("{{b}}") != std::string::npos);
    EXPECT_TRUE(out.find("{{c}}") != std::string::npos);
}

// ===== 3. emitPackageHeader: 包含 GenPackage/Class/feature 名字 =====
EMF_TEST(CppTemplates_emitPackageHeader_containsClassNames) {
    auto gm = buildSampleGenModel();
    TemplateContext ctx; ctx.genModel = gm.get(); ctx.genPackage = gm->genPackages[0].get();
    auto h = emitPackageHeader(ctx);
    EXPECT_TRUE(h.find("class LibraryPackage") != std::string::npos);
    EXPECT_TRUE(h.find("static const int LIBRARY = 0") != std::string::npos);
    EXPECT_TRUE(h.find("static const int BOOK = 1") != std::string::npos);
    EXPECT_TRUE(h.find("getLibrary_Name()") != std::string::npos);
    EXPECT_TRUE(h.find("getBook_Title()") != std::string::npos);
    EXPECT_TRUE(h.find("Library_name_attr_") != std::string::npos);
    EXPECT_TRUE(h.find("Book_title_attr_") != std::string::npos);
}

// ===== 4. emitPackageSource: 含 eNS_URI 字面量 + ecore 类型 accessor =====
EMF_TEST(CppTemplates_emitPackageSource_containsNsURIAndEcoreTypes) {
    auto gm = buildSampleGenModel();
    TemplateContext ctx; ctx.genModel = gm.get(); ctx.genPackage = gm->genPackages[0].get();
    auto src = emitPackageSource(ctx);
    EXPECT_TRUE(src.find("LibraryPackage::eNS_URI") != std::string::npos);
    EXPECT_TRUE(src.find("\"http://example.com/library/1.0\"") != std::string::npos);
    EXPECT_TRUE(src.find("LibraryPackage::eINSTANCE = nullptr") != std::string::npos);
    EXPECT_TRUE(src.find("getEDataType_EString()") != std::string::npos);
    EXPECT_TRUE(src.find("getEDataType_EInt()") != std::string::npos);
    EXPECT_TRUE(src.find("EPackageRegistry::instance().put") != std::string::npos);
}

// ===== 5. emitFactoryHeader/Source: create<X> 方法 =====
EMF_TEST(CppTemplates_emitFactory_methodsForEachClass) {
    auto gm = buildSampleGenModel();
    TemplateContext ctx; ctx.genModel = gm.get(); ctx.genPackage = gm->genPackages[0].get();
    auto h = emitFactoryHeader(ctx);
    EXPECT_TRUE(h.find("class LibraryFactory") != std::string::npos);
    EXPECT_TRUE(h.find("createLibrary()") != std::string::npos);
    EXPECT_TRUE(h.find("createBook()") != std::string::npos);
    auto src = emitFactorySource(ctx);
    // 单类单继承方案：无 Impl 后缀
    EXPECT_TRUE(src.find("#include \"Library.h\"") != std::string::npos);
    EXPECT_TRUE(src.find("#include \"Book.h\"") != std::string::npos);
    EXPECT_TRUE(src.find("return new Library()") != std::string::npos);
    EXPECT_TRUE(src.find("return new Book()") != std::string::npos);
}

// ===== 6. emitSwitchHeader: doSwitch + 每个 case =====
EMF_TEST(CppTemplates_emitSwitch_hasCaseForEachClass) {
    auto gm = buildSampleGenModel();
    TemplateContext ctx; ctx.genModel = gm.get(); ctx.genPackage = gm->genPackages[0].get();
    auto h = emitSwitchHeader(ctx);
    EXPECT_TRUE(h.find("class LibrarySwitch") != std::string::npos);
    EXPECT_TRUE(h.find("T doSwitch") != std::string::npos);
    EXPECT_TRUE(h.find("caseLibrary(Library* object)") != std::string::npos);
    EXPECT_TRUE(h.find("caseBook(Book* object)") != std::string::npos);
    EXPECT_TRUE(h.find("dynamic_cast<Library*>") != std::string::npos);
    EXPECT_TRUE(h.find("dynamic_cast<Book*>") != std::string::npos);
}

// ===== 7. emitInterfaceHeader: 现在输出统一类 <ClassName>.h（继承 EObjectImpl）=====
EMF_TEST(CppTemplates_emitInterfaceHeader_attributeAccessors) {
    auto gm = buildSampleGenModel();
    TemplateContext ctx;
    ctx.genModel = gm.get();
    ctx.genPackage = gm->genPackages[0].get();
    ctx.genClass = gm->genPackages[0]->genClasses[1].get();  // Book
    auto h = emitInterfaceHeader(ctx);
    // 单类单继承方案：class Book 直接继承 EObjectImpl，无 Impl 后缀
    // S1: accessor 不覆盖基类虚函数，故不带 override
    EXPECT_TRUE(h.find("class Book : public emf::ecore::EObjectImpl") != std::string::npos);
    EXPECT_TRUE(h.find("std::string getTitle() const {") != std::string::npos);
    EXPECT_TRUE(h.find("void setTitle(std::string v) {") != std::string::npos);
    EXPECT_TRUE(h.find("int getPages() const {") != std::string::npos);
    EXPECT_TRUE(h.find("void setPages(int v) {") != std::string::npos);
}

// ===== 8. emitImplSource: 含字段 + eStaticClass（统一类源文件）=====
EMF_TEST(CppTemplates_emitImpl_fieldAndEStaticClass) {
    auto gm = buildSampleGenModel();
    TemplateContext ctx;
    ctx.genModel = gm.get();
    ctx.genPackage = gm->genPackages[0].get();
    ctx.genClass = gm->genPackages[0]->genClasses[1].get();  // Book
    // 字段和 typed getter 在 header（emitInterfaceHeader）
    auto h = emitInterfaceHeader(ctx);
    EXPECT_TRUE(h.find("class Book : public emf::ecore::EObjectImpl") != std::string::npos);
    EXPECT_TRUE(h.find("title_") != std::string::npos);
    EXPECT_TRUE(h.find("pages_") != std::string::npos);
    EXPECT_TRUE(h.find("eStaticClass()") != std::string::npos);
    // 源文件由 emitImplSource 输出
    auto src = emitImplSource(ctx);
    EXPECT_TRUE(src.find("Book::Book()") != std::string::npos);
    EXPECT_TRUE(src.find("return LibraryPackage::instance()->getBook()") != std::string::npos);
}

// ===== 9. emitAdapterFactory: 包含 isFactoryForType =====
EMF_TEST(CppTemplates_emitAdapterFactory_isFactoryForType) {
    auto gm = buildSampleGenModel();
    TemplateContext ctx; ctx.genModel = gm.get(); ctx.genPackage = gm->genPackages[0].get();
    auto h = emitAdapterFactoryHeader(ctx);
    EXPECT_TRUE(h.find("class LibraryAdapterFactory") != std::string::npos);
    EXPECT_TRUE(h.find("isFactoryForType") != std::string::npos);
    auto src = emitAdapterFactorySource(ctx);
    EXPECT_TRUE(src.find("getName() == \"library\"") != std::string::npos);
}

// ===== 10. emitValidator: validate<Book> / validate<Library> =====
EMF_TEST(CppTemplates_emitValidator_classSpecificMethods) {
    auto gm = buildSampleGenModel();
    TemplateContext ctx; ctx.genModel = gm.get(); ctx.genPackage = gm->genPackages[0].get();
    auto h = emitValidatorHeader(ctx);
    EXPECT_TRUE(h.find("class LibraryValidator") != std::string::npos);
    EXPECT_TRUE(h.find("validateLibrary(Library*") != std::string::npos);
    EXPECT_TRUE(h.find("validateBook(Book*") != std::string::npos);
    auto src = emitValidatorSource(ctx);
    EXPECT_TRUE(src.find("dynamic_cast<Library*>") != std::string::npos);
    EXPECT_TRUE(src.find("dynamic_cast<Book*>") != std::string::npos);
}

// ===== 11. 模板字符串不为空 + 包含 @generated 标记（注释里的占位）=====
EMF_TEST(CppTemplates_allTemplatesReturnNonEmpty) {
    auto gm = buildSampleGenModel();
    TemplateContext ctx; ctx.genModel = gm.get(); ctx.genPackage = gm->genPackages[0].get();
    ctx.genClass = gm->genPackages[0]->genClasses[0].get();

    EXPECT_FALSE(emitPackageHeader(ctx).empty());
    EXPECT_FALSE(emitPackageSource(ctx).empty());
    EXPECT_FALSE(emitFactoryHeader(ctx).empty());
    EXPECT_FALSE(emitFactorySource(ctx).empty());
    EXPECT_FALSE(emitSwitchHeader(ctx).empty());
    EXPECT_FALSE(emitSwitchSource(ctx).empty());
    EXPECT_FALSE(emitAdapterFactoryHeader(ctx).empty());
    EXPECT_FALSE(emitAdapterFactorySource(ctx).empty());
    EXPECT_FALSE(emitValidatorHeader(ctx).empty());
    EXPECT_FALSE(emitValidatorSource(ctx).empty());
    EXPECT_FALSE(emitInterfaceHeader(ctx).empty());
    EXPECT_FALSE(emitImplSource(ctx).empty());
}
