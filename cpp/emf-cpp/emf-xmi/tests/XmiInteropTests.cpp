// XmiInteropTests.cpp —— C++ XMI 与 Java EMF 互操作性测试
//
// 验证三个核心问题：
//   1. C++ 生成的 XMI 是否符合 Java EMF 可读的格式（xmi:version、xmlns、//@feat.idx 跨引用、xsi:type）
//   2. C++ roundtrip（读 → 写 → 读）是否保持跨引用和类型信息
//   3. Java 风格的 XMI（带 //@feat.idx 属性跨引用、<feat href="..."/> 子元素跨引用、xsi:type 子类型）
//      能否被 C++ 正确读取并恢复
#include "test_main.h"
#include "emf/xmi/XMIResource.h"
#include "emf/xmi/XMIResourceFactory.h"
#include "emf/ecore/EcorePackage.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/common/EList.h"

#include <any>
#include <string>

using emf::xmi::XMIResource;
using emf::xmi::XMIResourceFactory;
using emf::ecore::EcoreFactory;
using emf::ecore::EcorePackage;
using emf::ecore::EPackage;
using emf::ecore::EClass;
using emf::ecore::EReference;
using emf::ecore::EAttribute;
using emf::ecore::EFactory;
using emf::common::EObject;

namespace {

const char* kLibraryEcore =
    "<?xml version=\"1.0\"?>\n"
    "<ecore:EPackage xmlns:ecore=\"http://www.eclipse.org/emf/2002/Ecore\" "
    "xmlns:xmi=\"http://www.omg.org/XMI\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
    "xmi:version=\"2.0\" name=\"library\" nsURI=\"http://example.com/library/1.0\" nsPrefix=\"library\">"
    "<eClassifiers xsi:type=\"ecore:EClass\" name=\"Library\">"
    "<eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"name\" "
    "eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString\"/>"
    "<eStructuralFeatures xsi:type=\"ecore:EReference\" name=\"books\" upperBound=\"-1\" "
    "eType=\"#//Book\" containment=\"true\"/>"
    "<eStructuralFeatures xsi:type=\"ecore:EReference\" name=\"writers\" upperBound=\"-1\" "
    "eType=\"#//Writer\" containment=\"true\"/>"
    "<eStructuralFeatures xsi:type=\"ecore:EReference\" name=\"address\" "
    "eType=\"#//Address\" containment=\"true\"/>"
    "</eClassifiers>"
    "<eClassifiers xsi:type=\"ecore:EClass\" name=\"Book\">"
    "<eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"title\" "
    "eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString\"/>"
    "<eStructuralFeatures xsi:type=\"ecore:EReference\" name=\"author\" "
    "eType=\"#//Writer\"/>"
    "</eClassifiers>"
    "<eClassifiers xsi:type=\"ecore:EClass\" name=\"Writer\">"
    "<eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"name\" "
    "eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString\"/>"
    "</eClassifiers>"
    "<eClassifiers xsi:type=\"ecore:EClass\" name=\"Address\">"
    "<eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"street\" "
    "eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString\"/>"
    "</eClassifiers>"
    "</ecore:EPackage>";

void initEnv() {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    XMIResourceFactory::registerDefaults();
}

struct ModelMeta {
    EPackage* pkg = nullptr;
    EClass* libCls = nullptr;
    EClass* bookCls = nullptr;
    EClass* writerCls = nullptr;
    EClass* addressCls = nullptr;
    EFactory* factory = nullptr;
};

ModelMeta loadModel() {
    XMIResource res;
    res.loadFromString(std::string(kLibraryEcore));
    ModelMeta m;
    m.pkg = dynamic_cast<EPackage*>(res.getContents().front());
    res.getContents().clear();
    m.libCls = dynamic_cast<EClass*>(m.pkg->getEClassifier("Library"));
    m.bookCls = dynamic_cast<EClass*>(m.pkg->getEClassifier("Book"));
    m.writerCls = dynamic_cast<EClass*>(m.pkg->getEClassifier("Writer"));
    m.addressCls = dynamic_cast<EClass*>(m.pkg->getEClassifier("Address"));
    m.factory = m.pkg->getEFactoryInstance();
    return m;
}

EObject* getRef(EObject* obj, EClass* cls, const std::string& featName) {
    auto* sf = cls->getEStructuralFeature(featName);
    auto v = obj->eGet(sf);
    if (v.type() == typeid(EObject*)) return std::any_cast<EObject*>(v);
    return nullptr;
}

std::string getStr(EObject* obj, EClass* cls, const std::string& featName) {
    auto* sf = cls->getEStructuralFeature(featName);
    auto v = obj->eGet(sf);
    if (v.type() == typeid(std::string)) return std::any_cast<std::string>(v);
    return "";
}

}  // namespace

