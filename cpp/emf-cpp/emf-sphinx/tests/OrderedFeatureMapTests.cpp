// OrderedFeatureMapTests.cpp
// 测试 OrderedFeatureMap：按 featureID 升序插入、按 feature 过滤、clear/size。
// 对齐 Java org.eclipse.sphinx.emf.ecore.OrderedFeatureMap 行为
#include "test_main.h"
#include "emf/sphinx/ecore/OrderedFeatureMap.h"
#include "emf/ecore/EcoreImpls.h"

using emf::sphinx::ecore::OrderedFeatureMap;
using emf::sphinx::ecore::FeatureMapEntry;
using emf::ecore::EStructuralFeatureImpl;

namespace {

// 构造一个带指定 featureID 的 EStructuralFeature（仅用于排序测试）
EStructuralFeatureImpl* makeFeature(int id) {
    auto* f = new EStructuralFeatureImpl();
    f->setFeatureID(id);
    return f;
}

}  // namespace

// 测试 1：初始为空
EMF_TEST(OrderedFeatureMap_InitiallyEmpty) {
    OrderedFeatureMap m;
    EXPECT_TRUE(m.empty());
    EXPECT_EQ((int)m.size(), 0);
}

// 测试 2：乱序插入后按 featureID 升序排列
EMF_TEST(OrderedFeatureMap_AddOrderedByFeatureID) {
    OrderedFeatureMap m;
    auto* f3 = makeFeature(3);
    auto* f1 = makeFeature(1);
    auto* f2 = makeFeature(2);
    m.add(f3, nullptr, 0);
    m.add(f1, nullptr, 0);
    m.add(f2, nullptr, 0);
    EXPECT_EQ((int)m.size(), 3);
    const auto& e = m.entries();
    EXPECT_EQ(e[0].feature->getFeatureID(), 1);
    EXPECT_EQ(e[1].feature->getFeatureID(), 2);
    EXPECT_EQ(e[2].feature->getFeatureID(), 3);
    delete f3;
    delete f1;
    delete f2;
}

// 测试 3：相同 featureID 时按 index 升序
EMF_TEST(OrderedFeatureMap_SameFeatureIDOrderedByIndex) {
    OrderedFeatureMap m;
    auto* f = makeFeature(5);
    m.add(f, nullptr, 2);
    m.add(f, nullptr, 0);
    m.add(f, nullptr, 1);
    const auto& e = m.entries();
    EXPECT_EQ((int)m.size(), 3);
    EXPECT_EQ(e[0].index, 0);
    EXPECT_EQ(e[1].index, 1);
    EXPECT_EQ(e[2].index, 2);
    delete f;
}

// 测试 4：按 feature 过滤
EMF_TEST(OrderedFeatureMap_GetByFeature) {
    OrderedFeatureMap m;
    auto* f1 = makeFeature(1);
    auto* f2 = makeFeature(2);
    m.add(f1, nullptr, 0);
    m.add(f2, nullptr, 0);
    m.add(f1, nullptr, 1);
    m.add(f2, nullptr, 1);
    auto r1 = m.get(f1);
    auto r2 = m.get(f2);
    EXPECT_EQ((int)r1.size(), 2);
    EXPECT_EQ((int)r2.size(), 2);
    EXPECT_TRUE(r1[0].feature == f1);
    EXPECT_TRUE(r2[0].feature == f2);
    delete f1;
    delete f2;
}

// 测试 5：clear 清空
EMF_TEST(OrderedFeatureMap_Clear) {
    OrderedFeatureMap m;
    auto* f = makeFeature(1);
    m.add(f, nullptr, 0);
    m.add(f, nullptr, 1);
    EXPECT_EQ((int)m.size(), 2);
    m.clear();
    EXPECT_TRUE(m.empty());
    EXPECT_EQ((int)m.size(), 0);
    delete f;
}

// 测试 6：defaultOrder 按 featureID（feature 为 null 时返回 -1）
EMF_TEST(OrderedFeatureMap_DefaultOrder) {
    FeatureMapEntry e;
    EXPECT_EQ(OrderedFeatureMap::defaultOrder(e), -1);  // feature == nullptr
    auto* f = makeFeature(42);
    e.feature = f;
    EXPECT_EQ(OrderedFeatureMap::defaultOrder(e), 42);
    delete f;
}

// 测试 7：插入后 size 递增
EMF_TEST(OrderedFeatureMap_SizeGrows) {
    OrderedFeatureMap m;
    auto* f1 = makeFeature(10);
    auto* f2 = makeFeature(20);
    EXPECT_EQ((int)m.size(), 0);
    m.add(f1, nullptr, 0);
    EXPECT_EQ((int)m.size(), 1);
    m.add(f2, nullptr, 0);
    EXPECT_EQ((int)m.size(), 2);
    delete f1;
    delete f2;
}
