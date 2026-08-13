// OldResourceProviderAdapter.cpp
// 对齐 Java org.eclipse.sphinx.emf.resource.OldResourceProviderAdapter
#include "emf/sphinx/resource/OldResourceProviderAdapter.h"

namespace emf::sphinx::resource {

void OldResourceProviderAdapter::cacheOldContent(emf::common::Resource* /*res*/) {}
std::vector<emf::common::EObject*> OldResourceProviderAdapter::getOldContents(emf::common::Resource* /*res*/) { return {}; }
std::string OldResourceProviderAdapter::getOldURIFragment(emf::common::EObject* /*obj*/) { return ""; }
emf::common::EObject* OldResourceProviderAdapter::getOldEObject(const emf::common::URI& /*uri*/) { return nullptr; }
void OldResourceProviderAdapter::clearCache(emf::common::Resource* /*res*/) {}

}  // namespace emf::sphinx::resource
