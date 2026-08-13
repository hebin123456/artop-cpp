// AddCommand.cpp
// 对齐 Java: org.eclipse.emf.edit.command.AddCommand
#include "emf/edit/command/AddCommand.h"

#include "emf/common/EList.h"
#include "emf/ecore/EcoreImpls.h"

namespace emf::edit::command {

AddCommand::AddCommand(emf::edit::EditingDomain* domain,
                       emf::common::EObject* owner,
                       emf::ecore::EStructuralFeature* feature,
                       std::any value)
    : domain_(domain), owner_(owner), feature_(feature) {
    if (value.has_value()) values_.push_back(std::move(value));
}

AddCommand::AddCommand(emf::edit::EditingDomain* domain,
                       emf::common::EObject* owner,
                       emf::ecore::EStructuralFeature* feature,
                       std::vector<std::any> collection)
    : domain_(domain), owner_(owner), feature_(feature), values_(std::move(collection)) {}

// 内部辅助：取 list 当前 size（不支持类型返回 0）
namespace {
size_t listSize(const std::any& listAny) {
    if (listAny.type() == typeid(emf::common::EList<emf::common::EObject*>*)) {
        auto* list = std::any_cast<emf::common::EList<emf::common::EObject*>*>(listAny);
        return list ? list->size() : 0;
    }
    if (listAny.type() == typeid(emf::common::EList<std::string>*)) {
        auto* list = std::any_cast<emf::common::EList<std::string>*>(listAny);
        return list ? list->size() : 0;
    }
    return 0;
}

// 内部辅助：将 values_ 追加到 list（按 any 内部类型分发）
void appendValues(const std::any& listAny, const std::vector<std::any>& values) {
    if (listAny.type() == typeid(emf::common::EList<emf::common::EObject*>*)) {
        auto* list = std::any_cast<emf::common::EList<emf::common::EObject*>*>(listAny);
        if (!list) return;
        for (const auto& v : values) {
            if (v.type() == typeid(emf::common::EObject*)) {
                list->add(std::any_cast<emf::common::EObject*>(v));
            }
        }
    } else if (listAny.type() == typeid(emf::common::EList<std::string>*)) {
        auto* list = std::any_cast<emf::common::EList<std::string>*>(listAny);
        if (!list) return;
        for (const auto& v : values) {
            if (v.type() == typeid(std::string)) {
                list->add(std::any_cast<std::string>(v));
            }
        }
    }
}

// 内部辅助：从 list 末尾移除到 size == targetSize
void truncateTo(const std::any& listAny, size_t targetSize) {
    if (listAny.type() == typeid(emf::common::EList<emf::common::EObject*>*)) {
        auto* list = std::any_cast<emf::common::EList<emf::common::EObject*>*>(listAny);
        if (!list) return;
        while (list->size() > targetSize) {
            list->removeByIndex(static_cast<int>(list->size()) - 1);
        }
    } else if (listAny.type() == typeid(emf::common::EList<std::string>*)) {
        auto* list = std::any_cast<emf::common::EList<std::string>*>(listAny);
        if (!list) return;
        while (list->size() > targetSize) {
            list->removeByIndex(static_cast<int>(list->size()) - 1);
        }
    }
}
}  // namespace

void AddCommand::execute() {
    if (!owner_ || !feature_ || values_.empty()) return;

    auto listAny = owner_->eGet(feature_);

    // 首次执行：记录 oldSize 用于 undo
    if (!executed_) {
        oldSize_ = listSize(listAny);
    }

    // 追加 values_ 到 list（同时是 redo 路径）
    appendValues(listAny, values_);
    executed_ = true;
}

void AddCommand::undo() {
    if (!owner_ || !feature_ || !executed_) return;
    // 移除从 oldSize_ 到末尾的元素（即本次添加的元素）
    auto listAny = owner_->eGet(feature_);
    truncateTo(listAny, oldSize_);
}

void AddCommand::redo() {
    if (!owner_ || !feature_ || !executed_ || values_.empty()) return;
    // 重新追加 values_（executed_ 已为 true，不重新快照 oldSize_）
    auto listAny = owner_->eGet(feature_);
    appendValues(listAny, values_);
}

}  // namespace emf::edit::command
