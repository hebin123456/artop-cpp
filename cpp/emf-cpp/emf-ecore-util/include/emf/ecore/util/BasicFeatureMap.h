// BasicFeatureMap.h
// 对齐 Java org.eclipse.emf.ecore.util.BasicFeatureMap (2639 行)
//
// BasicFeatureMap 是 FeatureMap 接口的默认实现。
// Java 内部核心：
//   - FeatureEList 维护 (Entry) 顺序
//   - BasicEMap<EStructuralFeature, Object> 索引到 wrapper
//   - 5 个嵌套 view 内部类：FeatureListIterator / FeatureIterator / FeatureFilter /
//     Wrapper / BasicEMapEntry
//
// 本 C++ 实现保留所有 public API 签名，并以类内嵌套类提供对应的 5 个 view。
#pragma once

#include "emf/common/EList.h"
#include "emf/common/EObject.h"
#include "emf/common/util/BasicEMap.h"
#include "emf/ecore/util/FeatureMap.h"

#include <any>
#include <functional>
#include <memory>
#include <stdexcept>
#include <string>
#include <unordered_map>
#include <vector>

namespace emf::ecore {
class EAttribute;
class EStructuralFeature;
}

namespace emf::ecore::util {

// =========================== BasicFeatureMap ============================
class BasicFeatureMap : public FeatureMap {
public:
    using EntryT = FeatureMap::Entry;

    // ====== 内部类：Wrapper（Java BasicFeatureMap.Wrapper） ======
    // 对齐 Java：BasicFeatureMap.Wrapper 实现了 InternalEObject（即 EObject 子接口）
    // 用作 FeatureMap 中的代理对象。
    // C++ 端 EObject 拆成 abstract interface + EObjectImpl concrete 后，
    // 继承 EObjectImpl 而不是 EObject 来获得默认实现。
    class Wrapper : public emf::common::EObjectImpl {
    public:
        Wrapper() = default;
        Wrapper(EStructuralFeature* feature, std::any value)
            : feature_(feature), value_(std::move(value)) {}

        // EObjectImpl::eClass() 是纯虚，Wrapper 作为代理对象返回 nullptr
        // （FeatureMap wrapper 不参与反射 eGet/eSet，无需真实 eClass）
        emf::ecore::EClass* eClass() const override { return nullptr; }

        emf::ecore::EStructuralFeature* getEStructuralFeature() const { return feature_; }
        void setEStructuralFeature(emf::ecore::EStructuralFeature* f) { feature_ = f; }
        const std::any& getValue() const { return value_; }
        void setValue(const std::any& v) { value_ = v; }
        std::any getFeature() const { return std::any{feature_}; }

    private:
        emf::ecore::EStructuralFeature* feature_ = nullptr;
        std::any value_;
    };

    // ====== 内部类：BasicEMapEntryImpl ======
    // 对应 Java BasicFeatureMap.BasicEMapEntry（继承 Entry implements Map.Entry）
    class BasicEMapEntryImpl : public FeatureMap::Entry {
    public:
        BasicEMapEntryImpl() = default;
        BasicEMapEntryImpl(int hash, FeatureMap::Entry* key, std::any value)
            : FeatureMap::Entry(key ? key->getEStructuralFeature() : nullptr, std::move(value)),
              ownerKey_(key), hash_(hash) {}

        int getHash() const { return hash_; }
        void setHash(int h) { hash_ = h; }

        std::any getFeature() const override {
            return ownerKey_ ? ownerKey_->getFeature() : std::any{};
        }
        int getFeatureID() const override {
            auto* sf = getEStructuralFeature();
            return sf ? sf->getFeatureID() : -1;
        }
        bool isGroup() const override { return false; }
        FeatureMap::Entry* ownerEntry() const { return ownerKey_; }
    private:
        FeatureMap::Entry* ownerKey_ = nullptr;
        int hash_ = 0;
    };

    // ====== 视图 1：FeatureListIterator（对齐 Java BasicFeatureMap.FeatureListIterator） ======
    class FeatureListIterator : public emf::common::EListIterator<EntryT*> {
    public:
        FeatureListIterator() = default;
        FeatureListIterator(BasicFeatureMap* map, bool reverse, int startIndex)
            : owner_(map), reverse_(reverse), cursor_(startIndex) {}

