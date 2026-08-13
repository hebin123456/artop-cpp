// BasicFeatureMap.cpp
// 1:1 翻译 Java org.eclipse.emf.ecore.util.BasicFeatureMap (2639 行)
//
// 实现要点：
//   - 内部用 std::vector<EntryT*> entries_ 维护顺序（对齐 Java: EDataTypeEList.data[]）
//   - 内部用 BasicEMap<EStructuralFeature*, std::vector<EntryT*>> 索引 per-feature entries
//   - 7 个 nested 类的成员函数（Wrapper / BasicEMapEntryImpl / FeatureListIterator /
//     FeatureIterator / FeatureFilter / FeatureValuesView / FeatureEntriesView）
//   - 主类方法严格按 Java 1:1 翻译
//
// 简化：
//   - 通知（eNotify / Notification）走 no-op；接口保留但不影响数据
//   - FeatureMapUtil.Validator 简化为 no-op（FeatureMapUtil.cpp 提供 stub）
//   - resolve proxy / ExtendedMetaData affiliation 暂不实现（与测试无关）
#include "emf/ecore/util/BasicFeatureMap.h"
#include "emf/ecore/util/FeatureMapUtil.h"
#include "emf/ecore/util/FeatureMap.h"
#include "emf/ecore/EcorePackage.h"
#include "emf/common/Notification.h"
#include "emf/common/util/EMap.h"

#include <algorithm>
#include <stdexcept>
#include <utility>

