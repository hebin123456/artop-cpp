// BasicEMap.h
// 对齐 Java org.eclipse.emf.common.util.BasicEMap
// C++ 模板实现，模拟 Java 的 BasicEMap（高扩展性 map 容器）
#pragma once

#include "emf/common/util/EMap.h"
#include "emf/common/EList.h"

#include <algorithm>
#include <cstddef>
#include <functional>
#include <stdexcept>
#include <utility>
#include <vector>

namespace emf::common::util {

// BasicEMap<K, V>: 用 emf::common::EList<MapEntry<K, V>*> 作为 delegate，
// 同时提供 hash-bucket 加速 key 查找（与 Java 一致）。
// 模板参数 K/V 必须支持 std::hash 等价物（K 必须可哈希用于 containsKey；V 用于 containsValue）。
template <typename K, typename V>
class BasicEMap : public EMap<K, V> {
public:
    using Entry = MapEntry<K, V>;
    using EntryPtr = Entry*;
    using DelegateList = ::emf::common::EList<EntryPtr>;
    using Iterator = typename ::emf::common::EList<EntryPtr>::iterator;
    using ConstIterator = typename ::emf::common::EList<EntryPtr>::const_iterator;

    BasicEMap() {
        // EList 的 notifier 钩子：add 时 doPut，remove 时 doRemove，set 时 doSet（pair）
        // 新 EList API 用函数指针 + void* ctx（替代 3 个 std::function，节省内存）
        delegate_ = new DelegateList(&BasicEMap::elistCb, this, nullptr);
    }

    // 静态回调：将 void* ctx 转回 BasicEMap*，按事件类型分发到 doPut/doRemove
    static void elistCb(void* ctx, const ::emf::ecore::EStructuralFeature* /*feat*/,
                        ::emf::common::EListEvent ev, int /*pos*/,
                        EntryPtr oldV, EntryPtr newV) {
        auto* self = static_cast<BasicEMap*>(ctx);
        if (ev == ::emf::common::EListEvent::Add) {
            self->doPut(newV);
        } else if (ev == ::emf::common::EListEvent::Remove) {
            self->doRemove(oldV);
        } else {  // Set
            self->doRemove(oldV);
            self->doPut(newV);
        }
    }

    BasicEMap(int initialCapacity) : BasicEMap() {
        if (initialCapacity < 0) {
            throw std::invalid_argument("Illegal Capacity: " + std::to_string(initialCapacity));
        }
        if (initialCapacity > 0) {
            entryData_.resize(static_cast<std::size_t>(initialCapacity * 2 + 1));
        }
    }

    virtual ~BasicEMap() {
        // 清空 entries（释放内存）
        if (delegate_) {
            for (int i = 0; i < delegate_->size(); ++i) {
                delete delegate_->get(i);
            }
            delete delegate_;
        }
    }

    // ===== Map 语义接口 =====
    V get(const K& key) const override {
        int idx = indexOfKey(key);
        if (idx < 0) {
            return V{};
        }
        Entry* e = delegate_->get(idx);
        return resolve(key, e->value);
    }

    V put(const K& key, const V& value) override {
        int idx = indexOfKey(key);
        if (idx >= 0) {
            Entry* e = delegate_->get(idx);
            V oldVal = e->value;
            validateValue(value);  // 钩子；子类可重写抛异常
            e->value = value;
            didModify(e, oldVal);
            return oldVal;
        } else {
            validateKey(key);
            validateValue(value);
            int hash = hashOf(key);
            Entry* e = newEntry(hash, key, value);
            delegate_->add(e);
            didAdd(e);
            return V{};
        }
    }

    bool containsKey(const K& key) const override {
        return indexOfKey(key) >= 0;
    }

    bool containsValue(const V& value) const override {
        for (int i = 0, n = delegate_->size(); i < n; ++i) {
            const Entry* e = delegate_->get(i);
            if (value_equals(e->value, value)) return true;
        }
        return false;
    }

    V removeKey(const K& key) override {
        int idx = indexOfKey(key);
        if (idx < 0) return V{};
        Entry* e = delegate_->get(idx);
        V oldVal = e->value;
        delegate_->removeByIndex(idx);
        didRemove(e);
        delete e;
        return oldVal;
    }

    int indexOfKey(const K& key) const override {
        for (int i = 0, n = delegate_->size(); i < n; ++i) {
            const Entry* e = delegate_->get(i);
            if (key_equals(e->key, key)) return i;
        }
        return -1;
    }

    // ===== 视图（Java 风格） =====
    void forEachKey(const std::function<void(const K&)>& fn) const override {
        for (int i = 0, n = delegate_->size(); i < n; ++i) {
            const Entry* e = delegate_->get(i);
            fn(e->key);
        }
    }

    void forEachValue(const std::function<void(const V&)>& fn) const override {
        for (int i = 0, n = delegate_->size(); i < n; ++i) {
            const Entry* e = delegate_->get(i);
            fn(e->value);
        }
    }

