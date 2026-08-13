// MergeEngine 单元测试
#include "test_main.h"
#include "emf/compare/Comparison.h"
#include "emf/compare/MergeEngine.h"

EMF_TEST(MergeEngine_NullTarget_ReturnsFalse) {
    emf::compare::Comparison comp;
    emf::compare::MergeEngine me;
    EXPECT_FALSE(me.merge(comp, nullptr));
}
