// library_demo.cpp —— C++ 版 Java EMF end-to-end demo
// 对应 /workspace/emf-demo/src/main/java/com/example/emfdemo/
//
//   Demo 0: 动态构建 Library Ecore 元模型 + 保存 .ecore + 重新加载
//   Demo 1: 跳过 GenModel（Java codegen 概念；C++ 用 emf-ecore-codegen 替代）
//   Demo 2: 动态 EMF 实例化 + XMI 序列化/反序列化 + 结构对比
//   Demo 3: 变更通知（普通 Adapter / EContentAdapter / 类型过滤 Adapter / 移除）
#include <iostream>
#include <fstream>
#include <sstream>
#include <string>
#include <vector>
#include <unordered_map>
#include <unordered_set>
#include <any>
#include <typeinfo>
#include <ctime>
#include <cstdio>
#include <stdexcept>

#include "emf/common/EPackageRegistry.h"
#include "emf/common/Resource.h"
#include "emf/common/URI.h"
#include "emf/common/ENotifier.h"
#include "emf/common/EList.h"
#include "emf/common/Notification.h"

#include "emf/ecore/EcorePackage.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/ecore/EcoreMetadata.h"

#include "emf/xmi/XMIResource.h"
#include "emf/xmi/XMIResourceFactory.h"

using namespace emf::common;
using namespace emf::ecore;

// 明确用 emf::ecore::EPackage 避免和 emf::common::EPackage 冲突
using EPackageBase = emf::ecore::EPackage;

// ===== 工具：dump 树 =====
static void dumpTree(EObject* root, int indent, const std::string& tag = "node") {
    for (int i = 0; i < indent; i++) std::cout << "  ";
    std::cout << tag << " " << root->eClass()->getName();
    for (auto* sf : root->eClass()->getEAttributes()) {
        std::any v;
        try { v = root->eGet(sf); } catch (...) { v.reset(); }
        if (!v.has_value() || sf->getName() == "name") continue;
        if (v.type() == typeid(std::string)) {
            std::string s = std::any_cast<std::string>(v);
            if (s.size() > 40) s = s.substr(0, 40) + "...";
            std::cout << " " << sf->getName() << "=\"" << s << "\"";
        } else {
            std::cout << " " << sf->getName() << "=<?>";
        }
    }
    std::cout << "\n";
    for (auto* ref : root->eClass()->getEReferences()) {
        if (!ref->isContainment()) continue;
        std::any v;
        try { v = root->eGet(ref); } catch (...) { v.reset(); }
        if (v.type() == typeid(EList<EObject*>*)) {
            auto* p = std::any_cast<EList<EObject*>*>(v);
            for (size_t i = 0; p && i < p->size(); ++i) {
                if (auto* child = p->get(i)) dumpTree(child, indent + 1);
            }
        } else if (v.type() == typeid(std::vector<EObject*>)) {
            auto& arr = std::any_cast<std::vector<EObject*>&>(v);
            for (auto* child : arr) if (child) dumpTree(child, indent + 1);
        } else if (v.type() == typeid(EObject*)) {
            if (auto* child = std::any_cast<EObject*>(v)) dumpTree(child, indent + 1);
        }
    }
}

