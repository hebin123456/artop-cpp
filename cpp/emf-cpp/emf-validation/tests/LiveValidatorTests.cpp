// LiveValidator 单元测试
#include "test_main.h"
#include "emf/validation/LiveValidator.h"
#include "emf/validation/EValidator.h"

EMF_TEST(LiveValidator_Construct) {
    emf::validation::EValidator v;
    emf::validation::ValidationLiveAdapter live(v);
    EXPECT_TRUE(live.isEnabled());
}

EMF_TEST(LiveValidator_SetEnabled) {
    emf::validation::EValidator v;
    emf::validation::ValidationLiveAdapter live(v);
    live.setEnabled(false);
    EXPECT_FALSE(live.isEnabled());
}

EMF_TEST(LiveValidator_ValidateNow_NullTarget) {
    emf::validation::EValidator v;
    emf::validation::ValidationLiveAdapter live(v);
    auto diags = live.validateNow(nullptr);
    EXPECT_EQ(diags.size(), 0u);
}
