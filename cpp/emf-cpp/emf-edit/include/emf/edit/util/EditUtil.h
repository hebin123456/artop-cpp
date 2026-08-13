// EditUtil.h
// 对齐 Java: org.eclipse.emf.edit.util.EditUtil
// 状态: 框架骨架（仅声明，未实现）
#pragma once

#include "emf/common/EObject.h"

#include <any>
#include <string>
#include <vector>

namespace emf::edit::util {

// Java 端 EMF Edit 静态工具集。
// 注：Java 端 EditUtil 与 emf::ecore::util::EcoreUtil 大部分功能重叠，
// EditUtil 是 Edit 框架上下文下的快捷封装。
class EditUtil {
public:
    // 名字解析（Java: getText, getString）
    static std::string getText(std::any value);
    static std::string getString(std::any value);

    // 资源集合相关
    static void* findElementByName(const std::vector<std::any>& elements, const std::string& name);

    // 容器可编辑性
    static bool isEditable(emf::common::EObject* object);
    static bool isReadOnly(emf::common::EObject* object);
    static bool isSet(emf::common::EObject* object);
};

}  // namespace emf::edit::util
