// BasicEList.h
// 对齐 Java org.eclipse.emf.common.util.BasicEList
#pragma once

#include "emf/common/util/AbstractEList.h"

#include <cstddef>
#include <cstdint>
#include <cstdio>
#include <stdexcept>
#include <string>
#include <vector>

namespace emf::common::util {

template <typename E>
class BasicEList : public AbstractEList<E> {
public:
    int size_ = 0;
    std::vector<E> data_;

    BasicEList() = default;

    explicit BasicEList(int initialCapacity) {
        if (initialCapacity < 0) {
            throw std::invalid_argument("Illegal Capacity: " + std::to_string(initialCapacity));
        }
        if (initialCapacity > 0) {
            data_.resize(static_cast<std::size_t>(initialCapacity));
        }
    }

    BasicEList(const std::vector<E>& collection) {
        size_ = static_cast<int>(collection.size());
        if (size_ > 0) {
            data_.resize(static_cast<std::size_t>(size_ + size_ / 8 + 1));
            for (int i = 0; i < size_; ++i) {
                data_[static_cast<std::size_t>(i)] = collection[static_cast<std::size_t>(i)];
            }
        }
    }

protected:
    BasicEList(int size, std::vector<E> data) : size_(size), data_(std::move(data)) {}

public:
    virtual std::vector<E> newData(int capacity) const {
        return std::vector<E>(static_cast<std::size_t>(capacity));
    }

    // 写入数据存储
    virtual E assign(int index, const E& object) {
        data_[static_cast<std::size_t>(index)] = object;
        return object;
    }

    int size() const override { return size_; }
    bool isEmpty() const override { return size_ == 0; }

    bool contains(const E& object) const override {
        if (this->useEquals() && !(object == E{})) {
            for (int i = 0; i < size_; ++i) {
                if (object == data_[static_cast<std::size_t>(i)]) return true;
            }
        } else {
            for (int i = 0; i < size_; ++i) {
                if (data_[static_cast<std::size_t>(i)] == object) return true;
            }
        }
        return false;
    }

    int indexOf(const E& object) const override {
        if (this->useEquals() && !(object == E{})) {
            for (int i = 0; i < size_; ++i) {
                if (object == data_[static_cast<std::size_t>(i)]) return i;
            }
        } else {
            for (int i = 0; i < size_; ++i) {
                if (data_[static_cast<std::size_t>(i)] == object) return i;
            }
        }
        return -1;
    }

    int lastIndexOf(const E& object) const override {
        if (this->useEquals() && !(object == E{})) {
            for (int i = size_ - 1; i >= 0; --i) {
                if (object == data_[static_cast<std::size_t>(i)]) return i;
            }
        } else {
            for (int i = size_ - 1; i >= 0; --i) {
                if (data_[static_cast<std::size_t>(i)] == object) return i;
            }
        }
        return -1;
    }

    std::vector<E> toArray() const {
        return std::vector<E>(data_.begin(), data_.begin() + size_);
    }

    E get(int index) const override {
        if (data_.empty() || index >= size_) {
            throw typename AbstractEList<E>::BasicIndexOutOfBoundsException(index, size_);
        }
        return this->resolve(index, data_[static_cast<std::size_t>(index)]);
    }

    E basicGet(int index) const override {
        if (data_.empty() || index >= size_) {
            throw typename AbstractEList<E>::BasicIndexOutOfBoundsException(index, size_);
        }
        return primitiveGet(index);
    }

    E primitiveGet(int index) const override {
        return data_[static_cast<std::size_t>(index)];
    }

    E setUnique(int index, E object) override {
        E oldObject = data_[static_cast<std::size_t>(index)];
        assign(index, this->validate(index, object));
        this->didSet(index, object, oldObject);
        this->didChange();
        return oldObject;
    }

    void addUnique(E object) override {
        grow(size_ + 1);
        assign(size_, this->validate(size_, object));
        ++size_;
        this->didAdd(size_ - 1, object);
        this->didChange();
    }

    void addUnique(int index, E object) override {
        grow(size_ + 1);
        E validatedObject = this->validate(index, object);
        if (index != size_) {
            for (int i = size_; i > index; --i) {
                data_[static_cast<std::size_t>(i)] = data_[static_cast<std::size_t>(i - 1)];
            }
        }
        assign(index, validatedObject);
        ++size_;
        this->didAdd(index, object);
        this->didChange();
    }

    bool addAllUnique(const std::vector<E>& collection) override {
        int growth = static_cast<int>(collection.size());
        if (growth == 0) return false;
        grow(size_ + growth);
        int oldSize = size_;
        size_ += growth;
        for (int i = 0; i < growth; ++i) {
            E object = collection[static_cast<std::size_t>(i)];
            int target = oldSize + i;
            assign(target, this->validate(target, object));
            this->didAdd(target, object);
            this->didChange();
        }
        return true;
    }

    bool addAllUnique(int index, const std::vector<E>& collection) override {
        int growth = static_cast<int>(collection.size());
        if (growth == 0) return false;
        grow(size_ + growth);
        int shifted = size_ - index;
        if (shifted > 0) {
            for (int i = size_ - 1; i >= index; --i) {
                data_[static_cast<std::size_t>(i + growth)] = data_[static_cast<std::size_t>(i)];
            }
        }
        size_ += growth;
        for (int i = 0; i < growth; ++i) {
            E object = collection[static_cast<std::size_t>(i)];
            int target = index + i;
            assign(target, this->validate(target, object));
            this->didAdd(target, object);
            this->didChange();
        }
        return true;
    }

