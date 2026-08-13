// CommandHelper.cpp
// 对齐 Java: org.eclipse.emf.edit.command.CommandHelper
#include "emf/edit/command/CommandHelper.h"
#include "emf/edit/command/AddCommand.h"
#include "emf/edit/command/RemoveCommand.h"
#include "emf/edit/command/SetCommand.h"
#include "emf/edit/command/ReplaceCommand.h"
#include "emf/edit/command/MoveCommand.h"

namespace emf::edit::command {

emf::common::command::Command* CommandHelper::createAddCommand(
    emf::edit::EditingDomain* domain,
    emf::common::EObject* owner,
    emf::ecore::EStructuralFeature* feature,
    std::any value) {
    return new AddCommand(domain, owner, feature, std::move(value));
}

emf::common::command::Command* CommandHelper::createRemoveCommand(
    emf::edit::EditingDomain* domain,
    emf::common::EObject* owner,
    emf::ecore::EStructuralFeature* feature,
    std::any value) {
    return new RemoveCommand(domain, owner, feature, std::move(value));
}

emf::common::command::Command* CommandHelper::createSetCommand(
    emf::edit::EditingDomain* domain,
    emf::common::EObject* owner,
    emf::ecore::EStructuralFeature* feature,
    std::any value) {
    return new SetCommand(domain, owner, feature, std::move(value));
}

emf::common::command::Command* CommandHelper::createReplaceCommand(
    emf::edit::EditingDomain* domain,
    emf::common::EObject* owner,
    emf::ecore::EStructuralFeature* feature,
    std::any value,
    std::any replacement) {
    return new ReplaceCommand(domain, owner, feature, std::move(value), std::move(replacement));
}

emf::common::command::Command* CommandHelper::createMoveCommand(
    emf::edit::EditingDomain* domain,
    emf::common::EObject* owner,
    emf::ecore::EStructuralFeature* feature,
    std::any value,
    int index) {
    return new MoveCommand(domain, owner, feature, std::move(value), index);
}

}  // namespace emf::edit::command
