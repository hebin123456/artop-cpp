// 验证 xmi 跨文件引用：通过 ResourceSet 加载 authors.xmi/publishers.xmi，
// 其 <books href="library.xmi#//@books.0"/> 应解析到 library.xmi 中的 Book 对象。
// 对齐 Java: ResourceSet.getEObject(URI, loadOnDemand) demand-load 跨文件引用。
#include "emf/xmi/XMIResourceSet.h"
#include "emf/xmi/XMIResource.h"
#include "emf/xmi/XMIResourceFactory.h"
#include "emf/ecore/EcorePackage.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/common/EPackageRegistry.h"
#include "emf/common/URI.h"

#include <cstdio>
#include <fstream>
#include <sstream>
#include <string>

using namespace emf;

// 内联 ecore 元模型（nsURI 匹配 multi-xmi-java 样本）
static const char* kMetaEcore =
    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
    "<ecore:EPackage xmi:version=\"2.0\"\n"
    "    xmlns:xmi=\"http://www.omg.org/XMI\"\n"
    "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
    "    xmlns:ecore=\"http://www.eclipse.org/emf/2002/Ecore\"\n"
    "    name=\"library\" nsURI=\"http://example.com/emfdemo/library\" nsPrefix=\"library\">\n"
    "  <eClassifiers xsi:type=\"ecore:EClass\" name=\"Library\">\n"
    "    <eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"name\"\n"
    "        eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString\"/>\n"
    "    <eStructuralFeatures xsi:type=\"ecore:EReference\" name=\"books\" upperBound=\"-1\"\n"
    "        eType=\"#//Book\" containment=\"true\"/>\n"
    "    <eStructuralFeatures xsi:type=\"ecore:EReference\" name=\"authors\" upperBound=\"-1\"\n"
    "        eType=\"#//Author\" containment=\"true\"/>\n"
    "    <eStructuralFeatures xsi:type=\"ecore:EReference\" name=\"publishers\" upperBound=\"-1\"\n"
    "        eType=\"#//Publisher\" containment=\"true\"/>\n"
    "  </eClassifiers>\n"
    "  <eClassifiers xsi:type=\"ecore:EClass\" name=\"Book\">\n"
    "    <eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"title\"\n"
    "        eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString\"/>\n"
    "    <eStructuralFeatures xsi:type=\"ecore:EReference\" name=\"authors\" upperBound=\"-1\"\n"
    "        eType=\"#//Author\"/>\n"
    "    <eStructuralFeatures xsi:type=\"ecore:EReference\" name=\"publisher\"\n"
    "        eType=\"#//Publisher\"/>\n"
    "  </eClassifiers>\n"
    "  <eClassifiers xsi:type=\"ecore:EClass\" name=\"Author\">\n"
    "    <eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"name\"\n"
    "        eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString\"/>\n"
    "    <eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"email\"\n"
    "        eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString\"/>\n"
    "    <eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"birthYear\"\n"
    "        eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EInt\"/>\n"
    "    <eStructuralFeatures xsi:type=\"ecore:EReference\" name=\"books\" upperBound=\"-1\"\n"
    "        eType=\"#//Book\"/>\n"
    "  </eClassifiers>\n"
    "  <eClassifiers xsi:type=\"ecore:EClass\" name=\"Publisher\">\n"
    "    <eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"name\"\n"
    "        eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString\"/>\n"
    "    <eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"email\"\n"
    "        eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString\"/>\n"
    "    <eStructuralFeatures xsi:type=\"ecore:EReference\" name=\"address\"\n"
    "        eType=\"#//Address\" containment=\"true\"/>\n"
    "    <eStructuralFeatures xsi:type=\"ecore:EReference\" name=\"books\" upperBound=\"-1\"\n"
    "        eType=\"#//Book\"/>\n"
    "  </eClassifiers>\n"
    "  <eClassifiers xsi:type=\"ecore:EClass\" name=\"Address\">\n"
    "    <eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"street\"\n"
    "        eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString\"/>\n"
    "    <eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"city\"\n"
    "        eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString\"/>\n"
    "    <eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"country\"\n"
    "        eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString\"/>\n"
    "    <eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"zipCode\"\n"
    "        eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString\"/>\n"
    "  </eClassifiers>\n"
    "</ecore:EPackage>\n";

