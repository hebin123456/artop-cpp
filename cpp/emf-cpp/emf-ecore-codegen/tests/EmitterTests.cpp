// EmitterTests.cpp —— 各 emitter 单元测试
#include "test_main.h"
#include "test_helpers.h"
#include "emf/ecore/codegen/CppGenerator.h"
#include "emf/ecore/codegen/PackageEmitter.h"
#include "emf/ecore/codegen/FactoryEmitter.h"
#include "emf/ecore/codegen/EClassEmitter.h"
#include "emf/ecore/codegen/SwitchEmitter.h"
#include "emf/ecore/codegen/AdapterFactoryEmitter.h"
#include "emf/ecore/codegen/ValidatorEmitter.h"
#include "emf/ecore/codegen/TypeMapper.h"

#include "emf/ecore/EcorePackage.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/common/EPackage.h"

#include <fstream>
#include <filesystem>
#include <regex>

using namespace emf;
using namespace emf::ecore;
using namespace emf::ecore::codegen;

namespace {

EClass* addClass(EPackage* pkg, const std::string& name) {
    auto* c = EcoreFactory::instance().createEClass();
    c->setName(name);
    pkg->addEClassifier(c);
    return c;
}
EAttribute* addAttr(EClass* c, const std::string& name, const std::string& typeName, const std::string& defLit = "") {
    auto* a = EcoreFactory::instance().createEAttribute();
    a->setName(name);
    auto& pkg = EcorePackage::instance();
    EDataType* dt = nullptr;
    if (typeName == "EString") dt = pkg.getEDataType_EString();
    else if (typeName == "EBoolean") dt = pkg.getEDataType_EBoolean();
    else if (typeName == "EInt") dt = pkg.getEDataType_EInt();
    else if (typeName == "EDouble") dt = pkg.getEDataType_EDouble();
    else if (typeName == "EFloat") dt = pkg.getEDataType_EFloat();
    else if (typeName == "ELong") dt = pkg.getEDataType_ELong();
    else if (typeName == "EShort") dt = pkg.getEDataType_EShort();
    else if (typeName == "EByte") dt = pkg.getEDataType_EByte();
    else dt = pkg.getEDataType_EString();  // 兜底
    a->setEAttributeType(dt);
    if (!defLit.empty()) a->setDefaultValueLiteral(defLit);
    c->addEStructuralFeature(a);
    return a;
}
EReference* addRef(EClass* c, const std::string& name, EClass* target, bool containment, int lower, int upper) {
    auto* r = EcoreFactory::instance().createEReference();
    r->setName(name);
    r->setEReferenceType(target);
    r->setContainment(containment);
    r->setLowerBound(lower);
    r->setUpperBound(upper);
    c->addEStructuralFeature(r);
    return r;
}

EPackage* buildLibraryPackage() {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* pkg = EcoreFactory::instance().createEPackage();
    pkg->setName("library");
    pkg->setNsURI("http://example.com/library/1.0");
    pkg->setNsPrefix("library");
    auto* library = addClass(pkg, "Library");
    auto* book = addClass(pkg, "Book");
    auto* writer = addClass(pkg, "Writer");
    addAttr(library, "name", "EString");
    addRef(library, "books", book, true, 0, -1);
    addAttr(book, "title", "EString");
    addAttr(book, "pages", "EInt", "0");
    addRef(book, "author", writer, true, 0, 1);
    addAttr(writer, "name", "EString");
    return pkg;
}

}  // namespace

// ===== TypeMapper =====
EMF_TEST(TypeMapper_EStringToStdString) {
    EXPECT_EQ(TypeMapper::cppType("EString"), std::string("std::string"));
    EXPECT_EQ(TypeMapper::cppType("EInt"), std::string("int32_t"));
    EXPECT_EQ(TypeMapper::cppType("ELong"), std::string("int64_t"));
    EXPECT_EQ(TypeMapper::cppType("EBoolean"), std::string("bool"));
    EXPECT_EQ(TypeMapper::cppType("EDouble"), std::string("double"));
    EXPECT_EQ(TypeMapper::cppType("EFloat"), std::string("float"));
    EXPECT_EQ(TypeMapper::cppType("EShort"), std::string("int16_t"));
    EXPECT_EQ(TypeMapper::cppType("EByte"), std::string("int8_t"));
}
EMF_TEST(TypeMapper_DefaultValues) {
    EXPECT_EQ(TypeMapper::defaultValueLiteral("EString", "hello"), std::string("std::string(\"hello\")"));
    EXPECT_EQ(TypeMapper::defaultValueLiteral("EInt", "42"), std::string("42"));
    EXPECT_EQ(TypeMapper::defaultValueLiteral("EBoolean", "true"), std::string("true"));
    EXPECT_EQ(TypeMapper::defaultValueLiteral("EString", ""), std::string(""));
}
EMF_TEST(TypeMapper_IncludeFor) {
    EXPECT_EQ(TypeMapper::includeFor("std::string"), std::string("<string>"));
    EXPECT_EQ(TypeMapper::includeFor("int32_t"), std::string("<cstdint>"));
    EXPECT_EQ(TypeMapper::includeFor("bool"), std::string(""));
}

