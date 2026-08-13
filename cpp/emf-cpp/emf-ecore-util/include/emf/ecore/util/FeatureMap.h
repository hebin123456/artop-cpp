// FeatureMap.h
// 对齐 Java org.eclipse.emf.ecore.util.FeatureMap
// FeatureMap 是一个 EList<FeatureMap.Entry>，每个 Entry 关联一个 EStructuralFeature
// 用于在 XML 序列化和模型中表达 group/choice/sequence/wildcard/anyAttribute。
//
// Java 关系链：
//   public interface FeatureMap extends EList<FeatureMap.Entry> { ... }
//
// Entry 接口（Java 公共内部接口）：
//   Object getValue();
//   void setValue(Object value);
//   EStructuralFeature getEStructuralFeature();
//   Object getFeature();
//   Internal.Wrapper getWrapper();
//   boolean isGroup();
//   int getFeatureID();
//
// Internal 接口（Java 内部接口）：
//   Wrapper getWrapper(Object value);
//   void setFeature(Entry entry, Object value);
//   Object getFeature(Entry entry, Object value);
#pragma once

#include "emf/common/EList.h"
#include "emf/common/Notification.h"
#include "emf/common/util/BasicEMap.h"
#include "emf/ecore/EcorePackage.h"

#include <any>
#include <memory>
#include <string>
#include <vector>

namespace emf::ecore {
class EStructuralFeature;
}

namespace emf::ecore::util {

// Forward declarations
class FeatureMap;
class BasicFeatureMap;

class FeatureMapEntry : public emf::common::util::MapEntry<emf::ecore::EStructuralFeature*, std::any> {
public:
    FeatureMapEntry() = default;
    FeatureMapEntry(emf::ecore::EStructuralFeature* feature, std::any value)
        : MapEntry(feature, std::move(value)) {}

    emf::ecore::EStructuralFeature* getEStructuralFeature() { return getKey(); }
    emf::ecore::EStructuralFeature* getEStructuralFeature() const { return getKey(); }
    void setEStructuralFeature(emf::ecore::EStructuralFeature* f) { setKey(f); }

    // EMF 扩展：原始 XSD 顺序 / wildcard 值
    virtual std::any getFeature() const = 0;
    virtual int getFeatureID() const = 0;
    virtual bool isGroup() const = 0;
};

// FeatureMap 抽象接口
class FeatureMap : public emf::common::EList<FeatureMapEntry*> {
public:
    // ====== 内部接口（Java BasicFeatureMap.Internal） ======
    class Internal {
    public:
        virtual ~Internal() = default;
        // 取得 feature 上指定 value 的 Wrapper（Java 内部类 Wrapper）。
        virtual FeatureMapEntry* getWrapper(FeatureMapEntry* /*entry*/, std::any /*value*/) = 0;
        // 设定/取得当前 entry 的"feature 化"值（XML 序列化时分组）
        virtual void setFeature(FeatureMapEntry* /*entry*/, std::any /*value*/) = 0;
        virtual std::any getFeature(FeatureMapEntry* /*entry*/, std::any /*value*/) = 0;
        // 判断给定 feature 是否是 group（Java 中是 EModelElement 的 group）
        virtual bool isGroup(emf::ecore::EStructuralFeature* feature) = 0;
        // Java: Wrapper 上 getFeature(Wrapper) / setFeature(Wrapper, Object) / getOwner
    };

    // ====== Java BasicFeatureMap.Entry 公共接口 ======
    class Entry : public FeatureMapEntry {
    public:
        Entry() = default;
        Entry(emf::ecore::EStructuralFeature* feature, std::any value)
            : FeatureMapEntry(feature, std::move(value)) {}

        std::any getFeature() const override {
            return std::any{getKey()};
        }
        int getFeatureID() const override {
            auto* sf = getKey();
            return sf ? sf->getFeatureID() : -1;
        }
        bool isGroup() const override { return false; }

        // Forward declaration of nested Internal; defined after FeatureMap is complete
        class Internal;
    };

    virtual ~FeatureMap() = default;

    // ====== 视图工厂（与 Java 的 FeatureMapUtil/FeatureMap.Entry 一致） ======
    // 通过某个 EStructuralFeature 过滤出本 map 中此 feature 的所有 value
    // 模拟 Java FeatureMap.entryList() / FeatureMap.iterator() 行为
    virtual std::vector<std::any> values(emf::ecore::EStructuralFeature* feature) = 0;
    virtual std::vector<FeatureMap::Entry*> entries(emf::ecore::EStructuralFeature* feature) = 0;

    // ====== group/choice/sequence/wildcard 支持 ======
    // 添加/取值时按 feature 分组，wildcard 用 EAttribute 的 namespaceURI/featureID 表示。
    virtual bool add(std::any value) = 0;
    virtual bool add(emf::ecore::EStructuralFeature* feature, std::any value) = 0;
    virtual bool add(emf::ecore::EStructuralFeature* feature, int index, std::any value) = 0;

    // 设置/获取 wildcard value
    virtual std::any get(emf::ecore::EStructuralFeature* feature, int index) = 0;
    virtual std::any set(emf::ecore::EStructuralFeature* feature, int index, std::any value) = 0;

    // size(feature) 模拟 Java 的 FeatureMap.size(EStructuralFeature) 视图大小
    virtual int size(emf::ecore::EStructuralFeature* feature) = 0;
};

// ====== Java: Entry.Internal 内部接口 ======
// 定义在 FeatureMap 外部，因为 Internal 继承 Entry，需要 Entry 完整定义。
// Java 中 Entry.Internal 是 Entry 的内部接口，含 inverseAdd/inverseRemove
// 等方法；这里提供一个最小可继承的 Internal 类供 BasicFeatureMap 派生。
class FeatureMap::Entry::Internal : public FeatureMap::Entry {
public:
    Internal() = default;
    Internal(emf::ecore::EStructuralFeature* feature, std::any value)
        : Entry(feature, std::move(value)) {}
    ~Internal() override = default;

    // 对齐 Java: inverseAdd / inverseRemove —— C++ 端默认 no-op
    virtual std::vector<emf::common::Notification> inverseAdd(
        emf::common::EObject* /*owner*/, int /*featureID*/,
        std::vector<emf::common::Notification> notifications) {
        return notifications;
    }
    virtual std::vector<emf::common::Notification> inverseRemove(
        emf::common::EObject* /*owner*/, int /*featureID*/,
        std::vector<emf::common::Notification> notifications) {
        return notifications;
    }
    // 对齐 Java: validate(Object value)
    virtual void validate(const std::any& /*value*/) {}
    // 对齐 Java: createEntry(Object value) / createEntry(InternalEObject value)
    virtual Internal* createEntry(const std::any& value) {
        auto* f = getEStructuralFeature();
        return new Internal(f, value);
    }
};

}  // namespace emf::ecore::util