int main(int argc, char** argv) {
    std::string dir = (argc > 1) ? std::string(argv[1]) + "/"
                                 : "emf-xmi/tests/samples/multi-xmi-java/";

    ecore::EcoreFactory::initialize();
    ecore::EcorePackage::initialize();
    xmi::XMIResourceFactory::registerDefaults();

    // 1. 加载并注册元模型
    {
        xmi::XMIResource metaRes;
        metaRes.loadFromString(kMetaEcore);
        auto* pkg = dynamic_cast<ecore::EPackage*>(metaRes.getContents().front());
        if (!pkg) { std::fprintf(stderr, "FAIL: meta pkg null\n"); return 1; }
        common::EPackageRegistry::instance().put(pkg->getNsURI(), pkg);
        std::fprintf(stderr, "[meta] registered nsURI=%s, classifiers=%zu\n",
                     pkg->getNsURI().c_str(), pkg->getEClassifiers().size());
    }

    // 2. 通过 ResourceSet 加载 authors.xmi（含跨文件 href 到 library.xmi）
    xmi::XMIResourceSet rs;
    common::URI authorsUri = common::URI::createFileURI(dir + "authors.xmi");
    auto* authorsRes = rs.createResource(authorsUri);
    try {
        authorsRes->load();   // 从文件加载（设置 URI，触发跨文件引用 demand-load）
    } catch (const std::exception& ex) {
        std::fprintf(stderr, "[authors] load() threw: %s\n", ex.what());
        return 1;
    }

    // 对照：直接读文件字符串 + loadFromString
    {
        std::ifstream f(dir + "authors.xmi", std::ios::binary);
        std::stringstream ss; ss << f.rdbuf();
        std::string src = ss.str();
        xmi::XMIResource tmp;
        tmp.loadFromString(src);
        std::fprintf(stderr, "[authors via loadFromString] roots=%zu (sanity check)\n",
                     tmp.getContents().size());
    }

    auto& authors = authorsRes->getContents();
    std::fprintf(stderr, "[authors] uri=%s roots=%zu\n", authorsUri.toString().c_str(), authors.size());
    for (auto& e : authorsRes->getErrors()) std::fprintf(stderr, "[authors err] %s\n", e.c_str());
    for (auto& w : authorsRes->getWarnings()) std::fprintf(stderr, "[authors warn] %s\n", w.c_str());
    if (authors.size() != 3) { std::fprintf(stderr, "FAIL: expected 3 authors\n"); return 1; }

    // 3. 验证 Author[0] 的 books 引用解析到了 library.xmi 的 Book
    auto* author0 = authors[0];
    auto* cls = author0->eClass();
    auto* booksFeat = cls->getEStructuralFeature("books");
    if (!booksFeat) { std::fprintf(stderr, "FAIL: no books feature\n"); return 1; }

    auto booksV = author0->eGet(booksFeat);
    std::vector<common::EObject*> bookList;
    if (auto* elist = std::any_cast<common::EList<common::EObject*>*>(&booksV)) {
        if (*elist) for (auto* o : **elist) bookList.push_back(o);
    } else if (auto* listPtr = std::any_cast<std::vector<common::EObject*>*>(&booksV)) {
        if (*listPtr) bookList = **listPtr;
    }

    std::fprintf(stderr, "[author0] books count=%zu\n", bookList.size());
    if (bookList.empty()) {
        std::fprintf(stderr, "FAIL: cross-file books reference NOT resolved\n");
        return 1;
    }

    // 4. 验证解析到的 Book 来自 library.xmi（title="The Pragmatic Programmer"）
    auto* book0 = bookList[0];
    auto* bookCls = book0->eClass();
    auto* titleFeat = bookCls->getEStructuralFeature("title");
    std::string title;
    if (titleFeat) {
        auto tv = book0->eGet(titleFeat);
        title = std::any_cast<std::string>(tv);
    }
    std::fprintf(stderr, "[resolved book0] title=\"%s\"\n", title.c_str());
    if (title != "The Pragmatic Programmer") {
        std::fprintf(stderr, "FAIL: wrong book title\n");
        return 1;
    }

    // 5. 验证 library.xmi 被 demand-load 进 ResourceSet
    auto& allRes = rs.getResources();
    std::fprintf(stderr, "[resourceset] resources=%zu\n", allRes.size());
    if (allRes.size() < 2) {
        std::fprintf(stderr, "FAIL: library.xmi not demand-loaded into ResourceSet\n");
        return 1;
    }

    std::fprintf(stderr, "PASS: xmi cross-file reference resolved via ResourceSet demand-load\n");
    return 0;
}
