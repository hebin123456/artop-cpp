// IStructuredItemContentProvider.h
// 对齐 Java: org.eclipse.emf.edit.provider.IStructuredItemContentProvider
// 状态: 框架骨架（仅声明，未实现）
#pragma once

#include "IItemProvider.h"

namespace emf::edit::provider {

// Java 端 IItemContentProvider 的子接口，提供 getElements(getViewer().getInput())。
class IStructuredItemContentProvider : virtual public IItemProvider {
public:
    ~IStructuredItemContentProvider() override = default;

    // Java: getElements(Object input) —— 通常直接调 getChildren(input)
    virtual std::vector<std::any> getElements(std::any input) = 0;
};

}  // namespace emf::edit::provider
