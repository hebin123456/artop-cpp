// CommandHelper.h
// 对齐 Java: org.eclipse.emf.edit.command.CommandHelper
// 状态: 框架骨架（最小可编译实现）
#pragma once

#include "emf/common/command/Command.h"

#include <any>
#include <string>
#include <vector>

namespace emf::edit {
class EditingDomain;
}

namespace emf::common {
class EObject;
}

namespace emf::ecore {
class EStructuralFeature;
}

namespace emf::edit::command {

// CommandHelper：便捷工厂，按参数创建常用 Command（对齐 Java CommandHelper）
class CommandHelper {
public:
    static emf::common::command::Command* createAddCommand(
        emf::edit::EditingDomain* domain,
        emf::common::EObject* owner,
        emf::ecore::EStructuralFeature* feature,
        std::any value);

    static emf::common::command::Command* createRemoveCommand(
        emf::edit::EditingDomain* domain,
        emf::common::EObject* owner,
        emf::ecore::EStructuralFeature* feature,
        std::any value);

    static emf::common::command::Command* createSetCommand(
        emf::edit::EditingDomain* domain,
        emf::common::EObject* owner,
        emf::ecore::EStructuralFeature* feature,
        std::any value);

    static emf::common::command::Command* createReplaceCommand(
        emf::edit::EditingDomain* domain,
        emf::common::EObject* owner,
        emf::ecore::EStructuralFeature* feature,
        std::any value,
        std::any replacement);

    static emf::common::command::Command* createMoveCommand(
        emf::edit::EditingDomain* domain,
        emf::common::EObject* owner,
        emf::ecore::EStructuralFeature* feature,
        std::any value,
        int index);
};

}  // namespace emf::edit::command
