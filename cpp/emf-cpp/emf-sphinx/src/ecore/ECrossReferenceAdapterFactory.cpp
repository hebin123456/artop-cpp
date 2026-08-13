// ECrossReferenceAdapterFactory.cpp - 对齐 Java org.eclipse.sphinx.emf.ecore.ECrossReferenceAdapterFactory
#include "emf/sphinx/ecore/ECrossReferenceAdapterFactory.h"
#include "emf/common/AdapterFactory.h"
#include "emf/common/ENotifier.h"

namespace emf::sphinx::ecore {

bool ECrossReferenceAdapterFactory::isFactoryForType(const std::any& /*type*/) const { return true; }
emf::common::Adapter* ECrossReferenceAdapterFactory::createAdapter(emf::common::Notifier* /*target*/) { return nullptr; }
emf::common::Adapter* ECrossReferenceAdapterFactory::adapt(emf::common::Notifier* target, emf::common::Adapter* existing) {
    if (existing) return existing;
    return createAdapter(target);
}

}  // namespace emf::sphinx::ecore
