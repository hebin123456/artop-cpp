// EcoreComparator.h
// 对齐 Java org.eclipse.sphinx.emf.ecore.EcoreComparator
// EObject 比较器（按 name / URI / id）
#pragma once

#include <string>
#include <vector>

namespace emf::common {
class EObject;
}

namespace emf::sphinx::ecore {

class EcoreComparator {
public:
    EcoreComparator() = default;
    virtual ~EcoreComparator() = default;

    virtual int compare(emf::common::EObject* a, emf::common::EObject* b);

    // 排序
    void sort(std::vector<emf::common::EObject*>& objects);
};

}  // namespace emf::sphinx::ecore
