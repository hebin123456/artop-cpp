#include "emf/xmi/XMIResource.h"
#include "emf/xmi/XMIResourceFactory.h"
#include "emf/common/EPackageRegistry.h"
#include "emf/ecore/EcorePackage.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/common/URI.h"
#include <fstream>
#include <iostream>
#include <sstream>
using namespace emf;
int main(int argc, char** argv) {
    ecore::EcoreFactory::initialize();
    ecore::EcorePackage::initialize();
    xmi::XMIResourceFactory::registerDefaults();
    std::ifstream f(argv[1], std::ios::binary);
    std::stringstream ss; ss << f.rdbuf();
    std::string original = ss.str();
    auto res = xmi::XMIResourceFactory::createResourceFor(common::URI::createFileURI(argv[1]));
    res->loadFromString(original);
    std::string saved = res->saveToString();
    std::ofstream out(argv[2], std::ios::binary);
    out << saved;
    if (saved == original) { std::printf("IDENTICAL %zu\n", saved.size()); }
    else { std::printf("DIFFER orig=%zu saved=%zu\n", original.size(), saved.size()); }
    return 0;
}
