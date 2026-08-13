// ChangeDescription.h
// 对齐 Java: org.eclipse.emf.edit.command.ChangeDescription
// 状态: 框架骨架（最小可编译实现）
#pragma once

#include "emf/common/EObject.h"

#include <any>
#include <string>
#include <utility>
#include <vector>

namespace emf::common {
class EObject;
}

namespace emf::ecore {
class EStructuralFeature;
}

namespace emf::edit::command {

// FeatureChange：单个 feature 的变更记录（对齐 Java FeatureChange）
struct FeatureChange {
    emf::common::EObject* eObject = nullptr;
    emf::ecore::EStructuralFeature* feature = nullptr;
    std::any oldValue;
    std::any newValue;
};

// ChangeDescription：一次 command 执行所产生的变更集合（对齐 Java ChangeDescription）
class ChangeDescription {
public:
    ChangeDescription() = default;
    ~ChangeDescription() = default;

    void add(FeatureChange change) { changes_.push_back(std::move(change)); }
    const std::vector<FeatureChange>& getChanges() const { return changes_; }
    bool isEmpty() const { return changes_.empty(); }
    void clear() { changes_.clear(); }

    // 应用变更（对齐 Java ChangeDescription.apply）
    // 对每个 FeatureChange，eObject 的 feature 调 eSet(newValue)。
    // newValue 为空 any 时执行 eUnset（与 SetCommand.UNSET_VALUE 语义一致）。
    void apply() const {
        for (const auto& c : changes_) {
            if (c.eObject && c.feature) {
                if (c.newValue.has_value()) {
                    c.eObject->eSet(c.feature, c.newValue);
                } else {
                    c.eObject->eUnset(c.feature);
                }
            }
        }
    }

    // 应用并反转（对齐 Java ChangeDescription.applyAndReverse）
    // 先 apply 所有变更，然后把每个 FeatureChange 的 oldValue/newValue 互换，
    // 使本 ChangeDescription 描述反向变更（再 apply 即可撤销）。
    // 返回 *this 的拷贝（持有反转后的变更）。
    ChangeDescription applyAndReverse() {
        apply();
        for (auto& c : changes_) {
            std::swap(c.oldValue, c.newValue);
        }
        return *this;
    }

private:
    std::vector<FeatureChange> changes_;
};

}  // namespace emf::edit::command
