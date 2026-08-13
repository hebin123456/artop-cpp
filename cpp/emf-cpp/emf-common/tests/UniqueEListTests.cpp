// UniqueEListTests.cpp
// 对齐 Java org.eclipse.emf.common.util.UniqueEList 的单元测试
#include "test_main.h"
#include "emf/common/util/UniqueEList.h"
#include "emf/common/util/BasicEList.h"

#include <string>
#include <vector>

using emf::common::util::UniqueEList;
using emf::common::util::BasicEList;

EMF_TEST(UniqueEList_AddUnique) {
    UniqueEList<int> list;
    EXPECT_TRUE(list.add(1));
    EXPECT_TRUE(list.add(2));
    EXPECT_TRUE(list.add(3));
    EXPECT_FALSE(list.add(2));   // 重复应被拒绝
    EXPECT_FALSE(list.add(1));
    EXPECT_EQ(list.size(), 3);
}

EMF_TEST(UniqueEList_AddAllDropsDuplicates) {
    UniqueEList<int> list;
    std::vector<int> data = {1, 2, 2, 3, 1, 4, 4, 4};
    list.addAll(data);
    EXPECT_EQ(list.size(), 4);
    EXPECT_TRUE(list.contains(1));
    EXPECT_TRUE(list.contains(2));
    EXPECT_TRUE(list.contains(3));
    EXPECT_TRUE(list.contains(4));
}

EMF_TEST(UniqueEList_SetDuplicateThrows) {
    UniqueEList<int> list;
    list.add(1);
    list.add(2);
    bool threw = false;
    try {
        list.set(1, 1);  // 把 index 1 替换为已存在的 1
    } catch (const std::invalid_argument&) {
        threw = true;
    }
    EXPECT_TRUE(threw);
}

EMF_TEST(UniqueEList_AddAtIndexDuplicateThrows) {
    UniqueEList<int> list;
    list.add(10);
    list.add(20);
    bool threw = false;
    try {
        list.add(1, 20);
    } catch (const std::invalid_argument&) {
        threw = true;
    }
    EXPECT_TRUE(threw);
}

EMF_TEST(UniqueEList_ContainsAndIndexOf) {
    UniqueEList<std::string> list;
    std::string a = "alpha", b = "beta", c = "gamma", miss = "delta";
    list.add(a);
    list.add(b);
    list.add(c);
    EXPECT_TRUE(list.contains(b));
    EXPECT_FALSE(list.contains(miss));
    EXPECT_EQ(list.indexOf(a), 0);
    EXPECT_EQ(list.indexOf(c), 2);
    EXPECT_EQ(list.indexOf(miss), -1);
}

EMF_TEST(UniqueEList_ConstructorFromCollection) {
    std::vector<int> data = {1, 2, 2, 3, 1, 4};
    UniqueEList<int> list(data);
    EXPECT_EQ(list.size(), 4);
}

EMF_TEST(UniqueEList_ConstructorWithCapacity) {
    UniqueEList<int> list(100);
    EXPECT_EQ(list.size(), 0);
    for (int i = 0; i < 50; ++i) list.add(i);
    EXPECT_EQ(list.size(), 50);
}

EMF_TEST(UniqueEList_RemoveAndClear) {
    UniqueEList<int> list;
    list.add(1);
    list.add(2);
    list.add(3);
    EXPECT_TRUE(list.remove(1));
    EXPECT_EQ(list.size(), 2);
    EXPECT_FALSE(list.contains(2));
    list.clear();
    EXPECT_EQ(list.size(), 0);
    EXPECT_TRUE(list.isEmpty());
}

EMF_TEST(UniqueEList_Move) {
    UniqueEList<int> list;
    list.add(10);
    list.add(20);
    list.add(30);
    list.add(40);
    int moved = list.move(0, 2);  // 30 -> index 0
    EXPECT_EQ(moved, 30);
    EXPECT_EQ(list.get(0), 30);
    EXPECT_EQ(list.get(1), 10);
    EXPECT_EQ(list.get(2), 20);
    EXPECT_EQ(list.get(3), 40);
}

EMF_TEST(UniqueEList_FastCompareIdentityOnly) {
    emf::common::util::FastCompareUniqueEList<int> list;
    int a = 42, b = 42;
    list.add(a);
    // FastCompare: useEquals() = false，所以 == 比较（指针地址）—— 不同指针对象视为不同
    EXPECT_FALSE(list.add(b));
    EXPECT_EQ(list.size(), 1);
    list.add(100);
    EXPECT_EQ(list.size(), 2);
}

EMF_TEST(UniqueEList_IsBasicEListSubclass) {
    UniqueEList<int> list;
    static_assert(std::is_base_of<BasicEList<int>, UniqueEList<int>>::value,
                  "UniqueEList must inherit from BasicEList");
    BasicEList<int>* base = &list;
    EXPECT_EQ(base->size(), 0);
}
