// OrderedFeatureMap.h
// 对齐 Java org.eclipse.sphinx.emf.ecore.OrderedFeatureMap
// 维护按 feature ID 升序排列的 FeatureMap entry
#pragma once

#include "emf/ecore/EcorePackage.h"
#include "emf/common/EObject.h"
#include <vector>
#include <functional>
#include <algorithm>

namespace emf::sphinx::ecore {

// 一条 entry：(feature, value, index)
struct FeatureMapEntry {
    emf::ecore::EStructuralFeature* feature = nullptr;
    emf::common::EObject* value = nullptr;  // 对齐 Java: InternalEObject + FeatureMap.Entry.value
    int index = -1;  // 同 feature 内的位置

    FeatureMapEntry() = default;
    FeatureMapEntry(emf::ecore::EStructuralFeature* f, emf::common::EObject* v, int i)
        : feature(f), value(v), index(i) {}
};

// 按 featureID 升序的 OrderedFeatureMap
// 对齐 Java: OrderedFeatureMap / BasicFeatureMap.Entry
class OrderedFeatureMap {
public:
    using OrderFn = std::function<int(const FeatureMapEntry&)>;

    OrderedFeatureMap() = default;
    ~OrderedFeatureMap() = default;

    // 默认 order：按 featureID
    static int defaultOrder(const FeatureMapEntry& e) {
        return e.feature ? e.feature->getFeatureID() : -1;
    }

    // 添加（按 order 升序插入，相同 order 时按 index 升序）
    void add(emf::ecore::EStructuralFeature* feature, emf::common::EObject* value, int index) {
        FeatureMapEntry e(feature, value, index);
        int o = defaultOrder(e);
        auto it = std::find_if(entries_.begin(), entries_.end(), [&](const FeatureMapEntry& other) {
            int oo = defaultOrder(other);
            if (o != oo) return oo > o;
            return other.index > index;
        });
        entries_.insert(it, e);
    }

    // 数量
    size_t size() const { return entries_.size(); }
    bool empty() const { return entries_.empty(); }

    // 访问
    const std::vector<FeatureMapEntry>& entries() const { return entries_; }

    void clear() { entries_.clear(); }

    // 按 feature 过滤
    std::vector<FeatureMapEntry> get(emf::ecore::EStructuralFeature* f) const {
        std::vector<FeatureMapEntry> r;
        for (auto& e : entries_) {
            if (e.feature == f) r.push_back(e);
        }
        return r;
    }

private:
    std::vector<FeatureMapEntry> entries_;
};

}  // namespace emf::sphinx::ecore
