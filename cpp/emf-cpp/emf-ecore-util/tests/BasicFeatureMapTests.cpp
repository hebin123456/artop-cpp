// BasicFeatureMapTests.cpp
// 单元测试：覆盖 emf::ecore::util::BasicFeatureMap 的核心行为
// 严格 1:1 对齐 Java BasicFeatureMap 行为
//   1) AddEntry                 — add 单一 entry 后 size == 1
//   2) AddMultipleEntries       — add 3 个 entry 后 size == 3
//   3) AddByFeature             — add(feature, value) 自动创建 entry
//   4) AddByFeatureAndIndex     — add(feature, index, value) 在指定位置插入
//   5) RemoveEntry              — remove 后 size 减 1
//   6) GetByFeature             — get(feature, 0) 取值
//   7) SetByFeature             — set(feature, 0, value) 设置并返回旧值
//   8) ValuesByFeature          — values(feature) 返回 vector<any>
//   9) EntriesByFeature         — entries(feature) 返回 vector<Entry*>
//  10) SizeByFeature            — size(feature) 返回该 feature 的 entry 数
//  11) IteratorBasic            — iterator 遍历所有 entry
//  12) ListIterator             — listIterator 支持双向遍历
//  13) Clear                    — clear 后 size == 0
//  14) Contains                 — contains(entry) true/false
//  15) ViewFeatureValues        — FeatureValuesView.get / size
//  16) ViewFeatureEntries       — FeatureEntriesView.get / size
//  17) IndexOf / lastIndexOf    — indexOf / lastIndexOf
//  18) AddWithAnyValue          — add(std::any) 包装为 entry
#include "test_main.h"

#include "emf/ecore/util/BasicFeatureMap.h"
#include "emf/ecore/util/FeatureMap.h"
#include "emf/ecore/EcorePackage.h"
#include "emf/ecore/EcoreImpls.h"
#include "emf/common/EList.h"

#include <any>
#include <memory>
#include <string>
#include <vector>

using namespace emf;
using namespace emf::ecore;
using namespace emf::ecore::util;
using emf::common::EObject;
using emf::common::EList;

namespace {

// ----- 测试模型：含 FeatureMap 风格的多个 attribute -----
struct TestModel {
    EPackage* pkg = nullptr;
    EClass* rootClass = nullptr;
    EAttribute* attrA = nullptr;
    EAttribute* attrB = nullptr;
    EAttribute* attrC = nullptr;

    void build() {
        EcoreFactory::initialize();
        EcorePackage::initialize();
        EPackageImpl* p = new EPackageImpl();
        p->setName("testFM");
        p->setNsURI("urn:test:fm");
        p->setNsPrefix("fm");
        EFactoryImpl* f = new EFactoryImpl();
        f->setEPackage(p);
        p->setEFactoryInstance(f);
        pkg = p;

        rootClass = EcoreFactory::instance().createEClass();
        rootClass->setName("Root");
        p->addEClassifier(rootClass);

        // attrA / attrB / attrC：upperBound = -1 (unbounded)，对应多个值
        attrA = EcoreFactory::instance().createEAttribute();
        attrA->setName("a");
        attrA->setEAttributeType(EcorePackage::instance().getEDataType_EString());
        attrA->setUpperBound(-1);
        rootClass->addEStructuralFeature(attrA);

        attrB = EcoreFactory::instance().createEAttribute();
        attrB->setName("b");
        attrB->setEAttributeType(EcorePackage::instance().getEDataType_EInt());
        attrB->setUpperBound(-1);
        rootClass->addEStructuralFeature(attrB);

        attrC = EcoreFactory::instance().createEAttribute();
        attrC->setName("c");
        attrC->setEAttributeType(EcorePackage::instance().getEDataType_EString());
        attrC->setUpperBound(-1);
        rootClass->addEStructuralFeature(attrC);
    }
};

}  // namespace

