// StrictCompoundCommand.h
// 对齐 Java org.eclipse.emf.common.command.StrictCompoundCommand
// 假设后续命令依赖前序命令的结果/副作用。
// prepare() 试探执行前 N-1 个；execute()/undo()/redo() 按 isPessimistic 分两种模式。
#pragma once

#include "emf/common/command/CompoundCommand.h"

#include <list>

namespace emf::common::command {

class StrictCompoundCommand : public CompoundCommand {
public:
    StrictCompoundCommand();
    explicit StrictCompoundCommand(const std::string& label);
    StrictCompoundCommand(const std::string& label, const std::string& description);
    explicit StrictCompoundCommand(std::list<Command*> commandList);
    StrictCompoundCommand(const std::string& label, std::list<Command*> commandList);
    StrictCompoundCommand(const std::string& label, const std::string& description, std::list<Command*> commandList);

    // Java 字段：isUndoable（控制 canUndo）、isPessimistic、rightMostExecutedCommandIndex
    bool getIsUndoable() const { return isUndoable_; }
    void setIsUndoable(bool v) { isUndoable_ = v; }
    bool getIsPessimistic() const { return isPessimistic_; }
    void setIsPessimistic(bool v) { isPessimistic_ = v; }
    int getRightMostExecutedCommandIndex() const { return rightMostExecutedCommandIndex_; }

    bool prepare() override;
    void execute() override;
    void undo() override;
    void redo() override;

    // Java: appendAndExecute 在 Strict 中行为略不同：执行后 isPessimistic = true
    bool appendAndExecute(Command* command) override;

protected:
    bool isUndoable_ = false;
    bool isPessimistic_ = false;
    int rightMostExecutedCommandIndex_ = -1;
};

}  // namespace emf::common::command
