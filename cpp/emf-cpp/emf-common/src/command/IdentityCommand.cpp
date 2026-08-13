// IdentityCommand.cpp
// 对齐 Java org.eclipse.emf.common.command.IdentityCommand
#include "emf/common/command/IdentityCommand.h"
#include "emf/common/CommonPlugin.h"

namespace emf::common::command {

std::string IdentityCommand::getLabel() {
    return label_.empty()
               ? CommonPlugin::instance().getString("_UI_IdentityCommand_label")
               : label_;
}

std::string IdentityCommand::getDescription() {
    return description_.empty()
               ? CommonPlugin::instance().getString("_UI_IdentityCommand_description")
               : description_;
}

}  // namespace emf::common::command