// ===================================================================
// 1) AddEntry
// ===================================================================
EMF_TEST(BasicFeatureMap_AddEntry) {
    TestModel m;
    m.build();
    BasicFeatureMap fm;
    auto* e = new FeatureMap::Entry(m.attrA, std::any{std::string("v1")});
    EXPECT_TRUE(fm.add(e));
    EXPECT_EQ((int)fm.size(), 1);
}

// ===================================================================
// 2) AddMultipleEntries
// ===================================================================
EMF_TEST(BasicFeatureMap_AddMultipleEntries) {
    TestModel m;
    m.build();
    BasicFeatureMap fm;
    fm.add(new FeatureMap::Entry(m.attrA, std::any{std::string("a1")}));
    fm.add(new FeatureMap::Entry(m.attrB, std::any{42}));
    fm.add(new FeatureMap::Entry(m.attrC, std::any{std::string("c1")}));
    EXPECT_EQ((int)fm.size(), 3);
}

// ===================================================================
// 3) AddByFeature
// ===================================================================
EMF_TEST(BasicFeatureMap_AddByFeature) {
    TestModel m;
    m.build();
    BasicFeatureMap fm;
    EXPECT_TRUE(fm.add(m.attrA, std::any{std::string("hello")}));
    EXPECT_TRUE(fm.add(m.attrA, std::any{std::string("world")}));
    EXPECT_EQ((int)fm.size(), 2);
    EXPECT_EQ((int)fm.size(m.attrA), 2);
    EXPECT_EQ((int)fm.size(m.attrB), 0);
}

// ===================================================================
// 4) AddByFeatureAndIndex
// ===================================================================
EMF_TEST(BasicFeatureMap_AddByFeatureAndIndex) {
    TestModel m;
    m.build();
    BasicFeatureMap fm;
    fm.add(m.attrA, std::any{std::string("first")});
    fm.add(m.attrA, std::any{std::string("third")});
    // 在 index = 1 插入 "second"
    EXPECT_TRUE(fm.add(m.attrA, 1, std::any{std::string("second")}));
    auto vals = fm.values(m.attrA);
    EXPECT_EQ((int)vals.size(), 3);
    EXPECT_EQ(std::any_cast<std::string>(vals[0]), std::string("first"));
    EXPECT_EQ(std::any_cast<std::string>(vals[1]), std::string("second"));
    EXPECT_EQ(std::any_cast<std::string>(vals[2]), std::string("third"));
}

// ===================================================================
// 5) RemoveEntry
// ===================================================================
EMF_TEST(BasicFeatureMap_Remove) {
    TestModel m;
    m.build();
    BasicFeatureMap fm;
    auto* e1 = new FeatureMap::Entry(m.attrA, std::any{std::string("v1")});
    auto* e2 = new FeatureMap::Entry(m.attrA, std::any{std::string("v2")});
    fm.add(e1);
    fm.add(e2);
    EXPECT_EQ((int)fm.size(), 2);
    EXPECT_TRUE(fm.remove(e1));
    EXPECT_EQ((int)fm.size(), 1);
    EXPECT_FALSE(fm.remove(e1));  // 第二次 remove 返回 false
}

// ===================================================================
// 6) GetByFeature
// ===================================================================
EMF_TEST(BasicFeatureMap_GetByFeature) {
    TestModel m;
    m.build();
    BasicFeatureMap fm;
    fm.add(m.attrA, std::any{std::string("alpha")});
    fm.add(m.attrA, std::any{std::string("beta")});
    auto v = fm.get(m.attrA, 1);
    EXPECT_EQ(std::any_cast<std::string>(v), std::string("beta"));
}

// ===================================================================
// 7) SetByFeature
// ===================================================================
EMF_TEST(BasicFeatureMap_SetByFeature) {
    TestModel m;
    m.build();
    BasicFeatureMap fm;
    fm.add(m.attrA, std::any{std::string("old1")});
    fm.add(m.attrA, std::any{std::string("old2")});
    auto old = fm.set(m.attrA, 0, std::any{std::string("new1")});
    EXPECT_EQ(std::any_cast<std::string>(old), std::string("old1"));
    auto current = fm.get(m.attrA, 0);
    EXPECT_EQ(std::any_cast<std::string>(current), std::string("new1"));
}

