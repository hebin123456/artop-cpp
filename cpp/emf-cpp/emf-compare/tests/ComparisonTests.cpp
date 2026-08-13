// Comparison 单元测试
#include "test_main.h"
#include "emf/compare/Comparison.h"
#include "emf/compare/Diff.h"

EMF_TEST(Comparison_AddMatch) {
    emf::compare::Comparison comp;
    auto& m = comp.addMatch(nullptr, nullptr, emf::compare::MatchKind::IDENTICAL, 1.0);
    EXPECT_EQ(comp.getMatches().size(), 1u);
    EXPECT_TRUE(m.getKind() == emf::compare::MatchKind::IDENTICAL);
    EXPECT_EQ(m.getSimilarity(), 1.0);
}

EMF_TEST(Comparison_Differences_Empty) {
    emf::compare::Comparison comp;
    EXPECT_EQ(comp.getDifferences().size(), 0u);
}

EMF_TEST(Comparison_Clear) {
    emf::compare::Comparison comp;
    comp.addMatch(nullptr, nullptr, emf::compare::MatchKind::IDENTICAL, 1.0);
    comp.clear();
    EXPECT_EQ(comp.getMatches().size(), 0u);
}
