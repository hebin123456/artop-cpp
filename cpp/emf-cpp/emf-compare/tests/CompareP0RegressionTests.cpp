// CompareP0RegressionTests.cpp —— Compare P0 修复回归测试
// 覆盖：P0-1 自动 ID 匹配 / P0-2 Diff 子类型+old/new value / P0-3 多值冲突
//       P0-4 双向 merge / P0-5 ADD 克隆 / P0-6 CHANGE reference 映射
//       P0-7 eOpposite 维护 / P0-8 Equivalence
#include "test_main.h"
#include "emf/compare/Comparison.h"
#include "emf/compare/MatchEngine.h"
#include "emf/compare/DiffEngine.h"
#include "emf/compare/MergeEngine.h"
#include "emf/compare/RequirementEngine.h"
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
using emf::ecore::EAttribute;
using emf::ecore::EReference;
using emf::ecore::EStructuralFeature;
using emf::ecore::EFactory;
using emf::common::EObject;
using emf::compare::Comparison;
using emf::compare::MatchEngine;
using emf::compare::DiffEngine;
using emf::compare::MergeEngine;
using emf::compare::RequirementEngine;
using emf::compare::MergeDirection;
using emf::compare::DiffKind;
using emf::compare::DiffType;
using emf::compare::MatchKind;
using emf::compare::ConflictKind;

namespace {

// 模型 A：Item(id:string, name:string)，id 是 ID 属性
const char* kIdModelEcore =
    "<?xml version=\"1.0\"?>\n"
    "<ecore:EPackage xmlns:ecore=\"http://www.eclipse.org/emf/2002/Ecore\" "
    "xmlns:xmi=\"http://www.omg.org/XMI\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
    "xmi:version=\"2.0\" name=\"shop\" nsURI=\"http://example.com/shop/1.0\" nsPrefix=\"shop\">"
    "<eClassifiers xsi:type=\"ecore:EClass\" name=\"Item\">"
    "<eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"id\" "
    "eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString\" iD=\"true\"/>"
    "<eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"name\" "
    "eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString\"/>"
    "</eClassifiers>"
    "<eClassifiers xsi:type=\"ecore:EClass\" name=\"Shop\">"
    "<eStructuralFeatures xsi:type=\"ecore:EReference\" name=\"items\" upperBound=\"-1\" "
    "eType=\"#//Item\" containment=\"true\"/>"
    "</eClassifiers>"
    "</ecore:EPackage>";

struct IdModelMeta {
    EPackage* pkg = nullptr;
    EClass* itemCls = nullptr;
    EClass* shopCls = nullptr;
    EFactory* factory = nullptr;
};

IdModelMeta loadIdModel() {
    XMIResource res;
    res.loadFromString(std::string(kIdModelEcore));
    IdModelMeta m;
    m.pkg = dynamic_cast<EPackage*>(res.getContents().front());
    res.getContents().clear();
    m.itemCls = dynamic_cast<EClass*>(m.pkg->getEClassifier("Item"));
    m.shopCls = dynamic_cast<EClass*>(m.pkg->getEClassifier("Shop"));
    m.factory = m.pkg->getEFactoryInstance();
    return m;
}

void initEnv() {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    XMIResourceFactory::registerDefaults();
}

// 构造 Shop -> [Item(id,name), ...]
EObject* buildShop(const IdModelMeta& m,
                   const std::vector<std::pair<std::string, std::string>>& items) {
    auto* shop = m.factory->create(m.shopCls);
    auto itemsV = shop->eGet(m.shopCls->getEStructuralFeature("items"));
    auto* itemsList = std::any_cast<emf::common::EList<EObject*>*>(itemsV);
    for (auto& [id, name] : items) {
        auto* it = m.factory->create(m.itemCls);
        it->eSet(m.itemCls->getEStructuralFeature("id"), std::any(id));
        it->eSet(m.itemCls->getEStructuralFeature("name"), std::any(name));
        itemsList->add(it);
    }
    return shop;
}

Comparison compareModels(EObject* left, EObject* right) {
    Comparison comp;
    MatchEngine me;
    me.match(left, right, comp);
    DiffEngine de;
    de.diff(comp);
    return comp;
}

size_t countDiffs(const Comparison& comp, DiffKind kind) {
    size_t n = 0;
    for (auto* d : comp.getDifferences()) {
        if (d->getKind() == kind) ++n;
    }
    return n;
}

size_t countDiffsOfType(const Comparison& comp, DiffType type) {
    size_t n = 0;
    for (auto* d : comp.getDifferences()) {
        if (d->getType() == type) ++n;
    }
    return n;
}

}  // namespace

