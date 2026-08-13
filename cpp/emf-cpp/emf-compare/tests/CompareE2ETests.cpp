// CompareE2ETests.cpp —— emf-compare 端到端比对测试
// 对齐 Java: EMF Compare 的 match + diff 流程
//
// 场景（动态建模，同一 EPackage 实例）：
//   1. 两个相同模型 → 0 diff
//   2. 属性变更 → per-attribute CHANGE diff
//   3. 子对象新增 → ADD diff
//   4. 子对象删除 → DELETE diff
#include "test_main.h"
#include "emf/compare/Comparison.h"
#include "emf/compare/MatchEngine.h"
#include "emf/compare/DiffEngine.h"
#include "emf/compare/MergeEngine.h"
#include "emf/compare/Diff.h"
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
using emf::ecore::EStructuralFeature;
using emf::ecore::EFactory;
using emf::common::EObject;
using emf::compare::Comparison;
using emf::compare::MatchEngine;
using emf::compare::DiffEngine;
using emf::compare::DiffKind;
using emf::compare::MatchKind;

namespace {

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
    "</eClassifiers>"
    "</ecore:EPackage>";

struct ModelMeta {
    EPackage* pkg = nullptr;
    EClass* libCls = nullptr;
    EClass* bookCls = nullptr;
    EFactory* factory = nullptr;
};

ModelMeta loadModel() {
    XMIResource res;
    res.loadFromString(std::string(kLibraryEcore));
    ModelMeta m;
    m.pkg = dynamic_cast<EPackage*>(res.getContents().front());
    res.getContents().clear();
    m.libCls = dynamic_cast<EClass*>(m.pkg->getEClassifier("Library"));
    m.bookCls = dynamic_cast<EClass*>(m.pkg->getEClassifier("Book"));
    m.factory = m.pkg->getEFactoryInstance();
    return m;
}

void initEnv() {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    XMIResourceFactory::registerDefaults();
}

// 构造 Library(name) -> [Book(title,pages), ...]
EObject* buildLibrary(const ModelMeta& m, const std::string& name,
                       const std::vector<std::pair<std::string, int>>& books) {
    auto* lib = m.factory->create(m.libCls);
    lib->eSet(m.libCls->getEStructuralFeature("name"), std::any(name));
    auto booksV = lib->eGet(m.libCls->getEStructuralFeature("books"));
    auto* booksList = std::any_cast<emf::common::EList<EObject*>*>(booksV);
    for (auto& [title, pages] : books) {
        auto* b = m.factory->create(m.bookCls);
        b->eSet(m.bookCls->getEStructuralFeature("title"), std::any(title));
        b->eSet(m.bookCls->getEStructuralFeature("pages"), std::any(pages));
        booksList->add(b);
    }
    return lib;
}

// 顶层 compare：match + diff
Comparison compareModels(EObject* left, EObject* right) {
    Comparison comp;
    MatchEngine me;
    me.match(left, right, comp);
    DiffEngine de;
    de.diff(comp);
    return comp;
}

// 统计指定 kind 的 diff 数量
size_t countDiffs(const Comparison& comp, DiffKind kind) {
    size_t n = 0;
    for (auto* d : comp.getDifferences()) {
        if (d->getKind() == kind) ++n;
    }
    return n;
}

// 查找指定 attributeName 的 CHANGE diff
bool hasChangeDiffOn(const Comparison& comp, const std::string& attrName) {
    for (auto* d : comp.getDifferences()) {
        if (d->getKind() == DiffKind::CHANGE && d->getAttributeName() == attrName) {
            return true;
        }
    }
    return false;
}

}  // namespace

// ===== 测试 1：两个相同模型 → 0 diff =====
EMF_TEST(CompareE2E_IdenticalModels_NoDiff) {
    initEnv();
    auto m = loadModel();
    auto* left = buildLibrary(m, "My Library", {{"B1", 10}, {"B2", 20}});
    auto* right = buildLibrary(m, "My Library", {{"B1", 10}, {"B2", 20}});

    auto comp = compareModels(left, right);
    EXPECT_EQ(comp.getDifferences().size(), 0u);
}

