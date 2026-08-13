// BasicCommandStack.h
// 对齐 Java org.eclipse.emf.common.command.BasicCommandStack
// CommandStack 的标准实现：commandList + top 指针 + saveIndex dirty tracking。
#pragma once

#include "emf/common/command/CommandStack.h"
#include "emf/common/command/Command.h"

#include <list>
#include <vector>

namespace emf::common::command {

class CommandStack;

class BasicCommandStack : public CommandStack {
public:
    BasicCommandStack() : commandList_(), top_(-1), saveIndex_(-1) {}

    void execute(Command* command) override;
    bool canUndo() override;
    void undo() override;
    bool canRedo() override;
    void redo() override;
    void flush() override;

    Command* getUndoCommand() override;
    Command* getRedoCommand() override;
    Command* getMostRecentCommand() override;

    void addCommandStackListener(Listener* listener) override;
    void removeCommandStackListener(Listener* listener) override;

    // Java 额外 API
    void saveIsDone();
    bool isSaveNeeded() const;

protected:
    void notifyListeners();
    void handleError(const std::exception& e);

    std::list<Command*> commandList_;
    int top_;
    int saveIndex_;
    Command* mostRecentCommand_ = nullptr;
    std::vector<Listener*> listeners_;
};

}  // namespace emf::common::command
