// E2E_GenModelXmiCxxProducesTests.cpp —— 端到端：C++ 静态类型产 XMI → Java EMF 读
//
// 用户场景：
//   "把未覆盖的场景覆盖上，还有，现在你是不是只验证了java产，c++测，
//    反向的还没有，也要覆盖上，我的目标是两边等价替换"
//
// 设计：
//   1. CppGenerator 从 java_ref/library.ecore 生成 C++ 静态模型
//   2. driver_cpp_produces.cpp 用生成的 LibraryFactory / Library / Book /
//      Author / Publisher 构造数据，序列化为 library.xmi
//   3. C++ 端用 system() 调起 java -cp "build/classes:lib/*" com.example.emfdemo.XMIReader
//      验证 C++ 产出的 XMI 能被 Java EMF 加载并打印所有关键字段
//   4. 断言：Java XMIReader 退出码 0；stdout 含全部关键字段
//
// 关键点（区别于已存在的 typed-multi-xmi 端到端测试）：
//   - 那个测试是 Java 产 XMI → C++ 读 → C++ save
//   - 本测试是 C++ 构造数据 → C++ save → Java 读（反向）
#include "test_main.h"
#include "emf/xmi/XMIResource.h"
#include "emf/xmi/XMIResourceSet.h"
#include "emf/xmi/XMIResourceFactory.h"
#include "emf/ecore/EcorePackage.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/ecore/EcoreMetadata.h"
#include "emf/ecore/codegen/CppGenerator.h"
#include "emf/common/EPackageRegistry.h"
#include "emf/common/URI.h"

#include <cstdio>
#include <filesystem>
#include <fstream>
#include <sstream>
#include <string>
#include <vector>

using emf::ecore::EcoreFactory;
using emf::ecore::EcorePackage;
using emf::ecore::EPackage;
using emf::ecore::EClass;
using emf::xmi::XMIResource;
using emf::xmi::XMIResourceSet;
using emf::common::URI;
using emf::common::EPackageRegistry;
using emf::ecore::codegen::CppGenerator;
using emf::ecore::codegen::GenConfig;

namespace {

// 关键路径：Java EMF 工具链（运行 java XMIReader 用）
constexpr const char* kJavaEcorePath    = "/workspace/emf-cpp-demo/build/java_ref/library.ecore";
constexpr const char* kJavaEcoreAbsPath = "/workspace/emf-cpp-demo/build/library.ecore";
constexpr const char* kEmfDemoDir       = "/workspace/emf-demo";
constexpr const char* kJavaCmd          = "java";

std::string makeTestDir(const std::string& sub) {
    std::string path = std::string(EMF_CODEGEN_TEST_OUTPUT_DIR) + "/" + sub;
    std::filesystem::create_directories(path);
    return path;
}

std::string readAll(const std::string& path) {
    std::ifstream f(path);
    if (!std::filesystem::exists(path)) return "";
    std::stringstream ss; ss << f.rdbuf();
    return ss.str();
}

}  // namespace

