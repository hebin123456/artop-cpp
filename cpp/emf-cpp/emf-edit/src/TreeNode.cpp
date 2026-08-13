// TreeNode.cpp
// 对齐 Java: org.eclipse.emf.edit.tree.TreeNode
// 状态: 框架骨架（仅占位实现）
#include "emf/edit/tree/TreeNode.h"

#include <algorithm>
#include <stdexcept>

namespace emf::edit::tree {

TreeNode::TreeNode() = default;
TreeNode::TreeNode(std::any data) : data_(std::move(data)) {}
TreeNode::~TreeNode() = default;

void TreeNode::addChild(TreeNode* child) {
    if (!child) return;
    child->setParent(this);
    children_.push_back(child);
}

void TreeNode::removeChild(TreeNode* child) {
    auto it = std::find(children_.begin(), children_.end(), child);
    if (it != children_.end()) {
        (*it)->setParent(nullptr);
        children_.erase(it);
    }
}

TreeNode* TreeNode::getChild(int index) const {
    if (index < 0 || index >= static_cast<int>(children_.size())) {
        throw std::out_of_range("TreeNode::getChild index out of range");
    }
    return children_[static_cast<std::size_t>(index)];
}

// TreeIterator 简单 DFS pre-order
TreeIterator::TreeIterator(TreeNode* root) {
    if (root) stack_.push_back(root);
}

bool TreeIterator::hasNext() const {
    return !stack_.empty();
}

TreeNode* TreeIterator::next() {
    if (stack_.empty()) return nullptr;
    TreeNode* node = stack_.back();
    stack_.pop_back();
    // 倒序 push 子节点以保持 pre-order
    for (auto it = node->getChildren().rbegin(); it != node->getChildren().rend(); ++it) {
        stack_.push_back(*it);
    }
    return node;
}

}  // namespace emf::edit::tree
