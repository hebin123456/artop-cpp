// AutosarConstraintsTests.cpp —— 核心 AUTOSAR 业务约束回归测试
// 对齐 artop 内置 AUTOSAR 约束语义：
//   - shortName 非空 / 同父同类型兄弟唯一
//   - uuid 非空
//   - category（lowerBound>=1）非空
//   - 无未解析 proxy 引用
//   - LIVE 模式实时校验
#include "test_main.h"
#include "emf/validation/AutosarConstraints.h"
#include "emf/validation/EValidator.h"
#include "emf/validation/ValidationService.h"
#include "emf/validation/LiveValidator.h"
#include "emf/xmi/XMIResource.h"
#include "emf/xmi/XMIResourceFactory.h"
#include "emf/ecore/EcorePackage.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/ecore/DynamicEObject.h"
#include "emf/common/EObject.h"
#include "emf/common/URI.h"

#include <any>
#include <string>
#include <vector>

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
using emf::common::EObjectImpl;
using emf::validation::EValidator;
using emf::validation::ValidationService;
using emf::validation::ValidationLiveAdapter;
using emf::validation::registerAutosarConstraints;

namespace {

// 测试元模型：Container -> [Element(shortName,uuid,category,ref), Other(shortName)]
// category lowerBound=1（必填），ref 为非 containment 单值引用（用于 proxy 测试）
const char* kAutosarTestEcore =
    "<?xml version=\"1.0\"?>\n"
    "<ecore:EPackage xmlns:ecore=\"http://www.eclipse.org/emf/2002/Ecore\" "
    "xmlns:xmi=\"http://www.omg.org/XMI\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
    "xmi:version=\"2.0\" name=\"astest\" nsURI=\"http://example.com/astest/1.0\" nsPrefix=\"astest\">"
    "<eClassifiers xsi:type=\"ecore:EClass\" name=\"Container\">"
    "<eStructuralFeatures xsi:type=\"ecore:EReference\" name=\"elements\" upperBound=\"-1\" "
    "eType=\"#//Element\" containment=\"true\"/>"
    "<eStructuralFeatures xsi:type=\"ecore:EReference\" name=\"others\" upperBound=\"-1\" "
    "eType=\"#//Other\" containment=\"true\"/>"
    "</eClassifiers>"
    "<eClassifiers xsi:type=\"ecore:EClass\" name=\"Element\">"
    "<eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"shortName\" "
    "eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString\"/>"
    "<eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"uuid\" "
    "eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString\"/>"
    "<eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"category\" lowerBound=\"1\" "
    "eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString\"/>"
    "<eStructuralFeatures xsi:type=\"ecore:EReference\" name=\"ref\" eType=\"#//Element\"/>"
    "</eClassifiers>"
    "<eClassifiers xsi:type=\"ecore:EClass\" name=\"Other\">"
    "<eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"shortName\" "
    "eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString\"/>"
    "</eClassifiers>"
    "</ecore:EPackage>";

struct AsMeta {
    EPackage* pkg = nullptr;
    EClass* containerCls = nullptr;
    EClass* elementCls = nullptr;
    EClass* otherCls = nullptr;
    EFactory* factory = nullptr;
};

void initEnv() {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    XMIResourceFactory::registerDefaults();
}

AsMeta loadModel() {
    XMIResource res;
    res.loadFromString(std::string(kAutosarTestEcore));
    AsMeta m;
    m.pkg = dynamic_cast<EPackage*>(res.getContents().front());
    res.getContents().clear();
    m.containerCls = dynamic_cast<EClass*>(m.pkg->getEClassifier("Container"));
    m.elementCls = dynamic_cast<EClass*>(m.pkg->getEClassifier("Element"));
    m.otherCls = dynamic_cast<EClass*>(m.pkg->getEClassifier("Other"));
    m.factory = m.pkg->getEFactoryInstance();
    return m;
}

// 设置多值 containment（通过 eSet(vector<EObject*>)，自动维护 eContainer）
void setContainment(EObject* owner, EStructuralFeature* feat,
                    const std::vector<EObject*>& children) {
    owner->eSet(feat, std::any(std::vector<EObject*>(children)));
}

// 设置单值字符串属性
void setStr(EObject* obj, EStructuralFeature* feat, const std::string& v) {
    obj->eSet(feat, std::any(v));
}

// 构造 valid Element：shortName/uuid/category 均非空
EObject* makeValidElement(const AsMeta& m, const std::string& sn,
                          const std::string& uuid, const std::string& cat = "default") {
    auto* e = m.factory->create(m.elementCls);
    setStr(e, m.elementCls->getEStructuralFeature("shortName"), sn);
    setStr(e, m.elementCls->getEStructuralFeature("uuid"), uuid);
    setStr(e, m.elementCls->getEStructuralFeature("category"), cat);
    return e;
}

// 在 diags 中查找 source 含 sub 的 diagnostic
bool hasDiagWith(const std::vector<emf::common::Diagnostic>& diags, const std::string& sub) {
    for (auto& d : diags) {
        if (d.source().find(sub) != std::string::npos) return true;
    }
    return false;
}

}  // namespace

