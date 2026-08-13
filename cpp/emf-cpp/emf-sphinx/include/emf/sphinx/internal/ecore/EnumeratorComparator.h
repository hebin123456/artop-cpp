// EnumeratorComparator.h
// 对齐 Java org.eclipse.sphinx.emf.internal.ecore.EnumeratorComparator
#pragma once

#include <vector>

namespace emf::common {
class Enumerator;
}

namespace emf::sphinx::internal::ecore {

class EnumeratorComparator {
public:
    static int compare(emf::common::Enumerator* a, emf::common::Enumerator* b);
    static void sort(std::vector<emf::common::Enumerator*>& objects);
};

}  // namespace emf::sphinx::internal::ecore
