// EcoreEMap.h
// 对齐 Java org.eclipse.emf.ecore.util.EcoreEMap
//
// EcoreEMap<K,V> 继承 BasicEMap<K,V> 并提供：
//   - 使用 EStructuralFeature 作为底层存储（Java: uses an EClass.EStore）
//   - getEStructuralFeature() 内部用
//   - EMap 的 EObject 内容直接通过 owner/feature 访问
//   - 对应 Java EcoreEMap.Entry 子类（override getFeature/setFeature）
#pragma once

#include "emf/common/util/BasicEMap.h"
#include "emf/ecore/EcorePackage.h"
#include "emf/common/EObject.h"

#include <any>
#include <stdexcept>

namespace emf::ecore::util {

class EcoreEMap : public emf::common::util::BasicEMap<emf::ecore::EStructuralFeature*, std::any> {
public:
    using Base = emf::common::util::BasicEMap<emf::ecore::EStructuralFeature*, std::any>;
    using K = emf::ecore::EStructuralFeature*;
    using V = std::any;

    // 对齐 Java: EcoreEMap.Entry
    class Entry : public emf::common::util::MapEntry<K, V> {
    public:
        Entry() = default;
        Entry(int /*hash*/, K key, V value)
            : emf::common::util::MapEntry<K, V>(key, std::move(value)) {}

        // Java: getFeature / setFeature
        virtual K getFeature() const {
            return this->key;
        }
        virtual void setFeature(K f) {
            this->setKey(f);
        }
        int getFeatureID() const {
            return this->key ? this->key->getFeatureID() : -1;
        }
    };

    EcoreEMap() = default;
    explicit EcoreEMap(int initialCapacity) : Base(initialCapacity) {}

    // Java: EcoreEMap override newEntry
    // 签名必须严格匹配父类 newEntry(int, const K&, const V&)
    Entry* newEntry(int hash, const K& key, const V& value) override {
        validateKey(key);
        validateValue(value);
        return new Entry(hash, key, value);
    }

    // Java: getEStructuralFeature() 内部用
    K getEStructuralFeature() const {
        if (!delegate_ || delegate_->size() == 0) return nullptr;
        return delegate_->get(0)->getKey();
    }

    // Java: EMap 风格 API 由 Base 提供。
};

}  // namespace emf::ecore::util