// ===== 测试 1：valid 模型 → 无 AUTOSAR diagnostic =====
EMF_TEST(Autosar_ValidModel_NoAutosarDiagnostics) {
    initEnv();
    auto m = loadModel();
    auto* container = m.factory->create(m.containerCls);
    auto* e0 = makeValidElement(m, "ElemA", "uuid-A");
    auto* e1 = makeValidElement(m, "ElemB", "uuid-B");
    setContainment(container, m.containerCls->getEStructuralFeature("elements"), {e0, e1});

    ValidationService svc;
    registerAutosarConstraints(svc.validator());
    auto diags = svc.validateAll(container);
    EXPECT_FALSE(hasDiagWith(diags, "Autosar"));
}

// ===== 测试 2：空 shortName → AutosarShortNameNonEmpty =====
EMF_TEST(Autosar_EmptyShortName_ProducesDiagnostic) {
    initEnv();
    auto m = loadModel();
    auto* container = m.factory->create(m.containerCls);
    auto* e = makeValidElement(m, "", "uuid-A");  // 空 shortName
    setContainment(container, m.containerCls->getEStructuralFeature("elements"), {e});

    ValidationService svc;
    registerAutosarConstraints(svc.validator());
    auto diags = svc.validateAll(container);
    EXPECT_TRUE(hasDiagWith(diags, "AutosarShortNameNonEmpty"));
}

// ===== 测试 3：同父同类型兄弟 shortName 重复 → AutosarShortNameUniqueInParent =====
EMF_TEST(Autosar_DuplicateShortNameSameType_ProducesDiagnostic) {
    initEnv();
    auto m = loadModel();
    auto* container = m.factory->create(m.containerCls);
    auto* e0 = makeValidElement(m, "Dup", "uuid-A");
    auto* e1 = makeValidElement(m, "Dup", "uuid-B");  // 同 shortName，同 EClass
    setContainment(container, m.containerCls->getEStructuralFeature("elements"), {e0, e1});

    ValidationService svc;
    registerAutosarConstraints(svc.validator());
    auto diags = svc.validateAll(container);
    EXPECT_TRUE(hasDiagWith(diags, "AutosarShortNameUniqueInParent"));
}

// ===== 测试 4：同父不同类型兄弟 shortName 相同 → 不产唯一性 diagnostic =====
// AUTOSAR：shortName 唯一性只在同类型兄弟间要求，不同类型可重名
EMF_TEST(Autosar_DuplicateShortNameDifferentType_NoUniquenessDiagnostic) {
    initEnv();
    auto m = loadModel();
    auto* container = m.factory->create(m.containerCls);
    auto* e = makeValidElement(m, "Shared", "uuid-A");
    // Other 类型，shortName 也是 "Shared"
    auto* o = m.factory->create(m.otherCls);
    setStr(o, m.otherCls->getEStructuralFeature("shortName"), "Shared");
    setContainment(container, m.containerCls->getEStructuralFeature("elements"), {e});
    setContainment(container, m.containerCls->getEStructuralFeature("others"), {o});

    ValidationService svc;
    registerAutosarConstraints(svc.validator());
    auto diags = svc.validateAll(container);
    EXPECT_FALSE(hasDiagWith(diags, "AutosarShortNameUniqueInParent"));
}