// ===== Demo 0: 动态构建 Library Ecore 元模型 =====
namespace EcoreModelBuilder {

const std::string NS_URI = "http://example.com/emfdemo/library";
const std::string NS_PREFIX = "library";

EClass* mkClass(EcoreFactory& f, const std::string& name) {
    EClass* c = f.createEClass();
    c->setName(name);
    return c;
}
EClass* mkAbstractClass(EcoreFactory& f, const std::string& name) {
    EClass* c = mkClass(f, name);
    c->setAbstract(true);
    return c;
}
void addAttr(EcoreFactory& f, EClass* owner, const std::string& name, EClassifier* type) {
    EAttribute* a = f.createEAttribute();
    a->setName(name);
    a->setEType(type);
    a->setLowerBound(0);
    a->setUpperBound(1);
    owner->addEStructuralFeature(a);
}
EReference* mkRef(EcoreFactory& f, const std::string& name, EClass* type,
                  int lower, int upper, bool containment) {
    EReference* r = f.createEReference();
    r->setName(name);
    r->setEType(type);
    r->setLowerBound(lower);
    r->setUpperBound(upper);
    r->setContainment(containment);
    return r;
}

EPackageBase* build() {
    EcoreFactory& f = EcoreFactory::instance();
    EcorePackage& p = EcorePackage::instance();

    // Package
    emf::ecore::EPackage* libraryPkg = f.createEPackage();
    libraryPkg->setName("library");
    libraryPkg->setNsPrefix(NS_PREFIX);
    libraryPkg->setNsURI(NS_URI);

    // Enum: BookCategory
    EEnum* bookCategory = f.createEEnum();
    bookCategory->setName("BookCategory");
    const char* literals[][2] = {
        {"FICTION", "Fiction"},
        {"NON_FICTION", "Non-Fiction"},
        {"SCIENCE", "Science"},
        {"HISTORY", "History"},
        {"TECHNOLOGY", "Technology"}
    };
    for (auto& kv : literals) {
        EEnumLiteral* lit = f.createEEnumLiteral();
        lit->setName(kv[0]);
        lit->setLiteral(kv[1]);
        bookCategory->addELiteral(lit);
    }
    libraryPkg->addEClassifier(bookCategory);

    // DataType: ISBN
    EDataType* isbnType = f.createEDataType();
    isbnType->setName("ISBN");
    isbnType->setInstanceClassName("java.lang.String");
    libraryPkg->addEClassifier(isbnType);

    // 骨架 EClass
    EClass* address = mkClass(f, "Address");
    EClass* person = mkAbstractClass(f, "Person");
    EClass* author = mkClass(f, "Author");
    author->addESuperType(person);
    EClass* publisher = mkClass(f, "Publisher");
    publisher->addESuperType(person);
    EClass* publication = mkAbstractClass(f, "Publication");
    EClass* book = mkClass(f, "Book");
    book->addESuperType(publication);
    EClass* magazine = mkClass(f, "Magazine");
    magazine->addESuperType(publication);
    EClass* library = mkClass(f, "Library");

    for (auto* c : std::vector<EClass*>{address, person, author, publisher, publication, book, magazine, library}) {
        libraryPkg->addEClassifier(c);
    }

    // Attributes
    addAttr(f, address, "street", p.getEDataType_EString());
    addAttr(f, address, "city", p.getEDataType_EString());
    addAttr(f, address, "country", p.getEDataType_EString());
    addAttr(f, address, "zipCode", p.getEDataType_EString());

    addAttr(f, person, "name", p.getEDataType_EString());
    addAttr(f, person, "email", p.getEDataType_EString());

    addAttr(f, author, "birthYear", p.getEDataType_EInt());

    addAttr(f, publication, "title", p.getEDataType_EString());
    addAttr(f, publication, "publishDate", p.getEDataType_EDate());
    addAttr(f, publication, "category", bookCategory);

    addAttr(f, book, "isbn", isbnType);
    addAttr(f, book, "pages", p.getEDataType_EInt());
    addAttr(f, book, "price", p.getEDataType_EDouble());

    addAttr(f, magazine, "issueNumber", p.getEDataType_EInt());
    addAttr(f, magazine, "periodicity", p.getEDataType_EInt());

    addAttr(f, library, "name", p.getEDataType_EString());

    // References
    EReference* publisherAddress = mkRef(f, "address", address, 1, 1, true);
    publisher->addEStructuralFeature(publisherAddress);

    EReference* bookPublisher = mkRef(f, "publisher", publisher, 0, 1, false);
    book->addEStructuralFeature(bookPublisher);

    EReference* bookAuthors = mkRef(f, "authors", author, 0, -1, false);
    book->addEStructuralFeature(bookAuthors);

    EReference* authorBooks = mkRef(f, "books", book, 0, -1, false);
    author->addEStructuralFeature(authorBooks);

    EReference* publisherBooks = mkRef(f, "books", book, 0, -1, false);
    publisher->addEStructuralFeature(publisherBooks);

    EReference* libBooks = mkRef(f, "books", book, 0, -1, true);
    library->addEStructuralFeature(libBooks);
    EReference* libMagazines = mkRef(f, "magazines", magazine, 0, -1, true);
    library->addEStructuralFeature(libMagazines);
    EReference* libAuthors = mkRef(f, "authors", author, 0, -1, true);
    library->addEStructuralFeature(libAuthors);
    EReference* libPublishers = mkRef(f, "publishers", publisher, 0, -1, true);
    library->addEStructuralFeature(libPublishers);

    bookAuthors->setEOpposite(authorBooks);
    authorBooks->setEOpposite(bookAuthors);
    bookPublisher->setEOpposite(publisherBooks);
    publisherBooks->setEOpposite(bookPublisher);

    EOperation* greet = f.createEOperation();
    greet->setName("greet");
    greet->setEType(p.getEDataType_EString());
    person->addEOperation(greet);

    return libraryPkg;
}

void buildAndSave(const std::string& path) {
    EPackageBase* pkg = build();
    EPackageRegistry::instance().put(NS_URI, pkg);

    auto res = emf::xmi::XMIResourceFactory::createResourceFor(URI::createFileURI(path));
    res->addToContents(pkg);
    std::ofstream ofs(path);
    res->save(ofs);
    ofs.close();
    std::cout << "[EcoreModelBuilder] Saved Ecore model to: " << path << "\n";
}

EPackageBase* load(const std::string& path) {
    auto res = emf::xmi::XMIResourceFactory::createResourceFor(URI::createFileURI(path));
    std::ifstream ifs(path);
    res->load(ifs);
    ifs.close();
    EPackageBase* pkg = dynamic_cast<EPackageBase*>(res->getContents().empty() ? nullptr : res->getContents().front());
    std::cout << "[EcoreModelBuilder] Loaded Ecore model: " << (pkg ? pkg->getName() : "NULL")
              << " (classes=" << (pkg ? pkg->getEClassifiers().size() : 0) << ")\n";
    return pkg;
}

}  // namespace EcoreModelBuilder

