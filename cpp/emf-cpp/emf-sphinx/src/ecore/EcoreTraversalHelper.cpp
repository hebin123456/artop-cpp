// EcoreTraversalHelper.cpp - 对齐 Java org.eclipse.sphinx.emf.ecore.EcoreTraversalHelper
#include "emf/sphinx/ecore/EcoreTraversalHelper.h"
#include "emf/common/EObject.h"
#include <functional>

namespace emf::sphinx::ecore {

void EcoreTraversalHelper::traverse(emf::common::EObject* root,
                                    std::function<bool(emf::common::EObject*, int)> visitor, int depth) {
    if (!root) return;
    std::function<void(emf::common::EObject*, int)> recurse;
    recurse = [&](emf::common::EObject* obj, int d) {
        if (!obj) return;
        if (!visitor(obj, d)) return;
        if (depth >= 0 && d >= depth) return;
        for (auto* child : obj->eContents()) {
            recurse(child, d + 1);
        }
    };
    recurse(root, 0);
}

std::vector<emf::common::EObject*> EcoreTraversalHelper::getAllContents(emf::common::EObject* root, int depth) {
    std::vector<emf::common::EObject*> r;
    traverse(root, [&](emf::common::EObject* obj, int) { r.push_back(obj); return true; }, depth);
    return r;
}

}  // namespace emf::sphinx::ecore
