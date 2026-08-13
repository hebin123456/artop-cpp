// SetCommand.h
// 对齐 Java: org.eclipse.emf.edit.command.SetCommand
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

// SetCommand：设置 owner 的某 feature 值（对齐 Java SetCommand）
class SetCommand : public emf::common::command::AbstractCommand {
public:
    SetCommand() = default;
    SetCommand(emf::edit::EditingDomain* domain,
               emf::common::EObject* owner,
               emf::ecore::EStructuralFeature* feature,
               std::any value);

    void execute() override;
    void undo() override;
    void redo() override;

    // 对齐 Java SetCommand.UNSET_VALUE
    static const std::any UNSET_VALUE;

protected:
    bool prepare() override { return owner_ != nullptr; }

private:
    emf::edit::EditingDomain* domain_ = nullptr;
    emf::common::EObject* owner_ = nullptr;
    emf::ecore::EStructuralFeature* feature_ = nullptr;
    std::any value_;
    std::any oldValue_;
    // undo/redo 状态：executed_ 区分首次执行与 redo（避免 redo 重新快照 oldValue_）
    bool executed_ = false;
    // wasSet_：执行前 feature 是否处于 isSet 状态（用于 undo 判断 eSet vs eUnset）
    bool wasSet_ = false;
    // 多值 feature 的旧列表快照（仅 EObject* 列表支持，用于 undo 恢复）
    std::vector<emf::common::EObject*> oldEObjectList_;
};

}  // namespace emf::edit::command