// ===== 测试 1: C++ 输出的 XMI 含 Java 兼容的跨引用 position-path =====
// 构造 Library -> Book(author=Writer)，author 是非 containment 跨引用
// 期望输出包含 author="//@books.0" 或类似 //@feat.idx 形式（而非有缺陷的 "//"）
EMF_TEST(XmiInterop_CppOutput_HasJavaCompatibleHref) {
    initEnv();
    auto m = loadModel();
    auto* lib = m.factory->create(m.libCls);
    lib->eSet(m.libCls->getEStructuralFeature("name"), std::any(std::string("My Lib")));

    auto* book = m.factory->create(m.bookCls);
    book->eSet(m.bookCls->getEStructuralFeature("title"), std::any(std::string("B1")));

    auto* writer = m.factory->create(m.writerCls);
    writer->eSet(m.writerCls->getEStructuralFeature("name"), std::any(std::string("W1")));

    // 把 writer 挂到 book.author（非 containment 跨引用）
    book->eSet(m.bookCls->getEStructuralFeature("author"), std::any((EObject*)writer));

    // book 挂到 lib.books（containment）
    emf_test::addToContainment(lib, m.libCls->getEStructuralFeature("books"), book);

    // writer 挂到 lib.writers（containment），这样 writer 在 containment 树中，
    // Saver 会生成 position path //@writers.0（Java 兼容）
    emf_test::addToContainment(lib, m.libCls->getEStructuralFeature("writers"), writer);

    XMIResource res;
    res.getContents().push_back(lib);
    std::string out = res.saveToString();

    // 必须包含 Java 兼容的 position-path 跨引用 //@writers.0（writer 在 containment 树中）
    EXPECT_TRUE(out.find("author=\"//@writers.0\"") != std::string::npos);
    // 不应再出现有缺陷的占位 "//"（不带 @ 的空 href）
    EXPECT_TRUE(out.find("author=\"//\"") == std::string::npos);
    // 必须声明 xmlns:xsi（Java 兼容）
    EXPECT_TRUE(out.find("xmlns:xsi") != std::string::npos);
}

