// EMF Common: FeatureMap + FeatureMapEntry
//
// 对齐 org.eclipse.emf.ecore.util.FeatureMap (Java)
//
// FeatureMap 是 EMF 的核心机制，用于处理 volatile+transient+derived 的
// EStructuralFeature（即"扩展特性 / 包装元素 / mixed 容器"）。Java 端典型用法：
//   - ARTOP / XSD：每个 EObject 实例的 mixed 字段是个 EList<FeatureMap.Entry>
//   - FeatureMap.Entry 关联一个 EStructuralFeature（arPackages、adminData 等）
//     并持有值（EObject* 或 atomic）
//   - getXxx() 不直接读 xxx_ 字段，而是过滤 mixed 容器，按 feature 抽取条目
//   - setXxx(list) 同样：清空匹配 feature 的 mixed 条目 + 添加新条目
//
// 本文件实现：
//   - FeatureMapEntry：单条（feature 指针 + value + 可选嵌套 children）
//   - FeatureMap：EList-like wrapper，支持按 feature 过滤（list(feature)）
//
// FeatureMap 是普通值类型（与 EObject 树独立），不依赖 EObject 子类化。
// 业务 Impl 类（AUTOSAR、ARPackage 等）持有 FeatureMap 字段：mixed_，
// 并在 getArPackages()/setArPackages() 里操作 mixed_。
#pragma once

#include "emf/common/EList.h"
#include "emf/common/EObject.h"

#include <any>
#include <string>
#include <vector>

namespace emf::ecore {
class EStructuralFeature;
}  // namespace emf::ecore

namespace emf::common {

// ===== FeatureMapEntry =====
// 对齐 Java org.eclipse.emf.ecore.util.FeatureMap.Entry
//
// 单个 mixed 容器条目：含 feature 指针 + value。
// Java 端 FeatureMap.Entry.getEStructuralFeature() / getValue() / getType()
// / getGroup() / isProxy() 一一对应。
struct FeatureMapEntry {
    // 关联的 EStructuralFeature（如 arPackages、adminData）。
    // 强引用：feature 自身（EAttribute/EReference）由 EPackageRegistry 拥有。
    const emf::ecore::EStructuralFeature* feature = nullptr;

    // 值。EObject*（containment reference / non-containment reference）
    // 或 atomic value（EAttribute，如 EString / EInt / EBoolean）。
    std::any value;

    // 构造函数
    FeatureMapEntry() = default;
    explicit FeatureMapEntry(const emf::ecore::EStructuralFeature* f)
        : feature(f) {}
    FeatureMapEntry(const emf::ecore::EStructuralFeature* f, std::any v)
        : feature(f), value(std::move(v)) {}

    // ---- 对齐 Java FeatureMap.Entry API ----

    // 值是 EObject* 时返回之；否则返回 nullptr。
    emf::common::EObject* getValueAsObject() const;

    // 值是 atomic 类型时返回 std::any 副本；否则返回空 std::any。
    std::any getValue() const { return value; }

    // 值是 EObject* 且为 proxy → true。
    bool isProxy() const;

    // == 算子（让 EList<FeatureMapEntry>::remove 可用）：按 feature 指针相等判定
    bool operator==(const FeatureMapEntry& other) const {
        return feature == other.feature;
    }
    bool operator!=(const FeatureMapEntry& other) const {
        return !(*this == other);
    }
};

// ===== FeatureMap =====
// 对齐 Java org.eclipse.emf.ecore.util.FeatureMap
//
// 实际是 std::vector<FeatureMapEntry> 的简单 wrapper，加 list(feature) 过滤。
// 不维护单独的 sub-list 状态（Java 端 FeatureMap 的 list() 每次返回新 view）。
class FeatureMap {
public:
    using value_type = FeatureMapEntry;
    using container_type = std::vector<FeatureMapEntry>;
    using iterator = std::vector<FeatureMapEntry>::iterator;
    using const_iterator = std::vector<FeatureMapEntry>::const_iterator;

    FeatureMap() = default;

    // ---- EList 兼容 API（最小子集） ----
    size_t size() const { return entries_.size(); }
    bool empty() const { return entries_.empty(); }
    void clear() { entries_.clear(); }

    FeatureMapEntry& get(int index) { return entries_.at(index); }
    const FeatureMapEntry& get(int index) const { return entries_.at(index); }

    void add(FeatureMapEntry e) { entries_.push_back(std::move(e)); }
    void add(const emf::ecore::EStructuralFeature* feature, std::any value) {
        entries_.emplace_back(feature, std::move(value));
    }

    // 按索引删除
    void removeByIndex(int index) {
        if (index < 0 || static_cast<size_t>(index) >= entries_.size()) {
            throw std::out_of_range("FeatureMap::removeByIndex");
        }
        entries_.erase(entries_.begin() + index);
    }

    iterator begin() { return entries_.begin(); }
    iterator end() { return entries_.end(); }
    const_iterator begin() const { return entries_.begin(); }
    const_iterator end() const { return entries_.end(); }

    // ---- FeatureMap 专有 API：按 feature 过滤 ----
    // 返回 EList<FeatureMapEntry> 视图，仅含 feature 相等的条目。
    // 注意：返回的是**值拷贝视图**（独立的 std::vector），增删不会反向写回
    // 原 FeatureMap。Java 端 FeatureMap.Internal 返回 live view；C++ 端简化：
    // 调用方需要 back-push 时手动调 FeatureMap.add(entry)。
    EList<FeatureMapEntry> list(const emf::ecore::EStructuralFeature* feature) const;

    // ---- 辅助：按 feature 删除（清除所有 feature 匹配的条目） ----
    // 返回删除的条目数。
    int removeByFeature(const emf::ecore::EStructuralFeature* feature);

    // ---- 序列化辅助：检查是否有 feature 设置 ----
    bool hasFeature(const emf::ecore::EStructuralFeature* feature) const;

private:
    std::vector<FeatureMapEntry> entries_;
};

}  // namespace emf::common
