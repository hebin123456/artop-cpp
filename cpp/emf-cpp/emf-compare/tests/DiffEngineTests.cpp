// DiffEngine 单元测试
#include "test_main.h"
#include "emf/compare/Comparison.h"
#include "emf/compare/DiffEngine.h"
#include "emf/compare/Diff.h"

EMF_TEST(DiffEngine_NoDiff_OnIdentical) {
    emf::compare::Comparison comp;
    comp.addMatch(nullptr, nullptr, emf::compare::MatchKind::IDENTICAL, 1.0);
    emf::compare::DiffEngine de;
    de.diff(comp);
    EXPECT_EQ(comp.getDifferences().size(), 0u);
}

EMF_TEST(DiffEngine_ProducesDiff_OnDifferent) {
    emf::compare::Comparison comp;
    comp.addMatch(nullptr, nullptr, emf::compare::MatchKind::DIFFERENT, 0.0);
    emf::compare::DiffEngine de;
    de.diff(comp);
    EXPECT_EQ(comp.getDifferences().size(), 1u);
    EXPECT_TRUE(comp.getDifferences()[0]->getKind() == emf::compare::DiffKind::CHANGE);
}
