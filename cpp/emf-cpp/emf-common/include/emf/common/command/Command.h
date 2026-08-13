// Command.h
// 对齐 Java org.eclipse.emf.common.command.Command
// 命令接口：execute/undo/redo/canExecute/canUndo/result/affectedObjects/label/description/dispose/chain
#pragma once

#include <any>
#include <string>
#include <vector>

namespace emf::common::command {

// Java 顺序：与 Command.java 字段顺序一致
// Collection<?> -> std::vector<std::any>
using Collection = std::vector<std::any>;

class Command {
public:
    virtual ~Command() = default;

    virtual bool canExecute() = 0;
    virtual void execute() = 0;
    virtual bool canUndo() = 0;
    virtual void undo() = 0;
    virtual void redo() = 0;

    virtual Collection getResult() = 0;
    virtual Collection getAffectedObjects() = 0;
    virtual std::string getLabel() = 0;
    virtual std::string getDescription() = 0;
    virtual void dispose() = 0;

    // 默认 chain：返回 this 与 command 的复合
    virtual Command* chain(Command* command) = 0;
};

}  // namespace emf::common::command