// ===== 测试 5：空 uuid → AutosarUuidNonEmpty =====
EMF_TEST(Autosar_EmptyUuid_ProducesDiagnostic) {
    initEnv();
    auto m = loadModel();
    auto* container = m.factory->create(m.containerCls);
    auto* e = makeValidElement(m, "ElemA", "");  // 空 uuid
    setContainment(container, m.containerCls->getEStructuralFeature("elements"), {e});

    ValidationService svc;
    registerAutosarConstraints(svc.validator());
    auto diags = svc.validateAll(container);
    EXPECT_TRUE(hasDiagWith(diags, "AutosarUuidNonEmpty"));
}

// ===== 测试 6：空 category（lowerBound=1）→ AutosarCategoryRequired =====
EMF_TEST(Autosar_EmptyCategory_ProducesDiagnostic) {
    initEnv();
    auto m = loadModel();
    auto* container = m.factory->create(m.containerCls);
    auto* e = makeValidElement(m, "ElemA", "uuid-A");
    setStr(e, m.elementCls->getEStructuralFeature("category"), "");  // 空 category
    setContainment(container, m.containerCls->getEStructuralFeature("elements"), {e});

    ValidationService svc;
    registerAutosarConstraints(svc.validator());
    auto diags = svc.validateAll(container);
    EXPECT_TRUE(hasDiagWith(diags, "AutosarCategoryRequired"));
}

// ===== 测试 7：未解析 proxy 引用 → AutosarNoUnresolvedProxy =====
EMF_TEST(Autosar_UnresolvedProxy_ProducesDiagnostic) {
    initEnv();
    auto m = loadModel();
    auto* container = m.factory->create(m.containerCls);
    auto* e = makeValidElement(m, "ElemA", "uuid-A");
    // 创建一个 proxy 对象（设置 eProxyURI）
    auto* proxy = m.factory->create(m.elementCls);
    if (auto* impl = dynamic_cast<EObjectImpl*>(proxy)) {
        impl->eSetProxyURI(emf::common::URI("file:/nonexistent.arxml#//Unresolved"));
    }
    EXPECT_TRUE(proxy->eIsProxy());
    // 设置非 containment 引用 ref 指向 proxy
    e->eSet(m.elementCls->getEStructuralFeature("ref"), std::any(proxy));
    setContainment(container, m.containerCls->getEStructuralFeature("elements"), {e});

    ValidationService svc;
    registerAutosarConstraints(svc.validator());
    auto diags = svc.validateAll(container);
    EXPECT_TRUE(hasDiagWith(diags, "AutosarNoUnresolvedProxy"));
}

// ===== 测试 8：LIVE 模式——eSet 空 shortName 触发 AutosarShortNameNonEmpty =====
EMF_TEST(Autosar_Live_EmptyShortName_TriggersDiagnostic) {
    initEnv();
    auto m = loadModel();
    auto* container = m.factory->create(m.containerCls);
    auto* e = makeValidElement(m, "ElemA", "uuid-A");
    setContainment(container, m.containerCls->getEStructuralFeature("elements"), {e});

    EValidator validator;
    registerAutosarConstraints(validator);
    ValidationLiveAdapter live(validator);

    bool receivedShortNameDiag = false;
    live.addListener([&](EObject* /*target*/, const std::vector<emf::common::Diagnostic>& diags) {
        for (auto& d : diags) {
            if (d.source().find("AutosarShortNameNonEmpty") != std::string::npos) {
                receivedShortNameDiag = true;
            }
        }
    });
    live.attach(container);

    // 先设置合法 shortName → 不应触发
    setStr(e, m.elementCls->getEStructuralFeature("shortName"), "StillValid");
    EXPECT_FALSE(receivedShortNameDiag);

    // 改成空 shortName → 应触发 LIVE 约束
    setStr(e, m.elementCls->getEStructuralFeature("shortName"), "");
    EXPECT_TRUE(receivedShortNameDiag);

    live.detach();
}

