// AddCommand.h
// 对齐 Java: org.eclipse.emf.edit.command.AddCommand
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

// AddCommand：向 owner 的某 feature 集合添加元素（对齐 Java AddCommand）
class AddCommand : public emf::common::command::AbstractCommand {
public:
    AddCommand() = default;
    AddCommand(emf::edit::EditingDomain* domain,
               emf::common::EObject* owner,
               emf::ecore::EStructuralFeature* feature,
               std::any value);
    AddCommand(emf::edit::EditingDomain* domain,
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
    // 添加前 list 的 size（undo 时移除 oldSize_..end）
    size_t oldSize_ = 0;
};

}  // namespace emf::edit::command
