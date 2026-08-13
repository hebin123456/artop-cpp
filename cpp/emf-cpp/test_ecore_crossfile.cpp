// 验证 ecore 跨文件 demand-load：library_ext.ecore 通过文件路径 href
// (eSuperTypes="library.ecore#//Library") 引用 library.ecore，
// 应通过 ResourceSet 自动加载 library.ecore 并解析引用。
// 对齐 Java: XMLHelperImpl.getEObject → ResourceSet.getEObject(URI, loadOnDemand)
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

int main(int argc, char** argv) {
    std::string dir = (argc > 1) ? std::string(argv[1]) + "/"
                                 : "emf-xmi/tests/samples/multi/";

    ecore::EcoreFactory::initialize();
    ecore::EcorePackage::initialize();
    xmi::XMIResourceFactory::registerDefaults();

    // 通过 ResourceSet 加载 library_ext.ecore（含文件路径 href 到 library.ecore）
    xmi::XMIResourceSet rs;
    common::URI extUri = common::URI::createFileURI(dir + "library_ext.ecore");
    auto* extRes = rs.createResource(extUri);
    try {
        extRes->load();
    } catch (const std::exception& ex) {
        std::fprintf(stderr, "FAIL: load threw: %s\n", ex.what());
        return 1;
    }

    auto& contents = extRes->getContents();
    std::fprintf(stderr, "[ext] roots=%zu\n", contents.size());
    if (contents.empty()) { std::fprintf(stderr, "FAIL: no roots\n"); return 1; }

    auto* extPkg = dynamic_cast<ecore::EPackage*>(contents.front());
    if (!extPkg) { std::fprintf(stderr, "FAIL: root not EPackage\n"); return 1; }
    std::fprintf(stderr, "[ext] pkg name=%s classifiers=%zu\n",
                 extPkg->getName().c_str(), extPkg->getEClassifiers().size());

    // AnnotatedLibrary 应继承 library.ecore 的 Library
    auto* annLib = dynamic_cast<ecore::EClass*>(extPkg->getEClassifier("AnnotatedLibrary"));
    if (!annLib) { std::fprintf(stderr, "FAIL: no AnnotatedLibrary\n"); return 1; }

    auto& supers = annLib->getESuperTypes();
    std::fprintf(stderr, "[AnnotatedLibrary] superTypes=%zu\n", supers.size());
    if (supers.empty()) {
        std::fprintf(stderr, "FAIL: eSuperTypes not resolved (cross-file demand-load failed)\n");
        return 1;
    }
    std::fprintf(stderr, "[AnnotatedLibrary] super[0]=%s\n", supers[0]->getName().c_str());
    if (supers[0]->getName() != "Library") {
        std::fprintf(stderr, "FAIL: wrong supertype\n");
        return 1;
    }

    // highlighted 的 eType 应是 library.ecore 的 Book
    auto* highlighted = annLib->getEReference("highlighted");
    if (!highlighted) { std::fprintf(stderr, "FAIL: no highlighted ref\n"); return 1; }
    auto* bookType = highlighted->getEType();
    if (!bookType) {
        std::fprintf(stderr, "FAIL: highlighted.eType not resolved\n");
        return 1;
    }
    std::fprintf(stderr, "[highlighted] eType=%s\n", bookType->getName().c_str());
    if (bookType->getName() != "Book") {
        std::fprintf(stderr, "FAIL: wrong eType\n");
        return 1;
    }

    // library.ecore 应被 demand-load 进 ResourceSet
    auto& allRes = rs.getResources();
    std::fprintf(stderr, "[resourceset] resources=%zu\n", allRes.size());
    if (allRes.size() < 2) {
        std::fprintf(stderr, "FAIL: library.ecore not demand-loaded\n");
        return 1;
    }

    std::fprintf(stderr, "PASS: ecore cross-file demand-load resolved\n");
    return 0;
}