// ===== P0-1: 自动 ID 匹配 =====
// 同 ID 同 EClass 但属性不同的对象应匹配为 IDENTICAL（按 ID 匹配，不按相似度）
EMF_TEST(CompareP0_AutoIdMatch_SameIdDifferentAttr_MatchesAsIdentical) {
    initEnv();
    auto m = loadIdModel();
    // left: Item(id="A", name="foo")
    // right: Item(id="A", name="bar") —— 同 ID 但 name 不同
    // 按 ID 匹配应配对成同一对象，name 不同产 CHANGE diff
    auto* left = buildShop(m, {{"A", "foo"}});
    auto* right = buildShop(m, {{"A", "bar"}});

    auto comp = compareModels(left, right);
    // ID 相同 → 配对成功 → name 不同产 1 个 CHANGE diff
    EXPECT_TRUE(comp.getMatches().size() >= 2u);  // Shop match + Item match
    EXPECT_EQ(countDiffs(comp, DiffKind::CHANGE), 1u);
    // 无 ADD/DELETE
    EXPECT_EQ(countDiffs(comp, DiffKind::ADD), 0u);
    EXPECT_EQ(countDiffs(comp, DiffKind::DELETE), 0u);
}

// P0-1：不同顺序但同 ID 的对象仍按 ID 配对（不依赖位置）
EMF_TEST(CompareP0_AutoIdMatch_DifferentOrder_StillPairsById) {
    initEnv();
    auto m = loadIdModel();
    // left: [A, B]，right: [B, A] —— 顺序不同
    auto* left = buildShop(m, {{"A", "a"}, {"B", "b"}});
    auto* right = buildShop(m, {{"B", "b"}, {"A", "a"}});

    auto comp = compareModels(left, right);
    // ID 匹配应把 A↔A、B↔B 配对（而非 A↔B、B↔A）。
    // 验证：name 都相同 → 无 CHANGE diff（若 proximity 错配则会产 2 个 CHANGE）。
    // 注：顺序不同会产 MOVE diff，这是正常的，此处只验证 ID 配对正确性。
    EXPECT_EQ(countDiffs(comp, DiffKind::CHANGE), 0u);
    EXPECT_EQ(countDiffs(comp, DiffKind::ADD), 0u);
    EXPECT_EQ(countDiffs(comp, DiffKind::DELETE), 0u);
}

// P0-1：禁用 ID 匹配后退化为 proximity（按相似度）
EMF_TEST(CompareP0_AutoIdMatch_Disabled_FallsBackToProximity) {
    initEnv();
    auto m = loadIdModel();
    auto* left = buildShop(m, {{"A", "a"}});
    auto* right = buildShop(m, {{"A", "b"}});

    Comparison comp;
    MatchEngine me;
    me.setUseIdAttribute(false);  // 禁用自动 ID
    me.match(left, right, comp);
    DiffEngine de;
    de.diff(comp);
    // proximity 仍能配对（同 EClass 单元素），name 不同产 1 个 CHANGE
    EXPECT_EQ(countDiffs(comp, DiffKind::CHANGE), 1u);
}