        bool hasNext() const override { return cursor_ < owner_->delegateCount(); }
        bool hasPrevious() const override { return cursor_ > 0; }
        EntryT* next() override {
            if (cursor_ >= owner_->delegateCount())
                throw std::out_of_range("FeatureListIterator: no next");
            EntryT* e = owner_->delegateGet(cursor_++);
            last_ = cursor_ - 1;
            return e;
        }
        EntryT* previous() override {
            if (cursor_ <= 0)
                throw std::out_of_range("FeatureListIterator: no previous");
            EntryT* e = owner_->delegateGet(--cursor_);
            last_ = cursor_;
            return e;
        }
        int nextIndex() const override { return cursor_; }
        int previousIndex() const override { return cursor_ - 1; }
        void remove() override {
            if (last_ < 0) throw std::runtime_error("FeatureListIterator: no last returned");
            owner_->delegateRemoveAt(last_);
            if (last_ < cursor_) --cursor_;
            last_ = -1;
        }
        void set(EntryT* e) override {
            if (last_ < 0) throw std::runtime_error("FeatureListIterator: no last returned");
            owner_->delegateSet(last_, e);
        }
        void add(EntryT* e) override {
            owner_->delegateAdd(cursor_++, e);
            last_ = -1;
        }
    private:
        BasicFeatureMap* owner_ = nullptr;
        bool reverse_ = false;
        int cursor_ = 0;
        int last_ = -1;
    };

    // ====== 视图 2：FeatureIterator（对齐 Java BasicFeatureMap.FeatureIterator） ======
    class FeatureIterator : public emf::common::EIterator<EntryT*> {
    public:
        FeatureIterator() = default;
        explicit FeatureIterator(BasicFeatureMap* map) : owner_(map), idx_(0) {}
        bool hasNext() const override { return idx_ < owner_->delegateCount(); }
        EntryT* next() override {
            if (idx_ >= owner_->delegateCount())
                throw std::out_of_range("FeatureIterator: no next");
            EntryT* e = owner_->delegateGet(idx_++);
            last_ = idx_ - 1;
            return e;
        }
        void remove() override {
            if (last_ < 0) throw std::runtime_error("FeatureIterator: no last returned");
            owner_->delegateRemoveAt(last_);
            if (last_ < idx_) --idx_;
            last_ = -1;
        }
    private:
        BasicFeatureMap* owner_ = nullptr;
        int idx_ = 0;
        int last_ = -1;
    };

    // ====== 视图 3：FeatureFilter（对齐 Java BasicFeatureMap.FeatureFilter） ======
    class FeatureFilter : public emf::common::EListFilter<EntryT*> {
    public:
        FeatureFilter() = default;
        explicit FeatureFilter(emf::ecore::EStructuralFeature* f) : feature_(f) {}
        bool accept(EntryT* entry) const override {
            if (!entry || !feature_) return true;
            return entry->getEStructuralFeature() == feature_;
        }
    private:
        emf::ecore::EStructuralFeature* feature_ = nullptr;
    };

    // ====== 视图 4：FeatureValuesView (per-feature values) ======
    // 仿照 Java BasicFeatureMap 的 feature.valueList 视图
    class FeatureValuesView {
    public:
        FeatureValuesView() = default;
        FeatureValuesView(BasicFeatureMap* map, emf::ecore::EStructuralFeature* f)
            : map_(map), feature_(f) {}
        int size() const;
        bool empty() const { return size() == 0; }
        std::any get(int index) const;
        std::any set(int index, const std::any& v);
        void add(const std::any& v);
        bool remove(const std::any& v);
        bool contains(const std::any& v) const;
        int indexOf(const std::any& v) const;
    private:
        BasicFeatureMap* map_ = nullptr;
        emf::ecore::EStructuralFeature* feature_ = nullptr;
    };

    // ====== 视图 5：FeatureEntriesView (per-feature entries) ======
    class FeatureEntriesView {
    public:
        FeatureEntriesView() = default;
        FeatureEntriesView(BasicFeatureMap* map, emf::ecore::EStructuralFeature* f)
            : map_(map), feature_(f) {}
        int size() const;
        EntryT* get(int index) const;
        EntryT* set(int index, EntryT* e);
        void add(EntryT* e);
    private:
        BasicFeatureMap* map_ = nullptr;
        emf::ecore::EStructuralFeature* feature_ = nullptr;
    };

