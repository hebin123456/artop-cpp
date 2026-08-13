// CompoundCommand.h
// 对齐 Java org.eclipse.emf.common.command.CompoundCommand
#pragma once

#include "emf/common/command/AbstractCommand.h"
#include <climits>
#include <list>
#include <vector>

namespace emf::common::command {

class CompoundCommand : public AbstractCommand {
public:
    // Java 语义：
    //   LAST_COMMAND_ALL  = Integer.MIN_VALUE      = -2147483648
    //   MERGE_COMMAND_ALL = Integer.MIN_VALUE - 1  =  2147483647 (Java int 算术 wrapping)
    // C++ constexpr 拒绝 INT_MIN - 1 表达式（overflow），故分别用 INT_MIN / INT_MAX 表达
    static const int LAST_COMMAND_ALL;
    static const int MERGE_COMMAND_ALL;

    CompoundCommand() : CompoundCommand(MERGE_COMMAND_ALL) {}
    explicit CompoundCommand(const std::string& label)
        : CompoundCommand(MERGE_COMMAND_ALL, label) {}
    CompoundCommand(const std::string& label, const std::string& description)
        : CompoundCommand(MERGE_COMMAND_ALL, label, description) {}
    explicit CompoundCommand(std::list<Command*> commandList)
        : CompoundCommand(MERGE_COMMAND_ALL, commandList) {}
    CompoundCommand(const std::string& label, std::list<Command*> commandList)
        : CompoundCommand(MERGE_COMMAND_ALL, label, commandList) {}
    CompoundCommand(const std::string& label, const std::string& description, std::list<Command*> commandList)
        : CompoundCommand(MERGE_COMMAND_ALL, label, description, commandList) {}
    explicit CompoundCommand(int resultIndex);
    CompoundCommand(int resultIndex, const std::string& label);
    CompoundCommand(int resultIndex, const std::string& label, const std::string& description);
    CompoundCommand(int resultIndex, std::list<Command*> commandList);
    CompoundCommand(int resultIndex, const std::string& label, std::list<Command*> commandList);
    CompoundCommand(int resultIndex, const std::string& label, const std::string& description, std::list<Command*> commandList);

    bool isEmpty() const { return commandList_.empty(); }
    const std::list<Command*>& getCommandList() const { return commandList_; }
    int getResultIndex() const { return resultIndex_; }

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

    void append(Command* command);
    virtual bool appendAndExecute(Command* command);
    bool appendIfCanExecute(Command* command);
    Command* unwrap();

protected:
    Collection getMergedResultCollection() const;
    Collection getMergedAffectedObjectsCollection() const;

    std::list<Command*> commandList_;
    int resultIndex_;
};

}  // namespace emf::common::command
