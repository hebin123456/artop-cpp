// GenModelLoaderTests.cpp —— 单元测试：GenModelLoader
// 对应 Java: GenModelUtil（解析 .genmodel XMI）
#include "test_main.h"
#include "test_helpers.h"

#include "emf/ecore/codegen/GenModel.h"
#include "emf/ecore/codegen/GenModelLoader.h"
#include "emf/ecore/EcorePackage.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/common/EPackage.h"
#include "emf/common/Resource.h"
#include "emf/xmi/XMIResource.h"
#include "emf/xmi/XMIResourceFactory.h"
#include "emf/common/URI.h"

#include <cstdlib>
#include <filesystem>
#include <fstream>
#include <memory>
#include <string>

using namespace emf;
using namespace emf::ecore;
using namespace emf::ecore::codegen;

namespace {
const char* kSampleEcorePath = EMFCPP_SOURCE_DIR "/emf-ecore-codegen/tests/samples/library.ecore";
}  // namespace

// ===== 1. wrapEcore: 不需要 .genmodel 也能走 GenModel 路线 =====
EMF_TEST(GenModelLoader_wrapEcore_buildsGenPackage) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* pkg = EcoreFactory::instance().createEPackage();
    pkg->setName("library");
    pkg->setNsURI("http://example.com/library/1.0");
    pkg->setNsPrefix("library");
    auto* book = EcoreFactory::instance().createEClass();
    book->setName("Book");
    pkg->addEClassifier(book);
    auto* title = EcoreFactory::instance().createEAttribute();
    title->setName("title");
    title->setEAttributeType(EcorePackage::instance().getEDataType_EString());
    book->addEStructuralFeature(title);

    auto gm = GenModelLoader::wrapEcore(pkg, "emf");
    EXPECT_NOT_NULL(gm);
    EXPECT_EQ(gm->genPackages.size(), (size_t)1);
    EXPECT_EQ(gm->genPackages[0]->prefix, std::string("Library"));
    EXPECT_EQ(gm->genPackages[0]->basePackage, std::string("emf"));
    EXPECT_EQ(gm->genPackages[0]->genClasses.size(), (size_t)1);
    EXPECT_EQ(gm->genPackages[0]->genClasses[0]->getClassName(), std::string("Book"));
    EXPECT_EQ(gm->genPackages[0]->genClasses[0]->genFeatures.size(), (size_t)1);
    EXPECT_EQ(gm->genPackages[0]->genClasses[0]->genFeatures[0]->getFeatureName(), std::string("title"));
    EXPECT_TRUE(gm->genPackages[0]->genClasses[0]->genFeatures[0]->attribute);
    EXPECT_FALSE(gm->genPackages[0]->genClasses[0]->genFeatures[0]->reference);
    delete pkg;
}

// ===== 2. wrapEcore: prefix 首字母大写 =====
EMF_TEST(GenModelLoader_wrapEcore_capitalizesPrefix) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* pkg = EcoreFactory::instance().createEPackage();
    pkg->setName("myPkg");
    pkg->setNsURI("http://x/y");
    pkg->setNsPrefix("mp");
    auto gm = GenModelLoader::wrapEcore(pkg, "");
    EXPECT_EQ(gm->genPackages[0]->prefix, std::string("MyPkg"));
    delete pkg;
}

// ===== 3. loadFromString: 内联 .genmodel 字符串解析 =====
EMF_TEST(GenModelLoader_loadFromString_inlineGenModel) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    // 写一个临时 .ecore
    std::string outDir = makeTestDir("genmodel-loader/inline");
    std::string ecorePath = outDir + "/test.ecore";
    {
        std::ofstream f(ecorePath);
        f << "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
          << "<ecore:EPackage xmi:version=\"2.0\"\n"
          << "    xmlns:xmi=\"http://www.omg.org/XMI\"\n"
          << "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
          << "    xmlns:ecore=\"http://www.eclipse.org/emf/2002/Ecore\"\n"
          << "    name=\"tiny\" nsURI=\"http://x/tiny/1.0\" nsPrefix=\"tiny\">\n"
          << "  <eClassifiers xsi:type=\"ecore:EClass\" name=\"Node\">\n"
          << "    <eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"id\"\n"
          << "        eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString\"/>\n"
          << "  </eClassifiers>\n"
          << "</ecore:EPackage>\n";
    }
    std::string gmXml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        "<genmodel:GenModel xmi:version=\"2.0\"\n"
        "    xmlns:xmi=\"http://www.omg.org/XMI\"\n"
        "    xmlns:genmodel=\"http://www.eclipse.org/emf/2002/GenModel\"\n"
        "    modelDirectory=\"/tmp/\" modelName=\"Tiny\" modelPluginID=\"com.example.tiny\"\n"
        "    forceOverwrite=\"true\" bundleManifest=\"false\">\n"
        "  <genPackages prefix=\"Tiny\" basePackage=\"com.example.tiny\">\n"
        "    <ecorePackage href=\"test.ecore#/\"/>\n"
        "    <genClasses ecoreClass=\"test.ecore#//Node\"/>\n"
        "  </genPackages>\n"
        "</genmodel:GenModel>\n";
    auto gm = GenModelLoader::loadFromString(gmXml, outDir);
    EXPECT_NOT_NULL(gm);
    EXPECT_EQ(gm->modelName, std::string("Tiny"));
    EXPECT_EQ(gm->modelPluginID, std::string("com.example.tiny"));
    EXPECT_EQ(gm->genPackages.size(), (size_t)1);
    EXPECT_EQ(gm->genPackages[0]->prefix, std::string("Tiny"));
    EXPECT_EQ(gm->genPackages[0]->basePackage, std::string("com.example.tiny"));
    EXPECT_EQ(gm->genPackages[0]->genClasses.size(), (size_t)1);
    EXPECT_EQ(gm->genPackages[0]->genClasses[0]->getClassName(), std::string("Node"));
}

