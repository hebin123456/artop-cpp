// AbstractModelConverter.h
// 对齐 Java org.eclipse.sphinx.emf.resource.AbstractModelConverter
#pragma once

#include "emf/sphinx/resource/IModelConverter.h"

namespace emf::sphinx::resource {

class AbstractModelConverter : public IModelConverter {
public:
    AbstractModelConverter() = default;
    ~AbstractModelConverter() override = default;
};

}  // namespace emf::sphinx::resource
