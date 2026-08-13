// EcoreTraversalHelper.h
// 对齐 Java org.eclipse.sphinx.emf.ecore.EcoreTraversalHelper
// 树/图遍历助手（深度优先 / 广度优先等）
#pragma once

#include <functional>
#include <vector>
#include <string>

namespace emf::common {
class EObject;
}

namespace emf::sphinx::ecore {

class EcoreTraversalHelper {
public:
    EcoreTraversalHelper() = default;
    virtual ~EcoreTraversalHelper() = default;

    virtual void traverse(emf::common::EObject* root,
                          std::function<bool(emf::common::EObject*, int)> visitor,
                          int depth = -1);

    virtual std::vector<emf::common::EObject*> getAllContents(emf::common::EObject* root, int depth = -1);
};

}  // namespace emf::sphinx::ecore
