// CommandWrapper.h
// 对齐 Java org.eclipse.emf.common.command.CommandWrapper
// 装饰器/代理模式：把 Command 接口调用委托给内部 command。
#pragma once

#include "emf/common/command/AbstractCommand.h"

namespace emf::common::command {

class CommandWrapper : public AbstractCommand {
public:
    // 直接包一个 command（用其 label/description 初始化）
    explicit CommandWrapper(Command* command)
        : AbstractCommand(command->getLabel(), command->getDescription()),
          command_(command) {}

    // 装饰器模式：自定义 label/description
    CommandWrapper(const std::string& label, Command* command)
        : AbstractCommand(label, command->getDescription()),
          command_(command) {}

    CommandWrapper(const std::string& label, const std::string& description, Command* command)
        : AbstractCommand(label, description),
          command_(command) {}

    // 代理模式：command 为空，由 createCommand() 创建
    CommandWrapper() = default;
    explicit CommandWrapper(const std::string& label) : AbstractCommand(label) {}
    CommandWrapper(const std::string& label, const std::string& description)
        : AbstractCommand(label, description) {}

    Command* getCommand() const { return command_; }

    // Java 中 prepare() 懒创建
    bool prepare() override;

    void execute() override;
    bool canUndo() override;
    void undo() override;
    void redo() override;

    Collection getResult() override;
    Collection getAffectedObjects() override;
    std::string getLabel() override;
    std::string getDescription() override;
    void dispose() override;

protected:
    // 钩子：子类覆写以创建代理 command
    virtual Command* createCommand() { return nullptr; }

    Command* command_ = nullptr;
};

}  // namespace emf::common::command