// ===== 4. loadFromString: 解析 genFeature 关联 =====
EMF_TEST(GenModelLoader_loadFromString_parsesGenFeatures) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    std::string outDir = makeTestDir("genmodel-loader/features");
    std::string ecorePath = outDir + "/demo.ecore";
    {
        std::ofstream f(ecorePath);
        f << "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
          << "<ecore:EPackage xmi:version=\"2.0\"\n"
          << "    xmlns:xmi=\"http://www.omg.org/XMI\"\n"
          << "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
          << "    xmlns:ecore=\"http://www.eclipse.org/emf/2002/Ecore\"\n"
          << "    name=\"demo\" nsURI=\"http://x/demo/1.0\" nsPrefix=\"demo\">\n"
          << "  <eClassifiers xsi:type=\"ecore:EClass\" name=\"Item\">\n"
          << "    <eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"name\"\n"
          << "        eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString\"/>\n"
          << "    <eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"count\"\n"
          << "        eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EInt\"/>\n"
          << "  </eClassifiers>\n"
          << "</ecore:EPackage>\n";
    }
    std::string gmXml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        "<genmodel:GenModel xmi:version=\"2.0\"\n"
        "    xmlns:xmi=\"http://www.omg.org/XMI\"\n"
        "    xmlns:genmodel=\"http://www.eclipse.org/emf/2002/GenModel\"\n"
        "    modelDirectory=\"/tmp/\" modelName=\"Demo\" modelPluginID=\"com.example.demo\">\n"
        "  <genPackages prefix=\"Demo\" basePackage=\"com.example.demo\">\n"
        "    <ecorePackage href=\"demo.ecore#/\"/>\n"
        "    <genClasses ecoreClass=\"demo.ecore#//Item\">\n"
        "      <genFeatures ecoreFeature=\"ecore:EAttribute demo.ecore#//Item/name\"/>\n"
        "      <genFeatures ecoreFeature=\"ecore:EAttribute demo.ecore#//Item/count\"/>\n"
        "    </genClasses>\n"
        "  </genPackages>\n"
        "</genmodel:GenModel>\n";
    auto gm = GenModelLoader::loadFromString(gmXml, outDir);
    EXPECT_NOT_NULL(gm);
    EXPECT_EQ(gm->genPackages.size(), (size_t)1);
    auto& gc = gm->genPackages[0]->genClasses[0];
    EXPECT_EQ(gc->getClassName(), std::string("Item"));
    EXPECT_EQ(gc->genFeatures.size(), (size_t)2);
    // name -> EString
    EXPECT_EQ(gc->genFeatures[0]->getFeatureName(), std::string("name"));
    EXPECT_EQ(gc->genFeatures[0]->type, std::string("EString"));
    EXPECT_TRUE(gc->genFeatures[0]->attribute);
    // count -> EInt
    EXPECT_EQ(gc->genFeatures[1]->getFeatureName(), std::string("count"));
    EXPECT_EQ(gc->genFeatures[1]->type, std::string("EInt"));
}

// ===== 5. wrapEcore: EReference 解析 =====
EMF_TEST(GenModelLoader_wrapEcore_recognizesReference) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* pkg = EcoreFactory::instance().createEPackage();
    pkg->setName("ref");
    pkg->setNsURI("http://x/ref/1.0");
    pkg->setNsPrefix("ref");
    auto* a = EcoreFactory::instance().createEClass();
    a->setName("A");
    auto* b = EcoreFactory::instance().createEClass();
    b->setName("B");
    pkg->addEClassifier(a);
    pkg->addEClassifier(b);
    auto* ref = EcoreFactory::instance().createEReference();
    ref->setName("link");
    ref->setLowerBound(0);
    ref->setUpperBound(1);
    ref->setContainment(true);
    ref->setEReferenceType(b);
    a->addEStructuralFeature(ref);
    auto gm = GenModelLoader::wrapEcore(pkg, "");
    auto& gc = gm->genPackages[0]->genClasses[0];  // A
    EXPECT_EQ(gc->getClassName(), std::string("A"));
    EXPECT_EQ(gc->genFeatures.size(), (size_t)1);
    auto& gf = gc->genFeatures[0];
    EXPECT_EQ(gf->getFeatureName(), std::string("link"));
    EXPECT_TRUE(gf->reference);
    EXPECT_FALSE(gf->attribute);
    EXPECT_TRUE(gf->containment);
    EXPECT_EQ(gf->type, std::string("B"));
    delete pkg;
}
