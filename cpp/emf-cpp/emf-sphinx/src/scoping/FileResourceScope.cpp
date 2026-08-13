// FileResourceScope.cpp
// 对齐 Java org.eclipse.sphinx.emf.scoping.FileResourceScope
#include "emf/sphinx/scoping/FileResourceScope.h"
#include "emf/common/Resource.h"

namespace emf::sphinx::scoping {

bool FileResourceScope::belongsTo(emf::common::Resource* res, bool) const {
    if (!res) return false;
    return res->getURI() == fileUri_;
}

}  // namespace emf::sphinx::scoping
