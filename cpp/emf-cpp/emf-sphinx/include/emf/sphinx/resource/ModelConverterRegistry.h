// ModelConverterRegistry.h
// 对齐 Java org.eclipse.sphinx.emf.resource.ModelConverterRegistry
// 单例 - 跟踪所有 IModelConverter
#pragma once

#include "emf/sphinx/resource/IModelConverter.h"
#include <vector>
#include <string>

namespace emf::sphinx::resource {

class ModelConverterRegistry {
public:
    static ModelConverterRegistry& instance() {
        static ModelConverterRegistry inst;
        return inst;
    }

    void addConverter(IModelConverter* converter);
    void removeConverter(IModelConverter* converter);
    IModelConverter* findConverter(const std::string& sourceMM, const std::string& targetMM) const;
    std::vector<IModelConverter*> getAllConverters() const { return converters_; }

private:
    ModelConverterRegistry() = default;
    std::vector<IModelConverter*> converters_;
};

}  // namespace emf::sphinx::resource