// ===== PackageEmitter =====
EMF_TEST(PackageEmitter_HeaderContainsClassAndStaticMembers) {
    auto* pkg = buildLibraryPackage();
    PackageEmitter em(pkg, "emf");
    auto h = em.emitHeader();
    EXPECT_TRUE(h.find("class LibraryPackage") != std::string::npos);
    EXPECT_TRUE(h.find("static const std::string eNS_URI;") != std::string::npos);
    EXPECT_TRUE(h.find("getLibrary") != std::string::npos);
    EXPECT_TRUE(h.find("getBook") != std::string::npos);
    EXPECT_TRUE(h.find("getLibrary_Name") != std::string::npos);
    EXPECT_TRUE(h.find("getLibrary_Books") != std::string::npos);
    EXPECT_TRUE(h.find("static void initialize()") != std::string::npos);
}
EMF_TEST(PackageEmitter_SourceInitializeCreatesMetadata) {
    auto* pkg = buildLibraryPackage();
    PackageEmitter em(pkg, "emf");
    auto s = em.emitSource();
    EXPECT_TRUE(s.find("LibraryPackage::initialize()") != std::string::npos);
    EXPECT_TRUE(s.find("Library_class_->setName(\"Library\")") != std::string::npos);
    EXPECT_TRUE(s.find("Book_class_->setName(\"Book\")") != std::string::npos);
    EXPECT_TRUE(s.find("Library_books_ref_->setContainment(true)") != std::string::npos);
    EXPECT_TRUE(s.find("setEFactoryInstance(") != std::string::npos);
    EXPECT_TRUE(s.find("EPackageRegistry::instance().put(eNS_URI, eINSTANCE)") != std::string::npos);
}

// ===== FactoryEmitter =====
EMF_TEST(FactoryEmitter_HeaderHasCreateMethods) {
    auto* pkg = buildLibraryPackage();
    FactoryEmitter em(pkg, "emf");
    auto h = em.emitHeader();
    EXPECT_TRUE(h.find("LibraryFactory") != std::string::npos);
    EXPECT_TRUE(h.find("createLibrary") != std::string::npos);
    EXPECT_TRUE(h.find("createBook") != std::string::npos);
    EXPECT_TRUE(h.find("createWriter") != std::string::npos);
}
EMF_TEST(FactoryEmitter_SourceInstantiatesClasses) {
    auto* pkg = buildLibraryPackage();
    FactoryEmitter em(pkg, "emf");
    auto s = em.emitSource();
    // 单类单继承方案：create 直接返回 new <ClassName>()，无 Impl 后缀
    EXPECT_TRUE(s.find("return new Library();") != std::string::npos);
    EXPECT_TRUE(s.find("return new Book();") != std::string::npos);
    EXPECT_TRUE(s.find("setEPackage(LibraryPackage::eINSTANCE)") != std::string::npos);
}

