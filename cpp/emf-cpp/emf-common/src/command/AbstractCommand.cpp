// AbstractCommand.cpp
// 对齐 Java org.eclipse.emf.common.command.AbstractCommand.chain()
// 默认 chain() 把 this 和 command 包成一个 CompoundCommand。
#include "emf/common/command/AbstractCommand.h"
#include "emf/common/command/CompoundCommand.h"

namespace emf::common::command {

namespace {
// 内部类：覆盖 chain，让后续 chain 继续 append 而不是再嵌一个 CompoundCommand
class ChainedCompoundCommand : public CompoundCommand {
public:
    ChainedCompoundCommand() : CompoundCommand() {}

    Command* chain(Command* c) override {
        append(c);
        return this;
    }
};
}  // namespace

Command* AbstractCommand::chain(Command* command) {
    CompoundCommand* result = new ChainedCompoundCommand();
    result->append(this);
    result->append(command);
    return result;
}

}  // namespace emf::common::command
