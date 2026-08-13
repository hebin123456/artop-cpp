// CompoundCommand.cpp
// 对齐 Java org.eclipse.emf.common.command.CompoundCommand
#include "emf/common/command/CompoundCommand.h"
#include "emf/common/command/UnexecutableCommand.h"
#include "emf/common/CommonPlugin.h"

#include <algorithm>
#include <limits>
#include <stdexcept>

namespace emf::common::command {

// Java 常量定义：LAST_COMMAND_ALL = Integer.MIN_VALUE,
//                 MERGE_COMMAND_ALL = Integer.MIN_VALUE - 1 (Java int wrapping 后为 Integer.MAX_VALUE)
const int CompoundCommand::LAST_COMMAND_ALL = INT_MIN;
const int CompoundCommand::MERGE_COMMAND_ALL = INT_MAX;

CompoundCommand::CompoundCommand(int resultIndex)
    : resultIndex_(resultIndex) {}

CompoundCommand::CompoundCommand(int resultIndex, const std::string& label)
    : AbstractCommand(label), resultIndex_(resultIndex) {}

CompoundCommand::CompoundCommand(int resultIndex, const std::string& label, const std::string& description)
    : AbstractCommand(label, description), resultIndex_(resultIndex) {}

CompoundCommand::CompoundCommand(int resultIndex, std::list<Command*> commandList)
    : commandList_(std::move(commandList)), resultIndex_(resultIndex) {}

CompoundCommand::CompoundCommand(int resultIndex, const std::string& label, std::list<Command*> commandList)
    : AbstractCommand(label), commandList_(std::move(commandList)), resultIndex_(resultIndex) {}

CompoundCommand::CompoundCommand(int resultIndex, const std::string& label, const std::string& description, std::list<Command*> commandList)
    : AbstractCommand(label, description), commandList_(std::move(commandList)), resultIndex_(resultIndex) {}

bool CompoundCommand::prepare() {
    if (commandList_.empty()) return false;
    for (auto* c : commandList_) {
        if (!c->canExecute()) return false;
    }
    return true;
}

void CompoundCommand::execute() {
    auto it = commandList_.begin();
    try {
        while (it != commandList_.end()) {
            Command* cmd = *it;
            cmd->execute();
            ++it;
        }
    } catch (const std::exception&) {
        // 回滚：把已执行的命令按相反顺序 undo
        auto undoIt = it;
        bool firstSkipped = true;
        while (undoIt != commandList_.begin()) {
            if (firstSkipped) {
                firstSkipped = false;
                // 跳过当前抛错的命令
                if (undoIt == commandList_.begin()) break;
                --undoIt;
            } else {
                --undoIt;
            }
            if (!(*undoIt)->canUndo()) break;
            try { (*undoIt)->undo(); } catch (...) {
                CommonPlugin::instance().log("_UI_IgnoreException_exception");
            }
        }
        throw;
    }
}

bool CompoundCommand::canUndo() {
    for (auto* c : commandList_) if (!c->canUndo()) return false;
    return true;
}

void CompoundCommand::undo() {
    // 反向遍历
    for (auto it = commandList_.rbegin(); it != commandList_.rend(); ++it) {
        try { (*it)->undo(); }
        catch (const std::exception&) {
            // 顺着重做（Java 行为）
            for (auto redoIt = std::next(it); redoIt != commandList_.rend(); ++redoIt) {
                try { (*redoIt)->redo(); } catch (...) {
                    CommonPlugin::instance().log("_UI_IgnoreException_exception");
                }
            }
            throw;
        }
    }
}

void CompoundCommand::redo() {
    auto it = commandList_.begin();
    try {
        while (it != commandList_.end()) {
            (*it)->redo();
            ++it;
        }
    } catch (const std::exception&) {
        auto undoIt = it;
        while (undoIt != commandList_.begin()) {
            --undoIt;
            if (!(*undoIt)->canUndo()) break;
            try { (*undoIt)->undo(); } catch (...) {
                CommonPlugin::instance().log("_UI_IgnoreException_exception");
            }
        }
        throw;
    }
}

Collection CompoundCommand::getResult() {
    if (commandList_.empty()) return {};
    if (resultIndex_ == LAST_COMMAND_ALL) {
        return commandList_.back()->getResult();
    }
    if (resultIndex_ == MERGE_COMMAND_ALL) {
        return getMergedResultCollection();
    }
    if (resultIndex_ >= 0 && resultIndex_ < (int)commandList_.size()) {
        auto it = commandList_.begin();
        std::advance(it, resultIndex_);
        return (*it)->getResult();
    }
    return {};
}

Collection CompoundCommand::getAffectedObjects() {
    if (commandList_.empty()) return {};
    if (resultIndex_ == LAST_COMMAND_ALL) {
        return commandList_.back()->getAffectedObjects();
    }
    if (resultIndex_ == MERGE_COMMAND_ALL) {
        return getMergedAffectedObjectsCollection();
    }
    if (resultIndex_ >= 0 && resultIndex_ < (int)commandList_.size()) {
        auto it = commandList_.begin();
        std::advance(it, resultIndex_);
        return (*it)->getAffectedObjects();
    }
    return {};
}

std::string CompoundCommand::getLabel() {
    if (!label_.empty()) return label_;
    if (commandList_.empty()) {
        return CommonPlugin::instance().getString("_UI_CompoundCommand_label");
    }
    if (resultIndex_ == LAST_COMMAND_ALL || resultIndex_ == MERGE_COMMAND_ALL) {
        return commandList_.back()->getLabel();
    }
    if (resultIndex_ >= 0 && resultIndex_ < (int)commandList_.size()) {
        auto it = commandList_.begin();
        std::advance(it, resultIndex_);
        return (*it)->getLabel();
    }
    return CommonPlugin::instance().getString("_UI_CompoundCommand_label");
}

std::string CompoundCommand::getDescription() {
    if (!description_.empty()) return description_;
    if (commandList_.empty()) {
        return CommonPlugin::instance().getString("_UI_CompoundCommand_description");
    }
    if (resultIndex_ == LAST_COMMAND_ALL || resultIndex_ == MERGE_COMMAND_ALL) {
        return commandList_.back()->getDescription();
    }
    if (resultIndex_ >= 0 && resultIndex_ < (int)commandList_.size()) {
        auto it = commandList_.begin();
        std::advance(it, resultIndex_);
        return (*it)->getDescription();
    }
    return CommonPlugin::instance().getString("_UI_CompoundCommand_description");
}

void CompoundCommand::dispose() {
    for (auto* c : commandList_) c->dispose();
}

void CompoundCommand::append(Command* command) {
    if (isPrepared_) {
        throw std::runtime_error("The command is already prepared");
    }
    if (command) commandList_.push_back(command);
}

bool CompoundCommand::appendAndExecute(Command* command) {
    if (!command) return false;
    if (!isPrepared_) {
        if (commandList_.empty()) {
            isPrepared_ = true;
            isExecutable_ = true;
        } else {
            isExecutable_ = prepare();
            isPrepared_ = true;
            if (isExecutable_) execute();
        }
    }
    if (command->canExecute()) {
        try {
            command->execute();
            commandList_.push_back(command);
            return true;
        } catch (const std::exception& e) {
            (void)e;
            CommonPlugin::instance().log("_UI_IgnoreException_exception");
        }
    }
    command->dispose();
    return false;
}

bool CompoundCommand::appendIfCanExecute(Command* command) {
    if (!command) return false;
    if (command->canExecute()) {
        commandList_.push_back(command);
        return true;
    }
    command->dispose();
    return false;
}

Command* CompoundCommand::unwrap() {
    switch (commandList_.size()) {
        case 0:
            dispose();
            return &UnexecutableCommand::instance();
        case 1: {
            auto it = commandList_.begin();
            Command* r = *it;
            commandList_.erase(it);
            dispose();
            return r;
        }
        default:
            return this;
    }
}

Collection CompoundCommand::getMergedResultCollection() const {
    Collection r;
    for (auto* c : commandList_) {
        auto sub = c->getResult();
        r.insert(r.end(), sub.begin(), sub.end());
    }
    return r;
}

Collection CompoundCommand::getMergedAffectedObjectsCollection() const {
    Collection r;
    for (auto* c : commandList_) {
        auto sub = c->getAffectedObjects();
        r.insert(r.end(), sub.begin(), sub.end());
    }
    return r;
}

}  // namespace emf::common::command
