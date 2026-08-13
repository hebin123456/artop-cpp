// EObjectComparator.h
// 对齐 Java org.eclipse.sphinx.emf.internal.ecore.EObjectComparator
#pragma once

#include <vector>

namespace emf::common {
class EObject;
}

namespace emf::sphinx::internal::ecore {

class EObjectComparator {
public:
    static int compare(emf::common::EObject* a, emf::common::EObject* b);
    static void sort(std::vector<emf::common::EObject*>& objects);
};

}  // namespace emf::sphinx::internal::ecore
