// SetCommand.cpp
// 对齐 Java: org.eclipse.emf.edit.command.SetCommand
#include "emf/edit/command/SetCommand.h"

#include "emf/common/EList.h"
#include "emf/ecore/EcoreImpls.h"

namespace emf::edit::command {

// UNSET_VALUE：占位的“未设置”标记（对齐 Java SetCommand.UNSET_VALUE）
const std::any SetCommand::UNSET_VALUE;

SetCommand::SetCommand(emf::edit::EditingDomain* domain,
                       emf::common::EObject* owner,
                       emf::ecore::EStructuralFeature* feature,
                       std::any value)
    : domain_(domain), owner_(owner), feature_(feature), value_(std::move(value)) {}

void SetCommand::execute() {
    if (!owner_ || !feature_) return;

    // 首次执行：快照旧值用于 undo。redo 不重新快照。
    if (!executed_) {
        wasSet_ = owner_->eIsSet(feature_);
        if (feature_->isMany()) {
            // 多值 feature：快照列表内容（best-effort，支持 EObject* 列表）
            oldEObjectList_.clear();
            auto listAny = owner_->eGet(feature_);
            if (listAny.type() == typeid(emf::common::EList<emf::common::EObject*>*)) {
                auto* list = std::any_cast<emf::common::EList<emf::common::EObject*>*>(listAny);
                if (list) {
                    for (size_t i = 0; i < list->size(); ++i) {
                        oldEObjectList_.push_back((*list)[i]);
                    }
                }
            }
        } else if (wasSet_) {
            // 单值：记录旧值（用于 undo 时 eSet 回去）
            oldValue_ = owner_->eGet(feature_);
        }
    }

    // 应用 value_（同时是 redo 路径）
    // value_ 为空 any 时视为 UNSET_VALUE 语义（对齐 Java SetCommand.UNSET_VALUE）
    if (!value_.has_value()) {
        owner_->eUnset(feature_);
    } else {
        owner_->eSet(feature_, value_);
    }
    executed_ = true;
}

void SetCommand::undo() {
    if (!owner_ || !feature_ || !executed_) return;

    if (feature_->isMany()) {
        // 多值：清空当前列表并按快照恢复（best-effort，仅 EObject* 列表）
        auto listAny = owner_->eGet(feature_);
        if (listAny.type() == typeid(emf::common::EList<emf::common::EObject*>*)) {
            auto* list = std::any_cast<emf::common::EList<emf::common::EObject*>*>(listAny);
            if (list) {
                list->clear();
                for (auto* o : oldEObjectList_) {
                    list->add(o);
                }
            }
        }
    } else if (wasSet_) {
        // 之前是 set 状态：恢复 oldValue_
        owner_->eSet(feature_, oldValue_);
    } else {
        // 之前是 unset 状态：eUnset
        owner_->eUnset(feature_);
    }
}

void SetCommand::redo() {
    if (!owner_ || !feature_ || !executed_) return;
    // redo：重新应用 value_，不重新快照（executed_ 已为 true，execute 的快照分支会跳过）
    if (!value_.has_value()) {
        owner_->eUnset(feature_);
    } else {
        owner_->eSet(feature_, value_);
    }
}

}  // namespace emf::edit::command