// ===== P0-2: Diff 子类型 + old/new value =====
EMF_TEST(CompareP0_DiffType_AttributeChange_SetOnAttributeDiff) {
    initEnv();
    auto m = loadIdModel();
    auto* left = buildShop(m, {{"A", "foo"}});
    auto* right = buildShop(m, {{"A", "bar"}});

    auto comp = compareModels(left, right);
    // name CHANGE 应是 ATTRIBUTE_CHANGE 类型
    bool foundAttrChange = false;
    for (auto* d : comp.getDifferences()) {
        if (d->getKind() == DiffKind::CHANGE && d->getAttributeName() == "name") {
            EXPECT_TRUE(d->getType() == DiffType::ATTRIBUTE_CHANGE);
            // old/new value 应非空
            EXPECT_TRUE(d->getOldValue().has_value());
            EXPECT_TRUE(d->getNewValue().has_value());
            // 旧值 = "foo"，新值 = "bar"
            EXPECT_EQ(std::any_cast<std::string>(d->getOldValue()), std::string("foo"));
            EXPECT_EQ(std::any_cast<std::string>(d->getNewValue()), std::string("bar"));
            foundAttrChange = true;
        }
    }
    EXPECT_TRUE(foundAttrChange);
}

EMF_TEST(CompareP0_DiffType_ElementChange_SetOnAddDelete) {
    initEnv();
    auto m = loadIdModel();
    // left: [A]，right: [A, B]（B 新增）
    auto* left = buildShop(m, {{"A", "a"}});
    auto* right = buildShop(m, {{"A", "a"}, {"B", "b"}});

    auto comp = compareModels(left, right);
    // ADD 应是 ELEMENT_CHANGE 类型，且 newValue 非空
    bool foundElementAdd = false;
    for (auto* d : comp.getDifferences()) {
        if (d->getKind() == DiffKind::ADD) {
            EXPECT_TRUE(d->getType() == DiffType::ELEMENT_CHANGE);
            EXPECT_TRUE(d->getNewValue().has_value());
            foundElementAdd = true;
        }
    }
    EXPECT_TRUE(foundElementAdd);
}

// ===== P0-3: 多值 feature 冲突检测 =====
EMF_TEST(CompareP0_MultiValueConflict_Real_WhenBothSidesAddDifferent) {
    initEnv();
    auto m = loadIdModel();
    // origin: [A]
    // left:   [A, B]  (添加 B)
    // right:  [A, C]  (添加 C，不同于 B) → REAL 冲突 on items
    auto* origin = buildShop(m, {{"A", "a"}});
    auto* left = buildShop(m, {{"A", "a"}, {"B", "b"}});
    auto* right = buildShop(m, {{"A", "a"}, {"C", "c"}});

    auto comp = emf::compare::compare(left, right, origin);
    EXPECT_TRUE(comp.getConflicts().size() > 0u);
    bool hasReal = false;
    for (auto& c : comp.getConflicts()) {
        if (c.getKind() == ConflictKind::REAL) hasReal = true;
    }
    EXPECT_TRUE(hasReal);
}

EMF_TEST(CompareP0_MultiValueConflict_Pseudo_WhenBothSidesAddSame) {
    initEnv();
    auto m = loadIdModel();
    // origin: [A]
    // left:   [A, B]  (添加 B)
    // right:  [A, B]  (添加相同的 B，同 ID) → PSEUDO 冲突
    auto* origin = buildShop(m, {{"A", "a"}});
    auto* left = buildShop(m, {{"A", "a"}, {"B", "b"}});
    auto* right = buildShop(m, {{"A", "a"}, {"B", "b"}});

    auto comp = emf::compare::compare(left, right, origin);
    // 两边都添加了同 ID 的 B → 集合变化相同 → PSEUDO
    bool hasPseudo = false;
    for (auto& c : comp.getConflicts()) {
        if (c.getKind() == ConflictKind::PSEUDO) hasPseudo = true;
    }
    EXPECT_TRUE(hasPseudo);
}