// ===== 测试 2：属性变更 → per-attribute CHANGE diff =====
EMF_TEST(CompareE2E_AttributeChange_ProducesPerAttributeDiff) {
    initEnv();
    auto m = loadModel();
    // left: name="Old", right: name="New"，books 相同
    auto* left = buildLibrary(m, "Old Name", {{"B1", 10}});
    auto* right = buildLibrary(m, "New Name", {{"B1", 10}});

    auto comp = compareModels(left, right);
    // 应产至少 1 个 CHANGE diff，且其中有一个针对 "name" 属性
    EXPECT_TRUE(comp.getDifferences().size() > 0u);
    EXPECT_TRUE(hasChangeDiffOn(comp, "name"));
}

// ===== 测试 3：子对象新增 → ADD diff =====
EMF_TEST(CompareE2E_AddedChild_ProducesAddDiff) {
    initEnv();
    auto m = loadModel();
    // left: 1 book, right: 2 books（第 2 个是新增的）
    auto* left = buildLibrary(m, "Lib", {{"B1", 10}});
    auto* right = buildLibrary(m, "Lib", {{"B1", 10}, {"B2", 20}});

    auto comp = compareModels(left, right);
    // 应产 1 个 ADD diff（right 多了 B2）
    EXPECT_EQ(countDiffs(comp, DiffKind::ADD), 1u);
}

// ===== 测试 4：子对象删除 → DELETE diff =====
EMF_TEST(CompareE2E_RemovedChild_ProducesDeleteDiff) {
    initEnv();
    auto m = loadModel();
    // left: 2 books, right: 1 book（第 2 个被删除）
    auto* left = buildLibrary(m, "Lib", {{"B1", 10}, {"B2", 20}});
    auto* right = buildLibrary(m, "Lib", {{"B1", 10}});

    auto comp = compareModels(left, right);
    // 应产 1 个 DELETE diff（left 的 B2 在 right 中不存在）
    EXPECT_EQ(countDiffs(comp, DiffKind::DELETE), 1u);
}

// ===== 测试 5：多属性变更 → 多个 per-attribute CHANGE diff =====
EMF_TEST(CompareE2E_MultiAttributeChange_ProducesMultipleDiffs) {
    initEnv();
    auto m = loadModel();
    // left: name="L", B1(title="T1",pages=10)
    // right: name="L_changed", B1(title="T1_changed",pages=99)
    // → root 的 name 变 + book 的 title 变 + book 的 pages 变 = 3 个 CHANGE diff
    auto* left = buildLibrary(m, "L", {{"T1", 10}});
    auto* right = buildLibrary(m, "L_changed", {{"T1_changed", 99}});

    auto comp = compareModels(left, right);
    // 至少 3 个 CHANGE diff：name, title, pages
    EXPECT_TRUE(countDiffs(comp, DiffKind::CHANGE) >= 3u);
    EXPECT_TRUE(hasChangeDiffOn(comp, "name"));
    EXPECT_TRUE(hasChangeDiffOn(comp, "title"));
    EXPECT_TRUE(hasChangeDiffOn(comp, "pages"));
}

// ===== 测试 6：有序多值 reference 位置变化 → MOVE diff（对齐 Java ReferenceChange kind=MOVE） =====
EMF_TEST(CompareE2E_ReorderedChildren_ProducesMoveDiff) {
    initEnv();
    auto m = loadModel();
    // left: [B1, B2, B3]，right: [B1, B3, B2]（B2 和 B3 顺序交换）
    // 用相同的 title 让 match 按顺序配对 → 检测到 books 位置变化
    auto* left = buildLibrary(m, "Lib", {{"B1", 1}, {"B2", 2}, {"B3", 3}});
    auto* right = buildLibrary(m, "Lib", {{"B1", 1}, {"B3", 3}, {"B2", 2}});

    auto comp = compareModels(left, right);
    // 应产出 MOVE diff（books 顺序变化）
    EXPECT_TRUE(countDiffs(comp, DiffKind::MOVE) > 0u);
}

// ===== 测试 6b：G8 LCS 最小 MOVE 集合 =====
// [B1,B2,B3]→[B1,B3,B2]：朴素法会产 2 个 MOVE（B2 和 B3 都判位移），
// LCS 法把锚定子序列 [B1,B2] 或 [B1,B3] 排除，只产 1 个 MOVE（最小集）。
EMF_TEST(CompareE2E_LcsMove_MinimalMoveSet) {
    initEnv();
    auto m = loadModel();
    auto* left = buildLibrary(m, "Lib", {{"B1", 1}, {"B2", 2}, {"B3", 3}});
    auto* right = buildLibrary(m, "Lib", {{"B1", 1}, {"B3", 3}, {"B2", 2}});

    auto comp = compareModels(left, right);
    // G8：LCS 应把 MOVE 数降到 1（最小集），而非朴素的 2
    EXPECT_EQ(countDiffs(comp, DiffKind::MOVE), 1u);
}

