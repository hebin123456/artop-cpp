// EValidator 单元测试
#include "test_main.h"
#include "emf/validation/EValidator.h"
#include "emf/validation/Constraint.h"

EMF_TEST(EValidator_RegisterAndGet) {
    emf::validation::EValidator v;
    v.registerConstraint([](emf::common::EObject*) { return true; },
                         "id1", "Name1", "msg", emf::validation::Severity::WARNING);
    EXPECT_NOT_NULL(v.getConstraint("id1"));
    EXPECT_EQ(v.getConstraints().size(), 1u);
}

EMF_TEST(EValidator_Unregister) {
    emf::validation::EValidator v;
    v.registerConstraint([](emf::common::EObject*) { return true; },
                         "id1", "Name1", "msg", emf::validation::Severity::WARNING);
    EXPECT_TRUE(v.unregisterConstraint("id1"));
    EXPECT_NULL(v.getConstraint("id1"));
    EXPECT_FALSE(v.unregisterConstraint("missing"));
}

EMF_TEST(EValidator_DefaultConstraints_Registered) {
    emf::validation::EValidator v;
    v.registerDefaultConstraints();
    EXPECT_NOT_NULL(v.getConstraint("emf.validation.default.no_empty_name"));
    EXPECT_NOT_NULL(v.getConstraint("emf.validation.default.no_null_required_ref"));
}

EMF_TEST(EValidator_Validate_NullTarget_ReturnsEmpty) {
    emf::validation::EValidator v;
    auto diags = v.validate(nullptr);
    EXPECT_EQ(diags.size(), 0u);
}
