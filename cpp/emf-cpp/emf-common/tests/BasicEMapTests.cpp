// BasicEMap 单元测试
// 对齐 Java: org.eclipse.emf.common.util.BasicEMap
#include "test_main.h"
#include "emf/common/util/EMap.h"
#include "emf/common/util/BasicEMap.h"

#include <string>

using emf::common::util::EMap;
using emf::common::util::BasicEMap;
using emf::common::util::MapEntry;

EMF_TEST(BasicEMap_PutGet) {
    BasicEMap<std::string, int> m;
    m.put("a", 1);
    m.put("b", 2);
    EXPECT_EQ(m.get("a"), 1);
    EXPECT_EQ(m.get("b"), 2);
    EXPECT_EQ(m.size(), static_cast<size_t>(2));
}

EMF_TEST(BasicEMap_UpdateExistingKey) {
    BasicEMap<std::string, int> m;
    m.put("a", 1);
    int oldVal = m.put("a", 99);
    EXPECT_EQ(oldVal, 1);
    EXPECT_EQ(m.get("a"), 99);
    EXPECT_EQ(m.size(), static_cast<size_t>(1));
}

EMF_TEST(BasicEMap_ContainsKeyValue) {
    BasicEMap<int, std::string> m;
    m.put(1, "one");
    m.put(2, "two");
    EXPECT_TRUE(m.containsKey(1));
    EXPECT_FALSE(m.containsKey(3));
    EXPECT_TRUE(m.containsValue("two"));
    EXPECT_FALSE(m.containsValue("three"));
}

EMF_TEST(BasicEMap_RemoveKey) {
    BasicEMap<std::string, int> m;
    m.put("a", 1);
    m.put("b", 2);
    m.put("c", 3);
    int oldVal = m.removeKey("b");
    EXPECT_EQ(oldVal, 2);
    EXPECT_EQ(m.size(), static_cast<size_t>(2));
    EXPECT_FALSE(m.containsKey("b"));
}

EMF_TEST(BasicEMap_IndexOfKey) {
    BasicEMap<std::string, int> m;
    m.put("a", 1);
    m.put("b", 2);
    m.put("c", 3);
    EXPECT_EQ(m.indexOfKey("a"), 0);
    EXPECT_EQ(m.indexOfKey("b"), 1);
    EXPECT_EQ(m.indexOfKey("c"), 2);
    EXPECT_EQ(m.indexOfKey("z"), -1);
}

EMF_TEST(BasicEMap_Clear) {
    BasicEMap<std::string, int> m;
    m.put("a", 1);
    m.put("b", 2);
    m.clear();
    EXPECT_EQ(m.size(), static_cast<size_t>(0));
    EXPECT_FALSE(m.containsKey("a"));
}

EMF_TEST(BasicEMap_ForEachKeyValue) {
    BasicEMap<int, std::string> m;
    m.put(1, "one");
    m.put(2, "two");
    m.put(3, "three");

    int keySum = 0;
    m.forEachKey([&](const int& k) { keySum += k; });
    EXPECT_EQ(keySum, 6);

    std::string allValues;
    m.forEachValue([&](const std::string& v) { allValues += v; });
    EXPECT_EQ(allValues, std::string("onetwothree"));
}

EMF_TEST(BasicEMap_Iteration) {
    BasicEMap<std::string, int> m;
    m.put("a", 1);
    m.put("b", 2);

    int count = 0;
    for (auto* e : m) {
        EXPECT_TRUE(e->key == "a" || e->key == "b");
        ++count;
    }
    EXPECT_EQ(count, 2);
}