// ===== P0-4: MergeEngine 双向 =====
EMF_TEST(CompareP0_Merge_LeftToRight_AppliesLeftChangesToRight) {
    initEnv();
    auto m = loadIdModel();
    // left: Item(id="A", name="new")，right: Item(id="A", name="old")
    // LEFT_TO_RIGHT：把 left 的 name 同步到 right
    auto* left = buildShop(m, {{"A", "new"}});
    auto* right = buildShop(m, {{"A", "old"}});

    auto comp = compareModels(left, right);
    MergeEngine me;
    EXPECT_TRUE(me.merge(comp, right, MergeDirection::LEFT_TO_RIGHT));
    // right 端的 Item.name 应同步为 left 的 "new"
    auto itemsV = right->eGet(m.shopCls->getEStructuralFeature("items"));
    auto* itemsList = std::any_cast<emf::common::EList<EObject*>*>(itemsV);
    EXPECT_EQ(itemsList->size(), 1u);
    auto* item = itemsList->get(0);
    auto nameV = item->eGet(m.itemCls->getEStructuralFeature("name"));
    EXPECT_EQ(std::any_cast<std::string>(nameV), std::string("new"));
}

EMF_TEST(CompareP0_Merge_RightToLeft_DefaultDirection) {
    initEnv();
    auto m = loadIdModel();
    auto* left = buildShop(m, {{"A", "old"}});
    auto* right = buildShop(m, {{"A", "new"}});

    auto comp = compareModels(left, right);
    MergeEngine me;
    // 默认方向 RIGHT_TO_LEFT
    EXPECT_TRUE(me.merge(comp, left));
    auto itemsV = left->eGet(m.shopCls->getEStructuralFeature("items"));
    auto* itemsList = std::any_cast<emf::common::EList<EObject*>*>(itemsV);
    auto* item = itemsList->get(0);
    auto nameV = item->eGet(m.itemCls->getEStructuralFeature("name"));
    EXPECT_EQ(std::any_cast<std::string>(nameV), std::string("new"));
}

// ===== P0-5: ADD merge 克隆语义 =====
EMF_TEST(CompareP0_Merge_Add_ClonesObject_NotSharingPointer) {
    initEnv();
    auto m = loadIdModel();
    // left: [A]，right: [A, B]（B 新增）
    auto* left = buildShop(m, {{"A", "a"}});
    auto* right = buildShop(m, {{"A", "a"}, {"B", "b"}});

    auto comp = compareModels(left, right);
    MergeEngine me;
    EXPECT_TRUE(me.merge(comp, left));

    auto leftItemsV = left->eGet(m.shopCls->getEStructuralFeature("items"));
    auto* leftItems = std::any_cast<emf::common::EList<EObject*>*>(leftItemsV);
    EXPECT_EQ(leftItems->size(), 2u);
    auto rightItemsV = right->eGet(m.shopCls->getEStructuralFeature("items"));
    auto* rightItems = std::any_cast<emf::common::EList<EObject*>*>(rightItemsV);

    // left 端的新增对象不应是 right 端的同一指针（应克隆）
    auto* leftAdded = leftItems->get(1);
    auto* rightAdded = rightItems->get(1);
    EXPECT_TRUE(leftAdded != rightAdded);
    // 但属性应相同
    auto leftNameV = leftAdded->eGet(m.itemCls->getEStructuralFeature("name"));
    EXPECT_EQ(std::any_cast<std::string>(leftNameV), std::string("b"));
}

