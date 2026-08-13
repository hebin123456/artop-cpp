// DelegatingEList.h
// 对齐 Java org.eclipse.emf.common.util.DelegatingEList
#pragma once

#include "emf/common/util/AbstractEList.h"

#include <algorithm>
#include <cstddef>
#include <sstream>
#include <stdexcept>
#include <vector>

namespace emf::common::util {

template <typename E>
class DelegatingEList : public AbstractEList<E> {
public:
    DelegatingEList() = default;
    explicit DelegatingEList(const std::vector<E>& collection) {
        for (const auto& v : collection) this->add(v);
    }

    virtual std::vector<E>& delegateList() = 0;
    virtual const std::vector<E>& delegateList() const = 0;

    int size() const override { return delegateSize(); }
    int delegateSize() const { return static_cast<int>(delegateList().size()); }

    bool isEmpty() const override { return delegateIsEmpty(); }
    bool delegateIsEmpty() const { return delegateList().empty(); }

    bool contains(const E& object) const override { return delegateContains(object); }
    bool delegateContains(const E& object) const {
        const auto& d = delegateList();
        return std::find(d.begin(), d.end(), object) != d.end();
    }

    int indexOf(const E& object) const override { return delegateIndexOf(object); }
    int delegateIndexOf(const E& object) const {
        const auto& d = delegateList();
        auto it = std::find(d.begin(), d.end(), object);
        return it == d.end() ? -1 : static_cast<int>(it - d.begin());
    }

    int lastIndexOf(const E& object) const override { return delegateLastIndexOf(object); }
    int delegateLastIndexOf(const E& object) const {
        const auto& d = delegateList();
        for (int i = static_cast<int>(d.size()) - 1; i >= 0; --i) {
            if (d[static_cast<std::size_t>(i)] == object) return i;
        }
        return -1;
    }

    std::vector<E> toArray() const { return delegateToArray(); }
    std::vector<E> delegateToArray() const { return delegateList(); }

    E get(int index) const override { return this->resolve(index, delegateGet(index)); }
    E delegateGet(int index) const { return delegateList()[static_cast<std::size_t>(index)]; }

    E basicGet(int index) const override { return delegateGet(index); }
    E primitiveGet(int index) const override { return delegateGet(index); }

    E setUnique(int index, E object) override {
        E oldObject = delegateSet(index, this->validate(index, object));
        this->didSet(index, object, oldObject);
        this->didChange();
        return oldObject;
    }
    E delegateSet(int index, const E& object) {
        E old = delegateList()[static_cast<std::size_t>(index)];
        delegateList()[static_cast<std::size_t>(index)] = object;
        return old;
    }

    void addUnique(E object) override {
        ++this->modCount_;
        int s = size();
        delegateAdd(this->validate(s, object));
        this->didAdd(s, object);
        this->didChange();
    }
    void delegateAdd(const E& object) { delegateList().push_back(object); }

    void addUnique(int index, E object) override {
        ++this->modCount_;
        delegateAdd(index, this->validate(index, object));
        this->didAdd(index, object);
        this->didChange();
    }
    void delegateAdd(int index, const E& object) {
        delegateList().insert(delegateList().begin() + index, object);
    }

    bool addAllUnique(const std::vector<E>& collection) override {
        ++this->modCount_;
        if (collection.empty()) return false;
        int i = size();
        for (const auto& v : collection) {
            delegateAdd(this->validate(i, v));
            this->didAdd(i, v);
            this->didChange();
            ++i;
        }
        return true;
    }

    bool addAllUnique(int index, const std::vector<E>& collection) override {
        ++this->modCount_;
        if (collection.empty()) return false;
        int cur = index;
        for (const auto& v : collection) {
            delegateAdd(cur, this->validate(cur, v));
            this->didAdd(cur, v);
            this->didChange();
            ++cur;
        }
        return true;
    }

    bool addAllUnique(const std::vector<E>& objects, int start, int end) override {
        ++this->modCount_;
        int growth = end - start;
        if (growth == 0) return false;
        int index = size();
        for (int i = start; i < end; ++i, ++index) {
            E object = objects[static_cast<std::size_t>(i)];
            delegateAdd(this->validate(index, object));
            this->didAdd(index, object);
            this->didChange();
        }
        return true;
    }

    bool addAllUnique(int index, const std::vector<E>& objects, int start, int end) override {
        ++this->modCount_;
        int growth = end - start;
        if (growth == 0) return false;
        for (int i = start; i < end; ++i, ++index) {
            E object = objects[static_cast<std::size_t>(i)];
            delegateAdd(index, this->validate(index, object));
            this->didAdd(index, object);
            this->didChange();
        }
        return true;
    }

