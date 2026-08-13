// EcoreComparator.cpp - 对齐 Java org.eclipse.sphinx.emf.ecore.EcoreComparator
#include "emf/sphinx/ecore/EcoreComparator.h"
#include "emf/common/EObject.h"
#include "emf/ecore/EcorePackage.h"
#include <algorithm>

namespace emf::sphinx::ecore {

int EcoreComparator::compare(emf::common::EObject* a, emf::common::EObject* b) {
    if (!a && !b) return 0;
    if (!a) return -1;
    if (!b) return 1;
    auto* ca = a->eClass();
    auto* cb = b->eClass();
    if (!ca || !cb) return 0;
    return ca->getName().compare(cb->getName());
}

void EcoreComparator::sort(std::vector<emf::common::EObject*>& objects) {
    EcoreComparator cmp;
    std::sort(objects.begin(), objects.end(), [&](auto* a, auto* b) { return cmp.compare(a, b) < 0; });
}

}  // namespace emf::sphinx::ecore
