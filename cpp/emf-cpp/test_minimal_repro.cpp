// Minimal repro: does loadFromString(authors.xmi) give 3 roots standalone?
#include "emf/xmi/XMIResource.h"
#include "emf/xmi/XMIResourceFactory.h"
#include "emf/ecore/EcorePackage.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/common/EPackageRegistry.h"

#include <cstdio>
#include <fstream>
#include <sstream>
#include <string>

using namespace emf;

static const char* kEmfDemoLibraryEcore =
    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
    "<ecore:EPackage xmi:version=\"2.0\"\n"
    "    xmlns:xmi=\"http://www.omg.org/XMI\"\n"
    "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
    "    xmlns:ecore=\"http://www.eclipse.org/emf/2002/Ecore\"\n"
    "    name=\"library\" nsURI=\"http://example.com/emfdemo/library\" nsPrefix=\"library\">\n"
    "  <eClassifiers xsi:type=\"ecore:EClass\" name=\"Library\">\n"
    "    <eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"name\"\n"
    "        eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString\"/>\n"
    "  </eClassifiers>\n"
    "  <eClassifiers xsi:type=\"ecore:EClass\" name=\"Author\">\n"
    "    <eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"name\"\n"
    "        eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString\"/>\n"
    "    <eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"email\"\n"
    "        eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString\"/>\n"
    "  </eClassifiers>\n"
    "</ecore:EPackage>\n";

static std::string readAll(const std::string& path) {
    std::ifstream f(path);
    if (!f) return "";
    std::stringstream ss; ss << f.rdbuf();
    return ss.str();
}

int main() {
    ecore::EcoreFactory::initialize();
    ecore::EcorePackage::initialize();
    xmi::XMIResourceFactory::registerDefaults();

    // 加载并注册元模型
    {
        xmi::XMIResource res;
        res.loadFromString(kEmfDemoLibraryEcore);
        auto* pkg = dynamic_cast<ecore::EPackage*>(res.getContents().front());
        std::fprintf(stderr, "[meta] nsURI=%s prefix=%s classifiers=%zu\n",
                     pkg->getNsURI().c_str(), pkg->getNsPrefix().c_str(),
                     pkg->getEClassifiers().size());
        auto* reg = common::EPackageRegistry::instance().get(pkg->getNsURI());
        std::fprintf(stderr, "[meta] in registry? %s\n", reg ? "yes" : "no");
    }

    // 加载 authors.xmi
    std::string src = readAll("emf-xmi/tests/samples/multi-xmi-java/authors.xmi");
    std::fprintf(stderr, "[authors] file size=%zu\n", src.size());
    xmi::XMIResource res;
    res.loadFromString(src);
    std::fprintf(stderr, "[authors] roots=%zu\n", res.getContents().size());
    return 0;
}
