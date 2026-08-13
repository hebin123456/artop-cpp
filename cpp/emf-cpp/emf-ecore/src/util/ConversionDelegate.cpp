// EMF Ecore: ConversionDelegate 实现
// 各具体 delegate 的 vtable 必须在 .cpp 中 emit，不能在 header inline
#include "emf/ecore/util/ConversionDelegate.h"

namespace emf::ecore::util {

// StringConversionDelegate
std::any StringConversionDelegate::createFromString(const std::string& literal) const { return std::any{literal}; }
std::string StringConversionDelegate::convertToString(const std::any& instance) const {
    if (!instance.has_value()) return "";
    if (instance.type() == typeid(std::string)) return std::any_cast<std::string>(instance);
    return "";
}

// BooleanConversionDelegate
std::any BooleanConversionDelegate::createFromString(const std::string& literal) const { return std::any{(literal == "true" || literal == "1")}; }
std::string BooleanConversionDelegate::convertToString(const std::any& instance) const {
    if (!instance.has_value()) return "false";
    if (instance.type() == typeid(bool)) return std::any_cast<bool>(instance) ? "true" : "false";
    return "false";
}

// IntegerConversionDelegate
std::any IntegerConversionDelegate::createFromString(const std::string& literal) const {
    try { return std::any{std::stoi(literal)}; } catch (...) { return std::any{0}; }
}
std::string IntegerConversionDelegate::convertToString(const std::any& instance) const {
    if (!instance.has_value()) return "0";
    if (instance.type() == typeid(int)) return std::to_string(std::any_cast<int>(instance));
    return "0";
}

// LongConversionDelegate
std::any LongConversionDelegate::createFromString(const std::string& literal) const {
    try { return std::any{static_cast<long>(std::stoll(literal))}; } catch (...) { return std::any{0L}; }
}
std::string LongConversionDelegate::convertToString(const std::any& instance) const {
    if (!instance.has_value()) return "0";
    if (instance.type() == typeid(long)) return std::to_string(std::any_cast<long>(instance));
    return "0";
}

// ShortConversionDelegate
std::any ShortConversionDelegate::createFromString(const std::string& literal) const {
    try { return std::any{static_cast<short>(std::stoi(literal))}; } catch (...) { return std::any{static_cast<short>(0)}; }
}
std::string ShortConversionDelegate::convertToString(const std::any& instance) const {
    if (!instance.has_value()) return "0";
    if (instance.type() == typeid(short)) return std::to_string(std::any_cast<short>(instance));
    return "0";
}

// ByteConversionDelegate
std::any ByteConversionDelegate::createFromString(const std::string& literal) const {
    try { return std::any{static_cast<char>(std::stoi(literal))}; } catch (...) { return std::any{static_cast<char>(0)}; }
}
std::string ByteConversionDelegate::convertToString(const std::any& instance) const {
    if (!instance.has_value()) return "0";
    if (instance.type() == typeid(char)) return std::to_string(static_cast<int>(std::any_cast<char>(instance)));
    return "0";
}

// FloatConversionDelegate
std::any FloatConversionDelegate::createFromString(const std::string& literal) const {
    try { return std::any{std::stof(literal)}; } catch (...) { return std::any{0.0f}; }
}
std::string FloatConversionDelegate::convertToString(const std::any& instance) const {
    if (!instance.has_value()) return "0";
    if (instance.type() == typeid(float)) return std::to_string(std::any_cast<float>(instance));
    return "0";
}

// DoubleConversionDelegate
std::any DoubleConversionDelegate::createFromString(const std::string& literal) const {
    try { return std::any{std::stod(literal)}; } catch (...) { return std::any{0.0}; }
}
std::string DoubleConversionDelegate::convertToString(const std::any& instance) const {
    if (!instance.has_value()) return "0";
    if (instance.type() == typeid(double)) return std::to_string(std::any_cast<double>(instance));
    return "0";
}

// DateConversionDelegate
std::any DateConversionDelegate::createFromString(const std::string& literal) const { return std::any{literal}; }
std::string DateConversionDelegate::convertToString(const std::any& instance) const {
    if (!instance.has_value()) return "";
    if (instance.type() == typeid(std::string)) return std::any_cast<std::string>(instance);
    return "";
}

// DateTimeConversionDelegate
std::any DateTimeConversionDelegate::createFromString(const std::string& literal) const { return std::any{literal}; }
std::string DateTimeConversionDelegate::convertToString(const std::any& instance) const {
    if (!instance.has_value()) return "";
    if (instance.type() == typeid(std::string)) return std::any_cast<std::string>(instance);
    return "";
}

// TimeConversionDelegate
std::any TimeConversionDelegate::createFromString(const std::string& literal) const { return std::any{literal}; }
std::string TimeConversionDelegate::convertToString(const std::any& instance) const {
    if (!instance.has_value()) return "";
    if (instance.type() == typeid(std::string)) return std::any_cast<std::string>(instance);
    return "";
}

}  // namespace emf::ecore::util