// ===== 测试 2: C++ roundtrip 保持跨引用（读 → 写 → 读） =====
// 构造带跨引用的模型，序列化 → 反序列化 → 验证跨引用仍存在
EMF_TEST(XmiInterop_Roundtrip_PreservesCrossReference) {
    initEnv();
    auto m = loadModel();
    auto* lib = m.factory->create(m.libCls);
    lib->eSet(m.libCls->getEStructuralFeature("name"), std::any(std::string("Lib")));

    auto* book = m.factory->create(m.bookCls);
    book->eSet(m.bookCls->getEStructuralFeature("title"), std::any(std::string("Title")));

    auto* writer = m.factory->create(m.writerCls);
    writer->eSet(m.writerCls->getEStructuralFeature("name"), std::any(std::string("WriterName")));

    book->eSet(m.bookCls->getEStructuralFeature("author"), std::any((EObject*)writer));
    emf_test::addToContainment(lib, m.libCls->getEStructuralFeature("books"), book);
    // writer 挂到 lib.writers containment，保证 position path 可用
    emf_test::addToContainment(lib, m.libCls->getEStructuralFeature("writers"), writer);

    // 第一次序列化
    XMIResource res1;
    res1.getContents().push_back(lib);
    std::string xmi1 = res1.saveToString();

    // 反序列化
    XMIResource res2;
    res2.loadFromString(xmi1);
    auto* lib2 = dynamic_cast<EObject*>(res2.getContents().front());
    EXPECT_NOT_NULL(lib2);
    auto* lib2Cls = lib2->eClass();

    // 取 books[0]，验证 author 跨引用恢复
    auto books2V = lib2->eGet(lib2Cls->getEStructuralFeature("books"));
    auto* books2List = std::any_cast<emf::common::EList<EObject*>*>(books2V);
    EXPECT_EQ(books2List->size(), 1u);
    auto* book2 = (*books2List)[0];
    EXPECT_NOT_NULL(book2);

    auto* book2Cls = book2->eClass();
    auto* author2 = getRef(book2, book2Cls, "author");
    EXPECT_NOT_NULL(author2);
    EXPECT_EQ(getStr(author2, author2->eClass(), "name"), std::string("WriterName"));
}

// ===== 测试 3: C++ 能读 Java 风格 XMI（属性形式跨引用 //@feat.idx） =====
EMF_TEST(XmiInterop_CppCanRead_JavaStyle_AttributeCrossRef) {
    initEnv();
    auto m = loadModel();  // 注册 EPackage 到 registry

    // Java 风格 XMI：author 作为属性形式的跨引用 author="//@writers.0"
    // 这里 writers 不是 containment（writer 是顶层对象，用多根 <xmi:XMI> 包裹）
    // 但本测试用单根 + containment writer 验证属性跨引用解析
    std::string javaStyleXmi =
        "<?xml version=\"1.0\"?>\n"
        "<library:Library xmi:version=\"2.0\" "
        "xmlns:xmi=\"http://www.omg.org/XMI\" "
        "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
        "xmlns:library=\"http://example.com/library/1.0\" name=\"Java Lib\">\n"
        "  <books title=\"Java Book\" author=\"//@writers.0\"/>\n"
        "  <writers name=\"Java Writer\"/>\n"
        "</library:Library>\n";

    XMIResource res;
    res.loadFromString(javaStyleXmi);
    auto* lib = res.getContents().front();
    EXPECT_NOT_NULL(lib);
    auto* libCls = lib->eClass();

    // 取 books[0]，验证 author 跨引用被解析
    auto booksV = lib->eGet(libCls->getEStructuralFeature("books"));
    auto* booksList = std::any_cast<emf::common::EList<EObject*>*>(booksV);
    EXPECT_EQ(booksList->size(), 1u);
    auto* book = (*booksList)[0];
    auto* bookCls = book->eClass();
    EXPECT_EQ(getStr(book, bookCls, "title"), std::string("Java Book"));

    auto* author = getRef(book, bookCls, "author");
    EXPECT_NOT_NULL(author);
    EXPECT_EQ(getStr(author, author->eClass(), "name"), std::string("Java Writer"));
}

