// 测试主入口
#include "test_main.h"
#include <cstdlib>

// 由各测试文件通过 EMF_TEST 宏注册
EMF_TEST(Placeholder) {
    EXPECT_TRUE(true);
}

int main() {
    return RUN_ALL_TESTS();
}