// ===== 测试 6c：G8 LCS 全位移仍最小集 =====
// [A,B,C,D]→[D,C,B,A]：朴素法 4 个 MOVE；LCS 长度 1 → 3 个 MOVE（最小集）。
EMF_TEST(CompareE2E_LcsMove_FullReversal) {
    initEnv();
    auto m = loadModel();
    auto* left = buildLibrary(m, "Lib", {{"A", 1}, {"B", 2}, {"C", 3}, {"D", 4}});
    auto* right = buildLibrary(m, "Lib", {{"D", 4}, {"C", 3}, {"B", 2}, {"A", 1}});

    auto comp = compareModels(left, right);
    // LCS 长度 1（仅 1 个元素锚定），其余 3 个位移 → 3 个 MOVE
    EXPECT_EQ(countDiffs(comp, DiffKind::MOVE), 3u);
}

// ===== 测试 7：MergeEngine CHANGE → 把 right 的属性值同步到 left =====
EMF_TEST(CompareE2E_Merge_AttributeChange_AppliedToTarget) {
    initEnv();
    auto m = loadModel();
    auto* left = buildLibrary(m, "OldName", {{"B1", 10}});
    auto* right = buildLibrary(m, "NewName", {{"B1", 10}});

    auto comp = compareModels(left, right);
    emf::compare::MergeEngine me;
    EXPECT_TRUE(me.merge(comp, left));
    // 合并后 left.name 应等于 right.name
    auto v = left->eGet(m.libCls->getEStructuralFeature("name"));
    EXPECT_EQ(std::any_cast<std::string>(v), std::string("NewName"));
}

// ===== 测试 8：MergeEngine ADD → 把 right 新增的子对象加到 left =====
EMF_TEST(CompareE2E_Merge_AddedChild_AppliedToTarget) {
    initEnv();
    auto m = loadModel();
    auto* left = buildLibrary(m, "Lib", {{"B1", 10}});
    auto* right = buildLibrary(m, "Lib", {{"B1", 10}, {"B2", 20}});

    auto comp = compareModels(left, right);
    emf::compare::MergeEngine me;
    EXPECT_TRUE(me.merge(comp, left));
    // 合并后 left 应有 2 本书
    auto booksV = left->eGet(m.libCls->getEStructuralFeature("books"));
    auto* booksList = std::any_cast<emf::common::EList<EObject*>*>(booksV);
    EXPECT_EQ(booksList->size(), 2u);
}

// ===== 测试 9：MergeEngine DELETE → 从 left 移除被删的子对象 =====
EMF_TEST(CompareE2E_Merge_DeletedChild_AppliedToTarget) {
    initEnv();
    auto m = loadModel();
    auto* left = buildLibrary(m, "Lib", {{"B1", 10}, {"B2", 20}});
    auto* right = buildLibrary(m, "Lib", {{"B1", 10}});

    auto comp = compareModels(left, right);
    emf::compare::MergeEngine me;
    EXPECT_TRUE(me.merge(comp, left));
    // 合并后 left 应剩 1 本书
    auto booksV = left->eGet(m.libCls->getEStructuralFeature("books"));
    auto* booksList = std::any_cast<emf::common::EList<EObject*>*>(booksV);
    EXPECT_EQ(booksList->size(), 1u);
}

// ===== 测试 10：3-way compare 检测 REAL 冲突（left 和 right 改了同一属性为不同值） =====
EMF_TEST(CompareE2E_ThreeWay_RealConflict_Detected) {
    initEnv();
    auto m = loadModel();
    // origin: name="O"
    // left:   name="L"  (改了)
    // right:  name="R"  (改了，不同值) → REAL 冲突
    auto* origin = buildLibrary(m, "O", {{"B1", 10}});
    auto* left = buildLibrary(m, "L", {{"B1", 10}});
    auto* right = buildLibrary(m, "R", {{"B1", 10}});

    auto comp = emf::compare::compare(left, right, origin);
    EXPECT_TRUE(comp.isThreeWay());
    // 应检测到至少 1 个冲突，且为 REAL
    EXPECT_TRUE(comp.getConflicts().size() > 0u);
    bool hasReal = false;
    for (auto& c : comp.getConflicts()) {
        if (c.getKind() == emf::compare::ConflictKind::REAL) hasReal = true;
    }
    EXPECT_TRUE(hasReal);
}

