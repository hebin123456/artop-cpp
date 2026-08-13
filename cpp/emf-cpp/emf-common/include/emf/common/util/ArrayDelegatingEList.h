// ArrayDelegatingEList.h
// 对齐 Java org.eclipse.emf.common.util.ArrayDelegatingEList
#pragma once

#include "emf/common/util/AbstractEList.h"

#include <cstddef>
#include <cstdint>
#include <stdexcept>
#include <string>
#include <vector>

namespace emf::common::util {

template <typename E>
class ArrayDelegatingEList : public AbstractEList<E> {
public:
    ArrayDelegatingEList() = default;

    ArrayDelegatingEList(const std::vector<E>& collection) {
        int s = static_cast<int>(collection.size());
        if (s > 0) {
            data_ = newData(s);
            for (int i = 0; i < s; ++i) {
                data_[static_cast<std::size_t>(i)] = collection[static_cast<std::size_t>(i)];
            }
            setData(data_);
        }
    }

protected:
    ArrayDelegatingEList(std::vector<E> data) {
        setData(data);
    }

public:
    virtual std::vector<E> newData(int capacity) const {
        return std::vector<E>(static_cast<std::size_t>(capacity));
    }

    virtual E assign(std::vector<E>& data, int index, const E& object) {
        data[static_cast<std::size_t>(index)] = object;
        return object;
    }

    int size() const override {
        return data_.empty() ? 0 : static_cast<int>(data_.size());
    }

    bool isEmpty() const override { return data_.empty(); }

    bool contains(const E& object) const override {
        if (!data_.empty()) {
            if (this->useEquals() && !(object == E{})) {
                for (const auto& d : data_) {
                    if (object == d) return true;
                }
            } else {
                for (const auto& d : data_) {
                    if (d == object) return true;
                }
            }
        }
        return false;
    }

    int indexOf(const E& object) const override {
        if (!data_.empty()) {
            if (this->useEquals() && !(object == E{})) {
                for (std::size_t i = 0; i < data_.size(); ++i) {
                    if (object == data_[i]) return static_cast<int>(i);
                }
            } else {
                for (std::size_t i = 0; i < data_.size(); ++i) {
                    if (data_[i] == object) return static_cast<int>(i);
                }
            }
        }
        return -1;
    }

    int lastIndexOf(const E& object) const override {
        if (!data_.empty()) {
            if (this->useEquals() && !(object == E{})) {
                for (int i = static_cast<int>(data_.size()) - 1; i >= 0; --i) {
                    if (object == data_[static_cast<std::size_t>(i)]) return i;
                }
            } else {
                for (int i = static_cast<int>(data_.size()) - 1; i >= 0; --i) {
                    if (data_[static_cast<std::size_t>(i)] == object) return i;
                }
            }
        }
        return -1;
    }

    std::vector<E> toArray() const { return data_; }

    E get(int index) const override {
        int s = size();
        if (index >= s) {
            throw typename AbstractEList<E>::BasicIndexOutOfBoundsException(index, s);
        }
        return this->resolve(index, data_[static_cast<std::size_t>(index)]);
    }

    E basicGet(int index) const override {
        int s = size();
        if (index >= s) {
            throw typename AbstractEList<E>::BasicIndexOutOfBoundsException(index, s);
        }
        return data_[static_cast<std::size_t>(index)];
    }

    E primitiveGet(int index) const override {
        return data_[static_cast<std::size_t>(index)];
    }

    E setUnique(int index, E object) override {
        std::vector<E> d = copy();
        E oldObject = d[static_cast<std::size_t>(index)];
        assign(d, index, this->validate(index, object));
        setData(d);
        this->didSet(index, object, oldObject);
        this->didChange();
        return oldObject;
    }

    void addUnique(E object) override {
        int s = size();
        std::vector<E> d = grow(s + 1);
        assign(d, s, this->validate(s, object));
        setData(d);
        this->didAdd(s, object);
        this->didChange();
    }

    void addUnique(int index, E object) override {
        std::vector<E> oldData = data_;
        int s = size();
        std::vector<E> d = grow(s + 1);
        E validatedObject = this->validate(index, object);
        if (index != s) {
            for (int i = s; i > index; --i) {
                d[static_cast<std::size_t>(i)] = oldData[static_cast<std::size_t>(i - 1)];
            }
        }
        assign(d, index, validatedObject);
        setData(d);
        this->didAdd(index, object);
        this->didChange();
    }

    bool addAllUnique(const std::vector<E>& collection) override {
        int growth = static_cast<int>(collection.size());
        if (growth == 0) return false;
        int oldSize = size();
        int newSize = oldSize + growth;
        std::vector<E> d = grow(newSize);
        for (int i = 0; i < growth; ++i) {
            E object = collection[static_cast<std::size_t>(i)];
            int target = oldSize + i;
            assign(d, target, this->validate(target, object));
        }
        setData(d);
        for (int i = 0; i < growth; ++i) {
            E object = d[static_cast<std::size_t>(oldSize + i)];
            this->didAdd(oldSize + i, object);
            this->didChange();
        }
        return true;
    }

