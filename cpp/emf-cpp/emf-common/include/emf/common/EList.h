// EMF Common: EList
// 对齐 org.eclipse.emf.common.util.EList (Java)
#pragma once

#include <vector>
#include <functional>
#include <stdexcept>
#include <algorithm>
#include <memory>
#include <cstddef>
#include <type_traits>

// 前向声明（必须在 emf::common 命名空间外，否则会创建 emf::common::emf::ecore 影子命名空间）
namespace emf::ecore { class EStructuralFeature; }

namespace emf::common {

// 前向声明
class EObject;
class EAdapter;
class Notification;

// EList 事件类型（add/remove/set），用于轻量级函数指针回调。
// 定义在命名空间作用域（非模板内部），使回调签名不依赖 T。
enum class EListEvent { Add, Remove, Set };

// EList.Filter 接口（Java EList.Filter 内部接口）
template <typename T>
class EListFilter {
public:
    virtual ~EListFilter() = default;
    virtual bool accept(T /*entry*/) const = 0;
};

// EIterator 接口（Java EIterator）
template <typename T>
class EIterator {
public:
    virtual ~EIterator() = default;
    virtual bool hasNext() const = 0;
    virtual T next() = 0;
    virtual void remove() { throw std::runtime_error("EIterator: remove not supported"); }
};

// EListIterator 接口（Java EListIterator extends EIterator + ListIterator）
template <typename T>
class EListIterator : public EIterator<T> {
public:
    ~EListIterator() override = default;
    virtual int nextIndex() const = 0;
    virtual int previousIndex() const = 0;
    virtual bool hasPrevious() const = 0;
    virtual T previous() = 0;
    virtual void set(T /*value*/) { throw std::runtime_error("EListIterator: set not supported"); }
    virtual void add(T /*value*/) { throw std::runtime_error("EListIterator: add not supported"); }
};

template <typename T>
class EList {
public:
    using value_type = T;
    using container_type = std::vector<T>;
    using iterator = typename container_type::iterator;
    using const_iterator = typename container_type::const_iterator;

    // 轻量级回调（替代 3 个 std::function，节省内存）：
    // 单个函数指针 + 共享 context 处理 add/remove/set 三种事件。
    //   ctx  通常为 owner EObject*（codegen 路径），由调用方解释
    //   feat 为 EStructuralFeature*（用于构造 Notification）
    //   cb_ == nullptr 表示无回调（默认构造 / DynamicEObject 路径）
    // 内存：1 函数指针 + 1 void* + 1 feat* = 24 字节（原 3×std::function ≈ 96 字节，-75%）
    using CallbackFn = void(*)(void* ctx, const emf::ecore::EStructuralFeature* feat,
                               EListEvent ev, int pos, T oldValue, T newValue);

    EList() = default;
    EList(CallbackFn cb, void* ctx, const emf::ecore::EStructuralFeature* feat)
        : cb_(cb), cbCtx_(ctx), cbFeat_(feat) {}

    virtual size_t size() const { return data_.size(); }
    virtual bool empty() const { return data_.empty(); }

    virtual void clear() {
        // 借助 removeByIndex 避免与按值删除歧义（当 T=int 时）
        while (!data_.empty()) {
            removeByIndex(static_cast<int>(size()) - 1);
        }
    }

    T& get(size_t index) { return data_.at(index); }
    const T& get(size_t index) const { return data_.at(index); }
    T& operator[](size_t index) { return data_[index]; }
    const T& operator[](size_t index) const { return data_[index]; }

    virtual void add(T value) {
        data_.push_back(value);
        if (cb_) cb_(cbCtx_, cbFeat_, EListEvent::Add, static_cast<int>(data_.size()) - 1, T{}, std::move(value));
    }

    void addUnique(T value) {
        if (std::find(data_.begin(), data_.end(), value) == data_.end()) {
            add(std::move(value));
        }
    }

    // 按索引删除：使用独立名称避免与按值删除歧义
    T removeByIndex(int index) {
        if (index < 0 || static_cast<size_t>(index) >= data_.size()) throw std::out_of_range("EList.removeByIndex");
        T old = std::move(data_[index]);
        data_.erase(data_.begin() + index);
        if (cb_) cb_(cbCtx_, cbFeat_, EListEvent::Remove, index, old, T{});
        return old;
    }

    // 按索引删除（仅在 T 不是整数时启用，避免与按值删除歧义）
    template <typename U = T,
              typename = typename std::enable_if<!std::is_integral<U>::value || !std::is_same<U, int>::value>::type>
    T remove(int index) {
        return removeByIndex(index);
    }

    // 按值删除
    virtual bool remove(const T& value) {
        auto it = std::find(data_.begin(), data_.end(), value);
        if (it == data_.end()) return false;
        auto idx = static_cast<int>(it - data_.begin());
        removeByIndex(idx);
        return true;
    }

    T set(int index, T newValue) {
        if (index < 0 || static_cast<size_t>(index) >= data_.size()) throw std::out_of_range("EList.set");
        T old = std::move(data_[index]);
        data_[index] = newValue;
        if (cb_) cb_(cbCtx_, cbFeat_, EListEvent::Set, index, old, std::move(newValue));
        return old;
    }

    // addAll：批量添加。对齐 Java EList.addAll。
    // 注意：回调走逐元素 ADD（非 ADD_MANY），因为 CallbackFn 签名为单值。
    // 完整 ADD_MANY 支持需 codegen 改用 EcoreEList/NotifyingListImpl（见 NotifyingListImpl.h）。
    virtual bool addAll(const std::vector<T>& collection) {
        if (collection.empty()) return false;
        for (const auto& v : collection) add(v);
        return true;
    }

    // move：元素移位。对齐 Java EList.move(targetIndex, sourceIndex)。
    // 通知走 SET（position=sourceIndex，oldValue=移动元素，newValue=移动元素），
    // 对齐 NotifyingListImpl::move 的 MOVE 事件需 EcoreEList，此处用 SET 近似。
    virtual void move(int targetIndex, int sourceIndex) {
        if (targetIndex < 0 || static_cast<size_t>(targetIndex) >= data_.size() ||
            sourceIndex < 0 || static_cast<size_t>(sourceIndex) >= data_.size()) {
            throw std::out_of_range("EList.move");
        }
        if (targetIndex == sourceIndex) return;
        T tmp = std::move(data_[sourceIndex]);
        if (sourceIndex < targetIndex) {
            for (int i = sourceIndex; i < targetIndex; ++i) data_[i] = std::move(data_[i + 1]);
        } else {
            for (int i = sourceIndex; i > targetIndex; --i) data_[i] = std::move(data_[i - 1]);
        }
        data_[targetIndex] = std::move(tmp);
        if (cb_) cb_(cbCtx_, cbFeat_, EListEvent::Set, sourceIndex, data_[targetIndex], data_[targetIndex]);
    }

    bool contains(const T& v) const { return std::find(data_.begin(), data_.end(), v) != data_.end(); }
    int indexOf(const T& v) const {
        auto it = std::find(data_.begin(), data_.end(), v);
        return it == data_.end() ? -1 : static_cast<int>(it - data_.begin());
    }

    iterator begin() { return data_.begin(); }
    iterator end() { return data_.end(); }
    const_iterator begin() const { return data_.begin(); }
    const_iterator end() const { return data_.end(); }

    const container_type& data() const { return data_; }

private:
    container_type data_;
    CallbackFn cb_ = nullptr;
    void* cbCtx_ = nullptr;
    const emf::ecore::EStructuralFeature* cbFeat_ = nullptr;
};

}  // namespace emf::common