    // ===== EList<Entry*> 接口 =====
    size_t size() const override { return static_cast<size_t>(delegate_->size()); }
    bool empty() const override { return delegate_->size() == 0; }
    void clear() override {
        std::vector<Entry*> oldEntries;
        for (int i = 0, n = delegate_->size(); i < n; ++i) {
            oldEntries.push_back(delegate_->get(i));
        }
        for (auto* e : oldEntries) {
            delegate_->removeByIndex(0);  // 每次删第一个；效率低但正确
            // delegate_->removeByIndex 会触发 doRemove
        }
        // 清空 hash buckets
        if (entryData_.size() > 0) {
            for (auto& bucket : entryData_) bucket.clear();
        }
        didClear(oldEntries);
    }

    // 继承自 EList<Entry*> 的 begin()/end() 由 delegate_ 提供。
    // 但因为我们用 unique_ptr-style ownership 模式，需要自定义 begin/end。
    Iterator begin() { return delegate_->begin(); }
    Iterator end()   { return delegate_->end(); }
    ConstIterator begin() const { return delegate_->begin(); }
    ConstIterator end() const   { return delegate_->end(); }

    Entry* get(int index) const { return delegate_->get(index); }

    // ===== 钩子（Java protected hooks） =====
    virtual void validateKey(const K& /*key*/) const { /* override to constrain */ }
    virtual void validateValue(const V& /*value*/) const { /* override to constrain */ }
    virtual V resolve(const K& /*key*/, const V& value) const { return value; }
    virtual Entry* newEntry(int /*hash*/, const K& key, const V& value) {
        return new Entry(key, value);
    }
    virtual void didAdd(Entry* /*entry*/) {}
    virtual void didModify(Entry* /*entry*/, const V& /*oldValue*/) {}
    virtual void didRemove(Entry* /*entry*/) {}
    virtual void didClear(const std::vector<Entry*>& /*oldEntries*/) {}

    // ===== 内部回调（来自 delegate notifier） =====
    void doPut(Entry* entry) {
        ensureEntryDataExists();
        int hash = entry->hashCode();
        int bucket = static_cast<int>(static_cast<unsigned int>(hash) % entryData_.size());
        for (auto* existing : entryData_[bucket]) {
            if (key_equals(existing->key, entry->key)) {
                existing->value = entry->value;
                return;
            }
        }
        entryData_[bucket].push_back(entry);
    }

    void doRemove(Entry* entry) {
        if (entryData_.empty()) return;
        int hash = entry->hashCode();
        int bucket = static_cast<int>(static_cast<unsigned int>(hash) % entryData_.size());
        auto& b = entryData_[bucket];
        auto it = std::find(b.begin(), b.end(), entry);
        if (it != b.end()) b.erase(it);
    }

    void doMove(Entry* /*entry*/) { /* hash bucket 不变 */ }

    // ===== 委托访问 =====
    DelegateList* delegate() const { return delegate_; }

    // 比较 / 哈希策略：默认 == 和 std::hash 等价。子类可 override
    virtual bool useEqualsForKey() const { return true; }
    virtual bool useEqualsForValue() const { return true; }

    // Java Map.getRef: 不存在则插入空 value 并返回 ref。
    // C++ 端用 ref（异常：key 不存在时返回 static 哨兵，注意不要长期持有）。
    V& getRef(const K& key) {
        for (int i = 0, n = delegate_->size(); i < n; ++i) {
            Entry* e = delegate_->get(i);
            if (key_equals(e->key, key)) return e->value;
        }
        // 不存在：插入空 value 并返回 ref
        Entry* e = new Entry(key, V{});
        delegate_->add(e);
        didAdd(e);
        return e->value;
    }

    // 只读访问：未命中返回 nullptr
    V* tryGetRef(const K& key) {
        for (int i = 0, n = delegate_->size(); i < n; ++i) {
            Entry* e = delegate_->get(i);
            if (key_equals(e->key, key)) return &e->value;
        }
        return nullptr;
    }

    const V* tryGetRef(const K& key) const {
        for (int i = 0, n = delegate_->size(); i < n; ++i) {
            const Entry* e = delegate_->get(i);
            if (key_equals(e->key, key)) return &e->value;
        }
        return nullptr;
    }

protected:
    DelegateList* delegate_ = nullptr;
    std::vector<std::vector<Entry*>> entryData_;  // hash buckets（懒构造）

    bool key_equals(const K& a, const K& b) const {
        if (useEqualsForKey()) return a == b;
        return &a == &b;
    }

    bool value_equals(const V& a, const V& b) const {
        if (useEqualsForValue()) {
            if constexpr (std::is_same_v<V, std::any>) {
                return MapEntry<K, V>::value_default_equals(a, b);
            } else {
                return a == b;
            }
        }
        return &a == &b;
    }

    int hashOf(const K& key) const {
        if constexpr (std::is_pointer_v<K>) {
            return key ? static_cast<int>(reinterpret_cast<std::uintptr_t>(key) & 0x7fffffff) : 0;
        } else if constexpr (std::is_arithmetic_v<K>) {
            return static_cast<int>(key);
        } else {
            // 简单 fallback：利用 std::hash（如果可用）；否则退化为 0
            return static_cast<int>(std::hash<K>{}(key) & 0x7fffffff);
        }
    }

    void ensureEntryDataExists() {
        if (entryData_.empty()) {
            int cap = 2 * size() + 1;
            if (cap < 8) cap = 8;
            entryData_.resize(static_cast<std::size_t>(cap));
            for (int i = 0, n = delegate_->size(); i < n; ++i) {
                doPut(delegate_->get(i));
            }
        }
    }
};

}  // namespace emf::common::util
