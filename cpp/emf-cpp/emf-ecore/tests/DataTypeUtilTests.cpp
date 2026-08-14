// DataTypeUtil 单元测试
#include "test_main.h"
#include "emf/ecore/EcoreMetadata.h"
#include "emf/ecore/EcorePackage.h"
#include "emf/ecore/EcoreImpls.h"

#include <any>
#include <cstdint>
#include <string>

using emf::ecore::DataTypeUtil;
using emf::ecore::EcoreFactory;
using emf::ecore::EcorePackage;

EMF_TEST(DataTypeUtil_EStringFromTo) {
    auto v = DataTypeUtil::fromString("EString", "hello");
    EXPECT_TRUE(v.has_value());
    EXPECT_EQ(std::any_cast<std::string>(v), std::string("hello"));
    EXPECT_EQ(DataTypeUtil::toString("EString", v), std::string("hello"));
}

EMF_TEST(DataTypeUtil_EIntFromTo) {
    auto v = DataTypeUtil::fromString("EInt", "123");
    EXPECT_TRUE(v.has_value());
    EXPECT_EQ(std::any_cast<int>(v), 123);
    EXPECT_EQ(DataTypeUtil::toString("EInt", v), std::string("123"));
}

EMF_TEST(DataTypeUtil_EBooleanFromTo) {
    auto vt = DataTypeUtil::fromString("EBoolean", "true");
    auto vf = DataTypeUtil::fromString("EBoolean", "false");
    EXPECT_EQ(std::any_cast<bool>(vt), true);
    EXPECT_EQ(std::any_cast<bool>(vf), false);
    EXPECT_EQ(DataTypeUtil::toString("EBoolean", vt), std::string("true"));
    EXPECT_EQ(DataTypeUtil::toString("EBoolean", vf), std::string("false"));
}

EMF_TEST(DataTypeUtil_EDoubleFromTo) {
    auto v = DataTypeUtil::fromString("EDouble", "3.14");
    EXPECT_TRUE(v.has_value());
    EXPECT_EQ(std::any_cast<double>(v), 3.14);
}

EMF_TEST(DataTypeUtil_DefaultValues) {
    auto s = DataTypeUtil::defaultValue("EString");
    EXPECT_EQ(std::any_cast<std::string>(s), std::string(""));
    auto i = DataTypeUtil::defaultValue("EInt");
    EXPECT_EQ(std::any_cast<int>(i), 0);
    auto b = DataTypeUtil::defaultValue("EBoolean");
    EXPECT_EQ(std::any_cast<bool>(b), false);
    auto d = DataTypeUtil::defaultValue("EDouble");
    EXPECT_EQ(std::any_cast<double>(d), 0.0);
    auto l = DataTypeUtil::defaultValue("ELong");
    EXPECT_TRUE(l.type() == typeid(int64_t));
    EXPECT_EQ(std::any_cast<int64_t>(l), (int64_t)0);
}

EMF_TEST(DataTypeUtil_Coerce) {
    auto v = DataTypeUtil::coerce(std::any{std::string("42")}, "EInt");
    EXPECT_EQ(std::any_cast<int>(v), 42);
    auto v2 = DataTypeUtil::coerce(std::any{(int)7}, "EString");
    EXPECT_EQ(std::any_cast<std::string>(v2), std::string("7"));
    auto v3 = DataTypeUtil::coerce(std::any{(int)1}, "EBoolean");
    EXPECT_EQ(std::any_cast<bool>(v3), true);
}

EMF_TEST(DataTypeUtil_NameOfEDataType) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* dt = EcorePackage::instance().getEDataType_EString();
    EXPECT_EQ(DataTypeUtil::nameOf(dt), std::string("EString"));
    EXPECT_EQ(DataTypeUtil::nameOf(nullptr), std::string(""));
}

EMF_TEST(DataTypeUtil_DefaultValueForClassifier) {
    EcoreFactory::initialize();
    EcorePackage::initialize();
    auto* dt = EcorePackage::instance().getEDataType_EInt();
    auto v = DataTypeUtil::defaultValueForClassifier(dt);
    EXPECT_EQ(std::any_cast<int>(v), 0);
}
