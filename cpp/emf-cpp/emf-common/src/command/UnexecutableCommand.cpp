// UnexecutableCommand.cpp
// 对齐 Java org.eclipse.emf.common.command.UnexecutableCommand
#include "emf/common/command/UnexecutableCommand.h"
#include "emf/common/CommonPlugin.h"

#include <stdexcept>

namespace emf::common::command {

UnexecutableCommand::UnexecutableCommand()
    : AbstractCommand(
          CommonPlugin::instance().getString("_UI_UnexecutableCommand_label"),
          CommonPlugin::instance().getString("_UI_UnexecutableCommand_description")) {}

void UnexecutableCommand::execute() {
    throw std::runtime_error(
        CommonPlugin::instance().getString(
            "_EXC_Method_not_implemented",
            {std::string("UnexecutableCommand.execute()")}));
}

void UnexecutableCommand::redo() {
    throw std::runtime_error(
        CommonPlugin::instance().getString(
            "_EXC_Method_not_implemented",
            {std::string("UnexecutableCommand.redo()")}));
}

}  // namespace emf::common::command
