// EditingDomain.h
// 对齐 Java: org.eclipse.emf.edit.domain.EditingDomain
// 状态: 框架骨架（仅声明，未实现）
#pragma once

#include "emf/common/ENotifier.h"
#include "emf/common/AdapterFactory.h"
#include "emf/common/command/CommandStack.h"

#include <any>
#include <string>
#include <vector>

namespace emf::edit {

// Java 端 EditingDomain 接口是 Notifier（继承 EMF Common 的 ENotifier），
// 它承载 CommandStack / AdapterFactory / ResourceSet。
// C++ 端用抽象基类，对齐 Java 接口语义。
class EditingDomain : public emf::common::Notifier {
public:
    ~EditingDomain() override = default;

    // ResourceSet 访问（Java: getResourceSet）
    virtual class ResourceSet* getResourceSet() const = 0;

    // CommandStack 访问
    virtual emf::common::command::CommandStack* getCommandStack() const = 0;

    // AdapterFactory 树（Java: getAdapterFactory）
    virtual emf::common::AdapterFactory* getAdapterFactory() const = 0;

    // 创建并执行 command 的便捷入口（Java 行为）
    virtual emf::common::command::Command* createCommand(std::any /*commandClass*/,
                                                        const std::vector<std::any>& parameters) = 0;
};

}  // namespace emf::edit
