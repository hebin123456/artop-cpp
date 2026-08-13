// ConstraintDescriptor 单元测试
#include "test_main.h"
#include "emf/validation/ConstraintDescriptor.h"

EMF_TEST(ConstraintDescriptor_Defaults) {
    emf::validation::ConstraintDescriptor d;
    EXPECT_TRUE(d.getSeverity() == emf::validation::Severity::WARNING);
    EXPECT_TRUE(d.getMode() == emf::validation::ConstraintMode::BATCH);
    EXPECT_EQ(d.getCode(), 0);
}

EMF_TEST(ConstraintDescriptor_Setters) {
    emf::validation::ConstraintDescriptor d;
    d.setId("c1");
    d.setName("Name");
    d.setMessage("msg");
    d.setSeverity(emf::validation::Severity::ERROR);
    EXPECT_EQ(d.getId(), "c1");
    EXPECT_EQ(d.getName(), "Name");
    EXPECT_EQ(d.getMessage(), "msg");
    EXPECT_TRUE(d.getSeverity() == emf::validation::Severity::ERROR);
}

EMF_TEST(ConstraintDescriptor_ParseDescriptors) {
    std::string xml =
        "<constraints>"
        "<constraint id=\"a\" name=\"A\" message=\"ma\" severity=\"error\"/>"
        "<constraint id=\"b\" name=\"B\" message=\"mb\" severity=\"warning\"/>"
        "</constraints>";
    auto v = emf::validation::ConstraintDescriptorParser::parseDescriptors(xml);
    EXPECT_EQ(v.size(), 2u);
    EXPECT_EQ(v[0].getId(), "a");
    EXPECT_EQ(v[1].getId(), "b");
}
