// ReplaceCommand.h
// 对齐 Java: org.eclipse.emf.edit.command.ReplaceCommand
// 状态: 框架骨架（最小可编译实现）
#pragma once

#include "emf/common/command/AbstractCommand.h"
#include "emf/common/EObject.h"

#include <any>
#include <string>
#include <vector>

namespace emf::edit {
class EditingDomain;
}

namespace emf::ecore {
class EStructuralFeature;
}

namespace emf::edit::command {

// ReplaceCommand：替换 owner 的某 feature 集合中的元素（对齐 Java ReplaceCommand）
class ReplaceCommand : public emf::common::command::AbstractCommand {
public:
    ReplaceCommand() = default;
    ReplaceCommand(emf::edit::EditingDomain* domain,
                   emf::common::EObject* owner,
                   emf::ecore::EStructuralFeature* feature,
                   std::any value,
                   std::any replacement);

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
    std::any replacement_;
    // undo/redo 状态
    bool executed_ = false;
    // value_ 在 list 中的原始 index（-1 表示未找到）
    int replacedIndex_ = -1;
    // set 时返回的旧值（用于 undo）
    std::any oldValue_;
};

}  // namespace emf::edit::command
