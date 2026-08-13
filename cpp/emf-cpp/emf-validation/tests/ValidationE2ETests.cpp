// ValidationE2ETests.cpp —— emf-validation 端到端校验测试
// 对齐 Java: EMF Validation 框架的静态全量校验（validateAll）+ 动态变更校验（LiveValidator）
//
// 场景：
//   1. 动态加载 library.ecore → 创建模型实例
//   2. 静态全量校验：valid 模型无 diagnostic；违规模型（空 name / 缺 required ref）产 diagnostic
//   3. 动态变更校验：LiveValidator attach 后 eSet name="" → listener 收到 diagnostic
#include "test_main.h"
#include "emf/validation/ValidationService.h"
#include "emf/validation/EValidator.h"
#include "emf/validation/LiveValidator.h"
#include "emf/xmi/XMIResource.h"
#include "emf/xmi/XMIResourceFactory.h"
#include "emf/ecore/EcorePackage.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/ecore/DynamicEObject.h"
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
using emf::ecore::EStructuralFeature;
using emf::ecore::EFactory;
using emf::common::EObject;
using emf::validation::EValidator;
using emf::validation::ValidationService;
using emf::validation::ValidationLiveAdapter;

namespace {

// library.ecore 的内联 XMI（Library/Book/Writer，含 containment）
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
    "</eClassifiers>"
    "<eClassifiers xsi:type=\"ecore:EClass\" name=\"Book\">"
    "<eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"title\" "
    "eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString\"/>"
    "<eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"pages\" "
    "eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EInt\"/>"
    "<eStructuralFeatures xsi:type=\"ecore:EReference\" name=\"author\" "
    "eType=\"#//Writer\" containment=\"true\"/>"
    "</eClassifiers>"
    "<eClassifiers xsi:type=\"ecore:EClass\" name=\"Writer\">"
    "<eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"name\" "
    "eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString\"/>"
    "</eClassifiers>"
    "</ecore:EPackage>";

void initEnv() {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    XMIResourceFactory::registerDefaults();
}

// 加载 ecore，返回 (EPackage*, Library EClass*, Book EClass*, Writer EClass*, factory)
struct ModelMeta {
    EPackage* pkg = nullptr;
    EClass* libCls = nullptr;
    EClass* bookCls = nullptr;
    EClass* writerCls = nullptr;
    EFactory* factory = nullptr;
};

ModelMeta loadModel() {
    XMIResource res;
    res.loadFromString(std::string(kLibraryEcore));
    ModelMeta m;
    m.pkg = dynamic_cast<EPackage*>(res.getContents().front());
    res.getContents().clear();  // 移除 EPackage 所有权，只保留指针
    m.libCls = dynamic_cast<EClass*>(m.pkg->getEClassifier("Library"));
    m.bookCls = dynamic_cast<EClass*>(m.pkg->getEClassifier("Book"));
    m.writerCls = dynamic_cast<EClass*>(m.pkg->getEClassifier("Writer"));
    m.factory = m.pkg->getEFactoryInstance();
    return m;
}

// 构造一个 valid 模型：Library(name) -> [Book(title,pages), Book(title,pages)]
EObject* buildValidModel(const ModelMeta& m) {
    auto* lib = m.factory->create(m.libCls);
    lib->eSet(m.libCls->getEStructuralFeature("name"), std::any(std::string("Test Library")));

    auto* b0 = m.factory->create(m.bookCls);
    b0->eSet(m.bookCls->getEStructuralFeature("title"), std::any(std::string("Book One")));
    b0->eSet(m.bookCls->getEStructuralFeature("pages"), std::any(100));

    auto* b1 = m.factory->create(m.bookCls);
    b1->eSet(m.bookCls->getEStructuralFeature("title"), std::any(std::string("Book Two")));
    b1->eSet(m.bookCls->getEStructuralFeature("pages"), std::any(200));

    // 用 eSet 设置多值 containment 引用（books）。
    // 注意：DynamicEObject::eGet 对多值 EReference 返回堆副本，
    // 直接对副本 add 不会修改内部 list，导致 books 不会成为 Library 的子对象，
    // 进而 collectAll/eContents 无法遍历到 books，校验不会作用于它们。
    // 改用 eSet(vector<EObject*>) 经 getOrCreateList 写入内部 ContainmentEList，
    // 自动设置 eContainer，使 books 出现在 eContents() 中。
    std::vector<EObject*> booksVec;
    booksVec.push_back(b0);
    booksVec.push_back(b1);
    lib->eSet(m.libCls->getEStructuralFeature("books"), std::any(std::move(booksVec)));
    return lib;
}

}  // namespace

