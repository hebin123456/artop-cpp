// emf::artop::runtime —— UnknownElement
// 对齐 Java: org.eclipse.emf.ecore.xmi.UnknownFeature / FEATURE_MAP_UNKNOWN
//
// 当 AutosarXMLLoader 遇到无法映射到 EStructuralFeature 的 XML 元素时
// （OPTION_RECORD_UNKNOWN_FEATURE=true），创建 UnknownElement 记录：
//   - 元素标签名
//   - 属性键值对
//   - 文本内容
//   - 子 UnknownElement（递归）
//
// 这些记录附加到 Resource 的 errors_/warnings_ 或 FeatureMap 上，
// 供后续诊断或 round-trip 保持使用。
#pragma once

#include "emf/common/EObject.h"

#include <string>
#include <vector>
#include <memory>
#include <unordered_map>

namespace emf::artop::runtime {

// 未知 XML 元素的轻量记录（不继承 EObject，纯数据）
struct UnknownElement {
    std::string tagName;                                   // XML 元素名（含前缀）
    std::unordered_map<std::string, std::string> attributes; // 属性键值对
    std::string text;                                       // 文本内容
    std::vector<std::unique_ptr<UnknownElement>> children;  // 子元素

    // 添加子元素
    UnknownElement* addChild(std::unique_ptr<UnknownElement> child) {
        auto* raw = child.get();
        children.push_back(std::move(child));
        return raw;
    }

    // 设置属性
    void setAttribute(const std::string& key, const std::string& value) {
        attributes[key] = value;
    }

    // 查找属性
    const std::string* getAttribute(const std::string& key) const {
        auto it = attributes.find(key);
        return it != attributes.end() ? &it->second : nullptr;
    }

    // 工厂方法
    static std::unique_ptr<UnknownElement> create(const std::string& tag) {
        auto e = std::make_unique<UnknownElement>();
        e->tagName = tag;
        return e;
    }
};

}  // namespace emf::artop::runtime
