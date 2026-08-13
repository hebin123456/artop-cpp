// IItemLabelProvider.h
// 对齐 Java: org.eclipse.emf.edit.provider.IItemLabelProvider
// 状态: 框架骨架（仅声明，未实现）
#pragma once

#include "IItemProvider.h"

#include <string>
#include <any>

namespace emf::edit::provider {

// Java 端提供 getText / getImage / getFont / getForeground / getBackground 等。
class IItemLabelProvider : virtual public IItemProvider {
public:
    ~IItemLabelProvider() override = default;

    virtual std::string getText(std::any object) = 0;
    virtual void* getImage(std::any object) = 0;  // Java: Image; C++ 端用 void* 占位
};

}  // namespace emf::edit::provider
