// RemoveCommand.h
// 对齐 Java: org.eclipse.emf.edit.command.RemoveCommand
// 状态: 框架骨架（最小可编译实现）
#pragma once

#include "emf/common/command/AbstractCommand.h"
#include "emf/common/EObject.h"

#include <any>
#include <string>
#include <utility>
#include <vector>

namespace emf::edit {
class EditingDomain;
}

namespace emf::ecore {
class EStructuralFeature;
}

namespace emf::edit::command {

// RemoveCommand：从 owner 的某 feature 集合移除元素（对齐 Java RemoveCommand）
class RemoveCommand : public emf::common::command::AbstractCommand {
public:
    RemoveCommand() = default;
    RemoveCommand(emf::edit::EditingDomain* domain,
                  emf::common::EObject* owner,
                  emf::ecore::EStructuralFeature* feature,
                  std::any value);
    RemoveCommand(emf::edit::EditingDomain* domain,
                  emf::common::EObject* owner,
                  emf::ecore::EStructuralFeature* feature,
                  std::vector<std::any> collection);

    void execute() override;
    void undo() override;
    void redo() override;

protected:
    bool prepare() override { return owner_ != nullptr; }

private:
    emf::edit::EditingDomain* domain_ = nullptr;
    emf::common::EObject* owner_ = nullptr;
    emf::ecore::EStructuralFeature* feature_ = nullptr;
    std::vector<std::any> values_;
    // undo/redo 状态
    bool executed_ = false;
    // 被移除元素的 (原始 index, 旧值) 对，按 index 升序排序后用于 undo 重新插入
    std::vector<std::pair<int, std::any>> removed_;
};

}  // namespace emf::edit::command