// ===== P0-6: CHANGE reference merge 映射 =====
// 用 Library 模型：Library 有 books，Book 有 author（单值非 containment reference）
// 此场景验证：right 端 book 的 author 引用，merge 时映射到 left 端对应 author
namespace {

const char* kLibraryAuthorEcore =
    "<?xml version=\"1.0\"?>\n"
    "<ecore:EPackage xmlns:ecore=\"http://www.eclipse.org/emf/2002/Ecore\" "
    "xmlns:xmi=\"http://www.omg.org/XMI\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
    "xmi:version=\"2.0\" name=\"lib\" nsURI=\"http://example.com/lib/1.0\" nsPrefix=\"lib\">"
    "<eClassifiers xsi:type=\"ecore:EClass\" name=\"Library\">"
    "<eStructuralFeatures xsi:type=\"ecore:EReference\" name=\"books\" upperBound=\"-1\" "
    "eType=\"#//Book\" containment=\"true\"/>"
    "<eStructuralFeatures xsi:type=\"ecore:EReference\" name=\"authors\" upperBound=\"-1\" "
    "eType=\"#//Author\" containment=\"true\"/>"
    "</eClassifiers>"
    "<eClassifiers xsi:type=\"ecore:EClass\" name=\"Book\">"
    "<eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"title\" "
    "eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString\"/>"
    "<eStructuralFeatures xsi:type=\"ecore:EReference\" name=\"author\" "
    "eType=\"#//Author\"/>"
    "</eClassifiers>"
    "<eClassifiers xsi:type=\"ecore:EClass\" name=\"Author\">"
    "<eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"name\" "
    "eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString\"/>"
    "</eClassifiers>"
    "</ecore:EPackage>";

struct LibAuthorMeta {
    EPackage* pkg = nullptr;
    EClass* libCls = nullptr;
    EClass* bookCls = nullptr;
    EClass* authorCls = nullptr;
    EFactory* factory = nullptr;
};

LibAuthorMeta loadLibAuthorModel() {
    XMIResource res;
    res.loadFromString(std::string(kLibraryAuthorEcore));
    LibAuthorMeta m;
    m.pkg = dynamic_cast<EPackage*>(res.getContents().front());
    res.getContents().clear();
    m.libCls = dynamic_cast<EClass*>(m.pkg->getEClassifier("Library"));
    m.bookCls = dynamic_cast<EClass*>(m.pkg->getEClassifier("Book"));
    m.authorCls = dynamic_cast<EClass*>(m.pkg->getEClassifier("Author"));
    m.factory = m.pkg->getEFactoryInstance();
    return m;
}

}  // namespace

EMF_TEST(CompareP0_Merge_ChangeReference_MapsToTargetSideObject) {
    initEnv();
    auto m = loadLibAuthorModel();
    // left: Library{ books=[Book(title=T, author=Author(name=Alice))], authors=[Alice] }
    // right: Library{ books=[Book(title=T, author=Author(name=Bob))], authors=[Bob] }
    // 即 right 端把 book.author 改成 Bob
    // RIGHT_TO_LEFT merge 后，left 端 book.author 应映射到 left 端的 Bob 对象（非 right 端指针）
    auto buildLib = [&](const std::string& authorName) {
        auto* lib = m.factory->create(m.libCls);
        auto* book = m.factory->create(m.bookCls);
        book->eSet(m.bookCls->getEStructuralFeature("title"), std::any(std::string("T")));
        auto* author = m.factory->create(m.authorCls);
        author->eSet(m.authorCls->getEStructuralFeature("name"), std::any(authorName));
        auto booksV = lib->eGet(m.libCls->getEStructuralFeature("books"));
        std::any_cast<emf::common::EList<EObject*>*>(booksV)->add(book);
        auto authorsV = lib->eGet(m.libCls->getEStructuralFeature("authors"));
        std::any_cast<emf::common::EList<EObject*>*>(authorsV)->add(author);
        book->eSet(m.bookCls->getEStructuralFeature("author"), std::any(author));
        return lib;
    };
    auto* left = buildLib("Alice");
    auto* right = buildLib("Bob");

    auto comp = compareModels(left, right);
    MergeEngine me;
    EXPECT_TRUE(me.merge(comp, left));

    // 验证 left.book.author 现在指向 left 端的 Bob（而非 right 端的）
    auto leftBooksV = left->eGet(m.libCls->getEStructuralFeature("books"));
    auto* leftBooks = std::any_cast<emf::common::EList<EObject*>*>(leftBooksV);
    auto* leftBook = leftBooks->get(0);
    auto authorV = leftBook->eGet(m.bookCls->getEStructuralFeature("author"));
    auto* leftBookAuthor = std::any_cast<EObject*>(authorV);

    auto leftAuthorsV = left->eGet(m.libCls->getEStructuralFeature("authors"));
    auto* leftAuthors = std::any_cast<emf::common::EList<EObject*>*>(leftAuthorsV);
    auto* leftBob = leftAuthors->get(0);
    // 引用应映射到 left 端的 Bob（指针相等）
    EXPECT_TRUE(leftBookAuthor == leftBob);
    auto nameV = leftBob->eGet(m.authorCls->getEStructuralFeature("name"));
    EXPECT_EQ(std::any_cast<std::string>(nameV), std::string("Bob"));
}

