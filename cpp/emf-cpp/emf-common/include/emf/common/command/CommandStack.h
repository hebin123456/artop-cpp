// CommandStack.h
// 对齐 Java org.eclipse.emf.common.command.CommandStack
#pragma once

namespace emf::common::command {

class Command;

class CommandStack {
public:
    virtual ~CommandStack() = default;

    virtual void execute(Command* command) = 0;
    virtual bool canUndo() = 0;
    virtual void undo() = 0;
    virtual bool canRedo() = 0;
    virtual void redo() = 0;
    virtual void flush() = 0;

    virtual Command* getUndoCommand() = 0;
    virtual Command* getRedoCommand() = 0;
    virtual Command* getMostRecentCommand() = 0;

    class Listener {
    public:
        virtual ~Listener() = default;
        virtual void commandStackChanged(void* event) = 0;
    };
    virtual void addCommandStackListener(Listener* listener) = 0;
    virtual void removeCommandStackListener(Listener* listener) = 0;
};

}  // namespace emf::common::command