// ===== 测试 11：3-way compare 检测 PSEUDO 冲突（left 和 right 改成相同值） =====
EMF_TEST(CompareE2E_ThreeWay_PseudoConflict_Detected) {
    initEnv();
    auto m = loadModel();
    // origin: name="O"
    // left:   name="Same"  (改了)
    // right:  name="Same"  (改了，相同值) → PSEUDO 冲突
    auto* origin = buildLibrary(m, "O", {{"B1", 10}});
    auto* left = buildLibrary(m, "Same", {{"B1", 10}});
    auto* right = buildLibrary(m, "Same", {{"B1", 10}});

    auto comp = emf::compare::compare(left, right, origin);
    bool hasPseudo = false;
    for (auto& c : comp.getConflicts()) {
        if (c.getKind() == emf::compare::ConflictKind::PSEUDO) hasPseudo = true;
    }
    EXPECT_TRUE(hasPseudo);
}

// ===== 测试 12：3-way 无冲突（left 和 right 改了不同 feature） =====
EMF_TEST(CompareE2E_ThreeWay_NoConflict_WhenDifferentFeatures) {
    initEnv();
    auto m = loadModel();
    // origin: name="O", B1(title="T1",pages=10)
    // left:   name="L" (改 name)
    // right:  保持 name="O"，但 B1.pages=99 (改 pages) → 无冲突
    auto* origin = buildLibrary(m, "O", {{"T1", 10}});
    auto* left = buildLibrary(m, "L", {{"T1", 10}});
    auto* right = buildLibrary(m, "O", {{"T1", 99}});

    auto comp = emf::compare::compare(left, right, origin);
    // name 只在 left 改，pages 只在 right 改 → 无冲突
    EXPECT_EQ(comp.getConflicts().size(), 0u);
}

// ===== 测试 13：IdentifierProvider 注入（对齐 artop IdentifiableUtil） =====
// 验证：外部 provider 返回的 ID 被用于严格匹配，而非 proximity。
// 场景：两个 Book title 完全不同（proximity 会判 DIFFERENT 或不配对），
// 但 provider 用 pages 作 ID → 同 ID 严格匹配，title 不同产 CHANGE。
EMF_TEST(CompareE2E_IdentifierProvider_MatchesByIdNotProximity) {
    initEnv();
    auto m = loadModel();
    // left:  Book(title="ProGit", pages=42)
    // right: Book(title="Refactoring", pages=42) —— title 完全不同
    auto* left = buildLibrary(m, "L", {{"ProGit", 42}});
    auto* right = buildLibrary(m, "R", {{"Refactoring", 42}});

    // provider：用 pages 的字符串作 ID（模拟 artop shortName）
    // pages 是 int，转 string 作 ID
    auto provider = [](EObject* obj) -> std::string {
        auto* cls = obj ? obj->eClass() : nullptr;
        if (!cls || cls->getName() != "Book") return {};
        auto* sf = cls->getEStructuralFeature("pages");
        if (!sf) return {};
        std::any v = obj->eGet(sf);
        if (v.type() == typeid(int)) return std::to_string(std::any_cast<int>(v));
        return {};
    };

    // 不用 provider：proximity 按 title 相似度，ProGit vs Refactoring 相似度低
    // 可能不配对或配对为 DIFFERENT
    Comparison compNoProvider = emf::compare::compare(left, right);

    // 用 provider：pages=42 作 ID，严格匹配 → 1 个 Book match + title CHANGE
    Comparison comp = emf::compare::compare(left, right, provider);

    // 验证：有 Book match（非 ABSENT）
    bool hasBookMatch = false;
    for (auto& mm : comp.getMatches()) {
        if (mm.getLeft() && mm.getRight()
            && mm.getLeft()->eClass() && mm.getLeft()->eClass()->getName() == "Book") {
            hasBookMatch = true;
            break;
        }
    }
    EXPECT_TRUE(hasBookMatch);

    // 验证：title 不同应产 1 个 ATTRIBUTE_CHANGE
    size_t attrChange = 0;
    for (auto* d : comp.getDifferences()) {
        if (d->getType() == emf::compare::DiffType::ATTRIBUTE_CHANGE) ++attrChange;
    }
    EXPECT_TRUE(attrChange >= 1u);
}
