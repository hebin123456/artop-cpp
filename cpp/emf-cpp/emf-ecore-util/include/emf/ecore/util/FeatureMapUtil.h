// FeatureMapUtil.h
// 对齐 Java org.eclipse.emf.ecore.util.FeatureMapUtil (1771 行)
//
// FeatureMapUtil 是 EcoreUtil 之上的工具集合，专门处理
// EObject 上的 featureMap / group / choice / sequence / wildcard /
// anyAttribute 模式。绝大多数方法以静态函数实现。
#pragma once

#include "emf/ecore/EcorePackage.h"
#include "emf/ecore/util/BasicFeatureMap.h"
#include "emf/ecore/util/FeatureMap.h"
#include "emf/common/EObject.h"

#include <any>
#include <functional>
#include <string>
#include <vector>

namespace emf::ecore::util {

class FeatureMapUtil {
public:
    // ====== Validator（Java: public static class Validator）======
    // 用于判断一个 EStructuralFeature 在当前 owner.eClass() 上下文中是否有效。
    // 简化实现：始终返回 true（C++ stub）。
    class Validator {
    public:
        Validator() = default;
        bool isValid(emf::ecore::EStructuralFeature* feature) const;
    };

    // 获取 validator：返回全局共享 no-op validator。
    static Validator* getValidator(emf::ecore::EClass* eClass,
                                   emf::ecore::EStructuralFeature* feature);

    // ====== 基本谓词 ======
    // 是否是 EAttribute wildcard（isMany + 名字形如 "*"），
    // 对齐 Java FeatureMapUtil.isWildcard
    static bool isWildcard(emf::ecore::EStructuralFeature* feature);

    // 是否是 anyAttribute（XMLSchema anyAttribute）
    static bool isAnyAttribute(emf::ecore::EStructuralFeature* feature);

    // 是否是 group（EClass 包含的 EModelElement 是 group 标志）
    static bool isGroup(emf::ecore::EStructuralFeature* feature);

    // 是否是 FeatureMap（feature.eType 的 instance class 名为 EFeatureMap）
    static bool isFeatureMap(emf::ecore::EStructuralFeature* feature);

    // 是否是 many-valued（feature.upperBound != 1）
    static bool isMany(emf::common::EObject* owner,
                       emf::ecore::EStructuralFeature* feature);

    // 创建 Entry 工厂方法（Java FeatureMapUtil.createEntry / createRawEntry）
    static FeatureMap::Entry* createEntry(emf::ecore::EStructuralFeature* feature,
                                          std::any value);
    static FeatureMap::Entry::Internal* createRawEntry(emf::ecore::EStructuralFeature* feature,
                                                       std::any value);

    // 是否是 document root（Java FeatureMapUtil.isDocumentRoot）
    static bool isDocumentRoot(emf::common::EObject* eObject);

    // ====== FeatureMap 入口 ======
    // 返回 object 上 feature 关联的 FeatureMap.Entry 列表
    static std::vector<FeatureMap::Entry*> getEntries(
        emf::common::EObject* object,
        emf::ecore::EStructuralFeature* feature);

    // 提取给定 feature 在 object 上的所有 values
    static std::vector<std::any> getValues(
        emf::common::EObject* object,
        emf::ecore::EStructuralFeature* feature);

    // 简单添加：add(object, feature, value)
    static void add(emf::common::EObject* object,
                    emf::ecore::EStructuralFeature* feature,
                    std::any value);

    // 简单取值：get(object, feature, index)
    static std::any get(emf::common::EObject* object,
                        emf::ecore::EStructuralFeature* feature,
                        int index);

    // 简单设置：set(object, feature, index, value) -> oldValue
    static std::any set(emf::common::EObject* object,
                        emf::ecore::EStructuralFeature* feature,
                        int index,
                        std::any value);

    // 是否包含 value
    static bool contains(emf::common::EObject* object,
                         emf::ecore::EStructuralFeature* feature,
                         const std::any& value);

    // size
    static int size(emf::common::EObject* object,
                    emf::ecore::EStructuralFeature* feature);

    // isEmpty
    static bool isEmpty(emf::common::EObject* object,
                        emf::ecore::EStructuralFeature* feature);

    // ====== XML 处理工具 ======
    // 给定 EAttribute（wildcard）创建 placeholder feature
    // 对齐 Java FeatureMapUtil.createFeature
    static emf::ecore::EStructuralFeature* createFeature(
        emf::ecore::EClass* owner,
        const std::string& name,
        emf::ecore::EClassifier* type);

    // 给定字符串名，解析为 package 中的 nsURI（如 "http://...#name"）
    static std::pair<std::string, std::string> decodeFeatureName(
        const std::string& qualifiedName);

    // ====== 内部辅助 ======
    // 把字符串拆成 (namespaceURI, name) 形式
    static std::pair<std::string, std::string> splitName(
        const std::string& name);

    // 给定 namespaceURI + name，构造一个 EAttribute wildcard
    static emf::ecore::EStructuralFeature* createEAttributeWildcard(
        emf::ecore::EClass* owner,
        const std::string& name);
};

}  // namespace emf::ecore::util