// ===== 测试 1：静态全量校验 valid 模型 → 0 diagnostic =====
EMF_TEST(ValidationE2E_StaticAll_ValidModel_NoDiagnostics) {
    initEnv();
    auto m = loadModel();
    auto* lib = buildValidModel(m);

    ValidationService svc;
    auto diags = svc.validateAll(lib);
    EXPECT_EQ(diags.size(), 0u);
}

// ===== 测试 2：静态全量校验 空 name → 产 NoEmptyName diagnostic =====
EMF_TEST(ValidationE2E_StaticAll_EmptyName_ProducesDiagnostic) {
    initEnv();
    auto m = loadModel();
    auto* lib = buildValidModel(m);
    // 把 name 改成空
    lib->eSet(m.libCls->getEStructuralFeature("name"), std::any(std::string("")));

    ValidationService svc;
    auto diags = svc.validateAll(lib);
    EXPECT_TRUE(diags.size() > 0u);
    // 至少有一个 diagnostic 的 source 是 "NoEmptyName"
    bool found = false;
    for (auto& d : diags) {
        if (d.source().find("NoEmptyName") != std::string::npos) {
            found = true;
            break;
        }
    }
    EXPECT_TRUE(found);
}

// ===== 测试 3：静态全量校验 缺 required reference → 产 NoNullRequiredRef diagnostic =====
EMF_TEST(ValidationE2E_StaticAll_NullRequiredRef_ProducesDiagnostic) {
    initEnv();
    auto m = loadModel();
    auto* lib = buildValidModel(m);
    // 把 Book.author 的 lowerBound 设为 1（使其成为 required reference）
    auto* authorRef = dynamic_cast<EReference*>(m.bookCls->getEStructuralFeature("author"));
    EXPECT_NOT_NULL(authorRef);
    authorRef->setLowerBound(1);
    // lib 中的 books 没有 author → 违规

    ValidationService svc;
    auto diags = svc.validateAll(lib);
    EXPECT_TRUE(diags.size() > 0u);
    // 至少有一个 diagnostic 的 source 是 "NoNullRequiredRef"
    bool found = false;
    for (auto& d : diags) {
        if (d.source().find("NoNullRequiredRef") != std::string::npos) {
            found = true;
            break;
        }
    }
    EXPECT_TRUE(found);
}

// ===== 测试 4：动态变更校验 —— eSet name="" 触发 LiveValidator =====
EMF_TEST(ValidationE2E_LiveValidator_SetToEmpty_TriggersDiagnostic) {
    initEnv();
    auto m = loadModel();
    auto* lib = buildValidModel(m);

    EValidator validator;
    validator.registerDefaultConstraints();
    ValidationLiveAdapter live(validator);

    // 收集 listener 收到的 diagnostic
    size_t receivedCount = 0;
    bool receivedEmptyNameDiag = false;
    live.addListener([&](EObject* /*target*/, const std::vector<emf::common::Diagnostic>& diags) {
        receivedCount += diags.size();
        for (auto& d : diags) {
            if (d.source().find("NoEmptyName") != std::string::npos) {
                receivedEmptyNameDiag = true;
            }
        }
    });

    // attach 到 Library（递归挂到所有 containment 子对象）
    live.attach(lib);

    // 先验证 valid 状态下不产 diagnostic
    lib->eSet(m.libCls->getEStructuralFeature("name"), std::any(std::string("Still Valid")));
    EXPECT_EQ(receivedCount, 0u);

    // 改成空 name → 应触发 LiveValidator 产 NoEmptyName diagnostic
    lib->eSet(m.libCls->getEStructuralFeature("name"), std::any(std::string("")));
    EXPECT_TRUE(receivedCount > 0u);
    EXPECT_TRUE(receivedEmptyNameDiag);

    live.detach();
}
