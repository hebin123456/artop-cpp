// ITreeItemContentProvider.h
// 对齐 Java: org.eclipse.emf.edit.provider.ITreeItemContentProvider
// 状态: 框架骨架（仅声明，未实现）
#pragma once

#include "IItemProvider.h"

#include <any>
#include <vector>

namespace emf::edit::provider {

// Java 端 IStructuredItemContentProvider 的扩展。
// getParent / hasChildren 用于 tree viewer。
class ITreeItemContentProvider : virtual public IItemProvider {
public:
    ~ITreeItemContentProvider() override = default;

    virtual bool hasChildren(std::any object) = 0;
    // getParent / getChildren 由 IItemProvider 声明
};

}  // namespace emf::edit::provider
