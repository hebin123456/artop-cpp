// diag_esupertypes.cpp — 诊断 eSuperTypes/eOpposite 加载是否正确
#include "emf/ecore/EcorePackage.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/xmi/XMIResource.h"
#include "emf/xmi/XMIResourceFactory.h"
#include <iostream>
#include <fstream>
#include <sstream>

int main(int argc, char** argv) {
    std::string path = argc > 1 ? argv[1] : "/workspace/cpp/demo/build/library.ecore";
    emf::ecore::EcorePackage::initialize();
    emf::xmi::XMIResourceFactory::registerDefaults();
    auto res = emf::xmi::XMIResourceFactory::createResourceFor(emf::common::URI::createFileURI(path));
    std::ifstream f(path, std::ios::binary);
    std::stringstream ss; ss << f.rdbuf();
    res->loadFromString(ss.str());

    auto& contents = res->getContents();
    if (contents.empty()) { std::cerr << "EMPTY\n"; return 1; }
    auto* pkg = dynamic_cast<emf::ecore::EPackage*>(contents[0]);
    if (!pkg) { std::cerr << "NOT EPACKAGE\n"; return 1; }
    std::cout << "pkg=" << pkg->getName() << " classifiers=" << pkg->getEClassifiers().size() << "\n";
    for (auto* c : pkg->getEClassifiers()) {
        auto* cls = dynamic_cast<emf::ecore::EClass*>(c);
        if (!cls) continue;
        std::cout << "  class " << cls->getName() << " superTypes=" << cls->getESuperTypes().size();
        for (auto* s : cls->getESuperTypes()) std::cout << " " << s->getName();
        std::cout << " isSetEGenericSuperTypes=" << cls->isSetEGenericSuperTypes() << "\n";
        for (auto* sf : cls->getEStructuralFeatures()) {
            auto* ref = dynamic_cast<emf::ecore::EReference*>(sf);
            if (!ref) continue;
            auto* opp = ref->getEOpposite();
            std::cout << "    ref " << ref->getName() << " opposite=";
            if (opp) {
                auto* oc = opp->getEContainingClass();
                std::cout << (oc ? oc->getName() : std::string("?")) << "/" << opp->getName();
            } else std::cout << "null";
            std::cout << "\n";
        }
    }
    return 0;
}
