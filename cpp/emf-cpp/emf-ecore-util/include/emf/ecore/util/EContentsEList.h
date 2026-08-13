// EMF Ecore-util: EContentsEList / ECrossReferenceEList
// 对齐 Java: org.eclipse.emf.ecore.util.EContentsEList / ECrossReferenceEList
//
// EContentsEList：遍历一个 EObject 的所有 containment 子对象（eContents 的列表视图）
// ECrossReferenceEList：遍历一个 EObject 的所有非 containment 引用对象（跨引用视图）
//
// Java 的 EContentsEList extends AbstractEList<EObject>，是 lazy + feature 驱动遍历器。
// 这里采用"构造时快照"的简化实现，对齐"遍历 containment / 跨引用"的语义。
// 不继承 emf::common::EList（其内部 data_ 私有且 get 返回引用，子类难以正确 override），
// 而是提供等价的 size/get/迭代器接口。
#pragma once

#include "emf/common/EObject.h"

#include <cstddef>
#include <vector>

namespace emf::ecore::util {

class EContentsEList {
public:
    explicit EContentsEList(emf::common::EObject* owner);

    size_t size() const { return contents_.size(); }
    bool empty() const { return contents_.empty(); }
    emf::common::EObject* get(size_t index) const { return contents_[index]; }
    void add(emf::common::EObject* value) { contents_.push_back(value); }

    std::vector<emf::common::EObject*>::const_iterator begin() const { return contents_.begin(); }
    std::vector<emf::common::EObject*>::const_iterator end() const { return contents_.end(); }

private:
    emf::common::EObject* owner_ = nullptr;
    std::vector<emf::common::EObject*> contents_;
};

class ECrossReferenceEList {
public:
    explicit ECrossReferenceEList(emf::common::EObject* owner);

    size_t size() const { return refs_.size(); }
    bool empty() const { return refs_.empty(); }
    emf::common::EObject* get(size_t index) const { return refs_[index]; }
    void add(emf::common::EObject* value) { refs_.push_back(value); }

    std::vector<emf::common::EObject*>::const_iterator begin() const { return refs_.begin(); }
    std::vector<emf::common::EObject*>::const_iterator end() const { return refs_.end(); }

private:
    emf::common::EObject* owner_ = nullptr;
    std::vector<emf::common::EObject*> refs_;
};

}  // namespace emf::ecore::util
