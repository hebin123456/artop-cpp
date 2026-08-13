// EMF Compare: MatchEngine
// 对齐 org.eclipse.emf.compare.match.DefaultMatchEngine (Java)
#pragma once

#include "Comparison.h"  // IdentifierProvider 定义于此

#include <string>
#include <unordered_map>
#include <vector>

namespace emf::common {
class EObject;
}

namespace emf::compare {

class MatchEngine {
public:
    MatchEngine() = default;

    // 配置：相似度阈值（> threshold 视为 IDENTICAL，< 视为 DIFFERENT）
    void setSimilarityThreshold(double t) { threshold_ = t; }
    double getSimilarityThreshold() const { return threshold_; }

    // 配置：是否启用 identifier（xmi:id）优先匹配
    // Java EMF Compare 的 IdentifierEObjectMatcher：两边 EObject 拥有相同的 xmi:id 且
    // 同 EClass → 直接 IDENTICAL，跨文件场景的关键
    void setUseIdentifierMatcher(bool b) { useIdMatcher_ = b; }
    bool getUseIdentifierMatcher() const { return useIdMatcher_; }

    // 配置：是否启用自动 ID 属性匹配（P0-1 对齐 Java DefaultMatchEngine 默认行为）
    // 启用后：若对象 EClass 有 isID()==true 的 EAttribute，自动以其值作为 identifier 匹配
    // 优先级：手动 registerIdentifier > IdentifierProvider > 自动 ID 属性 > proximity 相似度
    void setUseIdAttribute(bool b) { useIdAttr_ = b; }
    bool getUseIdAttribute() const { return useIdAttr_; }

    // 顶层 match：2-way 递归处理整棵树
    void match(emf::common::EObject* left,
               emf::common::EObject* right,
               Comparison& comp);

    // 顶层 match：3-way 递归处理整棵树（对齐 Java DefaultMatchEngine 3-way）
    // origin 是公共祖先，分别与 left/right 做 2-way 匹配后合并
    void match(emf::common::EObject* left,
               emf::common::EObject* right,
               emf::common::EObject* origin,
               Comparison& comp);

    // 单个对象 match：返回新插入的 Match*（或已存在的）
    Match* matchOne(emf::common::EObject* left,
                    emf::common::EObject* right,
                    Comparison& comp);

    // 注册一个 xmi:id（EObject 的外部标识符），用于跨文件匹配
    void registerIdentifier(emf::common::EObject* obj, const std::string& id);
    const std::string* getIdentifier(emf::common::EObject* obj) const;
    void clearIdentifiers() { idMap_.clear(); }

    // 设置外部 IdentifierProvider（对齐 artop IdentifiableUtil）。
    // artop 层注册后，match 时优先调用 provider 取 ID，避免对无 isID 标记的
    // AUTOSAR 对象走 proximity（大文件性能关键）。
    // provider 返回非空字符串 → 该对象按 ID 严格匹配（不回退 proximity）。
    // provider 返回空字符串 → 走自动 ID 属性 / proximity。
    void setIdentifierProvider(IdentifierProvider provider) {
        idProvider_ = std::move(provider);
    }

private:
    double threshold_ = 1.0;
    bool useIdMatcher_ = false;
    bool useIdAttr_ = true;  // 默认启用自动 ID 属性匹配（对齐 Java DefaultMatchEngine）
    std::unordered_map<emf::common::EObject*, std::string> idMap_;
    IdentifierProvider idProvider_;  // 外部 ID provider（artop 层注入）

    // 取对象的 ID：手动注册 > provider > 自动 ID 属性
    // 返回 {id, hasId}：hasId=true 表示有 ID（严格按 ID 匹配），false 表示无 ID
    std::pair<std::string, bool> resolveId(emf::common::EObject* obj);
};

}  // namespace emf::compare
