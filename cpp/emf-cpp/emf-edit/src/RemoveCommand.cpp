// RemoveCommand.cpp
// 对齐 Java: org.eclipse.emf.edit.command.RemoveCommand
#include "emf/edit/command/RemoveCommand.h"

#include "emf/common/EList.h"
#include "emf/ecore/EcoreImpls.h"

#include <algorithm>

namespace emf::edit::command {

RemoveCommand::RemoveCommand(emf::edit::EditingDomain* domain,
                             emf::common::EObject* owner,
                             emf::ecore::EStructuralFeature* feature,
                             std::any value)
    : domain_(domain), owner_(owner), feature_(feature) {
    if (value.has_value()) values_.push_back(std::move(value));
}

RemoveCommand::RemoveCommand(emf::edit::EditingDomain* domain,
                             emf::common::EObject* owner,
                             emf::ecore::EStructuralFeature* feature,
                             std::vector<std::any> collection)
    : domain_(domain), owner_(owner), feature_(feature), values_(std::move(collection)) {}

void RemoveCommand::execute() {
    if (!owner_ || !feature_ || values_.empty()) return;

    // 首次执行：清空 removed_ 准备记录
    if (!executed_) {
        removed_.clear();
    }

    auto listAny = owner_->eGet(feature_);

    if (listAny.type() == typeid(emf::common::EList<emf::common::EObject*>*)) {
        auto* list = std::any_cast<emf::common::EList<emf::common::EObject*>*>(listAny);
        if (list) {
            for (const auto& v : values_) {
                if (v.type() == typeid(emf::common::EObject*)) {
                    auto* target = std::any_cast<emf::common::EObject*>(v);
                    int idx = list->indexOf(target);
                    if (idx >= 0) {
                        auto* removed = list->removeByIndex(idx);
                        if (!executed_) {
                            removed_.emplace_back(idx, std::any{removed});
                        }
                    }
                }
            }
        }
    } else if (listAny.type() == typeid(emf::common::EList<std::string>*)) {
        auto* list = std::any_cast<emf::common::EList<std::string>*>(listAny);
        if (list) {
            for (const auto& v : values_) {
                if (v.type() == typeid(std::string)) {
                    auto target = std::any_cast<std::string>(v);
                    int idx = list->indexOf(target);
                    if (idx >= 0) {
                        auto removed = list->removeByIndex(idx);
                        if (!executed_) {
                            removed_.emplace_back(idx, std::any{removed});
                        }
                    }
                }
            }
        }
    }
    executed_ = true;
}

void RemoveCommand::undo() {
    if (!owner_ || !feature_ || !executed_) return;

    auto listAny = owner_->eGet(feature_);

    // 按 index 升序重新插入（保证每次插入位置都对齐原始索引）
    auto sorted = removed_;
    std::sort(sorted.begin(), sorted.end(),
              [](const std::pair<int, std::any>& a, const std::pair<int, std::any>& b) {
                  return a.first < b.first;
              });

    if (listAny.type() == typeid(emf::common::EList<emf::common::EObject*>*)) {
        auto* list = std::any_cast<emf::common::EList<emf::common::EObject*>*>(listAny);
        if (!list) return;
        for (const auto& [idx, v] : sorted) {
            if (v.type() == typeid(emf::common::EObject*)) {
                auto* o = std::any_cast<emf::common::EObject*>(v);
                // EList 仅有 add(value)（末尾追加），需 add 后 move 到目标 idx
                list->add(o);
                int endIdx = static_cast<int>(list->size()) - 1;
                if (endIdx != idx) {
                    list->move(idx, endIdx);
                }
            }
        }
    } else if (listAny.type() == typeid(emf::common::EList<std::string>*)) {
        auto* list = std::any_cast<emf::common::EList<std::string>*>(listAny);
        if (!list) return;
        for (const auto& [idx, v] : sorted) {
            if (v.type() == typeid(std::string)) {
                auto s = std::any_cast<std::string>(v);
                list->add(s);
                int endIdx = static_cast<int>(list->size()) - 1;
                if (endIdx != idx) {
                    list->move(idx, endIdx);
                }
            }
        }
    }
}

void RemoveCommand::redo() {
    if (!owner_ || !feature_ || !executed_ || values_.empty()) return;
    // redo：重新移除 values_，不重新记录 removed_（executed_ 已为 true）。
    auto listAny = owner_->eGet(feature_);
    if (listAny.type() == typeid(emf::common::EList<emf::common::EObject*>*)) {
        auto* list = std::any_cast<emf::common::EList<emf::common::EObject*>*>(listAny);
        if (!list) return;
        for (const auto& v : values_) {
            if (v.type() == typeid(emf::common::EObject*)) {
                auto* target = std::any_cast<emf::common::EObject*>(v);
                int idx = list->indexOf(target);
                if (idx >= 0) {
                    list->removeByIndex(idx);
                }
            }
        }
    } else if (listAny.type() == typeid(emf::common::EList<std::string>*)) {
        auto* list = std::any_cast<emf::common::EList<std::string>*>(listAny);
        if (!list) return;
        for (const auto& v : values_) {
            if (v.type() == typeid(std::string)) {
                auto target = std::any_cast<std::string>(v);
                int idx = list->indexOf(target);
                if (idx >= 0) {
                    list->removeByIndex(idx);
                }
            }
        }
    }
}

}  // namespace emf::edit::command
