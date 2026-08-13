// 端到端探针：加载 library.ecore → 动态实例化 → 修改 → 存 .xmi → 重载对比
#include "emf/xmi/XMIResource.h"
#include "emf/xmi/XMIResourceFactory.h"
#include "emf/common/EPackageRegistry.h"
#include "emf/ecore/EcorePackage.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/common/URI.h"

#include <cstdio>
#include <fstream>
#include <sstream>
#include <string>
#include <vector>

using namespace emf;
using emf::ecore::EPackage;
using emf::ecore::EClass;
using emf::ecore::EClassifier;
using emf::ecore::EStructuralFeature;
using emf::ecore::EAttribute;
using emf::ecore::EReference;
using emf::ecore::EFactory;
using emf::ecore::EcorePackage;
using emf::ecore::EcoreFactory;

static std::string readAll(const std::string& path) {
    std::ifstream f(path);
    std::stringstream ss; ss << f.rdbuf();
    return ss.str();
}

static int failures = 0;
#define CHECK(cond, msg) do { if (cond) { std::printf("[OK] %s\n", msg); } else { std::printf("[FAIL] %s\n", msg); ++failures; } } while(0)

int main() {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    emf::xmi::XMIResourceFactory::registerDefaults();

    std::string ecorePath = "/workspace/cpp/emf-cpp/emf-ecore-codegen/tests/samples/library.ecore";
    std::string ecoreXml = readAll(ecorePath);
    CHECK(!ecoreXml.empty(), "读取 library.ecore 成功");
    if (ecoreXml.empty()) return 1;

    // (1) 加载 .ecore
    auto res = emf::xmi::XMIResourceFactory::createResourceFor(common::URI::createFileURI(ecorePath));
    res->loadFromString(ecoreXml);
    CHECK(!res->getContents().empty(), "加载 .ecore 得到 contents 非空");
    auto* pkg = dynamic_cast<EPackage*>(res->getContents().front());
    CHECK(pkg != nullptr, "根对象是 EPackage");
    if (!pkg) return 1;
    std::printf("  pkg name=%s nsURI=%s classifiers=%zu\n",
                pkg->getName().c_str(), pkg->getNsURI().c_str(), pkg->getEClassifiers().size());
    for (auto* c : pkg->getEClassifiers()) {
        std::printf("    classifier: %s\n", c ? c->getName().c_str() : "<null>");
    }

    // 注册包到 registry（便于 .xmi 实例加载时按 nsURI 找到）
    if (!pkg->getNsURI().empty()) {
        common::EPackageRegistry::instance().put(pkg->getNsURI(), pkg);
    }

    // (2) 找 Library / Book / Writer EClass
    EClass* libClass = nullptr;
    EClass* bookClass = nullptr;
    EClass* writerClass = nullptr;
    for (auto* c : pkg->getEClassifiers()) {
        auto* cls = dynamic_cast<EClass*>(c);
        if (!cls) continue;
        if (cls->getName() == "Library") libClass = cls;
        else if (cls->getName() == "Book") bookClass = cls;
        else if (cls->getName() == "Writer") writerClass = cls;
    }
    CHECK(libClass != nullptr, "找到 Library EClass");
    CHECK(bookClass != nullptr, "找到 Book EClass");
    CHECK(writerClass != nullptr, "找到 Writer EClass");

    if (libClass) {
        std::printf("  Library features:\n");
        for (auto* sf : libClass->getEAllStructuralFeatures()) {
            std::printf("    - %s (many=%d)\n", sf->getName().c_str(), sf->isMany() ? 1 : 0);
        }
    }

    // (3) 动态实例化
    EFactory* factory = pkg->getEFactoryInstance();
    CHECK(factory != nullptr, "EPackage 有 EFactoryInstance");
    if (!factory) return 1;

    auto* libObj = factory->create(libClass);
    CHECK(libObj != nullptr, "create(Library) 成功");
    auto* bookObj = factory->create(bookClass);
    CHECK(bookObj != nullptr, "create(Book) 成功");
    auto* writerObj = factory->create(writerClass);
    CHECK(writerObj != nullptr, "create(Writer) 成功");

    if (!libObj || !bookObj || !writerObj) return 1;

    // (4) 修改：set name / title / pages
    auto setAttr = [](common::EObject* obj, EClass* cls, const std::string& fname, const std::string& val) {
        auto* sf = cls->getEStructuralFeature(fname);
        if (!sf) { std::printf("  [WARN] feature %s not found on %s\n", fname.c_str(), cls->getName().c_str()); return false; }
        obj->eSet(sf, std::any(val));
        return true;
    };
    auto setIntAttr = [](common::EObject* obj, EClass* cls, const std::string& fname, int val) {
        auto* sf = cls->getEStructuralFeature(fname);
        if (!sf) { std::printf("  [WARN] feature %s not found on %s\n", fname.c_str(), cls->getName().c_str()); return false; }
        obj->eSet(sf, std::any(val));
        return true;
    };

    CHECK(setAttr(libObj, libClass, "name", "My Library"), "Library.name 设置");
    CHECK(setAttr(bookObj, bookClass, "title", "C++ Book"), "Book.title 设置");
    CHECK(setIntAttr(bookObj, bookClass, "pages", 42), "Book.pages 设置");
    CHECK(setAttr(writerObj, writerClass, "name", "Author One"), "Writer.name 设置");

    // (5) 把 book 加入 library.books (containment many)
    {
        auto* booksSf = libClass->getEStructuralFeature("books");
        CHECK(booksSf != nullptr, "Library.books feature 存在");
        if (booksSf) {
            auto v = libObj->eGet(booksSf);
        if (auto* elist = std::any_cast<common::EList<common::EObject*>*>(&v)) {
            CHECK(*elist != nullptr, "Library.books 返回 EList 指针");
            if (*elist) {
                (*elist)->add(bookObj);
                std::printf("  Library.books size after add = %zu\n", (*elist)->size());
            }
        } else if (auto* listPtr = std::any_cast<std::vector<common::EObject*>*>(&v)) {
            CHECK(listPtr != nullptr && *listPtr != nullptr, "Library.books 返回 list 指针");
            if (listPtr && *listPtr) {
                (*listPtr)->push_back(bookObj);
                std::printf("  Library.books size after add = %zu\n", (*listPtr)->size());
            }
        }
        }
    }

    // (6) 保存为 .xmi
    emf::xmi::XMIResource outRes(common::URI::createFileURI("/workspace/cpp/emf-cpp/probe_out.xmi"));
    outRes.addToContents(libObj);
    std::string xmiOut = outRes.saveToString();
    std::printf("=== 保存的 XMI ===\n%s\n=== XMI 结束 ===\n", xmiOut.c_str());
    CHECK(!xmiOut.empty(), "saveToString 非空");
    CHECK(xmiOut.find("My Library") != std::string::npos, "XMI 含 Library name");
    CHECK(xmiOut.find("C++ Book") != std::string::npos, "XMI 含 Book title");

    { std::ofstream f("/workspace/cpp/emf-cpp/probe_out.xmi"); f << xmiOut; }

    // (7) 重新加载 .xmi
    auto res2 = emf::xmi::XMIResourceFactory::createResourceFor(common::URI::createFileURI("/workspace/cpp/emf-cpp/probe_out.xmi"));
    res2->loadFromString(xmiOut);
    CHECK(!res2->getContents().empty(), "重载 .xmi 得到 contents 非空");
    auto* reloadedLib = res2->getContents().empty() ? nullptr : res2->getContents().front();
    CHECK(reloadedLib != nullptr, "重载得到根对象");
    if (reloadedLib) {
        auto* nameSf = libClass->getEStructuralFeature("name");
        if (nameSf) {
            auto v = reloadedLib->eGet(nameSf);
            std::string name;
            if (auto* p = std::any_cast<std::string>(&v)) name = *p;
            CHECK(name == "My Library", "重载后 Library.name == 'My Library'");
            std::printf("  reloaded Library.name = %s\n", name.c_str());
        }
        auto* booksSf = libClass->getEStructuralFeature("books");
        if (booksSf) {
            auto v = reloadedLib->eGet(booksSf);
            size_t cnt = 0;
            common::EObject* rb = nullptr;
            if (auto* elist = std::any_cast<common::EList<common::EObject*>*>(&v)) {
                if (*elist) {
                    cnt = (*elist)->size();
                    if (cnt >= 1) rb = (*elist)->get(0);
                }
            } else if (auto* listPtr = std::any_cast<std::vector<common::EObject*>*>(&v)) {
                if (listPtr && *listPtr) {
                    cnt = (*listPtr)->size();
                    if (cnt >= 1) rb = (*listPtr)->at(0);
                }
            }
            std::printf("  reloaded Library.books size = %zu\n", cnt);
            CHECK(cnt == 1, "重载后 Library.books 有 1 个 book");
            if (rb) {
                auto* titleSf = bookClass->getEStructuralFeature("title");
                if (titleSf) {
                    auto tv = rb->eGet(titleSf);
                    std::string title;
                    if (auto* p = std::any_cast<std::string>(&tv)) title = *p;
                    std::printf("  reloaded Book.title = %s\n", title.c_str());
                    CHECK(title == "C++ Book", "重载后 Book.title == 'C++ Book'");
                }
            }
        }
    }

    std::printf("\n=== 探针结束: %d 个失败 ===\n", failures);
    return failures == 0 ? 0 : 1;
}