// ===== 动态实例构造（对应 Java DynamicInstanceFactory） =====
namespace DynamicInstanceFactory {

// Java 的 libraryPkg.getEFactoryInstance().create(cls) 内部会回退到 DynamicEObjectImpl。
// C++ 端 EcoreFactory::create() 只能创建 Ecore 元类，对用户类返回 nullptr，
// 所以这里包一层：先尝试 EcoreFactory，再 fallback 到 DynamicEObjectImpl。
EObject* createObject(EClass* cls) {
    if (!cls) return nullptr;
    EObject* obj = EcoreFactory::instance().create(cls);
    if (!obj) obj = new DynamicEObjectImpl(cls);
    return obj;
}

EObject* createSampleLibrary(emf::ecore::EPackage* pkg) {
    EcoreFactory& f = EcoreFactory::instance();
    EClass* libraryCls = dynamic_cast<EClass*>(pkg->getEClassifier("Library"));
    EClass* bookCls = dynamic_cast<EClass*>(pkg->getEClassifier("Book"));
    EClass* authorCls = dynamic_cast<EClass*>(pkg->getEClassifier("Author"));
    EClass* publisherCls = dynamic_cast<EClass*>(pkg->getEClassifier("Publisher"));
    EClass* addressCls = dynamic_cast<EClass*>(pkg->getEClassifier("Address"));
    EClass* magazineCls = dynamic_cast<EClass*>(pkg->getEClassifier("Magazine"));
    EEnum* bookCategory = dynamic_cast<EEnum*>(pkg->getEClassifier("BookCategory"));

    EReference* libBooks = dynamic_cast<EReference*>(libraryCls->getEStructuralFeature("books"));
    EReference* libMagazines = dynamic_cast<EReference*>(libraryCls->getEStructuralFeature("magazines"));
    EReference* libAuthors = dynamic_cast<EReference*>(libraryCls->getEStructuralFeature("authors"));
    EReference* libPublishers = dynamic_cast<EReference*>(libraryCls->getEStructuralFeature("publishers"));

    // Library
    EObject* library = createObject(libraryCls);
    library->eSet(libraryCls->getEStructuralFeature("name"), std::string("City Central Library"));

    // 3 个 Author
    EObject* ada = createObject(authorCls);
    ada->eSet(authorCls->getEStructuralFeature("name"), std::string("Ada Lovelace"));
    ada->eSet(authorCls->getEStructuralFeature("email"), std::string("ada@example.com"));
    ada->eSet(authorCls->getEStructuralFeature("birthYear"), int(1815));

    EObject* alan = createObject(authorCls);
    alan->eSet(authorCls->getEStructuralFeature("name"), std::string("Alan Turing"));
    alan->eSet(authorCls->getEStructuralFeature("email"), std::string("alan@example.com"));
    alan->eSet(authorCls->getEStructuralFeature("birthYear"), int(1912));

    EObject* linus = createObject(authorCls);
    linus->eSet(authorCls->getEStructuralFeature("name"), std::string("Linus Torvalds"));
    linus->eSet(authorCls->getEStructuralFeature("email"), std::string("linus@example.com"));
    linus->eSet(authorCls->getEStructuralFeature("birthYear"), int(1969));

    auto authorsList = std::any_cast<EList<EObject*>*>(library->eGet(libAuthors));
    authorsList->add(ada);
    authorsList->add(alan);
    authorsList->add(linus);

    // 2 个 Publisher + Address
    EObject* oreilly = createObject(publisherCls);
    oreilly->eSet(publisherCls->getEStructuralFeature("name"), std::string("O'Reilly Media"));
    oreilly->eSet(publisherCls->getEStructuralFeature("email"), std::string("contact@oreilly.com"));
    EObject* oreillyAddr = createObject(addressCls);
    oreillyAddr->eSet(addressCls->getEStructuralFeature("street"), std::string("1005 Gravenstein Hwy N"));
    oreillyAddr->eSet(addressCls->getEStructuralFeature("city"), std::string("Sebastopol"));
    oreillyAddr->eSet(addressCls->getEStructuralFeature("country"), std::string("USA"));
    oreillyAddr->eSet(addressCls->getEStructuralFeature("zipCode"), std::string("95472"));
    oreilly->eSet(publisherCls->getEStructuralFeature("address"), oreillyAddr);

    EObject* addison = createObject(publisherCls);
    addison->eSet(publisherCls->getEStructuralFeature("name"), std::string("Addison-Wesley"));
    addison->eSet(publisherCls->getEStructuralFeature("email"), std::string("info@aw.com"));
    EObject* addisonAddr = createObject(addressCls);
    addisonAddr->eSet(addressCls->getEStructuralFeature("street"), std::string("75 Arlington St"));
    addisonAddr->eSet(addressCls->getEStructuralFeature("city"), std::string("Boston"));
    addisonAddr->eSet(addressCls->getEStructuralFeature("country"), std::string("USA"));
    addisonAddr->eSet(addressCls->getEStructuralFeature("zipCode"), std::string("02116"));
    addison->eSet(publisherCls->getEStructuralFeature("address"), addisonAddr);

    auto publishersList = std::any_cast<EList<EObject*>*>(library->eGet(libPublishers));
    publishersList->add(oreilly);
    publishersList->add(addison);

    // 2 本 Book
    EObject* book1 = createObject(bookCls);
    book1->eSet(bookCls->getEStructuralFeature("title"), std::string("The Pragmatic Programmer"));
    book1->eSet(bookCls->getEStructuralFeature("isbn"), std::string("978-0201616224"));
    book1->eSet(bookCls->getEStructuralFeature("pages"), int(320));
    book1->eSet(bookCls->getEStructuralFeature("price"), 49.99);
    // 简单 publishDate 模拟（Java 用 Date(99, 9, 20) -> 1999-10-20）
    book1->eSet(bookCls->getEStructuralFeature("category"),
                std::any(static_cast<EEnumLiteral*>(bookCategory->getELiteral("TECHNOLOGY"))));
    book1->eSet(bookCls->getEStructuralFeature("publisher"), addison);
    auto book1Authors = std::any_cast<EList<EObject*>*>(book1->eGet(bookCls->getEStructuralFeature("authors")));
    book1Authors->add(ada);
    book1Authors->add(alan);

    EObject* book2 = createObject(bookCls);
    book2->eSet(bookCls->getEStructuralFeature("title"), std::string("Just for Fun"));
    book2->eSet(bookCls->getEStructuralFeature("isbn"), std::string("978-0066620732"));
    book2->eSet(bookCls->getEStructuralFeature("pages"), int(288));
    book2->eSet(bookCls->getEStructuralFeature("price"), 27.99);
    book2->eSet(bookCls->getEStructuralFeature("category"),
                std::any(static_cast<EEnumLiteral*>(bookCategory->getELiteral("NON_FICTION"))));
    book2->eSet(bookCls->getEStructuralFeature("publisher"), oreilly);
    auto book2Authors = std::any_cast<EList<EObject*>*>(book2->eGet(bookCls->getEStructuralFeature("authors")));
    book2Authors->add(linus);

    auto booksList = std::any_cast<EList<EObject*>*>(library->eGet(libBooks));
    booksList->add(book1);
    booksList->add(book2);

    // Magazine
    EObject* mag = createObject(magazineCls);
    mag->eSet(magazineCls->getEStructuralFeature("title"), std::string("Communications of the ACM"));
    mag->eSet(magazineCls->getEStructuralFeature("category"),
              std::any(static_cast<EEnumLiteral*>(bookCategory->getELiteral("SCIENCE"))));
    mag->eSet(magazineCls->getEStructuralFeature("issueNumber"), int(67));
    mag->eSet(magazineCls->getEStructuralFeature("periodicity"), int(12));
    auto magList = std::any_cast<EList<EObject*>*>(library->eGet(libMagazines));
    magList->add(mag);

    return library;
}

}  // namespace DynamicInstanceFactory

