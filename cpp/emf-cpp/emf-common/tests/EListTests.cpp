// EList 单元测试
#include "test_main.h"
#include "emf/common/EList.h"

#include <string>
#include <vector>

using emf::common::EList;

EMF_TEST(EList_AddGetBasic) {
    EList<int> list;
    list.add(1);
    list.add(2);
    list.add(3);
    EXPECT_EQ(list.size(), (size_t)3);
    EXPECT_EQ(list.get(0), 1);
    EXPECT_EQ(list.get(1), 2);
    EXPECT_EQ(list.get(2), 3);
}

EMF_TEST(EList_OperatorBracket) {
    EList<int> list;
    list.add(10);
    list.add(20);
    EXPECT_EQ(list[0], 10);
    EXPECT_EQ(list[1], 20);
    list[1] = 99;
    EXPECT_EQ(list.get(1), 99);
}

EMF_TEST(EList_RemoveByIndex) {
    EList<long> list;
    list.add(1); list.add(2); list.add(3);
    int idx = 1;
    long removed = list.remove(idx);
    EXPECT_EQ(removed, 2L);
    EXPECT_EQ(list.size(), (size_t)2);
    EXPECT_EQ(list.get(0), 1L);
    EXPECT_EQ(list.get(1), 3L);
    int outOfRange = 5;
    EXPECT_THROWS(list.removeByIndex(outOfRange));
}

EMF_TEST(EList_RemoveByValue) {
    EList<long> list;
    list.add(10); list.add(20); list.add(30);
    long v1 = 20, v2 = 99;
    bool ok = list.remove(v1);
    EXPECT_TRUE(ok);
    EXPECT_EQ(list.size(), (size_t)2);
    bool notFound = list.remove(v2);
    EXPECT_FALSE(notFound);
}

EMF_TEST(EList_Set) {
    EList<long> list;
    list.add(1); list.add(2);
    int idx = 0;
    long newV = 42;
    long old = list.set(idx, newV);
    EXPECT_EQ(old, 1L);
    EXPECT_EQ(list.get(0), 42L);
    int outOfRange = 99;
    EXPECT_THROWS(list.set(outOfRange, 0L));
}

EMF_TEST(EList_ContainsAndIndexOf) {
    EList<std::string> list;
    std::string a = "a", b = "b", c = "c", z = "z", miss = "not-there";
    list.add(a); list.add(b); list.add(c);
    EXPECT_TRUE(list.contains(b));
    EXPECT_FALSE(list.contains(z));
    EXPECT_EQ(list.indexOf(a), 0);
    EXPECT_EQ(list.indexOf(c), 2);
    EXPECT_EQ(list.indexOf(miss), -1);
}

EMF_TEST(EList_AddUnique) {
    EList<int> list;
    int v1 = 1, v2 = 2;
    list.addUnique(v1);
    list.addUnique(v2);
    list.addUnique(v1);  // 重复
    list.addUnique(v2);  // 重复
    EXPECT_EQ(list.size(), (size_t)2);
    EXPECT_TRUE(list.contains(v1));
    EXPECT_TRUE(list.contains(v2));
}

EMF_TEST(EList_NotifierCallbacks) {
    // 新回调签名：单个函数指针 + void* context 处理 add/remove/set 三种事件。
    // 用 captureless lambda 转函数指针，context 持有计数器。
    struct Counters {
        int addCount = 0, removeCount = 0, setCount = 0;
        int lastAddPos = -1, lastRemovePos = -1, lastSetPos = -1;
    };
    Counters cnt;
    auto cb = [](void* ctx, const emf::ecore::EStructuralFeature* /*feat*/,
                 emf::common::EListEvent ev, int pos, long /*oldV*/, long /*newV*/) {
        auto* c = static_cast<Counters*>(ctx);
        switch (ev) {
            case emf::common::EListEvent::Add:    c->addCount++;    c->lastAddPos = pos;    break;
            case emf::common::EListEvent::Remove: c->removeCount++; c->lastRemovePos = pos; break;
            case emf::common::EListEvent::Set:    c->setCount++;    c->lastSetPos = pos;    break;
        }
    };
    EList<long> list(cb, &cnt, nullptr);
    list.add(1);
    list.add(2);
    list.add(3);
    EXPECT_EQ(cnt.addCount, 3);
    EXPECT_EQ(cnt.lastAddPos, 2);
    int sIdx = 1;
    long sVal = 99;
    list.set(sIdx, sVal);
    EXPECT_EQ(cnt.setCount, 1);
    EXPECT_EQ(cnt.lastSetPos, 1);
    int rIdx = 0;
    list.removeByIndex(rIdx);
    EXPECT_EQ(cnt.removeCount, 1);
    EXPECT_EQ(cnt.lastRemovePos, 0);
}

EMF_TEST(EList_ClearEmptiesList) {
    EList<int> list;
    for (int i = 0; i < 5; ++i) list.add(i);
    EXPECT_EQ(list.size(), (size_t)5);
    list.clear();
    EXPECT_EQ(list.size(), (size_t)0);
    EXPECT_TRUE(list.empty());
}

EMF_TEST(EList_Iteration) {
    EList<int> list;
    list.add(10); list.add(20); list.add(30);
    int sum = 0;
    for (auto v : list) sum += v;
    EXPECT_EQ(sum, 60);
}

EMF_TEST(EList_OutOfRangeThrows) {
    EList<int> list;
    list.add(1);
    int outOfRange = 99;
    EXPECT_THROWS(list.get(outOfRange));
}
