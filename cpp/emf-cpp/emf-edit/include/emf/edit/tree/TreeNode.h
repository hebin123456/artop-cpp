// TreeNode.h
// 对齐 Java: org.eclipse.emf.edit.tree.TreeNode
// 状态: 框架骨架（仅声明，未实现）
#pragma once

#include "emf/common/EObject.h"
#include "emf/common/util/BasicEMap.h"

#include <vector>
#include <memory>

namespace emf::edit::tree {

// Java 行为：TreeNode 适配 EObject 到 TreeViewer；可分层 parent/children。
class TreeNode {
public:
    TreeNode();
    TreeNode(std::any data);
    ~TreeNode();

    std::any getData() const { return data_; }
    void setData(std::any data) { data_ = std::move(data); }

    TreeNode* getParent() const { return parent_; }
    void setParent(TreeNode* parent) { parent_ = parent; }

    const std::vector<TreeNode*>& getChildren() const { return children_; }
    void addChild(TreeNode* child);
    void removeChild(TreeNode* child);
    int getChildCount() const { return static_cast<int>(children_.size()); }
    TreeNode* getChild(int index) const;

private:
    std::any data_;
    TreeNode* parent_ = nullptr;
    std::vector<TreeNode*> children_;
};

// TreeIterator 嵌套在 TreeNode 内（Java 行为）
class TreeIterator {
public:
    explicit TreeIterator(TreeNode* root);
    bool hasNext() const;
    TreeNode* next();

private:
    std::vector<TreeNode*> stack_;
};

}  // namespace emf::edit::tree
