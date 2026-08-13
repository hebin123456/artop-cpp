// ConversionDelegate 实现
// 对齐 Java: org.eclipse.emf.ecore.util.ConversionDelegate
//
// ConversionDelegate 用于自定义 EDataType 的字符串<->值转换。
// Java 端通过 EAnnotation "http://www.eclipse.org/emf/2002/Ecore/ConversionDelegate"
// 注册 ConversionFactory。C++ 端简化为：通过 EcoreUtil.createFromString/convertToString 处理。
#include "emf/ecore/util/ConversionDelegate.h"
#include "emf/ecore/util/EcoreUtil.h"

namespace emf::ecore::util {

using emf::ecore::EDataType;

// ConversionDelegate 接口：自定义类型转换器
// 对齐 Java org.eclipse.emf.ecore.util.ConversionDelegate
class ConversionDelegateImpl {
public:
    explicit ConversionDelegateImpl(EDataType* eDataType) : eDataType_(eDataType) {}

    std::any convertFromString(const std::string& literal) {
        return EcoreUtil::createFromString(eDataType_, literal);
    }

    std::string convertToString(const std::any& value) {
        return EcoreUtil::convertToString(eDataType_, value);
    }

private:
    EDataType* eDataType_;
};

}  // namespace emf::ecore::util
