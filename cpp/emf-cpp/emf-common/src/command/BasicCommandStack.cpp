// BasicCommandStack.cpp
// 对齐 Java org.eclipse.emf.common.command.BasicCommandStack
#include "emf/common/command/BasicCommandStack.h"
#include "emf/common/command/AbstractCommand.h"
#include "emf/common/command/CommandStackListener.h"
#include "emf/common/command/AbortExecutionException.h"
#include "emf/common/CommonPlugin.h"

#include <exception>
#include <stdexcept>

namespace emf::common::command {

void BasicCommandStack::execute(Command* command) {
    if (command == nullptr) return;
    if (command->canExecute()) {
        try {
            command->execute();

            // Clear the list past the top
            while (static_cast<int>(commandList_.size()) > top_ + 1) {
                auto back = std::prev(commandList_.end());
                (*back)->dispose();
                commandList_.erase(back);
            }

            mostRecentCommand_ = command;
            commandList_.push_back(command);
            ++top_;

            // saveIndex 在 redo 部分被清除，强制 isSaveNeeded 为 true
            if (saveIndex_ >= top_) {
                saveIndex_ = -2;
            }
            notifyListeners();
        } catch (const AbortExecutionException&) {
            command->dispose();
        } catch (const std::exception& e) {
            handleError(e);
            mostRecentCommand_ = nullptr;
            command->dispose();
            notifyListeners();
        }
    } else {
        command->dispose();
    }
}

bool BasicCommandStack::canUndo() {
    return top_ != -1 && commandList_.back()->canUndo();
}

void BasicCommandStack::undo() {
    if (canUndo()) {
        // 取 top 位置的 command 并 --top
        auto it = commandList_.begin();
        std::advance(it, top_);
        Command* command = *it;
        --top_;
        try {
            command->undo();
            mostRecentCommand_ = command;
        } catch (const std::exception& e) {
            handleError(e);
            mostRecentCommand_ = nullptr;
            flush();
        }
        notifyListeners();
    }
}

bool BasicCommandStack::canRedo() {
    return top_ < static_cast<int>(commandList_.size()) - 1;
}

void BasicCommandStack::redo() {
    if (canRedo()) {
        ++top_;
        auto it = commandList_.begin();
        std::advance(it, top_);
        Command* command = *it;
        try {
            command->redo();
            mostRecentCommand_ = command;
        } catch (const std::exception& e) {
            handleError(e);
            mostRecentCommand_ = nullptr;
            // Clear the list past the top
            while (static_cast<int>(commandList_.size()) > top_) {
                auto back = std::prev(commandList_.end());
                (*back)->dispose();
                commandList_.erase(back);
            }
            --top_;
        }
        notifyListeners();
    }
}

void BasicCommandStack::flush() {
    for (auto* c : commandList_) c->dispose();
    commandList_.clear();
    top_ = -1;
    saveIndex_ = -1;
    mostRecentCommand_ = nullptr;
    notifyListeners();
}

Command* BasicCommandStack::getUndoCommand() {
    if (top_ == -1 || top_ >= static_cast<int>(commandList_.size())) return nullptr;
    auto it = commandList_.begin();
    std::advance(it, top_);
    return *it;
}

Command* BasicCommandStack::getRedoCommand() {
    if (top_ + 1 >= static_cast<int>(commandList_.size())) return nullptr;
    auto it = commandList_.begin();
    std::advance(it, top_ + 1);
    return *it;
}

Command* BasicCommandStack::getMostRecentCommand() {
    return mostRecentCommand_;
}

void BasicCommandStack::addCommandStackListener(Listener* listener) {
    if (listener) listeners_.push_back(listener);
}

void BasicCommandStack::removeCommandStackListener(Listener* listener) {
    for (auto it = listeners_.begin(); it != listeners_.end(); ++it) {
        if (*it == listener) {
            listeners_.erase(it);
            return;
        }
    }
}

void BasicCommandStack::notifyListeners() {
    for (auto* l : listeners_) {
        l->commandStackChanged(this);
    }
}

void BasicCommandStack::handleError(const std::exception& e) {
    CommonPlugin::instance().log(std::string("_UI_IgnoreException_exception: ") + e.what());
}

void BasicCommandStack::saveIsDone() {
    saveIndex_ = top_;
}

bool BasicCommandStack::isSaveNeeded() const {
    if (saveIndex_ < -1) return true;
    if (top_ > saveIndex_) {
        auto it = commandList_.begin();
        std::advance(it, top_);
        for (int i = top_; i > saveIndex_; --i, --it) {
            // NonDirtying 标记：AbstractCommand 内嵌类
            if (!dynamic_cast<AbstractCommand::NonDirtying*>(*it)) {
                return true;
            }
        }
    } else {
        // 反向：从 saveIndex 向下到 top+1
        auto it = commandList_.begin();
        std::advance(it, saveIndex_);
        for (int i = saveIndex_; i > top_; --i, --it) {
            if (!dynamic_cast<AbstractCommand::NonDirtying*>(*it)) {
                return true;
            }
        }
    }
    return false;
}

}  // namespace emf::common::command
