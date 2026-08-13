// AbstractEList.h
// 对齐 Java org.eclipse.emf.common.util.AbstractEList
#pragma once

#include <cstddef>
#include <cstdint>
#include <sstream>
#include <stdexcept>
#include <string>
#include <type_traits>
#include <vector>

namespace emf::common::util {

template <typename E>
class EList {
public:
    virtual ~EList() = default;
    virtual int size() const = 0;
    virtual bool isEmpty() const = 0;
    virtual bool contains(const E& object) const = 0;
    virtual int indexOf(const E& object) const = 0;
    virtual int lastIndexOf(const E& object) const = 0;
    virtual E get(int index) const = 0;
    virtual E set(int index, E object) = 0;
    virtual bool add(E object) = 0;
    virtual void add(int index, E object) = 0;
    virtual bool addAll(const std::vector<E>& collection) = 0;
    virtual bool addAll(int index, const std::vector<E>& collection) = 0;
    virtual E remove(int index) = 0;
    virtual bool removeAll(const std::vector<E>& collection) = 0;
    virtual bool retainAll(const std::vector<E>& collection) = 0;
    virtual void clear() = 0;
    virtual E move(int targetIndex, int sourceIndex) = 0;
};

template <typename E>
class AbstractEList : public EList<E> {
public:
    virtual bool useEquals() const { return true; }
    virtual bool canContainNull() const { return true; }
    virtual bool isUnique() const { return false; }

    virtual E validate(int /*index*/, const E& object) {
        if (!canContainNull() && object == E{}) {
            throw std::invalid_argument("The 'no null' constraint is violated");
        }
        return object;
    }

    virtual E resolve(int /*index*/, const E& object) const { return object; }
    virtual E resolve(const E& object) const { return object; }

    virtual void didSet(int, const E&, const E&) {}
    virtual void didAdd(int, const E&) {}
    virtual void didRemove(int, const E&) {}
    virtual void didMove(int, const E&, int) {}
    virtual void didChange() {}
    virtual void didClear(int, const std::vector<E>&) {}

    struct BasicIndexOutOfBoundsException : public std::out_of_range {
        explicit BasicIndexOutOfBoundsException(int index, int size)
            : std::out_of_range("index=" + std::to_string(index) + ", size=" + std::to_string(size)) {}
    };

    int modCount_ = 0;

    virtual E basicGet(int index) const {
        if (index >= this->size()) throw BasicIndexOutOfBoundsException(index, this->size());
        return primitiveGet(index);
    }

    virtual E primitiveGet(int index) const = 0;

    bool add(E object) override {
        if (isUnique() && this->contains(object)) return false;
        addUnique(object);
        return true;
    }

    void add(int index, E object) override {
        if (index > this->size()) throw BasicIndexOutOfBoundsException(index, this->size());
        if (isUnique() && this->contains(object)) {
            throw std::invalid_argument("The 'no duplicates' constraint is violated");
        }
        addUnique(index, object);
    }

    E set(int index, E object) override {
        if (index >= this->size()) throw BasicIndexOutOfBoundsException(index, this->size());
        if (isUnique()) {
            int currentIndex = this->indexOf(object);
            if (currentIndex >= 0 && currentIndex != index) {
                throw std::invalid_argument("The 'no duplicates' constraint is violated");
            }
        }
        return setUnique(index, object);
    }

    // 模板版 remove(const E&)：仅在 E != int 时启用
    template <typename U = E,
              typename = typename std::enable_if<
                  !std::is_same<U, int>::value>::type>
    bool remove(const E& object) {
        int index = this->indexOf(object);
        if (index >= 0) {
            this->remove(index);
            return true;
        }
        return false;
    }

    bool removeByValue(const E& object) {
        int index = this->indexOf(object);
        if (index >= 0) {
            this->remove(index);
            return true;
        }
        return false;
    }

    bool removeAll(const std::vector<E>& collection) override {
        bool modified = false;
        for (int i = this->size(); --i >= 0;) {
            E cur = primitiveGet(i);
            for (const auto& c : collection) {
                if (equalObjects(c, cur)) {
                    this->remove(i);
                    modified = true;
                    break;
                }
            }
        }
        return modified;
    }

    bool retainAll(const std::vector<E>& collection) override {
        bool modified = false;
        for (int i = this->size(); --i >= 0;) {
            E cur = primitiveGet(i);
            bool found = false;
            for (const auto& c : collection) {
                if (equalObjects(c, cur)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                this->remove(i);
                modified = true;
            }
        }
        return modified;
    }

    bool containsAll(const std::vector<E>& collection) const {
        for (const auto& c : collection) {
            if (!this->contains(c)) return false;
        }
        return true;
    }

    virtual void addUnique(E object) = 0;
    virtual void addUnique(int index, E object) = 0;
    virtual E setUnique(int index, E object) = 0;
    virtual E remove(int index) override = 0;

    virtual bool addAllUnique(const std::vector<E>& collection) = 0;
    virtual bool addAllUnique(int index, const std::vector<E>& collection) = 0;
    virtual bool addAllUnique(const std::vector<E>& objects, int start, int end) = 0;
    virtual bool addAllUnique(int index, const std::vector<E>& objects, int start, int end) = 0;

    // move(int, int)：抽象 —— 使用 using 把基类 EList::move 引入，
    // 防止后面模板版 move(int, E) 隐藏。
    using EList<E>::move;
    virtual E move(int targetIndex, int sourceIndex) = 0;

    // move(int, E)：仅在 E != int 时启用
    template <typename U = E,
              typename = typename std::enable_if<
                  !std::is_same<U, int>::value>::type>
    void move(int newPosition, const E& object) {
        int srcIdx = this->indexOf(object);
        E moved = EList<E>::move(newPosition, srcIdx);
        (void)moved;
    }

    bool equalObjects(const E& a, const E& b) const {
        if (useEquals()) return a == b;
        return &a == &b;
    }

    bool addAll(const std::vector<E>& collection) override {
        return addAllUnique(collection);
    }

    bool addAll(int index, const std::vector<E>& collection) override {
        if (index > this->size()) throw BasicIndexOutOfBoundsException(index, this->size());
        return addAllUnique(index, collection);
    }

    bool listEquals(const AbstractEList<E>& other) const {
        if (this == &other) return true;
        if (this->size() != other.size()) return false;
        for (int i = 0, n = this->size(); i < n; ++i) {
            E a = primitiveGet(i);
            E b = other.primitiveGet(i);
            bool aNull = (a == E{});
            bool bNull = (b == E{});
            if (aNull ? !bNull : !(a == b)) return false;
        }
        return true;
    }

    int listHashCode() const {
        int h = 1;
        for (int i = 0, n = this->size(); i < n; ++i) {
            E obj = primitiveGet(i);
            if constexpr (std::is_integral_v<E>) {
                h = 31 * h + static_cast<int>(obj);
            } else if constexpr (std::is_pointer_v<E>) {
                h = 31 * h + static_cast<int>(reinterpret_cast<std::uintptr_t>(obj) & 0x7fffffff);
            } else {
                h = 31 * h + (obj == E{} ? 0 : static_cast<int>(std::hash<E>{}(obj)));
            }
        }
        return h;
    }

    std::string listToString() const {
        std::ostringstream oss;
        oss << "[";
        for (int i = 0, n = this->size(); i < n; ++i) {
            E obj = primitiveGet(i);
            if (obj == E{}) oss << "null";
            else oss << obj;
            if (i + 1 < n) oss << ", ";
        }
        oss << "]";
        return oss.str();
    }
};

}  // namespace emf::common::util