// ===== G6/G12: RequirementEngine + 拓扑序 merge =====
// 场景：right 端新增 Bob 到 authors，同时把 Book.author 由 Alice 改成 Bob。
// （Book.title 也微调以使 Book 匹配为 DIFFERENT，从而 diff 引擎产出 author 的 REFERENCE_CHANGE）
// REFERENCE_CHANGE(Book.author) 依赖 ADD(Bob)：拓扑序须先 ADD Bob（克隆到 left.authors
// 并注册 srcToDst），再合并 REFERENCE_CHANGE（把引用映射到 left 端 Bob 克隆）。
// 无拓扑序时 REFERENCE_CHANGE 先合并会落到“无映射”分支，left.book.author 错指 right 端 Bob。
EMF_TEST(CompareP0_Merge_ReferenceChangeToAddedObject_MapsToClone) {
    initEnv();
    auto m = loadLibAuthorModel();
    // left:  Library{ books=[Book(title=T,  author=Alice)], authors=[Alice] }
    // right: Library{ books=[Book(title=T2, author=Bob)],   authors=[Alice, Bob] }
    auto buildLib = [&](const std::string& title, bool addBob) {
        auto* lib = m.factory->create(m.libCls);
        auto* book = m.factory->create(m.bookCls);
        book->eSet(m.bookCls->getEStructuralFeature("title"), std::any(title));
        auto* alice = m.factory->create(m.authorCls);
        alice->eSet(m.authorCls->getEStructuralFeature("name"), std::any(std::string("Alice")));
        std::any_cast<emf::common::EList<EObject*>*>(
            lib->eGet(m.libCls->getEStructuralFeature("authors")))->add(alice);
        EObject* authorForBook = alice;
        if (addBob) {
            auto* bob = m.factory->create(m.authorCls);
            bob->eSet(m.authorCls->getEStructuralFeature("name"), std::any(std::string("Bob")));
            std::any_cast<emf::common::EList<EObject*>*>(
                lib->eGet(m.libCls->getEStructuralFeature("authors")))->add(bob);
            authorForBook = bob;
        }
        book->eSet(m.bookCls->getEStructuralFeature("author"), std::any(authorForBook));
        std::any_cast<emf::common::EList<EObject*>*>(
            lib->eGet(m.libCls->getEStructuralFeature("books")))->add(book);
        return lib;
    };
    auto* left = buildLib("T", false);    // authors=[Alice], book.author=Alice
    auto* right = buildLib("T2", true);   // authors=[Alice, Bob], book.author=Bob

    auto comp = compareModels(left, right);
    // 应同时存在 ADD(Bob) 与 REFERENCE_CHANGE(Book.author)
    EXPECT_TRUE(countDiffs(comp, DiffKind::ADD) >= 1u);
    bool hasRefChange = false;
    for (auto* d : comp.getDifferences()) {
        if (d->getType() == DiffType::REFERENCE_CHANGE) hasRefChange = true;
    }
    EXPECT_TRUE(hasRefChange);
    // G6：依赖应被 RequirementEngine 计算出来（REFERENCE_CHANGE 依赖 ADD Bob）
    RequirementEngine req;
    req.computeRequirements(comp);
    EXPECT_TRUE(comp.getDependencies().size() > 0u);

    MergeEngine me;
    EXPECT_TRUE(me.merge(comp, left));

    // left.authors 现应有 2 个（Alice + Bob 克隆）
    auto leftAuthorsV = left->eGet(m.libCls->getEStructuralFeature("authors"));
    auto* leftAuthors = std::any_cast<emf::common::EList<EObject*>*>(leftAuthorsV);
    EXPECT_EQ(leftAuthors->size(), 2u);
    // 找出 left 端 Bob 克隆
    EObject* leftBob = nullptr;
    for (size_t i = 0; i < leftAuthors->size(); ++i) {
        auto* a = leftAuthors->get(i);
        auto nm = a->eGet(m.authorCls->getEStructuralFeature("name"));
        if (std::any_cast<std::string>(nm) == "Bob") { leftBob = a; break; }
    }
    EXPECT_NOT_NULL(leftBob);
    // 关键：left.book.author 应指向 left 端 Bob 克隆（拓扑序合并结果），而非 right 端 Bob 指针
    auto leftBooksV = left->eGet(m.libCls->getEStructuralFeature("books"));
    auto* leftBooks = std::any_cast<emf::common::EList<EObject*>*>(leftBooksV);
    auto* leftBook = leftBooks->get(0);
    auto authorV = leftBook->eGet(m.bookCls->getEStructuralFeature("author"));
    auto* leftBookAuthor = std::any_cast<EObject*>(authorV);
    EXPECT_TRUE(leftBookAuthor == leftBob);
}