// ===== 测试 4: C++ 能读 Java 风格 XMI（多值属性跨引用，空格分隔） =====
EMF_TEST(XmiInterop_CppCanRead_JavaStyle_MultiValueAttributeCrossRef) {
    initEnv();
    // 构造一个 ecore，Book.authors 是多值非 containment
    std::string ecore =
        "<?xml version=\"1.0\"?>\n"
        "<ecore:EPackage xmlns:ecore=\"http://www.eclipse.org/emf/2002/Ecore\" "
        "xmlns:xmi=\"http://www.omg.org/XMI\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
        "xmi:version=\"2.0\" name=\"lib2\" nsURI=\"http://example.com/lib2/1.0\" nsPrefix=\"lib2\">"
        "<eClassifiers xsi:type=\"ecore:EClass\" name=\"Library\">"
        "<eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"name\" "
        "eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString\"/>"
        "<eStructuralFeatures xsi:type=\"ecore:EReference\" name=\"books\" upperBound=\"-1\" "
        "eType=\"#//Book\" containment=\"true\"/>"
        "<eStructuralFeatures xsi:type=\"ecore:EReference\" name=\"writers\" upperBound=\"-1\" "
        "eType=\"#//Writer\" containment=\"true\"/>"
        "</eClassifiers>"
        "<eClassifiers xsi:type=\"ecore:EClass\" name=\"Book\">"
        "<eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"title\" "
        "eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString\"/>"
        "<eStructuralFeatures xsi:type=\"ecore:EReference\" name=\"authors\" upperBound=\"-1\" "
        "eType=\"#//Writer\"/>"
        "</eClassifiers>"
        "<eClassifiers xsi:type=\"ecore:EClass\" name=\"Writer\">"
        "<eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"name\" "
        "eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString\"/>"
        "</eClassifiers>"
        "</ecore:EPackage>";

    XMIResource ecoreRes;
    ecoreRes.loadFromString(ecore);

    std::string javaStyleXmi =
        "<?xml version=\"1.0\"?>\n"
        "<lib2:Library xmi:version=\"2.0\" "
        "xmlns:xmi=\"http://www.omg.org/XMI\" "
        "xmlns:lib2=\"http://example.com/lib2/1.0\" name=\"Multi Lib\">\n"
        "  <books title=\"B1\" authors=\"//@writers.0 //@writers.1\"/>\n"
        "  <writers name=\"W1\"/>\n"
        "  <writers name=\"W2\"/>\n"
        "</lib2:Library>\n";

    XMIResource res;
    res.loadFromString(javaStyleXmi);
    auto* lib = res.getContents().front();
    auto* libCls = lib->eClass();

    auto booksV = lib->eGet(libCls->getEStructuralFeature("books"));
    auto* booksList = std::any_cast<emf::common::EList<EObject*>*>(booksV);
    auto* book = (*booksList)[0];

    // 验证 authors 多值跨引用被解析为 2 个 Writer
    auto* bookCls = book->eClass();
    auto authorsV = book->eGet(bookCls->getEStructuralFeature("authors"));
    auto* authorsList = std::any_cast<emf::common::EList<EObject*>*>(authorsV);
    EXPECT_EQ(authorsList->size(), 2u);
    EXPECT_EQ(getStr((*authorsList)[0], (*authorsList)[0]->eClass(), "name"), std::string("W1"));
    EXPECT_EQ(getStr((*authorsList)[1], (*authorsList)[1]->eClass(), "name"), std::string("W2"));
}

// ===== 测试 5: C++ 能读 Java 风格 XMI（xmi:id 引用） =====
EMF_TEST(XmiInterop_CppCanRead_JavaStyle_XmiIdReference) {
    initEnv();
    auto m = loadModel();

    // Java 风格：用 xmi:id 标记 writer，book.author 用 "//w1" 引用
    std::string javaStyleXmi =
        "<?xml version=\"1.0\"?>\n"
        "<library:Library xmi:version=\"2.0\" "
        "xmlns:xmi=\"http://www.omg.org/XMI\" "
        "xmlns:library=\"http://example.com/library/1.0\" name=\"ID Lib\">\n"
        "  <books title=\"B1\" author=\"//w1\"/>\n"
        "  <writers xmi:id=\"w1\" name=\"WriterByID\"/>\n"
        "</library:Library>\n";

    XMIResource res;
    res.loadFromString(javaStyleXmi);
    auto* lib = res.getContents().front();
    auto* libCls = lib->eClass();

    auto booksV = lib->eGet(libCls->getEStructuralFeature("books"));
    auto* booksList = std::any_cast<emf::common::EList<EObject*>*>(booksV);
    auto* book = (*booksList)[0];
    auto* bookCls = book->eClass();

    auto* author = getRef(book, bookCls, "author");
    EXPECT_NOT_NULL(author);
    EXPECT_EQ(getStr(author, author->eClass(), "name"), std::string("WriterByID"));
}