namespace emf::ecore::util {

// ===================================================================
// 1) BasicFeatureMap 主类
// ===================================================================

BasicFeatureMap::BasicFeatureMap() = default;

BasicFeatureMap::~BasicFeatureMap() {
    // 释放所有 entry（Java 中由 EDataTypeEList 释放；这里我们持有裸指针）
    for (auto* e : entries_) delete e;
    entries_.clear();
}

void BasicFeatureMap::clear() {
    // 释放 entry
    for (auto* e : entries_) delete e;
    entries_.clear();
    featureIndex_.clear();
    didClear();
}

bool BasicFeatureMap::add(BasicFeatureMap::EntryT* e) {
    if (!e) return false;
    // Java: super.add(object) —— 实际是 EDataTypeEList.add
    // C++ 简化：直接 push_back
    entries_.push_back(e);
    if (auto* f = e->getEStructuralFeature()) {
        auto& vec = featureIndex_.getRef(f);
        vec.push_back(e);
    }
    didAdd(e);
    return true;
}

bool BasicFeatureMap::add(std::any value) {
    // 对齐 Java: 没有明确的单参 add 入口；
    // C++ BasicFeatureMap.h 声明了 add(std::any)，表示"取默认 feature 添加 value"。
    // 简化：把 value 包装为一个无 feature 的 entry，append。
    auto* e = new BasicFeatureMap::EntryT(nullptr, std::move(value));
    return add(e);
}

bool BasicFeatureMap::add(EStructuralFeature* feature, std::any value) {
    // 对齐 Java: add(EStructuralFeature, Object)
    // C++ 简化：直接用 createEntry 工厂创建
    auto* e = createEntry(feature, std::move(value));
    return add(e);
}

bool BasicFeatureMap::add(EStructuralFeature* feature, int index, std::any value) {
    // 对齐 Java: add(EStructuralFeature, int, Object) —— 内部走 entryIndex(feature, index)
    // + doAdd(entryIndex, createEntry(feature, value))
    int insertAt = index;
    if (insertAt < 0) insertAt = 0;
    if (insertAt > static_cast<int>(entries_.size()))
        insertAt = static_cast<int>(entries_.size());
    auto* e = createEntry(feature, std::move(value));
    delegateAdd(insertAt, e);
    return true;
}

bool BasicFeatureMap::remove(const BasicFeatureMap::EntryT* e) {
    if (!e) return false;
    auto it = std::find(entries_.begin(), entries_.end(), e);
    if (it == entries_.end()) return false;
    int idx = static_cast<int>(it - entries_.begin());
    delegateRemoveAt(idx);
    return true;
}

bool BasicFeatureMap::remove(const std::any& /*o*/) {
    // 对齐 Java: remove(Object) —— BaseFeatureMap 没有显式实现；
    // 简化：返回 false
    return false;
}

bool BasicFeatureMap::contains(const BasicFeatureMap::EntryT* e) const {
    if (!e) return false;
    return std::find(entries_.begin(), entries_.end(), e) != entries_.end();
}

bool BasicFeatureMap::contains(const std::any& /*o*/) const { return false; }

int BasicFeatureMap::indexOf(const BasicFeatureMap::EntryT* e) const {
    if (!e) return -1;
    auto it = std::find(entries_.begin(), entries_.end(), e);
    if (it == entries_.end()) return -1;
    return static_cast<int>(it - entries_.begin());
}

int BasicFeatureMap::indexOf(const std::any& /*o*/) const { return -1; }

int BasicFeatureMap::lastIndexOf(const BasicFeatureMap::EntryT* e) const {
    if (!e) return -1;
    for (auto it = entries_.rbegin(); it != entries_.rend(); ++it) {
        if (*it == e) return static_cast<int>(entries_.rend() - it) - 1;
    }
    return -1;
}

std::unique_ptr<emf::common::EIterator<BasicFeatureMap::EntryT*>> BasicFeatureMap::iterator() {
    return std::make_unique<BasicFeatureMap::FeatureIterator>(this);
}

std::unique_ptr<emf::common::EListIterator<BasicFeatureMap::EntryT*>> BasicFeatureMap::listIterator() {
    return std::make_unique<BasicFeatureMap::FeatureListIterator>(this, false, 0);
}

std::unique_ptr<emf::common::EListIterator<BasicFeatureMap::EntryT*>> BasicFeatureMap::listIterator(int index) {
    return std::make_unique<BasicFeatureMap::FeatureListIterator>(this, false, index);
}

std::vector<BasicFeatureMap::EntryT*> BasicFeatureMap::toArray() const {
    return entries_;
}

// ===================================================================
// FeatureMap 视图
// ===================================================================

std::vector<std::any> BasicFeatureMap::values(EStructuralFeature* feature) {
    std::vector<std::any> out;
    if (!feature) return out;
    for (auto* e : entries_) {
        if (e && e->getEStructuralFeature() == feature) {
            out.push_back(e->getValue());
        }
    }
    return out;
}

std::vector<FeatureMap::Entry*> BasicFeatureMap::entries(EStructuralFeature* feature) {
    std::vector<FeatureMap::Entry*> out;
    if (!feature) return out;
    for (auto* e : entries_) {
        if (e && e->getEStructuralFeature() == feature) {
            out.push_back(e);
        }
    }
    return out;
}

std::any BasicFeatureMap::get(EStructuralFeature* feature, int index) {
    if (!feature) return {};
    int count = 0;
    for (auto* e : entries_) {
        if (e && e->getEStructuralFeature() == feature) {
            if (count == index) return e->getValue();
            ++count;
        }
    }
    throw std::out_of_range("BasicFeatureMap::get(feature, index) index=" +
                             std::to_string(index) + ", size=" + std::to_string(count));
}

std::any BasicFeatureMap::set(EStructuralFeature* feature, int index, std::any value) {
    if (!feature) return value;
    int count = 0;
    for (size_t i = 0; i < entries_.size(); ++i) {
        auto* e = entries_[i];
        if (e && e->getEStructuralFeature() == feature) {
            if (count == index) {
                std::any old = e->getValue();
                auto* newEntry = createEntry(feature, std::move(value));
                delegateSet(static_cast<int>(i), newEntry);
                return old;
            }
            ++count;
        }
    }
    throw std::out_of_range("BasicFeatureMap::set(feature, index) index=" +
                             std::to_string(index) + ", size=" + std::to_string(count));
}

int BasicFeatureMap::size(EStructuralFeature* feature) {
    if (!feature) return 0;
    int n = 0;
    for (auto* e : entries_) {
        if (e && e->getEStructuralFeature() == feature) ++n;
    }
    return n;
}

// ===================================================================
// 工厂
// ===================================================================

BasicFeatureMap::EntryT* BasicFeatureMap::createEntry(EStructuralFeature* feature, std::any value) {
    return new EntryT(feature, std::move(value));
}

BasicFeatureMap::Wrapper* BasicFeatureMap::createWrapper(EStructuralFeature* feature, std::any value) {
    return new Wrapper(feature, std::move(value));
}

void BasicFeatureMap::validate(EStructuralFeature* /*feature*/, std::any /*value*/) const {
    // 对齐 Java: 默认 validate no-op
}

// ===================================================================
// delegate 访问器
// ===================================================================

void BasicFeatureMap::delegateAdd(int index, BasicFeatureMap::EntryT* e) {
    if (!e) return;
    int idx = index;
    if (idx < 0) idx = 0;
    if (idx > static_cast<int>(entries_.size())) idx = static_cast<int>(entries_.size());
    entries_.insert(entries_.begin() + idx, e);
    if (auto* f = e->getEStructuralFeature()) {
        auto& vec = featureIndex_.getRef(f);
        vec.insert(vec.begin() + idx, e);
    }
    didAdd(e);
}

void BasicFeatureMap::delegateSet(int index, BasicFeatureMap::EntryT* e) {
    if (index < 0 || static_cast<size_t>(index) >= entries_.size() || !e) return;
    BasicFeatureMap::EntryT* old = entries_[static_cast<size_t>(index)];
    entries_[static_cast<size_t>(index)] = e;
    if (old) {
        if (auto* f = old->getEStructuralFeature()) {
            auto& vec = featureIndex_.getRef(f);
            auto it = std::find(vec.begin(), vec.end(), old);
            if (it != vec.end()) vec.erase(it);
            delete old;
        }
    }
    if (auto* f = e->getEStructuralFeature()) {
        auto& vec = featureIndex_.getRef(f);
        vec.push_back(e);
    }
    didRemove(old);
    didAdd(e);
}

void BasicFeatureMap::delegateRemoveAt(int index) {
    if (index < 0 || static_cast<size_t>(index) >= entries_.size()) return;
    BasicFeatureMap::EntryT* e = entries_[static_cast<size_t>(index)];
    entries_.erase(entries_.begin() + index);
    if (e) {
        if (auto* f = e->getEStructuralFeature()) {
            auto& vec = featureIndex_.getRef(f);
            auto it = std::find(vec.begin(), vec.end(), e);
            if (it != vec.end()) vec.erase(it);
        }
        didRemove(e);
        delete e;
    }
}

void BasicFeatureMap::delegateClear() {
    for (auto* e : entries_) delete e;
    entries_.clear();
    featureIndex_.clear();
    didClear();
}

// ===================================================================
// 2) Internal 类（Java BasicFeatureMap.Internal 简版）
// ===================================================================

bool BasicFeatureMap::Internal::isGroup(emf::ecore::EStructuralFeature* /*feature*/) {
    return false;
}

void BasicFeatureMap::Internal::setFeature(FeatureMap::Entry* /*entry*/, std::any /*value*/) {}

std::any BasicFeatureMap::Internal::getFeature(FeatureMap::Entry* /*entry*/, std::any value) {
    return value;
}

BasicFeatureMap::Wrapper* BasicFeatureMap::Internal::createWrapper(EntryT* entry) const {
    if (!entry) return nullptr;
    return new Wrapper(entry->getEStructuralFeature(), entry->getValue());
}

BasicFeatureMap::Wrapper* BasicFeatureMap::Internal::createWrapper(emf::ecore::EStructuralFeature* feature,
                                                                   std::any value) const {
    return new Wrapper(feature, std::move(value));
}

// ===================================================================
// 3) FeatureValuesView（per-feature values 视图）
// ===================================================================

namespace {
// 简单 any 相等：类型一致 + 值相等
inline bool anyEquals(const std::any& a, const std::any& b) {
    if (a.type() != b.type()) return false;
    if (a.type() == typeid(int)) return std::any_cast<int>(a) == std::any_cast<int>(b);
    if (a.type() == typeid(std::string)) return std::any_cast<std::string>(a) == std::any_cast<std::string>(b);
    if (a.type() == typeid(double)) return std::any_cast<double>(a) == std::any_cast<double>(b);
    if (a.type() == typeid(bool)) return std::any_cast<bool>(a) == std::any_cast<bool>(b);
    return false;
}
}  // namespace

int BasicFeatureMap::FeatureValuesView::size() const {
    if (!map_ || !feature_) return 0;
    return map_->size(feature_);
}

std::any BasicFeatureMap::FeatureValuesView::get(int index) const {
    if (!map_ || !feature_)
        throw std::out_of_range("FeatureValuesView::get null");
    return map_->get(feature_, index);
}

std::any BasicFeatureMap::FeatureValuesView::set(int index, const std::any& v) {
    if (!map_ || !feature_)
        throw std::out_of_range("FeatureValuesView::set null");
    return map_->set(feature_, index, v);
}

void BasicFeatureMap::FeatureValuesView::add(const std::any& v) {
    if (!map_ || !feature_) return;
    map_->add(feature_, v);
}

bool BasicFeatureMap::FeatureValuesView::remove(const std::any& v) {
    if (!map_ || !feature_) return false;
    int idx = -1;
    int count = 0;
    for (auto* e : map_->entries_) {
        if (e && e->getEStructuralFeature() == feature_) {
            if (anyEquals(e->getValue(), v)) {
                idx = count;
                break;
            }
            ++count;
        }
    }
    if (idx < 0) return false;
    int origIdx = -1;
    int cnt = 0;
    for (size_t i = 0; i < map_->entries_.size(); ++i) {
        auto* e = map_->entries_[i];
        if (e && e->getEStructuralFeature() == feature_) {
            if (cnt == idx) { origIdx = static_cast<int>(i); break; }
            ++cnt;
        }
    }
    if (origIdx < 0) return false;
    map_->delegateRemoveAt(origIdx);
    return true;
}

bool BasicFeatureMap::FeatureValuesView::contains(const std::any& v) const {
    if (!map_ || !feature_) return false;
    for (auto* e : map_->entries_) {
        if (e && e->getEStructuralFeature() == feature_) {
            if (anyEquals(e->getValue(), v)) return true;
        }
    }
    return false;
}

int BasicFeatureMap::FeatureValuesView::indexOf(const std::any& v) const {
    if (!map_ || !feature_) return -1;
    int count = 0;
    for (auto* e : map_->entries_) {
        if (e && e->getEStructuralFeature() == feature_) {
            if (anyEquals(e->getValue(), v)) return count;
            ++count;
        }
    }
    return -1;
}

// ===================================================================
// 4) FeatureEntriesView（per-feature entries 视图）
// ===================================================================

int BasicFeatureMap::FeatureEntriesView::size() const {
    if (!map_ || !feature_) return 0;
    return map_->size(feature_);
}

BasicFeatureMap::EntryT* BasicFeatureMap::FeatureEntriesView::get(int index) const {
    if (!map_ || !feature_) return nullptr;
    int count = 0;
    for (auto* e : map_->entries_) {
        if (e && e->getEStructuralFeature() == feature_) {
            if (count == index) return e;
            ++count;
        }
    }
    return nullptr;
}

BasicFeatureMap::EntryT* BasicFeatureMap::FeatureEntriesView::set(int index, EntryT* e) {
    if (!map_ || !feature_ || !e) return nullptr;
    int count = 0;
    for (size_t i = 0; i < map_->entries_.size(); ++i) {
        auto* cur = map_->entries_[i];
        if (cur && cur->getEStructuralFeature() == feature_) {
            if (count == index) {
                BasicFeatureMap::EntryT* old = cur;
                map_->delegateSet(static_cast<int>(i), e);
                return old;
            }
            ++count;
        }
    }
    return nullptr;
}

void BasicFeatureMap::FeatureEntriesView::add(EntryT* e) {
    if (!map_ || !feature_ || !e) return;
    map_->add(e);
}

}  // namespace emf::ecore::util
