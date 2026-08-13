// MoveCommand.cpp
// 对齐 Java: org.eclipse.emf.edit.command.MoveCommand
#include "emf/edit/command/MoveCommand.h"

#include "emf/common/EList.h"
#include "emf/ecore/EcoreImpls.h"

namespace emf::edit::command {

MoveCommand::MoveCommand(emf::edit::EditingDomain* domain,
                         emf::common::EObject* owner,
                         emf::ecore::EStructuralFeature* feature,
                         std::any value,
                         int index)
    : domain_(domain), owner_(owner), feature_(feature),
      value_(std::move(value)), index_(index) {}

namespace {
// 在 list 中查找 value 的当前 index（不支持类型 / 找不到返回 -1）
int findIndex(const std::any& listAny, const std::any& value) {
    if (listAny.type() == typeid(emf::common::EList<emf::common::EObject*>*)) {
        auto* list = std::any_cast<emf::common::EList<emf::common::EObject*>*>(listAny);
        if (!list) return -1;
        if (value.type() == typeid(emf::common::EObject*)) {
            return list->indexOf(std::any_cast<emf::common::EObject*>(value));
        }
    } else if (listAny.type() == typeid(emf::common::EList<std::string>*)) {
        auto* list = std::any_cast<emf::common::EList<std::string>*>(listAny);
        if (!list) return -1;
        if (value.type() == typeid(std::string)) {
            return list->indexOf(std::any_cast<std::string>(value));
        }
    }
    return -1;
}

// 调用 list->move(targetIndex, sourceIndex)
void moveInList(const std::any& listAny, int targetIndex, int sourceIndex) {
    if (listAny.type() == typeid(emf::common::EList<emf::common::EObject*>*)) {
        auto* list = std::any_cast<emf::common::EList<emf::common::EObject*>*>(listAny);
        if (list) list->move(targetIndex, sourceIndex);
    } else if (listAny.type() == typeid(emf::common::EList<std::string>*)) {
        auto* list = std::any_cast<emf::common::EList<std::string>*>(listAny);
        if (list) list->move(targetIndex, sourceIndex);
    }
}
}  // namespace

void MoveCommand::execute() {
    if (!owner_ || !feature_) return;

    auto listAny = owner_->eGet(feature_);

    // 首次执行：记录 value_ 的原始 index
    if (!executed_) {
        oldIndex_ = findIndex(listAny, value_);
    }

    // 应用 move（同时是 redo 路径）
    if (oldIndex_ >= 0 && oldIndex_ != index_) {
        moveInList(listAny, index_, oldIndex_);
    }
    executed_ = true;
}

void MoveCommand::undo() {
    if (!owner_ || !feature_ || !executed_ || oldIndex_ < 0) return;
    // 反向 move：从 index_ 移回 oldIndex_
    auto listAny = owner_->eGet(feature_);
    if (index_ != oldIndex_) {
        moveInList(listAny, oldIndex_, index_);
    }
}

void MoveCommand::redo() {
    if (!owner_ || !feature_ || !executed_ || oldIndex_ < 0) return;
    // 重新 move：从 oldIndex_ 移到 index_（executed_ 已为 true，不重新记录 oldIndex_）
    auto listAny = owner_->eGet(feature_);
    if (oldIndex_ != index_) {
        moveInList(listAny, index_, oldIndex_);
    }
}

}  // namespace emf::edit::command
