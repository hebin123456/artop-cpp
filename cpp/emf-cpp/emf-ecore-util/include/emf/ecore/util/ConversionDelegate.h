// ConversionDelegate.h
// 对齐 Java: org.eclipse.emf.ecore.EDataType.Internal.ConversionDelegate
//
// ConversionDelegate 是 EDataType 的字符串 ↔ 对象转换委托。
// Java 端通过 EAnnotation "http://www.eclipse.org/emf/2002/Ecore/ConversionDelegate"
// 在 EPackage 上注册 ConversionDelegate.Factory；C++ 端把这个接口 1:1 翻译为抽象类。
//
// 与 emf/ecore/util/EcoreUtil.h 中 createFromString / convertToString 的区别：
//   - EcoreUtil 的静态方法按 EDataType 名称走内置类型分发（int/string/bool/...）；
//   - ConversionDelegate 允许模型方注册自定义转换逻辑（例如复杂用户类型），
//     由 EDataTypeImpl::getConversionDelegate() 持有并在反射路径上调用。
//
// 方法签名（对齐 task spec）：
//   - convertToString(EObject* owner, EDataType* eDataType, std::any value)
//   - createFromString(EObject* owner, EDataType* eDataType, std::string literal)
// owner / eDataType 透传给实现，便于在同一段代码里处理多种 EDataType。
#pragma once

#include "emf/common/EObject.h"
#include <any>
#include <string>

namespace emf::ecore {
class EDataType;
}  // namespace emf::ecore

namespace emf::ecore::util {

// ConversionDelegate：自定义 EDataType 的字符串 ↔ 值转换器
// 对齐 Java org.eclipse.emf.ecore.EDataType.Internal.ConversionDelegate
class ConversionDelegate {
public:
    virtual ~ConversionDelegate() = default;

    // 把值转为字符串字面量（对齐 Java ConversionDelegate.convertToString）
    // owner   : 持有该值的 EObject（可空，用于上下文相关转换）
    // eDataType: 该值所属的 EDataType（非空）
    // value   : 待转换的值（可能为空 any）
    virtual std::string convertToString(emf::common::EObject* owner,
                                        emf::ecore::EDataType* eDataType,
                                        std::any value) = 0;

    // 把字符串字面量转为该 EDataType 的值（对齐 Java ConversionDelegate.createFromString）
    // owner    : 持有该值的 EObject（可空）
    // eDataType : 目标 EDataType（非空）
    // literal  : 字符串字面量
    virtual std::any createFromString(emf::common::EObject* owner,
                                      emf::ecore::EDataType* eDataType,
                                      std::string literal) = 0;

    // ===== Factory（对齐 Java ConversionDelegate.Factory）=====
    // 按 EDataType 创建对应的 ConversionDelegate。模型方注册 Factory 到
    // Factory.Registry，运行时按 EAnnotation 解析。
    class Factory {
    public:
        virtual ~Factory() = default;
        virtual ConversionDelegate* createConversionDelegate(emf::ecore::EDataType* eDataType) = 0;

        // Descriptor：Factory 的包装（对齐 Java Factory.Descriptor）
        // 用于延迟实例化（注册时只持有 descriptor，首次 getFactory 时才实例化）
        class Descriptor {
        public:
            virtual ~Descriptor() = default;
            virtual Factory* getFactory() = 0;
        };

        // Registry：URI -> Factory 映射（对齐 Java Factory.Registry）
        class Registry {
        public:
            static Registry& instance() {
                static Registry r;
                return r;
            }
            virtual Factory* getFactory(const std::string& uri) { (void)uri; return nullptr; }
            virtual void put(const std::string& uri, Factory* factory) { (void)uri; (void)factory; }
            virtual void put(const std::string& uri, Descriptor* descriptor) { (void)uri; (void)descriptor; }
        };
    };
};

}  // namespace emf::ecore::util
