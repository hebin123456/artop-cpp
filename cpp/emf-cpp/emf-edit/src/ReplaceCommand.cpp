// ReplaceCommand.cpp
// 对齐 Java: org.eclipse.emf.edit.command.ReplaceCommand
#include "emf/edit/command/ReplaceCommand.h"

#include "emf/common/EList.h"
#include "emf/ecore/EcoreImpls.h"

namespace emf::edit::command {

ReplaceCommand::ReplaceCommand(emf::edit::EditingDomain* domain,
                               emf::common::EObject* owner,
                               emf::ecore::EStructuralFeature* feature,
                               std::any value,
                               std::any replacement)
    : domain_(domain), owner_(owner), feature_(feature),
      value_(std::move(value)), replacement_(std::move(replacement)) {}

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

// 调用 list->set(index, newValue)，返回旧值（std::any 包装）
std::any setInList(const std::any& listAny, int index, const std::any& newValue) {
    if (listAny.type() == typeid(emf::common::EList<emf::common::EObject*>*)) {
        auto* list = std::any_cast<emf::common::EList<emf::common::EObject*>*>(listAny);
        if (!list) return std::any{};
        if (newValue.type() == typeid(emf::common::EObject*)) {
            auto* nv = std::any_cast<emf::common::EObject*>(newValue);
            auto* old = list->set(index, nv);
            return std::any{old};
        }
    } else if (listAny.type() == typeid(emf::common::EList<std::string>*)) {
        auto* list = std::any_cast<emf::common::EList<std::string>*>(listAny);
        if (!list) return std::any{};
        if (newValue.type() == typeid(std::string)) {
            auto nv = std::any_cast<std::string>(newValue);
            auto old = list->set(index, nv);
            return std::any{old};
        }
    }
    return std::any{};
}
}  // namespace

void ReplaceCommand::execute() {
    if (!owner_ || !feature_) return;

    auto listAny = owner_->eGet(feature_);

    // 首次执行：记录 value_ 的原始 index 并捕获 set 返回的旧值
    if (!executed_) {
        replacedIndex_ = findIndex(listAny, value_);
        if (replacedIndex_ >= 0) {
            oldValue_ = setInList(listAny, replacedIndex_, replacement_);
        }
    } else {
        // redo 路径：重新 set（不重新快照 oldValue_）
        if (replacedIndex_ >= 0) {
            setInList(listAny, replacedIndex_, replacement_);
        }
    }
    executed_ = true;
}

void ReplaceCommand::undo() {
    if (!owner_ || !feature_ || !executed_ || replacedIndex_ < 0) return;
    // 用 oldValue_ 恢复
    auto listAny = owner_->eGet(feature_);
    setInList(listAny, replacedIndex_, oldValue_);
}

void ReplaceCommand::redo() {
    if (!owner_ || !feature_ || !executed_ || replacedIndex_ < 0) return;
    // 重新 set 为 replacement_（不重新快照 oldValue_）
    auto listAny = owner_->eGet(feature_);
    setInList(listAny, replacedIndex_, replacement_);
}

}  // namespace emf::edit::command
