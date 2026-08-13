// UniqueEList.h
// 对齐 Java org.eclipse.emf.common.util.UniqueEList
#pragma once

#include "emf/common/util/BasicEList.h"

#include <vector>

namespace emf::common::util {

template <typename E>
class UniqueEList : public BasicEList<E> {
public:
    UniqueEList() = default;
    explicit UniqueEList(int initialCapacity) : BasicEList<E>(initialCapacity) {}
    explicit UniqueEList(const std::vector<E>& collection) {
        for (const auto& v : collection) this->add(v);
    }

    // 重写 addAll：对每个元素做唯一性检查
    bool addAll(const std::vector<E>& collection) override {
        bool modified = false;
        for (const auto& v : collection) {
            if (this->add(v)) modified = true;
        }
        return modified;
    }

    bool addAll(int index, const std::vector<E>& collection) override {
        bool modified = false;
        int cur = index;
        for (const auto& v : collection) {
            if (this->indexOf(v) < 0) {
                this->addUnique(cur, v);
                ++cur;
                modified = true;
            }
        }
        return modified;
    }

protected:
    bool isUnique() const override { return true; }
};

template <typename E>
class FastCompareUniqueEList : public UniqueEList<E> {
public:
    FastCompareUniqueEList() = default;
    explicit FastCompareUniqueEList(int initialCapacity) : UniqueEList<E>(initialCapacity) {}
    explicit FastCompareUniqueEList(const std::vector<E>& collection) : UniqueEList<E>(collection) {}

protected:
    bool useEquals() const override { return false; }
};

}  // namespace emf::common::util