// =====================================================================
// 1) C++ 静态类型构造数据 → save XMI → Java XMIReader 读出
//    Java reader 必须能正确加载并打印所有关键字段（name/title/isbn/pages/...）
//    这是反向验证（之前没覆盖的 C++ 产 → Java 测方向）
// =====================================================================
EMF_TEST(E2E_GenModelXmi_CppProducesXmi_JavaConsumes) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    emf::xmi::XMIResourceFactory::registerDefaults();

    if (!std::filesystem::exists(kJavaEcorePath)) {
        std::fprintf(stderr, "skip: %s not found\n", kJavaEcorePath);
        return;
    }
    // 确认 java 工具链可用
    if (!std::filesystem::exists(std::string(kEmfDemoDir) + "/lib/org.eclipse.emf.ecore.xmi-2.36.0.jar")) {
        std::fprintf(stderr, "skip: %s/lib/org.eclipse.emf.ecore.xmi-2.36.0.jar not found\n", kEmfDemoDir);
        return;
    }

    // (a) codegen
    std::string outDir = makeTestDir("e2e_genmodelxmi/cpp-produces-xmi");
    std::string runDir = makeTestDir("e2e_genmodelxmi/cpp-produces-xmi-run");
    {
        GenConfig cfg;
        cfg.inputEcorePath = kJavaEcorePath;
        cfg.outputDirectory = outDir;
        cfg.baseNamespace = "emf";
        CppGenerator gen(cfg);
        gen.generateFromFile();
    }

    // (b) driver_cpp_produces.cpp：实打实用生成的 typed API 构造数据，save 出一个 library.xmi
    std::string driverPath = outDir + "/library/driver_cpp_produces.cpp";
    {
        std::ofstream f(driverPath);
        f <<
"#include \"Library.h\"\n"
"#include \"Book.h\"\n"
"#include \"Author.h\"\n"
"#include \"Publisher.h\"\n"
"#include \"Magazine.h\"\n"
"#include \"LibraryPackage.h\"\n"
"#include \"LibraryFactory.h\"\n"
"#include \"emf/xmi/XMIResource.h\"\n"
"#include \"emf/xmi/XMIResourceSet.h\"\n"
"#include \"emf/xmi/XMIResourceFactory.h\"\n"
"#include \"emf/common/EPackageRegistry.h\"\n"
"#include \"emf/common/URI.h\"\n"
"#include \"emf/ecore/EcorePackage.h\"\n"
"#include \"emf/ecore/EcoreImpls.h\"\n"
"#include \"emf/ecore/EcoreMetadata.h\"\n"
"#include <iostream>\n"
"#include <fstream>\n"
"#include <sstream>\n"
"#include <string>\n"
"#include <cstdio>\n"
"using namespace emf::library;\n"
"int main(int argc, char** argv) {\n"
"    if (argc < 3) {\n"
"        std::fprintf(stderr, \"usage: %s <ecore> <out.xmi>\\n\", argv[0]);\n"
"        return 2;\n"
"    }\n"
"    emf::ecore::EcoreFactory::initialize();\n"
"    emf::ecore::EcorePackage::initialize();\n"
"    emf::xmi::XMIResourceFactory::registerDefaults();\n"
"    LibraryPackage::initialize();\n"
"    LibraryFactory::initialize();\n"
"\n"
"    // 1) 动态加载 java_ref/library.ecore 元模型\n"
"    std::ifstream ef(argv[1]);\n"
"    std::stringstream ess; ess << ef.rdbuf();\n"
"    emf::xmi::XMIResource eres(emf::common::URI::createFileURI(argv[1]));\n"
"    eres.loadFromString(ess.str());\n"
"    auto* pkg = dynamic_cast<emf::ecore::EPackage*>(eres.getContents().front());\n"
"    if (!pkg) return 3;\n"
"\n"
"    // 2) 注入 LibraryFactory\n"
"    pkg->setEFactoryInstance(LibraryFactory::eINSTANCE);\n"
"    emf::common::EPackageRegistry::instance().put(pkg->getNsURI(), pkg);\n"
"\n"
"    // 3) 用 typed API 构造 Library / Book / Author / Publisher\n"
"    auto* libRaw = LibraryFactory::eINSTANCE->createLibrary();\n"
"    auto* lib = dynamic_cast<Library*>(libRaw);\n"
"    if (!lib) return 4;\n"
"    lib->setName(\"Cpp Produced Library\");\n"
"\n"
"    auto* b0Raw = LibraryFactory::eINSTANCE->createBook();\n"
"    auto* b0 = dynamic_cast<Book*>(b0Raw);\n"
"    if (!b0) return 5;\n"
"    b0->setTitle(\"C++ Produces Book\");\n"
"    b0->setIsbn(\"978-CXX-PRODUCES\");\n"
"    b0->setPages(424);\n"
"    b0->setPrice(59.99);\n"
"    lib->getBooks().add(b0);\n"
"\n"
"    auto* a0Raw = LibraryFactory::eINSTANCE->createAuthor();\n"
"    auto* a0 = dynamic_cast<Author*>(a0Raw);\n"
"    if (!a0) return 6;\n"
"    a0->setName(\"C++ Author A\");\n"
"    a0->setEmail(\"a.cxx@example.com\");\n"
"    a0->setBirthYear(2000);\n"
"    lib->getAuthors().add(a0);\n"
"\n"
"    auto* p0Raw = LibraryFactory::eINSTANCE->createPublisher();\n"
"    auto* p0 = dynamic_cast<Publisher*>(p0Raw);\n"
"    if (!p0) return 7;\n"
"    p0->setName(\"C++ Publisher X\");\n"
"    p0->setEmail(\"x.cxx@example.com\");\n"
"    lib->getPublishers().add(p0);\n"
"\n"
"    // 4) save XMI\n"
"    emf::xmi::XMIResource res(emf::common::URI::createFileURI(argv[2]));\n"
"    res.addToContents(lib);\n"
"    std::string out = res.saveToString();\n"
"    std::ofstream of(argv[2]);\n"
"    of << out;\n"
"\n"
"    // 5) typed API 验证\n"
"    std::cout << \"name=\" << lib->name() << std::endl;\n"
"    std::cout << \"book[0].title=\" << b0->title() << std::endl;\n"
"    std::cout << \"book[0].isbn=\" << b0->isbn() << std::endl;\n"
"    std::cout << \"book[0].pages=\" << b0->pages() << std::endl;\n"
"    std::cout << \"book[0].price=\" << b0->price() << std::endl;\n"
"    std::cout << \"author[0].name=\" << a0->name() << std::endl;\n"
"    std::cout << \"author[0].birthYear=\" << a0->birthYear() << std::endl;\n"
"    std::cout << \"publisher[0].name=\" << p0->name() << std::endl;\n"
"    std::cout << \"saved=\" << (out.size() > 0 ? \"yes\" : \"no\") << std::endl;\n"
"    return 0;\n"
"}\n";
    }

    // (c) 编译
    std::vector<std::string> libSrcs;
    for (auto& entry : std::filesystem::directory_iterator(outDir + "/library")) {
        if (entry.path().extension() == ".cpp" && entry.path() != driverPath) {
            libSrcs.push_back(entry.path().string());
        }
    }
    std::string srcList;
    for (auto& s : libSrcs) srcList += s + " ";

    std::string cmd =
        "bash -c 'set -e; "
        "INC1=\"" + std::string(EMFCPP_SOURCE_DIR) + "/emf-common/include\"; "
        "INC2=\"" + std::string(EMFCPP_SOURCE_DIR) + "/emf-ecore/include\"; "
        "INC3=\"" + std::string(EMFCPP_SOURCE_DIR) + "/emf-xmi/include\"; "
        "INC4=\"" + std::string(EMFCPP_SOURCE_DIR) + "/emf-ecore-util/include\"; "
        "g++ -std=c++17 "
        "-I \"$INC1\" -I \"$INC2\" -I \"$INC3\" -I \"$INC4\" "
        "-I \"" + outDir + "/library\" "
        + srcList
        + driverPath + " "
        + std::string(EMF_BUILD_DIR) + "/emf-xmi/libemf_xmi.a "
        + std::string(EMF_BUILD_DIR) + "/emf-ecore-util/libemf_ecore_util.a "
        + std::string(EMF_BUILD_DIR) + "/emf-ecore/libemf_ecore.a "
        + std::string(EMF_BUILD_DIR) + "/emf-common/libemf_common.a "
        + "-o " + runDir + "/cpp_produces_driver 2>&1'";
    int rc = std::system(cmd.c_str());
    if (rc != 0) {
        throw std::runtime_error("driver compilation failed (rc=" + std::to_string(rc) +
                                 ") cmd=" + cmd);
    }

    // (d) 运行 C++ driver
    std::string cppOutPath = runDir + "/cpp_produces.xmi";
    std::string cppStdoutPath = runDir + "/cpp_stdout.txt";
    std::string runCmd = runDir + "/cpp_produces_driver "
                       + kJavaEcorePath + " "
                       + cppOutPath + " > " + cppStdoutPath + " 2>&1";
    int runrc = std::system(runCmd.c_str());
    if (runrc != 0) {
        std::ifstream so(cppStdoutPath);
        std::stringstream ss; ss << so.rdbuf();
        throw std::runtime_error("C++ driver run failed (rc=" + std::to_string(runrc) +
                                 ")\nstdout:\n" + ss.str());
    }
    std::fprintf(stderr, "[E2E_CppProduces] C++ driver stdout:\n%s\n",
                 readAll(cppStdoutPath).c_str());

    // 验证 C++ 端 typed API 验证
    std::string cppOut = readAll(cppStdoutPath);
    EXPECT_TRUE(cppOut.find("name=Cpp Produced Library") != std::string::npos);
    EXPECT_TRUE(cppOut.find("book[0].title=C++ Produces Book") != std::string::npos);
    EXPECT_TRUE(cppOut.find("book[0].isbn=978-CXX-PRODUCES") != std::string::npos);
    EXPECT_TRUE(cppOut.find("book[0].pages=424") != std::string::npos);
    EXPECT_TRUE(cppOut.find("author[0].name=C++ Author A") != std::string::npos);
    EXPECT_TRUE(cppOut.find("author[0].birthYear=2000") != std::string::npos);
    EXPECT_TRUE(cppOut.find("publisher[0].name=C++ Publisher X") != std::string::npos);

    // XMI 文件存在
    EXPECT_TRUE(std::filesystem::exists(cppOutPath));
    EXPECT_TRUE(std::filesystem::file_size(cppOutPath) > 0);

    // (e) 调起 Java XMIReader 验证 Java 能读 C++ 产出的 XMI
    // 关键：必须用绝对路径版本的 ecore（kJavaEcoreAbsPath），
    // 因为 java_ref/library.ecore 内部是 build/library.ecore 相对路径无法解析
    std::string javaReaderCp = std::string(kEmfDemoDir) + "/build/classes";
    std::string javaCmdLine =
        std::string(kJavaCmd) + " -cp \"" + javaReaderCp + ":"
        + std::string(kEmfDemoDir) + "/lib/*\""
        + " com.example.emfdemo.XMIReader "
        + kJavaEcoreAbsPath + " "
        + cppOutPath
        + " > " + runDir + "/java_reader_stdout.txt"
        + " 2>&1";
    int jrc = std::system(javaCmdLine.c_str());
    std::string javaStdout = readAll(runDir + "/java_reader_stdout.txt");
    std::fprintf(stderr, "[E2E_CppProduces] Java reader stdout:\n%s\n", javaStdout.c_str());
    // Java reader 退码：0=OK，2=内容错，3=加载失败
    if (jrc != 0) {
        throw std::runtime_error("Java XMIReader failed (rc=" + std::to_string(jrc) +
                                 ")\nstdout:\n" + javaStdout);
    }

    // (f) 断言 Java reader 打印了所有 C++ 端设置的关键字段
    EXPECT_TRUE(javaStdout.find("Library name=Cpp Produced Library") != std::string::npos);
    EXPECT_TRUE(javaStdout.find("title=C++ Produces Book") != std::string::npos);
    EXPECT_TRUE(javaStdout.find("isbn=978-CXX-PRODUCES") != std::string::npos);
    EXPECT_TRUE(javaStdout.find("pages=424") != std::string::npos);
    EXPECT_TRUE(javaStdout.find("name=C++ Author A") != std::string::npos);
    EXPECT_TRUE(javaStdout.find("birthYear=2000") != std::string::npos);
    EXPECT_TRUE(javaStdout.find("name=C++ Publisher X") != std::string::npos);
    EXPECT_TRUE(javaStdout.find("[XMIReader] exit=0") != std::string::npos);
}
