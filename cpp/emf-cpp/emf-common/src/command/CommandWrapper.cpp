// CommandWrapper.cpp
// 对齐 Java org.eclipse.emf.common.command.CommandWrapper
#include "emf/common/command/CommandWrapper.h"
#include "emf/common/CommonPlugin.h"

namespace emf::common::command {

bool CommandWrapper::prepare() {
    if (command_ == nullptr) {
        command_ = createCommand();
    }
    return command_ != nullptr && command_->canExecute();
}

void CommandWrapper::execute() {
    if (command_ != nullptr) {
        command_->execute();
    }
}

bool CommandWrapper::canUndo() {
    return command_ == nullptr || command_->canUndo();
}

void CommandWrapper::undo() {
    if (command_ != nullptr) {
        command_->undo();
    }
}

void CommandWrapper::redo() {
    if (command_ != nullptr) {
        command_->redo();
    }
}

Collection CommandWrapper::getResult() {
    return command_ == nullptr ? Collection() : command_->getResult();
}

Collection CommandWrapper::getAffectedObjects() {
    return command_ == nullptr ? Collection() : command_->getAffectedObjects();
}

std::string CommandWrapper::getLabel() {
    if (!label_.empty()) return label_;
    return command_ == nullptr
               ? CommonPlugin::instance().getString("_UI_CommandWrapper_label")
               : command_->getLabel();
}

std::string CommandWrapper::getDescription() {
    if (!description_.empty()) return description_;
    return command_ == nullptr
               ? CommonPlugin::instance().getString("_UI_CommandWrapper_description")
               : command_->getDescription();
}

void CommandWrapper::dispose() {
    if (command_ != nullptr) {
        command_->dispose();
    }
}

}  // namespace emf::common::command
