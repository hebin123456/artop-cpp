// AbstractCommand.h
// 对齐 Java org.eclipse.emf.common.command.AbstractCommand
#pragma once

#include "emf/common/command/Command.h"
#include "emf/common/CommonPlugin.h"

namespace emf::common::command {

class AbstractCommand : public Command {
public:
    // 标记接口：不会让 model 变 dirty
    class NonDirtying {
    public:
        virtual ~NonDirtying() = default;
    };

    AbstractCommand() = default;
    explicit AbstractCommand(const std::string& label) : label_(label) {}
    AbstractCommand(const std::string& label, const std::string& description)
        : label_(label), description_(description) {}

    // 默认 prepare() 返回 false；子类必须重写以使命令可执行
    virtual bool prepare() { return false; }

    bool canExecute() override {
        if (!isPrepared_) {
            isExecutable_ = prepare();
            isPrepared_ = true;
        }
        return isExecutable_;
    }

    bool canUndo() override { return true; }

    void undo() override {
        throw std::runtime_error(
            CommonPlugin::instance().getString(
                "_EXC_Method_not_implemented",
                {std::string("AbstractCommand.undo()")}));
    }

    Collection getResult() override { return {}; }
    Collection getAffectedObjects() override { return {}; }

    std::string getLabel() override {
        return label_.empty()
                   ? CommonPlugin::instance().getString("_UI_AbstractCommand_label")
                   : label_;
    }

    std::string getDescription() override {
        return description_.empty()
                   ? CommonPlugin::instance().getString("_UI_AbstractCommand_description")
                   : description_;
    }

    void setLabel(const std::string& label) { label_ = label; }
    void setDescription(const std::string& description) { description_ = description; }

    void dispose() override { /* 默认空操作 */ }

    // 默认 chain: 用 ChainedCompoundCommand 把两个合起来
    Command* chain(Command* command) override;

protected:
    bool isPrepared_ = false;
    bool isExecutable_ = false;
    std::string description_;
    std::string label_;
};

}  // namespace emf::common::command