    bool addAllUnique(const std::vector<E>& objects, int start, int end) override {
        int growth = end - start;
        if (growth == 0) return false;
        grow(size_ + growth);
        int index = size_;
        size_ += growth;
        for (int i = start; i < end; ++i) {
            E object = objects[static_cast<std::size_t>(i)];
            assign(index, this->validate(index, object));
            this->didAdd(index, object);
            this->didChange();
            ++index;
        }
        return true;
    }

    bool addAllUnique(int index, const std::vector<E>& objects, int start, int end) override {
        int growth = end - start;
        if (growth == 0) return false;
        grow(size_ + growth);
        int shifted = size_ - index;
        if (shifted > 0) {
            for (int i = size_ - 1; i >= index; --i) {
                data_[static_cast<std::size_t>(i + growth)] = data_[static_cast<std::size_t>(i)];
            }
        }
        size_ += growth;
        for (int i = start; i < end; ++i) {
            E object = objects[static_cast<std::size_t>(i)];
            int target = index + (i - start);
            assign(target, this->validate(target, object));
            this->didAdd(target, object);
            this->didChange();
        }
        return true;
    }

    E remove(int index) override {
        if (index >= size_) {
            throw typename AbstractEList<E>::BasicIndexOutOfBoundsException(index, size_);
        }
        ++this->modCount_;
        E oldObject = data_[static_cast<std::size_t>(index)];

        int shifted = size_ - index - 1;
        if (shifted > 0) {
            for (int i = index; i < size_ - 1; ++i) {
                data_[static_cast<std::size_t>(i)] = data_[static_cast<std::size_t>(i + 1)];
            }
        }
        data_[static_cast<std::size_t>(--size_)] = E{};
        this->didRemove(index, oldObject);
        this->didChange();
        return oldObject;
    }

    void clear() override {
        ++this->modCount_;
        std::vector<E> oldData = data_;
        int oldSize = size_;
        data_.clear();
        size_ = 0;
        this->didClear(oldSize, oldData);
        this->didChange();
    }

    E move(int targetIndex, int sourceIndex) override {
        ++this->modCount_;
        if (targetIndex >= size_) {
            throw std::out_of_range("targetIndex=" + std::to_string(targetIndex) + ", size=" + std::to_string(size_));
        }
        if (sourceIndex >= size_) {
            throw std::out_of_range("sourceIndex=" + std::to_string(sourceIndex) + ", size=" + std::to_string(size_));
        }
        E object = data_[static_cast<std::size_t>(sourceIndex)];
        if (targetIndex != sourceIndex) {
            if (targetIndex < sourceIndex) {
                for (int i = sourceIndex; i > targetIndex; --i) {
                    data_[static_cast<std::size_t>(i)] = data_[static_cast<std::size_t>(i - 1)];
                }
            } else {
                for (int i = sourceIndex; i < targetIndex; ++i) {
                    data_[static_cast<std::size_t>(i)] = data_[static_cast<std::size_t>(i + 1)];
                }
            }
            assign(targetIndex, object);
            this->didMove(targetIndex, object, sourceIndex);
            this->didChange();
        }
        return object;
    }

    void shrink() {
        ++this->modCount_;
        if (size_ == 0) {
            data_.clear();
        } else if (static_cast<int>(data_.size()) > size_) {
            std::vector<E> oldData = data_;
            data_ = newData(size_);
            for (int i = 0; i < size_; ++i) {
                data_[static_cast<std::size_t>(i)] = oldData[static_cast<std::size_t>(i)];
            }
        }
    }

    void grow(int minimumCapacity) {
        ++this->modCount_;
        int oldCapacity = static_cast<int>(data_.size());
        if (minimumCapacity > oldCapacity) {
            int newCapacity = oldCapacity + oldCapacity / 2 + 4;
            if (newCapacity < minimumCapacity) newCapacity = minimumCapacity;
            std::vector<E> oldData = data_;
            data_ = newData(newCapacity);
            for (int i = 0; i < size_; ++i) {
                data_[static_cast<std::size_t>(i)] = oldData[static_cast<std::size_t>(i)];
            }
        }
    }

    std::vector<E>& dataVector() { return data_; }
    const std::vector<E>& dataVector() const { return data_; }

    void setData(int size, std::vector<E> data) {
        size_ = size;
        data_ = std::move(data);
        ++this->modCount_;
    }

    BasicEList<E>* clone() const {
        BasicEList<E>* copy = new BasicEList<E>();
        if (size_ > 0) {
            copy->size_ = size_;
            copy->data_ = newData(size_);
            for (int i = 0; i < size_; ++i) {
                copy->data_[static_cast<std::size_t>(i)] = data_[static_cast<std::size_t>(i)];
            }
        }
        return copy;
    }
};

template <typename E>
class FastCompareBasicEList : public BasicEList<E> {
public:
    FastCompareBasicEList() = default;
    explicit FastCompareBasicEList(int initialCapacity) : BasicEList<E>(initialCapacity) {}
    explicit FastCompareBasicEList(const std::vector<E>& collection)
        : BasicEList<E>(static_cast<int>(collection.size())) {
        if (this->size_ > 0) {
            this->data_.resize(static_cast<std::size_t>(this->size_ + this->size_ / 8 + 1));
            for (int i = 0; i < this->size_; ++i) {
                this->data_[static_cast<std::size_t>(i)] = collection[static_cast<std::size_t>(i)];
            }
        }
    }

protected:
    bool useEquals() const override { return false; }
};

}  // namespace emf::common::util
