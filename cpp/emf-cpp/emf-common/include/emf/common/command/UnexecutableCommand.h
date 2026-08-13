// UnexecutableCommand.h
// 对齐 Java org.eclipse.emf.common.command.UnexecutableCommand
// 单例；canExecute/canUndo 都返回 false；execute/redo 抛异常。
#pragma once

#include "emf/common/command/AbstractCommand.h"

namespace emf::common::command {

class UnexecutableCommand : public AbstractCommand {
public:
    // Java 中 INSTANCE 是 public static final；这里提供 instance() 工厂
    static UnexecutableCommand& instance() {
        static UnexecutableCommand inst;
        return inst;
    }

    bool canExecute() override { return false; }
    void execute() override;
    bool canUndo() override { return false; }
    void redo() override;

    // 单例：禁用拷贝/赋值
    UnexecutableCommand(const UnexecutableCommand&) = delete;
    UnexecutableCommand& operator=(const UnexecutableCommand&) = delete;

private:
    UnexecutableCommand();
};

}  // namespace emf::common::command
