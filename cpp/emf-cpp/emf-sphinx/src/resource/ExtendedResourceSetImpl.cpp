#include "emf/sphinx/resource/ExtendedResourceSetImpl.h"
namespace emf::sphinx::resource {
emf::common::EObject* ExtendedResourceSetImpl::getEObjectInScope(const emf::common::URI& uri, bool loadOnDemand) {
    return getEObject(uri, loadOnDemand);
}
emf::common::Resource* ExtendedResourceSetImpl::getResourceInScope(const emf::common::URI& uri, bool loadOnDemand) {
    return getResource(uri, loadOnDemand);
}
}