// ===== EClassEmitter（合并旧 InterfaceEmitter + ImplEmitter）=====
EMF_TEST(EClassEmitter_DeclaresSingleClassSingleInheritance) {
    auto* pkg = buildLibraryPackage();
    auto* library = dynamic_cast<EClass*>(pkg->getEClassifier("Library"));
    EXPECT_NOT_NULL(library);
    EClassEmitter em(library, "emf");
    auto h = em.emitHeader();
    // 单类、单继承 EObjectImpl，无 Impl 后缀、无 virtual 继承、无多继承
    // 用 emf::common::EObjectImpl（实际类定义）作为基类，避免依赖 EcoreImpls.h 的 using 别名
    EXPECT_TRUE(h.find("class Library : public emf::common::EObjectImpl") != std::string::npos);
    EXPECT_TRUE(h.find("std::string name() const;") != std::string::npos);
    EXPECT_TRUE(h.find("void setName(") != std::string::npos);
    EXPECT_TRUE(h.find("emf::common::EList<Book*>& getBooks()") != std::string::npos);
    // 运行时行为 override 全部保留
    EXPECT_TRUE(h.find("eGet(const emf::ecore::EStructuralFeature*") != std::string::npos);
    EXPECT_TRUE(h.find("eSet(const emf::ecore::EStructuralFeature*") != std::string::npos);
    EXPECT_TRUE(h.find("eContents()") != std::string::npos);
}
EMF_TEST(EClassEmitter_BookHasPagesAttr) {
    auto* pkg = buildLibraryPackage();
    auto* book = dynamic_cast<EClass*>(pkg->getEClassifier("Book"));
    EClassEmitter em(book, "emf");
    auto h = em.emitHeader();
    EXPECT_TRUE(h.find("int32_t pages() const;") != std::string::npos);
    EXPECT_TRUE(h.find("void setPages(") != std::string::npos);
    EXPECT_TRUE(h.find("Writer* getAuthor() const;") != std::string::npos);
}
EMF_TEST(EClassEmitter_HasFieldsAndGettersAndNotify) {
    auto* pkg = buildLibraryPackage();
    auto* library = dynamic_cast<EClass*>(pkg->getEClassifier("Library"));
    EClassEmitter em(library, "emf");
    auto h = em.emitHeader();
    EXPECT_TRUE(h.find("std::string name_;") != std::string::npos);
    EXPECT_TRUE(h.find("emf::common::EList<Book*>* books_ = nullptr;") != std::string::npos);
    auto s = em.emitSource();
    EXPECT_TRUE(s.find("Library::name() const") != std::string::npos);
    EXPECT_TRUE(s.find("Library::setName(") != std::string::npos);
    EXPECT_TRUE(s.find("eStaticFeature(") != std::string::npos);
    EXPECT_TRUE(s.find("eNotify(n)") != std::string::npos);
}
EMF_TEST(EClassEmitter_DefaultLiteralForInt) {
    auto* pkg = buildLibraryPackage();
    auto* book = dynamic_cast<EClass*>(pkg->getEClassifier("Book"));
    EClassEmitter em(book, "emf");
    auto h = em.emitHeader();
    EXPECT_TRUE(h.find("int32_t pages_ = 0;") != std::string::npos);
}

// ===== SwitchEmitter =====
EMF_TEST(SwitchEmitter_DeclaresCaseForEachEClass) {
    auto* pkg = buildLibraryPackage();
    SwitchEmitter em(pkg, "emf");
    auto h = em.emitHeader();
    EXPECT_TRUE(h.find("class LibrarySwitch") != std::string::npos);
    EXPECT_TRUE(h.find("caseLibrary(Library*") != std::string::npos);
    EXPECT_TRUE(h.find("caseBook(Book*") != std::string::npos);
    EXPECT_TRUE(h.find("caseWriter(Writer*") != std::string::npos);
    EXPECT_TRUE(h.find("T doSwitch(emf::common::EObject*") != std::string::npos);
}

// ===== AdapterFactoryEmitter =====
EMF_TEST(AdapterFactoryEmitter_DeclaresFactoryAndAdapter) {
    auto* pkg = buildLibraryPackage();
    AdapterFactoryEmitter em(pkg, "emf");
    auto h = em.emitHeader();
    EXPECT_TRUE(h.find("class LibraryAdapterFactory") != std::string::npos);
    EXPECT_TRUE(h.find("createAdapter(emf::common::Notifier*") != std::string::npos);
    EXPECT_TRUE(h.find("LibraryAdapter") != std::string::npos);
}

// ===== ValidatorEmitter =====
EMF_TEST(ValidatorEmitter_HasValidateMethod) {
    auto* pkg = buildLibraryPackage();
    ValidatorEmitter em(pkg, "emf");
    auto h = em.emitHeader();
    EXPECT_TRUE(h.find("class LibraryValidator") != std::string::npos);
    EXPECT_TRUE(h.find("validate(emf::common::EObject*") != std::string::npos);
    auto s = em.emitSource();
    EXPECT_TRUE(s.find("LibraryPackage::initialize()") != std::string::npos);
    EXPECT_TRUE(s.find("LibraryPackage::eNAME") != std::string::npos);
}