    bool addAllUnique(int index, const std::vector<E>& collection) override {
        int growth = static_cast<int>(collection.size());
        if (growth == 0) return false;
        std::vector<E> oldData = data_;
        int oldSize = static_cast<int>(oldData.size());
        int newSize = oldSize + growth;
        std::vector<E> d = grow(newSize);

        int shifted = oldSize - index;
        if (shifted > 0) {
            for (int i = 0; i < shifted; ++i) {
                d[static_cast<std::size_t>(index + growth + i)] = oldData[static_cast<std::size_t>(index + i)];
            }
        }

        for (int i = 0; i < growth; ++i) {
            E object = collection[static_cast<std::size_t>(i)];
            int target = index + i;
            assign(d, target, this->validate(target, object));
        }
        setData(d);
        for (int i = 0; i < growth; ++i) {
            E object = d[static_cast<std::size_t>(index + i)];
            this->didAdd(index + i, object);
            this->didChange();
        }
        return true;
    }

    bool addAllUnique(const std::vector<E>& objects, int start, int end) override {
        int growth = end - start;
        if (growth == 0) return false;
        int oldSize = size();
        int newSize = oldSize + growth;
        std::vector<E> d = grow(newSize);
        int target = newSize;
        for (int i = end; i > start; ) {
            --i;
            E object = objects[static_cast<std::size_t>(i)];
            --target;
            assign(d, target, this->validate(target, object));
        }
        setData(d);
        for (int i = 0; i < growth; ++i) {
            int idx = oldSize + i;
            E object = d[static_cast<std::size_t>(idx)];
            this->didAdd(idx, object);
            this->didChange();
        }
        return true;
    }

    bool addAllUnique(int index, const std::vector<E>& objects, int start, int end) override {
        int growth = end - start;
        if (growth == 0) return false;
        std::vector<E> oldData = data_;
        int oldSize = static_cast<int>(oldData.size());
        int newSize = oldSize + growth;
        std::vector<E> d = grow(newSize);

        int shifted = oldSize - index;
        if (shifted > 0) {
            for (int i = 0; i < shifted; ++i) {
                d[static_cast<std::size_t>(index + growth + i)] = oldData[static_cast<std::size_t>(index + i)];
            }
        }

        for (int i = start; i < end; ++i) {
            E object = objects[static_cast<std::size_t>(i)];
            int target = index + (i - start);
            assign(d, target, this->validate(target, object));
        }
        setData(d);
        for (int i = 0; i < growth; ++i) {
            int target = index + i;
            E object = d[static_cast<std::size_t>(target)];
            this->didAdd(target, object);
            this->didChange();
        }
        return true;
    }

    bool removeAll(const std::vector<E>& collection) override {
        std::vector<E> d = data_;
        bool modified = false;
        for (int i = static_cast<int>(d.size()); --i >= 0;) {
            for (const auto& c : collection) {
                if (this->equalObjects(c, d[static_cast<std::size_t>(i)])) {
                    this->remove(i);
                    modified = true;
                    break;
                }
            }
        }
        return modified;
    }

    E remove(int index) override {
        std::vector<E> d = data_;
        int s = static_cast<int>(d.size());
        if (index >= s) {
            throw typename AbstractEList<E>::BasicIndexOutOfBoundsException(index, s);
        }
        E oldObject = d[static_cast<std::size_t>(index)];

        std::vector<E> newDataV;
        if (s == 1) {
            newDataV.clear();
        } else {
            newDataV = this->newData(s - 1);
            for (int i = 0; i < index; ++i) {
                newDataV[static_cast<std::size_t>(i)] = d[static_cast<std::size_t>(i)];
            }
            for (int i = index + 1; i < s; ++i) {
                newDataV[static_cast<std::size_t>(i - 1)] = d[static_cast<std::size_t>(i)];
            }
        }
        setData(newDataV);
        this->didRemove(index, oldObject);
        this->didChange();
        return oldObject;
    }

    bool retainAll(const std::vector<E>& collection) override {
        bool modified = false;
        for (int i = size(); --i >= 0;) {
            bool found = false;
            E cur = data_[static_cast<std::size_t>(i)];
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
        ++this->modCount_;
        std::vector<E> oldData = data_;
        int oldSize = static_cast<int>(oldData.size());
        setData({});
        this->didClear(oldSize, oldData);
        this->didChange();
    }

    E move(int targetIndex, int sourceIndex) override {
        std::vector<E> d = copy();
        int s = static_cast<int>(d.size());
        if (targetIndex >= s) {
            throw std::out_of_range("targetIndex=" + std::to_string(targetIndex) + ", size=" + std::to_string(s));
        }
        if (sourceIndex >= s) {
            throw std::out_of_range("sourceIndex=" + std::to_string(sourceIndex) + ", size=" + std::to_string(s));
        }
        E object = d[static_cast<std::size_t>(sourceIndex)];
        if (targetIndex != sourceIndex) {
            if (targetIndex < sourceIndex) {
                for (int i = sourceIndex; i > targetIndex; --i) {
                    d[static_cast<std::size_t>(i)] = d[static_cast<std::size_t>(i - 1)];
                }
            } else {
                for (int i = sourceIndex; i < targetIndex; ++i) {
                    d[static_cast<std::size_t>(i)] = d[static_cast<std::size_t>(i + 1)];
                }
            }
            assign(d, targetIndex, object);
            setData(d);
            this->didMove(targetIndex, object, sourceIndex);
            this->didChange();
        }
        return object;
    }

    std::vector<E> data() const { return data_; }

    void setData(std::vector<E> data) {
        data_ = std::move(data);
        ++this->modCount_;
    }

protected:
    std::vector<E> grow(int s) {
        std::vector<E> oldData = data_;
        std::vector<E> result = newData(s);
        for (std::size_t i = 0; i < oldData.size(); ++i) {
            result[i] = oldData[i];
        }
        return result;
    }

    std::vector<E> copy() const {
        return data_;
    }

    std::vector<E> data_;
};

}  // namespace emf::common::util
