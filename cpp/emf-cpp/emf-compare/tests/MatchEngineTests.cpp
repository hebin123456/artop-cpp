// MatchEngine 单元测试
#include "test_main.h"
#include "emf/compare/Comparison.h"
#include "emf/compare/MatchEngine.h"

EMF_TEST(MatchEngine_Threshold_Defaults) {
    emf::compare::MatchEngine me;
    EXPECT_EQ(me.getSimilarityThreshold(), 1.0);
    EXPECT_FALSE(me.getUseIdentifierMatcher());
}

EMF_TEST(MatchEngine_SetThreshold) {
    emf::compare::MatchEngine me;
    me.setSimilarityThreshold(0.5);
    EXPECT_EQ(me.getSimilarityThreshold(), 0.5);
}

EMF_TEST(MatchEngine_BothNull_ReturnsNoMatch) {
    emf::compare::Comparison comp;
    emf::compare::MatchEngine me;
    me.match(nullptr, nullptr, comp);
    EXPECT_EQ(comp.getMatches().size(), 0u);
}