// ===== 测试 9：幂等——重复注册替换旧约束，约束总数不翻倍 =====
EMF_TEST(Autosar_RegisterTwice_Idempotent) {
    initEnv();
    EValidator validator;
    registerAutosarConstraints(validator);
    size_t n1 = validator.getConstraints().size();
    registerAutosarConstraints(validator);  // 幂等替换
    size_t n2 = validator.getConstraints().size();
    EXPECT_EQ(n1, n2);
}

// ===== 测试 10：UUID 全局唯一——不同 uuid → 无 diagnostic =====
EMF_TEST(Autosar_UniqueUuids_NoGloballyUniqueDiagnostic) {
    initEnv();
    auto m = loadModel();
    auto* container = m.factory->create(m.containerCls);
    auto* e0 = makeValidElement(m, "ElemA", "uuid-A");
    auto* e1 = makeValidElement(m, "ElemB", "uuid-B");
    auto* e2 = makeValidElement(m, "ElemC", "uuid-C");
    setContainment(container, m.containerCls->getEStructuralFeature("elements"), {e0, e1, e2});

    ValidationService svc;
    registerAutosarConstraints(svc.validator());
    auto diags = svc.validateAll(container);
    EXPECT_FALSE(hasDiagWith(diags, "AutosarUuidGloballyUnique"));
}

// ===== 测试 11：UUID 全局唯一——重复 uuid → AutosarUuidGloballyUnique =====
// 对齐 artop FixUuidConflictsAction：两个不同对象有相同 uuid → 后续重复报告
EMF_TEST(Autosar_DuplicateUuid_ProducesGloballyUniqueDiagnostic) {
    initEnv();
    auto m = loadModel();
    auto* container = m.factory->create(m.containerCls);
    auto* e0 = makeValidElement(m, "ElemA", "same-uuid");
    auto* e1 = makeValidElement(m, "ElemB", "same-uuid");  // 重复 uuid
    setContainment(container, m.containerCls->getEStructuralFeature("elements"), {e0, e1});

    ValidationService svc;
    registerAutosarConstraints(svc.validator());
    auto diags = svc.validateAll(container);
    EXPECT_TRUE(hasDiagWith(diags, "AutosarUuidGloballyUnique"));
}

// ===== 测试 12：UUID 全局唯一——空 uuid 也报告（对齐 artop） =====
EMF_TEST(Autosar_EmptyUuid_ProducesGloballyUniqueDiagnostic) {
    initEnv();
    auto m = loadModel();
    auto* container = m.factory->create(m.containerCls);
    auto* e = makeValidElement(m, "ElemA", "");  // 空 uuid
    setContainment(container, m.containerCls->getEStructuralFeature("elements"), {e});

    // validateUuidUniqueness 独立调用
    auto diags = emf::validation::validateUuidUniqueness(container);
    EXPECT_TRUE(hasDiagWith(diags, "AutosarUuidGloballyUnique"));
}

// ===== 测试 13：UUID 全局唯一——深层嵌套重复 uuid 也能检测 =====
// 对齐 artop：整树 DFS 遍历，不限于同父兄弟
EMF_TEST(Autosar_DeepNestedDuplicateUuid_ProducesDiagnostic) {
    initEnv();
    auto m = loadModel();
    auto* outer = m.factory->create(m.containerCls);
    auto* inner = m.factory->create(m.containerCls);  // 嵌套 Container
    auto* e0 = makeValidElement(m, "ElemA", "deep-uuid");
    auto* e1 = makeValidElement(m, "ElemB", "deep-uuid");  // 不同层级但同 uuid
    // outer -> elements[e0], others[inner -> elements[e1]]
    setContainment(outer, m.containerCls->getEStructuralFeature("elements"), {e0});
    setContainment(outer, m.containerCls->getEStructuralFeature("others"), {inner});
    setContainment(inner, m.containerCls->getEStructuralFeature("elements"), {e1});

    ValidationService svc;
    registerAutosarConstraints(svc.validator());
    auto diags = svc.validateAll(outer);
    EXPECT_TRUE(hasDiagWith(diags, "AutosarUuidGloballyUnique"));
}
