// StrictCompoundCommand.cpp
// 对齐 Java org.eclipse.emf.common.command.StrictCompoundCommand
// 关键语义：
//   - prepare() 试探执行 N-1 个前序命令（不是全部执行）；
//   - 最后一个命令的 canUndo 缓存为 isUndoable_；
//   - execute() 视 isPessimistic_ 决定行为：
//       true  -> 重新执行所有（已执行过的 redo，其它 execute）
//       false -> 只执行最后一个（前面已在 prepare 中执行过）
//   - appendAndExecute 与父类不同：执行后 isPessimistic_ 置为 true。
#include "emf/common/command/StrictCompoundCommand.h"
#include "emf/common/CommonPlugin.h"

#include <stdexcept>

namespace emf::common::command {

StrictCompoundCommand::StrictCompoundCommand() : CompoundCommand() {
    resultIndex_ = LAST_COMMAND_ALL;
}
StrictCompoundCommand::StrictCompoundCommand(const std::string& label) : CompoundCommand(label) {
    resultIndex_ = LAST_COMMAND_ALL;
}
StrictCompoundCommand::StrictCompoundCommand(const std::string& label, const std::string& description)
    : CompoundCommand(label, description) {
    resultIndex_ = LAST_COMMAND_ALL;
}
StrictCompoundCommand::StrictCompoundCommand(std::list<Command*> commandList)
    : CompoundCommand(commandList) {
    resultIndex_ = LAST_COMMAND_ALL;
}
StrictCompoundCommand::StrictCompoundCommand(const std::string& label, std::list<Command*> commandList)
    : CompoundCommand(label, commandList) {
    resultIndex_ = LAST_COMMAND_ALL;
}
StrictCompoundCommand::StrictCompoundCommand(const std::string& label, const std::string& description,
                                             std::list<Command*> commandList)
    : CompoundCommand(label, description, commandList) {
    resultIndex_ = LAST_COMMAND_ALL;
}

bool StrictCompoundCommand::prepare() {
    // 与 Java: ListIterator 同步
    auto it = commandList_.begin();
    if (it != commandList_.end()) {
        bool result = true;
        for (;;) {
            Command* command = *it;
            if (command->canExecute()) {
                auto next = std::next(it);
                if (next != commandList_.end()) {
                    // 不是最后一个：试探执行；要求 canUndo
                    if (command->canUndo()) {
                        try {
                            int idx = static_cast<int>(std::distance(commandList_.begin(), it));
                            if (idx <= rightMostExecutedCommandIndex_) {
                                command->redo();
                            } else {
                                ++rightMostExecutedCommandIndex_;
                                command->execute();
                            }
                            ++it;
                        } catch (const std::exception&) {
                            CommonPlugin::instance().log("_UI_IgnoreException_exception");
                            result = false;
                            break;
                        }
                    } else {
                        // 不能 undo 就放弃
                        result = false;
                        break;
                    }
                } else {
                    // 最后一个：只查 canUndo 并缓存
                    isUndoable_ = command->canUndo();
                    break;
                }
            } else {
                result = false;
                break;
            }
        }

        if (isPessimistic_) {
            // 反向 undo 所有试探执行过的
            // Java 中先 commands.previous()（即不 undo 刚处理的最后一个），
            // 然后反向 undo。
            // 这里我们反向 undo，从 it 开始回退。
            // it 此时指向最后一个元素（未执行）或指向某处（如果在循环中 break）
            // 简化处理：从最后一个元素开始反向 undo
            for (auto rit = commandList_.rbegin(); rit != commandList_.rend(); ++rit) {
                auto fwdIt = std::next(rit).base();  // 转为正向 iterator
                if (fwdIt == it) break;  // 跳过当前 it 指向的（未执行）
                (*rit)->undo();
            }
        }
        return result;
    }
    isUndoable_ = false;
    return false;
}

void StrictCompoundCommand::execute() {
    if (isPessimistic_) {
        // 全部执行；已执行过的 redo，其它 execute
        auto it = commandList_.begin();
        try {
            int idx = 0;
            while (it != commandList_.end()) {
                if (idx <= rightMostExecutedCommandIndex_) {
                    (*it)->redo();
                } else {
                    (*it)->execute();
                }
                ++it;
                ++idx;
            }
        } catch (const std::exception&) {
            // 跳过当前 it，反向 undo 已执行的
            --it;
            while (it != commandList_.begin()) {
                if ((*it)->canUndo()) {
                    try { (*it)->undo(); } catch (...) {
                        CommonPlugin::instance().log("_UI_IgnoreException_exception");
                    }
                } else {
                    break;
                }
                --it;
            }
            throw;
        }
    } else if (!commandList_.empty()) {
        // 非悲观：只执行最后一个（前面已经在 prepare 中执行过）
        commandList_.back()->execute();
    }
}

void StrictCompoundCommand::undo() {
    if (isPessimistic_) {
        CompoundCommand::undo();
    } else if (!commandList_.empty()) {
        commandList_.back()->undo();
    }
}

void StrictCompoundCommand::redo() {
    if (isPessimistic_) {
        CompoundCommand::redo();
    } else if (!commandList_.empty()) {
        commandList_.back()->redo();
    }
}

bool StrictCompoundCommand::appendAndExecute(Command* command) {
    if (command != nullptr) {
        if (!isPrepared_) {
            if (commandList_.empty()) {
                isPrepared_ = true;
                isExecutable_ = true;
            } else {
                isExecutable_ = prepare();
                isPrepared_ = true;
                isPessimistic_ = true;  // 与父类的差别点
                if (isExecutable_) {
                    execute();
                }
            }
        }

        if (command->canExecute()) {
            try {
                command->execute();
                commandList_.push_back(command);
                ++rightMostExecutedCommandIndex_;
                isUndoable_ = command->canUndo();
                return true;
            } catch (const std::exception&) {
                CommonPlugin::instance().log("_UI_IgnoreException_exception");
            }
        }

        command->dispose();
    }
    return false;
}

}  // namespace emf::common::command
