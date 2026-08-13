// TypeMapper: EDataType 名字 → C++ 类型名 + 字面量解析/转换
// 对齐 Java: org.eclipse.emf.codegen.ecore.genmodel.GenModel 里的 EDataType 映射
#pragma once

#include <string>

namespace emf::ecore::codegen {

class TypeMapper {
public:
    // 已知 EDataType 名（http://www.eclipse.org/emf/2002/Ecore 内置）→ C++ 类型
    // 返回值用作 getter/setter / 字段类型
    static std::string cppType(const std::string& eDataTypeName);

    // 模型驱动类型映射：从 EDataType 的 instanceClassName（Java 全限定类名）决定 C++ 类型。
    // 对齐 Java EMF：EAttribute 的运行时类型由 EDataType.instanceClassName 决定。
    //   - java.lang.String → std::string
    //   - java.lang.Boolean → bool
    //   - java.lang.Integer → int32_t
    //   - java.lang.Long → int64_t
    //   - java.lang.Short → int16_t
    //   - java.lang.Byte → int8_t
    //   - java.lang.Character → char
    //   - java.lang.Double → double
    //   - java.lang.Float → float
    //   - java.math.BigInteger / 其他 → std::string（无原生等价物）
    //   - 空（EEnum 或未指定）→ std::string（枚举存字面量名）
    static std::string cppTypeFromInstanceClass(const std::string& instanceClassName);

    // C++ 类型对应的 #include
    static std::string includeFor(const std::string& cppType);

    // 字面量默认值：把 EDataType 的 defaultValueLiteral 转成 C++ 表达式
    static std::string defaultValueLiteral(const std::string& eDataTypeName,
                                           const std::string& literal);

    // 字面量默认值（按 C++ 类型，模型驱动路径用）
    static std::string defaultValueLiteralByCppType(const std::string& cppType,
                                                     const std::string& literal);
};

}  // namespace emf::ecore::codegen
