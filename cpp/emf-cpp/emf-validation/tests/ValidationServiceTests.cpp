// ValidationService 单元测试
#include "test_main.h"
#include "emf/validation/ValidationService.h"

EMF_TEST(ValidationService_Construct_HasValidator) {
    emf::validation::ValidationService svc;
    EXPECT_EQ(svc.getIncludeRoot(), true);
}

EMF_TEST(ValidationService_Validate_NullTarget) {
    emf::validation::ValidationService svc;
    auto diags = svc.validate(nullptr);
    EXPECT_EQ(diags.size(), 0u);
}

EMF_TEST(ValidationService_ValidateAll_NullRoot) {
    emf::validation::ValidationService svc;
    auto diags = svc.validateAll(nullptr);
    EXPECT_EQ(diags.size(), 0u);
}