// ===== P0-7: eOpposite 维护 =====
// 用双向引用模型：Parent.children <-> Child.parent
namespace {

const char* kBidirectionalEcore =
    "<?xml version=\"1.0\"?>\n"
    "<ecore:EPackage xmlns:ecore=\"http://www.eclipse.org/emf/2002/Ecore\" "
    "xmlns:xmi=\"http://www.omg.org/XMI\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
    "xmi:version=\"2.0\" name=\"fam\" nsURI=\"http://example.com/fam/1.0\" nsPrefix=\"fam\">"
    "<eClassifiers xsi:type=\"ecore:EClass\" name=\"Parent\">"
    "<eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"name\" "
    "eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString\"/>"
    "<eStructuralFeatures xsi:type=\"ecore:EReference\" name=\"children\" upperBound=\"-1\" "
    "eType=\"#//Child\" containment=\"true\" eOpposite=\"#//Child/parent\"/>"
    "</eClassifiers>"
    "<eClassifiers xsi:type=\"ecore:EClass\" name=\"Child\">"
    "<eStructuralFeatures xsi:type=\"ecore:EAttribute\" name=\"name\" "
    "eType=\"ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString\"/>"
    "<eStructuralFeatures xsi:type=\"ecore:EReference\" name=\"parent\" lowerBound=\"1\" "
    "eType=\"#//Parent\" transient=\"true\" eOpposite=\"#//Parent/children\"/>"
    "</eClassifiers>"
    "</ecore:EPackage>";

struct BiDirMeta {
    EPackage* pkg = nullptr;
    EClass* parentCls = nullptr;
    EClass* childCls = nullptr;
    EFactory* factory = nullptr;
};

BiDirMeta loadBiDirModel() {
    XMIResource res;
    res.loadFromString(std::string(kBidirectionalEcore));
    BiDirMeta m;
    m.pkg = dynamic_cast<EPackage*>(res.getContents().front());
    res.getContents().clear();
    m.parentCls = dynamic_cast<EClass*>(m.pkg->getEClassifier("Parent"));
    m.childCls = dynamic_cast<EClass*>(m.pkg->getEClassifier("Child"));
    m.factory = m.pkg->getEFactoryInstance();
    return m;
}

}  // namespace