// ===================================================================
// 8) ValuesByFeature
// ===================================================================
EMF_TEST(BasicFeatureMap_ValuesByFeature) {
    TestModel m;
    m.build();
    BasicFeatureMap fm;
    fm.add(m.attrA, std::any{std::string("x")});
    fm.add(m.attrB, std::any{100});
    fm.add(m.attrA, std::any{std::string("y")});
    auto vals = fm.values(m.attrA);
    EXPECT_EQ((int)vals.size(), 2);
    EXPECT_EQ(std::any_cast<std::string>(vals[0]), std::string("x"));
    EXPECT_EQ(std::any_cast<std::string>(vals[1]), std::string("y"));
}

// ===================================================================
// 9) EntriesByFeature
// ===================================================================
EMF_TEST(BasicFeatureMap_EntriesByFeature) {
    TestModel m;
    m.build();
    BasicFeatureMap fm;
    fm.add(m.attrA, std::any{std::string("e1")});
    fm.add(m.attrB, std::any{1});
    fm.add(m.attrA, std::any{std::string("e2")});
    auto entries = fm.entries(m.attrA);
    EXPECT_EQ((int)entries.size(), 2);
    EXPECT_TRUE(entries[0] != nullptr);
    EXPECT_TRUE(entries[0]->getEStructuralFeature() == m.attrA);
}

// ===================================================================
// 10) SizeByFeature
// ===================================================================
EMF_TEST(BasicFeatureMap_SizeByFeature) {
    TestModel m;
    m.build();
    BasicFeatureMap fm;
    EXPECT_EQ((int)fm.size(m.attrA), 0);
    fm.add(m.attrA, std::any{std::string("a")});
    fm.add(m.attrA, std::any{std::string("b")});
    fm.add(m.attrA, std::any{std::string("c")});
    fm.add(m.attrB, std::any{1});
    EXPECT_EQ((int)fm.size(m.attrA), 3);
    EXPECT_EQ((int)fm.size(m.attrB), 1);
    EXPECT_EQ((int)fm.size(m.attrC), 0);
}

// ===================================================================
// 11) IteratorBasic
// ===================================================================
EMF_TEST(BasicFeatureMap_IteratorBasic) {
    TestModel m;
    m.build();
    BasicFeatureMap fm;
    fm.add(m.attrA, std::any{std::string("a1")});
    fm.add(m.attrB, std::any{1});
    fm.add(m.attrA, std::any{std::string("a2")});
    auto it = fm.iterator();
    int count = 0;
    while (it->hasNext()) {
        auto* e = it->next();
        EXPECT_TRUE(e != nullptr);
        count++;
    }
    EXPECT_EQ(count, 3);
}

// ===================================================================
// 12) ListIterator
// ===================================================================
EMF_TEST(BasicFeatureMap_ListIterator) {
    TestModel m;
    m.build();
    BasicFeatureMap fm;
    fm.add(m.attrA, std::any{std::string("v1")});
    fm.add(m.attrA, std::any{std::string("v2")});
    fm.add(m.attrA, std::any{std::string("v3")});
    auto li = fm.listIterator();
    int forward = 0;
    while (li->hasNext()) {
        li->next();
        forward++;
    }
    EXPECT_EQ(forward, 3);
    // 反向遍历
    int backward = 0;
    while (li->hasPrevious()) {
        li->previous();
        backward++;
    }
    EXPECT_EQ(backward, 3);
}

// ===================================================================
// 13) Clear
// ===================================================================
EMF_TEST(BasicFeatureMap_Clear) {
    TestModel m;
    m.build();
    BasicFeatureMap fm;
    fm.add(m.attrA, std::any{std::string("a1")});
    fm.add(m.attrA, std::any{std::string("a2")});
    fm.add(m.attrB, std::any{1});
    EXPECT_EQ((int)fm.size(), 3);
    fm.clear();
    EXPECT_EQ((int)fm.size(), 0);
    EXPECT_EQ((int)fm.size(m.attrA), 0);
    EXPECT_EQ((int)fm.size(m.attrB), 0);
}