// ===== Demo 2: XMI 序列化/反序列化 =====
namespace XmiSerializerDemo {

// 递归比较 containment 结构
void assertSameStructure(EObject* a, EObject* b, int depth = 0) {
    if (std::string(a->eClass()->getName()) != std::string(b->eClass()->getName())) {
        throw std::runtime_error("Class mismatch: " + std::string(a->eClass()->getName())
                                 + " vs " + std::string(b->eClass()->getName()));
    }
    for (auto* attr : a->eClass()->getEAttributes()) {
        std::any va = a->eGet(attr);
        std::any vb = b->eGet(attr);
        bool eq = false;
        if (!va.has_value() && !vb.has_value()) eq = true;
        else if (va.type() == typeid(std::string) && vb.type() == typeid(std::string))
            eq = std::any_cast<std::string>(va) == std::any_cast<std::string>(vb);
        else if (va.type() == typeid(int) && vb.type() == typeid(int))
            eq = std::any_cast<int>(va) == std::any_cast<int>(vb);
        else if (va.type() == typeid(double) && vb.type() == typeid(double))
            eq = std::any_cast<double>(va) == std::any_cast<double>(vb);
        else if (va.type() == typeid(EEnumLiteral*) && vb.type() == typeid(EEnumLiteral*)) {
            auto* la = std::any_cast<EEnumLiteral*>(va);
            auto* lb = std::any_cast<EEnumLiteral*>(vb);
            eq = (la == nullptr && lb == nullptr) ||
                 (la && lb && std::string(la->getName()) == std::string(lb->getName()));
        }
        if (!eq) {
            std::ostringstream os; os << "Attribute mismatch on " << attr->getName() << " at depth " << depth
                                       << " (types " << va.type().name() << " vs " << vb.type().name() << ")";
            throw std::runtime_error(os.str());
        }
    }
    for (auto* ref : a->eClass()->getEReferences()) {
        if (!ref->isContainment()) continue;
        std::any ra = a->eGet(ref);
        std::any rb = b->eGet(ref);
        if (ref->getUpperBound() < 0 || ref->getUpperBound() > 1) {
            auto* ca = std::any_cast<EList<EObject*>*>(ra);
            auto* cb = std::any_cast<EList<EObject*>*>(rb);
            if (!ca || !cb || ca->size() != cb->size())
                throw std::runtime_error("Containment list size mismatch on " + std::string(ref->getName()));
            for (size_t i = 0; i < ca->size(); ++i)
                assertSameStructure(ca->get(i), cb->get(i), depth + 1);
        } else {
            EObject* oa = ra.has_value() ? std::any_cast<EObject*>(ra) : nullptr;
            EObject* ob = rb.has_value() ? std::any_cast<EObject*>(rb) : nullptr;
            if (oa && ob) assertSameStructure(oa, ob, depth + 1);
            else if (oa || ob) throw std::runtime_error("Containment null mismatch");
        }
    }
}

void run(const std::string& xmiFilePath) {
    std::cout << "\n========== Demo 2: XMI 序列化/反序列化 ==========\n";
    emf::ecore::EPackage* pkg = EcoreModelBuilder::build();
    EPackageRegistry::instance().put(EcoreModelBuilder::NS_URI, pkg);

    EObject* library = DynamicInstanceFactory::createSampleLibrary(pkg);
    std::cout << "[XmiSerializerDemo] In-memory library tree:\n";
    dumpTree(library, 0);

    auto res = emf::xmi::XMIResourceFactory::createResourceFor(URI::createFileURI(xmiFilePath));
    res->addToContents(library);
    std::ofstream ofs(xmiFilePath);
    res->save(ofs);
    ofs.close();
    std::cout << "[XmiSerializerDemo] Saved XMI to: " << xmiFilePath << "\n";

    auto res2 = emf::xmi::XMIResourceFactory::createResourceFor(URI::createFileURI(xmiFilePath));
    std::ifstream ifs(xmiFilePath);
    res2->load(ifs);
    ifs.close();
    if (res2->getContents().empty()) throw std::runtime_error("reload returned empty contents");
    EObject* restored = res2->getContents().front();
    std::cout << "[XmiSerializerDemo] Loaded back root: " << restored->eClass()->getName()
              << " name=" << std::any_cast<std::string>(restored->eGet(restored->eClass()->getEStructuralFeature("name"))) << "\n";

    std::cout << "[XmiSerializerDemo] Restored tree:\n";
    dumpTree(restored, 0);

    assertSameStructure(library, restored);
    std::cout << "[XmiSerializerDemo] 结构一致 ✓\n";
}

}  // namespace XmiSerializerDemo

