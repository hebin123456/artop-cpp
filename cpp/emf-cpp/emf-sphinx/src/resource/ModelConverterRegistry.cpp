// ModelConverterRegistry.cpp
// 对齐 Java org.eclipse.sphinx.emf.resource.ModelConverterRegistry
#include "emf/sphinx/resource/ModelConverterRegistry.h"
#include "emf/sphinx/metamodel/IMetaModelDescriptor.h"

namespace emf::sphinx::resource {

void ModelConverterRegistry::addConverter(IModelConverter* converter) {
    if (!converter) return;
    // 避免重复
    for (auto* c : converters_) {
        if (c == converter) return;
    }
    converters_.push_back(converter);
}

void ModelConverterRegistry::removeConverter(IModelConverter* converter) {
    for (auto it = converters_.begin(); it != converters_.end(); ++it) {
        if (*it == converter) {
            converters_.erase(it);
            return;
        }
    }
}

IModelConverter* ModelConverterRegistry::findConverter(const std::string& sourceMM, const std::string& targetMM) const {
    for (auto* c : converters_) {
        if (!c) continue;
        auto* src = c->getSourceMetaModelDescriptor();
        auto* tgt = c->getTargetMetaModelDescriptor();
        if (src && tgt
            && src->getIdentifier() == sourceMM
            && tgt->getIdentifier() == targetMM) {
            return c;
        }
    }
    return nullptr;
}

}  // namespace emf::sphinx::resource