    E remove(int index) override {
        ++this->modCount_;
        E oldObject = delegateRemove(index);
        this->didRemove(index, oldObject);
        this->didChange();
        return oldObject;
    }
    E delegateRemove(int index) {
        E old = delegateList()[static_cast<std::size_t>(index)];
        delegateList().erase(delegateList().begin() + index);
        return old;
    }

    bool removeAll(const std::vector<E>& collection) override {
        bool modified = false;
        for (int i = size(); --i >= 0;) {
            E cur = delegateList()[static_cast<std::size_t>(i)];
            for (const auto& c : collection) {
                if (this->equalObjects(c, cur)) {
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
        for (int i = size(); --i >= 0;) {
            E cur = delegateList()[static_cast<std::size_t>(i)];
            bool found = false;
            for (const auto& c : collection) {
                if (this->equalObjects(c, cur)) {
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

    void clear() override {
        doClear(size(), delegateToArray());
    }

    void doClear(int oldSize, const std::vector<E>& oldData) {
        ++this->modCount_;
        delegateClear();
        this->didClear(oldSize, oldData);
        this->didChange();
    }
    void delegateClear() { delegateList().clear(); }

    E move(int targetIndex, int sourceIndex) override {
        ++this->modCount_;
        int s = size();
        if (targetIndex >= s || targetIndex < 0) {
            throw std::out_of_range("targetIndex=" + std::to_string(targetIndex) + ", size=" + std::to_string(s));
        }
        if (sourceIndex >= s || sourceIndex < 0) {
            throw std::out_of_range("sourceIndex=" + std::to_string(sourceIndex) + ", size=" + std::to_string(s));
        }
        E object;
        if (targetIndex != sourceIndex) {
            object = delegateMove(targetIndex, sourceIndex);
            this->didMove(targetIndex, object, sourceIndex);
            this->didChange();
        } else {
            object = delegateGet(sourceIndex);
        }
        return object;
    }
    E delegateMove(int targetIndex, int sourceIndex) {
        E result = delegateRemove(sourceIndex);
        delegateAdd(targetIndex, result);
        return result;
    }

    bool listEquals(const DelegatingEList<E>& other) const {
        return this->delegateList() == other.delegateList();
    }

    int listHashCode() const { return delegateHashCode(); }
    int delegateHashCode() const {
        int h = 1;
        const auto& d = delegateList();
        for (const auto& v : d) {
            h = 31 * h + (v == E{} ? 0 : std::hash<E>{}(v));
        }
        return h;
    }

    std::string listToString() const { return delegateToString(); }
    std::string delegateToString() const {
        const auto& d = delegateList();
        std::ostringstream oss;
        oss << "[";
        for (std::size_t i = 0; i < d.size(); ++i) {
            if (d[i] == E{}) oss << "null";
            else oss << d[i];
            if (i + 1 < d.size()) oss << ", ";
        }
        oss << "]";
        return oss.str();
    }
};

template <typename E>
class UnmodifiableDelegatingEList : public DelegatingEList<E> {
public:
    explicit UnmodifiableDelegatingEList(std::vector<E>& underlying) : underlying_(underlying) {}

    std::vector<E>& delegateList() override { return underlying_; }
    const std::vector<E>& delegateList() const override { return underlying_; }

    E set(int /*index*/, E /*object*/) override {
        throw std::logic_error("UnsupportedOperationException");
    }
    bool add(E /*object*/) override {
        throw std::logic_error("UnsupportedOperationException");
    }
    void add(int /*index*/, E /*object*/) override {
        throw std::logic_error("UnsupportedOperationException");
    }
    bool addAll(const std::vector<E>& /*collection*/) override {
        throw std::logic_error("UnsupportedOperationException");
    }
    bool addAll(int /*index*/, const std::vector<E>& /*collection*/) override {
        throw std::logic_error("UnsupportedOperationException");
    }
    template <typename U = E,
              typename = typename std::enable_if<!std::is_same<U, int>::value>::type>
    bool remove(const E& /*object*/) { throw std::logic_error("UnsupportedOperationException"); }
    E remove(int /*index*/) override {
        throw std::logic_error("UnsupportedOperationException");
    }
    bool removeAll(const std::vector<E>& /*collection*/) override {
        throw std::logic_error("UnsupportedOperationException");
    }
    bool retainAll(const std::vector<E>& /*collection*/) override {
        throw std::logic_error("UnsupportedOperationException");
    }
    void clear() override {
        throw std::logic_error("UnsupportedOperationException");
    }
    template <typename U = E,
              typename = typename std::enable_if<!std::is_same<U, int>::value>::type>
    void move(int /*newPosition*/, const E& /*object*/) {
        throw std::logic_error("UnsupportedOperationException");
    }
    E move(int /*targetIndex*/, int /*sourceIndex*/) override {
        throw std::logic_error("UnsupportedOperationException");
    }

private:
    std::vector<E>& underlying_;
};

}  // namespace emf::common::util