    // ====== 内部类：Internal（Java BasicFeatureMap.Internal） ======
    class Internal {
    public:
        BasicFeatureMap* owner = nullptr;
        Internal() = default;
        explicit Internal(BasicFeatureMap* o) : owner(o) {}

        virtual ~Internal() = default;
        virtual bool isGroup(emf::ecore::EStructuralFeature* feature);
        virtual void setFeature(FeatureMap::Entry* entry, std::any value);
        virtual std::any getFeature(FeatureMap::Entry* entry, std::any value);
        virtual void validate(EntryT* entry) const {}
        virtual Wrapper* createWrapper(EntryT* entry) const;
        virtual Wrapper* createWrapper(emf::ecore::EStructuralFeature* feature, std::any value) const;
    };

    // ====================== BasicFeatureMap 主干 =======================
    BasicFeatureMap();
    ~BasicFeatureMap() override;

    // EList<Entry*> 实现
    size_t size() const override { return delegateCount(); }
    bool empty() const override { return delegateCount() == 0; }
    void clear() override;
    bool add(EntryT* e);
    bool add(std::any value);
    bool add(EStructuralFeature* feature, std::any value);
    bool add(EStructuralFeature* feature, int index, std::any value);
    bool remove(const EntryT* e);
    bool remove(const std::any& o);
    bool contains(const EntryT* e) const;
    bool contains(const std::any& o) const;
    int indexOf(const EntryT* e) const;
    int indexOf(const std::any& o) const;
    int lastIndexOf(const EntryT* e) const;
    std::unique_ptr<emf::common::EIterator<EntryT*>> iterator();
    std::unique_ptr<emf::common::EListIterator<EntryT*>> listIterator();
    std::unique_ptr<emf::common::EListIterator<EntryT*>> listIterator(int index);
    std::vector<EntryT*> toArray() const;

    // FeatureMap 视图
    std::vector<std::any> values(EStructuralFeature* feature) override;
    std::vector<FeatureMap::Entry*> entries(EStructuralFeature* feature) override;
    std::any get(EStructuralFeature* feature, int index) override;
    std::any set(EStructuralFeature* feature, int index, std::any value) override;
    int size(EStructuralFeature* feature) override;

    // 工厂
    virtual EntryT* createEntry(EStructuralFeature* feature, std::any value);
    virtual Wrapper* createWrapper(EStructuralFeature* feature, std::any value);
    virtual void validate(EStructuralFeature* feature, std::any value) const;
    virtual void didAdd(EntryT* entry) {}
    virtual void didRemove(EntryT* entry) {}
    virtual void didClear() {}

    Internal& internal() { return internal_; }
    const Internal& internal() const { return internal_; }

    // delegate 访问器（内部使用）
    int delegateCount() const { return static_cast<int>(entries_.size()); }
    EntryT* delegateGet(int index) const {
        if (index < 0 || static_cast<size_t>(index) >= entries_.size()) return nullptr;
        return entries_[static_cast<size_t>(index)];
    }
    void delegateAdd(int index, EntryT* e);
    void delegateSet(int index, EntryT* e);
    void delegateRemoveAt(int index);
    void delegateClear();

    // 内部：管理 entries_/featureIndex_
    std::vector<EntryT*>& delegateList() { return entries_; }
    const std::vector<EntryT*>& delegateList() const { return entries_; }
    emf::common::util::BasicEMap<EStructuralFeature*, std::vector<EntryT*>>& featureIndex() { return featureIndex_; }
    const emf::common::util::BasicEMap<EStructuralFeature*, std::vector<EntryT*>>& featureIndex() const { return featureIndex_; }

    // 视图访问
    FeatureValuesView valuesView(EStructuralFeature* feature) { return FeatureValuesView(this, feature); }
    FeatureEntriesView entriesView(EStructuralFeature* feature) { return FeatureEntriesView(this, feature); }

protected:
    // 顺序维护的 Entry 列表
    std::vector<EntryT*> entries_;
    // feature -> 该 feature 下的 entry 列表
    emf::common::util::BasicEMap<EStructuralFeature*, std::vector<EntryT*>> featureIndex_;
    Internal internal_;
};

}  // namespace emf::ecore::util