// ===================================================================
// 14) Contains
// ===================================================================
EMF_TEST(BasicFeatureMap_Contains) {
    TestModel m;
    m.build();
    BasicFeatureMap fm;
    auto* e1 = new FeatureMap::Entry(m.attrA, std::any{std::string("a1")});
    auto* e2 = new FeatureMap::Entry(m.attrA, std::any{std::string("a2")});
    fm.add(e1);
    EXPECT_TRUE(fm.contains(e1));
    EXPECT_FALSE(fm.contains(e2));
}

// ===================================================================
// 15) FeatureValuesView
// ===================================================================
EMF_TEST(BasicFeatureMap_ViewFeatureValues) {
    TestModel m;
    m.build();
    BasicFeatureMap fm;
    fm.add(m.attrA, std::any{std::string("x")});
    fm.add(m.attrA, std::any{std::string("y")});
    auto view = fm.valuesView(m.attrA);
    EXPECT_EQ(view.size(), 2);
    auto v = view.get(0);
    EXPECT_EQ(std::any_cast<std::string>(v), std::string("x"));
    EXPECT_TRUE(view.contains(std::any{std::string("y")}));
    EXPECT_EQ(view.indexOf(std::any{std::string("x")}), 0);
    EXPECT_EQ(view.indexOf(std::any{std::string("z")}), -1);
    EXPECT_TRUE(view.remove(std::any{std::string("x")}));
    EXPECT_EQ(view.size(), 1);
}

// ===================================================================
// 16) FeatureEntriesView
// ===================================================================
EMF_TEST(BasicFeatureMap_ViewFeatureEntries) {
    TestModel m;
    m.build();
    BasicFeatureMap fm;
    fm.add(m.attrA, std::any{std::string("e1")});
    fm.add(m.attrA, std::any{std::string("e2")});
    auto view = fm.entriesView(m.attrA);
    EXPECT_EQ(view.size(), 2);
    auto* e0 = view.get(0);
    EXPECT_TRUE(e0 != nullptr);
    EXPECT_EQ(std::any_cast<std::string>(e0->getValue()), std::string("e1"));
    auto* e1 = view.get(1);
    EXPECT_EQ(std::any_cast<std::string>(e1->getValue()), std::string("e2"));
}

// ===================================================================
// 17) IndexOf / lastIndexOf
// ===================================================================
EMF_TEST(BasicFeatureMap_IndexOf_LastIndexOf) {
    TestModel m;
    m.build();
    BasicFeatureMap fm;
    auto* e1 = new FeatureMap::Entry(m.attrA, std::any{std::string("v1")});
    auto* e2 = new FeatureMap::Entry(m.attrA, std::any{std::string("v2")});
    auto* e3 = new FeatureMap::Entry(m.attrB, std::any{1});
    fm.add(e1);
    fm.add(e2);
    fm.add(e3);
    EXPECT_EQ(fm.indexOf(e1), 0);
    EXPECT_EQ(fm.indexOf(e2), 1);
    EXPECT_EQ(fm.indexOf(e3), 2);
    EXPECT_EQ(fm.lastIndexOf(e1), 0);
    EXPECT_EQ(fm.lastIndexOf(e3), 2);
    // 不存在的 entry
    auto* eMissing = new FeatureMap::Entry(m.attrC, std::any{std::string("x")});
    EXPECT_EQ(fm.indexOf(eMissing), -1);
    delete eMissing;
}

// ===================================================================
// 18) AddWithAnyValue
// ===================================================================
EMF_TEST(BasicFeatureMap_AddWithAnyValue) {
    TestModel m;
    m.build();
    BasicFeatureMap fm;
    // add(std::any) —— Java 端无此重载，C++ 端简化为包装 entry
    EXPECT_TRUE(fm.add(std::any{std::string("wrapped")}));
    EXPECT_EQ((int)fm.size(), 1);
    auto arr = fm.toArray();
    EXPECT_EQ((int)arr.size(), 1);
}
