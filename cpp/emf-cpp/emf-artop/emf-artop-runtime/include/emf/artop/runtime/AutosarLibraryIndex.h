// AutosarLibraryIndex —— 跨文档 shortName path 索引，支持 demand-load
// 对齐 Java ARTOP 的 AutosarLibraryDescriptor + ReferenceHelper 全局 path 索引机制
//
// 背景：
//   arxml 引用用绝对 shortName path（如 "/AUTOSAR/AISpecification/KeywordSets_Blueprint/..."）。
//   单文件加载时只能索引本文件的对象；跨文件引用保持 proxy。
//   Java ARTOP 通过 Library 机制：预加载的 library resource 会把其所有 GReferrable 的
//   shortName path 注册到全局索引，供跨文档引用解析。
//
// 本实现：
//   - 全局单例 AutosarLibraryIndex，按 shortName path → EObject* 索引
//   - loadLibrary(resource)：遍历 resource 的所有 EObject，注册 shortName path
//   - lookup(path)：返回 EObject*（nullptr 表示未找到）
//   - clear()：清空索引（测试间重置）
//
// 使用方式：
//   1. 加载 library resource 后调用 AutosarLibraryIndex::instance().indexResource(res);
//   2. resolvePendingRefs 中，pathIndex 未命中时查 AutosarLibraryIndex
//   3. 命中则用真实 target 替换 proxy（实现 demand-load 效果）
#pragma once

#include "emf/common/EObject.h"
#include "emf/common/Resource.h"
#include <string>
#include <unordered_map>

namespace emf::artop::runtime {

class AutosarLibraryIndex {
public:
    static AutosarLibraryIndex& instance();

    // 索引一个 resource 的所有 EObject（按 shortName path）
    // 对齐 Java ARTOP 加载 library 后构建全局 path 索引
    void indexResource(emf::common::Resource* res);

    // 索引单个 EObject 及其 containment 子树
    void indexObject(emf::common::EObject* obj);

    // 按 shortName path 查找 EObject
    emf::common::EObject* lookup(const std::string& path) const;

    // 检查 path 是否在索引中
    bool contains(const std::string& path) const;

    // 清空索引（测试间重置）
    void clear();

    // 索引大小
    size_t size() const { return pathIndex_.size(); }

private:
    AutosarLibraryIndex() = default;
    // path → EObject*
    std::unordered_map<std::string, emf::common::EObject*> pathIndex_;
};

}  // namespace emf::artop::runtime
