// MoveCommand.h
// 对齐 Java: org.eclipse.emf.edit.command.MoveCommand
// 状态: 框架骨架（最小可编译实现）
#pragma once

#include "emf/common/command/AbstractCommand.h"
#include "emf/common/EObject.h"

#include <any>
#include <string>

namespace emf::edit {
class EditingDomain;
}

namespace emf::ecore {
class EStructuralFeature;
}

namespace emf::edit::command {

// MoveCommand：在 owner 的某 feature 集合中移动元素到指定 index（对齐 Java MoveCommand）
class MoveCommand : public emf::common::command::AbstractCommand {
public:
    MoveCommand() = default;
    MoveCommand(emf::edit::EditingDomain* domain,
                emf::common::EObject* owner,
                emf::ecore::EStructuralFeature* feature,
                std::any value,
                int index);

    void execute() override;
    void undo() override;
    void redo() override;

protected:
    bool prepare() override { return owner_ != nullptr; }

private:
    emf::edit::EditingDomain* domain_ = nullptr;
    emf::common::EObject* owner_ = nullptr;
    emf::ecore::EStructuralFeature* feature_ = nullptr;
    std::any value_;
    int index_ = 0;
    // undo/redo 状态
    bool executed_ = false;
    // value_ 在 list 中的原始 index（用于 undo move 回去）
    int oldIndex_ = -1;
};

}  // namespace emf::edit::command