// ===== Demo 3: 变更通知 =====
namespace ChangeNotificationDemo {

class LabeledAdapter : public EAdapter {
public:
    explicit LabeledAdapter(std::string l) : label_(std::move(l)) {}
    void notifyChanged(const Notification& n) override {
        std::cout << "  " << label_ << ": eventType=" << Notification::eventTypeName(n.eventType())
                  << " feature=" << (n.feature() ? n.feature()->getName().c_str() : "<null>") << "\n";
    }
private:
    std::string label_;
};

class NameOnlyAdapter : public EAdapter {
public:
    void notifyChanged(const Notification& n) override {
        auto* sf = n.feature();
        if (sf && std::string(sf->getName()) == "name") {
            auto* eo = dynamic_cast<EObject*>(n.notifier());
            std::cout << "  [Name-Only] " << (eo ? eo->eClass()->getName() : "<notifier>")
                      << " name changed: old=" << (n.oldValue().has_value() ? "<v>" : "null")
                      << " new=" << (n.newValue().has_value() ? "<v>" : "null") << "\n";
        }
    }
};

void run() {
    std::cout << "\n========== Demo 3: 变更通知事件 ==========\n";

    emf::ecore::EPackage* pkg = EcoreModelBuilder::build();
    EObject* library = DynamicInstanceFactory::createSampleLibrary(pkg);
    EClass* libraryCls = library->eClass();
    EReference* booksRef = dynamic_cast<EReference*>(libraryCls->getEStructuralFeature("books"));
    EReference* authorsRef = dynamic_cast<EReference*>(libraryCls->getEStructuralFeature("authors"));
    EAttribute* nameAttr = dynamic_cast<EAttribute*>(libraryCls->getEStructuralFeature("name"));

    std::cout << "\n--- Part A: Plain Adapter on Library ---\n";
    library->addAdapter(new LabeledAdapter("[Library-Adapter]"));
    library->eSet(nameAttr, std::string("City Central Library (Renamed)"));
    auto books = std::any_cast<EList<EObject*>*>(library->eGet(booksRef));
    EObject* firstBook = books->get(0);
    EObject* newBook = DynamicInstanceFactory::createObject(dynamic_cast<EClass*>(pkg->getEClassifier("Book")));
    newBook->eSet(newBook->eClass()->getEStructuralFeature("title"), std::string("New Arrival"));
    books->add(newBook);
    books->removeByIndex(0);

    std::cout << "\n--- Part B: EContentAdapter (recursive) ---\n";
    auto* contentAdapter = new EContentAdapter();
    contentAdapter->addAdapterTo(library);
    library->addAdapter(contentAdapter);
    EObject* target = books->get(0);
    EAttribute* titleAttr = dynamic_cast<EAttribute*>(target->eClass()->getEStructuralFeature("title"));
    EAttribute* priceAttr = dynamic_cast<EAttribute*>(target->eClass()->getEStructuralFeature("price"));
    target->eSet(titleAttr, std::string("The Pragmatic Programmer (2nd Edition)"));
    target->eSet(priceAttr, 59.99);
    EClass* magCls = dynamic_cast<EClass*>(pkg->getEClassifier("Magazine"));
    EObject* mag = DynamicInstanceFactory::createObject(magCls);
    mag->eSet(magCls->getEStructuralFeature("title"), std::string("Scientific American"));
    auto magList = std::any_cast<EList<EObject*>*>(library->eGet(libraryCls->getEStructuralFeature("magazines")));
    magList->add(mag);

    std::cout << "\n--- Part C: Type-safe Adapter for 'name' changes only ---\n";
    auto authors = std::any_cast<EList<EObject*>*>(library->eGet(authorsRef));
    EObject* specificAuthor = authors->get(0);
    specificAuthor->addAdapter(new NameOnlyAdapter());
    EAttribute* authorName = dynamic_cast<EAttribute*>(specificAuthor->eClass()->getEStructuralFeature("name"));
    specificAuthor->eSet(authorName, std::string("Ada Lovelace (Updated)"));
    EAttribute* authorEmail = dynamic_cast<EAttribute*>(specificAuthor->eClass()->getEStructuralFeature("email"));
    specificAuthor->eSet(authorEmail, std::string("ada-updated@example.com"));

    std::cout << "\n--- Part D: Remove adapter (no more notifications) ---\n";
    library->eAdapters().clear();
    library->eSet(nameAttr, std::string("Should NOT print anything"));
    std::cout << "[ChangeNotificationDemo] Done.\n";
}

}  // namespace ChangeNotificationDemo

int main() {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    emf::xmi::XMIResourceFactory::registerDefaults();

    std::cout << "========== Demo 0: 构建 Ecore 元模型 ==========\n";
    EcoreModelBuilder::buildAndSave("/workspace/emf-cpp-demo/build/library.ecore");
    EPackageBase* reloaded = EcoreModelBuilder::load("/workspace/emf-cpp-demo/build/library.ecore");
    (void)reloaded;

    std::cout << "\n========== Demo 1: GenModel skipped ==========\n";
    std::cout << "  (C++ 端用 emf-ecore-codegen 生成静态 .h/.cpp，对应 Java 的 .genmodel)\n";

    XmiSerializerDemo::run("/workspace/emf-cpp-demo/build/library.xmi");
    ChangeNotificationDemo::run();

    std::cout << "\n========== All Demos Done ==========\n";
    std::cout << "Artifacts under /workspace/emf-cpp-demo/build/ :\n";
    std::cout << "  - library.ecore (Ecore metamodel)\n";
    std::cout << "  - library.xmi   (Sample XMI instance)\n";
    return 0;
}
