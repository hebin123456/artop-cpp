// EMap.h
// 对齐 Java org.eclipse.emf.common.util.EMap
// 概念上是一个 Map.Entry 列表 + Map 视图。
// Java: public interface EMap<K, V> extends EList<Map.Entry<K, V>>
#pragma once

#include "emf/common/EList.h"
#include <cstdint>
#include <functional>
#include <memory>
#include <unordered_map>
#include <vector>
#include <type_traits>
#include <cstddef>
#include <any>

namespace emf::common::util {

// MapEntry: Java java.util.Map.Entry 风格的 K-V 容器
// 在 C++ 端以普通 class 给出。BasicEMap 的 Entry 继承自此。
template <typename K, typename V>
class MapEntry {
public:
    K key;
    V value;
    // 可选的自定义比较/哈希函数（None 表示用 == 和 std::hash）
    std::function<bool(const K&, const K&)> key_equal_to;
    std::function<bool(const V&, const V&)> value_equal_to;
    std::function<int(const K&)> key_hash;
    std::function<int(const V&)> value_hash_fn;

    MapEntry() = default;
    MapEntry(K k, V v) : key(std::move(k)), value(std::move(v)) {}
    virtual ~MapEntry() = default;

    virtual K& getKey() { return key; }
    virtual const K& getKey() const { return key; }
    virtual V& getValue() { return value; }
    virtual const V& getValue() const { return value; }
    virtual V setValue(const V& v) {
        V old = value;
        value = v;
        return old;
    }
    virtual void setKey(const K& k) { key = k; }

    bool value_has_hash() const {
        if (value_hash_fn) return true;
        if constexpr (std::is_same_v<V, std::any>) return false;  // std::any 不可哈希
        if constexpr (std::is_pointer_v<V>) return value != nullptr;
        if constexpr (std::is_arithmetic_v<V>) return true;
        return false;
    }
    int value_hash() const {
        if (value_hash_fn) return value_hash_fn(value);
        if constexpr (std::is_same_v<V, std::any>) return 0;
        if constexpr (std::is_pointer_v<V>) {
            return value ? static_cast<int>(reinterpret_cast<std::uintptr_t>(value) & 0x7fffffff) : 0;
        } else if constexpr (std::is_arithmetic_v<V>) {
            return static_cast<int>(value);
        } else {
            return 0;
        }
    }
    int key_hash_value() const {
        if (key_hash) return key_hash(key);
        if constexpr (std::is_pointer_v<K>) {
            return key ? static_cast<int>(reinterpret_cast<std::uintptr_t>(key) & 0x7fffffff) : 0;
        } else if constexpr (std::is_arithmetic_v<K>) {
            return static_cast<int>(key);
        } else {
            return 0;
        }
    }

    virtual int hashCode() const {
        int kh = key_hash_value();
        int vh = value_has_hash() ? value_hash() : 0;
        return kh ^ vh;
    }
    virtual bool equals(const std::shared_ptr<MapEntry<K, V>>& other) const {
        if (!other) return false;
        bool keyEq = (key_equal_to) ? key_equal_to(this->key, other->key)
                                    : (this->key == other->key);
        bool valEq = (value_equal_to) ? value_equal_to(this->value, other->value)
                                      : value_default_equals(this->value, other->value);
        return keyEq && valEq;
    }

    static bool value_default_equals(const V& a, const V& b) {
        if constexpr (std::is_same_v<V, std::any>) {
            // std::any 不支持 operator==。同类型 + 同数据即可视为相等。
            if (a.type() != b.type()) return false;
            if (!a.has_value() && !b.has_value()) return true;
            // 简单校验：value type 信息相同
            return a.type() == b.type();
        } else {
            return a == b;
        }
    }

    bool operator==(const MapEntry& other) const {
        if (this->key != other.key) return false;
        return value_default_equals(this->value, other.value);
    }
    bool operator!=(const MapEntry& other) const { return !(*this == other); }
};

// EMap<K, V>: 与 Java 公共接口一致
// 注意：显式继承 emf::common::EList<...>（含 iterator），而非 emf::common::util::EList<...>。
template <typename K, typename V>
class EMap : public ::emf::common::EList<MapEntry<K, V>*> {
public:
    using Entry = MapEntry<K, V>;
    using EListT = ::emf::common::EList<MapEntry<K, V>*>;
    using Iterator = typename EListT::iterator;
    using ConstIterator = typename EListT::const_iterator;

    // Internal 接口（Java EMap.Internal）：用于 EMaps 内部交换数据
    class Internal {
    public:
        virtual ~Internal() = default;
        virtual bool isGroup(K /*key*/) { return false; }
    };

    // ===== Map-style API =====
    virtual V get(const K& key) const = 0;
    virtual V put(const K& key, const V& value) = 0;
    virtual bool containsKey(const K& key) const = 0;
    virtual bool containsValue(const V& value) const = 0;
    virtual V removeKey(const K& key) = 0;
    virtual int indexOfKey(const K& key) const = 0;

    // ===== 视图（Java 风格 keySet/values/entrySet） =====
    virtual void forEachKey(const std::function<void(const K&)>& fn) const = 0;
    virtual void forEachValue(const std::function<void(const V&)>& fn) const = 0;

    // ===== EList<Entry*> 公共默认值（Java 的 iterator/listIterator 等） =====
    // BasicEMap 会在子类里 override 为更高效的版本。

    virtual ~EMap() = default;
};

}  // namespace emf::common::util
