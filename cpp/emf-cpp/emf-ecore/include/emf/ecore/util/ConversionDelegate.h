// ConversionDelegate.h
// 对齐 Java: org.eclipse.emf.ecore.util.ConversionDelegate
// 1:1 的最小翻译 —— EDataType 在 createFromString / convertToString
// 时的字面值 ↔ 对象转换委托。
//
// C++ 端当前最小可用实现：每个具体子类仅返回 literal 自身，复杂解析留给上层
// （Java 端对应的子类即 XML 序列化/反序列化路径的一部分）。本头文件存在是为了
// 让 EcorePackage.cpp 编译通过、emf_ecore_util 链接通过。
#pragma once

#include "emf/ecore/EcorePackage.h"
#include <string>
#include <any>

namespace emf::ecore::util {

class ConversionDelegate {
public:
    virtual ~ConversionDelegate() = default;
    virtual std::any createFromString(const std::string& literal) const = 0;
    virtual std::string convertToString(const std::any& instance) const = 0;
};

// 各具体 delegate 的声明（实现在 ConversionDelegate.cpp 中）
class StringConversionDelegate : public ConversionDelegate {
public:
    std::any createFromString(const std::string& literal) const override;
    std::string convertToString(const std::any& instance) const override;
};
class BooleanConversionDelegate : public ConversionDelegate {
public:
    std::any createFromString(const std::string& literal) const override;
    std::string convertToString(const std::any& instance) const override;
};
class IntegerConversionDelegate : public ConversionDelegate {
public:
    std::any createFromString(const std::string& literal) const override;
    std::string convertToString(const std::any& instance) const override;
};
class LongConversionDelegate : public ConversionDelegate {
public:
    std::any createFromString(const std::string& literal) const override;
    std::string convertToString(const std::any& instance) const override;
};
class ShortConversionDelegate : public ConversionDelegate {
public:
    std::any createFromString(const std::string& literal) const override;
    std::string convertToString(const std::any& instance) const override;
};
class ByteConversionDelegate : public ConversionDelegate {
public:
    std::any createFromString(const std::string& literal) const override;
    std::string convertToString(const std::any& instance) const override;
};
class FloatConversionDelegate : public ConversionDelegate {
public:
    std::any createFromString(const std::string& literal) const override;
    std::string convertToString(const std::any& instance) const override;
};
class DoubleConversionDelegate : public ConversionDelegate {
public:
    std::any createFromString(const std::string& literal) const override;
    std::string convertToString(const std::any& instance) const override;
};
class DateConversionDelegate : public ConversionDelegate {
public:
    std::any createFromString(const std::string& literal) const override;
    std::string convertToString(const std::any& instance) const override;
};
class DateTimeConversionDelegate : public ConversionDelegate {
public:
    std::any createFromString(const std::string& literal) const override;
    std::string convertToString(const std::any& instance) const override;
};
class TimeConversionDelegate : public ConversionDelegate {
public:
    std::any createFromString(const std::string& literal) const override;
    std::string convertToString(const std::any& instance) const override;
};

// BuiltinConversionDelegateFactory: 按 EDataType 名称推断一个默认的 ConversionDelegate
// 对齐 Java 端 EDataType 缺省的字面量 ↔ 对象转换逻辑
class BuiltinConversionDelegateFactory {
public:
    static ConversionDelegate* createForName(const std::string& name) {
        if (name == "EBoolean" || name == "EBooleanObject") return new BooleanConversionDelegate();
        if (name == "EInteger" || name == "EIntegerObject") return new IntegerConversionDelegate();
        if (name == "ELong" || name == "ELongObject") return new LongConversionDelegate();
        if (name == "EShort" || name == "EShortObject") return new ShortConversionDelegate();
        if (name == "EByte" || name == "EByteObject") return new ByteConversionDelegate();
        if (name == "EFloat" || name == "EFloatObject") return new FloatConversionDelegate();
        if (name == "EDouble" || name == "EDoubleObject") return new DoubleConversionDelegate();
        if (name == "EDate") return new DateConversionDelegate();
        if (name == "EDateTime") return new DateTimeConversionDelegate();
        if (name == "ETime") return new TimeConversionDelegate();
        // 默认 fallback：按字符串处理
        return new StringConversionDelegate();
    }
};

}  // namespace emf::ecore::util