EMF_TEST(CompareP0_Merge_EOpposite_MaintainedOnAdd) {
    initEnv();
    auto m = loadBiDirModel();
    // left: Parent(name=P) 无 children
    // right: Parent(name=P) 有 Child(name=C)
    // merge RIGHT_TO_LEFT 后，left 应有 child，且 child.parent 应指向 left.parent
    auto* left = m.factory->create(m.parentCls);
    left->eSet(m.parentCls->getEStructuralFeature("name"), std::any(std::string("P")));

    auto* right = m.factory->create(m.parentCls);
    right->eSet(m.parentCls->getEStructuralFeature("name"), std::any(std::string("P")));
    auto* rightChild = m.factory->create(m.childCls);
    rightChild->eSet(m.childCls->getEStructuralFeature("name"), std::any(std::string("C")));
    auto rightChildrenV = right->eGet(m.parentCls->getEStructuralFeature("children"));
    std::any_cast<emf::common::EList<EObject*>*>(rightChildrenV)->add(rightChild);

    auto comp = compareModels(left, right);
    MergeEngine me;
    EXPECT_TRUE(me.merge(comp, left));

    // left 应有 1 个 child
    auto leftChildrenV = left->eGet(m.parentCls->getEStructuralFeature("children"));
    auto* leftChildren = std::any_cast<emf::common::EList<EObject*>*>(leftChildrenV);
    EXPECT_EQ(leftChildren->size(), 1u);
    auto* leftChild = leftChildren->get(0);
    // child.parent 应指向 left（eOpposite 维护）
    auto parentV = leftChild->eGet(m.childCls->getEStructuralFeature("parent"));
    EXPECT_TRUE(parentV.has_value());
    auto* leftChildParent = std::any_cast<EObject*>(parentV);
    EXPECT_TRUE(leftChildParent == left);
}

// ===== P0-8: Equivalence 关系 =====
EMF_TEST(CompareP0_Equivalence_TrackedForNonContainmentReference) {
    initEnv();
    auto m = loadLibAuthorModel();
    // Library{ books=[Book(author=Alice)], authors=[Alice] }
    // book -> author 是非 containment reference，应产生 Equivalence 关联 book match 和 author match
    auto* lib = m.factory->create(m.libCls);
    auto* book = m.factory->create(m.bookCls);
    book->eSet(m.bookCls->getEStructuralFeature("title"), std::any(std::string("T")));
    auto* author = m.factory->create(m.authorCls);
    author->eSet(m.authorCls->getEStructuralFeature("name"), std::any(std::string("Alice")));
    auto booksV = lib->eGet(m.libCls->getEStructuralFeature("books"));
    std::any_cast<emf::common::EList<EObject*>*>(booksV)->add(book);
    auto authorsV = lib->eGet(m.libCls->getEStructuralFeature("authors"));
    std::any_cast<emf::common::EList<EObject*>*>(authorsV)->add(author);
    book->eSet(m.bookCls->getEStructuralFeature("author"), std::any(author));

    auto comp = emf::compare::compare(lib, lib);
    // 应至少有 1 个 Equivalence 关联 book match 和 author match
    EXPECT_TRUE(comp.getEquivalences().size() > 0u);
}

// Equivalence 在 3-way 也应工作
EMF_TEST(CompareP0_Equivalence_TrackedInThreeWayCompare) {
    initEnv();
    auto m = loadLibAuthorModel();
    auto buildLib = [&]() {
        auto* lib = m.factory->create(m.libCls);
        auto* book = m.factory->create(m.bookCls);
        book->eSet(m.bookCls->getEStructuralFeature("title"), std::any(std::string("T")));
        auto* author = m.factory->create(m.authorCls);
        author->eSet(m.authorCls->getEStructuralFeature("name"), std::any(std::string("Alice")));
        auto booksV = lib->eGet(m.libCls->getEStructuralFeature("books"));
        std::any_cast<emf::common::EList<EObject*>*>(booksV)->add(book);
        auto authorsV = lib->eGet(m.libCls->getEStructuralFeature("authors"));
        std::any_cast<emf::common::EList<EObject*>*>(authorsV)->add(author);
        book->eSet(m.bookCls->getEStructuralFeature("author"), std::any(author));
        return lib;
    };
    auto* left = buildLib();
    auto* right = buildLib();
    auto* origin = buildLib();

    auto comp = emf::compare::compare(left, right, origin);
    EXPECT_TRUE(comp.getEquivalences().size() > 0u);
}
